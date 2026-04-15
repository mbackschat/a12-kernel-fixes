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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.Regel_t;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.IMetaKeysInternal;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.LocaleUtil;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtIdentifierTemplate;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.InvalidKeyException;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaRule;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.internal.utils.IExtmpMetaKeysBase;

/**
 * Implementiert die Schnittstelle IMetaRule
 */

public class MetaRegel extends MetaEntity implements IMetaRule {
	private static final LinkedHashSet<String> schluessel = new LinkedHashSet<>();
	private static final LinkedHashSet<String> internalKeys = new LinkedHashSet<>();
	private final MetaModelImpl metaModel;

	static {
		schluessel.add(IMetaKeys.RULE_NAME);
		schluessel.add(IMetaKeys.RULE_FULL_NAME);
		schluessel.add(IMetaKeys.RULE_ERROR_CODE);
		schluessel.add(IMetaKeys.RULE_TYPE);
		schluessel.add(IExtmpMetaKeysBase.RULE_REFFELDER);
		schluessel.add(IMetaKeys.RULE_ERROR_FIELD);
		schluessel.add(IMetaKeys.RULE_REF_ROOTGROUP_OMISSION_ERROR);
		schluessel.add(IMetaKeys.RULE_ANNOTATION);

		internalKeys.add(IMetaKeysInternal.REGEL_FEHLERTEXT);
		internalKeys.add(IMetaKeysInternal.REGEL_REFFELDIDS_INTERN);
		internalKeys.add(IMetaKeysInternal.REGEL_REFFELDIDS_AUSLASSUNG_INTERN);
		internalKeys.add(IMetaKeysInternal.REGEL_REFENTITYIDS_INTERN);
		internalKeys.add(IMetaKeysInternal.REGEL_REFENTITYIDS_AUSLASSUNG_INTERN);
	}

	public MetaRegel(MetaModelImpl metaModel, Regel_t r,
			Map<String, List<IMetaField>> refFelder,
			Set<RtInternalIdentifier> refFeldIdsIntern,
			Set<RtInternalIdentifier> refIdentifierAuslassungIntern,
			Set<RtIdentifierTemplate> refEntityIdsIntern,
			Set<RtIdentifierTemplate> refOmissionEntityIdsIntern) {
		super(schluessel, internalKeys);
		this.metaModel = metaModel;
		addWert(IMetaKeys.RULE_NAME, r.getName());
		addWert(IMetaKeys.RULE_FULL_NAME, r.getPfadName());
		addWert(IMetaKeys.RULE_ERROR_CODE, r.getFehlercode());
		addWert(IMetaKeysInternal.REGEL_FEHLERTEXT, r.getFehlertexte());
		addWert(IMetaKeys.RULE_TYPE, r.getRegelArt());
		addWert(IExtmpMetaKeysBase.RULE_REFFELDER, refFelder);
		addWert(IMetaKeys.RULE_ERROR_FIELD,
				metaModel.getValue(IMetaKeys.MODEL_META_FIELD, r.getFehlerFeld()));
		addWert(IMetaKeysInternal.REGEL_REFFELDIDS_INTERN, refFeldIdsIntern);
		addWert(IMetaKeysInternal.REGEL_REFFELDIDS_AUSLASSUNG_INTERN, refIdentifierAuslassungIntern);
		addWert(IMetaKeysInternal.REGEL_REFENTITYIDS_INTERN, refEntityIdsIntern);
		addWert(IMetaKeysInternal.REGEL_REFENTITYIDS_AUSLASSUNG_INTERN, refOmissionEntityIdsIntern);

		addWert(IMetaKeys.RULE_REF_ROOTGROUP_OMISSION_ERROR, r.getRefAuslassungsVordrucke());

		addWert(IMetaKeys.RULE_ANNOTATION, null); // will be overwritten by additional values if any annotation is provided.

		Set<String> internalKeys = r.getInternalAdditionalKeys();
		for (final String key : r.getAdditionalKeys()) {
			if (internalKeys.contains(key)) {
				addAdditionalInternalKey(key);
			} else {
				addAdditionalKey(key);
			}
			addWert(key, r.getAdditionalValue(key));
		}

	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (other.getClass() != getClass()) {
			return false;
		}

		MetaRegel o = (MetaRegel) other;

		return (getValue(IMetaKeys.RULE_FULL_NAME).equals(o.getValue(IMetaKeys.RULE_FULL_NAME)));
	}

	public int hashCode() {
		return getValue(IMetaKeys.RULE_FULL_NAME).hashCode();
	}

	/**
	 * @throws InvalidKeyException
	 * 		if an invalid key is passed, a runtime exception is thrown
	 */
	@SuppressWarnings("unchecked")

	public Object getValue(String key, Object... parameter) {
		if (IMetaKeys.RULE_ANNOTATION.equals(key)) {
			return getAnnotation(key, parameter);
		}
		if (IMetaKeysInternal.REGEL_FEHLERTEXT.equals(key)) {
			Map<Locale, String> fehlertexte = (Map<Locale, String>) super.getValue(key);
			if ((parameter == null) || parameter.length == 0) {
				// Locale.GERMANY ExtMP fallback
				return fehlertexte.get(Locale.GERMANY); // default ist deutsch (wenn keine Sprache als Parameter übergeben)
			} else {
				if (!checkParameterAngegeben(key, 1, parameter)) {
					throw new IllegalArgumentException("The given parameter is invalid.");
				}

				Locale sprache = new LocaleUtil(
						(List<Locale>) metaModel.getValue(IMetaKeys.MODEL_LANGUAGES))
						.deriveLocaleIfPossible(parameter[0],
								String.format("The additional parameter for the key '%s' must be of type Locale.",
										IMetaKeysInternal.REGEL_FEHLERTEXT));
				return fehlertexte.get(sprache);
			}
		}
		return super.getValue(key, parameter);
	}
}
