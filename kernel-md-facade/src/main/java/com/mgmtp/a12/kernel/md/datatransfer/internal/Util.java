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

package com.mgmtp.a12.kernel.md.datatransfer.internal;

import java.util.ArrayList;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BaseCodeGenUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig;
import com.mgmtp.a12.kernel.md.model.api.services.a12internal.ICodeGeneratorConfigV1_2;

import lombok.NonNull;

public class Util {
	/**
	 * Appends "." + packageSuffix to the {@link ICodeGeneratorConfigV1_2#getPackageName()} of this config.<p/>
	 * If the package name was empty, the resulting config will have the packageName
	 * {@code <default package name>.<packageSuffix>}.
	 * See {@link IValidationCodeGeneratorConfig#getPackageName()} for details on the default package name.
	 *
	 * @implNote util method; there should be no need to override the default impl
	 */
	public static <C extends ICodeGeneratorConfigV1_2> C extendPackageName(C config, String packageSuffix) {
		if(packageSuffix.startsWith(".")) {
			throw new IllegalArgumentException("packageSuffix must not start with a '.', but got " + packageSuffix);
		}
		assert !packageSuffix.endsWith(".") : packageSuffix; // possibly ok but unexpected
		@SuppressWarnings("unchecked") // withPackageName specifies (in javadoc) that implementations must ensure that the returned
		// object's class is at least as specific that the original -> this cast is safe
		C updated = (C) config.withPackageName(
				BaseCodeGenUtils.getNormalizedPackageNameOrDefault(config.getPackageName()) + packageSuffix);
		return updated;
	}

	/**
	 * This cannot fail, as we only remove existing rules and computations
	 *
	 * @return a copy of the original DM without rules and computations
	 */
	public static @NonNull DocumentModel removeRulesAndComputations(@NonNull DocumentModel dm) {
		DocumentModel copy = DocumentModelCopyService.copy(dm);
		new com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker().acceptDocumentModel(copy,
				new RulesAndComputationRemover());
		return copy;
	}

	/**
	 * This simply _inplace_ removes the rules and computations of the traversed DM/group.
	 * No special cases (e.g. attachments) are considered here.
	 */
	private static class RulesAndComputationRemover extends com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor {
		@Override public void leaveGroup(@NonNull Group group) {
			group.setElements(group.getElements().stream()
					.filter(element -> !(element instanceof Rule || element instanceof Computation))
					.collect(Collectors.toCollection(ArrayList::new)));
			super.leaveGroup(group);
		}
	}
}
