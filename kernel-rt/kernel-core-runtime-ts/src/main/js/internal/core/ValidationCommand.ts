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

import { ComparativeSortedMap } from "@com.mgmtp.a12.utils/utils-collections";

import type { IData, IResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import {
	ErrorType,
	IIdentifier,
	ValidationType
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type {
	IEquatable,
	IHashCode
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import {
	ArrayMap,
	HashMap,
	HashSet
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { FeldUtil } from "../util/FeldUtil.js";
import { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";

import { CheckCommand } from "./CheckCommand.js";
import { MainValidatorController } from "./MainValidatorController.js";
import { ValidationCache } from "./ValidationCache.js";

export class ValidationCommand extends CheckCommand {
	/**
	 * <b>Hauptmethode für die Teilvalidierung:</b><br>
	 * (1) Die in den prüfungsrelevanten Entitäten enthaltenen Gruppen werden zu prüfungsrelevanten
	 * Feldern expandiert.<br>
	 * (2) alle Prüfungsrelevanten Felder werden geprüft, ob sie formal korrekt sind. Fehlerhafte
	 * Felder werden aus der Menge der prüfungsrelevanten Felder entfernt.<br>
	 * (3) Überprüfung, ob nach der formalen Prüfung weiter geprüft werden soll<br>
	 * (4) Prüfungsrelevante Felder werden für die Prüfung verdichtet. Pro Feld gibt es nur einen
	 * Prüfungsaufruf - nicht pro Feldinstanz<br>
	 * (5) pro prüfungsrelevantes Feld wird die zugehörige Check-Methode aufgerufen, wenn eine
	 * solche existiert. Dabei wird zur Optimierung die Zeile auf 1 und die lfdNummer entweder auf 0 oder auf die
	 * spezifisch prüfungsrelevante lfdNummer gesetzt.
	 *
	 * @param data 	Übergebene Daten zum validieren
	 * @param relevantEntities 	Menge der prüfungsrelevanten Entitäten (Gruppen und/oder Felder)
	 * @param ergebnisse 	hier werden die Fehler/Hinweis-Meldungen hinzugefügt.
	 */
	public processPartlyValidation(
		data: IData,
		relevantEntities: Set<IIdentifier>,
		ergebnisse: IResult
	): void {
		const startMesspunkt = this._logger.startMesspunkt();
		if (relevantEntities === undefined) {
			throw new Error("Relevant field must not be undefined.");
		}
		if (relevantEntities.size <= 0) {
			// nichts ist prüfungsrelevant,  Prüfung kann sofort enden
			return;
		}
		this._validationCache.setDaten(data);
		this._processTVInternal(relevantEntities, ergebnisse);
		this._logger.logMessung("The partial validation of the new API took ", startMesspunkt);
	}

	private _processTVInternal(relevantEntities: Set<IIdentifier>, ergebnisse: IResult): void {
		this._validationCache.addAllPruefungsrelevanteEntitiesExtern(relevantEntities);
		const relevanteFelderIntern: HashSet<RtInternalIdentifier> =
			this._initialisiereRelevanteFelder(relevantEntities);
		// Überprüfe alle relevanten Felder.  Fehlerhafte Felder werden aus den relevanten Feldern
		// gelöscht, da bei Formatfehlern nicht mehr weiter geprüft wird.
		this.formalePruefung(relevanteFelderIntern, ergebnisse);

		// falls nicht weiter geprüft werden muss,  kann direkt aufgehört werden.
		if (!this._pruefeWeiter(ergebnisse)) {
			return;
		}

		this._befuelleCacheMitRelevantenFeldern(relevanteFelderIntern);

		const pruefungsFelderMap: Map<string, RtInternalIdentifier> =
			this._compressRelevantFieldsForValidationCall(relevanteFelderIntern, true);

		/*
		 * für alle relevanten Felder werden die Check-Methoden aufgerufen.
		 * Die generierte Klasse hat für die jeweiligen Felder zusammengefasste Regeln
		 * mit dem Namenschema check__<feldName>
		 * Diese Methoden haben MainValidatorController, lfdNummer und Zeile als Parameter.
		 */
		const controller: MainValidatorController = new MainValidatorController(
			ergebnisse,
			this._validationCache,
			this._logger,
			this._modus,
			this._metaData,
			this._formalChecker,
		);
		controller.setIsValidationPartWith3ValueLogic();
		this._validator.validiereTeil(controller, this._modus, pruefungsFelderMap);
	}

	/**
	 * Methode wird bei der Teilvalidierung aufgerufen und setzt die Information zu den
	 * prüfungsrelevanten Feldern
	 */
	private _befuelleCacheMitRelevantenFeldern(relevanteFelder: HashSet<RtInternalIdentifier>): void {
		this._validationCache.addAllRelevantEntitiesIntern(relevanteFelder);
	}

	private _groupLengthComparator(a: string, b: string) {
		if (a.length > b.length) {
			return -1;
		} else if (a.length < b.length) {
			return 1;
		} else if (a < b) {
			return -1;
		} else if (a > b) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * Compress the relevant Fields for the call of the validation methods.
	 * Validation methods are connected to the field name. E.g. if a field is relevant for multiple
	 * rows, the validation method should be called only once.
	 *
	 * @param relevantFields 	fields relevant for validation
	 * @param changeCache 	specifies, whether the {@link ValidationCache} should be changed.
	 * @return compressed information for the relevant fields
	 */
	private _compressRelevantFieldsForValidationCall(
		relevantFields: HashSet<RtInternalIdentifier>,
		changeCache: boolean
	): Map<string, RtInternalIdentifier> {
		const result: Map<string, RtInternalIdentifier> = new Map<string, RtInternalIdentifier>();
		// We need to have the groups in reverse length order to accumulate the child groups before the parent groups
		const relevantGroups: ComparativeSortedMap<
			string,
			HashMap<RtInternalIdentifier, HashSet<RtInternalIdentifier>>
		> = new ComparativeSortedMap(this._groupLengthComparator);
		for (const id of relevantFields) {
			const name: string = id.getName();
			if (changeCache) {
				this._validationCache.addPruefungsrelevantesFeldInternZumNamen(id);
				this._validationCache.markAllParentGroupsRelevant(id);
				// This can never be null, because id is a field and never a root group
				// groups are expanded beforehand
				// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
				const parentIdentifier = RtInternalIdentifier.createParentId(
					id,
					this._metaData.getPfadName(id.getName())
				)!;
				ValidationCommand.addToMultiMapMultiSetHashMap(
					relevantGroups as any,
					parentIdentifier.getName(),
					parentIdentifier,
					id
				);
			}

			let resultId: RtInternalIdentifier | undefined = result.get(name);
			if (resultId != undefined) {
				// already found
				for (let i = 0; i < resultId.getIndices().length; i++) {
					if (resultId.getIndices()[i] !== id.getIndices()[i]) {
						resultId.getIndices()[i] = 0;
					}
				}
			} else {
				// not found yet, create new entry
				resultId = new RtInternalIdentifier(id.getName(), id.getIndices().slice(0));
				result.set(resultId.getName(), resultId);
			}
		}
		this._validationCache.reduceRedundanciesInRelevantEntities()
		if (changeCache) {
			this._setupFullyRelevantGroups(relevantGroups);
		}

		return result;
	}

	private _setupFullyRelevantGroups(
		relevantGroups: ComparativeSortedMap<
			string,
			HashMap<RtInternalIdentifier, HashSet<RtInternalIdentifier>>
		>
	): void {
		const fullyRelevantGroupsByName: Map<string, Set<RtInternalIdentifier>> = new Map();
		while (!relevantGroups.isEmpty) {
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			const groupName: string = relevantGroups.keys().next().value!;
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			const value = relevantGroups.deleteAndRetrieve(groupName)!;
			let fullyRelevantGroups = this._extractFullyRelevantGroups(groupName, value as any);
			fullyRelevantGroups = this._compress(groupName, fullyRelevantGroups);
			if (!fullyRelevantGroups.isEmpty) {
				fullyRelevantGroupsByName.set(groupName, fullyRelevantGroups as any);
				const totallyRelevantGroups: HashSet<RtInternalIdentifier> =
					this._filterTotallyRelevantGroups(fullyRelevantGroups);
				totallyRelevantGroups.forEach((id: RtInternalIdentifier) => {
					const parentId = RtInternalIdentifier.createParentId(
						id,
						this._metaData.getPfadName(id.getName())
					);
					if (parentId != null) {
						ValidationCommand.addToMultiMapMultiSetHashMap(
							relevantGroups as any,
							parentId.getName(),
							parentId,
							id
						);
					}
				});
			}
			fullyRelevantGroupsByName.forEach(ids => ValidationCache.reduceRedundancies(ids))
			this._validationCache.setFullyRelevantGroups(fullyRelevantGroupsByName);
		}
	}

	private _filterTotallyRelevantGroups(
		fullyRelevantGroups: HashSet<RtInternalIdentifier>
	): HashSet<RtInternalIdentifier> {
		const result = new HashSet<RtInternalIdentifier>();

		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const rep = this._getRepeatability(fullyRelevantGroups.values().next().value!.getName());
		fullyRelevantGroups.forEach(id => {
			const indices = id.getIndices();
			if (
				indices[indices.length - 1] === IIdentifier.ALL_INDICES ||
				(rep === 1 && indices[indices.length - 1] === 1)
			) {
				result.add(id);
			}
		});
		return result;
	}

	/**
	 * Compress the lowest level of the indices of the given identifiers to
	 * [IIdentifier.ALL_INDICES](../../kernel-core-runtime-api-ts/modules/
	 * _validation_iidentifier_.iidentifier.html#all_indices) if possible.
	 *
	 * @param groupName 	the name of the group.
	 * @param fullyRelevantGroups 	the uncompressed relevant groups.
	 * @return
	 */
	private _compress(
		groupName: string,
		fullyRelevantGroups: HashSet<RtInternalIdentifier>
	): HashSet<RtInternalIdentifier> {
		if (fullyRelevantGroups.isEmpty || fullyRelevantGroups.size === 1) {
			return fullyRelevantGroups;
		}
		const rep = this._getRepeatability(groupName);
		if (rep === 1) {
			// Non-repeatable groups cannot be compressed
			return fullyRelevantGroups;
		}
		const result: HashSet<RtInternalIdentifier> = new HashSet();
		// Calculate the available relevant groups for one iteration on the upper levels
		const groupsForIteration: ArrayMap<number[], Set<RtInternalIdentifier>> = new ArrayMap();
		fullyRelevantGroups.forEach(relevantGroup => {
			const indices: number[] = relevantGroup.getIndices();
			const currentIndex: number = indices[indices.length - 1];
			if (currentIndex === IIdentifier.ALL_INDICES) {
				result.add(relevantGroup);
				return;
			}
			if (currentIndex > rep) {
				// a repetition bigger than the repeatability is not allowed, so we ignore it
				return;
			}
			const parentIndices: number[] = indices.slice(0, indices.length - 1);
			ValidationCommand.addToMultiSetHashMap(groupsForIteration, parentIndices, relevantGroup);
		});
		groupsForIteration.forEach(groups => {
			if (groups.size < rep) {
				groups.forEach(g => result.add(g));
				return;
			}
			// Now we know, that we have >= rep different groups in the set,
			// which differ only by the last index, where the last index is between 1 and rep,
			// so we can add it with a 0
			const item: RtInternalIdentifier | undefined = groups.values().next().value;
			if (item != undefined) { // cannot really happen, only for compilation reasons
				const indices: number[] = item.getIndices().slice();
				indices[indices.length - 1] = 0;
				const addId = new RtInternalIdentifier(item.getName(), indices);
				result.add(addId);
			}
		});
		return result;
	}

	private _getRepeatability(groupName: string): number {
		const max = this._metaData.getMaxGroupIndices(groupName);
		return max[max.length - 1];
	}

	/**
	 * The algorithm is somewhat complicated and is documented in the internal documentation
	 * in the document svn/documents/interne_doku/Bestimmung vollständig relevanter Kontexte.docx.
	 * <ul>
	 * <li>The 'totally relevant'- and 'fully relevant'-attributes are assigned per fixed repetition
	 * of the considered entity's ancestors.</li>
	 * <li>Fields are trivially totally relevant in A12-Models, if they are relevant.</li>
	 * <li>A group is fully relevant, if all child entities of the group are totally relevant.</li>
	 * <li>A group is totally relevant, if all of its repetitions are fully relevant.</li>
	 * </ul>
	 */
	private _extractFullyRelevantGroups(
		groupName: string,
		candidateGroups: HashMap<RtInternalIdentifier, Set<RtInternalIdentifier>>
	): HashSet<RtInternalIdentifier> {
		const containedElements: Set<string> = this._metaData.getContainedElements(groupName);
		const result = new HashSet<RtInternalIdentifier>();

		candidateGroups.forEach((value: Set<RtInternalIdentifier>, key: RtInternalIdentifier) => {
			const missingElements = new Set<string>();
			containedElements.forEach(e => missingElements.add(e));

			for (const relevant of value) {
				missingElements.delete(relevant.getName());
				if (missingElements.size === 0) {
					// We found all sub-elements, so this candidate is fully relevant
					result.add(key);
					break;
				}
			}
		});

		// We need no further investigation for these candidates
		// The other candidates cannot gain beyond this identifier, even if they overlap with it
		result.forEach(e => candidateGroups.delete(e));

		if (candidateGroups.isEmpty) {
			// Did we find all candidates already => finished
			return result;
		}

		// Now we do the heavy work: For all the candidates, we make an intersection with all the containedElement
		// identifiers by name.
		// e.g. containedFieldNames = { A, B, C }
		// candidates: { <G, [1,0,1]>: {<A, [1,0,1,1]>, <B, [1,0,1,1]>},
		// <G, [1,1,0]>: {<A, [1,1,0,1]>, <C, [1,1,0,1]>} , <G, [2,0,0]>: {<C, [2,0,0,1]>}}
		// We intersect the indices of { <G, [1,0,1]>, <G, [1,1,0]>, <G, [2,0,0]> } with the relevant fields
		// for each contained ElementName.
		// So we have three steps:
		// 1. intersect { <G, [1,0,1]>, <G, [1,1,0]>, <G, [2,0,0]> } with {<A, [1,0,1,1]>, <A, [1,1,0,1]>}
		//   giving { <G, [1,0,1]>, <G, [1,1,0]> }.
		// 2. intersect { <G, [1,0,1]>, <G, [1,1,0]> } with {<B, [1,0,1,1]>} giving { <G, [1,0,1]>, <G, [1,1,1]> }
		// 3. intersect { <G, [1,0,1]>, <G, [1,1,1]> } with {<C, [1,1,0,1]>, <C, [2,0,0,1]>} giving { <G, [1,1,1]> }
		// Then we know that only <G, [1,1,1]> is fully relevant.
		let candidates: Set<RtInternalIdentifier> = new HashSet() as any;
		candidateGroups.forEach((_, key) => candidates.add(key));
		const totallyRelevantsByName: Map<string, Set<RtInternalIdentifier>> = new Map();

		// Build the map of totally relevant entities by name
		candidateGroups.forEach((relevantEntities: Set<RtInternalIdentifier>) => {
			relevantEntities.forEach((relEntity: RtInternalIdentifier) => {
				ValidationCommand.addToMultiSetHashMap(
					totallyRelevantsByName,
					relEntity.getName(),
					relEntity
				);
			});
		});

		totallyRelevantsByName.forEach((_, key) => containedElements.delete(key));
		if (containedElements.size > 0) {
			// Some field or direct sub-group of all the candidates is missing the totally relevant attribute
			// So, no further groups can be found
			return result;
		}

		// Do the intersection for each name
		totallyRelevantsByName.forEach((totallyRelevantsPerName: Set<RtInternalIdentifier>) => {
			candidates = this._intersectIdentifierIndexSets(candidates, totallyRelevantsPerName);
		});

		candidates.forEach(c => result.add(c));
		return result;
	}

	private _intersectIdentifierIndexSets(
		candidates: Set<RtInternalIdentifier>,
		totallyRelevants: Set<RtInternalIdentifier>
	): Set<RtInternalIdentifier> {
		const result = new HashSet<RtInternalIdentifier>();
		candidates.forEach((candidate: RtInternalIdentifier) => {
			totallyRelevants.forEach((relevant: RtInternalIdentifier) => {
				const cand = candidate.intersect(relevant);
				if (cand != null) {
					result.add(cand);
				}
			});
		});
		return result as any;
	}

	static addToMultiSetHashMap<T, U extends IHashCode & IEquatable>(
		map: Map<T, Set<U>>,
		key: T,
		value: U
	): void {
		this.computeIfAbsent(map, key, () => (new HashSet<U>()) as any).add(value)
	}

	static computeIfAbsent<T, U>(map: Map<T, U>, key: T, valueSupplier: () => U) {
		let result = map.get(key)
		if (result !== undefined) {
			return result;
		}
		result = valueSupplier()
		map.set(key, result)
		return result
	}

	static addToMultiMapMultiSetHashMap<
		S,
		T extends IHashCode & IEquatable,
		U extends IHashCode & IEquatable
	>(map: Map<S, Map<T, Set<U>>>, key0: S, key1: T, value: U): void {
		const innerMap = this.computeIfAbsent(map, key0, () => new HashMap<T, Set<U>>());
		this.computeIfAbsent(innerMap, key1, () => (new HashSet<U>()) as any).add(value);
	}

	/**
	 * Alle an der Schnittstelle übergebenen relevanten Kontexte werden expandiert.
	 *
	 * @param relevantEntities 	prüfungsrelevante Entitäten
	 * @return Menge der relevanten Felder, relevante Kontexte werden vorher expandiert
	 */
	private _initialisiereRelevanteFelder(
		relevantEntities: Set<IIdentifier>
	): HashSet<RtInternalIdentifier> {
		const result = new HashSet<RtInternalIdentifier>();
		for (const id of relevantEntities) {
			const isField = this._metaData.isFeldDefiniert(id.getName());
			if (!isField && !this._metaData.isGroupDefined(id.getName())) {
				this._validationCache.addUnbekanntesFeld(id);
				continue;
			}
			// Umschlüsselung externer Name auf interner Name
			const internId: RtInternalIdentifier = FeldUtil.makeInternIdentifier(id, this._metaData);
			if (isField) {
				result.add(internId);
			} else {
				this._expandRelevantGroup(internId).forEach(expandedId => result.add(expandedId));
			}
		}
		return result;
	}

	private _expandRelevantGroup(groupId: RtInternalIdentifier): Set<RtInternalIdentifier> {
		const expandedIds = new HashSet<RtInternalIdentifier>();
		const expandedIndices: number[] = groupId.getIndices().slice();
		expandedIndices.push(0);
		this._metaData.getContainedFields(groupId.getName()).forEach((fieldName: string) => {
			expandedIds.add(new RtInternalIdentifier(fieldName, expandedIndices));
		});
		this._metaData.getContainedGroups(groupId.getName()).forEach((groupName: string) => {
			const subGroupId = new RtInternalIdentifier(groupName, expandedIndices);
			this._expandRelevantGroup(subGroupId).forEach((expandedId: RtInternalIdentifier) =>
				expandedIds.add(expandedId)
			);
		});
		return expandedIds as any;
	}

	/**
	 * Main method for full validation.
	 *
	 * @param data 	the data to be validated. May not be <code>null</code>.
	 * @param validationResult 	container object for resulting messages on erroneous plausibility checks. May not
	 *            be <code>null</code>.
	 */
	public processFullValidation(
		data: IData,
		validationResult: IResult
	): void {
		const startMesspunkt: number = this._logger.startMesspunkt();
		this._validationCache.setDaten(data);
		this._processInternalVV(validationResult);
		this._logger.logMessung("The validation of the new API took ", startMesspunkt);
	}

	private _processInternalVV(ergebnisse: IResult): void {
		this.formalePruefung(null, ergebnisse);

		// Überprüfe, ob nach dem Validierungsmodus jetzt aufgehört werden muss.
		if (!this._pruefeWeiter(ergebnisse)) {
			return;
		}

		const controller: MainValidatorController = new MainValidatorController(
			ergebnisse,
			this._validationCache,
			this._logger,
			this._modus,
			this._metaData,
			this._formalChecker,
		);
		this._validator.validatePreliminaryRules(controller);
		this._validator.validiereVoll(controller, this._modus);
	}

	/**
	 * Prüfungsroutine zum Abprüfen, ob nach der formalen Prüfung weiter geprüft
	 * wird.
	 *
	 * @param ergebnisse 	Ergebnisse, die bereits gefunden wurden
	 * @return true oder false, je nachdem ob weiter geprüft werden soll.
	 */
	private _pruefeWeiter(ergebnisse: IResult): boolean {
		// Nur formale Pruefung
		if (this._modus.getValidationType() === ValidationType.onlyFormalNoRequiredFieldCheck) {
			// Ende der Plausibilitätsprüfung, da nur formale Pruefung
			return false;
		}
		// Abbruch nach Fehlern in formaler Prüfung und Fehler gefunden
		if (
			this._modus.getValidationType() === ValidationType.fullExaminationWithAbortOnFormalErrors &&
			ergebnisse.getCountOf(ErrorType.VALUE_ERROR) > 0
		) {
			// Ende der Plausibilitätsprüfung, da Fehlern in der syntaktischen Prüfung
			return false;
		}
		return true;
	}
}
