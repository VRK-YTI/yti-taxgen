package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.helpers.SortOps
import java.nio.file.Path

class YclSourceFolderStructureAdapter(
    baseFolderPath: Path
) : YclSource() {

    private val baseFolderPath = baseFolderPath.toAbsolutePath().normalize()

    override fun contextName(): String = "folder"
    override fun contextRef(): String = baseFolderPath.toString()

    override fun sourceInfoData(): String {
        return FileOps.readTextFile(baseFolderPath, "source_info.json")
    }

    override fun dpmDictionarySources(): List<DpmDictionarySource> {
        val paths = FileOps.listSubFoldersMatching(baseFolderPath, "dpmdictionary_*")
        val sortedPaths = SortOps.folderContentSortedByNumberAwareFilename(paths)

        return sortedPaths.mapIndexed { index, path -> DpmDictionarySourceFolderStructureAdapter(index, path) }
    }

    override fun close() {}
}
