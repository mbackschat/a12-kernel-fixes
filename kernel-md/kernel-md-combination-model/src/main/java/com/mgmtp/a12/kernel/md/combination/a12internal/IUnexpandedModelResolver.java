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

package com.mgmtp.a12.kernel.md.combination.a12internal;

import org.apache.commons.lang3.NotImplementedException;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views.MM_CombinationModel_1;
import com.mgmtp.a12.kernel.mmtypings.mm_selectionmodel_2.views.MM_SelectionModel_2;
import com.mgmtp.a12.model.utils.Experimental;

import lombok.NonNull;

/**
 * Resolver that is able to resolve a {@link DMLike} (either a {@link IDocumentModel} or a {@link MM_CombinationModel_1 CombinationModel}) and {@link MM_SelectionModel_2 SelectionModel}.
 * <p>
 * Implementations of this resolver are used by {@link CombinationModelService} and {@link ExpandingDmResolver}.
 * A default/reference implementations are provided with {@link UnexpandedModelResolverImpl} and {@link DirectoryBasedUnexpandedModelResolver}.
 */
@Experimental
public interface IUnexpandedModelResolver {

	/**
	 * Resolves an (unexpanded) {@link DMLike}. May throw an exception when the model cannot be resolved.
	 * <p>
	 * @throws CombinationException when there are problems resolving the model.
	 */
	@NonNull DMLike resolve(@NonNull String dmId);

	/**
	 * @return the loaded {@link MM_SelectionModel_2 SelectionModel}, may throw exceptions when the model cannot be loaded.
	 * <p>
	 * @throws CombinationException when there are problems resolving the selection model.
	 */
	default MM_SelectionModel_2 resolveSelectionModel(@NonNull String selId) {
		throw new NotImplementedException("the resolving of selection models is not implemented");
	}
}
