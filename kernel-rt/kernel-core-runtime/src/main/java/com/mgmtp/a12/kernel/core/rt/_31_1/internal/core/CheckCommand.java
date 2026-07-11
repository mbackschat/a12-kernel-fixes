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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.error.FehlerHandler;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.logging.ValidationLogger;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.ErrorCause;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.IFormaleFehlerConstants;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtExternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICause;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;

import lombok.NonNull;

/**
 * Kapselt den Code der einzelnen checkKommandos.
 * Die formale Pruefung ist generisch für alle Felder und erfolgt in dieser Klasse.
 * Die abhängigen Prüfungen werden nach der formalen Prüfung angestossen.
 */
public class CheckCommand {
	protected static final ValidationLogger LOG = new ValidationLogger();

	// Daten für eine Validierungsprüfung:
	protected final @NonNull ValidationMode modus;
	protected final @NonNull DarstellungsInformationIntern darstellungsInfo;

	protected final IValidatorIntern validator;
	protected final @NonNull IMetaDataValidierung metaData;
	// Not final, because we have to change it for calculations
	protected @NonNull ValidationCache validationCache;
	protected final @NonNull FormalChecker formalChecker;

	// Stores the maximum USB of the model
	protected long modelMaxUsb = -1;

	public CheckCommand(
			IValidatorIntern validatorOrNull,
			ValidationMode modeOrNull,
			@NonNull IMetaDataValidierung metaData,
			@NonNull IData data // only for stringification of identifiers and values in error messages
	) {
		this.modus = modeOrNull == null ? new ValidationMode() : modeOrNull;
		this.darstellungsInfo = new DarstellungsInformationIntern(
				metaData.getDezimalTrenner(),
				this.modus.getPresentationInformation(),
				metaData.getBaseYearOrDefault(),
				metaData.getDefaultPresentationInformation().get(this.modus.getLanguage()),
				metaData.getTimeZone()
		);
		this.validator = validatorOrNull;
		this.metaData = metaData;
		this.validationCache = new ValidationCache(metaData, this.modus, this.darstellungsInfo);
		this.formalChecker = new FormalChecker(this.metaData, this.darstellungsInfo, this.modus, data, isMeldeFehler());
	}

	public void formalePruefungMitDaten(
			@NonNull IData data,
			Set<RtInternalIdentifier> relevantFieldsOrNull,
			@NonNull IResult result
	) {
		this.validationCache.setDaten(data);
		formalePruefung(relevantFieldsOrNull, result);
	}

	/**
	 * Hilfsmethode zur formalen Pruefung der relevanten Felder.
	 *
	 * @param relevantFieldsOrNull
	 * 		prüfungsrelevanteFelder falls Teilvalidierung , null bei Vollvalidierung
	 * @param result
	 * 		zum Speichern der Ergebnisse bei formalen Fehlern.
	 */
	protected void formalePruefung(
			Set<RtInternalIdentifier> relevantFieldsOrNull,
			@NonNull IResult result
	) {
		long startMesspunkt = LOG.startMesspunkt();
		/*
		 * Die Felder, die formal korrekt sind, werden hier zwischengespeichert; sie können erst
		 * dem Datencache hinzugefügt werden, wenn die internen USBs ermittelt sind.
		 */
		Map<RtInternalIdentifier, Pair<String, String>> tempCache = new HashMap<>();
		List<Pair<RtInternalIdentifier, IIdentifier>> invalidFieldsWithSynthUsb = new ArrayList<>();
		Map<String, Set<RtInternalIdentifier>> relevantFieldsCacheOrNull = relevantFieldsOrNull != null
				? makeRelevanteFelderCache(addAllParentGroups(relevantFieldsOrNull))
				: null;

		for (IIdentifier idExtern : validationCache.getDaten()) {
			boolean isFieldDefined = metaData.isFeldDefiniert(idExtern.getName());
			boolean isGroupDefined = metaData.isGroupDefined(idExtern.getName());
			if (!isFieldDefined && (!isGroupDefined || metaData.hasUniqueEntities())) {
				validationCache.addUnbekanntesFeld(idExtern);
				continue;
			}

			if (!isFieldDefined) {
				// Special treatment for groups
				checkGroup(idExtern, result, tempCache, relevantFieldsCacheOrNull);
				continue;
			}

			String uniqueName = metaData.getUniqueName(idExtern.getName());
			List<Long> maxIndices = metaData.getMaxFieldIndices(uniqueName);
			if (!this.formalChecker.checkIndices(idExtern, maxIndices, result)) {
				continue;
			}
			long maxUsb = metaData.getMaxUsb(uniqueName);
			modelMaxUsb = Math.max(modelMaxUsb, maxUsb);
			if (!formalChecker.checkUSB(idExtern, maxUsb, result)) {
				continue;
			}

			if (LOG.isTraceEnabled()) {
				LOG.trace("Field == " + validationCache.getDaten().identifierToString(idExtern));
			}

			// nur prüfungsrelevante Felder werden überprüft
			RtInternalIdentifier idInternWithSynthUsb = EntityUtil.makeInternIdentifier(idExtern, metaData);
			if (!EntityUtil.isPruefungsRelevant(idInternWithSynthUsb, relevantFieldsCacheOrNull)) {
				continue;
			}

			String value = validationCache.getDaten().getValue(idExtern);
			if (value == null || value.isEmpty()) { //Leerstring oder Null value entsprechen "nicht angegeben"
				continue;
			}

			var fieldValueCheck = formalChecker.checkAndConvertFieldValue(idInternWithSynthUsb, idExtern, value, result);
			switch (fieldValueCheck.validity()) {
				case Invalid -> invalidFieldsWithSynthUsb.add(new Pair<>(idInternWithSynthUsb, idExtern));
				case Valid -> {
					if (fieldValueCheck.normalizedValue() != null && fieldValueCheck.internalXmlValue() != null) {
						addXmlAndDisplayValuesByInternIdToTempCache(tempCache, idExtern, idInternWithSynthUsb,
								fieldValueCheck.normalizedValue(), fieldValueCheck.internalXmlValue());
					} else { // considered valid, but the value should not be interned
						addXmlAndDisplayValuesByInternIdToTempCache(tempCache, idExtern, idInternWithSynthUsb,
								ValidationCache.PSEUDO_VALUE, ValidationCache.PSEUDO_VALUE);
					}
				}
			}
		} // Ende Schleife über alle Formatprüfungen

		/**
		 * Konvertierung der Felder in solche mit internen USBs (für Felder von USB-Vordrucken ist
		 * dies der ermittelte interne USB,
		 * für die von nicht USB-Vordrucken der Wert 1) und befüllt den Validationcache.
		 */
		updateDatenCacheUndUSBCacheAndDamageTmpCache(tempCache);
		tempCache = null; // don't use this map afterward, it is no longer consistent if usbs habe been updated.

		// report the invalid fields after converting from synth usb to intern usb
		for (Pair<RtInternalIdentifier, IIdentifier> invalidFieldWithSynthUsb : invalidFieldsWithSynthUsb) {
			RtInternalIdentifier idInternWithSynthUsb = invalidFieldWithSynthUsb.getEntry1();
			IIdentifier idExtern = invalidFieldWithSynthUsb.getEntry2();
			RtInternalIdentifier idInternWithInternUsb = metaData.hasUniqueEntities()
					? convertSynthUsbToInternUsb(idInternWithSynthUsb) : idInternWithSynthUsb;
			validationCache.addInvalidFieldValue(idInternWithInternUsb, idExtern);
		}

		/**
		 * Die unbekannten Felder die in den Daten oder in den prüfungsrelevanten Feldern gefunden
		 * wurden, werden der Ergebnismenge hinzugefügt.
		 */
		addUnbekannteFelderToErgebnis(result);

		if (LOG.isTraceEnabled()) {
			LOG.logMessung("Die formale Prüfung benötigt ", startMesspunkt);
		}
	}

	private void addXmlAndDisplayValuesByInternIdToTempCache(Map<RtInternalIdentifier, Pair<String, String>> tempCache,
			IIdentifier idExtern, RtInternalIdentifier idIntern, String value, String xmlValue) {
		assert !tempCache.containsKey(idIntern) : String.format(
				"The field '%s' is already contained in the data cache. The field already has a value assigned.",
				validationCache.getDaten().identifierToString(idExtern));
		tempCache.computeIfAbsent(idIntern, k -> new Pair<>(value, xmlValue));

		if (metaData.getMaxUsb(idIntern.getName()) != IIdentifier.USB_NOT_SPECIFIED) {
			validationCache.getUsbCache().updateMitXMLUSB(idIntern, xmlValue);
		}
	}

	protected RtInternalIdentifier convertSynthUsbToInternUsb(RtInternalIdentifier id) {
		return id.withUsb(validationCache.getUsbCache().getInternUSBAndUpdateUSBCache(id));
	}

	private void checkGroup(IIdentifier idExtern, IResult result, Map<RtInternalIdentifier, Pair<String, String>> tempCache,
			Map<String, Set<RtInternalIdentifier>> relevantFieldsCacheOrNull) {
		List<Long> maxIndices = metaData.getMaxGroupIndices(idExtern.getName());
		if (!formalChecker.checkIndices(idExtern, maxIndices, result)) {
			return;
		}
		String wert = validationCache.getDaten().getValue(idExtern);
		if (StringUtils.isEmpty(wert)) { // Also for groups: non existent or empty values mean N/A
			return;
		}

		RtInternalIdentifier idIntern = new RtInternalIdentifier(idExtern.getName(), idExtern.getIndices(), idExtern.getUsb());
		if (!EntityUtil.isPruefungsRelevant(idIntern, relevantFieldsCacheOrNull)) {
			return;
		}

		assert !tempCache.containsKey(idIntern) : String.format(
				"The group '%s' is already contained in the data cache. The group already has a value assigned.",
				validationCache.getDaten().identifierToString(idExtern));
		tempCache.computeIfAbsent(idIntern, id -> new Pair<>(wert, wert));
	}

	private void addUnbekannteFelderToErgebnis(IResult ergebnisse) {
		Map<String, RtExternalIdentifier> unbekannteFelder = validationCache.getUnbekannteFelder();
		for (Map.Entry<String, RtExternalIdentifier> e : unbekannteFelder.entrySet()) {
			formalChecker.addFormalError(ergebnisse,
					new RtExternalIdentifier(e.getValue().getName(), new long[e.getValue().getIndices().length],
							(e.getValue().getUsb() == IIdentifier.USB_NOT_SPECIFIED) ? IIdentifier.USB_NOT_SPECIFIED : 0),
					IFormaleFehlerConstants.FEHLER_FELD_UNBEKANNT,
					getFehlertext(RuntimeFormalErrorEnum.FEHLER_FELD_UNBEKANNT));
		}
	}

	/**
	 * Erzeugt aus der Menge der relevanten Felder eine Cache-Struktur, die dann für die
	 * Prüfung, ob ein Feld prüfungsrelevant ist, verwendet wird.
	 *
	 * @return aufgebaute Cache-Struktur oder null falls Vollvalidierung
	 */
	public static @NonNull Map<String, Set<RtInternalIdentifier>> makeRelevanteFelderCache(
			@NonNull Set<RtInternalIdentifier> relevanteFelder
	) {
		Map<String, Set<RtInternalIdentifier>> result = new HashMap<>();

		for (RtInternalIdentifier id : relevanteFelder) {
			Set<RtInternalIdentifier> set = result.computeIfAbsent(id.getName(), s -> new HashSet<>());
			set.add(id);
		}

		return result;
	}

	/**
	 * Felder, für die in der formalen Prüfungen kein Fehler gefunden wurde, werden in den
	 * Datencache geschrieben. Dazu wird der synthetische USB in den internen umgeschlüsselt.
	 * Für Felder von nicht USB-Vordrucken wird der USB auf 1 gesetzt, da intern dieser Wert
	 * verwendet wird.
	 * <br/>
	 * Hinweis: der tmpDatenCacheMitSynthUSB wird dabei inkonsistent, da die Keys in der Hashmap in-place modifiziert werden,
	 * wodurch danach lookups nicht mehr funktionieren.
	 *
	 * @param tmpDatenCacheMitSynthUSB
	 * 		temporärer Datencache
	 */
	private void updateDatenCacheUndUSBCacheAndDamageTmpCache(
			Map<RtInternalIdentifier, Pair<String, String>> tmpDatenCacheMitSynthUSB) {
		for (Entry<RtInternalIdentifier, Pair<String, String>> entry : tmpDatenCacheMitSynthUSB.entrySet()) {
			RtInternalIdentifier id = entry.getKey();
			Pair<String, String> pair = entry.getValue();
			String feldWertOberflaeche = pair.getEntry1();
			String feldWertXML = pair.getEntry2();

			if (metaData.hasUniqueEntities()) {
				long usbIntern = validationCache.getUsbCache().getInternUSBAndUpdateUSBCache(id);
				id.setUsb(usbIntern); // this makes the hashmap tmpDatenCacheMitSynthUSB inconsistent for subsequent lookups,
				// because the key is modified in-place.
			}

			//update Datencache
			validationCache.addFeldToDatenCache(id, feldWertOberflaeche, feldWertXML);
		}
		validationCache.addDefaultValuesForIndexFieldsIfNecessary();
	}

	static String ersetzeFeldParameter(String fehlerTextMitParam, String feldStringRepraesentation) {
		return fehlerTextMitParam.replace("$<fieldName>$", feldStringRepraesentation);
	}

	public static ICause createSingleFieldCause(IIdentifier id) {
		return new ErrorCause(id, Collections.singletonList(Collections.singleton(id)));
	}

	private @NonNull Set<RtInternalIdentifier> addAllParentGroups(@NonNull Set<RtInternalIdentifier> relevantIds) {
		if (metaData.hasUniqueEntities()) {
			return relevantIds;
		}
		Set<RtInternalIdentifier> result = new HashSet<>(relevantIds);
		for (RtInternalIdentifier id : relevantIds) {
			RtInternalIdentifier parentId = RtInternalIdentifier.createParentId(id, metaData.getPfadName(id.getName()));
			while (parentId != null && result.add(parentId)) {
				parentId = RtInternalIdentifier.createParentId(parentId, parentId.getName());
			}
		}
		return result;
	}

	String getFehlertext(RuntimeFormalErrorEnum fehlerId) {
		Locale sprache = modus.getLanguage();
		return FehlerHandler.getFehlertext(fehlerId, metaData.isExtmp(), sprache);
	}

	private boolean isMeldeFehler() {
		return ValidationMode.RuleType.ALL.equals(this.modus.getRuleType())
				|| ValidationMode.RuleType.ERROR.equals(this.modus.getRuleType());
	}
}
