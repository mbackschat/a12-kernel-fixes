RulesInRootGroup_method(t) ::= <<
<if(!t.isGlobal)>
R_<t.mTVA>.vordruck<t.postfix>__<t.rootGroupName4Method> = function (controller) {
	/* Aufruf der Regeln fuer Vordruck <t.rootGroupName>
	H: nur Hinweise
	F: nur Fehler
	kein Zusatz: fuer alle
	*/
	if (controller.mindestensEinVordruckAngegeben("<t.rootGroupName>", 0).isKnownAndTrue()) {
	    var maxLfdNummer = <t.maxLfdNummerCodeFragment>;
		for (var l = 1; l \<= maxLfdNummer; l++) {
			if (controller.mindestensEinVordruckAngegeben("<t.rootGroupName>", l).isKnownAndTrue()) {
				var indices = [l];
				this.vordruck<t.postfix>__<t.rootGroupName4Method>_lfdNr(controller, indices);
			}
		}
	}
};
<endif>
R_<t.mTVA>.vordruck<t.postfix>__<t.rootGroupName4Method><if(!t.isGlobal)>_lfdNr<endif> = function (controller<if(!t.isGlobal)>, indices<endif>) {
/* Aufruf der Regeln fuer Vordruck <t.rootGroupName> für eine spezifische lfdNr. */
	<if(t.isGlobal)>var indices = [0];<endif>
	var startMesspunkt = -1;
	<t.ruleCallsCode>};<\n>
>>