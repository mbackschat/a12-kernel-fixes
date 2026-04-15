CodeGenStringTypeImpl_meta(t) ::= <<
<t.minLaenge>, // minLaenge
<t.maxLaenge>, // maxLaenge
<t.minLaengePrintable>, // minLaengePrintable
<t.maxLaengePrintable>, // maxLaengePrintable
"<t.muster>", // muster
this._mehrsprachigeFehlertexte.get(<t.gemeinsamesFormatNumber>), // fehlertexte
<t.lineBreakAllowed>, // zeilenUmbruch
<t.noValueValidation>, // noValueValidation
this._stringHintLists.get(<t.gemeinsamesFormatNumber>)
>>