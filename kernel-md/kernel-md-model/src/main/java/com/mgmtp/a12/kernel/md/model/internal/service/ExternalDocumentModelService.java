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

package com.mgmtp.a12.kernel.md.model.internal.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.core.base.utils.internal.Consumer2ProblemReporter;
import com.mgmtp.a12.kernel.core.codegen.internal.CodeGeneratorService;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenContext;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BaseCodeGenUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.CodeGenMap;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.ICodeOutput;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.LanguageHelper;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.CodeGenResultUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.EntryPointCollection;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.ICodeGenResult;
import com.mgmtp.a12.kernel.core.facade.a12internal.ServiceFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IMetaInfoCodeGenParams;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.ICalculationService;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelContent;
import com.mgmtp.a12.kernel.md.model.a12internal.services.ElementContainer;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.MetaInfoCodegenParams;
import com.mgmtp.a12.kernel.md.model.internal.service.codegen.CodeGeneratorParameters;
import com.mgmtp.a12.kernel.md.model.internal.service.codegen.MdCoreConversionUtil;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.AttachmentGroupRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.BasicConsistencyCheckFromSerializerRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.CodeGenerationRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.DocumentSchemaVersionPatternRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.DocumentSchemaVersionRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.DocumentUniquenessCriteriaRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.IdUniqueRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.MultiSelectGroupRule;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocModelToIECMappingUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelAnnotationVisitor;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelServiceUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.ExternalDocumentModelSerializerService;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;

public class ExternalDocumentModelService implements IDocumentModelService {

	private final boolean considerNotInD;

	public ExternalDocumentModelService(boolean considerNotInD) {
		this.considerNotInD = considerNotInD;
	}

	public ExternalDocumentModelService() {
		this(false);
	}

	@Override
	public String getPath(@NonNull IElement element) {
		return ElementUtils.getPath(element);
	}

	@Override
	public void checkConsistency(
			@NonNull IDocumentModel documentModel,
			@NonNull Consumer<RankedNotification> notificationReceiver
	) {
		Stream.of(
				new DocumentSchemaVersionPatternRule(),
				new DocumentSchemaVersionRule(),
				new IdUniqueRule(),
				new AttachmentGroupRule(),
				new MultiSelectGroupRule(),
				new DocumentUniquenessCriteriaRule(),
				new CodeGenerationRule(),
				new BasicConsistencyCheckFromSerializerRule()
		).forEach(rule -> rule.checkModel(documentModel, notificationReceiver::accept));
	}

	@Override
	public byte[] generateValidationCode(@NonNull IDocumentModel documentModel, @NonNull IValidationCodeGeneratorConfig config,
			Properties properties, @NonNull Consumer<RankedNotification> notificationReceiver) {
		return CodeGenResultUtils.toBytes(
				generateValidationCodeKernelInternal(documentModel, config, properties, notificationReceiver),
				config.isPlain(),
				MdCoreConversionUtil.convertProglangMdToCore(config.getProgrammingLanguage()));
	}

	public ICodeGenResult<EntryPointCollection> generateValidationCodeKernelInternal(
			@NonNull IDocumentModel documentModel,
			@NonNull IValidationCodeGeneratorConfig config,
			Properties properties,
			@NonNull Consumer<RankedNotification> notificationReceiver
	) {
		Validate.isTrue(
				!config.isPlain() || config.getProgrammingLanguage() == IValidationCodeGeneratorConfig.ProgrammingLanguage.JAVASCRIPT || config.getProgrammingLanguage() == IValidationCodeGeneratorConfig.ProgrammingLanguage.GROOVY,
				"Plain source code can only be generated for " + IValidationCodeGeneratorConfig.ProgrammingLanguage.JAVASCRIPT.name() + " or " + IValidationCodeGeneratorConfig.ProgrammingLanguage.GROOVY.name());
		// We should not really need to initialize the CodeGenContext as long as we do not access toCode methods.
		CodeGenContext.initialize(ICodeGeneratorParameters.Product.VALIDATION,
				MdCoreConversionUtil.convertProglangMdToCore(config.getProgrammingLanguage()));
		IProblemReporter internPr = new Consumer2ProblemReporter(notificationReceiver);
		ElementContainer ec = DocumentModelServiceUtils.convert(documentModel, considerNotInD, internPr);
		IServiceConfiguration serviceConfig = DocumentModelServiceUtils.createServiceConfiguration(documentModel, ec);
		CodeGeneratorService codegenService = new CodeGeneratorService(serviceConfig);
		ICalculationService calcService = new ServiceFactory().getCalculationService(serviceConfig);

		Optional<List<ICalculationInfo>> calculationInfo = calcService.checkAndExtractAllCalculationInfo(ec.getAllCalculations(),
				internPr);

		boolean problemsWithCalculations = calculationInfo.isEmpty();
		ICodeGeneratorParameters params = createCodeGeneratorParameters(documentModel, config,
				calculationInfo.orElse(Collections.emptyList()));

		ICodeGenResult<EntryPointCollection> result = codegenService.getSourceCodeAsCodeGenResult(
				ec.getAllRules(), params, internPr);

		// if calculation infos were omitted due to problems, we should not return sucessfully (which could be misleading for users)
		// but instead abort as soon as all subsequent problems have been found and reported to internPr / notificationReceiver.
		if (problemsWithCalculations) {
			return null;
		}

		if (result != null && properties != null) {
			updateMappingProperties(properties, params);
		}

		return result;
	}

	private ICodeGeneratorParameters createCodeGeneratorParameters(
			final IDocumentModel model,
			final IValidationCodeGeneratorConfig config,
			final List<ICalculationInfo> calculationInfo) {
		assert !considerNotInD || model.getContent().getDocumentModelConfig().getDecimalSeparator().length() == 1;
		return new CodeGeneratorParameters(
				model.getContent().getDocumentModelConfig().getSupportedCharacters(),
				considerNotInD ? model.getContent().getDocumentModelConfig().getDecimalSeparator().charAt(0) : '.',
				MdCoreConversionUtil.convertProglangMdToCore(config.getProgrammingLanguage()),
				calculationInfo,
				DocumentModelServiceUtils.createGeneratedCodeId(model),
				model.getHeader().getId(),
				ListUtils.emptyIfNull(model.getHeader().getLocales()),
				DocumentModelContent.VERSION,
				config.getPackageName(),
				DocModelToIECMappingUtils.convertJsCodeGenConfig(config.getJsCodeGenConfig()),
				config.isPlain(),
				createMetaInfoParams(model));
	}

	private List<IMetaInfoCodeGenParams> createMetaInfoParams(IDocumentModel model) {
		final MetaInfoCodegenParams params = new MetaInfoCodegenParams(IMetaInfoCodeGenParams.META_ELEMENT_TYPE_FIELD,
				IMetaKeys.FIELD_LABEL,
				null);
		new DocumentModelWalker().acceptDocumentModel(model, new DocumentModelVisitor() {
			@Override public DocumentModelWalker.VisitProcess visitField(IField field) {
				params.addValue(ElementUtils.getPath(field), createLabelCode(field.getLabel()));
				return super.visitField(field);
			}
		});
		return List.of(params);
	}

	private ICodeOutput createLabelCode(ILocalizedTextMap labels) {
		CodeGenMap<LanguageHelper, StringParameter> result = new CodeGenMap<>("Locale, String");
		labels.forEach((locale, label) -> result.putEntry(new LanguageHelper(locale), new StringParameter(label)));
		return result;
	}

	private void updateMappingProperties(
			final Properties properties,
			final ICodeGeneratorParameters params) {

		final String key = params.getName();
		properties.setProperty(key, BaseCodeGenUtils.getNormalizedPackageNameOrDefault(params) + key);
	}

	@Override
	public Map<String, String> getAllAnnotatedElements(@NonNull IDocumentModel documentModel, @NonNull String annotationKey) {
		com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker walker = new com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker();
		DocumentModelAnnotationVisitor visitor = new DocumentModelAnnotationVisitor(annotationKey);
		DocumentModel intDm = getInternDocumentModel(documentModel);
		walker.acceptDocumentModel(intDm, visitor);
		return visitor.getAllAnnotations();
	}

	public static DocumentModel getInternDocumentModel(IDocumentModel documentModel) {
		return ExternalDocumentModelSerializerService.getInternDocumentModel(documentModel);
	}
}
