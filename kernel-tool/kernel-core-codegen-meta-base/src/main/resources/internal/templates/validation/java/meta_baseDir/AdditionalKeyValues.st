AdditionalKeyValues(t) ::= <<
new MapBuilder\<String, Object\>()<if(t.notEmpty)>.put(<t.codeFragments; separator = ").put(">)<endif>.build()
>>