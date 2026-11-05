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

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelObjectReferenceBuilder;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;

public class GroupBuilder {

	private final DocumentModel documentModel;
	private final Group.GroupBuilder groupBuilder;
	private final Map<String, Group> fullNameToGroupLookup;
	private Group parent;

	private GroupBuilder(DocumentModel documentModel) {
		this.documentModel = documentModel;
		this.fullNameToGroupLookup = initLookup(documentModel);
		this.groupBuilder = Group.builder();
	}

	public static GroupBuilder with(DocumentModel documentModel) {
		return new GroupBuilder(documentModel);
	}

	private Map<String, Group> initLookup(DocumentModel documentModel) {
		var generalLookup = BuilderUtil.initLookup(documentModel);
		return generalLookup.entrySet().stream()
				.filter(e -> e.getValue() instanceof Group)
				.collect(Collectors.toMap(Map.Entry::getKey, e -> (Group) e.getValue()));
	}

	public Group ensureGroup(String fullname) {
		Optional<Element> elementInModel = DocumentModelUtils.getElementByPath(documentModel, fullname);
		if (elementInModel.isPresent() && elementInModel.get() instanceof Group group) {
			return group;
		}
		if (fullname.lastIndexOf("/") == 0) {
			//check if a rootgroup with this name already exists
			var existingRootGroup = documentModel.getContent().getModelRoot().getElements().stream()
					.filter(Group.class::isInstance)
					.map(Group.class::cast)
					.filter(e -> e.getName().equals(BuilderUtil.lastPartOf(fullname)))
					.findFirst();
			if (existingRootGroup.isPresent()) {
				return existingRootGroup.get();
			}
		}

		BuilderUtil.explodeParts(fullname).forEach(item ->
				fullNameToGroupLookup.computeIfAbsent(BuilderUtil.stripRepeats(item), v -> addGroupInternal(item))
		);
		Group parentGroup = fullNameToGroupLookup.get(BuilderUtil.stripRepeats(fullname));
		return parentGroup == null ? documentModel.getContent().getModelRoot() : parentGroup;
	}

	private Group addGroupInternal(String fullname) {
		String parentGroupPath = BuilderUtil.stripRepeats(BuilderUtil.pathPartOf(fullname));
		String path = (parentGroupPath.isBlank()) ? BuilderUtil.PATH_SEPARATOR : parentGroupPath;
		int maxValue = BuilderUtil.getRepeat(BuilderUtil.lastPartOf(fullname));
		Group parentGroup = Optional.ofNullable(fullNameToGroupLookup.get(path))
				.orElseGet(() -> documentModel.getContent().getModelRoot());

		Group group = Group.builder()
				.id(fullname)
				.name(BuilderUtil.lastPartOf(fullname))
				.repeatability(maxValue)
				.build();
		parentGroup.addElement(group);
		return group;
	}

	public GroupBuilder id(String id) {
		groupBuilder.id(id);
		return this;
	}

	public GroupBuilder adaptId(UnaryOperator<String> idFunc) {
		groupBuilder.id(idFunc.apply(groupBuilder.getId()));
		return this;
	}

	public GroupBuilder name(String fullName) {
		String pathWithRepetitions = BuilderUtil.pathPartOf(fullName);
		parent = ensureGroup(pathWithRepetitions);
		groupBuilder
				.id(fullName)
				.name(BuilderUtil.lastPartOf(fullName));
		return this;
	}

	public GroupBuilder repeat(int repeatability) {
		groupBuilder.repeatability(repeatability);
		return this;
	}

	public GroupBuilder indexField(String relativePath) {
		groupBuilder.indexField(new DocumentModelObjectReferenceBuilder<Field>().relativePath(relativePath).build());
		return this;
	}

	public GroupBuilder modify(UnaryOperator<Group.GroupBuilder> modifier) {
		modifier.apply(groupBuilder);
		return this;
	}

	public Group build() {
		Group group = groupBuilder.build();
		parent.addElement(group);
		return group;
	}
}
