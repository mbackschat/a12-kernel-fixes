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

package com.mgmtp.a12.kernel.md.model.internal.service.utils;

import static org.apache.commons.lang3.StringUtils.splitPreserveAllTokens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;

import lombok.NonNull;

public class DocumentModelUtils {

	public static final String ATTACHMENT_NAME = "attachment";

	private DocumentModelUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean containsIncludes(@NonNull DocumentModel model) {
		DocumentModelWalker walker = new DocumentModelWalker();
		ContainsIncludesVisitor visitor = new ContainsIncludesVisitor();
		walker.acceptDocumentModel(model, visitor);
		return visitor.containsInclude;
	}

	private static class ContainsIncludesVisitor extends DocumentModelVisitor {
		private boolean containsInclude = false;

		@Override
		public VisitProcess visitGroup(@NonNull Group group) {
			if (group.getIncludeDetails().isPresent()) {
				containsInclude = true;
				return VisitProcess.STOP;
			}
			return VisitProcess.CONTINUE_TRAVERSAL;
		}
	}

	/**
	 * In contrast to {@link com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService#getByPath(String)}, this method
	 * does a one-off lookup in the DocumentModel without creating an Index of all Elements in the model first. It should be more
	 * efficient for single lookups, but less efficient if many lookups will be done on the same unmodified DocumentModel.
	 */
	public static Optional<Element> getElementByPath(@NonNull DocumentModel dm, @NonNull List<String> pathParts) {
		if (pathParts.isEmpty()) {
			return Optional.empty();
		}
		Element current = dm.getContent().getModelRoot();
		for (String pathPart : pathParts) {
			if (!(current instanceof Group group)) {
				return Optional.empty();
			}
			Optional<Element> searchResult = group.getElements().stream()
					.filter(e -> e.getName().equals(pathPart))
					.findFirst();
			if (searchResult.isEmpty()) {
				return Optional.empty();
			}
			current = searchResult.get();
		}
		return Optional.of(current);
	}

	public static Optional<Element> getElementByPath(@NonNull DocumentModel dm, @NonNull String fullName) {
		if (StringUtils.isEmpty(fullName) || !fullName.startsWith("/")) {
			return Optional.empty();
		}
		return getElementByPath(dm, Arrays.stream(splitPreserveAllTokens(fullName, "/")).skip(1).toList());
	}

	public static @NonNull Element getElementByPathOrThrow(@NonNull DocumentModel dm, @NonNull String fullName) {
		return getElementByPath(dm, fullName).orElseThrow(
				() -> new NoSuchElementException("The document model does not contain element " + fullName));
	}

	public static @NonNull List<Computation> getAllComputations(@NonNull DocumentModel documentModel) {
		List<Computation> computations = new ArrayList<>();
		new DocumentModelWalker().acceptDocumentModel(documentModel, new DocumentModelVisitor() {
			@Override
			public VisitProcess visitComputation(final Computation computation) {
				computations.add(computation);
				return super.visitComputation(computation);
			}
		});
		return computations;
	}

	public static @NonNull List<Rule> getAllRules(@NonNull DocumentModel documentModel) {
		List<Rule> rules = new ArrayList<>();
		new DocumentModelWalker().acceptDocumentModel(documentModel, new DocumentModelVisitor() {
			@Override
			public VisitProcess visitRule(final Rule rule) {
				rules.add(rule);
				return super.visitRule(rule);
			}
		});
		return rules;
	}

	public static @NonNull List<Element> getAllElements(@NonNull DocumentModel documentModel) {
		List<Element> elements = new ArrayList<>();
		new DocumentModelWalker().acceptDocumentModel(documentModel, new DocumentModelVisitor() {
			@Override
			public VisitProcess visitElement(final Element element) {
				elements.add(element);
				return super.visitElement(element);
			}
		});
		return elements;
	}

	/**
	 * Try to resolve all references within the given document model.
	 * References that are not/no longer resolvable will be emptied out (e.g. missing error field in a rule)
	 *
	 * @param documentModel
	 * 		the document model within which the references are resolved
	 * @param problemReporter
	 * 		receives problems if references are unresolvable
	 * @return the list of groups that have include details
	 */
	public static @NonNull List<Group> resolveAndUpdateReferences(@NonNull DocumentModel documentModel,
			@NonNull IProblemReporter problemReporter) {
		DocumentModelWalker walker = new DocumentModelWalker();
		EntityCollectorVisitor collectorVisitor = new EntityCollectorVisitor();
		walker.acceptDocumentModel(documentModel, collectorVisitor);
		DocumentModelVisitor resolverVisitor = new EntityReferenceResolverVisitor(
				collectorVisitor.getEntitiesByFullName(),
				collectorVisitor.getFieldTypeDefinitionsById(),
				problemReporter);
		walker.acceptDocumentModel(documentModel, resolverVisitor);
		return collectorVisitor.getCollectedGroupsWithIncludeDetails();
	}
}
