package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.ext.java.isBeforeOrEqual
import fi.vm.yti.taxgen.commons.ext.java.isBeforeOrEqualOrUndefined
import java.time.Instant
import java.time.LocalDate

data class Concept(
    val createdAt: Instant,
    val modifiedAt: Instant,
    val applicableFrom: LocalDate?,
    val applicableUntil: LocalDate?,
    val label: TranslatedText,
    val description: TranslatedText,
    val owner: Owner
) {
    init {
        label.defaultLanguage = owner.defaultLanguage
        description.defaultLanguage = owner.defaultLanguage

        //TODO - Make label accept only those languages which Owner defines
        //TODO - Then validate that there is label at least with one lang

        require(createdAt.isBeforeOrEqual(modifiedAt)) {
            "createdAt must precede modifiedAt"
        }

        //TODO applicableFrom && applicableUntil validation logic??
        if (applicableFrom != null) {
            require(applicableFrom.isBeforeOrEqualOrUndefined(applicableUntil)) {
                "applicableFrom must precede applicableUntil"
            }
        }

        require(!label.translations.isEmpty()) {
            "label must contain at least one translation"
        }
    }
}
