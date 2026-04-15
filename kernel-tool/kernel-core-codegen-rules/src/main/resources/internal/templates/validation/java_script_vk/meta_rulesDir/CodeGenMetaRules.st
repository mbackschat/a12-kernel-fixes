CodeGenMetaRules(t) ::= <<
var Meta_Regel_<t.name> = (function () {
	function Meta_Regel_<t.name>() {
		var _this = this;
		this._regeln = new Array();
		// speichert pro Regel die referenzierten Identifier
		this._refIdentifier = new Array(Meta_Regel_<t.name>.ANZAHL_REGELN);
		// speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
		this._refAuslassungsIdentifier = new Array(Meta_Regel_<t.name>.ANZAHL_REGELN);
		// speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
		this._refAuslassungsVordrucke = new Array(Meta_Regel_<t.name>.ANZAHL_REGELN);
		this._mehrsprachigeFehlertexte = new Map();
		this._fehlertexteParameterMapping = new Map();
		this._constructor = (function () {
			for (var i = 0; i \< Meta_Regel_<t.name>.ANZAHL_REGELN; i++) {
				_this._mehrsprachigeFehlertexte.set(i, new Map());
			}
		})();
		<t.metaRegelRefsMvkJsCodeAufruf>
		<t.metaRegelFehlertexteMvkJsCodeAufruf>
		<t.metaRegelnMvkJsCodeAufruf>
	}
	Meta_Regel_<t.name>.prototype.getRegeln = function () {
		return this._regeln;
	};
	Meta_Regel_<t.name>.ANZAHL_REGELN = <t.anzRegeln>;

	<t.regelMethodRefsMvkJsCode>

	<t.metaRegelFehlertexteMvkJsCode>

	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */
	<t.regelMethodMvkJsCode>

	return Meta_Regel_<t.name>;
}());

>>