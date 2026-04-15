CodeGenDynamicEnumeration(t) ::= <<
public List\<Pair\<String, String\>\> dynEnum_<t.uniqueName>(DynamicEnumerationController controller, RtInternalIdentifier id) {
	/* Enumeration value calculation for field '<t.dynEnumFieldName>'
	 */
	<t.filterParamsCreationCode; separator = "\n">
	<t.filterCode; separator= "\n">

	<t.rtControllerParamsCreationCode; separator = "\n">

	try {
		IterationState idx = new IterationState(RtInternalIdentifier.createIdxWithUsb(id), null);
		<if(t.safeGuard)>
		if (<t.safeGuardCode>) {
			return Collections.emptyList();
		}
		<endif>
		List\<Pair\<String, String\>\> values = new ArrayList\<\>();
		<t.enumValuesCode>
		return controller.addUIValues(values, id);
	} catch (Exception e) {
		assert e instanceof ValidatorException : "Unexpected exception thrown " + e;
		return Collections.emptyList();
	}
}<\n>
>>