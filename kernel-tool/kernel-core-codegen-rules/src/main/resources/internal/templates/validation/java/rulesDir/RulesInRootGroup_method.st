RulesInRootGroup_method(t) ::= <<
<if(!t.isGlobal)>
public void vordruck<t.postfix>__<t.rootGroupName4Method>(MainValidatorController controller)  throws ValidatorException {
/* Aufruf der Regeln fuer Vordruck <t.rootGroupName>
    H: nur Hinweise
    F: nur Fehler
    kein Zusatz: fuer alle
  */
	if (controller.mindestensEinVordruckAngegeben("<t.rootGroupName>", 0, 0).isKnownAndTrue()) {
	    long maxLfdNummer = <t.maxLfdNummerCodeFragment>;
 	    for (int l = 1; l \<= maxLfdNummer; l++) {
 	        if (controller.mindestensEinVordruckAngegeben("<t.rootGroupName>", l, 0).isKnownAndTrue()) {
 	        	long[] indices = {l};
 	            vordruck<t.postfix>__<t.rootGroupName4Method>_lfdNr(controller, indices);
 	        }
	  	}
	}
}
<endif>

public void vordruck<t.postfix>__<t.rootGroupName4Method><if(!t.isGlobal)>_lfdNr<endif>(MainValidatorController controller<if(!t.isGlobal)>, long[] indices<endif>)
                                   throws ValidatorException {
/* Aufruf der Regeln fuer Vordruck <t.rootGroupName> für eine spezifische lfdNr.
  */
	<if(t.isGlobal)>long[] indices = {0};<endif>
	long startMesspunkt = -1;
    <t.ruleCallsCode>
}<\n>
>>