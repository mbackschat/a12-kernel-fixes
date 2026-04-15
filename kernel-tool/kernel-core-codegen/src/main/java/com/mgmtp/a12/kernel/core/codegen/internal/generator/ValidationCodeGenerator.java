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

package com.mgmtp.a12.kernel.core.codegen.internal.generator;

import static com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.CodeGenResultUtils.GROOVY_FILE_INFO_KEY;
import static com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.CodeGenResultUtils.GROOVY_GLOBALS_KEY;
import static com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.CodeGenResultUtils.GROOVY_IMPORT_KEY;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenMetaFields;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenMetaFieldsBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenMetaModel;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenModel;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenModelBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenModelWrapper;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenRules;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenRulesBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenTableHolder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenTeilValidierung;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.CodeGenTypescriptDefCode;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.builder.CodeGenMetaModelBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.builder.CodeGenTableHolderBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.calc.CodeGenCalc;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.calc.CodeGenCalcBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.dynamicEnum.CodeGenDynamicEnumerations;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.dynamicEnum.CodeGenDynamicEnumerationsBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.CodeGenMetaPflichtfeldInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.CodeGenMetaPflichtfeldInfoBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.CodeGenMetaRules;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.CodeGenMetaRulesBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta.MetaRegelFehlertexte;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.teilvalidierung.CodeGenTeilvalidierungBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RuleInfo;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.CodeGenResultUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.IEntryPoints;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.SourceCodeCollectorV2;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.CodeGenUtilities;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.VBALineNormalizer;
import com.mgmtp.a12.kernel.core.codegen.internal.validation.VKCodeGenValidationDataCollector;
import com.mgmtp.a12.kernel.core.codegen.internal.validation.ValidationCodeGenDataChecker;
import com.mgmtp.a12.kernel.core.codegen.internal.validation.noMetaModelChecks.NoMetaModelChecks;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.util.AnalyseService;
import com.mgmtp.a12.kernel.core.service.internal.mandatoryinformation.PflichtAngaben;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationDataTable;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.Product;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.EnumerationDataTable;

/**
 * The code generator creating the validation code.
 */
public class ValidationCodeGenerator extends ModelBasedCodeGenerator {
	private static final String IMPORT = "import";
	private static final String PRELIMINARY = "preliminary";
	private static final String ERROR = "error";
	private static final String WARNING = "warning";
	private static final String INFO = "info";

	private static final String ERVALIDATOR_PREFIX = "ERValidator_";
	private static final String RULE_PREFIX = "R_";
	private static final String CALC_PREFIX = "C_";
	private static final String TABLE_PREFIX = "T_";
	private static final String DYN_ENUM_PREFIX = "E_";
	private static final String META_PREFIX = "Meta_";
	private static final String META_RULE_PREFIX = META_PREFIX + "Regel_";
	private static final String META_FIELDS_PREFIX = META_PREFIX + "Felder_";
	private static final String META_FIELD_FORMATS_PREFIX = META_PREFIX + "FeldFormate_";
	private static final String META_MANDATORYFIELDS_PREFIX = META_PREFIX + "PfFeld_";

	private static final String CSV_SUFFIX = ".csv";

	private static final String FACTORYCLASS_PREFIX = "Factory_";

	/**
	 * The root of the backing bean tree.
	 * <p>
	 * The roots of the template forest usually use this class as base.
	 */
	private CodeGenModel model;
	private CodeGenRules rules;
	private CodeGenCalc calc;
	private CodeGenDynamicEnumerations dynEnums;
	private CodeGenTableHolder tableHolder;
	private CodeGenMetaModel metaModel;
	private CodeGenMetaFields metaFields;
	private CodeGenMetaPflichtfeldInfo metaPflichtfeldInfo;
	private CodeGenMetaRules metaRules;
	private boolean isPublicAccessorNeeded;

	public ValidationCodeGenerator(CodeGenData params, IProblemReporter pm, SourceCodeCollectorV2 collector) {
		this(params, pm, collector, false);
	}

	public ValidationCodeGenerator(CodeGenData params, IProblemReporter pm, SourceCodeCollectorV2 collector,
			boolean isPublicAccessorNeeded) {
		super(params, pm, collector);
		this.isPublicAccessorNeeded = isPublicAccessorNeeded;
	}

	@Override
	protected boolean preCheck() {
		Map<String, ? extends IRule> ruleMap = data.getRules().stream()
				.collect(Collectors.toMap(IRule::getFullName, Function.identity()));
		ValidationCodeGenDataChecker dataChecker = new ValidationCodeGenDataChecker(problemReporter, data.getEc(),
				data.getModelSpecificSettings(), ruleMap);
		VKCodeGenValidationDataCollector dataCollector = new VKCodeGenValidationDataCollector(dataChecker, data.getEc(),
				data.getModelSpecificSettings(), data.getRules(), data.getParams());
		dataCollector.fillData();

		NoMetaModelChecks nmmc = new NoMetaModelChecks(data, problemReporter);
		// Execute both checks
		boolean noMetaModelChecks = nmmc.check();
		boolean dataChecks = dataChecker.check();

		return prepareRules() && noMetaModelChecks && dataChecks;
	}

	protected boolean prepareRules() {
		data.removeRules(this::handleLengthOfAllowedValueValidationRule, problemReporter);

		return !problemReporter.hasProblemOccurred();
	}

	/**
	 * Checks whether the given rule has the special form to be replaced with a meta API change and
	 * prepares the API change in that case.
	 *
	 * @param ruleInfo
	 * 		the rule to check.
	 * @return <code>true</code> if the rule should be eliminated, <code>false</code> otherwise.
	 */
	private boolean handleLengthOfAllowedValueValidationRule(RuleInfo ruleInfo) {
		var result = AnalyseService.getNameAndLengthOfAllowedValueValidation(ruleInfo.getCondition(), data.getEc());
		result.ifPresent(p -> addMetaDataLengthRestraint(p.fieldFullName(), p.length()));
		return result.isPresent();
	}

	private void addMetaDataLengthRestraint(String fieldFullName, Integer value) {
		data.addMaxLengthDefinedByRules(fieldFullName, value);
	}

	@Override
	protected boolean createTemplateParameterClasses() {
		/*
		 * This early initialization is necessary because
		 * CodeGenCreator#getParameterCode(IFieldIdOperationWithParameters) already uses a template
		 * at construction time.
		 *
		 * Otherwise, it seemed to be preferable to initialize this context not until the execution
		 * of {@link ValidationCodeGenerator#createEntries()}.
		 */
		CodeGenContext.initialize(Product.VALIDATION, data.getParams().getProgrammingLanguage());

		ProgrammingLanguage programmingLanguage = data.getParams().getProgrammingLanguage();
		boolean isCpp = ProgrammingLanguage.CPLUS.equals(data.getParams().getProgrammingLanguage());
		boolean isJava = ProgrammingLanguage.JAVA.equals(data.getParams().getProgrammingLanguage());
		boolean isJSReduced = ProgrammingLanguage.JAVA_SCRIPT_REDUCED.equals(data.getParams().getProgrammingLanguage());
		boolean isJSVK = ProgrammingLanguage.JAVA_SCRIPT_VK.equals(data.getParams().getProgrammingLanguage());
		boolean isGroovy = ProgrammingLanguage.GROOVY.equals(data.getParams().getProgrammingLanguage());

		if (!isJSReduced) {
			CodeGenModelBuilder modelBuilder = new CodeGenModelBuilder(data, problemReporter, getPackageInfo());
			model = modelBuilder.build();
		}

		// Currently, we only do not expand for Java, Groovy, and JSVK in case of A12-models
		boolean expandGroups = switch (programmingLanguage) {
			case JAVA, GROOVY, JAVA_SCRIPT_VK, CPLUS -> ExtmpUtils.isHModel(data.getModelSpecificSettings());
			case JAVA_SCRIPT_REDUCED, VBA -> true;
		};

		if (isJava || isJSVK || isGroovy) {
			calc = new CodeGenCalcBuilder(data, problemReporter, getPackageInfo(), expandGroups).build();
			dynEnums = new CodeGenDynamicEnumerationsBuilder(data, problemReporter, getPackageInfo(), expandGroups).build();
		}

		CodeGenRulesBuilder rulesBuilder = new CodeGenRulesBuilder(data, problemReporter, getPackageInfo(), expandGroups);
		// error parsing rules
		if (problemReporter.hasProblemOccurred()) {
			return false;
		}
		rules = rulesBuilder.build();

		boolean allePflichtAngaben = !CodeGenUtilities.isReducedStandardCodeProfile(data);
		PflichtAngaben pflichtAngaben = rulesBuilder.getMandatoryInformation().ermittlePflichtAngaben(allePflichtAngaben);

		CodeGenMetaFieldsBuilder fieldsBuilder = new CodeGenMetaFieldsBuilder(data, pflichtAngaben, problemReporter,
				getPackageInfo());
		metaFields = fieldsBuilder.build();

		if (isJava || isCpp || isJSVK || isGroovy) {
			CodeGenMetaPflichtfeldInfoBuilder metaPflichtfeldBuilder = new CodeGenMetaPflichtfeldInfoBuilder(data, pflichtAngaben,
					problemReporter, getPackageInfo());
			metaPflichtfeldInfo = metaPflichtfeldBuilder.build();
		}

		if (!isJSReduced) {
			List<MetaRegelFehlertexte> filteredRuleErrorTexts = rulesBuilder.getRegelFehlertexte().stream()
					.filter(r -> !r.getRule().isPreliminary())
					.collect(Collectors.toList());
			List<RuleInfo> filteredRules = rulesBuilder.getRules().stream().filter(r -> !r.isPreliminary())
					.collect(Collectors.toList());
			CodeGenMetaRulesBuilder metaRulesBuilder = new CodeGenMetaRulesBuilder(data, problemReporter, getPackageInfo(),
					filteredRules, filteredRuleErrorTexts, fieldsBuilder.getFieldIndices());
			metaRules = metaRulesBuilder.build();

			metaModel = new CodeGenMetaModelBuilder(data, filteredRules, problemReporter, getPackageInfo()).build();
		}

		if (isJava || isCpp) {
			if (data.getEc().getEnumerationTables() == null) {
				throw new IllegalArgumentException(
						"Wrong IEC-implementation for code generation. Tables not implemented: " + data.getEc().getClass()
								.getCanonicalName());
			}
			tableHolder = new CodeGenTableHolderBuilder(data.getEc().getEnumerationTables(), data, problemReporter,
					getPackageInfo()).build();
		}
		return !problemReporter.hasProblemOccurred();
	}

	@Override
	protected void createEntries() {
		CodeGenContext.initialize(Product.VALIDATION, data.getParams().getProgrammingLanguage());
		switch (data.getParams().getProgrammingLanguage()) {
			case JAVA -> createJavaEntries();
			case CPLUS -> createCppEntries();
			case VBA -> createVBAEntries();
			case JAVA_SCRIPT_REDUCED -> createJavaScriptEntries();
			case JAVA_SCRIPT_VK -> createJavaScriptMvkEntries();
			case GROOVY -> createGroovyEntries();
			default ->
					throw new IllegalArgumentException("Unknown programming language " + data.getParams().getProgrammingLanguage());
		}
	}

	private void createJavaScriptEntries() {
		String fileName = data.getParams().getName() + "/" + data.getParams().getName() + CodeGenConstants.JAVASCRIPT_SUFFIX;
		String jsFileContent = metaFields.toCode();

		createEntry(fileName, jsFileContent);
	}

	private void createVBAEntries() {
		BiConsumer<String, String> createVBAEntry = (name, text) -> createEntry(
				data.getParams().getName() + "/" + relativePath + "/" + name + CodeGenConstants.VBA_SUFFIX,
				VBALineNormalizer.normalizeLineLength(text));

		createVBAEntry.accept("ERG_Validator", model.toCode("er"));
		createVBAEntry.accept("ERG_Meta", metaModel.toCode());
		createVBAEntry.accept("ERG_MetaFelder", metaFields.toCode());
		createVBAEntry.accept("ERG_Validierungsdaten", model.toCode("Validierungsdaten"));
		createVBAEntry.accept("ERG_Validierungsergebnis", model.toCode("Validierungsergebnis"));
		createVBAEntry.accept("ERG_Regeln", rules.toCode());
		createVBAEntry.accept("ERG_MetaRegel", metaRules.toCode());
	}

	private void createJavaEntries() {
		if (ExtmpUtils.isMapper(data.getParams())) {
			createJavaEntries4MapperOnly();
		} else {
			createJavaEntries4FullSize();
		}
	}

	private void createJavaEntries4FullSize() {

		createJavaEntry(ERVALIDATOR_PREFIX, model.toCode("er"));
		createJavaEntry(RULE_PREFIX, rules.toCode());
		createJavaEntry(CALC_PREFIX, calc.toCode());
		addValidationAndCalcEntryPointMarkers();
		createJavaEntry(DYN_ENUM_PREFIX, dynEnums.toCode());
		if (!data.getEc().getEnumerationTables().isEmpty()) {
			createJavaEntry(TABLE_PREFIX, tableHolder.toCode());
		}

		//Start Meta
		createJavaEntry(META_PREFIX, metaModel.toCode());
		createJavaEntry(META_RULE_PREFIX, metaRules.toCode());
		createJavaEntry(META_FIELDS_PREFIX, metaFields.toCode());

		createJavaEntry(META_MANDATORYFIELDS_PREFIX, metaPflichtfeldInfo.toCode());

		createJavaAccessorFactories();

		CodeGenTeilvalidierungBuilder builder = new CodeGenTeilvalidierungBuilder(data, problemReporter, getPackageInfo(),
				rules);
		CodeGenTeilValidierung codeGenTeilvalidierung = builder.build();
		createJavaEntry("TVCheckAlle_", codeGenTeilvalidierung.toCode("all"));
		createJavaEntry("TVCheckInfo_", codeGenTeilvalidierung.toCode(INFO));
		createJavaEntry("TVCheckHinweis_", codeGenTeilvalidierung.toCode(WARNING));
		createJavaEntry("TVCheckFehler_", codeGenTeilvalidierung.toCode(ERROR));
		createJavaEntry("TVCheckPreliminary_", codeGenTeilvalidierung.toCode(PRELIMINARY));

		data.getEc().getEnumerationTables().forEach((tn, t) -> createEntry(
				getPrefix(getPackageInfo().getVKPackageName()) + tn + CSV_SUFFIX,
				new String(t.getBytes(), StandardCharsets.UTF_8)));
	}

	private void createJavaAccessorFactories() {
		// create the internal factory even if there'll also be a public factory, because the public factory delegates to the
		// internal one
		createJavaEntry(FACTORYCLASS_PREFIX, model.toCode("factory"));

		if (isPublicAccessorNeeded) {
			// Only needed in public API package for the main model in Java
			createJavaEntry(FACTORYCLASS_PREFIX, model.toCode("Public_factory"), model.getPublicPackageName());
		}

		if (areLegacyPublicExtmpAccessorsNeeded(isPublicAccessorNeeded) // only if public accessors are not disabled altogether
		) {
			//For ExtMP additionally create the factory class for the Main-Model in a public API package.
			createJavaEntry(FACTORYCLASS_PREFIX, model.toCode("erfactory"),
					model.getLegacyPublicExtmpPackageName().replace('.', '/'));
		}
	}

	private void addValidationAndCalcEntryPointMarkers() {
		List<String> validatorFqn = Arrays.asList(getFullNameWithoutExtension(ERVALIDATOR_PREFIX).split("/"));
		List<String> metaFqn = Arrays.asList(getFullNameWithoutExtension(META_PREFIX).split("/"));
		List<String> internalFactoryFqn = Arrays.asList(getFullNameWithoutExtension(FACTORYCLASS_PREFIX).split("/"));
		this.collector.addEntryPoints(new ValidationEntryPoint(validatorFqn, metaFqn, internalFactoryFqn));
	}

	private void createJavaEntries4MapperOnly() {
		createJavaEntry(META_PREFIX, metaModel.toCode());
		createJavaEntry(META_FIELDS_PREFIX, metaFields.toCode());

		createJavaAccessorFactories();

		if (!data.getEc().getEnumerationTables().isEmpty()) {
			createJavaEntry(TABLE_PREFIX, tableHolder.toCode());
		}
		data.getEc().getEnumerationTables().forEach((tn, t) -> createEntry(
				getPrefix(getPackageInfo().getVKPackageName()) + tn + CSV_SUFFIX,
				createHeaderTable(t)));
	}

	private String createHeaderTable(IEnumerationDataTable t) {
		return StringUtils.join(t.getColumns().keySet(), EnumerationDataTable.SEPARATOR);
	}

	private void createCppEntries() {
		createCppAndHEntry(ERVALIDATOR_PREFIX, model.toCode("er"), model.toCode("er_h"));

		createCppAndHEntry(RULE_PREFIX, rules.toCode(), rules.toCode("h"));
		for (int i = 1; i <= rules.getNumRuleFilesCpp(); i++) {
			createCppAndHIterativeEntry(RULE_PREFIX, rules.toCode("ruleFile"), null, i);
		}

		createCppAndHEntry(META_PREFIX, metaModel.toCode(), metaModel.toCode("h"));

		createCppAndHEntry(META_FIELD_FORMATS_PREFIX, metaFields.toCode("formate"), metaFields.toCode("formate_h"));

		for (int i = 1; i <= metaFields.getNumMetaFieldFilesCpp(); i++) {
			metaFields.setMetaFieldFileNumberCpp(i);
			createCppAndHIterativeEntry(META_FIELDS_PREFIX, metaFields.toCode(), metaFields.toCode("h"), i);
		}

		if (!data.getEc().getEnumerationTables().isEmpty()) {
			createCppAndHEntry(TABLE_PREFIX, tableHolder.toCode(), tableHolder.toCode("h"));
			for (int i = 1; i <= tableHolder.getNumTableFilesCpp(); i++) {
				createCppAndHIterativeEntry(TABLE_PREFIX, tableHolder.toCode("tableFile"), null, i);
			}
		}
	}

	private void createJavaScriptMvkEntries() {
		StringBuilder importSetBuilder = new StringBuilder();
		StringBuilder importBuilder = new StringBuilder();
		StringBuilder entryBuilder = createEntryBuilder4JsVkGroovy();

		importBuilder.append(metaFields.toCode(IMPORT))
				.append(metaRules.toCode(IMPORT))
				.append(metaPflichtfeldInfo.toCode(IMPORT))
				.append(metaModel.toCode(IMPORT))
				.append(calc.toCode(IMPORT))
				.append(rules.toCode(IMPORT))
				.append(model.toCode(IMPORT));

		// every import statement only once
		importBuilder.toString().lines()
				.distinct()
				.forEach(line -> importSetBuilder.append(line + System.lineSeparator()));

		String generatedCodeBody = importSetBuilder.toString() + entryBuilder.toString();
		String genFileInfo = metaFields.toCode("generatedFileInfo");
		switch (data.getParams().getJsVkCodeGenConfig()) {
			case STANDARD:
				createJavaScriptMvkEntry("", genFileInfo + generatedCodeBody, CodeGenConstants.JAVASCRIPT_SUFFIX);
				break;
			case WRAPPED_IN_FUNCTION:
				createJavaScriptMvkEntry("",
						genFileInfo + new CodeGenModelWrapper(metaModel.getRawName(), generatedCodeBody).toCode(),
						CodeGenConstants.JAVASCRIPT_SUFFIX);
				break;
			case WRAPPED_IN_FUNCTION_EXTENDED:
				createJavaScriptMvkEntry("",
						genFileInfo + new CodeGenModelWrapper(metaModel.getRawName(), generatedCodeBody).toCode(),
						CodeGenConstants.JAVASCRIPT_SUFFIX);
				createJavaScriptMvkEntry("", genFileInfo + new CodeGenTypescriptDefCode(metaModel.getName()).toCode(),
						CodeGenConstants.TS_DEF_CODE_SUFFIX);
				break;
			default:
				throw new IllegalArgumentException(
						"Invalid cofig for the javascript code generation: " + data.getParams().getJsVkCodeGenConfig().name());
		}
	}

	private void createGroovyEntries() {

		StringBuilder importBuilder = new StringBuilder();
		StringBuilder entryBuilder = createEntryBuilder4JsVkGroovy();

		// for groovy all imports are centralized in one file
		importBuilder.append(metaFields.toCode("imports"));

		createGroovyContentEntry("", entryBuilder.toString());
		createGroovyHelperEntry(GROOVY_FILE_INFO_KEY, metaFields.toCode("generatedFileInfo"));
		createGroovyHelperEntry(GROOVY_IMPORT_KEY, CodeGenResultUtils.deduplicateImports(importBuilder.toString()));
		createGroovyHelperEntry(GROOVY_GLOBALS_KEY, metaModel.toCode("factory"));
	}

	private StringBuilder createEntryBuilder4JsVkGroovy() {
		StringBuilder entryBuilder = new StringBuilder();

		entryBuilder
				.append(metaFields.toCode());
		for (int i = 1; i <= metaRules.getNumMetaRuleFiles(); i++) {
			metaRules.setMetaRuleFileNumber(i);
			entryBuilder.append(metaRules.toCode());
		}
		entryBuilder
				.append(metaPflichtfeldInfo.toCode())
				.append(metaModel.toCode())
				.append(dynEnums.toCode())
				.append(calc.toCode())
				.append(rules.toCode());
		// We add the general validation and calc entry points at least for groovy to be able to combine them like in java
		// In groovy, we need the fqn as well, since the generated classes contain the complete fqn in some way
		// It is not clear yet, what we will need in JS, but I guess it has a sufficient similarity to groovy to already add it the same way.
		addValidationAndCalcEntryPointMarkers();
		CodeGenTeilvalidierungBuilder builder = new CodeGenTeilvalidierungBuilder(data, problemReporter, getPackageInfo(),
				rules);
		CodeGenTeilValidierung codeGenTeilvalidierung = builder.build();
		entryBuilder.append(codeGenTeilvalidierung.toCode(ERROR));
		entryBuilder.append(codeGenTeilvalidierung.toCode(WARNING));
		entryBuilder.append(codeGenTeilvalidierung.toCode(INFO));
		entryBuilder.append(codeGenTeilvalidierung.toCode("all"));
		entryBuilder.append(codeGenTeilvalidierung.toCode(PRELIMINARY));

		entryBuilder.append(model.toCode("er"));

		return entryBuilder;
	}

	public record ValidationEntryPoint(
			List<String> validatorFqn,
			List<String> metaFqn,
			List<String> internalFactoryFqn
	) implements IEntryPoints {
		public List<String> calculatorFqn() {
			return validatorFqn;
		}
	}
}
