CodeGenFilterCondition(t) ::= <<
var <t.varName> = {
    isRelevant: (idx) => {
       return <t.conditionBooleanCode>;
    }
};
>>