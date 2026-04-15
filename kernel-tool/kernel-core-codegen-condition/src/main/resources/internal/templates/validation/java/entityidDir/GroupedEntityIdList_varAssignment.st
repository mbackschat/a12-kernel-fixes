GroupedEntityIdList_varAssignment(t) ::= <<
LinkedHashMap\<RtIdentifierTemplate, List\<RtIdentifierTemplate\>\> <t.variableName> = new LinkedHashMapBuilder\<RtIdentifierTemplate, List\<RtIdentifierTemplate\>\>()
	<t.allGroupedEntityIdsCode; separator = "">	.build();

>>