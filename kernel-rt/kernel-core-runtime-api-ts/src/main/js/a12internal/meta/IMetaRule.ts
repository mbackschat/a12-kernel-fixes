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
 * Interface to meta data of a rule.
 * <p>
 * All methods are thread-safe. All methods only provide read-only access.
 * </p>
 * <p>
 * Following information can be retrieved using
 * <code>{@link IMetaEntity.getValue}</code>:
 * <ul>
 * <li>{@link IMetaKeys.RULE_NAME}: short name of the rule as a string</li>
 * <li>{@link IMetaKeys.RULE_FULL_NAME}: full name of the rule as a string</li>
 * <li>{@link IMetaKeys.RULE_ERROR_CODE}: error code of the rule as a string</li>
 * <li>{@link IMetaKeys.RULE_ERROR_FIELD}: the error field of the rule as an
 * {@link IMetaField}</li>
 * <li>{@link IMetaKeys.RULE_TYPE}: error type as a string, i.e.
 * {@link IMetaKeys.RULE_TYPE_ERROR}, {@link IMetaKeys.RULE_TYPE_WARNING}, or
 * {@link IMetaKeys.RULE_TYPE_INFO}</li>
 * <li>{@link IMetaKeys.RULE_REF_ROOTGROUP_OMISSION_ERROR} Returns a set of
 * root group names referenced by the rule that can cause omission errors.
 * The set is empty if there are no such root groups.</li>
 * <li>{@link IMetaKeys.RULE_ANNOTATION}: with the parameter "key of annotation" of type string, it
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
export interface IMetaRule extends IMetaEntity { }
