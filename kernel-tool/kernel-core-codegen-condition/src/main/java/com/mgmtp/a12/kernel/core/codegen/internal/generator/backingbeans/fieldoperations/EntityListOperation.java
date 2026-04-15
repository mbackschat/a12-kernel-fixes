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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.fieldoperations;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityIdList;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.GroupedEntityIdList;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.RuntimeMethodNameFactory;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListOperationName;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;

/**
 *
 */
public class EntityListOperation extends CodeGenOperation implements IEntityListOperation {

	protected final EntityIdList entityList;
	protected final EntityListOperationName operatorName;
	protected final IEC ec;

	public EntityListOperation(IEntityListOperation op, EntityIdList entityIdListe, boolean expandGroups, IEC ec) {
		super(op);
		this.ec = ec;
		this.operatorName = op.getOperatorName();
		if (op.getOperatorName() == EntityListOperationName.NumberOfFilledGroups && expandGroups) {
			this.entityList = new GroupedEntityIdList(entityIdListe);
		} else {
			this.entityList = entityIdListe;
		}
	}

	@Override
	public IEntityIdList getEntityList() {
		return entityList;
	}

	@Override
	public EntityListOperationName getOperatorName() {
		return operatorName;
	}

	public String getOperatorNameCode() {
		return RuntimeMethodNameFactory.getRuntimeMethodNameFromParseTreeObject(this, ec);
	}

	public String getFeldListeCode() {
		return entityList.toCode();
	}
}
