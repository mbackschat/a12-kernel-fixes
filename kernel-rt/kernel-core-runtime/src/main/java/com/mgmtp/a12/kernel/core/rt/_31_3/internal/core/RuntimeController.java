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

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.BedingungsOperatorHelper.VglOp;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.custom.CustomFieldTypeValidationParamImpl;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.FormatDefinitionDatum;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.FormatDefinitionDatumBereich;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.FormatDefinitionString;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.FormatDefinitionVordefiniert;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.FormatDefinitionZahl;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.logging.ValidationLogger;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.CollectionWithCheapMergeCopy;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DatumBereich;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DreiWertBool;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.EbenenIterator;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.EntityIterator;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IterationState;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.KontextIterator;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.LongArray;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.MetaDataUtilValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtIdentifierTemplate;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.SemanticIndexSpec;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.SetOperation;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ValidType;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ValidierungArt;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.VkBigDecimal;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.VkDate;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.VkDateRange;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.VkString;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.VkValue;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateAndFormat;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateUtil;
import com.mgmtp.a12.utils.conversion.a12internal.util.DateUtil.DateInterval;
import com.mgmtp.a12.utils.conversion.a12internal.util.PartiallyKnownDatesUtil;
import com.mgmtp.a12.utils.conversion.a12internal.util.ValidationDateParser;

import lombok.NonNull;

/**
 * Die Klasse <code>RuntimeController</code> ist die zentrale Klasse zum
 * Laufzeit-Zugriff auf die Nutzdaten. Sie stellt die Basisoperationen der
 * Regelsprache zur Verfügung.
 */
public abstract class RuntimeController {

	protected static final ValidationLogger LOG = new ValidationLogger();

	protected final @NonNull IMetaDataValidierung metaData;
	/**
	 * von der Applikation übergebene Schnittstelle zum Einfügen
	 * eventuell gefundener Fehler oder Hinweise.
	 */
	protected final @NonNull IResult pruefErgebnis;
	/**
	 * von der Applikation übergebener Validierungsmodus.
	 * Hier werden Validierungsparameter übergeben.
	 */
	protected final @NonNull ValidationMode modus;
	/**
	 * Zur Laufzeitoptimierung werden die übergebenen Werte und
	 * verdichtete Werte zur Zugriffsoptimierung gecached.
	 */
	protected final @NonNull ValidationCache validationCache;
	/**
	 * Falls Teilvalidierung, wird zusätzlich gespeichert, ob immer alle
	 * Zeilen /Kontexte /USBs eines Feldes prüfungsrelevant, wenn das Feld
	 * prüfungsrelevant.
	 * Wird zur Laufzeitoptimierung bei Teilvalidierungen verwendet.
	 */
	protected final @NonNull ValidierungArt validierungArt = new ValidierungArt();

	/**
	 * Definiert den aktuellen Validierungszeitpunkt.
	 */
	protected final @NonNull ZonedDateTime validationTime;

	/**
	 * The time zone, date time and time values are interpreted in.
	 */
	protected final @NonNull TimeZone timeZone;

	/**
	 * Erzeugt ein Validator-Objekt mit den übergebenen Nutzdaten.
	 *
	 * @param ergebnisse
	 * 		Ergebnis-Objekt zu dem die gefundenen Fehler und Hinweise hinzugefügt
	 * 		werden.
	 * @param valCache
	 * 		Cache für die Nutzdaten und die Optimierungsstrukturen.
	 * @param modus
	 * 		Parameter für die Validierung
	 * @param metaData
	 * 		bietet Zugriff auf die Metadaten
	 */
	protected RuntimeController(
			@NonNull IResult ergebnisse,
			@NonNull ValidationCache valCache,
			@NonNull ValidationMode modus,
			@NonNull IMetaDataValidierung metaData
	) {

		this.pruefErgebnis = ergebnisse;
		this.validationCache = valCache;
		this.modus = modus;
		this.metaData = metaData;

		// init validation time & time zone
		this.timeZone = metaData.getTimeZone();
		Date heuteFuerTest = modus.currentDateForTest();
		if (heuteFuerTest != null) {
			this.validationTime = heuteFuerTest.toInstant().atZone(timeZone.toZoneId());
		} else {
			this.validationTime = ZonedDateTime.now(timeZone.toZoneId());
		}
	}

	/**
	 * Prüft, ob das Feld prüfungsrelevant ist.
	 * <p>
	 * Ein Feld ist <b>nicht</b> prüfungsrelevant
	 * <li>Fehler bei der formalen Prüfung</li>
	 * <li>
	 * Teilvalidierung: Feld ist nicht in der Menge der relevanten Felder</li>
	 *
	 * @param f
	 * 		Identifier, der geprüft wird
	 * @return true falls prüfungsrelevant, false sonst.
	 * 		<p>
	 * 		TestRoutine in MainValidatorTest.testIsPruefungsRelevant
	 */
	private boolean isPruefungsRelevant(RtInternalIdentifier f, boolean partialIsUnknown) {
		if (f == null) {
			return false;
		}

		return EntityUtil.isPruefungsRelevant(f, validationCache, partialIsUnknown && !metaData.hasUniqueEntities());
	}

	/**
	 * Following values will be returned:
	 * <ul>
	 * <li>{@literal identifier not relevant -> UNKNOWN}</li>
	 * <li>{@literal identifier relevant, but not contained in the data -> FALSE}</li>
	 * <li>{@literal identifier relevant and contained in the data -> TRUE}</li>
	 * </ul>
	 * For group identifier, the relevance depends on the parameter partialIsUnknown.
	 * If the parameter is set, the group is only determined as relevant, if the group is fully
	 * relevant, i.e. all fields of the given repetition are relevant.
	 * If the parameter is not set, the group is already determined as relevant, if the group is
	 * only partially relevant.
	 *
	 * @param id
	 * 		Identifier, to be checked for given data.
	 * @param partialIsUnknown
	 * 		for a group identifier, which is only partially relevant/unknown
	 * 		for the given
	 * 		repetition, this parameter specifies, whether this fact leads to
	 * 		the value
	 *        {@link DreiWertBool#UNKNOWN}.
	 * @return TRUE/FALSE/UNKNOWN je nachdem ob Wert angegeben/prüfungsrelevant
	 */
	public DreiWertBool isWertAngegeben(RtInternalIdentifier id, boolean partialIsUnknown) {
		if (!isPruefungsRelevant(id, partialIsUnknown)) {
			//Wenn das Feld nicht prüfungsrelevant ist,  wird UNKNOWN zurückgegeben.
			return DreiWertBool.UNKNOWN;
		}

		//Feldwert ist nun entweder angegeben oder nicht angegeben.
		return validationCache.isEntityFilled(id) ? DreiWertBool.TRUE : DreiWertBool.FALSE;
	}

	// TODO: Kommentar entsprechend der neuen Dummys aktualisieren.

	/**
	 * Prüft ob Formular fortlaufend nummeriert ist.
	 * Gibt TRUE_AF oder FALSE zurück
	 *
	 * @param formularName
	 * 		Name des zu prüfenden Formulars
	 * @param dummy2
	 * 		wird nur verwendet, damit alle Vordruckbedingungen gleiche Signatur
	 * 		haben {@literal ->} Vereinfachung der Templates
	 * @param u
	 * 		muss mit einem konkreten USB aufgerufen werden.
	 * @return TRUE_AF oder FALSE zurück
	 */
	public ValidierungsErgebnis vordruckNichtFortlaufend(String formularName, long dummy2, long u, IterationState dummy1,
			RtIdentifierTemplate dummy3) {
		assert metaData.hasUniqueEntities();
		if (validierungArt.isValidationPartWith3ValueLogic()) {
			// Bei einer Teilvalidierung nach 3-Wert-Logik wird immer false zurückgegeben
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}

		//Kein Formular mit USB u vorhanden: kann für den konkreten USB u nicht wahr werden
		SortedSet<Long> lfdNummern = validationCache.getAllLfdNrnMitAngegebenenFeldern(formularName, u);
		//Die maximale lfdNummer entspricht der Anzahl der lfdNummern (d.h. das Formular ist
		//durchgängig nummeriert) oder kein Formular vorhanden
		if (lfdNummern.isEmpty() || lfdNummern.last() == lfdNummern.size()) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}

		return ValidierungsErgebnis.TRUE_AF;
	}

	// TODO: Kommentar entsprechend der neuen Dummys aktualisieren.

	/**
	 * Prüft ob USB des Vordrucks fortlaufend nummeriert ist.
	 * Gibt TRUE_AF oder FALSE zurück
	 *
	 * @param formularName
	 * 		Name des zu prüfenden Formulars
	 * @param dummy2
	 * 		wird nur verwendet, damit alle Vordruckbedingungen gleiche Signatur
	 * 		haben {@literal ->} Vereinfachung der Templates
	 * @param dummy3
	 * 		wird nur verwendet, damit alle Vordruckbedingungen gleiche Signatur
	 * 		haben {@literal ->} Vereinfachung der Templates
	 * @return TRUE_AF oder FALSE zurück
	 */
	public ValidierungsErgebnis usbNichtFortlaufend(String formularName, long dummy2, long dummy3, IterationState dummy1,
			RtIdentifierTemplate dummy4) {
		assert metaData.hasUniqueEntities();
		if (validierungArt.isValidationPartWith3ValueLogic()) {
			// Bei einer Teilvalidierung nach 3-Wert-Logik wird immer false zurückgegeben
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}

		// angegebene interne USBs in XML-USBs umwandeln
		TreeSet<Long> xmlUsbs = new TreeSet<>();
		Map<Long, Long> internZuXML = validationCache.getUsbCache().getInternZuXML();
		for (long usb : validationCache.getAllUsbsMitAngegebenenFeldern(formularName)) {
			Long xmlUsb = internZuXML.get(usb);
			if (xmlUsb != null) {
				xmlUsbs.add(xmlUsb);
			}
		}
		//Die höchste angegebene USB-Wiederholung entspricht der Anzahl der angegebenen USBs (d.h. das Formular ist
		//durchgängig nummeriert) oder kein Formular vorhanden
		if (xmlUsbs.isEmpty() || xmlUsbs.last() == xmlUsbs.size()) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		return ValidierungsErgebnis.TRUE_AF;
	}

	/**
	 * Überprüft ob ein Formular mit Nummer l und USB u vorhanden ist.
	 * Es wird TRUE_WF oder FALSE zurückgegeben
	 *
	 * @param vordruckName
	 * 		Name des Vordrucks
	 * @param l
	 * 		Nummer des Vordrucks oder 0 für irgendeine Vordrucknummer
	 * @param u
	 * 		USB des Vordrucks oder 0 für irgendein Vordruck-USB
	 * 		Für Vordrucke, deren maxUsb nicht gesetzt (= null) ist, wird die
	 * 		Methode mit
	 * 		u = 1 aufgerufen
	 */
	public ValidierungsErgebnis mindestensEinVordruckAngegeben(String vordruckName, long l, long u) {
		return mindestensEinVordruckAngegeben(vordruckName, l, u, null, null);
	}

	public ValidierungsErgebnis mindestensEinVordruckAngegeben(String vordruckName, long l, long u, IterationState indices,
			RtIdentifierTemplate semIdxEntity) {
		if (metaData.hasUniqueEntities()) {
			return mindestensEinVordruckAngegebenHDV(vordruckName, l, u, indices, semIdxEntity);
		} else {
			assert u == 0 || u == -2;
			return mindestensEinVordruckAngegebenA12DV(vordruckName, l, indices, semIdxEntity);
		}
	}

	private ValidierungsErgebnis mindestensEinVordruckAngegebenA12DV(String vordruckName, long l, IterationState indices,
			RtIdentifierTemplate semIdxEntity) {
		assert !metaData.hasUniqueEntities();
		String groupPath = EntityUtil.getRootGroupPath(vordruckName);
		//kein (prüfungsrelevanter) Eintrag vorhanden -> kein Fehler
		if (validationCache.numberOfFilledGroupInstances(groupPath) == 0) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}

		if (l == IIdentifier.ALL_INDICES) {
			// Es muss nur irgendein Eintrag vorhanden sein -> Fehler
			return ValidierungsErgebnis.TRUE_WF;
		}

		long specificLfdNr = deriveSpecificLfdNr(vordruckName, l, indices, semIdxEntity);

		RtInternalIdentifier id = new RtInternalIdentifier(groupPath, new long[] { specificLfdNr });
		return validationCache.isGroupFilled(id)
				? ValidierungsErgebnis.TRUE_WF
				: ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	private ValidierungsErgebnis mindestensEinVordruckAngegebenHDV(String vordruckName, long l, long u, IterationState indices,
			RtIdentifierTemplate semIdxEntity) {
		assert metaData.hasUniqueEntities();
		Set<Long> usbsMitAngegebenenFeldern = validationCache.getAllUsbsMitAngegebenenFeldern(vordruckName);
		//kein (prüfungsrelevanter) Eintrag vorhanden -> kein Fehler
		if (usbsMitAngegebenenFeldern.isEmpty()) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}

		// Es muss nur irgendein Eintrag vorhanden sein -> Fehler
		if (l == IIdentifier.ALL_INDICES) {
			if (u == IIdentifier.ALL_INDICES) {
				return ValidierungsErgebnis.TRUE_WF;
			}
			//Eintrag für konkreten Usb vorhanden ?
			return usbsMitAngegebenenFeldern.contains(u) ? ValidierungsErgebnis.TRUE_WF : ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		} else {
			// Gibt es irgeneinen Usb, für den der spezifische Wert l eingetragen wurde ?
			if (u == IIdentifier.ALL_INDICES) {
				for (Long usb : usbsMitAngegebenenFeldern) {
					Set<Long> lfdNrnMitAngegebenenFeldern = validationCache.getAllLfdNrnMitAngegebenenFeldern(vordruckName, usb);
					if (lfdNrnMitAngegebenenFeldern.contains(l)) {
						return ValidierungsErgebnis.TRUE_WF;
					}
				}
				return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
			}

			//sowohl l als auch u sind also konkret
			Set<Long> laufendeNummern = validationCache.getAllLfdNrnMitAngegebenenFeldern(vordruckName, u);
			long lToLookup = deriveSpecificLfdNr(vordruckName, l, indices, semIdxEntity);
			return laufendeNummern.contains(lToLookup) ? ValidierungsErgebnis.TRUE_WF : ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
	}

	/**
	 * Only used in mindestensEinVordruckAngegeben.
	 */
	private long deriveSpecificLfdNr(String vordruckName, long lfdNr, IterationState indices, RtIdentifierTemplate semIdxEntity) {
		if (lfdNr != IIdentifier.PARALLEL_ITERATION && lfdNr != IIdentifier.SEMANTIC_INDEX) {
			// If we don't have parallel iteration or a semantic index for the root group repetition, we return the lfdNr
			// Iteration (-1) is already handled in the given parameter lfdNr.
			return lfdNr;
		}
		String indexValue;
		if (lfdNr == IIdentifier.PARALLEL_ITERATION) {
			if (indices == null) {
				return lfdNr;
			}
			indexValue = indices.getValue();
		} else {
			if (semIdxEntity == null) {
				return lfdNr;
			}
			SemanticIndexSpec semIdxSpec = semIdxEntity.getSemanticIndex().createSemanticIndex(indices);
			if (semIdxSpec == null) {
				return lfdNr;
			}
			indexValue = validationCache.getSemanticIndexValue(semIdxSpec);
		}
		return validationCache.getSpecificIndexForSemanticIndex(vordruckName, indexValue);
	}

	/**
	 * keinVordruckAngegeben überprüft, dass kein Formular mit laufender
	 * Nummer l und USB u vorhanden ist. <br>
	 * Es wird TRUE_AF oder FALSE zurückgegeben. <br>
	 * Soll geprüft werden ob überhaupt kein Formular mit dem angegebenen Namen vorhanden ist,
	 * so wird für l und u eine 0 eingegeben.
	 * Wird die Methode im Rahmen einer Teilvalidierung aufgerufen, so wird immer FALSE
	 * zurückgegeben.
	 */
	public ValidierungsErgebnis keinVordruckAngegeben(String formularName, long l, long u, RtIdentifierTemplate entity) {
		return keinVordruckAngegeben(formularName, l, u, null, entity);
	}

	public ValidierungsErgebnis keinVordruckAngegeben(String formularName, long l, long u, IterationState iterationState,
			RtIdentifierTemplate entity) {
		assert (this.metaData.hasUniqueEntities()
				? (entity == null) // for H-DVsm u can be -2 or some other value
				: (entity != null && u == -2));
		if (validierungArt.isValidationPartWith3ValueLogic()) {
			// Bei einer Teilvalidierung nach 3-Wert-Logik wird immer false zurückgegeben
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		if (mindestensEinVordruckAngegeben(formularName, l, u, iterationState, entity).isKnownAndTrue()
				// Note: if no field is given, but one is non-relevant, we will return FALSE too.
				|| !isPruefungsRelevant(new RtInternalIdentifier(
						"/" + formularName,
						new long[] { deriveSpecificLfdNr(formularName, l, iterationState, entity) },
						u),
				true)
		) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		return ValidierungsErgebnis.TRUE_AF;
	}

	/**
	 * Überprüft, ob alle Felder der FeldListe angegeben sind.
	 * Es wird nur TRUE_WF oder FALSE zurückgegeben.
	 * Es wird über die feldListe iteriert: Sobald ein Feld unknown oder
	 * nicht angegeben ist, wird FALSE zurückgegeben.
	 * Es wird nicht optimiert, da SternOperation für USB verboten und
	 * davon ausgegangen wird, dass diese Methode nicht für Sternoperationen
	 * mit großen Wiederholbarkeiten verwendet wird.
	 */
	public ValidierungsErgebnis alleFelderAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		return alleEntitiesAngegeben(index, entityList);
	}

	private ValidierungsErgebnis alleEntitiesAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		boolean containsFilter = false;
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			if (rtIdContainer.isAggregation()) {
				if (rtIdContainer.getFilter() != null) {
					containsFilter = true;
				}
				EntityIterator fIt = new EntityIterator(rtIdContainer, index);
				for (RtInternalIdentifier id : fIt) {//Prüfe für jeden Identifier, ob er angegeben ist
					if (isWertAngegeben(id, false) != DreiWertBool.TRUE) {
						return ValidierungsErgebnis.FALSE_OR_UNKNOWN; // einen nicht angegebenen Wert gefunden -> gib false zurück
					}
				}
			} else {
				RtInternalIdentifier rtId = RtInternalIdentifier.createFromTemplate(rtIdContainer, index);
				if (isWertAngegeben(rtId, false) != DreiWertBool.TRUE) {
					return ValidierungsErgebnis.FALSE_OR_UNKNOWN; // einen nicht angegebenen Wert gefunden -> gib false zurück
				}
			}
		}
		if (containsFilter) {
			return ValidierungsErgebnis.TRUE_AF;
		} else {
			return ValidierungsErgebnis.TRUE_WF;
		}
	}

	/**
	 * Überprüft, ob kein Feld aus der FeldListe angegeben ist.
	 * Es wird nur TRUE_AF oder FALSE zurückgegeben.
	 * Es wird über die ganze feldListe iteriert: Sobald ein Feld unknown oder
	 * angegeben ist, wird FALSE zurückgegeben.
	 */
	public ValidierungsErgebnis keinFeldAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		return keineEntityAngegeben(index, entityList);
	}

	private ValidierungsErgebnis keineEntityAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			if (rtIdContainer.isAggregation()) {
				EntityIterator fIt = new EntityIterator(rtIdContainer, index);
				if (internMindestensEinFeldAngegebenOderNichtPruefungsrelevant(fIt)) {
					return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
				}
			} else {
				RtInternalIdentifier rtId = RtInternalIdentifier.createFromTemplate(rtIdContainer, index);
				if (isWertAngegeben(rtId, true) != DreiWertBool.FALSE) {
					return ValidierungsErgebnis.FALSE_OR_UNKNOWN; // field filled or not relevant
				}
			}
		}
		return ValidierungsErgebnis.TRUE_AF;
	}

	private boolean internMindestensEinFeldAngegebenOderNichtPruefungsrelevant(EntityIterator fIt) {

		setzeEndIndicesAufMaxGesetzt(fIt, true);

		for (RtInternalIdentifier id : fIt) {
			if (isWertAngegeben(id, true) != DreiWertBool.FALSE) { // If one field is not relevant, we want to return true
				return true; // Feld angegeben oder nicht prüfungsrelevant
			}
		}
		return false;
	}

	/**
	 * Überprüft, ob mindestens ein Feld angegeben ist.
	 * Es wird über die ganze feldListe iteriert: Sobald ein prüfungsrelevantes Feld angegeben
	 * ist, wird TRUE_WF zurückgegeben, wenn das Feld keine Filterbedingung hat, TRUE_AF sonst.
	 */
	public ValidierungsErgebnis mindestensEinFeldAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		return mindestensEineEntityAngegeben(index, entityList);
	}

	/**
	 * Wenn mit nur einer entity oder einer *-Referenz aufgerufen:
	 * <pre>{@code
	 * ∃ G e G*. ∃ f e G. angegegeben(f) und relevant(f)
	 * }</pre>
	 */
	protected ValidierungsErgebnis mindestensEineEntityAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		// sobald ein Feld angegeben ist, gebe false zurück
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			if (rtIdContainer.isAggregation()) {
				EntityIterator fIt = new EntityIterator(rtIdContainer, index);
				if (internMindestensEinFeldAngegeben(fIt)) {
					return rtIdContainer.getFilter() != null ? ValidierungsErgebnis.TRUE_AF : ValidierungsErgebnis.TRUE_WF;
				}
			} else {
				RtInternalIdentifier rtId = RuntimeController.makeIdentifier(index, rtIdContainer);
				if (isWertAngegeben(rtId, false) == DreiWertBool.TRUE) {
					return ValidierungsErgebnis.TRUE_WF;
				}
			}
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	private boolean internMindestensEinFeldAngegeben(EntityIterator fIt) {
		//Optimierung auch wenn Teilvalidierung für konkreten USB / lfdNummer möglich
		//da nicht prüfungsrelevante Felder genauso behandelt werden wie nicht angegebene
		//Felder
		setzeEndIndicesAufMaxGesetzt(fIt, false);
		fIt.optimiereMitGesetztenWiederholungenWennSinnvoll(validationCache);

		for (RtInternalIdentifier id : fIt) {
			if (isWertAngegeben(id, false) == DreiWertBool.TRUE) { // one field relevant suffices for TRUE
				return true;
			}
		}
		return false;
	}

	/**
	 * Sobald ein prüfungsrelevantes Feld nicht angegeben ist, wird TRUE_AF zurückgegeben.
	 * Darf für USB keinen * spezifizieren.
	 * Es wird nicht optimiert, da SternOperation für USB verboten und
	 * davon ausgegangen wird, dass diese Methode nicht für Sternoperationen
	 * mit großen Wiederholbarkeiten verwendet wird.
	 */
	public ValidierungsErgebnis nichtAlleFelderOderKeinFeldAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		return nichtAlleOderKeineEntityAngegeben(index, entityList);
	}

	/**
	 * Wenn mit nur einer entity oder einer *-Referenz G* aufgerufen:
	 * <pre>{@code
	 * # wenn partialIsUnknown == false:
	 * ∃ G e G*. V f e G. !angegeben(f) or !relevant(f)  (?)
	 * # wenn partialIsUnknown == true:
	 * ∃ G e G*. V f e G. !angegeben(f) and relevant(f)  (?)
	 * }</pre>
	 */
	protected ValidierungsErgebnis nichtAlleOderKeineEntityAngegeben(
			IterationState index,
			List<RtIdentifierTemplate> entityList
	) {
		return nichtAlleOderKeineEntityAngegeben(index, entityList, false);
	}

	/**
	 * like {@link #nichtAlleOderKeineEntityAngegeben}, but with an additional flag (only relevant for partial validation)
	 *
	 * @param limitEffortForAggregationInPartialValidation
	 * 		if true, limit the maximum number of inspected non-relevant identifier
	 * 		repetitions and <em>guess</em> a result of
	 *        {@link ValidierungsErgebnis#TRUE_AF} if still undecided after 1000
	 * 		non-relevant repetitions (per identifier).
	 * 		<em>NOTE</em> {@code TRUE_AF} is only a suitable default for filtering of
	 * 		referenced omission entities.
	 * 		This flag <em>must not be used during condition evaluation itself</em>.
	 */
	protected ValidierungsErgebnis nichtAlleOderKeineEntityAngegeben(
			IterationState index,
			List<RtIdentifierTemplate> entityList,
			boolean limitEffortForAggregationInPartialValidation
	) {
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			if (rtIdContainer.isAggregation()) {
				EntityIterator fIt = new EntityIterator(rtIdContainer, index);
				long numCheckedNonRelevantRepetitions = 0;
				for (RtInternalIdentifier id : fIt) {
					DreiWertBool wertAngegeben = isWertAngegeben(id, true);
					if (wertAngegeben == DreiWertBool.FALSE) {
						return ValidierungsErgebnis.TRUE_AF; // Note: when called with partialIsUnknown==true, that only applies to
						// partially-relevant groups (within the same repetition) - but if some repetitions are non-relevant and
						// some are relevant, this will *not* return UNKNOWN.
					}
					if (wertAngegeben == DreiWertBool.UNKNOWN) {
						numCheckedNonRelevantRepetitions += 1;
					}
					if (limitEffortForAggregationInPartialValidation
							&& !this.validierungArt.isVollvalidierung()
							&& numCheckedNonRelevantRepetitions >= 1000 // 1000 is picked rather arbitrarily, to be too large to matter in
						// normal practical cases where a relevant higher repetition actually exists, but still small enough to
						// not prevent the prohibitively expensive cases in practice.
					) {
						return ValidierungsErgebnis.TRUE_AF;
					}
				}
			} else {
				RtInternalIdentifier rtId = RtInternalIdentifier.createFromTemplate(rtIdContainer, index);
				if (isWertAngegeben(rtId, true) == DreiWertBool.FALSE) {
					return ValidierungsErgebnis.TRUE_AF;
				}
			}
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	/**
	 * Sobald zwei prüfungsrelevante Felder angegeben sind, wird TRUE_AF zurückgegeben, wenn ein
	 * Feld mit Filterbedingung gefunden wurde, TRUE_WF sonst.
	 */
	public ValidierungsErgebnis mehrAlsEinFeldAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		// sobald mehr als ein Feld gefunden wurde, gebe TRUE_WF zurück
		int anzGefFelder = 0;
		boolean containsFilter = false;

		for (RtIdentifierTemplate rtIdContainer : entityList) {
			if (rtIdContainer.isAggregation()) {
				EntityIterator fIt = new EntityIterator(rtIdContainer, index);
				// es reicht, über die tatsächlich vorhandenen Felder zu iterieren, da
				// prüfungsrelevante UND angegebene Werte gezählt werden müssen
				setzeEndIndicesAufMaxGesetzt(fIt, false);

				for (RtInternalIdentifier id : fIt) {
					if (isWertAngegeben(id, false) == DreiWertBool.TRUE) {
						if (rtIdContainer.getFilter() != null) {
							containsFilter = true;
						}
						anzGefFelder++;
						if (anzGefFelder > 1) {
							return containsFilter ? ValidierungsErgebnis.TRUE_AF : ValidierungsErgebnis.TRUE_WF;
						}
					}
				}
			} else {
				RtInternalIdentifier rtId = RtInternalIdentifier.createFromTemplate(rtIdContainer, index);
				if (isWertAngegeben(rtId, false) == DreiWertBool.TRUE) {
					anzGefFelder++;
					if (anzGefFelder > 1) {
						return containsFilter ? ValidierungsErgebnis.TRUE_AF : ValidierungsErgebnis.TRUE_WF;
					}
				}
			}
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	/**
	 * Checks whether all groups in the list are filled.
	 * If all groups are definitely filled {@link ValidierungsErgebnis#TRUE_WF} is returned.
	 * If one group is not filled, or one group is irrelevant, {@link ValidierungsErgebnis#FALSE_OR_UNKNOWN} is
	 * returned.
	 */
	public ValidierungsErgebnis alleKontexteAngegeben(IterationState index, List<RtIdentifierTemplate> groupList) {
		return alleEntitiesAngegeben(index, groupList);
	}

	/**
	 * Checks whether no group in the list is filled.
	 * If all groups are definitely not filled {@link ValidierungsErgebnis#TRUE_AF} is returned.
	 * If one group is not filled or not fully relevant, {@link ValidierungsErgebnis#FALSE_OR_UNKNOWN} is
	 * returned.
	 */
	public ValidierungsErgebnis keinKontextAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		return keineEntityAngegeben(index, entityList);
	}

	/**
	 * Checks if at least one group in the list is filled.
	 * If at least one group is definitely filled, {@link ValidierungsErgebnis#TRUE_WF} is returned.
	 * If no group is relevant or no relevant group is filled, {@link ValidierungsErgebnis#FALSE_OR_UNKNOWN} is
	 * returned.
	 */
	public ValidierungsErgebnis mindestensEinKontextAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		return mindestensEineEntityAngegeben(index, entityList);
	}

	/**
	 * If there is one group that is fully relevant and is not filled
	 * {@link ValidierungsErgebnis#TRUE_AF} is returned.
	 * If all groups are either not fully relevant or filled, {@link ValidierungsErgebnis#FALSE_OR_UNKNOWN} is
	 * returned.
	 * Groups, which are not fully relevant, are ignored.
	 */
	public ValidierungsErgebnis nichtAlleOderKeinKontextAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		return nichtAlleOderKeineEntityAngegeben(index, entityList);
	}

	/**
	 * Implementiert das Sprachkonstrukt ZeileNichtEindeutig. Wird behandelt wie
	 * WiederholungNichtEindeutig, siehe {@link #wiederholungNichtEindeutig(IterationState, List, int[],
	 * RepetitionNotUniqueErrorCache, int)}
	 */
	public ValidierungsErgebnis zeileNichtEindeutig(IterationState index, List<RtIdentifierTemplate> entityList,
			int[] searchedDuplicateLayers, RepetitionNotUniqueErrorCache repNonUnqCache, int semanticIndexLevel) {
		return wiederholungNichtEindeutig(index, entityList, searchedDuplicateLayers, repNonUnqCache, semanticIndexLevel);
	}

	/**
	 * Implementiert die Sprachkonstrukte ZeileNichtEindeutig und WiederholungNichtEindeutig.
	 * <p>
	 * Prüft ob die Wertkombination dieser Wiederholung gleich ist der Wertkombination einer anderen
	 * Wiederholung.<br>
	 * Im Fehlerfall wird TRUE_AF zurückgegeben, wenn für ein Feld in der Feldliste kein Wert
	 * angegeben wurde, sonst TRUE_WF.
	 * </p>
	 * Durch den Parser wird zugesichert:
	 * <ul>
	 * <li>keines der Felder in der Feldliste spezifiziert einen Stern-Operator.</li>
	 * <li>alle Felder liegen auf einem Pfad</li>
	 * <li>Felder dürfen entweder Zahlen (oder Geldbeträge) sein oder Strings. Andere Feldtypen sind
	 * nicht in der Feldliste erlaubt.</li>
	 * </ul>
	 * <p>
	 * ACHTUNG: diese Implementierung verläßt sich darauf, daß die next()-Methode des
	 * EbenenIterators von "rechts nach links" weiterschaltet.
	 * </p>
	 *
	 * @param index
	 * 		die aktuelle Wiederholung, für die die Regel
	 * 		ausgewertet wird
	 * @param entityList
	 * 		Feldliste
	 * @param searchedDuplicateLayers
	 * 		die Ebenen, auf denen Duplikate gesucht werden. Entspricht
	 * 		der Menge der
	 * 		wiederholbaren Ebenen unterhalb der Ebene des
	 * 		Regelkontextes+1 bzw. der Ebene des
	 * 		Regelkontextes, falls Duplikate für Vordrucke gesucht
	 * 		werden.
	 * @param repNotUnqCache
	 * 		cache to collect the erroneous repetitions
	 */
	public ValidierungsErgebnis wiederholungNichtEindeutig(IterationState index, List<RtIdentifierTemplate> entityList,
			int[] searchedDuplicateLayers, RepetitionNotUniqueErrorCache repNotUnqCache, int semanticIndexLevel) {
		// Do only one slicing operation and use the copy multiple times
		long[] indexes = index.getIndexes();

		/*
		 * Es kann sein, daß das Sprachkonstrukt für die erste Wiederholung nicht aufgerufen wurde,
		 * z.B. weil eine andere Teilbedingung für diese falsch war. Daher wird die Variable
		 * 'initialisiert' verwendet.
		 */
		if (!repNotUnqCache.isInitialised()) {
			EbenenIterator iter = createEbenenIteratorAbPosition(indexes, searchedDuplicateLayers, semanticIndexLevel);
			ermittleWiederholungenMitDuplikaten(entityList, iter, repNotUnqCache);
			repNotUnqCache.setInitialised();
		}

		/*
		 * die Wiederholungen mit nicht eindeutigen Wertekonstellationen sind aufgesammelt; nun
		 * können wir das Ergebnis für die aktuelle Wiederholung zurückgeben
		 */
		LongArray aktWiederholung = new LongArray(indexes);
		if (repNotUnqCache.hasValueError(aktWiederholung)) {
			return ValidierungsErgebnis.TRUE_WF;
		}
		if (repNotUnqCache.hasOmissionError(aktWiederholung)) {
			return ValidierungsErgebnis.TRUE_AF;
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	/**
	 * Erzeugt einen EbenenIterator, der eine Iteration über alle Wiederholungen ermöglicht, die für
	 * die Ebenen bis zur übergebenen Position der Wiederholung aus dem übergebenen Index-Array
	 * entsprechen, ab da für alle Wiederholungen, für die Werte angegeben sind.<br>
	 * <p>
	 * ACHTUNG: Die Methode kann aktuell nicht in einer Filterbedingung mit Regeliterationsspezifikation verwendet werden, da der
	 * erzeugte {@link EbenenIterator} dafür nicht geeignet ist.<br>
	 *
	 * <b>Beispiel:</b><br>
	 * index = [-2,3,5,1,6]<br>
	 * searchedDuplicateLayers = [2,4]<br>
	 * Dann hat der erzeugte EbenenIterator als startIndices=[-2,3,1,1,1], endeIndices=[-2,3,7,1,8],
	 * wobei 7 und 8 die max. gesetzten Werte für die jeweiligen Kontextebenen sind.
	 */
	private EbenenIterator createEbenenIteratorAbPosition(long[] index, int[] searchedDuplicateLayers, int semanticIndexLevel) {
		long[] startIndices = index.clone();
		long[] endeIndices = index.clone();
		for (int layer : searchedDuplicateLayers) {
			startIndices[layer] = 1;
			// use layer - 1 because 'index' contains the lfdnr at the 2. position (usb is at the 1. pos.)
			// whereas in validationCache.getMaxGesetzterKontext lfdnr is at the 1. position
			endeIndices[layer] = validationCache.getMaxGesetzterKontext(layer - 1);
		}
		int zeile = startIndices.length - 1;
		// Für ZeileNichtEindeutig ist das immer korrekt, weil hier alle Felder Zeileniteration spezifizieren
		// Für WiederholungNichtEindeutig ist das nie relevant, weil es in A12-Modellen keine Zeilenwiederholung gibt.
		startIndices[zeile] = 1;
		endeIndices[zeile] = getMaxGesetzteZeilen();

		return new EbenenIterator(startIndices, endeIndices, validationCache.getGesetzteWiederholungen(), semanticIndexLevel);
	}

	/**
	 * Sammelt für alle Wiederholungen, die durch den übergebenen Iterator gegeben sind, die
	 * Wertekonstellation der Felder. Kommt die gleiche Wertekonstellation bereits für eine andere
	 * Wiederholung vor, werden beide Wiederholungen (d.h. die entsprechenden long-Arrays) in die
	 * passende
	 * Menge eingefügt: <br>
	 * In die Menge mit den Wertefehlern, wenn für die Wiederholung alle Felder angegeben sind und
	 * in die Menge mit den Auslassungsfehlern, andernfalls.
	 */
	private void ermittleWiederholungenMitDuplikaten(List<RtIdentifierTemplate> entityList, EbenenIterator iter,
			RepetitionNotUniqueErrorCache repNotUnqCache) {
		Map<LongArray, List<Object>> werteProWiederholung = new HashMap<>();
		for (IterationState aktW : iter) {
			List<Object> aktuelleWerte = getWerteOrNullIfEinFeldNichtPruefungsRelevant(aktW, entityList);
			if (aktuelleWerte == null) {
				continue;
			}
			LongArray aktuelleWiederholung = new LongArray(aktW.getIndexes());
			//suche aktuelle Wertekonstellation in den bereits gesammelten
			for (Entry<LongArray, List<Object>> entry : werteProWiederholung.entrySet()) {
				LongArray w = entry.getKey();
				List<Object> andereWerte = entry.getValue();
				ValidierungsErgebnis result = werteKonstellationenNichtEindeutig(aktuelleWerte, andereWerte);
				if (result == ValidierungsErgebnis.TRUE_AF) {
					repNotUnqCache.addErroneousRepetitionWithOmissionsError(w, aktuelleWiederholung);
				}
				if (result == ValidierungsErgebnis.TRUE_WF) {
					repNotUnqCache.addErroneousRepetitionWithValueError(w, aktuelleWiederholung);
				}
			}
			werteProWiederholung.put(aktuelleWiederholung, aktuelleWerte);
		}
	}

	/**
	 * Hilfsmethode für wiederholungNichtEindeutig. <br>
	 * Für beide Wertekonstellation wird überprüft, ob sie in jedem Wert gleich sind.<br>
	 * Es wird über die Wertelisten iteriert. Sobald an einer Stelle der Liste für beide
	 * Wertekonstellationen ein Wert angegeben ist, der nicht gleich ist, kann FALSE zurückgegeben
	 * werden.<br>
	 * Ist für einen Vergleichswert ein Wert <code>null</code> und der andere angegeben, wird
	 * ebenfalls FALSE zurückgegeben.<br>
	 * Sind die Wertekosntallationen gleich, wird TRUE_AF zurückgegeben, wenn ein <code>null</code>
	 * Wert gefunden, sonst TRUE_WF.
	 */
	private ValidierungsErgebnis werteKonstellationenNichtEindeutig(List<Object> werte1, List<Object> werte2) {
		if (werte1 == null || werte2 == null) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		int anzNullGefunden = 0;
		for (int i = 0; i < werte1.size(); i++) {
			Object wert1 = werte1.get(i);
			Object wert2 = werte2.get(i);
			if (wert1 == null && wert2 == null) {
				anzNullGefunden++;
				continue;
			}
			if (wert1 == null || wert2 == null) {
				//einer der Werte nicht null wg. vorheriger Prüfung -> Werte ungleich
				return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
			}

			if (!Objects.equals(wert1, wert2)) {
				return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
			}

		}
		//Falls kein Feld für beide Wiederholungen angegeben wurde, gelten die Werte nicht als gleich.
		if (anzNullGefunden == werte1.size()) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		if (anzNullGefunden > 0) {
			return ValidierungsErgebnis.TRUE_AF;
		}
		return ValidierungsErgebnis.TRUE_WF;
	}

	/**
	 * Hilfsmethode für wiederholungNichtEindeutig. <br>
	 * <b>Vorbedingung:</b> nur Zahlen (oder Geldbeträge) oder Strings als Feldtypen erlaubt.
	 * <p>
	 * Schreibt die Werte der Wiederholung, die durch das Index-Array spezifiziert wurde, in die
	 * Rückgabeliste. <br>
	 * Wenn ein Wert nicht prüfungsrelevant ist oder alle Felder nicht angegeben, wird
	 * <code>null</code> zurückgegeben.<br>
	 * Wenn ein Wert nicht angegeben ist, andere Werte aber schon, wird <code>null</code> in die
	 * Rückgabeliste geschrieben.<br>
	 * Jeder angegebene Wert wird als VkBigDecimal oder als String in die Rückgabeliste
	 * geschrieben, so dass dann die richtige equals-Methode aufgerufen wird.
	 * </p>
	 */
	private List<Object> getWerteOrNullIfEinFeldNichtPruefungsRelevant(IterationState index,
			List<RtIdentifierTemplate> entityList) {
		List<Object> werteFuerWiederholung = new ArrayList<>();
		boolean angegebenesFeldGefunden = false;
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			EntityIterator fIt = new EntityIterator(rtIdContainer, index);
			RtInternalIdentifier id = fIt.get();

			DreiWertBool b = isWertAngegeben(id, false);
			switch (b) {
				case UNKNOWN:
					return null;
				case FALSE:
					werteFuerWiederholung.add(null);
					break;
				case TRUE:
					angegebenesFeldGefunden = true;
					if (MetaDataUtilValidierung.isNumberFeld(id.getName(), metaData)) {
						VkBigDecimal v = feldWertAlsZahl(id);
						werteFuerWiederholung.add(v);
					} else {
						werteFuerWiederholung.add(feldWertIntern(id));
					}
			}
		}

		//ein Wert ist angegeben => gib das Ergebnis zurück
		if (angegebenesFeldGefunden) {
			return werteFuerWiederholung;
		}
		//kein Feld angegeben
		return null;
	}

	/**
	 * nichtGenauEinFeldAngegeben gibt
	 * <ul>
	 * <li>TRUE_AF zurück, falls alle Felder aus entityList prüfungsrelevant sind, aber keins
	 * angegeben wurde oder wenn mehr als ein Feld angegeben wurde und eines von diesen eine
	 * Filterbedingung hat</li>
	 * <li>TRUE_WF zurück, falls mehr als ein prüfungsrelevantes Feld ohne Filterbedingung angegeben
	 * wurde</li>
	 * <li>FALSE sonst</li>
	 * </ul>
	 */
	public ValidierungsErgebnis nichtGenauEinFeldAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		if (keinFeldAngegeben(index, entityList).isKnownAndTrue()) {
			return ValidierungsErgebnis.TRUE_AF;
		}
		return mehrAlsEinFeldAngegeben(index, entityList);
	}

	/**
	 * Gibt TRUE_AF zurück, wenn die Zeile der Felder aus der FeldListe nicht
	 * aufsteigend sind <br>
	 * (1) die aktuelle Zeile ist die erste Zeile {@literal ->} FALSE zurückgeben <br>
	 * (2) Kein Wert für aktuelle Zeile vorhanden {@literal ->} FALSE zurückgeben <br>
	 * (3) Vorgängerzeile existiert nicht {@literal ->} TRUE_AF zurückgeben
	 */
	public ValidierungsErgebnis zeilenNichtAufsteigend(IterationState index, List<RtIdentifierTemplate> entityList) {
		/*
		 * Sprachkonstrukt nur in H-DVs erlaubt, dort ist der Zeilenindex immer der
		 * dritte (übergebener index enthält an erster Stelle den USB!)
		 */
		long zeile = index.getIndex(2);
		if (zeile == 1) { // (1) erste Zeile kann gar nicht NichtAufsteigend sein
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}

		if (mindestensEinFeldAngegeben(index, entityList) == ValidierungsErgebnis.FALSE_OR_UNKNOWN) {
			//(2) es wurde diese Zeile gar nicht angegeben
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}

		long[] idxFuerVorgaengerZeile = { index.getIndex(0), index.getIndex(1), zeile - 1 };
		//prüfe ob Vorgänger Zeile vorhanden
		return wiederholungNichtVorhandenAberPruefungsrelevant(idxFuerVorgaengerZeile, entityList) ?
				ValidierungsErgebnis.TRUE_AF :
				ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	/**
	 * Hilfsmethode für zeilenNichtAufsteigend. <br>
	 * Durch den Parser wird zugesichert:
	 * <ul>
	 * <li>Modell mit eindeutigen Feldnamen</li>
	 * <li>Keine Ebenenspezifikation enthält einen Stern</li>
	 * <li>Alle laufendenNummer Spezifikationen sind gleich</li>
	 * <li>Zeilenspezifikation ist immer Iteration</li>
	 * </ul>
	 * Prüfe die Wiederholung:
	 * Wenn ein nicht pruefungsrelevantes oder angegebenes Feld gefunden wurde,
	 * gebe false zurück; true sonst.
	 */
	private boolean wiederholungNichtVorhandenAberPruefungsrelevant(long[] index, List<RtIdentifierTemplate> entityList) {
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			EntityIterator fIt = new EntityIterator(rtIdContainer,
					new IterationState(index, null)); // parallel iteration not allowed for zeilenNichtAufsteigend
			RtInternalIdentifier id = fIt.get(); // get-Operaton möglich, da Stern-Operation nicht erlaubt.
			if (isWertAngegeben(id, true) != DreiWertBool.FALSE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gibt TRUE_AF zurück, wenn die Zeile der Felder aus der FeldListe
	 * aufsteigend sind. TRUE_AF wird zurückgegeben, weil das Sprachkonstrukt
	 * nur in Verbindung mit MaximalAngegebenerZeilenIndex verwendet wird.
	 * D.h. im Falle, dass die Zeilen lueckenlos sind, besteht die Auslassung darin, dass
	 * nicht bis zum maximalen Zeilenindex Felder angegeben wurden. D.h. die Kategorisierung
	 * des Fehlers macht nur in der verwendeten Kombination Sinn. <br>
	 * Vorbedingung (durch den Parser garantiert):
	 * <ul>
	 * <li>Modell mit eindeutigen Feldnamen</li>
	 * <li>alle Felder habe die gleiche lfdNummer</li>
	 * <li>keine Ebenenspezifikation ausser der Zeile ist ein Stern</li>
	 * <li>Zeile spezifiziert einen Stern, die Felder sind Mehrzeiler</li>
	 * </ul>
	 */
	public ValidierungsErgebnis zeilenLueckenlos(IterationState index, List<RtIdentifierTemplate> entityList) {
		TreeSet<Long> zeileMitWert = new TreeSet<>();

		//Sammle alle Zeilen mit angegebenem Wert in der Menge 'zeileMitWert'
		//Falls ein nicht prüfungsrelevantes Feld gefunden wurde, gebe false zurück.
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			EntityIterator fIt = new EntityIterator(rtIdContainer, index);
			for (RtInternalIdentifier id : fIt) {
				DreiWertBool wertAngegeben = isWertAngegeben(id, false);
				//Cobertura:/Alle enumerationsmoeglichkeiten sind im Switch angegeben.
				switch (wertAngegeben) {
					case TRUE:
						//Sprachkonstrukt nur in H-DVs, daher ist der zweite Index die Zeile
						zeileMitWert.add(id.getIndices()[1]);
						break;
					case UNKNOWN:
						return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
					case FALSE:
				}
			}
		}

		//Überprüfe die Menge, ob alle Zeilen angegeben wurden.
		if (!zeileMitWert.isEmpty() && zeileMitWert.last().longValue() == zeileMitWert.size()) {
			return ValidierungsErgebnis.TRUE_AF;
		} else {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
	}

	/**
	 * Implements the operator <code>Ungueltig</code> for date constructions. Returns
	 * <ul>
	 * <li>{@link ValidierungsErgebnis#TRUE_WF}, if all fields used for the creation of date are
	 * relevant and filled and date is a not valid date</li>
	 * <li>{@link ValidierungsErgebnis#TRUE_AF}, if all fields used for the creation of date are
	 * relevant, but at least one them is not filled</li>
	 * <li>{@link ValidierungsErgebnis#FALSE_OR_UNKNOWN} otherwise</li>
	 * </ul>
	 *
	 * @param date
	 * 		the constructed date to be checked
	 * @return s. above
	 */
	public ValidierungsErgebnis dateUngueltig(VkDate date) {
		if (date.hasValue() || date.isNichtPruefungsrelevant()) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		return date.isNichtAngegeben() ? ValidierungsErgebnis.TRUE_AF : ValidierungsErgebnis.TRUE_WF;
	}

	/**
	 * Implements the operator <code>Gueltig</code> for date constructions. Returns
	 * <ul>
	 * <li>{@link ValidierungsErgebnis#TRUE_WF}, if all fields used for the creation of date are
	 * relevant and filled and date is a valid date</li>
	 * <li>{@link ValidierungsErgebnis#FALSE_OR_UNKNOWN} otherwise</li>
	 * </ul>
	 *
	 * @param date
	 * 		the constructed date to be checked
	 * @return s. above
	 */
	public ValidierungsErgebnis dateGueltig(VkDate date) {
		if (!date.hasValue() || date.isNichtPruefungsrelevant()) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		return date.isNichtAngegeben() ? ValidierungsErgebnis.TRUE_AF : ValidierungsErgebnis.TRUE_WF;
	}

	/**
	 * Implements the operator <code>Gueltig</code> for predefined types. Returns
	 * <ul>
	 * <li>{@link ValidierungsErgebnis#TRUE_WF}, if the field is relevant, filled and valid relating
	 * to the predefined type. <br>
	 * <b>Note:</b> For predefined types that define a display value different to the XML value, the
	 * check is carried out without any conversion i.e. it is checked if the value is a valid XML
	 * value for the predefined type.</li>
	 * <li>{@link ValidierungsErgebnis#FALSE_OR_UNKNOWN} otherwise</li>
	 * </ul>
	 */
	public ValidierungsErgebnis predefinedTypeGueltig(IterationState idx, RtIdentifierTemplate rtIdCon1, String customFieldType) {
		RtInternalIdentifier id = makeIdentifier(idx, rtIdCon1);
		if (isWertAngegeben(id, false) != DreiWertBool.TRUE) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		FormatDefinitionVordefiniert formatDef = new FormatDefinitionVordefiniert(
				CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH, CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH,
				customFieldType);
		String value = validationCache.getWertXML(id).getWert();

		if (ValidType.Valid == formatDef.validateWithoutErrorReporting(value, modus, false)) {
			return ValidierungsErgebnis.TRUE_WF;
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	/**
	 * Implements the operator <code>Ungueltig</code> for predefined types. Returns
	 * <ul>
	 * <li>{@link ValidierungsErgebnis#TRUE_WF}, if the field is relevant, filled and invalid
	 * relating to the predefined type.
	 * <b>Note:</b> For predefined types that define a display value different to the XML value, the
	 * check is carried out without any conversion i.e. it is checked if the value is a valid XML
	 * value for the predefined type.</li>
	 * <li>{@link ValidierungsErgebnis#FALSE_OR_UNKNOWN} otherwise</li>
	 * </ul>
	 */
	public ValidierungsErgebnis predefinedTypeUngueltig(IterationState idx, RtIdentifierTemplate rtIdCon1, String customFieldType) {
		RtInternalIdentifier id = makeIdentifier(idx, rtIdCon1);
		if (isWertAngegeben(id, false) != DreiWertBool.TRUE) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		FormatDefinitionVordefiniert formatDef = new FormatDefinitionVordefiniert(
				CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH, CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH,
				customFieldType);
		String value = validationCache.getWertXML(id).getWert();

		if (ValidType.Valid != formatDef.validateWithoutErrorReporting(value, modus, false)) {
			return ValidierungsErgebnis.TRUE_WF;
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	/**
	 * Implementiert das Sprachkonstrukt MaximalAngegebenerZeilenindex.
	 * Wird bei einem Vergleich ungleich kleiner oder kleiner gleich verwendet. <br>
	 * ACHTUNG: Alle Parameter der feldListe müssen auf dem gleichen USB, der gleichen
	 * lfd. Nr. sein und alle Zeilen spezifiziert haben. Das Sprachkonstrukt ist nur in H-DVs erlaubt.
	 */
	public VkBigDecimal maximalAngegebenerZeilenindex(IterationState index, List<RtIdentifierTemplate> entityList) {
		long maxAngMZI = 0;
		long maxMZI = 0;

		for (RtIdentifierTemplate rtIdContainer : entityList) {
			long aktZeilenindex = 0;
			EntityIterator fIt = new EntityIterator(rtIdContainer, index);
			for (RtInternalIdentifier id : fIt) {
				DreiWertBool wertAngegeben = isWertAngegeben(id, false);
				if (wertAngegeben == DreiWertBool.UNKNOWN) {
					//nicht prüfunsrelevantes Feld gefunden, Vergleich kann nicht mehr feuern ->
					//echter Wert nicht wichtig, gib 0 zurück
					return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
				}
				aktZeilenindex = id.getIndices()[id.getIndices().length - 1];
				if (wertAngegeben == DreiWertBool.TRUE) {
					maxAngMZI = Math.max(maxAngMZI, aktZeilenindex);
				}
			}
			maxMZI = Math.max(maxMZI, aktZeilenindex);
		}

		boolean kannGroesserWerden = maxAngMZI < maxMZI;
		return new VkBigDecimal(maxAngMZI, false, kannGroesserWerden, false);
	}

	/**
	 * felderNichtGemeinsamAngegeben gibt TRUE_AF zurück, falls mindestens eins,
	 * aber nicht alle prüfungsrelevanten Felder angegeben wurden.
	 * <p>
	 * Es wird nicht optimiert, da für diese Methode keine Stern-Operation für hohe
	 * Wiederholbarkeiten erwartet wird.
	 * </p>
	 */
	public ValidierungsErgebnis felderNichtGemeinsamAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		return entitiesNichtGemeinsamAngegeben(index, entityList);
	}

	/**
	 * Returns {@link ValidierungsErgebnis#TRUE_AF}, if one, but not all relevant groups are given.
	 * If for a context, one cannot decide, whether it is given or not, this context is ignored.
	 */
	public ValidierungsErgebnis kontexteNichtGemeinsamAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		return entitiesNichtGemeinsamAngegeben(index, entityList);
	}

	private ValidierungsErgebnis entitiesNichtGemeinsamAngegeben(IterationState index, List<RtIdentifierTemplate> entityList) {
		if (mindestensEineEntityAngegeben(index, entityList) == ValidierungsErgebnis.FALSE_OR_UNKNOWN) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		if (nichtAlleOderKeineEntityAngegeben(index, entityList) == ValidierungsErgebnis.FALSE_OR_UNKNOWN) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		return ValidierungsErgebnis.TRUE_AF;
	}

	/**
	 * Liefert
	 * <ul>
	 * <li>TRUE_WF, wenn zwei prüfungsrelevante Felder ohne Filterbedingung in der entityList den
	 * gleichen String- bzw. Zahl-Wert haben.</li>
	 * <li>TRUE_AF, wenn zwei prüfungsrelevante Felder in der entityList den gleichen String- bzw.
	 * Zahl-Wert haben und eines der Felder in entityList eine Filterbedingung hat.</li>
	 * <li>FALSE sonst</li>
	 * </ul>
	 */
	public ValidierungsErgebnis feldWerteNichtEindeutig(IterationState index, List<RtIdentifierTemplate> entityList) {
		boolean isZahl = isZahlFeld(entityList.get(0).getUniqueName()); // all fields must have the same type
		Set<Object> werte = new HashSet<>();//speichert die gefundenen Werte
		boolean containsFilter = false;
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			if (rtIdContainer.isAggregation()) {
				EntityIterator fIt = new EntityIterator(rtIdContainer, index);
				setzeEndIndicesAufMaxGesetzt(fIt, false);
				for (RtInternalIdentifier id : fIt) {
					if (isWertAngegeben(id, false) == DreiWertBool.TRUE) { // this method should never get groups in entityList
						if (rtIdContainer.getFilter() != null) {
							containsFilter = true;
						}
						// nur prüfungsrelevante, angegebene Werte werden gesammelt
						Object v = (isZahl) ? feldWertAlsZahl(id) : feldWertIntern(id);
						if (!werte.add(v)) {
							return containsFilter ? ValidierungsErgebnis.TRUE_AF : ValidierungsErgebnis.TRUE_WF;
						}
					}
				}
			} else {
				RtInternalIdentifier rtId = RtInternalIdentifier.createFromTemplate(rtIdContainer, index);
				if (isWertAngegeben(rtId, false) == DreiWertBool.TRUE) {
					// nur prüfungsrelevante, angegebene Werte werden gesammelt
					Object v = (isZahl) ? feldWertAlsZahl(rtId) : feldWertIntern(rtId);
					if (!werte.add(v)) {
						return containsFilter ? ValidierungsErgebnis.TRUE_AF : ValidierungsErgebnis.TRUE_WF;
					}
				}
			}
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN;//alle prüfungsrelevanten Felder der Feldliste haben verschiedene Werte
	}

	/**
	 * Konvertiert ein Object-Array in ein Array von VkStrings. Die Werte sind entweder Strings
	 * (bei Referenzierung von Stringkonstanten) oder VkStrings (bei Verwendung von Feldwert oder
	 * BereichAlsString).
	 * <p>
	 * Vorbedingung: das Object-Array enthält nur Objekte vom Typ String und/oder VkString.
	 */
	private VkString[] convertToVkStrings(Object[] werte) {
		VkString[] vkStrings = new VkString[werte.length];
		for (int i = 0; i < werte.length; i++) {
			assert (werte[i] instanceof String) || (werte[i] instanceof VkString);
			if (werte[i] instanceof String stringValue) {
				vkStrings[i] = new VkString(stringValue);
			} else if (werte[i] instanceof VkString vkStringValue) {
				vkStrings[i] = vkStringValue;
			}
		}
		return vkStrings;
	}

	public ValidierungsErgebnis feldWertInWerteListeSTRING(IterationState idx, RtIdentifierTemplate entity, Object... werte) {
		return feldWertInWerteListeSTRING(idx, entity, convertToVkStrings(werte));
	}

	/**
	 * gibt TRUE_WF zurück, falls der Wert des Feldes in der Werteliste ist
	 * (mittels Stringvergleich ermittelt).
	 * Ist der Wert nicht angegeben, wird FALSE zurückgegeben.
	 */
	private ValidierungsErgebnis feldWertInWerteListeSTRING(IterationState idx, RtIdentifierTemplate entity, VkString... werte) {
		RtInternalIdentifier id = makeIdentifier(idx, entity);
		return feldWertInWerteListeSTRING(id, werte);
	}

	private ValidierungsErgebnis feldWertInWerteListeSTRING(RtInternalIdentifier id, VkString... values) {
		VkString value = feldWert(id);
		// nicht prüfungsrelevante oder nicht angegebene Felder in der Werteliste können nicht zu einem Fehler führen
		if (value.length() != 0 && Arrays.asList(values).contains(value)) {
			// wir haben einen Wert und dieser ist in der Werteliste enthalten
			return ValidierungsErgebnis.TRUE_WF;
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN; // kein Wert in der Werteliste gefunden
	}

	/**
	 * liefert TRUE_WF, falls der Wert des Feldes in der Werteliste ist
	 * (mittels Zahlvergleich ermittelt).
	 * Ist der Wert nicht angegeben, wird FALSE zurückgegeben.
	 */
	public ValidierungsErgebnis feldWertInWerteListe(IterationState idx, RtIdentifierTemplate entity, VkBigDecimal... werte) {
		RtInternalIdentifier id = makeIdentifier(idx, entity);
		return feldWertInWerteListe(id, werte);
	}

	private ValidierungsErgebnis feldWertInWerteListe(RtInternalIdentifier id, VkBigDecimal... werte) {
		String wert = feldWertIntern(id);
		if (wert.length() != 0) { // wir haben einen Wert
			VkBigDecimal d = BigDecimalUtils.makeDecimal(wert);
			for (VkBigDecimal w : werte) {
				/**
				 * Da in der Werteliste neben Konstanten nur Feldwerte (keine Rechenoperationen)
				 * stehen können, bedeutet kann größer / kleiner werden hier, dass das Feld
				 * nicht angegeben war.
				 */
				if (d.equals(w) && !w.isNichtPruefungsrelevant() && !w.kannGroesserWerden() && !w.kannKleinerWerden()) {
					return ValidierungsErgebnis.TRUE_WF;
				}
			}
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN; // kein Wert in der Werteliste gefunden
	}

	public ValidierungsErgebnis feldWertNichtInWerteListeSTRING(IterationState idx, RtIdentifierTemplate entity, Object... werte) {
		return feldWertNichtInWerteListeSTRING(idx, entity, convertToVkStrings(werte));
	}

	/**
	 * Liefert TRUE_WF bzw. TRUE_AF, wenn der Wert des Feldes nicht in der Werteliste vorkommt: <br>
	 * TRUE_WF, wenn alle betroffenen Felder angegeben sind (Werte in der
	 * Werteliste können auch Ergebnisse von Feldoperationen sein). <br>
	 * TRUE_AF, wenn eines der beteiligten Felder nicht angegeben ist. <br>
	 * FALSE sonst.
	 */
	private ValidierungsErgebnis feldWertNichtInWerteListeSTRING(IterationState idx, RtIdentifierTemplate entity,
			VkString... werte) {
		RtInternalIdentifier id = makeIdentifier(idx, entity);
		return feldWertNichtInWerteListeSTRING(id, werte);
	}

	private ValidierungsErgebnis feldWertNichtInWerteListeSTRING(RtInternalIdentifier id, VkString... werte) {
		VkString value = feldWert(id);
		if (value.length() == 0 || Arrays.asList(werte).contains(value)) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		for (VkString s : werte) {
			if (s.isNichtPruefungsrelevant()) {
				return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
			}
			if (s.isNichtAngegeben()) {
				return ValidierungsErgebnis.TRUE_AF;
			}
		}
		return ValidierungsErgebnis.TRUE_WF;
	}

	/**
	 * Liefert TRUE_WF bzw. TRUE_AF (bei nicht angegebenen Feldern), wenn der Wert des Feldes nicht
	 * in der Werteliste vorkommt, FALSE sonst. <br>
	 */
	public ValidierungsErgebnis feldWertNichtInWerteListe(IterationState index, RtIdentifierTemplate entity,
			VkBigDecimal... werte) {
		RtInternalIdentifier id = makeIdentifier(index, entity);
		return feldWertNichtInWerteListe(id, werte);
	}

	private ValidierungsErgebnis feldWertNichtInWerteListe(RtInternalIdentifier id, VkBigDecimal... werte) {
		String wert = feldWertIntern(id);
		VkBigDecimal d = BigDecimalUtils.makeDecimal(wert);
		if (wert.length() == 0) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		boolean nichtAngegebenesFeldGefunden = false;
		for (VkBigDecimal w : werte) {
			if (w.isNichtPruefungsrelevant()) {
				return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
			}
			/**
			 * Da in der Werteliste neben Konstanten nur Feldwerte (keine Rechenoperationen) stehen
			 * können, bedeutet kann größer / kleiner werden hier, dass das Feld nicht angegeben
			 * war.
			 */
			if (w.kannGroesserWerden() || w.kannKleinerWerden()) {
				nichtAngegebenesFeldGefunden = true;
			} else if (d.equals(w)) {
				return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
			}
		}
		return nichtAngegebenesFeldGefunden ? ValidierungsErgebnis.TRUE_AF : ValidierungsErgebnis.TRUE_WF;
	}

	/**
	 * This is the method which is used in the generated code
	 */
	public VkBigDecimal feldWertAlsZahl(IterationState index, RtIdentifierTemplate entity) {
		RtInternalIdentifier id = makeIdentifier(index, entity);
		return feldWertAlsZahl(id);
	}

	/**
	 * Leerstring wird zu der Zahl 0, dies entspricht dem definierten Defaultverhalten.
	 */
	public VkBigDecimal feldWertAlsZahl(RtInternalIdentifier id) {
		VkString s = feldWert(id);
		IFormatDefinition fd = metaData.getIFormatDefinition(id.getName());
		if (s.isNichtPruefungsrelevant() || !s.hasValue()) {
			boolean kannGroesserWerden = s.isNichtAngegeben();
			boolean kannKleinerWerden = s.isNichtAngegeben();
			boolean isEmpty = !s.hasValue();
			if (kannKleinerWerden && fd instanceof FormatDefinitionZahl fdz) {
				kannKleinerWerden = fdz.isSignAllowed();
			}
			return new VkBigDecimal(0, s.isNichtPruefungsrelevant(), kannGroesserWerden, kannKleinerWerden, isEmpty,
					CollectionWithCheapMergeCopy.empty());
		}
		return new VkBigDecimal(s, fd);
	}

	/**
	 * Used for FieldValueAsString. Returns the value of a numeric field as string.
	 */
	public VkString feldWertAlsString(IterationState index, RtIdentifierTemplate entity) {
		return feldWert(index, entity);
	}

	/**
	 * This is the method which is used in the generated code
	 */
	public VkString feldWert(IterationState index, RtIdentifierTemplate entity) {
		RtInternalIdentifier id = makeIdentifier(index, entity);
		return feldWert(id);
	}

	/**
	 * Zugriff auf die Feldwerte.
	 * Falls das Feld nicht prüfungsrelevant oder nicht angegeben ist, merkt man sich diese
	 * Information im Rückgabeobjekt.
	 * Es wird immer der Wert des Feldes oder Leerstring falls Wert nicht angegeben ist,
	 * zurückgegeben.
	 */
	public VkString feldWert(RtInternalIdentifier id) {
		if (!isPruefungsRelevant(id, false)) { // id is never a group, parameter irrelevant
			return VkString.NICHT_PRUEF_REL_STRING;
		}
		return validationCache.getWertXML(id);
	}

	/**
	 * Wird an allen Stellen verwendet, an denen die zusätzlichen Informationen von VkString nicht benötigt werden.
	 */
	private String feldWertIntern(RtInternalIdentifier id) {
		if (!isPruefungsRelevant(id, false)) { // id is never a group, parameter irrelevant
			return "";
		}
		return validationCache.getWertXML(id).getWert();
	}

	/**
	 * Gibt den Wert des Feldes als Datum zurück.
	 * <p>
	 * Anmerkung
	 * Direkt von der Sprache darf bei teilbekanntem Datum nicht auf den FeldWert zugegriffen
	 * werden.
	 * Die Methode wird aber intern verwendet für MonatAusDatum, JahrAusDatum.
	 * Es ist sichergestellt, dass dann das abgefragte Datumsfragment nicht teilbekannt ist. d.h.
	 * eine Ersetzung durch jeweils "01" bei Teilbekanntheit ist o.k., da diese Teile nicht
	 * abgefragt
	 * werden.
	 */
	public VkDate feldWertAlsDatum(IterationState idx, RtIdentifierTemplate entity) {
		RtInternalIdentifier id = makeIdentifier(idx, entity);
		return feldWertAlsDatum(id);
	}

	public VkDate feldWertAlsDatum(RtInternalIdentifier id) {
		FormatDefinitionDatum formatDef = (FormatDefinitionDatum) metaData.getIFormatDefinition(id.getName());
		VkString feldWert = feldWert(id);
		return feldWertAlsDatumIntern(feldWert, formatDef);
	}

	VkDate feldWertAlsDatumIntern(VkString feldWert, FormatDefinitionDatum formatDef) {
		if (formatDef == null) {
			return new VkDate(null);
		}
		if (!feldWert.hasValue() || feldWert.isNichtPruefungsrelevant()) {
			return new VkDate(null, feldWert.isNichtAngegeben(), feldWert.isNichtPruefungsrelevant(), feldWert.getOrigins());
		}
		String wert = feldWert.getWert();
		String format = formatDef.getFormat();
		DateAndFormat datumMitFormat = new DateAndFormat(wert, format, timeZone);
		if (PartiallyKnownDatesUtil.isYearUnknown(datumMitFormat)) {
			/* This should not happen as
			 * 1. the parser is supposed to prevent the situation that this method is called for date fields with YEAR_OPTIONAL
			 * 2. the method is only called with formally correct values
			 * to prevent a ParseException in case that a parser check is missing, return a non relevant value
			 */
			return VkDate.NON_RELEVANT_DATE;
		}
		if (formatDef.isTeilbekannt()) {
			PartiallyKnownDatesUtil.replaceMonthAndYearIfUnknown(datumMitFormat, DateInterval.INTERVAL_START);
		}
		return new VkDate(DateUtil.getValueAsDate(datumMitFormat, metaData.getBaseYearOrDefault()), feldWert.isNichtAngegeben(),
				false, feldWert.getOrigins());
	}

	public VkDateRange feldWertAlsDateRange(IterationState idx, RtIdentifierTemplate entity) {
		RtInternalIdentifier id = makeIdentifier(idx, entity);
		return feldWertAlsDateRange(id);
	}

	public VkDateRange feldWertAlsDateRange(RtInternalIdentifier id) {
		FormatDefinitionDatumBereich formatDef = (FormatDefinitionDatumBereich) metaData.getIFormatDefinition(id.getName());
		VkString feldWert = feldWert(id);
		return feldWertAlsDateRangeIntern(feldWert, formatDef);
	}

	VkDateRange feldWertAlsDateRangeIntern(VkString feldWert, FormatDefinitionDatumBereich formatDef) {
		if (formatDef == null) {
			return VkDateRange.NULL_DATE_RANGE;
		}
		if (!feldWert.hasValue() || feldWert.isNichtPruefungsrelevant()) {
			return new VkDateRange(null, null, feldWert.isNichtAngegeben(), feldWert.isNichtPruefungsrelevant(),
					feldWert.getOrigins());
		}
		String wert = feldWert.getWert();
		DarstellungsInformationIntern di = new DarstellungsInformationIntern(metaData.getDezimalTrenner(),
				this.modus.getPresentationInformation(),
				metaData.getBaseYearOrDefault(), metaData.getDefaultPresentationInformation().get(this.modus.getLanguage()),
				timeZone);

		String datumBereichTrenner = formatDef.getTrenner();
		String datumFormat = formatDef.getFormat();
		boolean datumTrennerHintenOpt = di.datumTrennerHintenOptional(datumFormat);
		int jahr = metaData.getBaseYearOrDefault();

		DateAndFormat[] dates = DateUtil.getDateRange(wert, datumBereichTrenner, datumFormat, datumTrennerHintenOpt, jahr,
				formatDef.getInterpretationOfYear(), timeZone);

		return datesAndFormatsToDateRange(dates, jahr, feldWert.isNichtAngegeben(), feldWert.getOrigins());
	}

	private static VkDateRange datesAndFormatsToDateRange(DateAndFormat[] dates, int jahr, boolean nichtAngegeben,
			CollectionWithCheapMergeCopy<RtInternalIdentifier> origins) {
		Date start = DateUtil.createDateFromDateFragment(dates[0], DateInterval.INTERVAL_START, jahr);
		Date end = DateUtil.createDateFromDateFragment(dates[1], DateInterval.INTERVAL_END, jahr);

		return new VkDateRange(start, end, nichtAngegeben, false, origins);
	}

	/**
	 * Gibt die Anzahl der angegebenen Kontexte zurück.
	 * In jedem Kontext wird nur solange iteriert, bis ein angegebenes Feld gefunden wurde oder
	 * bis es keine neuen Felder und keine neuen Wiederholungen gibt.
	 *
	 * @return Anzahl der angegebenen Kontexte
	 *
	 * 		<b>Anmerkung:</b> Dieser Code wird nur in H-DVs verwendet.
	 *
	 * 		<p>
	 * 		<b>Anmerkung:</b> aus Performancegründen wird bei der Teilvalidierung nicht weiter
	 * 		ausgewertet, sobald ein nicht-prüfungsrelevantes Feld gefunden wird. Für den
	 * 		Sonderfall, dass in einem untersuchten Kontext prüfungsrelevante und
	 * 		nicht-prüfungsrelevante Felder vorhanden sind, führt dies dazu, dass die Reihenfolge
	 * 		der Felder relevant wird. <br>
	 * 		Beispiel: im Kontext K sind die Felder F1, F2, und F3. F2 ist nicht prüfungsrelevant,
	 * 		F1 und F3 sind prüfungsrelevant. <br>
	 * 		Dann gibt es folgende Szenarien:
	 * 		<ul>
	 * 		<li>F1 ist angegeben {@literal -->} dann gilt der Kontext als angegeben und wird
	 * 		gezählt</li>
	 * 		<li>F1 ist nicht angegeben, aber F3 ist angegeben. Da aber F2 vor F3 geprüft wird,
	 * 		gilt die Bedingung als nicht auswertbar in der Teilbedingung.</li>
	 * 		</ul>
	 * 		Das Beispiel zeigt, dass die Reihenfolge der Felder bei der Auswertung relevant ist.
	 * 		Die Behandlung führt jedoch nie dazu, dass fälschlicherweise eine Regelbedingung zu
	 * 		true evaluiert (und dann einen Fehler auslösen würde). Es kann jedoch sein, dass bei
	 * 		einer Teilvalidierung eine Regel nicht zu true evaluiert, obwohl man sie eigentlich
	 * 		auswerten könnte. Dieses Vorgehen wurde aber aus Optimierungsgründen auch in anderen
	 * 		Fällen gewählt. Ausserdem ist davon auszugehen, dass im Normalfall entweder alle
	 * 		Felder eines Kontextes prüfungsrelevant sind oder keine. Daher tritt diese
	 * 		Unsymmetrie bei der Auswertung nur in Sonderfällen auf.
	 */
	public VkBigDecimal anzahlAngegebenerKontexte(IterationState index,
			Map<RtIdentifierTemplate, List<RtIdentifierTemplate>> groupedEntityLists) {
		List<KontextIterator> kIters = createKontextIteratorListe(index, groupedEntityLists);
		return anzahlAngegebenerKontexteInternal(kIters);
	}

	private VkBigDecimal anzahlAngegebenerKontexteInternal(List<KontextIterator> kIters) {
		int anzAngKontexte = 0;
		int anzAllerKontexte = 0;
		for (KontextIterator kIter : kIters) {
			if (validierungArt.isValidationPartWith3ValueLogic() && kIter.istEineWiederholungNichtPruefungsRelevant(validationCache,
					false)) {
				return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
			}
			anzAllerKontexte += kIter.getAnzahlKontexte();
			kIter.optimiereIteratorWennMoeglich(validationCache, validierungArt);
			RtInternalIdentifier id = kIter.getNextEntity();
			while (id != null) {
				DreiWertBool b = isWertAngegeben(id, false);
				if (b == DreiWertBool.UNKNOWN) {
					return VkBigDecimal.NICHT_PRUEF_REL_ZAHL; //ein Feld nicht prüf. rel. -> korrekter Wert nicht relevant
				}
				if (b == DreiWertBool.TRUE) {
					anzAngKontexte++;
					kIter.setToNextWiederholung();
				}
				id = kIter.getNextEntity();
			}

		}

		boolean kannGroesserWerden = anzAllerKontexte > anzAngKontexte;
		return new VkBigDecimal(anzAngKontexte, false, kannGroesserWerden, false);
	}

	/**
	 * Erzeugt aus der Stringdarstellung einer Kontextliste inkl. Feldern eine Liste von
	 * Kontextiteratoren. Iterationsvariablen in den Kontext- und Feldlisten-Strings werden durch
	 * die Wiederholungen ersetzt, die durch das übergebene Index-Array gegeben sind.
	 */
	private List<KontextIterator> createKontextIteratorListe(IterationState index,
			Map<RtIdentifierTemplate, List<RtIdentifierTemplate>> groupedEntityLists) {
		List<KontextIterator> kontextListe = new ArrayList<>();
		for (Entry<RtIdentifierTemplate, List<RtIdentifierTemplate>> entry : groupedEntityLists.entrySet()) {
			RtIdentifierTemplate key = entry.getKey();
			List<RtIdentifierTemplate> entityList = entry.getValue();
			kontextListe.add(new KontextIterator(key, entityList, index));
		}
		return kontextListe;
	}

	/**
	 * Returns the number of groups for which a value is given.
	 * <p>
	 * Die Information über nicht angegebene oder nicht prüfungsrelevante Felder
	 * wird gespeichert.
	 */
	public VkBigDecimal anzahlAngegebenerKontexte(IterationState index, List<RtIdentifierTemplate> groupLists) {
		return anzahlAngegebenerEntities(index, groupLists);
	}

	/**
	 * Gibt die Anzahl der angegebenen Felder in der Feldliste zurück.
	 * Die Information über nicht angegebene oder nicht prüfungsrelevante Felder
	 * wird gespeichert.
	 */
	public VkBigDecimal anzahlAngegebenerFelder(IterationState index, List<RtIdentifierTemplate> entityList) {
		return anzahlAngegebenerEntities(index, entityList);
	}

	private VkBigDecimal anzahlAngegebenerEntities(IterationState index, List<RtIdentifierTemplate> entityList) {
		int anzAngFelder = 0;
		int anzAllerFelder = 0;
		boolean containsFilteredValue = false;
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			EntityIterator fIt = new EntityIterator(rtIdContainer, index);
			if (fIt.isUStern() && validationCache.nichtAlleUSBsRelevant()) {
				return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
			}
			if (validierungArt.isValidationPartWith3ValueLogic() && fIt.istEineWiederholungNichtPruefungsRelevant(validationCache,
					!metaData.hasUniqueEntities())) {
				return VkBigDecimal.NICHT_PRUEF_REL_ZAHL; //ein Feld nicht prüf. rel. -> korrekter Wert nicht relevant
			}
			anzAllerFelder += fIt.getAnzahlFelder();
			setzeEndIndicesAufMaxGesetzt(fIt, true);
			fIt.optimiereMitGesetztenWiederholungenWennSinnvoll(validationCache);
			for (RtInternalIdentifier id : fIt) {
				DreiWertBool isGiven = isWertAngegeben(id, false);
				DreiWertBool b = isGiven != DreiWertBool.FALSE ? isGiven : isWertAngegeben(id, true);
				if (b == DreiWertBool.UNKNOWN) {
					return VkBigDecimal.NICHT_PRUEF_REL_ZAHL; //ein Feld nicht prüf. rel. -> korrekter Wert nicht relevant
				}
				if (b == DreiWertBool.TRUE) {
					if (rtIdContainer.getFilter() != null) {
						containsFilteredValue = true;
					}
					anzAngFelder++;
				}
			}
		}
		if (containsFilteredValue) {
			return new VkBigDecimal(anzAngFelder, false, true, true);
		}
		boolean kannGroesserWerden = anzAllerFelder > anzAngFelder;
		// NOTE: hier (und in vorherigem return Statement) werden die origins nicht berücksichtigt.
		// Im Fall von Kontexten wäre das aber auch mühsam bzw unklar welches Verhalten gewuenscht waere
		return new VkBigDecimal(anzAngFelder, false, kannGroesserWerden, false);
	}

	public VkBigDecimal anzahlVerschiedenerWerteZahl(IterationState index, List<RtIdentifierTemplate> entityList) {
		return combineFeldListe(index, entityList, new NumberOfDifferentValuesCombiner<VkBigDecimal>(), this::feldWertAlsZahl,
				true);
	}

	public VkBigDecimal anzahlVerschiedenerWerteString(IterationState index, List<RtIdentifierTemplate> entityList) {
		return combineFeldListe(index, entityList, new NumberOfDifferentValuesCombiner<VkString>(), this::feldWert, true);
	}

	public VkBigDecimal anzahlVerschiedenerWerteDatum(IterationState index, List<RtIdentifierTemplate> entityList) {
		return combineFeldListe(index, entityList, new NumberOfDifferentValuesCombiner<VkDate>(), this::feldWertAlsDatum, true);
	}

	/**
	 * Implementiert das Sprachkonstrukt AnzahlAngegebenerVordrucke.
	 * Vergleiche, die dieses Sprachkonstrukt verwenden, führen bei einer Teilvalidierung nie zum
	 * Fehler.
	 */
	public VkBigDecimal anzVordrucke(String vordruckName, long usb) {
		if (validierungArt.isValidationPartWith3ValueLogic()) {
			//Wert irrelevant, umfassende Vergleichsbedingung wird nicht ausgewertet
			return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
		}

		int anzVordrucke = validationCache.getAnzahlVordrucke(vordruckName, usb);
		boolean kannGroesserWerden = true; // für Auslassungsfehler
		return new VkBigDecimal(anzVordrucke, false, kannGroesserWerden, false);
	}

	/**
	 * This is the method which is used in the generated code
	 */
	public VkBigDecimal aktuelleVordruckNummer(IterationState index, RtIdentifierTemplate entity) {
		// no categories allowed
		RtInternalIdentifier id = makeIdentifier(index, entity);
		return aktuelleVordruckNummer(id);
	}

	/**
	 * Gibt die laufende Nummer Vordruck des aktuellen Feldes zurück.
	 * Falls das Feld nicht prüfungsrelevant oder nicht angegeben ist,
	 * wird 0 zurückgegeben. 0 führt dazu, dass ein späterer
	 * Vergleich immer zu false führt.
	 */
	public VkBigDecimal aktuelleVordruckNummer(RtInternalIdentifier id) {
		if (isWertAngegeben(id, false) == DreiWertBool.TRUE) { // If one field of a group is set, the number can be useful
			return new VkBigDecimal(id.getIndices()[0]);
		}
		return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
	}

	/**
	 * This is the method which is used in the generated code
	 */
	public VkBigDecimal aktuelleWiederholung(IterationState index, RtIdentifierTemplate entity) {
		RtInternalIdentifier id = makeIdentifier(index, entity);
		return aktuelleWiederholung(id);
	}

	/**
	 * Implements the language construct <code>CurrentRepetition</code>.
	 * If the current repetition is unknown (this can happen for semantic index or parallel
	 * iteration if the group is not relevant or not filled),
	 * {@link VkBigDecimal#NICHT_PRUEF_REL_ZAHL} is
	 * returned, which leads to the surrounding comparison evaluating to false.
	 */
	public VkBigDecimal aktuelleWiederholung(RtInternalIdentifier id) {
		RtInternalIdentifier resolvedId = validationCache.resolveSemanticIndex(id);
		long[] indices = resolvedId.getIndices();
		long currentRepetition = indices[indices.length - 1];
		// If the current repetition is known, the number can be useful
		return currentRepetition != IIdentifier.UNKNOWN ? new VkBigDecimal(currentRepetition) : VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
	}

	/**
	 * Implementiert das Sprachkonstrukt 'MinWert'.
	 */
	public VkBigDecimal minWert(IterationState index, List<RtIdentifierTemplate> entityList) {
		return combineFeldListe(index, entityList, new NumberCombiner.MinWertCombiner(), this::feldWertAlsZahl, true);
	}

	/**
	 * Hilfsmethode für Feldlistenoperationen wie z.B. Summe, MaxWert, MinWert etc.
	 *
	 * @param sparsenessOptimization
	 * 		allow optimization to skip (even non-trailing) empty fields. Empty fields will be
	 * 		reported in summary to {@link Combiner#setOmittedTail(IFormatDefinition)} but the
	 * 		order of empty-vs-filled fields may be lost (relevant with e.g.
	 *        {@link FirstValueCombiner.FirstValueCombinerDate} etc). Also, during partial validation, this
	 * 		optimization will in some cases report falsely-non-relevant values.<br>
	 * 		<p>
	 * 		Note that empty fields beyond the highest non-empty repetition may be skipped
	 * 		(and reported in summary to {@link Combiner#setOmittedTail(IFormatDefinition)})
	 * 		regardless of the sparsenessOptimization flag.
	 */
	private <I, O> O combineFeldListe(IterationState index, List<RtIdentifierTemplate> entityList, Combiner<I, O> c,
			Function<RtInternalIdentifier, I> valueProvider, boolean sparsenessOptimization) {
		boolean containsFilter = false;
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			EntityIterator fIt = new EntityIterator(rtIdContainer, index);
			int numFields = fIt.getAnzahlFelder();
			int visitedFields = 0;
			IFormatDefinition fd = metaData.getIFormatDefinition(fIt.getEntityName());
			if (fIt.isUStern() && fIt.setEndeUsbAufMaxGesetzterUsbIfPossible(validationCache)) {
				if (!validationCache.isFeldFuerAlleUSBsRelevant(fIt.getEntityName(), validierungArt)) {
					//Nicht alle USBs relevant und nicht für alle USBs ein Wert angegeben
					//-> hier kann schon entschieden werden,  dass es ein nicht prüfungsrelevantes
					//Feld in der Feldliste gibt
					//Wert irrelevant, umfassende Vergleichsbedingung wird nicht ausgewertet
					return c.getNonRelevantValue();
				}
				c.setOmittedTail(fd);
			}

			if (setzeEndIndicesAufMaxGesetzt(fIt, true)) {
				c.setOmittedTail(fd);
			}

			if (rtIdContainer.getFilter() != null) {
				containsFilter = true;
			}

			if (sparsenessOptimization) {
				fIt.optimiereMitGesetztenWiederholungenWennSinnvoll(validationCache);
				// note that istEineWiederholungNichtPruefungsRelevant returns false-positive in some cases (see docstring), which seems acceptable here
				if (validierungArt.isValidationPartWith3ValueLogic() && fIt.istEineWiederholungNichtPruefungsRelevant(
						validationCache, !metaData.hasUniqueEntities())) {
					return c.getNonRelevantValue(); //ein Feld nicht prüf. rel. -> korrekter Wert nicht relevant
				}

			}
			for (RtInternalIdentifier id : fIt) {
				if (!isPruefungsRelevant(id, false)) { // id is never a group, parameter irrelevant
					return c.getNonRelevantValue();
				}
				I value = valueProvider.apply(id);
				c.addValue(value, fd, true);
				if (!c.canChange()) {
					return c.getValue(containsFilter);
				}
				visitedFields += 1;
			}
			if (sparsenessOptimization && visitedFields < numFields) {
				c.setOmittedTail(fd);
			}
		}
		return c.getValue(containsFilter);
	}

	public VkBigDecimal min(VkBigDecimal... operatorListe) {
		return combineOperatorListe(operatorListe, new NumberCombiner.MinWertCombiner());
	}

	public VkDate minDatum(VkDate... operatorListe) {
		return combineOperatorListe(operatorListe, new DateCombiner.MinValueCombiner());
	}

	private <I, O> O combineOperatorListe(I[] operatorListe, Combiner<I, O> c) {
		for (int i = 0; i < operatorListe.length; i++) {
			c.addValue(operatorListe[i], null, false);
		}
		return c.getValue(false);
	}

	public ValidierungsErgebnis vergleicheDATUM(VglOp op, VkDate d1, VkDate d2) {
		boolean nichtPruefungsrelevant = d1.isNichtPruefungsrelevant() || d2.isNichtPruefungsrelevant();
		boolean nichtAngegeben = d1.isNichtAngegeben() || d2.isNichtAngegeben();
		boolean vergleich = BedingungsOperatorHelper.vergleicheDATUM(op, d1.getDate(), d2.getDate());

		if (!vergleich || nichtPruefungsrelevant) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		} else if (nichtAngegeben) {
			return ValidierungsErgebnis.TRUE_AF;
		} else {
			return ValidierungsErgebnis.TRUE_WF;
		}
	}

	public ValidierungsErgebnis vergleicheDATERANGE(VglOp op, VkDateRange d1, VkDateRange d2) {
		boolean nichtPruefungsrelevant = d1.isNichtPruefungsrelevant() || d2.isNichtPruefungsrelevant();
		boolean nichtAngegeben = d1.isNichtAngegeben() || d2.isNichtAngegeben();
		boolean vergleich = BedingungsOperatorHelper.vergleicheDATERANGE(op, d1, d2);

		if (!vergleich || nichtPruefungsrelevant) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		} else if (nichtAngegeben) {
			return ValidierungsErgebnis.TRUE_AF;
		} else {
			return ValidierungsErgebnis.TRUE_WF;
		}
	}

	public ValidierungsErgebnis vergleicheSTRING(VglOp op, VkString v1, VkString v2) {
		if (isStringNotRelevant(v1) || isStringNotRelevant(v2)) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		return vergleicheSTRING(op, v1.getWert(), v2.getWert(), v1.isNichtAngegeben() || v2.isNichtAngegeben());
	}

	public ValidierungsErgebnis vergleicheSTRING(VglOp op, VkString v1, String v2) {
		if (isStringNotRelevant(v1)) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		return vergleicheSTRING(op, v1.getWert(), v2, v1.isNichtAngegeben());
	}

	public ValidierungsErgebnis vergleicheSTRING(VglOp op, String v1, VkString v2) {
		if (isStringNotRelevant(v2)) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		return vergleicheSTRING(op, v1, v2.getWert(), v2.isNichtAngegeben());
	}

	private boolean isStringNotRelevant(VkString string) {
		return !string.hasValue() || string.isNichtPruefungsrelevant();
	}

	public ValidierungsErgebnis vergleicheSTRING(VglOp op, String v1, String v2, boolean isNichtAngegeben) {
		if (BedingungsOperatorHelper.vergleicheSTRING(op, v1, v2)) {
			return isNichtAngegeben ? ValidierungsErgebnis.TRUE_AF : ValidierungsErgebnis.TRUE_WF;
		} else {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
	}

	public ValidierungsErgebnis vergleiche(VglOp op, VkBigDecimal v1, VkBigDecimal v2) {
		return BedingungsOperatorHelper.vergleiche(op, v1, v2);
	}

	private VkString getSubstring(RtInternalIdentifier id, int start, int ende) {
		VkString feldWert = feldWert(id);
		String wert = feldWert.getWert();
		// Es ist sichergestellt, dass start eine positive Zahl ist und <= ende
		String substring = (StringUtils.isEmpty(wert) || ende > wert.length()) ? "" : wert.substring(start, ende);
		return new VkString(substring, feldWert.isNichtAngegeben(), feldWert.isNichtPruefungsrelevant(), feldWert.getOrigins());
	}

	/**
	 * bereichAlsString gibt den Substring des Feldwertes zurück. Es wird ein
	 * Leerstring zurückgegeben, wenn (1) der Feldwert nicht angegeben (2)
	 * Indices nicht zur Länge des Feldwertes passen
	 *
	 * @param id
	 * 		: Identifier der betrachtet wird
	 * @param start
	 * 		Index Beginn (startend bei 0, wird bei der Codegenerierung
	 * 		umgesetzt)
	 * @param ende
	 * 		Index Ende
	 * @return gibt den teilstring zurück oder Leerstring, wenn das Interval ausserhalb des Strings
	 */
	public VkString bereichAlsString(RtInternalIdentifier id, int start, int ende) {
		return getSubstring(id, start, ende);
	}

	/**
	 * bereichAlsZahl gibt den Substring des Feldwertes als Zahl zurück. Es
	 * wird 0 zurückgegeben, wenn <br>
	 * (1) der Feldwert nicht angegeben <br>
	 * (2) Indices nicht zur Länge des Feldwertes passen <br>
	 * (3) der Teilstring nicht in eine Zahl umgewandelt werden kann
	 *
	 * @param start
	 * 		Index Beginn (startend bei 0, wird bei der Codegenerierung
	 * 		umgesetzt)
	 * @param ende
	 * 		Index Ende
	 * @return Teilstring als Zahl
	 */
	public VkBigDecimal bereichAlsZahl(RtInternalIdentifier id, int start, int ende) {
		VkBigDecimal d = VkBigDecimal.ZAHL_0;
		VkString s = getSubstring(id, start, ende);
		String substring = s.getWert();
		//EXT-7360: nur Ganzzahlen erlaubt
		if (substring.matches("\\d+")) {
			d = BigDecimalUtils.makeDecimal(substring, s.getOrigins());
		}
		boolean kannGroesserWerden = s.isNichtAngegeben(); // kleiner werden kann es nicht (BereichAlsZahl gibt nur Zahlen >= 0 zurück)
		return new VkBigDecimal(d.getBigDecimalValue(), s.isNichtPruefungsrelevant(), kannGroesserWerden, false, d.getOrigins());
	}

	/**
	 * Vorbedingung:
	 * Feld ist Datumsfeld mit Tagesgenauigkeit Optional, Monatsgenauigkeit Optional oder
	 * Jahresgenauigkeit Optional. <br>
	 * Falls Feld nicht angegeben ist, wird null zurückgegeben.
	 * Wenn der Tag mit 00 spezifiziert ist, wird je nach Wert des
	 * Parameters isErsterTag der erste Tag oder der letzte Tag des Monats zurückgegeben.
	 * Wenn der Monat mit 00 spezifiziert ist, wird 01 oder 12 zurückgegeben.
	 * Wenn das Jahr mit 0000 spezifiziert ist, wird das Feld als nicht prüfungsrelevant behandelt.
	 *
	 * @return Datum oder null, falls kein valider Datumswert angegeben wurde
	 */
	public VkDate wertAlsDatum(RtInternalIdentifier id, boolean isAnfang, @NonNull String format) {
		VkString feldWert = feldWert(id);
		return wertAlsDatumIntern(feldWert, isAnfang, format);
	}

	VkDate wertAlsDatumIntern(VkString feldWert, boolean isAnfang,
			@NonNull String format) {
		String wert = feldWert.getWert();
		if (!feldWert.hasValue() || feldWert.isNichtPruefungsrelevant()) {
			return new VkDate(null, feldWert.isNichtAngegeben(), feldWert.isNichtPruefungsrelevant(), feldWert.getOrigins());
		}
		DateAndFormat datumMitFormat = new DateAndFormat(wert, format, timeZone);
		if (PartiallyKnownDatesUtil.isYearUnknown(datumMitFormat)) {
			return VkDate.NON_RELEVANT_DATE; // Angabe 00.00.0000 wird behandelt wie nicht prüfungsrelevant
		}
		DateInterval intervallSpec = DateInterval.create(isAnfang);
		PartiallyKnownDatesUtil.replaceMonthAndYearIfUnknown(datumMitFormat, intervallSpec);
		return new VkDate(ValidationDateParser.parseDateWithoutSyntaxCheck(datumMitFormat), feldWert.isNichtAngegeben(), false,
				feldWert.getOrigins());
	}

	/**
	 * Gibt ein Datum zurück, konstruiert aus den Angaben Tag, Monat und Jahr.
	 * Ergeben die Angaben kein gültiges Datum, so wird null zurückgegeben.
	 *
	 * @return erzeugtes Datum oder null falls kein Datum erzeugt werden kann.
	 */
	public VkDate constructDatum(VkBigDecimal tag, VkBigDecimal monat, VkBigDecimal jahr) {
		Date date = null;
		if (tag.intValue() != 0 && monat.intValue() != 0 && jahr.intValue() != 0) {
			date = DateUtil.createDate(tag.intValue(), monat.intValue(), jahr.intValue(), timeZone);
		}
		VkBigDecimal[] dateParts = new VkBigDecimal[] { tag, monat, jahr };
		return constructDatumHelper(date, dateParts);
	}

	/**
	 * Gibt ein Datum zurück, konstruiert aus den Angaben Tag, Monat und Jahrhundert und
	 * Jahrzehnt (als getrennte Felder), so wird z.B. 1963, als 19 und 63 übergeben.
	 * Ergeben die Angaben kein gültiges Datum, so wird null zurückgegeben.
	 *
	 * @param tag
	 * 		angabe des Tages
	 * @param monat
	 * 		angabe des Monats (1 für Januar, 2 für Februar etc.)
	 * @param jahrhundert
	 * 		(erste zwei Ziffern als Jahrhundertangabe)
	 * @param jahrImJahrhundert
	 * 		(zweite zwei Ziffern als Jahrangabe)
	 * @return erzeugtes Datum oder null falls kein Datum erzeugt werden kann.
	 */
	public VkDate constructDatum(VkBigDecimal tag, VkBigDecimal monat, VkBigDecimal jahrhundert,
			VkBigDecimal jahrImJahrhundert) {
		Date date = null;
		if (tag.intValue() != 0 && monat.intValue() != 0 && jahrhundert.intValue() != 0 && !jahrImJahrhundert.kannGroesserWerden() && !jahrImJahrhundert.kannKleinerWerden()) {
			date = DateUtil.createDate(tag.intValue(), monat.intValue(),
					jahrhundert.intValue() * 100 + jahrImJahrhundert.intValue(), timeZone);
		}
		VkBigDecimal[] dateParts = new VkBigDecimal[] { tag, monat, jahrhundert, jahrImJahrhundert };

		return constructDatumHelper(date, dateParts);
	}

	/**
	 * Gibt das Datum zurück, dem der String 'wert' im Format 'format' entspricht, bzw. null, wenn
	 * das zu keinem gültigen Datum führt.
	 */
	public VkDate constructDatum(String wert, @NonNull String format) {
		if (StringUtils.isEmpty(wert)) {
			return new VkDate(null);
		}
		// This should only be used, when creating a date from a constant, therefore noe origin can be assigned
		return new VkDate(DateUtil.getValueAsDate(new DateAndFormat(wert, format, timeZone), metaData.getBaseYearOrDefault()));
	}

	/**
	 * Returns a {@link Date}-Object containing the date 1.1.1970 and the specified time.
	 *
	 * @return the created {@link Date}-Object or <code>null</code> if no date could be created.
	 */
	public VkDate constructTime(VkBigDecimal hour, VkBigDecimal minute, VkBigDecimal second) {
		Date date = null;
		if (isRegular(hour) && isRegular(minute) && isRegular(second)) {
			date = DateUtil.createTime(hour.intValue(), minute.intValue(), second.intValue(), timeZone);
		}
		VkBigDecimal[] dateParts = new VkBigDecimal[] { hour, minute, second };
		return constructDatumHelper(date, dateParts);
	}

	public VkDate constructDateTime(VkDate date, VkDate time) {
		Date result = DateUtil.createDateTime(date.getDate(), time.getDate(), timeZone);
		boolean nichtAngegeben = date.isNichtAngegeben() || time.isNichtAngegeben();
		boolean nichtPruefungsrelevant = date.isNichtPruefungsrelevant() || time.isNichtPruefungsrelevant();
		if (nichtPruefungsrelevant) {
			return VkDate.NON_RELEVANT_DATE;
		}
		return new VkDate(result, nichtAngegeben, nichtPruefungsrelevant, date.getOrigins().merge(time.getOrigins()));
	}

	public VkDateRange constructDateRange(IterationState index, RtIdentifierTemplate idStart, RtIdentifierTemplate idEnd) {
		VkString startString = feldWert(index, idStart);
		// We cannot simply use feldWertAlsDatum here, because we have to use the last day possible to correspond with feldWertAlsDateRange
		VkString endString = feldWert(index, idEnd);
		boolean notGiven = startString.isNichtAngegeben() || endString.isNichtAngegeben();
		boolean notRelevant = startString.isNichtPruefungsrelevant() || endString.isNichtPruefungsrelevant();
		boolean isEmpty = !startString.hasValue() || !endString.hasValue();
		if (notRelevant) {
			return VkDateRange.NON_RELEVANT_DATE_RANGE;
		}
		if (isEmpty) {
			return VkDateRange.NOT_GIVEN_DATE_RANGE;
		}

		String formatStart = ((FormatDefinitionDatum) metaData.getIFormatDefinition(idStart.getUniqueName())).getFormat();
		String formatEnd = ((FormatDefinitionDatum) metaData.getIFormatDefinition(idEnd.getUniqueName())).getFormat();

		DateAndFormat[] datesAndFormats = new DateAndFormat[] {
				new DateAndFormat(startString.getWert(), formatStart, timeZone),
				new DateAndFormat(endString.getWert(), formatEnd, timeZone)
		};

		int baseYear = metaData.getBaseYearOrDefault();
		var origins = startString.getOrigins().merge(endString.getOrigins());
		return datesAndFormatsToDateRange(datesAndFormats, baseYear, notGiven, origins);
	}

	/**
	 * @return <code>true</code> if the given number was actually filled and is relevant for the
	 * 		validation, otherwise <code>false</false>
	 */
	private boolean isRegular(VkBigDecimal number) {
		return !number.kannGroesserWerden() && !number.kannKleinerWerden() && !number.isNichtPruefungsrelevant();
	}

	private VkDate constructDatumHelper(Date date, VkBigDecimal[] dateParts) {
		boolean nichtAngegeben = false;
		boolean nichtPruefungsrelevant = false;
		for (VkBigDecimal d : dateParts) {
			/**
			 * Da ein Datum nur aus Feldern (nicht aus Rechenoperationen) konstruiert werden kann,
			 * bedeutet kann größer / kleiner werden hier, dass das Feld nicht angegeben war.
			 */
			if (d.kannGroesserWerden() || d.kannKleinerWerden()) {
				nichtAngegeben = true;
			}
			if (d.isNichtPruefungsrelevant()) {
				nichtPruefungsrelevant = true;
			}
		}
		if (nichtPruefungsrelevant) {
			return VkDate.NON_RELEVANT_DATE;
		}
		return new VkDate(date, nichtAngegeben, nichtPruefungsrelevant,
				Arrays.stream(dateParts).map(VkValue::getOrigins)
						.reduce(CollectionWithCheapMergeCopy.empty(), (a, b) -> a.merge(b)));
	}

	/**
	 * Gibt das Startdatum aus einem Feld vom Typ Datumbereich als Date zurück.
	 * Falls das Feld nicht angegeben ist, wird null zurückgegeben.
	 */
	public VkDate getStartAusDatumBereich(RtInternalIdentifier id) {
		return getDatumBereichTeil(id, DateInterval.INTERVAL_START);
	}

	/**
	 * Gibt das Endedatum aus einem Feld vom Typ Datumbereich als Date zurück
	 * Falls das Feld nicht angegeben ist, wird null zurückgegeben.
	 */
	public VkDate getEndeAusDatumBereich(RtInternalIdentifier id) {
		return getDatumBereichTeil(id, DateInterval.INTERVAL_END);
	}

	private VkDate getDatumBereichTeil(RtInternalIdentifier id, DateInterval intervallSpec) {
		VkDateRange dateRange = feldWertAlsDateRange(id);
		if (dateRange.isNichtPruefungsrelevant() || !dateRange.hasValue()) {
			return new VkDate(null, dateRange.isNichtAngegeben(), dateRange.isNichtPruefungsrelevant(), dateRange.getOrigins());
		}
		Date date = switch (intervallSpec) {
			case INTERVAL_START -> dateRange.getStart();
			case INTERVAL_END -> dateRange.getEnd();
		};
		return new VkDate(date, dateRange.isNichtAngegeben(), false, dateRange.getOrigins());
	}

	/**
	 * Gibt den Tag aus dem Datum als Zahl zurück.
	 * Ist das Datum null, so wird 0 zurückgegeben.
	 * <p>
	 * Die Information über nicht angegebene / nicht prüfungsrelevante Felder wird im Rückgabewert
	 * gespeichert.
	 */
	public VkBigDecimal getTagAusDatum(VkDate datum) {
		return BedingungsOperatorHelper.getDatumsTeil(datum, ChronoField.DAY_OF_MONTH, timeZone);
	}

	/**
	 * Gibt den Monat aus dem Datum als Zahl zurück.
	 * Ist das Datum null, so wird 0 zurückgegeben.
	 * <p>
	 * Die Information über nicht angegebene / nicht prüfungsrelevante Felder wird im Rückgabewert
	 * gespeichert.
	 */
	public VkBigDecimal getMonatAusDatum(VkDate datum) {
		return BedingungsOperatorHelper.getDatumsTeil(datum, ChronoField.MONTH_OF_YEAR, timeZone);
	}

	/**
	 * Gibt den Jahr aus dem Datum als Zahl zurück.
	 * Ist das Datum null, so wird 0 zurückgegeben.
	 * <p>
	 * Die Information über nicht angegebene / nicht prüfungsrelevante Felder wird im Rückgabewert
	 * gespeichert.
	 */
	public VkBigDecimal getJahrAusDatum(VkDate datum) {
		return BedingungsOperatorHelper.getDatumsTeil(datum, ChronoField.YEAR_OF_ERA, timeZone);
	}

	/**
	 * Gibt das Quartal aus dem Datum als Zahl zurück.
	 * Ist das Datum null, so wird 0 zurückgegeben.
	 * <p>
	 * Die Information über nicht angegebene / nicht prüfungsrelevante Felder wird im Rückgabewert
	 * gespeichert.
	 */
	public VkBigDecimal getQuartalAusDatum(VkDate datum) {
		VkBigDecimal monatWert = getMonatAusDatum(datum);
		int monat = monatWert.intValue();
		int quartal = (monat == 0) ? 0 : ((monat - 1) / 3 + 1);

		return new VkBigDecimal(quartal, monatWert.isNichtPruefungsrelevant(), monatWert.kannGroesserWerden(),
				monatWert.kannKleinerWerden(), datum.getOrigins());
	}

	/**
	 * Gibt die Stunden aus dem Datum als Zahl zurück.
	 * Ist das Datum null, so wird 0 zurückgegeben.
	 * <p>
	 * Die Information über nicht angegebene / nicht prüfungsrelevante Felder wird im Rückgabewert
	 * gespeichert.
	 */
	public VkBigDecimal getStundenAusZeit(VkDate datum) {
		return BedingungsOperatorHelper.getDatumsTeil(datum, ChronoField.HOUR_OF_DAY, timeZone);
	}

	/**
	 * Gibt die Minuten aus dem Datum als Zahl zurück.
	 * Ist das Datum null, so wird 0 zurückgegeben.
	 * <p>
	 * Die Information über nicht angegebene / nicht prüfungsrelevante Felder wird im Rückgabewert
	 * gespeichert.
	 */
	public VkBigDecimal getMinutenAusZeit(VkDate datum) {
		return BedingungsOperatorHelper.getDatumsTeil(datum, ChronoField.MINUTE_OF_HOUR, timeZone);
	}

	/**
	 * Gibt die Sekunden aus dem Datum als Zahl zurück.
	 * Ist das Datum null, so wird 0 zurückgegeben.
	 * <p>
	 * Die Information über nicht angegebene / nicht prüfungsrelevante Felder wird im Rückgabewert
	 * gespeichert.
	 */
	public VkBigDecimal getSekundenAusZeit(VkDate datum) {
		return BedingungsOperatorHelper.getDatumsTeil(datum, ChronoField.SECOND_OF_MINUTE, timeZone);
	}

	/**
	 * Gibt den Datumsanteil der DateTime-Angabe zurück {@literal =>} d.h. die Zeit wird auf
	 * 00:00:00 Uhr gesetzt.
	 * <p>
	 * Die Information über nicht angegebene / nicht prüfungsrelevante Felder wird im Rückgabewert
	 * gespeichert.
	 */
	public VkDate getDatumAusDateTime(VkDate datum) {
		if (!datum.hasValue()) {
			return datum;
		}
		ZonedDateTime zdt = datum.getDate().toInstant().atZone(timeZone.toZoneId());
		Instant instant = zdt.toLocalDate().atStartOfDay().atZone(timeZone.toZoneId()).toInstant();
		return new VkDate(Date.from(instant), datum.isNichtAngegeben(), datum.isNichtPruefungsrelevant(), datum.getOrigins());
	}

	/**
	 * Gibt den Zeitanteil der DateTime-Angabe zurück {@literal =>} d.h. das Datum wird auf den
	 * 01.01.1970 gesetzt.
	 * <p>
	 * Die Information über nicht angegebene / nicht prüfungsrelevante Felder wird im Rückgabewert
	 * gespeichert.
	 */
	public VkDate getZeitAusDateTime(VkDate datum) {
		if (!datum.hasValue()) {
			return datum;
		}

		LocalTime localTime = datum.getDate().toInstant().atZone(timeZone.toZoneId()).toLocalTime();
		Instant instant = localTime.atDate(DateUtil.DEFAULT_LOCAL_DATE).atZone(timeZone.toZoneId()).toInstant();
		return new VkDate(Date.from(instant), datum.isNichtAngegeben(), datum.isNichtPruefungsrelevant(), datum.getOrigins());
	}

	/**
	 * Gibt die Differenz zwischen den beiden Datumsangaben in Jahren zurück
	 * Ist eines der Datumsangaben null, so wird 0 zurückgegeben.
	 * <p>
	 * Die Information über nicht angegebene / nicht prüfungsrelevante Felder wird im Rückgabewert
	 * gespeichert.
	 */
	public VkBigDecimal getDifferenzInJahren(VkDate datum1, VkDate datum2) {
		return BedingungsOperatorHelper.getDatumsDiff(datum1, datum2, ChronoUnit.YEARS, timeZone);
	}

	public VkBigDecimal getDifferenzInMonaten(VkDate datum1, VkDate datum2) {
		return BedingungsOperatorHelper.getDatumsDiff(datum1, datum2, ChronoUnit.MONTHS, timeZone);
	}

	public VkBigDecimal getDifferenzInTagen(VkDate datum1, VkDate datum2) {
		return BedingungsOperatorHelper.getDatumsDiff(datum1, datum2, ChronoUnit.DAYS, timeZone);
	}

	public VkBigDecimal getDifferenzInStunden(VkDate datum1, VkDate datum2) {
		return BedingungsOperatorHelper.getDatumsDiff(datum1, datum2, ChronoUnit.HOURS, timeZone);
	}

	public VkBigDecimal getDifferenzInMinuten(VkDate datum1, VkDate datum2) {
		return BedingungsOperatorHelper.getDatumsDiff(datum1, datum2, ChronoUnit.MINUTES, timeZone);
	}

	public VkBigDecimal getDifferenzInSekunden(VkDate datum1, VkDate datum2) {
		return BedingungsOperatorHelper.getDatumsDiff(datum1, datum2, ChronoUnit.SECONDS, timeZone);
	}

	public VkDate addiereJahre(VkDate datum, VkBigDecimal amount) {
		return BedingungsOperatorHelper.addTime(datum, amount, ChronoUnit.YEARS, timeZone);
	}

	public VkDate addiereMonate(VkDate datum, VkBigDecimal amount) {
		return BedingungsOperatorHelper.addTime(datum, amount, ChronoUnit.MONTHS, timeZone);
	}

	public VkDate addiereTage(VkDate datum, VkBigDecimal amount) {
		return BedingungsOperatorHelper.addTime(datum, amount, ChronoUnit.DAYS, timeZone);
	}

	public VkDate addiereStunden(VkDate datum, VkBigDecimal amount) {
		return BedingungsOperatorHelper.addTime(datum, amount, ChronoUnit.HOURS, timeZone);
	}

	public VkDate addiereMinuten(VkDate datum, VkBigDecimal amount) {
		return BedingungsOperatorHelper.addTime(datum, amount, ChronoUnit.MINUTES, timeZone);
	}

	public VkDate addiereSekunden(VkDate datum, VkBigDecimal amount) {
		return BedingungsOperatorHelper.addTime(datum, amount, ChronoUnit.SECONDS, timeZone);
	}

	/**
	 * Vorbedingung:
	 * Alle Felder in entityList sind vom Typ DatumBereich mit Zusatzprüfung. <br>
	 * Liefert TRUE_WF oder TRUE_AF, wenn zwei prüfungsrelevante Felder der FeldListe sich als
	 * Datumsbereiche überlappen, FALSE sonst.<br>
	 * TRUE_WF, wenn keiner der Datumsbereiche eine Filterbedingung hat, TRUE_AF sonst.<br>
	 * Start- und Enddatum der Datumsbereiche werden dabei mitberücksichtigt.
	 */
	public ValidierungsErgebnis datumsBereicheUeberlappend(IterationState index, List<RtIdentifierTemplate> entityList) {
		Set<DatumBereich> datumBereiche = new HashSet<>();
		boolean containsFilter = false;
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			EntityIterator fIt = new EntityIterator(rtIdContainer, index);
			setzeEndIndicesAufMaxGesetzt(fIt, false);

			for (RtInternalIdentifier id : fIt) {
				if (isWertAngegeben(id, false) != DreiWertBool.TRUE) {// id is never a group, parameter irrelevant
					continue;
				}
				DatumBereich datBerAkt = new DatumBereich(getStartAusDatumBereich(id).getDate(),
						getEndeAusDatumBereich(id).getDate());
				if (rtIdContainer.getFilter() != null) {
					containsFilter = true;
				}
				if (datBerAkt.ueberlapptMitEinemAusMenge(datumBereiche)) {
					return containsFilter ? ValidierungsErgebnis.TRUE_AF : ValidierungsErgebnis.TRUE_WF;
				}
				datumBereiche.add(datBerAkt);
			}
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	/**
	 * Setzt im Felditerator Endzeile, -LfdNr und -USB jeweils auf den maximalen Wert, für den ein
	 * Feld angegeben wurde, wenn dieser Wert zw. Start- und Endindex des Felditerators liegt. Ist
	 * der Wert kleiner als der Startindex, wird Endindex gleich Startindex gesetzt. Sonst bleibt
	 * der Iterator unverändert.<br>
	 * Hat der boolesche Parameter den Wert <code>true</code>, wird die Änderung jeweils nur dann
	 * durchgeführt, wenn die Teilvalidierungsart für den betreffenden Index "gleich" ist, d.h.
	 * wenn alle prüfungsrelevanten Felder für alle Werte für diesen Index prüfungsrelevant sind.
	 *
	 * @return true, falls mindestens ein Endeindex geändert wurde, false sonst
	 */
	protected boolean setzeEndIndicesAufMaxGesetzt(EntityIterator fIt, boolean setzeNurWennTVArtGleich) {
		boolean indexGesetzt = false;
		if (setzeNurWennTVArtGleich) {
			if (fIt.setzeAlleEbenenMitZeileAufMaxGesetztIfPossible(validationCache, validierungArt, 0)) {
				indexGesetzt = true;
			}
		} else {
			int anzWiederholungsEbenen = fIt.getAnzahlWiederholungsEbenen();
			int maxEbene = fIt.isField() ? anzWiederholungsEbenen - 1 : anzWiederholungsEbenen;
			indexGesetzt = setUsbToMaxFilledForRootGroup(fIt);
			for (int i = 1; i < maxEbene; ++i) {
				if (fIt.setEndeIndexFallsKleinerAktuellesEnde(i, validationCache.getMaxGesetzterKontext(i - 1))) {
					indexGesetzt = true;
				}
			}
			if (fIt.isField() && fIt.setEndeZeileAufMaxGesetzt(validationCache)) {
				indexGesetzt = true;
			}
		}
		return indexGesetzt;
	}

	private boolean setUsbToMaxFilledForRootGroup(EntityIterator fIt) {
		String rootGroup = EntityUtil.getRootGroupName(metaData.getPfadName(fIt.getEntityName()));
		long maxUsb = metaData.hasUniqueEntities()
				? validationCache.getMaxUsbMitAngegebenenFeldern(rootGroup)
				: 0;
		return fIt.setEndeIndexFallsKleinerAktuellesEnde(0, maxUsb);
	}

	/**
	 * Gibt TRUE_WF zurück,
	 * wenn wert nicht dem Muster regexp entspricht und op==NM ('NichtWieMuster') oder
	 * wenn wert dem Muster regexp entspricht und op==WM ('WieMuster')
	 * sonst FALSE
	 */
	public ValidierungsErgebnis vergleichePatternSTRING(VglOp op, VkString string, String regexp) {
		if (StringUtils.isEmpty(regexp)) {
			throw new IllegalArgumentException("error during code generation: PatternString is empty");
		}
		String wert = string.getWert();
		if (!string.hasValue()) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		Pattern p = BedingungsOperatorHelper.getPattern(regexp);
		// op kann nur NM oder WM sein, die Bedingung entspricht einem XNOR
		return (!p.matcher(wert).matches() == (op == VglOp.NM)) ?
				ValidierungsErgebnis.TRUE_WF :
				ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	/**
	 * Implementiert das Sprachkonstrukt AnzahlAngegebeneUsb.
	 * Vergleiche, die dieses Sprachkonstrukt verwenden, werden bei einer Teilvalidierung nach
	 * 3-Wert-Logik nicht ausgewertet.
	 */
	public VkBigDecimal anzahlUSB() {
		if (validierungArt.isValidationPartWith3ValueLogic()) {
			//Wert irrelevant, umfassende Vergleichsbedingung wird nicht ausgewertet
			return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
		}

		long anzahlUSB = validationCache.getAnzahlAngegebeneUSB();
		boolean kannGroesserWerden = true; // für Auslassungsfehler
		return new VkBigDecimal(anzahlUSB, false, kannGroesserWerden, false);
	}

	/**
	 * Hilfsmethode, erzeugt einen boolean-Array für alle Wiederholungsebenen und initialisiert
	 * diesen mit true.
	 */
	protected boolean[] createArrayFuerEbenenGleich() {
		int anzEbenen = 2;
		for (RtInternalIdentifier id : validationCache.getAllRelevantEntitiesIntern()) {
			boolean isField = metaData.isUniqueFieldName(id.getName());
			// The rows are represented separately on each layer, so for groups we need to add an additional row
			int numIndicesNecessary = isField ? id.getIndices().length : id.getIndices().length + 1;
			anzEbenen = Math.max(anzEbenen, numIndicesNecessary);
		}

		boolean[] result = new boolean[anzEbenen + 1]; // USB dazu
		Arrays.fill(result, true);
		return result;
	}

	public boolean checkUSBIndexFeldFehler() {
		if (isUSBIndexFeldFehler()) {
			// The language of this error message is german because it only affects ExtMP.
			logNichtAusgewertet("Regel wird nicht ausgewertet, da nicht alle USB Index Felder korrekt angegeben sind.");
			return false;
		}
		return true;
	}

	protected abstract void logNichtAusgewertet(String fehlermeldung);

	/**
	 * Erzeuge einen Identifier aus der Stringrepräsentation eines Feldes.
	 * Variablen in der Stringrepräsentation werden durch den konkreten Index ersetzt.
	 * Vorbedingung: Keine Stern-Operation
	 * Es wird durch die Grammatik und durch die Codegenerierung garantiert, dass
	 * makeIdentifier nie für eine String-Repräsentation aufgerufen wird, die
	 * eine Stern-Spezifikation enthält.
	 */
	public static RtInternalIdentifier makeIdentifier(IterationState indices, RtIdentifierTemplate entity) {
		return RtInternalIdentifier.createFromTemplate(entity, indices);
	}

	public VkDate getHeute() {
		Instant instant = validationTime.toLocalDate().atStartOfDay().atZone(timeZone.toZoneId()).toInstant();
		return new VkDate(Date.from(instant));
	}

	public VkDate getJetzt() {
		return new VkDate(Date.from(validationTime.toInstant()));
	}

	public VkDate getReferenzjahr() {
		return new VkDate(metaData.getBaseYearAsDate());
	}

	/**
	 * Die ersten zwei Parameter beschreiben die Feldliste. Deren Werte werden überprüft, ob diese
	 * in der WerteListe (repräsentiert durch eine FeldListe) vorhanden sind.
	 * Es wird zunächst die WerteListe berechnet und dann die Methode verwendet, die
	 * die FeldListe zu einer WerteListe überprüft.
	 */
	public ValidierungsErgebnis nichtAlleFeldWerteInWerteListeFL(IterationState index, List<RtIdentifierTemplate> entityList,
			List<RtIdentifierTemplate> valueList) {
		// if no entity is filled, NotAllFieldsInValueList can not evaluate to true
		if (noEntityFilled(index, entityList)) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		Set<String> values = new HashSet<>();
		boolean[] einWertDerWerteListeNichtAngegeben = new boolean[1];
		boolean foundNonRelevantFieldInValueList = !fuegeFeldWerteZuWerteListe(index, valueList, values,
				einWertDerWerteListeNichtAngegeben);
		if (foundNonRelevantFieldInValueList) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		boolean foundNonFilledField = einWertDerWerteListeNichtAngegeben[0];
		boolean isNumber = isZahlFeld(entityList.get(0).getUniqueName());
		List<Object> werteMenge = convertStringArrayToMenge(values.toArray(new String[values.size()]), isNumber);
		return nichtAlleFeldWerteInWerteListeOLIntern(index, entityList, werteMenge, foundNonFilledField, isNumber);
	}

	/**
	 * Die ersten zwei Parameter beschreiben die Feldliste. Deren Werte werden überprüft, ob diese
	 * in der Werteliste vorhanden sind.
	 */
	public ValidierungsErgebnis nichtAlleFeldWerteInWerteListeOL(IterationState index, List<RtIdentifierTemplate> entityList,
			List<Object> values, boolean foundNonFilledField, boolean foundNonRelevantFieldInValueList, boolean isNumber) {
		if (foundNonRelevantFieldInValueList) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		return nichtAlleFeldWerteInWerteListeOLIntern(index, entityList, values, foundNonFilledField, isNumber);

	}

	private ValidierungsErgebnis nichtAlleFeldWerteInWerteListeOLIntern(IterationState index, List<RtIdentifierTemplate> entityList,
			List<Object> values, boolean foundNonFilledField, boolean isNumber) {
		ValidierungsErgebnis nichtAlleFeldWerteInWerteListe = nichtAlleFeldWerteInWerteListeIntern(index, entityList, values,
				isNumber);
		if (foundNonFilledField && nichtAlleFeldWerteInWerteListe.isKnownAndTrue()) {
			return ValidierungsErgebnis.TRUE_AF;
		}

		return nichtAlleFeldWerteInWerteListe;
	}

	/**
	 * Der erste Parameter ist immer eine FeldListe.
	 * Dessen Werte werden überprüft, ob diese in der übergebenen Werteliste vorhanden
	 * sind. Wird ein Feld gefunden, dessen wert nicht in der Werteliste vorhanden ist,
	 * wird TRUE_WF zurückgegeben, FALSE sonst.
	 * Je nach Feldtyp wird auf Strings oder auf Zahlen verglichen.
	 */
	public ValidierungsErgebnis nichtAlleFeldWerteInWerteListeSL(IterationState index, List<RtIdentifierTemplate> entityList,
			String... werteListeAlsStringListe) {
		boolean isNumber = isZahlFeld(entityList.get(0).getUniqueName());
		List<Object> values = convertStringArrayToMenge(werteListeAlsStringListe, isNumber);

		return nichtAlleFeldWerteInWerteListeIntern(index, entityList, values, isNumber);
	}

	private ValidierungsErgebnis nichtAlleFeldWerteInWerteListeIntern(IterationState index, List<RtIdentifierTemplate> entityList,
			List<Object> werteMenge, boolean isNumber) {
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			if (rtIdContainer.isAggregation()) {
				EntityIterator fIt = new EntityIterator(rtIdContainer, index);
				// es werden nur gesetzte Felder betrachet,  daher muss die
				// Optimierung keine Besonderheiten berücksichtigen.
				setzeEndIndicesAufMaxGesetzt(fIt, false);
				for (RtInternalIdentifier id : fIt) {
					if (isValueNotContainedInArray(id, werteMenge, isNumber)) {
						if (rtIdContainer.getFilter() == null) {
							return ValidierungsErgebnis.TRUE_WF;
						} else {
							return ValidierungsErgebnis.TRUE_AF;
						}
					}
				}
			} else {
				RtInternalIdentifier rtId = RtInternalIdentifier.createFromTemplate(rtIdContainer, index);
				if (isValueNotContainedInArray(rtId, werteMenge, isNumber)) {
					return ValidierungsErgebnis.TRUE_WF;
				}
			}
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	private boolean noEntityFilled(IterationState index, List<RtIdentifierTemplate> entityList) {
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			if (rtIdContainer.isAggregation()) {
				EntityIterator fIt = new EntityIterator(rtIdContainer, index);
				// es werden nur gesetzte Felder betrachet,  daher muss die
				// Optimierung keine Besonderheiten berücksichtigen.
				setzeEndIndicesAufMaxGesetzt(fIt, false);
				for (RtInternalIdentifier id : fIt) {
					if (isWertAngegeben(id, false) == DreiWertBool.TRUE) {
						return false;
					}
				}
			} else {
				RtInternalIdentifier rtId = RtInternalIdentifier.createFromTemplate(rtIdContainer, index);
				if (isWertAngegeben(rtId, false) == DreiWertBool.TRUE) {
					return false;
				}
			}
		}
		return true;

	}

	private boolean isValueNotContainedInArray(RtInternalIdentifier id, List<Object> valueArray, boolean isNumber) {
		if (isWertAngegeben(id, false) == DreiWertBool.TRUE) { // id is never a group, parameter irrelevant
			Object value = getValue(isNumber, id);
			if (!valueArray.contains(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Füge die Werte der feldListe in die werte ein.
	 * Sind nicht alle Feldwerte angegeben oder hat eines der Felder in valueList eine
	 * Filterbedingung, setze einWertDerWerteListeNichtAngegeben auf true.
	 * Wird ein nicht prüfungsrelevantes Feld gefunden, gebe false zurück, true sonst.
	 */
	public boolean fuegeFeldWerteZuWerteListe(IterationState index, List<RtIdentifierTemplate> valueList, Set<String> werte,
			boolean[] einWertDerWerteListeNichtAngegeben) {
		einWertDerWerteListeNichtAngegeben[0] = false;
		for (RtIdentifierTemplate werteListeFeld : valueList) {
			if (werteListeFeld.getFilter() != null) {
				einWertDerWerteListeNichtAngegeben[0] = true;
			}
			EntityIterator fIt = new EntityIterator(werteListeFeld, index);
			if (fIt.isUStern() && !validationCache.isFeldFuerAlleUSBsRelevant(fIt.getEntityName(), validierungArt)) {
				//das Feld ist nicht für alle USBs relevant -> gib gleich false zurück
				return false;
			}

			boolean atLeastOneWasSet = false;
			int anzWiederholungsEbenen = fIt.isField() ?
					fIt.getAnzahlWiederholungsEbenen() - 1 :
					fIt.getAnzahlWiederholungsEbenen();
			for (int i = 0; i < anzWiederholungsEbenen; ++i) {
				if (validierungArt.istEbeneGleich(i)) {
					atLeastOneWasSet = fIt.setEndeIndexFallsKleinerAktuellesEnde(i,
							validationCache.getMaxGesetzteWiederholungOhneZeile(i)) || atLeastOneWasSet;
				}
			}

			if (atLeastOneWasSet) {
				//nicht für jede Wiederholung ist was angegeben -> merken wir es uns für Auslassungsfehler
				einWertDerWerteListeNichtAngegeben[0] = true;
			}

			for (RtInternalIdentifier id : fIt) {
				DreiWertBool wertAngegeben = isWertAngegeben(id, false);// id is never a group, parameter irrelevant
				switch (wertAngegeben) {
					case TRUE:
						String value = (String) getValue(false, id);
						werte.add(value);
						break;
					case FALSE:
						einWertDerWerteListeNichtAngegeben[0] = true;
						break;
					case UNKNOWN:
						return false;
				}
			}
		}
		return true;
	}

	/**
	 * Gibt alle Werte prüfungsrelevanter Felder der feldListe zurück.
	 *
	 * @return <code>true</code>, wenn die Werteliste ein Feld mit Filterbedingung enthält,
	 * 		<code>false</code> sonst.
	 */
	public boolean fuegePruefungsrelevanteFeldWerteZuWerteListe(IterationState index, List<RtIdentifierTemplate> valueList,
			Set<String> werte) {
		boolean containsFilter = false;
		for (RtIdentifierTemplate rtIdContainer : valueList) {
			EntityIterator fIt = new EntityIterator(rtIdContainer, index);
			//Optimierung auch wenn Teilvalidierung für konkreten USB / lfdNummer möglich
			//da nicht prüfungsrelevante Felder genauso behandelt werden wie nicht angegebene
			//Felder
			setzeEndIndicesAufMaxGesetzt(fIt, false);
			fIt.optimiereMitGesetztenWiederholungenWennSinnvoll(validationCache);
			for (RtInternalIdentifier id : fIt) {
				if (isWertAngegeben(id, false).equals(DreiWertBool.TRUE)) {// id is never a group, parameter irrelevant
					String value = (String) getValue(false, id);
					werte.add(value);
					if (rtIdContainer.getFilter() != null) {
						containsFilter = true;
					}
				}
			}
		}
		return containsFilter;
	}

	/**
	 * Implementiert das Sprachkonstrukt KeinFeldWertInWerteListe, wenn die Werteliste auch durch
	 * eine Feldliste gegeben ist.
	 * Gibt TRUE_WF/ TRUE_AF zurück, wenn alle Felder in beiden Feldlisten prüfungsrelevant sind
	 * und für kein Feld aus feldListe
	 * der Wert in den Werten der Felder aus werteListeAlsFeldIdListe vorkommt. Das ist insbesondere
	 * der Fall, wenn gar kein Feld in feldListe oder in werteListeAlsFeldIdListe angegeben wurde.
	 */
	public ValidierungsErgebnis keinFeldWertInWerteListeFL(IterationState index, List<RtIdentifierTemplate> entityList,
			List<RtIdentifierTemplate> valueList) {
		Set<String> werte = new HashSet<>();
		boolean[] einWertDerWerteListeNichtAngegeben = new boolean[1];
		boolean foundNonRelevantFieldInValueList = !fuegeFeldWerteZuWerteListe(index, valueList, werte,
				einWertDerWerteListeNichtAngegeben);
		if (foundNonRelevantFieldInValueList) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		boolean isNumber = isZahlFeld(entityList.get(0).getUniqueName());
		List<Object> values = convertStringArrayToMenge(werte.toArray(new String[0]), isNumber);
		boolean foundNonFilledFieldInValueList = einWertDerWerteListeNichtAngegeben[0];
		return keinFeldWertInWerteListeOLIntern(index, entityList, values, foundNonFilledFieldInValueList, isNumber);
	}

	/**
	 * Implementiert das Sprachkonstrukt KeinFeldWertInWerteListe, wenn die Werteliste auch durch
	 * eine Feldliste gegeben ist und die dadurch gegebenen Werte bereits ermittelt wurden.
	 * Gibt TRUE_WF/ TRUE_AF zurück, wenn alle Felder in beiden Feldlisten prüfungsrelevant sind
	 * und für kein Feld aus feldListe der Wert in den Werten in <code>values</code> vorkommt. Das
	 * ist insbesondere der Fall, wenn gar kein Feld in feldListe angegeben wurde.
	 */
	public ValidierungsErgebnis keinFeldWertInWerteListeOL(IterationState index, List<RtIdentifierTemplate> entityList,
			List<Object> values, boolean foundNonFilledFieldInValueList, boolean foundNonRelevantFieldInValueList,
			boolean isNumber) {
		if (foundNonRelevantFieldInValueList) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		return keinFeldWertInWerteListeOLIntern(index, entityList, values, foundNonFilledFieldInValueList, isNumber);
	}

	private ValidierungsErgebnis keinFeldWertInWerteListeOLIntern(IterationState index, List<RtIdentifierTemplate> entityList,
			List<Object> values, boolean foundNonFilledFieldInValueList, boolean isNumber) {
		ValidierungsErgebnis keinFeldWerteInWerteListe = keinFeldWertInWerteListeIntern(index, entityList, values, isNumber);
		if (foundNonFilledFieldInValueList && keinFeldWerteInWerteListe.isKnownAndTrue()) {
			return ValidierungsErgebnis.TRUE_AF;
		}
		return keinFeldWerteInWerteListe;
	}

	/**
	 * Implementiert das Sprachkonstrukt KeinFeldWertInWerteListe, wenn die Werteliste durch eine
	 * Liste von Stringkonstanten gegeben ist.
	 * Gibt TRUE_WF / TRUE_AF zurück, wenn alle Felder in feldListe prüfungsrelevant sind und für
	 * kein Feld aus feldListe der Wert in der Liste der Stringkonstanten vorkommt. Das ist
	 * insbesondere der Fall, wenn gar kein Feld in feldListe angegeben wurde.
	 */
	public ValidierungsErgebnis keinFeldWertInWerteListeSL(IterationState index, List<RtIdentifierTemplate> entityList,
			String... werteListeAlsStringListe) {
		boolean isNumber = isZahlFeld(entityList.get(0).getUniqueName());
		List<Object> values = convertStringArrayToMenge(werteListeAlsStringListe, isNumber);

		return keinFeldWertInWerteListeIntern(index, entityList, values, isNumber);
	}

	private ValidierungsErgebnis keinFeldWertInWerteListeIntern(IterationState index, List<RtIdentifierTemplate> entityList,
			List<Object> values, boolean isNumber) {
		boolean isAuslassungsFehler = false;

		for (RtIdentifierTemplate rtIdContainer : entityList) {
			EntityIterator fIt = new EntityIterator(rtIdContainer, index);
			if (fIt.isUStern() && !validationCache.isFeldFuerAlleUSBsRelevant(fIt.getEntityName(), validierungArt)) {
				//das Feld ist nicht für alle USBs relevant -> kein Fehler
				return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
			}

			boolean atLeastOneWasSet = false;
			int anzWiederholungsEbenen = fIt.isField() ?
					fIt.getAnzahlWiederholungsEbenen() - 1 :
					fIt.getAnzahlWiederholungsEbenen();
			for (int i = 0; i < anzWiederholungsEbenen; ++i) {
				if (validierungArt.istEbeneGleich(i)) {
					// Schleife nur über die Wiederholungen, für die Werte vorhanden sind
					atLeastOneWasSet = fIt.setEndeIndexFallsKleinerAktuellesEnde(i,
							validationCache.getMaxGesetzteWiederholungOhneZeile(i)) || atLeastOneWasSet;
				}
			}

			if (atLeastOneWasSet || rtIdContainer.getFilter() != null) {
				//nicht für jede Wiederholung ist was angegeben -> merken wir es uns für Auslassungsfehler
				isAuslassungsFehler = true;
			}

			for (RtInternalIdentifier id : fIt) {
				DreiWertBool wertAngegeben = isWertAngegeben(id, false); // id is never a group, parameter irrelevant
				switch (wertAngegeben) {
					case TRUE:
						// Feld ist angegeben und prüfungsrelevant => falls in der Werteliste, ist Prüfung zuende
						Object value = getValue(isNumber, id);
						if (values.contains(value)) {
							return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
						}
						break;
					case FALSE:
						isAuslassungsFehler = true;
						break;
					case UNKNOWN:
						//nicht prüfungsrelevantes Feld in der Feldliste -> Prüfung ist zuende
						return ValidierungsErgebnis.FALSE_OR_UNKNOWN;

				}
			}
		}

		return isAuslassungsFehler ? ValidierungsErgebnis.TRUE_AF : ValidierungsErgebnis.TRUE_WF;
	}

	/**
	 * Implements the predicate AtLeastOneDateRangeOverlaps
	 * (MindestensEinDatumsBereichUeberlappend).
	 * Returns
	 * <ul>
	 * <li>TRUE_WF, if the date range of the field contained in {@code singleEntityList} overlaps
	 * with at least one date range from the fields contained in {@code entityList} and this field
	 * does <b>not</b> define a filter condition.</li>
	 * <li>TRUE_AF, if the date range of the field contained in {@code singleEntityList} overlaps
	 * with at least one date range from the fields contained in {@code entityList} and this field
	 * defines a filter condition.</li>
	 * <li>Otherwise FALSE.</li>
	 * </ul>
	 * The parser ensures that the first field-parameter is a single field without '*'-operator.
	 */
	public ValidierungsErgebnis mindestensEinDatumsBereichUeberlappendFL(IterationState index,
			List<RtIdentifierTemplate> singleEntityInList, List<RtIdentifierTemplate> entityList) {
		if (singleEntityInList.size() != 1) {
			throw new IllegalArgumentException(
					"error during code generation: singleEntityInList should contains exactly one element");
		}
		EntityIterator feldIt = new EntityIterator(singleEntityInList.get(0), index);
		if (feldIt.getAnzahlFelder() != 1) {
			throw new IllegalArgumentException("error during code generation: Number of fields in first param != 1");
		}
		RtInternalIdentifier feldIdentifier = feldIt.get();
		if (isWertAngegeben(feldIdentifier, false) != DreiWertBool.TRUE) {// id is never a group, parameter irrelevant
			// trivialer Fall
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		DatumBereich datBerFeld = new DatumBereich(getStartAusDatumBereich(feldIdentifier).getDate(),
				getEndeAusDatumBereich(feldIdentifier).getDate());

		for (RtIdentifierTemplate rtIdContainer : entityList) {
			EntityIterator fIt = new EntityIterator(rtIdContainer, index);
			//Optimierung auch wenn Teilvalidierung für konkreten USB / lfdNummer möglich
			//da nicht prüfungsrelevante Felder genauso behandelt werden wie nicht angegebene
			//Felder
			setzeEndIndicesAufMaxGesetzt(fIt, false);
			fIt.optimiereMitGesetztenWiederholungenWennSinnvoll(validationCache);

			for (RtInternalIdentifier id : fIt) {
				if (isWertAngegeben(id, false) != DreiWertBool.TRUE) { // id is never a group, parameter irrelevant
					continue;
				}
				DatumBereich datBerAkt = new DatumBereich(getStartAusDatumBereich(id).getDate(),
						getEndeAusDatumBereich(id).getDate());
				if (datBerFeld.ueberlappt(datBerAkt)) {
					return rtIdContainer.getFilter() != null ? ValidierungsErgebnis.TRUE_AF : ValidierungsErgebnis.TRUE_WF;
				}
			}
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	/**
	 * Implementiert das Sprachkonstrukt MindestensEinFeldWertInWerteListe, wenn die Werteliste auch
	 * durch eine Feldliste gegeben ist.<br>
	 * Wenn für mindestens ein prüfungsrelevantes Feld der ersten Feldliste der Wert in den Werten
	 * der prüfungsrelevanten Felder aus werteListeAlsFeldIdListe vorkommt, wird TRUE_WF oder
	 * TRUE_AF zurückgegeben: TRUE_WF, wenn keines der betroffenen Felder eine Filterbedingung hat,
	 * TRUE_AF sonst.
	 */
	public ValidierungsErgebnis mindestensEinFeldWertInWerteListeFL(IterationState index, List<RtIdentifierTemplate> entityList,
			List<RtIdentifierTemplate> valueList) {
		Set<String> werte = new HashSet<>();
		boolean containsValueListFilter = fuegePruefungsrelevanteFeldWerteZuWerteListe(index, valueList, werte);
		boolean isNumber = isZahlFeld(entityList.get(0).getUniqueName());
		List<Object> valuesAsObjectList = convertStringArrayToMenge(werte.toArray(new String[werte.size()]), isNumber);
		ValidierungsErgebnis resultIntern = mindestensEinFeldWertInWerteListeIntern(index, entityList, valuesAsObjectList,
				isNumber);
		if (containsValueListFilter && resultIntern.isKnownAndTrue()) {
			return ValidierungsErgebnis.TRUE_AF;
		}
		return resultIntern;
	}

	/**
	 * Implementiert das Sprachkonstrukt MindestensEinFeldWertInWerteListe, wenn die Werteliste auch
	 * durch eine Feldliste gegeben ist und wenn die Werteliste bereits berechnet wurde.<br>
	 * Wenn für mindestens ein prüfungsrelevantes Feld in entityList der Wert in der Werteliste
	 * vorkommt, wird TRUE_WF oder TRUE_AF zurückgegeben: TRUE_WF, wenn keines der betroffenen
	 * Felder eine Filterbedingung hat, TRUE_AF sonst.
	 */
	public ValidierungsErgebnis mindestensEinFeldWertInWerteListeOL(IterationState idx, List<RtIdentifierTemplate> entityList,
			List<Object> values, boolean containsValueListFilter, boolean isNumber) {
		ValidierungsErgebnis resultIntern = mindestensEinFeldWertInWerteListeIntern(idx, entityList, values, isNumber);
		if (containsValueListFilter && resultIntern.isKnownAndTrue()) {
			return ValidierungsErgebnis.TRUE_AF;
		}
		return resultIntern;
	}

	private ValidierungsErgebnis mindestensEinFeldWertInWerteListeIntern(IterationState index,
			List<RtIdentifierTemplate> entityList, List<Object> values, boolean isNumber) {
		if (values.isEmpty()) {
			// trivialer Fall
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			if (rtIdContainer.isAggregation()) {
				EntityIterator fIt = new EntityIterator(rtIdContainer, index);
				// Optimierung auch wenn Teilvalidierung für konkreten USB / lfdNummer möglich
				// da nicht prüfungsrelevante Felder genauso behandelt werden wie nicht angegebene
				// Felder
				setzeEndIndicesAufMaxGesetzt(fIt, false);
				fIt.optimiereMitGesetztenWiederholungenWennSinnvoll(validationCache);
				for (RtInternalIdentifier id : fIt) {
					if (isValueContainedInArray(id, values, isNumber)) {
						return rtIdContainer.getFilter() != null ? ValidierungsErgebnis.TRUE_AF : ValidierungsErgebnis.TRUE_WF;
					}
				}
			} else {
				RtInternalIdentifier rtId = RtInternalIdentifier.createFromTemplate(rtIdContainer, index);
				if (isValueContainedInArray(rtId, values, isNumber)) {
					return ValidierungsErgebnis.TRUE_WF;
				}
			}
		}
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
	}

	/**
	 * Implementiert das Sprachkonstrukt MindestensEinFeldWertInWerteListe, wenn die Werteliste
	 * durch eine Liste von Stringkonstanten gegeben ist.
	 * Gibt TRUE_WF zurück, wenn für mindestens ein prüfungsrelevantes Feld in feldListe der Wert in
	 * der Liste der Stringkonstanten vorkommt.
	 */
	public ValidierungsErgebnis mindestensEinFeldWertInWerteListeSL(IterationState index, List<RtIdentifierTemplate> entityList,
			String... werteListeAlsStringListe) {
		boolean isNumber = isZahlFeld(entityList.get(0).getUniqueName());
		List<Object> values = convertStringArrayToMenge(werteListeAlsStringListe, isNumber);

		return mindestensEinFeldWertInWerteListeIntern(index, entityList, values, isNumber);
	}

	/**
	 * @param index
	 * 		current iteration state
	 * @param entityList
	 * 		the one element list containing only the tested field.
	 * @param valueListAsMixedList
	 * 		the list of values. Possible types are {@link String} or
	 *        {@link RtIdentifierTemplate}. The latter will be dereferenced.
	 * @return {@link ValidierungsErgebnis#TRUE_WF} if the tested entity and all fields in the value
	 * 		list are relevant and the value of the tested entity is not consistent to the given
	 * 		values regarding the table for the enumeration.
	 */
	public ValidierungsErgebnis wertNichtKonsistentML(IterationState index, List<RtIdentifierTemplate> entityList,
			Object... valueListAsMixedList) {
		RtIdentifierTemplate entity = entityList.get(0);
		VkString value = feldWert(index, entity);
		if (value.isNichtPruefungsrelevant() || !value.hasValue()) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}

		if (containsNonRelevantField(index, valueListAsMixedList)) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}

		List<String> valuesAsString = getMixedListAsStrings(index, valueListAsMixedList);
		if (valuesAsString.stream().anyMatch(StringUtils::isEmpty)) {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}

		FormatDefinitionString fd = (FormatDefinitionString) metaData.getIFormatDefinition(entity.getUniqueName());
		if (!fd.getDistinctValuesForDependentColumns(valuesAsString).contains(value.getWert())) {
			if (value.isNichtAngegeben()) {
				return ValidierungsErgebnis.TRUE_AF;
			} else {
				return ValidierungsErgebnis.TRUE_WF;
			}
		} else {
			return ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		}
	}

	private boolean containsNonRelevantField(IterationState index, Object... valueListAsMixedList) {
		return Arrays.stream(valueListAsMixedList)
				.filter(RtIdentifierTemplate.class::isInstance)
				.map(RtIdentifierTemplate.class::cast)
				.map(id -> makeIdentifier(index, id))
				.anyMatch(id -> !isPruefungsRelevant(id, false));// id is never a group, parameter irrelevant
	}

	/**
	 * @param index
	 * 		the current iteration state.
	 * @param valueListAsMixedList
	 * 		the list of values. Possible types are {@link String} or
	 *        {@link RtIdentifierTemplate}. The latter will be dereferenced.
	 * @return a list of strings containing the values of the value list and the dereferenced values
	 * 		for {@link RtIdentifierTemplate}s.
	 */
	protected List<String> getMixedListAsStrings(IterationState index, Object... valueListAsMixedList) {
		return Arrays.stream(valueListAsMixedList)
				.map(v -> dereferenceValue(v, index))
				.collect(Collectors.toList());
	}

	private String dereferenceValue(Object value, IterationState index) {
		if (value instanceof String result) {
			return result;
		}
		RtIdentifierTemplate id = (RtIdentifierTemplate) value;
		return feldWertAlsString(index, id).getWert();
	}

	private boolean isValueContainedInArray(RtInternalIdentifier id, List<Object> valueArray, boolean isNumber) {
		if (isWertAngegeben(id, false) == DreiWertBool.TRUE) {// id is never a group, parameter irrelevant
			// Feld ist angegeben und prüfungsrelevant => falls in der Werteliste, ist Prüfung zuende
			Object value = getValue(isNumber, id);
			if (valueArray.contains(value)) {
				return true;
			}
		}
		return false;
	}

	public boolean isZahlFeld(String entityName) {
		return MetaDataUtilValidierung.isNumberFeld(entityName, metaData);
	}

	/**
	 * Gibt die Anzahl der Felder in der Feldliste zurück, deren Wert der 'Zahl' entsprechen.
	 * Ist ein Feld nicht prüfungsrelevant, wird 0 zurückgegeben.
	 * Die Information über nicht prüfungsrelevante Felder wird gespeichert.
	 */
	public VkBigDecimal anzahlWertInFeldlisteZahl(VkBigDecimal zahl, IterationState index,
			List<RtIdentifierTemplate> entityList) {
		return anzahlWertInFeldlisteZahlIntern(zahl, index, entityList);
	}

	/**
	 * Gibt die Anzahl der Felder in der Feldliste zurück, die dem 'stringWert' entsprechen.
	 * Ist ein Feld nicht prüfungsrelevant, wird 0 zurückgegeben.
	 * Die Information über nicht prüfungsrelevante Felder wird gespeichert.
	 */
	public VkBigDecimal anzahlWertInFeldlisteString(String stringWert, IterationState index,
			List<RtIdentifierTemplate> entityList) {
		return anzahlWertInFeldlisteStringIntern(stringWert, index, entityList);
	}

	private VkBigDecimal anzahlWertInFeldlisteZahlIntern(VkBigDecimal zahl, IterationState index,
			List<RtIdentifierTemplate> entityList) {
		return anzahlWertInFeldlisteIntern(zahl, index, entityList, true);
	}

	private VkBigDecimal anzahlWertInFeldlisteStringIntern(String stringWert, IterationState index,
			List<RtIdentifierTemplate> entityList) {
		return anzahlWertInFeldlisteIntern(stringWert, index, entityList, false);
	}

	private VkBigDecimal anzahlWertInFeldlisteIntern(Object wert, IterationState index, List<RtIdentifierTemplate> entityList,
			boolean isNumber) {
		int d = 0;
		boolean kannGroesserWerden = false;
		boolean containsFilteredValue = false;
		Set<RtInternalIdentifier> origins = new HashSet<>();
		for (RtIdentifierTemplate rtIdContainer : entityList) {
			EntityIterator fIt = new EntityIterator(rtIdContainer, index);
			if (fIt.isUStern() && validationCache.nichtAlleUSBsRelevant()) {
				return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
			}
			if (setzeEndIndicesAufMaxGesetzt(fIt, true)) {
				kannGroesserWerden = true;
			}
			if (rtIdContainer.getFilter() != null) {
				kannGroesserWerden = true;
			}
			for (RtInternalIdentifier id : fIt) {
				DreiWertBool b = isWertAngegeben(id, false);// id is never a group, parameter irrelevant
				if (b == DreiWertBool.UNKNOWN) {
					return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
				}
				if (b == DreiWertBool.FALSE) {
					kannGroesserWerden = true;
				}
				if (b == DreiWertBool.TRUE) {
					Object v = getValue(isNumber, id);
					if (v.equals(wert)) {
						if (rtIdContainer.getFilter() != null) {
							containsFilteredValue = true;
						}
						origins.add(id);
						d++;
					}
				}
			}
		}
		if (containsFilteredValue) {
			return new VkBigDecimal(d, false, true, true,
					CollectionWithCheapMergeCopy.of(origins));
		}
		return new VkBigDecimal(d, false, kannGroesserWerden, false,
				CollectionWithCheapMergeCopy.of(origins));
	}

	private Object getValue(boolean isNumber, RtInternalIdentifier id) {
		if (isNumber) {
			return feldWertAlsZahl(id);
		} else {
			return feldWertIntern(id);
		}
	}

	public List<Object> convertStringArrayToMenge(String[] werteListeAlsStringListe, boolean isZahl) {
		List<Object> result = new ArrayList<>();
		if (isZahl) {
			for (String wert : werteListeAlsStringListe) {
				result.add(BigDecimalUtils.makeDecimal(wert));
			}
		} else {
			Collections.addAll(result, werteListeAlsStringListe);
		}
		return result;
	}

	public long getMaxGesetzteZeilen() {
		return validationCache.getMaxGesetzteZeilen();
	}

	public long getMaxGesetzterKontext(int kontextIndex) {
		return validationCache.getMaxGesetzterKontext(kontextIndex);
	}

	public long startMesspunkt() {
		return LOG.startMesspunkt();
	}

	public void logMessung(String regelName, long l, long startMesspunkt) {
		LOG.logMessung(regelName, l, startMesspunkt);
	}

	public void logMessung(String berechnungsAufrufName, long startMesspunkt) {
		LOG.logMessung(berechnungsAufrufName, startMesspunkt);
	}

	/*
	 * ACHTUNG: Die folgenden Methoden müssen in dieser Klasse bleiben, obwohl
	 * sie meist den Aufruf nur an BigDecimalUtils delegieren, denn diese Methoden
	 * werden aus den generierten Validator-Klassen gerufen.
	 * Falls man diese Methoden hier entfernen wollte würden die Templates, aus denen
	 * die Validator-Klassen generiert werden, komplexer.
	 *
	 * Ist ein Feld nicht angegeben oder nicht prüfungrelevant, so gibt feldWert(id)
	 * immer einen leeren String zurück.
	 */
	public VkBigDecimal abrundenWert(RtInternalIdentifier id, int anzStellen) {
		return BigDecimalUtils.abrunden(feldWertAlsZahl(id), anzStellen);
	}

	public VkBigDecimal aufrundenWert(RtInternalIdentifier id, int anzStellen) {
		return BigDecimalUtils.aufrunden(feldWertAlsZahl(id), anzStellen);
	}

	public VkBigDecimal rundenWert(RtInternalIdentifier id, int anzStellen) {
		return BigDecimalUtils.runden(feldWertAlsZahl(id), anzStellen);
	}

	/**
	 * This is the method which is used in the generated code
	 */
	public VkBigDecimal laenge(IterationState index, RtIdentifierTemplate entity) {
		// no categories allowed
		RtInternalIdentifier id = makeIdentifier(index, entity);
		return laenge(id);
	}

	/**
	 * This method is also used in the interpreter
	 */
	public VkBigDecimal laenge(RtInternalIdentifier id) {
		return BigDecimalUtils.laenge(feldWert(id));
	}

	public VkBigDecimal runden(VkBigDecimal operator, int anzStellen) {
		return BigDecimalUtils.runden(operator, anzStellen);
	}

	public VkBigDecimal aufrunden(VkBigDecimal operator, int anzStellen) {
		return BigDecimalUtils.aufrunden(operator, anzStellen);
	}

	public VkBigDecimal abrunden(VkBigDecimal operator, int anzStellen) {
		return BigDecimalUtils.abrunden(operator, anzStellen);
	}

	public VkBigDecimal makeDecimal(long d) {
		return BigDecimalUtils.makeDecimal(d);
	}

	public VkBigDecimal abs(VkBigDecimal operator) {
		return BigDecimalUtils.absOp(operator);
	}

	public VkBigDecimal makeDecimal(String wert) {
		return BigDecimalUtils.makeDecimal(wert);
	}

	/**
	 * This is the method which is used in the generated code
	 */
	public VkBigDecimal absWert(IterationState index, RtIdentifierTemplate entity) {
		VkBigDecimal d = feldWertAlsZahl(index, entity);
		return d.abs();
	}

	/**
	 * Implementiert das Sprachkonstrukt 'Summe'. Berechnet die Summe der Feldwerte in feldliste.
	 */
	public VkBigDecimal summe(IterationState index, List<RtIdentifierTemplate> entityList) {
		return combineFeldListe(index, entityList, new NumberCombiner.SummeCombiner(), this::feldWertAlsZahl, true);
	}

	/**
	 * Implementiert das Sprachkonstrukt 'SummeVonProdukten'. Berechnet die Summe der
	 * Produkte-Pro-Wiederholung der Feldwerte in feldliste.
	 */
	public VkBigDecimal summeVonProdukten(IterationState index, List<RtIdentifierTemplate> entityList) {
		VkBigDecimal result = VkBigDecimal.ZAHL_0;
		boolean nichtAngFeldGefunden = false;

		// Iteratoren optimieren
		List<Iterator<RtInternalIdentifier>> innerFeldIteratoren = new ArrayList<>(2);
		for (int i = 0; i < 2; ++i) {
			EntityIterator fIt = new EntityIterator(entityList.get(i), index);

			if (setzeEndIndicesAufMaxGesetzt(fIt, true)) {
				nichtAngFeldGefunden = true;
			}
			if (entityList.get(i).getFilter() != null) {
				nichtAngFeldGefunden = true;
			}
			innerFeldIteratoren.add(i, fIt.iterator());
		}

		// Wert berechnen. Wenn hier ein Feld nicht angegeben ist, wird dies automatisch im Ergebnis beachtet
		while (innerFeldIteratoren.get(0).hasNext() && innerFeldIteratoren.get(1).hasNext()) {
			RtInternalIdentifier id1 = innerFeldIteratoren.get(0).next();
			RtInternalIdentifier id2 = innerFeldIteratoren.get(1).next();
			if (!isPruefungsRelevant(id1, false) || !isPruefungsRelevant(id2, false)) {// id is never a group, parameter irrelevant
				return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
			}
			VkBigDecimal zahlWert1 = feldWertAlsZahl(id1);
			VkBigDecimal zahlWert2 = feldWertAlsZahl(id2);
			result = result.add(zahlWert1.multiply(zahlWert2));
		}

		if (nichtAngFeldGefunden) {
			return new VkBigDecimal(result.getBigDecimalValue(), false, true, true, result.getOrigins());
		}
		return result;
	}

	public VkBigDecimal max(VkBigDecimal... operatorListe) {
		return combineOperatorListe(operatorListe, new NumberCombiner.MaxWertCombiner());
	}

	public VkDate maxDatum(VkDate... operatorListe) {
		return combineOperatorListe(operatorListe, new DateCombiner.MaxValueCombiner());
	}

	/**
	 * Implementiert das Sprachkonstrukt 'MaxWert'.
	 */
	public VkBigDecimal maxWert(IterationState index, List<RtIdentifierTemplate> entityList) {
		return combineFeldListe(index, entityList, new NumberCombiner.MaxWertCombiner(), this::feldWertAlsZahl, true);
	}

	public VkDate maxWertDatum(IterationState index, List<RtIdentifierTemplate> entityList) {
		return combineFeldListe(index, entityList, new DateCombiner.MaxValueCombiner(), this::feldWertAlsDatum, true);
	}

	public VkDate minWertDatum(IterationState index, List<RtIdentifierTemplate> entityList) {
		return combineFeldListe(index, entityList, new DateCombiner.MinValueCombiner(), this::feldWertAlsDatum, true);
	}

	public VkBigDecimal ersterAngegebenerWertZahl(IterationState index, List<RtIdentifierTemplate> entityList) {
		return combineFeldListe(index, entityList, new FirstValueCombiner.FirstValueCombinerNumber(), this::feldWertAlsZahl, false);
	}

	public VkString ersterAngegebenerWert(IterationState index, List<RtIdentifierTemplate> entityList) {
		return combineFeldListe(index, entityList, new FirstValueCombiner.FirstValueCombinerString(), this::feldWert, false);
	}

	public VkDate ersterAngegebenerWertDatum(IterationState index, List<RtIdentifierTemplate> entityList) {
		return combineFeldListe(index, entityList, new FirstValueCombiner.FirstValueCombinerDate(), this::feldWertAlsDatum, false);
	}

	public VkDateRange ersterAngegebenerWertDateRange(IterationState index, List<RtIdentifierTemplate> entityList) {
		return combineFeldListe(index, entityList, new FirstValueCombiner.FirstValueCombinerDateRange(), this::feldWertAlsDateRange,
				false);
	}

	public VkBigDecimal summeVonTermen(IterationState idx, RtIdentifierTemplate iterationEntity,
			SetOperation<VkBigDecimal> op) {
		int numVisitedRepetitions = 0;
		EntityIterator iterator = new EntityIterator(iterationEntity, idx);
		if (validierungArt.isValidationPartWith3ValueLogic() &&
				iterator.istEineWiederholungNichtPruefungsRelevant(validationCache, true)) {
			// We might have false positives here for partial validation, but else we could have false positive error messages
			return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
		}
		int numTotalRepetitions = iterator.getAnzahlFelder();
		setzeEndIndicesAufMaxGesetzt(iterator, true);
		iterator.optimiereMitGesetztenWiederholungenWennSinnvoll(validationCache);
		VkBigDecimal sum = VkBigDecimal.ZAHL_0;
		for (RtInternalIdentifier iterationEntityId : iterator) {
			IterationState iterationState = iterationEntityId.getIterationState().orElseThrow();
			numVisitedRepetitions++;

			// Even if it is only partially relevant (e.g. only the operands are relevant) and one of the relevant fields is given,
			// we want to evaluate the operation.
			// might be replaced with !alleKontexteAngegeben(...).isKnownAndTrue()
			DreiWertBool result = isWertAngegeben(iterationEntityId, false);
			if (result == DreiWertBool.FALSE) {
				sum = sum.add(VkBigDecimal.NICHT_ANGEGEBENE_ZAHL);
				continue;
			} else if (result == DreiWertBool.UNKNOWN) {
				return VkBigDecimal.NICHT_PRUEF_REL_ZAHL;
			}
			VkBigDecimal value = op.calculate(iterationState);
			if (value.wertIstUngueltig()) { // for invalid values, we do _not_ want to mark the overall sum as invalid, since they
				// most likely originate from a "/ [emptyField]" divison -> treat the repetition like an empty field
				sum = sum.add(VkBigDecimal.NICHT_ANGEGEBENE_ZAHL);
				// Note: we don't know for sure if the invalid value is indeed due to
				// a "/ [emptyField]" division, but it is safer to err on the side of Auslassungsfehler than not. Theoretically, the
				// value might have kannGrößerWerden/kannKleinerWerden set accurately, but we should not rely on this here because
				// it seems likely to not be accurately implemented for all relevant invalid-value code paths ->  always assuming
				// NICHT_ANGEGEBENE_ZAHL seems safer
				continue;
			}
			sum = sum.add(value);
		}
		if (numVisitedRepetitions < numTotalRepetitions) {
			sum = sum.add(VkBigDecimal.NICHT_ANGEGEBENE_ZAHL);
		}
		return sum;
	}

	public @NonNull ValidationCache getValidationCache() {
		return validationCache;
	}

	private boolean isUSBIndexFeldFehler() {
		return validationCache.getUsbCache().isUSBIndexFeldFehler();
	}

	/**
	 * Liefert den maximalen internen USB, für den ein Feld angegeben wurde.
	 * Methode wird für die USB-Optimierungen benötigt.
	 * Sichtbarkeit public, da sie auch vom generierten Code aufgerufen wird.
	 */
	public long getMaxInternerUSBMitAngegebenenFeldern() {
		return validationCache.getMaxInternerUSBMitAngegebenenFeldern();
	}
}
