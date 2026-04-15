MetaRegelReferenzen(t) ::= <<
// Init Daten zur Regel '<t.RegelFullName>'
<if(t.hasFelder)>
<if(t.useSplittedClosuresForIdentifierRef)>
<t.refIdentifierSplitFunctionCode>

refIdentifier[<t.regelNummer>] = <t.refIdentifierSplitFunctionCalls>
<else>
<if(t.useOneClosureForIdentifierRef)>
refIdentifier[<t.regelNummer>] = { [
		<t.identifierCode>
] }();
<else>
refIdentifier[<t.regelNummer>] = [
		<t.identifierCode>
];
<endif>
<endif>
<endif>

<if(t.hasAuslassungsReferenzen)>
<if(t.useSplittedClosuresForAuslassungsIdentifierRef)>
<t.refAuslassungsIdentifierSplitFunctionCode>

refAuslassungsIdentifier[<t.regelNummer>] = <t.refAuslassungsIdentifierSplitFunctionCalls>
<else>
<if(t.useOneClosureForAuslassungsIdentifierRef)>
refAuslassungsIdentifier[<t.regelNummer>] = { [
		<t.identifierAuslassungsCode>
] }();
<else>
refAuslassungsIdentifier[<t.regelNummer>] = [
		<t.identifierAuslassungsCode>
];
<endif>
<endif>
<endif>

<if(t.hasAuslassungsVordrucke)>
refAuslassungsVordrucke[<t.regelNummer>] = [ <t.vordruckAuslassungsCode> ];
<endif>

{ -\>
    // filter (decl & refs) omitted for referenced entities
  	<t.refEntityVarDeclarationCode; separator = "\n">

    <if(t.hasEntities)>
    refEntity[<t.regelNummer>] = [
    		<t.entityTemplateRefsCode>
    ];
    <endif>

    <if(t.hasOmissionEntityReferences)>
    refAuslassungsEntity[<t.regelNummer>] = [
    		<t.auslassungsEntityTemplateRefsCode>
    ];
    <endif>
}();

>>