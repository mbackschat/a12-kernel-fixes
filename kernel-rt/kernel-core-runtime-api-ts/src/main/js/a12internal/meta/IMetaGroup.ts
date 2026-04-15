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

import type {IMetaEntity} from "./IMetaEntity.js";

/**
 * Interface to the meta data of a group (i.e. root group or group).
 * <p>
 * All methods are thread-safe. All methods only provide read-only access.
 * </p>
 *
 * Following information can be retrieved using {@link IMetaEntity.getValue}
 * with the keys listed below.
 * No key requires additional parameters.
 * <ul>
 * <li>{@link IMetaKeys.GROUP_SHORT_NAME}: short name of the group as a string
 * </li>
 * <li>{@link IMetaKeys.GROUP_FULLNAME}: full name of the group as a
 * string</li>
 * <li>{@link IMetaKeys.GROUP_SUBGROUP_LIST}: returns a
 * <code>List&lt;{@link IMetaGroup}&gt;</code>, which are <i>directly</i> included in this group.
 * If there are no subgroups an empty list is returned. The ordering of the result lists corresponds
 * to the ordering of the groups passed to the code generator.</li>
 * <li>{@link IMetaKeys.GROUP_RULE_LIST}: returns the
 * <code>List&lt;{@link IMetaRule}&gt;</code>, which are <i>directly</i> included in this group.
 * If there are no such rules, an empty list is returned.
 * The ordering of the result lists corresponds to the ordering of the rules passed to the code generator</li>
 * <li>{@link IMetaKeys.GROUP_FIELD_LIST}: Returns a
 * <code>List&lt;{@link IMetaField}&gt;</code>, which are <i>directly</i> included within this
 * group. If there are no such fields an empty list is returned.</li>
 * <li>{@link IMetaKeys.GROUP_MAX_INDEX}: Returns the maximum repetition of this group
 * as a Long</li>
 * <li>{@link IMetaKeys.GROUP_INDEX_FIELDS}: Returns the list of index fields for the
 * group. These fields are used for the semantic index and parallel iteration. If the group doesn't
 * contain index fields, an empty list will be returned.</li>
 * <li>{@link IMetaKeys.GROUP_ANNOTATION}: with the parameter "key of annotation" of type string, it
 * returns:
 * <ul>
 *     <li><code>null</code>, if there is no annotation with the given key.</li>
 *     <li>Empty string, if the annotation key is present but no value is associated with it.</li>
 *     <li>Otherwise, the value associated with the given key.</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 */
// eslint-disable-next-line @typescript-eslint/no-empty-object-type
export interface IMetaGroup extends IMetaEntity { }
