CodeGenCalculatedField_call(t) ::= <<
startMesspunkt = controller.startMesspunkt();
calc_<t.uniqueName>(controller);
controller.logMessung("calc_<t.uniqueName>", startMesspunkt);<\n>
>>