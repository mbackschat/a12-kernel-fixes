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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.sorter.SortOrder;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;

/**
 * Struktur fuer die Generierung der Laufzeittypinformationen durch den Generator
 * <p>
 * Beschreibt die grundlegenden Attribute des Modells.
 */
public class Model_t {
	private final String erVersion;
	private final char dezimalTrenner;
	private final List<Locale> unterstuetzteSprachen;
	private final Set<String> applBedingungen;
	private final Set<String> customFieldTypes;
	private final Map<String, List<Pair<String, SortOrder>>> sortInfo;
	private final Map<String, Object> additionalKeyValues;
	private final Set<String> internalAdditionalKeys;

	public Model_t(String erVersion,
			char dezimalTrenner, List<Locale> unterstuetzteSprachen, Set<String> applBedingungen,
			Set<String> customFieldTypes,
			Map<String, List<Pair<String, SortOrder>>> sortInfo,
			Map<String, Object> additionalKeyValues,
			Set<String> internalAdditionalKeys) {
		this.erVersion = erVersion;
		this.dezimalTrenner = dezimalTrenner;
		this.additionalKeyValues = additionalKeyValues;
		this.internalAdditionalKeys = internalAdditionalKeys;
		this.unterstuetzteSprachen = unterstuetzteSprachen;
		this.applBedingungen = applBedingungen;
		this.customFieldTypes = customFieldTypes;
		this.sortInfo = sortInfo;
	}

	public String getErVersion() {
		return erVersion;
	}

	public char getDezimalTrenner() {
		return dezimalTrenner;
	}

	public List<Locale> getUnterstuetzteSprachen() {
		return unterstuetzteSprachen;
	}

	public Set<String> getApplbedingungen() {
		return applBedingungen;
	}

	public Set<String> getCustomFieldTypes() {
		return customFieldTypes;
	}

	public Map<String, Object> getAdditionalKeyValues() {
		return additionalKeyValues;
	}

	public Object getAdditionalValue(String key) {
		return additionalKeyValues.get(key);
	}

	public Map<String, List<Pair<String, SortOrder>>> getSortInfo() {
		return sortInfo;
	}

	public Set<String> getInternalAdditionalKeys() {
		return internalAdditionalKeys;
	}
}
