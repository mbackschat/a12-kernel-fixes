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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung;

import com.mgmtp.a12.kernel.core.parser.internal.constants.EntityIdConstants;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.RootGroupConditionName;

public class RootGroupConditionImpl extends ConditionImpl implements IRootGroupCondition {

	private final RootGroupConditionName bedingungName;
	private final String vordruckName;
	private final long lfdNr;
	private final long usb;
	private final IEntityId entityId;

	public RootGroupConditionImpl(RootGroupConditionName bedingungName, String vordruckName, long lfdNr, long usb,
			IEntityId entityId) {
		this.bedingungName = bedingungName;
		this.vordruckName = vordruckName;
		this.lfdNr = lfdNr;
		this.usb = usb; // is IParseTreeEntity.UNSPECIFIED, if the root group has no USB
		this.entityId = entityId;
	}

	public RootGroupConditionImpl(IRootGroupCondition b, IEntityId entity) {
		super(b);
		bedingungName = b.getConditionName();
		vordruckName = b.getRootGroupName();
		lfdNr = b.getRootGroupIndex();
		usb = b.getUsb();
		entityId = entity;
	}

	public String toString() {
		String semIdxStr;
		if (entityId == null) {
			semIdxStr = "";
		} else {
			semIdxStr = entityId.getSemanticIndex().isEmpty() ?
					"" :
					" " + EntityIdConstants.SEMANTIC_INDEX_TOKEN + " " + entityId.getSemanticIndex().get(0).toString();
		}
		return String.format("%s(%s/%d@%d%s)", bedingungName.name(), vordruckName, lfdNr, usb, semIdxStr);
	}

	@Override
	public RootGroupConditionName getConditionName() {
		return bedingungName;
	}

	@Override
	public String getRootGroupName() {
		return vordruckName;
	}

	@Override
	public long getRootGroupIndex() {
		return lfdNr;
	}

	@Override
	public long getUsb() {
		return usb;
	}

	@Override
	public IEntityId getEntityId() {
		return entityId;
	}
}
