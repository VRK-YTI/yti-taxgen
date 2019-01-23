package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength

data class TypedDomain(
    override val uri: String,
    override val concept: Concept,
    val domainCode: String,
    val dataType: String
) : DpmElement {

    companion object {
        val VALID_DATA_TYPES = listOf(
            "Boolean",
            "Date",
            "Integer",
            "Monetary",
            "Percent",
            "String",
            "Decimal",
            "Lei",
            "Isin"
        )
    }

    override fun validate(validationResults: ValidationResults) {

        super.validate(validationResults)

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = TypedDomain::domainCode,
            minLength = 2,
            maxLength = 50
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = TypedDomain::dataType,
            condition = { VALID_DATA_TYPES.contains(dataType) },
            message = { "unsupported data type '$dataType'" }
        )
    }

    override fun code(): String = domainCode
}
