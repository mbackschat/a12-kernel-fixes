EntityListCondition(t) ::= <<
<if(t.wiederholungNichtEindeutig)>controller.<t.bedingungNameCode>(idx, <t.feldListeCode>, [<t.searchedDuplicateLayers; separator=", ">], repNotUnqErrorCache, iter.semanticIndexLevel)
<else>controller.<t.bedingungNameCode>(idx, <t.feldListeCode>)<endif>
>>