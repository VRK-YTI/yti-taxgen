package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.MetricDomain
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TranslatedText
import fi.vm.yti.taxgen.rdsdpmmapper.ext.kotlin.replaceOrAddItemByUri
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionMember
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionType
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsMemberValueType
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.CodeListSourceReader

internal fun mapAndValidateMetricDomain(
    codeListSource: CodeListSourceReader?,
    owner: Owner,
    diagnostic: Diagnostic
): List<MetricDomain> {
    codeListSource ?: return emptyList()

    val domainConcept = codeListSource
        .codeListMeta()
        .dpmConcept(owner)
        .copy(
            label = TranslatedText(mapOf(Language.byIso6391CodeOrFail("en") to "Metrics")),
            description = TranslatedText.empty()
        )

    val metrics = mapMetrics(
        codeListSource,
        owner,
        diagnostic
    )

    val hierarchies = mapAndValidateHierarchies(
        codeListSource,
        listOf(RdsExtensionType.DefinitionHierarchy),
        owner,
        diagnostic
    )

    val domain = MetricDomain(
        uri = "MET",
        concept = domainConcept,
        domainCode = "MET",
        metrics = metrics,
        hierarchies = hierarchies
    )

    val domains = listOf(domain)

    validateDpmElements(diagnostic, domains)

    return domains
}

private fun mapMetrics(
    codeListSource: CodeListSourceReader?,
    owner: Owner,
    diagnostic: Diagnostic
): List<Metric> {
    if (codeListSource == null) return emptyList()

    val metricItems = mutableListOf<MetricItem>()

    //Base details
    codeListSource.eachCode { code ->
        val metricItem = MetricItem(
            uri = code.validUri(diagnostic),
            concept = code.dpmConcept(owner),
            metricCodeValue = code.codeValueOrEmpty(),
            dataType = "",
            flowType = null,
            balanceType = null,
            referencedDomainCode = null,
            referencedHierarchyCode = null
        )

        metricItems.add(metricItem)
    }

    //Extension based details
    codeListSource.eachExtensionSource { extensionSource ->
        val extensionMetadata = extensionSource.extensionMetaData()

        if (extensionMetadata.isType(RdsExtensionType.DpmMetric)) {

            extensionSource.eachExtensionMember { extensionMember ->
                val codeUri = extensionMember.validCodeUri(diagnostic)
                val metricItem = metricItems.find { it.uri == codeUri }

                if (metricItem != null) {

                    val updatedItem = metricItem.copy(
                        dataType = extensionMember.mappedMetricDataType(),
                        flowType = extensionMember.mappedMetricFlowTypeOrNull(),
                        balanceType = extensionMember.nonEmptyStringValueOrNull(RdsMemberValueType.DpmBalanceType),
                        referencedDomainCode = extensionMember.nonEmptyStringValueOrNull(RdsMemberValueType.DpmDomainReference),
                        referencedHierarchyCode = extensionMember.nonEmptyStringValueOrNull(RdsMemberValueType.DpmHierarchyReference)
                    )

                    metricItems.replaceOrAddItemByUri(updatedItem)
                }
            }
        }
    }

    val metrics = metricItems.map { it.toMetric() }

    validateDpmElements(diagnostic, metrics)

    return metrics
}

private val RDS_METRIC_DATA_TYPE_TO_DPM = mapOf(
    "Enumeration" to "Enumeration/Code",
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

private fun RdsExtensionMember.mappedMetricDataType(): String {
    val sourceVal = stringValueOrEmpty(RdsMemberValueType.DpmMetricDataType)
    val mappedVal = RDS_METRIC_DATA_TYPE_TO_DPM[sourceVal]

    return mappedVal ?: sourceVal
}

private val RDS_METRIC_FLOW_TYPE_TO_DPM = mapOf(
    "Instant" to "Stock",
    "Duration" to "Flow"
)

private fun RdsExtensionMember.mappedMetricFlowTypeOrNull(): String? {
    val sourceVal = nonEmptyStringValueOrNull(RdsMemberValueType.DpmFlowType)
    val mappedVal = RDS_METRIC_FLOW_TYPE_TO_DPM[sourceVal]

    return mappedVal ?: sourceVal
}
