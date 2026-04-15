CodeGenMetaRules(t) ::= <<
/*
 * Enthält Metadaten über die Regeln.
 *
 */
class <t.groovyPackageClassnamePrefix>Meta_Regel_<t.name>_<t.DateiNummerCode> {
	private ArrayList\<Regel_t\> m_regeln = new ArrayList\<Regel_t\>();

	public ArrayList\<Regel_t\> getRegeln(){
		return m_regeln;
	}

	private static final int ANZAHL_REGELN = <t.anzRegeln>;

	//speichert pro Regel die referenzierten Identifier
	private Identifier_t[][] refIdentifier = new Identifier_t[ANZAHL_REGELN][];

	//speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
	private Identifier_t[][] refAuslassungsIdentifier = new Identifier_t[ANZAHL_REGELN][];

	//speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
	private String[][] refAuslassungsVordrucke = new String[ANZAHL_REGELN][];

	//stores the referenced entities per rule (can contain groups)
	private RtIdentifierTemplate[][] refEntity = new RtIdentifierTemplate[ANZAHL_REGELN][];

	//stores the referenced entities per rule, which could cause omission errors (may contain groups)
	private RtIdentifierTemplate[][] refAuslassungsEntity = new RtIdentifierTemplate[ANZAHL_REGELN][];

	private List\<Map\<Locale, String\>\> mehrsprachigeFehlertexte = new ArrayList\<Map\<Locale, String\>\>();
	{
		for (int i = 0; i \< ANZAHL_REGELN; i++) {
			mehrsprachigeFehlertexte.add(new HashMap\<Locale, String\>());
		}
	}

	<t.regelMethodRefsSplitFunctionCode>

	<t.metaRegelFehlertexteSplitFunctionCode>

	/*
	 * Die übergebenen Parameter:
	 * name, voller pfad, nummer, fehlercode, fehlertexte, regelArt, refIdentifier,
	 * refAuslassungsIdentifier, refAuslassungsVordrucke, fehlerFeld, serverBerechnungsRegel
	 */
	<t.regelMethodSplitFunctionCode>

	public <t.groovyPackageClassnamePrefix>Meta_Regel_<t.name>_<t.DateiNummerCode>() {
		<t.MetaRegelRefsSplitFunctionCodeAufrufe>
		<t.MetaRegelFehlertexteSplitFunctionCodeAufrufe>
		<t.MetaRegelnSplitFunctionCodeAufrufe>
	}
}

>>