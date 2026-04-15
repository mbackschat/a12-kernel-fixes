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
package com.mgmtp.a12.kernel.md.model.internal.betterbuilders;

import java.util.List;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelObjectReferenceBuilder;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;

public class ComputationBuilder {

	private final DocumentModel documentModel;
	private final Computation.ComputationBuilder computationBuilder;
	private Group parent;
	private Integer targetPosition;
	private String fullName;

	private ComputationBuilder(DocumentModel documentModel) {
		this.documentModel = documentModel;
		this.computationBuilder = Computation.builder()
				.errorMessage(BuilderUtil.getTextMap(documentModel, "errorText"));
	}

	public static ComputationBuilder with(DocumentModel documentModel) {
		return new ComputationBuilder(documentModel);
	}

	public ComputationBuilder id(String id) {
		computationBuilder.id(id);
		return this;
	}

	public ComputationBuilder name(String fullName) {
		String pathWithRepetitions = BuilderUtil.pathPartOf(fullName);
		parent = GroupBuilder.with(documentModel).ensureGroup(pathWithRepetitions);
		computationBuilder.id(fullName)
				.name(BuilderUtil.lastPartOf(fullName));
		this.fullName = fullName;
		return this;
	}

	public ComputationBuilder field(Field field) {
		String fieldFullName = ElementUtils.getPath(field);
		String fieldRelativePath = ElementPathUtils.relPath(fieldFullName, fullName);
		computationBuilder
				.computedField(new DocumentModelObjectReferenceBuilder<Field>().relativePath(fieldRelativePath).dmo(field).build())
				.computationAlternatives(List.of(
						Computation.ComputationAlternative.builder()
								.precondition("GroupFilled(RuleGroup)")
								.operation("5").build()
				));
		return this;
	}

	public ComputationBuilder field(String fieldFullName) {
		String fieldRelativePath = ElementPathUtils.relPath(fieldFullName, fullName);
		computationBuilder
				.computedField(new DocumentModelObjectReferenceBuilder<Field>().relativePath(fieldRelativePath).build())
				.computationAlternatives(List.of(
						Computation.ComputationAlternative.builder()
								.precondition("GroupFilled(RuleGroup)")
								.operation("5").build()
				));
		return this;
	}

	public ComputationBuilder field(Field field, List<Pair<String, String>> preconditionOperationPairs) {
		String fieldFullName = ElementUtils.getPath(field);
		String fieldRelativePath = ElementPathUtils.relPath(fieldFullName, fullName);
		computationBuilder
				.computedField(new DocumentModelObjectReferenceBuilder<Field>().relativePath(fieldRelativePath).dmo(field).build())
				.computationAlternatives(
						preconditionOperationPairs.stream().map(preconditionOperationPair ->
								Computation.ComputationAlternative.builder()
										.precondition(preconditionOperationPair.getLeft())
										.operation(preconditionOperationPair.getRight()).build()
						).toList()
				);
		return this;
	}

	public ComputationBuilder modify(UnaryOperator<Computation.ComputationBuilder> modifier) {
		modifier.apply(computationBuilder);
		return this;
	}

	public ComputationBuilder moveToTop() {
		return moveToPosition(0);
	}

	public ComputationBuilder moveToPosition(Integer position) {
		targetPosition = position;
		return this;
	}

	public Computation build() {
		Computation computation = computationBuilder.build();
		if (targetPosition == null) {
			parent.addElement(computation);
		} else {
			parent.addElement(targetPosition, computation);
		}
		return computation;
	}
}
