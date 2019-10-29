package fi.vm.yti.taxgen.rdsprovider.configdata

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.CodeListConfig

data class CodeListConfigData(
    val uri: String?
) {

    @Suppress("UNUSED_PARAMETER")
    fun toConfig(diagnostic: Diagnostic): CodeListConfig {
        return CodeListConfig(
            uri = uri
        )
    }
}
