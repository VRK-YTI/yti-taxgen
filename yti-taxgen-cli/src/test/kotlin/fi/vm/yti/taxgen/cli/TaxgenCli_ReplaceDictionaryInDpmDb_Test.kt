package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.testcommons.TestFixture
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.RDS_SOURCE_CONFIG
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("Command ´--replace-dictionary-in-dpm-db´")
internal class TaxgenCli_ReplaceDictionaryInDpmDb_Test : TaxgenCli_TestBase(
    primaryCommand = "--replace-dictionary-in-dpm-db"
) {

    private lateinit var targetDbPath: Path

    @BeforeEach
    fun init() {
        targetDbPath = cloneTestFixtureToTemp(TestFixture.Type.DPM_DB, "dm_integration_fixture_pre_filled.db")
    }

    @Test
    fun `Should replace dictionary within database from DPM source capture`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(targetDbPath).exists().isRegularFile()

            assertThat(fetchDpmOwnersFromDb(targetDbPath)).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture"
            )

            assertThat(fetchElementCodesFromDb(targetDbPath)).containsExactly(
                "#Metrics [b1, b4, d10, d16, e3, i12, i6, l11, m7, p8, s9, sd14, si13]",
                "#ExpDoms [9999, DOME, EDA, EDA1, EDA10, EDA11, EDA20, EDA9, MET]",
                "#TypDoms [DOMT, TDB, TDD, TDI, TDM, TDP, TDR, TDS]",
                "#ExpDims [DIM, EDA-D1, EDA-D10, EDA-D2, MET]",
                "#TypDims [TDB-D1, TDB-D2]"
            )
        }
    }

    @Test
    fun `Should fail when target database filename is not given`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).isBlank()

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Single command with proper argument must be given"
            )
        }
    }

    @Test
    fun `Should report error when target file is not database`() {
        Files.write(targetDbPath, "Existing file".toByteArray())

        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "FATAL: org.sqlite.SQLiteException: [SQLITE_NOTADB]  File opened that is not a database file (file is not a database)"
            )

            assertThat(targetDbPath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should report error when given target database path points to folder`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "${tempFolder.path()}",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "FATAL: Target database file open failed: The database has been closed"
            )
        }
    }

    @Test
    fun `Should fail when no source option is given`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database"
            )

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Single source with proper argument must be given"
            )
        }
    }

    @Test
    fun `Should fail when source option without filepath is given`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-folder"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).isBlank()

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Option source-folder requires an argument"
            )
        }
    }

    @Test
    fun `Should fail when given source filepath does not exist`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "${tempFolder.resolve("non_existing_folder")}"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).isBlank()

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Option source-folder: Directory", "does not exist"
            )
        }
    }

    @Test
    fun `Should fail when more than one source option is given`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "$dpmSourceCapturePath",
            "--source-config",
            "$dpmSourceConfigPath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database"
            )

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Single source with proper argument must be given"
            )
        }
    }

    @Test
    fun `Should replace dictionary within database from DPM source config`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$dpmSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "Configuration file: (dm_integration_fixture.json)",
                "Configuration file: OK",
                "RDS to DPM mapper",
                "DPM source: Reference Data service",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(targetDbPath).exists().isRegularFile()

            assertThat(fetchDpmOwnersFromDb(targetDbPath)).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture"
            )

            assertThat(fetchElementCodesFromDb(targetDbPath)).containsExactly(
                "#Metrics [b1, b4, d10, d16, e3, i12, i6, l11, m7, p8, s9, sd14, si13]",
                "#ExpDoms [9999, DOME, EDA, EDA1, EDA10, EDA11, EDA20, EDA9, MET]",
                "#TypDoms [DOMT, TDB, TDD, TDI, TDM, TDP, TDR, TDS]",
                "#ExpDims [DIM, EDA-D1, EDA-D10, EDA-D2, MET]",
                "#TypDims [TDB-D1, TDB-D2]"
            )
        }
    }

    @Test
    fun `Should report error when DPM source config has Metrics without referenced ExpDoms`() {
        val partialSourceConfigPath = clonePartialSourceConfigFromConfig(
            configPath = dpmSourceConfigPath,
            nameTag = "Partial With Metrics",
            retainedElementSources = listOf("metrics")
        )

        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$partialSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Configuration file: (partial_source_config.json)",
                "DPM dictionary #0: ERROR: DpmDictionary () => metrics",
                "refers non existing ExplicitDomain 'EDA'",
                "Writing dictionaries to DPM database: INFO: Mapping failed due content errors"
            )
        }
    }

    @Test
    fun `Should replace dictionary within database from DPM source config having Metrics and ExpDoms`() {
        val partialSourceConfigPath = clonePartialSourceConfigFromConfig(
            configPath = dpmSourceConfigPath,
            nameTag = "Partial With Metrics and ExpDoms",
            retainedElementSources = listOf("metrics", "explicitDomainsAndHierarchies")
        )

        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$partialSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Configuration file: (partial_source_config.json)",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(fetchDpmOwnersFromDb(targetDbPath)).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture"
            )

            assertThat(fetchElementCodesFromDb(targetDbPath)).containsExactly(
                "#Metrics [b1, b4, d10, d16, e3, i12, i6, l11, m7, p8, s9, sd14, si13]",
                "#ExpDoms [9999, DOME, EDA, EDA1, EDA10, EDA11, EDA20, EDA9, MET]",
                "#TypDoms []",
                "#ExpDims [MET]",
                "#TypDims []"
            )
        }
    }

    @Test
    fun `Should replace dictionary within database from DPM source config having ExpDoms only`() {

        val partialSourceConfigPath = clonePartialSourceConfigFromConfig(
            configPath = dpmSourceConfigPath,
            nameTag = "Partial With ExpDoms",
            retainedElementSources = listOf("explicitDomainsAndHierarchies")
        )

        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$partialSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Configuration file: (partial_source_config.json)",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(fetchDpmOwnersFromDb(targetDbPath)).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture"
            )

            assertThat(fetchElementCodesFromDb(targetDbPath)).containsExactly(
                "#Metrics []",
                "#ExpDoms [9999, DOME, EDA, EDA1, EDA10, EDA11, EDA20, EDA9, MET]",
                "#TypDoms []",
                "#ExpDims [MET]",
                "#TypDims []"
            )
        }
    }

    @Test
    fun `Should replace dictionary within database from DPM source config having TypDoms only`() {

        val partialSourceConfigPath = clonePartialSourceConfigFromConfig(
            configPath = dpmSourceConfigPath,
            nameTag = "Partial With TypDoms",
            retainedElementSources = listOf("typedDomains")
        )

        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$partialSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Configuration file: (partial_source_config.json)",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(fetchDpmOwnersFromDb(targetDbPath)).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture"
            )

            assertThat(fetchElementCodesFromDb(targetDbPath)).containsExactly(
                "#Metrics []",
                "#ExpDoms [9999, MET]",
                "#TypDoms [DOMT, TDB, TDD, TDI, TDM, TDP, TDR, TDS]",
                "#ExpDims [MET]",
                "#TypDims []"
            )
        }
    }

    @Test
    fun `Should replace dictionary within database from DPM source config having ExpDims and ExpDoms`() {

        val partialSourceConfigPath = clonePartialSourceConfigFromConfig(
            configPath = dpmSourceConfigPath,
            nameTag = "Partial With ExpDims and ExpDoms",
            retainedElementSources = listOf("explicitDimensions", "explicitDomainsAndHierarchies")
        )

        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$partialSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Configuration file: (partial_source_config.json)",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(fetchDpmOwnersFromDb(targetDbPath)).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture"
            )

            assertThat(fetchElementCodesFromDb(targetDbPath)).containsExactly(
                "#Metrics []",
                "#ExpDoms [9999, DOME, EDA, EDA1, EDA10, EDA11, EDA20, EDA9, MET]",
                "#TypDoms []",
                "#ExpDims [DIM, EDA-D1, EDA-D10, EDA-D2, MET]",
                "#TypDims []"
            )
        }
    }

    @Test
    fun `Should replace dictionary within database from DPM source config having TypDims and TypDoms`() {

        val partialSourceConfigPath = clonePartialSourceConfigFromConfig(
            configPath = dpmSourceConfigPath,
            nameTag = "Partial With TypDims and TypDoms",
            retainedElementSources = listOf("typedDimensions", "typedDomains")
        )

        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$partialSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Configuration file: (partial_source_config.json)",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(fetchDpmOwnersFromDb(targetDbPath)).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture"
            )

            assertThat(fetchElementCodesFromDb(targetDbPath)).containsExactly(
                "#Metrics []",
                "#ExpDoms [9999, MET]",
                "#TypDoms [DOMT, TDB, TDD, TDI, TDM, TDP, TDR, TDS]",
                "#ExpDims [MET]",
                "#TypDims [TDB-D1, TDB-D2]"
            )
        }
    }

    @Test
    fun `Should report error when source config file is broken JSON`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-config",
            cloneTestFixtureToTemp(RDS_SOURCE_CONFIG, "broken_source_config_json.json").toString()
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "Configuration file: (broken_source_config_json.json)",
                "FATAL: Processing JSON content failed: "
            )
        }
    }

    @Test
    fun `Should fail when source config file does not exist`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-config",
            "${tempFolder.resolve("non_existing_config.json")}"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Option source-config: File", "does not exist"
            )

            assertThat(outText).isBlank()
        }
    }

    @Test
    fun `Should report error when source config links to non existing DPM code list`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-config",
            cloneTestFixtureToTemp(RDS_SOURCE_CONFIG, "broken_metric_uri_unknown_codelist.json").toString()
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "DPM dictionary", "codelist_uri_unknown_codelist",
                "Codelist",
                "Content URLs",
                "FATAL: JSON content fetch failed: HTTP 404 (Not Found)"
            )
        }
    }

    @Test
    fun `Should report error when source config links to unresolvable DPM host name`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-config",
            cloneTestFixtureToTemp(RDS_SOURCE_CONFIG, "broken_metric_uri_unresolvable_host.json").toString()
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "DPM dictionary", "codelist_uri_unresolvable_host",
                "Codelist",
                "Content URLs",
                "FATAL: Could not determine the server IP address"
            )
        }
    }

    @Test
    fun `Should report error when source config has URI with bad protocol`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-config",
            cloneTestFixtureToTemp(RDS_SOURCE_CONFIG, "broken_metric_uri_bad_protocol.json").toString()
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "DPM dictionary", "codelist_uri_bad_protocol",
                "Codelist",
                "Content URLs",
                "FATAL: Malformed URI"
            )
        }
    }

    @Test
    fun `Should report error when source config URI points to non-responsive host IP`() {
        val args = arrayOf(
            "--replace-dictionary-in-dpm-db",
            "$targetDbPath",
            "--source-config",
            cloneTestFixtureToTemp(RDS_SOURCE_CONFIG, "broken_metric_uri_non_responsive_host_ip.json").toString()
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "DPM dictionary", "codelist_uri_non_responsive_host_ip",
                "Codelist",
                "Content URLs",
                "FATAL: Could not connect the server"
            )
        }
    }
}