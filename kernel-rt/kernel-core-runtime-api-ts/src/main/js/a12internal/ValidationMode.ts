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

import type { DataFormats } from "@com.mgmtp.a12.utils/utils-localization";

import type { ICustomFieldTypeFactory } from "../custom/ICustomFieldTypeFactory.js";

import type { ICustomConditionFactory } from "./custom/ICustomConditionFactory.js";

/**
 * Specifies, which phases of validation are supposed to be run.
 */
export enum ValidationType {
	onlyFormalNoRequiredFieldCheck,
	fullExaminationWithAbortOnFormalErrors,
	fullExamination
}

/**
 * Specifies what types of rules are supposed to be validated.
 */
export enum RuleType {
	ERROR,
	HINT,
	INFO,
	ALL
}

/**
 * Parameter class for the configuration of a validation call.
 */
export class ValidationMode {
	public static readonly DE = "de";
	public static readonly EN = "en";
	public static readonly FR = "fr";
	public static readonly NL = "nl";

	/**
	 * Specifies, which phases of validation are supposed to be run.
	 */
	public _validationType: ValidationType = ValidationType.fullExamination;

	/**
	 * Specifies what types of rules are supposed to be validated.
	 */
	public _ruleType: RuleType = RuleType.ALL;

	/**
	 * If set, the rule language construct `Today` will evaluate to this field instead of
	 * the actual current date.
	 * 
	 * **Note:** Should only be used for test purposes.
	 */
	public _currentDateForTest: Date | undefined;

	/**
	 * Describes the parameter of the data formats.
	 */
	public _dataFormats: DataFormats = {} as DataFormats;

	/**
	 * Additional information that will be passed to the project specific implementation of the
	 * custom field types.
	 * 
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589.
	 */
	private _additionalInformation: Map<string, any> = new Map<string, any>();

	/**
	 * The factory for the custom conditions that should be used.
	 *
	 * Note: If provided, this will take precedence over the implementations that were statically registered via {@link CustomConditionService}.
	 */
	public customConditionFactory: ICustomConditionFactory | undefined;

	/**
	 * The factory for the custom field types that should be used.
	 *
	 * Note: If provided, this will take precedence over the implementations that were statically registered via {@link CustomFieldTypeService}.
	 */
	public customFieldTypeFactory: ICustomFieldTypeFactory | undefined;

	/**
	 * Specifies what types of rules are supposed to be validated.
	 */
	public getRuleType(): RuleType {
		return this._ruleType;
	}

	public setRuleType(ruleType: RuleType) {
		this._ruleType = ruleType;
	}

	/**
	 * Specifies, which phases of validation are supposed to be run.
	 */
	public getValidationType(): ValidationType {
		return this._validationType;
	}

	public setValidationType(pruefungsArt: ValidationType) {
		this._validationType = pruefungsArt;
	}

	/**
	 * Returns a short description of the validation mode.
	 * The specific details of the representation are not ensured for future versions.
	 *
	 * @return String representation of the validation mode
	 */
	public toString(): string {
		return `Validation Type: ${this._validationType}\nRule type: ${this._ruleType}`;
	}


	/**
	 * Get the data formats.
	 */
	public getDataFormats(): DataFormats {
		return this._dataFormats;
	}

	/**
	 * Set the data formats.
	 *
	 * @param dataFormats 	data formats for the presentation information
	 */
	public setDataFormats(dataFormats: DataFormats) {
		this._dataFormats = dataFormats;
	}

	/**
	 * Set the additional information for a specified key.
	 * (See {@link getAllAdditionalInformation})
	 *
	 * @param key 	the key, for which the value will be specified.
	 * @param value 	the value for the specified key.
	 * 
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use customConditions() or customFieldTypes() instead. In your
	 * 		implementation of {@link ICustomFieldTypeFactory} or {@link ICustomConditionFactory} you can already create the
	 * 		objects in the suitable configuration. Therefore, it is not necessary to pass this data all the way through any
	 * 		longer.
	 */
	public setAdditionalInformation(key: string, value: any) {
		this._additionalInformation.set(key, value);
	}

	/**
	 * Get the additional information for a specified key.
	 * (See {@link getAllAdditionalInformation})
	 *
	 * @param key 	the key, for which the value will be retrieved.
	 * @return the value for the specified key.
	 * 
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589.
	 */
	public getAdditionalInformation(key: string): any {
		return this._additionalInformation.get(key);
	}

	/**
	 * @return the additional information that will be passed to the project specific implementation of
	 *         the custom field types.
	 * 
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use {@link #customFieldTypeFactory()}  instead. In your
	 * 		implementation of {@link ICustomFieldTypeFactory} you can already create the objects in the suitable configuration.
	 * 		Therefore, it is not necessary to pass this data all the way through any longer.
	 */
	public getAllAdditionalInformation(): Map<string, any> {
		return this._additionalInformation;
	}

	public getCurrentDateForTest(): Date | undefined {
		return this._currentDateForTest;
	}

	/**
	 * The rule language construct `Today` will evaluate to the passed date instead of
	 * the actual current date.
	 * **Note:** Should only be used for test purposes.
	 */
	public setCurrentDateForTest(currentDateForTest: Date) {
		this._currentDateForTest = currentDateForTest;
	}
}
