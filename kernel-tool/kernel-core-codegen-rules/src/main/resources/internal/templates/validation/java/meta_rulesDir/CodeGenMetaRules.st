CodeGenMetaRules(t) ::= <<
<t.generatedFileInformation>

package <t.packageName>;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.Identifier_t;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.Identifier_t.ReferenzTyp;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.Regel_t;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.meta.util.MapBuilder;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.Constants;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtIdentifierTemplate;

/*
 * Enthält Metadaten über die Regeln.
 *
 */
public class  Meta_Regel_<t.name> {
	private ArrayList\<Regel_t\> regeln = new ArrayList\<Regel_t\>();
	
	public ArrayList\<Regel_t\> getRegeln(){
		return regeln;
	}
	
	private static final int ANZAHL_REGELN = <t.anzRegeln>;

	//speichert pro Regel die referenzierten Identifier
	private Identifier_t refIdentifier[][] = new Identifier_t[ANZAHL_REGELN][];

	//speichert pro Regel die referenzierten Identifier, die Auslassungsfehler erzeugen können
	private Identifier_t refAuslassungsIdentifier[][] = new Identifier_t[ANZAHL_REGELN][];

	//speichert pro Regel die Vordrucke, die einen Auslassungsfehler erzeugen können
	private String refAuslassungsVordrucke[][] = new String[ANZAHL_REGELN][];

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

  	public Meta_Regel_<t.name>() {
        <t.MetaRegelRefsSplitFunctionCodeAufrufe>
        <t.MetaRegelFehlertexteSplitFunctionCodeAufrufe>
        <t.MetaRegelnSplitFunctionCodeAufrufe>
	}
}
>>