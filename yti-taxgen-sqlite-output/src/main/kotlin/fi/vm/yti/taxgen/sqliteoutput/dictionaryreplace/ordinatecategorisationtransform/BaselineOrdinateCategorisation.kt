package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.validation.Validatable
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationSubjectDescriptor
import fi.vm.yti.taxgen.dpmmodel.validators.validateCustom
import fi.vm.yti.taxgen.sqliteoutput.tables.OrdinateCategorisationTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

data class BaselineOrdinateCategorisation(
    val ordinateId: EntityID<Int>?,

    // Tokenized from mOrdinateCategorisation.DimensionMemberSignature
    val databaseIdSignature: OrdinateCategorisationSignature,

    // Tokenized from mOrdinateCategorisation.DPS
    val xbrlCodeSignature: OrdinateCategorisationSignature,

    val source: String?
) : Validatable {

    companion object {

        fun fromRow(
            row: ResultRow,
            diagnostic: Diagnostic
        ): BaselineOrdinateCategorisation {

            val databaseIdSignature = tokenizeSignature(
                row,
                OrdinateCategorisationTable.dimensionMemberSignatureCol,
                OrdinateCategorisationSignature.IdentifierKind.DATABASE_ID,
                diagnostic
            )

            val xbrlCodeSignature = tokenizeSignature(
                row,
                OrdinateCategorisationTable.dpsCol,
                OrdinateCategorisationSignature.IdentifierKind.XBRL_CODE,
                diagnostic
            )

            return BaselineOrdinateCategorisation(
                ordinateId = row[OrdinateCategorisationTable.ordinateIdCol],
                databaseIdSignature = databaseIdSignature,
                xbrlCodeSignature = xbrlCodeSignature,
                source = row[OrdinateCategorisationTable.sourceCol]
            )
        }

        private fun tokenizeSignature(
            row: ResultRow,
            column: Column<String?>,
            identifierKind: OrdinateCategorisationSignature.IdentifierKind,
            diagnostic: Diagnostic
        ): OrdinateCategorisationSignature {
            val signatureLiteral = row[column] ?: diagnostic.fatal("Empty OrdinateCategorisation signature")

            val signatureMatch = SIGNATURE_PATTERN.matchEntire(signatureLiteral)
                ?: diagnostic.fatal("Unsupported signature in OrdinateCategorisation.${column.name}: $signatureLiteral")

            fun signatureElementValueOrNull(partName: String) =
                (signatureMatch.groups as MatchNamedGroupCollection)[partName]?.value

            fun signatureElementValue(partName: String) = signatureElementValueOrNull(partName)
                ?: thisShouldNeverHappen("SignaturePattern configuration mismatch")

            return when {
                signatureElementValueOrNull("dimension") != null -> {
                    OrdinateCategorisationSignature(
                        identifierKind = identifierKind,
                        signatureStructure = OrdinateCategorisationSignatureStructure.NO_OPEN_AXIS_VALUE_RESTRICTION,
                        dimensionIdentifier = signatureElementValue("dimension"),
                        memberIdentifier = signatureElementValue("member"),
                        hierarchyIdentifier = null,
                        hierarchyStartingMemberIdentifier = null,
                        startingMemberIncluded = null
                    )
                }

                (signatureElementValueOrNull("partialOavrDimension") != null) -> {
                    OrdinateCategorisationSignature(
                        identifierKind = identifierKind,
                        signatureStructure = OrdinateCategorisationSignatureStructure.PARTIAL_OPEN_AXIS_VALUE_RESTRICTION,
                        dimensionIdentifier = signatureElementValue("partialOavrDimension"),
                        memberIdentifier = signatureElementValue("partialOavrMember"),
                        hierarchyIdentifier = signatureElementValue("partialOavrHierarchy"),
                        hierarchyStartingMemberIdentifier = null,
                        startingMemberIncluded = null
                    )
                }

                (signatureElementValueOrNull("oavrDimension") != null) -> {
                    OrdinateCategorisationSignature(
                        identifierKind = identifierKind,
                        signatureStructure = OrdinateCategorisationSignatureStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION,
                        dimensionIdentifier = signatureElementValue("oavrDimension"),
                        memberIdentifier = signatureElementValue("oavrMember"),
                        hierarchyIdentifier = signatureElementValue("oavrHierarchy"),
                        hierarchyStartingMemberIdentifier = signatureElementValue("oavrStartMember"),
                        startingMemberIncluded = signatureElementValue("oavrStartMemberIncluded")
                    )
                }

                else -> {
                    thisShouldNeverHappen("Signature tokenizer mismatch.")
                }
            }
        }

        private val SIGNATURE_PATTERN =
            """
            \A

            (?<dimension>[^\(\)]+)
                \(
                (?<member>[^\(\)\[\]]+)
                \)

            |

            (?<oavrDimension>[^\(\)]+)
                \(
                (?<oavrMember>[^\(\)\[\]]+)
                    \[
                    (?<oavrHierarchy>[^\(\)\[\];]+)
                    ;
                    (?<oavrStartMember>[^\(\)\[\];]+)
                    ;
                    (?<oavrStartMemberIncluded>[^\(\)\[\];]+)
                    \]
                \)
            |

            (?<partialOavrDimension>[^\(\)]+)
                \(
                (?<partialOavrMember>[^\(\)\[\]\?]+)
                    \?\[
                    (?<partialOavrHierarchy>[^\(\)\[\]]+)
                    \]
                \)
            \z
            """.trimIndent().toRegex(RegexOption.COMMENTS)
    }

    override fun validate(validationResultBuilder: ValidationResultBuilder) {
        databaseIdSignature.validate(validationResultBuilder)
        xbrlCodeSignature.validate(validationResultBuilder)

        validateCustom(
            validationResultBuilder = validationResultBuilder,
            valueName = "DimensionMemberSignature, DPS",
            validate = { errorReporter ->
                val mismatchDescriptions = checkSignaturesMatching()

                if (mismatchDescriptions.any()) {
                    errorReporter.error(
                        reason = "Signatures do not match",
                        value = mismatchDescriptions.joinToString()
                    )
                }
            }
        )
    }

    override fun validationSubjectDescriptor(): ValidationSubjectDescriptor {
        return ValidationSubjectDescriptor(
            subjectType = "OrdinateCategorisation (baseline)",
            subjectIdentifier = "OrdinateID: $ordinateId"
        )
    }

    private fun checkSignaturesMatching(): List<String> {
        val descriptions = mutableListOf<String>()

        fun checkSignatureElementsMatching(valueA: String?, valueB: String?, elementDescription: String) {
            if (valueA != valueB) {
                descriptions.add(elementDescription)
            }
        }

        checkSignatureElementsMatching(
            databaseIdSignature.memberIdentifier,
            xbrlCodeSignature.memberIdentifier,
            "Members not same"
        )
        checkSignatureElementsMatching(
            databaseIdSignature.dimensionIdentifier,
            xbrlCodeSignature.dimensionIdentifier,
            "Dimensions not same"
        )

        checkSignatureElementsMatching(
            databaseIdSignature.lookupHierarchyCodeForHierarchyIdentifier(),
            xbrlCodeSignature.hierarchyIdentifier,
            "Hierarchies not same"
        )

        checkSignatureElementsMatching(
            databaseIdSignature.lookupMemberCodeForHierarchyStartingMemberIdentifier(),
            xbrlCodeSignature.hierarchyStartingMemberIdentifier,
            "Hierarchy starting members not same"
        )

        checkSignatureElementsMatching(
            databaseIdSignature.startingMemberIncluded,
            xbrlCodeSignature.startingMemberIncluded,
            "Starting member inclusion not same"
        )

        return descriptions
    }
}
