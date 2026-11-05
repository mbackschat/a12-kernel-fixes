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
import type {
	Localizable,
	LocalizableArgs,
	LocalizablePlaceholder,
	Placeholder
} from "@com.mgmtp.a12.utils/utils-localization";


import {
	DIVISION_BY_ZERO_INVALID,
	ERROR_DURING_DATA_CHECK_EVAL,
	RuntimeFormalErrorEnum
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import type { ICause, IMetaField, IResult, ValidationMode } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { CustomConditionException, ErrorType, HashSet, IIdentifier, SeverityType, ValidatorException } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";
import { FehlerHandler } from "../formatdef/error/FehlerHandler.js";
import type { ValidationLogger } from "../logging/ValidationLogger.js";
import { ArrayUtils } from "../util/ArrayUtils.js";
import { Constants } from "../util/Constants.js";
import { EntityIterator } from "../util/EntityIterator.js";
import { EntityUtil } from "../util/EntityUtil.js";
import { ErrorCause } from "../util/ErrorCause.js";
import { IMetaKeysInternal } from "../util/IMetaKeysInternal.js";
import type { IterationState } from "../util/IterationState.js";
import { MetaDataUtilValidierung } from "../util/MetaDataUtilValidierung.js";
import { ReadOnlyEmptyHashSet } from "../util/ReadOnlyEmptyHashSet.js";
import { RtExternalIdentifier } from "../util/RtExternalIdentifier.js";
import { RtIdentifierTemplate } from "../util/RtIdentifierTemplate.js";
import { RtIdentifierTemplateLayerSpec } from "../util/RtIdentifierTemplateLayerSpec.js";
import { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";

import { IFormaleFehlerConstants } from "../util/IFormaleFehlerConstants.js";
import type { LocalizableDefaults } from "../util/LocalizableUtil.js";
import { LocalizableUtil } from "../util/LocalizableUtil.js";

import type { FormalChecker } from "./FormalChecker.js";
import type { IMetaDataValidierung } from "./IMetaDataValidierung.js";
import { RuntimeController } from "./RuntimeController.js";
import type { ValidationCache } from "./ValidationCache.js";
import { ValidierungsErgebnis } from "./ValidierungsErgebnis.js";

export enum PruefErgebnisTyp {
	Fehler,
	Hinweis,
	Info
}

export class MainValidatorController extends RuntimeController {
	/**
	 * Die 3 folgenden Felder werden für jede Regel neu gesetzt.
	 */
	private _regelName = "";
	private _pruefErgebnisTyp: PruefErgebnisTyp = PruefErgebnisTyp.Fehler;

	/**
	 * Erzeugt ein Validator-Objekt mit den übergebenen Nutzdaten.
	 *
	 * @param ergebnisse	Ergebnis-Objekt zu dem die gefundenen Fehler, Hinweise und Infos hinzugefügt werden
	 * @param valCache 		Cache für die Nutzdaten und die Optimierungsstrukturen
	 * @param logger		Logger Objekt
	 * @param modus			Parameter für die Validierung
	 * @param metaData		bietet Zugriff auf die Metadaten
	 */
	constructor(
		ergebnisse: IResult,
		valCache: ValidationCache, // NOTE: is seems like this is not merely a cache, but is actually used to pass the data to this mainvalidatorcontroller
		logger: ValidationLogger,
		modus: ValidationMode,
		metaData: IMetaDataValidierung,
		private _formalChecker: FormalChecker
	) {
		super(ergebnisse, valCache, logger, modus, metaData);
	}

	/**
	 * Testet die Applikationsbedingung: Im Falle eines Fehlers wird immer ein Werte-Fehler
	 * (d.h. TRUE_WF) zurückgegeben.
	 * Zur Übergabe an die externe Schnittstelle müssen die externen Repräsentationen verwendet
	 * werden.
	 * Dies bedeutet: Verwendung der externen Namen.
	 * Je nachdem, ob die alte oder die neue Validierungsschnittstelle verwendet wird, wird die
	 * alte oder die neue Schnittstelle für die Applikationsbedingung verwendet. Die Daten und die
	 * relevanten Felder sind jeweils so gespeichert worden, wie sie für die Validierung übergeben
	 * wurden
	 * und müssen daher nicht mehr konvertiert werden. Die fehlerhaften Felder und das Fehlerfeld
	 * werden
	 * zur Laufzeit ermittelt und müssen daher in die externen Schnittstellen konvertiert werden.
	 *
	 * @param applName 	Name der Applikationsbedingung
	 * @param id 	Fehlerfeld der Regel
	 * @return TRUE_WF oder FALSE, je nachdem ob die Applikationsbedingung true oder false geliefert
	 *         hat.
	 * @throws ValidatorException
	 *             falls die verwendete Bedingung nicht übergeben wurde.
	 *             Errors, die von der Applikationsbedingung geworfen werden,
	 *             werden in eine ValidatorException eingewickelt.
	 */
	public applikationsBedingung(applName: string, id: RtInternalIdentifier): ValidierungsErgebnis {
		// Konvertiere das Fehlerfeld in das externe Fehlerfeld zurück.
		const externId: RtExternalIdentifier = this.validationCache.convert2Extern(id);

		// get the ICustomCondition passed by the project
		const applBed = this.modus.customConditionFactory?.createCustomCondition(applName);

		// Wenn keine ICustomCondition mit dem angegebenen Namen vorhanden ist, werfe Exception
		if (!applBed) {
			const mess: string =
				"Custom condition '" + applName + "' for rule '" + this._regelName + "' missing.";
			this._throwValidatorException(mess);
		}

		// Bei Teilvalidierung nach 3-Wert-Logik werden prüfungsrelevante Felder übergeben.
		const relevanteFelder: Set<IIdentifier> | undefined =
			this.validierungArt.isValidationPartWith3ValueLogic()
				? this.validationCache.getAllPruefungsrelevanteEntitiesExtern()
				: undefined;
		// Fehlerhafte Felder werden mit übergeben, aber im externen Format und müssen daher konvertiert werden.
		const fehlerhafteFelder: Set<IIdentifier> =
			this.validationCache.getAllFehlerhafteFelderExtern();

		try {
			// Aufruf des externen Interface.
			return applBed?.check(
				this.validationCache.getDaten(),
				relevanteFelder,
				fehlerhafteFelder,
				externId
			)
				? ValidierungsErgebnis.TRUE_WF
				: ValidierungsErgebnis.FALSE_OR_UNKNOWN;
		} catch (error: any) {
			if (error instanceof CustomConditionException) {
				throw error
			}
			// alle von der ApplikationsBedingung geworfenen
			// Fehler werden aufgefangen und von einer ValidatorException gewrapped.
			const mess: string =
				"Custom condition '" +
				applName +
				"' in rule '" +
				this._regelName +
				"' throws the following exception: '" +
				error +
				"'.";
			this._throwValidatorException(mess, error);
		}
		// tempcobertura::0.00::tempcobertura
		return ValidierungsErgebnis.FALSE_OR_UNKNOWN; // dummy
	}

	/**
	 * Hilfsmethode zur Erzeugung einer Validator-Exception
	 */
	private _throwValidatorException(message: string, error?: Error | unknown): void {
		throw new ValidatorException(message, error);
	}

	public logRegel(regelName: string): void {
		this.logger.logRegel(regelName);
	}

	public get validatorMessages(): IResult {
		return this.pruefErgebnis;
	}

	/**
	 * Setter Methode für die Teilvalidierung nach 3-Wert-Logik. Zur Performance-Steigerung wird
	 * überprüft, ob bei einer Teilvalidierung nach 3-Wert-Logik jeweils
	 * alle Instanzen der jeweiligen Kontextebenen (inkl. laufende Nummer Vordruck
	 * = 1. Kontextebene) aller möglichen Felder gleichbehandelt werden können.
	 *
	 * Ist dies der Fall, so kann auf der jeweiligen Verschachtelungstiefe
	 * optimiert werden. Damit wird ein nicht zu unterschätzender Performance-Gewinn
	 * realisiert.
	 *
	 * Um diese Analyse nicht andauernd machen zu müssen, wird dies nur beim ersten Aufruf
	 * (Setzen der Teilvalidierung) gemacht.
	 *
	 * Um das Analyse-Ergebnis aufzubewahren werden dafür bei der Instanz der Klasse
	 * ValidierungArt je Ebene einzelne Flags für eine Gleichbehandlung gesetzt.
	 *
	 * Die jeweiligen Bedingungen sind ebenfalls bei der Klasse {@Link ValidierungArt}
	 * dokumentiert.
	 *
	 */

	public setIsValidationPartWith3ValueLogic(): void {
		const ebenenGleich: boolean[] = this.createArrayFuerEbenenGleich();

		for (const id of this.validationCache.getAllRelevantEntitiesIntern()) {
			const isField: boolean = this.metaData.isUniqueFieldName(id.getName());
			const indices: number[] = id.getIndices();
			const maxLevel: number = isField ? id.getIndices().length - 1 : id.getIndices().length;
			for (let i = 0; i < maxLevel; ++i) {
				if (!ebenenGleich[i]) {
					continue; // Wiederholungsebene ist bereits nicht gleich.
				}
				const maxIndex = this.metaData.getMaxFieldIndices(id.getName())[i];
				const currentIndex: number = indices[i];
				if (maxIndex > 1 && currentIndex !== IIdentifier.ALL_INDICES) {
					// Wiederholbarkeit > 1
					ebenenGleich[i] = false;
				}
			}
			// index for fields is always 1
		}
		this.validierungArt.setValidationPartWith3ValueLogic(ebenenGleich);
		this.validationCache.setValidationPartWith3ValueLogic();
	}

	/**
	 * Bereitet den MainValidatorController auf die folgende
	 * Regelprüfung vor.
	 */
	public initFuerRegelpruefung(
		regelName: string,
		severity: PruefErgebnisTyp
	): void {
		this._regelName = regelName;
		this._pruefErgebnisTyp = severity;

		this.logRegel(regelName);
	}

	private _substParameters(
		idx: IterationState,
		parameterMapping: Map<string, string>): LocalizableArgs {
		return LocalizableUtil.createLocalizableArgs(parameterMapping, (p) => this._substParameter(p, idx));
	}

	/**
	 * Führt die Ersetzung für einen Parameter im Fehlertext durch.
	 */
	private _substParameter(parameter: string, idx: IterationState): Placeholder {
		if (!parameter) {
			return LocalizableUtil.createPlainPlaceholder("$");
		}

		parameter = parameter.trim();

		if (parameter.startsWith("#")) {
			return LocalizableUtil.createPlainPlaceholder(this._substIndexNumberParam(parameter, idx));
		}

		/*
		 * Parameter 'feldUniqueName@e1@e2...@en'
		 * bzw. 'feldUniqueName@e1@e2...@en.Wert'
		 * bzw. 'feldUniqueName@e1@e2...@en->categoryName'
		 * bzw. 'index(feldUniqueName).Wert'
		 * bzw. 'index(feldUniqueName)->categoryName' generiert aus 'feldBeschreibung'
		 */
		const fieldSpec: string = this._getFieldSpec(parameter);
		if (parameter.startsWith(Constants.ERROR_TEXT_PARAM_4_INDEX_FIELD_REF)) {
			return this._substIndexFieldRef(parameter, idx, fieldSpec);
		}
		const fIt: EntityIterator = new EntityIterator(
			RtIdentifierTemplate.fromStringRepresentation(fieldSpec),
			idx
		);
		const idParamIntern: RtInternalIdentifier = fIt.get();
		if (
			parameter.indexOf(
				Constants.ERROR_TEXT_PARAM_4_FIELD_VALUE,
				parameter.length - Constants.ERROR_TEXT_PARAM_4_FIELD_VALUE.length
			) !== -1
		) {
			// Rückgabe ohne die Dollarzeichen, da Wertersetzung
			return this._getReplacementForParam(idParamIntern);
		} else if (parameter.includes(Constants.CATEGORY_VALUE_ACCESS)) {
			const value: string | undefined = this.validationCache.getXMLValue(idParamIntern);
			return LocalizableUtil.createPlainPlaceholder(value ?? "");
		} else {
			return LocalizableUtil.createLocalizableFieldNamePlaceholder(idParamIntern.getName(), this.metaData);
		}
	}

	private _substIndexNumberParam(parameter: string, idx: IterationState) {
		/*
		 * Parameter $#n$ wird zur Laufzeit ersetzt durch den n-ten Index des aktuellen
		 * Fehlerfeldes.
		 * Bei der Codegenerierung wurde der Parameter aus '#<kontextPfad>' generiert.
		 */
		const regexp = new RegExp("^(?:\\d+)$");

		if (regexp.test(parameter.substring(1))) {
			const ebene = Number(parameter.substring(1));
			// Rückgabe ohne Dollarzeichen, da Wertersetzung
			return String(idx.getIndex(ebene));
		} else {
			// #<group>[#<semanticIdx>]
			const it: EntityIterator = new EntityIterator(
				RtIdentifierTemplate.fromStringRepresentation(parameter.substring(1)),
				idx
			);
			const semIdxId: RtInternalIdentifier = it.get();
			const resolvedId: RtInternalIdentifier = this.validationCache.resolveSemanticIndex(semIdxId);
			const indicesTemp: number[] = resolvedId.getIndices();
			const result = indicesTemp[indicesTemp.length - 1];
			if (result === IIdentifier.UNKNOWN) {
				return "?";
			}
			return String(result);
		}
	}

	private _substIndexFieldRef(parameter: string, idx: IterationState, fieldSpec: string): Placeholder {
		const indexFieldValue: string | undefined = this._getIndexFieldValue(fieldSpec, idx);
		if (!indexFieldValue) {
			// this can happen if the index field is not passed by the caller or is not in the relevant fields
			return LocalizableUtil.createPlainPlaceholder("");
		}
		if (parameter.endsWith(Constants.ERROR_TEXT_PARAM_4_FIELD_VALUE)) {
			// case 'index(feldUniqueName).Wert'
			return LocalizableUtil.createFieldValuePlaceholder(fieldSpec, this.metaData, indexFieldValue);
		} else {
			// case 'index(feldUniqueName)->categoryName'
			const categoryValue: string | undefined = MetaDataUtilValidierung.getValue4Category(
				this.metaData,
				fieldSpec,
				indexFieldValue,
				parameter.substring(
					parameter.indexOf(Constants.CATEGORY_VALUE_ACCESS) +
					Constants.CATEGORY_VALUE_ACCESS.length
				)
			);
			// categoryValue == null can happen if there is a formal error for the index field
			return LocalizableUtil.createPlainPlaceholder(categoryValue ?? "");
		}
	}

	private _getIndexFieldValue(fieldUniqueName: string, idx: IterationState): string | undefined {
		if (idx.getValue() != undefined) {
			// case 'parallel iteration'
			return idx.getValue();
		} else {
			// case 'iteration'
			const f: IMetaField = this.metaData.getMetaField(fieldUniqueName);
			const maxIndices: number[] = f.getValue(IMetaKeysInternal.FELD_MAXINDICES);
			const indexFieldIndices: number[] = ArrayUtils.copyOf(
				idx.getIndexes(),
				0,
				maxIndices.length
			);
			// the line iteration has to be always 1 for A12-DVs
			indexFieldIndices[indexFieldIndices.length - 1] = 1;
			const indexFieldId: RtInternalIdentifier = new RtInternalIdentifier(
				fieldUniqueName,
				indexFieldIndices
			);
			return this.validationCache.getXMLValue(indexFieldId);
		}
	}

	private _getFieldSpec(parameter: string): string {
		if (parameter.startsWith(Constants.ERROR_TEXT_PARAM_4_INDEX_FIELD_REF)) {
			return parameter.substring(
				Constants.ERROR_TEXT_PARAM_4_INDEX_FIELD_REF.length,
				parameter.indexOf(")")
			);
		} else if (parameter.endsWith(Constants.ERROR_TEXT_PARAM_4_FIELD_VALUE)) {
			return parameter.substring(
				0,
				parameter.length - Constants.ERROR_TEXT_PARAM_4_FIELD_VALUE.length
			);
		} else {
			return parameter;
		}
	}

	/**
	 * Returns according to the given identifier the value which should substitute the parameter
	 * $&lt;identifier&gt;.Wert$ in a rule error text. This value should be suitable for being
	 * displayed (for example in a html site) and therefore -- the method IData.getValueForDisplay is
	 * called -- (in this way the caller of the validation is responsible for a correct
	 * implementation). If no value is found, a default value will be returned.
	 */
	private _getReplacementForParam(idInternal: RtInternalIdentifier): Placeholder {
		// we are about to call an external implementation and therefore we need an external id
		let value: string | number | undefined = this.validationCache.getXMLValue(idInternal);
		if (!value) {
			// Kein Wert gefunden, nehme default Wert
			const f: IFormatDefinition = this.metaData.getIFormatDefinition(idInternal.getName());
			if (f == undefined) {
				return LocalizableUtil.createPlainPlaceholder("");
			}
			value = f.getDefaultValue(this.metaData, null);
			if (typeof (value) === "string" && !value) {
				return LocalizableUtil.createPlainPlaceholder("");
			}
		}
		return LocalizableUtil.createFieldValuePlaceholder(idInternal.getName(), this.metaData, value);
	}

	public addValidatorMessage(
		fehlerFeldName: string,
		idx: IterationState,
		key: string,
		fehlerTyp?: ErrorType
	): void;
	public addValidatorMessage(
		fehlerFeldName: string,
		idx: IterationState,
		key: string,
		e: Error
	): void;
	public addValidatorMessage(
		fehlerFeldName: string,
		idx: IterationState,
		key: string,
		errorTypeOrError?: ErrorType | Error
	): void {
		let fehlerTyp: ErrorType;
		let errorTexts: LocalizableDefaults;
		if (errorTypeOrError && errorTypeOrError instanceof Error) {
			// method 3
			errorTexts = FehlerHandler.getAllValues(ERROR_DURING_DATA_CHECK_EVAL);
			const localizableKey = LocalizableUtil.createFormalErrorKeyFromString(ERROR_DURING_DATA_CHECK_EVAL);
			const errorMessage: string = errorTypeOrError.message;
			const args: { [placeholder: string]: Placeholder; } = {};
			if ("Division by zero" === errorMessage) {
				const errorDefaults = FehlerHandler.getAllValues(DIVISION_BY_ZERO_INVALID);
				const localizablePlaceholder: LocalizablePlaceholder = {
					type: "localizable",
					value: undefined,
					properties: [{
						key: LocalizableUtil.createFormalErrorKeyFromString(DIVISION_BY_ZERO_INVALID),
						defaults: errorDefaults
					}]
				}
				args["error"] = localizablePlaceholder;
			} else {
				args["error"] = LocalizableUtil.createPlainPlaceholder(errorMessage);
			}
			const localizable: Localizable = {
				key: localizableKey,
				args,
				defaults: errorTexts,
			};
			fehlerTyp = ErrorType.VALUE_ERROR;
			this._addValidatorMessage(
				fehlerFeldName,
				idx,
				key,
				fehlerTyp,
				localizable,
				false,
				ReadOnlyEmptyHashSet.instance<number[]>()
			);
		} else {
			// since ErrorType.<value> returns a number and 0 is a valid value for the enum
			// we can not do if(!errorTypeOrError)
			if (errorTypeOrError === undefined) {
				fehlerTyp = ErrorType.VALUE_ERROR;
			} else {
				fehlerTyp = errorTypeOrError;
			}
			this.addValidatorMessageWithReplacement4RepetitionNotUnique(
				fehlerFeldName,
				idx,
				key,
				fehlerTyp,
				ReadOnlyEmptyHashSet.instance<number[]>()
			);
		}
	}

	public addValidatorMessageWithReplacement4RepetitionNotUnique(
		fehlerFeldName: string,
		idx: IterationState,
		key: string,
		fehlerTyp: ErrorType,
		erroneousRepetitions: Set<number[]>
	): void {
		const errorTexts = this.metaData.getErrorTexts(this._regelName);
		const parameterMapping = this.metaData.getParameterMapping(this._regelName);
		const args: LocalizableArgs = this._substParameters(idx, parameterMapping);
		const localizableKey = LocalizableUtil.createRuleErrorKey(this._regelName, this.metaData.getModelId());
		const localizable: Localizable = {
			key: localizableKey,
			args,
			defaults: errorTexts
		};
		this._addValidatorMessage(fehlerFeldName, idx, key, fehlerTyp, localizable, true, erroneousRepetitions);
	}

	private _addValidatorMessage(
		fehlerFeldName: string,
		idx: IterationState,
		key: string,
		fehlerTyp: ErrorType,
		localizable: Localizable,
		acquireReferencedFields: boolean,
		erroneousRepetitions: Set<number[]>
	): void {
		if (!key) {
			// we take key otherwise we cannot do fehlerTyp === ErrorType.OMISSION_ERROR
			return;
		}
		const id = RtInternalIdentifier.createFromIterationState(fehlerFeldName, idx);
		const idExtern = this.validationCache.convert2Extern(id);

		const referencedFields = acquireReferencedFields
			? this._getReferencedFields(idx, this._regelName, erroneousRepetitions)
			: ReadOnlyEmptyHashSet.instance<RtExternalIdentifier>();
		const refOmissionFields = fehlerTyp === ErrorType.OMISSION_ERROR && acquireReferencedFields
			? this._getRefOmissionFields(idx, this._regelName, erroneousRepetitions)
			: ReadOnlyEmptyHashSet.instance<RtExternalIdentifier>();
		const unknowns = this._extractUnknowns(referencedFields);

		const referencedEntities = acquireReferencedFields
			? this._getReferencedEntities(this._regelName, erroneousRepetitions)
			: [];
		const referencedOmissionEntities = acquireReferencedFields
			? this._getRefOmissionEntities(this._regelName, erroneousRepetitions)
			: [];
		const cause = this.createCause(idExtern, idx, referencedEntities, referencedOmissionEntities, fehlerTyp);
		this._addValidatorMessageToResult(key, fehlerTyp, localizable, idExtern, referencedFields, refOmissionFields, unknowns, cause);
	}

	private _addValidatorMessageToResult(
		key: string,
		fehlerTyp: ErrorType,
		localizable: Localizable,
		idExtern: RtExternalIdentifier,
		referencedFields: Set<IIdentifier>,
		refOmissionFields: Set<IIdentifier>,
		unknowns: Set<IIdentifier>,
		cause: ICause
	) {
		let severity: SeverityType;
		if (this._pruefErgebnisTyp === PruefErgebnisTyp.Fehler) {
			this.pruefErgebnis.addError(
				this._regelName,
				key,
				idExtern,
				[localizable],
				fehlerTyp,
				referencedFields,
				refOmissionFields,
				unknowns
			);
			severity = SeverityType.ERROR;
		} else if (this._pruefErgebnisTyp === PruefErgebnisTyp.Hinweis) {
			this.pruefErgebnis.addHint(
				this._regelName,
				key,
				idExtern,
				[localizable],
				fehlerTyp,
				referencedFields,
				refOmissionFields,
				unknowns
			);
			severity = SeverityType.HINT;
		} else {
			this.pruefErgebnis.addInfo(
				this._regelName,
				key,
				idExtern,
				[localizable],
				fehlerTyp,
				referencedFields,
				refOmissionFields,
				unknowns
			);
			severity = SeverityType.INFO;
		}
		this.pruefErgebnis.addMessageV2(this._regelName, key, [localizable], fehlerTyp, severity, cause);
		this.logger.logRegelFires(this._regelName, idExtern);
	}

	private _extractUnknowns(referencedFields: HashSet<RtExternalIdentifier>): HashSet<RtExternalIdentifier> {
		const result = new HashSet<RtExternalIdentifier>();
		for (const refField of referencedFields) {
			if (refField.getIndices().includes(IIdentifier.UNKNOWN)) {
				result.add(refField);
			}
		}
		return result;
	}

	private _getRefOmissionFields(
		idx: IterationState,
		ruleName: string,
		erroneousRepetitions: Set<number[]>
	): HashSet<RtExternalIdentifier> {
		const fields: HashSet<RtInternalIdentifier> =
			this.metaData.getReferencedOmissionFieldsWithoutReplacement(ruleName);
		const result: HashSet<RtExternalIdentifier> = new HashSet<RtExternalIdentifier>();
		for (const field of fields) {
			const refId = this._resolveExternField(field, idx);
			if (erroneousRepetitions.size > 0 && ArrayUtils.contains(field.getIndices(), IIdentifier.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED)) {
				erroneousRepetitions.forEach(errRep => result.add(this._replaceRepNonUnqConstants(refId, errRep)));
			} else {
				result.add(refId);
			}
		}
		return result;
	}

	private _resolveExternField(
		idInternal: RtInternalIdentifier,
		idx: IterationState
	): RtExternalIdentifier {
		const result: RtInternalIdentifier = new EntityIterator(idInternal, idx).get();
		return this.validationCache.convert2Extern(result);
	}

	private _getReferencedFields(
		idx: IterationState,
		ruleName: string,
		erroneousRepetitions: Set<number[]>
	): HashSet<RtExternalIdentifier> {
		const fields = this.metaData.getReferencedFieldsWithoutReplacement(ruleName);
		const result: HashSet<RtExternalIdentifier> = new HashSet<RtExternalIdentifier>();
		for (const field of fields) {
			const refId = this._resolveExternField(field, idx);
			if (field.getIndices().includes(IIdentifier.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED)) {
				// special case: erroneousRepetitions is empty. This means, in the RepetitinoNotUnique part of the rule condition
				// there was no error. So it is ok not to add the field to the referenced fields
				erroneousRepetitions.forEach(errRep => result.add(this._replaceRepNonUnqConstants(refId, errRep)));
			} else {
				result.add(refId);
			}
		}
		return result;
	}

	private _replaceRepNonUnqConstants(id: RtExternalIdentifier, erroneousRepetition: number[]): RtExternalIdentifier {
		const newIndices = id.getIndices().slice();
		for (let i = 0; i < newIndices.length; i++) {
			if (newIndices[i] == IIdentifier.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED) {
				newIndices[i] = erroneousRepetition[i];
			}
		}
		return new RtExternalIdentifier(id.getName(), newIndices);
	}

	// ===========================================================
	// V2 message API: possibly-causing entities (customRefErrorEntities)
	// ===========================================================

	/**
	 * Builds the {@link ICause} for a message, grouping the possibly-causing entities by given-ness.
	 *
	 * For {@link ErrorType.OMISSION_ERROR} two groups are produced: group 0 = not-given omission
	 * entities, group 1 = given referenced entities (with entities that are both given and not-given
	 * deduped into group 0 only). For {@link ErrorType.VALUE_ERROR} a single unioned group is produced.
	 */
	public createCause(
		idExtern: RtExternalIdentifier,
		iterState: IterationState,
		referencedEntities: RtIdentifierTemplate[],
		refOmissionEntities: RtIdentifierTemplate[],
		fehlerTyp: ErrorType
	): ICause {
		const emptyEntities = this._filter4GivenAndMapToIIdentifier(refOmissionEntities, false, iterState);
		const givenEntities = this._filter4GivenAndMapToIIdentifier(referencedEntities, true, iterState);
		if (fehlerTyp === ErrorType.OMISSION_ERROR) {
			// an entity that is both given and not-given (e.g. aggregation [0]) is deduped into group 0 only
			const givenOnly: HashSet<IIdentifier> = new HashSet<IIdentifier>();
			for (const e of givenEntities) {
				if (!emptyEntities.has(e)) {
					givenOnly.add(e);
				}
			}
			return new ErrorCause(idExtern, [emptyEntities, givenOnly]);
		} else {
			const allEntities: HashSet<IIdentifier> = new HashSet<IIdentifier>();
			for (const e of givenEntities) {
				allEntities.add(e);
			}
			for (const e of emptyEntities) {
				allEntities.add(e);
			}
			return new ErrorCause(idExtern, [allEntities]);
		}
	}

	/**
	 * Filters the given entity templates to keep only the ones that are given (`given==true`) or
	 * not-given (`given==false`) for the specified `iterState`, and maps them to external identifiers.
	 * Aggregation layers are represented by a `0` (wildcard) in the resulting identifier.
	 */
	private _filter4GivenAndMapToIIdentifier(
		refEntities: RtIdentifierTemplate[],
		given: boolean,
		iterState: IterationState
	): HashSet<IIdentifier> {
		const result = new HashSet<IIdentifier>();
		for (const tpl of refEntities) {
			if (this._isGiven(tpl, given, iterState)) {
				// isGiven has already been called *before* this conversion, so it is ok to use
				// createFromTemplateReplaceAggregationsWith0DoNotProcessFurther()
				const intId = RtInternalIdentifier.createFromTemplateReplaceAggregationsWith0DoNotProcessFurther(tpl, iterState);
				result.add(this.validationCache.convert2Extern(intId, true));
			}
		}
		return result;
	}

	private _isGiven(id: RtIdentifierTemplate, given: boolean, iterState: IterationState): boolean {
		if (!EntityUtil.uniqueNameIsRootGroup(id.getUniqueName())) {
			// TODO [A12K-2281] maybe we need still more tests, particularly for partialValidation
			if (given) {
				return this.mindestensEineEntityAngegeben(iterState, [id]).isKnownAndTrue();
			} else {
				return this.nichtAlleOderKeineEntityAngegeben(iterState, [id], true).isKnownAndTrue();
			}
		} else {
			const lfdNr: number = this._deriveLfdNr4Vordruck(id, iterState);
			const rootGroupName: string = EntityUtil.getRootGroupName(this.metaData.getPfadName(id.getUniqueName()));
			if (given) {
				return this.mindestensEinVordruckAngegeben(rootGroupName, lfdNr, iterState, id) !==
					ValidierungsErgebnis.FALSE_OR_UNKNOWN;
			} else {
				if (id.isAggregation()) {
					// for root-groups, we lack an implementation of nichtAlleOderKeine...Angegeben, so we
					// keep this "err-on-safe-side" shortcut: assume there is always at least one not-given
					// repetition for repeatable root groups.
					return true;
				}
				// use mindestensEinVordruckAngegeben (instead of keinVordruckAngegeben) so that we err on
				// the side of returning true, avoiding incorrectly filtering out (omission) entities.
				return this.mindestensEinVordruckAngegeben(rootGroupName, lfdNr, iterState, id) ===
					ValidierungsErgebnis.FALSE_OR_UNKNOWN;
			}
		}
	}

	private _deriveLfdNr4Vordruck(id: RtIdentifierTemplate, iterState: IterationState): number {
		const layersResolved: RtIdentifierTemplateLayerSpec[] =
			EntityUtil.resolveIterations(id.getLayerSpec(), iterState.getIndexes());
		let lfdNr: number = layersResolved[0].getIdxStart();
		const lfdNrEnd: number = layersResolved[0].getIdxEnd();
		if (lfdNr !== lfdNrEnd) {
			// replace explicit ranges by IIdentifier.ALL_INDICES
			lfdNr = IIdentifier.ALL_INDICES;
		}
		return lfdNr;
	}

	private _getReferencedEntities(
		ruleName: string,
		erroneousRepetitions: Set<number[]>
	): RtIdentifierTemplate[] {
		const entities: HashSet<RtIdentifierTemplate> =
			this.metaData.getReferencedEntitiesWithoutReplacement(ruleName);
		return this._expandRepetitionNotUniqueIndices(erroneousRepetitions, entities);
	}

	private _getRefOmissionEntities(
		ruleName: string,
		erroneousRepetitions: Set<number[]>
	): RtIdentifierTemplate[] {
		const entities: HashSet<RtIdentifierTemplate> =
			this.metaData.getReferencedOmissionEntitiesWithoutReplacement(ruleName);
		return this._expandRepetitionNotUniqueIndices(erroneousRepetitions, entities);
	}

	private _expandRepetitionNotUniqueIndices(
		erroneousRepetitions: Set<number[]>,
		entities: HashSet<RtIdentifierTemplate>
	): RtIdentifierTemplate[] {
		const result: RtIdentifierTemplate[] = [];
		for (const entity of entities) {
			if (
				entity.getLayerSpec().some(
					lSpec => lSpec.getIdxStart() === IIdentifier.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED
				)
			) {
				erroneousRepetitions.forEach(errRep =>
					result.push(this._replaceRepNonUnqConstantsTemplate(entity, errRep))
				);
			} else {
				result.push(entity);
			}
		}
		return result;
	}

	private _replaceRepNonUnqConstantsTemplate(
		entity: RtIdentifierTemplate,
		erroneousRepetition: number[]
	): RtIdentifierTemplate {
		const oldLayers: RtIdentifierTemplateLayerSpec[] = entity.getLayerSpec();
		const newLayers: RtIdentifierTemplateLayerSpec[] = [];
		for (let i = 0; i < oldLayers.length; i++) {
			if (oldLayers[i].getIdxStart() === IIdentifier.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED) {
				const idxStartAndEnd: number = erroneousRepetition[i];
				newLayers.push(new RtIdentifierTemplateLayerSpec(idxStartAndEnd, idxStartAndEnd));
			} else {
				newLayers.push(oldLayers[i]);
			}
		}
		return new RtIdentifierTemplate(
			entity.getUniqueName(),
			newLayers,
			entity.getSemanticIndex(),
			entity.isField(),
			entity.getCategory(),
			entity.getFilter(),
			entity.isOuter()
		);
	}

	public preliminaryError(
		errorFieldUniqueName: string,
		idx: IterationState,
		errorId: string,
		errorMsgId: RuntimeFormalErrorEnum,
		errorType: ErrorType
	): void {
		const id = RtInternalIdentifier.createFromIterationState(errorFieldUniqueName, idx);
		const idExtern = this.validationCache.convert2Extern(id);
		this.validationCache.markFieldAsInvalidForValidation(id, idExtern);

		const args: LocalizableArgs = this._getLocalizableArgs(errorFieldUniqueName, id, idExtern);
		if (errorMsgId == RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER && this.metaData.isExplicitlyMandatory(errorFieldUniqueName)) {
			// For mandatory field errors with possible overridden error messages, we create an error with fallback
			this._createMandatoryErrorWithFallback(id, idExtern, args, errorType);
			return;
		}
		const defaults = FehlerHandler.getAllErrorTexts(errorMsgId);
		const errorLocalizable: Localizable = LocalizableUtil.createLocalizableFormalError(
			errorMsgId,
			args,
			defaults
		);

		this._formalChecker.addFormalError(this.pruefErgebnis, idExtern, errorId, [errorLocalizable], errorType);
	}

	private _createMandatoryErrorWithFallback(id: RtInternalIdentifier, idExtern: RtExternalIdentifier, args: LocalizableArgs, errorType: ErrorType) {
		const key1 = LocalizableUtil.createFieldRequiredErrorKey(idExtern.getName(), this.metaData.getModelId());
		// These might be empty
		const defaults1 = this.metaData.getErrorMessage4Req(id.getName());
		const error: Localizable = { key: key1, args, defaults: defaults1 };
		const key2 = LocalizableUtil.createFormalErrorKey(RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER)
		const defaults2 = FehlerHandler.getAllErrorTexts(RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER)
		const fallbackError: Localizable = { key: key2, args, defaults: defaults2 }
		this._formalChecker.addFormalError(this.pruefErgebnis, idExtern, IFormaleFehlerConstants.FEHLER_PFLICHTFELD, [error, fallbackError], errorType)
	}

	private _getLocalizableArgs(
		errorFieldUniqueName: string,
		internalId: RtInternalIdentifier,
		externalId: IIdentifier
	): LocalizableArgs {
		const value = this.validationCache.getXMLValue(internalId);
		const placeholder = LocalizableUtil.createFieldValuePlaceholder(
			errorFieldUniqueName,
			this.metaData,
			value
		);
		return LocalizableUtil.createLocalizableArgs4FieldErrorMsg(
			this.metaData,
			externalId,
			value ?? "",
			{ "fieldName.Value": placeholder }
		);
	}

}
