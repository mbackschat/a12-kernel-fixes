MetaRegelReferenzen(t) ::= <<
// Init Daten zur Regel '<t.RegelFullName>'
<if(t.hasFelder)>
refIdentifier[<t.regelNummer>] = new Identifier_t[]{
		<t.identifierCode>
};
<endif>

<if(t.hasAuslassungsReferenzen)>
refAuslassungsIdentifier[<t.regelNummer>]= new Identifier_t[]{
		<t.identifierAuslassungsCode>
};
<endif>

<if(t.hasAuslassungsVordrucke)>
refAuslassungsVordrucke[<t.regelNummer>]= new String[]{<t.vordruckAuslassungsCode>
};
<endif>

{
    // filter (decl & refs) omitted for referenced entities
  	<t.refEntityVarDeclarationCode; separator = "\n">

    <if(t.hasEntities)>
    refEntity[<t.regelNummer>] = new RtIdentifierTemplate[]{
    		<t.entityTemplateRefsCode>
    };
    <endif>

    <if(t.hasOmissionEntityReferences)>
    refAuslassungsEntity[<t.regelNummer>]= new RtIdentifierTemplate[]{
    		<t.auslassungsEntityTemplateRefsCode>
    };
    <endif>
}
>>