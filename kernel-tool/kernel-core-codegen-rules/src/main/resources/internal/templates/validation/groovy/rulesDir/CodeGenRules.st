CodeGenRules(t) ::= <<
class <t.groovyPackageClassnamePrefix>R_<t.name> implements IValidatorIntern {
	private static final <t.groovyPackageClassnamePrefix>R_<t.name> checkObject = new <t.groovyPackageClassnamePrefix>R_<t.name>();
	<t.ruleCheckObjectCreationsCode>

	private static <t.groovyPackageClassnamePrefix>Meta_<t.name> feldData = <t.groovyPackageClassnamePrefix>Meta_<t.name>.getInstance();
	private static IMetaDataValidierung metaDataValidierung = feldData.getMetaDataValidierung();

	public static <t.groovyPackageClassnamePrefix>R_<t.name> getInstance() {
		return checkObject;
	}

	<t.rulesPerRootGroupMethodsCode>

	<t.preliminaryRulesPerErrorFieldMethodsCode; separator="\n">

    private final Map\<String, Consumer\<MainValidatorController\>\> uniqueFieldNameToPreliminaryValidationFunc = Map.ofEntries(
        <t.uniqueFieldNamesWithPreliminaryRules:{uniqueFieldName|Map.entry("<uniqueFieldName>", this.&validatePreliminaryRulesForField__<uniqueFieldName>)}; separator=",\n">
    );

    public void validatePreliminaryRulesForField(MainValidatorController controller, String uniqueFieldName) {
        def validationFunc = uniqueFieldNameToPreliminaryValidationFunc.get(uniqueFieldName);
        if (validationFunc != null) {
            validationFunc(controller);
        }
    }

	public void validatePreliminaryRules(MainValidatorController controller) {
		<t.preliminaryRulesPerRootGroupCalls>
	}

	public void validiereVoll(MainValidatorController controller, ValidationMode modus) {
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				alleRegeln(controller);
				break;
			case RuleType.INFO:
                alleInfoRegeln(controller);
                break;
            case RuleType.HINT:
				alleHinweisRegeln(controller);
				break;
			case RuleType.ERROR:
				alleFehlerRegeln(controller);
				break;
			default:
				throw new IllegalArgumentException("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
	}

	public void validiereTeil(MainValidatorController controller, ValidationMode modus, Map\<String, RtInternalIdentifier\> pruefungsFelderMap) {
		ITeilValidierungChecker checker = null;
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				checker = new <t.groovyPackageClassnamePrefix>TVCheckAlle_<t.name>();
				break;
			case RuleType.INFO:
                checker = new <t.groovyPackageClassnamePrefix>TVCheckInfo_<t.name>();
                break;
            case RuleType.HINT:
				checker = new <t.groovyPackageClassnamePrefix>TVCheckHinweis_<t.name>();
				break;
			case RuleType.ERROR: 
				checker = new <t.groovyPackageClassnamePrefix>TVCheckFehler_<t.name>();
				break;
			default: 
				throw new IllegalArgumentException("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}

		Collection\<RtInternalIdentifier\> felder = pruefungsFelderMap.values();
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			ITeilValidierungChecker preChecker = new <t.groovyPackageClassnamePrefix>TVCheckPreliminary_<t.name>();
	
			for (RtInternalIdentifier id : felder) {
				int interneFeldNr = metaDataValidierung.getInterneFeldNr(id.getName());
				preChecker.fuehreAus(controller, interneFeldNr, id.getIndices());
			}
		}

		for (RtInternalIdentifier id : felder) {
			int interneFeldNr = metaDataValidierung.getInterneFeldNr(id.getName());
			checker.fuehreAus(controller, interneFeldNr, id.getIndices());
		}
	}

	private void alleRegeln(MainValidatorController controller) 
					throws ValidatorException { 
		//Aufruf aller Regeln 
		<t.rulesPerRootGroupCalls>
	}

	private void alleFehlerRegeln(MainValidatorController controller) 
					throws ValidatorException { 
		//Aufruf aller Fehler-Regeln 
		<t.errorRulesPerRootGroupCalls>
	}

	private void alleHinweisRegeln(MainValidatorController controller) 
					throws ValidatorException { 
		//Aufruf aller Hinweis-Regeln 
		<t.warningRulesPerRootGroupCalls>
	}
	
	private void alleInfoRegeln(MainValidatorController controller) 
                    throws ValidatorException { 
        //Aufruf aller Info-Regeln 
        <t.infoRulesPerRootGroupCalls>
    }
}
<t.ruleCheckObjectClasses>
>>