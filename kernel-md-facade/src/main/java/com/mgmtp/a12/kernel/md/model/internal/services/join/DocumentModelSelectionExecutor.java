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
package com.mgmtp.a12.kernel.md.model.internal.services.join;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.ExpansionInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdge;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdgeLabel;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionNode;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.LocalCoordinates;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DMWithExpansionInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.expansioninfo.ExpansionInfoUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.join.selection.SelectionModelV2Wrapper;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelVisitorUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;

import lombok.Getter;
import lombok.NonNull;

/**
 * Executor class to join a selection.
 */
public class DocumentModelSelectionExecutor {
	private final DocumentModel referenceModel;
	private final Optional<ExpansionInfo> referenceModelExpansionInfo;
	private final DocumentModel resultModelMutatedInPlace;
	private final SelectionModelV2Wrapper selection;
	private final JoiningProblemReporter problemReporter;
	private final DocumentModelVisitorUtil documentModelVisitorUtil;
	private final Set<ExpansionNode> origins = new HashSet<>();

	public DocumentModelSelectionExecutor(
			DMWithExpansionInfo referenceModel,
			SelectionModelV2Wrapper selection,
			JoiningProblemReporter problemReporter) {
		this.referenceModel = DocumentModelCopyService.copy(referenceModel.dm());
		this.resultModelMutatedInPlace = DocumentModelCopyService.copy(this.referenceModel);
		this.referenceModelExpansionInfo = referenceModel.ei();
		this.selection = selection;
		this.problemReporter = problemReporter;
		this.documentModelVisitorUtil = new DocumentModelVisitorUtil(resultModelMutatedInPlace);
	}

	public DMWithExpansionInfo select(int stepNumber) {
		DocumentModelWalker modelWalker = new DocumentModelWalker();
		ElementsSelectorVisitor elementsSelector = new ElementsSelectorVisitor();
		modelWalker.acceptDocumentModel(referenceModel, elementsSelector);

		// we allow incomplete/invalid rules and computations, the final validation has to be done after by the user
		removeEmptyGroups();

		// ignore missing references as those might be added later
		DocumentModelUtils.resolveAndUpdateReferences(resultModelMutatedInPlace, p -> {
		});

		// modelReferences, typedefs, and uniqueness-criterion origins are forwarded unchanged.
		// DUC fieldFullNames may reference a removed field — caught later by consistencyCheck.
		referenceModelExpansionInfo.ifPresent(ei -> origins.addAll(
				ExpansionInfoUtils.forwardOrigins(ei,
						coords -> coords instanceof LocalCoordinates.ModelReference
								|| coords instanceof LocalCoordinates.Typedef
								|| coords instanceof LocalCoordinates.DocumentUniquenessCriterion
								|| (coords instanceof LocalCoordinates.Annotation acords
								    && acords.annotatedElementId().equals(LocalCoordinates.Annotation.MODEL_ANNOTATION)),
						ExpansionEdgeLabel.JoinInternal.of(),
						ei.localModelId()
				)));

		asModelItself(referenceModel.getHeader().getId(), selection.getSelectionModelId(), stepNumber,
				referenceModelExpansionInfo).ifPresent(origins::add);

		Optional<ExpansionInfo> expansionInfo = referenceModelExpansionInfo.map(
				ei -> ExpansionInfo.of(ei.localModelId(), ExpansionNode.merge(origins)));

		return problemReporter.hasCriticalErrorOccurred() ?
				DMWithExpansionInfo.of(null) :
				DMWithExpansionInfo.of(resultModelMutatedInPlace, expansionInfo);
	}

	static Optional<ExpansionNode> asModelItself(String resultModelId, @NonNull String selectionModelId, int stepNumber,
			Optional<ExpansionInfo> referenceModelExpansionInfo) {
		return referenceModelExpansionInfo.map(ei -> ExpansionNode.of(LocalCoordinates.ModelItself.of(),
				resultModelId,
				ExpansionEdge.of(ExpansionEdgeLabel.JoinInternal.of(), ei.getOrigin(LocalCoordinates.ModelItself.of())),
				ExpansionEdge.of(ExpansionEdgeLabel.Cm.Selection.of(stepNumber),
						ExpansionNode.of(LocalCoordinates.ModelItself.of(), selectionModelId)
				)
		));
	}

	private void removeEmptyGroups() {
		DocumentModel resultModelCopy = DocumentModelCopyService.copy(this.resultModelMutatedInPlace);
		Map<String, Group> emptyGroupMap = new HashMap<>();
		checkEmptyGroups(resultModelCopy.getContent().getModelRoot().getElements(), emptyGroupMap);
		DocumentModelWalker modelWalker = new DocumentModelWalker();
		EmptyGroupRemoverVisitor emptyGroupRemoverVisitor = new EmptyGroupRemoverVisitor(emptyGroupMap.keySet());
		modelWalker.acceptDocumentModel(resultModelCopy, emptyGroupRemoverVisitor);
	}

	private void checkEmptyGroups(List<Element> elements, Map<String, Group> emptyGroupMap) {
		List<Group> groups = elements.stream().filter(Group.class::isInstance).map(Group.class::cast).toList();
		for (Group group : groups) {
			checkEmptyGroups(group.getElements(), emptyGroupMap);

			if (group.getElements().isEmpty()) {
				emptyGroupMap.put(ElementUtils.getPath(group), group);
				continue;
			}

			List<Group> childGroups = group.getElements().stream().filter(Group.class::isInstance).map(Group.class::cast).toList();
			if (group.getElements().size() == childGroups.size() &&
					childGroups.stream().allMatch(g -> emptyGroupMap.containsKey(ElementUtils.getPath(g)))) {
				emptyGroupMap.put(ElementUtils.getPath(group), group);
			}
		}
	}

	private void addOriginalElementAndAnnotations(@NonNull Element element) {
		referenceModelExpansionInfo.ifPresent(ei -> {
			origins.add(ExpansionNode.of(LocalCoordinates.Element.of(element.getId()),
					ei.localModelId(),
					ExpansionEdge.of(
							ExpansionEdgeLabel.JoinInternal.of(),
							ei.getOrigin(LocalCoordinates.Element.of(element.getId()))
					)));
			ei.allAnnotationsForElementIdOrigins(element.getId()).forEach((annotationName, origin) ->
					origins.add(ExpansionNode.of(LocalCoordinates.Annotation.of(annotationName, element.getId()),
							ei.localModelId(),
							ExpansionEdge.of(ExpansionEdgeLabel.JoinInternal.of(), origin)))
			);
		});
	}

	/**
	 * {@link DocumentModelVisitor} implementation to handle selection of fields and rules of root model.
	 */
	private class ElementsSelectorVisitor extends DocumentModelVisitor {
		// we usually have only a single attachment but can also handle layered combinations
		private int attachmentLayerCounter = 0;

		@Override public void enterGroup(Group group) {
			if (isAttachmentGroup(group)) {
				// do not touch anything of this group and subelements
				// -> use the ExpansionInfo
				attachmentLayerCounter++;
			}
		}

		@Override public void leaveGroup(Group group) {
			if (isAttachmentGroup(group)) {
				attachmentLayerCounter--;
			}
		}

		private static boolean isAttachmentGroup(Group group) {
			return group.getUsageType().map(ElementUtils.A12_ATTACHMENT::equals).orElse(false);
		}

		@Override
		public VisitProcess visitField(Field field) {
			if (!selection.isSelected(field)) {
				documentModelVisitorUtil.removeElementFromResultModel(field);

				//remove sort and index field if necessary
				String fieldPath = ElementUtils.getPath(field);
				Group parentGroup = documentModelVisitorUtil.getResultParentGroup(fieldPath);
				parentGroup
						.getIndexField()
						.filter(indexField -> indexField.getRelativePath().equals(field.getName()))
						.ifPresent(indexField -> parentGroup.setIndexField(null));
				Optional<Group.SortField> optSortField = parentGroup.getSortFields().stream()
						.filter(sortField -> ElementUtils.resolveRelativePath(parentGroup, sortField.getField().getRelativePath())
								.equals(ElementUtils.getPath(field)))
						.findFirst();
				optSortField.ifPresent(sortField -> parentGroup.getSortFields().remove(sortField));
			} else {
				addOriginalElementAndAnnotations(field);
			}

			return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
		}

		@Override
		public VisitProcess visitRule(Rule rule) {
			if (attachmentLayerCounter == 0 && !selection.isSelected(rule)) {
				documentModelVisitorUtil.removeElementFromResultModel(rule);
			} else {
				addOriginalElementAndAnnotations(rule);
			}

			return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
		}

		@Override
		public VisitProcess visitComputation(Computation computation) {
			if (attachmentLayerCounter == 0 && !selection.isSelected(computation)) {
				documentModelVisitorUtil.removeElementFromResultModel(computation);
			} else {
				addOriginalElementAndAnnotations(computation);
			}

			return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
		}
	}

	@Getter
	private class EmptyGroupRemoverVisitor extends DocumentModelVisitor {
		private final Set<String> emptyGroupFullNames;

		public EmptyGroupRemoverVisitor(Set<String> emptyGroupFullNames) {
			this.emptyGroupFullNames = emptyGroupFullNames;
		}

		@Override
		public VisitProcess visitGroup(Group group) {
			String fullName = ElementUtils.getPath(group);
			if (emptyGroupFullNames.contains(fullName)) {
				documentModelVisitorUtil.removeElementFromResultModel(group);
				return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
			} else {
				addOriginalElementAndAnnotations(group);
			}

			return VisitProcess.CONTINUE_TRAVERSAL;
		}
	}
}
