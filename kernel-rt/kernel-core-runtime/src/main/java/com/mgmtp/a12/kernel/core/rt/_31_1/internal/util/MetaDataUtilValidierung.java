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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.FormatDefinitionString;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.FormatDefinitionZahl;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.meta.IMetaEntityInternal;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.meta.MetaContainer;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaEntity;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.internal.utils.IExtmpMetaKeysBase;

/**
 * Hilfsklasse für alle Utility Methoden bezogen auf ein Feld.
 */
public class MetaDataUtilValidierung {

	private MetaDataUtilValidierung() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Prüft, ob das Feld zum angegebenen Feldnamen ein Zahl-Feld ist.
	 */
	public static boolean isNumberFeld(String name, IMetaDataValidierung meta) {
		return meta.getIFormatDefinition(name) instanceof FormatDefinitionZahl;
	}

	/**
	 * Gibt die Länge des Index-Arrays für einen Identifier zurück.
	 * D.h. laufende Nummer Vordruck wird mit zurückgegeben, usb aber
	 * nicht.
	 */
	public static int getAnzahlWiederholungsEbenen(String uniqueName, IMetaDataValidierung meta) {
		return meta.getMaxFieldIndices(uniqueName).size();
	}

	public static long getMaxKontextEbenenIndex(String uniqueName, int kontextEbene, IMetaDataValidierung meta) {
		return meta.getMaxFieldIndices(uniqueName).get(kontextEbene);
	}

	public static long getMaxUSB(MetaContainer c) {
		Long value = null;
		if (isValidKey(c, IExtmpMetaKeysBase.GROUP_MAXUSB)) {
			value = (Long) c.getValue(IExtmpMetaKeysBase.GROUP_MAXUSB);
		}
		return value == null ? IIdentifier.USB_NOT_SPECIFIED : value.longValue();

	}

	public static boolean isHModel(IMetaModel metaModel) {
		//IExtmpMetaKeysBase.A12DV is a project specific key so we check if it is a valid key
		if (isValidKey(metaModel, IExtmpMetaKeysBase.MODEL_A12DV)) {
			return !((Boolean) metaModel.getValue(IExtmpMetaKeysBase.MODEL_A12DV));
		}
		return false; // if the key is not valid we are in A12
	}

	public static boolean isTransientField(IMetaField metafield) {
		if (isValidKey(metafield, IMetaKeys.FIELD_TRANSIENT)) {
			return (boolean) metafield.getValue(IMetaKeys.FIELD_TRANSIENT);
		}
		return true;

	}

	@SuppressWarnings("unchecked")
	public static Map<List<Long>, Pair<String, String>> getSbKzMap(IMetaField field) {
		if (isValidKey(field, IMetaKeysInternal.FELD_SB_KZ)) {
			return (Map<List<Long>, Pair<String, String>>) field.getValue(IMetaKeysInternal.FELD_SB_KZ);
		}
		return new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	public static List<Integer> getSbKzRepetition1List(IMetaField field) {
		if (isValidKey(field, IMetaKeysInternal.FELD_SB_KZ_REPETITION_1)) {
			return (List<Integer>) field.getValue(IMetaKeysInternal.FELD_SB_KZ_REPETITION_1);
		}
		return new ArrayList<>();
	}

	public static IMetaField getIndexField4SbKzQuery(IMetaField field) {
		if (isValidKey(field, IExtmpMetaKeysBase.FIELD_INDEX_FIELD_REQUIRED_4_SB_KZ_QUERY)) {
			return (IMetaField) field.getValue(IExtmpMetaKeysBase.FIELD_INDEX_FIELD_REQUIRED_4_SB_KZ_QUERY);
		}
		return null;
	}

	public static boolean isValidKey(IMetaEntity metaEntity, String key) {
		if (metaEntity instanceof IMetaEntityInternal) {
			return isValidKeyInternal((IMetaEntityInternal) metaEntity, key);
		}
		return Arrays.asList(metaEntity.getValidKeys()).contains(key);
	}

	private static boolean isValidKeyInternal(IMetaEntityInternal metaEntity, String key) {
		return Arrays.asList(metaEntity.getValidKeys()).contains(key) || metaEntity.getValidKeysInternal().contains(key);
	}

	public static String getValue4Category(IMetaDataValidierung meta, String fieldUniqueName, String xmlValue,
			String categoryName) {
		FormatDefinitionString format = (FormatDefinitionString) meta.getIFormatDefinition(fieldUniqueName);
		return format.getValue4Category(xmlValue, categoryName);
	}
}
