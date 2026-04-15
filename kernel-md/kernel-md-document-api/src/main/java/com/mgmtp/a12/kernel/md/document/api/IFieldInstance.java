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
package com.mgmtp.a12.kernel.md.document.api;

import java.util.Optional;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.document.api.services.IDocumentService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.utils.CustomImplementationIsOptional;

/**
 * Represents an instance of an {@link IField} (which is an element of a {@link IDocumentModel}) in
 * a {@link IDocument}.
 *
 * @deprecated use {@code FieldinstanceV2} or {@code DocumentPointer} instead.
 */
@CustomImplementationIsOptional
@Deprecated(since = "30.0.0")
public interface IFieldInstance extends IEntityInstance {

	/**
	 * The type of the value is derived from the corresponding {@link IFieldType} of the
	 * {@link IField} which this instance is referring to. The supported java types are listed at
	 * {@link IDocumentService#convertToJavaType(IFieldInstance, String, Consumer)}.
	 *
	 * @return the corresponding value of this {@link IFieldInstance}. Can be empty if no value has
	 * 		been set.
	 */
	Optional<Object> getValue();

	/**
	 * <b>Hint:</b>
	 * To convert the string representation of the value to the corresponding java type see
	 * {@link IDocumentService#convertToJavaType(IFieldInstance, String, Consumer)} (in the
	 * method description, you can find a list of the supported java types).
	 *
	 * @param value
	 * 		the new value for this {@link IFieldInstance}. May be {@code null} or empty to
	 * 		unset the value.
	 */
	void setValue(Object value);

}
