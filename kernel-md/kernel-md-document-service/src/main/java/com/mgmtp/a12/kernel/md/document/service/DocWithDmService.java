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

package com.mgmtp.a12.kernel.md.document.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelSearchServiceImpl;
import com.mgmtp.a12.kernel.md.model.internal.service.ExternalDocumentModelService;
import com.mgmtp.a12.model.utils.Experimental;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Provides util methods that need both a document and a (corresponding) document model.
 * <p>
 * This is an experimental implementation that may or may not be promoted to stable in a later release.
 */
// Experimental impl, based on the request for annotations-based lookup and set of field instance values, see MGM Wiki Shortlink x/er-7F
@Experimental
@OnlyForUsage
public class DocWithDmService {
	private final IDocumentModel dm;
	private final IDocumentModelService dmService; // we keep an instance here so that it can (potentially) cache dm-based info across calls
	private final IDocumentModelSearchService dmSearchService; // we keep an instance here so that it can (potentially) cache dm-based info across calls

	public DocWithDmService(final IDocumentModel dm) {
		this.dm = dm;
		this.dmService = new ExternalDocumentModelService();
		this.dmSearchService = new DocumentModelSearchServiceImpl(dm);
	}

	/**
	 * Finds all non-null field values from the given document for all fields that are annotated with annotationKey
	 * (irrespective of annotationValue)
	 */
	@Experimental
	public Collection<Pair<DocumentPointer, Object>> getFieldInstanceValuesByAnnotation(
			DocumentV2 document,
			String annotationKey
	) {
		return getFieldInstanceValuesByAnnotation(document, annotationKey, null);
	}

	/**
	 * Finds all non-null field values from the given document for all fields that are annotated with
	 * annotationKey and annotationValue
	 */
	@Experimental
	public Collection<Pair<DocumentPointer, Object>> getFieldInstanceValuesByAnnotation(
			DocumentV2 document,
			String annotationKey,
			String annotationValue
	) {
		return getFieldFullNamesByAnnotation(annotationKey, annotationValue).stream()
				.flatMap(fieldFullName -> getNonNullFieldValues(document, fieldFullName).stream())
				.toList();
	}

	/**
	 * For all fields that are annotated with annotationKey and (if annotationValue is not null) annotationValue,
	 * and for all corresponding field instances that are contained in non-repeatable groups or below existing repetitions of
	 * repeatable groups, the field value is set to the value provided by the getFieldValue function.
	 *
	 * @return the updated document
	 */
	@Experimental
	public DocumentV2 setFieldValuesByAnnotation(
			DocumentV2 document,
			String annotationKey,
			String annotationValue,
			Function<DocumentPointer, Object> getFieldValue
	) {
		List<UpdateAction> updates =
				getFieldFullNamesByAnnotation(annotationKey, annotationValue).stream()
						.flatMap(fieldFullName -> allPresentOrNonRepeatableInstances(document, fieldFullName).stream())
						.map(ptr -> UpdateAction.putFieldValue(ptr, getFieldValue.apply(ptr)))
						.toList();
		return document.withBatchUpdates(updates);
	}

	private List<String> getFieldFullNamesByAnnotation(String annotationKey, String annotationValue) {
		// Note: any caching should probably be done by dmService and dmSearchService -> don't duplicate it here
		return dmService.getAllAnnotatedElements(dm, annotationKey).entrySet().stream()
				.filter(e -> annotationValue == null || e.getValue().equals(annotationValue))
				.map(Map.Entry::getKey)
				.filter(fullName -> dmSearchService.getByPath(fullName).orElse(null) instanceof IField).toList();
	}

	private List<DocumentPointer> allPresentOrNonRepeatableInstances(DocumentV2 doc, String fieldFullName) {
		assert fieldFullName.startsWith(
				"/") : fieldFullName; // assertion should be enough because fieldFullName should only originate from dm service, which returns correct names
		List<String> pathParts = Arrays.asList(fieldFullName.substring(1).split("/"));
		DocumentPointer currentGroupPtr = DocumentPointer.of(List.of());
		return allPresentOrNonRepeatableInstancesInternal(doc, currentGroupPtr, pathParts);
	}

	private List<DocumentPointer> allPresentOrNonRepeatableInstancesInternal(
			GroupInstanceV2 groupInstance,
			DocumentPointer currentGroupPtr,
			List<String> fieldFullnameParts
	) {
		if (fieldFullnameParts.size() == 1) {
			assert currentGroupPtr.size() != 0;
			String fieldName = fieldFullnameParts.get(0);
			return List.of(currentGroupPtr.withAppended(fieldName, 1));
		}
		assert fieldFullnameParts.size() > 1;
		String subGroupName = fieldFullnameParts.get(0);
		RepetitionsV2 subGroupReps = groupInstance.directSubgroup(subGroupName);
		if (subGroupReps.size() > 0) { // subgroup is present
			List<DocumentPointer> results = new ArrayList<>();
			for (int iOneBased = 1; iOneBased <= subGroupReps.size(); iOneBased++) {
				results.addAll(allPresentOrNonRepeatableInstancesInternal(
						groupInstance.directSubgroup(subGroupName, iOneBased),
						currentGroupPtr.withAppended(subGroupName, iOneBased),
						fieldFullnameParts.subList(1, fieldFullnameParts.size())));
			}
			return results;
		} else { // subgroup is not present -> if it is repeatable we skip, if it is non-repeatable we pretend that it was present
			String subGroupFullname = currentGroupPtr.fullName() + "/" + subGroupName;
			IGroup groupElem = (IGroup) dmSearchService.getByPath(subGroupFullname).orElseThrow();
			if (groupElem.getRepeatability() == 1) {
				// the group in non-repeatable, so even though it is not currently present, we'll continue as if it was
				return allPresentOrNonRepeatableInstancesInternal(
						GroupInstanceV2.empty(),
						currentGroupPtr.withAppended(subGroupName, 1),
						fieldFullnameParts.subList(1, fieldFullnameParts.size()));
			}
			// nothing to do if the absent group is repeatable
			return List.of();
		}
	}

	private static List<Pair<DocumentPointer, Object>> getNonNullFieldValues(DocumentV2 document, String fieldFullName) {
		assert fieldFullName.startsWith(
				"/"); // assertion-only, because the fieldFullName is from dmSearchService and should be guaranteed to have the correct format (unless we have some integration error)
		assert !"/".equals(fieldFullName);
		List<String> pathParts = Arrays.asList(fieldFullName.substring(1).split("/"));
		return getNonNullFieldValuesInternal(DocumentPointer.of(List.of()), document, pathParts);
	}

	private static List<Pair<DocumentPointer, Object>> getNonNullFieldValuesInternal(
			DocumentPointer pointerPrefix,
			GroupInstanceV2 group,
			List<String> pathParts
	) {
		if (pathParts.size() == 1) {
			String fieldName = pathParts.get(0);
			Object fieldValue = group.directFieldValue(fieldName);
			return fieldValue == null
					? Collections.emptyList()
					: List.of(Pair.of(pointerPrefix.withAppended(fieldName, 1), fieldValue));
		} else {
			String subGroupName = pathParts.get(0);
			List<Pair<DocumentPointer, Object>> results = new ArrayList<>();
			RepetitionsV2 subgroupRepetitions = group.directSubgroup(subGroupName);
			for (int iOneBased = 1; iOneBased <= subgroupRepetitions.size(); iOneBased++) {
				results.addAll(getNonNullFieldValuesInternal(
						pointerPrefix.withAppended(subGroupName, iOneBased),
						subgroupRepetitions.get(iOneBased),
						pathParts.subList(1, pathParts.size())
				));
			}
			return results;
		}
	}
}
