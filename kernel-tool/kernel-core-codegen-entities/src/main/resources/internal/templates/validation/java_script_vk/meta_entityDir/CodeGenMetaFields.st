CodeGenMetaFields(t) ::= <<
var Meta_Felder_<t.name> = (function () {
	function Meta_Felder_<t.name>() {
		this._felder = new Array();
		this._metaFormate = new Map();
		this._mehrsprachigeFehlertexte = new Map();
		this._errorTexts4Req = new Map();
		this._enumerationOberflaechenWerte = new Map();
		this._enumerationCategoryValues = new Map();
		this._stringHintLists = new Map();
		<t.metaFeldFehlertexteJsVkCodeAufruf>
		<t.errorTexts4ReqJsVkCodeAufruf>
		<t.metaEnumerationJsVkCodeAufruf>
		<t.metaEnumerationCategoryValuesJsVkCodeAufruf>
		<t.metaStringHintListsJsVkCodeAufruf>
		<t.metaFeldtypenJsVkCodeAufruf>
		<t.metaFelderJsVkCodeAufruf>
	}
	Meta_Felder_<t.name>.prototype.getFelder = function () {
		return this._felder;
	};

    <t.errorTexts4ReqJsVkCode>
	<t.metaFelderCode>

	return Meta_Felder_<t.name>;
}());

>>