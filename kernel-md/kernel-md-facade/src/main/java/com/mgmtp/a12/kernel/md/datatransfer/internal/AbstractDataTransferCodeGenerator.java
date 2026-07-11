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

package com.mgmtp.a12.kernel.md.datatransfer.internal;

import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.appendList;
import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.mapMapValues;
import static com.mgmtp.a12.kernel.core.base.utils.internal.PackageNameUtil.packageOrFqnAsList;
import static com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BaseCodeGenUtils.encodeToGroovyClassname;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.Util.extendPackageName;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.FixedShapeMappingModelForCodegen.withoutRulesAndComputations;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.MappingModelUtils.getMappingModelForCodegen;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.MappingModelUtils.localizingWrapper;
import static com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.MappingModelUtils.reportAndReturnNullOnCodegenException;
import static com.mgmtp.a12.kernel.md.model.internal.service.ExternalDocumentModelService.getInternDocumentModel;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenContext;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.ValidationCodeGenerator;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BaseCodeGenUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.CodeGenResultUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.EntryPointCollection;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.ICodeGenResult;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.IEntryPoints;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.IMergedSingleScriptCodeGenResult;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.ISeparateFilesCodeGenResult;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.model.StructuralMappingModelForCodegen;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel.MappingModelForCodegen;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig.ProgrammingLanguage;
import com.mgmtp.a12.kernel.md.model.api.services.a12internal.ICodeGeneratorConfigV1_2;
import com.mgmtp.a12.kernel.md.model.internal.service.ExternalDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.codegen.MdCoreConversionUtil;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.kernel.md.structuralmapping.codegen.internal.StructuralMappingCodeGenerationService;
import com.mgmtp.a12.kernel.md.structuralmapping.codegen.internal.StructuralMappingCodeGenerationService.StructuralMappingEntryPoint;
import com.mgmtp.a12.kernel.md.structuralmapping.internal.util.StructuralMappingModelUtil;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.Getter;

public abstract class AbstractDataTransferCodeGenerator<
		R extends IDocumentModelResolver,
		C extends ICodeGeneratorConfigV1_2,
		N> {

	public static class DataTransferEntryPoint implements IEntryPoints {
		private final List<String> factory;

		public DataTransferEntryPoint(List<String> factory) {
			this.factory = factory;
		}

		public List<String> factory() {
			return this.factory;
		}
	}

	/**
     * similar to
     * {@link #generateDataTransferCode(MappingModelForCodegen, IDocumentModelResolver, Function,
     * ICodeGeneratorConfigV1_2, Consumer, Locale, boolean)}
     */
	// protected, in order to allow subclasses to control visibility themselves (e.g. via static methods)
	protected ICodeGenResult<DataTransferEntryPoint> generateDataTransferCode(
			MM_MappingModel_2 mappingModel,
			// note: the mappingmodel has already been validated during construction of the MappingModelForCodegen
			R resolver,
			Function<String, MM_StructuralMappingModel_1> structuralMappingModelResolver,
			C codegenConfig,
			Consumer<N> notificationReceiver,
			Locale localeForSomeButNotAllProblems,
			boolean addtargetWithoutRulesMetaCode // not currently user-configurable (internal-only), therefore not part of codeGenConfig
	) {
		MappingModelForCodegen mappingModelForCodegen = getMappingModelForCodegen(mappingModel,
				wrapNotificationReceiver(notificationReceiver), localeForSomeButNotAllProblems);

		if (mappingModelForCodegen == null) {
			return null; // error should already have been reported
		}

		return generateDataTransferCode(mappingModelForCodegen,
				resolver,
				structuralMappingModelResolver,
				codegenConfig,
				notificationReceiver,
				localeForSomeButNotAllProblems, addtargetWithoutRulesMetaCode);
	}

	private static final Set<ProgrammingLanguage> ALLOWED_PROGRAMMING_LANGUAGES = Set.of(
			ProgrammingLanguage.JAVA, ProgrammingLanguage.GROOVY);

	// @formatter:off (the formatter keeps adding indents to the code block below on every save)
	/**
	 * @param resolver
	 * 		must not return null (but is allowed to throw an exception if an id is not resolvable.).
	 * 		Note: as specified in the javadoc of {@link IDocumentModelResolver}, the resolver is responsible for
	 * 		expanding any includes that may exist in the resolved DMs. Such an include-expanding resolver can be
	 * 		created from a non-expanding resolver as follows: <pre>{@code
	 * 			IDocumentModelResolver expandingResolver = dmId -> {
	 * 				var dm = RESOLVER.getDocumentModelById(dmId);
	 * 				new DocumentModelServiceFactory().createDocumentModelService().expand(dm, RESOLVER::getDocumentModelById);
	 * 				return dm;
	 * 			};
	 *      }
	 * 			</pre>
	 * @param localeForSomeButNotAllProblems
	 * 		some problem notifications that may occur during code generation will be formatted
	 * 		according to this locale (note that not all checks take this parameter into account
	 * 		- especially the validation code generator will pick a locale by itself, based on
	 * 		the language of the corresponding model's condition language). Currently, only
	 *        {@link Locale#GERMANY} and {@link Locale#US} are supported.
	 */
	// @formatter:on
	// protected, in order to allow subclasses to control visibility themselves (e.g. via static methods)
	protected ICodeGenResult<DataTransferEntryPoint> generateDataTransferCode(
			MappingModelForCodegen mappingModel,
			// note: the mappingmodel has already been validated during construction of the MappingModelForCodegen
			R resolver,
			Function<String, MM_StructuralMappingModel_1> structuralMappingModelResolver,
			C codegenConfig,
			Consumer<N> notificationReceiver,
			Locale localeForSomeButNotAllProblems,
			boolean addtargetWithoutRulesMetaCode
	) {
		Consumer<RankedNotification> rankedNotificationConsumer = wrapNotificationReceiver(notificationReceiver);
		return reportAndReturnNullOnCodegenException(() -> {
			Validate.isTrue(ALLOWED_PROGRAMMING_LANGUAGES.contains(codegenConfig.getProgrammingLanguage()));
			if (codegenConfig.getProgrammingLanguage() == ProgrammingLanguage.JAVA) {
				// Only for java, collisions can happen between multiple code generations
				// (for groovy, code generation results are kept in separate isolated scripts)
				codegenConfig.getPackageName()
						.orElseThrow(() -> new IllegalArgumentException("Package name must be explicitly set " +
								"- the fallback default package name would result in collisions between code from different mapping models"));
			}
			List<String> packagePrefix = packageOrFqnAsList(
					BaseCodeGenUtils.getNormalizedPackageNameOrDefault(codegenConfig.getPackageName()));

			Map<String, ICodeGenResult<ValidationCodeGenerator.ValidationEntryPoint>> sourceValidators = mappingModel.sourceNames()
					.stream()
					.collect(Java8Utils.toNullableMap(
							sourceName -> sourceName,
							sourceName -> generateSourceValidationCode(
									sourceName,
									mappingModel,
									resolver, extendPackageName(codegenConfig, "sources." + sourceName), notificationReceiver)));
			if (sourceValidators.values().stream().anyMatch(Objects::isNull)) {
				return null;
			}

			IDocumentModel joinedModel = mappingModel.createPrecomputationDm(resolver,
					localizingWrapper(rankedNotificationConsumer, localeForSomeButNotAllProblems));

			ICodeGenResult<ValidationCodeGenerator.ValidationEntryPoint> joinedCalculator = generatePrecompValidationCode(
					joinedModel, extendPackageName(codegenConfig, "joined_precomputation_model"), rankedNotificationConsumer);
			if (joinedCalculator == null) {
				return null;
			}

			StructuralMappingModelForCodegen structuralMappingModel = StructuralMappingModelUtil.createAndPrepareValidStructuralMappingModel(
					structuralMappingModelResolver.apply(mappingModel.getStructuralMappingModelId()),
					joinedModel, joinedModel, rankedNotificationConsumer, localeForSomeButNotAllProblems);
			if (structuralMappingModel == null) {
				// problem messages have already been submitted to notificationReceiver
				return null;
			}
			var structuralMappingGenResult = generateStructuralMapperCode(
					structuralMappingModel,
					packagePrefix,
					codegenConfig);

			ICodeGenResult<PrecompMapperEntryPoint> precompDataMapper = serializePrecompDataMapper(mappingModel, packagePrefix,
					resolver, codegenConfig);

			ICodeGenResult<ValidationCodeGenerator.ValidationEntryPoint> targetWithoutRulesMetaIfAvailable = null;
			if(addtargetWithoutRulesMetaCode){
				String targetDmId = mappingModel.getTargetModelId();
				IDocumentModel targetDmWithoutRulesAndComps = new DocumentModelWrapper(withoutRulesAndComputations(getInternDocumentModel(resolver.getDocumentModelById(targetDmId))));
				targetWithoutRulesMetaIfAvailable = generateValidationCodeDMBased(targetDmWithoutRulesAndComps,
						extendPackageName(codegenConfig, "targetWithoutRulesAndComps"),
						wrapNotificationReceiver(notificationReceiver));
			}

			ICodeGenResult<DataTransferEntryPoint> factory = generateFactory(
					mappingModel,
					resolver,
					packagePrefix,
					precompDataMapper.entryPoints(),
					joinedCalculator.entryPoints(),
					mapMapValues(sourceValidators, ICodeGenResult::entryPoints),
					structuralMappingGenResult.entryPoints(),
					targetWithoutRulesMetaIfAvailable != null ? targetWithoutRulesMetaIfAvailable.entryPoints() : null,
					codegenConfig
			);

			ICodeGenResult<DataTransferEntryPoint> result = factory.mergeWith(
					Stream.concat(
							sourceValidators.values().stream(),
							Stream.of(precompDataMapper, joinedCalculator, structuralMappingGenResult, targetWithoutRulesMetaIfAvailable))
							.filter(Objects::nonNull) // only include targetWithoutRulesMetaIfAvailable if non-null (other entries should already be verified non-null
							.collect(Collectors.toList())
			);

			// some assertions are target-lang specific
			if(codegenConfig.getProgrammingLanguage() == ProgrammingLanguage.JAVA){
				CodeGenResultUtils.assertIsSeparateFileCodeGenResultAndAllEntryPathsStartWithPrefix(result, packagePrefix,
						CodeGenResultUtils.ALLOWED_FILES_IN_CODEGEN_ROOT::contains);
			}
			// for groovy we'd similarly expect that the class name (when written to a single-entry zip has a specific
			// collision-avoiding prefix, but this is onyl relevant for tests, not prod
			return result;
		}, rankedNotificationConsumer, localeForSomeButNotAllProblems);
	}

	/**
	 * no matter what notification class is supported by subclasses for the type paramter N, we need to be able to report
	 * RankedNotification, bescause that is what the structural mapping code-generator and the dm-based code generator for the
	 * precomputation dm report.
	 */
	protected abstract Consumer<RankedNotification> wrapNotificationReceiver(Consumer<N> notificationReceiver);

	protected abstract ICodeGenResult<DataTransferEntryPoint> generateFactory(
			MappingModelForCodegen mappingModel,
			R resolver,
			List<String> packageName,
			PrecompMapperEntryPoint precompMapperEntryPoint,
			ValidationCodeGenerator.ValidationEntryPoint precomputationEntryPoints,
			Map<String, ValidationCodeGenerator.ValidationEntryPoint> entryPointsForSourceValidators,
			StructuralMappingEntryPoint structuralMappingEntryPoint,
			ValidationCodeGenerator.ValidationEntryPoint targetWithoutRulesEntryPointIfAvailable,
			C codegenConfig);

	protected ICodeGenResult<ValidationCodeGenerator.ValidationEntryPoint> generateSourceValidationCode(
			String sourceName,
			MappingModelForCodegen mappingModel,
			R resolver,
			C codegenConfig,
			Consumer<N> notificationReceiver
	) {
		String sourceModelId = mappingModel.getSourceValidationModelId(sourceName);
		IDocumentModel dm = resolver.getDocumentModelById(sourceModelId);

		if (mappingModel.isNoSourceValidation(sourceName)) {
			dm = new DocumentModelWrapper(withoutRulesAndComputations(getInternDocumentModel(dm)));
		}
		return generateValidationCodeDMBased(dm, codegenConfig, wrapNotificationReceiver(notificationReceiver));
	}

	private ICodeGenResult<ValidationCodeGenerator.ValidationEntryPoint> generatePrecompValidationCode(
			IDocumentModel precompDm,
			C codegenConfig,
			Consumer<RankedNotification> notificationReceiver
	) {
		return generateValidationCodeDMBased(precompDm, codegenConfig, notificationReceiver);
	}

	private ICodeGenResult<ValidationCodeGenerator.ValidationEntryPoint> generateValidationCodeDMBased(
			IDocumentModel dm,
			C codegenConfigForSubpackage,
			Consumer<RankedNotification> notificationReceiver
	) {
		ExternalDocumentModelService codeGenerator = getDocumentModelService();
		ICodeGenResult<EntryPointCollection> genResult = codeGenerator.generateValidationCodeKernelInternal(dm,
				codegenConfigForSubpackage,
				null /* we do not support these output properties, and they would only make sense for the target dm, if at all */,
				notificationReceiver);
		if (genResult == null) {
			return null;
		}
		return genResult.withEntryPoints(Validate.notNull(
				genResult.entryPoints().getUniqueMatchingEntryPoint(ValidationCodeGenerator.ValidationEntryPoint.class),
				"no validation entry point found for generated validation code"));
	}

	protected ExternalDocumentModelService getDocumentModelService() {
		return new ExternalDocumentModelService();
	}

	private static <C extends ICodeGeneratorConfigV1_2> ICodeGenResult<StructuralMappingEntryPoint> generateStructuralMapperCode(
			StructuralMappingModelForCodegen smm,
			List<String> packageName, C codegenConfig) {
		var structuralMapperName = "StructuralMapper";
		return StructuralMappingCodeGenerationService.generateCode(smm, joinFqn(packageName),
				structuralMapperName, codegenConfig.getProgrammingLanguage());
	}

	static String makeMetaValidierungMapEntries(
			Map<String, ValidationCodeGenerator.ValidationEntryPoint> entryPointsForSourceValidators) {
		return makeMapEntries(entryPointsForSourceValidators, ValidationCodeGenerator.ValidationEntryPoint::metaFqn,
				".getInstance().getMetaDataValidierung()");
	}

	@StringTemplateGroup("base")
	private static class DataTransferMapEntry extends STParameterClassBase {
		private final String sourceName;
		private final String classFqn;
		private final String suffix;

		public DataTransferMapEntry(String sourceName, String classFqn, String suffix) {
			this.sourceName = sourceName;
			this.classFqn = classFqn;
			this.suffix = suffix;
		}

		@SuppressWarnings("unused") // accessed in template
		public String getClassFqn() {
			return classFqn;
		}

		@SuppressWarnings("unused") // accessed in template
		public String getGroovyFqn() {
			return BaseCodeGenUtils.encodeToGroovyClassname(classFqn);
		}

		@SuppressWarnings("unused") // accessed in template
		public String getSourceName() {
			return sourceName;
		}

		@SuppressWarnings("unused") // accessed in template
		public String getSuffix() {
			return suffix;
		}
	}

	static String makeMapEntries(
			Map<String, ValidationCodeGenerator.ValidationEntryPoint> entryPoints,
			Function<ValidationCodeGenerator.ValidationEntryPoint, List<String>> getEntryPoint,
			String suffix
	) {
		return entryPoints.keySet().stream()
				.map(sourceName ->
						new DataTransferMapEntry(
								sourceName,
								joinFqn(getEntryPoint.apply(entryPoints.get(sourceName))),
								suffix).toCode())
				.collect(Collectors.joining(",\n"));
	}

	private static String joinFqn(List<String> fqn) {
		return String.join(".", fqn);
	}

	static Object makeValidatorMapEntries(
			Map<String, ValidationCodeGenerator.ValidationEntryPoint> entryPointsForSourceValidators) {
		return makeMapEntries(entryPointsForSourceValidators, ValidationCodeGenerator.ValidationEntryPoint::validatorFqn,
				".getInstance()");
	}

	static String makeNoSourceValidationSources(MappingModelForCodegen mappingModel) {
		return mappingModel.sourceNames().stream()
				.filter(mappingModel::isNoSourceValidation)
				.map(sourceName -> "\"" + sourceName + "\"")
				.collect(Collectors.joining(",\n"));
	}

	public record PrecompMapperEntryPoint(List<String> serializedMapperResourceFqn) implements IEntryPoints {
	}

	@Getter @StringTemplateGroup("base")
	private static class PrecompDataMapperTmpl extends STParameterClassBase {
		private final String serializedModel;
		private final String groovyClassName;

		private PrecompDataMapperTmpl(String serializedModel, String groovyVarName) {
			this.serializedModel = serializedModel;
			this.groovyClassName = groovyVarName;
		}

	}

	/**
	 * @throws com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferCodegenException
	 * 		(see
	 *        {@link MappingModelForCodegen#rtPrecompModelDataMapper(IDocumentModelResolver)}
	 */
	private ICodeGenResult<PrecompMapperEntryPoint> serializePrecompDataMapper(
			MappingModelForCodegen mappingModel,
			List<String> packagePrefix,
			R resolver,
			C codegenConfig) {
		List<String> fqn = appendList(packagePrefix, "precomp_model_data_mapper");
		var programmingLanguage = MdCoreConversionUtil.convertProglangMdToCore(codegenConfig.getProgrammingLanguage());
		CodeGenContext.initialize(ICodeGeneratorParameters.Product.DATATRANSFER,
				programmingLanguage); // we need to set this context because the target-lang dependent template files are selected based on it
		String precompDataMapperCode = new PrecompDataMapperTmpl(
				mappingModel.rtPrecompModelDataMapper(resolver).serialize(),
				StringUtils.capitalize(fqn.stream().collect(Collectors.joining("_")))).toCode();

		return switch (codegenConfig.getProgrammingLanguage()) {
			case GROOVY -> IMergedSingleScriptCodeGenResult.of(
					new PrecompMapperEntryPoint(fqn),
					"",
					Set.of(),
					"",
					List.of(precompDataMapperCode),
					List.of("placeholder_file_name_for_singlemergedfile_standalone_precomp_mapper_should_not_occur")
			);
			case JAVA -> ISeparateFilesCodeGenResult.ofSingleFileUtf8(fqn, PrecompMapperEntryPoint::new, "", precompDataMapperCode);
			default -> throw new NotImplementedException("not implemented for target languages other than java and groovy");
		};
	}

	protected List<String> makeGroovyFilenamePath(DataTransferEntryPoint entryPoint) {
		return List.of(encodeToGroovyClassname(entryPoint.factory) + ".groovy"); // I think this is currently only used by test code, and the current result is simply what the tests expect
	}
}
