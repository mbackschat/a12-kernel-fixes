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

package com.mgmtp.a12.kernel.mapping.service.internal;

import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.IDataTransferService;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.md.rt.internal.service.ModelGroovyCode;

public class DataTransferModelGroovyCode extends ModelGroovyCode {
	/**
	 * The generated code is compiled in a lazy mode.
	 *
	 * @param modelHash
	 * 		hash code of the document model. This is used to determine if the generated code should be replaced, e.g.
	 * 		in case the document model was changed.
	 * @param generatedPlainCode
	 * 		generated groovy code from the model as string.
	 */
	public DataTransferModelGroovyCode(String modelHash, String generatedPlainCode) {
		super(modelHash, generatedPlainCode);
	}

	public IDataTransferService getDataTransferService() {
		return (IDataTransferService) modelCode().invokeMethod("getDataTransferService", null);
	}

	public IMetaModel getTargetMetaModel() {
		return (IMetaModel) modelCode().invokeMethod("getTargetMetaModel", null);
	}
}
