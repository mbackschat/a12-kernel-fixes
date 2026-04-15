CodeGenProjectLanguageSettings(t) ::= <<
new PresentationInformationValues("<t.dezimalTrenner>", "<t.thousandsSeparator>", "<t.datumTrenner>", "<t.datumReihenfolge>", <if(t.datumNullOptional)>PresentationInformation.TRUE<else>PresentationInformation.FALSE<endif>, "<t.datumbereichTrenner>", "<t.dateTimeFormat>", "<t.timeFormat>")
>>