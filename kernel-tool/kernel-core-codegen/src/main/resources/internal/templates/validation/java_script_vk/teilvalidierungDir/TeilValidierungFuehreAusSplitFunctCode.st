TeilValidierungFuehreAusSplitFunctCode(t) ::= <<
/**
* Führt die Validierung für alle Regeln aus, deren Aktionsfeld die
* angegebene interne Feldnummer besitzt wenn diese Nummer im Intervall [<t.MinFeldNummer>,<t.MaxFeldNummer>]
* liegt.
*
* In der Methode werden allerdings nur interne Nummern von Feldern
* betrachtet, die Aktionsfelder einer Methode sind. Es müssen somit nicht alle
* Felder des Intervalls in der Switch-Anweisung berücksichtigt werden.
*
* @param controller
* @param interneFeldNr
* @param indices
*/

<t.checkerTypNameCode>.prototype._fuehreAus_<t.MethodCounter> = function(controller, interneFeldNr, indices) {
	var startMesspunkt = -1;
	switch (interneFeldNr) {
		<t.CaseStmtCode>		default:
		if (interneFeldNr \<= <t.MaxFeldNummer>) {
			/* In der Methode werden nur interne Nummern von Feldern
			 * betrachtet, die Aktionsfelder einer Methode sind. Es werden somit nicht alle
			 * Zahlen des Intervalls in der Switch-Anweisung berücksichtigt.
			 */
		} else {
			throw new Error("Die interne Feldnummer " + interneFeldNr + " liegt nicht im Interval [<t.MinFeldNummer>,<t.MaxFeldNummer>].");
		}
		break;
	}
};
>>