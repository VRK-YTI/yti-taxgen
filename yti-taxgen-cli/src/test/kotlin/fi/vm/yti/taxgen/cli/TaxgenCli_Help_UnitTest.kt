package fi.vm.yti.taxgen.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--help´")
internal class TaxgenCli_Help_UnitTest : TaxgenCli_UnitTestBase(
    primaryCommand = "--help"
) {

    @Test
    fun `Should list available command line options`() {
        val args = arrayOf("--help")
        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()

        assertThat(outText).containsSubsequence(
            "--help",
            "--compile-dpm-db",
            "--capture-ycl-sources-to-folder",
            "--capture-ycl-sources-to-zip",
            "--force-overwrite",
            "--source-config",
            "--source-folder",
            "--source-zip"
        )

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }
}