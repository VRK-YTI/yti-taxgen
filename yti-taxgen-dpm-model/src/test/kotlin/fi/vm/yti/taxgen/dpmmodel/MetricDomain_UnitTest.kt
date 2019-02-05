package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.datafactory.Factory
import fi.vm.yti.taxgen.dpmmodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class MetricDomain_UnitTest :
    DpmModel_UnitTestBase<MetricDomain>(MetricDomain::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "uri,                   required",
        "concept,               required",
        "domainCode,            required",
        "metrics,               required",
        "hierarchies,           required"
    )
    fun testPropertyOptionality(
        propertyName: String,
        expectedOptionality: String
    ) {
        propertyOptionalityTemplate(
            propertyName = propertyName,
            expectedOptionality = expectedOptionality
        )
    }

    @DisplayName("Property length validation")
    @ParameterizedTest(name = "{0} {1} should be {2}")
    @CsvSource(
        "uri,                   minLength,      1",
        "uri,                   maxLength,      500",
        "domainCode,            minLength,      2",
        "domainCode,            maxLength,      50",
        "metrics,               maxColLength,   10000",
        "hierarchies,           maxColLength,   10000"
    )
    fun testPropertyLengthValidation(
        propertyName: String,
        validationType: String,
        expectedLimit: Int
    ) {
        propertyLengthValidationTemplate(
            propertyName = propertyName,
            validationType = validationType,
            expectedLimit = expectedLimit,
            customValueBuilder = { property, length ->
                if (property.name == "metrics") {
                    mapOf("metrics" to List(length) { index -> metric("$index") })
                } else if (property.name == "hierarchies") {
                    mapOf("hierarchies" to List(length) { index -> hierarchy("$index") })
                } else {
                    emptyMap()
                }
            }
        )
    }

    @Nested
    inner class ConceptProp {

        @Test
        fun `concept should error if invalid`() {
            attributeOverrides(
                "concept" to Factory.instantiateWithOverrides<Concept>(
                    "label" to TranslatedText(emptyMap())
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("Concept.label: has too few translations (minimum 1)")
        }
    }

    @Nested
    inner class MetricsProp {

        @Test
        fun `metrics should have unique ids and metricCodes`() {
            attributeOverrides(
                "metrics" to listOf(
                    metric("m_1"),
                    metric("m_2"),
                    metric("m_2"),
                    metric("m_4")
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactlyInAnyOrder(
                    "MetricDomain.metrics: duplicate uri value 'met_m_2_uri'",
                    "MetricDomain.metrics: duplicate metricCode value 'met_m_2_code'"
                )
        }
    }

    @Nested
    inner class HierarchiesProp {

        @Test
        fun `hierarchies should have unique ids and hierarchyCodes`() {
            attributeOverrides(
                "hierarchies" to listOf(
                    hierarchy("h_1"),
                    hierarchy("h_2"),
                    hierarchy("h_2"),
                    hierarchy("h_4")
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly(
                    "MetricDomain.hierarchies: duplicate hierarchyCode value 'hierarchy_h_2_code'",
                    "MetricDomain.hierarchies: duplicate uri value 'hierarchy_h_2_uri'"
                )
        }

        @Test
        fun `hierarchies should refer only Metrics which are from the Domain itself`() {

            attributeOverrides(
                "metrics" to listOf(
                    metric("m_1"),
                    metric("m_2")
                ),

                "hierarchies" to listOf(
                    hierarchy(
                        "h_1",
                        hierarchyNode(
                            "hn_1",
                            "met_m_1_uri"
                        ),

                        hierarchyNode(
                            "hn_1.2",
                            "met_m_2_uri",

                            hierarchyNode(
                                "hn_1.3",
                                "met_m_3_uri" //External
                            )
                        )

                    ),

                    hierarchy(
                        "h_2",
                        hierarchyNode(
                            "hn_2",
                            "met_m_1_uri",

                            hierarchyNode(
                                "hn_2.1",
                                "met_m_2_uri",

                                hierarchyNode(
                                    "hn_2.2",
                                    "met_m_4_uri", //External

                                    hierarchyNode(
                                        "hn_2.3",
                                        "met_m_5_uri" //External
                                    )
                                )
                            )
                        )
                    )
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly(
                    "MetricDomain.hierarchies: DPM HierarchyNode hierarchy_node_hn_1.3_uri refers to DPM Metric met_m_3_uri which is not part of the containing DPM MetricDomain.",
                    "MetricDomain.hierarchies: DPM HierarchyNode hierarchy_node_hn_2.2_uri refers to DPM Metric met_m_4_uri which is not part of the containing DPM MetricDomain.",
                    "MetricDomain.hierarchies: DPM HierarchyNode hierarchy_node_hn_2.3_uri refers to DPM Metric met_m_5_uri which is not part of the containing DPM MetricDomain."
                )
        }
    }
}