CodeGenCalculationAlternative(t) ::= <<
try {// <t.description>
	if (<t.preConditionBooleanCode>) {
		var id = RuntimeController.makeIdentifier(idx, <t.calculatedFieldCode>);
		<if(t.parallelIteration)>
		if (controller.isWiederholungGesetzt(id, <t.parallelIterationLevel>)) {
			controller.handleBerechnetenWert(<t.calcFunctionCode>, id);
		}
		<else>
		controller.handleBerechnetenWert(<t.calcFunctionCode>, id);
		<endif>
		continue;
	}
} catch (e) {
	var id = RuntimeController.makeIdentifier(idx, <t.calculatedFieldCode>);
	controller.markiereAlsFehlerhaftBerechnet(id);
	continue;
}<\n>
>>