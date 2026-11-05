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
package com.mgmtp.a12.kernel.md.rt.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Generic error message container for {@link IDocumentValidationResult}s.
 */
@OnlyForUsage
public interface IMessage {

	/**
	 * Differentiates validation errors into omission errors and value errors.
	 * An omission error is present if a rule can be satisfied by filling one or more additional
	 * fields.
	 * Otherwise, it is a value error.
	 */
	enum MessageType {
		OMISSION_ERROR,
		VALUE_ERROR
	}

	/**
	 * Specifies whether a {@link IMessage} is an error, a hint, or an info.
	 */
	enum Severity {
		ERROR,
		WARNING,
		INFO;

		public static Severity fromBaseSeverity(com.mgmtp.a12.model.notification.Severity severity) {
			return switch (severity) {
				case ERROR -> ERROR;
				case WARNING -> WARNING;
				case INFO -> INFO;
			};
		}

		public com.mgmtp.a12.model.notification.Severity toBaseSeverity() {
			return switch (this) {
				case ERROR -> com.mgmtp.a12.model.notification.Severity.ERROR;
				case WARNING -> com.mgmtp.a12.model.notification.Severity.WARNING;
				case INFO -> com.mgmtp.a12.model.notification.Severity.INFO;
			};
		}
	}

	/**
	 * @return The error message text.
	 * 		A detailed string representation of the message that should be displayed to the end user. All parameters used in the
	 * 		message are already replaced by actual values. Parameters for the field name (<i>$&lt;fieldname&gt;$</i>) are
	 * 		replaced by the String representation of the {@link IIdentifier} that is returned by the method
	 *        {@link IData#identifierToString(IIdentifier)}, parameters for the field value (<i>$&lt;field.value&gt;$</i> are
	 * 		replaced by the String returned by the method {@link IData#getValueForDisplay(IIdentifier)}, in both cases including
	 * 		the $-sign. Note: The text is otherwise returned unchanged, so in particular no substitutions of html-sensitive
	 * 		characters take place. If e.g. a substitution of html-sensitive characters is only relevant for field values,
	 *        {@link IData#identifierToString(IIdentifier)} can be implemented appropriately. If substitution of sensitive characters
	 * 		is not necessary, the caller won't need to change the error text before presentation to the end user.
	 */
	@NonNull String getErrorText();

	/**
	 * @return the pointer to the error field, i.e. the field the error is reported on
	 */
	@NonNull PartiallyKnownDocumentMultiPointer getErrorFieldPointer();

	/**
	 * @return field instances as {@link PartiallyKnownDocumentMultiPointer}s that are referenced by the rule and thus
	 * 		possibly responsible for it firing. This includes the fields returned by
	 *        {@link #getRefOmissionErrorResponsiblePointers()}. Note that the returned pointers may contain wildcard indices
	 * 		(repetitions with value 0).
	 * 		<p>
	 * 		Note: This does not take
	 *        {@link IRule.IFilterForPossiblyCausingEntities#getEntitiesEnabledAsPossibleCauseIfFilled()} and
	 *        {@link IRule.IFilterForPossiblyCausingEntities#getEntitiesEnabledAsPossibleCauseIfNotFilled()} into account.
	 */
	@NonNull Collection<PartiallyKnownDocumentMultiPointer> getReferencedFieldsPointers();

	/**
	 * @return field instances as {@link PartiallyKnownDocumentMultiPointer}s that might be responsible for an omission
	 * 		error, i.e. fields that were not specified and that, if given additionally, may satisfy the rule. Note that the
	 * 		returned pointers may contain wildcard indices (repetitions with value 0).
	 * 		<p>
	 * 		Note: This does not take
	 *        {@link IRule.IFilterForPossiblyCausingEntities#getEntitiesEnabledAsPossibleCauseIfFilled()} and
	 *        {@link IRule.IFilterForPossiblyCausingEntities#getEntitiesEnabledAsPossibleCauseIfNotFilled()} into account.
	 */
	@NonNull Collection<PartiallyKnownDocumentMultiPointer> getRefOmissionErrorResponsiblePointers();

	/**
	 * @return whether it is a {@link MessageType#VALUE_ERROR} or an {@link MessageType#OMISSION_ERROR}
	 */
	@NonNull MessageType getMessageType();

	/**
	 * @return whether it is an error, a hint or an info
	 */
	@NonNull Severity getSeverity();

	/**
	 * @return the path to the {@link IRule} that caused the error
	 * 		or an {@link java.util.Optional#empty()} in case of formal errors
	 */
	@NonNull Optional<String> getRulePath();

	/**
	 * @return the error code as defined in the validation rule
	 */
	@NonNull String getErrorCode();

	/**
	 * Returns the entities that possibly caused the error, gathered into groups so that they can be prioritized.
	 * <p>
	 * If {@link IRule#getFilterForPossiblyCausingEntities()} was provided in the corresponding rule, the entity instances
	 * determined as a possible cause for this rule firing are filtered: <ul>
	 * <li>filled entity instances are removed as possible causes if their entity is not contained in
	 * {@link IRule.IFilterForPossiblyCausingEntities#getEntitiesEnabledAsPossibleCauseIfFilled()}</li>
	 * <li>unfilled entity instances are removed as possible causes if their entity is not contained in
	 * {@link IRule.IFilterForPossiblyCausingEntities#getEntitiesEnabledAsPossibleCauseIfNotFilled()}</li>
	 * </ul>
	 * <p>
	 * The current implementation groups the possible causes as followed:
	 * <ul><li>For {@link MessageType#OMISSION_ERROR} the first group will consist of entity instances that are not filled. The
	 * second group will consist of the entities that are filled.</li>
	 * <li>For {@link MessageType#VALUE_ERROR} all possibly causing entities will be returned as a single group.</li>
	 * </ul>
	 * <p>
	 * Future implementations may create other or more groups. But we always try to follow the rule: Fields of the earlier groups
	 * should be more probable to cause the errors from a technical point of view.
	 * <p>
	 * Note that the returned pointers may contain wildcard indices (repetitions with value 0) or unknown indices (repetitions with
	 * value -5).
	 */
	@NonNull List<Set<PartiallyKnownDocumentMultiPointer>> getGroupsOfPossiblyCausingEntities();

}
