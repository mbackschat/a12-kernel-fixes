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

package com.mgmtp.a12.kernel.core.rt.a12internal.utils;

import com.mgmtp.a12.model.utils.OnlyForUsage;

@OnlyForUsage
public class RuntimeApiConstants {
	// Don't remove 'new String(..)' to prevent constant inlining (at compile-time).
	// Don't change the format of this line, it is automatically parsed and checked by verify-versions.sh
	// see  MGM Wiki Shortlink x/ZGQqDQ   for notes on how to update
	public static final String API_VERSION = new String("33.0.2");
	// Don't remove 'new String(..)' to prevent constant inlining (at compile-time).
	// Don't change the format of this line, it is automatically parsed and checked by verify-versions.sh
	// see MGM Wiki Shortlink x/ZGQqDQ   for notes on how to update
	// @formatter:off
	// NOTE: The version specified below is the Kernel version, not the runtime API version!!!
	public static final String OLDEST_COMPATIBLE_RUNTIME_AND_GENCODE_VERSION = new String("30.4.8"); // last checked for api version 33.0.2 Keep this comment here as it is (no extra or new line breaks), it will be automatically checked during api deployment
	// @formatter:on
}
