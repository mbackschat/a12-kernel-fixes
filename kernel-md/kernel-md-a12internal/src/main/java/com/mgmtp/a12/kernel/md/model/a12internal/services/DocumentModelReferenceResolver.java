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
package com.mgmtp.a12.kernel.md.model.a12internal.services;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelReferenceResolver;
import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;

/**
 * See {@link IDocumentModelReferenceResolver}
 * <p>
 * <b>Note:</b> The caller has to provide an implementation of this interface.
 */
@OnlyForA12InternalUsage
public interface DocumentModelReferenceResolver {

	/**
	 * see {@link IDocumentModelReferenceResolver#getDocumentModel(String)}
	 * <b>Note:</b>In contrast to
	 * {@link IDocumentModelReferenceResolver#getDocumentModel(String)}, the returned object of this method will be modified by
	 * Kernel. <b>Do not</b> use the modified object for this method again! If you want to preserve the state of the original
	 * object, you should make a copy and return the copy, or load it again each time and return the fresh loaded object.<p>
	 * <b>Note:</b> In following case, a self recursion cannot be identified and that's why no
	 * exception is thrown: calling
	 * DocumentModelService#expand(DocumentModel, DocumentModelReferenceResolver) after
	 * adding an include group object (containing a self recursion of the base
	 * {@link DocumentModel}) to a document model object. If the user wants to catch this situation,
	 * his own implementation of this interface should be correspondingly adapted.
	 */
	DocumentModel getDocumentModel(String reference);

}
