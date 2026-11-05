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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.ValidationCache;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * Hilfsklasse für alle Utility Methoden bezogen auf die Entitäten Feld, Kontext, Vordruck.
 */
public class EntityUtil {

	public static final String SEPARATOR_CHAR = "/";

	private EntityUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean isAncestor(String groupFullName, String entityFullName) {
		if (groupFullName.endsWith(SEPARATOR_CHAR)) {
			return entityFullName.startsWith(groupFullName);
		} else {
			return entityFullName.startsWith(groupFullName + SEPARATOR_CHAR);
		}
	}

	/**
	 * Returns the level of the given path.
	 * <p>
	 * 0 for a root group, 1 for a group below the root group,
	 * etc.
	 */
	public static int getIndexOfHierarchyLevel(String path) {
		return countHierarchyLevels(path) - 1;
	}

	/**
	 * Returns the number of hierarchy levels of the given path.
	 * <p>
	 * 1 for root group, 2 for a group below the root group etc.
	 *
	 * @param path
	 * @return
	 */
	public static int countHierarchyLevels(String path) {
		return StringUtils.countMatches(path, SEPARATOR_CHAR);
	}

	/**
	 * Returns the path for the given hierarchy level.
	 * <p>
	 * root group for 0, a group below the root group for 1 etc.
	 * <p>
	 * Example: getIndexOfHierarchyLevel(getPathOfHierarchyLevel(fullPath, level)) == level
	 *
	 * @param fullPath
	 * 		The full path of which a partial path should be extracted
	 * @param level
	 * 		The level of the path returned
	 * @return
	 */
	public static String getPathOfHierarchyLevel(String fullPath, int level) {
		int pos = fullPath.indexOf(SEPARATOR_CHAR, 1);
		if (pos < 0) {
			if (level == 0) {
				return fullPath;
			} else {
				throw new IllegalArgumentException("Not enough levels in path");
			}
		}
		while (level-- > 0) {
			pos = fullPath.indexOf(SEPARATOR_CHAR, pos + 1);
			if (pos < 0) {
				if (level == 0) {
					return fullPath;
				} else {
					throw new IllegalArgumentException("Not enough levels in path");
				}
			}
		}
		return fullPath.substring(0, pos);
	}

	/**
	 * For the full name of a field or group return the full name of the parent group.
	 *
	 * @param fullName
	 * 		the full name of the field or group.
	 * @return the full name of the parent group.
	 */
	public static String getParentPath(String fullName) {
		return fullName.substring(0, fullName.lastIndexOf(SEPARATOR_CHAR));
	}

	public static String getRestPath(String fullName) {
		if (!fullName.startsWith("/")) {
			throw new IllegalArgumentException("fullName must start with '/'");
		}
		return fullName.substring(getRootGroupFullName(fullName).length());
	}

	/**
	 * @param fullName
	 * 		the full name of the field or group.
	 * @return the short name of the field or group.
	 */
	public static String getEntityName(String fullName) {
		return fullName.substring(fullName.lastIndexOf(SEPARATOR_CHAR) + 1);
	}

	/**
	 * @param fullName
	 * 		the full name of the field or group.
	 * @return the full name of the root group (with leading slash).
	 */
	public static String getRootGroupFullName(String fullName) {
		int nextSlashIndex = fullName.indexOf(SEPARATOR_CHAR, 1);
		return nextSlashIndex != -1
				? fullName.substring(0, nextSlashIndex)
				: fullName;
	}

	/**
	 * @param fullName
	 * 		the full name of the field or group.
	 * @return the name of the root group (without leading slash).
	 */
	public static String getRootGroupName(String fullName) {
		int index = fullName.indexOf(SEPARATOR_CHAR, 1);
		if (index != -1) {
			return fullName.substring(1, index);
		} else {
			return fullName.substring(1);
		}
	}

	/**
	 * @param fullName
	 * 		the full name of the field or group.
	 * @return the full name of the entity with the rootGroupName replaced.
	 */
	public static String replaceRootGroupName(String fullName, String rootGroupFullName) {
		return rootGroupFullName + fullName.substring(fullName.indexOf(SEPARATOR_CHAR, 1), fullName.length());
	}

	/**
	 * @param rootGroupName
	 * 		the name of the root group (without leading slash)
	 * @return the path / full name of the root group
	 */
	public static String getRootGroupPath(String rootGroupName) {
		return SEPARATOR_CHAR + rootGroupName;
	}

	/**
	 * helper method to build a fullName or a relative name
	 *
	 * @return concatenates path and toAdd, or simply returns toAdd if path is empty.
	 */
	public static String createPath(String path, String toAdd) {
		if (path.isEmpty()) {
			return toAdd;
		}
		return String.format("%s%s%s", path, SEPARATOR_CHAR, toAdd);
	}

	/**
	 * @param absPathA
	 * 		must not end with '/', and must start with '/' unless empty.
	 * 		e.g. "/A/B/C", "/A" and "" are valid, but "/A/B/" and "/" are not.
	 * @param absPathB
	 * 		must not end with '/', and must start with '/' unless empty
	 * @return the 'index' of the level of common ancestors, can be -1, which means there are no common ancestors
	 */
	public static int commonAncestorLevel(String absPathA, String absPathB) {
		String[] partsA = splitAbsPath(absPathA);
		String[] partsB = splitAbsPath(absPathB);

		int lowerLevel = Math.min(partsA.length, partsB.length);
		for (int level = 0; level < lowerLevel; level++) {
			if (!Objects.equals(partsA[level], partsB[level])) {
				return level - 1;
			}
		}
		return lowerLevel - 1;
	}

	private static String[] splitAbsPath(String absPath) {
		if (StringUtils.isEmpty(absPath)) {
			return new String[0];
		} else if (!absPath.startsWith(SEPARATOR_CHAR)) {
			throw new IllegalArgumentException(
					"illegal abspath: '%s' (abspath must start with '/' unless they are empty)".formatted(absPath));
		} else {
			return absPath.substring(1).split(SEPARATOR_CHAR);
		}
	}

	/**
	 * Prüft ob der Identifier <code>id</code> prüfungsrelevant bezogen auf den übergebenen
	 * relevanteFelder Cache
	 *
	 * @param id
	 * 		zu prüfender Identifier, alle Wiederholungen müssen in jeder Ebene konkret sein.
	 * @param relevanteFelderCache
	 * 		prüfungsrelevante Felder
	 * @return true falls der Identifier prüfungsrelevant ist, false sonst.
	 */
	public static boolean isPruefungsRelevant(
			RtInternalIdentifier id,
			Map<String, Set<RtInternalIdentifier>> relevanteFelderCache) {
		// keine Teilvalidierung -> immer prüfungsrelevant
		if (relevanteFelderCache == null) {
			return true;
		}
		return relevanteEntitiesUmfassenIdentifier(relevanteFelderCache, id);
	}

	/**
	 * Prüft, ob der Identifier <code>id</code> prüfungsrelevant ist.<br>
	 * <p>
	 * An identifier is regarded as relevant if:
	 * <ul>
	 * <li>it is not erroneous,</li>
	 * <li>if it has a semantic index defined by an entity: the semantic index is relevant (and not
	 * erroneous),</li>
	 * <li>if it has a semantic index: the corresponding index field is relevant (and not
	 * erroneous),</li>
	 * <li>if an index of <code>id</code> is {@link IIdentifier#UNKNOWN}: the corresponding index
	 * field is relevant (and not erroneous) for all repetitions, and</li>
	 * <li>it is a full validation or in case of a partial validation: the <code>id</code> is
	 * contained in the relevant fields</li>
	 * </ul>
	 *
	 * <b>Precondition</b>: The indices of <code>id</code> have to be concrete,
	 * {@link IIdentifier#SEMANTIC_INDEX} or {@link IIdentifier#UNKNOWN}. An index
	 * {@link IIdentifier#PARALLEL_ITERATION} has to be replaced by
	 * {@link IIdentifier#SEMANTIC_INDEX}
	 * before calling the method. In case of a semantic index, it is resolved to check if the
	 * identifier is relevant.
	 */
	public static boolean isPruefungsRelevant(RtInternalIdentifier id, ValidationCache vc, boolean partialIsUnknown) {
		if (id.getSemanticIndex() == null) {
			return isIdWithoutSemanticIndexRelevant(id, vc, partialIsUnknown);
		}
		SemanticIndexSpec semIdx = id.getSemanticIndex();
		if (semIdx.isEntity() && !isIdWithoutSemanticIndexRelevant(semIdx.getAsEntity(), vc, partialIsUnknown)) {
			return false;
		}
		//index field of id with the same indices up to the indexed level, semantic index resolved
		RtInternalIdentifier indexFieldId = vc.resolveSemanticIndex(vc.getIndexFieldId(id));
		if (!isIdWithoutSemanticIndexRelevant(indexFieldId, vc, partialIsUnknown)) {
			return false;
		}
		RtInternalIdentifier idWithSemanticIndexResolved = vc.resolveSemanticIndex(id);
		if (isUnknown(idWithSemanticIndexResolved) && isIndexfieldNotRelevantForAllRepetitions(indexFieldId, vc)) {
			return false;
		}
		return isIdWithoutSemanticIndexRelevant(idWithSemanticIndexResolved, vc, partialIsUnknown);
	}

	/**
	 * <p>
	 * Checks for an <code>id</code> without semantic index if it is relevant, i.e. not erroneous
	 * and, in the case of partial validation, contained in the set of relevant fields considering
	 * the special meaning of {@link IIdentifier#ALL_INDICES} for a level.
	 * </p>
	 * <p>
	 * The indices of <code>id</code> may be concrete or {@link IIdentifier#UNKNOWN}, but nothing
	 * else, i.e. the following indices are <b>NOT</b> allowed:<br>
	 * {@link IIdentifier#ALL_INDICES }, {@link IIdentifier#SEMANTIC_INDEX }, {@link
	 * IIdentifier#PARALLEL_ITERATION } and {@link IIdentifier#ITERATION}.<br>
	 * In case of {@link IIdentifier#UNKNOWN} the field is only detected as relevant if it is with
	 * {@link IIdentifier#ALL_INDICES} for the respective level in the relevant fields (and not
	 * if each single repetition is listed as relevant).
	 * </p>
	 */
	private static boolean isIdWithoutSemanticIndexRelevant(RtInternalIdentifier id, ValidationCache vc, boolean partialIsUnknown) {
		if (vc.isFehlerhafteEntity(id, partialIsUnknown)) {
			return false;
		}
		if (!vc.isValidationPartWith3ValueLogic()) {
			// For full validation, all fields are relevant
			return true;
		}
		boolean isRelevant = relevanteEntitiesUmfassenIdentifier(vc.getPruefungsrelevanteEntitiesIntern(), id);
		if (!partialIsUnknown || !isRelevant) {
			// If relevant is enough or if it is not even relevant, we know the relevance
			return isRelevant;
		}
		if (!isGroup(id)) {
			return true;
		}
		return relevanteEntitiesUmfassenIdentifier(vc.getFullyRelevantGroupsIntern(), id);
	}

	private static boolean isUnknown(RtInternalIdentifier id) {
		return ArrayUtils.indexOf(id.getIndices(), IIdentifier.UNKNOWN) != -1;
	}

	private static boolean isIndexfieldNotRelevantForAllRepetitions(RtInternalIdentifier indexFieldId, ValidationCache vc) {
		if (vc.isIndexFieldInvalidForOneRepetition(indexFieldId)) {
			return true;
		}
		long[] indices = Arrays.copyOf(indexFieldId.getIndices(), indexFieldId.getIndices().length);
		indices[indices.length - 2] = IIdentifier.ALL_INDICES;
		RtInternalIdentifier indexFieldIdWithAllIndices = new RtInternalIdentifier(indexFieldId.getName(), indices);
		if (vc.isValidationPartWith3ValueLogic() && !relevanteEntitiesUmfassenIdentifier(vc.getPruefungsrelevanteEntitiesIntern(),
				indexFieldIdWithAllIndices)) {
			return true;
		}
		return false;
	}

	/**
	 * Prüft, ob der übergebene Identifier von der Menge der relevanten Felder umfasst wird. Die
	 * Bedeutung von 'umfasst' ist in {@link RtInternalIdentifier#umfasst(RtInternalIdentifier)}
	 * beschrieben.
	 *
	 * @param id
	 * 		- zu prüfender Identifier, normalerweise immer konkret. Falls für einen Index
	 *        {@link IIdentifier#ALL_INDICES} spezifiziert wurde, wird das Feld nur als
	 * 		prüfungsrelevant erkannt, wenn es auch mit {@link IIdentifier#ALL_INDICES} in den
	 * 		relevanten Feldern übergeben wurde (und nicht die Indices einzeln aufgezählt
	 * 		wurden).
	 * @param relevanteFelder
	 * 		- Map mit allen relevanten Feldern, key ist der Feldname, value die Menge der
	 * 		relevanten Felder mit diesem Namen. Die Identifier können auch
	 *        {@link IIdentifier#ALL_INDICES} auf einer Wiederholungsebene spezifizieren
	 * @return <code>true</code> falls ein relevantes Feld die id umfasst
	 */
	private static boolean relevanteEntitiesUmfassenIdentifier(Map<String, Set<RtInternalIdentifier>> relevanteFelder,
			RtInternalIdentifier id) {
		Set<RtInternalIdentifier> relevanteFelderMitGleichemNamen = relevanteFelder.get(id.getName());

		// noch nicht mal ein Feld mit gleichem Namen gefunden --> gib sofort false zurück
		if (relevanteFelderMitGleichemNamen == null) {
			return false;
		}

		// in den relevanten Feldern ist ein Identifier mit exakt den gesuchten Wiederholungsindices gefunden
		if (relevanteFelderMitGleichemNamen.contains(id)) {
			return true;
		}

		//prüfe für jeden relevanten Identifier, ob er den zu prüfenden Identifier umfasst.
		for (RtInternalIdentifier relId : relevanteFelderMitGleichemNamen) {
			if (relId.umfasst(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a layer specification obtained from layerSpecsWithUsb when all iteration layers (i.e.
	 * layers with spec {@link IIdentifier#ITERATION}) are replaced by the corresponding concrete
	 * value from iterationStateWithUsb.
	 *
	 * @param layerSpecsWithUsb
	 * 		index-ranges for layers in the group hierarchy. An index range can either be given
	 * 		by a pair of start- and end-index integers, or by a special value like
	 *        {@link IIdentifier#ITERATION}.
	 * @param iterationStateWithUsb
	 * 		as returned form {@link IterationState}.getIndices. If layerSpecsWithUsb has no
	 * 		entries with {@link IIdentifier#ITERATION}, iterationStateWithUsb may be null
	 * 		<p>
	 * 		Note: iterationStateWithUsb may apparently be from a different subtree than
	 * 		layerSpecsWithUsb, e.g. [[-2 - -2], [5 - 10], [-1 - -1]] and [-2, 1, 1] is a
	 * 		combination that occurs in the Java validation tests
	 */
	public static List<RtIdentifierTemplateLayerSpec> resolveIterations(List<RtIdentifierTemplateLayerSpec> layerSpecsWithUsb,
			long[] iterationStateWithUsb) {
		List<RtIdentifierTemplateLayerSpec> result = new ArrayList<>(layerSpecsWithUsb.size());
		for (int i = 0; i < layerSpecsWithUsb.size(); i++) {
			long start = layerSpecsWithUsb.get(i).getIdxStart();
			long end = layerSpecsWithUsb.get(i).getIdxEnd();
			if (start == IIdentifier.ITERATION) {
				if (iterationStateWithUsb == null || iterationStateWithUsb.length <= i) {
					throw new IllegalArgumentException(
							"given iteration state is insufficient to resolve all iterations in given layer spec");
				}
				start = end = iterationStateWithUsb[i];
			}
			result.add(new RtIdentifierTemplateLayerSpec(start, end));
		}
		return result;
	}

	public static boolean isGroup(RtInternalIdentifier id) {
		return uniqueNameIsGroup(id.getName());
	}

	public static boolean isGroup(RtIdentifierTemplate tpl) {
		return uniqueNameIsGroup(tpl.getUniqueName());
	}

	private static boolean uniqueNameIsGroup(String uniqueName) {
		return uniqueName.startsWith("/");
	}

	public static boolean isRootGroup(RtInternalIdentifier id) {
		return uniqueNameIsRootGroup(id.getName());
	}

	public static boolean isRootGroup(RtIdentifierTemplate id) {
		return uniqueNameIsRootGroup(id.getUniqueName());
	}

	private static boolean uniqueNameIsRootGroup(String uniqueName) {
		return uniqueNameIsGroup(uniqueName) && !uniqueName.substring(1).contains(SEPARATOR_CHAR);
	}

	public static boolean isField(RtInternalIdentifier id) {
		return !isGroup(id);
	}

	/**
	 * Erzeugt einen Identifier mit dem intern verwendeten 'uniqueName' von id.
	 * Die Indices und der usb sind die gleichen wie bei id.
	 */
	public static RtInternalIdentifier makeInternIdentifier(IIdentifier id, IMetaDataValidierung meta) {
		String eindeutigerName = meta.getUniqueName(id.getName());
		return new RtInternalIdentifier(eindeutigerName, id.getIndices(), id.getUsb());
	}

	public static String getFullName(String uniqueName, IMetaModel meta) {
		IMetaField metaField = (IMetaField) meta.getValue(IMetaKeys.MODEL_META_FIELD, uniqueName);
		if (metaField == null) {
			throw new IllegalArgumentException("Unknown field " + uniqueName);
		}
		return (String) metaField.getValue(IMetaKeys.FIELD_FULLNAME);
	}

	/**
	 * Return the path of a root group, i.e. a slash followed by the name of the root group.
	 */
	public static String createRootGroupPath(String rootGroupName) {
		return SEPARATOR_CHAR + rootGroupName;
	}
}
