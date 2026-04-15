CodeGenMetaFields(t) ::= <<
/*
 * Enthält Metadaten über Felder.
 *
 */
class <t.groovyPackageClassnamePrefix>Meta_Felder_<t.name> {
	private ArrayList\<Feld_t\> m_felder = new ArrayList\<Feld_t\>();
	private Map\<Integer, Map\<Locale, String\>\> errorTexts4Req = new HashMap\<Integer, Map\<Locale, String\>\>();

	private Map\<Integer, IFormatDefinition\> metaFormate = new HashMap\<\>();

	private Map\<Integer, Map\<Locale, String\>\> mehrsprachigeFehlertexte = new HashMap\<Integer, Map\<Locale, String\>\>();
	private Map\<Integer, Map\<Locale, List\<Pair\<String, String\>\>\>\> enumerationOberflaechenWerte = new HashMap\<Integer, Map\<Locale, List\<Pair\<String, String\>\>\>\>();
	private Map\<Integer, Map\<String, Map\<String, String\>\>\> enumerationCategoryValues = new HashMap\<Integer, Map\<String, Map\<String, String\>\>\> ();
	private final Map\<Integer, Map\<Locale, List\<String\>\>\> stringHintLists = new HashMap\<Integer, Map\<Locale, List\<String\>\>\>();

	private final List\<Locale\> supportedLanguages = Arrays.asList( <t.languagesCode> );

	public ArrayList\<Feld_t\> getFelder(){
		return m_felder;
	}

    <t.errorText4ReqSplitFunctionCode>
 	<t.metaFelderCode>

	public <t.groovyPackageClassnamePrefix>Meta_Felder_<t.name>() {
	    <t.errorTexts4ReqSplitFunctionCodeAufrufe>
		<t.metaFeldFehlertexteSplitFunctionCodeAufrufe>
		<t.metaEnumerationSplitFunctionCodeAufrufe>
		<t.metaEnumerationCategoryValuesSplitFunctionCodeAufrufe>
		<t.metaStringHintListsSplitFunctionCodeAufrufe>
		<t.metaFeldtypenSplitFunctionCodeAufrufe>
		<t.metaFelderSplitFunctionCodeAufrufe>
	}

}

>>