CodeGenTeilValidierung_all(t) ::= <<
<t.generatedFileInformation>

package <t.packageName>;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.ITeilValidierungChecker;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.MainValidatorController;

/**
 * Diese Klasse dient der Ausführung von Teilvalidierungen von Infos, Hinweisen und Fehlern.
 *  
 */
public class TVCheckAlle_<t.name> implements ITeilValidierungChecker {

	private TVCheckFehler_<t.name> tvFehlerChecker = new TVCheckFehler_<t.name>();
	private TVCheckHinweis_<t.name> tvHinweisChecker = new TVCheckHinweis_<t.name>();
	private TVCheckInfo_<t.name> tvInfoChecker = new TVCheckInfo_<t.name>();
	
	public TVCheckAlle_<t.name>() {
	}
	
	public void fuehreAus(MainValidatorController controller, int interneFeldNr, long[] indices) {
		tvFehlerChecker.fuehreAus(controller, interneFeldNr, indices);
		tvHinweisChecker.fuehreAus(controller, interneFeldNr, indices);
		tvInfoChecker.fuehreAus(controller, interneFeldNr, indices);
	}
}

>>