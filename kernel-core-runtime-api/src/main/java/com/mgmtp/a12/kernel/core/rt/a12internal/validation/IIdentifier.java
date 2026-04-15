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

import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;

/**
 * Specifies an <b>entity instance</b>, an <b>entity set</b> or an <b>entity variable</b>. An entity
 * may be a field or a group. In most cases, the instance is restricted to fields, sets of fields or
 * field variables. Currently, only in the {@link IData} interface used for the validation and
 * calculation, groups may be specified, too.
 * <br>
 * Access is optimized for loose coupling between the technical path name of the
 * entity and the specification of the index on the different hierarchy levels. Indices are given
 * only for repetition layers.
 *
 * <ul>
 * <li>For <b>unique</b> model type versions, only root groups and fields are repeatable.
 * <li>For <b>non-unique</b> model type versions, all groups are repeatable, including root groups.
 * </ul>
 * For unique model type versions this means, that all <em>field</em> identifiers have exactly two
 * repetition layers (root group and field) and <em>group</em> identifiers have exactly one (only
 * root group).
 * For non-unique model type versions, there are at least two repetition layers for a <em>field</em>
 * identifier or <em>group</em> identifiers, except group identifiers for <em>root groups</em>,
 * which
 * have exactly one.
 *
 * <p>
 * An implementation of this API has to be provided by the caller.
 * </p>
 * <p>
 * <u>Note</u>
 * </p>
 * The implementation provided by the caller will be only used for following purposes:
 * <ul>
 * <li>To pass the data to the validation API.</li>
 * <li>At runtime to internally cache wrong values. <b>Important:</b> Make sure to implement the
 * <i>hashCode</i>- and <i>equals</i>-methods.</li>
 * </ul>
 * For all other uses, the runtime works with its own implementation that is optimized
 * for its internal access scheme.
 *
 * <p>
 * <i>used terminology</i>:
 * An <b>entity name</b> contains a path and the short name of the field or group.
 * E.g.: For a field with name <code>/EUn/Enterprise/Address/Place</code>,
 * <code>/EUn/Enterprise/Address</code> is the path to the field, and <code>Place</code> is the
 * short name of the field.
 * For a group with name <code>/EUn/Enterprise/Address</code>, <code>/EUn/Enterprise</code> is the
 * path of the group, and <code>Address</code> is the short name of the group.
 */
public interface IIdentifier {
	/**
	 * SEPARATOR is used as a delimiter in path names.
	 */
	public static final String SEPARATOR = "/";

	/**
	 * {@link #ALL_INDICES} is used to represent the asterisk operator for one repeatable level, an
	 * abbreviation for all indices. By using this constant, there is no need to explicitly specify
	 * all Identifiers individually. See e.g.
	 * {@link IValidator#validatePartlyWith3ValueLogic}
	 * for the usage of {@link #ALL_INDICES} for the specification of relevant fields.
	 */
	@SuppressWarnings("javadoc")
	public static final long ALL_INDICES = 0;

	/**
	 * ITERATION is used to represent the iteration over the repeatable level.
	 * See {@link IMetaField IMetaField#getValue("OperandenMenge")} for an example of usage.
	 */
	public static final long ITERATION = -1;

	/**
	 * If no USB was specified, all USB specifications use the value USB_NOT_SPECIFIED.
	 */
	public static final long USB_NOT_SPECIFIED = -2;

	/**
	 * SEMANTIC_INDEX is used to represent a level, that is specified by a semantic index.
	 */
	public static final long SEMANTIC_INDEX = -4;

	/**
	 * UNKNOWN is used, when for a semantic index or parallel iteration the iteration cannot be
	 * determined, e.g. no repetition with this semantic index exists.
	 */
	public static final long UNKNOWN = -5;

	/**
	 * PARALLEL_ITERATION is used to represent a level that is repeated parallel in independent
	 * groups.
	 */
	public static final long PARALLEL_ITERATION = -6;

	/**
	 * REP_NOT_UNIQUE_REFERENCED_LEVEL_TO_BE_REPLACED is used to represent a level of the referenced
	 * fields in meta rules for the language construct RepetitionNotUniue (German name:
	 * WiederholungNichtEindeutig) that has to be replaced by all concrete erroneous repetitions. S. EXT-20784
	 */
	public static final long REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED = -7;

	/**
	 * Returns the full name of the field or group. The full name has to start with
	 * {@link #SEPARATOR}. After the first part specifying the name of the root group,
	 * the short names of the groups follow in the order of their containment hierarchy, separated
	 * by {@link #SEPARATOR}. The last part is the short name of field or group.
	 *
	 * <p>
	 * <u>Assertion</u>
	 * </p>
	 * The path name for a field will always consist of at least two parts (root group name, short
	 * field name), because no field exists without a containing root group. A field doesn't need
	 * to lie in an intermediate group. Only group identifiers for root groups contain one part in
	 * the path.
	 *
	 * @return full name of the field or group
	 */
	public String getName();

	/**
	 * <p>
	 * Returns the indices of the different levels.
	 *
	 * <ul>
	 * <li>For <b>H-DVs</b>, only root groups and fields are possibly
	 * repeatable.
	 * Only these will be returned. Therefore, the length of the array is exactly 2 for fields and 1
	 * for groups.</li>
	 * <li>For <b>A12-DVs</b>, all layers are returned including root group
	 * and
	 * field level. I.e. the length of the returned array is at least 2 for identifiers,
	 * except for group identifiers for root groups.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * The value for the individual layers may be
	 * <ul>
	 * <li><code>&gt;= 1</code> for the repetition of the corresponding group. It should be 1 for
	 * non-repeatable groups.</li>
	 * <li>{@value #ALL_INDICES} if the set of all repetitions for this layer should be referred to.
	 * In this case a complete set of fields or groups is referenced.</li>
	 * <li>{@value #ITERATION} if the field/group is iterated over and the current repetition should
	 * be determined by the iteration.</li>
	 * <li>{@value #SEMANTIC_INDEX} if the field/group has a semantic index and the repetition is to
	 * be determined by the value of the corresponding index field.</li>
	 * <li>{@value #UNKNOWN} if the repetition was tried to be resolved from a semantic index or a
	 * parallel iteration, but for no iteration, the corresponding index field matched the desired
	 * value.</li>
	 * <li>{@value #PARALLEL_ITERATION} if the field/group is used in a parallel iteration and the
	 * repetition is to be determined by the value of the corresponding index field for the
	 * iteration value.</li>
	 * </ul>
	 * All the values <code>&lt;0</code> should usually not be used by the caller but only as
	 * return values from kernel. Return values should usually not contain the values
	 * {@value #SEMANTIC_INDEX} or {@value #PARALLEL_ITERATION}.
	 *
	 * @return all repeatable indices
	 */
	public long[] getIndices();

	/**
	 * Returns the USB of this field instance. If no USB was specified for the field,
	 * {@link #USB_NOT_SPECIFIED} is returned.
	 *
	 * @return value of the USB or {@link #USB_NOT_SPECIFIED}, if no USB was specified.
	 */
	public long getUsb();
}
