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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IGroupOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.GroupOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;

/**
 *
 */
public class GroupOperationImpl extends OperationImpl implements IGroupOperation {

	private final GroupOperationName operatorName;
	private final IEntityId groupId;

	public GroupOperationImpl(GroupOperationName operatorName, IEntityId groupId) {
		super(getGroupOperationType(operatorName));
		this.operatorName = operatorName;
		this.groupId = groupId;
	}

	public GroupOperationImpl(IGroupOperation op, IEntityId groupId) {
		super(op);
		this.operatorName = op.getOperatorName();
		this.groupId = groupId;
	}

	private static OperationType getGroupOperationType(GroupOperationName operatorName) {
		if (operatorName == GroupOperationName.CurrentRepetition) {
			return OperationType.NUMBER;
		}
		throw new IllegalArgumentException("Internal Error - unknown " + operatorName);
	}

	@Override
	public GroupOperationName getOperatorName() {
		return operatorName;
	}

	@Override
	public IEntityId getGroup() {
		return groupId;
	}

	@Override
	public String toString() {
		return String.format("%s(%s)", operatorName.getRuntimeName(), groupId);
	}
}
