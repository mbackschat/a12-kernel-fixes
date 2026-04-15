CodeGenFilterCondition(t) ::= <<
var <t.varName> = {
    isRelevant: function (idx) {
       return <t.conditionBooleanCode>;
    }
};
>>