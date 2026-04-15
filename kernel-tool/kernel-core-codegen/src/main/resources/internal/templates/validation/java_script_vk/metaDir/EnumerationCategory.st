EnumerationCategory(t) ::= <<
<t.xmlValue>, new MapBuilder()<if(t.notEmpty)>.put(<t.categoryValuesCode; separator = ").put(">)<endif>.build()
>>