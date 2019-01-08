package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.datafactory.DataDefinition
import fi.vm.yti.taxgen.dpmmodel.datafactory.dynamicAttribute
import java.time.Instant
import java.time.LocalDate

fun dpmTestData(): Set<DataDefinition> {
    var definitions = HashSet<DataDefinition>()

    definitions.add(
        DataDefinition(
            kClass = Language::class,
            attributes = mapOf(
                "iso6391Code" to "en",
                "label" to dynamicAttribute { it.instantiate<TranslatedText>() }
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = TranslatedText::class,
            attributes = mapOf(
                "translations" to mapOf(
                    Language.findByIso6391Code("en") to "Text#en",
                    Language.findByIso6391Code("fi") to "Text#fi"
                )
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = Owner::class,
            attributes = mapOf(
                "name" to "name_value",
                "namespace" to "namespace_value",
                "prefix" to "ns_prefix",
                "location" to "official_location",
                "copyright" to "Lorem ipsum",
                "languageCodes" to listOf("en", "fi"),
                "defaultLanguageCode" to "fi"
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = Concept::class,
            attributes = mapOf(
                "createdAt" to Instant.parse("2018-03-20T10:20:30.40Z"),
                "modifiedAt" to Instant.parse("2018-03-22T10:20:30.40Z"),
                "applicableFrom" to LocalDate.of(2018, 3, 20),
                "applicableUntil" to LocalDate.of(2018, 4, 20),
                "label" to dynamicAttribute { it.instantiate<TranslatedText>() },
                "description" to dynamicAttribute { it.instantiate<TranslatedText>() },
                "owner" to dynamicAttribute { it.instantiate<Owner>() }
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = ExplicitDomain::class,
            attributes = mapOf(
                "uri" to "exp_dom_1_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "domainCode" to "exp_dom",
                "members" to dynamicAttribute { listOf(it.instantiate<Member>()) },
                "hierarchies" to dynamicAttribute { listOf<Hierarchy>() }
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = Member::class,
            attributes = mapOf(
                "uri" to "mem_1_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "memberCode" to "exp_mc",
                "defaultMember" to true
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = Hierarchy::class,
            attributes = mapOf(
                "uri" to "hie_1_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "hierarchyCode" to "hier_code",
                "rootNodes" to listOf<HierarchyNode>()
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = HierarchyNode::class,
            attributes = mapOf(
                "uri" to "hie_nod_1_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "abstract" to false,
                "comparisonOperator" to "=",
                "unaryOperator" to "+",
                "memberRef" to dpmElementRef<Member>("mem_uri", "diagnostic_label"),
                "childNodes" to listOf<HierarchyNode>()
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = TypedDomain::class,
            attributes = mapOf(
                "uri" to "typ_dom_1_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "domainCode" to "typ_dom",
                "dataType" to "String"
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = ExplicitDimension::class,
            attributes = mapOf(
                "uri" to "exp_dim_1_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "dimensionCode" to "exp_dim",
                "domainRef" to dpmElementRef<ExplicitDomain>("exp_dom_uri", "diagnostic_label")
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = TypedDimension::class,
            attributes = mapOf(
                "uri" to "typ_dim_1_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "dimensionCode" to "typ_dim",
                "domainRef" to dpmElementRef<ExplicitDomain>("typ_dom_uri", "diagnostic_label")
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = Metric::class,
            attributes = mapOf(
                "uri" to "met_1_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "memberCodeNumber" to "1",
                "dataType" to "String",
                "flowType" to "Duration",
                "balanceType" to "Credit",
                "referencedDomainCode" to "exp_dom",
                "referencedHierarchyCode" to "hier_code"
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = DpmDictionary::class,
            attributes = mapOf(
                "owner" to dynamicAttribute { it.instantiate<Owner>() },
                "metrics" to dynamicAttribute { listOf(it.instantiate<Metric>()) },
                "explicitDomains" to dynamicAttribute { listOf(it.instantiate<ExplicitDomain>()) },
                "typedDomains" to dynamicAttribute { listOf(it.instantiate<TypedDomain>()) },
                "explicitDimensions" to dynamicAttribute { listOf(it.instantiate<ExplicitDimension>()) },
                "typedDimensions" to dynamicAttribute { listOf(it.instantiate<TypedDimension>()) }
            )
        )
    )

    return definitions
}
