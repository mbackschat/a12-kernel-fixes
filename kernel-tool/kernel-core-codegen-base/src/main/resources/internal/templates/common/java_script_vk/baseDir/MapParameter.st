MapParameter(t) ::= <<
new Map(<if(!t.empty)>[
		<t.keyCodeFragments,t.valueCodeFragments:{key, value | [<key>, <value>]}; separator = ",\n">
	]<endif>)
>>