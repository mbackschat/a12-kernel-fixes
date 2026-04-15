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

import java.util.Collection;
import java.util.List;

import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IFieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.IRule;

import lombok.NonNull;

/**
 * Implements a depth-first traversal through the given {@link IDocumentModel} or its respective
 * {@link IElement}s.
 */
public class DocumentModelWalker {

	/**
	 * Return type of the visit methods of the {@link DocumentModelVisitor}.<br>
	 * Allows controlling the traversal process of the {@link DocumentModelVisitor} implementation.
	 */
	public enum VisitProcess {

		/**
		 * Continue with the traversal - visit all elements, as long as no other
		 * {@link VisitProcess} is returned in a visit method of {@link DocumentModelVisitor}.
		 */
		CONTINUE_TRAVERSAL,

		/**
		 * Continue with the traversal, but don't go deeper:
		 * <ul>
		 * <li>if returned in an implementation of
		 * {@link DocumentModelVisitor#visitElement(IElement)}: don't go through the inheritance
		 * specific visit method for the element ({@link DocumentModelVisitor#visitGroup(IGroup)},
		 * {@link DocumentModelVisitor#visitField(IField)},
		 * {@link DocumentModelVisitor#visitRule(IRule)} or
		 * {@link DocumentModelVisitor#visitComputation(IComputation)}).</li>
		 * <li>if returned for a group in an implementation of
		 * {@link DocumentModelVisitor#visitElement(IElement)} or
		 * {@link DocumentModelVisitor#visitGroup(IGroup)}: don't visit the elements contained in
		 * the group (subgroups, fields, rules and computations).</li>
		 * </ul>
		 */
		CONTINUE_BUT_DONT_GO_DEEPER,

		/**
		 * Stop the whole traversal process - no other elements are visited.
		 */
		STOP
	}

	/**
	 * Traverse the given {@link IDocumentModel}.
	 *
	 * @param documentModel
	 * 		the {@link IDocumentModel} to be traversed.
	 * @param visitor
	 * 		the {@link DocumentModelVisitor} that will be used during traversal.
	 */
	public void acceptDocumentModel(final @NonNull IDocumentModel documentModel, final @NonNull DocumentModelVisitor visitor) {
		if (!acceptFieldTypeDefinitions(documentModel.getContent().getTypeDefinitions(), visitor)) {
			return;
		}
		acceptElements(documentModel.getContent().getDocumentModelRoot().getElements(), visitor);
	}

	/**
	 * @return {@code true} if the traverse process should continue, otherwise {@code false}.
	 */
	private boolean acceptFieldTypeDefinitions(final @NonNull List<IFieldTypeDefinition> fieldTypeDefinitions,
			final @NonNull DocumentModelVisitor visitor) {
		for (final IFieldTypeDefinition fieldTypeDefinition : fieldTypeDefinitions) {
			if (visitor.visitFieldTypeDefinition(fieldTypeDefinition) == VisitProcess.STOP) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Traverse the given {@link IElement}s.
	 *
	 * @param elements
	 * 		the {@link IElement}s to be traversed.
	 * @param visitor
	 * 		the {@link DocumentModelVisitor} that will be used during traversal.
	 */
	public void acceptElements(final @NonNull Collection<IElement> elements, final @NonNull DocumentModelVisitor visitor) {
		acceptChildren(elements, visitor);
	}

	private boolean acceptElement(final @NonNull IElement element, final @NonNull DocumentModelVisitor visitor) {
		enterElement(element, visitor);

		final VisitProcess process;

		final VisitProcess elementVisitProcess = visitor.visitElement(element);
		if (elementVisitProcess == VisitProcess.CONTINUE_TRAVERSAL) {
			process = visitElementInheritance(element, visitor);
		} else {
			process = elementVisitProcess;
		}

		leaveElement(element, visitor);
		return process != VisitProcess.STOP;
	}

	private void enterElement(final @NonNull IElement element, final @NonNull DocumentModelVisitor visitor) {
		visitor.enter(element);
		visitor.enterElement(element);

		if (element instanceof IRule rule) {
			visitor.enterRule(rule);
		} else if (element instanceof IComputation computation) {
			visitor.enterComputation(computation);
		} else if (element instanceof IField field) {
			visitor.enterField(field);
		} else if (element instanceof IGroup group) {
			visitor.enterGroup(group);
		}
	}

	private void leaveElement(final @NonNull IElement element, final @NonNull DocumentModelVisitor visitor) {
		if (element instanceof IRule rule) {
			visitor.leaveRule(rule);
		} else if (element instanceof IComputation computation) {
			visitor.leaveComputation(computation);
		} else if (element instanceof IField field) {
			visitor.leaveField(field);
		} else if (element instanceof IGroup group) {
			visitor.leaveGroup(group);
		}

		visitor.leave(element);
		visitor.leaveElement(element);
	}

	private VisitProcess visitElementInheritance(final @NonNull IElement element, final @NonNull DocumentModelVisitor visitor) {
		if (element instanceof IField field) {
			return visitor.visitField(field);

		} else if (element instanceof IGroup group) {
			final VisitProcess localProcess = visitor.visitGroup(group);
			if (localProcess == VisitProcess.CONTINUE_TRAVERSAL) {
				if (!acceptChildren(group.getElements(), visitor)) {
					return VisitProcess.STOP;
				} else {
					return localProcess;
				}
			} else {
				return localProcess;
			}

		} else if (element instanceof IRule rule) {
			return visitor.visitRule(rule);

		} else if (element instanceof IComputation computation) {
			return visitor.visitComputation(computation);

		} else {
			throw new UnsupportedOperationException("Document model elements of type '"
					+ element.getClass().getSimpleName()
					+ "' are not supported by this walker implementation.");
		}
	}

	private boolean acceptChildren(@NonNull final Collection<IElement> elements, @NonNull final DocumentModelVisitor visitor) {
		for (final IElement child : elements) {
			if (!acceptElement(child, visitor)) {
				return false;
			}
		}
		return true;
	}
}
