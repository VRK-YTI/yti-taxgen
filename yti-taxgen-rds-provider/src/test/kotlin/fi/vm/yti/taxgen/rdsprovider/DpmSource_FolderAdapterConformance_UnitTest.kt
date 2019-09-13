package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory

@DisplayName("Test folder source adapter conformance")
internal class DpmSource_FolderAdapterConformance_UnitTest : DpmSource_ConformanceUnitTestBase() {

    companion object {
        lateinit var loopbackTempFolder: TempFolder
        lateinit var zipLoopbackTempFolder: TempFolder

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            loopbackTempFolder = TempFolder("conformance_loopback")
            zipLoopbackTempFolder = TempFolder("conformance_zip_loopback")
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            loopbackTempFolder.close()
            zipLoopbackTempFolder.close()
        }
    }

    @TestFactory
    fun `Folder adapter with static reference data`(): List<DynamicNode> {
        val (sourceHolder, rootPath) = sourceFolderAdapterFromReferenceData()

        val expectedDetails = ExpectedDetails(
            dpmSourceContextType = DiagnosticContextType.DpmSource,
            dpmSourceContextLabel = "folder",
            dpmSourceContextIdentifier = rootPath.toString()
        )

        return createAdapterConformanceTestCases(sourceHolder, expectedDetails)
    }

    @TestFactory
    fun `Context decorated folder adapter with static reference data`(): List<DynamicNode> {
        val (sourceHolder, rootPath) = sourceFolderAdapterFromReferenceData(diagnosticContext)

        val expectedDetails = ExpectedDetails(
            dpmSourceContextType = DiagnosticContextType.DpmSource,
            dpmSourceContextLabel = "folder",
            dpmSourceContextIdentifier = rootPath.toString()
        )

        return createAdapterConformanceTestCases(sourceHolder, expectedDetails)
    }

    @TestFactory
    fun `Folder adapter with loopback data`(): List<DynamicNode> {
        SourceFactory.folderRecorder(
            outputFolderPath = loopbackTempFolder.path(),
            forceOverwrite = false,
            diagnosticContext = diagnosticContext
        ).use {
            val (source, _) = sourceFolderAdapterFromReferenceData(
                diagnosticContext = diagnosticContext
            )
            it.captureSources(source)
        }

        val sourceHolder = SourceFactory.sourceForFolder(
            sourceRootPath = loopbackTempFolder.path(),
            diagnosticContext = diagnosticContext
        )

        val expectedDetails = ExpectedDetails(
            dpmSourceContextType = DiagnosticContextType.DpmSource,
            dpmSourceContextLabel = "folder",
            dpmSourceContextIdentifier = loopbackTempFolder.path().toString()
        )

        return createAdapterConformanceTestCases(sourceHolder, expectedDetails)
    }

    @TestFactory
    fun `Folder adapter with zip-loopback data`(): List<DynamicNode> {
        val targetZipPath = zipLoopbackTempFolder.resolve("file.zip")

        SourceFactory.zipRecorder(
            outputZipPath = targetZipPath,
            forceOverwrite = false,
            diagnosticContext = diagnosticContext
        ).use {
            val (source, _) = sourceFolderAdapterFromReferenceData(
                diagnosticContext = diagnosticContext)
            it.captureSources(source)
        }

        val sourceHolder = SourceFactory.sourceForZipFile(
            zipFilePath = targetZipPath,
            diagnosticContext = diagnosticContext
        )

        val expectedDetails = ExpectedDetails(
            dpmSourceContextType = DiagnosticContextType.DpmSource,
            dpmSourceContextLabel = "ZIP file",
            dpmSourceContextIdentifier = targetZipPath.toString()
        )

        return createAdapterConformanceTestCases(sourceHolder, expectedDetails)
    }
}
