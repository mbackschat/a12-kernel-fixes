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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.Container_t;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.IMetaKeysInternal;
import com.mgmtp.a12.kernel.core.rt.a12internal.InvalidKeyException;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaGroup;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaRule;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.internal.utils.IExtmpMetaKeysBase;

/**
 * Implementiert die Schnittstelle IMetaGroup
 */
public class MetaContainer extends MetaEntity implements IMetaGroup {

	private String usbIndexFeldName;
	private ArrayList<IMetaGroup> metaContainerListe = new ArrayList<>();
	private ArrayList<IMetaField> metaFeldListe = new ArrayList<>();
	private ArrayList<IMetaRule> metaRegelListe = new ArrayList<>();
	private List<IMetaField> indexFields = new ArrayList<>();

	// Only needed during initialization
	private List<String> indexFieldNames = new ArrayList<>();

	private static final LinkedHashSet<String> schluessel = new LinkedHashSet<>();
	private static final LinkedHashSet<String> internalKeys = new LinkedHashSet<>();

	static {
		schluessel.add(IMetaKeys.GROUP_FULLNAME);
		schluessel.add(IMetaKeys.GROUP_SHORT_NAME);
		schluessel.add(IMetaKeys.GROUP_MAX_INDEX);
		schluessel.add(IMetaKeys.GROUP_SUBGROUP_LIST);
		schluessel.add(IMetaKeys.GROUP_FIELD_LIST);
		schluessel.add(IMetaKeys.GROUP_RULE_LIST);
		schluessel.add(IMetaKeys.GROUP_INDEX_FIELDS);
		schluessel.add(IMetaKeys.GROUP_ANNOTATION);

		internalKeys.add(IMetaKeysInternal.CONTAINER_MAXINDICES);
	}

	public MetaContainer(Container_t ct) {
		super(schluessel, internalKeys);
		addWert(IMetaKeys.GROUP_FULLNAME, ct.getFullName());
		addWert(IMetaKeys.GROUP_SHORT_NAME, EntityUtil.getEntityName(ct.getFullName()));
		addWert(IMetaKeys.GROUP_MAX_INDEX, ct.getMaxIndex());
		addWert(IMetaKeys.GROUP_SUBGROUP_LIST, metaContainerListe);
		addWert(IMetaKeys.GROUP_FIELD_LIST, metaFeldListe);
		addWert(IMetaKeys.GROUP_RULE_LIST, metaRegelListe);
		// The max indices are set via setMaxIndices
		addWert(IMetaKeysInternal.CONTAINER_MAXINDICES, null);
		// The index fields are added via addFeld
		addWert(IMetaKeys.GROUP_INDEX_FIELDS, indexFields);

		addWert(IMetaKeys.GROUP_ANNOTATION, null); // will be overwritten by additional values if any annotation is provided.

		Set<String> internalKeys = ct.getInternalAdditionalKeys();
		for (String key : ct.getAdditionalKeyValues().keySet()) {
			if (internalKeys.contains(key)) {
				addAdditionalInternalKey(key);
			} else {
				addAdditionalKey(key);
			}
			if (IExtmpMetaKeysBase.GROUP_USB_INDEX_FELD.equals(key)) {
				//zunächst wird nur der Name des USB-Index-Feldes gespeichert
				usbIndexFeldName = (String) ct.getAdditionalKeyValues().get(key);
				//für USB-Vordrucke mit USB-Indexfeld wird der korrekte Wert (ein IMetaField-Objekt)
				//später hinzugefügt. Für die anderen Container ist null bereits der korrekte Wert
				addWert(key, null);
			} else {
				addWert(key, ct.getAdditionalKeyValues().get(key));
			}
		}

		indexFieldNames = Arrays.asList(ct.getIndexFields());
	}

	public String getUSBIndexFeldName() {
		return usbIndexFeldName;
	}

	public void addFeld(IMetaField feld) {
		String feldName = (String) feld.getValue(IMetaKeys.FIELD_SHORT_NAME);
		String fullName = (String) feld.getValue(IMetaKeys.FIELD_FULLNAME);
		if (indexFieldNames.contains(feldName) || indexFieldNames.contains(fullName)) {
			indexFields.add(feld);
		}
		metaFeldListe.add(feld);
	}

	public void addRegel(IMetaRule regel) {
		metaRegelListe.add(regel);
	}

	void addIMetaGroup(IMetaGroup cont) {
		metaContainerListe.add(cont);
	}

	void setMaxIndices(MetaContainer parentContainer, Long maxIndex) {
		List<Long> maxIndices;
		if (parentContainer == null) {
			maxIndices = new ArrayList<>();
		} else {
			@SuppressWarnings("unchecked")
			List<Long> parentIndices = (List<Long>) parentContainer.getValue(IMetaKeysInternal.CONTAINER_MAXINDICES);
			maxIndices = new ArrayList<>(parentIndices);
		}
		maxIndices.add(maxIndex);
		addWert(IMetaKeysInternal.CONTAINER_MAXINDICES, maxIndices);
	}

	/**
	 * Zugriff auf die Werte: für die Werte in Collections werden die Rückgabewerte kopiert.
	 * Für alle anderen Zugriffe wird die Zugriffsmethode der super-Klasse verwendet.
	 *
	 * @throws InvalidKeyException
	 * 		if an invalid key is passed, a runtime exception is thrown
	 */
	@SuppressWarnings("unchecked")
	public Object getValue(String key, Object... parameter) {
		if (IMetaKeys.GROUP_ANNOTATION.equals(key)) {
			return getAnnotation(key, parameter);
		}
		if (IMetaKeys.GROUP_SUBGROUP_LIST.equals(key)) {//kopiere Werte:
			List<IMetaGroup> werte = (List<IMetaGroup>) super.getValue(IMetaKeys.GROUP_SUBGROUP_LIST);
			List<IMetaGroup> result = new ArrayList<>(werte.size());
			for (IMetaGroup m : werte) {
				result.add(m);
			}
			return result;
		}
		if (IMetaKeys.GROUP_RULE_LIST.equals(key)) {//kopiere Werte:
			List<IMetaRule> werte = (List<IMetaRule>) super.getValue(IMetaKeys.GROUP_RULE_LIST);
			List<IMetaRule> result = new ArrayList<>(werte.size());
			for (IMetaRule m : werte) {
				result.add(m);
			}
			return result;
		}
		if (IMetaKeys.GROUP_FIELD_LIST.equals(key)) {//kopiere Werte:
			List<IMetaField> werte = (List<IMetaField>) super.getValue(IMetaKeys.GROUP_FIELD_LIST);
			List<IMetaField> result = new ArrayList<>(werte.size());
			for (IMetaField m : werte) {
				result.add(m);
			}
			return result;
		}
		if (IMetaKeys.GROUP_INDEX_FIELDS.equals(key)) {
			return Collections.unmodifiableList((List<?>) super.getValue(key));
		}
		return super.getValue(key);
	}

}
