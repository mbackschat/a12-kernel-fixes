CodeGenCalc(t) ::= <<

class <t.groovyPackageClassnamePrefix>C_<t.name> implements ICalculatorIntern {
	private static final <t.groovyPackageClassnamePrefix>C_<t.name> calcObject = new <t.groovyPackageClassnamePrefix>C_<t.name>();
	<t.calcObjectCreationsCode>

	private static <t.groovyPackageClassnamePrefix>Meta_<t.name> feldData = <t.groovyPackageClassnamePrefix>Meta_<t.name>.getInstance();
	private static IMetaDataValidierung metaDataValidierung = feldData.getMetaDataValidierung();
	private static Set\<String\> expandedOperandFieldsOfCalculations = <t.expandedOperandFieldsOfCalculations>;

	public static <t.groovyPackageClassnamePrefix>C_<t.name> getInstance() {
		return calcObject;
	}

	public Set\<String\> getExpandedOperandFieldsOfCalculations() {
		 return expandedOperandFieldsOfCalculations;
	}

	public void calculate(CalculationController controller) {
		long startMesspunkt;
		<t.berechneteFelderAufrufCode>
	}
}
<t.calcObjectClasses>
>>