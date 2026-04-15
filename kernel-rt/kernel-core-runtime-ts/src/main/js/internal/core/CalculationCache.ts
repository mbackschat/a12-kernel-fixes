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

import type { IData } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import {
	HashMap,
	HashSet
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { DatenImpl } from "../util/DatenImpl.js";
import { EntityUtil } from "../util/EntityUtil.js";
import type { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";
import type { SemanticIndexSpec } from "../util/SemanticIndexSpec.js";

import type { RtExternalIdentifier } from "../util/RtExternalIdentifier.js";

import { ValidationCache, WerteContainer } from "./ValidationCache.js";

export class CalculationCache extends ValidationCache {
	/**
	 * Eine Map von {@link RtInternalIdentifier} auf den berechneten Wert (in XML und Oberflächendarstellung)
	 */
	private _calculatedValues: HashMap<RtInternalIdentifier, WerteContainer | null> = new HashMap();

	private _invalidFields: HashSet<RtInternalIdentifier> = new HashSet();

	/**
	 * Remembers the fields whose value was set to {@link PSEUDO_VALUE}.
	 */
	private readonly _fieldsWithPseudoValue: HashSet<RtInternalIdentifier> = new HashSet();

	/**
	 * Stores index field instances which were cleared by an external calculation, but were filled with
	 * a default value. We need to be able to differentiate this case, since the calculation result should
	 * contain the cleared value instead of the inserted default value.
	 */
	private _calculatedIndexFieldsWithDefaultValue: HashSet<RtInternalIdentifier> = new HashSet();

	/**
	 * Fügt den angegebenen Wert für das angegebene Feld hinzu.
	 *
	 * @param f	die ID des Feldes.
	 * @param wertOberflaeche	der Wert des Feldes in der Oberflächendarstellung.
	 * @param wertXml	der Wert des Feldes in der internen Repräsentation.
	 */
	public addCalculatedFieldValue(
		f: RtInternalIdentifier,
		wertOberflaeche: string,
		wertXml: string
	): void {
		const id = this.resolveSemanticIndex(f);

		if (!wertXml) {
			this._calculatedValues.set(id, null);
			this.groupFillCache.setFieldNotFilled(id);
		} else {
			const wc = new WerteContainer(wertOberflaeche, wertXml);
			this._calculatedValues.set(id, wc);
			this.setFieldFilled(id);
		}

		if (this._isIndexFieldInstance(id)) {
			this._indexFieldCache.setIndexFieldValue(id, wertXml, this._datenCache.get(id)?.feldWertXML);
		}
	}

	/**
	 * @returns the XML value of the field (incl. calculated fields), or undefined if the field is not in the cache.
	 * @throws an Error if the field was an erroneously calculated
	 */
	override getXMLValue(f: RtInternalIdentifier): string | undefined {
		const id = this.resolveSemanticIndex(f);
		if (this._invalidFields.has(id)) {
			throw this._createCascadingError();
		}
		return this._getXMLValue(id);
	}

	private _createCascadingError(): Error {
		return new Error(
			"The calculation of the field returns an invalid value. No further calculation will be performed on this field."
		);
	}

	/**
	 * @returns the XML value of the field (incl. calculated fields), or undefined if the field is not in the cache.
	 * Unlike {@link CalculationCache#getXMLValue}, does not throw an Error for invalid fields.
	 */
	public getXMLValueUnchecked(f: RtInternalIdentifier): string | undefined {
		return this._getXMLValue(this.resolveSemanticIndex(f));
	}

	override _getXMLValue(resolvedField: RtInternalIdentifier): string | undefined {
		if (this._calculatedValues.has(resolvedField)) {
			// semantic index in f is already resolved
			const result = this._calculatedValues.get(resolvedField)?.feldWertXML;
			return result && this._resolveCategory(resolvedField, result);
		}
		return super._getXMLValue(resolvedField);
	}

	override isEntityFilled(entity: RtInternalIdentifier): boolean {
		const id = this.resolveSemanticIndex(entity);
		if (this._invalidFields.has(id)) {
			throw this._createCascadingError();
		}
		if (this._calculatedValues.has(id)) {
			return this._calculatedValues.get(id) != null;
		}
		return super.isEntityFilled(id);
	}

	override getWertOberflaeche(f: RtInternalIdentifier): string | undefined {
		const id = this.resolveSemanticIndex(f);
		if (this._calculatedValues.has(id)) {
			return this._calculatedValues.get(id)?.feldWertOberflaeche;
		}
		return super.getWertOberflaeche(id);
	}

	protected override _actuallyResolveSemanticIndex(id: RtInternalIdentifier, semIdx: SemanticIndexSpec): RtInternalIdentifier {
		const indexFieldId = this.getIndexFieldId(id);
		if (this._indexFieldCache.isIndexFieldInvalidForOneRepetition(indexFieldId)) {
			throw this._createCascadingError();
		}
		return super._actuallyResolveSemanticIndex(id, semIdx);
	}

	protected override _getIterationValuesForGroups(indexGroupFullNamesWithParentIndices: [string, number[]][]): string[] {
		for (const [indexGroupFullName, parentIndices] of indexGroupFullNamesWithParentIndices) {
			// no parallel iteration occurs if any of the index group repetitions is invalid
			// the fields that would have been calculated while iterating over such groups are marked as
			// erroneous with CalculationCache#markFieldsUsingInvalidIndexGroupsAsIncorrectlyCalculated
			if (this._indexFieldCache.isIndexGroupInvalidForOneRepetition(indexGroupFullName, parentIndices)) {
				return [];
			}
		}
		return super._getIterationValuesForGroups(indexGroupFullNamesWithParentIndices);
	}

	public addDefaultValuesForCalculatedIndexField(indexFieldFullName: string): void {
		for (const indexFieldId of this._getIndexFieldIdsEligibleForDefaultValueHandling(indexFieldFullName)) {
			this._setCalculatedIndexFieldToDefaultValue(indexFieldId);
			this._calculatedIndexFieldsWithDefaultValue.add(indexFieldId);
		}
	}

	private _setCalculatedIndexFieldToDefaultValue(indexFieldId: RtInternalIdentifier): void {
		const formatDef = this._metaData.getIFormatDefinition(indexFieldId.getName());
		// only used in `addDefaultValuesForCalculatedIndexField`, we require the svalue to be set
		/* eslint-disable-next-line @typescript-eslint/no-non-null-assertion */
		const sValue = this._getSValue(indexFieldId.getName())!;

		const uiValue = formatDef.convert(sValue, this._presentationInfo, true, this._mode.customFieldTypeFactory);
		this.addCalculatedFieldValue(indexFieldId, uiValue, sValue);
	}

	/**
	 * Marks the given field as invalid. Subsequent accesses to that field's value will throw a {@link _createCascadingError}.
	 */
	public markFieldAsInvalid(f: RtInternalIdentifier): void {
		const id = this.resolveSemanticIndex(f);
		this._invalidFields.add(id);
	}

	public isInvalidField(f: RtInternalIdentifier): boolean {
		const id: RtInternalIdentifier = this.resolveSemanticIndex(f);
		return this._invalidFields.has(id);
	}

	public isFieldWithPseudoValue(id: RtInternalIdentifier): boolean {
		return this._fieldsWithPseudoValue.has(id);
	}

	override addPseudoFieldValueIfAbsent(id: RtInternalIdentifier): void {
		super.addPseudoFieldValueIfAbsent(id);
		this._fieldsWithPseudoValue.add(id);
	}

	override addInvalidFieldValue(id: RtInternalIdentifier, idExtern?: RtExternalIdentifier): void {
		super.addInvalidFieldValue(id, idExtern);
		this.addPseudoFieldValueIfAbsent(id);
	}

	/**
	 * The formal and preliminary validation gather the entities determined to be invalid in:
	 * - {@link _fehlerhafteFelderIntern} for fields, and
	 * - {@link _erroneousGroups} for groups.
	 * These are then used in {@link isFehlerhafteEntity} in order to make these entities irrelevant.
	 * 
	 * During calculation, however, we currently use a different mechanism to handle erroneous entities. To that end
	 * the invalid entities are converted here so that nothing is treated as irrelevant, but causes exceptions
	 * to be thrown instead (see callers of {@link _createCascadingError}).
	 * 
	 * [A12K-2813] NOTE (sb): The resetting that gets done here, don't seem clear to me. Maybe someday we can make this less confusing.
	 * sl: I refactored this code, so the note above does not appear in the context it used to. Might still apply though.
	 */
	public convertInvalidFieldsForCalculation() {
		this._fehlerhafteFelderIntern.forEach(id => this._invalidFields.add(id));
		this.resetErroneousEntitiesForValidation(); // don't treat any entity as "not relevant"
	}

	public removeAllInvalidIndexFields(indexFieldUniqueName: string): void {
		this._indexFieldCache.removeAllInvalidIndexFields(indexFieldUniqueName);
	}

	/**
	 * Clears the erroneous entities used in validation so that they aren't treated as "not relevant".
	 */
	public resetErroneousEntitiesForValidation(): void {
		this._fehlerhafteFelderIntern.clear();
		this._erroneousGroups.clear();
	}

	/**
	 * Marks existing field instances of `uniqueFieldName` as erroneous calculated fields if during parallel iteration
	 * over `indexGroupFullNames` they would iterate over an invalid index group.
	 *
	 * This method might be marking more field instances as invalid than strictly necessary, but it guarantees to not
	 * leave any actually-problematic field instances unmarked.
	 * @param uniqueFieldName unique field name of a field calculated with parallel iteration
	 * @param indexGroupFullNames full names of the index groups used in the parallel iteration
	 */
	public markFieldsUsingInvalidIndexGroupsAsIncorrectlyCalculated(uniqueFieldName: string, indexGroupFullNames: string[]): void {
		if (!this._indexFieldCache.hasInvalidIndexFields()) {
			return;
		}

		const fullFieldName = this.getMetaData().getPfadName(uniqueFieldName);
		const existingFieldIds = Array.from(this.groupFillCache.getExistingFieldInstances(fullFieldName));
		if (existingFieldIds.length === 0) {
			return;
		}

		for (const indexGroupFullName of indexGroupFullNames) {
			const invalidIndexGroupParentIndices = this._indexFieldCache.getInvalidIndexGroupParentIndices(indexGroupFullName);
			if (invalidIndexGroupParentIndices.size === 0) {
				continue;
			}
			// Example:
			//
			// /shared[10]/normal[10]/iteration/levels[10]/nonrep/indexGroup1/
			// /shared[10]/normal[10]/iteration/levels[10]/nonrep2/indexGroup2/myField
			//
			// Case A: indexGroupFullName is not the group that contains myField
			// =>
			// indexGroupParentFullName = /shared/normal/iteration/levels/nonrep
			// indexGroupParentLevel = 5
			// numSharedLevels = level(/shared/normal/iteration/levels) + 1 = 3 + 1 = 4 // nonrep != nonrep2, so "levels" is the last part of the shared prefix
			//
			// Note: if the current indexGroupFullName was ...indexGroup2, then level would be equal to indexGroupParentLevel because
			//   the field would be _inside_ the indexGroup
			//
			// => numNonRepeatableLevels = 5 - 4 = 1 (the "nonrep" / "nonrep2" groups in the above example)
			// => nonRepeatableIndices = [1]
			// => in the loop below, indexGroupParentIndices will take the indices for /shared/normal/iteration/levels from the field instance,
			//    and the [1, ...] from nonRepeatableIndices
			//
			// Case B: what if the current indexGroupFullName is the index group that contains the current field?
			// =>
			// indexGroupParentFullName = /shared/normal/iteration/levels/nonrep2
			// indexGroupParentLevel = 5
			// numSharedLevels = level(/shared/normal/iteration/levels/nonrep2) + 1 = 4 + 1 = 5
			// numNonRepeatableLevels = 5 - 5 = 0
			// nonRepeatableIndices = []
			// => in the loop below, indexGroupParentIndices will take the indices for /shared/normal/iteration/levels/nonrep2 from the field instance,
			//    and append the empty list [] (0 ones)
			//
			// => if one of the indexGroups has invalidities, we mark all instances of myField that share the same normal-iteration context (see numSharedLevels)
			//    as invalid. This is probably more conservative that strictly necessary, but currently we don't think we need to get too clever about identifying
			//    cases where the field might in fact be totally ok, despite the issues of one of the index groups.
			const indexGroupParentFullName = EntityUtil.getParentPath(indexGroupFullName);
			const indexGroupParentLevel = EntityUtil.countHierarchyLevels(indexGroupParentFullName); // 1-based
			const numSharedLevels = EntityUtil.commonAncestorLevel(fullFieldName, indexGroupParentFullName) + 1; // commonAncestorLevel is 0-based -> +1 for one-based
			const numNonRepeatableLevels = Math.max(0, indexGroupParentLevel - numSharedLevels);
			const nonRepeatableIndices = Array.from({ length: numNonRepeatableLevels }, () => 1);

			for (const existingFieldId of existingFieldIds) {
				const indexGroupParentIndices = existingFieldId.getIndices().slice(0, numSharedLevels).concat(nonRepeatableIndices);
				if (invalidIndexGroupParentIndices.has(indexGroupParentIndices)) {
					this.markFieldAsInvalid(existingFieldId);
				}
			}
		}
	}

	/**
	 * @returns the calculated field instances as an {@link IData} object.
	 * 
	 * The {@link RtInternalIdentifier}s are converted back to {@link IIdentifier} and associated
	 * with the calculated display value.
	 */
	public getBerechneteFelder(): IData {
		const result = new DatenImpl();
		for (const [key, value] of this._calculatedValues) {
			const idExtern = this.convert2Extern(key);
			const calculatedValue = this._calculatedIndexFieldsWithDefaultValue.has(key)
				? ""
				: value?.feldWertOberflaeche ?? "";
			result.addValue(idExtern, calculatedValue);
		}
		return result;
	}
}
