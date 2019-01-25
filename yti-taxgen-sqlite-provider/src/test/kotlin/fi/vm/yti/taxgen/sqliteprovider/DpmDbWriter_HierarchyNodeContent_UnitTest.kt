package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SQLite DPM DB content: hierarchy nodes")
internal class DpmDbWriter_HierarchyNodeContent_UnitTest : DpmDbWriter_UnitTestBase() {

    @Test
    fun `should have correct HierarchyNode structure`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

        val rs = dbConnection.createStatement().executeQuery(
            """
                SELECT
					H.HierarchyCode,
                    N.HierarchyNodeLabel,
					M.MemberCode,
					P.MemberCode AS ParentMemberCode,
                    N.Level,
                    N.'Order',
                    N.Path
                FROM mHierarchyNode AS N
                INNER JOIN mHierarchy AS H ON H.HierarchyID = N.HierarchyID
				INNER JOIN mMember AS M ON M.MemberID = N.MemberID
				LEFT JOIN mMember AS P ON P.MemberID = N.ParentMemberID
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#HierarchyCode, #HierarchyNodeLabel, #MemberCode, #ParentMemberCode, #Level, #Order, #Path",
            "ExpDomHier-1-Code, ExpDomHierNode-1-Lbl-Fi, Mbr-1-Code, nil, 1, 1, nil",
            "ExpDomHier-1-Code, ExpDomHierNode-2-Lbl-Fi, Mbr-2-Code, nil, 1, 2, nil",
            "ExpDomHier-1-Code, ExpDomHierNode-2.1-Lbl-Fi, Mbr-3-Code, Mbr-2-Code, 2, 1, nil",
            "ExpDomHier-1-Code, ExpDomHierNode-2.1.1-Lbl-Fi, Mbr-4-Code, Mbr-3-Code, 3, 1, nil",
            "ExpDomHier-1-Code, ExpDomHierNode-2.2-Lbl-Fi, Mbr-5-Code, Mbr-2-Code, 2, 2, nil",
            "MetHier-1-Code, MetHierNode-1-Lbl-Fi, ed1, nil, 1, 1, nil",
            "MetHier-1-Code, MetHierNode-2-Lbl-Fi, bd2, nil, 1, 2, nil",
            "MetHier-1-Code, MetHierNode-2.1-Lbl-Fi, di3, bd2, 2, 1, nil",
            "MetHier-1-Code, MetHierNode-2.2-Lbl-Fi, ii4, bd2, 2, 2, nil",
            "MetHier-1-Code, MetHierNode-3-Lbl-Fi, p5, nil, 1, 3, nil"
        )
    }

    @Test
    fun `should have HierarchyNodes with Member, Concept and Owner relation`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

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
              """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#HierarchyCode, #HierarchyNodeLabel, #MemberCode, #ComparisonOperator, #UnaryOperator, #IsAbstract, #ConceptType, #CreationDate, #ModificationDate, #FromDate, #ToDate, #OwnerName",
            "ExpDomHier-1-Code, ExpDomHierNode-1-Lbl-Fi, Mbr-1-Code, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "ExpDomHier-1-Code, ExpDomHierNode-2-Lbl-Fi, Mbr-2-Code, =, +, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "ExpDomHier-1-Code, ExpDomHierNode-2.1-Lbl-Fi, Mbr-3-Code, =, +, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "ExpDomHier-1-Code, ExpDomHierNode-2.1.1-Lbl-Fi, Mbr-4-Code, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "ExpDomHier-1-Code, ExpDomHierNode-2.2-Lbl-Fi, Mbr-5-Code, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "MetHier-1-Code, MetHierNode-1-Lbl-Fi, ed1, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "MetHier-1-Code, MetHierNode-2-Lbl-Fi, bd2, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "MetHier-1-Code, MetHierNode-2.1-Lbl-Fi, di3, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "MetHier-1-Code, MetHierNode-2.2-Lbl-Fi, ii4, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName",
            "MetHier-1-Code, MetHierNode-3-Lbl-Fi, p5, nil, nil, 0, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, FixName"
        )
    }

    @Test
    fun `should have ConceptTranslations for HierarchyNode`() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())

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
    }
}
