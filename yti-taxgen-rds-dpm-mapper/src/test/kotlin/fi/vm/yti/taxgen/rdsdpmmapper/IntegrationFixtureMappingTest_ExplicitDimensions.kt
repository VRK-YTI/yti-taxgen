package fi.vm.yti.taxgen.rdsdpmmapper

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@DisplayName("Mapping Integration Fixture to DPM model - ExplicitDimensions")
internal class IntegrationFixtureMappingTest_ExplicitDimensions
    : RdsToDpmMapper_UnitTestBase() {

    @Test
    fun `4 Explicit Dimensions`() {
        val dpmDictionary = performMappingFromIntegrationFixture()

        dpmDictionary.explicitDimensions.forEachIndexed { index, it ->
            when (index) {
                0 -> {
                    assertThat(it.dimensionCode).isEqualTo("DIM")

                    assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/taxgen-test-fixtures/exp-dims-2018-1/code/DIM")
                    assertThat(it.type).isEqualTo("ExplicitDimension")

                    assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(it.concept.applicableFrom).isEqualTo("2018-10-31")
                    assertThat(it.concept.applicableUntil).isNull()

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Dimension (fi, label)")
                    )

                    assertThat(it.concept.description.translations).containsOnly(
                        entry(fi, "Dimension (fi, description)")
                    )

                    assertThat(it.referencedDomainCode).isEqualTo("DOME")
                }

                1 -> {
                    assertThat(it.dimensionCode).isEqualTo("EDA-D1")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "EDA dimension 1")
                    )

                    assertThat(it.referencedDomainCode).isEqualTo("EDA")
                }

                2 -> {
                    assertThat(it.dimensionCode).isEqualTo("EDA-D2")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "EDA dimension 2")
                    )

                    assertThat(it.referencedDomainCode).isEqualTo("EDA")
                }

                3 -> {
                    assertThat(it.dimensionCode).isEqualTo("EDA-D10")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "EDA dimension 10")
                    )

                    assertThat(it.referencedDomainCode).isEqualTo("EDA")
                }

                else -> {
                    fail { "Unexpected item" }
                }
            }
        }
    }
}