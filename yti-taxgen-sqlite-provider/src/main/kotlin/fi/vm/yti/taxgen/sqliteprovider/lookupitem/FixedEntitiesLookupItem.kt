package fi.vm.yti.taxgen.sqliteprovider.lookupitem

import org.jetbrains.exposed.dao.EntityID

data class FixedEntitiesLookupItem(
    val metricDomainOwnerId: EntityID<Int>,
    val metricDomainCode: String,
    val metricDomainId: EntityID<Int>,

    val metricDimensionXbrlCode: String,
    val metricDimensionId: EntityID<Int>,

    val openMemberId: EntityID<Int>
)
