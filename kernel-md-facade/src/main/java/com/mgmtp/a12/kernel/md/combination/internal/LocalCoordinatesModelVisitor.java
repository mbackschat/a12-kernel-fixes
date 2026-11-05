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

package com.mgmtp.a12.kernel.md.combination.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentUniquenessCriterion;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdge;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionEdgeLabel;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.ExpansionNode;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph.LocalCoordinates;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.kernel.md.model.internal.expansioninfo.ExpansionInfoUtils;
import com.mgmtp.a12.model.header.Header;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Visitor to collect {@link LocalCoordinates} (of elements, annotations, modelReferences etc) for use in
 * {@link com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.ExpansionInfo}.
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class LocalCoordinatesModelVisitor extends DocumentModelVisitor {
	@Getter
	private final HashSet<LocalCoordinates> localCoordinates = new HashSet<>();

	public static Set<LocalCoordinates> getLocalCoordinates(DocumentModel documentModel) {
		LocalCoordinatesModelVisitor localCoordinatesModelVisitor = new LocalCoordinatesModelVisitor();
		new DocumentModelWalker().acceptDocumentModel(documentModel, localCoordinatesModelVisitor);
		return localCoordinatesModelVisitor.getLocalCoordinates();
	}

	public static Set<LocalCoordinates> getLocalCoordinates(List<com.mgmtp.a12.kernel.md.model.a12internal.Element> elements) {
		LocalCoordinatesModelVisitor localCoordinatesModelVisitor = new LocalCoordinatesModelVisitor();
		new DocumentModelWalker().acceptElements(elements, localCoordinatesModelVisitor);
		return localCoordinatesModelVisitor.getLocalCoordinates();
	}

	public static Map<LocalCoordinates, ExpansionNode> getAllAsIncludeBase(DocumentModel documentModel) {
		String modelId = documentModel.getHeader().getId();
		return getLocalCoordinates(documentModel).stream()
				.collect(Collectors.toMap(Function.identity(),
						lc -> ExpansionNode.of(lc,
								modelId,
								ExpansionEdge.of(ExpansionEdgeLabel.Dm.Base.of(),
										ExpansionNode.of(lc, modelId)
								))));
	}

	public static Map<LocalCoordinates, ExpansionNode> getAllAsLocal(DocumentModel documentModel) {
		String modelId = documentModel.getHeader().getId();
		return getLocalCoordinates(documentModel).stream()
				.collect(Collectors.toMap(Function.identity(), lc -> ExpansionNode.of(lc, modelId)));
	}

	@Override public VisitProcess visitHeader(Header header) {
		// collect the model detail itself (reference to the model), used for getting the whole input for a result model
		localCoordinates.add(LocalCoordinates.ModelItself.of());

		localCoordinates.addAll(ExpansionInfoUtils.unmanagedModelReferences(header.getModelReferences()));

		// all annotations from the header are "model annotations"
		if (header.getAnnotations() != null) {
			header.getAnnotations().forEach(
					annotation -> localCoordinates.add(LocalCoordinates.Annotation.model(annotation.getName()))
			);
		}

		return super.visitHeader(header);
	}

	@Override
	public VisitProcess visitFieldTypeDefinition(final FieldTypeDefinition td) {
		localCoordinates.add(LocalCoordinates.Typedef.of(td.getId()));
		return super.visitFieldTypeDefinition(td);
	}

	@Override
	public VisitProcess visitDocumentUniquenessCriterion(final DocumentUniquenessCriterion uc) {
		localCoordinates.add(LocalCoordinates.DocumentUniquenessCriterion.of(uc.getName()));
		return super.visitDocumentUniquenessCriterion(uc);
	}

	@Override
	public VisitProcess visitElement(com.mgmtp.a12.kernel.md.model.a12internal.Element element) {
		localCoordinates.add(LocalCoordinates.Element.of(element.getId()));

		element.getAnnotations().forEach(
				annotation -> localCoordinates.add(LocalCoordinates.Annotation.of(
						annotation.getName(), element.getId()
				))
		);

		return super.visitElement(element);
	}
}
