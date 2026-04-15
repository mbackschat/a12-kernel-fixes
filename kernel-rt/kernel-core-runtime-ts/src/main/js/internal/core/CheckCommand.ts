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
import type { Logger } from "@com.mgmtp.a12.utils/utils-logging";

import type { IIdentifier, IResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { RuleType, ValidationMode } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import {
	RuntimeFormalErrorEnum
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import { HashSet } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { ValidationLogger } from "../logging/ValidationLogger.js";
import { DarstellungsInformationIntern } from "../util/DarstellungsInformationIntern.js";
import { DatenImpl } from "../util/DatenImpl.js";
import { EntityUtil } from "../util/EntityUtil.js";
import { FeldUtil } from "../util/FeldUtil.js";
import { IFormaleFehlerConstants } from "../util/IFormaleFehlerConstants.js";
import { LocalizableUtil } from "../util/LocalizableUtil.js";
import { RtExternalIdentifier } from "../util/RtExternalIdentifier.js";
import { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";
import { ValidType } from "../util/ValidType.js";

import { IteratorWrapper } from "../util/IteratorWrapper.js";

import { FormalChecker } from "./FormalChecker.js";
import type { IMetaDataValidierung } from "./IMetaDataValidierung.js";
import type { IValidatorIntern } from "./IValidatorIntern.js";
import { ValidationCache } from "./ValidationCache.js";

/**
 * Kapselt den Code der einzelnen check-Kommandos.
 * Die formale Pruefung ist generisch für alle Felder und erfolgt in dieser Klasse.
 * Die abhängigen Prüfungen werden nach der formalen Prüfung angestoßen.
 */
export class CheckCommand {
	// Daten für eine Validierungsprüfung:
	protected _logger: ValidationLogger;
	protected _modus: ValidationMode;
	protected _darstellungsInfo: DarstellungsInformationIntern;

	protected _validator: IValidatorIntern;
	protected _metaData: IMetaDataValidierung;
	protected _validationCache: ValidationCache;
	protected _formalChecker: FormalChecker;

	// Globale Daten für eine Auswertung
	// Zähler für Anzahl gefundener Testfehler
	protected _anzTestFehler = 0;

	constructor(
		validator: IValidatorIntern | undefined,
		modus: ValidationMode | undefined,
		metaData: IMetaDataValidierung,
		logger: Logger
	) {
		this._logger = new ValidationLogger(logger);
		this._modus = modus == undefined ? new ValidationMode() : modus;
		this._darstellungsInfo = new DarstellungsInformationIntern(
			metaData.getDezimalTrenner(),
			this._modus.getDataFormats(),
			metaData.getBaseYearOrDefault(),
			metaData.getTimeZone(),
		);
		this._validator = validator ? validator : <IValidatorIntern>new Object();
		this._metaData = metaData;

		this._validationCache = new ValidationCache(this._metaData, this._darstellungsInfo, this._modus);
		this._formalChecker = new FormalChecker(this._metaData, this._darstellungsInfo, this._modus, this._isMeldeFehler())
	}

	/**
	 * Hilfsmethode zur formalen Pruefung der relevanten Felder.
	 * Die Menge alle fehlerhaften Felder werden in den validationCache eingefügt,
	 * da diese später nicht mehr bei den abhängigen Pruefungen überprüft werden sollen.
	 *
	 * Bei der Überprüfung der Hinweis- und Info-relevanten Felder sollen keine Fehler für die
	 * formale Prüfung ausgegeben werden. Die Prüfung muss aber durchgeführt werden,
	 * weil bei den Regel-Prüfungen nur auf formal korrekte Felder zugegriffen werden darf.
	 *
	 * Zur Formatüberprüfung zählt:
	 * 1. Ist das Feld spezifiziert?
	 *    (wird implizit über	{@Link IMetaDataValidierung.getIFormatDefinition} überprüft)
	 * 2. Überprüfung ob die Ebenenspezifikation korrekt ist.
	 * 3. Überprüfung ob das Format korrekt ist.
	 *
	 * @param relevantFields    prüfungsrelevante Felder falls Teilvalidierung , undefined bei Vollvalidierung.
	 * @param result    zum Speichern der Ergebnisse bei formalen Fehlern.
	 */
	public formalePruefung(
		relevantFields: HashSet<RtInternalIdentifier> | null,
		result: IResult,
	): void {
		const startMesspunkt: number = this._logger.startMesspunkt();
		const relevantFieldsCache: Map<string, Set<RtInternalIdentifier>> | null = relevantFields != null
			? this._makeRelevanteFelderCache(this._addAllParentGroups(relevantFields))
			: null;

		for (const idExtern of new IteratorWrapper(this._validationCache.getDaten().iterator())) {
			const isFieldDefined: boolean = this._metaData.isFeldDefiniert(idExtern.getName());
			const isGroupDefined: boolean = this._metaData.isGroupDefined(idExtern.getName());

			if (!isFieldDefined && !isGroupDefined) {
				this._validationCache.addUnbekanntesFeld(idExtern);
				continue;
			}

			if (!isFieldDefined) {
				// Special treatment for groups
				this._checkGroup(idExtern, result, relevantFieldsCache);
				continue;
			}

			const uniqueName: string = this._metaData.getUniqueName(idExtern.getName());
			const maxIndices: number[] = this._metaData.getMaxFieldIndices(uniqueName);
			if (!this._formalChecker.checkIndices(idExtern, maxIndices, result)) {
				continue;
			}
			if (this._logger.isTraceEnabled) {
				this._logger.trace("Field == " + DatenImpl.idIdToString(idExtern));
			}

			// nur prüfungsrelevante Felder werden überprüft
			const idIntern = FeldUtil.makeInternIdentifier(idExtern, this._metaData);
			if (!EntityUtil.isPruefungsRelevant(idIntern, relevantFieldsCache)) {
				continue;
			}

			const value: string | undefined = this._validationCache.getDaten().getValue(idExtern);
			if (!value || value.length === 0) {
				// Leerstring oder undefined value entsprechen "nicht angegeben"
				continue;
			}

			const fieldValueCheck = this._formalChecker.checkAndConvertFieldValue(idIntern, idExtern, value, result);
			if (fieldValueCheck.validity === ValidType.Invalid) {
				this._validationCache.addInvalidFieldValue(idIntern, RtExternalIdentifier.ensureImpl(idExtern));
			} else {
				if (fieldValueCheck.validity === ValidType.Test) {
					this._anzTestFehler++;
				}
				if (fieldValueCheck.normalizedValue !== undefined && fieldValueCheck.internalXmlValue !== undefined) {
					this._validationCache.addValidFieldValueIfAbsent(idIntern, fieldValueCheck.normalizedValue, fieldValueCheck.internalXmlValue);
				} else { // considered valid, but the value should not be interned
					this._validationCache.addPseudoFieldValueIfAbsent(idIntern);
				}
			}
		} // Ende Schleife über alle Formatprüfungen

		this._validationCache.addDefaultValuesForIndexFieldsIfNecessary();

		/**
		 * Die unbekannten Felder die in den Daten oder in den prüfungsrelevanten Feldern gefunden
		 * wurden, werden der Ergebnismenge hinzugefügt.
		 */
		this._addUnbekannteFelderToErgebnis(result);

		this._logger.logMessung("Die formale Prüfung benötigt ", startMesspunkt);
	}

	private _checkGroup(
		idExtern: IIdentifier,
		ergebnisse: IResult,
		relevanteFelderCache: Map<string, Set<RtInternalIdentifier>> | null,
	): void {
		const maxIndices: number[] = this._metaData.getMaxGroupIndices(idExtern.getName());
		if (!this._formalChecker.checkIndices(idExtern, maxIndices, ergebnisse)) {
			return;
		}
		const wert: string | undefined = this._validationCache.getDaten().getValue(idExtern);
		if (wert == undefined || wert.length === 0) {
			// Also for groups: non existent or empty values mean N/A
			return;
		}

		const idIntern = new RtInternalIdentifier(idExtern.getName(), idExtern.getIndices());

		if (!EntityUtil.isPruefungsRelevant(idIntern, relevanteFelderCache)) {
			return;
		}

		this._validationCache.addGroupIfAbsent(idIntern);
	}

	private _addUnbekannteFelderToErgebnis(ergebnisse: IResult): void {
		const unbekannteFelder: Map<string, RtExternalIdentifier> =
			this._validationCache.getUnbekannteFelder();
		for (const [, value] of unbekannteFelder) {
			this._formalChecker.addFormalError(
				ergebnisse,
				new RtExternalIdentifier(
					value.getName(),
					Array.from({ length: value.getIndices().length }, () => 0)),
				IFormaleFehlerConstants.FEHLER_FELD_UNBEKANNT,
				LocalizableUtil.createLocalizableFormalError(RuntimeFormalErrorEnum.FEHLER_FELD_UNBEKANNT)
			);
		}
	}

	/**
	 * Erzeugt aus der Menge der relevanten Felder eine Cache-Struktur, die dann für die
	 * Prüfung, ob ein Feld prüfungsrelevant ist, verwendet wird.
	 *
	 * @param relevanteFelder
	 * @return aufgebaute Cache-Struktur oder undefined falls Vollvalidierung
	 */
	protected _makeRelevanteFelderCache(
		relevanteFelder: Set<RtInternalIdentifier> | null
	): Map<string, Set<RtInternalIdentifier>> | null {
		if (!relevanteFelder) {
			// für Vollvalidierung ist relevanteFelder undefined, daher muss kein Cache aufgebaut werden.
			return null;
		}
		const result: Map<string, HashSet<RtInternalIdentifier>> = new Map<
			string,
			HashSet<RtInternalIdentifier>
		>();
		for (const id of relevanteFelder) {
			let set: HashSet<RtInternalIdentifier> | undefined = result.get(id.getName());
			if (set == undefined) {
				set = new HashSet<RtInternalIdentifier>();
				result.set(id.getName(), set);
			}
			set.add(id);
		}
		return result;
	}

	private _addAllParentGroups(
		relevantIds: HashSet<RtInternalIdentifier>
	): HashSet<RtInternalIdentifier> {
		const result = new HashSet<RtInternalIdentifier>();
		relevantIds.forEach((id: RtInternalIdentifier) => {
			result.add(id);
			let parentId = RtInternalIdentifier.createParentId(id, this._metaData.getPfadName(id.getName()));
			while (parentId != null) {
				if (!result.tryAdd(parentId)) {
					break;
				}
				parentId = RtInternalIdentifier.createParentId(parentId, parentId.getName());
			}
		});
		return result;
	}

	private _isMeldeFehler(): boolean {
		return (
			RuleType.ALL === this._modus.getRuleType() || RuleType.ERROR === this._modus.getRuleType()
		);
	}
}
