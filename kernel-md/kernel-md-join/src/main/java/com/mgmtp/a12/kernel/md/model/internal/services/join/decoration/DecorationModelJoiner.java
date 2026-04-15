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
package com.mgmtp.a12.kernel.md.model.internal.services.join.decoration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition.IAdditionOrigins;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCode;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCodeUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.JoinProblem;
import com.mgmtp.a12.kernel.md.model.internal.services.join.selection.SelectionModelV2Wrapper;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelJoinUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.NameCollisionHandler;

/**
 * Executor class to join decoration models for fields or groups.
 */
public abstract class DecorationModelJoiner<T extends Element> {

	protected final DocumentModel resultModel;
	protected final DocumentModel decorationModel;
	protected final SelectionModelV2Wrapper selectionModelWrapper;
	protected final JoiningProblemReporter problemReporter;
	protected final DocumentModelService documentModelService;
	protected final NameCollisionHandler nameCollisionHandler;
	protected final Locale locale;
	protected final Class<T> clazz;
	protected final String anchorName;
	protected final DocumentModelSearchService resultModelSearchService;
	protected int numberOfElementsBeforeAnchorInDecorationModel;
	protected int numberOfElementsAfterAnchorInDecorationModel;

	protected DecorationModelJoiner(
			DocumentModel resultModel,
			DocumentModel decorationModel,
			SelectionModelV2Wrapper selectionModelWrapper,
			Locale locale,
			JoiningProblemReporter problemReporter,
			Class<T> clazz
	) {
		this.resultModel = resultModel;
		this.decorationModel = decorationModel;
		this.selectionModelWrapper = selectionModelWrapper;
		this.problemReporter = problemReporter;
		this.documentModelService = new DocumentModelService();
		this.nameCollisionHandler = new NameCollisionHandler(DocumentModelJoinUtil.getModelString(decorationModel), locale,
				problemReporter);
		this.resultModelSearchService = new DocumentModelSearchService(resultModel);
		this.locale = locale;
		this.clazz = clazz;
		this.anchorName = clazz.getSimpleName();
	}

	public abstract boolean isValidDecorationModel();

	protected static boolean internal_isValidDecorationModel(Consumer<Group> anchorChecker,
			DocumentModel decorationModel, String anchorName, Locale locale, JoiningProblemReporter problemReporter) {
		Group rootGroup = determineValidRootGroup(decorationModel, locale, problemReporter);
		if (rootGroup == null) {
			return false;
		}

		anchorChecker.accept(rootGroup);
		checkElementNames(decorationModel, rootGroup, anchorName, locale, problemReporter);
		return !problemReporter.hasCriticalProblemOccurred();
	}

	protected static <T extends Element> String createAnchorName(Class<T> clazz) {
		return clazz.getSimpleName();
	}

	protected static Group determineValidRootGroup(DocumentModel decorationModel, Locale locale,
			JoiningProblemReporter problemReporter) {
		List<Element> rootGroups = decorationModel.getContent().getModelRoot().getElements();

		if (rootGroups.size() == 1) {
			Element element = rootGroups.getFirst();
			if (element instanceof Group group && group.getRepeatability() == 1) {
				return group;
			}
		}
		problemReporter.reportCriticalProblem(JoinProblem.createError(decorationModel,
				ErrorCodeUtil.getMessageWithCode(ErrorCode.InvalidRootGroups, locale,
						DocumentModelJoinUtil.getModelString(decorationModel))));
		return null;
	}

	protected static <T extends Element> void checkAnchor(Class<T> clazz, DocumentModel decorationModel, Group rootGroup,
			String anchorName, Locale locale, JoiningProblemReporter problemReporter) {
		boolean existsAnchor = rootGroup.getElements().stream()
				.filter(clazz::isInstance)
				.anyMatch(e -> anchorName.equals(e.getName()));

		if (!existsAnchor) {
			String entity = ErrorCodeUtil.getLocalizedMessageFragment(locale, ErrorCode.ErrorMessageFragmentEntity,
					clazz.getSimpleName().toLowerCase());
			problemReporter.reportCriticalProblem(JoinProblem.createError(decorationModel,
					ErrorCodeUtil.getMessageWithCode(ErrorCode.AnchorMissing, locale,
							DocumentModelJoinUtil.getModelString(decorationModel), entity,
							anchorName)));
		}
	}

	protected static void checkElementNames(DocumentModel decorationModel, Group rootGroup, String anchorName, Locale locale,
			JoiningProblemReporter problemReporter) {
		rootGroup.getElements().stream()
				.filter(e -> !e.getName().contains(anchorName))
				.forEach(e -> {

					String elementType = ErrorCodeUtil.getLocalizedMessageFragmentStart(locale, e);
					problemReporter.reportCriticalProblem(JoinProblem.createError(e,
							ErrorCodeUtil.getMessageWithCode(ErrorCode.InvalidName, locale, elementType, e.getName(),
									DocumentModelJoinUtil.getModelString(decorationModel), anchorName)));
				});
	}

	protected int getIndexOfAnchor() {
		List<Element> elements = ((Group) decorationModel.getContent().getModelRoot().getElements().getFirst()).getElements();

		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i).getName().equals(anchorName)) {
				return i;
			}
		}
		String errMsg = ErrorCodeUtil.getMessageWithCode(ErrorCode.AnchorMissing, locale,
				DocumentModelJoinUtil.getModelString(decorationModel));
		throw new IllegalArgumentException(errMsg);
	}

	protected int getNumberOfElementsAfterAnchor() {
		int numberOfElementsInDecorationModel = ((Group) decorationModel.getContent().getModelRoot().getElements()
				.getFirst()).getElements().size();
		return numberOfElementsInDecorationModel - numberOfElementsBeforeAnchorInDecorationModel - 1;
	}

	/**
	 * Checks whether the decoration model causes a name collision with the root model.
	 *
	 * @param elementsBeforeAnchor
	 * 		collections of elements that are located within the corresponding group before the anchor element
	 * @param elementsAfterAnchor
	 * 		collections of elements that are located within the corresponding group after the anchor element
	 * @param nameToElementMapper
	 * 		mapping between the elements (value) in the group in the root model which contains the element to
	 * 		be decorated and their names (key)
	 */
	protected void checkForNameCollisions(List<Element> elementsBeforeAnchor, List<Element> elementsAfterAnchor,
			Map<String, Element> nameToElementMapper) {
		Stream.concat(elementsBeforeAnchor.stream(), elementsAfterAnchor.stream())
				.filter(e -> nameToElementMapper.containsKey(e.getName()))
				.forEach(e -> {
					Element elementInResultModelGroup = nameToElementMapper.get(e.getName());
					String path = documentModelService.getPath(elementInResultModelGroup);
					nameCollisionHandler.handleNameCollision(e, nameToElementMapper.get(e.getName()), path);
				});
	}

	/**
	 * Replaces first occurrence of the anchor element name with the name of the specified element for every element in the given
	 * document model.
	 *
	 * @param documentModel
	 * 		model in which the replacement takes place
	 * @param elementToBeDecorated
	 * 		element whose name replaces the anchor element name
	 * @return mapping between the former full names (keys) of the adjusted elements and their new full names (values)
	 */
	protected Map<String, EntityFullName> replaceIdsAndNamesInDecorationModel(DocumentModel documentModel,
			Element elementToBeDecorated) {
		LinkedHashMap<String, EntityFullName> formerToNewFullNamesMapper = new LinkedHashMap<>();
		DocumentModelWalker docModelWalker = new DocumentModelWalker();
		ElementIdsAndNamesReplacerVisitor elementsNameReplacerVisitor = new ElementIdsAndNamesReplacerVisitor(
				DocumentModelJoinUtil.getModelString(documentModel), elementToBeDecorated, formerToNewFullNamesMapper, anchorName,
				locale,
				problemReporter);
		docModelWalker.acceptDocumentModel(documentModel, elementsNameReplacerVisitor);
		return formerToNewFullNamesMapper;
	}

	protected void handleSortAndIndexFieldsInDecorationModel(DocumentModel decoModel,
			Map<String, EntityFullName> formerToNewFullNamesMapper) {
		DocumentModelWalker docModelWalker = new DocumentModelWalker();
		GroupAdjusterVisitor groupAdjusterVisitor = new GroupAdjusterVisitor(formerToNewFullNamesMapper);
		docModelWalker.acceptDocumentModel(decoModel, groupAdjusterVisitor);
	}

	protected void adjustRulesAndComputationsInDecorationModel(DocumentModel decoModelCopy,
			Map<String, EntityFullName> formerToNewFullNamesMapper) {
		DocumentModelWalker decoModelWalker = new DocumentModelWalker();
		RulesAndComputationsAdjusterVisitor rulesAndComputationsAdjuster = new RulesAndComputationsAdjusterVisitor(
				decorationModel, decoModelCopy, formerToNewFullNamesMapper, problemReporter);
		decoModelWalker.acceptDocumentModel(decoModelCopy, rulesAndComputationsAdjuster);
	}

	protected List<Element> getElementsBeforeAnchor(Group rootGroupInDecoModel) {
		return rootGroupInDecoModel.getElements().subList(0, numberOfElementsBeforeAnchorInDecorationModel);
	}

	protected List<Element> getElementsAfterAnchor(Group rootGroupInDecoModel) {
		List<Element> elements = rootGroupInDecoModel.getElements();
		return elements.subList(numberOfElementsBeforeAnchorInDecorationModel + 1, elements.size());
	}

	protected void handleHeaderAndTypedefs() {
		DocumentModelWalker decoModelWalker = new DocumentModelWalker();
		HeaderAndTypedefAdjusterVisitor headerAndTypedefAdjusterVisitor = new HeaderAndTypedefAdjusterVisitor(resultModel);
		decoModelWalker.acceptDocumentModel(decorationModel, headerAndTypedefAdjusterVisitor);

	}

	/**
	 * <b>Note:</b> References to error entities in rules or computed fields in computations are not
	 * updated here, but in {@link DocumentModelUtils#resolveAndUpdateReferences}.
	 */
	public void decorateElements() {
		if (isValidDecorationModel()) {
			DocumentModel resultModelCopy = DocumentModelCopyService.copy(resultModel);
			numberOfElementsBeforeAnchorInDecorationModel = getIndexOfAnchor();
			numberOfElementsAfterAnchorInDecorationModel = getNumberOfElementsAfterAnchor();

			DocumentModelWalker modelWalker = new DocumentModelWalker();
			ElementsDecoratorVisitor elementsDecoratorVisitor = getElementsDecoratorVisitor();
			modelWalker.acceptDocumentModel(resultModelCopy, elementsDecoratorVisitor);

			handleHeaderAndTypedefs();
		}
	}

	protected ElementsDecoratorVisitor getElementsDecoratorVisitor() {
		return new ElementsDecoratorVisitor();
	}

	protected class ElementsDecoratorVisitor extends DocumentModelVisitor {
		@Override
		public VisitProcess visitGroup(final Group group) {
			if (noDecorationNecessary(group)) {
				return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
			}

			return decorateElements(group);
		}

		private VisitProcess decorateElements(Group group) {
			int offset = 0; // already inserted elements in the group
			List<Element> elements = group.getElements();
			Map<String, Element> nameToElementMapper = elements.stream()
					.collect(Collectors.toMap(Element::getName, Function.identity()));
			Group groupInResultModel = getGroupInResultModel(group);

			decorateRootGroupsIfNecessary(group, nameToElementMapper);

			for (int i = 0; i < elements.size(); i++) {
				offset = doDecoration(elements.get(i), nameToElementMapper, groupInResultModel, offset, i);
			}
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		protected void decorateRootGroupsIfNecessary(Group group, Map<String, Element> nameToElementMapper) {
			//implementation in subclass
		}

		protected int doDecoration(Element element, Map<String, Element> nameToElementMapper, Group groupInResultModel, int offset,
				int i) {
			if (clazz.isInstance(element)) {
				T elementToBeDecorated = clazz.cast(element);
				if (!selectionModelWrapper.isSelected(elementToBeDecorated) || additionalDecorationChecks(element)) {
					return offset;
				}
				DocumentModel decoModel = prepareDecoModelForElement(elementToBeDecorated);
				Group rootGroupInDecoModel = (Group) decoModel.getContent().getModelRoot().getElements().getFirst();
				List<Element> elementsBeforeAnchorField = getElementsBeforeAnchor(rootGroupInDecoModel);
				List<Element> elementsAfterAnchorField = getElementsAfterAnchor(rootGroupInDecoModel);
				// get the (possibly) adapted anchor element (annotation might be adapted),
				// the preparation (prepareDecoModelForElement) does not change the position of the anchor field,
				// so we can use the position to retrieve the field from the adapted rootGroupInDecoModel
				Element anchorElement = rootGroupInDecoModel.getElements().get(numberOfElementsBeforeAnchorInDecorationModel);

				checkForNameCollisions(elementsBeforeAnchorField, elementsAfterAnchorField, nameToElementMapper);

				String path = documentModelService.getPath(elementToBeDecorated);
				T elementInResultModel = resultModelSearchService.getByPath(path)
						.filter(clazz::isInstance).map(clazz::cast).orElseThrow();
				IAdditionOrigins.IAnnotationsOrigins annotationsOrigins = new DummyAnnotationsOrigins(); // we don't need this information for decoration
				DocumentModelJoinUtil.determineAndSetJoinOfAnnotations(elementToBeDecorated.getAnnotations(),
						anchorElement.getAnnotations(),
						elementInResultModel::setAnnotations,
						() -> nameCollisionHandler.handleInconsistentAnnotations(clazz, elementInResultModel),
						annotationsOrigins,
						problemReporter
				);

				if (!problemReporter.hasCriticalProblemOccurred()) { // there may be name collisions or problems caused by adjusting the rules and computations
					groupInResultModel.addElements(i + offset, elementsBeforeAnchorField);
					offset += numberOfElementsBeforeAnchorInDecorationModel;
					groupInResultModel.addElements(i + 1 + offset, elementsAfterAnchorField);
					offset += numberOfElementsAfterAnchorInDecorationModel;
				}
			}
			return offset;
		}

		protected boolean additionalDecorationChecks(Element element) {
			//implementation in subclass if necessary
			return false;
		}

		private Group getGroupInResultModel(Group group) {
			String path = documentModelService.getPath(group);
			Optional<Element> optGroupInResultModel = resultModelSearchService.getByPath(path);

			if (optGroupInResultModel.isPresent()) {
				return (Group) optGroupInResultModel.get();
			}

			throw new IllegalArgumentException(String.format("Group '%s' not found.", path));
		}
	}

	protected DocumentModel prepareDecoModelForElement(T elementToBeDecorated) {
		DocumentModel decoModelCopy = DocumentModelCopyService.copy(decorationModel);
		Map<String, EntityFullName> formerToNewFullNamesMapper = replaceIdsAndNamesInDecorationModel(decoModelCopy,
				elementToBeDecorated);

		if (!problemReporter.hasCriticalProblemOccurred() && !formerToNewFullNamesMapper.isEmpty()) {
			handleSortAndIndexFieldsInDecorationModel(decoModelCopy, formerToNewFullNamesMapper);
			adjustRulesAndComputationsInDecorationModel(decoModelCopy, formerToNewFullNamesMapper);
		}
		return decoModelCopy;
	}

	protected boolean noDecorationNecessary(Group group) {
		//implementation in subclass if necessary
		return false;
	}
}
