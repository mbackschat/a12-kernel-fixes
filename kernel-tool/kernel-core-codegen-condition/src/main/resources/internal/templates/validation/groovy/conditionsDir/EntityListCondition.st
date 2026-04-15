EntityListCondition(t) ::= <<
<if(t.wiederholungNichtEindeutig)>controller.<t.bedingungNameCode>(idx, <t.feldListeCode>, [<t.searchedDuplicateLayers; separator=", ">] as int[], repNotUnqErrorCache, iter.getSemanticIndexLevel())
<else>controller.<t.bedingungNameCode>(idx, <t.feldListeCode>)<endif>
>>