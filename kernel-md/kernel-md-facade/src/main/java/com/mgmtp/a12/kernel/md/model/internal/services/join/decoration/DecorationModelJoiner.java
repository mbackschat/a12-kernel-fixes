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

import static com.mgmtp.a12.kernel.md.model.internal.expansioninfo.ExpansionInfoUtils.forwardOriginForSingleElementWithAnnotations;
import static com.mgmtp.a12.kernel.md.model.internal.expansioninfo.ExpansionInfoUtils.forwardOrigins;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.ExpansionInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdge;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdgeLabel;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionNode;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.LocalCoordinates;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DMWithExpansionInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCode;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCodeUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.JoinProblem;
import com.mgmtp.a12.kernel.md.model.internal.services.join.selection.SelectionModelV2Wrapper;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelJoinUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.NameCollisionHandler;

import lombok.NonNull;

/**
 * Executor class to join decoration models for fields or groups.
 */
public abstract class DecorationModelJoiner<T extends Element> {

	protected final DocumentModel referenceModelMutatedInPlaceToResultModel;
	protected final Optional<ExpansionInfo> referenceModelExpansionInfo;
	protected final DocumentModel decorationModel;
	protected final Optional<ExpansionInfo> decorationModelExpansionInfo;
	protected final SelectionModelV2Wrapper selectionModelWrapper;
	protected final JoiningProblemReporter problemReporter;
	protected final NameCollisionHandler nameCollisionHandler;
	protected final Locale locale;
	protected final Class<T> clazz;
	protected final String anchorName;
	protected final DocumentModelSearchService resultModelSearchService;
	protected int numberOfElementsBeforeAnchorInDecorationModel;
	protected int numberOfElementsAfterAnchorInDecorationModel;
	private final Set<ExpansionNode> originsForResult = new HashSet<>();
	private final int stepNumber;
	private final Map<String, Pair<String, String>> newIdToFormerIdAndFullNameMapper = new HashMap<>();

	protected DecorationModelJoiner(
			DMWithExpansionInfo referenceModelMutatedInPlaceToResultModel,
			DMWithExpansionInfo decorationModel,
			int stepNumber,
			SelectionModelV2Wrapper selectionModelWrapper,
			Locale locale,
			JoiningProblemReporter problemReporter,
			Class<T> clazz
	) {
		this.referenceModelMutatedInPlaceToResultModel = referenceModelMutatedInPlaceToResultModel.dm();
		this.referenceModelExpansionInfo = referenceModelMutatedInPlaceToResultModel.ei();
		this.decorationModel = decorationModel.dm();
		this.decorationModelExpansionInfo = decorationModel.ei();
		this.selectionModelWrapper = selectionModelWrapper;
		this.problemReporter = problemReporter;
		this.nameCollisionHandler = new NameCollisionHandler(DocumentModelJoinUtil.getModelString(this.decorationModel), locale,
				problemReporter);
		this.resultModelSearchService = new DocumentModelSearchService(this.referenceModelMutatedInPlaceToResultModel);
		this.locale = locale;
		this.clazz = clazz;
		this.anchorName = createAnchorName(clazz);
		this.stepNumber = stepNumber;
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
		return !problemReporter.hasCriticalErrorOccurred();
	}

	protected static <T extends Element> String createAnchorName(Class<T> clazz) {
		return clazz.getSimpleName();
	}

	protected static Group determineValidRootGroup(DocumentModel decorationModel, Locale locale,
			JoiningProblemReporter problemReporter) {
		List<Element> rootGroups = decorationModel.getContent().getModelRoot()
				.getElements();

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

	protected static <T extends Element> void checkAnchor(Class<T> clazz,
			DocumentModel decorationModel, Group rootGroup,
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
		List<Element> elements = ((Group) decorationModel.getContent().getModelRoot()
				.getElements().getFirst()).getElements();

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
	protected void checkForNameCollisions(List<Element> elementsBeforeAnchor,
			List<Element> elementsAfterAnchor,
			Map<String, Element> nameToElementMapper) {
		Stream.concat(elementsBeforeAnchor.stream(), elementsAfterAnchor.stream())
				.filter(e -> nameToElementMapper.containsKey(e.getName()))
				.forEach(e -> {
					Element elementInResultModelGroup = nameToElementMapper.get(
							e.getName());
					String path = ElementUtils.getPath(elementInResultModelGroup);
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
				locale, problemReporter);
		docModelWalker.acceptDocumentModel(documentModel, elementsNameReplacerVisitor);

		if (referenceModelExpansionInfo.isPresent() && decorationModelExpansionInfo.isPresent()) {
			Map<String, Pair<String, String>> newId2OldIdAndFullname = elementsNameReplacerVisitor.getNewIdToFormerIdAndFullNameMapper();
			String resultModelId = referenceModelMutatedInPlaceToResultModel.getHeader().getId();

			// the element that is decorated is always based on the original and the decorated element
			originsForResult.add(ExpansionNode.of(
					LocalCoordinates.Element.of(elementToBeDecorated.getId()),
					resultModelId,
					Set.of(
							ExpansionEdge.of(ExpansionEdgeLabel.JoinInternal.of(),
									referenceModelExpansionInfo.get().elementOriginById(elementToBeDecorated.getId())),
							ExpansionEdge.of(
									ExpansionEdgeLabel.Cm.Decoration.of(stepNumber, elementToBeDecorated.getId()),
									decorationModelExpansionInfo.get().elementOriginById(
											newId2OldIdAndFullname.get(elementToBeDecorated.getId()).getLeft()))
					)
			));

			// add all other elements that have to be new (will be checked and reported)
			newId2OldIdAndFullname.forEach((newElementId, oldIdAndFullname) -> {
				// as we only use the content of the model root group, we ignore the root group itself
				if (isRootGroup(oldIdAndFullname.getRight())) {
					return;
				}

				if (!newElementId.equals(elementToBeDecorated.getId())) {
					originsForResult.add(ExpansionNode.of(
							LocalCoordinates.Element.of(newElementId),
							resultModelId,
							Set.of(ExpansionEdge.of(
									ExpansionEdgeLabel.Cm.Decoration.of(stepNumber, elementToBeDecorated.getId()),
									decorationModelExpansionInfo.get().elementOriginById(oldIdAndFullname.getLeft())
							))
					));

					formerToNewFullNamesMapper.get(oldIdAndFullname.getRight()).getElement().getAnnotations().forEach(annotation ->
							originsForResult.add(ExpansionNode.of(
									LocalCoordinates.Annotation.of(annotation.getName(), newElementId),
									resultModelId,
									Set.of(ExpansionEdge.of(
											ExpansionEdgeLabel.Cm.Decoration.of(stepNumber, elementToBeDecorated.getId()),
											decorationModelExpansionInfo.get().elementOriginById(oldIdAndFullname.getLeft())
									))
							))
					);
				}
			});

			newIdToFormerIdAndFullNameMapper.putAll(newId2OldIdAndFullname);
		}

		return formerToNewFullNamesMapper;
	}

	private static boolean isRootGroup(@NonNull String elementPath) {
		return ElementPathUtils.getParentPath(elementPath).isEmpty();
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
		HeaderAndTypedefAdjusterVisitor headerAndTypedefAdjusterVisitor = new HeaderAndTypedefAdjusterVisitor(
				referenceModelMutatedInPlaceToResultModel);
		decoModelWalker.acceptDocumentModel(decorationModel, headerAndTypedefAdjusterVisitor);
	}

	/**
	 * <b>Note:</b> References to error entities in rules or computed fields in computations are not
	 * updated here, but in {@link DocumentModelUtils#resolveAndUpdateReferences}.
	 */
	public @NonNull Optional<ExpansionInfo> decorateElements() {
		if (isValidDecorationModel()) {
			DocumentModel resultModelCopy = DocumentModelCopyService.copy(referenceModelMutatedInPlaceToResultModel);
			numberOfElementsBeforeAnchorInDecorationModel = getIndexOfAnchor();
			numberOfElementsAfterAnchorInDecorationModel = getNumberOfElementsAfterAnchor();

			DocumentModelWalker modelWalker = new DocumentModelWalker();

			// find all elements from the reference model, subsequently we remove all that get altered,
			// so we'll retain all those that end up in the result untouched by the decoration
			Map<String, Element> unalteredElementsFromPreviousStep = new HashMap<>();
			if (referenceModelExpansionInfo.isPresent()) {
				modelWalker.acceptDocumentModel(referenceModelMutatedInPlaceToResultModel, new DocumentModelVisitor() {
					@Override public VisitProcess visitElement(Element element) {
						unalteredElementsFromPreviousStep.put(element.getId(), element);
						return super.visitElement(element);
					}
				});
			}

			ElementsDecoratorVisitor elementsDecoratorVisitor = getElementsDecoratorVisitor(unalteredElementsFromPreviousStep);
			modelWalker.acceptDocumentModel(resultModelCopy, elementsDecoratorVisitor);

			if (referenceModelExpansionInfo.isPresent() && decorationModelExpansionInfo.isPresent()) {
				String resultModelId = referenceModelMutatedInPlaceToResultModel.getHeader().getId();
				// add all unaltered elements, those are still in the result
				unalteredElementsFromPreviousStep.forEach((elementId, element) ->
						originsForResult.addAll(forwardOriginForSingleElementWithAnnotations(elementId, resultModelId,
								ExpansionEdgeLabel.JoinInternal.of(), eId -> eId, referenceModelExpansionInfo.get()
								/* unaltered id from reference */))
				);

				ExpansionInfo refEi = referenceModelExpansionInfo.get();
				ExpansionInfo decoEi = decorationModelExpansionInfo.get();

				originsForResult.addAll(forwardOrigins(refEi,
						coords -> coords instanceof LocalCoordinates.ModelReference
								|| coords instanceof LocalCoordinates.Typedef
								|| coords instanceof LocalCoordinates.DocumentUniquenessCriterion,
						ExpansionEdgeLabel.JoinInternal.of(), resultModelId
				));

				originsForResult.addAll(forwardOrigins(decoEi,
						coords -> coords instanceof LocalCoordinates.ModelReference || coords instanceof LocalCoordinates.Typedef,
						ExpansionEdgeLabel.Cm.Decoration.modelInfo(stepNumber), resultModelId
				));

				// add all nested Annotation of the header, the annotations of the DecoModel are not used
				// the header annotations are not part of the unaltered elements
				referenceModelExpansionInfo.get().allAnnotationsForElementIdOrigins(LocalCoordinates.Annotation.MODEL_ANNOTATION)
						.forEach((annotationName, nestedOrigin) ->
								originsForResult.add(ExpansionNode.of(nestedOrigin.localCoordinates(), resultModelId, Set.of(
										ExpansionEdge.of(ExpansionEdgeLabel.JoinInternal.of(),
												nestedOrigin
										)
								)))
						);

				// add all (transitive) model connections
				originsForResult.add(ExpansionNode.of(LocalCoordinates.ModelItself.of(),
						resultModelId,
						ExpansionEdge.of(ExpansionEdgeLabel.JoinInternal.of(),
								referenceModelExpansionInfo.get().getOrigin(LocalCoordinates.ModelItself.of())),
						ExpansionEdge.of(ExpansionEdgeLabel.Cm.Decoration.modelInfo(stepNumber),
								decorationModelExpansionInfo.get().getOrigin(LocalCoordinates.ModelItself.of()))
				));
			}

			handleHeaderAndTypedefs();

			if (referenceModelExpansionInfo.isPresent() && decorationModelExpansionInfo.isPresent()) {
				return Optional.of(ExpansionInfo.of(referenceModelMutatedInPlaceToResultModel.getHeader().getId(),
						ExpansionNode.merge(originsForResult)));
			}
		}

		return Optional.empty();
	}

	protected ElementsDecoratorVisitor getElementsDecoratorVisitor(
			Map<String, Element> unalteredElementsFromPreviousStep) {
		return new ElementsDecoratorVisitor(unalteredElementsFromPreviousStep);
	}

	protected class ElementsDecoratorVisitor extends DocumentModelVisitor {
		private final Map<String, Element> unalteredElementsFromPreviousStep;

		public ElementsDecoratorVisitor(
				Map<String, Element> unalteredElementsFromPreviousStep) {
			this.unalteredElementsFromPreviousStep = unalteredElementsFromPreviousStep;
		}

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
					.collect(Collectors.toMap(com.mgmtp.a12.kernel.md.model.a12internal.Element::getName, Function.identity()));
			Group groupInResultModel = getGroupInResultModel(group);

			decorateRootGroupsIfNecessary(group, nameToElementMapper);

			for (int i = 0; i < elements.size(); i++) {
				offset = doDecoration(elements.get(i), nameToElementMapper, groupInResultModel, offset, i);
			}
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		protected void decorateRootGroupsIfNecessary(Group group,
				Map<String, Element> nameToElementMapper) {
			//implementation in subclass
		}

		protected int doDecoration(
				Element element,
				Map<String, Element> nameToElementMapper, Group groupInResultModel,
				int offset,
				int i) {
			if (clazz.isInstance(element)) {
				T elementToBeDecorated = clazz.cast(element);
				if (!selectionModelWrapper.isSelected(elementToBeDecorated) || additionalDecorationChecks(element)) {
					return offset;
				}
				DocumentModel decoModel = prepareDecoModelForElement(elementToBeDecorated);
				Group rootGroupInDecoModel = (Group) decoModel.getContent().getModelRoot().getElements().getFirst();
				List<Element> elementsBeforeAnchorField = getElementsBeforeAnchor(
						rootGroupInDecoModel);
				List<Element> elementsAfterAnchorField = getElementsAfterAnchor(
						rootGroupInDecoModel);
				// get the (possibly) adapted anchor element (annotation might be adapted),
				// the preparation (prepareDecoModelForElement) does not change the position of the anchor field,
				// so we can use the position to retrieve the field from the adapted rootGroupInDecoModel
				Element anchorElement = rootGroupInDecoModel.getElements()
						.get(numberOfElementsBeforeAnchorInDecorationModel);

				unalteredElementsFromPreviousStep.remove(anchorElement.getId());

				checkForNameCollisions(elementsBeforeAnchorField, elementsAfterAnchorField, nameToElementMapper);

				String path = ElementUtils.getPath(elementToBeDecorated);
				T elementInResultModel = resultModelSearchService.getByPath(path)
						.filter(clazz::isInstance).map(clazz::cast).orElseThrow();

				DocumentModelJoinUtil.AnnotationSourceCallback annotationSourceCallback =
						getAnnotationSourceCallback(elementToBeDecorated.getId(), anchorElement.getId(),
								elementToBeDecorated.getId());

				DocumentModelJoinUtil.determineAndSetJoinOfAnnotations(elementToBeDecorated.getAnnotations(),
						anchorElement.getAnnotations(),
						elementInResultModel::setAnnotations,
						() -> nameCollisionHandler.handleInconsistentAnnotations(clazz, elementInResultModel),
						problemReporter,
						annotationSourceCallback
				);

				if (!problemReporter.hasCriticalErrorOccurred()) { // there may be name collisions or problems caused by adjusting the rules and computations
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
			String path = ElementUtils.getPath(group);
			Optional<Element> optGroupInResultModel = resultModelSearchService.getByPath(
					path);

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

		if (!problemReporter.hasCriticalErrorOccurred() && !formerToNewFullNamesMapper.isEmpty()) {
			handleSortAndIndexFieldsInDecorationModel(decoModelCopy, formerToNewFullNamesMapper);
			adjustRulesAndComputationsInDecorationModel(decoModelCopy, formerToNewFullNamesMapper);
		}
		return decoModelCopy;
	}

	protected boolean noDecorationNecessary(Group group) {
		//implementation in subclass if necessary
		return false;
	}

	protected @NonNull DocumentModelJoinUtil.AnnotationSourceCallback getAnnotationSourceCallback(
			String existingElementId, String newElementId, String decoratedElementId) {
		String resultModelId = referenceModelMutatedInPlaceToResultModel.getHeader().getId();
		return new DocumentModelJoinUtil.AnnotationSourceCallback() {
			@Override public void onlyOld(String annotationName) {
				referenceModelExpansionInfo.ifPresent(ei ->
						originsForResult.add(ExpansionNode.of(
								LocalCoordinates.Annotation.of(annotationName, existingElementId), resultModelId, Set.of(
										ExpansionEdge.of(ExpansionEdgeLabel.JoinInternal.of(),
												ei.annotationOrigin(annotationName, existingElementId))
								)))
				);
			}

			@Override public void onlyNew(String annotationName) {
				decorationModelExpansionInfo.ifPresent(ei ->
						originsForResult.add(ExpansionNode.of(
								LocalCoordinates.Annotation.of(annotationName, existingElementId), resultModelId, Set.of(
										ExpansionEdge.of(
												ExpansionEdgeLabel.Cm.Decoration.of(stepNumber, decoratedElementId),
												ei.annotationOrigin(annotationName,
														newIdToFormerIdAndFullNameMapper.get(newElementId).getLeft()))
								)))
				);
			}

			@Override public void both(String annotationName) {
				if (referenceModelExpansionInfo.isPresent() && decorationModelExpansionInfo.isPresent()) {
					originsForResult.add(ExpansionNode.of(
							LocalCoordinates.Annotation.of(annotationName, existingElementId), resultModelId, Set.of(
									ExpansionEdge.of(ExpansionEdgeLabel.JoinInternal.of(),
											referenceModelExpansionInfo.get().annotationOrigin(annotationName, existingElementId)),
									ExpansionEdge.of(
											ExpansionEdgeLabel.Cm.Decoration.of(stepNumber, decoratedElementId),
											decorationModelExpansionInfo.get().annotationOrigin(annotationName,
													newIdToFormerIdAndFullNameMapper.get(newElementId).getLeft()))
							)));
				}
			}
		};
	}
}
