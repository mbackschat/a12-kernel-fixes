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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.util;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.RuntimeController;

/**
 * An instance of this class specifies the definition of a semantic index before the concretization
 * with indices.
 * <p>
 * This class is a template to generate a {@link SemanticIndexSpec} with the method
 * {@link #createSemanticIndex}.
 */
public class SemanticIndexTemplate {
	final SemanticIndexSpec constant;
	final RtIdentifierTemplate field;

	private SemanticIndexTemplate(SemanticIndexSpec constant, RtIdentifierTemplate container) {
		if (constant != null) {
			this.constant = constant;
		} else {
			this.constant = null;
		}
		if (container != null) {
			this.field = container;
		} else {
			this.field = null;
		}
	}

	public static SemanticIndexTemplate createNullIndex() {
		return new SemanticIndexTemplate(null, null);
	}

	public static SemanticIndexTemplate createFromConstant(String constant) {
		return new SemanticIndexTemplate(new SemanticIndexSpec(constant), null);
	}

	public static SemanticIndexTemplate createFromContainer(RtIdentifierTemplate container) {
		return new SemanticIndexTemplate(null, container);
	}

	public static SemanticIndexTemplate createFromStringRepresentation(String semIndexSpec) {
		if (semIndexSpec == null) {
			return createFromConstant(null);
		} else if (semIndexSpec.startsWith("\"")) {
			return createFromConstant(RuntimeUtils.removeQuotes(semIndexSpec));
		} else {
			RtIdentifierTemplate field = RtIdentifierTemplate.fromStringRepresentation(semIndexSpec);
			return createFromContainer(field);
		}
	}

	public static SemanticIndexTemplate createFromSpec(SemanticIndexSpec semIdxSpec) {
		if (semIdxSpec == null) {
			return createNullIndex();
		} else if (semIdxSpec.isConstant()) {
			return new SemanticIndexTemplate(semIdxSpec, null);
		} else {
			return createFromContainer(RtIdentifierTemplate.createFromIdentifierWithoutSemIdx(semIdxSpec.getAsEntity()));
		}
	}

	public RtIdentifierTemplate getField() {
		return field;
	}

	public SemanticIndexSpec createSemanticIndex(IterationState iterationState) {
		if (constant != null) {
			return constant;
		} else if (field != null && iterationState != null) {
			RtInternalIdentifier id = RuntimeController.makeIdentifier(iterationState, field);
			return new SemanticIndexSpec(id);
		} else {
			return null;
		}
	}
}
