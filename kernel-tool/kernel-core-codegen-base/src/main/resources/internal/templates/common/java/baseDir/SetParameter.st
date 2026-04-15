SetParameter(t) ::= <<
<if(t.empty || !t.withLineBreaks)>
Set.of(<t.codeFragments; separator = ", ">)<\\>
<else>
Set.of(
		<t.codeFragments; separator = ",\n">
	)<\\>
<endif>
>>