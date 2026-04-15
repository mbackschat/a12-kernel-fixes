CodeGenTeilValidierung_preliminary(t) ::= <<
var TVCheckPreliminary_<t.name> = (function () {
	function TVCheckPreliminary_<t.name>() {
		this._checkObject = R_<t.name>.getInstance();
	}
	TVCheckPreliminary_<t.name>.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		<t.FuehreAusSplitFunctionCodeCalls_Preliminary>
	};
	<t.FuehreAusSplitFunctionCode_Preliminary>
	return TVCheckPreliminary_<t.name>;
}());

>>