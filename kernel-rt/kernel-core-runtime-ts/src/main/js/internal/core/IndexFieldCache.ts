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

import {
	NumberUtil
} from "@com.mgmtp.a12.utils/utils-localization/a12internal";

import { IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import {
	ArrayMap,
	ArraySet
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { EntityUtil } from "../util/EntityUtil.js";

import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";
import type { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";

import { FormatDefinitionZahl } from "../formatdef/FormatDefinitionZahl.js";
import { IMetaKeysInternal } from "../util/IMetaKeysInternal.js";

import type { IMetaDataValidierung } from "./IMetaDataValidierung.js";

/**
 * Caches the index field.
 * For each index group and each iteration outside the index group, the mapping from index field
 * value to numeric iteration index is stored.
 *
 * E.g. an index field /V[1]/K[15]/K[10]/I[1] with following values:
 * - I@1,1,1,1="A"
 * - I@1,1,2,1="Test"
 * - I@1,1,3,1="B"
 * - I@1,2,1,1="B"
 * - I@1,2,2,1="C"
 * - I@1,2,3,1="Test"
 *
 * The following cache entries should be added:
 * - "/V/K/K" -> [1,1] -> "A" -> 1
 * - "/V/K/K" -> [1,1] -> "Test" -> 2
 * - "/V/K/K" -> [1,1] -> "B" -> 3
 * - "/V/K/K" -> [1,2] -> "B" -> 1
 * - "/V/K/K" -> [1,2] -> "C" -> 2
 * - "/V/K/K" -> [1,2] -> "Test" -> 3
 *
 * When looking up with the values "/V/K/K", [1,2], "Test", one should receive a value of 3.
 */
export class IndexFieldCache {
	private _cache: Map<
		string, // index group full name
		ArrayMap< // "index array cache"
			number[], // index group parent repetitions
			Map< // "index mapping for group"
				string, // index field value
				number // index group repetition
			>
		>
	> = new Map();

	/**
	 * If there are invalid values for an index field, the grand parent indices of the invalid field
	 * are stored in the map. Key is the unique name of the index field.
	 *
	 * @Note At the moment we only need to know if there is one invalid index field for each
	 * iteration above the indexed level, so we only store the parent indices of the indexed level
	 * (= grand parent indices of the index field).
	 */
	private _indexFieldUniqueNameToInvalidGrandParentIndices: Map<
		string, // index field unique name
		ArraySet<number[]> // grand parent indices of invalid index field instance
	> = new Map();

	constructor(private _metaData: IMetaDataValidierung) { }

	/**
	 * Adds the given index field identifier to the index field cache.
	 *
	 * @param id identifier of the index field.
	 * @param newXmlValue given value for the index field.
	 * @param oldXmlValue previous value of the index field.
	 */
	setIndexFieldValue(id: RtInternalIdentifier, newXmlValue: string, oldXmlValue?: string): void {
		const indexFieldUniqueName = id.getName();
		const indexFieldIndices = id.getIndices();
		const indexGroupParentIndices = indexFieldIndices.slice(0, -2);

		const indexFieldFullName = this._metaData.getPfadName(indexFieldUniqueName);
		const indexGroupFullName = EntityUtil.getParentPath(indexFieldFullName);
		const indexCache = newXmlValue
			? this._assuredIndexMappingForGroup(indexGroupFullName, indexGroupParentIndices)
			// If newXmlValue is empty, we only have to remove old mappings from the cache and have
			// no new indexFieldValue -> repetition-index mapping to add
			// ==> if there is no old entry, we don't need to initialize a new one,
			// because we have nothing to do here
			: this.getIndexMappingForGroup(indexGroupFullName, indexGroupParentIndices);
		if (indexCache == undefined) {
			// this happens only when newXmlValue was emtpy in the previous step
			// ==> we know that we have neither a new value to add nor an old value to remove => nothing to do
			return;
		}

		const indexGroupRepetition = indexFieldIndices[indexFieldIndices.length - 2];
		// disassociate old value
		if (oldXmlValue) {
			const normalizedOldXmlValue = this._normalizeValue(indexFieldUniqueName, oldXmlValue);
			// we must test that this value is still associated with this instance, otherwise we might overwrite
			// an entry already updated by another instance
			if (indexCache.get(normalizedOldXmlValue) === indexGroupRepetition) {
				indexCache.delete(normalizedOldXmlValue);
			}
		}

		// associate new value
		if (newXmlValue) {
			const normalizedNewXmlValue = this._normalizeValue(indexFieldUniqueName, newXmlValue);
			indexCache.set(normalizedNewXmlValue, indexGroupRepetition);
		}
	}

	/**
	 * Normalizes the value of the index field. If the values are numbers, we want to do comparison
	 * as if they were numbers.
	 */
	private _normalizeValue(uniqueFieldName: string, xmlValue: string): string {
		const fd: IFormatDefinition = this._metaData.getIFormatDefinition(uniqueFieldName);
		if (fd instanceof FormatDefinitionZahl) {
			let basicVal: number | string | null;
			try {
				basicVal = NumberUtil.createBigDecimal(xmlValue, this._metaData.getDezimalTrenner());
			} catch (_e) {
				basicVal = null;
			}
			if (basicVal == null) {
				return xmlValue;
			}
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			return fd.convertFromBasicType(basicVal, this._metaData)!;
		}
		return xmlValue;
	}

	/**
	 * Same as {@link getIndexMappingForGroup}, but creates an entry in the cache if it doesn't already exist.
	 */
	private _assuredIndexMappingForGroup(indexGroupFullName: string, indexGroupParentIndices: number[]): Map<string, number> {
		let indexArrayCache = this._cache.get(indexGroupFullName);
		if (indexArrayCache === undefined) {
			indexArrayCache = new ArrayMap();
			this._cache.set(indexGroupFullName, indexArrayCache);
		}

		let indexCache = indexArrayCache.get(indexGroupParentIndices);
		if (indexCache === undefined) {
			indexCache = new Map<string, number>();
			indexArrayCache.set(indexGroupParentIndices, indexCache);
		}

		return indexCache;
	}

	/**
	 * Lookup the numeric index of a group for a level with index field given the value of the index
	 * field.
	 *
	 * @param indexGroupFullName the full name of the index group
	 * @param indexGroupParentIndices the indices of the index group's parent.
	 * 		If the index group is a root group, an empty list must be given.
	 * @param xmlValue the value of the index field.
	 * @return the iteration index of the index group for the corresponding value or {@link IIdentifier.UNKNOWN}
	 * if there was no entry for this combination of values.
	 */
	lookup(indexGroupFullName: string, indexGroupParentIndices: number[], xmlValue: string): number {
		const indexCache = this.getIndexMappingForGroup(indexGroupFullName, indexGroupParentIndices);
		const indexFieldUniqueName = this._getIndexFieldUniqueName(indexGroupFullName);
		if (indexCache == undefined || indexFieldUniqueName == null) {
			return IIdentifier.UNKNOWN;
		}

		const normalizedXmlValue = this._normalizeValue(indexFieldUniqueName, xmlValue);
		const indexGroupRepetition = indexCache.get(normalizedXmlValue);
		return indexGroupRepetition ?? IIdentifier.UNKNOWN;
	}

	private _getIndexFieldUniqueName(indexGroupFullName: string): string | null {
		const idxFields = this._metaData.getIndexFields(indexGroupFullName);
		if (idxFields == null || idxFields.length !== 1) {
			return null;
		}
		return idxFields[0].getValue(IMetaKeysInternal.FELD_UNIQUENAME);
	}

	/**
	 * Get the mapping from value of the index field to repetition number.
	 *
	 * @param indexGroupFullName 	the name of the group containing the index field.
	 * @param indexGroupParentIndices 	the repetition indices of the parent group of the group denoted by groupName.
	 * @return the mapping from index value to repetition number, or `undefined` if  there is none in the cache yet.
	 */
	getIndexMappingForGroup(
		indexGroupFullName: string,
		indexGroupParentIndices: number[]
	): Map<string, number> | undefined {
		return this._cache.get(indexGroupFullName)?.get(indexGroupParentIndices);
	}

	/**
	 * @param indexGroupFullName full name of the group in which the index field is contained
	 * @param indexGroupParentIndices indices of the given index group's parent
	 * @return `true` if no index fields are filled for the given index group parent, otherwise `false`
	 */
	noIndexFieldsFilled(indexGroupFullName: string, indexGroupParentIndices: number[]): boolean {
		const indexCache = this._cache.get(indexGroupFullName)?.get(indexGroupParentIndices);
		return indexCache == undefined || indexCache.size === 0;
	}

	/**
	 * Mark the given index field instance as invalid.
	 */
	addInvalidIndexField(indexFieldId: RtInternalIdentifier): void {
		this._addInvalidIndexFieldGrandParentIndices(indexFieldId.getName(), indexFieldId.getIndices().slice(0, -2));
	}

	/**
	 * Removes all previously with {@link addInvalidIndexField} as invalid marked instances of the given index field (unique name).
	 */
	removeAllInvalidIndexFields(indexFieldUniqueName: string): void {
		this._getOrCreateInvalidGrandParentIndices(indexFieldUniqueName).clear();
	}

	private _addInvalidIndexFieldGrandParentIndices(indexFieldUniqueName: string, grandParentIndices: number[]): void {
		this._getOrCreateInvalidGrandParentIndices(indexFieldUniqueName).add(grandParentIndices);
	}

	private _getOrCreateInvalidGrandParentIndices(indexFieldUniqueName: string): ArraySet<number[]> {
		let invalidGrandParentIndices = this._indexFieldUniqueNameToInvalidGrandParentIndices.get(indexFieldUniqueName);
		if (invalidGrandParentIndices == undefined) {
			invalidGrandParentIndices = new ArraySet();
			this._indexFieldUniqueNameToInvalidGrandParentIndices.set(indexFieldUniqueName, invalidGrandParentIndices);
		}
		return invalidGrandParentIndices;
	}

	private _isAnyIndexFieldInvalid(indexFieldUniqueName: string, indexFieldGrandParentIndices: number[]): boolean {
		return this._indexFieldUniqueNameToInvalidGrandParentIndices
			.get(indexFieldUniqueName)
			?.has(indexFieldGrandParentIndices)
			?? false;
	}

	isIndexFieldInvalidForOneRepetition(indexFieldId: RtInternalIdentifier): boolean {
		return this._isAnyIndexFieldInvalid(indexFieldId.getName(), indexFieldId.getIndices().slice(0, -2));
	}

	/**
	 * @param indexGroupFullName the full name of the index group to test
	 * @param indexGroupParentIndices the indices of the index group's parent, within which to look for invalid repetitions
	 * @returns `true`, if the parent instance of the given index group (defined by the given indices) contains one or more repetitions
	 * of the index group with an invalid index field.
	 */
	isIndexGroupInvalidForOneRepetition(indexGroupFullName: string, indexGroupParentIndices: number[]): boolean {
		const uniqueIndexFieldName = this._getIndexFieldUniqueName(indexGroupFullName);
		return uniqueIndexFieldName != null && this._isAnyIndexFieldInvalid(uniqueIndexFieldName, indexGroupParentIndices);
	}

	getInvalidIndexGroupParentIndices(indexGroupFullName: string): Set<number[]> {
		const uniqueIndexFieldName = this._getIndexFieldUniqueName(indexGroupFullName);
		if (uniqueIndexFieldName != null) {
			const indexGroupParentIndices = this._indexFieldUniqueNameToInvalidGrandParentIndices.get(uniqueIndexFieldName);
			if (indexGroupParentIndices != undefined) {
				return indexGroupParentIndices as any;
			}
		}
		return new Set();
	}

	hasInvalidIndexFields(): boolean {
		return this._indexFieldUniqueNameToInvalidGrandParentIndices.size > 0;
	}
}
