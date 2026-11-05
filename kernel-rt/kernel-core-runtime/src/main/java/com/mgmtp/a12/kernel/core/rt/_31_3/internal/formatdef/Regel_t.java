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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtIdentifierTemplate;

/**
 * Struktur fuer die Generierung der Laufzeittypinformationen durch den Generator
 * <p>
 * Diese Klasse repräsentiert eine Regel.
 */
public class Regel_t {
	private final String fehlerFeld;
	private final String name;
	private final String pfadName;
	private final String fehlercode;
	private final Map<Locale, String> fehlertexte;
	private final String regelArt;
	private final Identifier_t[] refIdentifier;
	private final Identifier_t[] refAuslassungsIdentifier;
	private final RtIdentifierTemplate[] refEntity;
	private final RtIdentifierTemplate[] refOmissionEntity;
	private final String[] refAuslassungsVordrucke;
	private final boolean isCalculationRule;
	private final boolean dynamicEnumerationRule;

	private final Map<String, Object> additionalKeyValues;
	private final Set<String> internalAdditionalKeys;

	public Regel_t(String name, String pfadName, String fehlercode,
			Map<Locale, String> fehlertexte, String regelArt,
			Identifier_t[] refIdentifier,
			Identifier_t[] refAuslassungsIdentifier,
			RtIdentifierTemplate[] refEntity,
			RtIdentifierTemplate[] refOmissionEntity,
			String[] refAuslassungsVordrucke,
			String fehlerfeld,
			boolean isCalculationRule,
			boolean dynamicEnumerationRule,
			Map<String, Object> additionalKeyValues,
			Set<String> internalAdditionalKeys
	) {
		this.name = name;
		this.pfadName = pfadName;
		this.fehlercode = fehlercode;
		this.fehlertexte = Collections.unmodifiableMap(fehlertexte);
		this.regelArt = regelArt;
		this.refIdentifier = refIdentifier;
		this.refAuslassungsIdentifier = refAuslassungsIdentifier;
		this.refEntity = refEntity;
		this.refOmissionEntity = refOmissionEntity;
		this.refAuslassungsVordrucke = refAuslassungsVordrucke;
		this.fehlerFeld = fehlerfeld;
		this.isCalculationRule = isCalculationRule;
		this.dynamicEnumerationRule = dynamicEnumerationRule;
		this.additionalKeyValues = additionalKeyValues;
		this.internalAdditionalKeys = internalAdditionalKeys;
	}

	public Identifier_t[] getRefAuslassungsIdentifier() {
		return refAuslassungsIdentifier;
	}

	public Set<String> getRefAuslassungsVordrucke() {
		return (refAuslassungsVordrucke == null) ?
				Collections.<String>emptySet() :
				new HashSet<>(Arrays.asList(refAuslassungsVordrucke));
	}

	public String getName() {
		return name;
	}

	public String getPfadName() {
		return pfadName;
	}

	public String getFehlercode() {
		return fehlercode;
	}

	public Map<Locale, String> getFehlertexte() {
		return fehlertexte;
	}

	public String getRegelArt() {
		return regelArt;
	}

	public Identifier_t[] getRefIdentifier() {
		return refIdentifier;
	}

	public RtIdentifierTemplate[] getRefEntity() {
		return refEntity;
	}

	public RtIdentifierTemplate[] getRefOmissionEntity() {
		return refOmissionEntity;
	}

	public String getFehlerFeld() {
		return fehlerFeld;
	}

	public boolean isCalculationRule() {
		return isCalculationRule;
	}

	public boolean isDynamicEnumerationRule() {
		return dynamicEnumerationRule;
	}

	public Object getAdditionalValue(String key) {
		return additionalKeyValues.get(key);
	}

	public Set<String> getInternalAdditionalKeys() {
		return internalAdditionalKeys;
	}

	public Set<String> getAdditionalKeys() {
		return additionalKeyValues.keySet();
	}
}
