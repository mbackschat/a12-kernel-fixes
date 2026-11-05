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

export class Constants {
	public static get FIELD_LAYER_SEPARATOR(): string {
		return "@";
	}
	public static get SEMANTIC_INDEX_SEPARATOR(): string {
		return "#";
	}

	// allgemeine Strings für die Formatprüfung
	public static get BLANK(): string {
		return " ";
	}

	// Fehlermeldungen für Datum Format:
	// -------------------------------
	// allgemeine Strings: dieses Datum gilt zur Festlegung eines gültigen Datums,
	// ein Datum muss später als dieses Datum sein, außer es wurde beim Datumsformat
	// definiert, dass keine Zusatzprüfung erfolgen soll.
	public static get DATUM_FORMAT(): string {
		return "dd.MM.yyyy";
	}

	/**
	 * Used in rule error texts to access to the value of the field.
	 * <p>
	 * Format: $FIELD_NAME.Wert$
	 * <p>
	 * Note: If the rule error text was written in English with $FIELD_NAME.value$, the code
	 * generator replaces the substring ".value" with ".Wert". In this way we have to deal only with
	 * ".Wert" in the runtime.
	 */
	public static get ERROR_TEXT_PARAM_4_FIELD_VALUE(): string {
		return ".Wert";
	}
	/**
	 * Used in rule error texts to access to the corresponding category value of the field.
	 * <p>
	 * Format: $FIELD_NAME->CATEGORY_VALUE$
	 * <p>
	 * Note: This constant must be equal to EntityIdConstants.CATEGORY_VALUE_ACCESS
	 */
	public static get CATEGORY_VALUE_ACCESS(): string {
		return "->";
	}

	/**
	 * Used in rule error texts to access index field properties
	 * <p>
	 * Format: $index(FIELD_NAME).Wert$ resp. $index(FIELD_NAME)->CATEGORY_VALUE$
	 * <p>
	 * Note: If the rule error text was written in English with $index(FIELD_NAME).value$, the code
	 * generator replaces the substring ".value" with ".Wert". In this way we have to deal only with
	 * ".Wert" in the runtime.
	 */
	public static get ERROR_TEXT_PARAM_4_INDEX_FIELD_REF(): string {
		return "index(";
	}

	// Für die Bezeichnung welche Art von Berechnungen verwendet werden.
	public static get SERVER(): string {
		return "Server";
	}

	/**
	 * <b>Note:</b>
	 * The value of 'RUNTIME_VERSION' is generally not equal to the 'version' of the module,
	 * because, for example in case of a minor patch that does not change the internal interface
	 * between generated code and runtime, 'version' needs to be changed, but 'RUNTIME_VERSION'
	 * can remain the same. This has the advantage for the projects that they do not need to
	 * re-generate code.
	 * <p>
	 * <b>WARNING:</b> Don't change this line. Updated by versionupdate script
	 */
	// prettier-ignore
	public static get RUNTIME_VERSION(): string { return "31.3"; }

}
