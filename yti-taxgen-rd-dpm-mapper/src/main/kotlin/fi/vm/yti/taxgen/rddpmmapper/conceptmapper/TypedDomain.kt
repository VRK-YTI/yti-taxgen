package fi.vm.yti.taxgen.rddpmmapper.conceptmapper

import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rddpmmapper.conceptitem.TypedDomainItem
import fi.vm.yti.taxgen.rddpmmapper.conceptitem.UriIdentifiedItemCollection
import fi.vm.yti.taxgen.rddpmmapper.modelmapper.CodeListModelMapper
import fi.vm.yti.taxgen.rddpmmapper.rdsmodel.RdsExtensionMember
import fi.vm.yti.taxgen.rddpmmapper.rdsmodel.RdsExtensionType
import fi.vm.yti.taxgen.rddpmmapper.rdsmodel.RdsMemberValueType

internal fun mapAndValidateTypedDomains(
    codeListSource: CodeListModelMapper?,
    owner: Owner,
    diagnostic: Diagnostic
): List<TypedDomain> {
    codeListSource ?: return emptyList()

    val typedDomainItems = UriIdentifiedItemCollection<TypedDomainItem>()

    // Base details
    codeListSource.eachCode { code ->
        val typedDomainItem = TypedDomainItem(
            uri = code.validUri(diagnostic),
            concept = code.dpmConcept(owner),
            domainCode = code.codeValueOrEmpty(),
            dataType = ""
        )

        typedDomainItems.addItem(typedDomainItem)
    }

    // Extension based details
    codeListSource.eachExtensionModelMapper { extensionSource ->
        val extensionMetadata = extensionSource.extensionMetaData()

        if (extensionMetadata.isType(RdsExtensionType.DpmTypedDomain)) {

            extensionSource.eachExtensionMember { extensionMember ->
                val codeUri = extensionMember.validCodeUri(diagnostic)
                val typedDomain = typedDomainItems.findByUri(codeUri)

                if (typedDomain != null) {

                    val updatedTypedDomain = typedDomain.copy(
                        dataType = extensionMember.mappedDomainDataType()
                    )

                    typedDomainItems.replaceOrAddItemByUri(updatedTypedDomain)
                }
            }
        }
    }

    val typedDomains = typedDomainItems.itemsList().map { it.toTypedDomain() }

    diagnostic.validate(typedDomains)

    return typedDomains
}

private val RDS_DOMAIN_DATA_TYPE_TO_DPM = mapOf(
    "Boolean" to "Boolean",
    "Date" to "Date",
    "Integer" to "Integer",
    "Monetary" to "Monetary",
    "Percentage" to "Percent",
    "String" to "String",
    "Decimal" to "Decimal",
    "Lei" to "Lei",
    "Isin" to "Isin"
)

private fun RdsExtensionMember.mappedDomainDataType(): String {
    val sourceVal = stringValueOrEmpty(RdsMemberValueType.DpmDomainDataType)
    val mappedVal = RDS_DOMAIN_DATA_TYPE_TO_DPM[sourceVal]

    return mappedVal ?: sourceVal
}
