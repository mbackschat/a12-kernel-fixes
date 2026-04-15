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
package com.mgmtp.a12.kernel.md.model.a12internal.services.join;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.DocumentModelJoiningItem;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.SelectionJoiningItem;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Supported join types.
 * Depending on its join type, the entities from the {@link DocumentModelJoiningItem}
 * are added differently to the root {@link DocumentModel} or from {@link SelectionJoiningItem}
 * are removed from the root.
 * For all join types, the following applies:
 * <ul>
 * <li>All {@link FieldTypeDefinition}s are added/removed.</li>
 * <li>Information on the additional model is added: name, branch (optional), revision
 * (optional), join type.</li>
 * <li>The attachments root group is transferred unchanged to the result model.</li>
 * </ul>
 */
@AllArgsConstructor
@Getter
public enum JoiningType {

	/**
	 * ADDITIVE_MODEL - elements from the joined {@link DocumentModel} are added to the root
	 * {@link DocumentModel}:
	 * <ul>
	 * <li>{@link Group}s (only if not already contained in the root model)</li>
	 * <li>{@link Field}s (only if not already contained in the root model)</li>
	 * <li>{@link Rule}s (always - Rules already contained in the root model are replaced)</li>
	 * <li>{@link Computation}s (always - Computations already contained in the root model are
	 * replaced)</li>
	 * </ul>
	 * Notes for usage:
	 * <ul>
	 * <li>The annotations of the header are merged.</li>
	 * <li>If the added element does not exist in the root model, the element is added at the last position in the parent context.
	 * Only rules are added at the top.</li>
	 * <li>If the added element already exists (with same fullname), the elements' annotations are merged. Only for rules the
	 * existing rule is overwritten.</li>
	 * <li>If the root model and the additive model specify the same field with fieldtype
	 * enumeration, the enumeration values are merged. For equal xml values the display values
	 * are used from the additive model.</li>
	 * <li>The added elements get a new id of the format {@code <hash of DocumentModelId of additive model>_<OriginalId>}.</li>
	 * </ul>
	 */
	ADDITIVE_MODEL("+Add"),
	/**
	 * DECORATION_MODEL_FIELD - elements from the {@link DocumentModel} are added to the root
	 * {@link DocumentModel} for each of its {@link Field}s:
	 * <ul>
	 * <li>{@link Group}s (all except for the root group)</li>
	 * <li>{@link Field}s (all except for the anchor field)</li>
	 * <li>{@link Rule}s (all)</li>
	 * <li>{@link Computation}s (all)</li>
	 * </ul>
	 * A {@link DocumentModel} with this join type must meet the following properties:
	 * <ul>
	 * <li>It contains only one non-repeatable root group.</li>
	 * <li>The root group contains a field named "Field", the anchor field. The anchor field
	 * defines the positions around the field to be decorated, where the additional elements are
	 * added. Rules or computations that reference the anchor field in the decoration model will
	 * reference the field to be decorated in the result model.</li>
	 * <li>The names of all elements that are directly contained in the root group must contain the string
	 * "Field". In the result model the first occurrence of "Field"
	 * is replaced by the name of the field to be decorated.</li>
	 * </ul>
	 * Notes for usage:
	 * <ul>
 	 * <li>The annotations of the decorated field and the anchor of the decoration model are merged.</li>
	 * <li>Variant filters are NOT merged, i.e. if the field to be decorated specifies variant filters,
	 * these are not adopted for the elements of the decoration model.</li>
	 * <li>The decorated elements (except the anchor) get a new id of the format {@code <DecoratedElementId>_<OriginalId>}.</li>
	 * <li>If the decoration model contains a computation there is a special case: if the attribute toleranceRangeOp is set and
	 * roundingMode is 'exact' the operation will be modified to {@code 'RoundDown(<oldOperation>)'}.</li>
	 * <li>In the decoration model elements should NOT be referenced using the absolute path. This may result in invalid
	 * rules/computations</li>
	 * <li>In variant error messages, references of entities whose name contain the anchor name are NOT adapted to the name
	 * of the decorated element, this may result in invalid rules. Note: variant error messages are currently not supported
	 * in A12.</li>
	 * <li>Fields in the decoration model of fieldtype Unspecified get the fieldtype of the decorated field.</li>
	 * <li>Fields in attachments are not decorated.</li>
	 * </ul>
	 */
	DECORATION_MODEL_FIELD("+DecoF"),
	/**
	 * DECORATION_MODEL_GROUP - elements from the {@link DocumentModel} are added to the root
	 * {@link DocumentModel} for each of its {@link Group}s:
	 * <ul>
	 * <li>{@link Group}s (all except for the root group and the anchor group)</li>
	 * <li>{@link Field}s (all)</li>
	 * <li>{@link Rule}s (all)</li>
	 * <li>{@link Computation}s (all)</li>
	 * </ul>
	 * A {@link DocumentModel} with this join type must meet the following properties:
	 * <ul>
	 * <li>It contains only one non-repeatable root group.</li>
	 * <li>The root group contains a group named "Group", the anchor group. The anchor group
	 * defines the positions around the group to be decorated, where the additional elements are
	 * added. Rules or computations that reference the anchor group in the decoration model will
	 * reference the group to be decorated in the result model.</li>
	 * <li>The names of all elements that are directly contained in the root group must contain the string
	 * "Group". In the result model the first occurrence of "Group"
	 * is replaced by the name of the group to be decorated.</li>
	 * <li>The anchor group must not contain any substructure.</li>
	 * </ul>
	 * Notes for usage:
	 * <ul>
	 * <li>The annotations of the decorated group and the anchor of the decoration model are merged.</li>
	 * <li>The decorated elements (except the anchor) get a new id of the format {@code <DecoratedElementId>_<OriginalId>}.</li>
	 * <li>If the decoration model contains a computation there is a special case: if the attribute toleranceRangeOp is set and
	 * roundingMode is 'exact' the operation will be modified to {@code 'RoundDown(<oldOperation>)'}.</li>
	 * <li>In the decoration model elements should NOT be referenced using the absolute path. This may result in invalid
	 * rules/computations</li>
	 * <li>In variant error messages, references of entities whose name contain the anchor name are NOT adapted to the name
	 * of the decorated element, this may result in invalid rules. Note: variant error messages are currently not supported
	 * in A12.</li>
	 * <li>If no selection for decoration is used for the decoration of groups, note that even root groups are decorated.
	 * If that is the case, no elements other than groups may be on the root group layer of the decoration model.</li>
	 * </ul>
	 */
	DECORATION_MODEL_GROUP("+DecoG"),
	/**
	 * SELECTION - elements unselected via the {@link DocumentV2} are removed from the root
	 * {@link DocumentModel}. The {@link DocumentV2} must comply the SelectionModel (Version: V1) of the metamodel.
	 * In the SelectionModel you can specify the default value, if every element is selected or unselected by default.
	 * Additional to that it is possible to specify paths or fullnames that should be explicitly selected or unselected.
	 * To identify if an element is selected or not (especially if it is defined by more than one path/fullname), there is
	 * a matching priority. The following priorities are listed from the most matching to the less matching ones:
	 * <ol>
	 * <li>/A/K1/K2/K3/F21 - complete fullname of the element</li>
	 * <li>/{@literal *}/F21 - complete element name, path as wildcard</li>
	 * <li>/A/K1/K2/K3/*21 - exact path, element with wildcard as prefix</li>
	 * <li>/{@literal *}/*1 - path as wildcard, element with wildcard as prefix</li>
	 * <li>/A/K1/K2/K3/F* - exact path, element with wildcard as postfix</li>
	 * <li>/{@literal *}/F* - path as wildcard, element with wildcard as postfix</li>
	 * <li>/A/K1/K2/K3/ - path exact, no element name</li>
	 * <li>/A/K1/K2/ - part of path, no element name</li>
	 * </ol>
	 * Notes for usage:
	 * <ul>
	 * <li>The wildcard operator is not possible for sub paths like: /A/{@literal *}/K2/</li>
	 * <li>For element names the wildcard operator is only possible as prefix or postfix but not in the middle of the
	 * element name like: /A/K1/K2/F*2</li>
	 * <li>Is a field removed, all references to those entities are removed as well. This includes
	 * index/sort field references, error field references.</li>
	 * <li>If a group gets empty due to a selection (contains no child elements), the group will be removed as well.</li>
	 * </ul>
	 */
	SELECTION("+Sel"),
	/**
	 * SELECTION_DECORATION - this can be used before {@link JoiningType#DECORATION_MODEL_FIELD}
	 * or {@link JoiningType#DECORATION_MODEL_GROUP}. Only the selected elements via the {@link DocumentV2}
	 * are used for the following decoration.
	 * </br>
	 * The {@link DocumentV2} must comply the SelectionModel (Version: V1) of the metamodel.
	 * </br>
	 * Notes for usage:
	 * <ul>
	 * <li>For further information regarding the matching priority see {@link #SELECTION}.</li>
	 * <li>The selection for decoration can be applied for decoration for fields and for decoration for groups. If the
	 * following joiningItem is a decoration for groups the element names are interpreted as group names.</li>
	 * </ul>
	 */
	SELECTION_DECORATION("+SelDeco");

	private final String modelAddition;

	public boolean isSelectionType() {
		return this == SELECTION || this == SELECTION_DECORATION;
	}
}
