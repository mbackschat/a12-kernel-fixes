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
package com.mgmtp.a12.kernel.core.rt.a12internal.meta;

import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Common interface to meta data of all model entities.
 */
@OnlyForUsage
public interface IMetaEntity {

	/**
	 * Returns the value corresponding to the given key. Please refer to the javadoc of the
	 * specific meta entity for a list and a specification of the supported keys.
	 * <p>
	 * If the entity has no value for the key then <code>null</code> is returned.
	 * </p>
	 * <p>
	 * If additional parameters are passed, even though they are not required, they will be
	 * ignored.
	 * </p>
	 * <p>
	 * If a string is returned, it is UTF-8 encoded.
	 * </p>
	 * <p>
	 * <b>Note on querying maximum and minimum length for date values:</b>
	 * <p>
	 * Date values can be formatted variably at runtime by using a {@link PresentationInformation}. If this option is used, the
	 * query of minimum and maximum length of a field must also specify the presentation information.
	 * </p>
	 * <p>
	 * Specifically, the desired {@link PresentationInformation} should be passed as a parameter. Kernel then determines the
	 * correct maximum or minimum length for the passed presentation information.
	 * </p>
	 * <p>
	 * When specifying the minimum length, it is also taken into account whether a month (or day) specification may only use one
	 * digit. (This is also referred to as {@link PresentationInformation#DATE_ZERO_OPTIONAL})
	 * </p>
	 * <p>
	 * There is the following special case: The XML format is 'DD.MM.', i.e. the last character is a dot. The order of the day and
	 * month can be changed in the UI representation. This means that it is (theoretically) possible to select a UI representation
	 * that does not contain this special case. If the order month before day is selected in the UI representation, this final dot
	 * is omitted in the UI representation. A final dot is generally only permitted if it is a
	 * date, that only consists of the day and month and the day comes before the month.
	 *
	 * </p>
	 * </p>
	 *
	 * @param key
	 * 		key, whose value should be returned.
	 * @param params
	 * 		additional parameters that might be necessary for specification.
	 * @return value for this key
	 */
	public Object getValue(String key, Object... params);

	/**
	 * Returns all valid keys supported by the generated artifact.
	 * Note: a project may specify additional keys. Thus, the generated artifact may
	 * support more keys than documented in the meta interfaces, all keys are specified.
	 * Refer to the project documentation to get specifications of these additional keys.
	 *
	 * @return array of all valid meta keys for this artifact.
	 */
	public String[] getValidKeys();

	public boolean equals(Object other);

	public int hashCode();
}
