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

package com.mgmtp.a12.kernel.md.document.a12internal.services;

import java.util.List;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.IGroupInstance;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.internal.service.DocumentFactoryImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.utils.DocumentUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.NonRepeatableEntitiesFinder;
import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;

/**
 * Provides functionality additional to {@link IDocumentFactory}.
 *
 * @deprecated for {@link DocumentV2} this factory is no longer necessary, as the method {@link DocumentV2#empty(String)} can be
 * 		used to create an empty document
 */
@OnlyForA12InternalUsage
@Deprecated(since = "30.0.0")
public class EmptyDocumentFactory {

	private final IDocumentModel documentModel;
	private List<String> nonRepeatableGroupPaths;
	private List<String> nonRepeatableFieldPaths;

	/**
	 * @param documentModel
	 *        {@link IDocumentModel} from which this services takes its necessary
	 * 		information. May not be {@code null}.
	 */
	public EmptyDocumentFactory(IDocumentModel documentModel) {
		this.documentModel = documentModel;
		NonRepeatableEntitiesFinder nrgf = new NonRepeatableEntitiesFinder();
		new DocumentModelWalker().acceptDocumentModel(documentModel, nrgf);
		this.nonRepeatableGroupPaths = nrgf.getNonRepeatableGroupPaths();
		this.nonRepeatableFieldPaths = nrgf.getNonRepeatableFieldPaths();
	}

	/**
	 * Creates an "empty" {@link IDocument} instance based on the {@link IDocumentModel} that was
	 * given when having created this class. It will already contain all non-repeatable
	 * content. The created {@link IFieldInstance}s will have no assigned values.
	 *
	 * @param documentFactory
	 * 		the factory which will be used to create the {@link IDocument},
	 * 		the {@link IGroupInstance}s and {@link IFieldInstance}s.<br>
	 * 		May be {@code null}, in this case the default implementation will be used.
	 * @return the new {@link IDocument} instance.
	 */
	public IDocument createEmptyDocument(IDocumentFactory documentFactory) {
		if (documentFactory == null) {
			documentFactory = new DocumentFactoryImpl();
		}
		IDocument document = documentFactory.createDocument(documentModel.getHeader().getId());
		for (String nonRepeatableGroupPath : nonRepeatableGroupPaths) {
			int[] repetitions = DocumentUtils.createRepetitions4NonRepeatable(nonRepeatableGroupPath);
			IGroupInstance groupInstance = documentFactory.createGroupInstance(nonRepeatableGroupPath, repetitions);
			document.addEntityInstance(groupInstance);
		}
		for (String nonRepeatableFieldPath : nonRepeatableFieldPaths) {
			int[] repetitions = DocumentUtils.createRepetitions4NonRepeatable(nonRepeatableFieldPath);
			IFieldInstance fieldInstance = documentFactory.createFieldInstance(nonRepeatableFieldPath, repetitions);
			document.addEntityInstance(fieldInstance);
		}
		return document;
	}

}
