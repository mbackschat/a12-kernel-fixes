CodeGenMap(t) ::= <<
new MapBuilder\<<t.parameterTypeAsString>\>()<if(t.notEmpty)>.put(<t.entryListCode; separator = ").put(">)<endif>.build()
>>