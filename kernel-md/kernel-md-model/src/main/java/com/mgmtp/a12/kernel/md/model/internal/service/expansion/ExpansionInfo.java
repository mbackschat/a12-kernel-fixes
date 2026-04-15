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

package com.mgmtp.a12.kernel.md.model.internal.service.expansion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.model.header.ModelReference;

public class ExpansionInfo {

	private final String idPrefix;
	/*
	 * Collects all current model references. These model references must have unique IDs.
	 * These references are used to determine any cyclic dependency and also to get the current model reference path.
	 */
	private final List<ModelReference> modelReferencePath;
	private final boolean excludeRules;
	private final boolean excludeComputations;

	public ExpansionInfo() {
		this.idPrefix = "";
		this.modelReferencePath = new ArrayList<>();
		this.excludeRules = false;
		this.excludeComputations = false;
	}

	public ExpansionInfo(final ExpansionInfo formerExpansionInfo, final Group includingGroup) {
		final String includePrefix = includingGroup.getId() + "_";
		this.idPrefix = formerExpansionInfo.idPrefix.isEmpty()
				? includePrefix
				: formerExpansionInfo.idPrefix + includePrefix;
		// we already checked that the include details are present
		assert includingGroup.getIncludeDetails().isPresent();
		Group.IncludeDetails includeDetails = includingGroup.getIncludeDetails().get();
		this.excludeRules = formerExpansionInfo.excludeRules || includeDetails.isExcludeRules();
		this.excludeComputations = formerExpansionInfo.excludeComputations || includeDetails.isExcludeComputations();

		this.modelReferencePath = initModelReferencePath(includeDetails.getModelReference(), formerExpansionInfo);
	}

	public ExpansionInfo(final ModelReference modelReference, final ExpansionInfo formerExpansionInfo) {
		this.modelReferencePath = initModelReferencePath(modelReference, formerExpansionInfo);

		this.idPrefix = "";
		this.excludeRules = false;
		this.excludeComputations = false;
	}

	private List<ModelReference> initModelReferencePath(ModelReference modelReference, ExpansionInfo formerExpansionInfo) {
		final List<ModelReference> modelReferencePath = new ArrayList<>();
		if (formerExpansionInfo != null) {
			modelReferencePath.addAll(formerExpansionInfo.modelReferencePath);
		}
		modelReferencePath.add(modelReference);
		return modelReferencePath;
	}

	public String getUpdatedId(String oldId) {
		return this.idPrefix.isEmpty() ? oldId : this.idPrefix + oldId;
	}

	public boolean isExcludeRules() {
		return this.excludeRules;
	}

	public boolean isExcludeComputations() {
		return this.excludeComputations;
	}

	public Set<String> getModelReferences() {
		return this.modelReferencePath.stream()
				.map(ModelReference::getReference)
				.collect(Collectors.toSet());
	}

	public List<ModelReference> getModelReferencePath() {
		return Collections.unmodifiableList(modelReferencePath); // the modelReferencePath really shouldn't be externally modified
	}
}
