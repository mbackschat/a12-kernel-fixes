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

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCode;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.ErrorCodeUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.error.JoinProblem;
import com.mgmtp.a12.kernel.md.model.internal.services.join.selection.SelectionModelV2Wrapper;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.DocumentModelJoinUtil;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;

/**
 * Executor class to join group decoration models.
 */
public class DecorationGroupModelJoiner extends DecorationModelJoiner<Group> {

	public DecorationGroupModelJoiner(DocumentModel resultModel,
			DocumentModel decorationModel, SelectionModelV2Wrapper selectionModelWrapper,
			Locale locale,
			JoiningProblemReporter problemReporter) {
		super(resultModel, decorationModel, selectionModelWrapper, locale, problemReporter, Group.class);
	}

	@Override public boolean isValidDecorationModel() {
		return isValidGroupDecorationModel(decorationModel, locale, problemReporter);
	}

	public static boolean isValidGroupDecorationModel(DocumentModel decorationModel, Locale locale,
			JoiningProblemReporter problemReporter) {
		String anchorName = createAnchorName(Group.class);
		Consumer<Group> groupAnchorChecker = (Group rootGroup) -> DecorationGroupModelJoiner.checkGroupAnchor(
				decorationModel, rootGroup, anchorName, locale, problemReporter);;

		return DecorationModelJoiner.internal_isValidDecorationModel(
				groupAnchorChecker, decorationModel, anchorName, locale, problemReporter);
	}

	protected static void checkGroupAnchor(DocumentModel decorationModel, Group rootGroup, String anchorName, Locale locale,
			JoiningProblemReporter problemReporter) {
		DecorationModelJoiner.checkAnchor(Group.class, decorationModel, rootGroup, anchorName, locale, problemReporter);
		if (problemReporter.hasCriticalProblemOccurred()) {
			return;
		}

		Group anchorGroup = rootGroup.getElements().stream()
				.filter(Group.class::isInstance)
				.map(Group.class::cast)
				.filter(e -> anchorName.equals(e.getName()))
				.findFirst()
				.orElseThrow();
		if (!anchorGroup.getElements().isEmpty()) {
			problemReporter.reportCriticalProblem(JoinProblem.createError(decorationModel,
					ErrorCodeUtil.getMessageWithCode(ErrorCode.InvalidSubstructure, locale,
							DocumentModelJoinUtil.getModelString(decorationModel))));
		}
		if (anchorGroup.getIndexField().isPresent() ||
				!anchorGroup.getSortFields().isEmpty() ||
				anchorGroup.getRepeatability() != 1 ||
				anchorGroup.getUsageType().isPresent() ||
				anchorGroup.getIncludeDetails().isPresent()) {
			problemReporter.reportCriticalProblem(JoinProblem.createError(decorationModel,
					ErrorCodeUtil.getMessageWithCode(ErrorCode.NonDefaultData, locale,
							DocumentModelJoinUtil.getModelString(decorationModel))));
		}
	}

	@Override
	protected ElementsDecoratorVisitor getElementsDecoratorVisitor() {
		return new GroupsDecoratorVisitor();
	}

	protected class GroupsDecoratorVisitor extends ElementsDecoratorVisitor {
		boolean decoModelContainsElementsOtherThanGroupsOnRootLevel = determineValidRootGroup(decorationModel, locale,
				problemReporter).getElements().stream()
				.anyMatch(e -> !(e instanceof Group));
		private final Group modelRoot = resultModel.getContent().getModelRoot();
		private int indexOfRootGroup = 0;
		private int offsetForRootLayer = 0; // already inserted elements on root layer

		@Override protected boolean additionalDecorationChecks(Element element) {
			if (!(element instanceof Group group)) {
				return false;
			}

			String path = documentModelService.getPath(group);
			boolean decoratedElementOnRootLayer = path.equals(ElementPathUtils.SEPARATOR + group.getName());
			if (decoratedElementOnRootLayer && decoModelContainsElementsOtherThanGroupsOnRootLevel) {
				problemReporter.reportCriticalProblem(JoinProblem.createError(decorationModel, ErrorCodeUtil.getMessageWithCode(
						ErrorCode.InvalidDecorationElementsOnRootLayer, locale,
						DocumentModelJoinUtil.getModelString(decorationModel))));
				return true;
			}
			return false;

		}

		@Override protected void decorateRootGroupsIfNecessary(Group group, Map<String, Element> nameToElementMapper) {
			String fullName = documentModelService.getPath(group);
			if (!ElementPathUtils.getParentPath(fullName).isEmpty()) {
				return;
			}

			offsetForRootLayer = doDecoration(group, nameToElementMapper, modelRoot, offsetForRootLayer, indexOfRootGroup++);
		}
	}
}
