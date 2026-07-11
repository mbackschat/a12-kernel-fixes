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
package com.mgmtp.a12.kernel.md.model.internal.services.join.error;

/**
 * ErrorCodes meant to be used when generating external error messages.
 */
public enum ErrorCode {

	ErrorMessageFragmentStart("Dm.join.error.message.fragment.start"),
	ErrorMessageFragmentStartGroup("Dm.join.error.message.fragment.start.group"),
	ErrorMessageFragmentStartField("Dm.join.error.message.fragment.start.field"),
	ErrorMessageFragmentStartRule("Dm.join.error.message.fragment.start.rule"),
	ErrorMessageFragmentStartComputation("Dm.join.error.message.fragment.start.computation"),
	ErrorMessageFragmentStartHeader("Dm.join.error.message.fragment.start.header"),
	ErrorMessageFragmentMiddleGroup("Dm.join.error.message.fragment.middle.group"),
	ErrorMessageFragmentMiddleField("Dm.join.error.message.fragment.middle.field"),
	ErrorMessageFragmentMiddleRule("Dm.join.error.message.fragment.middle.rule"),
	ErrorMessageFragmentMiddleComputation("Dm.join.error.message.fragment.middle.computation"),
	ErrorMessageFragmentEntity("Dm.join.error.message.fragment.entity"),
	ErrorMessageFragmentEntityGroup("Dm.join.error.message.fragment.entity.group"),
	ErrorMessageFragmentEntityField("Dm.join.error.message.fragment.entity.field"),
	ErrorMessageFragmentEntityRule("Dm.join.error.message.fragment.entity.rule"),
	ErrorMessageFragmentEntityComputation("Dm.join.error.message.fragment.entity.computation"),
	/**
	 * Params: errMsgFragment1, elementFullName, DmToJoinDetails, errMsgFragment2
	 */
	NameCollision("Dm.join.NameCollision"),
	/**
	 * Params: fieldToBeDecoratedFullName, decorationModelDetails, fullName
	 */
	DuplicatePath("Dm.join.DuplicatePath"),
	/**
	 * Params: errMsgFragmentStart, elementFullName, DmToJoinDetails
	 */
	InconsistentAnnotations("Dm.join.InconsistentAnnotations"),
	/**
	 * Params: decorationModelDetails, entity, anchor name
	 */
	AnchorMissing("Dm.join.decoration.model.AnchorMissing"),
	/**
	 * Params: decorationModelDetails
	 */
	InvalidRootGroups("Dm.join.decoration.model.InvalidRootGroups"),
	/**
	 * Params: elementTypeFragment, elementName, decorationModelDetails
	 */
	InvalidName("Dm.join.decoration.model.InvalidName"),
	/**
	 * Params: decorationModelDetails
	 */
	InvalidSubstructure("Dm.join.decoration.model.InvalidSubstructure"),
	/**
	 * Params: decorationModelDetails
	 */
	NonDefaultData("Dm.join.decoration.model.NonDefaultData"),
	/**
	 * Params: decorationModelDetails
	 */
	InvalidDecorationElementsOnRootLayer("Dm.join.decoration.model.InvalidDecorationElementsOnRootLayer"),
	WrongModelVersion("Dm.join.wrong.model.version"),
	/**
	 * Params: errorMessage
	 */
	InvalidSelection("Dm.join.selection.invalid"),
	/**
	 * Params: path, selectionModelId
	 */
	SelectionPathNotFound("Dm.join.selection.path.not.found");

	private final String msgKey;

	ErrorCode(String msgKey) {
		this.msgKey = msgKey;
	}

	public String getMessageKey() {
		return msgKey;
	}
}
