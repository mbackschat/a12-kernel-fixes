CodeGenMetaModel_model(t) ::= <<
private Model_t da = new Model_t (
	"<t.produktVersion>", //Produkt-Version
    '<t.dezimalTrenner>', //dezimaltrenner
	Arrays.asList( <t.unterstuetzteSprachenCode> ), // unterstuetzte Sprachen
	MetaDataHelper.createSetOfStrings(<t.applBedNamenCode; separator=", ">), // Namen der Applikationsbedingungen
	MetaDataHelper.createSetOfStrings(<t.customFieldTypeNamesCode; separator=", ">), // Names of custom field types
	<t.sortInfo>,
    // additional Values:
    <t.additionalValuesCode>,
    <t.internalAdditionalKeysCode>
	);

>>