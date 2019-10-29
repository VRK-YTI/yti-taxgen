package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.SourceHolder
import fi.vm.yti.taxgen.rdsprovider.DpmSourceConfigHolder

class SourceHolderRdsAdapter(
    private val configHolder: DpmSourceConfigHolder,
    private val diagnostic: Diagnostic
) : SourceHolder {

    override fun withDpmSource(action: (DpmSource) -> Unit) {
        val dpmSource = DpmSourceRdsAdapter(configHolder, diagnostic)
        action(dpmSource)
    }

    override fun close() {}
}
