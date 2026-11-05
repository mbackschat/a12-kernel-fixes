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

import java.util.HashSet;
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
import com.mgmtp.a12.kernel.md.model.a12internal.LocalizedTextMapBuilder;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.ExpansionInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdge;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdgeLabel;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionNode;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.LocalCoordinates;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.BooleanType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.ConfirmType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.CustomFieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateFragmentType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateRangeType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateTimeType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.NumberType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.StringType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TimeType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TypeDefType;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DMWithExpansionInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.HeaderBuilderUtil;
import com.mgmtp.a12.kernel.md.model.internal.expansioninfo.ExpansionInfoUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ProblemReporter2Consumer;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelJoinUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelVisitorUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.IdUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.NameCollisionHandler;
import com.mgmtp.a12.kernel.md.model.internal.utils.ExpansionUtils;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderFactory;

import lombok.Getter;
import lombok.NonNull;

/**
 * Executor class to join an additive model using a {@link DocumentModelWalker} combined with the {@link ElementsAdderVisitor}. This
 * class is not immutable since the input root model is modified during execution and returned as result model.
 */
public class AdditiveModelJoiner {
	private final DocumentModel referenceModelMutatedInPlaceToResultModel;
	private final Optional<ExpansionInfo> referenceModelExpansionInfo;
	private final DocumentModel additiveModel;
	private final Optional<ExpansionInfo> additiveModelExpansionInfo;
	private final JoiningProblemReporter problemReporter;
	private final NameCollisionHandler nameCollisionHandler;
	private final DocumentModelVisitorUtil documentModelVisitorUtil;
	private final Map<String, String> origAddIdByModifiedAddId;
	private final Set<ExpansionNode> originsForResult = new HashSet<>();
	private final int stepNumber;

	public AdditiveModelJoiner(DMWithExpansionInfo referenceModelMutatedInPlaceToResultModel,
			DMWithExpansionInfo additiveModel,
			int stepNumber,
			Locale locale,
			JoiningProblemReporter problemReporter) {
		this.referenceModelMutatedInPlaceToResultModel = referenceModelMutatedInPlaceToResultModel.dm();
		this.referenceModelExpansionInfo = referenceModelMutatedInPlaceToResultModel.ei();
		this.additiveModel = additiveModel.dm();
		this.additiveModelExpansionInfo = additiveModel.ei();
		this.stepNumber = stepNumber;
		this.problemReporter = problemReporter;
		this.nameCollisionHandler = new NameCollisionHandler(DocumentModelJoinUtil.getModelString(this.additiveModel), locale,
				problemReporter);
		this.documentModelVisitorUtil = new DocumentModelVisitorUtil(this.referenceModelMutatedInPlaceToResultModel);
		this.origAddIdByModifiedAddId = new LinkedHashMap<>();
	}

	/**
	 * Adds elements that are contained in the given additional model to the result model. Elements that do not yet exist in the
	 * root model are added, already existing rules and computations are replaced. <br/>
	 * <b>Note:</b> References to error entities in rules or computed fields in computations are not
	 * updated here, but in {@link DocumentModelUtils#resolveAndUpdateReferences}.
	 */
	public @NonNull Optional<ExpansionInfo> addElements() {
		DocumentModel addModelCopy = DocumentModelCopyService.copy(additiveModel);
		DocumentModelWalker modelWalker = new DocumentModelWalker();

		ElementIdsReplacerVisitor elementIdsReplacerVisitor = new ElementIdsReplacerVisitor();
		modelWalker.acceptDocumentModel(addModelCopy, elementIdsReplacerVisitor);

		// get a list of all elements, so we can then have ElementsAdderVisitor remove the modified ones and use the remaining ones
		// later to find all unaltered elements of the resultModel
		// (the already existing "old" elements are not all iterated during the ElementsAdderVisitor)
		Set<String> untouchedElementsToUseFromReferenceModel = new HashSet<>();
		if (referenceModelExpansionInfo.isPresent()) {
			modelWalker.acceptDocumentModel(referenceModelMutatedInPlaceToResultModel, new DocumentModelVisitor() {
				@Override public VisitProcess visitElement(Element element) {
					untouchedElementsToUseFromReferenceModel.add(element.getId());
					return super.visitElement(element);
				}
			});
		}

		ElementsAdderVisitor elementsAdder = new ElementsAdderVisitor(untouchedElementsToUseFromReferenceModel);
		modelWalker.acceptDocumentModel(addModelCopy, elementsAdder);

		// check overlapping typdefs (the referenceModel and the additiveModel also contain all their transitive typeDefs)
		ProblemReporter2Consumer problemConsumer = new ProblemReporter2Consumer(problemReporter);
		for (var addedTypeDef : additiveModel.getContent().getTypeDefinitions()) {
			// we continue even when errors occur: they are added to the notifications, the user has to handle such cases
			boolean addTypeDef = ExpansionUtils.checkCanAddNewTypeDefAndReportError(
					referenceModelMutatedInPlaceToResultModel.getContent(), addedTypeDef, problemConsumer
			);
			if (addTypeDef) {
				referenceModelMutatedInPlaceToResultModel.getContent().getTypeDefinitions().add(addedTypeDef);
			}
		}

		referenceModelMutatedInPlaceToResultModel.getContent().getDocumentUniquenessCriteria()
				.addAll(addModelCopy.getContent().getDocumentUniquenessCriteria());

		// add remaining to the ExpansionInfo as those are all used
		if (referenceModelExpansionInfo.isPresent() && additiveModelExpansionInfo.isPresent()) {
			String resultModelId = referenceModelMutatedInPlaceToResultModel.getHeader().getId();
			untouchedElementsToUseFromReferenceModel.forEach(element ->
					originsForResult.addAll(ExpansionInfoUtils.forwardOriginForSingleElementWithAnnotations(
							element, resultModelId, ExpansionEdgeLabel.JoinInternal.of(), id -> id,
							referenceModelExpansionInfo.get()
					))
			);

			// add all (transitive) modelReferences, typedefs and document uniqueness criteria of the base model, those are only the
			// unmanaged modelReferences, see DocumentModelExpandService#MANAGED_PURPOSES
			originsForResult.addAll(ExpansionInfoUtils.forwardOrigins(referenceModelExpansionInfo.get(), coords ->
							coords instanceof LocalCoordinates.ModelReference
									|| coords instanceof LocalCoordinates.Typedef
									|| coords instanceof LocalCoordinates.DocumentUniquenessCriterion,
					ExpansionEdgeLabel.JoinInternal.of(), resultModelId
			));

			// add all (transitive) modelReferences, typedefs and document uniqueness criteria of the additive model, those will be
			// merged with the one from base.
			// the typedefs will be linked in the EI regardless of the (valid) overlapping of the typedefs
			originsForResult.addAll(ExpansionInfoUtils.forwardOrigins(additiveModelExpansionInfo.get(), coords ->
							coords instanceof LocalCoordinates.ModelReference
									|| coords instanceof LocalCoordinates.Typedef
									|| coords instanceof LocalCoordinates.DocumentUniquenessCriterion,
					ExpansionEdgeLabel.Cm.Addition.of(stepNumber), resultModelId
			));

			// add all (transitive) model connections
			originsForResult.add(ExpansionNode.of(LocalCoordinates.ModelItself.of(),
					resultModelId,
					ExpansionEdge.of(ExpansionEdgeLabel.JoinInternal.of(),
							referenceModelExpansionInfo.get().getOrigin(LocalCoordinates.ModelItself.of())),
					ExpansionEdge.of(ExpansionEdgeLabel.Cm.Addition.of(stepNumber),
							additiveModelExpansionInfo.get().getOrigin(LocalCoordinates.ModelItself.of()))
			));

			return Optional.of(ExpansionInfo.of(referenceModelMutatedInPlaceToResultModel.getHeader().getId(),
					ExpansionNode.merge(originsForResult)));
		} else {
			return Optional.empty();
		}
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
				referenceModelMutatedInPlaceToResultModel.getContent().getTypeDefinitions().stream().map(FieldTypeDefinition::getId)
						.collect(Collectors.toSet());
		private final Set<String> untouchedElementsFromPreviousStep;

		public ElementsAdderVisitor(Set<String> untouchedElementsToUseFromReferenceModel) {
			this.untouchedElementsFromPreviousStep = untouchedElementsToUseFromReferenceModel;
		}

		@Override
		public VisitProcess visitHeader(Header header) {
			DocumentModelJoinUtil.determineAndSetJoinOfAnnotations(
					referenceModelMutatedInPlaceToResultModel.getHeader().getAnnotations(),
					header.getAnnotations(),
					annotations -> referenceModelMutatedInPlaceToResultModel.setHeader(
							getCopyHeaderBuilder().withAnnotations(annotations).build()),
					() -> nameCollisionHandler.handleInconsistentAnnotations(Header.class, null),
					problemReporter, getAnnotationSourceCallbackForHeader()
			);

			DocumentModelJoinUtil.mergeModelReferences(referenceModelMutatedInPlaceToResultModel, header);
			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		private HeaderFactory.Builder getCopyHeaderBuilder() {
			return HeaderBuilderUtil.toBuilder(referenceModelMutatedInPlaceToResultModel.getHeader());
		}

		@Override
		public VisitProcess visitElement(Element element) {
			String path = ElementUtils.getPath(element);
			Optional<Element> elementInResultModel = documentModelVisitorUtil.getResultElement(path);

			Group resultParentGroup = documentModelVisitorUtil.getResultParentGroup(path);

			if (elementInResultModel.isEmpty()) {
				handleNewElement(resultParentGroup, element);
				addOriginsForElementsFromNewElement(element);
				return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
			} else if (!element.getClass().equals(elementInResultModel.get().getClass())) {
				// Could be the case if a field or rule already has the same full name
				nameCollisionHandler.handleNameCollision(element, elementInResultModel.get(), path);
				handleDuplicateElement(resultParentGroup, elementInResultModel.get(), element);
				return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
			} else {
				handleExistingElement(resultParentGroup, elementInResultModel.get(), element);
				// symmetric equivalent to addOriginsForElementsFromNewElement happens via unalteredElementsFromPreviousStep
			}

			return VisitProcess.CONTINUE_TRAVERSAL;
		}

		private void addOriginsForElementsFromNewElement(Element newElement) {
			additiveModelExpansionInfo.ifPresent(additiveDmEi ->
					new DocumentModelWalker().acceptElements(List.of(newElement), new DocumentModelVisitor() {
						@Override public VisitProcess visitElement(Element element) {
							String resultModelId = referenceModelMutatedInPlaceToResultModel.getHeader().getId();
							originsForResult.addAll(
									ExpansionInfoUtils.forwardOriginForSingleElementWithAnnotations(element.getId(), resultModelId,
											ExpansionEdgeLabel.Cm.Addition.of(stepNumber), origAddIdByModifiedAddId::get,
											additiveDmEi
									));

							return super.visitElement(element);
						}
					}));
		}

		private void handleNewElement(Group resultParent, Element newElement) {
			int posForNewElem = newElementPositionAnchorBased(resultParent, newElement);
			resultParent.addElement(posForNewElem, newElement);
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
		private static int newElementPositionAnchorBased(Group resultParent,
				Element newElement) {
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
					.map(com.mgmtp.a12.kernel.md.model.a12internal.Element::getName)
					// after this filter, we have the names of all elems that are anchor elements for newElement:
					.filter(resultSiblingNamesToPos::containsKey) // note: the general join consistency check should additionally
					// ensure that the element of same name has the same type both in source and result
					.mapToInt(resultSiblingNamesToPos::get)
					.reduce(resultParent.getElements().size(), Math::min);
		}

		private void handleDuplicateElement(Group resultParent, Element resultElement,
				Element newElement) {
			if (referenceModelExpansionInfo.isPresent() && additiveModelExpansionInfo.isPresent()) {
				originsForResult.add(ExpansionNode.of(
						LocalCoordinates.Element.of(newElement.getId()),
						referenceModelMutatedInPlaceToResultModel.getHeader().getId(),
						Set.of(
								ExpansionEdge.of(ExpansionEdgeLabel.JoinInternal.of(),
										referenceModelExpansionInfo.get().elementOriginById(resultElement.getId())),
								ExpansionEdge.of(ExpansionEdgeLabel.Cm.Addition.of(stepNumber),
										additiveModelExpansionInfo.get().elementOriginById(
												origAddIdByModifiedAddId.get(newElement.getId())))
						)));
				untouchedElementsFromPreviousStep.remove(resultElement.getId());
			}

			// we put the duplicates together in order for the user to identify them easily
			int originalPos = resultParent.getElements().indexOf(resultElement);
			resultParent.addElement(originalPos + 1, newElement);
		}

		private void handleExistingElement(Group resultParent, Element existingElement,
				Element newElement) {

			String elementIdInResult = null;
			boolean completelyOverwritesOriginalElement = false;
			if (newElement instanceof Rule || newElement instanceof Computation) {
				// the new element completely replaces the one in the base, nothing will be merged
				elementIdInResult = newElement.getId();
				completelyOverwritesOriginalElement = true;
				resultParent.replaceElement(existingElement, newElement);
			} else if (existingElement instanceof Group existingGroup) {
				elementIdInResult = existingElement.getId();
				DocumentModelJoinUtil.AnnotationSourceCallback annotationSourceCallback =
						getAnnotationSourceCallback(existingElement.getId(), newElement.getId(), elementIdInResult);
				handleExistingGroup(existingGroup, (Group) newElement, annotationSourceCallback);
			} else if (existingElement instanceof Field existingField) {
				elementIdInResult = existingElement.getId();
				DocumentModelJoinUtil.AnnotationSourceCallback annotationSourceCallback =
						getAnnotationSourceCallback(existingElement.getId(), newElement.getId(), elementIdInResult);
				handleExistingField(existingField, (Field) newElement, annotationSourceCallback);
			}

			assert elementIdInResult != null : "unknown element type";

			if (referenceModelExpansionInfo.isPresent() && additiveModelExpansionInfo.isPresent()) {
				// NOTE: the expansionInfo handling is currently a bit messily spread across annotationSourceCallback in the
				// "merge" cases above, and the handling of the element itself and of the "fully replace" case below.
				// -> maybe clean this up at some point.
				String resultModelId = referenceModelMutatedInPlaceToResultModel.getHeader().getId();
				if (completelyOverwritesOriginalElement) {
					// A12K-4294: For Rule/Computation wholesale-replacement, the per-annotation EI
					// entries are emitted here (no determineAndSetJoinOfAnnotations is called for this
					// path). Each annotation on the overwriting element gets an EI entry with a
					// single Cm.Addition(stepNumber) edge pointing to the corresponding annotation in
					// the ADD-DM, symmetric to the Field/Group merge path's AnnotationSourceCallback.
					originsForResult.addAll(ExpansionInfoUtils.forwardOriginForSingleElementWithAnnotations(
							elementIdInResult, resultModelId,
							ExpansionEdgeLabel.Cm.Addition.of(stepNumber),
							origAddIdByModifiedAddId::get,
							additiveModelExpansionInfo.get()));
				} else {
					// in the merge case, annotation merge logic is somewhat more complex, and the ExpansionNode handling
					// happens via determineAndSetJoinOfAnnotations, so in this case we only add the ExpansionNodes for the
					// element itself here:
					originsForResult.add(ExpansionNode.of(
							LocalCoordinates.Element.of(elementIdInResult),
							resultModelId,
							ExpansionEdge.of(ExpansionEdgeLabel.JoinInternal.of(),
									referenceModelExpansionInfo.get().elementOriginById(existingElement.getId())),
							ExpansionEdge.of(ExpansionEdgeLabel.Cm.Addition.of(stepNumber),
									additiveModelExpansionInfo.get().elementOriginById(
											origAddIdByModifiedAddId.get(newElement.getId())))
					));
				}

				untouchedElementsFromPreviousStep.remove(existingElement.getId());
			}
		}

		private void handleExistingGroup(Group existingGroup, Group newGroup,
				DocumentModelJoinUtil.AnnotationSourceCallback annotationSourceCallback) {
			DocumentModelJoinUtil.determineAndSetJoinOfAnnotations(existingGroup.getAnnotations(), newGroup.getAnnotations(),
					existingGroup::setAnnotations,
					() -> nameCollisionHandler.handleInconsistentAnnotations(Group.class, newGroup),
					problemReporter,
					annotationSourceCallback
			);

			assert existingGroup.getIncludeConfig().isEmpty() : "Expected that Model Addition deals with expanded models " +
					"which " +
					"should not contain any includeConfig" +
					" ";
			assert newGroup.getIncludeConfig().isEmpty() : "Expected that Model Addition deals with expanded models that " +
					"should not contain any includeConfig" +
					" ";
		}

		private void handleExistingField(Field existingField, Field newField,
				DocumentModelJoinUtil.AnnotationSourceCallback annotationSourceCallback) {
			DocumentModelJoinUtil.determineAndSetJoinOfAnnotations(existingField.getAnnotations(),
					newField.getAnnotations(),
					existingField::setAnnotations,
					() -> nameCollisionHandler.handleInconsistentAnnotations(Field.class, newField),
					problemReporter,
					annotationSourceCallback
			);

			var existingElementType = existingField.getEffectiveType().orElse(existingField.getFieldType());
			var newElementType = newField.getEffectiveType().orElse(newField.getFieldType());

			// report problem of unresolved TD (effective field type cannot be extracted); comparing the field types further is
			// pointless in that case, so stop here after reporting
			boolean unresolved = false;

			boolean unresolvedExistingElementType = existingElementType instanceof TypeDefType;
			boolean unresolvedNewElementType = newElementType instanceof TypeDefType;

			if (unresolvedExistingElementType && unresolvedNewElementType &&
					((TypeDefType) existingElementType).getId().equals(((TypeDefType) newElementType).getId())) {
				// if both are unresolved, but have the same ID, we do not have to check anything, they could become the same when the TD is added later,
				// the validation is then later responsible to check if the type is resolvable
				unresolved = true;
			} else {
				if (unresolvedExistingElementType) {
					nameCollisionHandler.handleFieldTypeNotResolvable(existingField, false);
					unresolved = true;
				}
				if (unresolvedNewElementType) {
					nameCollisionHandler.handleFieldTypeNotResolvable(newField, true);
					unresolved = true;
				}
			}

			if (unresolved) {
				return;
			}

			boolean sameType = existingElementType.getClass().equals(newElementType.getClass());
			String existingTypeName = getTypeName(existingElementType);
			String newTypeName = getTypeName(newElementType);

			if (!sameType) {
				nameCollisionHandler.handleFieldTypeDivergence(existingField,
						existingTypeName,
						newTypeName);
			} else if (!sameConfiguration(existingElementType, newElementType)) {
				// same type, different configuration
				nameCollisionHandler.handleFieldTypeDifferentConfiguration(existingField,
						existingTypeName);
			}
		}

		/**
		 * Compares two field types of the same class for configuration equality. Enum value display labels are intentionally
		 * ignored, i.e. two {@link EnumerationType}s that only differ in their {@link EnumerationType.EnumValue} labels are
		 * considered to have the same configuration.
		 */
		private static boolean sameConfiguration(FieldType existingElementType, FieldType newElementType) {
			if (existingElementType instanceof EnumerationType existingEnum && newElementType instanceof EnumerationType newEnum) {
				return withoutEnumValueLabels(existingEnum).equals(withoutEnumValueLabels(newEnum));
			}
			return existingElementType.equals(newElementType);
		}

		private static EnumerationType withoutEnumValueLabels(EnumerationType enumerationType) {
			List<EnumerationType.EnumValue> valuesWithoutLabels = enumerationType.getValues().stream()
					.map(value -> value.toBuilder().label(new LocalizedTextMapBuilder().build()).build())
					.toList();
			return enumerationType.toBuilder().values(valuesWithoutLabels).build();
		}

		private static String getTypeName(FieldType existingElementType) {
			return switch (existingElementType) {
				// this case should never be relevant, as unresolved typedefs lead to early return in the implementation
				case TypeDefType td -> String.format("unresolvable type with id '%s'", td.getId());
				case CustomFieldType cft -> String.format("Custom %s", cft.getName());
				case StringType t -> "String";
				case NumberType t -> "Number";
				case DateType t -> "Date";
				case DateTimeType t -> "Date Time";
				case TimeType t -> "Time";
				case DateFragmentType t -> "Date Fragment";
				case DateRangeType t -> "Date Range";
				case ConfirmType t -> "Confirm";
				case BooleanType t -> "Boolean";
				case EnumerationType t -> "Enumeration";
				default -> throw new UnsupportedOperationException("Unknown field type: " + existingElementType.getClass().getSimpleName());
			};
		}

		@Override
		public VisitProcess visitFieldTypeDefinition(FieldTypeDefinition fieldTypeDefinition) {
			String typeDefId = fieldTypeDefinition.getId();

			additiveModelExpansionInfo.ifPresent(ei -> {
				LocalCoordinates.Typedef localCoordinates = LocalCoordinates.Typedef.of(fieldTypeDefinition.getId());
				originsForResult.add(ExpansionNode.of(
						localCoordinates,
						referenceModelMutatedInPlaceToResultModel.getHeader().getId(),
						ExpansionEdge.of(
								ExpansionEdgeLabel.Cm.Addition.of(stepNumber),
								ei.getOrigin(localCoordinates)
						)
				));
			});

			if (!typeDefinitionIdsRoot.contains(typeDefId)) {
				referenceModelMutatedInPlaceToResultModel.getContent().getTypeDefinitions().add(fieldTypeDefinition);
				// should not be necessary, but makes sure that no typedef is used multiple times
				typeDefinitionIdsRoot.add(typeDefId);
			}

			return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
		}

		private @NonNull DocumentModelJoinUtil.AnnotationSourceCallback getAnnotationSourceCallbackForHeader() {
			return getAnnotationSourceCallback(LocalCoordinates.Annotation.MODEL_ANNOTATION,
					LocalCoordinates.Annotation.MODEL_ANNOTATION, LocalCoordinates.Annotation.MODEL_ANNOTATION);
		}

		private @NonNull DocumentModelJoinUtil.AnnotationSourceCallback getAnnotationSourceCallback(
				@NonNull String existingElementId, @NonNull String newElementId, @NonNull String elementIdInResult) {
			String resultModelId = referenceModelMutatedInPlaceToResultModel.getHeader().getId();
			return new DocumentModelJoinUtil.AnnotationSourceCallback() {
				@Override public void onlyOld(@NonNull String annotationName) {
					referenceModelExpansionInfo.ifPresent(ei -> originsForResult.add(ExpansionNode.of(
							LocalCoordinates.Annotation.of(annotationName, elementIdInResult), resultModelId, Set.of(
									ExpansionEdge.of(ExpansionEdgeLabel.JoinInternal.of(),
											ei.annotationOrigin(annotationName,
													existingElementId))
							))));
				}

				@Override public void onlyNew(@NonNull String annotationName) {
					additiveModelExpansionInfo.ifPresent(ei -> originsForResult.add(ExpansionNode.of(
							LocalCoordinates.Annotation.of(annotationName, elementIdInResult), resultModelId, Set.of(
									ExpansionEdge.of(
											ExpansionEdgeLabel.Cm.Addition.of(stepNumber),
											ei.annotationOrigin(annotationName,
													newElementId.equals(LocalCoordinates.Annotation.MODEL_ANNOTATION) ?
															newElementId :
															origAddIdByModifiedAddId.get(newElementId)))
							))));
				}

				@Override public void both(@NonNull String annotationName) {
					if (referenceModelExpansionInfo.isPresent() && additiveModelExpansionInfo.isPresent()) {
						originsForResult.add(
								ExpansionNode.of(
										LocalCoordinates.Annotation.of(annotationName, elementIdInResult), resultModelId,
										Set.of(
												ExpansionEdge.of(ExpansionEdgeLabel.JoinInternal.of(),
														referenceModelExpansionInfo.get()
																.annotationOrigin(annotationName, existingElementId)),
												ExpansionEdge.of(
														ExpansionEdgeLabel.Cm.Addition.of(stepNumber),
														additiveModelExpansionInfo.get().annotationOrigin(annotationName,
																newElementId.equals(LocalCoordinates.Annotation.MODEL_ANNOTATION) ?
																		newElementId : origAddIdByModifiedAddId.get(newElementId)))
										)));
					}
				}
			};
		}
	}
}
