CodeGenDynamicEnumerationValue(t) ::= <<
<if(t.withPreCondition)>
if (!(<t.preConditionBooleanCode>)) {
	<endif>
	var values = controller.handleEnumerationValue(<t.dynEnumCalcCode>, id);
	values.forEach(function(v) { xmlValues.push(v); });
<if(t.withPreCondition)>
}<\n>
<endif>
>>