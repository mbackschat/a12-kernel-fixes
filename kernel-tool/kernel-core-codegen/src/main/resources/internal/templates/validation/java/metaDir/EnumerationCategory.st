EnumerationCategory(t) ::= <<
<t.xmlValue>, new MapBuilder\<String, String\>()<if(t.notEmpty)>.put(<t.categoryValuesCode; separator = ").put(">)<endif>.build()
>>