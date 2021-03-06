package fi.vm.yti.taxgen.sqliteoutput

import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest

internal class DpmDbWriter_ContentHierarchy_ModuleTest : DpmDbWriter_ContentModuleTestBase() {

    override fun createDynamicTests(): List<DynamicNode> {

        return listOf(

            dynamicTest("should have Hierarchy with Domain, Concept and Owner relation") {
                executeDpmDbWriterWithDefaults()

                val rs = dbConnection.createStatement().executeQuery(
                    """
                    SELECT
                        H.HierarchyCode,
                        H.HierarchyLabel,
                        H.HierarchyDescription,
                        C.ConceptType,
                        C.CreationDate,
                        C.ModificationDate,
                        C.FromDate,
                        C.ToDate,
                        D.DomainCode,
                        O.OwnerName
                    FROM mHierarchy AS H
                    INNER JOIN mConcept AS C ON C.ConceptID = H.ConceptID
                    INNER JOIN mDomain AS D ON D.DomainID = H.DomainID
                    INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
                    WHERE O.OwnerPrefix = "FixPrfx" OR O.OwnerPrefix = "eu"
                    """
                )

                assertThat(rs.toStringList()).containsExactly(
                    "#HierarchyCode, #HierarchyLabel, #HierarchyDescription, #ConceptType, #CreationDate, #ModificationDate, #FromDate, #ToDate, #DomainCode, #OwnerName",
                    "ExpDomHier-1-Code, ExpDomHier-1-Lbl-Fi, ExpDomHier-1-Desc-Fi, Hierarchy, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, ExpDom-1-Code, FixName",
                    "ExpDomHier-2-Code, ExpDomHier-2-Lbl-Fi, ExpDomHier-2-Desc-Fi, Hierarchy, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, ExpDom-1-Code, FixName",
                    "ExpDomHier-3-Code, ExpDomHier-3-Lbl-Fi, ExpDomHier-3-Desc-Fi, Hierarchy, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, ExpDom-1-Code, FixName",
                    "MetHier-1-Code, MetHier-1-Lbl-Fi, MetHier-1-Desc-Fi, Hierarchy, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, MET, FixName"
                )
            },

            dynamicTest("should have ConceptTranslations for DPM Hierarchy") {
                executeDpmDbWriterWithDefaults()

                val rs = dbConnection.createStatement().executeQuery(
                    """
                SELECT
                    H.HierarchyCode,
                    C.ConceptType,
                    T.Role,
                    TL.IsoCode,
                    T.Text
                FROM mHierarchy AS H
                INNER JOIN mConcept AS C ON C.ConceptID = H.ConceptID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
                INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                WHERE H.HierarchyCode = 'ExpDomHier-1-Code' AND O.OwnerPrefix = "FixPrfx"
                ORDER BY T.Role DESC, TL.IsoCode ASC
              """
                )

                assertThat(rs.toStringList()).containsExactly(
                    "#HierarchyCode, #ConceptType, #Role, #IsoCode, #Text",
                    "ExpDomHier-1-Code, Hierarchy, label, en, ExpDomHier-1-Lbl-En",
                    "ExpDomHier-1-Code, Hierarchy, label, fi, ExpDomHier-1-Lbl-Fi",
                    "ExpDomHier-1-Code, Hierarchy, description, en, ExpDomHier-1-Desc-En",
                    "ExpDomHier-1-Code, Hierarchy, description, fi, ExpDomHier-1-Desc-Fi"
                )
            },

            dynamicTest("should detect when multiple HierarchyNodes refer same Member") {
                val throwable = catchThrowable {
                    executeDpmDbWriter(
                        true,
                        false,
                        processingOptionsWithInherentTextLanguageFi(),
                        FixtureVariety.TWO_HIERARCHY_NODES_REFER_SAME_MEMBER
                    )
                }

                assertThat(throwable).isInstanceOf(HaltException::class.java)

                assertThat(diagnosticCollector.eventsString()).contains(
                    "FATAL",
                    "UNIQUE constraint failed",
                    "mHierarchyNode.HierarchyID, mHierarchyNode.MemberID"
                )
            }
        )
    }
}
