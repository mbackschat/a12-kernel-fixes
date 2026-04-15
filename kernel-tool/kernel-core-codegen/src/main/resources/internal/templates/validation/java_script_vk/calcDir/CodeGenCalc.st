CodeGenCalc(t) ::= <<
var C_<t.name> = (function () {
	function C_<t.name>() {
		if (C_<t.name>._checkObject) {
			throw new Error("Instantiation failed: Use C_<t.name>.getInstance() instead of new.");
		}
		C_<t.name>._checkObject = this;
		this._feldData = Meta_<t.name>.getInstance();
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}
	C_<t.name>.getInstance = function () {
		return this._checkObject;
	};

	<t.berechneteFelderCode>

	C_<t.name>.CALCULATED_FIELD_DEPENDENCIES = <t.dependencyGraph>;

	C_<t.name>.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS = <t.expandedOperandFieldsOfCalculations>;

	C_<t.name>.CALC_FUNC_NAME_MAP = <t.calcFuncNameMap>;

	C_<t.name>.CALCULATED_FIELD_FULL_NAMES_IN_ORDER = <t.calculatedFieldFullNamesInOrder>;

	C_<t.name>.prototype.executeCalculation = function(fullFieldName, controller) {
	    const startMesspunkt = controller.startMesspunkt();
	    const calcFuncName = C_<t.name>.CALC_FUNC_NAME_MAP.get(fullFieldName);
	    this[calcFuncName](controller);
	    controller.logMessung(calcFuncName, startMesspunkt);
    };

    C_<t.name>.prototype.hasCalculation = function(fullFieldName) {
        return C_<t.name>.CALC_FUNC_NAME_MAP.has(fullFieldName);
    };

    C_<t.name>.prototype.getDependencyGraph = function() {
        return C_<t.name>.CALCULATED_FIELD_DEPENDENCIES;
    };

	C_<t.name>.prototype.getExpandedOperandFieldsOfCalculations = function() {
        return C_<t.name>.EXPANDED_OPERAND_FIELDS_OF_CALCULATIONS;
    }

    C_<t.name>.prototype.getCalculatedFieldFullNamesInOrder = function() {
        return C_<t.name>.CALCULATED_FIELD_FULL_NAMES_IN_ORDER;
    }

	C_<t.name>._checkObject = new C_<t.name>();
	return C_<t.name>;
}());

>>