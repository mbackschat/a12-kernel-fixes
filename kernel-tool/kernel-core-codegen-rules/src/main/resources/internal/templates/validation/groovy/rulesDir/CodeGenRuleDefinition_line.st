CodeGenRuleDefinition_line(t) ::= <<
<if(t.useClosureForConditionLineCode)>
<t.conditionCodeInOrder :{ cond | ValidierungsErgebnis tb<i0>;}>
<t.conditionLineSplitFunctionCode>
<t.conditionLineSplitFunctionCalls>
<else>
<t.conditionCodeInOrder :{ cond | ValidierungsErgebnis tb<i0> = <cond>;
}>
<endif>
ValidierungsErgebnis valErg = <t.combinedExpression>;

>>