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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.datatransfer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.datatransfer.mappingmodel.IPrecompModelDataMapper;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.datatransfer.util.SplittingDelegatingResultForFormalValidationErrors;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.FormatDefinitionZahl;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.execution.StructuralMapperVK;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.execution.StructuralMappingCollisionVK;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.execution.StructuralMappingResultVK;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.structuralmapping.execution.StructurallyMappedFieldInstanceVK;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.Constants;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DatenImpl;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DatenImplWithOrigins;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.ErrorCause;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.InternalFallbackErrorLookup;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtExternalIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.IDataTransferService;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.MappedData;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.MappedField;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.SourceData;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.SourceIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICause;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;

import lombok.NonNull;

public class DataTransferServiceImpl implements IDataTransferService {
	public interface IDataTransferServiceParams {
		ICalculator precompCalculator();

		IMetaDataValidierung precompMetaObject();

		StructuralMapperVK getStructuralMapper();

		Set<String> supportedSourceNames();

		IValidator getValidatorForSource(String sourceName);

		/**
		 * <em>WARNING:</em> the precomp data mapper is currently stateful, therefore,
		 * implementations of this method must guarantee that each call returns a fresh independent instance.
		 */
		IPrecompModelDataMapper getPrecompModelDataMapper();

		IMetaDataValidierung getMetaObjectForSource(String sourceName);

		boolean isNoSourceValidation(String sourceName);
	}

	private final IDataTransferServiceParams params;

	public DataTransferServiceImpl(IDataTransferServiceParams params) {
		this.params = params;
	}

	@Override public @NonNull MappedData getTransferredData(
			@NonNull IData targetData,
			@NonNull List<SourceData> sourceData,
			@NonNull IResult targetResult,
			@NonNull IResult precomputationConsistencyResult,
			@NonNull DataTransferConfig config
	) {
		List<SourceData> assignableSourceDataAsList = sourceData.stream() // mapperWithMapBackState.mapDataIntoPrecompDM will take
				// care of omitting & reporting source instance that exceed the max allowed number for the corresponding source name
				.filter(s -> params.isNoSourceValidation(s.sourceName())
							 || this.validateForAssignability(s, mkValidationMode(config)))
				.toList();

		IPrecompModelDataMapper mapperWithMapBackState = params.getPrecompModelDataMapper();
		char precompDecimalSeaparator = params.precompMetaObject().getDezimalTrenner();
		List<SourceData> assignableSourceDataWithMappedDecimalSeparators = assignableSourceDataAsList.stream()
				.map(source -> mapDecimalSeparator(source, precompDecimalSeaparator))
				.toList();
		IPrecompModelDataMapper.MapToPrecompResult joinedDataAndExclusionInfo =
				mapperWithMapBackState.mapDataIntoPrecompDM(targetData, assignableSourceDataWithMappedDecimalSeparators,
						config.validationLanguage());
		IData joinedData = joinedDataAndExclusionInfo.dataForPrecomp();

		ICalculator calculator = params.precompCalculator();
		IResult splittingResult = new SplittingDelegatingResultForFormalValidationErrors(mapperWithMapBackState,
				targetResult, precomputationConsistencyResult,
				DataTransferServiceImpl::assignToPrecompFragmentUnlessFromTarget
		);

		DatenImplWithOrigins calculationResult = calculateNonNull(
				calculator, joinedData, mkValidationMode(config), splittingResult);
		var finalData = calculationResult.apply(joinedData); // apply the calculation results no matter what.
		// If errors occurred, we still want to map the other fields, and for now we don't even care that
		// _invalid_ calculated values are not omitted from the mapping, so we don't need to selectively apply
		// only valid computed fields.

		StructuralMappingResultVK structuralMappingResult = params.getStructuralMapper()
				.map(finalData, finalData, RtExternalIdentifier::of);

		structuralMappingResult.mappingCollisions()
				.forEach(collision -> reportWarningForStructuralMappingCollision(
						precomputationConsistencyResult, collision, config.validationLanguage()));

		return MappedDataImpl.success(
				new HashSet<>(structuralMappingResult.clearedGroupInstances()), // NOTE: the clearGroup identifiers will need to get
				// mapped back when the target can have a mountpoint (stage > 1)
				structuralMappingResult.mappedFieldInstances().stream().map(
						smf -> structurallyMappedFieldToMappedField(smf, calculationResult, mapperWithMapBackState)
				).toList(),
				joinedDataAndExclusionInfo.includedSourceIds()
		);
	}

	@Override public @NonNull Set<String> supportedSourceNames() {
		return Collections.unmodifiableSet(params.supportedSourceNames());
	}

	@Override public @NonNull Set<String> getTargetFields() {
		return params.getStructuralMapper().structuralMappingModel().mappingBlocks().stream()
				.flatMap(mb -> mb.fieldMappings().stream())
				.map(fm -> fm.targetField().fullName())
				.collect(Collectors.toSet());
	}

	/**
	 * calculate and make sure the result is non-null (replace any null result with an empty DatenImplWithOrigins)
	 */
	private static DatenImplWithOrigins calculateNonNull(
			ICalculator calculator, IData inputData, ValidationMode validationMode, IResult result
	) {
		DatenImplWithOrigins calculationResult = (DatenImplWithOrigins) calculator.calculateAll(
				inputData, validationMode, result);
		assert calculationResult != null || result.getCount(SeverityType.ERROR) > 0
				: "unexpected: if no errors occurred, the calculation result should probably be non-null";
		// the fieldsToClear of the newly created DatenImplWithOrigins do not need to be set since we know they are empty
		return calculationResult != null ? calculationResult : new DatenImplWithOrigins(Map.of());
	}

	private static String assignToPrecompFragmentUnlessFromTarget(SourceIdentifier errFieldId) {
		return errFieldId.sourceId().equals(SourceIdentifier.ID_FOR_TARGET) ?
				SourceIdentifier.ID_FOR_TARGET :
				SourceIdentifier.ID_FOR_PRECOMPUTATION_DM;
	}

	private SourceData mapDecimalSeparator(SourceData sourceData, char precompDecimalSeparator) {
		return SourceData.of(
				sourceData.sourceName(),
				sourceData.sourceId(),
				mapDecimalSeparator(sourceData.data(), sourceData.sourceName(), precompDecimalSeparator),
				sourceData.result());
	}

	private IData mapDecimalSeparator(IData data, String sourceName, char precompDecimalSeparator) {
		IMetaDataValidierung sourceMeta = params.getMetaObjectForSource(sourceName);
		char decimalSeparator = sourceMeta.getDezimalTrenner();
		if (decimalSeparator == precompDecimalSeparator) {
			// If there is no change in decimal separator, were done
			return data;
		}
		IData result = new DatenImpl();
		for (IIdentifier id : data) {
			String uniqueName = sourceMeta.getUniqueName(id.getName());
			String value = data.getValue(id);
			if (isFieldWithDecimalPlaces(sourceMeta.getIFormatDefinition(uniqueName))) {
				value = value.replace(decimalSeparator, precompDecimalSeparator);
			}
			result.addValue(id, value);
		}
		return result;
	}

	private boolean isFieldWithDecimalPlaces(IFormatDefinition fieldType) {
		return fieldType instanceof FormatDefinitionZahl numberType && numberType.hasDecimalPlaces();
	}

	private static void reportWarningForStructuralMappingCollision(IResult result, StructuralMappingCollisionVK collision,
			Locale localeForErrors) {
		String ruleName = "structuralMappingCollision";
		String errCode = "STRUCTURAL_MAPPING_COLLISION";
		IResult.ErrorType errType = IResult.ErrorType.VALUE_ERROR;
		SeverityType severity = SeverityType.HINT;
		IIdentifier errField = collision.targetFieldInstance();
		String errMsg = InternalFallbackErrorLookup.getBareErrorMessage(
				errCode,
				Constants.DATATRANSFER_ERROR_MESSAGES_MESSAGES_RESOURCE_NAME,
				localeForErrors) + " [" + errCode + "]";
		result.addHint(ruleName, errCode, errField, errMsg, errType, Set.of(errField), Set.of(), Set.of());
		ICause cause = new ErrorCause(errField, List.of(Set.of(errField)));
		result.addMessageV2(ruleName, errCode, errMsg, errType, severity, cause);
	}

	private static MappedField structurallyMappedFieldToMappedField(
			StructurallyMappedFieldInstanceVK smf,
			DatenImplWithOrigins calculationResult,
			IPrecompModelDataMapper mapperWithMapBackState
	) {
		return new MappedFieldImpl(
				smf.targetFieldInstance(),
				smf.value(),
				new MappedFieldSourceInfoImpl(
						calculationResult.getOriginsOrSelf(smf.sourceFieldInstance()).stream()
								.map(mapperWithMapBackState::mapIdentifierBack)
								.collect(Collectors.toSet()))
		);
	}

	@Override public IMetaModel _internal_getMetaObjectForSource(@NonNull String sourceName) {
		return this.params.getMetaObjectForSource(sourceName).getMetaModel();
	}

	@Override public IValidator _internal_getValidatorForSource(@NonNull String sourceName) {
		return this.params.getValidatorForSource(sourceName);
	}

	@SuppressWarnings("removal")
	private ValidationMode mkValidationMode(DataTransferConfig cfg) {
		var m = new ValidationMode();
		m.setLanguage(cfg.validationLanguage());
		m.setCurrentDateForTest(cfg.currentDateForTest()); // may be null
		if(cfg.additionalInformation()!=null) {
			cfg.additionalInformation().forEach(m::setAdditionalInformation);
		}
		m.setCustomConditionFactory(cfg.customConditionFactory()); // may be null
		m.setCustomFieldTypeFactory(cfg.customFieldTypeFactory()); // may be null
		return m;
	}

	private boolean validateForAssignability(SourceData sourceData, ValidationMode validationMode) {
		IValidator validator = params.getValidatorForSource(sourceData.sourceName());
		if (validator == null) {
			throw new IllegalArgumentException(
					"no validator found for name " + sourceData.sourceName());
		}
		return checkAssignable(validator, sourceData, validationMode);
	}

	private boolean checkAssignable(IValidator validator, SourceData sourceData, ValidationMode validationMode) {
		IResult result = sourceData.result();
		validator.validateAll(sourceData.data(), result, validationMode);
		return 0 == result.getCount(SeverityType.ERROR);
	}
}
