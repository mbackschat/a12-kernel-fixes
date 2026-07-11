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

package com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages;

public enum DataTransferErrorEnum {
	/**
	 * Source DVs / DMs must have exactly one root group (as long as root-absorbing includes (currently the only implemented
	 * includes) are used for sources)
	 */
	SOURCE_HAS_MULTIPLE_ROOTGROUPS,
	SOURCE_ROOTGROUP_REPEATABLE,
	NO_COMMON_LOCALES_BASE,
	NO_COMMON_LOCALES_PRECOMP,
	PROBLEM_DURING_PRECOMPUTATION_DM_CREATION,
	NONERROR_MESSAGE_DURING_PRECOMPUTATION_DM_CREATION,
	/**
	 * If the timezones differ, corrupted data can happen very easily because the same string representation of a date/time
	 * value is deserialized to different java.time.Instants depending on the time zone (probably similar for the conversion to
	 * the vk-internal string representation?)
	 */
	DIFFERENT_TIMEZONES,
	SOURCE_NAME_CLASHES_WITH_TARGET_ROOTGROUP,
	PRECOMPUTATION_FIELDS_IN_TARGET,
	/**
	 * for this error we only really care that no <em>fields</em> in the precomp fragment are in or below target root groups,
	 * but it seems simpler to completely prevent overlapping root groups. (currently, we prune all empty groups recursively before
	 * applying this check, because the fragment may contain empty target groups).
	 */
	PRECOMP_FRAGMENT_OVERLAPS_TARGET,

	WARN_NOT_ALL_TARGET_LOCALES_SUPPORTED_BASEDM,
	WARN_NOT_ALL_TARGET_LOCALES_SUPPORTED_PRECOMP_DM,
	WARN_PRECOMP_FRAGMENT_HAS_DIFFERENT_DECIMAL_SEPARATOR,
	SOURCE_ALLOWS_UNSUPPORTED_CHARACTERS,

	INFO_NO_PRECOMPUTATION_FRAGMENT_SPECIFIED;
}
