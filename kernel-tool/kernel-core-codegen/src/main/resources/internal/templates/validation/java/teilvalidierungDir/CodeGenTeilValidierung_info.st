CodeGenTeilValidierung_info(t) ::= <<
<t.generatedFileInformation>

package <t.packageName>;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.ITeilValidierungChecker;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.MainValidatorController;

/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen.
 *  
 */
public class TVCheckInfo_<t.name> implements ITeilValidierungChecker {

	private R_<t.name> checkObject = R_<t.name>.getInstance();

	public TVCheckInfo_<t.name>() {
	}
	

	public void fuehreAus(MainValidatorController controller, int interneFeldNr, long[] indices) {
		<t.FuehreAusSplitFunctionCodeCalls_Info>
	}
	
	<t.FuehreAusSplitFunctionCode_Info>
}

>>