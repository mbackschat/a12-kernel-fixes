EntityListCondition(t) ::= <<
<if(t.wiederholungNichtEindeutig)><\\>
controller.<t.bedingungNameCode>(idx, <t.feldListeCode>, new int[] {<t.searchedDuplicateLayers; separator=", ">}, repNotUnqErrorCache, iter.getSemanticIndexLevel())<\\>
<else><\\>
controller.<t.bedingungNameCode>(idx, <t.feldListeCode>)<\\>
<endif>
>>