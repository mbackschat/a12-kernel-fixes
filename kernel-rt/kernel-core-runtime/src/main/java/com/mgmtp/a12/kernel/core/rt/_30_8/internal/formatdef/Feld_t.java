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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;

/**
 * The code generator creates an instance of this per field. It contains all the meta information
 * related to the field. In the parameter {@code additionalKeyValues} all ExtMP-specific
 * information (no matter the type they are) should be generated.
 */
public class Feld_t {

	private final int interneNr;
	private final Integer[] vordruckPflichtFeldLfdNr;
	private final Integer[] pflichtFeldLfdNr;
	private final String uniqueName;
	private final String pfadName;
	private final long maxZeile;
	private final boolean isPflichtFeld;
	private final boolean isMandatoryForRepeatableGroup;
	private final IFormatDefinition formatDef;
	private final String javascriptName;
	private final boolean berechnungsUebertrag;
	private final Map<String, Object> additionalKeyValues;
	private final Set<String> internalAdditionalKeys;
	private final List<Integer> sbKzRepetition1List;
	private final Map<List<Long>, Pair<String, String>> sbKzMap;
	private final Map<Locale, String> errorTexts4Req;

	public Feld_t(int interneNr, String uniqueName, String pfadName, int maxZeile,
			boolean isPflichtFeld,
			boolean isMandatoryForRepeatableGroup,
			IFormatDefinition formatDef,
			String javaScriptName, Integer[] vordruckPflichtFeldLfdNr, Integer[] pflichtFeldLfdNr,
			Map<String, Object> additionalKeyValues,
			Set<String> internalAdditionalKeys,
			boolean berechnungsUebertrag,
			List<Integer> sbKzRepetition1List,
			Map<List<Long>, Pair<String, String>> sbKzMap,
			Map<Locale, String> errorTexts4Req) {
		this.interneNr = interneNr;
		this.uniqueName = uniqueName;
		this.pfadName = pfadName;
		this.maxZeile = maxZeile;
		this.isPflichtFeld = isPflichtFeld;
		this.isMandatoryForRepeatableGroup = isMandatoryForRepeatableGroup;
		this.formatDef = formatDef;
		this.javascriptName = javaScriptName;
		this.vordruckPflichtFeldLfdNr = vordruckPflichtFeldLfdNr;
		this.pflichtFeldLfdNr = pflichtFeldLfdNr;
		this.additionalKeyValues = additionalKeyValues;
		this.internalAdditionalKeys = internalAdditionalKeys;
		this.berechnungsUebertrag = berechnungsUebertrag;
		this.sbKzRepetition1List = sbKzRepetition1List;
		this.sbKzMap = sbKzMap;
		this.errorTexts4Req = Collections.unmodifiableMap(errorTexts4Req != null ? errorTexts4Req : new HashMap<>());
	}

	/**
	 * This constructor is exclusively for the mapper. In this case we don't generate all the meta
	 * information of a field, but only the necessary for the mapper. The same constraints should be
	 * applied for the parameter {@code additionalKeyValues}.
	 */
	public Feld_t(String uniqueName, String pfadName, IFormatDefinition formatDef, Map<String, Object> additionalKeyValues,
			Set<String> internalAdditionalKeys) {
		this.interneNr = 0;
		this.uniqueName = uniqueName;
		this.pfadName = pfadName;
		this.maxZeile = 0;
		this.isPflichtFeld = false;
		this.isMandatoryForRepeatableGroup = false;
		this.formatDef = formatDef;
		this.javascriptName = null;
		this.vordruckPflichtFeldLfdNr = new Integer[] {};
		this.pflichtFeldLfdNr = new Integer[] {};
		this.additionalKeyValues = additionalKeyValues;
		this.internalAdditionalKeys = internalAdditionalKeys;
		this.berechnungsUebertrag = false;
		this.sbKzRepetition1List = new ArrayList<>();
		this.sbKzMap = new HashMap<>();
		this.errorTexts4Req = new HashMap<>();
	}

	public int getInterneNr() {
		return interneNr;
	}

	public Integer[] getPflichtFeldLfdNr() {
		return pflichtFeldLfdNr;
	}

	public Integer[] getVordruckPflichtFeldLfdNr() {
		return vordruckPflichtFeldLfdNr;
	}

	public String getJavascriptName() {
		return javascriptName;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public String getPfadName() {
		return pfadName;
	}

	public long getMaxZeile() {
		return maxZeile;
	}

	public boolean isPflichtFeld() {
		return isPflichtFeld;
	}

	public boolean isMandatoryForRepeatableGroup() {
		return isMandatoryForRepeatableGroup;
	}

	public IFormatDefinition getFormatDefinition() {
		return formatDef;
	}

	public boolean isBerechnungsUebertrag() {
		return berechnungsUebertrag;
	}

	public Object getAdditionalValue(String key) {
		return additionalKeyValues.get(key);
	}

	public Set<String> getAdditionalKeys() {
		return additionalKeyValues.keySet();
	}

	public List<Integer> getSbKzRepetition1List() {
		return sbKzRepetition1List;
	}

	public Map<List<Long>, Pair<String, String>> getSbKzMap() {
		return sbKzMap;
	}

	public Set<String> getInternalAdditionalKeys() {
		return internalAdditionalKeys;
	}

	public Map<Locale, String> getErrorTexts4Req() {
		return errorTexts4Req;
	}
}
