CodeGenCalculationAlternative(t) ::= <<
try { // <t.description>
	if (<t.preConditionBooleanCode>) {
		RtInternalIdentifier id = RuntimeController.makeIdentifier(idx, <t.calculatedFieldCode>);
		<if(t.parallelIteration)>
		if (controller.isWiederholungGesetzt(id, <t.parallelIterationLevel>)) {
			controller.handleBerechnetenWert(<t.calcFunctionCode>, id);
		}
		<else>
		controller.handleBerechnetenWert(<t.calcFunctionCode>, id);
		<endif>
		<if(t.useClosureForCalculationCode)>
		return true;
		<else>
		continue;
		<endif>
	}
} catch (CalculationFieldException e) {
	// Berechnung wird unterbrochen, wenn der Wert eines Operators, der selbst berechnet wurde, fehlerhaft ist
	// Wird in CalculationCache geworfen. 
	RtInternalIdentifier id = RuntimeController.makeIdentifier(idx, <t.calculatedFieldCode>);
	controller.markiereAlsFehlerhaftBerechnet(id);
	<if(t.useClosureForCalculationCode)>
	return true;
	<else>
	continue;
	<endif>
} catch (Exception e) {
	RtInternalIdentifier id = RuntimeController.makeIdentifier(idx, <t.calculatedFieldCode>);
	controller.markiereAlsFehlerhaftBerechnet(id);
	controller.addBerechnungsFehler(id);
	<if(t.useClosureForCalculationCode)>
	return true;
	<else>
	continue;
	<endif>
}<\n>
>>