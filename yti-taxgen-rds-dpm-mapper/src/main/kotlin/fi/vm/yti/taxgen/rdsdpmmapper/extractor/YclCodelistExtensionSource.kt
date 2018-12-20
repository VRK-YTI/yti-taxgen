package fi.vm.yti.taxgen.rdsdpmmapper.extractor

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.rdsdpmmapper.DpmMappingContext
import fi.vm.yti.taxgen.rdsdpmmapper.yclmodel.YclExtension
import fi.vm.yti.taxgen.rdsdpmmapper.yclmodel.YclExtensionMember
import fi.vm.yti.taxgen.rdsdpmmapper.yclmodel.YclExtensionMembersCollection
import fi.vm.yti.taxgen.rdsprovider.CodeListExtensionSource

internal fun CodeListExtensionSource.tryExtractDpmHierarchy(
    ctx: DpmMappingContext
): Hierarchy? {
    return ctx.extract(this) {

        val extension = extractSupportedExtension(ctx) ?: return@extract null //TODO - test unsupported extension case

        ctx.diagnostic.updateCurrentContextDetails(
            label = extension.diagnosticLabel()
        )

        val extensionMembers = extractExtensionMembers(ctx)
        val rootNodes = collectHierarchyRootNodes(extensionMembers, ctx)

        Hierarchy(
            id = extension.idOrEmpty(),
            uri = extension.uriOrEmpty(),
            concept = extension.dpmConcept(ctx.owner),
            hierarchyCode = extension.codeValueOrEmpty(),
            rootNodes = rootNodes
        )
    }
}

private fun CodeListExtensionSource.extractSupportedExtension(
    ctx: DpmMappingContext
): YclExtension? {
    val extension = JsonOps.readValue<YclExtension>(extensionMetaData(), ctx.diagnostic)

    return when (extension.propertyType?.uri) {
        "http://uri.suomi.fi/datamodel/ns/code#definitionHierarchy" -> extension
        "http://uri.suomi.fi/datamodel/ns/code#calculationHierarchy" -> extension
        else -> null
    }
}

private fun CodeListExtensionSource.extractExtensionMembers(
    ctx: DpmMappingContext
): List<YclExtensionMember> {
    return extensionMemberPagesData()
        .map { data ->
            val extensionsCollection = JsonOps.readValue<YclExtensionMembersCollection>(data, ctx.diagnostic)
            extensionsCollection.results ?: ctx.diagnostic.fatal("Missing YCL Extension Members")
        }
        .flatten()
        .toList()
}

private fun collectHierarchyRootNodes(
    extensionMembers: List<YclExtensionMember>,
    ctx: DpmMappingContext
): List<HierarchyNode> {

    return extensionMembers
        .filter { it.isRootMember() }
        .map {
            hierarchyNode(
                it,
                collectHierarchyChildNodes(
                    parentMember = it,
                    extensionMembers = extensionMembers,
                    ctx = ctx
                ),
                ctx
            )
        }
}

private fun collectHierarchyChildNodes(
    parentMember: YclExtensionMember,
    extensionMembers: List<YclExtensionMember>,
    ctx: DpmMappingContext
): List<HierarchyNode> {
    return extensionMembers
        .filter { it.isChildOf(parentMember) }
        .map {
            hierarchyNode(
                it,
                collectHierarchyChildNodes(
                    parentMember = it,
                    extensionMembers = extensionMembers,
                    ctx = ctx
                ),
                ctx
            )
        }
}

private fun hierarchyNode(
    extensionMember: YclExtensionMember,
    childNodes: List<HierarchyNode>,
    ctx: DpmMappingContext
): HierarchyNode {
    return ctx.extract(extensionMember) {
        HierarchyNode(
            id = extensionMember.idOrEmpty(),
            uri = extensionMember.uriOrEmpty(),
            concept = extensionMember.dpmConcept(ctx.owner),
            abstract = false,
            comparisonOperator = extensionMember.comparisonOpOrNull(),
            unaryOperator = extensionMember.unaryOpOrNull(),
            memberRef = extensionMember.memberRef(),
            childNodes = childNodes
        )
    }
}