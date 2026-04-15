MetaFeld(t) ::= <<
this._felder.push(
	new Feld_t(
		<t.interneNr>,
		"<t.uniqueName>",
		"<t.pfadName>",
		<t.pflichtFeldCode>,
		<t.mandatoryForRepeatableGroupCode>,
		this._metaFormate.get(<t.gemeinsamesFormatNrCode>),
		"<t.JSName>",
		[<t.vordruckPflichtFeldLfdNr; separator=", ">],
		[<t.pflichtFeldLfdNr; separator=", ">],
		<t.additionalKeyValuesCode>,
		<t.internalAdditionalKeysCode>,
		<t.berechnungsUebertragServer>,
		this._errorTexts4Req.get(<t.interneNr>)
	)
);
>>