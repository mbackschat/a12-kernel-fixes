CodeGenModel_er(t) ::= <<
var ERValidator_<t.name> = (function () {
	function ERValidator_<t.name>() {
		if (ERValidator_<t.name>._instance) {
			throw new Error("Error: Instantiation failed: Use ERValidator_<t.name>.getInstance() instead of new.");
		}
		ERValidator_<t.name>._instance = this;
	}
	ERValidator_<t.name>.getInstance = function () {
		return ERValidator_<t.name>._instance;
	};
	ERValidator_<t.name>.prototype.pruefeParams = function (daten, ergebnis, logger, modus) {
		if (!daten) {
			throw new Error("Übergebene Daten dürfen nicht null sein");
		}
		if (!logger) {
			throw new Error("Übergebener Logger darf nicht null sein");
		}
		if (!ergebnis) {
			throw new Error("Übergebenes Ergebnis darf nicht null sein");
		}
	};
	ERValidator_<t.name>.prototype.validatePartlyWith3ValueLogic = function (daten, relevantEntities, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_<t.name>.getInstance(), modus, ERValidator_<t.name>._metaData, logger);
        c.processPartlyValidation(daten, relevantEntities, ergebnis);
	}
	ERValidator_<t.name>.prototype.validateAll = function (daten, ergebnis, modus, logger) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new ValidationCommand(R_<t.name>.getInstance(), modus, ERValidator_<t.name>._metaData, logger);
        c.processFullValidation(daten, ergebnis);
	};
	ERValidator_<t.name>.prototype.calculateAll = function (daten, modus, ergebnis, logger, externalCalculations, changedFieldInstances, forceCalculationSorting) {
        this.pruefeParams(daten, ergebnis, logger, modus);
        var c = new CalculationCommand(R_<t.name>.getInstance(), modus, ERValidator_<t.name>._metaData, logger, C_<t.name>.getInstance(), externalCalculations, changedFieldInstances, forceCalculationSorting);
        var result = c.processCalc(daten, ergebnis);
        return result;
	};
	ERValidator_<t.name>._instance = new ERValidator_<t.name>();
	ERValidator_<t.name>._metaData = Meta_<t.name>.getInstance().getMetaDataValidierung();
	// typescript has no static initializer, so this is used
	ERValidator_<t.name>._constructor = (function () {
	})();
	return ERValidator_<t.name>;
}());

return {
	validator: ERValidator_<t.name>,
	meta: Meta_<t.name>
};

>>