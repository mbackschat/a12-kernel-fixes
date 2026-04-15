CodeGenSbKzMap(t) ::= <<
new MapBuilder\<\>()<if(t.notEmpty)>.put(<t.codeFragments; separator = ").put(">)<endif>.build()
>>