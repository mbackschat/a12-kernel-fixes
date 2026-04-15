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
package com.mgmtp.a12.kernel.core.parser.internal.error;

import org.antlr.v4.runtime.Token;

import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;

/**
 * Exception to handle all parsing and lexing errors.
 */
public class MVKParserError extends MVKCoreError {
	private static final long serialVersionUID = -8275952239007976003L;
	private final int sourceStart;
	private final int sourceEnd;
	private final int line;
	private final int lineEnd;

	public MVKParserError(Object origin, String message) {
		super(message, origin);
		this.sourceStart = -1;
		this.sourceEnd = -1;
		this.line = -1;
		this.lineEnd = -1;
	}

	public MVKParserError(Token startToken, Token endToken, Object origin, String message) {
		super(message, origin);

		int startTokenLength;
		if (startToken != null) {
			this.sourceStart = startToken.getCharPositionInLine();
			this.line = startToken.getLine();
			startTokenLength = tokenLength(startToken);
		} else {
			this.sourceStart = -1;
			this.line = -1;
			startTokenLength = 0;
		}

		if (endToken != null) {
			this.sourceEnd = endToken.getCharPositionInLine() + tokenLength(endToken);
			this.lineEnd = endToken.getLine();
		} else {
			this.sourceEnd = sourceStart + startTokenLength;
			this.lineEnd = this.line;
		}
	}

	private int tokenLength(Token t) {
		return t.getStopIndex() - t.getStartIndex() + 1;
	}

	public MVKParserError(int start, int end, int line, int lineEnd, Object origin, String message) {
		super(message, origin);
		this.sourceStart = start;
		this.sourceEnd = end;
		this.line = line;
		this.lineEnd = lineEnd;
	}

	@Override
	public int getLine() {
		return line;
	}

	@Override
	public int getLineEnd() {
		return lineEnd;
	}

	@Override
	public int getSourceEnd() {
		return sourceEnd;
	}

	@Override
	public int getSourceStart() {
		return sourceStart;
	}

}
