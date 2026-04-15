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
import java.util.Optional;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Generic error message container for {@link IDocumentValidationResult}s.
 */
@OnlyForUsage
public interface IMessage {

	/**
	 * Provides differentiation of validation errors in value and omission.
	 * An omission error is present, if a rule can be satisfied by filling one or more additional
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
	String getErrorText();

	/**
	 * @return the erroneous entity instance
	 *
	 * @deprecated use {@link #getErrorFieldPointer()} instead.
	 */
	@Deprecated(since = "30.0.0")
	IEntityInstance getEntityInstance();

	PartiallyKnownDocumentMultiPointer getErrorFieldPointer();

	/**
	 * @return field instances as {@link IEntityInstance}s that might have caused the error
	 *
	 * @deprecated use {@link #getReferencedFieldsPointers()} instead.
	 */
	@Deprecated(since = "30.0.0")
	Collection<IEntityInstance> getReferencedFields();

	/**
	 * see {@link #getReferencedFields()}. Note that the returned pointers may contain wildcard indices (repetitions with value 0)
	 */
	Collection<PartiallyKnownDocumentMultiPointer> getReferencedFieldsPointers();

	/**
	 * @return field instances as {@link IEntityInstance}s that might be responsible for generating omission errors.
	 *
	 * @deprecated use {@link #getRefOmissionErrorResponsiblePointers()} instead.
	 */
	@Deprecated(since = "30.0.0")
	Collection<IEntityInstance> getRefOmissionErrorResponsible();

	/**
	 * see {@link #getRefOmissionErrorResponsible()}. Note that the returned pointers may contain wildcard indices (repetitions with
	 * value 0)
	 */
	Collection<PartiallyKnownDocumentMultiPointer> getRefOmissionErrorResponsiblePointers();

	/**
	 * @return whether it is a value error or an omission error
	 */
	MessageType getMessageType();

	/**
	 * @return whether it is an error, a hint or an info
	 */
	Severity getSeverity();

	/**
	 * @return the path to the {@link IRule} that caused the error
	 * 		or an empty {@link java.util.Optional#empty()} in case of formal errors
	 */
	Optional<String> getRulePath();

	/**
	 * @return the error code as defined in the validation rule
	 */
	String getErrorCode();

}
