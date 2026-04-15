CodeGenMetaModel_factory(t) ::= <<
<\n>
def getValidator() {
	return <t.groovyPackageClassnamePrefix>ERValidator_<t.name>.getInstance()
}

def getCalculator() {
	return <t.groovyPackageClassnamePrefix>ERValidator_<t.name>.getInstance()
}

def getMetaModel() {
	return <t.groovyPackageClassnamePrefix>Meta_<t.name>.getInstance()
}

>>