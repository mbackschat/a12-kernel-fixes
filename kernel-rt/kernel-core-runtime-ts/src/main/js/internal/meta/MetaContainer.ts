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

import type { IMetaField, IMetaGroup, IMetaRule } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { Container_t } from "../formatdef/Container_t.js";
import { EntityUtil } from "../util/EntityUtil.js";
import { IMetaKeysInternal } from "../util/IMetaKeysInternal.js";

import { MetaEntity } from "./MetaEntity.js";

/**
 * Implementiert die Schnittstelle [IMetaGroup](
 * ../../../kernel-core-runtime-api-ts/interfaces/_meta_imetagroup_.imetagroup.html)
 *
 */
export class MetaContainer extends MetaEntity implements IMetaGroup {
	private _metaContainerListe: IMetaGroup[] = new Array<IMetaGroup>();
	private _metaFeldListe: IMetaField[] = new Array<IMetaField>();
	private _metaRegelListe: IMetaRule[] = new Array<IMetaRule>();
	private _indexFields: IMetaField[] = new Array<IMetaField>();

	// Only needed during initialization
	private _indexFieldNames: string[] = new Array<string>();

	private static _schluessel: Set<string> = new Set<string>();
	private static _internalKeys: Set<string> = new Set<string>();

	/**
	 * Don't use this. This is just for a static initialization.
	 */
	static _constructor = (() => {
		MetaContainer._schluessel.add(IMetaKeys.GROUP_FULLNAME);
		MetaContainer._schluessel.add(IMetaKeys.GROUP_SHORT_NAME);
		MetaContainer._schluessel.add(IMetaKeys.GROUP_MAX_INDEX);
		MetaContainer._schluessel.add(IMetaKeys.GROUP_SUBGROUP_LIST);
		MetaContainer._schluessel.add(IMetaKeys.GROUP_FIELD_LIST);
		MetaContainer._schluessel.add(IMetaKeys.GROUP_RULE_LIST);
		MetaContainer._schluessel.add(IMetaKeys.GROUP_INDEX_FIELDS);
		MetaContainer._schluessel.add(IMetaKeys.GROUP_ANNOTATION);
		MetaContainer._internalKeys.add(IMetaKeysInternal.CONTAINER_MAXINDICES);
	})();

	constructor(ct: Container_t) {
		super(MetaContainer._schluessel, MetaContainer._internalKeys);
		this.addWert(IMetaKeys.GROUP_FULLNAME, ct.fullName);
		this.addWert(IMetaKeys.GROUP_SHORT_NAME, EntityUtil.getEntityName(ct.fullName));
		this.addWert(IMetaKeys.GROUP_MAX_INDEX, ct.maxIndex);
		this.addWert(IMetaKeys.GROUP_SUBGROUP_LIST, this._metaContainerListe);
		this.addWert(IMetaKeys.GROUP_FIELD_LIST, this._metaFeldListe);
		this.addWert(IMetaKeys.GROUP_RULE_LIST, this._metaRegelListe);
		// The max indices are set via setMaxIndices
		this.addWert(IMetaKeysInternal.CONTAINER_MAXINDICES, undefined);
		// The index fields are added via addFeld
		this.addWert(IMetaKeys.GROUP_INDEX_FIELDS, this._indexFields);

		this.addWert(IMetaKeys.GROUP_ANNOTATION, undefined); // will be overwritten by additional values if any annotation is provided.

		const internalKeys: Set<string> = ct.internalAdditionalKeys;
		for (const [key, value] of ct.additionalKeyValues) {
			if (internalKeys.has(key)) {
				this.addAdditionalInternalKey(key);
			} else {
				this.addAdditionalKey(key);
			}
			this.addWert(key, value);
		}

		this._indexFieldNames = ct.indexFields;
	}

	public addFeld(feld: IMetaField): void {
		const feldName: string = feld.getValue(IMetaKeys.FIELD_SHORT_NAME);
		const fullName: string = feld.getValue(IMetaKeys.FIELD_FULLNAME);
		if (
			this._indexFieldNames !== undefined &&
			(this._indexFieldNames.indexOf(feldName) >= 0 || this._indexFieldNames.indexOf(fullName) >= 0)
		) {
			this._indexFields.push(feld);
		}
		this._metaFeldListe.push(feld);
	}

	public addRegel(regel: IMetaRule): void {
		this._metaRegelListe.push(regel);
	}

	public addIMetaGroup(cont: IMetaGroup): void {
		this._metaContainerListe.push(cont);
	}

	public setMaxIndices(parentContainer: MetaContainer | undefined, maxIndex: number): void {
		let maxIndices: number[];
		if (!parentContainer) {
			maxIndices = [];
		} else {
			const parentIndices: number[] = parentContainer.getValue(IMetaKeysInternal.CONTAINER_MAXINDICES);
			maxIndices = parentIndices.slice(0);
		}
		maxIndices.push(maxIndex);
		this.addWert(IMetaKeysInternal.CONTAINER_MAXINDICES, maxIndices);
	}

	/**
	 * Zugriff auf die Werte: für die Werte in Collections werden die Rückgabewerte kopiert.
	 * Für alle anderen Zugriffe wird die Zugriffsmethode der Super-Klasse verwendet.
	 */
	// TODO: Fix this the next time the file is edited.

	public getValue(key: string, ...parameter: any[]): any {
		if (IMetaKeys.GROUP_ANNOTATION === key) {
			return super._getAnnotation(key, parameter);
		}
		if (IMetaKeys.GROUP_SUBGROUP_LIST === key) {
			// kopiere Werte:
			const werte: IMetaGroup[] = super.getValue(IMetaKeys.GROUP_SUBGROUP_LIST);
			const result: IMetaGroup[] = new Array<IMetaGroup>();
			for (const m of werte) {
				result.push(m);
			}
			return result;
		}
		if (IMetaKeys.GROUP_RULE_LIST === key) {
			// kopiere Werte:
			const werte: IMetaRule[] = super.getValue(IMetaKeys.GROUP_RULE_LIST);
			const result: IMetaRule[] = new Array<IMetaRule>();
			for (const m of werte) {
				result.push(m);
			}
			return result;
		}
		if (IMetaKeys.GROUP_FIELD_LIST === key) {
			// kopiere Werte:
			const werte: IMetaField[] = super.getValue(IMetaKeys.GROUP_FIELD_LIST);
			const result: IMetaField[] = new Array<IMetaField>();
			for (const m of werte) {
				result.push(m);
			}
			return result;
		}
		if (IMetaKeys.GROUP_INDEX_FIELDS === key) {
			return <any[]>super.getValue(key);
		}
		return super.getValue(key);
	}

	public equals(other: any): boolean {
		return this === other;
	}
}
