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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstanceDerivable;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.IGroupInstance;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSearchService;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentService;
import com.mgmtp.a12.kernel.md.document.api.services.IEntityInstanceChangeInfo;
import com.mgmtp.a12.kernel.md.document.internal.service.DocumentFactoryImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.impl.FieldInstanceImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.impl.GroupInstanceImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.utils.DocumentUtils;
import com.mgmtp.a12.kernel.md.document.internal.service.utils.EntityInstanceUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelResolverUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.RankedNotificationUtils;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.check.DocumentChecker;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.compare.DocumentCompare;
import com.mgmtp.a12.model.notification.RankedNotification;

/**
 * @deprecated use {@link com.mgmtp.a12.kernel.md.document.apiV2.services.IDmAwareDocService} and
 *        {@link com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils} instead.
 */
@Deprecated(since = "30.2.0")
public class DocumentServiceImpl implements IDocumentService {
	private final IDocument document;
	private final IDocumentSearchService documentSearchService;
	private final IDocumentModel documentModel;
	private final IDocumentModelResolver documentModelResolver;
	private final IDocumentModelSearchService documentModelSearchService;
	/**
	 * we delegate to dmAwareDocV2Service for methods that only use the dm / dmSearchService and not the document.
	 */
	private final DmAwareDocServiceImpl dmAwareDocV2Service;

	public DocumentServiceImpl(IDocument document, IDocumentModelResolver documentModelResolver) {
		this.document = document;
		this.documentModelResolver = documentModelResolver;
		this.documentModel = documentModelResolver.getDocumentModelById(document.getDocumentModelId());
		this.documentModelSearchService = DocumentModelResolverUtils.getOrCreateSearchService(documentModelResolver,
				document.getDocumentModelId());
		this.documentSearchService = new DocumentSearchServiceImpl(this.document, this.documentModelSearchService);
		this.dmAwareDocV2Service = new DmAwareDocServiceImpl(this.documentModel, this.documentModelSearchService);
	}

	private DocumentServiceImpl(IDocument document, IDocumentModelResolver documentModelResolver, IDocumentModel documentModel,
			IDocumentModelSearchService documentModelSearchService) {
		this.document = document;
		this.documentModelResolver = documentModelResolver;
		this.documentModel = documentModel;
		this.documentModelSearchService = documentModelSearchService;
		this.documentSearchService = new DocumentSearchServiceImpl(this.document, this.documentModelSearchService);
		this.dmAwareDocV2Service = new DmAwareDocServiceImpl(this.documentModel, this.documentModelSearchService);
	}

	@Override
	public Object convertToJavaType(IFieldInstance fieldInstance, String value, Consumer<RankedNotification> notificationReceiver) {
		return this.dmAwareDocV2Service.convertToJavaType(fieldInstance, value, notificationReceiver);
	}

	@Override public Object convertToJavaTypeV2(String fieldFullName, String value,
			Consumer<RankedNotification> notificationReceiver) {
		return this.dmAwareDocV2Service.convertToJavaTypeV2(fieldFullName, value, notificationReceiver);
	}

	@Override
	public void setValue(Object value, String path, int[] repetitions, IDocumentFactory documentFactory) {
		Validate.notBlank(path);
		setValueInternal(value, path, repetitions, true, documentFactory);
	}

	private void setValueInternal(Object value, String path, int[] repetitions, boolean createNewFieldInstance,
			IDocumentFactory documentFactory) {
		IFieldInstance fi = getFieldInstance(path, repetitions, createNewFieldInstance, documentFactory);
		setValueToFieldInstance(value, fi);
	}

	private void setValueToFieldInstance(Object value, IFieldInstance fi) {
		document.removeEntityInstance(fi);
		Object valueToSet = value;
		if (value instanceof String) {
			List<RankedNotification> notifications = new ArrayList<>();
			valueToSet = convertToJavaType(fi, (String) value, notifications::add);
			if (!notifications.isEmpty()) {
				throw new IllegalArgumentException(
						String.format("While converting the value '%s', following errors were occurred:\n%s", value,
								RankedNotificationUtils.joinMessages(notifications)));
			}
		}
		fi.setValue(valueToSet);
		document.addEntityInstance(fi);
	}

	private IFieldInstance getFieldInstance(String path, int[] repetitions, boolean createNewFieldInstance,
			IDocumentFactory documentFactory) {
		Optional<IFieldInstance> fiOpt = documentSearchService.getFieldInstance(path, repetitions);
		if (fiOpt.isEmpty()) {
			if (!createNewFieldInstance) {
				throw new IllegalStateException(
						String.format("Field instance with path '%s' and repetitions '%s' was not found.", path, repetitions));
			}
			IDocumentFactory documentFactoryToUse = documentFactory;
			if (documentFactory == null) {
				documentFactoryToUse = new DocumentFactoryImpl();
			}
			return documentFactoryToUse.createFieldInstance(path, repetitions);
		} else {
			return fiOpt.get();
		}
	}

	@Override
	public void setValue(Object value, String path, int[] repetitions) {
		Validate.notBlank(path);
		setValueInternal(value, path, repetitions, false, null);
	}

	@Override
	public void setValue(Object value, IFieldInstance fieldInstance) {
		Objects.requireNonNull(fieldInstance);
		setValueToFieldInstance(value, fieldInstance);
	}

	@Override
	public boolean checkEntities(Consumer<RankedNotification> notificationReceiver) {
		Objects.requireNonNull(notificationReceiver);
		return new DocumentChecker(documentModelResolver, documentModel, documentModelSearchService)
				.checkEntities(document, notificationReceiver);
	}

	@Override
	public boolean checkPresenceOfFieldInstancesInNonRepeatableGroups(Consumer<RankedNotification> notificationReceiver) {
		Objects.requireNonNull(notificationReceiver);
		return new DocumentChecker(documentModelResolver, documentModel, documentModelSearchService)
				.checkPresenceOfFieldInstancesInNonRepeatableGroups(document, notificationReceiver);
	}

	@Override
	public boolean removeEntityTree(IGroupInstance groupInstance) {
		return removeEntityTree(
				groupInstance,
				(e, newRepetitions) -> {
					if (e instanceof IEntityInstanceDerivable eiDerivable) {
						return eiDerivable.withRepetitions(newRepetitions);
					} else if (e instanceof IGroupInstance) {
						return new GroupInstanceImpl(e.getPath(), newRepetitions);
					} else if (e instanceof IFieldInstance fi) {
						var f = new FieldInstanceImpl(e.getPath(), newRepetitions);
						f.setValue(fi.getValue().orElse(null));
						return f;
					} else {
						throw new IllegalArgumentException("IEntityInstance of type " + e.getClass() + " was not expected");
					}
				});
	}

	public boolean removeEntityTree(
			IGroupInstance groupInstance,
			BiFunction<IEntityInstance, int[], IEntityInstance> deriveEntityInstanceWithNewRepetitions) {
		Objects.requireNonNull(groupInstance);
		Set<IEntityInstance> entityInstancesToRemove = document.getEntityInstances().stream()
				.filter(ei -> ElementPathUtils.isDescendantOrEqual(ei.getPath(), groupInstance.getPath()))
				.filter(ei -> DocumentUtils.contains(groupInstance.getRepetitions(), ei.getRepetitions()))
				.collect(Collectors.toSet());
		boolean resultStatus = entityInstancesToRemove.stream()
				.map(document::removeEntityInstance)
				.reduce(false, (a, b) -> a || b);
		removeGaps(
				document,
				ei -> canMaybeAffectOrBeAffectedByRemoval(ei, groupInstance), // this filter is only a performance optimization
				deriveEntityInstanceWithNewRepetitions);
		return resultStatus;
	}

	/**
	 * Remove gaps in the repetitions of repeatable groups that may be present in a document.<br/>
	 * Optionally allows to filter the considered entityInstances as a performance optimization, if it is known that e.g. only
	 * instances that are within a certain group and with certain repetition indexes can be affected (this happens e.g. after an
	 * instance of a repeatable group has been removed from a previously-gapless document).
	 */
	private static void removeGaps(
			IDocument document,
			Predicate<IEntityInstance> onlyConsiderMatchingEIs,
			BiFunction<IEntityInstance, int[], IEntityInstance> deriveWithNewRepetitions) {

		// Algorithm Description:
		//
		// Definition: RepeatableGroupInstance: Group with path and repetitions, but without the last level of repetition.
		//              e.g. /RGx[1]/Gx[2]/G2x[1]
		//                   /RGx[1]/Gx[2]/G2x[2]
		//                   /RGx[1]/Gx[2]/G2x[3]
		//              all belong to the same RepeatableGroupInstance, but
		//                   /RGx[2]/Gx[2]/G2x[1]
		//                   /RGx[1]/Gx[3]/G2x[1]
		//              both belong to a different RepeatableGroupInstance (each).
		//
		// Algorithm: we first sort the entityInstances lexicographically by repetitions. This ensures that lower repetitions of a
		// repeatable group occur before higher repetitions of the same group.
		//
		// Then we iterate over this sorted sequence of entiyInstaces and all levels of each entityInstance's repetitions
		// (including those where the repetition is 1, for simplicity). We record which repetitions are occupied for
		// the corresponding RepeatableGroupInstances (keeping this occupancy info across the outer iteration over entityInstances)
		// and if gaps are encountered, we update the repetitions of the entityInstances so that the gap is closed.
		//
		// The order of the sorted elements guarantees that when we encounter a gap in the repetitions it is indeed a gap, and we
		// can be sure that no subsequent entityInstance exists that occupies that gap.
		//
		// Note that entityInstances are not allowed to be modified in-place, instead they are replaced by a corresponding new
		// entityInstance that is otherwise the same but has the new repetitions.

		List<IEntityInstance> sortedFiltered = document.getEntityInstances().stream()
				.filter(onlyConsiderMatchingEIs)
				.sorted((eiA, eiB) -> Java8Utils.compare(eiA.getRepetitions(), eiB.getRepetitions()))
				.toList();

		Map<Pair<String, List<Integer>>, RepetitionUpdate> highestOccupiedRepetitionsByGroup = new HashMap<>();
		for (IEntityInstance ei : sortedFiltered) {
			int[] newRepetitions = computeNewRepetitionsAndUpdateOccupiedRepetitions(ei, highestOccupiedRepetitionsByGroup);
			if (!Arrays.equals(ei.getRepetitions(), newRepetitions)) {
				document.removeEntityInstance(ei);
				document.addEntityInstance(deriveWithNewRepetitions.apply(ei, newRepetitions));
			}
		}
	}

	private static int[] computeNewRepetitionsAndUpdateOccupiedRepetitions(
			IEntityInstance ei,
			Map<Pair<String, List<Integer>>, RepetitionUpdate> highestOccupiedRepetitionsByGroup
	) {
		int[] newRepetitions = ei.getRepetitions().clone(); // We must not edit e, or we'd corrupt the document's hashSet
		for (int level = 0; level < ei.getRepetitions().length; level++) {
			// at this point we don't know if there exists > 1 repeatability at the current level because we don't know
			// the later entityInstances yet -> we treat all levels as if they *might* be repeatable
			IGroupInstance possiblyRepeatableGroup = EntityInstanceUtils.getAncestorGroup(ei, level);
			Pair<String, List<Integer>> repetitionsKey =
					EntityInstanceUtils.commonKeyForAllRepetitionsOfRepeatableGroup(possiblyRepeatableGroup);

			if (!highestOccupiedRepetitionsByGroup.containsKey(repetitionsKey)) {
				highestOccupiedRepetitionsByGroup.put(repetitionsKey, new RepetitionUpdate(0, 0));
			}
			RepetitionUpdate highestOccupied = highestOccupiedRepetitionsByGroup.get(repetitionsKey);

			if (ei.getRepetitions()[level] != highestOccupied.originalRepetition) {
				highestOccupied.originalRepetition = ei.getRepetitions()[level];
				highestOccupied.newRepetition++;
			}
			newRepetitions[level] = highestOccupied.newRepetition;
		}
		return newRepetitions;
	}

	/**
	 * Check if Entity Instance ei can possibly be relevant during {@link #removeEntityTree(IGroupInstance)} of Group Instance
	 * giToDelete. Otherwise, ei can be ignored during that operation (performance optimization).
	 * </p>
	 * There are at least the following scenarios where ei can be relevant:
	 * <ul>
	 *     <li>ei is affected, because it is in a higher repetition of a repeatable group than the removed group instances, so
	 *     that ei's repetitions will be decremented during the {@link #removeGaps(IDocument, Predicate, BiFunction)} step in
	 *     {@link #removeEntityTree(IGroupInstance)}</li>
	 *
	 *     <li>the existence of ei affects the removal, because it is in a lower or equal repetition than the removed group
	 *     instance, so that the presence of ei means that a potential gap in the repetitions does not exist</li>
	 * </ul>
	 * Therefore, we can only be sure that ei is not relevant, if we know that it does not share ar repeatable ancestor group with
	 * giToDelete. As we don't consider the DM here, we can only be sure to be in this case if the very first path parts of ei
	 * and giToDelete differ.
	 */
	private boolean canMaybeAffectOrBeAffectedByRemoval(IEntityInstance ei, IGroupInstance giToDelete) {
		String[] eiPathParts = EntityInstanceUtils.getPathParts(ei.getPath());
		String[] groupPathParts = EntityInstanceUtils.getPathParts(giToDelete.getPath());
		return eiPathParts[0].equals(groupPathParts[0]);
	}

	private static class RepetitionUpdate {
		int newRepetition;
		int originalRepetition;

		public RepetitionUpdate(int originalRepetition, int newRepetition) {
			this.originalRepetition = originalRepetition;
			this.newRepetition = newRepetition;
		}
	}

	@Override
	public IDocumentService copyDocument(IDocumentFactory documentFactory) {
		IDocumentFactory usedFactory = documentFactory == null ? new DocumentFactoryImpl() : documentFactory;
		IDocument newDocument = DocumentUtils.copy(document, usedFactory);
		return new DocumentServiceImpl(newDocument, documentModelResolver, documentModel, documentModelSearchService);
	}

	@Override
	public boolean removeTransientFields() {
		return DocumentUtils.removeTransientFields(document, documentModel);
	}

	@Override public boolean removeEmptyFieldsAndGroups() {
		return DocumentUtils.removeEmptyFieldsAndGroups(document);
	}

	@Override
	public boolean removeUnknownEntityInstances() {
		return DocumentUtils.removeUnknownEntityInstances(document, documentModelSearchService);
	}

	@Override
	public IDocument getDocument() {
		return document;
	}

	@Override
	public IDocumentModel getDocumentModel() {
		return documentModel;
	}

	@Override
	public IDocumentModelSearchService getDocumentModelSearchService() {
		return documentModelSearchService;
	}

	@Override
	public Collection<IEntityInstanceChangeInfo> compare(IDocument reference) {
		return new DocumentCompare().compare(this.document, reference);
	}

}
