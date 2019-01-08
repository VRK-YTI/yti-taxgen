package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import fi.vm.yti.taxgen.rdsdpmmapper.ext.kotlin.replaceOrAddByUri
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionType
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsMemberValueType
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.CodeListSourceReader

internal fun mapAndValidateTypedDimensions(
    codeListSource: CodeListSourceReader?,
    owner: Owner,
    diagnostic: Diagnostic
): List<TypedDimension> {
    val dimensions = mapDimensions(codeListSource, owner, diagnostic).map { it.toTypedDimension() }

    diagnostic.validate(dimensions)

    return dimensions
}

internal fun mapAndValidateExplicitDimensions(
    codeListSource: CodeListSourceReader?,
    owner: Owner,
    diagnostic: Diagnostic
): List<ExplicitDimension> {
    val dimensions = mapDimensions(codeListSource, owner, diagnostic).map { it.toExplicitDimension() }

    diagnostic.validate(dimensions)

    return dimensions
}

private fun mapDimensions(
    codeListSource: CodeListSourceReader?,
    owner: Owner,
    diagnostic: Diagnostic
): List<DimensionItem> {
    val dimensionItems = mutableListOf<DimensionItem>()

    if (codeListSource == null) return dimensionItems

    //Base details
    codeListSource.eachCode { code ->
        val typedDomain = DimensionItem(
            uri = code.validUri(diagnostic),
            concept = code.dpmConcept(owner),
            dimensionCode = code.codeValueOrEmpty(),
            referencedDomainCode = ""
        )

        dimensionItems.add(typedDomain)
    }

    //Extension based details
    codeListSource.eachExtensionSource { extensionSource ->
        val extensionMetadata = extensionSource.extensionMetaData()

        if (extensionMetadata.isType(RdsExtensionType.DpmDimension)) {

            extensionSource.eachExtensionMember { extensionMember ->
                val codeUri = extensionMember.validCodeUri(diagnostic)
                val dimensionItem = dimensionItems.find { it.uri == codeUri }

                if (dimensionItem != null) {

                    val updatedDimensionItem = dimensionItem.copy(
                        referencedDomainCode = extensionMember.stringValueOrEmpty(RdsMemberValueType.DpmDomainReference)
                    )

                    dimensionItems.replaceOrAddByUri(updatedDimensionItem)
                }
            }
        }
    }

    return dimensionItems
}
