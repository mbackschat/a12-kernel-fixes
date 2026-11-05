CodeGenRules(t) ::= <<
<t.generatedFileInformation>

package <t.packageName>;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.*;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.*;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidatorException;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.CustomConditionException;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult.ErrorType;


public class  R_<t.name> implements IValidatorIntern {
	private static final R_<t.name> checkObject = new R_<t.name>();

	private static Meta_<t.name> feldData = Meta_<t.name>.getInstance();
	private static IMetaDataValidierung metaDataValidierung = feldData.getMetaDataValidierung();
 
	public static R_<t.name> getInstance() {
		return checkObject;
	}

	<t.ruleDefinitionsCode>

	<t.rulesPerRootGroupMethodsCode>

	<t.preliminaryRulesPerErrorFieldMethodsCode; separator="\n">

	private final Map\<String, Consumer\<MainValidatorController\>\> uniqueFieldNameToPreliminaryValidationFunc = Map.ofEntries(
	    <t.uniqueFieldNamesWithPreliminaryRules:{uniqueFieldName|Map.entry("<uniqueFieldName>", this::validatePreliminaryRulesForField__<uniqueFieldName>)}; separator=",\n">
	);

	public void validatePreliminaryRulesForField(MainValidatorController controller, String uniqueFieldName) {
        var validationFunc = uniqueFieldNameToPreliminaryValidationFunc.get(uniqueFieldName);
        if (validationFunc != null) {
            validationFunc.accept(controller);
        }
	}

	public void validatePreliminaryRules(MainValidatorController controller) {
		<t.preliminaryRulesPerRootGroupCalls>
	}

	public void validiereVoll(MainValidatorController controller, ValidationMode modus) {
		switch (modus.getRuleType()) {
			case ALL:
				alleRegeln(controller);
				break;
			case INFO:
                alleInfoRegeln(controller);
                break;
            case HINT:
				alleHinweisRegeln(controller);
				break;
			case ERROR:
				alleFehlerRegeln(controller);
				break;
			default:
				throw new IllegalArgumentException("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
	}	
	
	public void validiereTeil(MainValidatorController controller, ValidationMode modus, Map\<String, RtInternalIdentifier\> pruefungsFelderMap) {
		ITeilValidierungChecker checker = null;
		switch (modus.getRuleType()) {
			case ALL:
				checker = new TVCheckAlle_<t.name>();
				break;
			case INFO:
                checker = new TVCheckInfo_<t.name>();
                break;
            case HINT:
				checker = new TVCheckHinweis_<t.name>();
				break;
			case ERROR: 
				checker = new TVCheckFehler_<t.name>();
				break;
			default: 
				throw new IllegalArgumentException("Regelart " + modus.getRuleType() + " wird nicht unterstützt.");
		}
		
		Collection\<RtInternalIdentifier\> felder = pruefungsFelderMap.values();
		if (controller.getValidationCache().isValidationPartWith3ValueLogic()) {
			ITeilValidierungChecker preChecker = new TVCheckPreliminary_<t.name>();
			
			for (RtInternalIdentifier id : felder) {
				int interneFeldNr = metaDataValidierung.getInterneFeldNr(id.getName());
				preChecker.fuehreAus(controller, interneFeldNr, id.getIndices());
			}
		}
		
		for (RtInternalIdentifier id : felder) {
			int interneFeldNr = metaDataValidierung.getInterneFeldNr(id.getName());
			checker.fuehreAus(controller, interneFeldNr, id.getIndices());
		}
	}

	private void alleRegeln(MainValidatorController controller) 
					throws ValidatorException { 
		//Aufruf aller Regeln 
		<t.rulesPerRootGroupCalls>
	}

	private void alleFehlerRegeln(MainValidatorController controller) 
					throws ValidatorException { 
		//Aufruf aller Fehler-Regeln 
		<t.errorRulesPerRootGroupCalls>
	}

	private void alleHinweisRegeln(MainValidatorController controller) 
					throws ValidatorException { 
		//Aufruf aller Hinweis-Regeln 
		<t.warningRulesPerRootGroupCalls>
	}
	
	private void alleInfoRegeln(MainValidatorController controller) 
                    throws ValidatorException { 
        //Aufruf aller Info-Regeln 
        <t.infoRulesPerRootGroupCalls>
    }
}



>>