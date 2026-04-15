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

import type { SupportedType } from "@com.mgmtp.a12.utils/utils-localization";

import type { IMetaField } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";


import { VkString } from "./VkString.js";

export class RuntimeUtils {

	/**
	 * ersetzt Class.getSimpleName()
	 *
	 * @param o 	Objekt, dessen einfacher Klassenname gesucht wird
	 * @return der einfache Klassenname
	 */
	public static getSimpleNameFromClass(o: any): string {
		return o.constructor.name;
	}

	/**
	 * Checks if a String is whitespace, empty("") or `undefined`.
	 * ```
	 * isBlank(undefined) === true
	 * isBlank("")        === true
	 * isBlank(" ")       === true
	 * isBlank("bob")     === false
	 * isBlank("  bob  ") === false
	 * ```
	 *
	 * @param str 	the String to check, may be `undefined`
	 * @return `true` if the String is `undefined`, empty or whitespace
	 */
	public static isBlank(str: string | undefined): boolean {
		if (!str) {
			return true;
		}
		if (/\S/.test(str)) {
			// string is not empty and is just whitespace
			return false;
		}
		return true;
	}

	public static isStringArray(array: any): boolean {
		const arrayIsArray: boolean = array instanceof Array;
		let itemIsString = true;
		if (arrayIsArray) {
			itemIsString = array.every((item: any) => {
				return typeof item === "string";
			});
		}
		return arrayIsArray && itemIsString;
	}

	public static isNumberArray(array: any): boolean {
		const arrayIsArray: boolean = array instanceof Array;
		let itemIsNumber = true;
		if (arrayIsArray) {
			itemIsNumber = array.every((item: any) => {
				return typeof item === "number";
			});
		}
		return arrayIsArray && itemIsNumber;
	}

	public static isVkStringArray(array: any): boolean {
		const arrayIsArray: boolean = array instanceof Array;
		let itemIsVkString = true;
		if (arrayIsArray) {
			itemIsVkString = array.every((item: any) => {
				return item instanceof VkString;
			});
		}
		return arrayIsArray && itemIsVkString;
	}

	public static isIIdentifier(id: any): id is IIdentifier {
		if (id == undefined) {
			return false;
		}
		return id.getIndices !== undefined && id.getName !== undefined;
	}

	public static isIMetaField(field: any): field is IMetaField {
		if (field == undefined) {
			return false;
		}
		return field.convert !== undefined;
	}

	public static basicValueEquals(valueA: SupportedType, valueB: SupportedType): boolean {
		if (typeof valueA !== typeof valueB) {
			return false;
		}
		switch (typeof valueA) {
			case "string":
			case "number":
			case "boolean":
				return valueA === valueB;
			case "object":
				// null
				if (valueA === null) {
					return valueB === null;
				}

				// Date[]
				if (Array.isArray(valueA)) {
					return Array.isArray(valueB)
						&& valueA.length === 2
						&& valueB.length === 2
						&& this.dateEquals(valueA[0], valueB[0])
						&& this.dateEquals(valueA[1], valueB[1]);
				}

				// Date
				return this.dateEquals(valueA, <object>valueB);
		}
		return false;
	}

	public static dateEquals(valueA: object, valueB: object): boolean {
		return valueA instanceof Date
			&& valueB instanceof Date
			&& valueA.getTime() === valueB.getTime();
	}

	public static idToString(id: IIdentifier): string {
		const pathParts = id.getName().split(IIdentifier.SEPARATOR);
		const indices = id.getIndices();
		let result = "";
		for (let i = 1; i < pathParts.length; i++) {
			result += `${IIdentifier.SEPARATOR}${pathParts[i]}[${indices[i - 1] ?? 1}]`;
		}
		return result;
	}

	/**
	 * @returns a copy of `stringValue` where all occurrences of `\r\n` are replaced with `\n`.
	 */
	public static normalizeLinebreaks(stringValue: string): string {
		return stringValue.replace(/\r\n/g, "\n");
	}
}
