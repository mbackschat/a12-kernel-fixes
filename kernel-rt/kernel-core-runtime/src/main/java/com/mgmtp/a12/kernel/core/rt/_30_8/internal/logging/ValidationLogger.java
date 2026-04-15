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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * ValidationLogger ist eine Wrapper-Datei um einen statisch erzeugten Logger. In einer
 * späteren Ausbaustufe kann hier für das Tracen der Rules zusätzliche Ausgaben
 * konfiguriert werden. Im generiert Code wird daher nicht nur ein LogInfo
 * geschrieben, sondern der explizite Aufruf von logRegel, logRegelFires,
 * logRegelPasses Die Erweiterung des RuleTracings kann dann ohne Änderung der
 * Codegenerators implementiert werden.
 */
public class ValidationLogger {
	private static final Logger LOG = LoggerFactory.getLogger(ValidationLogger.class);

	private static long ungueltigerMesspunkt = -1;

	public long startMesspunkt() {
		if (isTraceEnabled()) {
			return System.nanoTime();
		}

		return ungueltigerMesspunkt;
	}

	public void logMessung(String regelName, long l, long startMesspunkt) {
		if (isTraceEnabled() && startMesspunkt != ungueltigerMesspunkt) {
			long aktNanos = System.nanoTime() - startMesspunkt;
			long aktMicros = aktNanos / 1000;
			String logL = l == 1 ? "" : "(" + l + ")";
			LOG.trace(regelName + logL + ": " + aktMicros + " microsec");
		}
	}

	public void logMessung(String logText, long startMesspunkt) {
		if (isTraceEnabled() && startMesspunkt != ungueltigerMesspunkt) {
			long aktNanos = System.nanoTime() - startMesspunkt;
			long aktMicros = aktNanos / 1000;
			LOG.trace(logText + ": " + aktMicros + " microsec");
		}
	}

	public void logRegelFires(String regelName, IIdentifier id, IData data) {
		if (LOG.isTraceEnabled()) {
			String text = String.format("Rule '%s' triggered for '%s'", regelName, data.identifierToString(id));
			LOG.trace(text);
		}
	}

	public void logRegel(String regelName) {
		if (LOG.isTraceEnabled()) {
			String text = String.format("Rule '%s' was validated.", regelName);
			LOG.trace(text);
		}
	}

	public void debug(String arg) {
		LOG.debug(arg);
	}

	public void debug(String arg, Throwable t) {
		LOG.debug(arg, t);
	}

	public void trace(String arg) {
		LOG.trace(arg);
	}

	public void trace(String arg, Throwable t) {
		LOG.trace(arg, t);
	}

	public void error(String message, Throwable t) {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		t.printStackTrace(printWriter);
		LOG.error(message, t);
		LOG.error(writer.toString());
	}

	public boolean isDebugEnabled() {
		return LOG.isDebugEnabled();
	}

	public boolean isTraceEnabled() {
		return LOG.isTraceEnabled();
	}

	public void logRegelNichtAusgewertet(String regelname, String fehlertext) {
		if (LOG.isTraceEnabled()) {
			StringBuilder buf = new StringBuilder();
			buf.append("Rule <" + regelname + "> was not validated. ");
			buf.append(fehlertext);
			LOG.trace(buf.toString());
		}
	}

	public void logBerechnetesFeldNichtAusgewertet(String berechnetesFeld, String fehlertext) {
		if (LOG.isTraceEnabled()) {
			StringBuilder buf = new StringBuilder();
			buf.append("Calculation for calculated field <" + berechnetesFeld + "> was not evaluated. ");
			buf.append(fehlertext);
			LOG.trace(buf.toString());
		}
	}
}
