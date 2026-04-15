CodeGenTeilValidierung_error(t) ::= <<
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Fehlern.
 *
 */
class <t.groovyPackageClassnamePrefix>TVCheckFehler_<t.name> implements ITeilValidierungChecker {

	private <t.groovyPackageClassnamePrefix>R_<t.name> checkObject = <t.groovyPackageClassnamePrefix>R_<t.name>.getInstance();
	<t.ruleCheckObjectCreationsCode>

	public <t.groovyPackageClassnamePrefix>TVCheckFehler_<t.name>() {
	}

	public void fuehreAus(MainValidatorController controller, int interneFeldNr, long[] indices) {
		<t.FuehreAusSplitFunctionCodeCalls_Fehler>
	}

	<t.FuehreAusSplitFunctionCode_Fehler>
}

>>