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
package com.mgmtp.a12.kernel.core.rt.a12internal.validation;

import java.util.Iterator;

/**
 * This API specifies the access on the data that is passed to the validation methods. This
 * interface has to be implemented by the caller in order to achieve loose coupling between runtime
 * and caller.
 * <br>
 * <b>Note:</b> Internally, the runtime uses its own data cache for high-performance access on the
 * user data. The implementation of this interface is only used for an initial access of the entity
 * values. Thus, later changes to the user data will not be noticed by the validation runtime.
 */
public interface IData extends Iterable<IIdentifier> {

	/**
	 * Returns the value of the passed field or group.
	 * <p>
	 * <u>Pre-condition</u>:
	 * <ol>
	 * <li>The parameter specifies a valid field or group of the model type, which is passed to the
	 * validation or calculation.
	 * </li>
	 * <li>The parameter is not <code>null</code>.</li>
	 * <li>The parameter specifies a particular field. Wildcards for the specification of an index
	 * are not allowed.</li>
	 * </ol>
	 * An access of a field or group value is a read-only access on an {@link IData} object.
	 *
	 * <p>
	 * The value of a group only specifies that the group is not empty. Therefore, to represent a
	 * non-empty group this value may be any string (except <code>null</code> or the empty string).
	 * </p>
	 *
	 * @param entity
	 * 		identification of the field/group
	 * @return the value of the field/group or <code>null</code> / the empty string, if not present
	 */
	public String getValue(IIdentifier entity);

	/**
	 * Returns the value to display of the given field.
	 * <p>
	 * <u>Note:</u> The parameter $field.value$ will be replaced in the runtime with the returned
	 * value of this method.
	 *
	 * <p>
	 * <u>Attention:</u> The implementation of the {@link IIdentifier} given in the parameter entity
	 * will not be the one used in the result of {@link #iterator()} but an internal implementation.
	 * Therefore, if the implementation uses e.g. a HashMap for data storage, the method
	 * must take care of transforming the given entity to the correct implementation.
	 *
	 * @param entity
	 * 		identification of the field
	 * @return the value of the field to display or <code>null</code> / the empty string, if not
	 * 		present
	 */
	public String getValueForDisplay(IIdentifier entity);

	/**
	 * Returns the iterator over all user data fields.
	 * <p>
	 * <u>Extra condition:</u>
	 * <nl>
	 * <li>All index specifications of the returned {@link IIdentifier}s have to be concrete
	 * (without Wildcards).</li>
	 * <li>The iterator may only return fields or groups that are specified in the model type.</li>
	 * </nl>
	 * </p>
	 * <p>
	 * <u>Note:</u> The runtime can achieve a higher performance, if fields and groups without
	 * values
	 * are not returned by the iterator. However, this is not a condition for correct behaviour.
	 * This method is used by the runtime during formal validation. During this phase of validation
	 * all passed field values are checked for syntactical correctness.
	 * </p>
	 *
	 * @return the iterator over all {@link IIdentifier}s. If there are no fields or groups present,
	 * 		an empty iterator is returned. The iterator may not be <code>null</code>.
	 */
	public Iterator<IIdentifier> iterator();

	/**
	 * Returns the String representation of the {@link IIdentifier}. This method is used to replace
	 * the field or group identifier with its String representation in error messages. The
	 * implementation of the String representation is provided by the caller. The error message
	 * parameter <i>$&lt;fieldName&gt;$</i> (including the $ signs) is replaced by the String
	 * representation of the {@link IIdentifier}. The caller has to make sure to choose an
	 * implementation that allows him to extract the information he needs from the error message and
	 * their field references.
	 * <p>
	 * <u>Note:</u>Using a string representation given by the caller enables a loose coupling
	 * between caller and the runtime.
	 * </p>
	 *
	 * @param entity
	 * 		identification of the field or group
	 * @return {@link IIdentifier} in String representation
	 */
	public String identifierToString(IIdentifier entity);

	/**
	 * Adds a field-value pair to the data.
	 *
	 * <p>
	 * <i>This method is used for the XML-Reader.</i>
	 * </p>
	 *
	 * @param field
	 * 		the identifier of the field (s. {@link IIdentifier} )
	 * @param value
	 * 		the value of the field
	 */
	public void addValue(IIdentifier field, String value);

}
