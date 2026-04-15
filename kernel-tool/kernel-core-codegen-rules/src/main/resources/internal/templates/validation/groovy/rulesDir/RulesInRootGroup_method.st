RulesInRootGroup_method(t) ::= <<
<if(!t.isGlobal)>
public void vordruck<t.postfix>__<t.rootGroupName4Method>(MainValidatorController controller) throws ValidatorException {
/* Aufruf der Regeln fuer Vordruck <t.rootGroupName>
	H: nur Hinweise
	F: nur Fehler
	kein Zusatz: fuer alle
*/
	if (controller.mindestensEinVordruckAngegeben("<t.rootGroupName>", 0, 0).isKnownAndTrue()) {
	    long maxLfdNummer = <t.maxLfdNummerCodeFragment>;
 		for (int l = 1; l \<= maxLfdNummer; l++) {
 			if (controller.mindestensEinVordruckAngegeben("<t.rootGroupName>", l, 0).isKnownAndTrue()) {
 				long[] indices = [l];
 				<t.rootGroupSplitFunctionCalls>
 			}
		}
	}
}

<t.rootGroupSplitFunctionCode>
<else>
public void vordruck<t.postfix>__<t.rootGroupName4Method>(MainValidatorController controller) throws ValidatorException {
/* Aufruf der Regeln fuer Vordruck <t.rootGroupName> für eine spezifische lfdNr.
*/
	<t.rootGroupSplitFunctionCalls>
}

<t.rootGroupSplitFunctionCode>
<endif>
>>