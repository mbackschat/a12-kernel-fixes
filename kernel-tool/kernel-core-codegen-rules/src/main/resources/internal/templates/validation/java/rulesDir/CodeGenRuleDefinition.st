CodeGenRuleDefinition(t) ::= <<
public void <t.uniqueRuleName>(final MainValidatorController controller, long[] indices)
		throws ValidatorException {	
	<if(t.usbPattern)>
	controller.initFuerRegelpruefung("<t.fullName>",MainValidatorController.PruefErgebnisTyp.<t.ruleType>, true);
	<else>
	controller.initFuerRegelpruefung("<t.fullName>",MainValidatorController.PruefErgebnisTyp.<t.ruleType>, false);
	<endif>

	<if(t.usbPatternForRootGroup)>
	if (!controller.checkUSBIndexFeldFehler()) return;
	<endif>
	<if(t.filterConditions)>
	if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
		return;
	}
	<endif>
	<if(t.usbPattern)>
	String[] usbRootGroups = new String[] { <t.usbRootGroupsCode; separator = ", "> };
	<endif>
	long[] start = {<t.startIndicesCode>};
	long[] ende = {<t.endIndicesCode>};
	<if(t.parallelIteration)>
	String[] iterationGroups = new String[] { <t.parallelIterationGroupsCode; separator = ", "> };
	<endif>

	<if(t.levelIterationOptimization)>
	<if(t.parallelIteration)>
	EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), iterationGroups, controller.getValidationCache());
	<else>
	EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), <t.semanticIndexLevel>);
	<endif>
	<else>
	<if(t.parallelIteration)>
	EbenenIterator iter = new EbenenIterator(start, ende, iterationGroups, controller.getValidationCache());
	<else>
	EbenenIterator iter = new EbenenIterator(start, ende);
	<endif>
	<endif>
	<if(t.iteration)>
	if (!iter.checkAndSetWiederholungen(indices)) {
		return;
	}
	<endif>

	<t.filterParamsCreationCode; separator = "\n">
	<t.filterCode; separator= "\n">
	<t.setOpParamsCreationCode; separator = "\n">
	<t.setOperationCode; separator="\n">

	<t.rtControllerParamsCreationCode; separator = "\n">

	try {
		<t.repetitionNotUniqueInitCode>
		<t.entityListAndValueListInitCode>
		for (IterationState idx : iter) {
			<t.repetitionNotUniqueClearCode>
			if (<t.conditionBooleanCode>) {
			  <if(t.preliminary)>
				controller.preliminaryError("<t.uniqueErrorFieldName>", idx, IFormaleFehlerConstants.<t.formalErrorId>, RuntimeFormalErrorEnum.<t.formalMsgId>, ErrorType.<if(t.omissionError)>OMISSION_ERROR<else>VALUE_ERROR<endif>);
			  <else>
				<if(t.uniqueSemanticIndex4ErrorField4NotIteratingLevel)>
				String semanticIndexValue = <t.semanticIndexValueCode>;
				idx = controller.getValidationCache().replaceSemanticIndexRepetition(idx, "<t.groupOnErrorFieldPathWithSemanticIndexFullName>", <t.semanticIndexLevelInIterationState>, semanticIndexValue);
				<endif>
				<if(t.withOmissionErrors)>
				<t.conditionLineCode>
				<endif>
				<t.addValidatorMessageCode>
			  <endif>
			} 
		}    
	} catch (CustomConditionException cce) {
	    throw cce;
	} catch (Exception e) {
		controller.addValidatorMessage("<t.uniqueErrorFieldName>", iter.get(), <t.errorCode>, e);
	}
}<\n>

>>