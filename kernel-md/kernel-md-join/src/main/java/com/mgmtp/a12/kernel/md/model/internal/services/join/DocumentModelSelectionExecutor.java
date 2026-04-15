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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TypeDefType;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.join.selection.SelectionModelV2Wrapper;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelVisitorUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;

import lombok.Getter;

/**
 * Executor class to join a selection.
 */
public class DocumentModelSelectionExecutor {
	private final DocumentModel rootModel;
	private final DocumentModel resultModel;
	private final SelectionModelV2Wrapper selection;
	private final JoiningProblemReporter problemReporter;
	private final DocumentModelVisitorUtil documentModelVisitorUtil;
	private final DocumentModelService dmService;

	public DocumentModelSelectionExecutor(
			DocumentModel rootModelCopy,
			SelectionModelV2Wrapper selection,
			JoiningProblemReporter problemReporter) {
		this.rootModel = rootModelCopy;
		this.resultModel = DocumentModelCopyService.copy(rootModelCopy);
		this.selection = selection;
		this.problemReporter = problemReporter;
		this.documentModelVisitorUtil = new DocumentModelVisitorUtil(resultModel);
		this.dmService = new DocumentModelService();
	}

	public DocumentModel select() {
		DocumentModelWalker modelWalker = new DocumentModelWalker();
		ElementsSelectorVisitor elementsSelector = new ElementsSelectorVisitor();
		modelWalker.acceptDocumentModel(rootModel, elementsSelector);

		// we allow incomplete/invalid rules and computations, the final validation has to be done after by the user
		removeEmptyGroupsAndUnusedTypedefs();

		// ignore missing references as those might be added later
		DocumentModelUtils.resolveAndUpdateReferences(resultModel, p -> {});

		return problemReporter.hasCriticalProblemOccurred() ? null : resultModel;
	}

	private void removeEmptyGroupsAndUnusedTypedefs() {
		DocumentModel resultModelCopy = DocumentModelCopyService.copy(this.resultModel);
		Map<String, Group> emptyGroupMap = new HashMap<>();
		checkEmptyGroups(resultModelCopy.getContent().getModelRoot().getElements(), emptyGroupMap);
		DocumentModelWalker modelWalker = new DocumentModelWalker();
		EmptyGroupRemoverVisitor emptyGroupRemoverVisitor = new EmptyGroupRemoverVisitor(emptyGroupMap.keySet());
		modelWalker.acceptDocumentModel(resultModelCopy, emptyGroupRemoverVisitor);

		UnusedTypedefsRemoverVisitor unusedTypedefsRemoverVisitor = new UnusedTypedefsRemoverVisitor(
				emptyGroupRemoverVisitor.getUnusedTypedefIds());
		modelWalker.acceptDocumentModel(resultModelCopy, unusedTypedefsRemoverVisitor);
	}

	private void checkEmptyGroups(List<Element> elements, Map<String, Group> emptyGroupMap) {
		List<Group> groups = elements.stream().filter(Group.class::isInstance).map(Group.class::cast).toList();
		for (Group group : groups) {
			checkEmptyGroups(group.getElements(), emptyGroupMap);

			if (group.getElements().isEmpty()) {
				emptyGroupMap.put(dmService.getPath(group), group);
				continue;
			}

			List<Group> childGroups = group.getElements().stream().filter(Group.class::isInstance).map(Group.class::cast).toList();
			if (group.getElements().size() == childGroups.size() &&
					childGroups.stream().allMatch(g -> emptyGroupMap.containsKey(dmService.getPath(g)))) {
				emptyGroupMap.put(dmService.getPath(group), group);
			}
		}
	}

	/**
	 * {@link DocumentModelVisitor} implementation to handle selection of fields and rules of root model.
	 */
	private class ElementsSelectorVisitor extends DocumentModelVisitor {

		@Override
		public VisitProcess visitField(Field field) {
			if (!selection.isSelected(field)) {
				documentModelVisitorUtil.removeElementFromResultModel(field);

				//remove sort and index field if necessary
				String fieldPath = dmService.getPath(field);
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
			}

			return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
		}

		@Override
		public VisitProcess visitRule(Rule rule) {
			if (!selection.isSelected(rule)) {
				documentModelVisitorUtil.removeElementFromResultModel(rule);
			}

			return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
		}

		@Override
		public VisitProcess visitComputation(Computation computation) {
			if (!selection.isSelected(computation)) {
				documentModelVisitorUtil.removeElementFromResultModel(computation);
			}

			return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
		}
	}

	@Getter
	private class EmptyGroupRemoverVisitor extends DocumentModelVisitor {

		private final Set<String> unusedTypedefIds =
				resultModel.getContent().getTypeDefinitions().stream().map(FieldTypeDefinition::getId)
						.collect(Collectors.toSet());
		private final Set<String> emptyGroupFullNames;

		public EmptyGroupRemoverVisitor(Set<String> emptyGroupFullNames) {
			this.emptyGroupFullNames = emptyGroupFullNames;
		}

		@Override
		public VisitProcess visitField(Field field) {
			if (field.getFieldType() instanceof TypeDefType typedef) {
				unusedTypedefIds.remove(typedef.getId());
			}
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		@Override
		public VisitProcess visitGroup(Group group) {
			String fullName = dmService.getPath(group);
			if (emptyGroupFullNames.contains(fullName)) {
				documentModelVisitorUtil.removeElementFromResultModel(group);
				return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
			}

			return VisitProcess.CONTINUE_TRAVERSAL;
		}
	}

	private class UnusedTypedefsRemoverVisitor extends DocumentModelVisitor {

		private final Set<String> unusedTypedefIds;

		public UnusedTypedefsRemoverVisitor(Set<String> unusedTypedefIds) {
			this.unusedTypedefIds = unusedTypedefIds;
		}

		@Override
		public VisitProcess visitElement(Element element) {
			return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
		}

		@Override
		public VisitProcess visitFieldTypeDefinition(FieldTypeDefinition fieldTypeDefinition) {
			if (unusedTypedefIds.contains(fieldTypeDefinition.getId())) {
				resultModel.getContent().getTypeDefinitions().remove(fieldTypeDefinition);
			}
			return VisitProcess.CONTINUE_TRAVERSAL;
		}
	}
}
