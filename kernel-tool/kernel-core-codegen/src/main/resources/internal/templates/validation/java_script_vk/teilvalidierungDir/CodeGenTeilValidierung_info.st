CodeGenTeilValidierung_info(t) ::= <<
var TVCheckInfo_<t.name> = (function () {
	function TVCheckInfo_<t.name>() {
		this._checkObject = R_<t.name>.getInstance();
	}
	TVCheckInfo_<t.name>.prototype.fuehreAus = function (controller, interneFeldNr, indices) {
		<t.FuehreAusSplitFunctionCodeCalls_Info>
	};
	<t.FuehreAusSplitFunctionCode_Info>
	return TVCheckInfo_<t.name>;
}());

>>