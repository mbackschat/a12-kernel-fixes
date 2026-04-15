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

package com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer;

import java.util.List;
import java.util.Set;

import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

@OnlyForUsage
public interface MappedData {
	/**
	 * @return true if the datatransfer was successful, false otherwise.
	 * 		<p>
	 * 		Related but different: if some or all <em>source</em> data is deselected in the source-selection step, the result is
	 * 		considered neither failed not incomplete, as this deselection is considered to be regular modeled behavior.
	 * 		<p>
	 * 		Note: currently, {@link #success()} always returns true, because in case of precomputation errors, the mapping will
	 * 		nonetheless continue (but some mapped values might be omitted).
	 * 		There is currently no flag that explicitly identifies <em>incomplete</em> mapping
	 * 		results that are caused by unsuccessful precomputations. If this information is needed please contact the kernel team. (It
	 * 		can be <em>approximated</em> by checking {@code targetResult} and {@code precomputationConsistencyResult} for errors)
	 * 		(but the presence of errors does not guarantee that some values have indeed been omitted in the mapping, as
	 * 		invalidly-precomputed values that are <em>not</em> operands of further precomputations will result in reported errors,
	 * 		but will not themselves be omitted).
	 */
	// NOTE the javadoc of this method should be kept in sync with the javadoc of
	// com.mgmtp.a12.kernel.mapping.rt.api.MappingResult#success
	boolean success();

	Set<IIdentifier> removedEntities();

	List<MappedField> mappedFields();

	/**
	 * The {@link SourceData#sourceId()} of all SourceData that have been selected in this datatransfer (they were determined
	 * to be eligible by the validation selection step, and were not in excess of the maximum allowed number of source instances
	 * for the corresponding source).<br/>
	 * If the datatransfer overall is successful ({@link #success()} returns true), then this is the set of included sources, on
	 * which the transferred data is based.<br>
	 * (If the datatransfer returns a failure, this set will nonetheless contain the selected sources; which were inputs to the
	 * failed precomputation or later step)
	 */
	Set<String> selectedSourceIds();
}
