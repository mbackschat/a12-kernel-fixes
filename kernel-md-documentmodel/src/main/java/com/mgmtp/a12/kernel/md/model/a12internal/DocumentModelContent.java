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
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModelContent;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
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
public class DocumentModelContent implements Content, Serializable {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * Current version of the document model.
	 * <br>
	 * <p>When updating the DM version, follow the
	 * <a href="https://semver.org/spec/v2.0.0.html">Semantic Versioning</a> convention,
	 * regardless of whether a migration is provided for the change:
	 * <ul>
	 *   <li><b>Major</b> ({@code 29.x.x → 30.0.0}): Increment for breaking changes
	 *       (e.g. removing fields or structures). I.e. the document model has to be migrated in order to be processed in kernel.
	 *       This will only be done in breaking releases.</li>
	 *   <li><b>Minor</b> ({@code 29.0.x → 29.1.0}): Increment for non-breaking
	 *       additions or changes.</li>
	 *   <li><b>Patch</b> ({@code 29.0.0 → 29.0.1}): Increment only to correct a faulty
	 *       migration. Because migrations are selected by target version, a new version
	 *       guarantees the corrected migration runs even on data already migrated with the
	 *       broken one.</li>
	 * </ul>
	 *
	 * <p>This versioning scheme is applied per individual requirement — DM-relevant changes
	 * are not batched together within a release. As a result, there may be multiple DM version
	 * increments within a single kernel release cycle (including multiple major version increments
	 * in breaking releases).</p>
	 *
	 * <p>This can lead to gaps in the DM versions across actually shipped Kernel versions,
	 * since those intermediate DM versions existed only during development. These gaps are accepted
	 * in order to deliver individual features completely at an early stage and to avoid unnecessary
	 * organizational overhead.</p>
	 *
	 * <p><b>Note:</b> Multiple version increments may also occur as part of pre-release or release
	 * candidate (RC) builds, as was the case for the A12 2026.06 release. That release shipped
	 * Kernel version 31.1.0, which contained DM version 29.4.0.</p>
	 */
	// NOTE: when this version is changed, the instructions of kernel-md/kernel-md-model-migration/README.md should be followed.
	public static final String VERSION = "29.8.0";

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
	 * @return see {@link IDocumentModelContent#getDocumentUniquenessCriteria()}
	 * @param documentUniquenessCriteria
	 * 		the new uniqueness criteria for the {@link DocumentModel}. Use an empty list to unset.
	 */
	@Builder.Default
	private @NonNull List<DocumentUniquenessCriterion> documentUniquenessCriteria = new ArrayList<>();
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
	@Builder.Default
	private DocumentModelRoot modelRoot = new DocumentModelRoot();

	public Optional<FieldTypeDefinition> getTypeDefinitionById(String typeDefId) {
		return typeDefinitions.stream().filter(e -> e.getId().equals(typeDefId)).findFirst();
	}
}
