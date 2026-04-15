SetParameter(t) ::= <<
<if(t.empty)>
new Set()<\\>
<elseif(!t.withLineBreaks)>
new Set([<t.codeFragments; separator = ", ">])<\\>
<else>
new Set([
		<t.codeFragments; separator = ",\n">
	])<\\>
<endif>
>>