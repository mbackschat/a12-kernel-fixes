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

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;

import lombok.NonNull;

/**
 * Provides a custom way to determine the ID for a document model, in case the ID contained in the document model is not enough to
 * identify the DM unambiguously.
 * <p>
 * <b>Note:</b> The caller has to provide an implementation of this interface.
 */
public interface IDocumentModelIdProvider {

	/**
	 * @param documentModel
	 *        {@link IDocumentModel} from and for which the ID should be determined.
	 * @return document model ID, which is calculated from the parameters. This ID is e.g. used to
	 * 		load the generated code. Must not be {@code null} or empty.
	 */
	@NonNull String getDocumentModelId(@NonNull IDocumentModel documentModel);
}
