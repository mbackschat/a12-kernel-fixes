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
import { ValidationMode } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

export class LocaleUtil {
	public static readonly DE = ValidationMode.DE;
	public static readonly EN = ValidationMode.EN;
	public static readonly FR = ValidationMode.FR;
	public static readonly NL = ValidationMode.NL;
	public static readonly LOCALE_PATTERN: RegExp = new RegExp(
		"^(?:[a-z]{2}(_(?:[a-z]{2}){1,2}(?:_[a-z]{2})?)?)$",
		"i"
	);
	private static readonly _GERMAN_LOCALE: RegExp = new RegExp(
		"^(?:de(_(?:[a-z]{2}){1,2}(?:_[a-z]{2})?)?)$",
		"i"
	);
	private static readonly _FR_LOCALE: RegExp = new RegExp(
		"^(?:fr(_(?:[a-z]{2}){1,2}(?:_[a-z]{2})?)?)$",
		"i"
	);
	private static readonly _NL_LOCALE: RegExp = new RegExp(
		"^(?:nl(_(?:[a-z]{2}){1,2}(?:_[a-z]{2})?)?)$",
		"i"
	);

	private readonly _supportedLocales: string[];

	public constructor(supportedLocales: string[]) {
		this._supportedLocales = supportedLocales;
	}

	public deriveLocaleIfPossible(param: any, msgNoLocale: string): string {
		if (typeof param !== "string" || !LocaleUtil.LOCALE_PATTERN.test(param)) {
			throw new Error(msgNoLocale);
		} else {
			const locale: string = param;
			if (!this._isSupportedLocale(locale)) {
				throw new Error("The locale '" + param + "' is not supported.");
			}
			return locale;
		}
	}

	public deriveLocaleFromFirstParam(params: any[]): string | undefined {
		let sprache: string | undefined;
		if (params != undefined && params.length !== 0) {
			if (
				typeof params[0] !== "string" ||
				(typeof params[0] === "string" && !LocaleUtil.LOCALE_PATTERN.test(params[0]))
			) {
				return undefined; // Error handling needs to happen at the caller!
			}
			if (!this._isSupportedLocale(params[0])) {
				throw new Error("The locale '" + params[0] + "' is not supported.");
			}
			sprache = params[0];
		}
		return sprache;
	}

	public checkLocale(sprache: string): any {
		if (!this._isSupportedLocale(sprache)) {
			throw new Error("The locale '" + sprache + "' is not supported.");
		}
	}

	private _isSupportedLocale(sprache: string): boolean {
		return this._supportedLocales.indexOf(sprache) !== -1;
	}

	public static isGerman(sprache: string): boolean {
		return LocaleUtil._GERMAN_LOCALE.test(sprache);
	}

	public static isFrensh(sprache: string): boolean {
		return LocaleUtil._FR_LOCALE.test(sprache);
	}

	public static isNetherlands(sprache: string): boolean {
		return LocaleUtil._NL_LOCALE.test(sprache);
	}
}
