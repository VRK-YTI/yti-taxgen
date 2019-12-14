package fi.vm.yti.taxgen.dpmmodel.diagnostic

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.validation.Validatable

interface DiagnosticContext : Diagnostic {

    fun <R> withContext(
        contextType: DiagnosticContextType,
        contextDetails: DiagnosticContextDetails?,
        action: () -> R
    ): R

    fun significantErrorsReceived(): Boolean
    fun stopIfSignificantErrorsReceived(messageProvider: () -> String)

    override fun updateCurrentContextDetails(contextTitle: String?, contextIdentifier: String?)

    override fun fatal(message: String): Nothing
    override fun error(message: String)
    override fun info(message: String)

    override fun validate(
        validatable: Validatable
    )

    override fun validate(
        validatables: List<Validatable>
    )

    override fun diagnosticSourceLanguages(): List<Language>
}
