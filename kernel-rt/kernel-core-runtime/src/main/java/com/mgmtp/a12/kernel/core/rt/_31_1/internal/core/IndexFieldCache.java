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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.core;

import static com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RuntimeUtils.grandParentIndices;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.FormatDefinitionZahl;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.IMetaKeysInternal;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.utils.conversion.a12internal.util.NumberUtil;

/**
 * Caches the index field.
 * For each index group and each iteration outside the index group, the mapping from index field
 * value to numeric iteration index is stored.
 *
 * <p>
 * E.g. an index field /V[1]/K[15]/K[10]/I[1] with following values:
 * <ul>
 * <li>I@1,1,1,1="A"</li>
 * <li>I@1,1,2,1="Test"</li>
 * <li>I@1,1,3,1="B"</li>
 * <li>I@1,2,1,1="B"</li>
 * <li>I@1,2,2,1="C"</li>
 * <li>I@1,2,3,1="Test"</li>
 * </ul>
 * </p>
 *
 * <p>
 * The following cache entries should be added:
 * <ul>
 * <li>{@literal "/V/K/K" -> [1,1] -> "A" -> 1}</li>
 * <li>{@literal "/V/K/K" -> [1,1] -> "Test" -> 2}</li>
 * <li>{@literal "/V/K/K" -> [1,1] -> "B" -> 3}</li>
 * <li>{@literal "/V/K/K" -> [1,2] -> "B" -> 1}</li>
 * <li>{@literal "/V/K/K" -> [1,2] -> "C" -> 2}</li>
 * <li>{@literal "/V/K/K" -> [1,2] -> "Test" -> 3}</li>
 * </ul>
 * </p>
 * <p>
 * When looking up with the values "/V/K/K", [1,2], "Test", one should receive a value of 3.
 */
public class IndexFieldCache {

	private final IMetaDataValidierung metaData;

	private final Map<
			String, // index group full name
			Map< // "index array cache"
					List<Long>, // index group parent repetitions
					Map< // "index mapping for group"
							String, // index field value
							Long // index group repetition
							>
					>
			> cache = new HashMap<>();

	/**
	 * If there are invalid values for an index field, the grandparent indices of the invalid field
	 * are stored in the map. Key is the unique name of the index field.<br>
	 * Note: at the moment we need only to know if there is one invalid index field for each
	 * iteration above the indexed level, so we only store the parent indices of the indexed level
	 * (= grandparent indices of the index field).
	 */
	private final Map<
			String, // index field unique name
			Set<List<Long>> // grandparent indices of invalid index field instance
			> indexFieldUniqueNameToInvalidGrandParentIndices = new HashMap<>();

	public IndexFieldCache(IMetaDataValidierung metaData) {
		this.metaData = metaData;
	}

	/**
	 * Adds the given index field identifier to the index field cache.
	 *
	 * @param id
	 * 		identifier of the index field.
	 * @param newXmlValue
	 * 		given value for the index field.
	 * @param oldXmlValue
	 * 		previous value of the index field.
	 */
	public void setIndexFieldValue(RtInternalIdentifier id, String newXmlValue, String oldXmlValue) {
		String indexFieldUniqueName = id.getName();
		long[] indexFieldIndices = id.getIndices();
		List<Long> indexGroupParentIndices = grandParentIndices(indexFieldIndices);

		String indexFieldFullName = metaData.getPfadName(indexFieldUniqueName);
		String indexGroupFullName = EntityUtil.getParentPath(indexFieldFullName);
		Map<String, Long> indexCache = StringUtils.isNotEmpty(newXmlValue)
				? assuredIndexMappingForGroup(indexGroupFullName, indexGroupParentIndices)
				// If newXmlValue is empty, we only have to remove old mappings from the cache and have
				// no new indexFieldValue -> repetition-index mapping to add
				// ==> if there is no old entry, we don't need to initialize a new one,
				// because we have nothing to do here
				: getIndexMappingForGroup(indexGroupFullName, indexGroupParentIndices);
		if (indexCache == null) {
			// this happens only when newXmlValue was emtpy in the previous step
			// ==> we know that we have neither a new value to add nor an old value to remove => nothing to do
			return;
		}

		long indexGroupRepetition = indexFieldIndices[indexFieldIndices.length - 2];
		// disassociate old value
		if (StringUtils.isNotEmpty(oldXmlValue)) {
			String normalizedOldXmlValue = normalizeValue(indexFieldUniqueName, oldXmlValue);
			// we must test that this value is still associated with this instance, otherwise we might overwrite
			// an entry already updated by another instance
			Long oldRepetition = indexCache.get(normalizedOldXmlValue);
			if (oldRepetition != null && oldRepetition == indexGroupRepetition) {
				indexCache.remove(normalizedOldXmlValue);
			}
		}

		// associate new value
		if (StringUtils.isNotEmpty(newXmlValue)) {
			String normalizedNewXmlValue = normalizeValue(indexFieldUniqueName, newXmlValue);
			indexCache.put(normalizedNewXmlValue, indexGroupRepetition);
		}
	}

	/**
	 * Normalizes the value of the index field. If the values are numbers, we want to do comparison
	 * as if they were numbers.
	 */
	private String normalizeValue(String uniqueFieldName, String xmlValue) {
		IFormatDefinition fd = metaData.getIFormatDefinition(uniqueFieldName);
		if (fd instanceof FormatDefinitionZahl) {
			Object basicVal;
			try {
				basicVal = NumberUtil.createBigDecimal(xmlValue, metaData.getDezimalTrenner());
			} catch (Exception e) {
				// Usually, the values should have been validate by formal validation or code generation
				// and no Exception should be thrown, but EMU doesn't do that and fills in whatever encountered.
				basicVal = null;
			}
			if (basicVal == null) {
				return xmlValue;
			}
			return fd.convertFromBasicType(basicVal, metaData);
		}
		return xmlValue;
	}

	/**
	 * Same as {@link #getIndexMappingForGroup}, but creates an entry in the cache if it doesn't already exist.
	 */
	private Map<String, Long> assuredIndexMappingForGroup(String indexGroupFullName, List<Long> indexGroupParentIndices) {
		Map<List<Long>, Map<String, Long>> indexArrayCache = cache.computeIfAbsent(indexGroupFullName, k -> new HashMap<>());
		return indexArrayCache.computeIfAbsent(indexGroupParentIndices, k -> new HashMap<>());
	}

	/**
	 * Lookup the numeric index of a group for a level with index field given the value of the index
	 * field.
	 *
	 * @param indexGroupFullName
	 * 		the full name of the index group
	 * @param indexGroupParentIndices
	 * 		the indices of the index group's parent.
	 * 		If the index group is a root group, an empty list must be given.
	 * @param xmlValue
	 * 		the value of the index field.
	 * @return the iteration index of the index group for the corresponding value or {@link IIdentifier#UNKNOWN}
	 * 		if there was no entry for this combination of values.
	 */
	public long lookup(String indexGroupFullName, List<Long> indexGroupParentIndices, String xmlValue) {
		Map<String, Long> indexCache = getIndexMappingForGroup(indexGroupFullName, indexGroupParentIndices);
		String indexFieldUniqueName = getIndexFieldUniqueName(indexGroupFullName);
		String normalizedXmlValue = indexFieldUniqueName == null ? xmlValue : normalizeValue(indexFieldUniqueName, xmlValue);
		Long indexGroupRepetition = indexCache == null ? null : indexCache.get(normalizedXmlValue);
		return indexGroupRepetition == null ? IIdentifier.UNKNOWN : indexGroupRepetition.longValue();
	}

	private String getIndexFieldUniqueName(String indexGroupFullName) {
		List<IMetaField> idxFields = metaData.getIndexFields(indexGroupFullName);
		if (idxFields == null || idxFields.size() != 1) {
			return null;
		}
		return (String) idxFields.getFirst().getValue(IMetaKeysInternal.FELD_UNIQUENAME);
	}

	/**
	 * Get the mapping from value of the index field to repetition number.
	 *
	 * @param indexGroupFullName
	 * 		the name of the group containing the index field.
	 * @param indexGroupParentIndices
	 * 		the repetition indices of the parent group of the group denoted by groupName.
	 * @return the mapping from index value to repetition number, or {@code null} if  there is none in the cache yet.
	 */
	public Map<String, Long> getIndexMappingForGroup(String indexGroupFullName, List<Long> indexGroupParentIndices) {
		Map<List<Long>, Map<String, Long>> indexArrayCache = cache.get(indexGroupFullName);
		return indexArrayCache == null ? null : indexArrayCache.get(indexGroupParentIndices);
	}

	/**
	 * @param indexGroupFullName
	 * 		full name of the group in which the index field is contained.
	 * @param indexGroupParentIndices
	 * 		indices of the given group.
	 * @return <code>true</code> if no index fields are filled for the given group, otherwise
	 * 		<code>false</code>.
	 */
	public boolean noIndexFieldsFilled(String indexGroupFullName, List<Long> indexGroupParentIndices) {
		Map<String, Long> indexCache = getIndexMappingForGroup(indexGroupFullName, indexGroupParentIndices);
		return indexCache == null || indexCache.isEmpty();
	}

	public void addInvalidIndexField(RtInternalIdentifier indexFieldId) {
		addInvalidIndexFieldGrandParentIndices(indexFieldId.getName(), grandParentIndices(indexFieldId.getIndices()));
	}

	private void addInvalidIndexFieldGrandParentIndices(String indexFieldUniqueName, List<Long> grandParentIndices) {
		getOrCreateInvalidGrandParentIndices(indexFieldUniqueName).add(grandParentIndices);
	}

	private Set<List<Long>> getOrCreateInvalidGrandParentIndices(String indexFieldUniqueName) {
		return indexFieldUniqueNameToInvalidGrandParentIndices.computeIfAbsent(indexFieldUniqueName, k -> new HashSet<>());
	}

	private boolean isAnyIndexFieldInvalid(String indexFieldUniqueName, List<Long> indexFieldGrandParentIndices) {
		Set<List<Long>> invalidGrandParentIndices = indexFieldUniqueNameToInvalidGrandParentIndices.get(indexFieldUniqueName);
		return invalidGrandParentIndices != null && invalidGrandParentIndices.contains(indexFieldGrandParentIndices);
	}

	public boolean isIndexFieldInvalidForOneRepetition(RtInternalIdentifier indexFieldId) {
		return isAnyIndexFieldInvalid(indexFieldId.getName(), grandParentIndices(indexFieldId.getIndices()));
	}

	/**
	 * @param indexGroupFullName
	 * 		the full name of the index group to test
	 * @param indexGroupParentIndices
	 * 		the indices of the index group's parent, within which to look for invalid repetitions
	 * @return `true`, if the parent instance of the given index group (defined by the given indices) contains one or more
	 * 		repetitions of the index group with an invalid index field.
	 */
	public boolean isIndexGroupInvalidForOneRepetition(String indexGroupFullName, List<Long> indexGroupParentIndices) {
		String uniqueIndexFieldName = this.getIndexFieldUniqueName(indexGroupFullName);
		return uniqueIndexFieldName != null && isAnyIndexFieldInvalid(uniqueIndexFieldName, indexGroupParentIndices);
	}

	public Set<List<Long>> getInvalidIndexGroupParentIndices(String indexGroupFullName) {
		String uniqueIndexFieldName = getIndexFieldUniqueName(indexGroupFullName);
		return uniqueIndexFieldName != null
				? indexFieldUniqueNameToInvalidGrandParentIndices.getOrDefault(uniqueIndexFieldName, Set.of())
				: Set.of();
	}

	public boolean hasInvalidIndexFields() {
		return !indexFieldUniqueNameToInvalidGrandParentIndices.isEmpty();
	}
}
