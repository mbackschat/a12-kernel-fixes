CodeGenCalculatedField(t) ::= <<
public void calc_<t.uniqueName>(final CalculationController controller)
		throws ValidatorException {	
	/* Berechnung für das Feld '<t.calculatedFieldName>'
	 */
	controller.initFuerBerechnung("<t.calculatedFieldName>");

	<if(t.usbMusterUeberVordrucke)>
	if (!controller.checkUSBIndexFeldFehler()) return;
	<endif>
	<if(t.usbMuster)>
	String[] usbRootGroups = new String[] { <t.usbRootGroupsCode; separator = ", "> };
	<endif>
	long[] start = {<t.startIndicesCode>};
	long[] ende = {<t.endeIndicesCode>};
	<if(t.parallelIteration)>
	String[] iterationGroups = new String[] { <t.parallelIterationGroupsCode> };
	EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), iterationGroups, controller.getValidationCache());
	<else>
	EbenenIterator iter = new EbenenIterator(start, ende, controller.getValidationCache().getGesetzteWiederholungen(), -1);
	<endif>
	
	<t.filterParamsCreationCode; separator = "\n">
	<t.filterCode; separator= "\n">
	
	<t.rtControllerParamsCreationCode; separator = "\n">

	try {
		<t.parameterRepetitionNotUniqueClearCode>
		for (IterationState idx : iter) {
            <! Würde durch die Berechnung ein neuer USB-Vordruck erzeugt, müssten wir auch das USB-Indexfeld setzen.
				Das ist problematisch, weil dieses Feld vom Benutzer editierbar ist, aber effektiv berechnet wird.
				Derartige Vordrucke müssen aber ohnehin durch spezielle Konstrukte abgesichert sein, so dass der Benutzer eine
				Fehlermeldung erhält, wenn er den Vordruck noch nicht angegeben hat. Korrigiert der Benutzer diesen Umstand und fügt
				das USB-Indexfeld in einem neuen Vordruck ein, so werden dann auch bei einer erneuten Berechnung die Berechnungen durchgeführt
				(Nebenbemerkung: Wir gehen davon aus, dass in projAlpha immer usbIndexfelder verwendet werden.) !>
			<if(t.usbMusterUeberVordrucke)>
			if (controller.keinVordruckAngegeben("<t.vordruckBerechnetesFeld>", idx.getIndexes()[1], idx.getIndexes()[0], null).isKnownAndTrue()) {
				// Existiert der USB-Vordruck noch nicht, erzeugen wir ihn auch nicht.
				continue;
			}
			<endif>
			<t.parameterRepetitionNotUniqueClearCode>
			<t.calculationCode>
		}
		<if(t.parallelIteration)>
		controller.markFieldsUsingInvalidIndexGroupsAsIncorrectlyCalculated("<t.uniqueName>", iterationGroups);
		<endif>
	} catch (Exception e) {
		throw new ValidatorException(e.getMessage(), e);
	}
}<\n>
>>