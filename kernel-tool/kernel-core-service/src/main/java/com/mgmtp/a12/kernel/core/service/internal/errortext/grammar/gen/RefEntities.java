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

package com.mgmtp.a12.kernel.core.service.internal.errortext.grammar.gen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.parser.internal.util.ReferencedIdsUtil;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FeldIdUtilities;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;

/**
 * a class to manage how entities are referenced by a condition
 */
class RefEntities {

	private final String rulePath;

	record FullName(String path) {
	}

	/**
	 * the key used in refEntities, composed of a fullName and possibly semantic index (also maybe using a path)
	 */
	record RefEntitiesKey(FullName path, Optional<SemanticIndexSpec> semanticIndex) {
		static RefEntitiesKey of(FullName fullName, SemanticIndexSpec semanticIndex) {
			return new RefEntitiesKey(fullName, Optional.ofNullable(semanticIndex));
		}

		static RefEntitiesKey of(FullName fullName) {
			return new RefEntitiesKey(fullName, Optional.empty());
		}

	}

	public record SemanticIndexSpec(String specification) {
		public static SemanticIndexSpec of(String specification) {
			return new SemanticIndexSpec(specification);
		}
	}

	private Map<RefEntitiesKey, List<IEntityId>> refEntities;
	private final Set<IEntityId> allRefIndexFields;

	RefEntities(ICondition condition, IEC entityContainer,
			String rulePath,
			boolean isHModel) {

		this.allRefIndexFields = new HashSet<>();
		this.rulePath = rulePath;
		Set<? extends IEntityId> refIds = ReferencedIdsUtil.getReferencedEntitiesWithRootGroups(condition, entityContainer,
				allRefIndexFields);

		if (refIds != null) {
			Map<RefEntitiesKey, List<IEntityId>> ret = new HashMap<>();
			for (IEntityId id : refIds) {
				var fieldNameKey = new FullName(id.getFullName());
				SemanticIndexSpec semIndex = null;
				if (id.getSemanticIndex() != null && !id.getSemanticIndex().isEmpty()) {
					if (id.getSemanticIndex().size() > 1) {
						throw new IllegalArgumentException("Currently only one semantic index is allowed!");
					}
					if (isHModel) {
						throw new IllegalArgumentException("Semantic index is not allowed in H-DVs!");
					}
					semIndex = SemanticIndexSpec.of(ParserUtils.getSemanticIndexRepresentation(id.getSemanticIndex().getFirst()));
				}
				var refEntitiesKey = semIndex != null ? RefEntitiesKey.of(fieldNameKey, semIndex) : RefEntitiesKey.of(fieldNameKey);
				Java8Utils.addToListMultiMap(ret, refEntitiesKey, id);
			}
			this.refEntities = ret;
		}
	}

	public Set<IEntityId> getAllRefIndexFields() {
		return allRefIndexFields;
	}

	boolean isNotReferencedWithoutWildcard(FullName absolutePath) {

		var rawPath = absolutePath.path();
		String absolutePathWithSlash = ParserUtils.getWithTrailingSlash(rawPath);
		//given name references a group and the group, a subgroup or a field in the group is referenced in the
		//condition. check if the group, field or subgroup is referenced without a wildcard in a relevant repetition layer.
		return refEntities.values().stream()
				.flatMap(List::stream)
				.noneMatch(id -> (id.getFullName() + ElementPathUtils.SEPARATOR).startsWith(
						absolutePathWithSlash) && !FeldIdUtilities.hasWildCardInPath(id,
						rawPath));
	}

	record GroupOrSemIndexResult(boolean hasValidGroupReference, boolean containsSemanticIndexRef) {
	}

	GroupOrSemIndexResult hasValidGroupRefOrContainsSemanticIndexRef(
			FullName fullName, boolean isIndexGroup) {
		String pathWithSlash = ParserUtils.getWithTrailingSlash(fullName.path());
		boolean containsSemanticIndexRef = false;
		for (RefEntities.RefEntitiesKey refEntity : refEntities.keySet()) {
			var refEntityName = refEntity.path();
			if (refEntity.semanticIndex.isPresent()) {
				containsSemanticIndexRef = true;
				if (isIndexGroup) {
					continue;
				}
			}
			if (refEntityName.path.startsWith(pathWithSlash) || //has valid group
					(refEntityName.path.equals(fullName.path()) && containsSemanticIndexRef) //contains semantic index
			) {
				return new GroupOrSemIndexResult(true, containsSemanticIndexRef);
			}
		}
		return new GroupOrSemIndexResult(false, containsSemanticIndexRef);
	}

	boolean hasValidGroupReference(FullName fullName,
			RefEntities.SemanticIndexSpec semanticIndex,
			boolean isIndexGroup) {
		if (semanticIndex != null) {
			return hasValidGroupReferenceWithSemanticIndex(fullName, semanticIndex, isIndexGroup);
		} else {
			return hasValidGroupReference(fullName, isIndexGroup);
		}
	}

	/**
	 * A group fullName is valid, if
	 * <ol>
	 * <li>the condition references the fullName itself and it is a group</li>
	 * <li>the condition references a field in the group</li>
	 * <li>the fullName is an ancestor fullName of the rule group and does not contain an index field or the
	 * condition references no fields with semantic index</li>
	 * </ol>
	 * If the group for the fullName contains an index field, the references are regarded as positive
	 * only if it does not contain a semantic index.
	 *
	 * @param fullName
	 * 		the fullName to the relevant group.
	 * @param isIndexGroup
	 * 		does the group for the fullName contain an index field?
	 * @return <code>true</code> if the fullName may be specified in an error text without semantic
	 * 		index.
	 */
	private boolean hasValidGroupReference(FullName fullName,
			boolean isIndexGroup) {

		var result = hasValidGroupRefOrContainsSemanticIndexRef(fullName, isIndexGroup);
		if (result.hasValidGroupReference())
			return true;

		String pathWithSlash = ParserUtils.getWithTrailingSlash(fullName.path());
		if ((rulePath + "/").startsWith(pathWithSlash)) {
			return !isIndexGroup || !result.containsSemanticIndexRef();
		}
		return false;
	}

	/**
	 * Precondition: the fullName is a group containing index fields.
	 * A group fullName with a semantic index is valid, if the condition
	 * <ul>
	 * <li>references the fullName</li>
	 * <li>references a field within the fullName</li>
	 * </ul>
	 * with the specified semantic index.
	 *
	 * @param fullName
	 * 		the fullName to the relevant group.
	 * @param semanticIndex
	 * 		the semantic index specified in the parameter
	 * @param isIndexGroup
	 * 		does the group for the fullName contain an index field?
	 */
	boolean hasValidGroupReferenceWithSemanticIndex(FullName fullName,
			RefEntities.SemanticIndexSpec semanticIndex,
			boolean isIndexGroup) {
		if (!isIndexGroup) {
			return false;
		}
		var refEntitiesKey = RefEntitiesKey.of(fullName, semanticIndex);
		if (refEntities.containsKey(refEntitiesKey)) {
			return true;
		}

		if (pathsEqualIgnoreTrailingSlash(fullName.path(), rulePath)) {
			// The rule fullName is referenced, if the field is relatively referenced
			var relativeReferences = refEntities.entrySet().stream().filter(kvp
					-> containsFirstChoiceRelativeReference(kvp.getValue()));

			return relativeReferences.map(Map.Entry::getKey).anyMatch(key ->
					matches(key, fullName, semanticIndex));
		}
		String pathWithSlash = fullName.path() + ElementPathUtils.SEPARATOR;

		return refEntities.keySet().stream().anyMatch(k -> k.path.path.startsWith(pathWithSlash)
				&& k.semanticIndex.isPresent() && k.semanticIndex.get().equals(semanticIndex));
	}

	List<IEntityId> tryGetFor(FullName fullName,
			RefEntities.SemanticIndexSpec semanticIndex) {
		return refEntities.get(RefEntitiesKey.of(fullName, semanticIndex));
	}

	private static boolean containsFirstChoiceRelativeReference(List<IEntityId> iEntityIds) {
		return
				iEntityIds.stream().map(iEntityId -> iEntityId.getPathNotationConfig().firstChoice()).anyMatch(
						firstChoice -> firstChoice.equals(PathNotationConfig.Type.RELATIVE)
				);
	}

	private static boolean matches(RefEntitiesKey key,
			FullName fullName, SemanticIndexSpec semanticIndexSpec) {
		var semIndexMatch =
				(key.semanticIndex.isPresent() && key.semanticIndex.get().specification().startsWith(
						semanticIndexSpec.specification
				));

		return semIndexMatch && key.path().path.startsWith(fullName.path);
	}

	static boolean pathsEqualIgnoreTrailingSlash(String pathOne, String pathTwo) {
		var pathOneTrimmed = ParserUtils.trimTrailingSlash(pathOne);
		var pathTwoTrimmed = ParserUtils.trimTrailingSlash(pathTwo);
		return pathOneTrimmed.equals(pathTwoTrimmed);
	}
}
