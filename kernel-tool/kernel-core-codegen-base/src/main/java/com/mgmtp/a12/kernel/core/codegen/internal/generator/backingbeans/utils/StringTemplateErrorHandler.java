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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.STMessage;

/**
 * Handles errors occurring during {@link ST} (StringTemplate) evaluation.
 */
public class StringTemplateErrorHandler implements org.stringtemplate.v4.STErrorListener {
	private static Logger logger = LoggerFactory.getLogger(StringTemplateErrorHandler.class);

	protected void error(String msg, Throwable t) {
		if (msg == null) {
			msg = "";
		}

		String errorMessage = "Fehler bei der Generierung: " + msg;
		if (t != null && t.getMessage() != null) {
			errorMessage += t.getMessage();
		}
		throw new IllegalArgumentException(errorMessage, t);
	}

	public void warning(String msg) {
		logger.warn("Fehler bei der Generierung " + msg);
	}

	@Override
	public void IOError(STMessage stMsg) {
		error(stMsg.toString(), stMsg.cause);
	}

	@Override
	public void compileTimeError(STMessage stMsg) {
		error(stMsg.toString(), stMsg.cause);
	}

	@Override
	public void internalError(STMessage stMsg) {
		error(stMsg.toString(), stMsg.cause);
	}

	@Override
	public void runTimeError(STMessage stMsg) {
		error(stMsg.toString(), stMsg.cause);
	}

}
