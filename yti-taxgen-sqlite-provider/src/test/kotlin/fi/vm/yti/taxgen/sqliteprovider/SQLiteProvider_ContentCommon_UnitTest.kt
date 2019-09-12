package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest

internal class SQLiteProvider_ContentCommon_UnitTest : SQLiteProvider_ContentDynamicUnitTestBase() {

    override fun createDynamicTests(): List<DynamicNode> {

        return listOf(
            dynamicTest("should have all configured languages") {

                val rs = dbConnection.createStatement().executeQuery("SELECT IsoCode FROM mLanguage")
                val dbIsoCodes = rs.toStringList(false)

                val allKnownIsoCodes = Language.languages().map { it.iso6391Code }.toList()
                assertThat(dbIsoCodes).containsExactlyInAnyOrderElementsOf(allKnownIsoCodes)

                assertThat(dbIsoCodes).size().isEqualTo(24)
            },

            dynamicTest("should have English language but no Concept nor ConceptTranslation relations") {

                val rs = dbConnection.createStatement().executeQuery(
                    """
                    SELECT
                        L.IsoCode AS LanguageIsoCode,
                        L.LanguageName,
                        L.EnglishName AS LanguageEnglishName,
                        C.ConceptType,
                        C.OwnerID,
                        C.CreationDate,
                        C.ModificationDate,
                        C.FromDate,
                        C.ToDate,
                        T.Role,
                        TL.IsoCode,
                        T.Text
                    FROM mLanguage AS L
                    LEFT JOIN mConcept AS C ON C.ConceptID = L.ConceptID
                    LEFT JOIN mConceptTranslation AS T ON T.ConceptID = L.ConceptID
                    LEFT JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                    WHERE L.IsoCode = 'en'
                    """
                )

                assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                    "#LanguageIsoCode, #LanguageName, #LanguageEnglishName, #ConceptType, #OwnerID, #CreationDate, #ModificationDate, #FromDate, #ToDate, #Role, #IsoCode, #Text",
                    "en, English, English, nil, nil, nil, nil, nil, nil, nil, nil, nil"
                )
            },

            dynamicTest("should have Owner") {

                val rs = dbConnection.createStatement().executeQuery(
                    """
                    SELECT
                        O.OwnerName,
                        O.OwnerNamespace,
                        O.OwnerLocation,
                        O.OwnerPrefix,
                        O.OwnerCopyright,
                        O.ParentOwnerID,
                        O.ConceptID
                    FROM mOwner AS O
                    """
                )

                if (initMode == DbInitMode.DICTIONARY_CREATE) {
                    assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                        "#OwnerName, #OwnerNamespace, #OwnerLocation, #OwnerPrefix, #OwnerCopyright, #ParentOwnerID, #ConceptID",
                        "FixName, FixNSpace, FixLoc, FixPrfx, FixCop, nil, nil",
                        "EuroFiling, http://www.eurofiling.info/xbrl/, http://www.eurofiling.info/eu/fr/xbrl/, eu, (C) Eurofiling, nil, nil"
                    )
                }

                if (initMode == DbInitMode.DICTIONARY_REPLACE) {
                    assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                        "#OwnerName, #OwnerNamespace, #OwnerLocation, #OwnerPrefix, #OwnerCopyright, #ParentOwnerID, #ConceptID",
                        "FixName, FixNSpace_Existing_In_DB, FixLoc, FixPrfx, FixCop, nil, nil",
                        "EuroFiling, http://www.eurofiling.info/xbrl/, http://www.eurofiling.info/eu/fr/xbrl/, eu, (C) Eurofiling, nil, nil"
                    )
                }
            },

            dynamicTest("should produce proper context events") {

                assertThat(diagnosticCollector.eventsString()).contains(
                    "ENTER [SQLiteDbWriter]",
                    "EXIT [SQLiteDbWriter]"
                )
            }
        )
    }
}
