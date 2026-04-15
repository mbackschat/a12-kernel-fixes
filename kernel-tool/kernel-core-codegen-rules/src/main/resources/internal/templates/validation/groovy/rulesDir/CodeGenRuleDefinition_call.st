CodeGenRuleDefinition_call(t) ::= <<
startMesspunkt = controller.startMesspunkt();
checkObject<t.checkObjectNum>.<t.uniqueRuleName>(controller, indices);
controller.logMessung("<t.uniqueRuleName>", indices[0], startMesspunkt);<\n>
>>