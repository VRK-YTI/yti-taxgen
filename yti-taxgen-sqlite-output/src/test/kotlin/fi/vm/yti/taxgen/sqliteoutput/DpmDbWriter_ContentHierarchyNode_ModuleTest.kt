package fi.vm.yti.taxgen.sqliteoutput

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest

internal class DpmDbWriter_ContentHierarchyNode_ModuleTest : DpmDbWriter_ContentModuleTestBase() {

    override fun createDynamicTests(): List<DynamicNode> {

        return listOf(

            dynamicTest("should have correct HierarchyNode structure") {
                executeDpmDbWriterWithDefaults()

                val rs = dbConnection.createStatement().executeQuery(
                    """
                    SELECT
                        H.HierarchyCode,
                        N.HierarchyNodeLabel,
                        M.MemberCode,
                        P.MemberCode AS ParentMemberCode,
                        P.MemberLabel AS ParentMemberLabel,
                        N.Level,
                        N.'Order',
                        N.Path
                    FROM mHierarchyNode AS N
                    INNER JOIN mHierarchy AS H ON H.HierarchyID = N.HierarchyID
                    INNER JOIN mConcept AS C ON C.ConceptID = H.ConceptID
                    INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
                    INNER JOIN mMember AS M ON M.MemberID = N.MemberID
                    LEFT JOIN mMember AS P ON P.MemberID = N.ParentMemberID
                    WHERE O.OwnerPrefix = "FixPrfx" OR O.OwnerPrefix = "eu"
                    """
                )

                assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                    "#HierarchyCode, #HierarchyNodeLabel, #MemberCode, #ParentMemberCode, #ParentMemberLabel, #Level, #Order, #Path",
                    "ExpDomHier-2-Code, ExpDomHierNode-1-Lbl-Fi, Mbr-1-Code, nil, nil, 1, 1, nil",
                    "ExpDomHier-2-Code, ExpDomHierNode-2-Lbl-Fi, Mbr-2-Code, nil, nil, 1, 2, nil",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.1-Lbl-Fi, Mbr-3-Code, Mbr-2-Code, Mbr-2-Lbl-Fi, 2, 3, nil",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.1.1-Lbl-Fi, Mbr-4-Code, Mbr-3-Code, Mbr-3-Lbl-Fi, 3, 4, nil",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.2-Lbl-Fi, Mbr-5-Code, Mbr-2-Code, Mbr-2-Lbl-Fi, 2, 5, nil",
                    "MetHier-1-Code, MetHierNode-1-Lbl-Fi, ed1, nil, nil, 1, 1, nil",
                    "MetHier-1-Code, MetHierNode-2-Lbl-Fi, bd2, nil, nil, 1, 2, nil",
                    "MetHier-1-Code, MetHierNode-2.1-Lbl-Fi, di3, bd2, Met-2-Lbl-Fi, 2, 3, nil",
                    "MetHier-1-Code, MetHierNode-2.2-Lbl-Fi, ii4, bd2, Met-2-Lbl-Fi, 2, 4, nil",
                    "MetHier-1-Code, MetHierNode-3-Lbl-Fi, p5, nil, nil, 1, 5, nil"
                )
            },

            dynamicTest("should have HierarchyNodes with Member, Concept and Owner relation") {
                executeDpmDbWriterWithDefaults()

                val rs = dbConnection.createStatement().executeQuery(
                    """
                    SELECT
                        H.HierarchyCode,
                        N.HierarchyNodeLabel,
                        M.MemberCode,
                        N.ComparisonOperator,
                        N.UnaryOperator,
                        N.IsAbstract,
                        C.ConceptType,
                        C.CreationDate,
                        C.ModificationDate,
                        C.FromDate,
                        C.ToDate,
                        O.OwnerName
                    FROM mHierarchyNode AS N
                    INNER JOIN mHierarchy AS H ON H.HierarchyID = N.HierarchyID
                    INNER JOIN mMember AS M ON M.MemberID = N.MemberID
                    LEFT JOIN mMember AS P ON P.MemberID = N.ParentMemberID
                    INNER JOIN mConcept AS C ON C.ConceptID = N.ConceptID
                    INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
                    WHERE O.OwnerPrefix = "FixPrfx" OR O.OwnerPrefix = "eu"
                    """
                )

                assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                    "#HierarchyCode, #HierarchyNodeLabel, #MemberCode, #ComparisonOperator, #UnaryOperator, #IsAbstract, #ConceptType, #CreationDate, #ModificationDate, #FromDate, #ToDate, #OwnerName",
                    "ExpDomHier-2-Code, ExpDomHierNode-1-Lbl-Fi, Mbr-1-Code, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "ExpDomHier-2-Code, ExpDomHierNode-2-Lbl-Fi, Mbr-2-Code, =, +, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.1-Lbl-Fi, Mbr-3-Code, =, +, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.1.1-Lbl-Fi, Mbr-4-Code, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.2-Lbl-Fi, Mbr-5-Code, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "MetHier-1-Code, MetHierNode-1-Lbl-Fi, ed1, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "MetHier-1-Code, MetHierNode-2-Lbl-Fi, bd2, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "MetHier-1-Code, MetHierNode-2.1-Lbl-Fi, di3, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "MetHier-1-Code, MetHierNode-2.2-Lbl-Fi, ii4, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
                    "MetHier-1-Code, MetHierNode-3-Lbl-Fi, p5, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName"
                )
            },

            dynamicTest("should have ConceptTranslations for HierarchyNode") {
                executeDpmDbWriterWithDefaults()

                val rs = dbConnection.createStatement().executeQuery(
                    """
                    SELECT
                        N.HierarchyNodeLabel,
                        C.ConceptType,
                        T.Role,
                        TL.IsoCode,
                        T.Text
                    FROM mHierarchyNode AS N
                    INNER JOIN mConcept AS C ON C.ConceptID = N.ConceptID
                    INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                    INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                    WHERE N.HierarchyNodeLabel = 'ExpDomHierNode-1-Lbl-Fi' or N.HierarchyNodeLabel = 'MetHierNode-1-Lbl-Fi'
                    ORDER BY T.Role DESC, TL.IsoCode ASC
                    """
                )

                assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                    "#HierarchyNodeLabel, #ConceptType, #Role, #IsoCode, #Text",
                    "ExpDomHierNode-1-Lbl-Fi, HierarchyNode, label, en, ExpDomHierNode-1-Lbl-En",
                    "ExpDomHierNode-1-Lbl-Fi, HierarchyNode, label, fi, ExpDomHierNode-1-Lbl-Fi",
                    "ExpDomHierNode-1-Lbl-Fi, HierarchyNode, description, en, ExpDomHierNode-1-Desc-En",
                    "ExpDomHierNode-1-Lbl-Fi, HierarchyNode, description, fi, ExpDomHierNode-1-Desc-Fi",
                    "MetHierNode-1-Lbl-Fi, HierarchyNode, label, en, MetHierNode-1-Lbl-En",
                    "MetHierNode-1-Lbl-Fi, HierarchyNode, label, fi, MetHierNode-1-Lbl-Fi",
                    "MetHierNode-1-Lbl-Fi, HierarchyNode, description, en, MetHierNode-1-Desc-En",
                    "MetHierNode-1-Lbl-Fi, HierarchyNode, description, fi, MetHierNode-1-Desc-Fi"
                )
            },

            dynamicTest("should bind HierarchyNodes to Members from same Explicit Domain") {
                executeDpmDbWriter(
                    false,
                    false,
                    processingOptionsWithInherentTextLanguageFi(),
                    FixtureVariety.NONE
                )

                // Verify that data is properly setup
                val membersRs = dbConnection.createStatement().executeQuery(
                    """
                    SELECT
                        M.MemberCode,
                        M.MemberXBRLCode,
                        D.DomainCode,
                        D.DomainXBRLCode
                    FROM mMember as M
                    INNER JOIN mConcept AS C ON C.ConceptID = M.ConceptID
                    INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
                    INNER JOIN mDomain AS D on M.DomainID = D.DomainID
                    WHERE M.MemberCode = "Mbr-1-Code" AND O.OwnerPrefix = "FixPrfx"
                    """
                )

                assertThat(membersRs.toStringList()).containsExactlyInAnyOrder(
                    "#MemberCode, #MemberXBRLCode, #DomainCode, #DomainXBRLCode",
                    "Mbr-1-Code, FixPrfx_ExpDom-1-Code:Mbr-1-Code, ExpDom-1-Code, FixPrfx_exp:ExpDom-1-Code",
                    "Mbr-1-Code, FixPrfx_ExpDom-2-Code:Mbr-1-Code, ExpDom-2-Code, FixPrfx_exp:ExpDom-2-Code",
                    "Mbr-1-Code, FixPrfx_ExpDom-3-Code:Mbr-1-Code, ExpDom-3-Code, FixPrfx_exp:ExpDom-3-Code"
                )

                // Verify that Member linked to HierarchyNode is from same ExplicitDomain where the HierarchyNode belongs to
                val nodeRs = dbConnection.createStatement().executeQuery(
                    """
                    SELECT
                        H.HierarchyCode,
                        N.HierarchyNodeLabel,
                        M.MemberCode,
                        HD.DomainCode AS "Hierarchy.DomainCode",
                        MD.DomainCode AS "Member.DomainCode"
                    FROM mHierarchyNode AS N
                    INNER JOIN mHierarchy AS H ON H.HierarchyID = N.HierarchyID
                    INNER JOIN mConcept AS C ON C.ConceptID = H.ConceptID
                    INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
                    INNER JOIN mDomain AS HD ON H.DomainID = HD.DomainID
                    INNER JOIN mMember AS M ON M.MemberID = N.MemberID
                    INNER JOIN mDomain AS MD ON M.DomainID = MD.DomainID
                    WHERE H.HierarchyCode = "ExpDomHier-2-Code" AND O.OwnerPrefix = "FixPrfx"
                    ORDER BY HD.DomainCode ASC
                    """
                )

                assertThat(nodeRs.toStringList()).containsExactlyInAnyOrder(
                    "#HierarchyCode, #HierarchyNodeLabel, #MemberCode, #Hierarchy.DomainCode, #Member.DomainCode",
                    "ExpDomHier-2-Code, ExpDomHierNode-1-Lbl-Fi, Mbr-1-Code, ExpDom-1-Code, ExpDom-1-Code",
                    "ExpDomHier-2-Code, ExpDomHierNode-2-Lbl-Fi, Mbr-2-Code, ExpDom-1-Code, ExpDom-1-Code",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.1-Lbl-Fi, Mbr-3-Code, ExpDom-1-Code, ExpDom-1-Code",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.1.1-Lbl-Fi, Mbr-4-Code, ExpDom-1-Code, ExpDom-1-Code",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.2-Lbl-Fi, Mbr-5-Code, ExpDom-1-Code, ExpDom-1-Code",

                    "ExpDomHier-2-Code, ExpDomHierNode-1-Lbl-Fi, Mbr-1-Code, ExpDom-2-Code, ExpDom-2-Code",
                    "ExpDomHier-2-Code, ExpDomHierNode-2-Lbl-Fi, Mbr-2-Code, ExpDom-2-Code, ExpDom-2-Code",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.1-Lbl-Fi, Mbr-3-Code, ExpDom-2-Code, ExpDom-2-Code",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.1.1-Lbl-Fi, Mbr-4-Code, ExpDom-2-Code, ExpDom-2-Code",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.2-Lbl-Fi, Mbr-5-Code, ExpDom-2-Code, ExpDom-2-Code",

                    "ExpDomHier-2-Code, ExpDomHierNode-1-Lbl-Fi, Mbr-1-Code, ExpDom-3-Code, ExpDom-3-Code",
                    "ExpDomHier-2-Code, ExpDomHierNode-2-Lbl-Fi, Mbr-2-Code, ExpDom-3-Code, ExpDom-3-Code",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.1-Lbl-Fi, Mbr-3-Code, ExpDom-3-Code, ExpDom-3-Code",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.1.1-Lbl-Fi, Mbr-4-Code, ExpDom-3-Code, ExpDom-3-Code",
                    "ExpDomHier-2-Code, ExpDomHierNode-2.2-Lbl-Fi, Mbr-5-Code, ExpDom-3-Code, ExpDom-3-Code"
                )
            }
        )
    }
}
