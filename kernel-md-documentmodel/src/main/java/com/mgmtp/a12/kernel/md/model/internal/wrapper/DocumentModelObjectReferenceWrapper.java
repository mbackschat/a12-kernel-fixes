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
package com.mgmtp.a12.kernel.md.model.internal.wrapper;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelObjectReference;
import com.mgmtp.a12.kernel.md.model.api.IEntity;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocumentModelObjectReferenceWrapper<T extends Serializable> implements IDocumentModelObjectReference<T> {

	@NoNeedToAdaptSerialVersionUID @Serial
	private static final long serialVersionUID = 1L;

	private final @NonNull String relativePath;
	private final T documentModelObject;

	public static DocumentModelObjectReferenceWrapper<IEntity> wrapEntity(
			@NonNull com.mgmtp.a12.kernel.md.model.a12internal.IDocumentModelObjectReference<com.mgmtp.a12.kernel.md.model.a12internal.IEntity> reference
	) {
		return wrap(reference, dmo -> switch (dmo) {
			case Group group -> new GroupWrapper(group);
			case Field field -> new FieldWrapper(field);
			default -> null;
		});
	}

	public static DocumentModelObjectReferenceWrapper<IField> wrapField(
			@NonNull com.mgmtp.a12.kernel.md.model.a12internal.IDocumentModelObjectReference<com.mgmtp.a12.kernel.md.model.a12internal.Field> reference
	) {
		return wrap(reference, FieldWrapper::new);
	}

	private static <T extends Serializable, R extends Serializable> DocumentModelObjectReferenceWrapper<R> wrap(
			@NonNull com.mgmtp.a12.kernel.md.model.a12internal.IDocumentModelObjectReference<T> reference,
			@NonNull Function<T, R> dmoMapper
	) {
		return new DocumentModelObjectReferenceWrapper<>(
				reference.getRelativePath(),
				reference.getDocumentModelObject().map(dmoMapper).orElse(null));
	}

	@Override
	public @NonNull String getRelativePath() {
		return relativePath;
	}

	@Override
	public Optional<T> getDocumentModelObject() {
		return Optional.ofNullable(documentModelObject);
	}

}
