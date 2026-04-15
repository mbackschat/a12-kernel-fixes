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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.meta;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.InvalidKeyException;

/**
 * Gemeinsame Basisklasse für alle MetaKlassen.
 */
public abstract class MetaEntity implements IMetaEntityInternal {

	protected static final String ERROR_PARAMETER_NOT_SINGLE_STRING = "For lookups with key '%s', a single parameter of type String" +
			" must be given.";
	private final Map<String, Object> metadaten = new HashMap<>();
	private final LinkedHashSet<String> gueltigeSchluessel;
	private final LinkedHashSet<String> internalKeys;

	public MetaEntity(Set<String> inGueltigeSchluessel, Set<String> internalKeys) {
		gueltigeSchluessel = new LinkedHashSet<>(inGueltigeSchluessel);
		this.internalKeys = new LinkedHashSet<>(internalKeys);
	}

	public void addWert(String schluessel, Object wert) {
		pruefeSchluessel(schluessel);
		metadaten.put(schluessel, wert);
	}

	protected void addAdditionalKey(String key) {
		gueltigeSchluessel.add(key);
	}

	protected void addAdditionalInternalKey(String key) {
		internalKeys.add(key);
	}

	protected void removeKey(String key) {
		gueltigeSchluessel.remove(key);
	}

	private void pruefeSchluessel(String schluessel) {
		if (!gueltigeSchluessel.contains(schluessel) && !internalKeys.contains(schluessel)) {
			throw new InvalidKeyException(schluessel, this);
		}
	}

	/**
	 * @throws InvalidKeyException
	 * 		if an invalid key is passed, a runtime exception is thrown
	 */
	public Object getValue(String key, Object... parameter) {
		return getValueIntern(key);
	}

	private Object getValueIntern(String key) {
		if (metadaten.containsKey(key)) {
			return metadaten.get(key);
		} else {
			throw new InvalidKeyException(key, this);
		}
	}

	protected Object getAnnotation(String key, Object... parameter) {
		if (parameter == null || parameter.length != 1 || !(parameter[0] instanceof String antKey)) {
			throw new IllegalArgumentException(String.format(ERROR_PARAMETER_NOT_SINGLE_STRING, key));
		}

		Object ants = getValueIntern(key);
		if (!(ants instanceof Map)) {
			return null;
		}

		@SuppressWarnings("unchecked") Map<String, Object> antMap = (Map<String, Object>) ants;

		if (!antMap.containsKey(antKey)) {
			return null;
		}

		return Optional.ofNullable(antMap.get(antKey)).orElse("");
	}

	public String[] getValidKeys() {
		return gueltigeSchluessel.toArray(new String[gueltigeSchluessel.size()]);
	}

	public Set<String> getValidKeysInternal() {
		return internalKeys;
	}

	/**
	 * Überprüft ob dass die optionalen Parameter angegeben sind
	 * Es wird unterschieden ob einer der Parameter gar nicht angegeben ist,
	 * oder ob null für ihn angegeben ist. In Beiden Fällen wird false
	 * zurückgegeben bzw. eine Assert Exception geworfen.
	 *
	 * @param key
	 * 		wird nur zur Log-ausgabe verwendet
	 * @param anzahlParameter
	 * 		Anzahl der erwarteten Parameter
	 * @param parameter
	 * 		zu prüfender Parameter.
	 * @return Ergebnis der Prüfung
	 */
	protected boolean checkParameterAngegeben(String key, int anzahlParameter, Object... parameter) {
		assert parameter != null && parameter.length == anzahlParameter : String.format(
				"For key '%s' exactly %d parameters have to be passed.", key, anzahlParameter);
		if (parameter == null || parameter.length < anzahlParameter) {
			// wir tolerieren, dass zuviel Parameter angegeben wurden.
			return false;
		}
		for (int i = 0; i < parameter.length; i++) {

			assert parameter[i] != null : String.format("The additional parameter for key '%s' must not be null.", key);
			if (parameter[i] == null) {
				return false;
			}
		}
		return true;
	}

	protected void checkNumberOfParameters(int num, Object... parameter) {
		if (parameter == null || parameter.length != num) {
			throw new IllegalArgumentException(String.format("The number of additional parameters must be %s.", num));
		}
	}

	protected void checkMinNumberOfParameters(int min, Object... parameter) {
		if (parameter == null || parameter.length < min) {
			throw new IllegalArgumentException(String.format("The number of additional parameters must be at least %s.", min));
		}
	}

	protected void checkNoParameters(Object... parameter) {
		if (parameter != null && parameter.length > 0) {
			throw new IllegalArgumentException("No additional parameters may be passed.");
		}
	}

}
