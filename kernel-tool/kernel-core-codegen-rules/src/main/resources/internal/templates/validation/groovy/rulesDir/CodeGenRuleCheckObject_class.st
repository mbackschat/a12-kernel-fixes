CodeGenRuleCheckObject_class(t) ::= <<
class <t.groovyPackageClassnamePrefix>R<t.num>_<t.name> {
	private static final <t.groovyPackageClassnamePrefix>R<t.num>_<t.name> checkObject = new <t.groovyPackageClassnamePrefix>R<t.num>_<t.name>();

	private static <t.groovyPackageClassnamePrefix>Meta_<t.name> feldData = <t.groovyPackageClassnamePrefix>Meta_<t.name>.getInstance();
	private static IMetaDataValidierung metaDataValidierung = feldData.getMetaDataValidierung();

	private static getInstance() {
		return checkObject;
	}

	<t.ruleDefinitionsCode>
}

>>