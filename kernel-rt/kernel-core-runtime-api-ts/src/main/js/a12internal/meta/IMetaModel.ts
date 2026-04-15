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
 * Interface to meta data of a model type.
 * <p>
 * All methods are thread-safe. All methods only provide read-only access.
 * </p>
 * <p>
 * Following information can be retrieved using
 * <code>{@link IMetaEntity.getValue}</code>
 * <ul>
 * <li>{@link IMetaKeys.MODEL_META_FIELD}
 * <br>
 * The field name must be passed as an extra parameter.
 * Returns a {@link IMetaField}, if a field with the given name exists, otherwise <code>null</code>
 * is returned. If the short name of the field is unique within the model, this short name or the
 * full field name can be used to identify the field, otherwise just the full field name may be
 * used.</li>
 * <li>{@link IMetaKeys.MODEL_MANDATORY_FIELDS}
 * <br>
 * A <code>Set&lt;{@link IIdentifier}&gt;</code> has to be passed as an additional parameter.
 * Returns a <code>Set&lt;{@link IIdentifier}&gt;</code> which are mandatory relative to the fields
 * passed as parameter. If faulty parameters are passed, <code>null</code> is returned. If there are
 * no mandatory fields found, then an empty set is returned. For performance reasons the returned
 * set is not copied and so may not be changed by the caller.</li>
 * <li>{@link IMetaKeys.MODEL_GROUP_LIST}
 * <br>
 * Returns the set of groups directly contained in the root group as a <code>List&lt;
 * {@link IMetaGroup}&gt;</code>.
 * The ordering in the List corresponds to the ordering passed when generating code.</li>
 * <li>{@link IMetaKeys.MODEL_APPLICATION_CONDITION}
 * <br>
 * Returns all names of the applications conditions used in the model type as a Set of strings
 * which are used in the model type. If no application conditions are used in the model type, an empty set is returned.
 * The order in which the conditions are returned has no meaning.</li>
 * <li>{@link IMetaKeys.MODEL_META_RULE}
 * <br>
 * As an additional parameter the full name of a rule is expected. Returns an {@link IMetaRule}
 * object or <code>null</code>, if no rule with the given name exists.<br>
 * Note: If the generate was generated for an application without UI support, it always
 * returns <code>null</code>.</li>
 * <li>{@link IMetaKeys.MODEL_META_GROUP}
 * <br>
 * As an additional parameter the full name of a group is expected. Returns {@link IMetaGroup}
 * object or <code>null</code>, if no group with the given name exists.
 * </li>
 * <li>{@link IMetaKeys.MODEL_LANGUAGES}
 * <br>
 * Returns a list, containing all the languages supported by this generate.
 * </p>
 * </li>
 * <li>{@link IMetaKeys.MODEL_CALC_FIELDS}
 * <p>
 * Returns the set of fields as a Set of {@link IMetaField}, which are the calculated fields
 * in a calculation. If there are no calculated fields, the Set is empty.
 * </li>
 * <li>{@link IMetaKeys.MODEL_INDEX_FIELDS}
 * <p>
 * Returns <code>true</code> if there are index fields defined in the model, otherwise
 * <code>false</code>.
 * </li>
 * <li>{@link IMetaKeys#MODEL_INDEX_FIELD_FULL_NAMES}
 * <p>
 * Returns a list with the full names of all index fields in the model. If there are no index
 * fields, an empty list is returned.
 * </p>
 * </li>
 * <li>{@link IMetaKeys.MODEL_DYNAMIC_ENUM_FIELDS}
 * <p>
 * Returns the set of fields as a Set of {@link IMetaField}, which are defined as dynamic
 * enumeration. If there are no such fields, the Set is empty.
 *
 * HINT: For document models, dynamic enumerations are not generated, yet. Therefore for code generated
 * from document models, this key always returns an empty <code>Set</code>
 * </li>
 * <li>{@link IMetaKeys.MODEL_ANNOTATION}: with the parameter "key of annotation" of type string, it
 * returns:
 * <ul>
 *     <li><code>null</code>, if there is no annotation with the given key.</li>
 *     <li>Empty string, if the annotation key is present but no value is associated with it.</li>
 *     <li>Otherwise, the value associated with the given key.</li>
 * </ul>
 * </li>
 * <li>{@link IMetaKeys.MODEL_ALL_ANNOTATED_ENTITIES}: with the parameter "key of
 * annotation" of type string, it returns
 * <ul>
 *     <li><code>null</code>, in the absence of any annotation with the given key.</li>
 *     <li>Otherwise, a map of entries of the form full entity name as the key, and empty string, or the annotation value as the
 *     value. If the model itself has the annotation, the key in the resulting map will be an empty string.</li>
 * </ul>
 * </li>
 * </ul>
 */
// eslint-disable-next-line @typescript-eslint/no-empty-object-type
export interface IMetaModel extends IMetaEntity { }
