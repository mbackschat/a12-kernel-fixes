CodeGenTeilValidierung_warning(t) ::= <<
var TVCheckHinweis_<t.name> = (function () {
	function TVCheckHinweis_<t.name>() {
		this._checkObject = R_<t.name>.getInstance();
	}
	TVCheckHinweis_<t.name>.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		<t.FuehreAusSplitFunctionCodeCalls_Hinweis>
	};
	<t.FuehreAusSplitFunctionCode_Hinweis>
	return TVCheckHinweis_<t.name>;
}());

>>