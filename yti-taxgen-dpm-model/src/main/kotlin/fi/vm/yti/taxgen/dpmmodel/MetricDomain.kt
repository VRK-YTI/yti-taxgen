package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateCustom
import fi.vm.yti.taxgen.dpmmodel.validators.validateElementPropertyValuesUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength
import fi.vm.yti.taxgen.dpmmodel.validators.validateLengths

data class MetricDomain(
    override val uri: String,
    override val concept: Concept,
    val domainCode: String,
    val metrics: List<Metric>,
    val hierarchies: List<Hierarchy>
) : DpmElement {

    override fun validate(validationResults: ValidationResults) {

        super.validate(validationResults)

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = MetricDomain::domainCode,
            minLength = 2,
            maxLength = 50
        )

        validateLengths(
            validationResults = validationResults,
            instance = this,
            properties = listOf(MetricDomain::metrics, MetricDomain::hierarchies),
            minLength = 0,
            maxLength = 10000
        )

        validateElementPropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = MetricDomain::metrics,
            valueProperties = listOf(Metric::uri, Metric::metricCode)
        )

        validateElementPropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = MetricDomain::hierarchies,
            valueProperties = listOf(Hierarchy::uri, Hierarchy::hierarchyCode)
        )

        validateCustom( //TODO - abstract the validation & share with ExplicitDomain
            validationResults = validationResults,
            instance = this,
            propertyName = "hierarchies",
            validate = { messages ->
                val domainMetricUris = metrics.map { it.uri }.toSet()

                hierarchies.forEach { hierarchy ->
                    hierarchy.allNodes().forEach { node ->
                        if (!domainMetricUris.contains(node.referencedMemberUri)) {
                            messages.add(
                                "DPM HierarchyNode ${node.uri} refers to DPM Metric ${node.referencedMemberUri} which is not part of the containing DPM MetricDomain."
                            )
                        }
                    }
                }
            }
        )
    }

    override fun code(): String = domainCode
}