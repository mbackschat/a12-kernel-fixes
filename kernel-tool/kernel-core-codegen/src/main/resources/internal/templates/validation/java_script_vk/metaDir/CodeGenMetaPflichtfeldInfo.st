CodeGenMetaPflichtfeldInfo(t) ::= <<
var Meta_PfFeld_<t.name> = (function () {
	function Meta_PfFeld_<t.name>(metaDataValidierung) {
		this._metaDataValidierung = metaDataValidierung;
	}
	Meta_PfFeld_<t.name>.prototype.addLokalePflichtFeldInfos = function (additivUndPflichtfelder) {
		var _this = this;
		var infos = [];
		<t.lokalePflichtFeldInfoMethodenAufrufCode>
		infos.forEach(function (info) {
			_this._addLokalePflichtFeldInfo(additivUndPflichtfelder, getMetaFSMenge(info[0]), getMetaFSMenge(info[1]));
		});
	};
	
	<t.lokalePflichtFeldInfoMethodenCode>
	
	Meta_PfFeld_<t.name>.prototype._addLokalePflichtFeldInfo = function (additivUndPflichtfelder, additiv, pflichtfelder) {
		var info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this._metaDataValidierung, additiv, pflichtfelder);
		additivUndPflichtfelder.push(info);
	};
	return Meta_PfFeld_<t.name>;
}());

>>