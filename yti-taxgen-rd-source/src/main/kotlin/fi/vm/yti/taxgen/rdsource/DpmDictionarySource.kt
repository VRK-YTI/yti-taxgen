package fi.vm.yti.taxgen.rdsource

import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextDetails

interface DpmDictionarySource : DiagnosticContextDetails {
    fun dpmOwner(action: (Owner) -> Unit)
    fun metricsSource(action: (CodeListSource?) -> Unit)
    fun explicitDomainsAndHierarchiesSource(action: (CodeListSource?) -> Unit)
    fun explicitDimensionsSource(action: (CodeListSource?) -> Unit)
    fun typedDomainsSource(action: (CodeListSource?) -> Unit)
    fun typedDimensionsSource(action: (CodeListSource?) -> Unit)
}
