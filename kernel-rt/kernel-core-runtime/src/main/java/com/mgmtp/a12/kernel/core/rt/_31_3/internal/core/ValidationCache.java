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

import static com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IndexFieldDefaultValueUtils.getIndexFieldIdsEligibleForDefaultValueHandling;
import static com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IndexFieldDefaultValueUtils.getSValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.meta.RawParamHandler4Convert;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ArrayUtils;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.CollectionWithCheapMergeCopy;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.Constants;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.FeldUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.GesetzteWiederholungen;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IMetaKeysInternal;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IterationState;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.MetaDataUtilValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtExternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtExternalIdentifierWithSemanticIndex;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RuntimeUtils;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.SemanticIndexSpec;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.ValidierungArt;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.VkString;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICustomCondition;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ResultBaseImpl;

/**
 * Hier werden Daten zur Laufzeitoptimierung gecached.
 */
public class ValidationCache {

	/**
	 * This value is currently used to substitute the value of a string field with the property
	 * 'noValueValidation'. To this kind of fields huge values are normally assigned and that is the
	 * reason why the huge value is substituted.
	 */
	public static final String PSEUDO_VALUE = "X";

	private IData data = null;

	/**
	 * Hier werden zum Feld der XML-Wert und der Wert in Oberflächendarstellung
	 * gecached. Diese Klasse stellt Methoden bereit um den Cache zu füllen und
	 * um die relevanten Werte auszulesen.
	 */
	private final Map<RtInternalIdentifier, WerteContainer> datenCache = new HashMap<>();
	/**
	 * Speichert zu jedem Vordruck und jedem (internen) USB die laufenden Nummern, für die
	 * mindestens ein Feld in den zu validierenden Daten angegeben wurde. Als
	 * Key dient der VordruckName. Key der inneren Map ist der USB. Die Map wird
	 * während der formalen Prüfung gefüllt.
	 */
	private final Map<String, TreeMap<Long, TreeSet<Long>>> lfdNrnMitAngegebenenFeldernZuVordruckUndUsb = new HashMap<>();

	/**
	 * Speichert den maximalen Zeilenindex für den irgendein (prüfungsrelevantes) Feld befüllt
	 * ist.
	 */
	private long maxGesetzteZeile = 0;

	private final List<Long> maxGesetzteKontextIndices = new ArrayList<>();

	private GesetzteWiederholungen gesetzteWiederholungen = new GesetzteWiederholungen();

	/**
	 * Es gibt einen USB der für alle Felder nicht relevant ist.
	 */
	private boolean nichtRelevanterUSBVorhanden = false;

	/**
	 * Nur Felder in der Menge sind prüfungsrelevant (für Teilvalidierung).
	 * Felder mit den intern verwendeten USBs.
	 */
	private final Set<RtInternalIdentifier> pruefungsrelevanteEntitiesIntern = new HashSet<>();

	/**
	 * Zu jedem (eindeutigen) Feldnamen, für den ein Identifier prüfungsrelevant ist,
	 * werden die prüfungsrelevanten Identifier in der Map gespeichert.
	 * <p>
	 * A group counts as relevant, if at least one identifier in the group is relevant.
	 */
	private final Map<String, Set<RtInternalIdentifier>> pruefungsrelevanteEntitiesInternProName = new HashMap<>();

	/**
	 * For every group name, for which a contained identifier is relevant, the fully relevant group
	 * iterations are stored. For a fully relevant group, all descendant identifiers need to be
	 * relevant, too.
	 */
	private Map<String, Set<RtInternalIdentifier>> fullyRelevantGroupsIntern = new HashMap<>();

	protected Set<RtInternalIdentifier> erroneousGroups = new HashSet<>();

	/**
	 * Menge der prüfungsrelevanten Felder, wie von der Anwendung übergeben (mit den
	 * ursprünglichen USBs).
	 */
	private final Set<IIdentifier> pruefungsrelevanteEntitiesExtern = new HashSet<>();

	/**
	 * Felder, die bei der formalen Prüfung zu Fehlern führten, werden in dieser
	 * Menge gesammelt. Sie enthalten die ursprünglichen USB, wie von der Anwendung
	 * übergeben.
	 */
	private final Set<IIdentifier> fehlerhafteFelderExtern = new HashSet<>();
	/**
	 * Wie fehlerhafteFelderExtern, aber mit internen USBs und den intern verwendeten
	 * einduetigen Feldnamen.
	 */
	protected final Set<RtInternalIdentifier> fehlerhafteFelderIntern = new HashSet<>();

	/**
	 * Unbekannte Feldnamen von Feldern, die zur Validierung als Daten oder prüfungsrelevante Felder
	 * übergeben wurden, werden in dieser Menge gesammelt. Diese wird dazu benutzt, um
	 * pro unbekanntem Feldnamen nur einen formalen Fehler der Ergebnismenge hinzuzufügen
	 */
	private final Map<String, RtExternalIdentifier> unbekannteEntitiesExtern = new HashMap<>();

	private final USBIndexCache usbCache;
	protected final IMetaDataValidierung metaData;
	private final ValidationMode mode;
	private final DarstellungsInformationIntern presentationInfo;

	/**
	 * Der maximale interne USB, für den ein - formal nicht fehlerhaftes - Feld angegeben wurde.
	 * Wird benötigt für die USB-Optimierung als Iterationsgrenze.
	 * Das geht, weil die internen USBs fortlaufend vergeben werden.
	 */
	private long maxInternerUSBMitAngegebenenFeldern = 0;

	/**
	 * Speichert die Information, ob ein Feld eines USB-Vordrucks in den Datencache aufgenommen
	 * wurde.
	 */
	private boolean entityAufUSBVordruckAngegeben = false;
	private final Map<String, FeldVerwendung> felderVerwendung = new HashMap<>();

	/**
	 * For each index group (outer key) and the indices of its parent (key 2)
	 * there is a map from value to specific iteration index.
	 * Used to look up semantic indexes.
	 */
	protected final IndexFieldCache indexFieldCache;

	/**
	 * Remembers the fill status of groups (must not be used for HDVs).
	 */
	protected final GroupFillCache groupFillCache;

	private boolean isValidationPartWith3ValueLogic = false;

	private static class FeldVerwendung {

		/**
		 * Speichert pro (internem) USB und Indices ohne Zeile den maximalen
		 * befüllten Zeilenindex.
		 * Key ist die Liste der Indices -- mit dem USB vorne -- und ohne Zeile.
		 */
		Map<List<Long>, Long> maxFilledZeilen = new HashMap<>();

		boolean alleUSBsRelevant = false;

		long updateAndReturnMaxFilledZeile(RtInternalIdentifier identifier) {

			List<Long> indicesMitUSBOhneZeile = FeldUtil.getIndicesMitUSBUndOhneZeile(identifier);

			long[] indices = identifier.getIndices();
			long z = indices[indices.length - 1];

			Long cachedZeile = maxFilledZeilen.get(indicesMitUSBOhneZeile);
			if (cachedZeile == null) { // für lfdNummer noch nix gespeichert
				maxFilledZeilen.put(indicesMitUSBOhneZeile, z);
			} else {
				maxFilledZeilen.put(indicesMitUSBOhneZeile, Math.max(z, cachedZeile));
			}

			return z;
		}
	}

	private FeldVerwendung getFeldVerwendung(String name) {

		FeldVerwendung feldVerwendung = felderVerwendung.get(name);
		if (feldVerwendung != null) {
			return feldVerwendung;
		}
		feldVerwendung = new FeldVerwendung();
		felderVerwendung.put(name, feldVerwendung);
		return feldVerwendung;
	}

	/**
	 * Initialisiere den ValidationCache mit der maximalen Wiederholbarkeit 1
	 * für die laufende Nummer Vordruck
	 */
	public ValidationCache(IMetaDataValidierung metaData, ValidationMode mode, DarstellungsInformationIntern presentationInfo) {
		maxGesetzteKontextIndices.add(1L);
		this.metaData = metaData;
		this.usbCache = new USBIndexCache(metaData);
		this.mode = mode;
		this.presentationInfo = presentationInfo;
		this.indexFieldCache = new IndexFieldCache(metaData);
		this.groupFillCache = new GroupFillCache(metaData);
	}

	/**
	 * Liefert interne Metadaten Schnittstelle.
	 */
	public IMetaDataValidierung getMetaData() {
		return metaData;
	}

	/**
	 * Feld mit den zugehörigen Werten dem Cache hinzufügen.
	 * Überprüfung auf Dubletten erfolgt bereits in CheckCommand
	 */
	public void addFeldToDatenCache(final RtInternalIdentifier id, final String feldWertOberflaeche, final String xmlValue) {
		if (metaData.isIndexField(id.getName())) {
			// For index fields we cache all the values in the IndexFieldCache to be able to easily look up the correct iteration
			// for a given value in a semantic index construct. See class comment of IndexFieldCache for an example.
			indexFieldCache.setIndexFieldValue(id, xmlValue, null);
		}
		if (metaData.isUniqueFieldName(id.getName())) {
			setFieldFilled(id);
		} else {
			setGroupFilled(id);
		}
		WerteContainer wc = new WerteContainer(feldWertOberflaeche, xmlValue);
		datenCache.put(id, wc);
	}

	public void addPseudoFieldValueIfAbsent(RtInternalIdentifier id) {
		if (!datenCache.containsKey(id)) {
			addFeldToDatenCache(id, PSEUDO_VALUE, PSEUDO_VALUE);
		}
	}

	public void addInvalidFieldValue(RtInternalIdentifier id, IIdentifier idExtern) {
		markFieldAsInvalidForValidation(id, idExtern);
	}

	/**
	 * Liefert den XML-Wert des Feldes oder null falls es das Feld im Cache
	 * nicht gibt.
	 */
	public VkString getWertXML(final RtInternalIdentifier id) {
		RtInternalIdentifier resolvedId = resolveSemanticIndex(id);
		WerteContainer wc = datenCache.get(resolvedId);
		if (wc == null) {
			return VkString.NICHT_ANGEGEBENER_STRING;
		}
		String result = wc.getFeldWertXML();
		// NOTE: in case we'd want to mostly disable the origins-tracking, we might achieve this by simply using
		// CollectionWithCheapMergeCopy.empty() here (and maybe in the few places that also call CollectionWithCheapMergeCopy.of(id)
		// Background: CollectionWithCheapMergeCopy::merge optimizes merges where one or both collections are the empty singleton
		return new VkString(resolveCategory(resolvedId, result), CollectionWithCheapMergeCopy.of(resolvedId));
	}

	protected String resolveCategory(RtInternalIdentifier f, String value) {
		String category = f.getCategory();
		if (StringUtils.isEmpty(category)) {
			return value;
		}
		return MetaDataUtilValidierung.getValue4Category(metaData, f.getName(), value, category);
	}

	/**
	 * Liefert den Oberflächen-Wert des Feldes oder null falls es das Feld im
	 * Cache nicht gibt.
	 */
	public String getWertOberflaeche(RtInternalIdentifier f) {
		f = resolveSemanticIndex(f);
		WerteContainer wc = datenCache.get(f);
		if (wc == null) {
			return null;
		}
		return wc.getFeldWertOberflaeche();
	}

	/**
	 * Checks whether the given entity is filled.
	 */
	public boolean isEntityFilled(RtInternalIdentifier entity) {
		RtInternalIdentifier resolvedEntity = resolveSemanticIndex(entity);
		return datenCache.containsKey(resolvedEntity)
				|| (!metaData.hasUniqueEntities() && groupFillCache.isGroupFilled(resolvedEntity));
	}

	public boolean isGroupFilled(RtInternalIdentifier group) {
		assert !metaData.hasUniqueEntities();
		RtInternalIdentifier resolvedGroup = resolveSemanticIndex(group);
		return groupFillCache.isGroupFilled(resolvedGroup);
	}

	/**
	 * If a semantic index was defined, this method returns a new instance of
	 * {@link RtInternalIdentifier}
	 * in
	 * which the semantic index (see {@link Constants#SEMANTIC_INDEX}) is replaced with the specific
	 * index.
	 */
	public RtInternalIdentifier resolveSemanticIndex(RtInternalIdentifier id) {
		SemanticIndexSpec semIdx = id.getSemanticIndex();
		return semIdx == null
				? id
				: actuallyResolveSemanticIndex(id, semIdx);
	}

	protected RtInternalIdentifier actuallyResolveSemanticIndex(RtInternalIdentifier id, SemanticIndexSpec semIdx) {
		String indexValue = getSemanticIndexValue(semIdx);
		return replaceSemanticIndex(id, indexValue);
	}

	/**
	 * Get the index value for the given semantic index specification. If the semantic index is
	 * represented by a field, the value will be looked up in the data.
	 *
	 * @param semIdx
	 * 		the semantic index specification. Must not be null.
	 * @return the value for the semantic index.
	 */
	public String getSemanticIndexValue(SemanticIndexSpec semIdx) {
		if (semIdx.isEntity()) {
			RtInternalIdentifier semIdxId = semIdx.getAsEntity();
			return getWertXML(semIdxId).getWert();
		} else {
			return semIdx.getAsConstant();
		}
	}

	private RtInternalIdentifier replaceSemanticIndex(RtInternalIdentifier semIdxId, String indexValue) {
		Pair<Integer, Long> lvlIdx = getSemanticIndexReplacement(semIdxId, indexValue);

		return semIdxId.cloneWithSemanticIndexReplaced(lvlIdx.getEntry1(), lvlIdx.getEntry2());
	}

	/**
	 * @param semIdxId
	 * @return a {@link Pair} whose {@link Pair#getEntry1()} is the level, on which the semantic
	 * 		index is to be replaced and whose {@link Pair#getEntry2()} is the index that should
	 * 		be set.
	 */
	public Pair<Integer, Long> getSemanticIndexReplacement(RtInternalIdentifier semIdxId, String indexValue) {
		List<Long> indices = Arrays.stream(semIdxId.getIndices()).boxed().toList();
		return getSemanticIndexReplacement(semIdxId.getName(), indices, indexValue);
	}

	public Pair<Integer, Long> getSemanticIndexReplacement(String uniqueName, List<Long> idx, String indexValue) {
		String fullName = metaData.getPfadName(uniqueName);
		int lvl = idx.indexOf(Constants.SEMANTIC_INDEX);
		// The name of the group containing the index fields
		String groupPath = EntityUtil.getPathOfHierarchyLevel(fullName, lvl);
		List<Long> indices = idx.subList(0, lvl);

		long index = indexFieldCache.lookup(groupPath, indices, indexValue);

		return new Pair<>(lvl, index);
	}

	/**
	 * @return the specific index for the given root group and the given index value.
	 */
	public long getSpecificIndexForSemanticIndex(String rootGroupName, String indexValue) {
		return indexFieldCache.lookup(String.format("/%s", rootGroupName), Collections.<Long>emptyList(), indexValue);
	}

	public IterationState replaceSemanticIndexRepetition(
			IterationState origIdx,
			String indexGroupFullName, // group with the index field
			int levelOfIndexGroup, // zero-based index into IterationState.getIndexes (i.e. with USB)
			String indexValue
	) {
		long[] newIndexes = origIdx.getIndexes().clone();
		// The parent context is everything above the index group, in root-relative (USB-free) coordinates; the
		// index group's layer there is one less than its with-USB level, since USB occupies position 0.
		long repetition4IndexFieldValue = indexFieldCache.lookup(indexGroupFullName,
				RuntimeUtils.createListForSubArray(origIdx.getIndexesWithoutUsb(), 0, levelOfIndexGroup - 1), indexValue);
		newIndexes[levelOfIndexGroup] = repetition4IndexFieldValue;
		return new IterationState(newIndexes, indexValue);
	}

	/**
	 * Fügt das Feld zur Menge der unbekannten Felder hinzu. Diese werden dabei wird nur nach
	 * Feldname unterschieden.
	 */
	public void addUnbekanntesFeld(IIdentifier id) {
		unbekannteEntitiesExtern.put(id.getName(), new RtExternalIdentifier(id));
	}

	/**
	 * Liefert die unbekannten Felder. Pro unbekanntem Feldnamen wird nur ein IdentifierImpl als
	 * Vertreter gespeichert.
	 *
	 * @return die unbekannten Felder
	 */
	public Map<String, RtExternalIdentifier> getUnbekannteFelder() {
		return unbekannteEntitiesExtern;
	}

	private void setEntityFilledHDV(RtInternalIdentifier id, boolean isField) {
		assert metaData.hasUniqueEntities();
		//Setze Zeilenangabe
		if (isField) {
			// Nur für Felder werden die Zeilen erneuert
			long z = getFeldVerwendung(id.getName()).updateAndReturnMaxFilledZeile(id);
			maxGesetzteZeile = Math.max(z, maxGesetzteZeile);
		}
		// setze KontextAngabe und lfdNummerAngabe, aber ohne Zeilenangabe, daher length -1
		int numGroupLevels = isField ? id.getIndices().length - 1 : id.getIndices().length;
		for (int i = 0; i < numGroupLevels; i++) {
			if (maxGesetzteKontextIndices.size() <= i) {
				//es wurde noch kein Feld mit dieser Kontext-Schachtelungstiefe angegeben.
				maxGesetzteKontextIndices.add(1L);
			}
			maxGesetzteKontextIndices.set(i, Math.max(maxGesetzteKontextIndices.get(i), id.getIndices()[i]));
		}
		long usb = id.getUsb();
		if (maxInternerUSBMitAngegebenenFeldern < usb) {
			maxInternerUSBMitAngegebenenFeldern = usb;
		}
		if (metaData.getMaxUsb(id.getName()) != IIdentifier.USB_NOT_SPECIFIED) {
			entityAufUSBVordruckAngegeben = true;
		}
		String vordruck = EntityUtil.getRootGroupName(metaData.getPfadName(id.getName()));
		TreeMap<Long, TreeSet<Long>> vordruckInfo = lfdNrnMitAngegebenenFeldernZuVordruckUndUsb.get(vordruck);
		if (vordruckInfo == null) {
			vordruckInfo = new TreeMap<>();
		}
		TreeSet<Long> lfdNummern = vordruckInfo.get(usb);
		if (lfdNummern == null) {
			lfdNummern = new TreeSet<>();
		}
		lfdNummern.add(id.getIndices()[0]);
		vordruckInfo.put(usb, lfdNummern); // füge hinzu, da Set werden Duplikate vermieden
		lfdNrnMitAngegebenenFeldernZuVordruckUndUsb.put(vordruck, vordruckInfo);
		gesetzteWiederholungen.update(id);
	}

	private void setFieldFilledA12DV(RtInternalIdentifier id) {
		assert !metaData.hasUniqueEntities();
		maxGesetzteZeile = 1;
		groupFillCache.setFieldFilled(id);
		gesetzteWiederholungen.update(id);
	}

	private void setGroupFilledA12DV(RtInternalIdentifier id) {
		assert !metaData.hasUniqueEntities();
		groupFillCache.setGroupFilled(id);
		gesetzteWiederholungen.update(id);
	}

	protected void setFieldFilled(RtInternalIdentifier id) {
		if (metaData.hasUniqueEntities()) {
			setEntityFilledHDV(id, true);
		} else {
			setFieldFilledA12DV(id);
		}
	}

	protected void setGroupFilled(RtInternalIdentifier id) {
		if (metaData.hasUniqueEntities()) {
			setEntityFilledHDV(id, false);
		} else {
			setGroupFilledA12DV(id);
		}
	}

	/**
	 * Speichert, ob es nicht relevante USBS gibt
	 * d.h. USBs die für alle Felder nicht relevant sind.
	 */
	public void setNichtRelevanterUSBVorhanden(boolean b) {
		nichtRelevanterUSBVorhanden = b;
	}

	/**
	 * Gibt true zurück, wenn es einen USB gibt, für den kein Feld
	 * prüfungsrelevant ist.
	 */
	public boolean nichtAlleUSBsRelevant() {
		return nichtRelevanterUSBVorhanden;
	}

	/**
	 * Liefert die Zeilenzahl des Feldes, bei dem die Zeilenzahl unter allen
	 * befüllten Feldern maximal ist.
	 */
	public long getMaxGesetzteZeilen() {
		return maxGesetzteZeile;
	}

	/**
	 * Liefert den Index des Kontextes mit der Verschachtelungstiefe kontextIndex,
	 * bei dem unter allen angegebenen Feldern der Index maximal ist.
	 * Laufende Nummer ist Kontext mit Schachtelungstiefe 0
	 */
	public long getMaxGesetzterKontext(int kontextIndex) {
		if (metaData.hasUniqueEntities()) {
			return kontextIndex < maxGesetzteKontextIndices.size()
					? maxGesetzteKontextIndices.get(kontextIndex)
					: 1L;
		} else {
			return groupFillCache.getHighestRepetitionAtLevel(kontextIndex);
		}
	}

	/**
	 * Liefert für die spezifizierte Wiederholungsebene die maximal gesetzte Wiederholung, wobei nur
	 * Vordruck und Kontextwiederholbarkeiten betrachtet werden, Zeilen nicht. <br>
	 * Die Zeile muss separat betrachtet werden, weil die Zeilenebene bei A12-Modellen nicht fix ist.<br>
	 * Ebene 0 ist der USB, Ebene 1 die LfdNr, Ebene 2 Kontexte direkt unter dem Vordruck etc.
	 */
	public long getMaxGesetzteWiederholungOhneZeile(int ebene) {
		if (ebene == 0) {
			return maxInternerUSBMitAngegebenenFeldern;
		}
		return getMaxGesetzterKontext(ebene - 1);
	}

	public GesetzteWiederholungen getGesetzteWiederholungen() {
		return gesetzteWiederholungen;
	}

	/**
	 * Liefert die Anzahl der befüllten Vordrucke.
	 */
	public int getAnzahlVordrucke(String vordruckName, long usb) {
		if (metaData.hasUniqueEntities()) {
			Map<Long, TreeSet<Long>> felderProUsb = lfdNrnMitAngegebenenFeldernZuVordruckUndUsb.get(vordruckName);
			if (felderProUsb == null) {
				return 0;
			}
			Set<Long> indices = felderProUsb.get(usb);
			if (indices == null) {
				return 0;
			}
			return indices.size();
		} else {
			if (usb != IIdentifier.USB_NOT_SPECIFIED) {
				return 0;
			}
			String groupFullName = EntityUtil.getRootGroupPath(vordruckName);
			return groupFillCache.numberOfFilledGroupInstances(groupFullName);
		}
	}

	/**
	 * Liefert ein Set der USBs, bei denen für diesen Vordruck mindestens ein
	 * Feld in den zu validierenden Daten angegeben wurde.
	 */
	public SortedSet<Long> getAllUsbsMitAngegebenenFeldern(String vordruckName) {
		assert metaData.hasUniqueEntities();
		TreeMap<Long, TreeSet<Long>> sortedMap = lfdNrnMitAngegebenenFeldernZuVordruckUndUsb.get(vordruckName);
		return sortedMap != null && !sortedMap.isEmpty()
				? sortedMap.navigableKeySet()
				: Collections.emptySortedSet();
	}

	/**
	 * Called from generated code!
	 */
	public long getMaxUsbMitAngegebenenFeldern(String... vordruckNamen) {
		assert metaData.hasUniqueEntities();
		long result = 0;
		for (String vordruck : vordruckNamen) {
			SortedSet<Long> allUsbs = getAllUsbsMitAngegebenenFeldern(vordruck);
			if (!allUsbs.isEmpty()) {
				result = Math.max(result, allUsbs.last().longValue());
			}
		}
		return result;
	}

	/**
	 * Liefert ein Set der laufenden Nummern, bei denen für diesen Vordruck und
	 * (internen) USB mindestens ein Feld in den zu validierenden Daten angegeben wurde.
	 */
	public SortedSet<Long> getAllLfdNrnMitAngegebenenFeldern(String vordruckName, long usb) {
		assert metaData.hasUniqueEntities();
		Map<Long, TreeSet<Long>> lfdNummerMitAngegebenenFelder = lfdNrnMitAngegebenenFeldernZuVordruckUndUsb.get(vordruckName);
		if (lfdNummerMitAngegebenenFelder != null) {
			TreeSet<Long> result = lfdNummerMitAngegebenenFelder.get(usb);
			return result != null
					? result
					: Collections.emptySortedSet();
		}
		return Collections.emptySortedSet();
	}

	public int numberOfFilledGroupInstances(String groupPath) {
		assert !metaData.hasUniqueEntities();
		return groupFillCache.numberOfFilledGroupInstances(groupPath);
	}

	/**
	 * Fügt alle übergebenen Felder/Kontexte dem Cache der prüfungsrelevanten Felder/Kontexte mit
	 * internen USBs hinzu (Info für Teilvalidierung relevant).
	 *
	 * @param idSet
	 * 		- zu setzende Menge der prüfungsrelevanten Felder/Kontexte.
	 */
	public void addAllRelevantEntitiesIntern(Set<RtInternalIdentifier> idSet) {
		pruefungsrelevanteEntitiesIntern.addAll(idSet);
	}

	/**
	 * Liefert alle prüfungsrelevanten Felder/Kontexte mit den internen USBs.
	 *
	 * @return Menge der prüfungsrelevanten Felder/Kontexte, leere Menge falls kein
	 * 		Feld prüfungsrelevant.
	 */
	public Set<RtInternalIdentifier> getAllRelevantEntitiesIntern() {
		return pruefungsrelevanteEntitiesIntern;
	}

	/**
	 * Fügt der Map mit den prüfungsrelevanten Feldern/Kontexten pro Feld-/Kontextname einen
	 * Identifier hinzu. Interne Darstellung, d.h. unique Name
	 */
	public void addPruefungsrelevanteEntityInternZumNamen(RtInternalIdentifier id) {
		Set<RtInternalIdentifier> ids = pruefungsrelevanteEntitiesInternProName.computeIfAbsent(id.getName(), s -> new HashSet<>());
		ids.add(id);
	}

	public void reduceRedundanciesInRelevantEntities() {
		pruefungsrelevanteEntitiesInternProName.values().forEach(ValidationCache::reduceRedundancies);
	}

	public static void reduceRedundancies(Set<RtInternalIdentifier> rtInternalIdentifiers) {
		Set<RtInternalIdentifier> idsWithStar = rtInternalIdentifiers.stream()
				.filter(id -> ArrayUtils.contains(id.getIndices(), IIdentifier.ALL_INDICES))
				.collect(Collectors.toSet());
		Set<RtInternalIdentifier> toRemove = rtInternalIdentifiers.stream()
				.filter(id -> isRedundant(id, idsWithStar))
				.collect(Collectors.toSet());
		rtInternalIdentifiers.removeAll(toRemove);
	}

	private static boolean isRedundant(RtInternalIdentifier id, Set<RtInternalIdentifier> idsWithStar) {
		return idsWithStar.stream().anyMatch(starId -> !starId.equals(id) && starId.umfasst(id));
	}

	/**
	 * Liefert die internen prüfungsrelevanten Felder und Kontexte.
	 *
	 * @return die internen prüfungsrelevanten Felder und Kontexte.
	 */
	public Map<String, Set<RtInternalIdentifier>> getPruefungsrelevanteEntitiesIntern() {
		return pruefungsrelevanteEntitiesInternProName;
	}

	public void setFullyRelevantGroups(Map<String, Set<RtInternalIdentifier>> fullyRelevantGroupsByName) {
		fullyRelevantGroupsIntern = fullyRelevantGroupsByName;
	}

	public Map<String, Set<RtInternalIdentifier>> getFullyRelevantGroupsIntern() {
		return fullyRelevantGroupsIntern;
	}

	/**
	 * Fügt alle übergebenen Entitäten dem Cache mit externen USBs hinzu. Menge wird von den
	 * Applikationsbedingungen benötigt.
	 * Übergebene prüfungsrelevante Gruppen sind in dieser Menge nicht expandiert, dies muss von
	 * Applikationsbedingungen beachtet werden.
	 * Um nicht von den internen USBs rückkonvertieren zu müssen, wird die Menge gespeichert.
	 */
	public void addAllPruefungsrelevanteEntitiesExtern(Set<IIdentifier> set) {
		pruefungsrelevanteEntitiesExtern.addAll(set);
	}

	/**
	 * Liefert alle prüfungsrelevanten Felder mit den externen USBs.
	 */
	public Set<IIdentifier> getAllPruefungsrelevanteEntitiesExtern() {
		return pruefungsrelevanteEntitiesExtern;
	}

	/**
	 * Returns the information, whether the given entity has been detected as erroneous.<br>
	 * <p>
	 * The entity indices of <code>id</code> should be concrete, semantic indices and parallel
	 * iteration indices have to be resolved before calling this method. Otherwise the id will not
	 * be identified as erroneous.
	 *
	 * @param id
	 *        {@link RtInternalIdentifier} of the entity to be checked.
	 * @param partialIsUnknown
	 * 		specifies, whether for a group, one erroneous field leads to an erroneous group.
	 * @return <code>true</code>, if the syntactic validation of the entity failed.
	 */
	public boolean isFehlerhafteEntity(RtInternalIdentifier id, boolean partialIsUnknown) {
		if (fehlerhafteFelderIntern.contains(id)) {
			return true;
		}
		if (!erroneousGroups.contains(id)) {
			return false;
		}
		// We could return partialIsUnknown directly, but documentation is easier this way.
		if (partialIsUnknown) {
			// If one erroneous field makes the group erroneous, it is erroneous already.
			return true;
		}
		// This case should never be relevant: Either we have a negative language construct,
		// then partialIsUnknown should be true, or it is a positive language construct, then
		// the case, where all fields are erroneous is irrelevant, because then, no field is
		// filled anyway (the exception are preliminary rule errors, but these are corner cases).
		return false;
	}

	/**
	 * Liefert alle fehlerhaften Felder mit den externen USBs (d.h. USB==null
	 * für Felder von nicht USB-Vordrucken und dem synthetischen USB für Felder
	 * von USB-Vordrucken).
	 */
	public Set<IIdentifier> getAllFehlerhafteFelderExtern() {
		return fehlerhafteFelderExtern;
	}

	protected static class WerteContainer {
		private final String feldWertOberflaeche;
		private final String feldWertXML;

		protected WerteContainer(String feldWertOberflaeche, String feldWertXML) {
			this.feldWertOberflaeche = feldWertOberflaeche;
			this.feldWertXML = feldWertXML;
		}

		protected String getFeldWertOberflaeche() {
			return feldWertOberflaeche;
		}

		protected String getFeldWertXML() {
			return feldWertXML;
		}
	}

	public IData getDaten() {
		return data;
	}

	public void setDaten(IData data) {
		this.data = data;
	}

	public void addFeldFuerAlleUSBsRelevant(String feldName) {
		getFeldVerwendung(feldName).alleUSBsRelevant = true;
	}

	public boolean isFeldFuerAlleUSBsRelevant(String feldName, ValidierungArt valArt) {

		if (valArt.isVollvalidierung()) {
			return true;
		}
		FeldVerwendung usage = felderVerwendung.get(feldName);
		if (usage == null) {
			return false;
		}
		return usage.alleUSBsRelevant;
	}

	public ICustomCondition getCustomCondition(String name) {
		if (mode.customConditionFactory() != null) {
			return mode.customConditionFactory().createCustomConditionV2(name).orElse(null);
		} else {
			return null;
		}
	}

	public long getMaxInternerUSBMitAngegebenenFeldern() {
		return maxInternerUSBMitAngegebenenFeldern;
	}

	public USBIndexCache getUsbCache() {
		return usbCache;
	}

	/**
	 * Adds the field to the list of erroneous fields. Also marks containing groups as erroneous.
	 *
	 * @param internId
	 * 		the internal id of the field, that caused the error.
	 */
	public void markFieldAsInvalidForValidation(RtInternalIdentifier internId, IIdentifier externId) {
		if (fehlerhafteFelderIntern.add(internId)) {
			fehlerhafteFelderExtern.add(externId);
			addErroneousEntityToParent(internId);
			if (metaData.isIndexField(internId.getName())) {
				indexFieldCache.addInvalidIndexField(internId);
			}
		}
	}

	public void markFieldAsInvalidForValidation(RtInternalIdentifier internId) {
		markFieldAsInvalidForValidation(internId, convert2Extern(internId));
	}

	private void addErroneousEntityToParent(RtInternalIdentifier internId) {
		RtInternalIdentifier parentId = RtInternalIdentifier.createParentId(internId, metaData.getPfadName(internId.getName()));
		while (parentId != null && erroneousGroups.add(parentId)) {
			parentId = RtInternalIdentifier.createParentId(parentId, parentId.getName());
		}
	}

	/**
	 * Liefert die Anzahl der verschiedenen USBs, für die Felder ohne formale Fehler übergeben wurden.
	 * Wenn die Anwendung USB-Index-Felder verwendet, muss hier die Anzahl der verschiedenen XML-USBs zurückgegeben werden.
	 */
	public long getAnzahlAngegebeneUSB() {
		if (!entityAufUSBVordruckAngegeben) {//Felder von USB-Vordrucken nicht vorhanden
			return 0L;
		}
		if (metaData.hasUSBIndexField()) {
			return usbCache.getXMLZuIntern().size();
		}
		//keine USB-Index-Felder -> die Anzahl der verschiedenen USBs mit angegebenen Feldern
		//entspricht dem maximalen internen USB mit ang. Feldern, da diese fortlaufend sind und
		//es eine 1-1 Entsprechung zw. internen und externen USBs gibt
		return maxInternerUSBMitAngegebenenFeldern;

	}

	/**
	 * For the given identifier, mark all parent groups on the path to the root as relevant for
	 * validation.
	 */
	public void markAllParentGroupsRelevant(RtInternalIdentifier id, boolean isHModel) {
		String fullName = metaData.getPfadName(id.getName());
		int numLevels = EntityUtil.countHierarchyLevels(fullName);
		for (int i = 0; i < numLevels - 1; i++) {
			RtInternalIdentifier groupId = FeldUtil.getAncestorForLevel(id, fullName, i, isHModel);
			addPruefungsrelevanteEntityInternZumNamen(groupId);
		}
	}

	/**
	 * Konvertiere einen internen Identifier in einen externen.
	 * Dazu muss der UniqueName in den Pfadnamen geändert werden und
	 * der interne USB in dex synthetischen USB.
	 *
	 * @param idIntern
	 * 		zu konvertierende Identifier.
	 * @param useSemanticIndex
	 * 		bestimmt, ob gegebenenfalls ein RtExternalIdentifierWithSemanticIndex erstellt werden soll.
	 * @return externer Identifier
	 */
	public RtExternalIdentifier convert2Extern(RtInternalIdentifier idIntern, boolean useSemanticIndex) {
		String externName = metaData.getPfadName(idIntern.getName());
		RtInternalIdentifier resolvedId = resolveSemanticIndex(idIntern);
		long externUSB = metaData.hasUniqueEntities() ? usbCache.getExternUSB(resolvedId) : resolvedId.getUsb();
		SemanticIndexSpec semIndexSpec = idIntern.getSemanticIndex();
		boolean indicesContainM5 = Arrays.stream(resolvedId.getIndices()).anyMatch(i -> i == IIdentifier.UNKNOWN);
		if (useSemanticIndex && semIndexSpec != null && indicesContainM5) {
			String indexValue = getSemanticIndexValue(semIndexSpec);
			Pair<Integer, Long> replacement = getSemanticIndexReplacement(idIntern, indexValue);
			assert replacement.getEntry2() == IIdentifier.UNKNOWN :
					"Id " + resolvedId + " with -5 in indices but replacement doesn't assign -5: " + replacement;
			int semIdxLevel = replacement.getEntry1();
			RtExternalIdentifier indexField = createIndexField(externName, resolvedId.getIndices(), externUSB, semIdxLevel);
			Object indexValueAsBasicType = null;
			if (indexField != null && indexValue != null && !indexValue.isEmpty()) {
				indexValueAsBasicType = metaData.getMetaField(indexField.getName())
						.convertWithOnlyBasicTypeCheck(indexValue, null, new ResultBaseImpl());
			}
			return new RtExternalIdentifierWithSemanticIndex(externName, resolvedId.getIndices(), externUSB, indexField,
					indexValue, indexValueAsBasicType);
		}
		return new RtExternalIdentifier(externName, resolvedId.getIndices(), externUSB);
	}

	public RtExternalIdentifier convert2Extern(RtInternalIdentifier idIntern) {
		return convert2Extern(idIntern, false);
	}

	private RtExternalIdentifier createIndexField(
			String fullNameOfEntityWithSemIdx,
			long[] indicesOfEntityWithSemIdx,
			long usbOfEntityWithSemIdx,
			int levelOfSemIdx
			// pass level separately because it is not easy to identify in resolved/unresolved indices (-4? -5? -6? ?)
	) {
		assert usbOfEntityWithSemIdx == IIdentifier.USB_NOT_SPECIFIED :
				"We should never have semantic indices in old world (and therefore not with USB)";

		String groupPath = EntityUtil.getPathOfHierarchyLevel(fullNameOfEntityWithSemIdx, levelOfSemIdx);

		List<IMetaField> indexFields = metaData.getIndexFields(groupPath);
		if (indexFields == null || indexFields.isEmpty()) {
			assert false : "This should never happen if codegen checks are sufficient. But throwing unnecessary Exceptions in runtime isn't good.";
			return null;
		}
		assert indexFields.size() == 1 : "Index field for group " + groupPath + " not unique: " + indexFields;
		IMetaField indexField = indexFields.get(0);

		String indexFieldName = (String) indexField.getValue(IMetaKeys.FIELD_FULLNAME);
		long[] indices = Arrays.copyOfRange(indicesOfEntityWithSemIdx, 0, levelOfSemIdx + 2); // +1 for field, +1: level 0 -> 1
		indices[levelOfSemIdx + 1] = 1; // Field index always 1
		return new RtExternalIdentifier(indexFieldName, indices, usbOfEntityWithSemIdx);
	}

	/**
	 * For all index fields, set eligible instances to their default value.
	 * <p>
	 * In the case of partial validation, such index field ids are set as erroneous fields, to
	 * prevent additional error messages in comparison to full validation. If the index field would
	 * be set, value errors could occur that won't occur for full validation. If the index field
	 * would not be set, omission errors for the index field would occur that wouldn't occur in full
	 * validation if a default value is set there.
	 * <p>
	 * Note: The default value is the s-value given by the key {@link IMetaKeys#FIELD_S_VALUE}.
	 */
	public void addDefaultValuesForIndexFieldsIfNecessary() {
		for (String indexFieldFullName : metaData.getIndexFieldFullNames()) {
			addDefaultValuesForIndexFieldIfNecessary(indexFieldFullName);
		}
	}

	/**
	 * @param indexFieldFullName
	 * 		full name of the index field to fill with default values
	 */
	private void addDefaultValuesForIndexFieldIfNecessary(String indexFieldFullName) {
		assert !metaData.hasUniqueEntities();
		for (RtInternalIdentifier indexFieldId : getIndexFieldIdsEligibleForDefaultValueHandling(
				indexFieldFullName, groupFillCache, indexFieldCache, metaData)) {
			if (!pruefungsrelevanteEntitiesExtern.isEmpty()) {
				// in case of partly validation we set the index fields, which should be filled with their default value, as erroneous
				// this avoids the mandatory check, uniqueness check and evaluation of rules in which these fields are referenced.
				// This is necessary to avoid having additional errors in partial validation in comparison with full validation.
				markFieldAsInvalidForValidation(indexFieldId);
			} else {
				setIndexFieldToDefaultValue(indexFieldId);
			}
		}
	}

	private void setIndexFieldToDefaultValue(RtInternalIdentifier indexFieldId) {
		IFormatDefinition formatDef = metaData.getIFormatDefinition(indexFieldId.getName());
		// non-null because this is only called in `addDefaultValuesForIndexFieldIfNecessary`, where we require the svalue to be set
		String sValue = getSValue(indexFieldId.getName(), metaData);
		String uiValue = formatDef.convert(sValue, presentationInfo, true,
				RawParamHandler4Convert.of(new Object[] { mode.getLanguage(), mode.customFieldTypeFactory() }));
		addFeldToDatenCache(indexFieldId, uiValue, sValue);
	}

	/**
	 * Get the union of the value sets of (corresponding) index fields with respect to a concrete
	 * iteration.
	 * <p>
	 * For partial validation the returned list is empty if the index fields of the groups are not
	 * relevant for all repetitions.
	 *
	 * @param indexGroupFullNames
	 * 		full names of the groups, each containing an index field with the same name.
	 * 		each group's IRG must be on the error field path
	 * @param indexesWithUsb
	 * 		indices of the current iteration
	 * @return present index values for the current iteration or an empty list if the index fields
	 * 		are not relevant for all repetitions of the current iteration
	 */
	public List<String> getIterationValuesForGroups(String[] indexGroupFullNames, long[] indexesWithUsb) {
		var indexGroupFullNameToParentIndices = Arrays.stream(indexGroupFullNames).collect(
				Collectors.toMap(Function.identity(), indexGroupFullName -> getParentIndices(indexGroupFullName, indexesWithUsb)));
		return getIterationValuesForGroups(indexGroupFullNameToParentIndices);
	}

	protected List<String> getIterationValuesForGroups(Map<String, List<Long>> indexGroupFullNameToParentIndices) {
		Set<String> usedIndexValues = new TreeSet<>(); // sort w.r.t. index value
		indexGroupFullNameToParentIndices.forEach((indexGroupFullName, parentIndices) -> {
			Map<String, Long> iterationIndexes4Group = indexFieldCache.getIndexMappingForGroup(indexGroupFullName, parentIndices);
			if (iterationIndexes4Group != null) {
				usedIndexValues.addAll(iterationIndexes4Group.keySet());
			}
		});
		return new ArrayList<>(usedIndexValues);
	}

	/**
	 * @param groupName
	 * 		full name of the group. Might not necessarily be on the error field path, but the innermost repeatable group (IRG) above
	 * 		the group (if it exists) must be on the error field path.
	 * @param indexesWithUsb
	 * 		indexes of the current iteration w.r.t. the error field path
	 * @return parent indices of the group w.r.t. the current iteration , these contain 1 for non repeatable groups and the index of
	 * 		<code>indexes</code> for repeatable groups
	 * 		<p>
	 * 		For example, given a parallel iteration over:
	 * 		- `/Root[5]/A[5]/B[5]/IndexGroup1[5]/IndexField1`
	 * 		- `/Root[5]/X[1]/Y[1]/Z[1]/IndexGroup2[5]/IndexField2`
	 * 		<p>
	 * 		The function might receive:
	 * 		- `groupName` = `"/Root/X/Y/Z/IndexGroup2"`
	 * 		- `indexesWithUsb` = `[-2, 2, 3, 4, -6, 1]` (for an error field in `IndexGroup1`)
	 * 		<p>
	 * 		The result would be: `[2, 1, 1, 1]`
	 */
	private List<Long> getParentIndices(String groupName, long[] indexesWithUsb) {
		List<Long> maxIndices = metaData.getMaxGroupIndices(groupName);

		List<Long> parentIndices = new ArrayList<>(maxIndices.size() - 1);
		for (int i = 0; i < maxIndices.size() - 1; i++) {
			if (maxIndices.get(i) == 1) { // if maxIndices == 1, then the group can potentially be not on the error field path
				// (because we are already below the groups IRG) -> here we use index 1 instead of copying the
				// indexesWithUsb (which might be > 1, if the error field is additionally repeatable on lower levels)
				// See also the example in this method's javadoc.
				parentIndices.add(1L);
			} else {
				// If the max index is > 1, the group has to be on the error field path -> take the index from the current iteration
				parentIndices.add(indexesWithUsb[i + 1]);
			}
		}

		return parentIndices;
	}

	/**
	 * Precondition: the group has to contain exactly one index field.
	 *
	 * @param groupName
	 * 		full name of group
	 * @return unique name of the index field of the group
	 */
	private String getIndexFieldUniqueName(String groupName) {
		List<IMetaField> indexFields = metaData.getIndexFields(groupName);
		assert indexFields.size() == 1;
		IMetaField indexField = indexFields.get(0);

		return (String) indexField.getValue(IMetaKeysInternal.FELD_UNIQUENAME);
	}

	/**
	 * Returns an identifier for the corresponding index field of <code>id</code>, which has the
	 * same parent indices above the indexed level. The indexed level is identified by
	 * {@link IIdentifier#SEMANTIC_INDEX} in the indices of <code>id</code>.<br>
	 *
	 * <b>Precondition</b>: the indices of the id have to contain
	 * {@link IIdentifier#SEMANTIC_INDEX}.
	 */
	public RtInternalIdentifier getIndexFieldId(RtInternalIdentifier id) {
		String fullFieldName = metaData.getPfadName(id.getName());
		int indexedGroupLevel = ArrayUtils.indexOf(id.getIndices(), IIdentifier.SEMANTIC_INDEX);
		String indexedGroupName = EntityUtil.getPathOfHierarchyLevel(fullFieldName, indexedGroupLevel);
		String indexFieldUniqueName = getIndexFieldUniqueName(indexedGroupName);
		long[] indices = Arrays.copyOfRange(id.getIndices(), 0, indexedGroupLevel + 2);
		indices[indices.length - 1] = 1L; // the row is not repeatable in A12 models
		return new RtInternalIdentifier(indexFieldUniqueName, indices, IIdentifier.USB_NOT_SPECIFIED, null, id.getSemanticIndex(),
				id.getIterationState().orElse(null));
	}

	public boolean isIndexFieldInvalidForOneRepetition(RtInternalIdentifier indexFieldId) {
		return indexFieldCache.isIndexFieldInvalidForOneRepetition(indexFieldId);
	}

	public boolean isValidationPartWith3ValueLogic() {
		return isValidationPartWith3ValueLogic;
	}

	public void setValidationPartWith3ValueLogic() {
		isValidationPartWith3ValueLogic = true;
	}
}
