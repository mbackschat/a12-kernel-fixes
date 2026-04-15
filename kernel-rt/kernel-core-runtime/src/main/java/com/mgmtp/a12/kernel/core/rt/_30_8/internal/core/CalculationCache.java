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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.core;

import java.io.Serial;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DarstellungsInformationIntern;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.DatenImplWithOrigins;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.VkString;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtExternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.util.SemanticIndexSpec;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculationResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

import lombok.NonNull;

/**
 * Erweitert einen ValidationCache um die für die Berechnungen benötigte Information.
 */
public class CalculationCache extends ValidationCache {

	/**
	 * Exception is only used internally to interrupt calculation on calculated values which are
	 * formally invalid.
	 */
	public static class CalculationFieldException extends RuntimeException {
		@Serial
		private static final long serialVersionUID = -5335145233021843202L;
		private final RtInternalIdentifier id;

		private CalculationFieldException(String msg, RtInternalIdentifier id) {
			super(msg);
			this.id = id;
		}

		/**
		 * @return the exception occurred when calculating this runtime internal identifier of the
		 * 		field
		 */
		public RtInternalIdentifier getInternalId() {
			return id;
		}
	}

	protected static class ExtendedWerteContainer {
		private final String feldWertOberflaeche;
		private final VkString feldWertXML;

		protected ExtendedWerteContainer(String feldWertOberflaeche, VkString feldWertXML) {
			assert feldWertOberflaeche != null; // does this hold too?
			assert feldWertXML != null; // this should really hold (I checked the callers that IntelliJ found (but others from
			// gencode are possible) and CalculationCache::getBerechneteFelder might crash otherwise

			this.feldWertOberflaeche = feldWertOberflaeche;
			this.feldWertXML = feldWertXML;
		}

		protected String getFeldWertOberflaeche() {
			return feldWertOberflaeche;
		}

		protected VkString getFeldWertXML() {
			return feldWertXML;
		}
	}

	/**
	 * Eine Map von {@link IIdentifier} auf den berechneten Wert (in XML und Oberflächendarstellung)
	 */
	private final Map<RtInternalIdentifier, ExtendedWerteContainer> calculatedValues = new LinkedHashMap<>();

	private final Set<RtInternalIdentifier> invalidFields = new HashSet<>();
	private Set<RtExternalIdentifier> previouslyFilledCalculatedFields = new HashSet<>();

	public CalculationCache(IMetaDataValidierung metaData, ValidationMode mode, DarstellungsInformationIntern presentationInfo) {
		super(metaData, mode, presentationInfo);
	}

	/**
	 * Fügt den angegebenen Wert für das angegebene Feld hinzu.
	 *
	 * @param f
	 * 		die ID des Feldes.
	 * @param wertOberflaeche
	 * 		der Wert des Feldes in der Oberflächendarstellung.
	 * @param wertXml
	 * 		der Wert des Feldes in der internen Repräsentation.
	 */
	public void addBerechnetenFeldWert(RtInternalIdentifier f, String wertOberflaeche, VkString wertXml) {
		ExtendedWerteContainer wc = new ExtendedWerteContainer(wertOberflaeche, wertXml);
		RtInternalIdentifier id = resolveSemanticIndex(f);
		this.calculatedValues.put(id, wc);
		if (!StringUtils.isEmpty(wertXml.getWert())) {
			setFieldFilled(id);
		}
	}

	/**
	 * Liefert den XML-Wert des Feldes für berechneteFelder oder die Ausgangsdaten oder null falls
	 * es das Feld im Cache nicht gibt.
	 */
	@Override
	public VkString getWertXML(final RtInternalIdentifier f) {
		RtInternalIdentifier id = resolveSemanticIndex(f);
		if (invalidFields.contains(id)) {
			throw createCascadingError(f);
		}
		return getWertXMLOhnePruefung(id);
	}

	private CalculationFieldException createCascadingError(RtInternalIdentifier errorId) {
		return new CalculationFieldException(
				"The calculation of the field returns an invalid value. No further calculation will be performed on this field.",
				errorId);
	}

	private VkString getWertXMLOhnePruefung(final RtInternalIdentifier f) {
		// semantic index in f is already resolved
		if (calculatedValues.containsKey(f)) {
			VkString result = calculatedValues.get(f).getFeldWertXML();
			return new VkString(resolveCategory(f, result.getWert()), result.getOrigins());
		}
		return super.getWertXML(f);
	}

	/**
	 * Bei den Berechnungen sollen auch die berechneten Werte berücksichtigt werden.
	 * Daher wird die Methode existsEntity überschrieben.
	 */
	@Override
	public boolean isEntityFilled(RtInternalIdentifier entity) {
		RtInternalIdentifier id = resolveSemanticIndex(entity);
		if (invalidFields.contains(id)) {
			throw createCascadingError(entity);
		}
		if (calculatedValues.containsKey(id)) {
			return true;
		}
		return super.isEntityFilled(id);
	}

	@Override
	public String getWertOberflaeche(RtInternalIdentifier f) {
		RtInternalIdentifier id = resolveSemanticIndex(f);
		if (calculatedValues.containsKey(id)) {
			return calculatedValues.get(id).getFeldWertOberflaeche();
		}
		return super.getWertOberflaeche(id);
	}

	@Override
	protected RtInternalIdentifier actuallyResolveSemanticIndex(RtInternalIdentifier id, SemanticIndexSpec semIdx) {
		RtInternalIdentifier indexFieldId = getIndexFieldId(id);
		if (indexFieldCache.isIndexFieldInvalidForOneRepetition(indexFieldId)) {
			throw createCascadingError(id);
		}
		return super.actuallyResolveSemanticIndex(id, semIdx);
	}

	@Override
	protected List<String> getIterationValuesForGroups(Map<String, List<Long>> indexGroupFullNameToParentIndices) {
		for (var indexGroupFullNameWithParentIndices : indexGroupFullNameToParentIndices.entrySet()) {
			// no parallel iteration occurs if any of the index group repetitions is invalid
			// the fields that would have been calculated while iterating over such groups are marked as
			// erroneous with CalculationCache#markFieldsUsingInvalidIndexGroupsAsIncorrectlyCalculated
			if (indexFieldCache.isIndexGroupInvalidForOneRepetition(
					indexGroupFullNameWithParentIndices.getKey(), indexGroupFullNameWithParentIndices.getValue())) {
				return List.of();
			}
		}
		return super.getIterationValuesForGroups(indexGroupFullNameToParentIndices);
	}

	/**
	 * Marks the given field as invalid. Subsequent accesses to that field's value will throw a {@link CalculationFieldException}.
	 */
	public void markFieldAsInvalid(RtInternalIdentifier f) {
		RtInternalIdentifier id = resolveSemanticIndex(f);
		this.invalidFields.add(id);
	}

	@Override
	public void addInvalidFieldValue(RtInternalIdentifier id, IIdentifier idExtern) {
		super.addInvalidFieldValue(id, idExtern);
		addPseudoFieldValueIfAbsent(id);
	}

	/**
	 * The formal and preliminary validation gather the entities determined to be invalid in:
	 * - {@link #fehlerhafteFelderIntern} for fields, and
	 * - {@link #erroneousGroups} for groups.
	 * These are then used in {@link #isFehlerhafteEntity} in order to make these entities irrelevant.
	 * <p>
	 * During calculation, however, we currently use a different mechanism to handle erroneous entities. To that end
	 * the invalid entities are converted here so that nothing is treated as irrelevant, but causes exceptions
	 * to be thrown instead (see callers of {@link #createCascadingError}).
	 */
	public void convertInvalidFieldsForCalculation() {
		invalidFields.addAll(fehlerhafteFelderIntern);
		resetErroneousEntitiesForValidation(); // don't treat any entity as "not relevant"
	}

	/**
	 * Clears the erroneous entities used in validation so that they aren't treated as "not relevant".
	 */
	public void resetErroneousEntitiesForValidation() {
		fehlerhafteFelderIntern.clear();
		erroneousGroups.clear();
	}

	/**
	 * Marks existing field instances of `uniqueFieldName` as erroneous calculated fields if during parallel iteration
	 * over `indexGroupFullNames` they would iterate over an invalid index group.
	 * <p>
	 * This method might be marking more field instances as invalid than strictly necessary, but it guarantees to not
	 * leave any actually-problematic field instances unmarked.
	 *
	 * @param uniqueFieldName
	 * 		unique field name of a field calculated with parallel iteration
	 * @param indexGroupFullNames
	 * 		full names of the index groups used in the parallel iteration
	 */
	public void markFieldsUsingInvalidIndexGroupsAsIncorrectlyCalculated(String uniqueFieldName, String[] indexGroupFullNames) {
		assert !metaData.hasUniqueEntities();
		if (!indexFieldCache.hasInvalidIndexFields()) {
			return;
		}

		String fullFieldName = metaData.getPfadName(uniqueFieldName);
		List<RtInternalIdentifier> existingFieldIds = groupFillCache.getExistingFieldInstances(fullFieldName);
		if (existingFieldIds.isEmpty()) {
			return;
		}

		for (String indexGroupFullName : indexGroupFullNames) {
			Set<List<Long>> invalidIndexGroupParentIndices = indexFieldCache.getInvalidIndexGroupParentIndices(indexGroupFullName);
			if (invalidIndexGroupParentIndices.isEmpty()) {
				continue;
			}
			// Example:
			//
			// /shared[10]/normal[10]/iteration/levels[10]/nonrep/indexGroup1/
			// /shared[10]/normal[10]/iteration/levels[10]/nonrep2/indexGroup2/myField
			//
			// Case A: indexGroupFullName is not the group that contains myField
			// =>
			// indexGroupParentFullName = /shared/normal/iteration/levels/nonrep
			// indexGroupParentLevel = 5
			// numSharedLevels = level(/shared/normal/iteration/levels) + 1 = 3 + 1 = 4 // nonrep != nonrep2, so "levels" is the last part of the shared prefix
			//
			// Note: if the current indexGroupFullName was ...indexGroup2, then level would be equal to indexGroupParentLevel because
			//   the field would be _inside_ the indexGroup
			//
			// => numNonRepeatableLevels = 5 - 4 = 1 (the "nonrep" / "nonrep2" groups in the above example)
			// => nonRepeatableIndices = [1]
			// => in the loop below, indexGroupParentIndices will take the indices for /shared/normal/iteration/levels from the field instance,
			//    and the [1, ...] from nonRepeatableIndices
			//
			// Case B: what if the current indexGroupFullName is the index group that contains the current field?
			// =>
			// indexGroupParentFullName = /shared/normal/iteration/levels/nonrep2
			// indexGroupParentLevel = 5
			// numSharedLevels = level(/shared/normal/iteration/levels/nonrep2) + 1 = 4 + 1 = 5
			// numNonRepeatableLevels = 5 - 5 = 0
			// nonRepeatableIndices = []
			// => in the loop below, indexGroupParentIndices will take the indices for /shared/normal/iteration/levels/nonrep2 from the field instance,
			//    and append the empty list [] (0 ones)
			//
			// => if one of the indexGroups has invalidities, we mark all instances of myField that share the same normal-iteration context (see numSharedLevels)
			//    as invalid. This is probably more conservative that strictly necessary, but currently we don't think we need to get too clever about identifying
			//    cases where the field might in fact be totally ok, despite the issues of one of the index groups.
			String indexGroupParentFullName = EntityUtil.getParentPath(indexGroupFullName);
			int indexGroupParentLevel = EntityUtil.countHierarchyLevels(indexGroupParentFullName); // 1-based
			int numSharedLevels = EntityUtil.commonAncestorLevel(fullFieldName,
					indexGroupParentFullName) + 1; // commonAncestorLevel is 0-based -> +1 for one-based
			int numNonRepeatableLevels = Math.max(0, indexGroupParentLevel - numSharedLevels);
			List<Long> nonRepeatableIndices = Collections.nCopies(numNonRepeatableLevels, 1L);

			for (RtInternalIdentifier existingFieldId : existingFieldIds) {
				List<Long> indexGroupParentIndices = Stream.concat(
						Arrays.stream(existingFieldId.getIndices(), 0, numSharedLevels).boxed(),
						nonRepeatableIndices.stream()).toList();
				// note that in order for the hash set to work, the concrete implementation of the List<Long> object matters!
				if (invalidIndexGroupParentIndices.contains(indexGroupParentIndices)) {
					markFieldAsInvalid(existingFieldId);
				}
			}
		}
	}

	/**
	 * Liefert die berechneten Felder als {@link IData}-Objekt.
	 * Als Werte werden die Oberflächenwerte zurückgeliefert.
	 * Die Identifier müssen in die externen Identifier zurückkonvertiert werden.
	 *
	 * @return die berechneten Felder.
	 */
	public @NonNull ICalculationResult getBerechneteFelder() {
		DatenImplWithOrigins result = new DatenImplWithOrigins();

		HashSet<RtExternalIdentifier> calculatedFieldIdsToBeRemoved = new HashSet<>(previouslyFilledCalculatedFields);

		for (Entry<RtInternalIdentifier, ExtendedWerteContainer> e : calculatedValues.entrySet()) {
			RtInternalIdentifier idIntern = e.getKey();
			RtExternalIdentifier idExtern = this.convert2Extern(idIntern);
			calculatedFieldIdsToBeRemoved.remove(idExtern);
			result.addValue(idExtern, e.getValue().getFeldWertOberflaeche());
			Set<RtInternalIdentifier> originsSet = new HashSet<>();
			e.getValue().getFeldWertXML().getOrigins().exportToSetExpensive(originsSet);
			Set<IIdentifier> origins = originsSet.stream()
					.map(this::convert2Extern)
					.collect(Collectors.toSet());
			result.addOrigins(idExtern, origins);
		}

		result.setFieldsToBeCleared(calculatedFieldIdsToBeRemoved);

		return result;
	}

	public void setPreviouslyFilledCalculatedFieldIds(Set<RtExternalIdentifier> previouslyFilledCalculatedFields) {
		this.previouslyFilledCalculatedFields = previouslyFilledCalculatedFields;
	}
}
