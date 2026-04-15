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

import type { HashSet, IIdentifier, IMetaRule } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { Regel_t } from "../formatdef/Regel_t.js";
import { IMetaKeysInternal } from "../util/IMetaKeysInternal.js";
import { LocaleUtil } from "../util/LocaleUtil.js";
import { LocalizableUtil } from "../util/LocalizableUtil.js";
import type { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";

import { MetaEntity } from "./MetaEntity.js";
import type { MetaModelImpl } from "./MetaModelImpl.js";

/**
 * Implementiert die Schnittstelle IMetaRule
 */
export class MetaRegel extends MetaEntity implements IMetaRule, IMetaKeysInternal {
	private static _schluessel: Set<string> = new Set<string>();
	private static _internalKeys: Set<string> = new Set<string>();
	private _metaModel: MetaModelImpl;

	/**
	 * Don't use this. This is just for a static initialization.
	 */
	static _constructor = (() => {
		MetaRegel._schluessel.add(IMetaKeys.RULE_NAME);
		MetaRegel._schluessel.add(IMetaKeys.RULE_FULL_NAME);
		MetaRegel._schluessel.add(IMetaKeys.RULE_ERROR_CODE);
		MetaRegel._schluessel.add(IMetaKeys.RULE_TYPE);
		MetaRegel._schluessel.add(IMetaKeys.RULE_ERROR_FIELD);
		MetaRegel._schluessel.add(IMetaKeys.RULE_REF_ROOTGROUP_OMISSION_ERROR);
		MetaRegel._schluessel.add(IMetaKeys.RULE_ANNOTATION);

		MetaRegel._internalKeys.add(IMetaKeysInternal.REGEL_FEHLERTEXT);
		MetaRegel._internalKeys.add(IMetaKeysInternal.REGEL_FEHLERTEXTE);
		MetaRegel._internalKeys.add(IMetaKeysInternal.REGEL_PARAMETER_MAPPING);
		MetaRegel._internalKeys.add(IMetaKeysInternal.REGEL_REFFELDIDS_INTERN);
		MetaRegel._internalKeys.add(IMetaKeysInternal.REGEL_REFFELDIDS_AUSLASSUNG_INTERN);
	})();

	constructor(
		metaModel: MetaModelImpl,
		r: Regel_t,
		refFeldIdsIntern: HashSet<RtInternalIdentifier>,
		refIdentifierAuslassungIntern: HashSet<RtInternalIdentifier>
	) {
		super(MetaRegel._schluessel, MetaRegel._internalKeys);
		this._metaModel = metaModel;
		this.addWert(IMetaKeys.RULE_NAME, r.name);
		this.addWert(IMetaKeys.RULE_FULL_NAME, r.pfadName);
		this.addWert(IMetaKeys.RULE_ERROR_CODE, r.fehlercode);
		this.addWert(IMetaKeysInternal.REGEL_FEHLERTEXT, r.fehlertexte);
		this.addWert(IMetaKeysInternal.REGEL_PARAMETER_MAPPING, r.parameterMapping);
		this.addWert(IMetaKeys.RULE_TYPE, r.regelArt);
		this.addWert(
			IMetaKeys.RULE_ERROR_FIELD,
			metaModel.getValue(IMetaKeys.MODEL_META_FIELD, r.fehlerFeld)
		);
		this.addWert(IMetaKeysInternal.REGEL_REFFELDIDS_INTERN, refFeldIdsIntern);
		this.addWert(
			IMetaKeysInternal.REGEL_REFFELDIDS_AUSLASSUNG_INTERN,
			refIdentifierAuslassungIntern
		);
		this.addWert(IMetaKeys.RULE_REF_ROOTGROUP_OMISSION_ERROR, r.getRefAuslassungsVordrucke());

		this.addWert(IMetaKeys.RULE_ANNOTATION, undefined); // will be overwritten by additional values if any annotation is provided.

		const internalKeys: Set<string> = r.internalAdditionalKeys;
		for (const key of r.additionalKeys) {
			if (internalKeys.has(key)) {
				this.addAdditionalInternalKey(key);
			} else {
				this.addAdditionalKey(key);
			}
			this.addWert(key, r.getAdditionalValue(key));
		}

	}

	public equals(other: any): boolean {
		if (this === other) {
			return true;
		}
		if (!(other instanceof MetaRegel)) {
			return false;
		}

		// der erste Parameter reicht für die Identifikation
		return this.getValue(IMetaKeys.RULE_FULL_NAME) === other.getValue(IMetaKeys.RULE_FULL_NAME);
	}

	// is been used instead of javas equals method
	public toString(): string {
		return this.getValue(IMetaKeys.RULE_FULL_NAME);
	}

	public getValue(key: string, ...parameter: any[]): any {
		if (IMetaKeys.RULE_ANNOTATION === key) {
			return this._getAnnotation(key, parameter);
		}
		// Für die in der Regel referenzierten Felder bzw. Felder, die Auslassungfehler erzeugen können, werden
		// noch die Indices umgesetzt, so dass sie zum übergebenen aktuellen Fehler-IIdentifier passen.
		// Dazu werden im Falle einer Iteration die Indices auf den konkreten Index des Fehlerfeldes gesetzt.
		// Die beiden Schlüssel referenzieren beide IIdentifier-Mengen und können daher gleichartig bearbeitet werden.
		if (
			IMetaKeysInternal.REGEL_REFFELDIDS_INTERN === key ||
			IMetaKeysInternal.REGEL_REFFELDIDS_AUSLASSUNG_INTERN === key
		) {
			if (parameter.length === 0) {
				// For internal use return the unresolved entities
				const result: Set<IIdentifier> = super.getValue(key);
				return result;
			}
			throw new Error(
				"As of ER-30, the keys ReferenzierteFeldIdentifierIntern and " +
				"RefFeldIdentifierAuslassungsFehlerIntern are not supported any more!"
			);
		}

		if (IMetaKeysInternal.REGEL_FEHLERTEXT === key) {
			const fehlertexte: Map<string, string> = <Map<string, string>>super.getValue(key);
			if (parameter == undefined || parameter.length === 0) {
				throw Error("No language found. One language has to be specified.");
			} else {
				if (!this.checkParameterAngegeben(key, 1, parameter)) {
					throw new Error("The given parameter is invalid.");
				}
				const sprache: string = new LocaleUtil(
					<string[]>this._metaModel.getValue(IMetaKeys.MODEL_LANGUAGES)
				).deriveLocaleIfPossible(
					parameter[0],
					"The additional parameter for the key '" +
					IMetaKeysInternal.REGEL_FEHLERTEXT +
					"' must be of type Locale."
				);

				return fehlertexte.get(sprache);
			}
		}

		if (IMetaKeysInternal.REGEL_FEHLERTEXTE === key) {
			const fehlertexte: Map<string, string> = <Map<string, string>>super.getValue(IMetaKeysInternal.REGEL_FEHLERTEXT);
			return LocalizableUtil.defaultsFromMap(fehlertexte);
		}
		return super.getValue(key, ...parameter);
	}
}
