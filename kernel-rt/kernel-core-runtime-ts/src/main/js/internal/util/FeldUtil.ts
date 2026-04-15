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
import type { IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import type { Feld_t } from "../formatdef/Feld_t.js";

import { RtInternalIdentifier } from "./RtInternalIdentifier.js";

export class FeldUtil {
	/**
	 * Prüft, ob das Feld global ist.<br>
	 * ACHTUNG: Die Überprüfung erfolgt in diesem Fall nicht über den Key FELD_GLOBAL,
	 * da in einigen Testfällen der additional key FELD_GLOBAL nicht angegeben wird.
	 *
	 * @param f 	zu prüfendes Feld
	 * @return <code>true</code>, falls das Feld global ist
	 */
	public static isGlobal(f: Feld_t): boolean {
		return f.getAdditionalValue("GlobalesFeld") != null && f.getAdditionalValue("GlobalesFeld");
	}

	public static isGroup(id: RtInternalIdentifier): boolean {
		return id.getName().startsWith("/");
	}

	/**
	 * Erzeugt einen Identifier mit dem intern verwendeten 'uniqueName' von id.
	 * Die Indices sind die gleichen wie bei id.
	 */
	public static makeInternIdentifier(
		id: IIdentifier,
		meta: IMetaDataValidierung
	): RtInternalIdentifier {
		const eindeutigerName: string = meta.getUniqueName(id.getName());
		return new RtInternalIdentifier(eindeutigerName, id.getIndices());
	}

	/**
	 * Erzeuge einen number-Array aus seiner Stringdarstellung
	 */
	public static erzeugeLongArray(s: string): number[] {
		const teile: string[] = s.split(",");
		const result: number[] = new Array<number>(teile.length);
		for (let i = 0; i < teile.length; i++) {
			result[i] = Number(teile[i]);
		}
		return result;
	}
}
