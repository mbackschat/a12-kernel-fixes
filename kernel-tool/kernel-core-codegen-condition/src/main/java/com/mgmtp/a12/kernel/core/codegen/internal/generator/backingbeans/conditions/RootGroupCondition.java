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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.conditions;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.EntityId;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.LfdNrIndexCode;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.UsbIndexCode;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.RuntimeMethodNameFactory;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.RootGroupConditionName;

/**
 *
 */
public class RootGroupCondition extends CodeGenCondition implements IRootGroupCondition {
	private final RootGroupConditionName bedingungName;
	private final String vordruckName;
	private final long lfdNr;
	private final long usb;
	private final IEntityId entityId;

	public RootGroupCondition(IRootGroupCondition b, IEntityId entityId) {
		this.bedingungName = b.getConditionName();
		this.vordruckName = b.getRootGroupName();
		this.lfdNr = b.getRootGroupIndex();
		this.usb = b.getUsb();
		this.entityId = entityId;
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

	public String getBedingungNameCode() {
		return RuntimeMethodNameFactory.getRuntimeMethodNameFromParseTreeObject(this);
	}

	/**
	 * @return Codefragment für USB
	 */
	public String getUsbCode() {
		return new UsbIndexCode(usb).toCode();
	}

	/**
	 * @return Codefragment für lfdNr
	 */
	public String getLfdNummerCode() {
		return new LfdNrIndexCode(lfdNr).toCode();
	}

	public String getEntityIdCodeRefCpp() {
		return entityId == null ? "ER::RtIdentifierTemplate()" : ((EntityId) entityId).toCode();
	}

	public String getEntityIdCodeRef() {
		return entityId == null ? "null" : ((EntityId) entityId).toCode();
	}
}
