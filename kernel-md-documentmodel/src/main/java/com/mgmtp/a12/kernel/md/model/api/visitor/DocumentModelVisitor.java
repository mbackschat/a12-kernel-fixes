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
package com.mgmtp.a12.kernel.md.model.api.visitor;

import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IFieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;

import lombok.NonNull;

/**
 * This class defines the standard behavior of the visitor pattern for a {@link IDocumentModel} in a
 * <b>read-only manner</b>. This means, the visited {@link IDocumentModel} instance <b>cannot</b> be
 * changed.
 * <p>
 * This class should be extended and used with overridden methods to implement the desired
 * functionality.
 */
public class DocumentModelVisitor {

	/**
	 * Visit an {@link IFieldTypeDefinition}
	 */
	public VisitProcess visitFieldTypeDefinition(final @NonNull IFieldTypeDefinition fieldTypeDefinition) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	/**
	 * Visit any {@link IElement}
	 */
	public VisitProcess visitElement(final @NonNull IElement element) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	/**
	 * Visit an {@link IRule}
	 */
	public VisitProcess visitRule(final @NonNull IRule rule) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	/**
	 * Visit an {@link IComputation}
	 */
	public VisitProcess visitComputation(final @NonNull IComputation computation) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	/**
	 * Visit an {@link IField}
	 */
	public VisitProcess visitField(final @NonNull IField field) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	/**
	 * Visit an {@link IGroup}
	 */
	public VisitProcess visitGroup(final @NonNull IGroup group) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	/**
	 * The given {@link IElement} is about to be visited. Can be used for stack implementations.
	 * Note: Is called before the element specific methods below.
	 */
	public void enterElement(final @NonNull IElement element) {
		// Do nothing as default.
	}

	/**
	 * The given {@link IRule} is about to be visited. Note: Is called after {@link #enterElement}.
	 */
	public void enterRule(final @NonNull IRule rule) {
		// Do nothing as default.
	}

	/**
	 * The given {@link IComputation} is about to be visited. Note: Is called after {@link #enterElement}.
	 */
	public void enterComputation(final @NonNull IComputation computation) {
		// Do nothing as default.
	}

	/**
	 * The given {@link IField} is about to be visited. Note: Is called after {@link #enterElement}.
	 */
	public void enterField(final @NonNull IField field) {
		// Do nothing as default.
	}

	/**
	 * The given {@link IGroup} is about to be visited. Note: Is called after {@link #enterElement}.
	 */
	public void enterGroup(final @NonNull IGroup group) {
		// Do nothing as default.
	}

	/**
	 * The given {@link IElement} has been visited and is now left. Can be used for stack implementations.
	 * Note: Is called after the element specific methods below.
	 */
	public void leaveElement(final @NonNull IElement element) {
		// Do nothing as default.
	}

	/**
	 * The given {@link IRule} has been visited and is now left. Note: Is called before {@link #leaveElement}.
	 */
	public void leaveRule(final @NonNull IRule rule) {
		// Do nothing as default.
	}

	/**
	 * The given {@link IComputation} has been visited and is now left. Note: Is called before {@link #leaveElement}.
	 */
	public void leaveComputation(final @NonNull IComputation computation) {
		// Do nothing as default.
	}

	/**
	 * The given {@link IField} has been visited and is now left. Note: Is called before {@link #leaveElement}.
	 */
	public void leaveField(final @NonNull IField field) {
		// Do nothing as default.
	}

	/**
	 * The given {@link IGroup} has been visited and is now left. Note: Is called before {@link #leaveElement}.
	 */
	public void leaveGroup(final @NonNull IGroup group) {
		// Do nothing as default.
	}
}
