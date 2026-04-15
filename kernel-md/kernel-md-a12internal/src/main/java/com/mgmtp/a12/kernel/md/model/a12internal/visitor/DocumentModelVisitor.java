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
package com.mgmtp.a12.kernel.md.model.a12internal.visitor;

import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;
import com.mgmtp.a12.model.header.Header;

/**
 * This class defines the standard behavior of the visitor pattern for a {@link DocumentModel} in a
 * <b>read and update manner</b>. This means, the developer may change the visited
 * {@link DocumentModel}.
 * <p>
 * This class should be extended and used with overridden methods to implement the desired
 * functionality.
 */
@OnlyForA12InternalUsage
public class DocumentModelVisitor {

	/**
	 * Visit a {@link Header}
	 */
	public VisitProcess visitHeader(final Header header) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	/**
	 * Visit a {@link FieldTypeDefinition}
	 */
	public VisitProcess visitFieldTypeDefinition(final FieldTypeDefinition fieldTypeDefinition) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	/**
	 * Visit any {@link Element}.
	 */
	public VisitProcess visitElement(final Element element) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	/**
	 * Visit a {@link Rule}
	 */
	public VisitProcess visitRule(final Rule rule) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	/**
	 * Visit a {@link Computation}
	 */
	public VisitProcess visitComputation(final Computation computation) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	/**
	 * Visit a {@link Field}
	 */
	public VisitProcess visitField(final Field field) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	/**
	 * Visit an {@link Group}
	 */
	public VisitProcess visitGroup(final Group group) {
		return VisitProcess.CONTINUE_TRAVERSAL;
	}

	/**
	 * The given {@link Element} is about to be visited. Can be used for stack implementations.
	 * Note: Is called before the element specific methods below.
	 */
	public void enterElement(final Element element) {
		// Do nothing as default.
	}

	/**
	 * The given {@link Rule} is about to be visited. Note: Is called after {@link #enterElement}.
	 */
	public void enterRule(final Rule rule) {
		// Do nothing as default.
	}

	/**
	 * The given {@link Computation} is about to be visited. Note: Is called after {@link #enterElement}.
	 */
	public void enterComputation(final Computation computation) {
		// Do nothing as default.
	}

	/**
	 * The given {@link Field} is about to be visited. Note: Is called after {@link #enterElement}.
	 */
	public void enterField(final Field field) {
		// Do nothing as default.
	}

	/**
	 * The given {@link Group} is about to be visited. Note: Is called after {@link #enterElement}.
	 */
	public void enterGroup(final Group group) {
		// Do nothing as default.
	}

	/**
	 * The given {@link Element} has been visited and is now left. Can be used for stack implementations.
	 * Note: Is called after the element specific methods below.
	 */
	public void leaveElement(final Element element) {
		// Do nothing as default.
	}

	/**
	 * The given {@link Rule} has been visited and is now left. Note: Is called before {@link #leaveElement}.
	 */
	public void leaveRule(final Rule rule) {
		// Do nothing as default.
	}

	/**
	 * The given {@link Computation} has been visited and is now left. Note: Is called before {@link #leaveElement}.
	 */
	public void leaveComputation(final Computation computation) {
		// Do nothing as default.
	}

	/**
	 * The given {@link Field} has been visited and is now left. Note: Is called before {@link #leaveElement}.
	 */
	public void leaveField(final Field field) {
		// Do nothing as default.
	}

	/**
	 * The given {@link Group} has been visited and is now left. Note: Is called before {@link #leaveElement}.
	 */
	public void leaveGroup(final Group group) {
		// Do nothing as default.
	}

	/**
	 * The document model has been visited.
	 */
	public void leave() {
		// Do nothing as default.
	}
}
