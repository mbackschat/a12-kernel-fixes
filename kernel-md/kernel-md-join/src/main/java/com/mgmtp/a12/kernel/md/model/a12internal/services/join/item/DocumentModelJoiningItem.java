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
package com.mgmtp.a12.kernel.md.model.a12internal.services.join.item;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DocumentModelJoiningService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.JoiningType;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.Getter;
import lombok.NonNull;

/**
 * Element container, containing all information to join a {@link DocumentModel} via {@link DocumentModelJoiningService} with
 * respect to the given usage type.
 */
@Getter
@OnlyForUsage
// NOTE: this class mixes representation of decoration items with addition items. It may be better to represent them by separate
// classes. (But right now it would be too much disturbance for callers, which are directly coupled to the constructor, to be all
// updated)
public class DocumentModelJoiningItem extends JoiningItem {

	private final DocumentModel documentModel;
	private final ElementOrderForAddition elementOrderForAddition;

	/**
	 * like {@link DocumentModelJoiningItem(DocumentModel, JoiningType, ElementOrderForAddition)} but with implicit
	 * {@link DocumentModelJoiningItem.ElementOrderForAddition#ANCHOR_BASED_ORDERING}
	 */
	public DocumentModelJoiningItem(@NonNull DocumentModel documentModel, @NonNull JoiningType joiningType) {
		this(documentModel, joiningType, ElementOrderForAddition.ANCHOR_BASED_ORDERING);
	}

	public DocumentModelJoiningItem(@NonNull DocumentModel documentModel, @NonNull JoiningType joiningType,
			ElementOrderForAddition elementOrderForAddition) {
		super(joiningType);
		if (joiningType.isSelectionType()) {
			throw new RuntimeException("Invalid usage type " + joiningType);
		}
		this.documentModel = documentModel;
		this.elementOrderForAddition = elementOrderForAddition;
	}

	public enum ElementOrderForAddition {
		/**
		 * The recommended setting for all users except some specific callers in ExtMP.
		 * The position of added groups, fields, rules and computations relative to their sibling elements from
		 * the base model can be controlled by the modeler with anchor elements (elements that occur in the added model but also in
		 * the base).
		 */
		ANCHOR_BASED_ORDERING,
		/**
		 * Legacy compatibility mode, only for callers in ExtMP that work with PacketV1 additive model definitions.
		 */
		LEGACY_EXTMP_ORDERING
	}
}
