CodeGenDynamicEnumerations(t) ::= <<
var E_<t.name> = (function () {
	function E_<t.name>() {
		if (E_<t.name>._valueCalcObject) {
			throw new Error("Instantiation failed: Use E_<t.name>.getInstance() instead of new.");
		}
		E_<t.name>._valueCalcObject = this;
		<! We don't need this on its own, but for the next line !>
		this._feldData = Meta_<t.name>.getInstance();
		<! We need this for the BaseYear parameter !>
		this._metaDataValidierung = this._feldData.getMetaDataValidierung();
	}	
	E_<t.name>.getInstance = function () {
		return this._valueCalcObject;
	};

	<t.dynEnumCalcCode>

	E_<t.name>.prototype.calcEnumerationValues = function (field, controller) {
		var startMesspunkt = controller.startMesspunkt();
		var result = new Array();
		switch (field.getName()) {
			<t.dynEnumCalcCallCode>
		}
		return result;
	};
	
	E_<t.name>._valueCalcObject = new E_<t.name>();
	return E_<t.name>;
}());

>>