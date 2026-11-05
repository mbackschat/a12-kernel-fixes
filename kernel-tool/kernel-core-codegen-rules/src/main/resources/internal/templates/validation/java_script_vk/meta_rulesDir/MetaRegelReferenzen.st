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

{
	// filter (decl & refs) omitted for referenced entities
	<t.refEntityVarDeclarationCode; separator = "\n">

	<if(t.hasEntities)>
	this._refEntity[<t.regelNummer>] = [
			<t.entityTemplateRefsCode>
	];
	<endif>

	<if(t.hasOmissionEntityReferences)>
	this._refAuslassungsEntity[<t.regelNummer>]= [
			<t.auslassungsEntityTemplateRefsCode>
	];
	<endif>
}
>>