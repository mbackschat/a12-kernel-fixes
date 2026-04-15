CodeGenDynamicEnumeration_call(t) ::= <<
case "<t.uniqueName>":
	result = dynEnum_<t.uniqueName>(controller, field);
	controller.logMessung("dynEnum_<t.uniqueName>", startMesspunkt);
	break;<\n>
>>