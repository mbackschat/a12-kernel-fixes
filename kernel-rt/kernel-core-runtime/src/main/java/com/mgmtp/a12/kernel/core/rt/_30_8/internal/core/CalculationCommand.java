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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.core;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DatenFilterImpl;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DatenImplWithOrigins;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtExternalIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculationResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult.ErrorType;

import lombok.NonNull;

public class CalculationCommand extends CheckCommand {
	protected final ICalculatorIntern calculator;

	private final CalculationCache calculationCache;
	private final Set<String> indexFieldFullNames;
	private final Set<String> calculatedFieldFullNames;
	private final Set<String> expandedOperandFieldFullNames;
	private final Set<String> entityFullNamesNotRemovedFromData; // empty and ignored for H-Models
	private final boolean isHModel;

	public CalculationCommand(
			@NonNull IValidatorIntern validator,
			ValidationMode modeOrNull,
			@NonNull IMetaDataValidierung metaData,
			@NonNull IData data,
			@NonNull ICalculatorIntern calculator
	) {
		super(validator, modeOrNull, metaData, data);
		this.calculationCache = new CalculationCache(metaData, this.modus, this.darstellungsInfo);
		this.validationCache = calculationCache;
		this.indexFieldFullNames = new HashSet<>(metaData.getIndexFieldFullNames());
		this.calculator = calculator;
		this.isHModel = metaData.hasUniqueEntities();
		this.calculatedFieldFullNames = metaData.getBerechneteFelder().stream()
				.map(calculatedField -> (String) calculatedField.getValue(IMetaKeys.FIELD_FULLNAME))
				.collect(Collectors.toUnmodifiableSet());
		this.expandedOperandFieldFullNames = getExpandedOperandFieldFullNames();
		this.entityFullNamesNotRemovedFromData = getEntityFullNamesNotRemovedFromData();
	}

	public ICalculationResult processCalc(@NonNull IData data, @NonNull IResult formaleFehler) {
		long startMesspunkt = LOG.startMesspunkt();

		Set<RtExternalIdentifier> previouslyFilledCalculatedFields = StreamSupport.stream(data.spliterator(), false)
				.filter(id -> calculatedFieldFullNames.contains(id.getName()) && !StringUtils.isEmpty(data.getValue(id)))
				.map(RtExternalIdentifier::new)
				.collect(Collectors.toSet());
		this.calculationCache.setPreviouslyFilledCalculatedFieldIds(previouslyFilledCalculatedFields);
		this.calculationCache.setDaten(getDataWithoutEntitiesNotNeededForCalculations(data));

		ICalculationResult result = processInternalC(formaleFehler);

		if (LOG.isTraceEnabled()) {
			LOG.logMessung("The calculation took ", startMesspunkt);
		}

		return result;
	}

	private IData getDataWithoutEntitiesNotNeededForCalculations(IData data) {
		return new DatenFilterImpl(data, id -> {
			// test that the given entity path exists, throws error otherwise
			String fullName = metaData.getPfadName(metaData.getUniqueName(id.getName()));
			return isNotRemovedFromData(fullName);
		});
	}

	/**
	 * @return all entities (full names) which must not be removed from the user-provided {@link IData} for calculations.
	 * 		<p>
	 * 		Includes:
	 * 		- ancestor groups of calculated fields (not the calculated fields themselves)
	 * 		- operand fields of calculations (+ their ancestor groups)
	 */
	private Set<String> getEntityFullNamesNotRemovedFromData() {
		Set<String> entityFullNamesRelevantForCalculation = new HashSet<>();
		for (String fieldFullName : getFieldFullNamesRelevantForCalculation()) {
			if (!shouldFieldValueBeIgnoredInFormalValidation(fieldFullName)) {
				entityFullNamesRelevantForCalculation.add(fieldFullName);
			}

			// all ancestors are needed
			String groupFullName = EntityUtil.getParentPath(fieldFullName);
			while (!groupFullName.isEmpty() && entityFullNamesRelevantForCalculation.add(groupFullName)) {
				groupFullName = EntityUtil.getParentPath(groupFullName);
			}
		}

		return entityFullNamesRelevantForCalculation;
	}

	private Set<String> getFieldFullNamesRelevantForCalculation() {
		Set<String> result = new HashSet<>();
		result.addAll(expandedOperandFieldFullNames);
		result.addAll(calculatedFieldFullNames);
		return result;
	}

	/**
	 * @return {@code true}, if the given field (full name) relevant for calculations should not have its value formally validated
	 */
	private boolean shouldFieldValueBeIgnoredInFormalValidation(String fullName) {
		return isCalculated(fullName); // calculations always produce a new value anyways
	}

	private @NonNull ICalculationResult processInternalC(@NonNull IResult erg) {
		formalePruefung(null, erg);
		if (metaData.hasUniqueEntities() && erg.getCountOf(ErrorType.VALUE_ERROR) > anzTestFehler) {
			return new DatenImplWithOrigins();
		}

		preliminaryValidationOfIndexFields(erg);
		if (metaData.hasUniqueEntities() && erg.getCountOf(ErrorType.VALUE_ERROR) > anzTestFehler) {
			return new DatenImplWithOrigins();
		}

		calculationCache.convertInvalidFieldsForCalculation();

		return executeCalculations(erg);
	}

	/**
	 * Executes the preliminary validation only for index fields which are operands.
	 */
	private void preliminaryValidationOfIndexFields(@NonNull IResult result) {
		var validationController = new MainValidatorController(result, validationCache, modus, metaData, formalChecker);

		for (String indexFieldFullName : indexFieldFullNames) {
			if (isNotRemovedFromData(indexFieldFullName)) {
				String uniqueIndexFieldName = metaData.getUniqueName(indexFieldFullName);
				validator.validatePreliminaryRulesForField(validationController, uniqueIndexFieldName);
			}
		}
	}

	private @NonNull ICalculationResult executeCalculations(@NonNull IResult erg) {
		CalculationController controller = new CalculationController(
				erg, calculationCache, modus, metaData, darstellungsInfo, formalChecker);

		calculator.calculate(controller);
		return calculationCache.getBerechneteFelder();
	}

	private Set<String> getExpandedOperandFieldFullNames() {
		return calculator.getExpandedOperandFieldsOfCalculations();
	}

	private boolean isNotRemovedFromData(String fullName) {
		// legacy behavior: in H-Models only calculated fields will be removed from the user-specified data
		return isHModel
				? !this.calculatedFieldFullNames.contains(fullName)
				: this.entityFullNamesNotRemovedFromData.contains(fullName);
	}

	private boolean isCalculated(String fullName) {
		return calculatedFieldFullNames.contains(fullName);
	}
}
