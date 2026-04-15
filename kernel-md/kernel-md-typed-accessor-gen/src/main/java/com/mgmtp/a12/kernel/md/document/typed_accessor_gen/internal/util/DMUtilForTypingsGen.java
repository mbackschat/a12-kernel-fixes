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

package com.mgmtp.a12.kernel.md.document.typed_accessor_gen.internal.util;

import static com.mgmtp.a12.kernel.core.base.utils.internal.FileAndResourceUtil.readFromFileOrResourceUtf8;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.kernel.md.serializer.MDSerializerFactory;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.DocumentModelSerializerImpl;
import com.mgmtp.a12.model.notification.RankedNotification;

public class DMUtilForTypingsGen {
	private DMUtilForTypingsGen(){throw new RuntimeException("static utility class");};

	/**
	 *
	 * @param dm
	 * @param keepByFullName Decides if a given fullname should be kept in the dm, or if the entity at fullname and all its
	 *                          descendents should be removed in the copy that gets created. Expects the fullName as a list of
	 *                          strings (i.e. the parts of the fullName as a list instead of prepended with "/" and joined).
	 * @return a pruned version of dm, that only contains subgroups and fields that are accepted by keeyByFullName
	 * based on their fullName.
	 */
	public static IDocumentModel filterDm(IDocumentModel dm, Predicate<List<String>> keepByFullName) {
		IDocumentModel dmCopy = deepCopyDM(dm);
		DocumentModel internalDmCopy = ((DocumentModelWrapper) dmCopy).getDocumentModel();
		prune(internalDmCopy.getContent().getModelRoot(), List.of(), keepByFullName);
		return new DocumentModelWrapper(internalDmCopy);
	}

	private static void prune(Group group, List<String> fullName, Predicate<List<String>> keepByFullName) {
		List<Element> origElements = group.getElements();
		group.setElements(
				origElements.stream()
						.map((Element e) -> {
							List<String> childFullName = Java8Utils.appendList(fullName, e.getName());
							if (!keepByFullName.test(childFullName)) {
								return null;
							}
							if (e instanceof Group subGroup) {
								prune(subGroup, childFullName, keepByFullName);
							}
							return e;
						})
						.filter(Objects::nonNull)
						.toList()
		);
	}

	public static IDocumentModel deepCopyDM(IDocumentModel dm) {
		var dmDeser = new DocumentModelSerializerImpl();
		Writer writer = new StringWriter();
		List<RankedNotification> problems = new ArrayList<>();
		try {
			dmDeser.serialize(dm, writer, problems::add);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if(!problems.isEmpty()) {
			throw new RuntimeException("problems occured while serializing the dm: " + problems);
		}
		Reader reader = new StringReader(writer.toString());
		try {
			return dmDeser.deserialize(reader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static IDocumentModel readDmFromFileOrClasspathResource(String inputPath) {
		return readFromFileOrResourceUtf8(inputPath,
				reader -> new MDSerializerFactory().createDocumentModelSerializer().deserialize(reader));
	}

	public static Set<ModelRef4Include> makeLocalModelsRefs(Collection<IDocumentModel> dms, List<String> localPackagePrefix) {
		Set<ModelRef4Include> localModelRefs = new HashSet<>();
		for(var dm : dms) {
			List<IGroup> rootGroups = dm.getContent().getDocumentModelRoot().getElements().stream()
					.map(e -> e instanceof IGroup group ? group : null)
					.filter(Objects::nonNull)
					.toList();
			localModelRefs.add(ModelRef4Include.of(
					dm.getHeader().getId(),
					localPackagePrefix,
					rootGroups.size() == 1 ? rootGroups.get(0).getName() : null));
		}
		return localModelRefs;
	}
}
