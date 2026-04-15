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

package com.mgmtp.a12.kernel.md.facade.a12internal;

import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * In this class, Kernel will offer the document migration for ticket A12-15101.
 */
@OnlyForUsage
public class DocumentMigrationService {

	private static final String EMPTY_VALUE_IDENTIFIER = "emptyValue=\"true\"";
	/**
	 * This regex covers the fact that a project can manipulate the serialized document right before it is saved in the database.
	 */
	private static final String FIELD_W_EMPTY_VALUE_SERIALIZATION = "<([^\\s>]+)([^>]*)></\\1(\\s*)>";

	/**
	 * Migrates the given document as follows: all serialized field instances with the form &lt;field_name&gt;&lt;/field_name&gt;
	 * will be textually migrated to &lt;field_name emptyValue="true"&gt;&lt;/field_name&gt;
	 *
	 * @param document
	 * 		serialized document in XML format. Must not be {@code null} or empty.
	 * @return migrated document. Is never {@code null}.
	 */
	public String migrate(String document) {
		Validate.notNull(document, "document must not be null");
		Validate.isTrue(document.length() > 0);
		// already migrated? We don't support the mixture of parts already migrated and parts to migrate
		if (document.contains(EMPTY_VALUE_IDENTIFIER)) {
			return document;
		}
		// To keep readability, we don't use the constant EMPTY_VALUE_IDENTIFIER in the next line
		return document.replaceAll(FIELD_W_EMPTY_VALUE_SERIALIZATION, "<$1 emptyValue=\"true\"$2></$1$3>");
	}

}
