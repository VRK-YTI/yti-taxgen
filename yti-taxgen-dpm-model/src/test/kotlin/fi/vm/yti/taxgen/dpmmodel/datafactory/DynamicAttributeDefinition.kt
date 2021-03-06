package fi.vm.yti.taxgen.dpmmodel.datafactory

fun dynamicAttribute(valueMaker: (DynamicAttributeContext) -> Any?): DynamicAttributeDefinition =
    DynamicAttributeDefinition(valueMaker)

data class DynamicAttributeDefinition(val valueMaker: (DynamicAttributeContext) -> Any?)
