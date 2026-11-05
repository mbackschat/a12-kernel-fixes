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

package com.mgmtp.a12.kernel.mapping.rt.api;

import java.util.List;

import com.mgmtp.a12.kernel.md.rt.api.IMessage;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

@OnlyForUsage
public interface SourceEligibilityResult {
	/**
	 * The id that was specified for the corresponding source document in
	 * {@link SourceDocument#sourceInstanceId()}.
	 */
	@NonNull String sourceInstanceId();

	/**
	 * @return true if the source document was included in the mapping; false if it
	 * 		was deselected because of failed source-selection validation.
	 * 		See also {@link #messages()}.
	 */
	boolean sourceWasIncludedInMapping();

	/**
	 * Messages from the validation of the source document during the
	 * source-instance selection step. (Normally, if no messages with Error
	 * severity are contained, then {@link #sourceWasIncludedInMapping()}
	 * returns true)
	 */
	// see the javadoc of IDataTransferService#getTransferredData for details about what messages
	// are reported to the IResults of the SourceData
	@NonNull List<IMessage> messages();

	static @NonNull SourceEligibilityResult of(
			@NonNull String sourceInstanceId,
			boolean sourceWasIncludedInMapping,
			@NonNull List<IMessage> messages
	) {
		return new SourceEligibilityResultImpl(sourceInstanceId, sourceWasIncludedInMapping, messages);
	}
}
