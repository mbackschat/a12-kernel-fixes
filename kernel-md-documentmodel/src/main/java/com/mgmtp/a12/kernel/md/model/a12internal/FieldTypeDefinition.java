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

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.model.header.Annotation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Models a {@link FieldType} which is normally complex and that's why is used several times in the
 * current document model (even in other document models {@literal ->} ExtMP approach).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class FieldTypeDefinition implements IIdNamed, IDescribed, IAnnotated {

	@Serial @NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @return the id of the type definition. Is never {@code null} or empty.
	 * @param id
	 * 		the new id of this type definition. May not be {@code null} or empty.
	 */
	private @NonNull String id;
	/**
	 * @return the name of this type definition. Is never {@code null} or empty.
	 * @param name
	 * 		the new name of this type definition. May not be {@code null} or empty.
	 */
	private @NonNull String name;
	/**
	 * @return the defined internal description in different {@link Locale}s.
	 * @param internalDescription
	 * 		the new internal description in different {@link Locale}s. May not be
	 *        {@code null}. Use an empty {@link ILocalizedTextMap} to unset.
	 */
	@Builder.Default
	private @NonNull ILocalizedTextMap internalDescription = new LocalizedTextMapBuilder().build();
	/**
	 * @return the defined external description in different {@link Locale}s.
	 * @param externalDescription
	 * 		the new external description in different {@link Locale}s. May not be
	 *        {@code null}. Use an empty {@link ILocalizedTextMap} to unset.
	 */
	@Builder.Default
	private @NonNull ILocalizedTextMap externalDescription = new LocalizedTextMapBuilder().build();
	/**
	 * @return a map with the defined annotations in the order they were inserted into the map. Is
	 * 		never {@code null}. If no annotations were defined, the map is empty.
	 * @param annotations
	 * 		the new annotations in the order they were inserted into the map. May not be
	 *        {@code null}. Use an empty map to unset.
	 */
	@Builder.Default
	private @NonNull List<Annotation> annotations = new ArrayList<>();
	/**
	 * @return the specific definition of the {@link FieldType}. Is never {@code null}.
	 * @param fieldType
	 * 		the new definition of the {@link FieldType} for this type definition. May not be
	 *        {@code null}.
	 */
	private @NonNull FieldType fieldType;
	/**
	 * @param revision
	 * 		the new designation of the version of this type definition (ExtMP-specific). May be
	 *        {@code null} to unset.
	 */
	private String revision;
	/**
	 * @param changeNote
	 * 		the new change note documenting why changes to this type definition were done. May be
	 *        {@code null} to unset.
	 */
	private String changeNote;

	/**
	 * @return the designation of the version of this type definition (ExtMP-specific).
	 */
	public Optional<String> getRevision() {
		return Optional.ofNullable(revision);
	}

	/**
	 * @return the change note documenting why changes to this type definition were done. Empty if
	 * 		none was defined.
	 */
	public Optional<String> getChangeNote() {
		return Optional.ofNullable(changeNote);
	}

	/**
	 * @return {@code false} is this type definition may be changed. Otherwise {@code true}.
	 */
	public boolean isImmutable() {
		return getRevision().isPresent();
	}

}
