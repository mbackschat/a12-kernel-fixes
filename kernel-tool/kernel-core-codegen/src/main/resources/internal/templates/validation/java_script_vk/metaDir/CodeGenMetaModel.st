CodeGenMetaModel(t) ::= <<
var getMetaFSMenge = function (beschreibung) {
		var result = new HashSet();
		var felder = beschreibung.split(";");
		for (var i = 0; i \< felder.length; i++) {
			if (felder[i].length \> 0) {
				result.add(MetaIdentifier.parse(felder[i]));
			}
		}
		return result;
};

var Meta_<t.name> = (function () {
	function Meta_<t.name>() {
		this._metaFelder = new Meta_Felder_<t.name>();
		this._metaRegeln = new Meta_Regel_<t.name>();
		this._unterstuetzteSprachen = [<t.unterstuetzteSprachenCode>];
		<t.metaCode>
		if (Meta_<t.name>._instance) {
			throw new Error("Error: Instantiation failed: Use Meta_<t.name>.getInstance() instead of new.");
		}
		Meta_<t.name>._instance = this;
		var container = Meta_<t.name>._initContainer();
		this._meta = new MetaModelImpl(
			this._da,
			this._metaFelder.getFelder(),
			this._metaRegeln.getRegeln(),
			container,
			Meta_<t.name>.LEGAL_CHARACTERS,
			Meta_<t.name>.LEGAL_GRAPHEME_TRIE,
			() => E_<t.name>.getInstance()
        );
		var metaPlfFeldInfo = new Meta_PfFeld_<t.name>(this._meta.getMetaDataValidierung());
		metaPlfFeldInfo
			.addLokalePflichtFeldInfos(this._meta.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD));
	}
	Object.defineProperty(Meta_<t.name>, "RUNTIME_VERSION", { get: function () { return "<t.GencodeTypescriptRuntimeVersion>"; },
		enumerable: true,
		configurable: true
	});
	<if(t.EingeschraenkteZeichen)>
	Object.defineProperty(Meta_<t.name>, "LEGAL_CHARACTERS_START", {
		// There is no char basic data type in Typescript. Using this string values? Or calculating String-Values from numbers, where this is used?
		get: function () {
			return [
				<t.erlaubterZeichensatzCodeStart>
			];
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_<t.name>, "LEGAL_CHARACTERS_END", {
		// There is no char basic data type in Typescript. Using this string values? Or calculating String-Values from numbers, where this is used?
		get: function () {
			return [
				<t.erlaubterZeichensatzCodeEnd>
			];
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_<t.name>, "LEGAL_CHARACTERS", {
		get: function () {
			return [
				Meta_<t.name>.LEGAL_CHARACTERS_START, Meta_<t.name>.LEGAL_CHARACTERS_END
			];
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_<t.name>, "LEGAL_GRAPHEME_TRIE", {
        get: function () {
            var legalGraphemeTrie = new TrieNode(new Map(), false);
            <t.initLegalGraphemeTrie>
            return legalGraphemeTrie;
        },
        enumerable: true,
        configurable: true
    });
	
	<else>
	Object.defineProperty(Meta_<t.name>, "LEGAL_CHARACTERS_START", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_<t.name>, "LEGAL_CHARACTERS_END", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_<t.name>, "LEGAL_CHARACTERS", {
		get: function () {
			return undefined;
		},
		enumerable: true,
		configurable: true
	});
	Object.defineProperty(Meta_<t.name>, "LEGAL_GRAPHEME_TRIE", {
    		get: function () {
    			return new TrieNode(new Map(), false);
    		},
    		enumerable: true,
    		configurable: true
    	});
	<endif>

	Meta_<t.name>.getInstance = function () {
		return Meta_<t.name>._instance;
	};
	Meta_<t.name>.prototype.getValue = function (key) {
		var params = [];
		for (var _i = 1; _i \< arguments.length; _i++) {
			params[_i - 1] = arguments[_i];
		}

		return this._meta.getValue.apply(this._meta, [key].concat(params));
	};
	Meta_<t.name>.prototype.getValidKeys = function () {
		return this._meta.getValidKeys();
	};
	Meta_<t.name>._initContainer = function () {
		var result = new Array();
		<t.metaContainerListe>
		return result;
	};
	Meta_<t.name>.prototype.getMetaDataValidierung = function () {
		return this._meta.getMetaDataValidierung();
	};

	Meta_<t.name>.prototype.isSpracheUnterstuetzt = function (sprache) {
		return this._unterstuetzteSprachen.indexOf(sprache) !== -1;
	};

	Meta_<t.name>._constructor = (function () {
		if (Meta_<t.name>.RUNTIME_VERSION !== Constants.RUNTIME_VERSION) {
			throw new Error("The version of the generated code [" + Meta_<t.name>.RUNTIME_VERSION + "] does not match the version of the used runtime [" + Constants.RUNTIME_VERSION + "]!");
		}
		Meta_<t.name>._instance = new Meta_<t.name>();
	})();
	return Meta_<t.name>;
}());
>>