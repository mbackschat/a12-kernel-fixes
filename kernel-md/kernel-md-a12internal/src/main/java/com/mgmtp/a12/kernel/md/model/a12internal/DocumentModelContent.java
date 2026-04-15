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

package com.mgmtp.a12.kernel.md.model.a12internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;
import com.mgmtp.a12.model.Content;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Specific content for the {@link DocumentModel}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@OnlyForA12InternalUsage
public class DocumentModelContent implements Content, Serializable {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * Current version of the document model.
	 */
    // NOTE: when this version is changed, the checklist at MGM Confluence Shortlink x/SbP7H should be followed.
	public static final String VERSION = "28.4.0";

	/**
	 * @return the corresponding {@link DocumentModelInfo} for the {@link DocumentModel}. Is never
	 *        {@code null}.
	 * @param modelInfo
	 * 		the new {@link DocumentModelInfo} for the {@link DocumentModel}. May not be
	 *        {@code null}.
	 */
	private @NonNull DocumentModelInfo modelInfo;
	/**
	 * @return the corresponding {@link DocumentModelConfig} for the {@link DocumentModel}. Is never
	 *        {@code null}.
	 * @param modelConfig
	 * 		the new {@link DocumentModelConfig} for the {@link DocumentModel}. May not
	 * 		be
	 *        {@code null}.
	 */
	@Builder.Default
	private @NonNull DocumentModelConfig modelConfig = DocumentModelConfig.builder().build();
	/**
	 * @return a list with the type definition used in the {@link DocumentModel}. Is never
	 *        {@code null}. If there are no type definitions used, the list is empty.
	 * @param typeDefinitions
	 * 		a list with the new type definitions used in the
	 *        {@link DocumentModel}. May not be
	 *        {@code null}. Use an empty list to unset.
	 */
	@Builder.Default
	private @NonNull List<FieldTypeDefinition> typeDefinitions = new ArrayList<>();
	/**
	 * @return the {@link DocumentModelRoot}. Is never {@code null}.
	 */
	@Setter(value = AccessLevel.NONE)
	private final DocumentModelRoot modelRoot = new DocumentModelRoot();

}
