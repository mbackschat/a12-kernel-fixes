/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */

package com.mgmtp.a12.kernel.core.rt._31_3.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.FehlerHandler;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.RawParamHandler4Convert;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ArrayUtils;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.Constants;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.EntityIterator;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ErrorCause;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IMetaKeysInternal;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IterationState;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.LocalizationHelper;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.LongArray;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.MetaDataUtilValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtExternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtExternalIdentifierWithSemanticIndex;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtIdentifierTemplate;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtIdentifierTemplateLayerSpec;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidatorException;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.CustomConditionException;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICause;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICustomCondition;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult.ErrorType;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;

import lombok.NonNull;

/**
 * Die Klasse <code>MainValidatorController</code> ist die zentrale Klasse zum
 * Laufzeit-Zugriff auf die Nutzdaten speziell für die Validierung. Sie enthält
 * zusätzlich zur abgeleiteten Klasse {@link RuntimeController} noch Methoden,
 * die speziell die Behandlung der Validierung betreffen.
 */
public class MainValidatorController extends RuntimeController {

	public enum PruefErgebnisTyp {
		Fehler,
		Hinweis,
		Info;
	}

	/**
	 * Die 3 folgenden Felder werden für jede Regel neu gesetzt.
	 */
	private String regelName = "";
	private boolean isUSBMusterRegel = false;
	private PruefErgebnisTyp pruefErgebnisTyp = PruefErgebnisTyp.Fehler;
	private final @NonNull FormalChecker formalChecker;
	private static final Pattern ZEILE_PLUS_MINUS_PATTERN = Pattern.compile("#\\d+[+-]\\d+");

	/**
	 * Erzeugt ein Validator-Objekt mit den übergebenen Nutzdaten.
	 *
	 * @param ergebnisse
	 * 		Ergebnis-Objekt zu dem die gefundenen Fehler, Hinweise und Infos hinzugefügt
	 * 		werden.
	 * @param valCache
	 * 		Cache für die Nutzdaten und die Optimierungsstrukturen.
	 * @param modus
	 * 		Parameter für die Validierung
	 * @param metaData
	 * 		bietet Zugriff auf die Metadaten
	 */
	public MainValidatorController(
			@NonNull IResult ergebnisse,
			@NonNull ValidationCache valCache,
			@NonNull ValidationMode modus,
			@NonNull IMetaDataValidierung metaData,
			@NonNull FormalChecker formalChecker
	) {
		super(ergebnisse, valCache, modus, metaData);
		this.formalChecker = formalChecker;
	}

	/**
	 * Testet die Applikationsbedingung: Im Falle eines Fehlers wird immer ein Werte-Fehler (d.h.
	 * TRUE_WF) zurückgegeben. Zur Übergabe an die externe Schnittstelle müssen die externen
	 * Repräsentationen verwendet werden.
	 * Dies bedeutet: Verwendung der externen Namen und des externen USBs.
	 * Je nachdem, ob die alte oder die neue Validierungsschnittstelle verwendet wird, wird die
	 * alte oder die neue Schnittstelle für die Applikationsbedingung verwendet. Die Daten und die
	 * relevanten Felder sind jeweils so gespeichert worden, wie sie für die Validierung übergeben
	 * wurden und müssen daher nicht mehr konvertiert werden. Die fehlerhaften Felder und das
	 * Fehlerfeld werden zur Laufzeit ermittelt und müssen daher in die externen Schnittstellen
	 * konvertiert werden.
	 *
	 * @param applName
	 * 		Name der Applikationsbedingung
	 * @param id
	 * 		Fehlerfeld der Regel
	 * @return TRUE_WF oder FALSE, je nachdem ob die Applikationsbedingung true oder false geliefert
	 * 		hat.
	 *
	 * @throws ValidatorException
	 * 		falls die verwendete Bedingung nicht übergeben wurde.
	 * 		Exceptions, die von der Applikationsbedingung geworfen werden,
	 * 		werden in eine ValidatorException eingewickelt.
	 */
	public ValidierungsErgebnis applikationsBedingung(
			String applName,
			RtInternalIdentifier id) {
		//Konvertiere das Fehlerfeld in das externe Fehlerfeld zurück.
		RtExternalIdentifier externId = validationCache.convert2Extern(id);

		// get the ICustomCondition passed by the project
		ICustomCondition applBed = validationCache.getCustomCondition(applName);

		// Wenn keine ICustomCondition mit dem angegebenen Namen vorhanden ist, werfe Exception
		if (applBed == null) {
			String mess = String.format("Custom condition '%s' for rule '%s' missing.", applName, regelName);
			throwValidatorException(mess, null);
		}

		//Bei Teilvalidierung nach 3-Wert-Logik werden prüfungsrelevante Felder übergeben.
		Set<IIdentifier> relevanteFelder = (validierungArt.isValidationPartWith3ValueLogic()) ?
				validationCache.getAllPruefungsrelevanteEntitiesExtern() :
				null;
		//Fehlerhafte Felder werden mit übergeben, aber im externen Format und müssen daher konvertiert werden.
		Set<IIdentifier> fehlerhafteFelder = validationCache.getAllFehlerhafteFelderExtern();

		try {//Aufruf des externen Interface.
			return applBed.check(
					validationCache.getDaten(),
					relevanteFelder,
					fehlerhafteFelder,
					externId) ? ValidierungsErgebnis.TRUE_WF : ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		} catch (CustomConditionException e) {
			throw e;
		} catch (Exception e) {// alle anderen von der ApplikationsBedingung geworfenen
			// Fehler werden aufgefangen und von einer ValidatorException gewrapped.
			String mess = String.format(
					"Custom condition '%s' in rule '%s' throws the following exception: '%s'.", applName, regelName,
					e.getClass().getName());
			throwValidatorException(mess, e);
		}
		//tempcobertura::0.00::tempcobertura
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN; // dummy
	}

	/**
	 * Hilfsmethode zur Erzeugung einer Validator-Exception
	 */
	private void throwValidatorException(String message, Exception e) {
		throw new ValidatorException(message, e);
	}

	public void logRegel(String regelName) {
		LOG.logRegel(regelName);
	}

	public void logNichtAusgewertet(String fehlertext) {
		LOG.logRegelNichtAusgewertet(regelName, fehlertext);
	}

	/**
	 * Setter Methode für die Teilvalidierung nach 3-Wert-Logik. Zur Performance-Steigerung wird
	 * überprüft, ob bei einer Teilvalidierung jeweils alle Zeilen der Felder /
	 * alle Instanzen der jeweiligen Kontextebenen (inkl. laufende Nummer Vordruck
	 * = 1. Kontextebene) aller möglichen Felder gleichbehandelt werden können.
	 * <p>
	 * Ist dies der Fall, so kann auf der jeweiligen Verschachtelungstiefe
	 * optimiert werden. Damit wird ein nicht zu unterschätzender Performance-Gewinn
	 * realisiert.
	 * <p>
	 * Um diese Analyse nicht andauernd machen zu müssen, wird dies nur beim ersten Aufruf
	 * (Setzen der Teilvalidierung) gemacht.
	 * <p>
	 * Um das Analyse-Ergebnis aufzubewahren werden dafür bei der Instanz der Klasse
	 * ValidierungArt je Ebene einzelne Flags für eine Gleichbehandlung gesetzt.
	 * <p>
	 * Die jeweiligen Bedingungen sind ebenfalls bei der Klasse ValidierungArt
	 * dokumentiert.
	 */
	public void setIsValidationPartWith3ValueLogic() {
		boolean[] ebenenGleich = createArrayFuerEbenenGleich();

		for (RtInternalIdentifier id : validationCache.getAllRelevantEntitiesIntern()) {
			boolean isField = metaData.isUniqueFieldName(id.getName());
			long[] indices = id.getIndices();
			int maxLevel = isField ? id.getIndices().length - 1 : id.getIndices().length;
			for (int i = 0; i < maxLevel; ++i) {
				if (!ebenenGleich[i + 1]) {
					continue; //Wiederholungsebene ist bereits nicht gleich.
				}
				long maxIndex = MetaDataUtilValidierung.getMaxKontextEbenenIndex(id.getName(), i, metaData);
				long currentIndex = indices[i];
				if (maxIndex > 1 && currentIndex != IIdentifier.ALL_INDICES) {//Wiederholbarkeit > 1
					ebenenGleich[i + 1] = false;
				}
			}

			if (isField) {
				//Zeile muss separat behandelt werden. Zeile ist immer der letzte Eintrag in indices, unabhängig
				//davon, wieviele Kontext-Ebenen vorhanden sind.
				long maxZeile = metaData.getMaxZeile(id.getName());
				long zeile = indices[indices.length - 1];
				if (maxZeile > 1 && zeile != IIdentifier.ALL_INDICES) {
					ebenenGleich[ebenenGleich.length - 1] = false;
				}
			}

			//USB wird separat behandelt
			if (id.getUsb() != IIdentifier.ALL_INDICES && metaData.getMaxUsb(id.getName()) != IIdentifier.USB_NOT_SPECIFIED) {
				ebenenGleich[0] = false;
			}
		}
		validierungArt.setValidationPartWith3ValueLogic(ebenenGleich);
		validationCache.setValidationPartWith3ValueLogic();
	}

	/**
	 * Bereitet den MainValidatorController auf die folgende Regelprüfung vor.
	 */
	public void initFuerRegelpruefung(String regelName, PruefErgebnisTyp severity, boolean isUSBMusterRegel) {
		this.regelName = regelName;
		pruefErgebnisTyp = severity;
		this.isUSBMusterRegel = isUSBMusterRegel;

		logRegel(regelName);
	}

	/**
	 * Ersetzt im Fehlertext die Parametrisierungen durch die tatsächlichen Werte.
	 * Parameter sind immer zwischen zwei Dollarzeichen geschlossen.
	 * Das Dollarzeichen wird im Fehlertext durch $$ parametrisiert.
	 */
	private String substFehlertext(IterationState idx, String fehlerText) {
		String[] teile = StringUtils.splitPreserveAllTokens(fehlerText, "$");

		//nur jeder zweite ist Parameter.
		for (int i = 1; i < teile.length; i = i + 2) {
			teile[i] = substParameter(teile[i], idx);
		}
		//jetzt kleben wir die Teile wieder zusammen; eventuelle Dollarzeichen sind bereits
		//in den Ersetzungen, denn bei manchen (wg. .Wert oder #Zeile+/-n) mussten sie entfernt werden
		return StringUtils.join(teile, "");
	}

	/**
	 * Führt die Ersetzung für einen Parameter im Fehlertext durch.
	 */
	private String substParameter(String parameter, IterationState idx) {
		if (StringUtils.isEmpty(parameter)) {
			return "$";
		}

		parameter = parameter.trim();

		if ("#Usb".equals(parameter)) {
			return substUsbParameter(idx.getIndex(0));
		}

		/*
		 * Parameter generiert aus '#Zeile +/- Konstante'
		 */
		if (ZEILE_PLUS_MINUS_PATTERN.matcher(parameter).matches()) {
			//Rückgabe ohne Dollarzeichen, da Wertersetzung
			return behandleZeilePlusMinusKonstante(parameter, idx.getZeile());
		}
		if (parameter.startsWith("#")) {
			return substIndexNumberParam(parameter, idx);
		}

		/*
		 * Parameter 'feldUniqueName@e1@e2...@en'
		 * bzw. 'feldUniqueName@e1@e2...@en.Wert'
		 * bzw. 'feldUniqueName@e1@e2...@en->categoryName'
		 * bzw. 'index(feldUniqueName).Wert'
		 * bzw. 'index(feldUniqueName)->categoryName' generiert aus 'feldBeschreibung'
		 */
		String fieldSpec = getFieldSpec(parameter);
		if (parameter.startsWith(Constants.ERROR_TEXT_PARAM_4_INDEX_FIELD_REF)) {
			return substIndexFieldRef(parameter, idx, fieldSpec);
		}

		EntityIterator fIt = new EntityIterator(RtIdentifierTemplate.fromStringRepresentation(fieldSpec), idx);
		RtInternalIdentifier idParamIntern = fIt.get();
		if (parameter.endsWith(Constants.ERROR_TEXT_PARAM_4_FIELD_VALUE)) {
			//Rückgabe ohne die Dollarzeichen, da Wertersetzung
			//fuer .Wert wird der interne USB verwendet
			return getReplacementForParam(idParamIntern);
		} else if (parameter.contains(Constants.CATEGORY_VALUE_ACCESS)) {
			return validationCache.getWertXML(idParamIntern).getWert();
		} else {
			RtExternalIdentifier idParamExtern = validationCache.convert2Extern(idParamIntern);
			if (!isUSBMusterRegel) {
				idParamExtern.setUsb(idParamIntern.getUsb());
			}
			return validationCache.getDaten().identifierToString(idParamExtern);
		}
	}

	private String substIndexNumberParam(String parameter, IterationState idx) {
		/*
		 * Parameter $#n$ wird zur Laufzeit ersetzt durch den n-ten Index des aktuellen
		 * Fehlerfeldes.
		 * Bei der Codegenerierung wurde der Parameter aus '#<kontextPfad>' generiert.
		 */
		if (parameter.substring(1).matches("\\d+")) {
			int ebene = Integer.parseInt(parameter.substring(1));
			//Rückgabe ohne Dollarzeichen, da Wertersetzung
			return String.valueOf(idx.getIndex(ebene + 1)); // +1 because of additional usb
		} else {
			// #<group>[#<semanticIdx>]
			EntityIterator it = new EntityIterator(RtIdentifierTemplate.fromStringRepresentation(parameter.substring(1)), idx);
			RtInternalIdentifier semIdxId = it.get();
			RtInternalIdentifier resolvedId = validationCache.resolveSemanticIndex(semIdxId);
			long[] indices = resolvedId.getIndices();
			// the relevant number is the index on the level of the group
			long result = indices[indices.length - 1];
			if (result == IIdentifier.UNKNOWN) {
				return "?";
			}
			return String.valueOf(result);
		}
	}

	private String substIndexFieldRef(String parameter, IterationState idx, String fieldSpec) {
		String indexFieldValue = getIndexFieldValue(fieldSpec, idx);
		if (indexFieldValue == null) {
			// this can happen if the index field is not passed by the caller or is not in the relevant fields
			return "";
		}
		if (parameter.endsWith(Constants.ERROR_TEXT_PARAM_4_FIELD_VALUE)) {
			// case 'index(feldUniqueName).Wert'
			IMetaField f = metaData.getMetaField(fieldSpec);
			try {
				return f.convert(indexFieldValue, modus.getPresentationInformation(), true, modus.getLanguage(),
						RawParamHandler4Convert.of(new Object[] { modus.customFieldTypeFactory() }));
			} catch (IllegalArgumentException e) {
				// this can happen if there is a formal error for the index field
				return "";
			}
		} else {
			// case 'index(feldUniqueName)->categoryName'
			String categoryName = parameter.substring(
					parameter.indexOf(Constants.CATEGORY_VALUE_ACCESS) + Constants.CATEGORY_VALUE_ACCESS.length());
			String categoryValue = MetaDataUtilValidierung.getValue4Category(metaData, fieldSpec, indexFieldValue, categoryName);
			// categoryValue == null can happen if there is a formal error for the index field
			return categoryValue != null ? categoryValue : "";
		}
	}

	private String getIndexFieldValue(String fieldUniqueName, IterationState idx) {
		if (idx.getValue() != null) {
			// case 'parallel iteration'
			return idx.getValue();
		} else {
			// case 'iteration'
			IMetaField f = metaData.getMetaField(fieldUniqueName);
			@SuppressWarnings("unchecked")
			List<Long> maxIndices = (List<Long>) f.getValue(IMetaKeysInternal.FELD_MAXINDICES);
			// skip the usb which is at pos 0 of idx.getIndexes()
			long[] indexFieldIndices = Arrays.copyOfRange(idx.getIndexes(), 1, maxIndices.size() + 1);
			// the line iteration has to be always 1 for A12-DVs
			indexFieldIndices[indexFieldIndices.length - 1] = 1;
			RtInternalIdentifier indexFieldId = new RtInternalIdentifier(fieldUniqueName, indexFieldIndices);
			String indexFieldValue = validationCache.getWertXML(indexFieldId).getWert();
			return RtExternalIdentifierWithSemanticIndex.emptyToNull(indexFieldValue);
		}
	}

	private String getFieldSpec(String parameter) {
		if (parameter.startsWith(Constants.ERROR_TEXT_PARAM_4_INDEX_FIELD_REF)) {
			return parameter.substring(Constants.ERROR_TEXT_PARAM_4_INDEX_FIELD_REF.length(), parameter.indexOf(')'));
		} else if (parameter.endsWith(Constants.ERROR_TEXT_PARAM_4_FIELD_VALUE)) {
			return parameter.substring(0, parameter.length() - Constants.ERROR_TEXT_PARAM_4_FIELD_VALUE.length());
		} else {
			return parameter;
		}
	}

	/**
	 * Der Parameter #Usb wird ersetzt durch den Wert des USB-Indexfeldes, falls ein solches
	 * existiert, ansonsten mit dem aktuellen USB. (Gibt es keine USB-Indexfelder, so ist
	 * XML-USB = synthetischer USB, vgl. {@link USBIndexCache})
	 */
	private String substUsbParameter(long usbIndex) {
		return validationCache.getUsbCache()
				.convertInternToXmlUSB(usbIndex); // Liefert "0" wenn das Usb-Index-Feld nicht angegeben wurde
	}

	/**
	 * Hilfsmethode: ersetzt im Parameter den Ausdruck "#Zeile+Konstante" bzw. "#Zeile-Konstante"
	 * durch den Wert z+/-Konstante. Wird benötigt für die Ersetzung der Parameter in Fehlertexten.
	 */
	private String behandleZeilePlusMinusKonstante(String parameter, long z) {
		String[] teile = parameter.split("[-+]");
		if (teile.length > 1) {
			int konstante = Integer.parseInt(teile[1]);
			if (parameter.contains("-")) {
				konstante = -konstante;
			}
			return z + konstante >= 0 ? Long.toString(z + konstante) : "0";
		}
		return parameter;
	}

	/**
	 * Returns according to the given identifier the value which should substitute the parameter
	 * $&lt;identifier&gt;.Wert$ in a rule error text. This value should be suitable for being
	 * displayed (for example in a html site) and therefore the method IData#getValueForDisplay is
	 * called (in this way the caller of the validation is responsible for a correct
	 * implementation). If no value is found, a default value will be returned.
	 */
	private String getReplacementForParam(RtInternalIdentifier idInternal) {
		// we are about to call an external implementation and therefore we need an external id
		RtExternalIdentifier idExternal = validationCache.convert2Extern(idInternal);
		String valueForDisplay = null;
		if (isValidId(idExternal)) {
			valueForDisplay = validationCache.getDaten().getValueForDisplay(idExternal);
		}
		if (StringUtils.isEmpty(valueForDisplay)) { // no value found, we take the default value
			IFormatDefinition f = metaData.getIFormatDefinition(idInternal.getName());
			if (f == null) {
				return "";
			}
			valueForDisplay = f.getDefaultValue(metaData, modus.getPresentationInformation(), modus.getLanguage());
		}
		return valueForDisplay;
	}

	/**
	 * Identifiers might contain -5 values if an index defined by index field value could not be resolved.
	 * These are declared as invalid in this method.
	 */
	private boolean isValidId(RtExternalIdentifier idExternal) {
		return Arrays.stream(idExternal.getIndices()).allMatch(it -> it >= 0);
	}

	public void addValidatorMessage(String fehlerFeldName, IterationState idx, String key) {
		addValidatorMessage(fehlerFeldName, idx, key, IResult.ErrorType.VALUE_ERROR);
	}

	public void addValidatorMessage(String fehlerFeldName, IterationState idx, String key, ErrorType fehlerTyp) {
		addValidatorMessageWithReplacement4RepetitionNotUnique(fehlerFeldName, idx, key, fehlerTyp, new HashSet<>());
	}

	public void addValidatorMessage(String fehlerFeldName, IterationState idx, String key, Exception e) {
		String fehlerText = LocalizationHelper.getValue(LocalizationHelper.ERROR_DURING_DATA_CHECK_EVAL, modus.getLanguage());
		if (e instanceof ArithmeticException && "Division by zero".equals(e.getMessage())) {
			fehlerText = fehlerText + LocalizationHelper.getValue(LocalizationHelper.DIVISION_BY_ZERO_INVALID, modus.getLanguage());
		} else {
			fehlerText = fehlerText + e.getMessage();
		}
		addValidatorMessage(fehlerFeldName, idx, key, IResult.ErrorType.VALUE_ERROR, fehlerText, false, new HashSet<>());
		LOG.error(fehlerText, e);
	}

	public void addValidatorMessageWithReplacement4RepetitionNotUnique(String fehlerFeldName, IterationState idx, String key,
			ErrorType fehlerTyp, Set<LongArray> erroneousRepetitions) {
		String fehlerText = metaData.getFehlertext(regelName, modus.getLanguage());
		addValidatorMessage(fehlerFeldName, idx, key, fehlerTyp, fehlerText, true, erroneousRepetitions);
	}

	private void addValidatorMessage(String fehlerFeldName, IterationState idx, String key, ErrorType fehlerTyp, String fehlerText,
			boolean acquireReferencedFields, Set<LongArray> erroneousRepetitions) {
		if (fehlerTyp == null) {
			return;
		}
		fehlerText = substFehlertext(idx, fehlerText);
		RtInternalIdentifier errorFieldId = RtInternalIdentifier.createFromIterationState(fehlerFeldName, idx);
		RtExternalIdentifier errroFieldIdExtern = validationCache.convert2Extern(errorFieldId);
		if (!isUSBMusterRegel) {
			errroFieldIdExtern.setUsb(idx.getIndex(0));
		}

		Set<IIdentifier> referencedFields = acquireReferencedFields ?
				getReferencedFields(idx, regelName, erroneousRepetitions) :
				Collections.<IIdentifier>emptySet();
		Set<IIdentifier> refOmissionFields = (fehlerTyp == IResult.ErrorType.OMISSION_ERROR && acquireReferencedFields)
				? getRefOmissionFields(idx, regelName, erroneousRepetitions)
				: Collections.<IIdentifier>emptySet();
		Set<RtIdentifierTemplate> referencedEntityIds = acquireReferencedFields ?
				getReferencedEntities(regelName, erroneousRepetitions) :
				Collections.emptySet();
		Set<RtIdentifierTemplate> referencedOmissionEntities = acquireReferencedFields ?
				getRefOmissionEntities(regelName, erroneousRepetitions) :
				Collections.emptySet();
		Set<IIdentifier> unknowns = extractUnknowns(referencedFields);
		ICause cause = createCause(errroFieldIdExtern, idx, referencedEntityIds, referencedOmissionEntities, fehlerTyp);
		addValidatorMessageToResult(key, fehlerTyp, fehlerText, errroFieldIdExtern, referencedFields, refOmissionFields, unknowns,
				cause);
	}

	private void addValidatorMessageToResult(String key, ErrorType fehlerTyp, String fehlerText, RtExternalIdentifier idExtern,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionFields, Set<IIdentifier> unknowns, ICause cause) {
		SeverityType severity;
		if (pruefErgebnisTyp == PruefErgebnisTyp.Fehler) {
			pruefErgebnis.addError(regelName, key, idExtern, fehlerText, fehlerTyp, referencedFields, refOmissionFields, unknowns);
			severity = SeverityType.ERROR;
		} else if (pruefErgebnisTyp == PruefErgebnisTyp.Hinweis) {
			pruefErgebnis.addHint(regelName, key, idExtern, fehlerText, fehlerTyp, referencedFields, refOmissionFields, unknowns);
			severity = SeverityType.HINT;
		} else {
			pruefErgebnis.addInfo(regelName, key, idExtern, fehlerText, fehlerTyp, referencedFields, refOmissionFields, unknowns);
			severity = SeverityType.INFO;
		}
		pruefErgebnis.addMessageV2(regelName, key, fehlerText, fehlerTyp, severity, cause);
		LOG.logRegelFires(regelName, idExtern, validationCache.getDaten());
	}

	public ICause createCause(RtExternalIdentifier idExtern, IterationState iterState, Set<RtIdentifierTemplate> referencedEntities,
			Set<RtIdentifierTemplate> refOmissionEntities, ErrorType fehlerTyp) {
		// Fields can only create omission errors if they are empty.
		assert iterState != null : "iterState was null in createCause!";
		Set<IIdentifier> emptyEntities = filter4GivenAndMapToIIdentifier(refOmissionEntities, false, iterState);
		// We assume that if a field can heal the rule by adding it if it was not given, then we would detect it as omission field.
		// ReferencedFields currently contains all fields inclusive the omission fields. Therefore, given omission fields will
		// also be shown as relevant fields no matter what. But they also could be responsible for the error
		// (e.g. FelderNichtGemeinsamAngegeben). In the future, we could remove the fields which cannot cause the error if they
		// are not given from referenced fields during code generation.
		Set<IIdentifier> givenEntities = filter4GivenAndMapToIIdentifier(referencedEntities, true, iterState);
		if (fehlerTyp == ErrorType.OMISSION_ERROR) {
			// For entities containing 0-index, they may be in both sets. But the sets better were distinct.
			givenEntities = givenEntities.stream().filter(e -> !emptyEntities.contains(e)).collect(Collectors.toSet());
			return new ErrorCause(idExtern, List.of(emptyEntities, givenEntities));
		} else {
			Set<IIdentifier> allEntities = givenEntities;
			allEntities.addAll(emptyEntities);
			return new ErrorCause(idExtern, List.of(allEntities));
		}
	}

	/**
	 * Filters the given identifier templates to keep only the ones that are given (if the parameter given==true) or not-given
	 * (if given==false) for the specified iterState.
	 * Additionally, the RtIdentifierTemplates are converted to IIdentifiers according to the iterState; If a template specified
	 * an aggregation for some of the repeatability layers, this will be represented by a "0" (wildcard) in the IIdentifier (even
	 * if the template might have specified a restricted range of repetitions).
	 */
	private Set<IIdentifier> filter4GivenAndMapToIIdentifier(
			Set<RtIdentifierTemplate> refEntities,
			boolean given,
			IterationState iterState
	) {
		return refEntities.stream()
				.filter(tpl -> isGiven(tpl, given, iterState))
				// isGiven has already been called *before* this conversion, so it is ok to use
				// createFromTemplateReplaceAggregationsWith0DoNotProcessFurther()
				.map(tpl -> RtInternalIdentifier.createFromTemplateReplaceAggregationsWith0DoNotProcessFurther(tpl, iterState))
				.map(intId -> validationCache.convert2Extern(intId, true)).collect(Collectors.toSet());
	}

	private boolean isGiven(RtIdentifierTemplate id, boolean given, IterationState iterState) {
		// TODO [A12K-2281] maybe we need still more tests, particularly for partialValidation and for H-DVs
		if (!EntityUtil.isRootGroup(id)) {
			if (given) {
				return mindestensEineEntityAngegeben(iterState, List.of(id)).isKnownAndTrue();
			} else {
				return nichtAlleOderKeineEntityAngegeben(iterState, List.of(id), true).isKnownAndTrue();
			}
		} else {
			var usbAndLfdnr = deriveUsbAndLfdnr4Vordruck(id, iterState);
			long usb = usbAndLfdnr.getEntry1();
			long lfdNr = usbAndLfdnr.getEntry2();
			String rootGroupName = EntityUtil.getRootGroupName(metaData.getPfadName(id.getUniqueName()));
			if (given) {
				return mindestensEinVordruckAngegeben(rootGroupName, lfdNr, usb, iterState,
						id) // is iterState the correct iter state here??
						!= ValidierungsErgebnis.FALSE_OR_UNKNOWN;
			} else {
				if (id.isAggregation()) { // for root-groups, we lack an implementation of
					// nichtAlleOderKeine...Angegeben, so we'll keep this "err-on-safe-side" shortcut from the pre-A12K-2273
					// implementation
					return true;
				}
				// keinVordruckAngegeben(rootGroupName, lfdNr, usb, iterState, id).isKnownAndTrue(); can return false-UNKNOWNs
				// in various cases (at least with Teilvalidierung). This could lead to refOmissionEntities beeing incorrectly
				// filtered out (and the corresponding (non-omission) refEntity could be filtered out too
				// -> use mindestensEinVordruckAngegeben instead, so that we err on the side of returning true.
				return mindestensEinVordruckAngegeben(rootGroupName, lfdNr, usb, iterState, id)
						== ValidierungsErgebnis.FALSE_OR_UNKNOWN;
			}
		}
	}

	private Pair<Long, Long> deriveUsbAndLfdnr4Vordruck(RtIdentifierTemplate id, IterationState iterState) {
		List<RtIdentifierTemplateLayerSpec> layersWithIterationResolved =
				EntityUtil.resolveIterations(id.getLayerSpecWithUsb(), iterState.getIndexes());

		long usb = layersWithIterationResolved.get(0).getIdxStart();
		assert usb != IIdentifier.ALL_INDICES
				: "wildcards or ranges are expected to be represented by explicit start and end indices in RtIdentifierTemplate";
		assert usb != IIdentifier.ITERATION : "iteration should already be resolved via EntityUtil.resolveIterations";
		// USB_NOT_SPECIFIED is possible
		assert usb != IIdentifier.SEMANTIC_INDEX : "semantic index not expected in H-DVS";
		// UNKNOWN (unresolved semantic index or parallel iteration) is this possible?
		// PARALLEL_ITERATION ?
		// REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED is this possible?
		// 1: possible: non-repeatable of start for *-reference
		// > 1: probably not possible because range restrictions might not exists for root groups
		long usbEnd = layersWithIterationResolved.get(0).getIdxEnd();
		if (usb != usbEnd) {
			assert usb > 0 : usb;
			assert usbEnd > usb : usb + " " + usbEnd;
			usb = IIdentifier.ALL_INDICES;
		}

		long lfdNr = layersWithIterationResolved.get(1).getIdxStart();
		assert lfdNr != IIdentifier.ALL_INDICES
				: "wildcards or ranges are expected to be represented by explicit start and end indices in RtIdentifierTemplate";
		assert lfdNr != IIdentifier.ITERATION : "iteration should already be resolved via EntityUtil.resolveIterations";
		assert lfdNr != IIdentifier.USB_NOT_SPECIFIED : "not applicable for lfdNr";
		// SEMANTIC_INDEX is probably possible
		// UNKNOWN (unresolved semantic index) is this possible?
		// PARALLEL_ITERATION ?
		// REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED is this possible?
		// 1: possible: non-repeatable of start for *-reference
		// > 1: probably not possible because range restrictions might not exists for root groups
		long lfdNrEnd = layersWithIterationResolved.get(1).getIdxEnd();
		if (lfdNr != lfdNrEnd) { // replace explicit ranges by IIdentifier.ALL_INDICES
			assert lfdNr > 0 : lfdNr;
			assert lfdNrEnd > lfdNr : lfdNr + " " + lfdNrEnd;
			lfdNr = IIdentifier.ALL_INDICES;
		}

		return new Pair<>(usb, lfdNr);
	}

	private Set<IIdentifier> extractUnknowns(Set<IIdentifier> referencedFields) {
		Set<IIdentifier> result = new HashSet<>();
		for (IIdentifier refField : referencedFields) {
			if (ArrayUtils.indexOf(refField.getIndices(), IIdentifier.UNKNOWN) >= 0) {
				result.add(refField);
			}
		}
		return result;
	}

	private Set<IIdentifier> getReferencedFields(IterationState idx, String ruleName, Set<LongArray> erroneousRepetitions) {
		Set<RtInternalIdentifier> fields = metaData.getReferencedFieldsWithoutReplacement(ruleName);
		return replaceIndices(idx, erroneousRepetitions, fields, false);
	}

	private Set<IIdentifier> getRefOmissionFields(IterationState idx, String ruleName, Set<LongArray> erroneousRepetitions) {
		Set<RtInternalIdentifier> fields = metaData.getReferencedOmissionFieldsWithoutReplacement(ruleName);
		return replaceIndices(idx, erroneousRepetitions, fields, false);
	}

	private Set<RtIdentifierTemplate> getReferencedEntities(String ruleName, Set<LongArray> erroneousRepetitions) {
		Set<RtIdentifierTemplate> entities = metaData.getReferencedEntitiesWithoutReplacement(ruleName);
		return expandRepetitionNotUniqueIndices(erroneousRepetitions, entities);
	}

	private Set<RtIdentifierTemplate> getRefOmissionEntities(String ruleName, Set<LongArray> erroneousRepetitions) {
		Set<RtIdentifierTemplate> entities = metaData.getReferencedOmissionEntitiesWithoutReplacement(ruleName);
		return expandRepetitionNotUniqueIndices(erroneousRepetitions, entities);
	}

	private Set<IIdentifier> replaceIndices(IterationState idx, Set<LongArray> erroneousRepetitions,
			Set<RtInternalIdentifier> fields, boolean useSemanticIndex) {
		Set<IIdentifier> result = new HashSet<>();
		for (RtInternalIdentifier field : fields) {
			RtExternalIdentifier refId = resolveExternField(field, idx, useSemanticIndex);
			if (ArrayUtils.contains(field.getIndices(),
					IIdentifier.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED)) {
				erroneousRepetitions.stream().forEach(errRep -> result.add(replaceRepNonUnqConstants(refId, errRep)));
			} else {
				result.add(refId);
			}
		}
		return result;
	}

	private Set<RtIdentifierTemplate> expandRepetitionNotUniqueIndices(
			Set<LongArray> erroneousRepetitions,
			Set<RtIdentifierTemplate> entities
	) {
		Set<RtIdentifierTemplate> result = new HashSet<>();
		for (RtIdentifierTemplate entity : entities) {
			if (entity.getLayerSpec().stream()
					.anyMatch(lSpec -> lSpec.getIdxStart() == IIdentifier.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED)
			) {
				erroneousRepetitions.forEach(errRep -> result.add(replaceRepNonUnqConstants(entity, errRep)));
			} else {
				result.add(entity);
			}
		}
		return result;
	}

	private RtExternalIdentifier resolveExternField(RtInternalIdentifier idInternal, IterationState idx, boolean useSemanticIndex) {
		RtInternalIdentifier result = new EntityIterator(idInternal, idx).get();
		return validationCache.convert2Extern(result, useSemanticIndex);
	}

	private RtExternalIdentifier replaceRepNonUnqConstants(RtExternalIdentifier id, LongArray erroneousRepetition) {
		long[] newIndices = id.getIndices().clone();
		long[] errIndicesWithUSB = erroneousRepetition.getOriginClone();
		for (int i = 0; i < newIndices.length; i++) {
			if (newIndices[i] == IIdentifier.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED) {
				// errIndicesWithUSB comes from interationState and contains USB at position 0, so take i + 1
				newIndices[i] = errIndicesWithUSB[i + 1];
			}
		}
		return new RtExternalIdentifier(id.getName(), newIndices);
	}

	private RtIdentifierTemplate replaceRepNonUnqConstants(RtIdentifierTemplate id, LongArray erroneousRepetition) {
		List<RtIdentifierTemplateLayerSpec> oldLayerSpecWithUsb = id.getLayerSpecWithUsb();
		List<RtIdentifierTemplateLayerSpec> newLayerSpecWithUsb = new ArrayList<>();
		long[] errIndicesWithUSB = erroneousRepetition.getOriginClone();
		for (int i = 0; i < oldLayerSpecWithUsb.size(); i++) {
			if (oldLayerSpecWithUsb.get(i).getIdxStart() == IIdentifier.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED) {
				assert oldLayerSpecWithUsb.get(i).getIdxEnd() == IIdentifier.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED;
				long idxStartAndEnd = errIndicesWithUSB[i];
				newLayerSpecWithUsb.add(new RtIdentifierTemplateLayerSpec(idxStartAndEnd, idxStartAndEnd));
			} else {
				newLayerSpecWithUsb.add(oldLayerSpecWithUsb.get(i));
			}
		}
		return id.withLayerSpecWithUsb(newLayerSpecWithUsb);
	}

	public void preliminaryError(
			String errorFieldUniqueName,
			IterationState idx,
			String errorId,
			RuntimeFormalErrorEnum errorMsgId,
			ErrorType errorType
	) {
		RtInternalIdentifier id = RtInternalIdentifier.createFromIterationState(errorFieldUniqueName, idx);
		RtExternalIdentifier idExtern = validationCache.convert2Extern(id);
		String fehlerText = getErrorMessage(errorFieldUniqueName, errorMsgId);
		String valueExtern = validationCache.getWertOberflaeche(id);
		if (valueExtern == null) {
			valueExtern = "";
		}

		if (fehlerText.contains(Constants.FORMAL_ERROR_TEXT_PARAM_4_FIELD_VALUE)) {
			fehlerText = fehlerText.replace(Constants.FORMAL_ERROR_TEXT_PARAM_4_FIELD_VALUE, valueExtern);
		}
		fehlerText = FehlerHandler.transformFehlerText(fehlerText, validationCache.getDaten(), idExtern, valueExtern);

		this.formalChecker.addFormalError(this.pruefErgebnis, idExtern, errorId, fehlerText, errorType);
		this.validationCache.markFieldAsInvalidForValidation(id, idExtern);
	}

	private String getErrorMessage(String fieldFullName, RuntimeFormalErrorEnum errorMsgId) {
		if (errorMsgId == RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER) {
			return this.metaData.getErrorMessage4Req(fieldFullName, this.modus.getLanguage());
		} else {
			return this.formalChecker.getFehlertext(errorMsgId);
		}
	}
}
