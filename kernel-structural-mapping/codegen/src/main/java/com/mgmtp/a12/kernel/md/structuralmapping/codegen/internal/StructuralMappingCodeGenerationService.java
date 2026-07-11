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

package com.mgmtp.a12.kernel.md.structuralmapping.codegen.internal;

import static com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig.ProgrammingLanguage.GROOVY;
import static com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig.ProgrammingLanguage.JAVA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.StringEscapeUtils;
import org.stringtemplate.v4.STGroup;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenContext;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BaseCodeGenUtils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.ICodeGenResult;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.IEntryPoints;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.IMergedSingleScriptCodeGenResult;
import com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.ISeparateFilesCodeGenResult;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.Entity;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.model.FieldMapping;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.model.Fold;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.model.MappingBlock;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.model.ResolutionStrategy;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.model.Slice;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.model.StructuralMappingModelForCodegen;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig.ProgrammingLanguage;
import com.mgmtp.a12.kernel.md.model.internal.service.codegen.MdCoreConversionUtil;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true) @Getter
@RequiredArgsConstructor
@StringTemplateGroup("templates")
public class StructuralMappingCodeGenerationService extends STParameterClassBase {
	public record StructuralMappingEntryPoint(List<String> structualMapperVK) implements IEntryPoints {
	}

	private final StructuralMappingModelForCodegen structuralMappingModel;
	private final String packageName;
	private final String className;

	// This is 2^16 - 2 because the end_pc is exclusive and an additional byte at the end is required for exception handling
	// (see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7, CTRL+F "65534")
	private static final int JAVA_MAX_BYTECODES_PER_METHOD = 65534;

	@Override protected Consumer<STGroup> getGroupAdapter() {
		return templateGroup -> {
			templateGroup.registerModelAdaptor(Collection.class,
					PluggableModelAdaptor.create((model, propertyName) -> switch (propertyName) {
						case "size" -> model.size();
						default -> null;
					}));
			templateGroup.registerModelAdaptor(Entity.class,
					PluggableModelAdaptor.create((model, propertyName) -> switch (propertyName) {
						case "fullName" -> javaStringLiteralOrNull(model.fullName());
						default -> null;
					}));
			templateGroup.registerModelAdaptor(ResolutionStrategy.class,
					PluggableModelAdaptor.create((model, propertyName) -> switch (propertyName) {
						case "isSlice" -> model instanceof Slice;
						case "isFold" -> model instanceof Fold;
						default -> null;
					}));
			templateGroup.registerModelAdaptor(MappingBlock.class,
					PluggableModelAdaptor.create((model, propertyName) -> switch (propertyName) {
						case "partitionedFieldMappings" -> partitionList(model.fieldMappings(), JAVA_MAX_BYTECODES_PER_METHOD / 2,
								it -> StructuralMappingCodeGenerationService.guesstimateNumBytecodes(it,
										MdCoreConversionUtil.convertProglangCoreToMd(
												CodeGenContext.getInstance().getProgrammingLanguage())));
						default -> null;
					}));
		};
	}

	/**
	 * Generate Java-Code for the provided {@link StructuralMappingModelForCodegen}. The generated code will contain a class in the
	 * given package and named according to the given simple class name. It will provide the following static methods: <ul>
	 * <li>{@code public static StructuralMapperVK getStructuralMapper()}</li>
	 * <li>{@code public static StructuralMappingModelForCodegen getStructuralMappingModel()}</li>
	 * </ul>
	 *
	 * @param structuralMappingModel
	 * 		the structural mapping model to generate code for. Must be valid. Must not be {@code null}.
	 * @param packageName
	 * 		the package name without a trailing dot (e.g. {@code "com.mgmtp.a12.kernel"}). Must not be {@code null}.
	 * @param simpleClassName
	 * 		the simple class name (e.g. {@code "MyGeneratedStructuralMapping"}). Must not be {@code null}.
	 * @param programmingLanguage
	 * @return valid source code for a single Java class (i.e. including package spec, imports, etc.)
	 */
	public static ICodeGenResult<StructuralMappingEntryPoint> generateCode(
			@NonNull StructuralMappingModelForCodegen structuralMappingModel, @NonNull String packageName,
			@NonNull String simpleClassName, ProgrammingLanguage programmingLanguage) {
		CodeGenContext.initialize(ICodeGeneratorParameters.Product.STRUCTURALMAPPING,
				MdCoreConversionUtil.convertProglangMdToCore(programmingLanguage));
		if (!isValidPackageName(packageName)) {
			throw new IllegalArgumentException("packageName '%s' is not valid".formatted(packageName));
		}
		if (!isValidJavaIdentifier(simpleClassName)) {
			throw new IllegalArgumentException("simpleClassName '%s' is not valid".formatted(simpleClassName));
		}
		return new StructuralMappingCodeGenerationService(structuralMappingModel, packageName, simpleClassName).generateCode(
				programmingLanguage);
	}

	public static boolean isValidPackageName(String packageName) {
		if (packageName.endsWith(".")) {
			return false;
		}
		return Arrays.stream(packageName.split("\\."))
				.allMatch(StructuralMappingCodeGenerationService::isValidJavaIdentifier);
	}

	public static boolean isValidJavaIdentifier(String identifier) {
		if (identifier.isEmpty() || !Character.isJavaIdentifierStart(identifier.charAt(0))) {
			return false;
		}
		for (int i = 1; i < identifier.length(); i++) {
			if (!Character.isJavaIdentifierPart(identifier.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private ICodeGenResult<StructuralMappingEntryPoint> generateCode(ProgrammingLanguage programmingLanguage) {
		String content = toCode();
		List<String> fqn = Stream.concat(Arrays.stream(packageName.split("\\.")), Stream.of("StructuralMapper")).toList();
		switch (programmingLanguage) {
			case JAVA -> {
				return ISeparateFilesCodeGenResult.ofSingleFileUtf8(fqn, StructuralMappingEntryPoint::new, ".java", content);
			}
			case GROOVY -> {
				return IMergedSingleScriptCodeGenResult.of(
						new StructuralMappingEntryPoint(fqn),
						"", // fileHeader not relevant, because smm isn't written to standalone script anyway
						Arrays.stream(toCode("imports").split("\n")).collect(Collectors.toSet()),
						"", // no globals for smm
						List.of(content),
						List.of("placeholderStandaloneSMMSingleScriptName.groovy")
						// smms should not be written to standalone scripts anyway
				);
			}
			default -> throw new IllegalArgumentException(
					"Unexpected programming language for structural mapping code: " + programmingLanguage);
		}
	}

	private static String javaStringLiteralOrNull(String unescapedString) {
		return unescapedString == null
				? "null"
				: "\"" + StringEscapeUtils.escapeJava(unescapedString) + "\"";
	}

	public String getGroovyPackage() {
		return BaseCodeGenUtils.packageAsGroovyClassnamePrefix(packageName);
	}

	public List<List<Entity>> partitionedSourceEntities() {
		List<Entity> sourceEntities = structuralMappingModel.sourceElementsByFullName().values().stream()
				.sorted().toList(); // produce a stable order
		return partitionEntities(sourceEntities);
	}

	public List<List<Entity>> partitionedTargetEntities() {
		List<Entity> targetEntities = structuralMappingModel.targetElementsByFullName().values().stream()
				.sorted().toList(); // produce a stable order
		return partitionEntities(targetEntities);
	}

	private static List<List<Entity>> partitionEntities(List<Entity> entities) {
		return partitionList(entities, JAVA_MAX_BYTECODES_PER_METHOD / 2,
				StructuralMappingCodeGenerationService::guesstimateNumBytecodes);
	}

	public List<MappingBlock> mappingBlocks() {
		return structuralMappingModel.mappingBlocks();
	}

	private static <T> List<List<T>> partitionList(List<T> elements, int maxSizePerPartition, ToIntFunction<T> elementToSizeFunc) {
		if (elements.isEmpty()) {
			return List.of();
		}
		List<List<T>> partitionedList = new ArrayList<>();
		List<T> currentPartition = new ArrayList<>();
		int currentPartitionSize = 0;
		for (T element : elements) {
			int elementSize = elementToSizeFunc.applyAsInt(element);
			if (elementSize > maxSizePerPartition) {
				throw new IllegalArgumentException(
						"Cannot partition elements where a single element exceeds the max partition size");
			}
			if ((currentPartitionSize + elementSize) > maxSizePerPartition) {
				partitionedList.add(currentPartition);
				currentPartition = new ArrayList<>();
				currentPartitionSize = 0;
			}
			currentPartition.add(element);
			currentPartitionSize += elementSize;
		}
		partitionedList.add(currentPartition);
		return partitionedList;
	}

	private static final String FIELD_MAPPING_KEY = "FIELD_MAPPING";
	private static final String FOLD_KEY = "FOLD";
	private static final String SLICE_KEY = "SLICE";
	private static final String OTHER_KEY = "OTHER";
	// The estimated sizes of the individual parts
	private static Map<ProgrammingLanguage, Map<String, Integer>> SIZES = Map.of(
			JAVA, Map.of(FIELD_MAPPING_KEY, 41, FOLD_KEY, 27, SLICE_KEY, 39, OTHER_KEY, 9),
			GROOVY, Map.of(FIELD_MAPPING_KEY, 60, FOLD_KEY, 50, SLICE_KEY, 80, OTHER_KEY, 20)
			// NOTE: the numbers for groovy are not derived in an equally methodical way as in java, because this seems harder
			// for groovy. They are rought estimates.
	);

	/**
	 * We try to give a rough estimate as to how many bytecodes creating the given field mapping might require in the generated Java
	 * code. This does not need to be perfect but should be within 2x of actual (we aim for half the actual limit to leave room for
	 * error).
	 */
	private static int guesstimateNumBytecodes(FieldMapping fieldMapping,
			ProgrammingLanguage programmingLanguage) {
		/* derived from 'javap -p -v':
			1: aload_0				(push list of field mappings to stack)
			3: new					(create uninitialized field mapping)
			1: dup					(push new field mapping to stack again)
			2-3: ldc				(push source field full name to stack; can be ldc_w (3 wide))
			3: invokestatic			(invoke method 's')
			2-3: ldc				(push target field full name to stack; can be ldc_w (3 wide))
			3: invokestatic			(invoke method 't')
			1-3: iconst_n			(push array size to stack; can be bipush/ldc (2 wide) or ldc_w (3 wide) for larger integers)
			3: anewarray			(create new ResolutionStrategy[] array)
			... (bytecode for adding each resolution strategy to the array, see below) ...
			3: invokestatic			(invoke method Arrays::asList)
			2-3: ldc				(push group to clear full name to stack; can be ldc_w (3 wide))
			3: invokestatic			(invoke method 't')
			3: invokespecial		(invoke initializer for field mapping)
			5: invokeinterface		(invoke method List::add)
			1: pop					(remove method result from stack)
		 */
		int size = SIZES.get(programmingLanguage).get(FIELD_MAPPING_KEY);
		for (var resolutionStrategy : fieldMapping.resolutionStrategiesByTargetLevel()) {
			/* derived from 'javap -p -v':
				1: dup					(push array to stack again)
				1-3: iconst_n			(push current array index to stack; can be bipush/ldc (2 wide) or ldc_w (3 wide) for larger integers)
				3: new					(create uninitialized Slice/Fold object)
				1: dup					(push new Slice/Fold to stack again)
				1-3: iconst_n			(push rank to stack; can be bipush/ldc (2 wide) or ldc_w (3 wide) for larger integers)
				2-3: ldc				(push source group full name to stack; can be ldc_w (3 wide))
				3: invokestatic			(invoke method 's')
				2-3: ldc				(push target group full name to stack)
				3: invokestatic			(invoke method 't')
				2-3: ldc				(only for slice: push source field full name to stack; can be ldc_w (3 wide))
				3: invokestatic			(only for slice: invoke method 's')
				2-3: ldc				(only for slice: push target field full name to stack; can be ldc_w (3 wide))
				3: invokestatic			(only for slice: invoke method 't')
				3: invokespecial		(invoke initializer for Slice/Fold)
				1: aastore				(store Slice/Fold in array)
			*/
			if (resolutionStrategy instanceof Slice) {
				size += SIZES.get(programmingLanguage).get(SLICE_KEY);
			} else if (resolutionStrategy instanceof Fold) {
				size += SIZES.get(programmingLanguage).get(FOLD_KEY);
			} else {
				/* derived from 'javap -p -v':
					1: dup				(push array to stack again)
					1-3: iconst_n		(push current array index to stack; can be bipush/ldc (2 wide) or ldc_w (3 wide) for larger integers)
					1: aconst_null		(push null to stack)
					3: checkcast		(cast to ResolutionStrategy)
					1: aastore			(store null in array)
				*/
				size += SIZES.get(programmingLanguage).get(OTHER_KEY);
			}
		}
		return size;
	}

	/**
	 * We try to give a rough estimate as to how many bytecodes creating the given entity might require in the generated Java code.
	 * This does not need to be perfect but should be within 2x of actual (we aim for half the actual limit to leave room for
	 * error).
	 */
	private static int guesstimateNumBytecodes(Entity entity) {
		/* derived from 'javap -p -v':
			1: aload_0			(push builder to stack)
			2-3: ldc         	(push full name to stack; can be ldc_w (3 wide))
			1-3: iconst_n      	(only for groups: push repeatability to stack; can be bipush/ldc (2 wide) or ldc_w (3 wide) for larger integers)
			3: invokevirtual	(invoke method Builder::group or Builder::field)
			1: pop				(pop method result from stack)
		 */
		return entity.isField() ? 8 : 11;
	}
}
