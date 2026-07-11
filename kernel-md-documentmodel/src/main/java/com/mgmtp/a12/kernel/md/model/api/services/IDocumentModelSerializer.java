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

package com.mgmtp.a12.kernel.md.model.api.services;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;

/**
 * This service provides (de)serialization of a {@link IDocumentModel}.
 * <p>
 * <b>Important note:</b> To create an instance see
 * DocumentModelServiceFactory#createDocumentModelSerializer().
 */
@OnlyForUsage
public interface IDocumentModelSerializer {

	/**
	 * @param reader
	 * 		the {@link Reader} for a json representation of a {@link IDocumentModel} which
	 * 		should be
	 * 		deserialized. May not be {@code null}. <b>Important note:</b> The caller is
	 * 		responsible to close the {@link Reader}.
	 * @return the deserialized {@link IDocumentModel}.
	 *
	 * @throws IOException
	 */
	IDocumentModel deserialize(@NonNull Reader reader) throws IOException;

	/**
	 * Serializes the given {@link IDocumentModel} via the given {@link Writer}.
	 * <p>
	 * <b>Note:</b> During serialization, a basic consistency check will be performed in order to
	 * ensure that the given {@link IDocumentModel} can be deserialized again afterward.
	 *
	 * @param documentModel
	 * 		the {@link IDocumentModel} which should be serialized. May not be {@code null}.
	 * @param writer
	 * 		the {@link Writer} to which the serialized {@link IDocumentModel} is written. May
	 * 		not be {@code null}. If errors were found, nothing will be written to this {@link Writer}.
	 * 		<b>Important note:</b> The caller is responsible for closing the {@link Writer}.
	 * @param notificationReceiver
	 * 		a callback to which basic consistency problems found with the {@link IDocumentModel}
	 * 		will be reported. May not be {@code null}.
	 * @throws IOException
	 * 		if the writer cannot be written to successfully.
	 */
	void serialize(@NonNull IDocumentModel documentModel, @NonNull Writer writer,
			@NonNull Consumer<RankedNotification> notificationReceiver)
			throws IOException;
}
