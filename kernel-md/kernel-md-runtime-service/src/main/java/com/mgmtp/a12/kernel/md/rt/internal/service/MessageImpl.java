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

import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;

public class MessageImpl implements IMessage {

	private final String errorText;
	private final IEntityInstance entityInstance;
	private final Collection<IEntityInstance> referencedFields;
	private final Collection<IEntityInstance> refOmissionErrorResponsible;
	private final MessageType messageType;
	private final Severity severity;
	private final Optional<String> rulePath;
	private final String code;

	public MessageImpl(final String errorText,
			final IEntityInstance entityInstance,
			final Collection<IEntityInstance> referencedFields,
			final Collection<IEntityInstance> refOmissionErrorResponsible,
			final MessageType messageType,
			final Severity severity,
			final Optional<String> rulePath,
			final String code) {
		Objects.requireNonNull(errorText);

		this.errorText = errorText;
		this.entityInstance = entityInstance;
		this.referencedFields = referencedFields;
		this.refOmissionErrorResponsible = refOmissionErrorResponsible;
		this.messageType = messageType;
		this.severity = severity;
		this.rulePath = rulePath;
		this.code = code;
	}

	@Override
	public String getErrorText() {
		return errorText;
	}

	@SuppressWarnings("removal") @Override
	public IEntityInstance getEntityInstance() {
		return entityInstance;
	}

	@Override public PartiallyKnownDocumentMultiPointer getErrorFieldPointer() {
		return PartiallyKnownDocumentMultiPointer.of(getEntityInstance());
	}

	@SuppressWarnings("removal") @Override
	public Collection<IEntityInstance> getReferencedFields() {
		return referencedFields;
	}

	@Override
	public Collection<PartiallyKnownDocumentMultiPointer> getReferencedFieldsPointers() {
		return getReferencedFields().stream()
				.map(PartiallyKnownDocumentMultiPointer::of)
				.toList();
	}

	@SuppressWarnings("removal") @Override
	public Collection<IEntityInstance> getRefOmissionErrorResponsible() {
		return refOmissionErrorResponsible;
	}

	@Override
	public Collection<PartiallyKnownDocumentMultiPointer> getRefOmissionErrorResponsiblePointers() {
		return getRefOmissionErrorResponsible().stream()
				.map(PartiallyKnownDocumentMultiPointer::of)
				.toList();
	}

	@Override
	public MessageType getMessageType() {
		return messageType;
	}

	@Override
	public Severity getSeverity() {
		return severity;
	}

	@Override
	public Optional<String> getRulePath() {
		return rulePath;
	}

	@Override
	public String getErrorCode() {
		return code;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Entity: ").append(getEntityInstance().toString());
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
				Objects.equals(entityInstance, message.entityInstance) &&
				Objects.equals(referencedFields, message.referencedFields) &&
				Objects.equals(refOmissionErrorResponsible, message.refOmissionErrorResponsible) &&
				messageType == message.messageType &&
				severity == message.severity &&
				Objects.equals(rulePath, message.rulePath) &&
				Objects.equals(code, message.code);
	}

	@Override public int hashCode() {
		return Objects.hash(errorText, entityInstance, referencedFields, refOmissionErrorResponsible, messageType, severity,
				rulePath, code);
	}
}
