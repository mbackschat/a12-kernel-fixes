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
import type { Localizable } from "@com.mgmtp.a12.utils/utils-localization";

import type { IIdentifier } from "./IIdentifier.js";

/**
 * Errors have different types:
 * - **Omission errors** are *errors*, *hints*, or *infos* that exist because not all data was entered yet.
 * - **Value errors** are *errors*, *hints*, or *infos* that exist because invalid values have been entered.
 */
export enum ErrorType {
	OMISSION_ERROR,
	VALUE_ERROR
}

/**
 * API for storing messages that specify the result of a validation.
 * 
 * Messages can be
 * - an `error`, a `hint`, or an `info`.
 * - a {@link ErrorType.VALUE_ERROR} or an {@link ErrorType.OMISSION_ERROR}.
 * 
 * The implementation of this API has to be provided by the application.
 */
export interface IResult {
	/**
	 * Adds an info to the result set.
	 *
	 * @param ruleName 	rule name - unique per rule set. Error messages concerning the syntactical
	 *          validation will always specify {@link FORMAL_VALIDATION_RULE_NAME}. Thus,
	 *          ruleName is only unique for rules.
	 * @param infoCode 	The info code is the error code of the rule given in the editor or the error code
	 *          of the syntactical validation. It is not necessarily unique per rule.
	 * @param infoField	{@link IIdentifier} of the error field.
	 * @param infoMessage A detailed localizable message that should be displayed to the end user.
	 *      The default values for the parameter replacements and the formatted values are returned
	 *      unchanged, so in particular no substitutions of html-sensitive characters take place. Localizer can be
	 *      implemented accordingly if needed.
	 * @param type 	Specification of the error type ( {@link ErrorType.OMISSION_ERROR} or
	 *          {@link ErrorType.VALUE_ERROR}).
	 * @param referencedFields 	set of all fields that are referenced by the rule generating the info. These are
	 *            the fields possibly being responsible for the rule to fire.
	 * @param refOmissionErrorResponsible 	set of all fields that may be responsible for generating omission infos. These
	 *            are fields, a user did not specify, that given additionally may solve the rule.
	 * @param unknowns 	set of all fields, the indices of which cannot be specified, because the
	 *            corresponding index field is not given. These contain an index value of {@link IIdentifier.UNKNOWN}.
	 */
	addInfo(
		ruleName: string,
		infoCode: string,
		infoField: IIdentifier,
		infoMessage: Localizable[],
		type: ErrorType,
		referencedFields: Set<IIdentifier>,
		refOmissionErrorResponsible: Set<IIdentifier>,
		unknowns: Set<IIdentifier>
	): void;

	/**
	 * Adds a hint to the result set.
	 *
	 * @param ruleName 	rule name - unique per rule set. Error messages concerning the syntactical
	 *          validation will always specify {@link FORMAL_VALIDATION_RULE_NAME}. Thus,
	 *          ruleName is only unique for rules.
	 * @param hintCode 	The hint code is the error code of the rule given in the editor or the error code
	 *          of the syntactical validation. It is not necessarily unique per rule.
	 * @param hintField	{@link IIdentifier} of the error field.
	 * @param hintMessage A detailed localizable message that should be displayed to the end user.
	 *      The default values for the parameter replacements and the formatted values are returned
	 *      unchanged, so in particular no substitutions of html-sensitive characters take place. Localizer can be
	 *      implemented accordingly if needed.
	 * @param type 	Specification of the error type ( {@link ErrorType.OMISSION_ERROR} or
	 *          {@link ErrorType.VALUE_ERROR}).
	 * @param referencedFields 	set of all fields that are referenced by the rule generating the hint. These are
	 *            the fields possibly being responsible for the rule to fire.
	 * @param refOmissionErrorResponsible 	set of all fields that may be responsible for generating omission hints. These
	 *            are fields, a user did not specify, that given additionally may solve the rule.
	 * @param unknowns 	set of all fields, the indices of which cannot be specified, because the
	 *            corresponding index field is not given. These contain an index value of {@link IIdentifier.UNKNOWN}.
	 */
	addHint(
		ruleName: string,
		hintCode: string,
		hintField: IIdentifier,
		hintMessage: Localizable[],
		type: ErrorType,
		referencedFields: Set<IIdentifier>,
		refOmissionErrorResponsible: Set<IIdentifier>,
		unknowns: Set<IIdentifier>
	): void;

	/**
	 * Adds an error to the result set
	 *
	 * @param ruleName 	rule name - unique per rule set. Error messages concerning the syntactical
	 *          validation will always specify {@link FORMAL_VALIDATION_RULE_NAME}. Thus,
	 *          ruleName is only unique for rules.
	 * @param errorCode 	The error code of the rule as given in the editor or the error code of the
	 *          syntactical validation. It is not necessarily unique per rule.
	 * @param errorField 	{@link IIdentifier} of the error field. The actual implementation of the
	 *          {@link IIdentifier} can be a different one than the one used in the {@link IData}
	 *          implementation, which was passed to the validation interface.
	 * @param errorMessage A detailed localizable message that should be displayed to the end user.
	 *      The default values for the parameter replacements and the formatted values are returned
	 *      unchanged, so in particular no substitutions of html-sensitive characters take place. Localizer can be
	 *      implemented accordingly if needed.
	 * @param type 	Specification of the error type ( {@link ErrorType.OMISSION_ERROR} or
	 *          {@link ErrorType.VALUE_ERROR}).
	 * @param referencedFields 	set of all fields that are referenced by the rule generating the error. These are
	 *          the fields possibly being responsible for the rule to fire.
	 * @param refOmissionErrorResponsible 	set of all fields that may be responsible for generating omission errors. These
	 *          are fields, a user did not specify, that given additionally may solve the rule.
	 * @param unknowns 	set of all fields, the indices of which cannot be specified, because the
	 *          corresponding index field is not given. These contain an index value of {@link IIdentifier.UNKNOWN}.
	 */
	addError(
		ruleName: string,
		errorCode: string,
		errorField: IIdentifier,
		errorMessage: Localizable[],
		type: ErrorType,
		referencedFields: Set<IIdentifier>,
		refOmissionErrorResponsible: Set<IIdentifier>,
		unknowns: Set<IIdentifier>
	): void;

	/**
	 * Returns the number of all errors, hints, or infos. This method is used by the runtime system,
	 * if a limit for returned errors was specified. (See {@link ValidationMode}.)
	 *
	 * @param type	specification of the error type ({@link ErrorType.OMISSION_ERROR} or {@link ErrorType.VALUE_ERROR})
	 * @return the number of errors, hints, or infos
	 */
	getCountOf(type: ErrorType): number;
}

// TODO: Fix this the next time the file is edited.
// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace IResult {
	/**
	 * {@link FORMAL_VALIDATION_RULE_NAME} is the value of the parameter "ruleName" for all formal errors.
	 */
	export const FORMAL_VALIDATION_RULE_NAME = "formalePruefung";
}
