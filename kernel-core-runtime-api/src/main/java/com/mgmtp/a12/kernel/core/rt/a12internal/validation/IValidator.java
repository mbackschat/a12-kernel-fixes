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

import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidatorException;
import com.mgmtp.a12.kernel.core.rt.a12internal.custom.impl.CustomConditionService;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * API for validation method calls.
 * <br>
 * The methods are thread-safe.
 */
// @formatter:off (the formatter keeps adding indents in the comments below on every save)
@OnlyForUsage
public interface IValidator {

	/**
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Please use
	 *        {@link #validatePartlyWith3ValueLogic(IData, Set, IResult, ValidationMode)} instead and provide your
	 *        {@link ICustomCondition}s in {@link ValidationMode#customConditionFactory()} instead.
	 */
	@Deprecated(since = "30.6.0", forRemoval = true)
	void validatePartlyWith3ValueLogic(
			IData data,
			Set<IIdentifier> relevantEntities,
			Map<String, ICustomCondition> customConditions,
			IResult validationResult,
			ValidationMode mode // null currently allowed (contrary to javadoc) --> defaults to `new ValidationMode()`
	);

	// @formatter:off (prevent indent on every save)
	/**
	 * This method executes a partly validation. Only the entities defined in
	 * <code>relevantEntities</code> will be checked. Only rules, whose error field is a relevant
	 * field, are checked.
	 * Rule conditions are validated in a way, which will not return errors, if omission
	 * errors of non-relevant fields are present.
	 * <p>
	 * <b>Note:</b>
	 * <ul>
	 * <li>Some rules may eventually not fire if not all fields referenced by the rule are
	 * marked as relevant for the validation.</li>
	 * </ul>
	 *
	 * @param data
	 * 		the data to be validated. May not be <code>null</code>.
	 * @param relevantEntities
	 * 		Specification of the entities (fields and/or groups) relevant for the validation.
	 * 		If <code>null</code> is passed, an <code>IllegalArgumentException</code> will be
	 * 		thrown.
	 * 		<p>
	 * 		Entities in the set of relevant entities may specify one or more indices with
	 *        {@link IIdentifier#ALL_INDICES}. This is recommended as a short-cut specification,
	 * 		e.g. if all one thousand instances of an entity are relevant.
	 * 		For Groups in the set of relevant entities, their descendants (fields and/or
	 * 		groups) are assumed as relevant, too. Indices of the descendant levels are
	 * 		implicitly assumed as {@link IIdentifier#ALL_INDICES}.
	 * 		If entity instances with a higher index value than the maximum repetition of the
	 * 		corresponding repetition layer are passed, these entity instances will be ignored.
	 *
	 * 		<p>
	 * 		Note:
	 * 		</p>
	 * 		Setting all index levels to {@link IIdentifier#ALL_INDICES} including non
	 * 		repeatable layers instead of listing each single index will give a performance
	 * 		gain. Listing all indices of a repetition layer separately is discouraged not only
	 * 		because of the performance, but also because some conditions are not evaluated in
	 * 		this case (NumberOfFilledFields, NumberOfFilledGroups and in some constellations
	 * 		semantic index and parallel iteration)
	 * @param validationResult
	 * 		container object for resulting messages on erroneous plausibility checks. May not
	 * 		be <code>null</code>.
	 * @param mode
	 * 		specifies the mode of validation. May not be <code>null</code>.
	 * @throws ValidatorException
	 * 		<ul>
	 * 			<li>If a rule that is validated uses a custom condition which is unknown (either in
	 * 				{@link ValidationMode#customConditionFactory()} or not registered in the deprecated
	 * 				{@link CustomConditionService}.</li>
	 * 		</ul>
	 * @throws IllegalArgumentException
	 * 		<ul>
	 * 			<li>if <i>data, relevantEntities</i> or <i>validationResult</i> are <code> null</code>.</li>
	 * 			<li>if the mode of validation specifies a language not supported by the generated code.</li>
	 * 		</ul>
	 * @throws CustomConditionException
	 * 		<ul>
	 * 			<li>If an {@link ICustomCondition} throws it during validation.</li>
	 * 		</ul>
	 */
	// @formatter:on
	default void validatePartlyWith3ValueLogic(
			@NonNull IData data,
			Set<IIdentifier> relevantEntities,
			@NonNull IResult validationResult,
			ValidationMode mode // null currently allowed (contrary to javadoc) --> defaults to new `ValidationMode()`
	) {
		// A default implementation exists solely to allow the use of generated code before 30.2.0 via this API.
		// This default implementation will be removed with A12K-3589.
		// Use customConditions 'null' to be compatible with ValidationCommand#cacheAllCustomConditions(..) in previous version.
		validatePartlyWith3ValueLogic(data, relevantEntities, null, validationResult, mode);
	}

	// @formatter:off (prevent indent on every save)
	/**
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Please use
	 *        {@link #validateAll( IData, IResult, ValidationMode)} instead and provide your
	 *        {@link ICustomCondition}s in {@link ValidationMode#customConditionFactory()} instead.
	 */
	// @formatter:on
	@Deprecated(since = "30.6.0", forRemoval = true)
	void validateAll(
			@NonNull IData data,
			Map<String, ICustomCondition> customConditions,
			@NonNull IResult validationResult,
			ValidationMode mode // null currently allowed (contrary to javadoc) --> defaults to `new ValidationMode()`
	);

	// @formatter:off (prevent indent on every save)
	/**
	 * This method executes a full validation.
	 * <p>
	 * ExtMP specific additional information:<br>
	 * In a full validation, all rules of the model are checked. However, the Kernel runtime system uses internal optimization
	 * strategies so that only rules are checked that can lead to an error for the passed values. This must be taken into account
	 * when considering validation traces. For example, for root groups that may occur more than once but are not mandatory, only
	 * those root group numbers are checked for which values have been passed.<br>
	 * <p>
	 * The use of USBs in the root groups is also optimized, as the number of maximum USBs is usually very high in relation to the
	 * actual USBs specified. Here too, only the quantity of the specified USBs is taken into account at the appropriate points.
	 * <br>
	 * <p>
	 * The use of maximum rows is also optimized, as - especially with a high maximum number of rows - data is usually not
	 * specified for all rows. Here, only the quantity of the specified rows is taken into account at the appropriate points.
	 *
	 * @param data
	 * 		the data to be validated. May not be <code>null</code>.
	 * @param validationResult
	 * 		container object for resulting messages on erroneous plausibility checks. May not
	 * 		be <code>null</code>.
	 * @param mode
	 * 		specifies the mode of validation. May not be <code>null</code>.
	 * @throws ValidatorException
	 * 		<ul>
	 * 			<li>If a rule that is validated uses a custom condition which is unknown (either in
	 * 				{@link ValidationMode#customConditionFactory()} or not registered in the deprecated
	 * 				{@link CustomConditionService}).</li>
	 * 		</ul>
	 * @throws IllegalArgumentException
	 * 		<ul>
	 * 			<li>if <i>data, relevantFields</i> or <i>validationResult</i> are <code> null</code>.</li>
	 * 			<li>if the mode of validation specifies a language not supported by the generated code.</li>
	 * 		</ul>
	 * @throws CustomConditionException
	 * 		<ul>
	 * 			<li>If an {@link ICustomCondition} throws it during validation.</li>
	 * 		</ul>
	 */
	// @formatter:on
	default void validateAll(
			@NonNull IData data,
			@NonNull IResult validationResult,
			ValidationMode mode // null currently allowed (contrary to javadoc) --> defaults to `new ValidationMode()`
	) {
		// A default implementation exists solely to allow the use of generated code before 30.2.0 (ER-54) via this API.
		// This default implementation will be removed with A12K-3589.
		// Use customConditions 'null' to be compatible with ValidationCommand#cacheAllCustomConditions(..) in previous version.
		validateAll(data, null, validationResult, mode);
	}
}
