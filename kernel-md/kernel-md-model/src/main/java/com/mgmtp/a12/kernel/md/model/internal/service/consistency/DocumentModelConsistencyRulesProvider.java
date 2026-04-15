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
package com.mgmtp.a12.kernel.md.model.internal.service.consistency;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.AttachmentGroupRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.BasicConsistencyCheckFromSerializerRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.CodeGenerationRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.DocumentSchemaVersionPatternRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.DocumentSchemaVersionRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.IdUniqueRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.MultiSelectGroupRule;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.NumberFieldValueLimitRule;
import com.mgmtp.a12.model.consistency.rules.ModelConsistencyRule;
import com.mgmtp.a12.model.consistency.rules.ModelConsistencyRulesProvider;

public class DocumentModelConsistencyRulesProvider implements ModelConsistencyRulesProvider<IDocumentModel> {

	@Override
	public Class<IDocumentModel> modelType() {
		return IDocumentModel.class;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		Objects.requireNonNull(clazz);
		return IDocumentModel.class.isAssignableFrom(clazz);
	}

	@Override
	public List<ModelConsistencyRule<IDocumentModel>> getRules() {
		return new ArrayList<>(getDocumentModelConsistencyRules());
	}

	public List<DocumentModelConsistencyRule> getDocumentModelConsistencyRules() {
		return List.of(
				new DocumentSchemaVersionPatternRule(),
				new DocumentSchemaVersionRule(),
				new IdUniqueRule(),
				new NumberFieldValueLimitRule(),
				new AttachmentGroupRule(),
				new MultiSelectGroupRule(),
				new CodeGenerationRule(),
				new BasicConsistencyCheckFromSerializerRule()
		);
	}
}
