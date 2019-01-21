package fi.vm.yti.taxgen.sqliteprovider.conceptitems

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Owner
import org.jetbrains.exposed.dao.EntityID

data class DpmDictionaryItem(
    val owner: Owner,
    val ownerId: EntityID<Int>,
    val languageIds: Map<Language, EntityID<Int>>
) {

    private val domainItems = mutableListOf<DomainItem>()

    fun addDomainItem(domainItem: DomainItem) {
        domainItems.add(domainItem)
    }

    fun domainItemForCode(domainCode: String?): DomainItem? {
        if (domainCode == null) {
            return null
        }

        return domainItems.find { it.domainCode == domainCode }
            ?: thisShouldNeverHappen("No domain for given code: $domainCode")
    }
}