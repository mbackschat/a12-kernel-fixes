CodeGenDynamicEnumeration(t) ::= <<
E_<t.name>.prototype.dynEnum_<t.uniqueName> = function (controller, id) {
	/* Enumeration value calculation for field '<t.dynEnumFieldName>'
	 */
	<t.filterParamsCreationCode; separator = "\n">
	<t.filterCode; separator= "\n">

	<t.rtControllerParamsCreationCode; separator = "\n">
	try {
		var idx = new IterationState(RtInternalIdentifier.createIdx(id), null);
		<if(t.safeGuard)>
		if (<t.safeGuardCode>) {
			return new Array(0);
		}
		<endif>
		var xmlValues = new Array();
		<t.enumValuesCode>
		return controller.addUIValues(xmlValues, id);
	} catch (e) {
		if (!(e instanceof ValidatorException)) {
			throw new Error("Unexpected exception thrown " + e);
		}
		return new Array(0);
	}
};<\n>
>>