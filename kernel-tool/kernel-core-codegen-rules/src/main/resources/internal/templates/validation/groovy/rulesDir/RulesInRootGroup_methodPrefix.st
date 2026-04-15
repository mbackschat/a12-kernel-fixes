RulesInRootGroup_methodPrefix(t) ::= <<
/* Aufruf der Regeln fuer Vordruck <t.rootGroupName> für eine spezifische lfdNr.
*/
	<if(t.isGlobal)>long[] indices = [0];<endif>
	long startMesspunkt = -1;
>>