MetaRegelReferenzen(t) ::= <<
// Init Daten zur Regel '<t.RegelFullName>'
<if(t.hasFelder)>
this._refIdentifier[<t.regelNummer>] = [<t.identifierCode>];
<endif>

<if(t.hasAuslassungsReferenzen)>
this._refAuslassungsIdentifier[<t.regelNummer>]= [<t.identifierAuslassungsCode>];
<endif>

<if(t.hasAuslassungsVordrucke)>
this._refAuslassungsVordrucke[<t.regelNummer>]= [<t.vordruckAuslassungsCode>];
<endif>
>>