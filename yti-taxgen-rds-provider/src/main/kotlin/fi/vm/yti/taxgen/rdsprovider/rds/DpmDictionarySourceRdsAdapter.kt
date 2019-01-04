package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.config.DpmDictionarySourceConfig

internal class DpmDictionarySourceRdsAdapter(
    private val config: DpmDictionarySourceConfig,
    private val diagnostic: Diagnostic
) : DpmDictionarySource {

    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = ""

    override fun dpmOwnerConfigData(action: (String) -> Unit) {
        action(JsonOps.writeAsJsonString(config.owner))
    }

    override fun metricsSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForUri(
                config.metrics.uri,
                CodeListBlueprint.metrics()
            )
        )
    }

    override fun explicitDomainsAndHierarchiesSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForUri(
                config.explicitDomainsAndHierarchies.uri,
                CodeListBlueprint.explicitDomainsAndHierarchies()
            )
        )
    }

    override fun explicitDimensionsSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForUri(
                config.explicitDimensions.uri,
                CodeListBlueprint.explicitOrTypedDimensions()
            )
        )
    }

    override fun typedDomainsSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForUri(
                config.typedDomains.uri,
                CodeListBlueprint.typedDomains()
            )
        )
    }

    override fun typedDimensionsSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForUri(
                config.typedDimensions.uri,
                CodeListBlueprint.explicitOrTypedDimensions()
            )
        )
    }

    private fun codeListSourceOrNullForUri(
        uri: String?,
        blueprint: CodeListBlueprint
    ): CodeListSource? {
        return if (uri != null) {
            CodeListSourceRdsAdapter(
                codeListUri = uri,
                blueprint = blueprint,
                diagnostic = diagnostic
            )
        } else {
            null
        }
    }
}
