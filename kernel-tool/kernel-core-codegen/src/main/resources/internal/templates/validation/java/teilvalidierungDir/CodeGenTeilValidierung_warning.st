CodeGenTeilValidierung_warning(t) ::= <<
<t.generatedFileInformation>

package <t.packageName>;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.ITeilValidierungChecker;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.MainValidatorController;

/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen.
 *  
 */
public class TVCheckHinweis_<t.name> implements ITeilValidierungChecker {

	private R_<t.name> checkObject = R_<t.name>.getInstance();

	public TVCheckHinweis_<t.name>() {
	}
	

	public void fuehreAus(MainValidatorController controller, int interneFeldNr, long[] indices) {
		<t.FuehreAusSplitFunctionCodeCalls_Hinweis>
	}
	
	<t.FuehreAusSplitFunctionCode_Hinweis>
}

>>