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
package com.mgmtp.a12.kernel.md.rt.api;

import java.util.Optional;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Provides access to the details of a single computed field instance.
 */
@OnlyForUsage
public interface IComputedFieldInstance {
	DocumentPointer pointer();

	/**
	 * Returns an object that is suitable for {@link FieldInstanceV2#value()}.
	 *
	 * @return the value that was computed. Can be empty in case of errors (see {@link #getErrorMessage()}).
	 */
	Object getValueV2();

	/**
	 * @return the computed value in string representation. Can be empty in case of errors (see {@link #getErrorMessage()}.
	 */
	Optional<String> getStringValue();

	/**
	 * @return an error message if there is a formal error with the computed value.
	 */
	Optional<IMessage> getErrorMessage();

	/**
	 * Applies the computed field instance's value to the provided {@link DocumentV2}.
	 * <p>
	 * The value of the {@link FieldInstanceV2} in the provided {@link DocumentV2} which corresponds to {@link #pointer()}
	 * is set to {@link #getValueV2()}.
	 * <p>
	 * If no such {@link FieldInstanceV2} exists yet, and {@link #getValueV2()} is neither {@code null} or {@code ""}, such a
	 * {@link FieldInstanceV2} will be created and added to the document.
	 *
	 * @param document
	 * 		the {@link DocumentV2} to apply the change to. May not be {@code null}.
	 */
	DocumentV2 applyTo(@NonNull DocumentV2 document);
}
