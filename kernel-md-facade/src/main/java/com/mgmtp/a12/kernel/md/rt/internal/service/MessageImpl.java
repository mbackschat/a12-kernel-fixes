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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ResultMessageV1;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ResultMessageV2;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;
import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @EqualsAndHashCode
public class MessageImpl implements IMessage {

	@Getter private final @NonNull String errorText;
	@Getter private final @NonNull PartiallyKnownDocumentMultiPointer errorFieldPointer;
	@Getter private final @NonNull Collection<PartiallyKnownDocumentMultiPointer> referencedFieldsPointers;
	@Getter private final @NonNull Collection<PartiallyKnownDocumentMultiPointer> refOmissionErrorResponsiblePointers;
	@Getter private final @NonNull MessageType messageType;
	@Getter private final @NonNull Severity severity;
	@Getter private final @NonNull Optional<String> rulePath;
	@Getter private final @NonNull String errorCode;
	@Getter private final @NonNull List<Set<PartiallyKnownDocumentMultiPointer>> groupsOfPossiblyCausingEntities;

	public static @NonNull MessageImpl fromVkMessage(@NonNull ResultMessageV1 v1, @NonNull ResultMessageV2 v2) {
		return new MessageImpl(
				v2.messageStr(),
				DocumentPointerConverter.convertToPartiallyKnownDocumentMultiPointer(v1.field()),
				DocumentPointerConverter.convertToPartiallyKnownDocumentMultiPointerSet(v1.referencedFields()),
				DocumentPointerConverter.convertToPartiallyKnownDocumentMultiPointerSet(v1.refOmissionErrorResponsible()),
				MessageType.valueOf(v2.type().name()),
				mapSeverity(v2.severity()),
				Optional.ofNullable(v2.ruleName()),
				v2.errorCode(),
				v2.possiblyCausingEntities().getCausingEntityGroups().stream().map(
						DocumentPointerConverter::convertToPartiallyKnownDocumentMultiPointerSet).toList());
	}

	private static @NonNull Severity mapSeverity(@NonNull SeverityType severity) {
		return switch (severity) {
			case ERROR -> Severity.ERROR;
			case HINT -> Severity.WARNING;
			case INFO -> Severity.INFO;
		};
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
}
