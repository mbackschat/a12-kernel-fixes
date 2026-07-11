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
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelInfo;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.model.header.Header;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Specific meta information about the {@link DocumentModel}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DocumentModelInfo implements Serializable {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * This is the name of the {@link IDocumentModel}. In A12 this name is equal to
	 * {@link Header#getId()}.
	 *
	 * @return the name of the {@code DocumentModel}. Is never {@code null} or empty.
	 * @param name
	 * 		the new name of the {@code DocumentModel}. May not be {@code null} or empty.
	 */
	private @NonNull String name;
	/*
	 * The setter is not necessary because the variant of the {@code DocumentModel} is set when
	 * exporting it and changing the variant afterwards is not usefull.
	 */
	@Setter(value = AccessLevel.NONE)
	private String variant;
	/**
	 * @param branch
	 * 		the new name of the branch of the {@code DocumentModel}. May be {@code null} to
	 * 		unset.
	 */
	private String branch;

	/**
	 * See {@link IDocumentModelInfo#getBaseYear()}.
	 *
	 * @param baseYear
	 * 		new year which should be taken for year based operations. May be {@code null} to
	 * 		unset.
	 */
	private Integer baseYear;
	/**
	 * @param revision
	 * 		the new name of the revision (working status) of the {@code DocumentModel}.
	 * 		May be {@code null} to unset.
	 */
	private String revision;
	/**
	 * At the moment this property does not affect any process in A12.
	 *
	 * @return {@code false} if this {@link DocumentModel} may be changed. Otherwise {@code true}.
	 * @param immutable
	 * 		use {@code false} if this {@link DocumentModel} may be changed. Otherwise
	 *        {@code true}.
	 */
	@Builder.Default
	private boolean immutable = false;
	/**
	 * @param comment
	 * 		the new comment (or an internal description) for the {@link DocumentModel}.
	 * 		May be {@code null} to unset.
	 */
	private String comment;
	/**
	 * @param packet
	 * 		the packet of the {@link DocumentModel}.
	 * 		May be {@code null} to unset.
	 */
	private String packet;
	/**
	 * @return {@code true} if this {@link DocumentModel} is intended to be a meta data document model (MD-DM). Otherwise
	 *        {@code false}.
	 * @param isMetaData
	 * 		use {@code true} if this {@link DocumentModel} is a meta data model. Otherwise {@code false}.
	 */
	@Builder.Default
	private boolean metaData = false;

	/**
	 * @return see {@link IDocumentModelInfo#getVariant()}.
	 */
	public Optional<String> getVariant() {
		return Optional.ofNullable(variant);
	}

	/**
	 * @return the name of the branch from the {@code DocumentModel}.
	 */
	public Optional<String> getBranch() {
		return Optional.ofNullable(branch);
	}

	/**
	 * @return the name of the revision (working status) of the {@code DocumentModel}.
	 */
	public Optional<String> getRevision() {
		return Optional.ofNullable(revision);
	}

	/**
	 * See {@link IDocumentModelInfo#getBaseYear()}.
	 *
	 * @return year which should be taken for year based operations.
	 */
	public Optional<Integer> getBaseYear() {
		return Optional.ofNullable(baseYear);
	}

	/**
	 * @return the comment (or an internal description) for the {@link DocumentModel}.
	 */
	public Optional<String> getComment() {
		return Optional.ofNullable(comment);
	}

	/**
	 * @return the packet of the {@link DocumentModel}.
	 */
	public Optional<String> getPacket() {
		return Optional.ofNullable(packet);
	}
}
