CodeGenTeilValidierung_error(t) ::= <<
/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Fehlern.
 *
 */
var TVCheckFehler_<t.name> = (function () {
	function TVCheckFehler_<t.name>() {
		this._checkObject = R_<t.name>.getInstance();
	}
	TVCheckFehler_<t.name>.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		<t.FuehreAusSplitFunctionCodeCalls_Fehler>
	};

	<t.FuehreAusSplitFunctionCode_Fehler>

	return TVCheckFehler_<t.name>;
}());

>>