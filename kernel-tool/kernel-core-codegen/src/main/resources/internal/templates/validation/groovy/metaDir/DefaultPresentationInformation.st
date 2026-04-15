DefaultPresentationInformation(t) ::= <<
new MapBuilder\<Locale, PresentationInformationValues\>()<if(t.notEmpty)>.put(<t.codeFragments; separator = ").put(">)<endif>.build()
>>