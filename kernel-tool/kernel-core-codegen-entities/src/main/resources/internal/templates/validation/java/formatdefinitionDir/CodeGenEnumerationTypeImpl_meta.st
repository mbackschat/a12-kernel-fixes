CodeGenEnumerationTypeImpl_meta(t) ::= <<
mehrsprachigeFehlertexte.get(<t.gemeinsamesFormatNumber>),
supportedLanguages,
<if(t.tableReference)>
<t.tableName>,
<t.columnName>,
new String[] {<t.dependentColumns; separator=", ">},
T_<t.name>.getInstance()
<else>
new String[] {<t.werteCode>},
enumerationOberflaechenWerte.get(<t.gemeinsamesFormatNumber>),
enumerationCategoryValues.get(<t.gemeinsamesFormatNumber>),
<t.sValueCode>
<endif>
>>