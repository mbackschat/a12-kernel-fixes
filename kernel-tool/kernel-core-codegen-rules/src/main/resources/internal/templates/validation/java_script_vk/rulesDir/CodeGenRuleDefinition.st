CodeGenRuleDefinition(t) ::= <<
R_<t.mTVA>.prototype.<t.uniqueRuleName> = function (controller, indices) {
	controller.initFuerRegelpruefung("<t.fullName>", PruefErgebnisTyp.<t.ruleType>);
	<if(t.filterConditions)>
	if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
		return;
	}
	<endif>
	var start = [<t.startIndicesCode>];
	var ende = [<t.endIndicesCode>];
	<if(t.parallelIteration)>
	var iterationGroups = [<t.parallelIterationGroupsCode; separator = ", ">];
	<endif>
	
	<if (t.levelIterationOptimization)>
	<if(t.parallelIteration)>
	var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), iterationGroups, controller.getValidationCache());
	<else>
	var iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), <t.semanticIndexLevel>);
	<endif>
	<else>
	<if(t.parallelIteration)>
	var iter = new EbenenIterator(start, ende, iterationGroups, controller.getValidationCache());
	<else>
	var iter = new EbenenIterator(start, ende);
	<endif>
	<endif>
	<if (t.iteration)>
	if (!iter.checkAndSetWiederholungen(indices)) {
		return;
	}
	<endif>

	<t.filterParamsCreationCode; separator = "\n">
	<t.filterCode; separator= "\n">
	<t.rtControllerParamsCreationCode; separator = "\n">

	try {
		<t.repetitionNotUniqueInitCode>
		<t.entityListAndValueListInitCode>
		var innerIter = iter.iterator();
		while (innerIter.hasNext()) {
			var idx = innerIter.next();
			<t.repetitionNotUniqueClearCode>
			if (<t.conditionBooleanCode>) {
				<if(t.preliminary)>
					controller.preliminaryError("<t.uniqueErrorFieldName>", idx, IFormaleFehlerConstants.<t.formalErrorId>, RuntimeFormalErrorEnum.<t.formalMsgId>, ErrorType.<if(t.omissionError)>OMISSION_ERROR<else>VALUE_ERROR<endif>);
				<else>
					<if(t.uniqueSemanticIndex4ErrorField4NotIteratingLevel)>
					var semanticIndexValue = <t.semanticIndexValueCode>;
					idx = controller.getValidationCache().replaceSemanticIndexRepetition(idx, "<t.groupOnErrorFieldPathWithSemanticIndexFullName>", <t.semanticIndexLevelInIterationState>, semanticIndexValue);
					<endif>
					<if(t.withOmissionErrors)>
					<t.conditionLineCode>
					<endif>
					<t.addValidatorMessageCode>
				<endif>
			}
		}
	}
	catch (e) {
	    if (e instanceof CustomConditionException) {
	        throw e;
	    } else {
		    controller.addValidatorMessage("<t.uniqueErrorFieldName>", iter.get(), <t.errorCode>, e);
		}
	}
};<\n>
>>