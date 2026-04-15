CodeGenCalculatedField(t) ::= <<
C_<t.name>.prototype.calc_<t.uniqueName> = function (controller) {
	// Berechnung für das Feld '<t.calculatedFieldName>'
	var start = [<t.startIndicesCode>];
	var ende = [<t.endeIndicesCode>];
	<if(t.parallelIteration)>
	var iterationGroups = [<t.parallelIterationGroupsCode>];
	var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), iterationGroups, controller.getValidationCache());
	<else>
	var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
	<endif>
	
	<t.filterParamsCreationCode; separator = "\n">
	<t.filterCode; separator= "\n">
	
	<t.rtControllerParamsCreationCode; separator = "\n">
	try {
		var innerIterator = iter.iterator();
		while (innerIterator.hasNext()) {
			var idx = innerIterator.next();
			<t.parameterRepetitionNotUniqueClearCode>
			<t.calculationCode>
		}
        <if(t.parallelIteration)>
		controller.markFieldsUsingInvalidIndexGroupsAsIncorrectlyCalculated("<t.uniqueName>", iterationGroups);
        <endif>
	}
	catch (e) {
		throw new ValidatorException(e.message, e);
	}
};<\n>
>>