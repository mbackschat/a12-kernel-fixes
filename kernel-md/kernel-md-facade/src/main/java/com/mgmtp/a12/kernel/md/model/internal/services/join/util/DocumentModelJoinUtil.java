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
package com.mgmtp.a12.kernel.md.model.internal.services.join.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType.EnumValue;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.HeaderBuilderUtil;
import com.mgmtp.a12.kernel.md.model.internal.impl.LocalizedTextMapImpl;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.JoinProblem;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization.SerializationUtils;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.ModelReference;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Helper class containing utility methods used during join operations of {@link DocumentModel}s
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DocumentModelJoinUtil {
	public interface AnnotationSourceCallback {
		default void onlyOld(@NonNull String annotationName) {
		}

		default void onlyNew(@NonNull String annotationName) {
		}

		default void both(@NonNull String annotationName) {
		}
	}

	/**
	 * Determines the join of old and new {@link List<Annotation>} and set it to the old element via the given setter.
	 *
	 * @param oldAnnotations
	 * 		the old {@link List<Annotation>}
	 * @param newAnnotations
	 * 		the new {@link List<Annotation>}
	 * @param annotationsSetter
	 * 		the setter for the joined {@link List<Annotation>}
	 * @param inconsistentAnnotationsHandler
	 *        {@link Runnable} as handler for inconsistent annotations
	 * @param problemReporter
	 * 		the reporter for when there are problems during joining
	 */
	public static void determineAndSetJoinOfAnnotations(List<Annotation> oldAnnotations, List<Annotation> newAnnotations,
			Consumer<List<Annotation>> annotationsSetter, Runnable inconsistentAnnotationsHandler,
			IProblemReporter problemReporter, AnnotationSourceCallback annotationSourceCallback) {
		boolean oldAnnotationsProvided = (oldAnnotations != null) && !oldAnnotations.isEmpty();
		boolean newAnnotationsProvided = (newAnnotations != null) && !newAnnotations.isEmpty();

		// remove unjoinable annotations
		if (newAnnotationsProvided) {
			// report a warning for each existing type of unjoinable annotations
			for (UnjoinableAnnotations ua : UnjoinableAnnotations.values()) {
				if (newAnnotations.stream().anyMatch(a -> a.getName().equals(ua.annotationName()))) {
					problemReporter.reportProblem(JoinProblem.createWarning(newAnnotations, ua.ignoreMessage()));
				}
			}

			newAnnotations.removeIf(a -> UnjoinableAnnotations.annotationNames().anyMatch(ua -> ua.equals(a.getName())));
			newAnnotationsProvided = !newAnnotations.isEmpty();
		}

		if (newAnnotationsProvided) {
			if (!oldAnnotationsProvided) {
				annotationsSetter.accept(newAnnotations);
				newAnnotations.forEach(a -> annotationSourceCallback.onlyNew(a.getName()));
			} else {
				List<Annotation> joinedAnnotations = new ArrayList<>();
				Map<Annotation, Boolean> newAnnotationsToAdd = newAnnotations.stream()
						.collect(Collectors.toMap(
								Function.identity(),
								a -> Boolean.FALSE, // is inconsistent
								(x, y) -> y, LinkedHashMap::new
						));
				for (Annotation oldAnnotationInfo : oldAnnotations) {
					Optional<Annotation> matchingNewAnnotationInfo = newAnnotations.stream()
							.filter(ai -> ai.getName().equals(oldAnnotationInfo.getName())).findFirst();

					if (matchingNewAnnotationInfo.isPresent()) {
						if (annotationValuesDontMatch(oldAnnotationInfo, matchingNewAnnotationInfo.get())) {
							// in this case we will add the annotation twice in the resulting element
							newAnnotationsToAdd.replace(matchingNewAnnotationInfo.get(), Boolean.TRUE);
						} else {
							newAnnotationsToAdd.remove(matchingNewAnnotationInfo.get());
						}

						annotationSourceCallback.both(oldAnnotationInfo.getName());
					} else {
						annotationSourceCallback.onlyOld(oldAnnotationInfo.getName());
					}
					joinedAnnotations.add(oldAnnotationInfo);
				}
				if (newAnnotationsToAdd.values().stream().anyMatch(v -> v)) {
					inconsistentAnnotationsHandler.run();
				}

				// Add all not already handled new annotations
				joinedAnnotations.addAll(newAnnotationsToAdd.keySet());
				newAnnotationsToAdd.keySet().forEach(a -> annotationSourceCallback.onlyNew(a.getName()));

				annotationsSetter.accept(joinedAnnotations);
			}
		} else {
			if (oldAnnotationsProvided) {
				oldAnnotations.forEach(a -> annotationSourceCallback.onlyOld(a.getName()));
			}
			// Nothing else to do as no new annotations provided
		}
	}

	private static boolean annotationValuesDontMatch(Annotation oldAnnotationInfo, Annotation matchingNewAnnotationInfo) {
		return !Objects.equals(matchingNewAnnotationInfo.getValue(), oldAnnotationInfo.getValue());
	}

	/**
	 * Determines the optional join of old and new {@link EnumValue} if preconditions are fulfilled.
	 *
	 * @param oldField
	 * 		the old {@link Field}
	 * @param newField
	 * 		the new {@link Field}
	 * @return the joined {@link EnumValue} or {@link Optional#empty()}
	 */
	public static Optional<List<EnumValue>> determineOptionalJoinOfEnumerationValues(Field oldField,
			Field newField) {
		EnumerationType oldEnumerationFieldType = tryCastFieldTypeToEnumeration(oldField.getFieldType());
		EnumerationType newEnumerationFieldType = tryCastFieldTypeToEnumeration(newField.getFieldType());

		if (oldEnumerationFieldType == null || newEnumerationFieldType == null) {
			// At least one of the field types is not enumeration -> no join
			return Optional.empty();
		}

		List<EnumValue> oldEnumValues = oldEnumerationFieldType.getValues();
		List<EnumValue> newEnumValues = newEnumerationFieldType.getValues();

		Set<Locale> oldLocaleSet = oldEnumValues.stream().map(EnumValue::getLabel).map(ILocalizedTextMap::keySet)
				.flatMap(Set::stream).collect(Collectors.toSet());
		Set<Locale> newLocaleSet = newEnumValues.stream().map(EnumValue::getLabel).map(ILocalizedTextMap::keySet)
				.flatMap(Set::stream).collect(Collectors.toSet());
		if (!oldLocaleSet.containsAll(newLocaleSet) || !newLocaleSet.containsAll(oldLocaleSet)) {
			// The supported languages do not exactly match -> no join
			return Optional.empty();
		}

		if (containsOnlyInOneList(oldEnumValues, newEnumValues, (a, b) -> a.getValue().equals(b.getValue()))) {
			return Optional.empty();
		}

		List<EnumValue> joinedEnumValues = new ArrayList<>();
		for (EnumValue oldEnumValue : oldEnumValues) {
			// List entry must exist due to check before
			ILocalizedTextMap joinedLocaleMap = new LocalizedTextMapImpl();
			for (Locale locale : oldEnumValue.getLabel().keySet()) {
				String newDisplayValue = newEnumValues.stream()
						.filter(nev -> nev.getValue().equals(oldEnumValue.getValue()))
						.filter(nev -> nev.getLabel().containsKey(locale))
						.findFirst()
						.orElseThrow()
						.getLabel()
						.get(locale);
				joinedLocaleMap.put(locale, newDisplayValue);
			}
			joinedEnumValues.add(EnumValue.builder().value(oldEnumValue.getValue()).label(joinedLocaleMap).build());
		}

		return Optional.of(joinedEnumValues);
	}

	public static EnumerationType tryCastFieldTypeToEnumeration(FieldType fieldtype) {
		return (fieldtype instanceof EnumerationType ftEnum) ? ftEnum : null;
	}

	private static <T> boolean containsOnlyInOneList(List<T> listOne, List<T> listTwo,
			BiPredicate<T, T> listEntryComparator) {
		boolean someEntryOnlyInListOne = listOne.stream().anyMatch(listOneEntry ->
				listTwo.stream().noneMatch(listTwoEntry -> listEntryComparator.test(listOneEntry, listTwoEntry)));
		boolean someEntryOnlyInListTwo = listTwo.stream().anyMatch(listTwoEntry ->
				listOne.stream().noneMatch(listOneEntry -> listEntryComparator.test(listTwoEntry, listOneEntry)));
		return someEntryOnlyInListOne || someEntryOnlyInListTwo;
	}

	public static void mergeModelReferences(DocumentModel resultModel, Header header) {
		List<ModelReference> resultModelReferences = resultModel.getHeader().getModelReferences();
		List<ModelReference> joinModelReferences = header.getModelReferences();
		if ((resultModelReferences == null || resultModelReferences.isEmpty())
				&& (joinModelReferences == null || joinModelReferences.isEmpty())) {
			return;
		}
		Set<String> existingAliases = Stream.ofNullable(resultModelReferences)
				.flatMap(Collection::stream)
				.map(ModelReference::getAlias)
				.collect(Collectors.toSet());
		List<ModelReference> additionalModelReferences = Stream.ofNullable(joinModelReferences)
				.flatMap(Collection::stream)
				.filter(mr -> !existingAliases.contains(mr.getAlias()))
				.map(SerializationUtils::copy)
				.toList();
		if (!additionalModelReferences.isEmpty()) {
			resultModel.setHeader(HeaderBuilderUtil.toBuilder(resultModel.getHeader())
					.withModelReferences(Stream.concat(
							Stream.ofNullable(resultModelReferences).flatMap(Collection::stream),
							additionalModelReferences.stream()).toList())
					.build());
		}
	}

	public static String getModelString(DocumentModel documentModel) {
		var modelInfo = documentModel.getContent().getModelInfo();
		var branch = modelInfo.getBranch();
		if (branch.isPresent()) {
			boolean nameContainsBranch = modelInfo.getName().endsWith("_" + branch.get());
			return String.format("%s/%s%s",
					nameContainsBranch
							? modelInfo.getName()
							  .substring(0, (modelInfo.getName().length() - (branch.get().length() + 1)))
							: modelInfo.getName(),
					branch.get(),
					modelInfo.getRevision().map(r -> String.format("/%s", r)).orElse(""));
		} else {
			return modelInfo.getName();
		}
	}
}
