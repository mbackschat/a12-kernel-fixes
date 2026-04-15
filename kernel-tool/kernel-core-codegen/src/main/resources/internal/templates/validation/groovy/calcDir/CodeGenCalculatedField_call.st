CodeGenCalculatedField_call(t) ::= <<
startMesspunkt = controller.startMesspunkt();
calcObject<t.calcObjectNum>.calc_<t.uniqueName>(controller);
controller.logMessung("calc_<t.uniqueName>", startMesspunkt);<\n>
>>