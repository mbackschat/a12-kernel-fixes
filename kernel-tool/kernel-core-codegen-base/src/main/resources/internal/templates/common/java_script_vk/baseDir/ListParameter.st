ListParameter(t) ::= <<
<if(!t.withLineBreaks || t.empty)>
[<t.codeFragments; separator = ", ">]<\\>
<else>
[
		<t.codeFragments; separator = ",\n">
	]<\\>
<endif>
>>