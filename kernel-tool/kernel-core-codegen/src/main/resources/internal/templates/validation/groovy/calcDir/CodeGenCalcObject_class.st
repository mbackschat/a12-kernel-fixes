CodeGenCalcObject_class(t) ::= <<
class <t.groovyPackageClassnamePrefix>C<t.num>_<t.name> {
	private static final <t.groovyPackageClassnamePrefix>C<t.num>_<t.name> calcObject = new <t.groovyPackageClassnamePrefix>C<t.num>_<t.name>();

	private static <t.groovyPackageClassnamePrefix>Meta_<t.name> feldData = <t.groovyPackageClassnamePrefix>Meta_<t.name>.getInstance();
	private static IMetaDataValidierung metaDataValidierung = feldData.getMetaDataValidierung();

	private static getInstance() {
		return calcObject;
	}

	<t.berechneteFelderCode>
}

>>