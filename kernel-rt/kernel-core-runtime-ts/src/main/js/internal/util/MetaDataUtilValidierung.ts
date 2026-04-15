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


import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import type { FormatDefinitionString } from "../formatdef/FormatDefinitionString.js";
import { FormatDefinitionZahl } from "../formatdef/FormatDefinitionZahl.js";


/**
 * Hilfsklasse für alle Utility Methoden bezogen auf ein Feld.
 */
export class MetaDataUtilValidierung {
	/**
	 * Prüft, ob das Feld zum angegebenen Feldnamen ein Zahl-Feld ist.
	 */
	public static isNumberFeld(name: string, meta: IMetaDataValidierung): boolean {
		return meta.getIFormatDefinition(name) instanceof FormatDefinitionZahl;
	}

	public static getValue4Category(
		meta: IMetaDataValidierung,
		fieldUniqueName: string,
		xmlValue: string,
		categoryName: string
	): string | undefined {
		const format: FormatDefinitionString = <FormatDefinitionString>(
			meta.getIFormatDefinition(fieldUniqueName)
		);
		const categoryValues: Map<string, string> | undefined = format.getEnumCategoryValues(xmlValue);
		if (!categoryValues) {
			return undefined;
		}
		const value4Category: string | undefined = categoryValues.get(categoryName);
		return value4Category;
	}

}
