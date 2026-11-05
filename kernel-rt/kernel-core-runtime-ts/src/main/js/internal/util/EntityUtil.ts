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
import { StringUtil } from "@com.mgmtp.a12.utils/utils-localization/a12internal";

import { IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";


import { ValidationCache } from "../core/ValidationCache.js";

import { ArrayUtils } from "./ArrayUtils.js";
import { FeldUtil } from "./FeldUtil.js";
import { RtIdentifierTemplateLayerSpec } from "./RtIdentifierTemplateLayerSpec.js";
import { RtInternalIdentifier } from "./RtInternalIdentifier.js";

/**
 * Hilfsklasse für alle Utility Methoden bezogen auf die Entitäten Feld, Kontext, Vordruck.
 */
export class EntityUtil {
	public static get SEPARATOR_CHAR(): string {
		return "/";
	}

	/**
	 * Returns the level of the given path.<br>
	 * 0 for a root group, 1 for a group below the root group,
	 * etc.
	 */
	public static getIndexOfHierarchyLevel(path: string): number {
		return this.countHierarchyLevels(path) - 1;
	}

	/**
	 * Returns the number of hierarchy levels of the given path.<br>
	 * 1 for root group, 2 for a group below the root group etc.
	 *
	 * @param path
	 * @return
	 */
	public static countHierarchyLevels(path: string): number {
		return StringUtil.countMatches(path, this.SEPARATOR_CHAR);
	}

	/**
	 * Returns the path for the given hierarchy level.<br>
	 * root group for 0, a group below the root group for 1 etc.<br>
	 * Example: getIndexOfHierarchyLevel(getPathOfHierarchyLevel(fullPath, level)) == level
	 *
	 * @param fullPath 	The full path of which a partial path should be extracted
	 * @param level 	The level of the path returned
	 * @return
	 */
	public static getPathOfHierarchyLevel(fullPath: string, level: number): string {
		let pos: number = fullPath.indexOf(this.SEPARATOR_CHAR, 1);
		if (pos < 0) {
			if (level === 0) {
				return fullPath;
			} else {
				throw new Error("False amount of levels in path");
			}
		}
		// Variables cannot be reassigned in while-loops
		while (level > 0) {
			level = level - 1;
			pos = fullPath.indexOf(this.SEPARATOR_CHAR, pos + 1);
			if (pos < 0) {
				if (level === 0) {
					return fullPath;
				} else {
					throw new Error("False amount of levels in path");
				}
			}
		}
		return fullPath.substring(0, pos);
	}

	/**
	 * For the full name of a field or group return the full name of the parent group.
	 *
	 * @param fullName 	the full name of the field or group.
	 * @return the full name of the parent group.
	 */
	public static getParentPath(fullName: string): string {
		return fullName.substring(0, fullName.lastIndexOf(this.SEPARATOR_CHAR));
	}

	/**
	 * @param fullName 	the full name of the field or group.
	 * @return the short name of the field or group.
	 */
	public static getEntityName(fullName: string): string {
		return fullName.substring(fullName.lastIndexOf(this.SEPARATOR_CHAR) + 1);
	}

	/**
	 * @param fullName 	the full name of the field or group.
	 * @return the name of the root group (without leading slash).
	 */
	public static getRootGroupName(fullName: string): string {
		const index = fullName.indexOf(this.SEPARATOR_CHAR, 1);
		if (index !== -1) {
			return fullName.substring(1, index);
		} else {
			return fullName.substring(1);
		}
	}

	/**
	 * @param rootGroupName the name of the root group (without leading slash)
	 * @returns the path / full name of the root group
	 */
	public static getRootGroupPath(rootGroupName: string): string {
		return this.SEPARATOR_CHAR + rootGroupName;
	}

	/**
	 * @param uniqueName 	the unique name of an entity (a group unique name starts with a leading
	 * 		slash, a field unique name does not).
	 * @return `true` if the unique name denotes a root group (a group with exactly one path segment).
	 */
	public static uniqueNameIsRootGroup(uniqueName: string): boolean {
		return uniqueName.startsWith(this.SEPARATOR_CHAR)
			&& !uniqueName.substring(1).includes(this.SEPARATOR_CHAR);
	}

	/**
	 * helper method to build a fullName or a relative name
	 *
	 * @return concatenates path and toAdd, or simply returns toAdd if path is empty.
	 */
	public static createPath(path: string, toAdd: string): string {
		return path
			? `${path}${this.SEPARATOR_CHAR}${toAdd}`
			: toAdd;
	}

	/**
	 * @param absPathA
	 * 		must not end with '/', and must start with '/' unless empty.
	 * 		e.g. "/A/B/C", "/A" and "" are valid, but "/A/B/" and "/" are not.
	 * @param absPathB
	 * 		must not end with '/', and must start with '/' unless empty
	 * @returns the 'index' of the level of common ancestors, can be -1, which means there are no common ancestors
	 */
	public static commonAncestorLevel(absPathA: string, absPathB: string): number {
		const partsA = EntityUtil.splitAbsPath(absPathA);
		const partsB = EntityUtil.splitAbsPath(absPathB);

		const lowerLevel = Math.min(partsA.length, partsB.length);
		for (let level = 0; level < lowerLevel; level++) {
			if (partsA[level] !== partsB[level]) {
				return level - 1;
			}
		}
		return lowerLevel - 1;
	}

	private static splitAbsPath(absPath: string): string[] {
		if (absPath.length == 0) {
			return [];
		} else if (absPath[0] !== this.SEPARATOR_CHAR) {
			throw new Error(`illegal abspath: '${absPath}' (abspath must start with '/' unless they are empty`);
		} else {
			return absPath.split(this.SEPARATOR_CHAR).slice(1);
		}
	}

	/**
	 * Prüft, ob der Identifier `id` prüfungsrelevant ist.
	 * Fehlerhafte Felder führen auch bei Vollvalidierung dazu, dass Felder nicht prüfungsrelevant sind.
	 * Bei der Teilvalidierung wird geprüft, ob das Feld in den prüfungsrelevanten Feldern enthalten ist.
	 *
	 * @param id 	zu prüfender Identifier, alle Wiederholungen müssen in jeder Ebene konkret sein.
	 * @param vcORrelevanteFelderCache 	prüfungsrelevante Felder
	 * @return true falls der Identifier prüfungsrelevant ist, false sonst.
	 */
	public static isPruefungsRelevant(
		id: RtInternalIdentifier,
		vcORrelevanteFelderCache: Map<string, Set<RtInternalIdentifier>> | null
	): boolean;
	/**
	 * Checks if the identifier `id` is relevant.
	 *
	 * An identifier is regarded as relevant if:
	 *   - it is not erroneous,
	 *   - it has a semantic index defined by an entity: the semantic index is relevant (and not erroneous),
	 *   - it has a semantic index: the corresponding index field is relevant (and not erroneous),
	 *   - an index of `id` is {@link IIdentifier.UNKNOWN}: the corresponding index field is relevant
	 *     (and not erroneous) for all repetitions, and
	 *   - it is a full validation or in case of a partial validation: the `id` is contained in the relevant fields
	 *
	 * **Precondition**: The indices of `id` have to be concrete, {@link IIdentifier.SEMANTIC_INDEX}
	 * or {@link IIdentifier.UNKNOWN}. An index {@link IIdentifier.PARALLEL_ITERATION} has to be replaced by
	 * {@link IIdentifier.SEMANTIC_INDEX} before calling the method. In case of a semantic index, it is resolved
	 * to check if the identifier is relevant.
	 *
	 * @param id identifier to check.
	 * @param vcORrelevanteFelderCache a {@link ValidationCache} or a map from field name to that field's relevant identifiers
	 * @return `true` if the identifier is relevant, `false` otherwise.
	 */
	public static isPruefungsRelevant(
		id: RtInternalIdentifier,
		vcORrelevanteFelderCache: ValidationCache,
		partialIsUnknown: boolean
	): boolean;
	public static isPruefungsRelevant(
		id: RtInternalIdentifier,
		vcORrelevanteFelderCache: ValidationCache | Map<string, Set<RtInternalIdentifier>> | null,
		partialIsUnknown = false,
	): boolean {
		// keine Teilvalidierung -> immer prüfungsrelevant
		if (vcORrelevanteFelderCache === null) {
			return true;
		} else if (vcORrelevanteFelderCache instanceof ValidationCache) {
			const validationCache = vcORrelevanteFelderCache;
			const semIdx = id.getSemanticIndex();
			if (semIdx == undefined) {
				return this._isIdWithoutSemanticIndexRelevant(id, validationCache, partialIsUnknown);
			}
			if (semIdx.isEntity()
				&& !this._isIdWithoutSemanticIndexRelevant(semIdx.getAsEntity(), validationCache, partialIsUnknown)
			) {
				return false;
			}
			// index field of id with the same indices up to the indexed level, semantic index resolved
			const indexFieldId = validationCache.resolveSemanticIndex(validationCache.getIndexFieldId(id));
			if (!this._isIdWithoutSemanticIndexRelevant(indexFieldId, validationCache, partialIsUnknown)) {
				return false;
			}
			const idWithSemanticIndexResolved = validationCache.resolveSemanticIndex(id);
			if (
				this._isUnknown(idWithSemanticIndexResolved) &&
				this._isIndexfieldNotRelevantForAllRepetitions(indexFieldId, validationCache)
			) {
				return false;
			}
			return this._isIdWithoutSemanticIndexRelevant(idWithSemanticIndexResolved, vcORrelevanteFelderCache, partialIsUnknown);
		} else if (vcORrelevanteFelderCache instanceof Map) {
			return this._relevanteEntitiesUmfassenIdentifier(vcORrelevanteFelderCache, id);
		} else {
			throw new Error("Wrong argument types for the method 'isPruefungsRelevant'.");
		}
	}

	/**
	 * Checks for an `id` without semantic index if it is relevant, i.e. not erroneous
	 * and, in the case of partial validation, contained in the set of relevant fields considering
	 * the special meaning of {@link IIdentifier.ALL_INDICES} for a level.
	 *
	 * The indices of `id` may be concrete or {@link IIdentifier.UNKNOWN}, but nothing
	 * else, i.e. the following indices are **NOT** allowed:
	 *   - {@link IIdentifier.ALL_INDICES}
	 *   - {@link IIdentifier.SEMANTIC_INDEX}
	 *   - {@link IIdentifier.PARALLEL_ITERATION}
	 *   - {@link IIdentifier.ITERATION}
	 *
	 * In case of {@link IIdentifier.UNKNOWN} the field is only detected as relevant if it is with
	 * {@link IIdentifier.ALL_INDICES} for the respective level in the relevant fields (and not
	 * if each single repetition is listed as relevant).
	 */
	private static _isIdWithoutSemanticIndexRelevant(
		id: RtInternalIdentifier,
		vc: ValidationCache,
		partialIsUnknown: boolean
	): boolean {
		if (vc.isFehlerhafteEntity(id, partialIsUnknown)) {
			return false;
		}
		if (!vc.isValidationPartWith3ValueLogic()) {
			// For full validation, all fields are relevant
			return true;
		}
		const isRelevant = this._relevanteEntitiesUmfassenIdentifier(
			vc.getPruefungsrelevanteFelderIntern() as any,
			id
		);
		if (!partialIsUnknown || !isRelevant) {
			return isRelevant;
		}
		if (!FeldUtil.isGroup(id)) {
			return true;
		}
		return this._relevanteEntitiesUmfassenIdentifier(vc.getFullyRelevantGroupsIntern(), id);
	}

	private static _isUnknown(id: RtInternalIdentifier): boolean {
		return ArrayUtils.indexOf(id.getIndices(), IIdentifier.UNKNOWN) !== -1;
	}

	private static _isIndexfieldNotRelevantForAllRepetitions(
		indexFieldId: RtInternalIdentifier,
		vc: ValidationCache
	): boolean {
		if (vc.isIndexFieldInvalidForOneRepetition(indexFieldId)) {
			return true;
		}
		const indices: number[] = ArrayUtils.copyOf(indexFieldId.getIndices());
		indices[indices.length - 2] = IIdentifier.ALL_INDICES;
		const indexFieldIdWithAllIndices: RtInternalIdentifier = new RtInternalIdentifier(
			indexFieldId.getName(),
			indices
		);
		if (
			vc.isValidationPartWith3ValueLogic() &&
			!this._relevanteEntitiesUmfassenIdentifier(
				vc.getPruefungsrelevanteFelderIntern() as any,
				indexFieldIdWithAllIndices
			)
		) {
			return true;
		}
		return false;
	}

	/**
	 * Prüft, ob der übergebene Identifier von der Menge der relevanten Felder umfasst wird. Die
	 * Bedeutung von 'umfasst' ist in
	 * {@link RtInternalIdentifier.umfasst} beschrieben.
	 *
	 * @param id 	zu prüfender Identifier, immer konkret
	 * @param relevanteFelder 	Map mit allen relevanten Feldern, key ist der Feldname, value die Menge der
	 *            relevanten Felder mit diesem Namen. Die Identifier können auch ALLE auf einer
	 *            Wiederholungsebene spezifizieren
	 * @return true falls ein relevantes Feld die id umfasst
	 */
	private static _relevanteEntitiesUmfassenIdentifier(
		relevanteFelder: Map<string, Set<RtInternalIdentifier>>,
		id: RtInternalIdentifier
	): boolean {
		const relevanteFelderMitGleichemNamen: Set<RtInternalIdentifier> | undefined =
			relevanteFelder.get(id.getName());

		// noch nicht mal ein Feld mit gleichem Namen gefunden --> gib sofort false zurück
		if (!relevanteFelderMitGleichemNamen) {
			return false;
		}

		// in den relevanten Feldern ist ein Identifier mit exakt den gesuchten Wiederholungsindices gefunden
		if (relevanteFelderMitGleichemNamen.has(id)) {
			return true;
		}
		// prüfe für jeden relevanten Identifier, ob er den zu prüfenden Identifier umfasst.
		for (const relId of relevanteFelderMitGleichemNamen) {
			if (relId.umfasst(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a layer specification obtained from layerSpecs when all iteration layers (i.e.
	 * layers with spec {@link IIdentifier#ITERATION}) are replaced by the corresponding concrete
	 * value from iterationState.
	 *
	 * @param layerSpecs
	 *            index-ranges for layers in the group hierarchy. An index range can either be given
	 *            by a pair of start- and end-index integers, or by a special value like
	 *            {@link IIdentifier#ITERATION}.
	 * @param iterationState
	 *            as returned form {@link IterationState}.getIndices. If layerSpecs has no
	 *            entries with {@link IIdentifier#ITERATION}, iterationState may be null
	 *
	 *            Note: iterationState may apparently be from a different subtree than
	 *            layerSpecs, e.g. [[5 - 10], [-1 - -1]] and [1, 1] is a
	 *            combination that might occur
	 */
	public static resolveIterations(
		layerSpecs: Array<RtIdentifierTemplateLayerSpec>,
		iterationState: number[] | undefined
	): Array<RtIdentifierTemplateLayerSpec> {
		const result: Array<RtIdentifierTemplateLayerSpec> = [];
		for (let i = 0; i < layerSpecs.length; i++) {
			let start: number = layerSpecs[i].getIdxStart();
			let end: number = layerSpecs[i].getIdxEnd();
			if (start == IIdentifier.ITERATION) {
				if (iterationState === undefined || iterationState.length <= i) {
					throw Error(
						"given iteration state is insufficient to resolve all iterations in given layer spec: " +
						iterationState
					);
				}
				start = end = iterationState[i];
			}
			result.push(new RtIdentifierTemplateLayerSpec(start, end));
		}
		return result;
	}
}
