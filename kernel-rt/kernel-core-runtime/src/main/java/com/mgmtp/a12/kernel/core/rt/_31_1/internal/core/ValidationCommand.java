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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RuntimeUtils;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode.ValidationType;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult.ErrorType;

import lombok.NonNull;

public class ValidationCommand extends CheckCommand {

	public ValidationCommand(
			@NonNull IValidatorIntern validator,
			ValidationMode modeOrNull,
			@NonNull IMetaDataValidierung metaData,
			@NonNull IData data
	) {
		super(validator, modeOrNull, metaData, data);
	}

	/**
	 * Hauptmethode für die Teilvalidierung
	 * ====================================
	 * (1) Die in den prüfungsrelevanten Entitäten enthaltenen Gruppen werden zu prüfungsrelevanten
	 * Feldern expandiert.
	 * (2) Menge der prüfungsrelevanten Felder wird für die Behandlung des USB analysiert.
	 * (3) alle Prüfungsrelevanten Felder werden geprüft, ob sie formal korrekt sind. Fehlerhafte
	 * Felder werden aus der Menge der prüfungsrelevanten Felder entfernt.
	 * (4) Überprüfung, ob nach der formalen Prüfung weiter geprüft werden soll
	 * (5) Prüfungsrelevante Felder werden für die Prüfung verdichtet. Pro Feld gibt es nur einen
	 * Prüfungsaufruf - nicht pro Feldinstanz
	 * (6) pro prüfungsrelevantes Feld wird die zugehörige Check-Methode aufgerufen, wenn eine
	 * solche
	 * existiert. Dabei wird zur Optimierung lfdNummer und Zeile entweder auf 0 oder auf die
	 * spezifisch
	 * prüfungsrelevante lfdNummer / Zeile gesetzt.
	 *
	 * @param data
	 * 		Übergebene Daten zum validieren
	 * @param relevantEntities
	 * 		Menge der prüfungsrelevanten Entitäten (Gruppen und/oder Felder)
	 * @param ergebnisse
	 * 		hier werden die Fehler/Hinweis-Meldungen hinzugefügt.
	 */
	public void processPartlyValidation(
			@NonNull IData data,
			@NonNull Set<IIdentifier> relevantEntities,
			@NonNull IResult ergebnisse) {
		long startMesspunkt = LOG.startMesspunkt();
		if (relevantEntities.isEmpty()) {
			// nichts ist prüfungsrelevant,  Prüfung kann sofort enden

			// ACHTUNG: Die alte API erlaubt null fuer die Menge der relevanten Felder (vgl. IValidator).
			// Die neue API wirft dagegen bei null eine IllegalArgumentException (vgl. IERValidator).
			// Dieser Unterschied in den beiden process-Methoden ist also beabsichtigt.
			return;
		}

		this.validationCache.setDaten(data);

		processTVInternal(relevantEntities, ergebnisse);
		if (LOG.isTraceEnabled()) {
			LOG.logMessung("The partial validation of the new API took ", startMesspunkt);
		}
	}

	private void processTVInternal(@NonNull Set<IIdentifier> relevantEntities, @NonNull IResult ergebnisse) {
		validationCache.addAllPruefungsrelevanteEntitiesExtern(relevantEntities);
		Set<RtInternalIdentifier> relevantEntitiesInternWithSynthUsb = initialisiereRelevanteFelder(relevantEntities);
		// Überprüfe alle relevanten Felder.  Fehlerhafte Felder werden aus den relevanten Feldern
		// gelöscht, da bei Formatfehlern nicht mehr weiter geprüft wird.
		formalePruefung(relevantEntitiesInternWithSynthUsb, ergebnisse);

		// falls nicht weiter geprüft werden muss,  kann direkt aufgehört werden.
		if (!pruefeWeiter(ergebnisse)) {
			return;
		}

		Set<RtInternalIdentifier> relevantEntitiesInternWithInternUsb = metaData.hasUniqueEntities()
				? relevantEntitiesInternWithSynthUsb.stream().map(this::convertSynthUsbToInternUsb).collect(Collectors.toSet())
				: relevantEntitiesInternWithSynthUsb;
		befuelleCacheMitRelevantenFeldern(relevantEntitiesInternWithInternUsb);

		Map<String, RtInternalIdentifier> pruefungsFelderMap = compressRelevantFieldsForValidationCall(
				relevantEntitiesInternWithInternUsb, true);

		/*
		 * für alle relevanten Felder werden die Check-Methoden aufgerufen.
		 * Die generierte Klasse hat für die jeweiligen Felder zusammengefasste Regeln
		 * mit dem Namenschema check__<feldName>
		 * Diese Methoden haben MainValidatorController, lfdNummer und Zeile als Parameter.
		 */
		MainValidatorController controller = new MainValidatorController(
				ergebnisse, validationCache, modus, metaData, formalChecker);
		controller.setIsValidationPartWith3ValueLogic();
		validator.validiereTeil(controller, modus, pruefungsFelderMap);
	}

	/**
	 * Methode wird bei der Teilvalidierung aufgerufen und setzt die Information zu den
	 * prüfungsrelevanten Feldern
	 */
	private void befuelleCacheMitRelevantenFeldern(@NonNull Set<RtInternalIdentifier> relevanteFelder) {
		validationCache.addAllRelevantEntitiesIntern(relevanteFelder);
		if (modelMaxUsb != -1) {// es gibt USB Vordrucke
			setzeUSBOptimierung(relevanteFelder);
		}
	}

	/**
	 * Initialisiert die Information, ob für alle USBs ein relevantes Feld existiert.
	 * Dies kann sein, wenn entweder: der USB für ein Feld auf 0 gesetzt ist
	 * oder
	 * alle USBs explizit angegeben wurden.
	 */
	private void setzeUSBOptimierung(@NonNull Set<RtInternalIdentifier> relevanteFelder) {
		validationCache.setNichtRelevanterUSBVorhanden(false); //Standardfall, alle USBS sind relevant
		Set<Long> usbs = new HashSet<>();
		for (RtInternalIdentifier id : relevanteFelder) {
			if (id.getUsb() == 0) {
				//ein Feld mit allen USBs relevant vorhanden,  wir können aufhören.
				return;
			}
			usbs.add(id.getUsb());
		}

		//Nicht alle USBs sind relevant:  setze die Information im validation Cache entsprechend
		if (usbs.size() != modelMaxUsb) {
			validationCache.setNichtRelevanterUSBVorhanden(true);
		}
	}

	private static class GroupLengthComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			int result = Integer.compare(o2.length(), o1.length());
			if (result != 0) {
				// longer first
				return result;
			}
			return o1.compareTo(o2);
		}
	}

	/**
	 * Compress the relevant Fields for the call of the validation methods.
	 * Validation methods are connected to the field name. E.g. if a field is relevant for multiple
	 * rows, the validation method should be called only once.
	 *
	 * @param relevantFields
	 * 		fields relevant for validation
	 * @param changeCache
	 * 		specifies, whether the {@link ValidationCache} should be changed.
	 * @return compressed information for the relevant fields
	 */
	private Map<String, RtInternalIdentifier> compressRelevantFieldsForValidationCall(
			Set<RtInternalIdentifier> relevantFields, boolean changeCache) {
		Map<String, RtInternalIdentifier> result = new HashMap<>();
		boolean isHModel = metaData.hasUniqueEntities();
		// We need to have the groups in reverse length order to accumulate the child groups before the parent groups
		SortedMap<String, Map<RtInternalIdentifier, Set<RtInternalIdentifier>>> relevantGroups = new TreeMap<>(
				new GroupLengthComparator());
		for (RtInternalIdentifier id : relevantFields) {
			String name = id.getName();
			if (changeCache) {
				validationCache.addPruefungsrelevanteEntityInternZumNamen(id);
				validationCache.markAllParentGroupsRelevant(id, isHModel);
				if (!isHModel) {
					RtInternalIdentifier parentIdentifier = RtInternalIdentifier.createParentId(id,
							metaData.getPfadName(id.getName()));
					addToMultiMapMultiSetHashMap(relevantGroups, parentIdentifier.getName(), parentIdentifier, id);
				}
			}

			RtInternalIdentifier resultId = result.get(name);
			if (resultId != null) { // already found
				for (int i = 0; i < resultId.getIndices().length; i++) {
					if (resultId.getIndices()[i] != id.getIndices()[i]) {
						resultId.getIndices()[i] = 0;
					}
				}
				if (resultId.getUsb() != id.getUsb()) {
					resultId.setUsb(0);
				}
			} else {// not found yet, create new entry
				resultId = new RtInternalIdentifier(id.getName(), id.getIndices().clone(), id.getUsb());
				result.put(resultId.getName(), resultId);
			}
		}
		validationCache.reduceRedundanciesInRelevantEntities();

		if (changeCache && !isHModel) {
			// For H models, we still have the expansion as before and need no fully relevant groups
			setupFullyRelevantGroups(relevantGroups);
		}
		return result;
	}

	/**
	 * The algorithm is somewhat complicated and is documented in the internal documentation
	 * in the document svn/documents/interne_doku/Bestimmung vollständig relevanter Kontexte.docx.
	 *
	 * @param relevantGroups
	 */
	private void setupFullyRelevantGroups(SortedMap<String, Map<RtInternalIdentifier, Set<RtInternalIdentifier>>> relevantGroups) {
		Map<String, Set<RtInternalIdentifier>> fullyRelevantGroupsByName = new HashMap<>();
		while (!relevantGroups.isEmpty()) {
			String groupName = relevantGroups.firstKey();
			Map<RtInternalIdentifier, Set<RtInternalIdentifier>> value = relevantGroups.remove(groupName);
			Set<RtInternalIdentifier> fullyRelevantGroups = extractFullyRelevantGroups(groupName, value);
			fullyRelevantGroups = compress(groupName, fullyRelevantGroups);
			if (!fullyRelevantGroups.isEmpty()) {
				fullyRelevantGroupsByName.put(groupName, fullyRelevantGroups);
				Set<RtInternalIdentifier> totallyRelevantGroups = filterTotallyRelevantGroups(fullyRelevantGroups);
				for (RtInternalIdentifier id : totallyRelevantGroups) {
					RtInternalIdentifier parentId = RtInternalIdentifier.createParentId(id, metaData.getPfadName(id.getName()));
					if (parentId != null) {
						addToMultiMapMultiSetHashMap(relevantGroups, parentId.getName(), parentId, id);
					}
				}
			}
		}
		fullyRelevantGroupsByName.values().forEach(ValidationCache::reduceRedundancies);
		validationCache.setFullyRelevantGroups(fullyRelevantGroupsByName);
	}

	private Set<RtInternalIdentifier> filterTotallyRelevantGroups(Set<RtInternalIdentifier> fullyRelevantGroups) {
		HashSet<RtInternalIdentifier> result = new HashSet<>();
		long rep = getRepeatability(fullyRelevantGroups.iterator().next().getName());
		for (RtInternalIdentifier fullyRelevantGroup : fullyRelevantGroups) {
			long[] indices = fullyRelevantGroup.getIndices();
			if (indices[indices.length - 1] == IIdentifier.ALL_INDICES || (rep == 1 && indices[indices.length - 1] == 1)) {
				result.add(fullyRelevantGroup);
			}
		}
		return result;
	}

	/**
	 * Compress the lowest level of the indices of the given identifiers to
	 * {@link IIdentifier#ALL_INDICES} if possible.
	 *
	 * @param groupName
	 * 		the name of the group.
	 * @param fullyRelevantGroups
	 * 		the uncompressed relevant groups.
	 * @return
	 */
	private Set<RtInternalIdentifier> compress(String groupName, Set<RtInternalIdentifier> fullyRelevantGroups) {
		if (fullyRelevantGroups.isEmpty() || fullyRelevantGroups.size() == 1) {
			return fullyRelevantGroups;
		}
		long rep = getRepeatability(groupName);
		if (rep == 1) {
			// Non-repeatable groups cannot be compressed
			return fullyRelevantGroups;
		}
		HashSet<RtInternalIdentifier> result = new HashSet<>();
		// Calculate the available relevant groups for one iteration on the upper levels
		HashMap<List<Long>, Set<RtInternalIdentifier>> groupsForIteration = new HashMap<>();
		for (RtInternalIdentifier relevantGroup : fullyRelevantGroups) {
			long[] indices = relevantGroup.getIndices();
			long currentIndex = indices[indices.length - 1];
			if (currentIndex == IIdentifier.ALL_INDICES) {
				result.add(relevantGroup);
				continue;
			}
			if (currentIndex > rep) {
				// a repetition bigger than the repeatability is not allowed, so we ignore it
				continue;
			}
			List<Long> parentIndices = RuntimeUtils.createListForSubArray(indices, 0, indices.length - 1);
			addToMultiSetHashMap(groupsForIteration, parentIndices, relevantGroup);
		}
		for (Set<RtInternalIdentifier> groups : groupsForIteration.values()) {
			if (groups.size() < rep) {
				result.addAll(groups);
				continue;
			}
			// Now we know, that we have >= rep different groups in the set,
			// which differ only by the last index, where the last index is between 1 and rep,
			// so we can add it with a 0
			RtInternalIdentifier item = groups.iterator().next();
			long[] indices = item.getIndices().clone();
			indices[indices.length - 1] = 0;
			RtInternalIdentifier addId = new RtInternalIdentifier(item.getName(), indices);
			result.add(addId);
		}
		return result;
	}

	private long getRepeatability(String groupName) {
		List<Long> max = metaData.getMaxGroupIndices(groupName);
		return max.get(max.size() - 1);
	}

	/**
	 * The algorithm is somewhat complicated and is documented in the internal documentation
	 * in the document svn/documents/interne_doku/Bestimmung vollständig relevanter Kontexte.docx.
	 * <p>
	 * ## The 'totally relevant'- and 'fully relevant'-attributes are assigned per fixed repetition
	 * of the considered entity's ancestors.
	 * ## Fields are trivially totally relevant in A12-Models, if they are relevant.
	 * ## A group is fully relevant, if all child entities of the group are totally relevant.
	 * ## A group is totally relevant, if all of its repetitions are fully relevant.
	 */
	private Set<RtInternalIdentifier> extractFullyRelevantGroups(
			String groupName,
			Map<RtInternalIdentifier, Set<RtInternalIdentifier>> candidateGroups) {
		Set<String> containedElementNames = metaData.getContainedElements(groupName);
		Set<RtInternalIdentifier> result = new HashSet<>();

		for (Entry<RtInternalIdentifier, Set<RtInternalIdentifier>> entry : candidateGroups.entrySet()) {
			Set<String> missingElements = new HashSet<>(containedElementNames);
			for (RtInternalIdentifier relevant : entry.getValue()) {
				missingElements.remove(relevant.getName());
				if (missingElements.isEmpty()) {
					// We found all sub-elements, so this candidate is fully relevant
					result.add(entry.getKey());
					break;
				}
			}
		}
		for (RtInternalIdentifier toRemove : result) {
			// We need no further investigation for these candidates
			// The other candidates cannot gain beyond this identifier, even if they overlap with it
			candidateGroups.remove(toRemove);
		}

		if (candidateGroups.isEmpty()) {
			// Did we find all candidates already => finished
			return result;
		}

		// Now we do the heavy work: For all the candidates, we make an intersection with all the containedElement identifiers by name.
		// e.g. containedFieldNames = { A, B, C }
		// candidates: { <G, [1,0,1]>: {<A, [1,0,1,1]>, <B, [1,0,1,1]>}, <G, [1,1,0]>: {<A, [1,1,0,1]>, <C, [1,1,0,1]>} , <G, [2,0,0]>: {<C, [2,0,0,1]>}}
		// We intersect the indices of { <G, [1,0,1]>, <G, [1,1,0]>, <G, [2,0,0]> } with the relevant fields for each contained ElementName.
		// So we have three steps:
		// 1. intersect { <G, [1,0,1]>, <G, [1,1,0]>, <G, [2,0,0]> } with {<A, [1,0,1,1]>, <A, [1,1,0,1]>} giving { <G, [1,0,1]>, <G, [1,1,0]> }.
		// 2. intersect { <G, [1,0,1]>, <G, [1,1,0]> } with {<B, [1,0,1,1]>} giving { <G, [1,0,1]>, <G, [1,1,1]> }
		// 3. intersect { <G, [1,0,1]>, <G, [1,1,1]> } with {<C, [1,1,0,1]>, <C, [2,0,0,1]>} giving { <G, [1,1,1]> }
		// Then we know that only <G, [1,1,1]> is fully relevant.
		Set<RtInternalIdentifier> candidates = candidateGroups.keySet();
		Map<String, Set<RtInternalIdentifier>> totallyRelevantsByName = new HashMap<>();

		// Build the map of totally relevant entities by name
		for (Set<RtInternalIdentifier> relevantEntities : candidateGroups.values()) {
			for (RtInternalIdentifier relEntity : relevantEntities) {
				addToMultiSetHashMap(totallyRelevantsByName, relEntity.getName(), relEntity);
			}
		}

		containedElementNames.removeAll(totallyRelevantsByName.keySet());
		if (!containedElementNames.isEmpty()) {
			// Some field or direct sub-group of all the candidates is missing the totally relevant attribute
			// So, no further fully relevant groups can be found
			return result;
		}

		// Do the intersection for each name
		for (Set<RtInternalIdentifier> totallyRelevantsPerName : totallyRelevantsByName.values()) {
			candidates = intersectIdentifierIndexSets(candidates, totallyRelevantsPerName);
		}

		result.addAll(candidates);
		return result;
	}

	private Set<RtInternalIdentifier> intersectIdentifierIndexSets(Set<RtInternalIdentifier> candidates,
			Set<RtInternalIdentifier> totallyRelevants) {
		Set<RtInternalIdentifier> result = new HashSet<>();
		for (RtInternalIdentifier candidate : candidates) {
			for (RtInternalIdentifier relevant : totallyRelevants) {
				RtInternalIdentifier cand = candidate.intersect(relevant);
				if (cand != null) {
					result.add(cand);
				}
			}
		}
		return result;
	}

	public static <T, U> boolean addToMultiSetHashMap(Map<T, Set<U>> map, T key, U value) {
		return map.computeIfAbsent(key, s -> new HashSet<U>()).add(value);
	}

	public static <S, T, U> boolean addToMultiMapMultiSetHashMap(Map<S, Map<T, Set<U>>> map, S key0, T key1, U value) {
		Map<T, Set<U>> innerMap = map.computeIfAbsent(key0, k -> new HashMap<>());
		return innerMap.computeIfAbsent(key1, k -> new HashSet<>()).add(value);
	}

	/**
	 * Alle an der Schnittstelle übergebenen relevanten Kontexte werden expandiert.
	 * Für alle relevanten Felder wird die Rolle des USB geprüft. Ist der USB null, so wird für die
	 * Ergebnisliste der USB auf 0 gesetzt (entspricht prüfe Alle)
	 * Abhängig vom USB wird die Optimierungsinformation des validation Cache gesetzt.
	 *
	 * @param relevantEntities
	 * 		prüfungsrelevante Entitäten
	 * @return Menge der relevanten Felder, usb == null wird auf usb == 0 konvertiert, relevante
	 * 		Kontexte werden vorher expandiert
	 */
	private Set<RtInternalIdentifier> initialisiereRelevanteFelder(Set<IIdentifier> relevantEntities) {
		Set<RtInternalIdentifier> result = new HashSet<>();
		for (IIdentifier id : relevantEntities) { //in den relevanten Entities ersetzen wir für den USB null durch 0L
			boolean isField = metaData.isFeldDefiniert(id.getName());
			if (!isField && !metaData.isGroupDefined(id.getName())) {
				validationCache.addUnbekanntesFeld(id);
				continue;
			}
			// Umschlüsselung externer Name auf interner Name
			RtInternalIdentifier internId = EntityUtil.makeInternIdentifier(id, metaData);

			if (isField) {
				result.add(internId);
				if (id.getUsb() == 0) {
					validationCache.addFeldFuerAlleUSBsRelevant(internId.getName());
				}
			} else {
				Set<RtInternalIdentifier> expandedIds = new HashSet<>();
				expandedIds.addAll(expandRelevantGroup(internId));
				for (RtInternalIdentifier expandedId : expandedIds) {
					result.add(expandedId);
					if (id.getUsb() == 0) {
						validationCache.addFeldFuerAlleUSBsRelevant(expandedId.getName());
					}
				}
			}

		}
		return result;
	}

	private Set<RtInternalIdentifier> expandRelevantGroup(RtInternalIdentifier groupId) {
		Set<RtInternalIdentifier> expandedIds = new HashSet<>();
		long[] expandedIndices;
		if (metaData.hasUniqueEntities()) {
			expandedIndices = Arrays.copyOf(groupId.getIndices(), 2);
		} else {
			expandedIndices = Arrays.copyOf(groupId.getIndices(), groupId.getIndices().length + 1);
		}

		Set<String> fieldNames = metaData.getContainedFields(groupId.getName());
		for (String fieldName : fieldNames) {
			expandedIds.add(new RtInternalIdentifier(fieldName, expandedIndices, groupId.getUsb()));
		}

		Set<String> groupNames = metaData.getContainedGroups(groupId.getName());
		for (String groupName : groupNames) {
			expandedIds.addAll(expandRelevantGroup(new RtInternalIdentifier(groupName, expandedIndices, groupId.getUsb())));
		}
		return expandedIds;
	}

	/**
	 * Main method for full validation.
	 *
	 * @param data
	 * 		the data to be validated. May not be <code>null</code>.
	 * @param validationResult
	 * 		container object for resulting messages on erroneous
	 * 		plausibility checks. May not
	 * 		be <code>null</code>.
	 */
	public void processFullValidation(
			IData data,
			IResult validationResult) {
		long startMesspunkt = LOG.startMesspunkt();
		this.validationCache.setDaten(data);
		processInternalVV(validationResult);
		if (LOG.isTraceEnabled()) {
			LOG.logMessung("The validation of the new API took ", startMesspunkt);
		}
	}

	private void processInternalVV(IResult ergebnisse) {
		formalePruefung(null, ergebnisse);
		// Überprüfe, ob nach dem Validierungsmodus jetzt aufgehört werden muss.
		if (!pruefeWeiter(ergebnisse)) {
			return;
		}

		MainValidatorController controller = new MainValidatorController(
				ergebnisse, validationCache, modus, metaData, formalChecker);

		validator.validatePreliminaryRules(controller);
		validator.validiereVoll(controller, modus);
	}

	/**
	 * Prüfungsroutine zum Abprüfen, ob nach der formalen Prüfung weiter geprüft
	 * wird.
	 *
	 * @param ergebnisse
	 * 		die bereits gefunden wurden
	 * @return true oder false, je nachdem ob weiter geprüft werden soll.
	 */
	private boolean pruefeWeiter(IResult ergebnisse) {

		// Nur formale Pruefung
		if (modus.getValidationType() == ValidationType.onlyFormalNoRequiredFieldCheck) {
			//Ende der Plausibilitätsprüfung, da nur formale Pruefung
			return false;
		}
		// Abbruch nach Fehlern in formaler Prüfung und Fehler gefunden
		if (modus.getValidationType() == ValidationType.fullExaminationWithAbortOnFormalErrors &&
				ergebnisse.getCountOf(ErrorType.VALUE_ERROR) > 0) {
			//Ende der Plausibilitätsprüfung, da Fehlern in der syntaktischen Prüfung
			return false;
		}

		return true;
	}
}
