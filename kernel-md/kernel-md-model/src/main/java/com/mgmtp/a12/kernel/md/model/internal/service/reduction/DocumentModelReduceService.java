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
package com.mgmtp.a12.kernel.md.model.internal.service.reduction;

import static com.mgmtp.a12.kernel.md.model.internal.service.utils.WrappedProblemWithSeverityLimitAndOrigin.forwardAsNonError;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.core.base.utils.internal.Consumer2ProblemReporter;
import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.facade.internal.MVKService;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.LocalizedTextMapBuilder;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.ConfirmType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.ElementContainerImpl;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.Entity;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.Rule;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization.SerializationUtils;
import com.mgmtp.a12.model.notification.RankedNotification;

/**
 * Offers the functionality to create a reduced {@link DocumentModel}.
 * <br/>
 * See {@link DocumentModelService#createReducedDocumentModel(DocumentModel, Set, Set, Consumer)}
 */
public class DocumentModelReduceService {

	private static final String CONFIRM_FIELD_NAME = "RootgroupFilledMarker";
	private final DocumentModel documentModel;
	private final MVKService mvkService;
	private final ElementContainerImpl elementContainer;

	public DocumentModelReduceService(final DocumentModel documentModel) {
		Objects.requireNonNull(documentModel);
		this.documentModel = documentModel;
		DocumentModelService documentModelService = new DocumentModelService();
		// following casts should be ok, otherwise we have an implementation error
		this.mvkService = (MVKService) documentModelService.getMvkServiceForModel(documentModel, null);
		this.elementContainer = ((ElementContainerImpl) mvkService.getIEC());
	}

	public Optional<DocumentModel> reduce(Set<String> selectedElementIds, Set<String> partlySelectedElementIds,
			Consumer<RankedNotification> notificationReceiver) {
		Validate.notEmpty(selectedElementIds);
		Objects.requireNonNull(partlySelectedElementIds);
		Objects.requireNonNull(notificationReceiver);
		Consumer2ProblemReporter pr = new Consumer2ProblemReporter(notificationReceiver);
		Set<IGroup> directlyReferencedRootGroups = new HashSet<>();
		Set<String> elementIdsInResult = new HashSet<>(selectedElementIds);
		elementIdsInResult.addAll(partlySelectedElementIds); // Ancestors of selectedElementIds

		addReferencedEntitiesInRules(selectedElementIds, elementIdsInResult, directlyReferencedRootGroups, pr);
		if (pr.hasErrorOccurred()) {
			return Optional.empty();
		}

		DocumentModel reducedDm = SerializationUtils.copy(
				documentModel, elementIdsInResult, forwardAsNonError(notificationReceiver));

		createConfirmFields(directlyReferencedRootGroups, reducedDm);
		return Optional.of(reducedDm);
	}

	private void addReferencedEntitiesInRules(Set<String> selectedElementIds, Set<String> elementIdsInResult,
			Set<IGroup> directlyReferencedRootGroups, IProblemReporter pr) {
		Set<String> attachmentPaths = new HashSet<>();
		elementContainer.getAllRules().stream()
				.filter(r -> selectedElementIds.contains(((Rule) r).getId()))
				.forEach(r -> {
					ReferencedEntitiesInRule rec = new ReferencedEntitiesInRule();
					ReferencedEntitiesInRule.ReferencedEntitiesInRuleResult result = rec.compute(mvkService, r, pr);

					result.referencedEntities().forEach(e -> elementIdsInResult.add(((Entity) e).getId()));
					addAncestorsIdsFromReferencedEntitiesIds(elementIdsInResult, result);
					// getReferencedEntities contains already the directly referenced root groups
					// we need the following to create the confirm fields
					directlyReferencedRootGroups.addAll(result.directlyReferencedRootGroups());

					// determine which attachment-groups are referenced in a rule
					result.referencedEntities().stream()
							.filter(IEntity::isField)
							.map(com.mgmtp.a12.kernel.md.model.internal.service.mapping.Field.class::cast)
							.filter(com.mgmtp.a12.kernel.md.model.internal.service.mapping.Field::isInAttachment)
							.forEach(f -> attachmentPaths.add(ElementPathUtils.getParentPath(
									f.getFullName()))); // we consider only the parent, since A12 attachmens have a fixed structure
				});
		// If an attachment is referenced in a rule, only the fields have been added to the referenced entities, not the rules.
		elementContainer.getAllRules().stream()
				.filter(r -> attachmentPaths.contains(ElementPathUtils.getParentPath(r.getFullName())))
				.forEach(r -> elementIdsInResult.add(((Rule) r).getId()));
	}

	private void addAncestorsIdsFromReferencedEntitiesIds(Set<String> elementIdsInResult,
			ReferencedEntitiesInRule.ReferencedEntitiesInRuleResult result) {
		result.referencedEntities().forEach(e -> {
			final String[] parts = EntityUtils.getPathParts(e.getFullName());
			final StringBuilder currentPath = new StringBuilder();
			for (int i = 0; i < parts.length - 1; i++) {
				currentPath.append(EntityUtils.SEPARATOR);
				currentPath.append(parts[i]);
				String groupId = elementContainer.getGroupIdByPath(currentPath.toString());
				if (groupId != null) {
					elementIdsInResult.add(groupId);
				}
			}
		});
	}

	private void createConfirmFields(Set<IGroup> directlyReferencedRootGroups,
			DocumentModel reducedDm) {
		Set<String> directlyReferencedRootGroupIds = directlyReferencedRootGroups.stream()
				.map(Entity.class::cast)
				.map(Entity::getId)
				.collect(Collectors.toSet());
		reducedDm.getContent().getModelRoot().getElements().stream()
				.filter(elem -> isRootgroupToAddConfirmField(directlyReferencedRootGroupIds, elem))
				.forEach(elem -> {
					Group group = (Group) elem;
					FieldType confirmType = ConfirmType.builder().build();
					LocalizedTextMapBuilder builder = new LocalizedTextMapBuilder();
					reducedDm.getHeader().getLocales().forEach(locale -> {
						if (Locale.GERMAN.getLanguage().equals(locale.getLanguage())) {
							builder.add(locale, "Vordruck angegeben");
						} else {
							builder.add(locale, "Group filled?");
						}
					});
					ILocalizedTextMap confirmLabel = builder.build();
					Field confirmField = Field.builder()
							.id(group.getId() + "_" + CONFIRM_FIELD_NAME)
							.name(CONFIRM_FIELD_NAME)
							.fieldType(confirmType)
							.label(confirmLabel)
							.build();
					group.addElement(0, confirmField);
				});
	}

	/**
	 * An additional confirm field has to be created for a rootgroups that fullfill the following conditions:
	 * <ul>
	 *     <li>directly referenced by a rule</li>
	 *     <li>repeatability 1 or doesn't contain any other field</li>
	 * </ul>
	 *
	 * @param directlyReferencedRootGroupIds
	 * @param elem
	 * @return true if an additional confirm field has to be created.
	 */
	private boolean isRootgroupToAddConfirmField(Set<String> directlyReferencedRootGroupIds, Element elem) {
		return directlyReferencedRootGroupIds.contains(elem.getId())
				&& (((Group) elem).getRepeatability() == 1 || !containsField((Group) elem));
	}

	private boolean containsField(Group group) {
		return !group.getElements().stream()
				.filter(elem -> ((elem instanceof Field) || (elem instanceof Group g && containsField(g))))
				.collect(Collectors.toSet())
				.isEmpty();
	}
}
