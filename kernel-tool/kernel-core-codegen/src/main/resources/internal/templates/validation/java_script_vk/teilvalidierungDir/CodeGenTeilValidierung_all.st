CodeGenTeilValidierung_all(t) ::= <<
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Infos, Hinweisen und Fehlern.
 *
 */
var TVCheckAlle_<t.name> = (function () {
	function TVCheckAlle_<t.name>() {
		this._tvFehlerChecker = new TVCheckFehler_<t.name>();
		this._tvHinweisChecker = new TVCheckHinweis_<t.name>();
		this._tvInfoChecker = new TVCheckInfo_<t.name>();
	}
	TVCheckAlle_<t.name>.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		this._tvFehlerChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvHinweisChecker.fuehreAus(controller, interneFeldNr, indices);
		this._tvInfoChecker.fuehreAus(controller, interneFeldNr, indices);
	};
	return TVCheckAlle_<t.name>;
}());

>>