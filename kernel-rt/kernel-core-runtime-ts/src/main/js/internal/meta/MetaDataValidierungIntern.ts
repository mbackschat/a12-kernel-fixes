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
import { TimeZonedDate } from "@com.mgmtp.a12.utils/utils-localization/a12internal";

import type { HashSet, IMetaField, IMetaGroup, IMetaRule } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { IDynEnumsIntern } from "../core/IDynEnumsIntern.js";
import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";
import { Constants } from "../util/Constants.js";
import { IAnnotationNamesInternal } from "../util/IAnnotationNamesInternal.js";
import { IMetaKeysInternal } from "../util/IMetaKeysInternal.js";
import type { LocalizableDefaults } from "../util/LocalizableUtil.js";
import type { RtIdentifierTemplate } from "../util/RtIdentifierTemplate.js";
import type { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";

import { EntityUtil } from "../util/EntityUtil.js";

import type { TrieNode } from "../util/TrieNode.js";

import type { MetaModelImpl } from "./MetaModelImpl.js";

/**
 * Diese Implementierung von IMetaDataValidierung dient dem Zugriff auf Methoden des Pakets
 * 'runtime' aus dem Paket 'meta'.
 */
export class MetaDataValidierungIntern implements IMetaDataValidierung {
	private _metaModel: MetaModelImpl;
	private _dezimalTrenner: string;
	private _legalCodepoints: number[][];
	private _legalGraphemeTrie: TrieNode;
	private _baseYearAsDate: Date;
	private _baseYearAsString: string;

	private _dynEnumSupplier: { (): IDynEnumsIntern } | undefined;

	constructor(
		meta: MetaModelImpl,
		dezimalTrenner: string,
		legalCodepoints: number[][],
		legalGraphemeTrie: TrieNode,
		dynEnumSupplier: { (): IDynEnumsIntern } | undefined
	) {
		this._dezimalTrenner = dezimalTrenner;
		this._legalCodepoints = legalCodepoints;
		this._legalGraphemeTrie = legalGraphemeTrie;
		this._metaModel = meta;

		/*
		 * the following are additional, project specific properties so we check if the keys are
		 * valid. If not, we define default values
		 */
		const validKeys: string[] = this._metaModel.getValidKeys().concat(...this._metaModel.getValidKeysInternal());
		if (validKeys.indexOf(IMetaKeysInternal.MODEL_BASE_YEAR) > -1) {
			this._baseYearAsString = this._metaModel.getValue(IMetaKeysInternal.MODEL_BASE_YEAR);
		} else {
			this._baseYearAsString = "";
		}
		if (this._baseYearAsString) {
			this._baseYearAsDate = TimeZonedDate.createForYear(Number(this._baseYearAsString), this.getTimeZone()).toDate();
		} else {
			this._baseYearAsDate = TimeZonedDate.createForYear(0, this.getTimeZone()).toDate();
		}
		this._dynEnumSupplier = dynEnumSupplier;
	}

	// ===========================================================
	// Die folgenden Methoden dienen zur internen Verwendung
	// in der Runtime.
	// ===========================================================

	public getBaseYearAsString(): string {
		return this._baseYearAsString;
	}

	public getDezimalTrenner(): string {
		return this._dezimalTrenner;
	}

	public getBaseYearOrDefault(): number {
		return this._baseYearAsString ? Number(this._baseYearAsString) : 2000;
	}

	public getPfadName(uniqueName: string): string {
		if (this.isUniqueFieldName(uniqueName)) {
			const feld: IMetaField = this._getMetaFieldByUniqueName(uniqueName);
			return feld.getValue(IMetaKeys.FIELD_FULLNAME);
		} else {
			const group: IMetaGroup = this._getMetaGroup(uniqueName);
			return group.getValue(IMetaKeys.GROUP_FULLNAME);
		}
	}

	public getUniqueName(name: string): string {
		const feld: IMetaField = this._metaModel.getValue(IMetaKeys.MODEL_META_FIELD, name);
		if (feld == undefined) {
			// if the field is not there, it may be a group, whose unique name is the full name
			if (this.isGroupDefined(name)) {
				return name;
			}
			throw new Error("The set of fields does not contain <" + name + ">.");
		}
		return feld.getValue(IMetaKeysInternal.FELD_UNIQUENAME);
	}

	public isFeldDefiniert(name: string): boolean {
		const feld: IMetaField = this._metaModel.getValue(IMetaKeys.MODEL_META_FIELD, name);
		return feld != undefined;
	}

	public isGroupDefined(fullName: string): boolean {
		const group: IMetaGroup = this._metaModel.getValue(IMetaKeys.MODEL_META_GROUP, fullName);
		return group != undefined;
	}

	public getMaxFieldIndices(uniqueName: string): number[] {
		const feld: IMetaField = this._getMetaFieldByUniqueName(uniqueName);
		return feld.getValue(IMetaKeysInternal.FELD_MAXINDICES);
	}

	public getMaxGroupIndices(name: string): number[] {
		const group: IMetaGroup = this._getMetaGroup(name);
		return group.getValue(IMetaKeysInternal.CONTAINER_MAXINDICES);
	}

	public getMetaField(name: string): IMetaField {
		const field: IMetaField = this._metaModel.getValue(IMetaKeys.MODEL_META_FIELD, name);
		if (field == undefined) {
			throw new Error("The set of fields does not contain <" + name + ">.");
		}
		return field;
	}

	private _getMetaFieldByUniqueName(uniqueName: string): IMetaField {
		const field: IMetaField = this._metaModel.getValue(IMetaKeysInternal.MODEL_METAFELDUNIQUE, uniqueName);
		if (field == undefined) {
			throw new Error("The set of fields does not contain <" + uniqueName + ">.");
		}
		return field;
	}

	public isUniqueFieldName(uniqueFeldName: string): boolean {
		const feld: IMetaField = this._metaModel.getValue(IMetaKeysInternal.MODEL_METAFELDUNIQUE, uniqueFeldName);
		return feld != undefined;
	}

	private _getMetaGroup(name: string): IMetaGroup {
		const group: IMetaGroup = this._metaModel.getValue(IMetaKeys.MODEL_META_GROUP, name);
		if (group == undefined) {
			throw new Error("The set of groups doesn't contain <" + name + ">.");
		}
		return group;
	}

	public getIndexFields(name: string): IMetaField[] {
		const group: IMetaGroup = this._getMetaGroup(name);
		return group.getValue(IMetaKeys.GROUP_INDEX_FIELDS);
	}

	public hasIndexFields(): boolean {
		return this._metaModel.getValue(IMetaKeys.MODEL_INDEX_FIELDS);
	}

	public isIndexField(uniqueFeldName: string): boolean {
		if (!this.isUniqueFieldName(uniqueFeldName)) {
			return false;
		}
		const field: IMetaField = this._getMetaFieldByUniqueName(uniqueFeldName);
		return field.getValue(IMetaKeys.FIELD_INDEX_FIELD);
	}

	public getIndexFieldFullNames(): string[] {
		return <string[]>this._metaModel.getValue(IMetaKeys.MODEL_INDEX_FIELD_FULL_NAMES)
	}

	public getInterneFeldNr(uniqueName: string): number {
		const feld: IMetaField = this._getMetaFieldByUniqueName(uniqueName);
		return feld.getValue(IMetaKeysInternal.FELD_INTERNENR);
	}

	public getLegalCodepoints(): number[][] {
		return this._legalCodepoints;
	}

	public getLegalGraphemeTrie(): TrieNode {
		return this._legalGraphemeTrie;
	}

	public isLegalCharCheckDeactivated(): boolean {
		return this._metaModel.getValue(IMetaKeys.MODEL_ANNOTATION, IAnnotationNamesInternal.MODEL_DEACTIVATE_LEGALCHARCHECK) != undefined;
	}

	public getBaseYearAsDate(): Date {
		return this._baseYearAsDate;
	}

	public getIFormatDefinition(uniqueName: string): IFormatDefinition {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return this._metaModel.getIFormatDefinition(uniqueName)!;
	}

	public getErrorTexts(regelName: string): LocalizableDefaults {
		const metaRegel: IMetaRule = this._metaModel.getValue(IMetaKeys.MODEL_META_RULE, regelName);
		return metaRegel.getValue(IMetaKeysInternal.REGEL_FEHLERTEXTE);
	}

	public getErrorMessage4Req(fieldFullName: string): LocalizableDefaults {
		const metaField: IMetaField = this.getMetaField(fieldFullName);
		return metaField.getValue(IMetaKeys.FIELD_CUSTOM_ERROR_MESSAGE_FOR_REQUIRED) ?? {};
	}

	public getParameterMapping(ruleName: string): Map<string, string> {
		const metaRegel: IMetaRule = this._metaModel.getValue(IMetaKeys.MODEL_META_RULE, ruleName);
		return <Map<string, string>>metaRegel.getValue(IMetaKeysInternal.REGEL_PARAMETER_MAPPING);
	}

	public getBerechneteFelder(): HashSet<IMetaField> {
		return this._metaModel.getValue(
			IMetaKeys.MODEL_CALC_FIELDS,
			Constants.SERVER
		);
	}

	public getReferencedFieldsWithoutReplacement(ruleName: string): HashSet<RtInternalIdentifier> {
		const metaRegel: IMetaRule = this._metaModel.getValue(IMetaKeys.MODEL_META_RULE, ruleName);
		return metaRegel.getValue(IMetaKeysInternal.REGEL_REFFELDIDS_INTERN);
	}

	public getReferencedOmissionFieldsWithoutReplacement(
		ruleName: string
	): HashSet<RtInternalIdentifier> {
		const metaRegel: IMetaRule = this._metaModel.getValue(IMetaKeys.MODEL_META_RULE, ruleName);
		return metaRegel.getValue(IMetaKeysInternal.REGEL_REFFELDIDS_AUSLASSUNG_INTERN);
	}

	public getReferencedEntitiesWithoutReplacement(
		ruleName: string
	): HashSet<RtIdentifierTemplate> {
		const metaRegel: IMetaRule = this._metaModel.getValue(IMetaKeys.MODEL_META_RULE, ruleName);
		return metaRegel.getValue(IMetaKeysInternal.REGEL_REFENTITYIDS_INTERN);
	}

	public getReferencedOmissionEntitiesWithoutReplacement(
		ruleName: string
	): HashSet<RtIdentifierTemplate> {
		const metaRegel: IMetaRule = this._metaModel.getValue(IMetaKeys.MODEL_META_RULE, ruleName);
		return metaRegel.getValue(IMetaKeysInternal.REGEL_REFENTITYIDS_AUSLASSUNG_INTERN);
	}

	public isExplicitlyMandatory(uniqueName: string): boolean {
		const metaField: IMetaField = this._metaModel.getValue(IMetaKeys.MODEL_META_FIELD, uniqueName);
		return metaField.getValue(IMetaKeys.FIELD_EXPLICITLY_MANDATORY);
	}

	public getContainedElements(groupName: string): Set<string> {
		const result = new Set<string>();
		this.getContainedGroups(groupName).forEach((g: string) => result.add(g));
		this.getContainedFields(groupName).forEach((f: string) => result.add(f));
		return result;
	}

	public getContainedGroups(groupName: string): Set<string> {
		const metaContainer: IMetaGroup = this._metaModel.getValue(IMetaKeys.MODEL_META_GROUP, groupName);
		if (metaContainer == null) {
			return new Set<string>();
		}
		const groups: IMetaGroup[] = metaContainer.getValue(IMetaKeys.GROUP_SUBGROUP_LIST);
		const result = new Set<string>();
		groups.forEach((group: IMetaGroup) => {
			const name: string = group.getValue(IMetaKeys.GROUP_FULLNAME);
			result.add(name);
		});
		return result;
	}

	public getContainedFields(groupName: string): Set<string> {
		const metaContainer: IMetaGroup = this._metaModel.getValue(IMetaKeys.MODEL_META_GROUP, groupName);
		if (metaContainer == null) {
			return new Set<string>();
		}
		const fields: IMetaField[] = metaContainer.getValue(IMetaKeys.GROUP_FIELD_LIST);
		const result: Set<string> = new Set<string>();
		fields.forEach((field: IMetaField) => {
			const name: string = field.getValue(IMetaKeysInternal.FELD_UNIQUENAME);
			result.add(name);
		});
		return result;
	}

	public getInnermostRepeatableGroup(entityFullName: string): string | null {
		let path = entityFullName;
		let metaGroup: IMetaGroup;
		do {
			path = EntityUtil.getParentPath(path);
			metaGroup = this._metaModel.getValue(IMetaKeys.MODEL_META_GROUP, path);
		} while (metaGroup != undefined && metaGroup.getValue(IMetaKeys.GROUP_MAX_INDEX) === 1);
		return path === "" ? null : path;
	}

	public getDynamicEnumerationCalculator(): IDynEnumsIntern | undefined {
		return this._dynEnumSupplier != undefined ? this._dynEnumSupplier() : undefined;
	}

	public getRequiredCustomConditions(): Set<string> {
		const _result: Set<string> = this._metaModel.getValue(
			IMetaKeys.MODEL_APPLICATION_CONDITION
		);
		if (!_result) {
			return new Set();
		}
		return _result;
	}

	public getTimeZone(): string {
		return this._metaModel.getValue(IMetaKeysInternal.MODEL_TIME_ZONE);
	}

	public getModelId(): string {
		return this._metaModel.getValue(IMetaKeysInternal.MODEL_ID);
	}

}
