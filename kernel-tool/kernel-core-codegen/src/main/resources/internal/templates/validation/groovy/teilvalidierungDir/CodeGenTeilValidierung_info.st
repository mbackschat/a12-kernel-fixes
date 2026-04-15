CodeGenTeilValidierung_info(t) ::= <<
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen.
 *
 */
class <t.groovyPackageClassnamePrefix>TVCheckInfo_<t.name> implements ITeilValidierungChecker {

	private <t.groovyPackageClassnamePrefix>R_<t.name> checkObject = <t.groovyPackageClassnamePrefix>R_<t.name>.getInstance();
	<t.ruleCheckObjectCreationsCode>

	public <t.groovyPackageClassnamePrefix>TVCheckInfo_<t.name>() {
	}


	public void fuehreAus(MainValidatorController controller, int interneFeldNr, long[] indices) {
		<t.FuehreAusSplitFunctionCodeCalls_Info>
	}

	<t.FuehreAusSplitFunctionCode_Info>
}

>>