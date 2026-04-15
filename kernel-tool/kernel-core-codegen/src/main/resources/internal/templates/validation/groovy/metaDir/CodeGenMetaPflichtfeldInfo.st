CodeGenMetaPflichtfeldInfo(t) ::= <<
/*
 * Enthält Metadaten über die Pflichtfeldinformationen.
 *
 */
class <t.groovyPackageClassnamePrefix>Meta_PfFeld_<t.name> {

	private final IMetaDataValidierung metaDataValidierung;

	<t.groovyPackageClassnamePrefix>Meta_PfFeld_<t.name>(IMetaDataValidierung metaDataValidierung) {
		this.metaDataValidierung = metaDataValidierung;
	}

	void addLokalePflichtFeldInfos(Set\<MetaLokalePflichtInfo\> additivUndPflichtfelder) {

		String[][] infos = new String[<t.anzahlLokalePflichtfeldInfosCode>][];
		<t.lokalePflichtFeldInfoMethodenAufrufCode>

		for (String[] info : infos) {
			addLokalePflichtFeldInfo(
					additivUndPflichtfelder,
					info[0],
					info[1],
					<t.groovyPackageClassnamePrefix>Meta_<t.name>.getMetaFSMenge(info[2]),
					<t.groovyPackageClassnamePrefix>Meta_<t.name>.getMetaFSMenge(info[3]));
		}

	}

	<t.lokalePflichtFeldInfoMethodenCode>

	private void addLokalePflichtFeldInfo(Set\<MetaLokalePflichtInfo\> additivUndPflichtfelder, String lfdNrMuster, String zeileMuster,
			Set\<MetaIdentifier\> additiv,
			Set\<MetaIdentifier\> pflichtfelder) {
		MetaLokalePflichtInfo info = MetaLokalePflichtInfo.createLokalePflichtFeldInfo(this.metaDataValidierung, additiv, pflichtfelder, lfdNrMuster, zeileMuster);
		additivUndPflichtfelder.add(info);
	}
}

>>