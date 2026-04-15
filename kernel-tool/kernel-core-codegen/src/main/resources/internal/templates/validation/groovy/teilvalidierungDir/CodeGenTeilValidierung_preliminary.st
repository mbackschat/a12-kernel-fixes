CodeGenTeilValidierung_preliminary(t) ::= <<
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Vorprüfungen (z.B. Pflichtfeldprüfung).
 *
 */
class <t.groovyPackageClassnamePrefix>TVCheckPreliminary_<t.name> implements ITeilValidierungChecker {

	private <t.groovyPackageClassnamePrefix>R_<t.name> checkObject = <t.groovyPackageClassnamePrefix>R_<t.name>.getInstance();
	<t.ruleCheckObjectCreationsCode>

	public <t.groovyPackageClassnamePrefix>TVCheckPreliminary_<t.name>() {
	}

	public void fuehreAus(MainValidatorController controller, int interneFeldNr, long[] indices) {
		<t.FuehreAusSplitFunctionCodeCalls_Preliminary>
	}

	<t.FuehreAusSplitFunctionCode_Preliminary>
}

>>