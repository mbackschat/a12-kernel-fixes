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
import java.util.Locale;

import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;
import com.mgmtp.a12.model.header.Annotation;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * Abstract base type for all document hierarchy elements of a {@link DocumentModel}.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@OnlyForA12InternalUsage
public class Element implements IIdNamed, IDescribed, IAnnotated, Serializable {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * @return the parent {@link Group}. Is not {@code null} (except for
	 *        {@link DocumentModelRoot}).
	 * @param parent
	 * 		the new parent {@link Group}. May not be {@code null}.
	 */
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	protected Group parent;
	/**
	 * @return the id of this element. Is never {@code null}.
	 * @param id
	 * 		the new id for this element. May not be {@code null} or empty.
	 */
	protected @NonNull String id;
	/**
	 * @return the name of the element. Is never {@code null} or empty.
	 * @param name
	 * 		the new name of the element. May not be {@code null} or empty.
	 */
	protected @NonNull String name;
	/**
	 * @return the defined internal description in different {@link Locale}s. Is
	 * 		never {@code null}. If no internal descriptions were defined, the
	 *        {@link ILocalizedTextMap} is empty.
	 * @param internalDescription
	 * 		the new internal description in different {@link Locale}s. May not
	 * 		be {@code null}. Use an empty {@link ILocalizedTextMap} to unset.
	 */
	protected ILocalizedTextMap internalDescription = new LocalizedTextMapBuilder().build();
	/**
	 * @return the defined external description in different {@link Locale}s. Is
	 * 		never {@code null}. If no external descriptions were defined, the
	 *        {@link ILocalizedTextMap} is empty.
	 * @param externalDescription
	 * 		the new external description in different {@link Locale}s. May not
	 * 		be {@code null}. Use an empty {@link ILocalizedTextMap} to unset.
	 */
	protected ILocalizedTextMap externalDescription = new LocalizedTextMapBuilder().build();
	/**
	 * @return a map with the defined annotations in the order they were inserted into the map. Is
	 * 		never {@code null}. If no annotations were defined, the map is empty.
	 * @param annotations
	 * 		the new annotations in the order they were inserted into the map. May not
	 * 		be {@code null}. Use an empty map to unset.
	 */
	protected List<Annotation> annotations = new ArrayList<>();

	protected Element(Group parent, @NonNull String id, @NonNull String name, ILocalizedTextMap internalDescription,
			ILocalizedTextMap externalDescription, List<Annotation> annotations) {
		super();
		this.parent = parent;
		this.id = id;
		this.name = name;
		this.internalDescription = internalDescription;
		this.externalDescription = externalDescription;
		this.annotations = new ArrayList<>();
		this.annotations.addAll(annotations);
	}

	@Override
	public void setAnnotations(List<Annotation> annotations) {
		this.annotations = new ArrayList<>();
		this.annotations.addAll(annotations);
	}
}
