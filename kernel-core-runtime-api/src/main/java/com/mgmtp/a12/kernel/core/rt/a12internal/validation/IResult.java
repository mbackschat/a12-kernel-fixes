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
package com.mgmtp.a12.kernel.core.rt.a12internal.validation;

import java.util.Arrays;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;

/**
 * API for storing messages that specify the result of a validation.
 * <p>
 * Messages can be
 * <ul>
 * <li>an <code>error</code>, a <code>hint</code>, or an <code>info</code>.</li>
 * <li>a {@link IResult.ErrorType#VALUE_ERROR} or an {@link IResult.ErrorType#OMISSION_ERROR}.</li>
 * </ul>
 * The implementation of this API has to be provided by the application.
 * </p>
 */
public interface IResult {

	/**
	 * Errors have different types:
	 * <ul>
	 * <li><b>Omission errors</b> are <i>errors</i>, <i>hints</i>, or <i>infos</i> that exist
	 * because not all data
	 * was entered yet.</li>
	 * <li><b>Value errors</b> are <i>errors</i>, <i>hints</i>, or <i>infos</i> that exist because
	 * invalid values
	 * have been entered.</li>
	 * </ul>
	 */
	public enum ErrorType {
		OMISSION_ERROR,
		VALUE_ERROR
	}

	/**
	 * {@link #FORMAL_VALIDATION_RULE_NAME} is the value of the parameter "ruleName" for all
	 * formal errors.
	 */
	public static final String FORMAL_VALIDATION_RULE_NAME = "formalePruefung";

	/**
	 * Adds a message to the result set.
	 *
	 * @param ruleName
	 * 		rule name. For real rules, this should be unique. Error messages concerning the syntactical validation will always
	 * 		specify {@link #FORMAL_VALIDATION_RULE_NAME}. Thus, ruleName is only unique for rules.
	 * @param errorCode
	 * 		the error code of the rule given in the editor or the error code of the syntactical validation. It is not necessarily
	 * 		unique per rule.
	 * @param messageStr
	 * 		A detailed string representation of the message that should be displayed to the end user. All parameters used in the
	 * 		message are already replaced by actual values. Parameters for the field name (<i>$&lt;fieldname&gt;$</i>) are
	 * 		replaced by the String representation of the {@link IIdentifier} that is returned by the method
	 *        {@link IData#identifierToString(IIdentifier)}, parameters for the field value (<i>$&lt;field.value&gt;$</i> are
	 * 		replaced by the String returned by the method {@link IData#getValueForDisplay(IIdentifier)}, in both cases including
	 * 		the $-sign. Note: The text is otherwise returned unchanged, so in particular no substitutions of html-sensitive
	 * 		characters take place. If e.g. a substitution of html-sensitive characters is only relevant for field values,
	 *        {@link IData#identifierToString(IIdentifier)} can be implemented appropriately. If substitution of sensitive characters
	 * 		is not necessary, the caller won't need to change the error text before presentation to the end user.
	 * @param type
	 * 		Specification of the error type ( {@link ErrorType#OMISSION_ERROR} or {@link ErrorType#VALUE_ERROR}).
	 * @param severity
	 * 		The severity of the message ({@link SeverityType#ERROR}, {@link SeverityType#HINT} or {@link SeverityType#INFO}.
	 * @param possiblyCausingEntities
	 * 		information regarding the entities causing the message.
	 */
	default public void addMessageV2(
			String ruleName,
			String errorCode,
			String messageStr,
			ErrorType type,
			SeverityType severity,
			ICause possiblyCausingEntities
	) {
	}

	/**
	 * Adds an info to the result set.
	 *
	 * @param ruleName
	 * 		rule name. For real rules, this should be unique. Error messages concerning the syntactical validation will always
	 * 		specify {@link #FORMAL_VALIDATION_RULE_NAME}. Thus, ruleName is only unique for rules.
	 * @param infoCode
	 * 		The info code is the error code of the rule given in the editor or the error code
	 * 		of the syntactical validation. It is not necessarily unique per rule.
	 * @param infoField
	 *        {@link IIdentifier} of the error field.
	 * @param infoMessage
	 * 		A detailed string representation of the message that should be displayed to the end user. All parameters used in the
	 * 		message are already replaced by actual values. Parameters for the field name (<i>$&lt;fieldname&gt;$</i>) are
	 * 		replaced by the String representation of the {@link IIdentifier} that is returned by the method
	 *        {@link IData#identifierToString(IIdentifier)}, parameters for the field value (<i>$&lt;field.value&gt;$</i> are
	 * 		replaced by the String returned by the method {@link IData#getValueForDisplay(IIdentifier)}, in both cases including
	 * 		the $-sign. Note: The text is otherwise returned unchanged, so in particular no substitutions of html-sensitive
	 * 		characters take place. If e.g. a substitution of html-sensitive characters is only relevant for field values,
	 *        {@link IData#identifierToString(IIdentifier)} can be implemented appropriately. If substitution of sensitive characters
	 * 		is not necessary, the caller won't need to change the error text before presentation to the end user.
	 * @param type
	 * 		Specification of the error type ( {@link IResult.ErrorType#OMISSION_ERROR} or
	 *        {@link IResult.ErrorType#VALUE_ERROR}).
	 * @param referencedFields
	 * 		set of all fields that are referenced by the rule generating the info. These are
	 * 		the fields possibly being responsible for the rule to fire.
	 * @param refOmissionErrorResponsible
	 * 		set of all fields that may be responsible for generating omission infos. These
	 * 		are fields, a user did not specify, that given additionally may solve the rule.
	 * @param unknowns
	 * 		set of all fields, the indices of which cannot be specified, because the
	 * 		corresponding index field is not given. These contain an index value of
	 *        {@value IIdentifier#UNKNOWN}.
	 */
	public void addInfo(
			String ruleName,
			String infoCode,
			IIdentifier infoField,
			String infoMessage,
			ErrorType type,
			Set<IIdentifier> referencedFields,
			Set<IIdentifier> refOmissionErrorResponsible,
			Set<IIdentifier> unknowns);

	/**
	 * Adds a hint to the result set.
	 *
	 * @param ruleName
	 * 		rule name. For real rules, this should be unique. Error messages concerning the syntactical validation will always
	 * 		specify {@link #FORMAL_VALIDATION_RULE_NAME}. Thus, ruleName is only unique for rules.
	 * @param hintCode
	 * 		The hint code is the error code of the rule given in the editor or the error code
	 * 		of the syntactical validation. It is not necessarily unique per rule.
	 * @param hintField
	 *        {@link IIdentifier} of the error field.
	 * @param hintMessage
	 * 		A detailed string representation of the message that should be displayed to the end user. All parameters used in the
	 * 		message are already replaced by actual values. Parameters for the field name (<i>$&lt;fieldname&gt;$</i>) are
	 * 		replaced by the String representation of the {@link IIdentifier} that is returned by the method
	 *        {@link IData#identifierToString(IIdentifier)}, parameters for the field value (<i>$&lt;field.value&gt;$</i> are
	 * 		replaced by the String returned by the method {@link IData#getValueForDisplay(IIdentifier)}, in both cases including
	 * 		the $-sign. Note: The text is otherwise returned unchanged, so in particular no substitutions of html-sensitive
	 * 		characters take place. If e.g. a substitution of html-sensitive characters is only relevant for field values,
	 *        {@link IData#identifierToString(IIdentifier)} can be implemented appropriately. If substitution of sensitive characters
	 * 		is not necessary, the caller won't need to change the error text before presentation to the end user.
	 * @param type
	 * 		Specification of the error type ( {@link IResult.ErrorType#OMISSION_ERROR} or
	 *        {@link IResult.ErrorType#VALUE_ERROR}).
	 * @param referencedFields
	 * 		set of all fields that are referenced by the rule generating the hint. These are
	 * 		the fields possibly being responsible for the rule to fire.
	 * @param refOmissionErrorResponsible
	 * 		set of all fields that may be responsible for generating omission hints. These
	 * 		are fields, a user did not specify, that given additionally may solve the rule.
	 * @param unknowns
	 * 		set of all fields, the indices of which cannot be specified, because the
	 * 		corresponding index field is not given. These contain an index value of
	 *        {@value IIdentifier#UNKNOWN}.
	 */
	public void addHint(
			String ruleName,
			String hintCode,
			IIdentifier hintField,
			String hintMessage,
			ErrorType type,
			Set<IIdentifier> referencedFields,
			Set<IIdentifier> refOmissionErrorResponsible,
			Set<IIdentifier> unknowns);

	/**
	 * Adds an error to the result set
	 *
	 * @param ruleName
	 * 		rule name. For real rules, this should be unique. Error messages concerning the syntactical validation will always
	 * 		specify {@link #FORMAL_VALIDATION_RULE_NAME}. Thus, ruleName is only unique for rules.
	 * @param errorCode
	 * 		The error code of the rule as given in the editor or the error code of the
	 * 		syntactical validation. It is not necessarily unique per rule.
	 * @param errorField
	 *        {@link IIdentifier} of the error field. The actual implementation of the
	 *        {@link IIdentifier} can be a different one than the one used in the {@link IData}
	 * 		implementation, which was passed to the validation interface.
	 * @param errorMessage
	 * 		A detailed string representation of the message that should be displayed to the end user. All parameters used in the
	 * 		message are already replaced by actual values. Parameters for the field name (<i>$&lt;fieldname&gt;$</i>) are
	 * 		replaced by the String representation of the {@link IIdentifier} that is returned by the method
	 *        {@link IData#identifierToString(IIdentifier)}, parameters for the field value (<i>$&lt;field.value&gt;$</i> are
	 * 		replaced by the String returned by the method {@link IData#getValueForDisplay(IIdentifier)}, in both cases including
	 * 		the $-sign. Note: The text is otherwise returned unchanged, so in particular no substitutions of html-sensitive
	 * 		characters take place. If e.g. a substitution of html-sensitive characters is only relevant for field values,
	 *        {@link IData#identifierToString(IIdentifier)} can be implemented appropriately. If substitution of sensitive characters
	 * 		is not necessary, the caller won't need to change the error text before presentation to the end user.
	 * @param type
	 * 		Specification of the error type ( {@link IResult.ErrorType#OMISSION_ERROR} or
	 *        {@link IResult.ErrorType#VALUE_ERROR}).
	 * @param referencedFields
	 * 		set of all fields that are referenced by the rule generating the error. These are
	 * 		the fields possibly being responsible for the rule to fire.
	 * @param refOmissionErrorResponsible
	 * 		set of all fields that may be responsible for generating omission errors. These
	 * 		are fields, a user did not specify, that given additionally may solve the rule.
	 * @param unknowns
	 * 		set of all fields, the indices of which cannot be specified, because the
	 * 		corresponding index field is not given. These contain an index value of
	 *        {@value IIdentifier#UNKNOWN}.
	 */
	public void addError(
			String ruleName,
			String errorCode,
			IIdentifier errorField,
			String errorMessage,
			ErrorType type,
			Set<IIdentifier> referencedFields,
			Set<IIdentifier> refOmissionErrorResponsible,
			Set<IIdentifier> unknowns);

	/**
	 * Returns the number of all (non-suppressed) errors, hints, or infos.
	 * <p>
	 * This method is used by the runtime system if a limit for returned errors was specified (see {@link ValidationMode}).
	 * The method may also be used, for example, to decide if a multistep operation (e.g., datatransfer) should be aborted or
	 * not, depending on whether any non-suppressed errors have occurred. The runtime cannot count errors/messages by itself because
	 * it doesn't know which of them are suppressed at the IResult-level and which aren't.
	 * <p>
	 * Note regarding suppression of validation errors:
	 * IResults can suppress validation errors by simply not including them in the error count. Note however that error suppression
	 * is not supported for <em>formal validation</em> errors: If such errors are suppressed, validation or computation rules that
	 * depend on the formally-invalid fields will nonetheless not work correctly, even if the formal validation error is
	 * suppressed. An exception is the IDNR_NUR_FUER_TEST formal error, which has special treatment that allows validation and
	 * computation rules to proceed successfully.
	 *
	 * @param type
	 * 		specification of the error type (
	 *        {@link IResult.ErrorType#OMISSION_ERROR} or
	 *        {@link IResult.ErrorType#VALUE_ERROR})
	 * @return the number of errors, hints, or infos
	 *
	 * @implNote if {@link #getCount(ErrorType, SeverityType)} is implemented (the placeholder default impl must be overridden),
	 * 		then an implementation for this method can simply delegate to it:<pre>{@code
	 *             @Override public int getCountOf(ErrorType type) {
	 * 		     		var leastSevere = SeverityType.values()[SeverityType.values().length - 1];
	 * 		     		assert Arrays.stream(SeverityType.values()).allMatch(s -> s.isMoreSevereThanOrEqual(leastSevere));
	 * 		     		int count = getCount(type, leastSevere);
	 * 		     		assert count != -1 : "it seems like neither getCountOf(ErrorType type, SeverityType severityThreshold) nor getCountOf(ErrorType type) are implemented";
	 * 		     		return count;
	 *             }}</pre>
	 */
	public int getCountOf(ErrorType type);

	/**
	 * Similar to {@link #getCountOf(ErrorType)}, but only counts messages with severity that is equal to or more severe than
	 * {@code severityThreshold} (see {@link SeverityType#isMoreSevereThanOrEqual(SeverityType)}).<br/>
	 * For example, if severityThreshold is {@link SeverityType#HINT}, then only messages with {@link SeverityType#HINT} and
	 * {@link SeverityType#ERROR} will be included in the count, not those of {@link SeverityType#INFO}.
	 *
	 * @param type
	 * 		the ErrorType to take into account for this count. If {@code null}, no filtering by ErrorType is done
	 * @param severityThreshold
	 * 		only messages that are at least as severe as the threshold will be included. Must not be null
	 */
	public int getCount(ErrorType type, SeverityType severityThreshold);

	/**
	 * Like {@link #getCount(ErrorType, SeverityType)}, but sums over the ErrorTypes.
	 *
	 * @implNote The default implementation of this method should be sufficient as long as
	 *        {@link #getCount(ErrorType, SeverityType)} is implemented properly (only this other overload needs its default
	 * 		implementation
	 * 		overridden).
	 */
	// Note: we can't name this "getCountOf" because getCountOf(ErrorType) allows null values, which could render existing valid
	// calls ambiguous
	public default int getCount(SeverityType severityThreshold) {
		return Arrays.stream(ErrorType.values())
				.mapToInt(errorType -> getCount(errorType, severityThreshold))
				.sum();
	}
}
