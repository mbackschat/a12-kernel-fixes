CodeGenTeilValidierung_all(t) ::= <<
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Infos, Hinweisen und Fehlern.
 *
 */
class <t.groovyPackageClassnamePrefix>TVCheckAlle_<t.name> implements ITeilValidierungChecker {

	private <t.groovyPackageClassnamePrefix>TVCheckFehler_<t.name> tvFehlerChecker = new <t.groovyPackageClassnamePrefix>TVCheckFehler_<t.name>();
	private <t.groovyPackageClassnamePrefix>TVCheckHinweis_<t.name> tvHinweisChecker = new <t.groovyPackageClassnamePrefix>TVCheckHinweis_<t.name>();
	private <t.groovyPackageClassnamePrefix>TVCheckInfo_<t.name> tvInfoChecker = new <t.groovyPackageClassnamePrefix>TVCheckInfo_<t.name>();

	public <t.groovyPackageClassnamePrefix>TVCheckAlle_<t.name>() {
	}

	public void fuehreAus(MainValidatorController controller, int interneFeldNr, long[] indices) {
		tvFehlerChecker.fuehreAus(controller, interneFeldNr, indices);
		tvHinweisChecker.fuehreAus(controller, interneFeldNr, indices);
		tvInfoChecker.fuehreAus(controller, interneFeldNr, indices);
	}
}

>>