CodeGenMetaModel(t) ::= <<
<\n>
class <t.groovyPackageClassnamePrefix>Meta_<t.name> implements IMetaModel, IMetaEntityInternal {

	private MetaModelImpl meta = null;
	private <t.groovyPackageClassnamePrefix>Meta_Felder_<t.name> metaFelder = new <t.groovyPackageClassnamePrefix>Meta_Felder_<t.name>();
	<t.metaRegelnNumbers:{ number | private <t.groovyPackageClassnamePrefix>Meta_Regel_<t.name>_<number> metaRegeln_<number> = new <t.groovyPackageClassnamePrefix>Meta_Regel_<t.name>_<number>();<\n>}>
	private static final <t.groovyPackageClassnamePrefix>Meta_<t.name> m_instance;

	private final List\<Locale\> unterstuetzteSprachen = Arrays.asList( <t.unterstuetzteSprachenCode> );

	private static final Map\<Locale, PresentationInformationValues\> DEFAULT_PRESENTATION_INFO = <t.defaultPresentationInfo>;

	<if(t.EingeschraenkteZeichen)>
	public static final int[] LEGAL_CHARACTERS_START = [
		<t.erlaubterZeichensatzCodeStart>
 	];
 	public static final int[] LEGAL_CHARACTERS_END = [
		<t.erlaubterZeichensatzCodeEnd>
 	];
 	public static final int[][] LEGAL_CHARACTERS = [
		LEGAL_CHARACTERS_START, LEGAL_CHARACTERS_END 
 	];
 	public static final TrieNode LEGAL_GRAPHEME_TRIE = new TrieNode(new HashMap\<\>(), false);
	<else>
	public static final int[] LEGAL_CHARACTERS_START = null;
	public static final int[] LEGAL_CHARACTERS_END = null;
	public static final int[][] LEGAL_CHARACTERS = null;
	public static final TrieNode LEGAL_GRAPHEME_TRIE = new TrieNode(new HashMap\<\>(), false);
	<endif>
	static {
		m_instance = new <t.groovyPackageClassnamePrefix>Meta_<t.name>();

		<t.initLegalGraphemeTrie>
	}

	public static <t.groovyPackageClassnamePrefix>Meta_<t.name> getInstance() {
		return m_instance;
	}

	public Object getValue(String key, Object... params) {
		return meta.getValue(key, params);
	}

	public String[] getValidKeys() {
		return meta.getValidKeys();
	}


    public Set\<String\> getValidKeysInternal() {
        return meta.getValidKeysInternal();
    }

	<t.metaContainerSplitFunctionCode>

	static Container_t[] initContainer() {
		Container_t[] result = new Container_t[<t.numContainers>];
		<t.metaContainerSplitFunctionCalls>
		return result;
	};
	
	<t.metaCode>
	 
	static Set\<MetaIdentifier\> getMetaFSMenge(String beschreibung) {
		Set\<MetaIdentifier\> result = new HashSet\<MetaIdentifier\>();
		String[] felder = beschreibung.split(";");
		for (int i=0; i \< felder.length; i++) {
			if (felder[i].length() \> 0) {
				result.add(MetaIdentifier.parse(felder[i]));
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private <t.groovyPackageClassnamePrefix>Meta_<t.name>() {
		Container_t[] container = initContainer();

		Regel_t[] metaRegeln = [ <if(t.metaRegelnNumbers)>metaRegeln_<first(t.metaRegelnNumbers)><rest(t.metaRegelnNumbers):{ number | , metaRegeln_<number>}><endif> ].collect({it.getRegeln()}).inject([]){result, r -\> result + r};
		meta = new MetaModelImpl(
					da,
					metaFelder.getFelder().toArray(new Feld_t[metaFelder.getFelder().size()]), 
					metaRegeln,
					container,
					LEGAL_CHARACTERS,
					LEGAL_GRAPHEME_TRIE,
					DEFAULT_PRESENTATION_INFO,
					<t.extmp>,
					<if(!t.mapperOnly)>{ -\> <t.groovyPackageClassnamePrefix>E_<t.name>.getInstance() }<else>null<endif>,
					<if(!t.mapperOnly)>{ -\> <t.groovyPackageClassnamePrefix>ERValidator_<t.name>.getInstance() }<else>null<endif>);
		// Der XML-Writer/-Reader kann nicht bereits dem Konstruktor von MetaModelImpl uebergeben werden,
		// denn dessen Konstruktor benoetigt die MetaModelImpl-Instanz.
		meta.addWert(IExtmpMetaKeysBase.MODEL_XMLWRITER, <t.xmlWriterCode>);
        meta.addWert(IExtmpMetaKeysBase.MODEL_UXMLWRITER, <t.uXmlWriterCode>);
        meta.addWert(IExtmpMetaKeysBase.MODEL_XMLREADER, <t.xmlReaderCode>);

		<t.groovyPackageClassnamePrefix>Meta_PfFeld_<t.name> metaPlfFeldInfo = new <t.groovyPackageClassnamePrefix>Meta_PfFeld_<t.name> (getMetaDataValidierung());
		metaPlfFeldInfo.addLokalePflichtFeldInfos((Set\<MetaLokalePflichtInfo\>) meta.getValue(IMetaKeysInternal.MODEL_ADDITIV_PFLICHTFELD));
    }

	public IMetaDataValidierung getMetaDataValidierung() {
		return meta.getMetaDataValidierung();
	}

	public boolean isSpracheUnterstuetzt(Locale sprache) {
		return unterstuetzteSprachen.contains(sprache);
	}
}

>>