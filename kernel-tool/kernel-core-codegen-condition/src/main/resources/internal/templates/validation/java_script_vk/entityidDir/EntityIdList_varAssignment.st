EntityIdList_varAssignment(t) ::= <<
var <t.variableName> = new ListBuilder()<if(t.hasEntityIds)>.add(<t.allEntityIds; separator = ").add(">)<endif>.build();
>>