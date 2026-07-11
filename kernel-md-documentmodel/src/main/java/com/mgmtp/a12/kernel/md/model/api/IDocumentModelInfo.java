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

package com.mgmtp.a12.kernel.md.model.api;

import java.io.Serializable;
import java.util.Optional;

import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Specific meta information about the {@link IDocumentModel}.
 */
@OnlyForUsage
public interface IDocumentModelInfo extends Serializable {

	/**
	 * This is the name of the {@link IDocumentModel}. In A12 this name is equal to
	 * {@link Header#getId()}.
	 *
	 * @return the name of the {@link IDocumentModel}.
	 */
	@NonNull String getName();

	/**
	 * @return the variant (formerly known as @{code application}). At the moment, this method
	 * 		returns a value when working with document models generated from ExtMP.
	 */
	Optional<String> getVariant();

	/**
	 * @return the name of the model branch (see branching in version control systems)
	 */
	Optional<String> getBranch();

	/**
	 * @return the revision of the model (see revision in version control systems)
	 */
	Optional<String> getRevision();

	/**
	 * In version control systems, for example, a particular revision is immutable.
	 * <p>
	 * <b>Hint:</b> Currently, this feature does not affect any process in A12.
	 *
	 * @return {@code false} if the document model may be changed, otherwise {@code true}.
	 */
	boolean isImmutable();

	/**
	 * @return the comment on the document model.
	 */
	Optional<String> getComment();

	/**
	 * @return the packet of the document model. At the moment, this method returns a value when document models have been
	 * 		generated from ExtMP for a packet specification.
	 */
	Optional<String> getPacket();

	/**
	 * This property will be used while handling date values without the year input (this is the
	 * case for fields of type date fragment whose format does not contain the year part).
	 *
	 * @return the base year of the document model.
	 */
	Optional<Integer> getBaseYear();

}
