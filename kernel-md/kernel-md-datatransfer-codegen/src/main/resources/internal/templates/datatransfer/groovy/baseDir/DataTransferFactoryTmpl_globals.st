DataTransferFactoryTmpl_globals(t) ::= <<
<\n>
def getDataTransferService() {
	<t.groovyFactoryName>.getInstance();
}

def getTargetMetaModel() {
	<t.targetMetaClassname>.getInstance();
}
>>