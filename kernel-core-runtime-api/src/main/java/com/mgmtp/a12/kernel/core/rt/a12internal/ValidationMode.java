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
package com.mgmtp.a12.kernel.core.rt.a12internal;

import java.util.Date;
import java.util.Locale;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.custom.ICustomConditionFactory;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Parameter class for the configuration of a validation call.
 * <p>
 * The following parameters are currently available:
 * <ul>
 * <li>validation type: specifies which phases of validation are supposed to be run.</li>
 * <li>rule type: specifies what types of rules are supposed to be validated.</li>
 * <li>current date for test: can override the value of <code>Today</code> (in the rule language)
 * with a user-set date.</li>
 * <li>PresentationInformation: contains the parameters of a {@link PresentationInformation}</li>
 * <li>additional information: here project specific parameters can be transmitted to the
 * validation of custom field types.</li>
 * <li>language: the language to be used for validation (default value: "de_DE"). Error messages
 * will be presented in this language.</li>
 * </ul>
 */
@OnlyForUsage
public class ValidationMode {

	public static final String LANGUAGE_KEY = "language";

	/**
	 * Specifies, which phases of validation are supposed to be run.
	 * <p>
	 * The following validation phases are used to describe the different types of validation and are offered in different
	 * combinations:
	 * <ul>
	 * <li>formal validation:<br>
	 * <ul>
	 * <li>Check that no fields are passed that have not been defined for this model.</li>
	 * <li>Check that no multiplicity greater than the maximum defined multiplicity for this field has been passed for any field.</li>
	 * <li>Check that only characters are used that are specified in the defined character set. The character set is specified for the model.</li>
	 * <li>Check that the field corresponds to the specified format.</li>
	 * <li>Check that field values only have a maximum length of 99,999 characters. This technical restriction is in place to prevent DoS attacks.</li>
	 * </ul>
	 * If there are errors during the formal validation, the error identifiers and an error text are returned.
	 * </p>
	 * <br>
	 * </li>
	 * <li>Rule validation:
	 * <p>
	 * All rules that are relevant for the specified set of fields are executed.
	 * </p>
	 * </li>
	 * </ul>
	 * The different validation phases can now be combined into the different validation types as follows:
	 * <ul>
	 * <li>{@link ValidationType#onlyFormalNoRequiredFieldCheck}: Formal validation is carried out for all fields (validateAll
	 * method) or for all relevant fields (validatePartlyWith3ValueLogic method)</li>
	 * <li>{@link ValidationType#fullExaminationWithAbortOnFormalErrors}: Formal validation is carried out for all fields
	 * (validateAll method) or for all relevant fields (validatePartlyWith3ValueLogic method)<br>
	 * If an error occurs during the formal validation, the method is terminated.
	 * Otherwise, the rule validation is carried out. The specific error that an identification number was recognized as a test
	 * identification number is reported as a formal error, but does not lead to the validation being aborted.</li>
	 * <li>{@link ValidationType#fullExamination}: Formal check is carried out for all fields (validateAll method) or for all
	 * relevant fields (validatePartlyWith3ValueLogic method)<br>
	 * This is followed by the rule validation.</li>
	 */
	public enum ValidationType {
		onlyFormalNoRequiredFieldCheck,
		fullExaminationWithAbortOnFormalErrors,
		fullExamination
	}

	/**
	 * Specifies what types of rules are supposed to be validated.
	 */
	public enum RuleType {
		ERROR,
		HINT,
		INFO,
		ALL
	}

	private ValidationType validationType;
	private RuleType ruleType = RuleType.ALL;

	/**
	 * If set, the rule language construct <code>Today</code> will evaluate to this field instead of
	 * the actual current date.<br>
	 * <b>Note:</b> Should only be used for test purposes.
	 */
	private Date currentDateForTest;

	/**
	 * Describes the parameter of the presentation information.
	 */
	private PresentationInformation presentationInformation = new PresentationInformation();

	private ICustomConditionFactory customConditionFactory;
	private ICustomFieldTypeFactory customFieldTypeFactory;

	// {@link Locale#GERMANY} is used as default for ValidationMode
	private Locale language = Locale.GERMANY;

	/**
	 * Returns the rule type.
	 *
	 * @return the type of the rule
	 */
	public RuleType getRuleType() {
		return ruleType;
	}

	public void setRuleType(RuleType ruleType) {
		this.ruleType = ruleType;
	}

	public ValidationMode() {
		validationType = ValidationType.fullExamination;
	}

	public ValidationType getValidationType() {
		return validationType;
	}

	public void setValidationType(ValidationType validationType) {
		this.validationType = validationType;
	}

	/**
	 * Returns a short description of the validation mode.
	 * The specific details of the representation are not ensured for future versions.
	 *
	 * @return String representation of the validation mode
	 */
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Validation Type: ");
		buf.append(validationType);
		buf.append("\nRule type: ");
		buf.append(ruleType);

		return buf.toString();
	}

	public PresentationInformation getPresentationInformation() {
		return presentationInformation;
	}

	public void setPresentationInformation(PresentationInformation presentationInformation) {
		this.presentationInformation = presentationInformation;
	}

	public Date currentDateForTest() {
		return currentDateForTest;
	}

	/**
	 * The rule language construct <code>Today</code> will evaluate to the passed date instead of
	 * the actual current date.<br>
	 * <b>Note:</b> Should only be used for test purposes.
	 */
	public void setCurrentDateForTest(Date currentDateForTest) {
		this.currentDateForTest = currentDateForTest;
	}

	public Locale getLanguage() {
		return language;
	}

	public void setLanguage(Locale language) {
		this.language = language;
	}

	/**
	 * @return The {@link ICustomConditionFactory}.
	 */
	public ICustomConditionFactory customConditionFactory() {
		return customConditionFactory;
	}

	public void setCustomConditionFactory(ICustomConditionFactory customConditionFactory) {
		this.customConditionFactory = customConditionFactory;
	}

	/**
	 * @return The {@link ICustomFieldTypeFactory}.
	 */
	public ICustomFieldTypeFactory customFieldTypeFactory() {
		return customFieldTypeFactory;
	}

	public void setCustomFieldTypeFactory(ICustomFieldTypeFactory customFieldTypeFactory) {
		this.customFieldTypeFactory = customFieldTypeFactory;
	}
}
