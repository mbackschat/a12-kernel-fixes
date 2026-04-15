EntityId_varAssignment(t) ::= <<
RtIdentifierTemplate <t.variableName> = RtIdentifierTemplate.builder().unqNm("<t.uniqueName>").isField(<t.field>)<if(t.hasSemanticIndex)>.semIdx(<t.SemanticIndexCodeRef>)<endif><if(t.hasCategory)>.category("<t.categoryName>")<endif><if(t.outer)>.outer(true)<endif>.usb(<t.usbCode>).idx(<t.allLayerSpecs; separator = ").idx(">)<\\>
<if(t.filterCondition)><\\>
.filter(<t.FilterConditionName>)<\\>
<endif>.build();
>>