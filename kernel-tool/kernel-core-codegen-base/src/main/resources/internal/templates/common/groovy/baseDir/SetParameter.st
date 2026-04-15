SetParameter(t) ::= <<
<if(t.empty || !t.withLineBreaks)>
[<t.codeFragments; separator = ", ">].toSet()<\\>
<else>
[
		<t.codeFragments; separator = ",\n">
	].toSet()<\\>
<endif>
>>