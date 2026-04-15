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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.IDynEnumsIntern;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.error.FehlerHandler;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.formatdef.error.RuntimeFormalErrorEnum;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.sorter.SortOrder;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DateUtil;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.IAnnotationNamesInternal;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.IMetaKeysInternal;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.MetaDataUtilValidierung;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.PresentationInformationValues;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtIdentifierTemplate;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.TrieNode;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaGroup;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaRule;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

import lombok.NonNull;

/**
 * Diese Implementierung von IMetaDataValidierung dient dem Zugriff auf Methoden des Packages
 * 'runtime' aus dem Package 'meta'.
 */
public class MetaDataValidierungIntern implements IMetaDataValidierung {

	private static final String FIELDS_DO_NOT_CONTAIN = "The set of fields does not contain <%s>.";

	private final @NonNull MetaModelImpl metaModel;
	private final char dezimalTrenner;
	private final int[][] legalCodepoints;
	private final TrieNode legalGraphemeTrie;
	private final @NonNull Set<String> usbIndexFelder;
	private final Date baseYearAsDate;
	private final boolean isExtmp;
	private final boolean isHModel;

	private final String baseYearAsString;
	private final String modelId;

	private final @NonNull Map<String, Long> felderUnqNameXmaxUsb;
	private final @NonNull Map<String, Long> felderUnqNameMaxZeile;

	private final Map<Locale, PresentationInformationValues> defaultPresentationInfo;

	private final Supplier<IDynEnumsIntern> dynEnumSupplier;

	public MetaDataValidierungIntern(
			@NonNull MetaModelImpl meta,
			char dezimalTrenner,
			int[][] legalCodepoints,
			TrieNode legalGraphemeTrie,
			@NonNull Set<String> usbIndexFelder,
			@NonNull Map<String, Long> felderUnqNameXmaxUsb,
			@NonNull Map<String, Long> felderUnqNameMaxZeile,
			Map<Locale, PresentationInformationValues> defaultPresentationInfo,
			boolean isExtmp,
			Supplier<IDynEnumsIntern> dynEnumSupplier
	) {
		this.dezimalTrenner = dezimalTrenner;
		this.legalCodepoints = legalCodepoints;
		this.legalGraphemeTrie = legalGraphemeTrie;
		this.usbIndexFelder = usbIndexFelder;
		this.metaModel = meta;
		this.felderUnqNameXmaxUsb = felderUnqNameXmaxUsb;
		this.felderUnqNameMaxZeile = felderUnqNameMaxZeile;
		this.defaultPresentationInfo = defaultPresentationInfo;

		/*
		 * the following are additional, project specific properties so we check if the keys are
		 * valid. If not, we define default values
		 */
		List<String> validKeys = new ArrayList<>(List.of(metaModel.getValidKeys()));
		validKeys.addAll(metaModel.getValidKeysInternal());
		if (validKeys.contains(IMetaKeysInternal.MODEL_BASE_YEAR)) {
			this.baseYearAsString = (String) metaModel.getValue(IMetaKeysInternal.MODEL_BASE_YEAR);
		} else {
			this.baseYearAsString = "";
		}
		if (!StringUtils.isEmpty(baseYearAsString)) {
			this.baseYearAsDate = DateUtil.createDate(1, 1, Integer.parseInt(baseYearAsString), this.getTimeZone());
		} else {
			this.baseYearAsDate = DateUtil.createDate(1, 1, 0, this.getTimeZone());
		}
		this.modelId = (String) metaModel.getValue(IMetaKeysInternal.MODEL_ID);
		this.isExtmp = isExtmp;
		this.isHModel = MetaDataUtilValidierung.isHModel(metaModel);
		this.dynEnumSupplier = dynEnumSupplier;
	}

	// ===========================================================
	// Die folgenden Methoden dienen zur internen Verwendung
	// in der Runtime.
	// ===========================================================

	public String getBaseYearAsString() {
		return this.baseYearAsString;
	}

	public char getDezimalTrenner() {
		return dezimalTrenner;
	}

	public int getBaseYearOrDefault() {
		return !StringUtils.isEmpty(getBaseYearAsString()) ? Integer.parseInt(getBaseYearAsString()) : 2000;
	}

	public String getPfadName(String uniqueName) {
		if (isUniqueFieldName(uniqueName)) {
			IMetaField feld = getMetaFieldByUniqueName(uniqueName);
			return (String) feld.getValue(IMetaKeys.FIELD_FULLNAME);
		} else {
			IMetaGroup group = getMetaGroup(uniqueName);
			return (String) group.getValue(IMetaKeys.GROUP_FULLNAME);
		}
	}

	public String getUniqueName(String name) {
		IMetaField feld = (IMetaField) metaModel.getValue(IMetaKeys.MODEL_META_FIELD, name);
		if (feld == null) {
			// if the field is not there, it may be a group, whose unique name is the full name
			if (isGroupDefined(name)) {
				return name;
			}
			throw new IllegalArgumentException(String.format(FIELDS_DO_NOT_CONTAIN, name));
		}
		return (String) feld.getValue(IMetaKeysInternal.FELD_UNIQUENAME);
	}

	@Override
	public boolean hasUniqueEntities() {
		return isHModel;
	}

	public boolean isFeldDefiniert(String name) {
		IMetaField feld = (IMetaField) metaModel.getValue(IMetaKeys.MODEL_META_FIELD, name);
		return (feld != null);
	}

	public boolean isGroupDefined(String fullName) {
		IMetaGroup group = getMetaGroupOrNull(fullName);
		return (group != null);
	}

	@SuppressWarnings("unchecked")
	public List<Long> getMaxFieldIndices(IMetaField field) {
		return (List<Long>) field.getValue(IMetaKeysInternal.FELD_MAXINDICES);
	}

	public List<Long> getMaxFieldIndices(String uniqueName) {
		IMetaField field = getMetaFieldByUniqueName(uniqueName);
		return getMaxFieldIndices(field);
	}

	@Override
	public List<Long> getMaxGroupIndices(String name) {
		IMetaGroup group = getMetaGroup(name);
		@SuppressWarnings("unchecked")
		List<Long> maxIndices = (List<Long>) group.getValue(IMetaKeysInternal.CONTAINER_MAXINDICES);
		return maxIndices;
	}

	public IMetaField getMetaField(String name) {
		IMetaField field = (IMetaField) metaModel.getValue(IMetaKeys.MODEL_META_FIELD, name);
		if (field == null) {
			throw new IllegalArgumentException(String.format(FIELDS_DO_NOT_CONTAIN, name));
		}
		return field;
	}

	private IMetaField getMetaFieldByUniqueName(String uniqueName) {
		IMetaField field = (IMetaField) metaModel.getValue(IMetaKeysInternal.MODEL_METAFELDUNIQUE, uniqueName);
		if (field == null) {
			throw new IllegalArgumentException(String.format(FIELDS_DO_NOT_CONTAIN, uniqueName));
		}
		return field;
	}

	@Override
	public boolean isUniqueFieldName(String uniqueFeldName) {
		IMetaField feld = (IMetaField) metaModel.getValue(IMetaKeysInternal.MODEL_METAFELDUNIQUE, uniqueFeldName);
		return feld != null;
	}

	private IMetaGroup getMetaGroup(String name) {
		IMetaGroup group = getMetaGroupOrNull(name);
		if (group == null) {
			throw new IllegalArgumentException("The set of groups does not contain <" + name + ">.");
		}
		return group;
	}

	public List<IMetaField> getIndexFields(String name) {
		IMetaGroup group = getMetaGroup(name);
		@SuppressWarnings("unchecked")
		List<IMetaField> result = (List<IMetaField>) group.getValue(IMetaKeys.GROUP_INDEX_FIELDS);
		return result;
	}

	public boolean hasIndexFields() {
		return (boolean) metaModel.getValue(IMetaKeys.MODEL_INDEX_FIELDS);
	}

	public boolean isIndexField(String uniqueFeldName) {
		if (!isUniqueFieldName(uniqueFeldName)) {
			return false;
		}
		IMetaField field = getMetaFieldByUniqueName(uniqueFeldName);
		return (boolean) field.getValue(IMetaKeys.FIELD_INDEX_FIELD);
	}

	public boolean isTransientField(String uniqueFieldName) {
		IMetaField field = getMetaFieldByUniqueName(uniqueFieldName);
		return (boolean) field.getValue(IMetaKeys.FIELD_TRANSIENT);
	}

	public int getInterneFeldNr(String uniqueName) {
		IMetaField feld = getMetaFieldByUniqueName(uniqueName);
		return (Integer) feld.getValue(IMetaKeysInternal.FELD_INTERNENR);
	}

	@Override
	public int[][] getLegalCodepoints() {
		return this.legalCodepoints;
	}

	@Override
	public TrieNode getLegalGraphemeTrie() {
		return this.legalGraphemeTrie;
	}

	@Override
	public boolean isLegalCharCheckDeactivated() {
		return metaModel.getValue(IMetaKeys.MODEL_ANNOTATION, IAnnotationNamesInternal.MODEL_DEACTIVATE_LEGALCHARCHECK) != null;
	}

	public @NonNull Set<String> getUSBIndexFelder() {
		return usbIndexFelder;
	}

	public Date getBaseYearAsDate() {
		return baseYearAsDate;
	}

	public long getMaxUsb(String uniqueFeldname) {
		Long erg = felderUnqNameXmaxUsb.get(uniqueFeldname);
		if (erg == null) {
			return IIdentifier.USB_NOT_SPECIFIED;
		}
		return erg;
	}

	public long getMaxZeile(String uniqueFeldName) {
		Long erg = felderUnqNameMaxZeile.get(uniqueFeldName);
		if (erg == null) {
			return 0;
		}
		return erg;
	}

	public IFormatDefinition getIFormatDefinition(String uniqueName) {
		return metaModel.getIFormatDefinition(uniqueName);
	}

	public String getFehlertext(String regelName, Locale sprache) {
		IMetaRule metaRegel = (IMetaRule) metaModel.getValue(IMetaKeys.MODEL_META_RULE, regelName);
		return (String) metaRegel.getValue(IMetaKeysInternal.REGEL_FEHLERTEXT, sprache);
	}

	@Override public String getErrorMessage4Req(String fieldFullName, Locale locale) {
		IMetaField metaField = getMetaField(fieldFullName);
		// the key FIELD_CUSTOM_ERROR_MESSAGE_FOR_REQUIRED does not consider index fields which were not explicitly marked as required
		boolean isImplicitlyRequired = (boolean) metaField.getValue(IMetaKeys.FIELD_INDEX_FIELD)
				&& !(boolean) metaField.getValue(IMetaKeys.FIELD_EXPLICITLY_MANDATORY);
		if (isImplicitlyRequired) {
			return FehlerHandler.getFehlertext(RuntimeFormalErrorEnum.PFLICHT_FELD_FEHLER,
					metaModel.getMetaDataValidierung().isExtmp(), locale);
		}
		return (String) metaField.getValue(IMetaKeys.FIELD_CUSTOM_ERROR_MESSAGE_FOR_REQUIRED, locale);
	}

	@SuppressWarnings("unchecked")
	public Set<IMetaField> getBerechneteFelder() {
		return (Set<IMetaField>) metaModel.getValue(IMetaKeys.MODEL_CALC_FIELDS);
	}

	@Override
	public Map<Locale, PresentationInformationValues> getDefaultPresentationInformation() {
		return defaultPresentationInfo;
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<Pair<String, SortOrder>>> getSortInfo() {
		return (Map<String, List<Pair<String, SortOrder>>>) metaModel.getValue(IMetaKeysInternal.MODEL_SORT_INFO);
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<Pair<String, SortOrder>>> getSortInfo4Mapper() {
		return (Map<String, List<Pair<String, SortOrder>>>) metaModel.getValue(
				IMetaKeysInternal.MODEL_SORTIER_INFO_4_MAPPER);
	}

	@Override
	public Set<RtInternalIdentifier> getReferencedFieldsWithoutReplacement(String ruleName) {
		IMetaRule metaRegel = (IMetaRule) metaModel.getValue(IMetaKeys.MODEL_META_RULE, ruleName);
		@SuppressWarnings("unchecked")
		Set<RtInternalIdentifier> result = (Set<RtInternalIdentifier>) metaRegel.getValue(
				IMetaKeysInternal.REGEL_REFFELDIDS_INTERN);
		return result;
	}

	@Override
	public Set<RtInternalIdentifier> getReferencedOmissionFieldsWithoutReplacement(String ruleName) {
		IMetaRule metaRegel = (IMetaRule) metaModel.getValue(IMetaKeys.MODEL_META_RULE, ruleName);
		@SuppressWarnings("unchecked")
		Set<RtInternalIdentifier> result = (Set<RtInternalIdentifier>) metaRegel.getValue(
				IMetaKeysInternal.REGEL_REFFELDIDS_AUSLASSUNG_INTERN);
		return result;
	}

	@Override
	public Set<RtIdentifierTemplate> getReferencedEntitiesWithoutReplacement(String ruleName) {
		IMetaRule metaRegel = (IMetaRule) metaModel.getValue(IMetaKeys.MODEL_META_RULE, ruleName);
		@SuppressWarnings("unchecked")
		Set<RtIdentifierTemplate> result = (Set<RtIdentifierTemplate>) metaRegel.getValue(
				IMetaKeysInternal.REGEL_REFENTITYIDS_INTERN);
		return result;
	}

	@Override
	public Set<RtIdentifierTemplate> getReferencedOmissionEntitiesWithoutReplacement(String ruleName) {
		IMetaRule metaRegel = (IMetaRule) metaModel.getValue(IMetaKeys.MODEL_META_RULE, ruleName);
		@SuppressWarnings("unchecked")
		Set<RtIdentifierTemplate> result = (Set<RtIdentifierTemplate>) metaRegel.getValue(
				IMetaKeysInternal.REGEL_REFENTITYIDS_AUSLASSUNG_INTERN);
		return result;
	}

	@Override
	public boolean isExtmp() {
		return isExtmp;
	}

	@Override
	public boolean isExplicitlyMandatory(String uniqueName) {
		IMetaField metaField = (IMetaField) metaModel.getValue(IMetaKeys.MODEL_META_FIELD, uniqueName);
		return (boolean) metaField.getValue(IMetaKeys.FIELD_EXPLICITLY_MANDATORY);
	}

	@Override
	public Set<String> getContainedElements(String groupName) {
		Set<String> result = new HashSet<>();
		result.addAll(getContainedGroups(groupName));
		result.addAll(getContainedFields(groupName));
		return result;
	}

	@Override
	public Set<String> getContainedGroups(String groupName) {
		IMetaGroup metaContainer = getMetaGroupOrNull(groupName);
		if (metaContainer == null) {
			return Collections.emptySet();
		}
		@SuppressWarnings("unchecked")
		List<IMetaGroup> groups = (List<IMetaGroup>) metaContainer.getValue(IMetaKeys.GROUP_SUBGROUP_LIST);

		Set<String> result = new HashSet<>(groups.size());
		for (IMetaGroup group : groups) {
			String name = (String) group.getValue(IMetaKeys.GROUP_FULLNAME);
			result.add(name);
		}
		return result;
	}

	@Override
	public IMetaGroup getMetaGroupOrNull(String groupName) {
		return (IMetaGroup) metaModel.getValue(IMetaKeys.MODEL_META_GROUP, groupName);
	}

	@Override
	public Set<String> getContainedFields(String groupName) {
		IMetaGroup metaContainer = getMetaGroupOrNull(groupName);
		if (metaContainer == null) {
			return Collections.emptySet();
		}
		@SuppressWarnings("unchecked")
		List<IMetaField> fields = (List<IMetaField>) metaContainer.getValue(IMetaKeys.GROUP_FIELD_LIST);

		Set<String> result = new HashSet<>(fields.size());
		for (IMetaField field : fields) {
			String name = (String) field.getValue(IMetaKeysInternal.FELD_UNIQUENAME);
			result.add(name);
		}
		return result;
	}

	@Override
	public String getInnermostRepeatableGroup(String entityFullName) {
		String path = entityFullName;
		IMetaGroup metaGroup;
		do {
			path = EntityUtil.getParentPath(path);
			metaGroup = (IMetaGroup) metaModel.getValue(IMetaKeys.MODEL_META_GROUP, path);
		} while (metaGroup != null && ((Long) metaGroup.getValue(IMetaKeys.GROUP_MAX_INDEX)) == 1);
		return StringUtils.isEmpty(path) ? null : path;
	}

	@Override
	public IDynEnumsIntern getDynamicEnumerationCalculator() {
		return dynEnumSupplier.get();
	}

	@Override
	public Set<String> getRequiredCustomConditions() {
		@SuppressWarnings("unchecked")
		Set<String> result = (Set<String>) metaModel.getValue(IMetaKeys.MODEL_APPLICATION_CONDITION);
		if (result == null) {
			return Collections.emptySet();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getIndexFieldFullNames() {
		return (List<String>) metaModel.getValue(IMetaKeys.MODEL_INDEX_FIELD_FULL_NAMES);
	}

	@Override
	public Set<String> getFieldFullNames() {
		return metaModel.getFieldFullNames();
	}

	@Override
	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone((String) metaModel.getValue(IMetaKeysInternal.MODEL_TIME_ZONE));
	}

	@Override
	public String getModelId() {
		return modelId;
	}

	@Override public IMetaModel getMetaModel() {
		return metaModel;
	}
}
