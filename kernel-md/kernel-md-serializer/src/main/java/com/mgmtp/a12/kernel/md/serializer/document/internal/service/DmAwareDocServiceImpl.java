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

package com.mgmtp.a12.kernel.md.serializer.document.internal.service;

import static com.mgmtp.a12.kernel.md.model.internal.impl.RankedNotificationImpl.error;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.RepetitionsV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDmAwareDocService;
import com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelResolverUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.NonRepeatableEntitiesFinder;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.TransientFieldsFinder;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.TransientFieldsInGroupsFinder;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.convertion.FieldValueConverter;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;
import lombok.NonNull;

public class DmAwareDocServiceImpl implements IDmAwareDocService {
	private final IDocumentModel documentModel;
	private final IDocumentModelSearchService documentModelSearchService;

	/**
	 * the caller must guarantee that the searchService is for the documentModel and not for some other DM
	 */
	DmAwareDocServiceImpl(IDocumentModel documentModel, IDocumentModelSearchService searchService) {
		this.documentModel = documentModel;
		this.documentModelSearchService = searchService;
	}

	public static DmAwareDocServiceImpl ofResolver(String dmId, IDocumentModelResolver documentModelResolver) {
		return new DmAwareDocServiceImpl(
				documentModelResolver.getDocumentModelById(dmId),
				DocumentModelResolverUtils.getOrCreateSearchService(documentModelResolver, dmId)
		);
	}

	@Override public Object convertToJavaTypeV2(@NonNull String fieldFullName, String value,
			@NonNull Consumer<RankedNotification> notificationReceiver) {
		FieldValueConverter.ConversionResult result = FieldValueConverter.convertToJavaTypeV2(fieldFullName, value,
				DmAndSearchService.of(documentModel, documentModelSearchService),
				notificationReceiver);
		return result.success() ? result.convertedValueNullable() : null;
	}

	@Override public Collection<Map.Entry<DocumentPointer, GroupInstanceV2>> getGroupInstancesById(@NonNull DocumentV2 document,
			@NonNull String groupId) {
		return documentModelSearchService.getGroupOrFieldPathById(groupId).map(
						path -> DocumentV2Utils.getGroupInstances(document, DocumentMultiPointer.ofPathWithAllWildcards(path)))
				.orElse(List.of());
	}

	@Override public Collection<Map.Entry<DocumentPointer, FieldInstanceV2>> getFieldInstancesById(@NonNull DocumentV2 document,
			@NonNull String fieldId) {
		return documentModelSearchService.getGroupOrFieldPathById(fieldId).map(
						path -> DocumentV2Utils.getFieldInstances(document, DocumentMultiPointer.ofPathWithAllWildcards(path)))
				.orElse(List.of());
	}

	@Override public boolean checkStructuralConsistencyWithDm(@NonNull DocumentV2 document,
			@NonNull Consumer<RankedNotification> notificationReceiver) {
		// has to be this weird, as we need to adapt it in the visitor
		AtomicBoolean result = new AtomicBoolean(true);
		Consumer<RankedNotification> notification = notificationReceiver.andThen(n -> {
			if (n.getSeverity().isAtLeastAsSevereAs(Severity.ERROR)) {
				result.set(false);
			}
		});

		IDocumentV2Visitor visitor = new IDocumentV2Visitor() {
			@Override public DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {
				DescendType descendType = IDocumentV2Visitor.super.visitGroup(pointerRelativeToBase, group);

				// root pointers are not explicitly included
				if (pointerRelativeToBase.equals(DocumentPointer.of(""))) {
					return descendType;
				}

				// <li>If entity instances ({@link GroupInstanceV2}s in the {@link DocumentV2} are unknown regarding the {@link com.mgmtp.a12.kernel.md.model.api.IDocumentModel}.</li>
				// <li>If a {@link GroupInstanceV2} is present at the path of a {@link IField}.</li>
				// <li>If anything is present at the path of a {@link com.mgmtp.a12.kernel.md.model.api.IRule} or {@link com.mgmtp.a12.kernel.md.model.api.IComputation}.</li>
				Optional<IElement> maybeElement = documentModelSearchService.getByPath(pointerRelativeToBase.fullName());
				if (!(maybeElement.orElse(null) instanceof IGroup)) {
					notification.accept(error(
							String.format("Group '%s' was not defined in the document model.", pointerRelativeToBase)));
					// does not make any sense to traverse further
					return DescendType.SKIP_CHILDREN;
				}

				return descendType;
			}

			@Override public RepetitionDescendType visitRepetitions(DocumentPointer pointerRelativeToBase, RepetitionsV2 rep) {
				RepetitionDescendType descendType = IDocumentV2Visitor.super.visitRepetitions(pointerRelativeToBase, rep);

				Optional<IElement> maybeElement = documentModelSearchService.getByPath(pointerRelativeToBase.fullName());
				if (maybeElement.isEmpty()) {
					// if the group does not exist at all, then we don't traverse further
					// we already checked in visitGroup if the group exists at all
					return descendType;
				}

				// <li>If the number of repetitions of a group is larger than the defined repeatability in the DM.</li>
				IElement element = maybeElement.get();
				if (element instanceof IGroup groupElement) {
					if (groupElement.getRepeatability() < rep.size()) {
						notification.accept(error(
								String.format("Max repetition exceeded: %s (got %s but max repetition is %d)",
										pointerRelativeToBase,
										rep.size(),
										groupElement.getRepeatability())));
						return RepetitionDescendType.VISIT_CHILDREN;
					}
				}

				return descendType;
			}

			@Override public void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 field) {
				IDocumentV2Visitor.super.visitField(pointerRelativeToBase, field);

				// <li>If entity instances {@link FieldInstanceV2}s) in the {@link DocumentV2} are unknown regarding the {@link com.mgmtp.a12.kernel.md.model.api.IDocumentModel}.</li>
				// <li>If a {@link FieldInstanceV2} is present at the path of a {@link IGroup}.</li>
				// <li>If anything is present at the path of a {@link com.mgmtp.a12.kernel.md.model.api.IRule} or {@link com.mgmtp.a12.kernel.md.model.api.IComputation}.</li>
				Optional<IElement> maybeElement = documentModelSearchService.getByPath(pointerRelativeToBase.fullName());
				if (!(maybeElement.orElse(null) instanceof IField)) {
					notification.accept(error(
							String.format("Field '%s' was not defined in the document model.", pointerRelativeToBase)));
				}
			}
		};

		document.traverse(visitor);
		return result.get();
	}

	@Override public DocumentV2 removeTransientFields(@NonNull DocumentV2 document) {
		TransientFieldsFinder tff = new TransientFieldsFinder();
		new DocumentModelWalker().acceptDocumentModel(documentModel, tff);
		if (tff.getAllTransientFieldPaths().isEmpty()) {
			return document;
		}

		return (DocumentV2) removeTransientFieldsInternal(DocumentPointer.of(""), document,
				tff.getAllTransientFieldPaths().stream().map(DocumentMultiPointer::ofPathWithAllWildcards)
						.collect(Collectors.toSet()));
	}

	@Override public DocumentV2 removeUnknowns(@NonNull DocumentV2 document) {
		return (DocumentV2) removeUnknownsInternal(DocumentPointer.of(""), document, _n -> {
		});
	}

	public record Unknown(@NonNull String unknownFullName, boolean isField) {
		public static Unknown field(@NonNull String unknownFullName) {
			return new Unknown(unknownFullName, true);
		}

		public static Unknown group(@NonNull String unknownFullName) {
			return new Unknown(unknownFullName, false);
		}

		@Override public @NonNull String toString() {
			return String.format("%s '%s'", this.isField() ? "Field" : "Group", this.unknownFullName);
		}
	}

	public record DocumentAndUnknowns(@NonNull DocumentV2 document, @NonNull List<Unknown> unknowns) {
	}

	public DocumentAndUnknowns removeAndReturnUnknowns(@NonNull DocumentV2 document) {
		List<Unknown> unknowns = new ArrayList<>();
		DocumentV2 documentV2 = (DocumentV2) removeUnknownsInternal(DocumentPointer.of(""), document, unknowns::add);

		return new DocumentAndUnknowns(documentV2, unknowns);
	}

	// [A12K-3432]: use batchUpdate
	static GroupInstanceV2 removeTransientFieldsInternal(DocumentPointer pointer, GroupInstanceV2 group,
			Set<DocumentMultiPointer> transientFieldPaths) {
		// NOTE: wenn wir kein batch-update haben, könnten wir hier filtern
		//		transientFieldPaths.stream().filter(
		//				filter mit contains
		//		);

		for (var field : group.directFields()) {
			DocumentPointer fieldPointer = pointer.withAppended(field.getKey(), 1);
			for (var dp : transientFieldPaths) {
				if (dp.contains(fieldPointer)) {
					group = group.withDirectFieldRemoved(field.getKey());
					// can only happen once
					break;
				}
			}
		}

		for (var subGroup : group.directSubgroups()) {
			var subGroupRepetitionsPointer = pointer.withAppended(subGroup.getKey(), 0);
			RepetitionsV2 repetitions = removeTransientFieldsInternal(subGroupRepetitionsPointer, subGroup.getValue(),
					transientFieldPaths);
			group = group.withDirectSubgroup(subGroup.getKey(), repetitions);
		}

		return group;
	}

	static RepetitionsV2 removeTransientFieldsInternal(DocumentPointer pointer, RepetitionsV2 repetitions,
			Set<DocumentMultiPointer> transientFieldPaths) {
		for (int iOneBased = 1; iOneBased <= repetitions.size(); iOneBased++) {
			var subGroup = repetitions.get(iOneBased);
			subGroup = removeTransientFieldsInternal(
					pointer.withLastRepetitionReplaced(iOneBased),
					subGroup,
					transientFieldPaths);

			repetitions = repetitions.withReplacedAt(iOneBased, subGroup);
		}

		return repetitions;
	}

	GroupInstanceV2 removeUnknownsInternal(DocumentPointer pointerFromBaseToGroup, GroupInstanceV2 group,
			Consumer<Unknown> unknowns) {
		for (var fieldEntry : group.directFields()) {
			String fieldShortName = fieldEntry.getKey();
			String fullName = ElementPathUtils.createPath(pointerFromBaseToGroup.fullName(), fieldShortName);
			Optional<IElement> elementOptional = documentModelSearchService.getByPath(fullName);
			if (elementOptional.isEmpty() || !(elementOptional.get() instanceof IField)) {
				group = group.withDirectFieldRemoved(fieldShortName);
				unknowns.accept(Unknown.field(fullName));
			}
		}

		for (var subGroupEntry : group.directSubgroups()) {
			String subGroupShortName = subGroupEntry.getKey();
			RepetitionsV2 subGroupRepetitions = subGroupEntry.getValue();
			String fullName = ElementPathUtils.createPath(pointerFromBaseToGroup.fullName(), subGroupShortName);
			Optional<IElement> elementOptional = documentModelSearchService.getByPath(fullName);
			if (elementOptional.isEmpty() || !(elementOptional.get() instanceof IGroup)) {
				group = group.withDirectSubgroupAllRepetitionsRemoved(subGroupShortName);
				unknowns.accept(Unknown.group(fullName));
			} else {
				var repetitions = removeUnknownsInternal(
						pointerFromBaseToGroup.withAppended(subGroupShortName, DocumentMultiPointer.WILDCARD),
						subGroupRepetitions, unknowns);
				group = group.withDirectSubgroup(subGroupShortName, repetitions);
			}
		}

		return group;
	}

	RepetitionsV2 removeUnknownsInternal(DocumentPointer pointerFromBaseToRepetitions,
			RepetitionsV2 repetitions, Consumer<Unknown> unknowns) {
		for (int iOneBased = repetitions.size(); iOneBased > 0; iOneBased--) {
			var subGroup = repetitions.get(iOneBased);
			subGroup = removeUnknownsInternal(pointerFromBaseToRepetitions.withLastRepetitionReplaced(iOneBased),
					subGroup, unknowns);
			repetitions = repetitions.withReplacedAt(iOneBased, subGroup);
		}

		return repetitions;
	}
}
