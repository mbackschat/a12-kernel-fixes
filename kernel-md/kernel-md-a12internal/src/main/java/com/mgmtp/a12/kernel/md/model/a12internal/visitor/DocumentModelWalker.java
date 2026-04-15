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

import java.util.Collection;
import java.util.List;

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
 * Implements a depth-first traversal through the given {@link DocumentModel} or its respective
 * {@link Element}s.
 */
@OnlyForA12InternalUsage
public class DocumentModelWalker {

	/**
	 * Traverses a whole {@link DocumentModel}.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} to walk through.
	 * @param visitor
	 * 		the {@link DocumentModelVisitor} to call during traversal.
	 */
	public void acceptDocumentModel(final DocumentModel documentModel, final DocumentModelVisitor visitor) {
		if (!acceptHeader(documentModel.getHeader(), visitor)) {
			return;
		}
		if (!acceptFieldTypeDefinitions(documentModel.getContent().getTypeDefinitions(), visitor)) {
			return;
		}
		acceptElements(documentModel.getContent().getModelRoot().getElements(), visitor);
		visitor.leave();
	}

	private boolean acceptHeader(final Header header, final DocumentModelVisitor visitor) {
		return visitor.visitHeader(header) != VisitProcess.STOP;
	}

	/**
	 * @return {@code true} if the traverse process should continue, otherwise {@code false}.
	 */
	private boolean acceptFieldTypeDefinitions(final List<FieldTypeDefinition> fieldTypeDefinitions,
			final DocumentModelVisitor visitor) {
		for (final FieldTypeDefinition fieldTypeDefinition : fieldTypeDefinitions) {
			if (visitor.visitFieldTypeDefinition(fieldTypeDefinition) == VisitProcess.STOP) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Traverses a {@link List} of {@link Element}s.
	 *
	 * @param elements
	 * 		the {@link List} of {@link Element}s to walk through.
	 * @param visitor
	 * 		the {@link DocumentModelVisitor} to call during traversal.
	 */
	public void acceptElements(final Collection<Element> elements, final DocumentModelVisitor visitor) {
		acceptChildren(elements, visitor);
	}

	private boolean acceptElement(final Element element, final DocumentModelVisitor visitor) {
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

	private void enterElement(final Element element, final DocumentModelVisitor visitor) {
		visitor.enterElement(element);

		if (element instanceof Rule rule) {
			visitor.enterRule(rule);
		} else if (element instanceof Computation computation) {
			visitor.enterComputation(computation);
		} else if (element instanceof Field field) {
			visitor.enterField(field);
		} else if (element instanceof Group group) {
			visitor.enterGroup(group);
		}
	}

	private void leaveElement(final Element element, final DocumentModelVisitor visitor) {
		if (element instanceof Rule rule) {
			visitor.leaveRule(rule);
		} else if (element instanceof Computation computation) {
			visitor.leaveComputation(computation);
		} else if (element instanceof Field field) {
			visitor.leaveField(field);
		} else if (element instanceof Group group) {
			visitor.leaveGroup(group);
		}

		visitor.leaveElement(element);
	}

	private VisitProcess visitElementInheritance(final Element element, final DocumentModelVisitor visitor) {
		if (element instanceof Field field) {
			return visitor.visitField(field);

		} else if (element instanceof Group group) {
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

		} else if (element instanceof Rule rule) {
			return visitor.visitRule(rule);

		} else if (element instanceof Computation computation) {
			return visitor.visitComputation(computation);

		} else {
			throw new UnsupportedOperationException("Document model elements of type '"
					+ element.getClass().getSimpleName()
					+ "' are not supported by this walker implementation.");
		}
	}

	private boolean acceptChildren(final Collection<Element> elements, final DocumentModelVisitor visitor) {
		for (final Element child : elements) {
			if (!acceptElement(child, visitor)) {
				return false;
			}
		}
		return true;
	}
}
