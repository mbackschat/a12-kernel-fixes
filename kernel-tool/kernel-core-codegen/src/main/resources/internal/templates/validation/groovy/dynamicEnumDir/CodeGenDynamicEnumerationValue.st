CodeGenDynamicEnumerationValue(t) ::= <<
<if(t.withPreCondition)>
if (!(<t.preConditionBooleanCode>)) {
	<endif>xmlValues.addAll(controller.handleEnumerationValue(<t.dynEnumCalcCode>));
<if(t.withPreCondition)>
}<\n>
<endif>
>>