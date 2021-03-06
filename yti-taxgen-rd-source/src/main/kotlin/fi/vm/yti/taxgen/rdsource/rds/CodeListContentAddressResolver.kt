package fi.vm.yti.taxgen.rdsource.rds

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.ext.jackson.arrayOrNullAt
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextAt
import fi.vm.yti.taxgen.commons.naturalsort.NumberAwareStringComparator
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rdsource.CodeListBlueprint
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

internal data class ContentAddress(
    val codeListUrl: HttpUrl,
    val codesUrl: HttpUrl,
    val extensionUrls: List<ExtensionAddress>
)

internal data class ExtensionAddress(
    val extensionUri: String,
    val extensionUrl: HttpUrl,
    val extensionMembersUrl: HttpUrl
)

internal class CodeListContentAddressResolver(
    codeLisUri: String,
    private val blueprint: CodeListBlueprint,
    private val rdsClient: RdsClient,
    private val diagnostic: Diagnostic
) {
    private val codeLisUri = codeLisUri.toHttpUrlOrNull() ?: diagnostic.fatal("Malformed URI")
    private val passthroughUriParams = resolveUriPassthroughParams()
    val contentAddress = resolveContentAddress()

    fun decorateUriWithInheritedParams(uri: String): String {
        val httpUrlBuilder = uri.toHttpUrlOrNull()?.newBuilder() ?: diagnostic.fatal("Malformed URI for decoration")

        passthroughUriParams.forEach { (name, values) ->
            values.forEach { value ->
                httpUrlBuilder.setQueryParameter(name, value)
            }
        }

        return httpUrlBuilder.build().toString()
    }

    private fun resolveUriPassthroughParams(): Map<String, List<String?>> {
        return arrayOf("env").map { name ->
            name to codeLisUri.queryParameterValues(name)
        }.toMap()
    }

    private fun resolveContentAddress(): ContentAddress {
        val metaDataJson = rdsClient.fetchJsonAsNodeTree(
            url = codeLisUri,
            extraQueryParams = emptyList(),
            requestPrettyJson = false
        )

        val httpUrl = metaDataJson.httpUrlAt(
            "/url",
            diagnostic,
            "Content URL resolution via URI"
        )

        val codeListJson = rdsClient.fetchJsonAsNodeTree(
            url = httpUrl,
            extraQueryParams = listOf(Pair("expand", "extension")),
            requestPrettyJson = false
        )

        val contentAddress = ContentAddress(
            codeListUrl = resolveCodeListContentUrl(codeListJson),
            codesUrl = resolveCodesContentUrl(codeListJson),
            extensionUrls = resolveExtensionContentAddress(codeListJson).sortedWith(
                compareBy(NumberAwareStringComparator.instance()) { it.extensionUri }
            )
        )

        return contentAddress
    }

    private fun resolveCodeListContentUrl(codeListJson: JsonNode): HttpUrl {
        return codeListJson
            .httpUrlAt("/url", diagnostic, "CodeList at content URL resolution")
            .newBuilder()
            .addQueryParameter("expand", "code")
            .build()
    }

    private fun resolveCodesContentUrl(codeListJson: JsonNode): HttpUrl {
        return codeListJson.httpUrlAt("/codesUrl", diagnostic, "Codes at content URL resolution")
    }

    private fun resolveExtensionContentAddress(codeListJson: JsonNode): List<ExtensionAddress> {
        return codeListJson.arrayOrNullAt("/extensions")?.mapNotNull { extensionNode ->

            val propertyTypeUri = extensionNode.nonBlankTextAt("/propertyType/uri", diagnostic)

            if (blueprint.extensionPropertyTypeUris.contains(propertyTypeUri)) {
                extensionAddressFromExtensionNode(extensionNode)
            } else {
                null
            }
        } ?: emptyList()
    }

    private fun extensionAddressFromExtensionNode(extensionNode: JsonNode): ExtensionAddress {
        return ExtensionAddress(
            extensionUri = extensionNode
                .nonBlankTextAt("/uri", diagnostic),
            extensionUrl = extensionNode
                .httpUrlAt("/url", diagnostic, "Extension at content URL resolution"),
            extensionMembersUrl = extensionNode
                .httpUrlAt("/membersUrl", diagnostic, "ExtensionMembers at content URL resolution")
                .newBuilder()
                .addQueryParameter(
                    "expand",
                    "memberValue"
                )
                .build()
        )
    }

    private fun JsonNode.httpUrlAt(jsonPtrExpr: String, diagnostic: Diagnostic, diagnosticName: String): HttpUrl {
        val rawUrl = nonBlankTextAt(jsonPtrExpr, diagnostic)
        return parseHttpUrlAt(rawUrl, diagnostic, diagnosticName)
    }

    private fun parseHttpUrlAt(rawUrl: String, diagnostic: Diagnostic, diagnosticName: String): HttpUrl {
        return rawUrl.toHttpUrlOrNull() ?: diagnostic.fatal("Malformed URL ($diagnosticName): $rawUrl")
    }
}
