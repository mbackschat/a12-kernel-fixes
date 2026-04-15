CodeGenDynamicEnumerations(t) ::= <<
class  <t.groovyPackageClassnamePrefix>E_<t.name> implements IDynEnumsIntern {
	private static final <t.groovyPackageClassnamePrefix>E_<t.name> valueCalcObject = new <t.groovyPackageClassnamePrefix>E_<t.name>();

	<! We don't need this on its own, but for the next line !>
	private static <t.groovyPackageClassnamePrefix>Meta_<t.name> feldData = <t.groovyPackageClassnamePrefix>Meta_<t.name>.getInstance();
	<! We need this for the BaseYear parameter !>
	private static IMetaDataValidierung metaDataValidierung = feldData.getMetaDataValidierung();

	public static <t.groovyPackageClassnamePrefix>E_<t.name> getInstance() {
		return valueCalcObject;
	}

	<t.dynEnumCalcCode>

	public List\<Pair\<String, String\>\> calcEnumerationValues(RtInternalIdentifier field, DynamicEnumerationController controller) {
		long startMesspunkt = controller.startMesspunkt();
		List\<Pair\<String, String\>\> result = Collections.emptyList();
		switch (field.getName()) {
		<t.dynEnumCalcCallCode>
		}
		return result;
	}
}

>>