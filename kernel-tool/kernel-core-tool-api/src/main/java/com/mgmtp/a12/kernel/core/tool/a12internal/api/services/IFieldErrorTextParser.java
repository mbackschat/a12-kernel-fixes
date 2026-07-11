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
package com.mgmtp.a12.kernel.core.tool.a12internal.api.services;

import java.util.Collection;
import java.util.Locale;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

@OnlyForUsage
public interface IFieldErrorTextParser {

	/**
	 * Checks the error texts defined in the field type definition of a field for the given locales.
	 * <p>
	 * This method can be used for any field. Field types, that don't have an error text return
	 * <code>true</code>.
	 *
	 * @param field
	 * 		the field whose error texts in its field type should be checked, may not be null
	 * @param locales
	 * 		for which the error texts should be checked, the collection and its content may not be null
	 * @param pr
	 * 		problem reporter, may not be null
	 * @return <code>true</code>, if the error texts are correct and <code>false</code> otherwise.
	 */
	boolean hasValidErrorText(@NonNull IField field, @NonNull Collection<Locale> locales, @NonNull IProblemReporter pr);

	/**
	 * Checks the custom error texts defined for a required field for the given locales.
	 * <p>
	 * This method can be used for any field. For not required fields, this method returns <code>true</code>.
	 *
	 * @param field
	 * 		the field whose error texts defined in the requiredness config should be checked, may not be null
	 * @param locales
	 * 		for which the error texts should be checked, the collection and its content may not be null
	 * @param pr
	 * 		problem reporter, may not be null
	 * @return <code>true</code>, if the error texts are correct and <code>false</code> otherwise.
	 */
	boolean hasValidErrorTextInRequirednessConfig(@NonNull IField field, @NonNull Collection<Locale> locales,
			@NonNull IProblemReporter pr);
}
