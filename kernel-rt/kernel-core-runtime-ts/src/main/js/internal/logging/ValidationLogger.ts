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
import type { Logger } from "@com.mgmtp.a12.utils/utils-logging";
import { LogLevel } from "@com.mgmtp.a12.utils/utils-logging";

import { Settings } from "@com.mgmtp.a12.utils/utils-logging";

import type { IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { DatenImpl } from "../util/DatenImpl.js";

/**
 * ValidationLogger ist eine Wrapper-Datei um den übergebenen Logger. In einer
 * späteren Ausbaustufe kann hier für das Tracen der Rules zusätzliche Ausgaben
 * konfiguriert werden. Im generiert Code wird daher nicht nur ein LogInfo
 * geschrieben, sondern der explizite Aufruf von logRegel, logRegelFires,
 * logRegelPasses Die Erweiterung des RuleTracings kann dann ohne Änderung der
 * Codegenerators implementiert werden.
 */
export class ValidationLogger {
	private _logger: Logger;
	private _traceEnabled: boolean;
	private _debugEnabled: boolean;
	private static get ungueltigerMesspunkt(): number {
		return -1;
	}

	constructor(logger: Logger) {
		this._logger = logger;
		try {
			this._traceEnabled = Settings.isActive(LogLevel.TRACE);
			this._debugEnabled = Settings.isActive(LogLevel.LOG);
		} catch (_e) {
			this._traceEnabled = false;
			this._debugEnabled = false;
		}
	}

	startMesspunkt(): number {
		if (this.isTraceEnabled) {
			return Date.now();
		}
		return ValidationLogger.ungueltigerMesspunkt;
	}

	logMessung(logText: string, startMesspunktOrL: number, startMesspunkt?: number): void {
		if (this.isTraceEnabled) {
			if (startMesspunkt == undefined) {
				if (startMesspunktOrL !== ValidationLogger.ungueltigerMesspunkt) {
					const aktMs: number = Date.now() - startMesspunktOrL;
					this._logger.trace(logText + ": " + aktMs + " ms");
				}
			} else {
				if (startMesspunkt !== ValidationLogger.ungueltigerMesspunkt) {
					const aktMs: number = Date.now() - startMesspunkt;
					const logL: string = startMesspunktOrL === 1 ? "" : "(" + startMesspunktOrL + ")";
					this._logger.trace(logText + logL + ": " + aktMs + " ms");
				}
			}
		}
	}

	logRegelFires(regelName: string, id: IIdentifier): void {
		if (this.isTraceEnabled) {
			const text: string = "Rule " + regelName + " triggered for " + DatenImpl.idIdToString(id);
			this.trace(text);
		}
	}

	logRegel(regelName: string): void {
		if (this.isTraceEnabled) {
			const text: string = "Rule " + regelName + " was validated.";
			this.trace(text);
		}
	}

	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	debug(message: any, t?: any): void {
		this._logger.log(message);
	}

	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	trace(message: any, t?: any): void {
		this._logger.trace(message);
	}

	get isDebugEnabled(): boolean {
		return this._debugEnabled;
	}

	get isTraceEnabled(): boolean {
		return this._traceEnabled;
	}

	get logger(): Logger {
		return this._logger;
	}

	set logger(log: Logger) {
		// Wird von der CoreChecker Klasse zu Beginn aufgerufen und immer wenn
		// sich der Logger ändert.
		this._logger = log;
	}

	logRegelNichtAusgewertet(regelname: string, fehlertext: string): void {
		if (this.isTraceEnabled) {
			this.trace("Rule <" + regelname + "> was not validated. " + fehlertext);
		}
	}
	logBerechnetesFeldNichtAusgewertet(berechnetesFeld: string, fehlertext: string): void {
		if (this.isTraceEnabled) {
			this.trace(
				"Calculation for calculated field <" +
				berechnetesFeld +
				"> was not evaluated. " +
				fehlertext
			);
		}
	}
}
