CodeGenTeilValidierung_preliminary(t) ::= <<
<t.generatedFileInformation>

package <t.packageName>;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.ITeilValidierungChecker;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.MainValidatorController;

/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Vorprüfungen (z.B. Pflichtfeldprüfung).
 *  
 */
public class TVCheckPreliminary_<t.name> implements ITeilValidierungChecker {

	private R_<t.name> checkObject = R_<t.name>.getInstance();

	public TVCheckPreliminary_<t.name>() {
	}

	public void fuehreAus(MainValidatorController controller, int interneFeldNr, long[] indices) {
		<t.FuehreAusSplitFunctionCodeCalls_Preliminary>
	}
	
	<t.FuehreAusSplitFunctionCode_Preliminary>
}

>>