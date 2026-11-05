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

package com.mgmtp.a12.kernel.md.serializer.model.internal.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.kernel.md.serializer.model.a12internal.services.DocumentModelSerializer;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.consistency.BasicConsistencyCheck;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization.KernelJsonMapper;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;
import tools.jackson.databind.ObjectMapper;

/**
 * This service offers (de)serialization of a {@link IDocumentModel}.
 */
public class DocumentModelSerializerImpl implements IDocumentModelSerializer {

	/**
	 * See
	 * {@link IDocumentModelSerializer#deserialize(Reader)}.
	 */
	public @NonNull IDocumentModel deserialize(@NonNull Reader reader) throws IOException {
		return new DocumentModelWrapper(new DocumentModelSerializer().deserialize(reader));
	}

	/**
	 * See {@link IDocumentModelSerializer#serialize(IDocumentModel, Writer, Consumer)}.
	 */
	@Override public void serialize(
			@NonNull IDocumentModel documentModel,
			@NonNull Writer writer,
			@NonNull Consumer<RankedNotification> notificationReceiver
	) throws IOException {
		serializeInternal(documentModel, writer, notificationReceiver, true);
	}

	public static void serializeInternal(
			@NonNull IDocumentModel documentModel,
			@NonNull Writer writer,
			@NonNull Consumer<RankedNotification> notificationReceiver,
			boolean abortOnErrors
	) {
		AtomicBoolean hasErrorOccurred = new AtomicBoolean(false);
		BasicConsistencyCheck.checkModel(documentModel, problem -> {
			if (problem.getSeverity().isAtLeastAsSevereAs(Severity.ERROR)) {
				hasErrorOccurred.set(true);
			}
			notificationReceiver.accept(problem);
		});
		if (abortOnErrors && hasErrorOccurred.get()) {
			return;
		}

		getMapper().writeValue(writer, ExternalDocumentModelSerializerService.getInternDocumentModel(documentModel));
	}

	public static ObjectMapper getMapper() {
		return KernelJsonMapper.getMapper();
	}
}
