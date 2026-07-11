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

package com.mgmtp.a12.kernel.md.document.typed_accessor_gen;

import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.appendList;
import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.concatToList;
import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.getAllButLast;
import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.getLast;
import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.isPrefix;
import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.setIntersection;
import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.setUnion;
import static com.mgmtp.a12.kernel.core.base.utils.internal.PackageNameUtil.packageOrFqnAsList;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;
import org.stringtemplate.v4.ST;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.md.document.typed_accessor_gen.internal.util.DMUtilForTypingsGen;
import com.mgmtp.a12.kernel.md.document.typed_accessor_gen.internal.util.ModelRef4Include;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IFieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IBooleanType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IConfirmType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.ICustomFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IEnumerationType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.INumberType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IStringType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.ITypeDefType;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.model.utils.OnlyForUsage;

@OnlyForUsage
public class TypedAccessorGenerator {

	private static final Pattern startsWithAZ = Pattern.compile("^[a-zA-Z].*");

	public static void main(String[] args) {
		// NOTE: maybe move this to a separate module so that the rest of the generator doesn't depend on the serializer?

		Options helpOnlyOptions = new Options();
		helpOnlyOptions.addOption("h", "help", false, "Display help information");

		Options options = new Options();
		options.addOption(Option.builder("m")
				.longOpt("document-model-file")
				.hasArg()
				.required()
				.desc("The path to the document model json for which to generate typed accessors")
				.get());
		options.addOption(Option.builder("o")
				.longOpt("output-dir")
				.hasArg()
				.required()
				.desc("The path to the sources root directory into which the generated typed accessor classes should be generated")
				.get());
		options.addOption(Option.builder("p")
				.longOpt("package-prefix")
				.hasArg()
				.required()
				.desc("the package prefix in format \"com.example.my.package.prefix\"")
				.get());
		options.addOption(Option.builder("s")
				.longOpt("only-generate-for-subgroups")
				.hasArgs()
				.desc("(optional) zero or more paths to groups or fields in the dm, if only a subset of the dm should be included in the " +
						"generated typings (if any paths are given, all groups and fields that are not on or below one of the " +
						"paths will be omitted)")
				.get());
		options.addOption(Option.builder("i")
				.longOpt("additional-models")
				.hasArgs()
				.desc("(optional) paths to additional document model files for which typed accessors should be generated. " +
						"If the primary document model has includes, specify all (transitively) included DMs here, e.g. --additional-models ./Include1.json ./Include2.json." +
						"It is currently not possible to specify a subset-of-paths filter for additional models.")
				.get());
		options.addOption(Option.builder("e")
				.longOpt("external-models")
				.hasArgs()
				.desc("For the given DMs (that are referenced in includes), assume that typings are generated externally " +
						"and will be available at the specified package path at compile time and runtime " +
						"(rather than generate them locally via additional-models). " +
						"Intended for the use-case where a DM is included in multiple " +
						"different DMs that are separately versioned and released, so that the included DM must also be " +
						"versioned and released separately." +
						"One or more such external DMs can be specified in the format " +
						"'<dmId>=package.prefix[:singleRootGroupIfSingleRgInclude]'. " +
						"Note that for SINGLE_RG includes, the suffix with the name of the unique root group of the included " +
						"DM is required, as shown in the format. " +
						"Example: '--external-models CT_MODEL_HEADER_1=com.mgmtp.a12.kernel.mmtypings.ct_model_header_1:header'")
				.get());
		options.addOption(Option.builder()
				.longOpt("typedef-models")
				.hasArgs()
				.desc("(optional) paths to additional document model files that are used only to resolve type definitions " +
						"(e.g. typedef models).")
				.get());
		options.addOption(Option.builder()
				.longOpt("use-shared-typedef-classes")
				.desc("(deprecated, ignored) Generating one shared value-class (e.g. enum class) per typedef id under " +
						"'<prefix>/typedefs/<typedef-id>/' is now always done. This flag is still accepted for backwards " +
						"compatibility but has no effect.")
				.get());
		options.addOption(Option.builder()
				.longOpt("mode")
				.hasArg()
				.desc("(deprecated, ignored) Previously selected the generation mode. Only 'overlay' is accepted. The pojo mode has been removed.")
				.get());
		options.addOption(Option.builder()
				.longOpt("annotations-fqns")
				.hasArgs()
				.desc("(optional) one or more fully qualified names of annotations that should be added to each generated class (view, pointer, enum), e.g. 'com.mgmtp.a12.model.utils.Experimental'")
				.get());
		CommandLineParser parser = new DefaultParser();
		try { // first try to check if --help was passed and not complain about mandatory options in that case
			CommandLine helpCmd = parser.parse(helpOnlyOptions, args);
			if (helpCmd.hasOption("help")) {
				printCLIHelpAnd(options);
				System.exit(0);
			}
		} catch (ParseException e) {
			// do nothing
		}

		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("unrecognized command line arguments: " + e.getMessage());
			printCLIHelpAnd(options);
			System.exit(1);
		}
		String packagePrefix = cmd.getOptionValue("package-prefix");
		Path outputSourceRootPath = Path.of(cmd.getOptionValue("output-dir"));
		String primaryDmPath = cmd.getOptionValue(
				"document-model-file"); // this dm is spcial insofar as "only-generate-for-subgroups" only applies to this dm
		List<List<String>> limitToPathsSubset = parsePathsArgs(cmd.getOptionValues("only-generate-for-subgroups"));
		String modeValue = cmd.getOptionValue("mode");
		if (modeValue != null && !"overlay".equals(modeValue)) {
			System.err.println(
					"unsupported mode: '" + modeValue + "'. Only 'overlay' is supported. The pojo mode has been removed.");
			System.exit(1);
		}

		IDocumentModel primaryDm = DMUtilForTypingsGen.readDmFromFileOrClasspathResource(primaryDmPath);
		Set<IDocumentModel> allDms = Stream.concat(
				Stream.of(primaryDm),
				getCLIOptionValues(cmd, "additional-models").stream()
						.map(DMUtilForTypingsGen::readDmFromFileOrClasspathResource)
		).collect(Collectors.toSet());
		Map<String, Collection<List<String>>> selectedSubgroupsPerDm = new HashMap<>();
		if (limitToPathsSubset != null) { // currently the cli only supports filtering for the primary dm
			selectedSubgroupsPerDm.put(primaryDm.getHeader().getId(), limitToPathsSubset);
		}

		Set<ModelRef4Include> externalModels = getCLIOptionValues(cmd, "external-models").stream()
				.map(arg -> parseExternalModelRef(arg))
				.collect(Collectors.toSet());

		List<IDocumentModel> typedefOnlyModels = getCLIOptionValues(cmd, "typedef-models").stream()
				.map(DMUtilForTypingsGen::readDmFromFileOrClasspathResource)
				.toList();

		if (cmd.hasOption("use-shared-typedef-classes")) {
			System.err.println("warning: --use-shared-typedef-classes is no longer relevant and is ignored: shared "
					+ "value-classes for typedefs are now always generated.");
		}

		generateAccessorClassesToFileSystem(allDms, externalModels, typedefOnlyModels, outputSourceRootPath, packagePrefix,
				selectedSubgroupsPerDm, getCLIOptionValues(cmd, "annotations-fqns"));
	}

	private static void printCLIHelpAnd(Options options) {
		HelpFormatter formatter = HelpFormatter.builder()
				.setShowSince(false)
				.get();

		try {
			formatter.printHelp(
					"java -jar kernel-md-facade-*-typed-accessor-gen-cli.jar OPTIONS",
					null,
					options,
					null,
					false
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void generateAccessorClassesToFileSystem(
			Collection<IDocumentModel> dms,
			Set<ModelRef4Include> externalModels,
			Collection<IDocumentModel> typedefOnlyModels,
			Path sourcesRoot,
			String packagePrefix,
			Map<String, Collection<List<String>>> limitToPathsSubsetPerDm,
			List<String> classAnnotationFqns
	) {
		List<String> packagePrefixAsList = packageOrFqnAsList(packagePrefix);
		var localModelsRefs = DMUtilForTypingsGen.makeLocalModelsRefs(dms, packagePrefixAsList);
		validateNoIntersection(externalModels, localModelsRefs, ModelRef4Include::dmId,
				"The same DM id must not occur in _both_ local and external models: duplicate dmIds: ");
		Set<ModelRef4Include> allModelRefsForIncludes = setUnion(externalModels, localModelsRefs);

		Map<List<String>, String> classesCollector = new HashMap<>();
		// Index every typedef (local, included or imported) by its globally unique id. Typedef-typed fields are then
		// typed purely via this map - by the field's ITypeDefType id - so we no longer need to resolve/expand the dm to
		// give such fields an effective type (which is why this module no longer depends on combination-model).
		Map<String, GroupClass.FieldTypeSpecifics> typedefSpecificsById = buildTypedefSpecifics(
				concatToList(dms, typedefOnlyModels), packagePrefixAsList, classAnnotationFqns, classesCollector);
		for (var dm : dms) {
			var pathSubset = limitToPathsSubsetPerDm.getOrDefault(dm.getHeader().getId(), null);
			if (pathSubset != null) {
				dm = DMUtilForTypingsGen.filterDm(dm, path -> isPathOnOrBelowAny(path, pathSubset));
			}
			var classesForCurrentDm = generateAccessorClasses(dm, packagePrefixAsList, allModelRefsForIncludes,
					classAnnotationFqns, typedefSpecificsById);
			mergeGeneratedClasses(classesCollector, classesForCurrentDm);
		}
		for (Map.Entry<List<String>, String> e : classesCollector.entrySet()) {
			Path filePath = sourcesRoot.resolve(String.join("/", e.getKey()) + ".java");
			var _r = filePath.getParent().toFile().mkdirs();
			try (PrintWriter out = new PrintWriter(filePath.toString(), StandardCharsets.UTF_8)) {
				out.println(e.getValue());
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * Merges generated classes into {@code into}. With the current design the same fully-qualified name is no longer
	 * expected to be produced twice in normal operation: each typedef's shared value-class is generated exactly once up
	 * front (deduped by typedef id in {@link #buildTypedefSpecifics}), and per-dm view / pointer / inline classes are
	 * namespaced by their dm. The guard therefore only protects against a genuine fully-qualified-name clash - e.g. two
	 * distinct typedef ids that escape to the same package and name (see {@link #typedefIdAsPackagePart}): a clash with
	 * <em>differing</em> content fails, while byte-identical content is tolerated.
	 */
	private static void mergeGeneratedClasses(Map<List<String>, String> into, Map<List<String>, String> additional) {
		additional.forEach((fqn, source) -> {
			String existing = into.putIfAbsent(fqn, source);
			assert existing == null : "not currently expected to get more than one entry (even if equal-value) for the same fqn";
			if (existing != null && !existing.equals(source)) {
				throw new IllegalArgumentException("colliding class name with differing content: " + String.join(".", fqn));
			}
		});
	}

	/**
	 * Indexes the {@link GroupClass.FieldTypeSpecifics} of every typedef in the union of {@code allModels} by the
	 * typedef's (globally unique) id; duplicate ids are assumed to denote the same typedef, so the first occurrence wins.
	 * <p>
	 * A typedef that warrants its own generated class (an enum typedef, or a string typedef with a hint list) has that
	 * single shared class generated up front here and added to {@code classesOut}; it lives under
	 * {@code <prefix>/typedefs/<typedef-id>/} and is named after the typedef, so every field referencing the typedef -
	 * in any model generated together - shares one class instead of getting a per-field duplicate. All other typedefs
	 * map to a plain runtime type (e.g. {@code BigDecimal}).
	 * <p>
	 * Fields then obtain their type purely by looking their {@link ITypeDefType} id up in this map (see
	 * {@link #resolveFieldTypeSpecificsAndGenerateInlineClass}), so the typedef need not be locally defined in (nor
	 * resolved on) the referencing dm - it only has to be provided via one of the models passed here.
	 */
	private static Map<String, GroupClass.FieldTypeSpecifics> buildTypedefSpecifics(
			Collection<IDocumentModel> allModels,
			List<String> packagePrefix,
			List<String> classAnnotationFqns,
			Map<List<String>, String> classesOut
	) {
		Map<String, GroupClass.FieldTypeSpecifics> result = new HashMap<>();
		for (IDocumentModel model : allModels) {
			for (IFieldTypeDefinition td : model.getContent().getTypeDefinitions()) {
				if (result.containsKey(td.getId())) {
					continue;
				}
				EnumOrHintlistClassLocation sharedLocation =
						new EnumOrHintlistClassLocation(mkPackage4Typedef(packagePrefix, td.getId()), td.getName());
				result.put(td.getId(),
						specificsForFieldType(td.getFieldType(), sharedLocation, classAnnotationFqns, classesOut));
			}
		}
		return result;
	}

	/**
	 * Builds the {@link GroupClass.FieldTypeSpecifics} for {@code type}, generating its companion class into
	 * {@code classesOut} if the type warrants one:
	 * <ul>
	 *     <li>an enum type yields an enum class at {@code location}; that class becomes the field's runtime type;</li>
	 *     <li>a string type with a hint list yields a constants class at {@code location}; the runtime type stays
	 *         {@code String} and the class is only referenced from the field's javadoc;</li>
	 *     <li>any other type maps to a plain runtime type and generates nothing (see {@link GroupClass#fieldTypeSpecificsNonEnum}).</li>
	 * </ul>
	 * The same logic serves both shared typedef classes (with {@code location} under {@code typedefs/<id>/}) and
	 * per-field inline classes (with {@code location} in the field's view package).
	 */
	private static GroupClass.FieldTypeSpecifics specificsForFieldType(IFieldType type, EnumOrHintlistClassLocation location,
			List<String> classAnnotationFqns, Map<List<String>, String> classesOut) {
		if (type instanceof IEnumerationType etype) {
			GenResult enumResult = generateEnumClass(location, etype, classAnnotationFqns);
			mergeGeneratedClasses(classesOut, enumResult.subtreeSources());
			return new GroupClass.FieldTypeSpecifics(enumResult.viewClassFqn(), null, GroupClass.FieldTypeCase.ENUM, null);
		}
		if (type instanceof IStringType stype && !stype.getHintList().isEmpty()) {
			GenResult hintListResult = generateHintListConstantsClass(location, stype, classAnnotationFqns);
			mergeGeneratedClasses(classesOut, hintListResult.subtreeSources());
			// the hint-list value itself is a plain String; the generated constants class is not reflected in the field
			// type, so we only mention it in the field's javadoc, for discoverability.
			return new GroupClass.FieldTypeSpecifics(GroupClass.parseFqn("java.lang.String"), null,
					GroupClass.FieldTypeCase.OTHER, buildHintListJavadocExtra(hintListResult.viewClassFqn()));
		}
		return GroupClass.fieldTypeSpecificsNonEnum(type);
	}

	private static Map<List<String>, String> generateAccessorClasses(
			IDocumentModel dm,
			List<String> packagePrefix,
			Set<ModelRef4Include> externalModelRefs,
			List<String> classAnnotationFqns,
			Map<String, GroupClass.FieldTypeSpecifics> typedefSpecificsById
	) {
		List<String> packageRest = List.of();
		String dmIdIfRoot = dm.getHeader().getId();
		return generateAccessorClassesInternal(
				dm.getContent().getDocumentModelRoot(),
				packagePrefix,
				packageRest,
				dmIdIfRoot,
				Java8Utils.associateBy(externalModelRefs, ModelRef4Include::dmId),
				classAnnotationFqns,
				typedefSpecificsById
		).subtreeSources();
	}

	private static GenResult generateAccessorClassesInternal(
			IGroup group,
			List<String> packagePrefix,
			List<String> packageRest,
			String dmIdIfRoot,
			Map<String, ModelRef4Include> modelRefsForIncludes,
			List<String> classAnnotationFqns,
			Map<String, GroupClass.FieldTypeSpecifics> typedefSpecificsById
	) {
		Map<List<String>, String> result = new HashMap<>();

		// normally the name is just group.name(), but for the (internal, hidden) documentModelRoot, we use the dm id instead
		String groupName = !StringUtils.isEmpty(dmIdIfRoot) ? dmIdIfRoot : group.getName();
		boolean isRoot = !StringUtils.isEmpty(dmIdIfRoot);
		FqnsForGroup names = FqnsForGroup.of(groupName, packagePrefix, packageRest);

		InfoForJavaDoc infoForJavaDoc = InfoForJavaDoc.of(group);
		ViewClass viewClass = new ViewClass(names.viewClassFqn, isRoot, dmIdIfRoot, names.pointerClassFqn, infoForJavaDoc,
				classAnnotationFqns);
		PointerClass pointerClass = new PointerClass(group, names.pointerClassFqn, names.viewClassFqn, isRoot, infoForJavaDoc,
				classAnnotationFqns);

		checkNoCaseInsensitiveElementNameCollisions(group);
		for (IElement elem : group.getElements()) {
			String elemName = elem.getName();
			if (elem instanceof IGroup subGroup) {
				GenResult subGroupResult;
				// an expanded model also has the modelReferences, but does not include the "reference" itself
				if (subGroup.getIncludeConfig().map(includeDetails -> includeDetails.reference())
						.filter(id -> !StringUtils.isEmpty(id))
						.isPresent()
				) {
					var includeLevel = subGroup.getIncludeConfig().get().includeLevel();
					if (includeLevel == IGroup.IIncludeConfig.IncludeLevel.MODEL_ROOT) {
						// in order to support MODEL_ROOT includes here, we need
						// - a variant of the view-class for documents that is a Group view class (AbstractTypedView<GroupInstanceV2, ...> instead of AbstractTypedView<DocumentV2, ...> ; important for _viewOf and _unwrap methods)
						throw new IllegalArgumentException("MODEL_ROOT includes are not supported by the typings generator yet");
					}
					String includedDmId = subGroup.getIncludeConfig().get().reference();
					// we assume that the included dm was separately passed to the generating method -- here, we just reference it
					subGroupResult = deriveEntryPointsFromIncludedDmTypingsRef(
							getOrThrow(modelRefsForIncludes, includedDmId, () -> new IllegalArgumentException("DM " + includedDmId
									+ " occurs as an included DM, but was not found among the local or external models. " +
									"Could not infer the FQN of the included group's typings.")),
							includeLevel);
					assert subGroupResult.subtreeSources().isEmpty();
				} else {
					subGroupResult = generateAccessorClassesInternal(subGroup, packagePrefix, names.subPackageRest, null,
							modelRefsForIncludes, classAnnotationFqns, typedefSpecificsById);
					result.putAll(subGroupResult.subtreeSources());
				}

				InfoForJavaDoc javaDocForSubgroup = InfoForJavaDoc.ofGroupWithKnownEffectiveRepeatability(subGroup);
				viewClass.addSubGroup(elemName, subGroupResult, subGroup, javaDocForSubgroup);
				pointerClass.addSubGroup(elemName, subGroupResult, subGroup, javaDocForSubgroup);
			} else if (elem instanceof IField fieldElem) {
				GroupClass.FieldTypeSpecifics fieldSpecs = resolveFieldTypeSpecificsAndGenerateInlineClass(fieldElem,
						packagePrefix, names.subPackageRest, typedefSpecificsById, classAnnotationFqns, result);
				InfoForJavaDoc javaDocForField = InfoForJavaDoc.of(fieldElem, fieldSpecs);
				viewClass.addField(elemName, fieldSpecs, javaDocForField);
				pointerClass.addField(elemName, fieldSpecs, javaDocForField);
			} else {
				// nothing for rules, computations, any other?
			}
		}

		result.put(names.viewClassFqn, viewClass.format());
		result.put(names.pointerClassFqn, pointerClass.format());
		return new GenResult(names.viewClassFqn, names.pointerClassFqn, result);
	}

	/**
	 * @param viewClassFqn
	 * @param pointerClassFqn
	 * @param subPackageRest
	 * 		the values for packageRest (see {@link #of(String, List, List)} that will be used for subgroups of this group.
	 */
	record FqnsForGroup(
			List<String> viewClassFqn,
			List<String> pointerClassFqn,
			List<String> subPackageRest
	) {
		static FqnsForGroup of(String groupName, List<String> packagePrefix, List<String> packageRest) {
			List<String> typedViewsPackage = mkPackage4View(packagePrefix, packageRest);
			List<String> typedPointersPackage = mkPackage4Pointer(packagePrefix, packageRest);
			List<String> viewClassFqn = appendList(typedViewsPackage, viewClassNameFor(groupName));
			List<String> pointerClassFqn = appendList(typedPointersPackage, pointerClassNameFor(getLast(viewClassFqn)));
			List<String> subPackageRest = appendList(packageRest, packageNamePartFor(getLast(viewClassFqn)));
			return new FqnsForGroup(viewClassFqn, pointerClassFqn, subPackageRest);
		}
	}

	private static GenResult deriveEntryPointsFromIncludedDmTypingsRef(
			ModelRef4Include ref,
			IGroup.IIncludeConfig.IncludeLevel includeLevel
	) {
		var packagePrefix = ref.packagePrefix();
		var includedDmId = ref.dmId();
		String innerRootGroupName = ref.singleRootGroupIfRgIncludeNullable();
		if (includeLevel == IGroup.IIncludeConfig.IncludeLevel.SINGLE_RG && innerRootGroupName == null) {
			throw new IllegalArgumentException(
					"DM " + includedDmId + " is used in a SINGLE_RG include but does not have a unique root group.");
		}

		FqnsForGroup namesForDmOfInclude = FqnsForGroup.of(includedDmId, packagePrefix, List.of());
		FqnsForGroup effectiveFdnsForGroup = switch (includeLevel) {
			case SINGLE_RG -> FqnsForGroup.of(innerRootGroupName, packagePrefix, namesForDmOfInclude.subPackageRest);
			case MODEL_ROOT ->
					namesForDmOfInclude; // Note: includeLevel MODEL_ROOT is not supported yet by TypedAccessorGenerator overall, so it is not tested yet
		};
		return new GenResult(effectiveFdnsForGroup.viewClassFqn, effectiveFdnsForGroup.pointerClassFqn, Map.of());
	}

	private static IFieldType getFieldType(IField field) {
		return field.getEffectiveType().orElse(field.getFieldType());
	}

	/**
	 * The package and base name for an enum or hint-list class generated for a field whose type warrants one (enum
	 * fields and string fields with hint-lists).
	 *
	 * @param packagePath
	 * 		the package the generated class lives in
	 * @param baseName
	 * 		the base name used to derive the generated class name
	 */
	private record EnumOrHintlistClassLocation(List<String> packagePath, String baseName) {
	}

	/**
	 * Resolves the {@link GroupClass.FieldTypeSpecifics} for a field and, for an inline enum / hint-list field, generates
	 * that field's companion class into {@code classesOut} as a side effect (hence the verb in the name).
	 * <p>
	 * A field whose type is a typedef is typed purely via the {@code typedefSpecificsById} map (looked up by the
	 * {@link ITypeDefType} id) - its enum / hint-list class, if any, was already generated once by
	 * {@link #buildTypedefSpecifics} at the shared {@code typedefs/<id>/} location, so nothing is generated here. A field
	 * with an <em>inline</em> (non-typedef) enum / hint-list type instead gets a per-field class generated here, in the
	 * group's view package and named after the field. All other types map via {@link GroupClass#fieldTypeSpecificsNonEnum}.
	 */
	private static GroupClass.FieldTypeSpecifics resolveFieldTypeSpecificsAndGenerateInlineClass(IField field,
			List<String> packagePrefix, List<String> subPackageRest,
			Map<String, GroupClass.FieldTypeSpecifics> typedefSpecificsById,
			List<String> classAnnotationFqns, Map<List<String>, String> classesOut) {
		if (field.getFieldType() instanceof ITypeDefType tdt) {
			GroupClass.FieldTypeSpecifics specs = typedefSpecificsById.get(tdt.getId());
			if (specs != null) {
				return specs;
			}
			// not in the map: the defining (imported / included) typedef model was not provided. Best-effort fall
			// through to the field's own (unresolved) type, which yields Object - matching the pre-refactoring behavior.
			// We still flag it, so it is noticed in CLI use (stderr) and fails fast where assertions are enabled (tests).
			String msg = "no typedef with id '" + tdt.getId() + "' was provided for field '" + field.getName()
					+ "'; its accessors fall back to Object. Pass the defining model via --typedef-models / --additional-models.";
			System.err.println("warning: " + msg);
			assert false : msg;
		}
		EnumOrHintlistClassLocation perFieldLocation =
				new EnumOrHintlistClassLocation(mkPackage4View(packagePrefix, subPackageRest), field.getName());
		return specificsForFieldType(getFieldType(field), perFieldLocation, classAnnotationFqns, classesOut);
	}

	private static void checkNoCaseInsensitiveElementNameCollisions(IGroup parentGroup) {
		Map<String, IElement> takenNames = new HashMap<>();
		for (IElement e : parentGroup.getElements()) {
			if (e instanceof IGroup || e instanceof IField) { // other element types (e.g. rules) currently don't matter for typed accessors
				String normalizedName = e.getName().toLowerCase();
				if (takenNames.containsKey(normalizedName)) {
					throw new IllegalArgumentException("Group " + ElementUtils.getPath(
							parentGroup) + " contains elements with colliding names: lowercased name " + normalizedName + " occurs for both " + takenNames.get(
							normalizedName) + " and " + e);
				} else {
					takenNames.put(normalizedName, e);
				}
			}
		}
	}

	private static String packageNamePartFor(String groupNameInDoc) {
		return "_" + groupNameInDoc.toLowerCase(); // Note: we must (probably) keep the "_" prefix, otherwise the name would / could
		// be identical except for case to the corresponding view class name, which would likely cause name collisions on
		// windows
	}

	private static String viewClassNameFor(String groupName) {
		groupName = escapeNameIfNecessary(groupName);
		return nameAsClassName(groupName);
	}

	private static String pointerClassNameFor(String viewClassName) {
		return nameAsClassName("P" + viewClassName);
	}

	private static String nameAsClassName(String name) {
		// uppercasing the first letter does not currently play a role in name collision avoidance (because windows file & folder
		// names are case insensitive), but it simply makes the names a bit more aligned with java naming conventions.
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	private static String childMethodNameFor(String subgroupName) {
		String methodName = escapeNameIfNecessary(subgroupName);
		// lowercase first letter is currently just in order to better align with java naming conventions and does _not_
		// have a role in name collisions avoidance
		return Character.toLowerCase(methodName.charAt(0)) + methodName.substring(1);
	}

	record GenResult(
			List<String> viewClassFqn,
			List<String> pointerClassFqn,
			Map<List<String>, String> subtreeSources
	) {
	}

	interface FormattableWithGlobalInfo<T> {
		String format(T globalInfo);
	}

	private interface Fqn2Ref {
		String fqn2Ref(List<String> fqn);
	}

	static class ImportCollisionResolver implements Fqn2Ref {
		private final Map<List<String>, String> fqnRefs;
		private final List<String> imports;

		ImportCollisionResolver(String selfClassName, Set<List<String>> referencedFqns) {
			this(selfClassName, referencedFqns, Set.of());
		}

		ImportCollisionResolver(String selfClassName, Set<List<String>> referencedFqns,
				Collection<String> additionalTakenNames) {
			Map<List<String>, String> fqnRefs = new HashMap<>();
			List<String> imports = new ArrayList<>();

			Set<String> takenNames = new HashSet<>(List.of(selfClassName));
			takenNames.addAll(additionalTakenNames);
			takenNames.addAll(referencedFqns.stream().map(fqn -> fqn.get(0)).toList()); // if we allow a class with e.g name
			// "com" to be referenced by shortname, then this would cause fqn-references to all com.* (e.g. com.mgmtp.*) classes
			// to break. This might never happen in practice because we now upper-case the first letter of generated class names and
			// package names are typically lowercase, but there might be exceptions.
			// TODO can we test this?

			for (var fqn : normalizeOrder(referencedFqns)) {
				var shortName = fqn.get(fqn.size() - 1);
				if (!takenNames.contains(shortName)) {
					imports.add(String.join(".", fqn));
					fqnRefs.put(fqn, shortName);
					takenNames.add(shortName);
				} else {
					fqnRefs.put(fqn, String.join(".", fqn));
					takenNames.add(fqn.get(0));
				}
			}
			this.fqnRefs = Collections.unmodifiableMap(fqnRefs); // NOTE: no true immutabilibty here because the fqn lists are
			this.imports = Collections.unmodifiableList(imports); // but we don't have a hard dependency on that here. We just want
			// to avoid accidental modifications after construction (they could break correctness of the collision avoidance)
		}

		/**
		 * Normalizes the order of the fqns, so that the import-vs-ref-by-fqn decision becomes deterministic and independent
		 * on the order of subgroups/fields in the document model.
		 */
		static private List<List<String>> normalizeOrder(Collection<List<String>> fqns) {
			return fqns.stream().sorted(Comparator.comparing(List::toString)).toList();
		}

		@Override public String fqn2Ref(List<String> fqn) {
			if (!fqnRefs.containsKey(fqn)) {
				throw new RuntimeException("fqn cannot be resolved to ref string: " + fqn);
			}
			return fqnRefs.get(fqn);
		}

		List<String> imports() {
			return imports;
		}
	}

	protected static final List<String> illegalArgumentExceptionFqn = GroupClass.parseFqn("java.lang.IllegalArgumentException");

	abstract static class GroupClass {
		protected static final List<String> abstractTypedViewFqn = parseFqn(
				"com.mgmtp.a12.kernel.md.document.internal.service.implV2.typedaccessors.AbstractTypedView");
		protected static final List<String> groupInstanceV2Fqn = parseFqn(
				"com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2");
		protected static final List<String> stringFqn = parseFqn("java.lang.String");
		protected static final List<String> objectFqn = parseFqn("java.lang.Object");
		protected static final List<String> listFqn = parseFqn("java.util.List");
		protected static final List<String> functionFqn = parseFqn("java.util.function.Function");
		protected static final List<String> documentPointerFqn = parseFqn("com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer");
		protected static final List<String> typedRepeatabilityPointerFqn = parseFqn(
				"com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedRepetitionsPointer");
		protected static final List<String> typedRepetitionsPointerImplFqn = parseFqn(
				"com.mgmtp.a12.kernel.md.document.internal.service.implV2.typedaccessors.TypedRepetitionsPointerImpl");
		protected final List<String> packagePath;
		protected final String selfClassName;
		protected List<FormattableWithGlobalInfo<Fqn2Ref>> subGroupAndFieldDeclarationFormattables = new ArrayList<>();
		protected Set<List<String>> referencedFqns = new HashSet<>();
		protected final InfoForJavaDoc infoForJavaDoc;
		protected final List<String> classAnnotationFqns;

		protected GroupClass(List<String> fqn, InfoForJavaDoc infoForJavaDoc, List<String> classAnnotationFqns) {
			this.selfClassName = getLast(fqn);
			this.packagePath = getAllButLast(fqn);
			this.infoForJavaDoc = infoForJavaDoc;
			this.classAnnotationFqns = classAnnotationFqns;
		}

		abstract String format();

		abstract void addSubGroup(String subGroupName, GenResult subGroupResult, IGroup subGroup, InfoForJavaDoc infoForJavaDoc);

		abstract void addField(String fieldName, FieldTypeSpecifics fieldSpecs, InfoForJavaDoc infoForJavaDoc);

		List<String> ownFqn() {
			return appendList(packagePath, selfClassName);
		}

		static List<String> parseFqn(String fqn) {
			return packageOrFqnAsList(fqn);
		}

		/**
		 * Everything the field-accessor rendering needs about a field's type, independent of whether the type came from a
		 * typedef or was inline.
		 *
		 * @param runtimeTypeFqn
		 * 		the field value's runtime type in the generated accessors. In the untyped {@code DocumentV2} a field value
		 * 		has static type {@code Object} but a more specific runtime type ({@code String}, {@code BigDecimal}, ...);
		 * 		this is that type, which the accessors cast/convert to. For an enum field it is the generated enum class.
		 * @param typeDescriptionForJavadoc
		 * 		an optional note about the runtime type to add to the field's javadoc, or {@code null}
		 * @param typeCase
		 * 		selects the render path (see {@link FieldTypeCase})
		 * @param hintListConstantsClassJavadocMention
		 * 		for a string field carrying a hint list: a javadoc mention of the generated hint-list constants class
		 * 		(the runtime type stays {@code String}); {@code null} otherwise
		 */
		record FieldTypeSpecifics(List<String> runtimeTypeFqn, String typeDescriptionForJavadoc, FieldTypeCase typeCase,
		                          String hintListConstantsClassJavadocMention) {
		}

		/** Selects how a field's accessors are rendered: an enum (with its {@code _of}/{@code _stringValue} string
		 * conversion protocol), a confirm field (cast plus the confirm-specific unwrap), or any other type (a plain cast). */
		enum FieldTypeCase {ENUM, CONFIRM, OTHER}

		/**
		 * The {@link FieldTypeSpecifics} for any field type that does not warrant a generated companion class (i.e. every
		 * type other than an enum or a string-with-hint-list - those are handled by
		 * {@link TypedAccessorGenerator#specificsForFieldType}).
		 */
		static FieldTypeSpecifics fieldTypeSpecificsNonEnum(IFieldType type) {
			List<String> runtimeType;
			String typeDescriptionForJavadoc = null;
			FieldTypeCase typeCase = FieldTypeCase.OTHER;
			if (type instanceof IStringType stringType) {
				assert stringType.getHintList().isEmpty() : "hintList fields are expected to be handled elsewhere, but got " + stringType.getHintList();
				runtimeType = parseFqn("java.lang.String");
			} else if ((type instanceof IDateType dateType && !dateType.arePartiallyKnownDatesAllowed())
					|| type instanceof IDateFragmentType) {
				runtimeType = parseFqn("java.time.Instant");
			} else if (type instanceof IDateType dateType && dateType.arePartiallyKnownDatesAllowed()) {
				runtimeType = parseFqn(
						"java.lang.Object"); // it appears that the type is dynamically decided between Date and String
				typeDescriptionForJavadoc = "the field value can be either an Instant or a String (the field's type allows partially known dates)";
			} else if (type instanceof INumberType numberType && !numberType.isLeadingZerosAllowed()) {
				runtimeType = parseFqn("java.math.BigDecimal");
			} else if (type instanceof INumberType numberType && numberType.isLeadingZerosAllowed()) {
				runtimeType = parseFqn("java.lang.Object"); // the value can either be String or BigDecimal (decided dynamically)
				typeDescriptionForJavadoc = "the field value can be either a BigDecimal or a String (the field's type allows leading zeros)";
			} else if (type instanceof IBooleanType || type instanceof IConfirmType) { // Note: confirm can only be null or true, but that is currently not encodable because both use Boolean -> javadoc? wrapper class?
				runtimeType = parseFqn("java.lang.Boolean"); // Note: the values in the document are allowed to be null!
				if (type instanceof IBooleanType) {
					typeDescriptionForJavadoc = "Boolean field: allowed values are Boolean.TRUE, Boolean.FALSE and null";
				} else {
					assert type instanceof IConfirmType;
					typeCase = FieldTypeCase.CONFIRM;
					typeDescriptionForJavadoc = "Confirm field: allowed values are Boolean.TRUE and null";
				}
			} else if (type instanceof IDateRangeType) {
				runtimeType = parseFqn("com.mgmtp.a12.utils.conversion.InstantRange");
			} else if (type instanceof ICustomFieldType customType) {
				runtimeType = parseFqn("java.lang.String"); // custom field types are strings in the document
				// (at least if eserialized by the kernel serializer).
				// also, other types could potentially violate the immutability requirement
				typeDescriptionForJavadoc = "custom field type: " + customType.getName();
			} else {
				// flag (like the missing-typedef case): warn for CLI visibility and fail fast where assertions are enabled.
				String msg = "unhandled field type '" + type.getClass().getSimpleName() + "'; accessors fall back to Object";
				System.err.println("warning: " + msg);
				assert false : msg;
				runtimeType = parseFqn("java.lang.Object");
			}
			return new FieldTypeSpecifics(runtimeType, typeDescriptionForJavadoc, typeCase, null);
		}
	}

	static class ViewClass extends GroupClass {
		private final List<String> untypedGroupOrDocFqn;
		private final List<String> typedGroupOrDocViewFqn;
		private final List<String> pointerClassFqn;
		private final boolean isRoot;
		private final String dmIdIfRoot;

		ViewClass(
				List<String> fqn,
				boolean isRoot,
				String dmIdIfRoot,
				List<String> pointerClassFqn,
				InfoForJavaDoc infoForJavaDoc,
				List<String> classAnnotationFqns
		) {
			super(fqn, infoForJavaDoc, classAnnotationFqns);
			this.untypedGroupOrDocFqn = parseFqn(isRoot
					? "com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2"
					: "com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2");
			this.typedGroupOrDocViewFqn = parseFqn(isRoot
					? "com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedDocumentView"
					: "com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedGroupView");
			this.pointerClassFqn = pointerClassFqn;
			this.referencedFqns.addAll(List.of(this.untypedGroupOrDocFqn, this.typedGroupOrDocViewFqn, abstractTypedViewFqn,
					this.pointerClassFqn, groupInstanceV2Fqn, illegalArgumentExceptionFqn));
			this.isRoot = isRoot;
			this.dmIdIfRoot = dmIdIfRoot;
		}

		String format() {
			ST tpl = new ST("""
					package <package>;
					<imports>
					<javaDocIfAny>
					<classAnnotations>
					public class <viewClassName> extends <AbstractTypedView>\\<<untypedGroupOrDoc>, <viewClassName>> implements <typedGroupOrDocView>\\<<viewClassName>> {
					    <subGroupAndFieldMethods>
					
					    <if(isRoot)>
					    public static <viewClassName> _viewOfCheckDmId(<untypedGroupOrDoc> document) {
					        if(!"<dmIdIfRoot>".equals(document.getDocumentModelId())) {
					            throw new <IllegalArgumentException>("Document Model id was expected to be <dmIdIfRoot> but was " + document.getDocumentModelId());
					        }
					        return _viewOf(document);
					    }
					
					    public static <viewClassName> _empty() {
					         return _viewOf(<untypedGroupOrDoc>.empty("<dmIdIfRoot>"));
					    }
					    <else>
					    public static <viewClassName> _empty() {
					         return _viewOf(<untypedGroupOrDoc>.empty());
					    }
					    <endif>
					
					    public static <viewClassName> _viewOf(<untypedGroupOrDoc> group) {
					        return group != null ? new <viewClassName>(group) : null;
					    }
					    private <viewClassName>(<untypedGroupOrDoc> group) {
					              super(group);
					    }
					    @Override protected <viewClassName> __internal_self() { return this; }
					
					    /**
					     * @return a typed pointer with {@link <viewClassName>} as the start type.
					     * The returned pointer is zero-length (i.e. its target type is {@link <viewClassName>} too),
					     * but offers methods that return pointers to subgroups and fields.
					     */
					    public static <pointerClass>\\<<viewClassName>> _pointer() {
					        return <pointerClass>.__internal_from(
					            // the static type checks should already guarantee that this cast succeeds
					            (<GroupInstanceV2> g) ->  <viewClassName>._viewOf((<untypedGroupOrDoc>) g));
					    }
					}
					""");
			ImportCollisionResolver fqn2Ref = new ImportCollisionResolver(selfClassName, this.referencedFqns);
			tpl.add("package", String.join(".", packagePath));
			tpl.add("imports", formatImports(fqn2Ref));
			tpl.add("javaDocIfAny", formatJavaDoc(infoForJavaDoc));
			tpl.add("classAnnotations", formatClassAnnotations(classAnnotationFqns));
			tpl.add("viewClassName", selfClassName);
			tpl.add("AbstractTypedView", fqn2Ref.fqn2Ref(this.abstractTypedViewFqn));
			tpl.add("untypedGroupOrDoc", fqn2Ref.fqn2Ref(this.untypedGroupOrDocFqn));
			tpl.add("typedGroupOrDocView", fqn2Ref.fqn2Ref(this.typedGroupOrDocViewFqn));
			tpl.add("subGroupAndFieldMethods", String.join("\n",
					// sort for determinism and independence from order in the dm
					this.subGroupAndFieldDeclarationFormattables.stream().map(m -> m.format(fqn2Ref)).sorted().toList()));
			tpl.add("GroupInstanceV2", fqn2Ref.fqn2Ref(groupInstanceV2Fqn));
			tpl.add("pointerClass", fqn2Ref.fqn2Ref(this.pointerClassFqn));
			tpl.add("isRoot", isRoot);
			tpl.add("dmIdIfRoot", this.dmIdIfRoot);
			tpl.add("IllegalArgumentException", fqn2Ref.fqn2Ref(illegalArgumentExceptionFqn));
			return tpl.render();
		}

		@Override void addSubGroup(String subGroupName, GenResult subGroupResult, IGroup subGroup, InfoForJavaDoc infoForJavaDoc) {
			List<String> subGroupViewFqn = subGroupResult.viewClassFqn();
			int rep = subGroup.getRepeatability();
			String childMethodName = childMethodNameFor(subGroupName);
			this.referencedFqns.add(subGroupViewFqn); // will be replaced during render by either fqn or unqualified name + import
			if (rep == 1) {
				subGroupAndFieldDeclarationFormattables.add(
						(fqn2Ref) -> {
							var tpl = new ST("""
									<javaDocIfAny>
									public <subGroupViewClass> <childMethodName>() { return <subGroupViewClass>._viewOf(_unwrap().directSubgroup("<subGroupName>", 1)); }""");
							tpl.add("javaDocIfAny", formatJavaDoc(infoForJavaDoc));
							tpl.add("subGroupViewClass", fqn2Ref.fqn2Ref(subGroupViewFqn));
							tpl.add("childMethodName", childMethodName);
							tpl.add("subGroupName", StringEscapeUtils.escapeJava(
									subGroupName)); // escaping is probably not strictly necessary here due to dm naming constraints
							return tpl.render();
						}
				);
			} else {
				assert rep > 1 : rep;
				var listFqn = parseFqn("java.util.List");
				this.referencedFqns.add(listFqn);
				subGroupAndFieldDeclarationFormattables.add(
						(fqn2Ref) -> {
							var tpl = new ST("""
									<javaDocIfAny>
									public <List>\\<<subGroupViewClass>> <childMethodName>() { return _unwrap().directSubgroup("<subGroupName>").stream().map(<subGroupViewClass>::_viewOf).toList(); }""");
							tpl.add("javaDocIfAny", formatJavaDoc(infoForJavaDoc));
							tpl.add("List", fqn2Ref.fqn2Ref(listFqn));
							tpl.add("subGroupViewClass", fqn2Ref.fqn2Ref(subGroupViewFqn));
							tpl.add("childMethodName", childMethodName);
							tpl.add("subGroupName", StringEscapeUtils.escapeJava(
									subGroupName)); // escaping is probably not strictly necessary here due to dm naming constraints
							return tpl.render();
						}
				);
			}
		}

		@Override void addField(String fieldName, FieldTypeSpecifics fieldSpecs, InfoForJavaDoc infoForJavaDoc) {
			String childMethodName = childMethodNameFor(fieldName);
			if (fieldSpecs.typeCase() == FieldTypeCase.ENUM) {
				List<String> enumFqn = fieldSpecs.runtimeTypeFqn();
				var stringFqn = parseFqn("java.lang.String");
				subGroupAndFieldDeclarationFormattables.add(fqn2ref -> {
					var tpl = new ST("""
							<javaDocIfAny>
							public <enumClass> <fieldMethodName>() { return <enumClass>._of((<String>) _unwrap().directFieldValue("<fieldName>")); }""");
					tpl.add("javaDocIfAny", formatJavaDoc(infoForJavaDoc));
					tpl.add("fieldName", StringEscapeUtils.escapeJava(
							fieldName)); // escaping is probably not strictly necessary here due to dm naming constraints
					tpl.add("fieldMethodName", childMethodName);
					tpl.add("String", fqn2ref.fqn2Ref(stringFqn));
					tpl.add("enumClass", fqn2ref.fqn2Ref(enumFqn));
					return tpl.render();
				});
				this.referencedFqns.add(enumFqn);
				this.referencedFqns.add(stringFqn);
			} else {
				this.referencedFqns.add(fieldSpecs.runtimeTypeFqn());
				subGroupAndFieldDeclarationFormattables.add(
						fqn2ref -> renderFieldMethod(fqn2ref, fieldName, childMethodName, fieldSpecs, infoForJavaDoc));
			}
		}

		private static String renderFieldMethod(Fqn2Ref globalResolver, String fieldName, String fieldMethodName,
				FieldTypeSpecifics fieldTypeSpecifics, InfoForJavaDoc infoForJavaDoc) {
			var tpl = new ST("""
					<javaDocIfAny>
					public <runtimeType> <fieldMethodName>() { return (<runtimeType>) _unwrap().directFieldValue("<fieldName>"); }""");
			tpl.add("javaDocIfAny", formatJavaDoc(infoForJavaDoc));
			tpl.add("fieldName", StringEscapeUtils.escapeJava(
					fieldName)); // escaping is probably not strictly necessary here due to dm naming constraints
			tpl.add("fieldMethodName", fieldMethodName);
			tpl.add("runtimeType", globalResolver.fqn2Ref(fieldTypeSpecifics.runtimeTypeFqn()));
			return tpl.render();
		}
	}

	static class PointerClass extends GroupClass {
		private static final List<String> typedGroupViewFqn = parseFqn(
				"com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedGroupView");
		private static final List<String> typedGroupPointerImplFqn = parseFqn(
				"com.mgmtp.a12.kernel.md.document.internal.service.implV2.typedaccessors.TypedGroupPointerImpl");
		private static final List<String> typedPointerFqn = parseFqn("com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedPointer");
		private static final List<String> typedFieldValuePointerImplFqn = parseFqn(
				"com.mgmtp.a12.kernel.md.document.internal.service.implV2.typedaccessors.TypedFieldValuePointerImpl");

		private final List<String> viewClassFqn;
		private final List<String> untypedGroupOrDocFqn;
		private final List<String> typedPointerSuperclassFqn;

		PointerClass(
				IGroup group,
				List<String> fqn,
				List<String> viewClassFqn,
				boolean isRoot,
				InfoForJavaDoc infoForJavaDoc,
				List<String> classAnnotationFqns
		) {
			super(fqn, infoForJavaDoc, classAnnotationFqns);
			this.viewClassFqn = viewClassFqn;
			this.untypedGroupOrDocFqn = parseFqn(isRoot
					? "com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2"
					: "com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2");
			this.typedPointerSuperclassFqn = parseFqn(group.getRepeatability() == 1
					? "com.mgmtp.a12.kernel.md.document.internal.service.implV2.typedaccessors.TypedGroupPointerImpl"
					: "com.mgmtp.a12.kernel.md.document.internal.service.implV2.typedaccessors.TypedRepeatableGroupPointerImpl");
			this.referencedFqns.addAll(
					List.of(this.viewClassFqn, typedPointerSuperclassFqn, untypedGroupOrDocFqn, typedGroupViewFqn,
							typedGroupPointerImplFqn, stringFqn, functionFqn, groupInstanceV2Fqn, documentPointerFqn, listFqn));
		}

		@Override String format() {
			ST tpl = new ST("""
					package <package>;
					<imports>
					<javaDocIfAny>
					<classAnnotations>
					public class <className>\\<_S extends <TypedGroupView>\\<_S>> extends <superClass>\\<_S, <viewClass>> {
					
					    <subGroupAndFieldMethods>
					
					    public static \\<_S_ extends <TypedGroupView>\\<_S_>> <className>\\<_S_> __internal_derive(
					        <TypedGroupPointerImpl>\\<_S_, ?> parent,
					        <String> name,
					        int repetition
					    ) {
					        return new <className>\\<>(parent._unwrap().withAppended(name, repetition), parent.__internal_wrapStart());
					    }
					
						public static \\<_S_ extends <TypedGroupView>\\<_S_>> <className>\\<_S_> __internal_derive(
							<Function>\\<<GroupInstanceV2>,_S_> wrapStart,
						    <DocumentPointer> singleRepPointer
						) {
						    return new <className>\\<>(singleRepPointer, wrapStart);
						}
					
					    public static \\<_S_ extends <TypedGroupView>\\<_S_>> <className>\\<_S_> __internal_from(
					        <Function>\\<<GroupInstanceV2>, _S_> wrapStart
					    ) {
					        return new <className>\\<>(<DocumentPointer>.of(<List>.of(), <List>.of()), wrapStart);
					    }
					
					    private <className>(<DocumentPointer> untypedPointer, <Function>\\<<GroupInstanceV2>, _S> wrapStart) {
					        // the static type checks should already guarantee that this cast succeeds
					        super(untypedPointer, wrapStart, (<GroupInstanceV2> g) -> <viewClass>._viewOf((<untypedGroupOrDoc>) g));
					    }
					}
					""");
			ImportCollisionResolver fqn2Ref = new ImportCollisionResolver(selfClassName, this.referencedFqns);
			tpl.add("package", String.join(".", packagePath));
			tpl.add("imports", formatImports(fqn2Ref));
			tpl.add("javaDocIfAny", formatJavaDoc(infoForJavaDoc));
			tpl.add("classAnnotations", formatClassAnnotations(classAnnotationFqns));
			tpl.add("className", selfClassName);
			tpl.add("TypedGroupView", fqn2Ref.fqn2Ref(typedGroupViewFqn));
			tpl.add("superClass", fqn2Ref.fqn2Ref(this.typedPointerSuperclassFqn));
			tpl.add("viewClass", fqn2Ref.fqn2Ref(this.viewClassFqn));
			tpl.add("subGroupAndFieldMethods", String.join("\n",
					// sort for determinism and independence from the order in the dm
					this.subGroupAndFieldDeclarationFormattables.stream().map(m -> m.format(fqn2Ref)).sorted().toList()));
			tpl.add("TypedGroupPointerImpl", fqn2Ref.fqn2Ref(typedGroupPointerImplFqn));
			tpl.add("String", fqn2Ref.fqn2Ref(stringFqn));
			tpl.add("Function", fqn2Ref.fqn2Ref(functionFqn));
			tpl.add("GroupInstanceV2", fqn2Ref.fqn2Ref(groupInstanceV2Fqn));
			tpl.add("DocumentPointer", fqn2Ref.fqn2Ref(documentPointerFqn));
			tpl.add("List", fqn2Ref.fqn2Ref(listFqn));
			tpl.add("untypedGroupOrDoc", fqn2Ref.fqn2Ref(this.untypedGroupOrDocFqn));
			return tpl.render();
		}

		@Override void addSubGroup(String subGroupName, GenResult subGroupResult, IGroup subGroup, InfoForJavaDoc infoForJavaDoc) {
			List<String> subGroupViewFqn = subGroupResult.viewClassFqn();
			List<String> subGroupPointerFqn = subGroupResult.pointerClassFqn();
			int rep = subGroup.getRepeatability();
			String childMethodName = childMethodNameFor(subGroupName);
			this.referencedFqns.addAll(List.of(subGroupPointerFqn, subGroupViewFqn, typedRepeatabilityPointerFqn,
					typedRepetitionsPointerImplFqn)); // will be replaced during render by either fqn or unqualified name + import
			String tplString;
			if (rep == 1) {
				tplString = """
						<javaDocIfAny>
						public <subGroupPointerClass>\\<_S> <childMethodName>() { return <subGroupPointerClass>.__internal_derive(this, "<subGroupName>", 1); }""";
			} else {
				assert rep > 1 : rep;
				tplString = """
						<javaDocIfAny>
						public <subGroupPointerClass>\\<_S> <childMethodName>(int oneBasedRepetition) { return <subGroupPointerClass>.__internal_derive(this, "<subGroupName>", oneBasedRepetition); }
						<javaDocIfAny>
						public <TypedRepetitionsPointer>\\<_S, <subGroupViewClass>, <subGroupPointerClass>\\<_S>> <childMethodName>() {
							return <TypedRepetitionsPointerImpl>.__internal_derive(
									this._unwrap(),
									"<subGroupName>",
									this.__internal_wrapStart(),
									<subGroupViewClass>::_viewOf,
									<subGroupPointerClass>::__internal_derive
							);
						}
						""";
			}
			subGroupAndFieldDeclarationFormattables.add(
					(fqn2Ref) -> {
						var tpl = new ST(tplString);
						tpl.add("javaDocIfAny", formatJavaDoc(infoForJavaDoc));
						tpl.add("subGroupPointerClass", fqn2Ref.fqn2Ref(subGroupPointerFqn));
						tpl.add("subGroupViewClass", fqn2Ref.fqn2Ref(subGroupViewFqn));
						tpl.add("childMethodName", childMethodName);
						tpl.add("subGroupName", StringEscapeUtils.escapeJava(
								subGroupName)); // escaping is probably not strictly necessary here due to dm naming constraints
						tpl.add("TypedRepetitionsPointerImpl", fqn2Ref.fqn2Ref(typedRepetitionsPointerImplFqn));
						tpl.add("TypedRepetitionsPointer", fqn2Ref.fqn2Ref(typedRepeatabilityPointerFqn));
						return tpl.render();
					}
			);
		}

		@Override void addField(String fieldName, FieldTypeSpecifics fieldSpecs, InfoForJavaDoc infoForJavaDoc) {
			// TODO maybe dedup with views code
			String childMethodName = childMethodNameFor(fieldName);
			if (fieldSpecs.typeCase() == FieldTypeCase.ENUM) {
				List<String> enumFqn = fieldSpecs.runtimeTypeFqn();
				var stringFqn = parseFqn("java.lang.String");
				subGroupAndFieldDeclarationFormattables.add(fqn2ref -> {
					var tpl = new ST("""
							<javaDocIfAny>
							public <TypedPointer>\\<_S, <enumClass>> <fieldMethodName>() { return <TypedFieldValuePointerImpl>.__internal_derive(this, "<fieldName>", o -> <enumClass>._of(<String>.class.cast(o)), <enumClass>::_stringValue); }""");
					tpl.add("javaDocIfAny", formatJavaDoc(infoForJavaDoc));
					tpl.add("fieldName", StringEscapeUtils.escapeJava(
							fieldName)); // escaping is probably unnecessary here, because special chars are disallowed for field names anyway
					tpl.add("fieldMethodName", childMethodName);
					tpl.add("String", fqn2ref.fqn2Ref(stringFqn));
					tpl.add("enumClass", fqn2ref.fqn2Ref(enumFqn));
					tpl.add("TypedPointer", fqn2ref.fqn2Ref(typedPointerFqn));
					tpl.add("TypedFieldValuePointerImpl", fqn2ref.fqn2Ref(typedFieldValuePointerImplFqn));
					return tpl.render();
				});
				this.referencedFqns.addAll(List.of(typedPointerFqn, typedFieldValuePointerImplFqn, stringFqn));
				this.referencedFqns.add(enumFqn);
				this.referencedFqns.add(stringFqn);
			} else {
				this.referencedFqns.addAll(List.of(fieldSpecs.runtimeTypeFqn(), typedPointerFqn,
						typedFieldValuePointerImplFqn, objectFqn, typedGroupPointerImplFqn));
				subGroupAndFieldDeclarationFormattables.add(fqn2ref -> renderFieldMethod(fqn2ref, fieldName, childMethodName,
						fieldSpecs, infoForJavaDoc));
			}
		}

		private static String renderFieldMethod(Fqn2Ref globalResolver, String fieldName, String fieldMethodName,
				FieldTypeSpecifics fieldTypeSpecifics, InfoForJavaDoc infoForJavaDoc) {
			var tpl = new ST("""
					<javaDocIfAny>
					public <TypedPointer>\\<_S, <runtimeType>> <fieldMethodName>() { return <TypedFieldValuePointerImpl>.__internal_derive(this, "<fieldName>", <runtimeType>.class::cast, <if(isConfirm)><TypedGroupPointerImpl>::__unwrapAndCheckConfirmFieldValue<else>(<runtimeType> t) -> (<Object>) t<endif>); }""");
			tpl.add("javaDocIfAny", formatJavaDoc(infoForJavaDoc));
			tpl.add("fieldName", StringEscapeUtils.escapeJava(
					fieldName)); // escaping is probably unnecessary here, because special chars are disallowed for field names anyway
			tpl.add("fieldMethodName", fieldMethodName);
			tpl.add("runtimeType", globalResolver.fqn2Ref(fieldTypeSpecifics.runtimeTypeFqn()));
			tpl.add("TypedPointer", globalResolver.fqn2Ref(typedPointerFqn));
			tpl.add("TypedFieldValuePointerImpl", globalResolver.fqn2Ref(typedFieldValuePointerImplFqn));
			tpl.add("Object", globalResolver.fqn2Ref(objectFqn));
			tpl.add("isConfirm", fieldTypeSpecifics.typeCase == FieldTypeCase.CONFIRM);
			tpl.add("TypedGroupPointerImpl", globalResolver.fqn2Ref(typedGroupPointerImplFqn));
			return tpl.render();
		}
	}

	private static GenResult generateEnumClass(EnumOrHintlistClassLocation loc, IEnumerationType etype,
			List<String> classAnnotationFqns) {
		String enumClassName = nameAsClassName(escapeNameIfNecessary(loc.baseName()));
		List<String> enumClassFqn = appendList(loc.packagePath(), enumClassName);
		List<String> stringFqn = GroupClass.parseFqn("java.lang.String");
		List<String> arraysFqn = GroupClass.parseFqn("java.util.Arrays");
		List<String> mapFqn = GroupClass.parseFqn("java.util.Map");
		List<String> collectorsFqn = GroupClass.parseFqn("java.util.stream.Collectors");
		ST st = new ST("""
				package <package>;
				<imports>
				<classAnnotations>
				public enum <enumClassName> {
				    <enumConstants>
				
					private final <String> _internalUntypedValue;
					private static final <Map>\\<<String>, <enumClassName>> _internalCasesMap = <Arrays>.stream(
					    <enumClassName>.values()).collect(<Collectors>.toMap(e -> e._internalUntypedValue, e -> e));
				
					private <enumClassName>(<String> untypedValue) {
						this._internalUntypedValue = untypedValue;
					}
				
					public static <enumClassName> _of(<String> value) {
						if(value == null) {
							return null;
						}
						if(!_internalCasesMap.containsKey(value)) {
							throw new <IllegalArgumentException>(value + "is not a valid string value for this enum. Valid values are " + _internalCasesMap.keySet());
						}
						return _internalCasesMap.get(value);
					}
					public <String> _stringValue() {
					    return this._internalUntypedValue;
					}
				}
				""");
		List<Pair<String, String>> enumConstantNamesAndStrings = etype.getValues().stream()
				.map(IEnumerationType.IEnumValue::getValue)
				.map(str -> Pair.of(escapeNameIfNecessary(str, s -> !startsWithAZ.matcher(s).matches()), str)).toList();
		ImportCollisionResolver fqnResolver = new ImportCollisionResolver(enumClassName,
				Set.of(stringFqn, arraysFqn, mapFqn, collectorsFqn, illegalArgumentExceptionFqn),
				enumConstantNamesAndStrings.stream().map(Pair::getLeft).toList());
		st.add("package", String.join(".", enumClassFqn.subList(0, enumClassFqn.size() - 1)));
		st.add("enumClassName", enumClassName);
		st.add("enumConstants", enumConstantNamesAndStrings.stream()
				.sorted(Comparator.comparing(
						Pair::getLeft)) // sort enum constants, to make codegen deterministic and independent of order in the document model
				.map(pair -> pair.getLeft() + "(\"" + StringEscapeUtils.escapeJava(pair.getRight()) + "\")")
				.collect(Collectors.joining(",\n", "", ";")));
		st.add("imports", formatImports(fqnResolver));
		st.add("classAnnotations", formatClassAnnotations(classAnnotationFqns));
		st.add("String", fqnResolver.fqn2Ref(stringFqn));
		st.add("Map", fqnResolver.fqn2Ref(mapFqn));
		st.add("Arrays", fqnResolver.fqn2Ref(arraysFqn));
		st.add("Collectors", fqnResolver.fqn2Ref(collectorsFqn));
		st.add("IllegalArgumentException", fqnResolver.fqn2Ref(illegalArgumentExceptionFqn));
		return new GenResult(enumClassFqn, null, Map.of(enumClassFqn, st.render()));
	}

	private static GenResult generateHintListConstantsClass(EnumOrHintlistClassLocation loc,
			IStringType stype, List<String> classAnnotationFqns) {
		String className = nameAsClassName(escapeNameIfNecessary(loc.baseName()));
		List<String> classFqn = appendList(loc.packagePath(), className);

		ST st = new ST("""
				package <package>;
				<classAnnotations>
				public final class <className> {
				    private <className>() {}
				    <localeInnerClasses>
				}
				""");
		String localeInnerClasses = stype.getHintList().stream()
				.sorted(Comparator.comparing(hl -> hl.getLocale().toString()))
				.map(hintList -> renderLocaleInnerClass(hintList))
				.collect(Collectors.joining("\n"));
		st.add("package", String.join(".", loc.packagePath()));
		st.add("classAnnotations", formatClassAnnotations(classAnnotationFqns));
		st.add("className", className);
		st.add("localeInnerClasses", localeInnerClasses);
		return new GenResult(classFqn, null, Map.of(classFqn, st.render()));
	}

	private static String renderLocaleInnerClass(IStringType.IHintList hintList) {
		String localeClassName = nameAsClassName(escapeNameIfNecessary(hintList.getLocale().toString()));
		String constants = hintList.getValues().stream()
				.map(value -> Pair.of(
						escapeNameIfNecessary(value, s -> !startsWithAZ.matcher(s).matches()),
						value))
				.sorted(Comparator.comparing(Pair::getLeft))
				.map(pair -> "public static final String " + pair.getLeft()
						+ " = \"" + StringEscapeUtils.escapeJava(pair.getRight()) + "\";")
				.collect(Collectors.joining("\n"));
		ST st = new ST("""
				public static final class <localeClassName> {
				    private <localeClassName>() {}
				    <constants>
				}""");
		st.add("localeClassName", localeClassName);
		st.add("constants", constants);
		return st.render();
	}

	private static String buildHintListJavadocExtra(List<String> hintListClassFqn) {
		String simpleClassName = hintListClassFqn.getLast();
		String fullClassName = String.join(".", hintListClassFqn);
		return "Hint list constants: {" + "@link " + fullClassName + " " + simpleClassName + "}";
	}

	private static String formatImports(ImportCollisionResolver fqnResolver) {
		return fqnResolver.imports().stream().sorted().map(fqn -> "import " + fqn + ";\n").collect(Collectors.joining());
	}

	private static String formatClassAnnotations(List<String> classAnnotationFqns) {
		return classAnnotationFqns.stream().map(fqn -> "@" + fqn).collect(Collectors.joining("\n"));
	}

	private static String escapeNameIfNecessary(String name) {
		return escapeNameIfNecessary(name, null);
	}

	/**
	 * <em>Note</em>: the comparison to names that are reserved in java is done case-insensitive, so that the returned value should
	 * be safe even if it is later (partially) lowercased or uppercased.<br/>
	 * <em>Note</em>: additionalEscapeCondition must work on the original names - there is no case normalization before
	 * additionalEscapeCondition#test is evaluated.
	 */
	private static String escapeNameIfNecessary(String name, Predicate<String> additionalEscapeCondition) {
		// NOTE: theoretically, the escaping could make a name collision with a
		// neighboring subgroup possible.
		// This seems like a rare edge-case, and we don't support it for now.
		// (Supporting it would require that another subgroup's existence or absence
		// could change the name of a method, violating the desired independence from
		// all un-accessed parts of the document model.
		// Also, the correspondence between accessor methods and subgroups would be
		// ambiguous in that scenario)

		name = replaceDisallowedChars(name);

		Set<String> reservedNames = new HashSet<>(Set.of(
				// list of keywords from https://docs.oracle.com/javase/tutorial/java/nutsandbolts/_keywords.html
				"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
				"class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
				"for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package",
				"private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized",
				"this", "throw", "throws", "transient", "try", "void", "volatile", "while",
				// not keywords, but literals:
				"true", "false", "null",
				// method names that are taken because they are inherited from Object: (from https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html)
				"clone", "equals", "finalize", "getClass", "hashCode", "notify", "notifyAll", "toString", "wait"
				// are there other categories of reserved names?
		).stream().map(String::toLowerCase).toList());
		if (reservedNames.contains(
				name.toLowerCase()) // disallow case-insensitive collision with keywords, to be robust against later changes of capitalization
				|| (additionalEscapeCondition != null && additionalEscapeCondition.test(name))) {
			name = "Escaped_" + name;
		}
		return name;
	}

	private static String replaceDisallowedChars(String str) {
		// special case treatment for spaces, for better readability
		str = str.replaceAll("\\s", "_"); // not bijective, but it is probably ok to assume that this won't create name collisions
		// or otherwise the dm can be "not supported"

		// catch-all fallback for all other characters (intends to be mostly bijective)
		return replaceNonAlphanumericOrUnderscoreWithUnicodeCodepoint(str);
	}

	private static String replaceNonAlphanumericOrUnderscoreWithUnicodeCodepoint(String str) {
		StringBuilder sb = new StringBuilder();
		for (char c : str.toCharArray()) {
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') {
				sb.append(c);
			} else {
				sb.append(sb.length() == 0 ? "u" : "_u").append(String.format("%04X", (int) c)).append("_");
			}
		}
		return sb.toString();
	}

	private static List<String> mkPackage4View(List<String> packagePrefix, List<String> packageRest) {
		return concatToList(packagePrefix, List.of("views"), packageRest);
	}

	private static List<String> mkPackage4Pointer(List<String> packagePrefix, List<String> packageRest) {
		return concatToList(packagePrefix, List.of("pointers"), packageRest);
	}

	/**
	 * @return the package for the value-classes (e.g. enum classes) of the typedef with the given id. Sibling of the
	 * 		{@code views}/{@code pointers}/{@code pojos} packages; the per-id segment keeps distinct typedefs apart while
	 * 		letting all references to the <em>same</em> typedef id share a single class.
	 */
	private static List<String> mkPackage4Typedef(List<String> packagePrefix, String typedefId) {
		return concatToList(packagePrefix, List.of("typedefs", typedefIdAsPackagePart(typedefId)));
	}

	/**
	 * Turns a (globally unique) typedef id into a valid, lowercase java package segment.
	 * <p>
	 * Standard-format typedef ids as produced by sme will yield nicely-readable strings (just replace "-" by "_"),
	 * while nonstandard strings will be escaped in a way that makes only mild assumptions regarding collision-safety.
	 */
	private static String typedefIdAsPackagePart(String typedefId) {
		return replaceNonAlphanumericOrUnderscoreWithUnicodeCodepoint(typedefId.replace("-", "_"));
	}

	private static List<List<String>> parsePathsArgs(String[] pathsArgs) {
		if (pathsArgs == null) {
			return null;
		}
		return Arrays.stream(pathsArgs).map(p -> {
			if (p.startsWith("/")) {
				p = p.substring(1);
			}
			return Arrays.asList(p.split("/"));
		}).toList();
	}

	private static boolean isPathOnOrBelowAny(List<String> path, Collection<List<String>> otherPaths) {
		return otherPaths.isEmpty()
				|| otherPaths.stream().anyMatch(
				includedPrefix -> isPrefix(path, includedPrefix) || isPrefix(includedPrefix, path));
	}

	private static String formatJavaDoc(InfoForJavaDoc infoForJavaDoc) {
		ST tpl = new ST("""
				<if(hasJavaDoc)>
				/**
				 * External Description: <externalDescription>
				 * \\<p>
				 * Internal Description: <internalDescription>
				<if(hasFieldTypeInfo)>
				 * \\<p>
				 * Field Type Info: <fieldTypeInfo>
				<endif>
				<if(hasKnownRepeatabilityGreater1)>
				 * \\<p>
				 * the maximum allowed one-based repetition is <maxRepetition>
				<endif>
				<if(hasHintListMention)>
				 * \\<p>
				 * <hintListMention>
				<endif>
				 */<endif>""");
		tpl.add("hasJavaDoc",
				!StringUtils.isEmpty(infoForJavaDoc.internalDescription)
						|| !StringUtils.isEmpty(infoForJavaDoc.externalDescription)
						|| !StringUtils.isEmpty(infoForJavaDoc.fieldTypeInfo)
						|| infoForJavaDoc.maxRepeatabilityOneBasedIfApplicable > 1
						|| !StringUtils.isEmpty(infoForJavaDoc.hintListConstantsClassJavadocMentionIfApplicable));
		tpl.add("externalDescription", sanitizeForJavaDoc(infoForJavaDoc.externalDescription));
		tpl.add("internalDescription", sanitizeForJavaDoc(infoForJavaDoc.internalDescription));
		tpl.add("hasFieldTypeInfo", !StringUtils.isEmpty(infoForJavaDoc.fieldTypeInfo));
		tpl.add("fieldTypeInfo", sanitizeForJavaDoc(infoForJavaDoc.fieldTypeInfo));
		tpl.add("hasKnownRepeatabilityGreater1", infoForJavaDoc.maxRepeatabilityOneBasedIfApplicable > 1);
		tpl.add("maxRepetition", infoForJavaDoc.maxRepeatabilityOneBasedIfApplicable);
		tpl.add("hasHintListMention", !StringUtils.isEmpty(infoForJavaDoc.hintListConstantsClassJavadocMentionIfApplicable));
		tpl.add("hintListMention", infoForJavaDoc.hintListConstantsClassJavadocMentionIfApplicable);
		return tpl.render();
	}

	record InfoForJavaDoc(String internalDescription, String externalDescription, String fieldTypeInfo,
	                      int maxRepeatabilityOneBasedIfApplicable, String hintListConstantsClassJavadocMentionIfApplicable) {
		static InfoForJavaDoc of(IGroup group) {
			return ofInternal(group, "", -1, null);
		}

		static InfoForJavaDoc of(IField field, GroupClass.FieldTypeSpecifics fieldSpecs) {
			String fieldTypeInfo = fieldSpecs.typeDescriptionForJavadoc() != null ? fieldSpecs.typeDescriptionForJavadoc() : "";
			return ofInternal(field, fieldTypeInfo, -1, fieldSpecs.hintListConstantsClassJavadocMention());
		}

		static InfoForJavaDoc ofGroupWithKnownEffectiveRepeatability(IGroup group) {
			return ofInternal(group, null, group.getRepeatability(), null);
		}

		static InfoForJavaDoc ofInternal(IElement groupOrField, String fieldTypeInfo, int maxRepeatabilityOneBasedIfApplicable,
				String hintListConstantsClassJavadocMentionIfApplicable) {
			return new InfoForJavaDoc(
					groupOrField != null ? pickDescriptionFromLocalizedTextMap(groupOrField.getInternalDescription()) : "",
					groupOrField != null ? pickDescriptionFromLocalizedTextMap(groupOrField.getExternalDescription()) : "",
					fieldTypeInfo,
					maxRepeatabilityOneBasedIfApplicable,
					hintListConstantsClassJavadocMentionIfApplicable);
		}
	}

	private static String sanitizeForJavaDoc(String nullableString) {
		String str = nullableString != null ? nullableString : "";
		StringBuilder sb = new StringBuilder();
		for (char c : str.toCharArray()) {
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
					|| c == '_' || c == '-' || c == ',' || c == '.' || c == ';'
					|| c == ' ') {
				sb.append(c);
			}
			// other characters are simply dropped
		}
		return sb.toString();
	}

	/**
	 * @return the text for Locale.ENGLISH or another locale, or null if none exists
	 */
	private static String pickDescriptionFromLocalizedTextMap(ILocalizedTextMap localizedTextMap) {
		if (localizedTextMap.containsKey(Locale.ENGLISH)) {
			return localizedTextMap.get(Locale.ENGLISH);
		} else {
			return localizedTextMap.entrySet().stream().sorted().findFirst().map(Map.Entry::getValue).orElse(null);
		}
	}

	/**
	 * for a named cli option that allows multiple values, the list of those values (empty list of no value is given or if the
	 * option is not passed at all)
	 */
	private static List<String> getCLIOptionValues(CommandLine cmd, String argname) {
		String[] params = cmd.getOptionValues(argname);
		if (params == null) {
			return List.of();
		}
		return Arrays.asList(params);
	}

	private static <K, V> V getOrThrow(Map<K, V> map, K key, Supplier<RuntimeException> mkException) {
		if (map.containsKey(key)) {
			return map.get(key);
		}
		throw mkException.get();
	}

	private static ModelRef4Include parseExternalModelRef(String arg) {
		Pattern pattern = Pattern.compile(
				"^([^=]*)=([^:]*)(:(\\S*))?$"); // re-compiling the pattern should not matter performance-wise here
		String dmId = null;
		List<String> packagePrefix = null;
		String singleRgNullable = null;
		var matcher = pattern.matcher(arg);
		if (matcher.find()) {
			dmId = matcher.group(1);
			packagePrefix = Arrays.asList(matcher.group(2).split("\\.", -1));
			singleRgNullable = matcher.group(4);
		} else {
			throw new IllegalArgumentException("invalid parameter '" + arg + "'");
		}
		if (StringUtils.isEmpty(dmId)) {
			throw new IllegalArgumentException(
					"dm id in external models parameter must not be empty (got overall parameter " + arg + ")");
		}
		return ModelRef4Include.of(dmId, packagePrefix, singleRgNullable);
	}

	private static <T, K> void validateNoIntersection(Collection<T> a, Collection<T> b, Function<T, K> keyExtractor, String msg) {
		var intersection = setIntersection(
				a.stream().map(keyExtractor).collect(Collectors.toSet()),
				b.stream().map(keyExtractor).collect(Collectors.toSet()));
		if (!intersection.isEmpty()) {
			throw new IllegalArgumentException(msg + intersection);
		}
	}
}
