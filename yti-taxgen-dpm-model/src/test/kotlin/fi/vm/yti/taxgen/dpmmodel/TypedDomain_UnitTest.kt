package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.datafactory.Factory
import fi.vm.yti.taxgen.dpmmodel.exception.throwIllegalDpmModelState
import fi.vm.yti.taxgen.dpmmodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class TypedDomain_UnitTest :
    DpmModel_UnitTestBase<TypedDomain>(TypedDomain::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1} property")
    @CsvSource(
        "uri,                   required",
        "concept,               required",
        "domainCode,            required"
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
        "domainCode,            maxLength,      50"
    )
    fun testPropertyLengthValidation(
        propertyName: String,
        validationType: String,
        expectedLimit: Int
    ) {
        propertyLengthValidationTemplate(
            propertyName = propertyName,
            validationType = validationType,
            expectedLimit = expectedLimit
        )
    }

    @Nested
    inner class ConceptProp {

        @Test
        fun `concept should produce validation error when it is not valid`() {
            attributeOverrides(
                "concept" to Factory.instantiateWithOverrides<Concept>(
                    "label" to TranslatedText(emptyMap())
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly(
                "[TypedDomain] [typ_dom_uri] [Concept.Label] [Too few translations (minimum 1)]"
            )
        }
    }

    @Nested
    inner class DataTypeProp {

        @ParameterizedTest(name = "`{0}` should be {1} dataType")
        @CsvSource(
            "Boolean,       valid",
            "Date,          valid",
            "Integer,       valid",
            "Monetary,      valid",
            "Percent,       valid",
            "String,        valid",
            "Decimal,       valid",
            "Lei,           valid",
            "Isin,          valid",
            "'',            invalid",
            "null,          invalid",
            "foo,           invalid"
        )
        fun testDataTypeValidation(
            dataType: String,
            expectedValidity: String
        ) {
            attributeOverrides(
                "dataType" to dataType
            )

            instantiateAndValidate()

            when (expectedValidity) {
                "valid" -> assertThat(validationErrors).isEmpty()
                "invalid" -> assertThat(validationErrors).containsExactly("[TypedDomain] [typ_dom_uri] [DataType] [Unsupported value] [$dataType]")
                else -> throwIllegalDpmModelState()
            }
        }
    }
}
