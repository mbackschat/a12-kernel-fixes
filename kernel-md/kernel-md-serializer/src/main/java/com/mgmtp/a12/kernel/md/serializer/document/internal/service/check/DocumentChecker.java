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

package com.mgmtp.a12.kernel.md.serializer.document.internal.service.check;

import static com.mgmtp.a12.kernel.md.model.internal.impl.RankedNotificationImpl.error;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.IGroupInstance;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSearchService;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentService;
import com.mgmtp.a12.kernel.md.document.internal.service.impl.EntityInstanceImpl;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.NonRepeatableEntitiesFinder;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSearchServiceImpl;
import com.mgmtp.a12.model.notification.RankedNotification;

/**
 * Implements the functionality for {@link IDocumentService#checkEntities(Consumer)} and
 * {@link IDocumentService#checkPresenceOfFieldInstancesInNonRepeatableGroups(Consumer)}.
 *
 * @deprecated use {@code IDocumentRtService#checkStructuralConsistencyWithDm} instead.
 */
@Deprecated(since = "30.0.0")
public class DocumentChecker {

	private final IDocumentModel documentModel;
	private final IDocumentModelSearchService dmSearchService;
	private final IDocumentModelResolver documentModelResolver;
	private List<String> nonRepeatableGroupPaths;
	private List<String> nonRepeatableFieldPaths;

	public DocumentChecker(IDocumentModelResolver dmr, IDocumentModel documentModel, IDocumentModelSearchService dmSearchService) {
		this.documentModelResolver = dmr;
		this.documentModel = documentModel;
		this.dmSearchService = dmSearchService;
		init();
	}

	public boolean checkEntities(IDocument document, Consumer<RankedNotification> notificationReceiver) {
		return checkEntityConsistency(document, notificationReceiver)
				& checkPresenceOfNonRepeatableEntities(document, notificationReceiver, nonRepeatableGroupPaths, "group");
	}

	public boolean checkEntityConsistency(IDocument document, Consumer<RankedNotification> notificationReceiver) {
		boolean success = true;
		for (IEntityInstance ei : document.getEntityInstances()) {
			if (isErroneous(ei)) {
				success = false;
				notificationReceiver.accept(error(
						String.format("Repetitions %s of '%s' are wrong.", Arrays.toString(ei.getRepetitions()), ei.getPath())));
				continue;
			}
			Optional<IElement> element = dmSearchService.getByPath(ei.getPath());
			if (!element.isPresent()
					|| !ElementUtils.isEntity(element.get())) {
				success = false;
				notificationReceiver.accept(error(
						String.format("Entity '%s' was not found in the document model.", entityInstanceToString(ei))));
			} else if (ElementUtils.isField(element.get()) && ei instanceof IGroupInstance) {
				success = false;
				notificationReceiver.accept(error(
						String.format("Field '%s' was wrongly instantiated as group.", entityInstanceToString(ei))));
			} else if (ElementUtils.isGroup(element.get()) && ei instanceof IFieldInstance) {
				success = false;
				notificationReceiver.accept(error(
						String.format("Group '%s' was wrongly instantiated as field.", entityInstanceToString(ei))));
			}
		}
		return success;
	}

	private boolean isErroneous(IEntityInstance entityInstance) {
		return entityInstance.getRepetitions().length != ElementPathUtils.getNumberOfPathParts(entityInstance.getPath())
				|| Arrays.stream(entityInstance.getRepetitions()).anyMatch(rep -> rep <= 0)
				|| (entityInstance instanceof IFieldInstance && entityInstance.getRepetitions()[entityInstance.getRepetitions().length - 1] != 1);
	}

	private String entityInstanceToString(IEntityInstance entityInstance) {
		// to get a nice error message we create an internal instance
		return new EntityInstanceImpl(entityInstance.getPath(), entityInstance.getRepetitions()).toString();
	}

	public boolean checkPresenceOfFieldInstancesInNonRepeatableGroups(IDocument document,
			Consumer<RankedNotification> notificationReceiver) {
		return checkPresenceOfNonRepeatableEntities(document, notificationReceiver, nonRepeatableFieldPaths, "field");
	}

	private boolean checkPresenceOfNonRepeatableEntities(IDocument document, Consumer<RankedNotification> notificationReceiver,
			List<String> nonRepeatableEntityPaths, String fieldOrGroup) {
		AtomicBoolean success = new AtomicBoolean(true);
		IDocumentSearchService searchService = new DocumentSearchServiceImpl(document, documentModelResolver);
		for (String path : nonRepeatableEntityPaths) {
			Set<IEntityInstance> found = searchService.get(path, null);
			if (found.isEmpty()) {
				success.set(false);
				notificationReceiver.accept(error(
						String.format("The corresponding instance of the non-repeatable %s '%s' was not found in the document.",
								fieldOrGroup, path)));
			}
		}
		return success.get();
	}

	private void init() {
		NonRepeatableEntitiesFinder nrgf = new NonRepeatableEntitiesFinder();
		new DocumentModelWalker().acceptDocumentModel(documentModel, nrgf);
		this.nonRepeatableGroupPaths = nrgf.getNonRepeatableGroupPaths();
		this.nonRepeatableFieldPaths = nrgf.getNonRepeatableFieldPaths();
	}

}
