CodeGenMetaModel_model(t) ::= <<
this._da = new Model_t(
		"<t.produktVersion>", // Produkt-Version
		"<t.dezimalTrenner>", // dezimaltrenner
		[<t.unterstuetzteSprachenCode>], // unterstuetzte Sprachen
		MetaDataHelper.createSetOfStrings(<t.applBedNamenCode; separator=", ">), //  Namen der Applikationsbedingungen
		MetaDataHelper.createSetOfStrings(<t.customFieldTypeNamesCode; separator=", ">), // Names of custom field types
		// additional Values
		<t.additionalValuesCode>,
		<t.internalAdditionalKeysCode>
	);
>>