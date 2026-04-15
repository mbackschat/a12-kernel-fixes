CodeGenRules(t) ::= <<
var R_<t.name> = (function () {
	function R_<t.name>() {
		this._feldData = Meta_<t.name>.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	R_<t.name>.getInstance = function () {
		return R_<t.name>._checkObject;
	};
	<t.ruleDefinitionsCode>

	<t.rulesPerRootGroupMethodsCode>

	<t.preliminaryRulesPerErrorFieldMethodsCode; separator="\n">

	R_<t.name>.prototype.validatePreliminaryRulesForField = function(controller, uniqueFieldName) {
		const validationFunc = R_<t.name>["validatePreliminaryRulesForField__" + uniqueFieldName];
		if (validationFunc != undefined) {
			validationFunc.call(R_<t.name>, controller);
		}
	};

	R_<t.name>.prototype.validatePreliminaryRules = function(controller) {
		<t.preliminaryRulesPerRootGroupCalls>};

	R_<t.name>.prototype.validiereVoll = function (controller, modus) {
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				this.alleRegeln(controller);
				break;
			case RuleType.INFO:
                this.alleInfoRegeln(controller);
                break;
            case RuleType.HINT:
				this.alleHinweisRegeln(controller);
				break;
			case RuleType.ERROR:
				this.alleFehlerRegeln(controller);
				break;
			default:
				throw new Error("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
	};
	R_<t.name>.prototype.validiereTeil = function (controller, modus, pruefungsFelderMap) {
		var _this = this;
		var checker;
		switch (modus.getRuleType()) {
			case RuleType.ALL:
				checker = new TVCheckAlle_<t.name>();
				break;
			case RuleType.INFO:
                checker = new TVCheckInfo_<t.name>();
                break;
            case RuleType.HINT:
				checker = new TVCheckHinweis_<t.name>();
				break;
			case RuleType.ERROR:
				checker = new TVCheckFehler_<t.name>();
				break;
			default:
				throw new Error("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
		var felder = Array.from(pruefungsFelderMap.values());
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			var preChecker = new TVCheckPreliminary_<t.name>();
			felder.forEach(function (id) {
				var interneFeldNr = _this._metaDataValidierung.getInterneFeldNr(id.getName());
				preChecker.fuehreAus(controller, interneFeldNr, id.getIndices());
			});
		}
		felder.forEach(function (id) {
			var interneFeldNr = _this._metaDataValidierung.getInterneFeldNr(id.getName());
			checker.fuehreAus(controller, interneFeldNr, id.getIndices());
		});
	};
	R_<t.name>.prototype.alleRegeln = function (controller) {
		// Aufruf aller Regeln
		<t.rulesPerRootGroupCalls>};
	R_<t.name>.prototype.alleFehlerRegeln = function (controller) {
		// Aufruf aller Fehler-Regeln
		<t.errorRulesPerRootGroupCalls>};
	R_<t.name>.prototype.alleHinweisRegeln = function (controller) {
		// Aufruf aller Hinweis-Regeln
		<t.warningRulesPerRootGroupCalls>};
	R_<t.name>.prototype.alleInfoRegeln = function (controller) {
        // Aufruf aller Info-Regeln
        <t.infoRulesPerRootGroupCalls>};
	R_<t.name>._checkObject = new R_<t.name>();
	return R_<t.name>;
}());

>>