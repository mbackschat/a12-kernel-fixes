CodeGenRuleDefinition_call(t) ::= <<
startMesspunkt = controller.startMesspunkt();
this._checkObject.<t.uniqueRuleName>(controller, indices);
controller.logMessung("<t.uniqueRuleName>", indices[0], startMesspunkt);<\n>
>>