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

import type {IHashAndEquals} from "../CollectionsWrapper.js";

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
 * <br>
 * There are at least one repetition layer for <em>field</em>
 * identifiers and at least two repetition layers for <em>group</em> identifiers, except group identifiers for <em>root groups</em>,
 * which have exactly one.
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
 */
export interface IIdentifier extends IHashAndEquals {
	/**
	 * Returns the full name of the field or group. The full name has to start with
	 * {@link SEPARATOR}. After the first part specifying the name of the root group,
	 * the short names of the groups follow in the order of their containment hierarchy, separated
	 * by {@link SEPARATOR}. The last part is the short name of field or group.
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
	getName(): string;

	/**
	 * <p>
	 * Returns the indices of the different levels.
	 *
	 * <br>
	 * All layers are returned including root group and
	 * field level. I.e. the length of the returned array is at least 2 for identifiers,
	 * except for group identifiers for root groups.
	 * </p>
	 *
	 * The value for the individual layers may be
	 * <ul>
	 * <li><code>&gt;= 1</code> for the repetition of the corresponding group. It should be 1 for
	 * non-repeatable groups.</li>
	 * <li>{@link ALL_INDICES} if the set of all repetitions for this layer should be referred to.
	 * In this case a complete set of fields or groups is referenced.</li>
	 * <li>{@link ITERATION} if the field/group is iterated over and the current repetition should
	 * be determined by the iteration.</li>
	 * <li>{@link SEMANTIC_INDEX} if the field/group has a semantic index and the repetition is to
	 * be determined by the value of the corresponding index field.</li>
	 * <li>{@link UNKNOWN} if the repetition was tried to be resolved from a semantic index or a
	 * parallel iteration, but for no iteration, the corresponding index field matched the desired
	 * value.</li>
	 * <li>{@link PARALLEL_ITERATION} if the field/group is used in a parallel iteration and the
	 * repetition is to be determined by the value of the corresponding index field for the
	 * iteration value.</li>
	 * </ul>
	 * All the values <code>&lt; 0</code> should usually not be used by the caller but only as
	 * return values from kernel. Return values should usually not contain the values
	 * {@link SEMANTIC_INDEX} or {@link PARALLEL_ITERATION}.
	 *
	 * @return all repeatable indices
	 */
	getIndices(): number[];

	/**
	 * Returns the USB of this field instance. If no USB was specified for the field,
	 * {@link USB_NOT_SPECIFIED} is returned.
	 *
	 * @return value of the USB or {@link USB_NOT_SPECIFIED}, if no USB was specified.
	 * 
	 * NOTE: DO NOT USE THIS METHOD ANY MORE - IT IS NOT IMPLEMENTED AND AN EXCEPTION IS THROWN!
	 */
	getUsb(): number;
}

// declaration merging
// TODO: Fix this the next time the file is edited.
// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace IIdentifier {
	/**
	 * SEPARATOR is used as a delimiter in path names.
	 */
	export const SEPARATOR = "/";

	/**
	 * {@link ALL_INDICES} is used to represent the asterisk operator for one repeatable level, an
	 * abbreviation for all indices. By using this constant, there is no need to explicitly specify
	 * all Identifiers individually. See e.g.
	 * {@link IValidator.validatePartlyWith3ValueLogic(IData, Set, IResult, ValidierungsModus, Log)}
	 * for the usage of {@link ALL_INDICES} for the specification of relevant fields.
	 */
	export const ALL_INDICES = 0;

	/**
	 * ITERATION is used to represent the iteration over the repeatable level.
	 * See {@link IMetaField.getValue IMetaField.getWert("OperandenMenge")} for an example of usage.
	 */
	export const ITERATION = -1;

	/**
	 * If no USB was specified, all USB specifications use the value USB_NOT_SPECIFIED.
	 * 
	 * NOTE: DO NOT USE THIS VALUE ANY MORE - IT IS NOT IMPLEMENTED IN THE RUNTIME ANY LONGER!
	 */
	export const USB_NOT_SPECIFIED = -2;

	/**
	 * SEMANTIC_INDEX is used to represent a level, that is specified by a semantic index.
	 */
	export const SEMANTIC_INDEX = -4;

	/**
	 * UNKNOWN is used, when for a semantic index or parallel iteration the iteration cannot be determined.
	 */
	export const UNKNOWN = -5;

	/**
	 * PARALLEL_ITERATION is used to represent a level that is repeated parallel in independent groups.
	 */
	export const PARALLEL_ITERATION = -6;

	/**
	 * REP_NOT_UNIQUE_REFERENCED_LEVEL_TO_BE_REPLACED is used to represent a level of the referenced
	 * fields in meta rules for the language construct RepetitionNotUniue (German name:
	 * WiederholungNichtEindeutig) that has to be replaced by all concrete erroneous repetitions. S.
	 * ticket A12K-1536
	 */
	export const REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED = -7;
}
