CodeGenCalc(t) ::= <<
<t.generatedFileInformation>

package <t.packageName>;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.ValidatorException;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.*;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.CalculationCache.CalculationFieldException;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.*;

public class C_<t.name> implements ICalculatorIntern {
	private static final C_<t.name> checkObject = new C_<t.name>();

	private static Meta_<t.name> feldData = Meta_<t.name>.getInstance();
	private static IMetaDataValidierung metaDataValidierung = feldData.getMetaDataValidierung();
	private static Set\<String\> expandedOperandFieldsOfCalculations = <t.expandedOperandFieldsOfCalculations>;

	public static C_<t.name> getInstance() {
		return checkObject;
	}

	<t.berechneteFelderCode>

	public Set\<String\> getExpandedOperandFieldsOfCalculations() {
		 return expandedOperandFieldsOfCalculations;
	}

	public void calculate(CalculationController controller) {
		long startMesspunkt;
		<t.berechneteFelderAufrufCode>
	}
}

>>