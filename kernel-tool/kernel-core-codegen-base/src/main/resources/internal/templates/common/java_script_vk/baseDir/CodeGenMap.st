CodeGenMap(t) ::= <<
new MapBuilder()<if(t.notEmpty)>.put(<t.entryListCode; separator = ").put(">)<endif>.build()
>>