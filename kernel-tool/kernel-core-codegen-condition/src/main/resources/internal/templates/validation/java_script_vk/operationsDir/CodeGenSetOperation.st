CodeGenSetOperation(t) ::= <<
var <t.varName> = {
    calculate: (idx) => {
       return <t.operationCode>;
    }
};
>>