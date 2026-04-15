CodeGenDynamicEnumerationValue(t) ::= <<
<if(t.withPreCondition)>
if (!(<t.preConditionBooleanCode>)) {
<\t><endif>values.addAll(controller.handleEnumerationValue(<t.dynEnumCalcCode>));
<if(t.withPreCondition)>
}<\n>
<endif>
>>