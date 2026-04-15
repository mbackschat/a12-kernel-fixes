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
package com.mgmtp.a12.kernel.md.model.internal.services.join.addition;

import static com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils.zipWithIndex;
import static com.mgmtp.a12.kernel.md.model.internal.services.join.addition.DocumentModelInfo4AdditionExtractor.extractElementsInAdditive;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition.AnnotationsOrigins;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition.IAdditionOrigins;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.DocumentModelJoiningItem;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.HeaderBuilderUtil;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelJoinUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelVisitorUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.IdUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.NameCollisionHandler;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderFactory;

import lombok.Getter;

/**
 * Executor class to join an additive model using a {@link DocumentModelWalker} combined with the {@link ElementsAdderVisitor}. This
 * class is not immutable since the input root model is modified during execution and returned as result model.
 */
public class AdditiveModelJoiner {

	/**
	 * This method is for elements from the joined model whose id changes related to the root model (rules/computations which are
	 * taken from the additive model). The mapping goes from the id in the joined model (modified additive id) to the corresponding
	 * original id in the additive model (the mapping for the root model will be handled with
	 * {@link ElementsAdderVisitor#addJoinedIdToRootIdMapping(String, String)}).
	 */
	public static void addJoinedIdToAdditiveIdMapping(Map<String, String> origAddIdByModifiedAddId, String elementId,
			IAdditionOrigins additionOrigins) {
		assert origAddIdByModifiedAddId.containsKey(elementId);
		additionOrigins.addIdInAdditiveByIdInJoined(elementId, origAddIdByModifiedAddId.get(elementId));
	}

	private final DocumentModel resultModel;
	private final DocumentModel additiveModel;
	private final IAdditionOrigins additionOrigins;
	private final DocumentModelJoiningItem.ElementOrderForAddition elementOrderForAddition;
	private final JoiningProblemReporter problemReporter;
	private final NameCollisionHandler nameCollisionHandler;
	private final DocumentModelVisitorUtil documentModelVisitorUtil;
	private final Map<String, String> origAddIdByModifiedAddId;

	public AdditiveModelJoiner(DocumentModel resultModel, DocumentModel additiveModel,
			IAdditionOrigins additionOrigins,
			DocumentModelJoiningItem.ElementOrderForAddition elementOrderForAddition,
			Locale locale,
			JoiningProblemReporter problemReporter) {
		this.resultModel = resultModel;
		this.additiveModel = additiveModel;
		this.additionOrigins = additionOrigins;
		this.elementOrderForAddition = elementOrderForAddition;
		this.problemReporter = problemReporter;
		this.nameCollisionHandler = new NameCollisionHandler(DocumentModelJoinUtil.getModelString(additiveModel), locale,
				problemReporter);
		this.documentModelVisitorUtil = new DocumentModelVisitorUtil(resultModel);
		this.origAddIdByModifiedAddId = new LinkedHashMap<>();
	}

	/**
	 * Adds elements that are contained in the given additional model to the result model. Elements that do not yet exist in the
	 * root model are added, already existing rules and computations are replaced. <br/>
	 * <b>Note:</b> References to error entities in rules or computed fields in computations are not
	 * updated here, but in {@link DocumentModelUtils#resolveAndUpdateReferences}.
	 */
	public void addElements() {
		DocumentModel addModelCopy = DocumentModelCopyService.copy(additiveModel);
		DocumentModelWalker modelWalker = new DocumentModelWalker();
		ElementIdsReplacerVisitor elementIdsReplacerVisitor = new ElementIdsReplacerVisitor();
		modelWalker.acceptDocumentModel(addModelCopy, elementIdsReplacerVisitor);

		ElementsAdderVisitor elementsAdder = new ElementsAdderVisitor();
		modelWalker.acceptDocumentModel(addModelCopy, elementsAdder);
	}

	@Getter
	private class ElementIdsReplacerVisitor extends DocumentModelVisitor {

		private final IdUtil idUtil = new IdUtil();

		@Override
		public VisitProcess visitElement(Element element) {
			String modifiedId = idUtil.getIdForAdditiveModel(additiveModel, element);
			origAddIdByModifiedAddId.put(modifiedId, element.getId());
			element.setId(modifiedId);
			return VisitProcess.CONTINUE_TRAVERSAL;
		}
	}

	private class ElementsAdderVisitor extends DocumentModelVisitor {
		private final Set<String> typeDefinitionIdsRoot =
				resultModel.getContent().getTypeDefinitions().stream().map(FieldTypeDefinition::getId).collect(Collectors.toSet());
		private final Map<String, Integer> newRuleOrCompPositionCache = new HashMap<>();

		@Override
		public VisitProcess visitHeader(Header header) {
			AnnotationsOrigins annotationsOrigins = new AnnotationsOrigins();
			DocumentModelJoinUtil.determineAndSetJoinOfAnnotations(resultModel.getHeader().getAnnotations(),
					header.getAnnotations(),
					annotations -> resultModel.setHeader(getCopyHeaderBuilder().withAnnotations(annotations).build()),
					() -> nameCollisionHandler.handleInconsistentAnnotations(Header.class, null),
					annotationsOrigins, problemReporter
			);
			if (!resultModel.getHeader().getAnnotations().isEmpty()) {
				additionOrigins.addAnnotationsForCommon("", annotationsOrigins);
			}
			DocumentModelJoinUtil.mergeModelReferences(resultModel, header);
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		private HeaderFactory.Builder getCopyHeaderBuilder() {
			return HeaderBuilderUtil.toBuilder(resultModel.getHeader());
		}

		@Override
		public VisitProcess visitElement(Element element) {
			String path = ElementUtils.getPath(element);
			Optional<Element> elementInResultModel = documentModelVisitorUtil.getResultElement(path);

			Group resultParentGroup = documentModelVisitorUtil.getResultParentGroup(path);

			if (elementInResultModel.isEmpty()) {
				handleNewElement(resultParentGroup, element);
				return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
			} else if (!element.getClass().equals(elementInResultModel.get().getClass())) {
				// Could be the case if a field or rule already has the same full name
				nameCollisionHandler.handleNameCollision(element, elementInResultModel.get(), path);
				handleDuplicateElement(resultParentGroup, elementInResultModel.get(), element);
				return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
			} else {
				handleExistingElement(resultParentGroup, elementInResultModel.get(), element);
			}

			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		private void handleNewElement(Group resultParent, Element newElement) {
			int posForNewElem = positionForNewElementAndUpdateRuleOrCompPositionCache(resultParent, newElement);
			resultParent.addElement(posForNewElem, newElement);
			additionOrigins.addAdditiveElement(newElement.getId());
			AdditiveModelJoiner.addJoinedIdToAdditiveIdMapping(origAddIdByModifiedAddId, newElement.getId(), additionOrigins);
			if (newElement instanceof Group group && !(additionOrigins instanceof DummyAdditionOrigins)) {
				extractElementsInAdditive(group, additionOrigins, origAddIdByModifiedAddId);
			}
		}

		private int positionForNewElementAndUpdateRuleOrCompPositionCache(Group resultParent, Element newElement) {
			return switch (elementOrderForAddition) {
				case ANCHOR_BASED_ORDERING -> newElementPositionAnchorBased(resultParent, newElement);
				case LEGACY_EXTMP_ORDERING -> newElementPositionLegacyAndUpdateRuleOrCompPositionCache(resultParent, newElement);
			};
		}

		/**
		 * Determines the position of an added element among its sibling elements in the result model.<br>
		 * The position is determined as follows:
		 * <ol>
		 *     <li>If the added element has, in the added fragment model, a <em>later</em> sibling element that also exists
		 *         in the base model ("anchor element"), then the added element must be inserted at a position <em>before</em> that
		 *         sibling element in the result model</li>
		 *     <li>Added elements are inserted at the lowest position among their sibling elements that satisfies the above
		 *         "anchoring constraint", in the order in which they occur in the added fragment model (this implies that added
		 *         element that do not have anchor elements are inserted below all base-model siblings.).
		 *     </li>
		 * </ol>
		 * Context: the above rule is applied iteratively, where siblings in the added fragment model are visited in the order in
		 * which they appear in that model; the insert into the result model happens during the visit (so that result positions
		 * will get shifted before the next element is visited)
		 */
		private static int newElementPositionAnchorBased(Group resultParent, Element newElement) {
			// NOTE: this sibling -> index map is only temporarily valid. inserts of other elements (e.g. rules) can shift
			// the indices.
			Map<String, Integer> resultSiblingNamesToPos = zipWithIndex(resultParent.getElements().stream())
					.collect(Collectors.toMap(
							i_e -> i_e.getRight().getName(),
							Pair::getLeft,
							(first, second) -> first // name collisions can't occur in the happy path, but in the
							// error case we must not abort here yet in order to let error reporting work as intended
					));
			List<Element> sourceSiblings = newElement.getParent().getElements();
			List<Element> sourceSiblingsAfterNewElement = sourceSiblings
					.subList(sourceSiblings.indexOf(newElement) + 1, sourceSiblings.size());

			return sourceSiblingsAfterNewElement.stream()
					.map(Element::getName)
					// after this filter, we have the names of all elems that are anchor elements for newElement:
					.filter(resultSiblingNamesToPos::containsKey) // note: the general join consistency check should additionally
					// ensure that the element of same name has the same type both in source and result
					.mapToInt(resultSiblingNamesToPos::get)
					.reduce(resultParent.getElements().size(), Math::min);
		}

		private int newElementPositionLegacyAndUpdateRuleOrCompPositionCache(Group resultParent, Element newElement) {
			if (newElement instanceof Rule || newElement instanceof Computation) {
				// for LEGACY_EXTMP_ORDERING, rules and computations get added at the top (below only their preceding sibling
				// rules and comps from the additive fragment
				int pos = newRuleOrCompPositionCache.get(resultParent.getId());
				newRuleOrCompPositionCache.put(resultParent.getId(), pos + 1);
				return pos;
			} else {
				// all other elements (groups, fields) get added below the base-model elements, for LEGACY_EXTMP_ORDERING
				return resultParent.getElements().size();
			}
		}

		private void handleDuplicateElement(Group resultParent, Element resultElement, Element newElement) {
			// since we have duplicates, we cannot talk about a common element
			// that's why, we assign each element to its corresponding model
			additionOrigins.addRootElement(resultParent.getId());
			additionOrigins.addAdditiveElement(newElement.getId());
			// no corresponding mapping for the root element since it is not a common element yet
			AdditiveModelJoiner.addJoinedIdToAdditiveIdMapping(origAddIdByModifiedAddId, newElement.getId(), additionOrigins);
			// we put the duplicates together in order for the user to identify them easily
			int originalPos = resultParent.getElements().indexOf(resultElement);
			resultParent.addElement(originalPos + 1, newElement);
		}

		private void handleExistingElement(Group resultParent, Element existingElement, Element newElement) {
			if (newElement instanceof Rule || newElement instanceof Computation) {
				additionOrigins.addCommonElement(newElement.getId());
				additionOrigins.addCompletelyOverwrittenRootElement(existingElement.getId());
				AdditiveModelJoiner.addJoinedIdToAdditiveIdMapping(origAddIdByModifiedAddId, newElement.getId(), additionOrigins);
				addJoinedIdToRootIdMapping(existingElement.getId(), newElement.getId());
				resultParent.replaceElement(existingElement, newElement);
				if (!newElement.getAnnotations().isEmpty()) {
					additionOrigins.addAnnotationsForCommon(newElement.getId(),
							AnnotationsOrigins.createWithAnnotationsInAdditive(newElement.getAnnotations()));
				}
			} else if (existingElement instanceof Group existingGroup) {
				additionOrigins.addCommonElement(existingGroup.getId());
				addJoinedIdToAdditiveIdMapping(existingElement.getId(), newElement.getId());
				handleExistingGroup(existingGroup, (Group) newElement);
				newRuleOrCompPositionCache.put(existingGroup.getId(), 0);
			} else if (existingElement instanceof Field existingField) {
				additionOrigins.addCommonElement(existingField.getId());
				addJoinedIdToAdditiveIdMapping(existingElement.getId(), newElement.getId());
				handleExistingField(existingField, (Field) newElement);
			}
		}

		/**
		 * This method is for elements from the joined model whose id does not change related to the root model (fields/groups).
		 * The mapping goes from the id in the joined model (in this case, the id in the root model) to the corresponding original
		 * id in the additive model.
		 */
		private void addJoinedIdToAdditiveIdMapping(String existingId, String newId) {
			assert origAddIdByModifiedAddId.containsKey(newId);
			additionOrigins.addIdInAdditiveByIdInJoined(existingId, origAddIdByModifiedAddId.get(newId));
		}

		/**
		 * This method is for elements from the joined model whose id changes related to the root model (rules/computations which
		 * are taken from the additive model). The mapping goes from the id in the joined model (modified additive id) to the
		 * corresponding id in the root model (the mapping for the additive model will be handled with
		 * {@link AdditiveModelJoiner#addJoinedIdToAdditiveIdMapping(Map, String, IAdditionOrigins)}).
		 */
		private void addJoinedIdToRootIdMapping(String existingId, String newId) {
			additionOrigins.addIdInRootByIdInJoined(newId, existingId);
		}

		private void handleExistingGroup(Group existingGroup, Group newGroup) {
			IAdditionOrigins.IAnnotationsOrigins annotationsOrigins = new AnnotationsOrigins();
			DocumentModelJoinUtil.determineAndSetJoinOfAnnotations(existingGroup.getAnnotations(), newGroup.getAnnotations(),
					existingGroup::setAnnotations,
					() -> nameCollisionHandler.handleInconsistentAnnotations(Group.class, newGroup),
					annotationsOrigins,
					problemReporter
			);
			if (!existingGroup.getAnnotations().isEmpty()) {
				additionOrigins.addAnnotationsForCommon(existingGroup.getId(), annotationsOrigins);
			}
			// Only relevant in case existingGroup or newGroup originated from an include (are non-null):
			// we use the include details (modelAlias, includeLevel, ...) from the additive fragment (newGroup),
			// so that the include will be correctly displayed in SME's additive-fragment editor.
			//   (that editor uses the _result_ of the addition, in order to display the context
			//    from the reference model (read-only) around the contents of the additive fragment,
			//    so it must rely on the expansion code path here)
			// This is maybe not ideal (the resulting group contains not only contents of the included model
			// but also from the reference model), but seems like the best we can do with the current
			// representation of expanded includes.
			existingGroup.setIncludeDetails(newGroup.getIncludeDetails().orElse(null));
		}

		private void handleExistingField(Field existingField, Field newField) {
			IAdditionOrigins.IAnnotationsOrigins annotationsOrigins = new AnnotationsOrigins();
			DocumentModelJoinUtil.determineAndSetJoinOfAnnotations(existingField.getAnnotations(),
					newField.getAnnotations(),
					existingField::setAnnotations,
					() -> nameCollisionHandler.handleInconsistentAnnotations(Field.class, newField),
					annotationsOrigins,
					problemReporter
			);
			if (!existingField.getAnnotations().isEmpty()) {
				additionOrigins.addAnnotationsForCommon(existingField.getId(), annotationsOrigins);
			}
			Optional<List<EnumerationType.EnumValue>> optJoinedEnumValues = DocumentModelJoinUtil.determineOptionalJoinOfEnumerationValues(
					existingField, newField);
			optJoinedEnumValues.ifPresent(
					stringListMap -> DocumentModelJoinUtil.tryCastFieldTypeToEnumeration(existingField.getFieldType())
							.setValues(stringListMap));
		}

		@Override
		public VisitProcess visitFieldTypeDefinition(FieldTypeDefinition fieldTypeDefinition) {
			String typeDefId = fieldTypeDefinition.getId();
			if (!typeDefinitionIdsRoot.contains(typeDefId)) {
				additionOrigins.addAdditiveTypeDef(typeDefId);
				resultModel.getContent().getTypeDefinitions().add(fieldTypeDefinition);
			} else {
				additionOrigins.addCommonTypeDef(typeDefId);
			}
			return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
		}
	}

}
