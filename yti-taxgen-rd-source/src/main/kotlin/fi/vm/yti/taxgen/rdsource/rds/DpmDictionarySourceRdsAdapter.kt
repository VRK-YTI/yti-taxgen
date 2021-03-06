package fi.vm.yti.taxgen.rdsource.rds

import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsource.CodeListBlueprint
import fi.vm.yti.taxgen.rdsource.CodeListSource
import fi.vm.yti.taxgen.rdsource.DpmDictionarySource
import fi.vm.yti.taxgen.rdsource.DpmDictionarySourceConfig

internal class DpmDictionarySourceRdsAdapter(
    private val config: DpmDictionarySourceConfig,
    private val rdsClient: RdsClient,
    private val diagnosticContext: DiagnosticContext
) : DpmDictionarySource {

    override fun contextTitle(): String = ""
    override fun contextIdentifier(): String = ""

    override fun dpmOwner(action: (Owner) -> Unit) {
        action(config.owner)
    }

    override fun metricsSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForUri(
                config.metrics.uri,
                CodeListBlueprint.metrics()
            )
        )
    }

    override fun explicitDomainsAndHierarchiesSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForUri(
                config.explicitDomainsAndHierarchies.uri,
                CodeListBlueprint.explicitDomainsAndHierarchies()
            )
        )
    }

    override fun explicitDimensionsSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForUri(
                config.explicitDimensions.uri,
                CodeListBlueprint.explicitOrTypedDimensions()
            )
        )
    }

    override fun typedDomainsSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForUri(
                config.typedDomains.uri,
                CodeListBlueprint.typedDomains()
            )
        )
    }

    override fun typedDimensionsSource(action: (CodeListSource?) -> Unit) {
        action(
            codeListSourceOrNullForUri(
                config.typedDimensions.uri,
                CodeListBlueprint.explicitOrTypedDimensions()
            )
        )
    }

    private fun codeListSourceOrNullForUri(
        uri: String?,
        blueprint: CodeListBlueprint
    ): CodeListSource? {
        return if (uri != null) {
            CodeListSourceRdsAdapter(
                codeListUri = uri,
                blueprint = blueprint,
                rdsClient = rdsClient,
                diagnosticContext = diagnosticContext
            )
        } else {
            null
        }
    }
}
