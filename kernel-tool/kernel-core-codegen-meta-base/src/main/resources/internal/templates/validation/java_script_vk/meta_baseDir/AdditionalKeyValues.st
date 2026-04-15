AdditionalKeyValues(t) ::= <<
new MapBuilder()<if(t.notEmpty)>
	.put(<t.codeFragments; separator = ")\n\r\t.put(">)<endif>
	.build()
>>