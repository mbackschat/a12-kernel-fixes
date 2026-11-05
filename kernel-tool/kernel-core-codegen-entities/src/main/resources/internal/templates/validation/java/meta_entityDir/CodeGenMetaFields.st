CodeGenMetaFields(t) ::= <<
<t.generatedFileInformation>

package <t.packageName>;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.*;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.IFormatDefinition.FeldFormatEnumType;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.util.MapBuilder;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.util.MetaFeldUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DatumTeilbekanntArt;

import com.mgmtp.a12.utils.conversion.InterpretationOfYear;

/*
 * Enthält Metadaten über Felder.
 *
 */
public class Meta_Felder_<t.name> {
	private final ArrayList\<Feld_t\> felder = new ArrayList\<Feld_t\>();
	private final Map\<Integer, Map\<Locale, String\>\> errorTexts4Req = new HashMap\<Integer, Map\<Locale, String\>\>();

	private final Map\<Integer, IFormatDefinition\> metaFormate = new HashMap\<\>();
	
	private final Map\<Integer, Map\<Locale, String\>\> mehrsprachigeFehlertexte = new HashMap\<Integer, Map\<Locale, String\>\>();
	private final Map\<Integer, Map\<Locale, List\<Pair\<String, String\>\>\>\> enumerationOberflaechenWerte = new HashMap\<Integer, Map\<Locale, List\<Pair\<String, String\>\>\>\>();
	private final Map\<Integer, Map\<String, Map\<String, String\>\>\> enumerationCategoryValues = new HashMap\<Integer, Map\<String, Map\<String, String\>\>\> ();
    private final Map\<Integer, Map\<Locale, List\<String\>\>\> stringHintLists = new HashMap\<Integer, Map\<Locale, List\<String\>\>\>();

	private final List\<Locale\> supportedLanguages = Arrays.asList( <t.languagesCode> );

	public ArrayList\<Feld_t\> getFelder(){
		return felder;
	}

    <t.errorText4ReqSplitFunctionCode>
 	<t.metaFelderCode>

	public Meta_Felder_<t.name>() {
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