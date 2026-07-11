CodeGenModel_er(t) ::= <<
class <t.groovyPackageClassnamePrefix>ERValidator_<t.name> implements IValidator, ICalculator {
	// if you change the order of these two lines consider the order of (static) initializer execution carefully
	private static final IMetaDataValidierung metaData = <t.groovyPackageClassnamePrefix>Meta_<t.name>.getInstance().getMetaDataValidierung();
	private static final <t.groovyPackageClassnamePrefix>ERValidator_<t.name> m_instance = new <t.groovyPackageClassnamePrefix>ERValidator_<t.name>();

	private <t.groovyPackageClassnamePrefix>ERValidator_<t.name> () {
	}

	public static <t.groovyPackageClassnamePrefix>ERValidator_<t.name> getInstance() {
		return m_instance;
	}

	private void pruefeParams(IData daten, IResult ergebnis, ValidationMode modus) {
		if (daten == null) {
			throw new IllegalArgumentException("Übergebene Daten dürfen nicht null sein");
		}
		if (ergebnis == null) {
			throw new IllegalArgumentException("Übergebenes Ergebnis darf nicht null sein");
		}
		if (modus != null && !<t.groovyPackageClassnamePrefix>Meta_<t.name>.getInstance().isSpracheUnterstuetzt(modus.getLanguage())) {
			throw new IllegalArgumentException("Die im ValidierungsModus übergebene Sprache " + modus.getLanguage() + " wird nicht unterstützt.");
		}
	} 

	public void validatePartlyWith3ValueLogic(
			IData daten,
			Set\<IIdentifier\> relevantEntities,
			IResult ergebnis,
			ValidationMode modus) throws ValidatorException {
		pruefeParams(daten, ergebnis, modus);
		ValidationCommand c = new ValidationCommand(<t.groovyPackageClassnamePrefix>R_<t.name>.getInstance(), modus, metaData, daten);
		c.processPartlyValidation(daten, relevantEntities, ergebnis);
	}

	public void validateAll(IData daten,
			IResult ergebnis,
			ValidationMode modus) throws ValidatorException {
		pruefeParams(daten, ergebnis, modus);
		ValidationCommand c = new ValidationCommand(<t.groovyPackageClassnamePrefix>R_<t.name>.getInstance(), modus, metaData, daten);
		c.processFullValidation(daten, ergebnis);
	}

	public ICalculationResult calculateAll(IData daten, ValidationMode modus, IResult ergebnis) {
		pruefeParams(daten, ergebnis, modus);
		CalculationCommand c = new CalculationCommand(<t.groovyPackageClassnamePrefix>R_<t.name>.getInstance(), modus, metaData, daten, <t.groovyPackageClassnamePrefix>C_<t.name>.getInstance());
		ICalculationResult result = c.processCalc(daten, ergebnis);
		return result;
	}
}

>>