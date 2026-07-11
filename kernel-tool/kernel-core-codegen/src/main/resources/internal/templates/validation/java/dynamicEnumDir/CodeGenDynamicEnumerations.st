CodeGenDynamicEnumerations(t) ::= <<
<t.generatedFileInformation>

package <t.packageName>;

import java.util.*;

import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidatorException;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.*;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.*;

public class  E_<t.name> implements IDynEnumsIntern {
	private static final E_<t.name> valueCalcObject = new E_<t.name>();

	<! We don't need this on its own, but for the next line !>
	private static Meta_<t.name> feldData = Meta_<t.name>.getInstance();
	<! We need this for the BaseYear parameter !>
	private static IMetaDataValidierung metaDataValidierung = feldData.getMetaDataValidierung();

	public static E_<t.name> getInstance() {
		return valueCalcObject;
	}

	<t.dynEnumCalcCode>

	public List\<Pair\<String, String\>\> calcEnumerationValues(RtInternalIdentifier field, DynamicEnumerationController controller) {
		long startMesspunkt = controller.startMesspunkt();
		List\<Pair\<String, String\>\> result = Collections.emptyList();
		switch (field.getName()) {
		<t.dynEnumCalcCallCode>
		}
		return result;
	}
}

>>