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

package com.mgmtp.a12.kernel.md.combination.a12internal;

import java.util.Locale;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.core.base.utils.internal.Consumer2ProblemReporter;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.internal.services.join.decoration.DecorationFieldModelJoiner;
import com.mgmtp.a12.kernel.md.model.internal.services.join.decoration.DecorationGroupModelJoiner;
import com.mgmtp.a12.kernel.md.model.internal.services.join.util.JoiningProblemReporter;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views._mm_combinationmodel_1._content._combinationsteps.Type;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

@OnlyForUsage
public class DecorationModelService {
	public enum DecorationType {
		FIELD,
		GROUP;

		public static DecorationType fromCombinationStep(Type t) {
			return switch (t) {
				case DecorationForFields -> FIELD;
				case DecorationForGroups -> GROUP;
				case Addition, Selection ->
						throw new IllegalArgumentException("Only a decoration step is allowed, but got " + t._stringValue());
			};
		}
	}

	/**
	 * Check to see if the structure of the decoration model is valid according to the stepType.
	 * <p>
	 * The provided `decorationModel` has to be fully expanded.
	 */
	public static boolean checkConsistencyStandalone(@NonNull Type stepType,
			@NonNull IDocumentModel decorationModel,
			@NonNull Locale locale, @NonNull Consumer<? super RankedNotification> notificationReceiver) {
		return checkConsistencyStandalone(DecorationType.fromCombinationStep(stepType), decorationModel, locale,
				notificationReceiver);
	}

	/**
	 * Check to see if the structure of the decoration model is valid according to the decorationType.
	 * <p>
	 * The provided `decorationModel` has to be fully expanded.
	 */
	public static boolean checkConsistencyStandalone(@NonNull DecorationType decorationType,
			@NonNull IDocumentModel decorationModel,
			@NonNull Locale locale, @NonNull Consumer<? super RankedNotification> notificationReceiver) {
		JoiningProblemReporter problemReporter = new JoiningProblemReporter(new Consumer2ProblemReporter(notificationReceiver));

		return switch (decorationType) {
			case FIELD -> DecorationFieldModelJoiner.isValidFieldDecorationModel(
					new DocumentModelService().convertFromExternal(decorationModel), locale, problemReporter);
			case GROUP -> DecorationGroupModelJoiner.isValidGroupDecorationModel(
					new DocumentModelService().convertFromExternal(decorationModel), locale, problemReporter);
		};
	}
}
