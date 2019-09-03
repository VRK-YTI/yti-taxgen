package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.sql.ResultSet

internal class SQLiteProvider_DictionaryReplace_TableCellTransform_UnitTest :
    SQLiteProvider_DictionaryReplaceUnitTestBase() {

    @Test
    fun `TableCell signature values should get NULL`() {
        baselineDbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mTableCell(CellID, TableID, IsRowKey, IsShaded, BusinessCode, DatapointSignature, DPS)
            VALUES (100, 200, 0, 0, "BizCode", "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-2-Code)", "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-2-Code)")
            """.trimIndent()
        )

        dumpDiagnosticsWhenThrown { replaceDictionaryInDb() }

        assertThat(diagnosticCollector.events).containsExactly(
            "ENTER [SQLiteDbWriter] []",
            "EXIT [SQLiteDbWriter]"
        )

        val rs = readAllTableCells()

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#CellID, #TableID, #IsRowKey, #IsShaded, #BusinessCode, #DatapointSignature, #DPS",
            "100, 200, 0, 0, BizCode, nil, nil"
        )
    }

    private fun readAllTableCells(): ResultSet {
        return outputDbConnection.createStatement().executeQuery(
            """
            SELECT * FROM mTableCell
            """.trimIndent()
        )
    }
}
