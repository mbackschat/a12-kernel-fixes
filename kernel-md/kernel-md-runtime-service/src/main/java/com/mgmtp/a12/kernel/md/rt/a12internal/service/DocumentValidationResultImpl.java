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
package com.mgmtp.a12.kernel.md.rt.a12internal.service;

import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ResultBaseImpl;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;
import com.mgmtp.a12.kernel.md.rt.api.IMessage.MessageType;
import com.mgmtp.a12.kernel.md.rt.api.IMessage.Severity;
import com.mgmtp.a12.kernel.md.rt.internal.service.EntityInstanceConverter;
import com.mgmtp.a12.kernel.md.rt.internal.service.MessageImpl;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Implementation of the results of a document validation.
 * <p>
 * <em>Note:</em> is considered kernel internal but can also be used by Q12-TDS
 */
@OnlyForUsage
public class DocumentValidationResultImpl extends ResultBaseImpl implements IResult, IDocumentValidationResult {

	public List<IMessage> getMessages() {
		return v1Messages().stream().map(m -> (IMessage) new MessageImpl(
				m.messageStr(),
				EntityInstanceConverter.convertToEntityInstance(m.field()),
				EntityInstanceConverter.convertToEntityInstanceSet(m.referencedFields()),
				EntityInstanceConverter.convertToEntityInstanceSet(m.refOmissionErrorResponsible()),
				MessageType.valueOf(m.type().name()),
				mapSeverity(m.severity()),
				Optional.ofNullable(m.ruleName()),
				m.errorCode()
		)).toList();
	}

	private Severity mapSeverity(SeverityType severity) {
		return switch (severity) {
			case ERROR -> Severity.ERROR;
			case HINT -> Severity.WARNING;
			case INFO -> Severity.INFO;
		};
	}

	@Override
	public boolean noErrorOccurred() {
		return getCount(SeverityType.ERROR) == 0;
	}
}
