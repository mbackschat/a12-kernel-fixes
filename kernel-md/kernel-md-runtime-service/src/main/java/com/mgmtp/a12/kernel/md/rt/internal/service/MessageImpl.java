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
package com.mgmtp.a12.kernel.md.rt.internal.service;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;

import lombok.EqualsAndHashCode;
import lombok.Getter;

public class MessageImpl implements IMessage {

	@Getter private final String errorText;
	@Getter private final PartiallyKnownDocumentMultiPointer errorFieldPointer;
	@Getter private final Collection<PartiallyKnownDocumentMultiPointer> referencedFieldsPointers;
	@Getter private final Collection<PartiallyKnownDocumentMultiPointer> refOmissionErrorResponsiblePointers;
	@Getter private final MessageType messageType;
	@Getter private final Severity severity;
	@Getter private final Optional<String> rulePath;
	@Getter private final String errorCode;

	public MessageImpl(final String errorText,
			final PartiallyKnownDocumentMultiPointer errorFieldPointer,
			final Collection<PartiallyKnownDocumentMultiPointer> referencedFieldsPointers,
			final Collection<PartiallyKnownDocumentMultiPointer> refOmissionErrorResponsiblePointers,
			final MessageType messageType,
			final Severity severity,
			final Optional<String> rulePath,
			final String errorCode) {
		Objects.requireNonNull(errorText);

		this.errorText = errorText;
		this.errorFieldPointer = errorFieldPointer;
		this.referencedFieldsPointers = referencedFieldsPointers;
		this.refOmissionErrorResponsiblePointers = refOmissionErrorResponsiblePointers;
		this.messageType = messageType;
		this.severity = severity;
		this.rulePath = rulePath;
		this.errorCode = errorCode;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Entity: ").append(getErrorFieldPointer().toString());
		sb.append(" Type: ").append(getMessageType());
		sb.append(" Message: ").append(getErrorText());
		sb.append(" ErrorCode: ").append(getErrorCode());
		getRulePath().ifPresent(p -> sb.append(" Rule: ").append(p));
		return sb.toString();
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MessageImpl message))
			return false;
		return Objects.equals(errorText, message.errorText) &&
				Objects.equals(errorFieldPointer, message.errorFieldPointer) &&
				Objects.equals(referencedFieldsPointers, message.referencedFieldsPointers) &&
				Objects.equals(refOmissionErrorResponsiblePointers, message.refOmissionErrorResponsiblePointers) &&
				messageType == message.messageType &&
				severity == message.severity &&
				Objects.equals(rulePath, message.rulePath) &&
				Objects.equals(errorCode, message.errorCode);
	}

	@Override public int hashCode() {
		return Objects.hash(errorText, errorFieldPointer, referencedFieldsPointers, refOmissionErrorResponsiblePointers, messageType, severity,
				rulePath, errorCode);
	}
}
