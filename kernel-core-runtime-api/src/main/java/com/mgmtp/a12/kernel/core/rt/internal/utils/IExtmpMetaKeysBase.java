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

package com.mgmtp.a12.kernel.core.rt.internal.utils;

/**
 * This interface contains the ExtMP-specific meta keys that are also referenced in the A12 generated code / runtime. It is internal
 * and should not be used outside A12.
 */
public interface IExtmpMetaKeysBase {

	String MODEL_A12DV = "A12DV";
	String MODEL_ERVERSION = "ERVersion";
	String MODEL_XMLWRITER = "XMLWriter";
	String MODEL_UXMLWRITER = "UXMLWriter";
	String MODEL_XMLREADER = "XMLReader";

	String FIELD_FORMATKENNZEICHEN = "FormatKennzeichen";
	String FIELD_VORDRUCKZEILENNUMMER = "VordruckZeilennummer";
	String FIELD_OBERFLAECHENTEXT = "OberflaechenText";
	String FIELD_MAXZEILE = "MaxZeile";
	String FIELD_JSFUNKTIONSNAME = "JSFunktionsName";
	String FIELD_INDEX_FIELD_REQUIRED_4_SB_KZ_QUERY = "IndexFieldRequired4SbKzQuery";

	String FIELD_KENNZAHL = "Kennzahl";
	String FIELD_BERECHNUNG_UEBERTRAG = "BerechnungIstUebertrag";

	String GROUP_MAXUSB = "MaxUSB";
	String GROUP_USB_INDEX_FELD = "USBIndexFeld";

	String RULE_REFFELDER = "RefFelder";
}
