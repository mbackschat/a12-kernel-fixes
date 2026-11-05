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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum;

import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.bedingung.KontextListenBedingungCreator;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.creation.creators.operation.FeldListenAnzahlOpCreator;

/**
 * ExpandMode definiert, ob expandiert werden soll, wenn der ParseBaum erzeugt wird.
 */
public enum ExpandMode {

	/**
	 * No change for groups or language constructs.
	 * But some group language constructs when used for root groups are created as root group constructs in the parse tree (see
	 * {@link KontextListenBedingungCreator} and {@link FeldListenAnzahlOpCreator}).
	 */
	NO_EXPAND,
	/**
	 * Like {@link #NO_EXPAND}, but the group constructs that {@link #NO_EXPAND} turns into root group constructs are kept as
	 * ordinary group constructs.
	 * The mode should be used when a rule or computation is parsed in the context of an include, which changes the root group
	 * to an ordinary group.
	 * It should not be used for code generation or similar for real root groups.
	 */
	NO_EXPAND_OR_RG_REPLACEMENT,
	/**
	 * Kontexte werden durch ihre Feldlisten ersetzt (bei dem Sprachkonstrukt
	 * AnzahlAngegebenerKontexte wird zusätzlich der Kontext der jeweiligen Feldliste
	 * vorangestellt). Kontext-Sprachkonstrukte werden durch die FeldListen-Sprachkonstrukte
	 * ersetzt.
	 * Vordrucke werden nicht expandiert. Wenn Vordrucke in Kontext-Sprachkonstrukten verwendet
	 * werden, werden diese intern (d.h. im Parsebaum) als Vordruck-Sprachkonstrukte behandelt.
	 */
	EXPAND,
	/**
	 * For language constructs other than group constructs, groups are expanded to field lists.
	 * Root groups will not be expanded. They cannot be used in language constructs other than
	 * group constructs and will be treated as root group constructs.
	 */
	EXPAND_PARTIALLY,
	/**
	 * For language constructs other than group constructs, the behavior is equal to the
	 * behavior of {@link #EXPAND}.
	 * For group constructs, the entity lists resulting from the expansion of groups
	 * additionally contain the groups themselves and the groups contained in these expanded
	 * groups.
	 */
	EXPAND_AND_ADD_GROUPS;

	/**
	 * Convenience method to limit the given expansion mode to {@link ExpandMode#EXPAND}. I.e
	 * {@link ExpandMode#EXPAND_AND_ADD_GROUPS} and {@link ExpandMode#EXPAND_PARTIALLY} will be
	 * replaced with {@link ExpandMode#EXPAND}. The method is used to eliminate the additional
	 * expansion behavior for group constructs.
	 *
	 * @param expandMode
	 * 		the defined global expansion mode.
	 * @return the resulting expansion mode.
	 */
	public static ExpandMode limitToExpand(ExpandMode expandMode) {
		switch (expandMode) {
			case NO_EXPAND:
				return NO_EXPAND;
			case NO_EXPAND_OR_RG_REPLACEMENT:
				return NO_EXPAND_OR_RG_REPLACEMENT;
			case EXPAND:
				return EXPAND;
			case EXPAND_AND_ADD_GROUPS:
				return EXPAND;
			case EXPAND_PARTIALLY:
				return EXPAND;
			default:
				throw new IllegalArgumentException("Unknown expansion mode " + expandMode);
		}
	}
}
