EntityIdList_varAssignment(t) ::= <<
List\<RtIdentifierTemplate\> <t.variableName> = new ListBuilder\<RtIdentifierTemplate\>()<if(t.hasEntityIds)>.add(<t.allEntityIds; separator = ").add(">)<endif>.build();
>>