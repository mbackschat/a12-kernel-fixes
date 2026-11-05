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

package com.mgmtp.a12.kernel.md.datatransfer.a12internal.mappingmodel;

import static com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.MappingModelUtils.ensureSmmId;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.MappingModelUtils.getMappingModelForCodegen;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.MappingModelUtils.localizingWrapper;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.MappingModelUtils.reportAndReturnNullOnCodegenException;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.CodeGenResultUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.ISeparateFilesCodeGenResult;
import com.mgmtp.a12.kernel.md.datatransfer.internal.DataTransferCodeGenerator;
import com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.MappingModelForCodegen;
import com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.MappingModelUtils.SmmGenInfos;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig;
import com.mgmtp.a12.kernel.md.model.api.services.a12internal.ICodeGeneratorConfigV1_2;
import com.mgmtp.a12.kernel.md.model.internal.service.codegen.MdCoreConversionUtil;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.MMDocumentModelLoader;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.SMMGeneratorConfig;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.StructuralMappingModelService;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.generator.SMMGeneratorForMigration;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@OnlyForUsage
public class MappingModelService {
	private static final String META_MODEL_DM_NAME = "extmpinternal/none-mm_mappingmodel-2.json";
	public static final String RUNTIME_PRECOMP_FRAGMENT_MODEL_TYPE = "runtime_mapping_precompfragment_document";
	@Getter(value = AccessLevel.PRIVATE, lazy = true)
	private static final IDocumentModel metaModel = MMDocumentModelLoader.deserializeMMDocumentModel(META_MODEL_DM_NAME);
	@Getter(value = AccessLevel.PRIVATE, lazy = true)
	private static final IDocumentModelResolver metaModelResolver = MMDocumentModelLoader.createResolver(metaModel());
	private static final IDocumentV2Serializer documentSerializer = new DocumentSerializerImpl(metaModelResolver());

	@NonNull private final IDocumentModelResolver dmResolver;

	private MappingModelService(IDocumentModelResolver dmResolver) {
		this.dmResolver = Objects.requireNonNull(dmResolver);
	}

	public static MappingModelService create(@NonNull IDocumentModelResolver documentModelResolver) {
		return new MappingModelService(documentModelResolver);
	}

	public boolean checkMappingModelConsistency(
			@NonNull MM_MappingModel_2 mappingModel,
			@NonNull Function<String, MM_StructuralMappingModel_1> structuralMappingModelResolver,
			@NonNull Consumer<? super RankedNotification> notificationReceiver
	) {
		return null != generateMappingCode(
				mappingModel,
				ICodeGeneratorConfigV1_2.of(
						"packagename.should_not_matter.for_consistency_check",
						IValidationCodeGeneratorConfig.ProgrammingLanguage.JAVA),
				structuralMappingModelResolver,
				notificationReceiver);
	}

	public interface GeneratedSources {
		/**
		 * @return the zipfile bytes of the generated code. For
		 *        {@link IValidationCodeGeneratorConfig.ProgrammingLanguage#JAVA} the zip is laid out as a standalone Gradle
		 * 		Java project: sources under {@code src/main/java/}, resources under {@code src/main/resources/}, with
		 *        {@code build.gradle} / {@code settings.gradle} at the root.
		 */
		byte[] toZip();

		/**
		 * @return a map where the keys are source file paths within the logical output layout and the values are the
		 * 		contents of the source files. For {@link IValidationCodeGeneratorConfig.ProgrammingLanguage#JAVA} keys
		 * 		follow the Gradle Java project layout (e.g.
		 *        {@code src/main/java/com/mgmtp/a12/.../Factory.java}).
		 */
		Map<String, byte[]> sourceFiles();
	}

	/**
	 * @param mappingModel
	 * 		The modelled specification of the mapping.
	 * @param config
	 * 		The configuration of the code generation. It can be created e.g. via {@link ICodeGeneratorConfigV1_2#of}.
	 * 		<p>Notes:
	 * 		     <ul>
	 * 		         <li>Currently, only {@link IValidationCodeGeneratorConfig.ProgrammingLanguage#JAVA} is supported as
	 * 		         programming language.</li>
	 * 		         <li>If you want to use the mapping code of several mapping models in parallel, please specify {@link IValidationCodeGeneratorConfig#getPackageName()} to avoid overlaps in the generated code.</li>
	 * 			</ul>
	 * 		</p>
	 * @param structuralMappingModelResolver
	 * @param notificationReceiver
	 */
	public GeneratedSources generateMappingCode(
			@NonNull MM_MappingModel_2 mappingModel,
			@NonNull ICodeGeneratorConfigV1_2 config,
			@NonNull Function<String, MM_StructuralMappingModel_1> structuralMappingModelResolver,
			@NonNull Consumer<? super RankedNotification> notificationReceiver
	) {
		var result = DataTransferCodeGenerator.createCodeForMappingModel(
				mappingModel,
				this.dmResolver,
				structuralMappingModelResolver,
				config,
				notificationReceiver::accept,
				Locale.US,
				false // NOTE: maybe for md it would be really helpful if we _did_ include the target meta code here, so that static
				// datatransfer code becomes usable without separately providing target meta code. Would maybe require adding a new
				// optional method to the datatransferservice interface or similar.
		);
		return result == null ? null :
				new GeneratedSources() {
					@Override public byte[] toZip() {
						return CodeGenResultUtils.toBytes(
								result, false,
								MdCoreConversionUtil.convertProglangMdToCore(config.getProgrammingLanguage()));
					}

					@Override public Map<String, byte[]> sourceFiles() {
						return switch (result) {
							case ISeparateFilesCodeGenResult<?> separateFilesResult ->
									config.getProgrammingLanguage() == IValidationCodeGeneratorConfig.ProgrammingLanguage.JAVA
											? CodeGenResultUtils.applyGradleJavaLayout(separateFilesResult.sources())
											: separateFilesResult.sources();
							// technically we could return a "files" map, with the single file and file name. should we? (depends on expected subsequent use, probably?)
							// note: non-ISeparateFilesCodeGenResult should not happen when (static) java code is generated, and I think this method is only meant for java code, not groovy?
							default -> throw new UnsupportedOperationException(
									"sourceFiles() is not supported for codegenresults other than ISeparateFilesCodeGenResult, got " + result.getClass());
						};
					}
				};
	}

	/**
	 * Create the source and target Document Model without validation rules or computations - to be used for
	 * {@link StructuralMappingModelService#create(DocumentModel, DocumentModel)}.
	 *
	 * @param mappingModelDocument
	 * 		the Mapping Model to use as a reference for creating the Document Models.
	 * @param notificationReceiver
	 * 		a callback that will be fed problems with the mapping model and the creation of the precomputation DM.
	 * @param locale
	 * 		the locale to report the issues to the {@code notificationReceiver} in.
	 * @return {@code null}, if errors were reported, otherwise the source and target Document Model for the Mapping Model as used
	 * 		by the Structural Mapping Model.
	 */
	public SourceAndTargetDMForSMM createSourceAndTargetDMForSMM(
			@NonNull MM_MappingModel_2 mappingModelDocument,
			@NonNull Consumer<? super RankedNotification> notificationReceiver,
			@NonNull Locale locale
	) {
		MappingModelForCodegen mappingModel = getMappingModelForCodegen(
				ensureSmmId(mappingModelDocument), notificationReceiver, locale);
		if (mappingModel == null) {
			return null;
		}

		return reportAndReturnNullOnCodegenException(() ->
						// the rules and computations are already removed, so nothing to adapt here
						mappingModel.createSourceAndTargetDMForSMM(dmResolver, localizingWrapper(notificationReceiver, locale))
				, notificationReceiver, locale);
	}

	/**
	 * Create the base {@link DocumentModel} or "reference dm" onto which the pre-computation fragment of this mapping model gets
	 * added that results in the pre-computation model. The mapping model determines the behavior of the data transfer.
	 * <p>
	 * <b>Hint on its supported characters:</b> The received {@link DocumentModel} basically specifies the superset of all character
	 * sets supported by
	 * Kernel, because if an intermediate value were to be rejected during data transfer due to a
	 * character restricted here, this would lead to an error that is incomprehensible to the end user. On the one hand during
	 * data transfer input values are validated against their corresponding source {@link DocumentModel} (of course including
	 * validation against its supported characters). Invalid source documents are excluded from data transfer and data transfer
	 * reports corresponding validation errors. On the other hand transferred values should be validated against the target
	 * {@link DocumentModel}, i.e. if a character is invalid according to the specification in the target {@link DocumentModel},
	 * this is reported as a validation error and can be displayed in an understandable way to the end user (the validation of
	 * transferred values against the target {@link DocumentModel} is not part of the data transfer, it can and should be called
	 * manually afterwards).
	 * </p>
	 *
	 * @param mappingModelDocument
	 * 		the Mapping Model to use as a reference for creating the Document Models.
	 * @param notificationReceiver
	 * 		a callback that will be fed problems with the mapping model and the creation of the precomputation DM.
	 * @param locale
	 * 		the locale to report the issues to the {@code notificationReceiver} in.
	 * @return {@code null}, if errors were reported, otherwise the source and target Document Model for the Mapping Model as used
	 * 		by the Structural Mapping Model.
	 */
	public DocumentModel createBaseDmForPrecomputationFragment(
			@NonNull MM_MappingModel_2 mappingModelDocument,
			@NonNull Consumer<? super RankedNotification> notificationReceiver,
			@NonNull Locale locale
	) {
		MappingModelForCodegen mappingModel = getMappingModelForCodegen(
				ensureSmmId(mappingModelDocument), notificationReceiver, locale);
		if (mappingModel == null) {
			return null;
		}

		return reportAndReturnNullOnCodegenException(() -> {
			var iDocumentModel = mappingModel.createBaseDMForPrecomputationFragment(
					dmResolver, localizingWrapper(notificationReceiver, locale));
			return ((DocumentModelWrapper) iDocumentModel).getDocumentModel();
		}, notificationReceiver, locale);
	}

	/**
	 * @param mappingModelDocument
	 * 		the Mapping Model to use as a reference for generating the SMM.
	 * @param config
	 * 		a configuration of the SMM generator
	 * @param notificationReceiver
	 * 		a callback that will be fed problems with the mapping model and the creation of the precomputation DM.
	 * @param locale
	 * 		the locale to report the issues to the {@code notificationReceiver} in.
	 * @return {@code null}, if errors were reported, otherwise the generated SMM.
	 */
	public MM_StructuralMappingModel_1 generateStructuralMappingModel(
			@NonNull MM_MappingModel_2 mappingModelDocument,
			@NonNull SMMGeneratorConfig config,
			@NonNull Consumer<? super RankedNotification> notificationReceiver,
			@NonNull Locale locale
	) {
		SmmGenInfos smmGenInfos = SmmGenInfos.create(
				ensureSmmId(mappingModelDocument, config.idForGeneratedSMM()), dmResolver, notificationReceiver, locale);
		if (!(smmGenInfos instanceof SmmGenInfos(
				MappingModelForCodegen mappingModelForCodegen,
				SourceAndTargetDMForSMM sourceAndTargetDMForSMM,
				Map<String, String> originalRootGroupNamesBySourceName
		))) {
			return null;
		}
		return SMMGeneratorForMigration.generateStructuralMappingModel(
				new DocumentModelWrapper(sourceAndTargetDMForSMM.sourceDM()),
				new DocumentModelWrapper(sourceAndTargetDMForSMM.targetDM()),
				rootGroupShortName -> originalRootGroupNamesBySourceName.getOrDefault(rootGroupShortName, rootGroupShortName),
				List.of(),
				config);
	}

	/**
	 * See {@link IDocumentV2Serializer#serializeV2(DocumentV2, Writer, DocumentSerializationConfig)}.
	 */
	public static void serialize(@NonNull MM_MappingModel_2 document, @NonNull Writer writer) {
		documentSerializer.serializeV2(document._unwrap(), writer, DocumentSerializationConfig.builder().build());
	}

	/**
	 * See {@link IDocumentV2Serializer#deserializeV2(Reader, String, DocumentDeserializationConfig, Consumer)}.
	 */
	public static @NonNull MM_MappingModel_2 deserialize(@NonNull Reader reader,
			@NonNull Consumer<? super RankedNotification> notificationReceiver) {
		DocumentV2 document = documentSerializer.deserializeV2(
				reader,
				metaModel().getHeader().getId(),
				DocumentDeserializationConfig.builder().build(),
				notificationReceiver::accept);
		return MM_MappingModel_2._viewOf(document);
	}
}
