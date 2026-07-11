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

package com.mgmtp.a12.kernel.core.parser.internal.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.EntityIdImpl;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IGroupOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfRootGroupsOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ISetOpOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.GroupOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;

public class ReferencedIdsUtil {

	private ReferencedIdsUtil() {
		throw new IllegalArgumentException("This is a utility class and must not be instantiated.");
	}

	/**
	 * Enum to specify the mode how to determine the referenced {@link IEntityId}s in a parse tree
	 * entity. It affects entities with a filter condition, i.e. the language construct 'Mit'.
	 */
	public enum ReferencedIdsMode {
		IGNORE_SET_EXPRESSIONS,
		USE_SET_EXPRESSION_REPLACE_INNER_BY_WILDCARD
	}

	private static class ReferencedEntityVisitor extends ASTVisitor {
		Set<IEntityId> entityIds = new HashSet<>();
		Set<IEntityId> allRefIndexFields = new HashSet<>();
		private final IEC ec;
		private final boolean addIndexFields;
		private final boolean addRootGroups;
		private final Predicate<IEntityId> filter;
		private final ReferencedIdsMode refIdsMode;

		/**
		 * the following two fields are only relevant when determining the referenced fields for
		 * meta rules in the generated code, s. Method
		 * {@link #getReferencedIds4MetaReferences}
		 */
		private boolean inRepetitionNotUnique = false;
		private int levelToStartDuplicateSearch = -1;

		/**
		 * @param addIndexFields
		 * 		specifies whether index fields should be added to the list before
		 * 		filtering.
		 * @param addRootGroups
		 * 		specifies, whether root groups should be added to the gathered referenced entities.
		 * @param ec
		 * 		the entity container, the index fields are retrieved from. May be
		 * 		<code>null</code> if addIndexFields is <code>false</code>.
		 * @param refIdsMode
		 * 		specifies how to treat filter conditions.
		 * @param filter
		 * 		filter for entities.
		 */
		public ReferencedEntityVisitor(boolean addIndexFields, boolean addRootGroups, IEC ec, ReferencedIdsMode refIdsMode,
				Predicate<IEntityId> filter) {
			this.ec = ec;
			this.addIndexFields = addIndexFields;
			this.filter = filter;
			this.refIdsMode = refIdsMode;
			this.addRootGroups = addRootGroups;
		}

		@Override
		public boolean visit(IEntityListCondition c) {
			if (c.getConditionName() == EntityListConditionName.RepetitionNotUnique) {
				inRepetitionNotUnique = true;
			}
			return true;
		}

		@Override
		public void endVisit(IEntityListCondition c) {
			if (c.getConditionName() == EntityListConditionName.RepetitionNotUnique) {
				inRepetitionNotUnique = false;
			}
			// additionalGroupReference does not matter for referenced fields
			// Note: it does matter for move/rename-support, but that appears to be based on
			// ParserService.getDirectlyReferencedEntities (via server's feldverwendung/kontextverwendung?), and that method
			// does not use a visitor but the parser's entityMap
		}

		@Override
		public boolean visit(IEntityId e) {
			addEntityToResult(e);

			handleEntityAdditions(e);
			return false;
		}

		private void handleEntityAdditions(IEntityId e) {
			if (addIndexFields) {
				Set<IEntityId> indexFieldReferences = createAllIndexFieldEntityIds(e, ec);
				allRefIndexFields.addAll(indexFieldReferences);
			}

			ReferencedIdsUtil.AdditionalRefsResult semIdxRefs = getEntitiyReferencesIfSemanticIndex(e, ec, addIndexFields);
			semIdxRefs.all().forEach(this::addEntityToResult);
			// a field reference from semantic index can itself have parallel iteration
			// It might be cleaner to call handleEntityAdditions instead of getIndexFieldIfParallelIteration,
			// but in order to be more robust against stack-overflow errors, we'll call getIndexFieldIfParallelIteration +
			// addEntityToResult directly
			semIdxRefs.semIdxRef().flatMap(semE -> getIndexFieldIfParallelIteration(semE, ec, addIndexFields))
					.ifPresent(this::addEntityToResult);

			getIndexFieldIfParallelIteration(e, ec, addIndexFields).ifPresent(this::addEntityToResult);

			if (e.getFilterCondition() != null && refIdsMode != ReferencedIdsMode.IGNORE_SET_EXPRESSIONS) {
				ReferencedEntityVisitor filterVisitor = new ReferencedEntityVisitor(addIndexFields, addRootGroups, ec, refIdsMode,
						filter);
				e.getFilterCondition().traverse(filterVisitor);
				Set<? extends IEntityId> ids = filterVisitor.getEntityIds();
				if (refIdsMode == ReferencedIdsMode.USE_SET_EXPRESSION_REPLACE_INNER_BY_WILDCARD) {
					ids = transformFilterConditionIds(filterVisitor.getEntityIds(), e);
				}
				entityIds.addAll(ids);
			}
		}

		@Override
		public boolean visit(IRootGroupCondition rgc) {
			IEntityId groupReference = rgc.getEntityId();
			if (addRootGroups) {
				if (groupReference == null) {
					groupReference = createEntityForRootGroupCondition(rgc, ec);
				}
				addEntityToResult(groupReference);
				handleEntityAdditions(groupReference);
			} else if (groupReference != null) { // For A12-models we still may add index fields
				handleEntityAdditions(groupReference);
			}
			return false;
		}

		@Override
		public boolean visit(IGroupOperation gop) {
			if (gop.getOperatorName() == GroupOperationName.CurrentRepetition) {
				// for root groups stop here, currently they are never considered as referenced entities
				if (!addRootGroups && !EntityUtils.isRootGroup(gop.getGroup().getFullName())) {
					return true;
				}
				handleEntityAdditions(gop.getGroup());
				return false;
			} else {
				throw new IllegalArgumentException("Unknown IGroupOperation '" + gop.getOperatorName() + "'.");
			}
		}

		@Override public void endVisit(INumberOfRootGroupsOperation norgo) {
			if (addRootGroups) {
				addEntityToResult(createEntityForNumberOfRootGroupCondition(norgo, ec));
				// No semantic index possible (only rootGroup*)
			}
		}

		@Override public boolean visit(ISetOpOperation setOp) {
			setOp.getIterationEntity().traverse(this);
			if (refIdsMode == ReferencedIdsMode.IGNORE_SET_EXPRESSIONS) {
				return false;
			}
			ReferencedEntityVisitor setExpressionVisitor = new ReferencedEntityVisitor(addIndexFields, addRootGroups, ec,
					refIdsMode, filter);
			setOp.getOperation().traverse(setExpressionVisitor);
			long targetIdx = refIdsMode == ReferencedIdsMode.USE_SET_EXPRESSION_REPLACE_INNER_BY_WILDCARD ?
					IParseTreeEntity.WILDCARD :
					IParseTreeEntity.ITERATION;
			Set<? extends IEntityId> ids = transformSetExpressionIds(setExpressionVisitor.getEntityIds(), targetIdx);
			entityIds.addAll(ids);
			return false;
		}

		private void addEntityToResult(IEntityId e) {
			if (filter.test(e)) {
				entityIds.add(replaceIndices4RepNotUnqIfRequired(e, inRepetitionNotUnique, levelToStartDuplicateSearch));
			}
		}

		Set<IEntityId> getEntityIds() {
			return entityIds;
		}

		Set<IEntityId> getAllRefIndexFields() {
			return allRefIndexFields;
		}

		void setLevelToStartDuplicateSearch(int level) {
			this.levelToStartDuplicateSearch = level;
		}
	}

	public static final Predicate<IEntityId> isField = p -> p != null && p.isField();
	public static final Predicate<IEntityId> isGroup = p -> p != null && !p.isField();
	public static final Predicate<IEntityId> isGroupButNoRootGroup = p -> p != null && !p.isField() && !EntityUtils.isRootGroup(
			p.getFullName());

	/**
	 * Create an {@link IEntityId} for the given {@link IRootGroupCondition}. For A12-DVs this should be obsolete, because there,
	 * we already have a valid {@link IEntityId} in the interface.
	 *
	 * @param ec
	 * 		the {@link IEC} containing the root group.
	 */
	public static IEntityId createEntityForRootGroupCondition(IRootGroupCondition rgc, IEC ec) {
		// rgc.getEntityId() is (at least sometimes) null, so the assertion below doesn't work:
		//assert rgc.getEntityId().isHmodel() :
		//		"For A12-DVs this should be obsolete, because there, we already have a valid IEntityId in the interface.";
		IEntity e = ec.get("/" + rgc.getRootGroupName());
		return new EntityIdImpl(e, List.of(rgc.getRootGroupIndex()), rgc.getUsb(), Collections.emptyList(), true, ec, false,
				PathNotationConfig.ABSPATH);
	}

	public static IEntityId createEntityForNumberOfRootGroupCondition(INumberOfRootGroupsOperation rgc, IEC ec) {
		IEntity e = ec.get("/" + rgc.getRootGroupName());
		// We cannot be sure, whether it is HModel or not, but it should not matter for referenced fields
		return new EntityIdImpl(e, List.of(0L), rgc.getUsb(), Collections.emptyList(), true, ec, false, PathNotationConfig.ABSPATH);
	}

	/**
	 * @param condition
	 * 		the condition to examine
	 * @param ec
	 * 		the entity container containing the index fields.
	 * @return the set of field names of all referenced fields including index fields indirectly
	 * 		referenced by semantic index constructs or an empty set if the condition is
	 * 		<code>null</code>.
	 */
	public static Set<String> getDirectlyReferencedFieldNames(ICondition condition, IEC ec) {
		return getFullNamesOfFilteredIdsWithoutRootGroups(Collections.singletonList(condition), isField, true, ec);
	}

	/**
	 * Returns the names of all referenced groups in the condition.
	 *
	 * @param condition
	 * 		the condition to examine.
	 * @return the set of names of all referenced groups or the empty set if the condition is
	 * 		<code>null</code>.
	 */
	public static Set<String> getDirektlyReferencedGroupNamesWithoutRootGroups(ICondition condition, IEC ec) {
		return getFullNamesOfFilteredIdsWithoutRootGroups(Collections.singletonList(condition), isGroupButNoRootGroup, false, ec);
	}

	/**
	 * Returns the set of names of all referenced fields and groups of the given condition.
	 *
	 * @param condition
	 * 		the condition to be examined
	 * @param ec
	 * 		the entity container containing the index fields.
	 * @return the set of names of all referenced fields and groups of the given condition.
	 */
	public static Set<String> getDirectlyReferencedEntityNamesWithoutRootGroups(ICondition condition, IEC ec) {
		Predicate<IEntityId> dummy = Objects::nonNull;
		return getDirectlyReferencedEntityNamesWithoutRootGroups(condition, dummy, ec);
	}

	public static Set<String> getDirectlyReferencedEntityNamesWithoutRootGroups(ICondition condition, Predicate<IEntityId> filter,
			IEC ec) {
		return getFullNamesOfFilteredIdsWithoutRootGroups(Collections.singletonList(condition), filter, true, ec);
	}

	public static Set<String> getReferenzierteVordruckNamen(ICondition b) {
		return getReferenzierteVordruckNamen(Collections.singletonList(b));
	}

	private static Set<String> getReferenzierteVordruckNamen(List<? extends IParseTreeEntity> parts) {

		final Set<String> vordruckNamen = new HashSet<>();
		if (parts == null) {
			return vordruckNamen;
		}

		class Visitor extends ASTVisitor {

			@Override
			public boolean visit(IRootGroupCondition b) {
				vordruckNamen.add(b.getRootGroupName());
				return true;
			}

			@Override
			public void endVisit(INumberOfRootGroupsOperation fop) {
				vordruckNamen.add(fop.getRootGroupName());
			}

			@Override
			public void endVisit(IEntityId id) {
				// Also account for referenced root groups in filter conditions
				if (id.getFilterCondition() != null) {
					id.getFilterCondition().traverse(this);
				}
			}
		}
		final Visitor visitor = new Visitor();
		parts.stream()
				.filter(Objects::nonNull)
				.forEach(p -> p.traverse(visitor));
		return vordruckNamen;
	}

	/**
	 * Returns the set of full names of the entities referenced in the given condition filtered by
	 * the given predicate.
	 * If the condition uses parallel iteration or a semantic index construct, the indirectly used
	 * index fields are added, too.
	 *
	 * @param parts
	 * 		condition parts to be examined.
	 * @param tester
	 * 		predicate to filter the set.
	 * @param addIndexFields
	 * 		specifies whether index fields should be added to the list before
	 * 		filtering.
	 * @param ec
	 * 		the entity container, the index fields are retrieved from. May be
	 * 		<code>null</code> if either no semantic index is specified in the
	 * 		condition or
	 * 		addIndexFields is <code>false</code>.
	 * @return the set of full names of the entities referenced by the condition, fulfilling the
	 * 		predicate.
	 */
	private static Set<String> getFullNamesOfFilteredIdsWithoutRootGroups(List<? extends IParseTreeEntity> parts,
			Predicate<IEntityId> tester,
			boolean addIndexFields, IEC ec) {

		Set<IEntityId> refEntities = getDirectlyReferencedIEntityIdsFilteredWithoutRootGroups(parts,
				ReferencedIdsMode.USE_SET_EXPRESSION_REPLACE_INNER_BY_WILDCARD, ec, tester, addIndexFields);
		return refEntities.stream()
				.map(IEntityId::getFullName)
				.collect(Collectors.toSet());
	}

	/**
	 * Returns all directly referenced fields of the condition as {@link IEntityId}.
	 * Additionally, if the condition uses parallel iteration or semantic indices the indirectly
	 * used index fields are added to the result.
	 *
	 * @param b
	 * 		the condition to examine.
	 * @param refIdsMode
	 * 		specifies if filter conditions are considered and if yes, if "non real"
	 * 		iterations
	 * 		in the filter conditions are replaced by wildcard, s.
	 *        {@link ReferencedEntityVisitor#transformFilterConditionIds(Set, IEntityId)}
	 * @param ec
	 * 		the entity container to retrieve the index fields from.
	 * @param addIndexFields
	 * 		should index fields be added to the result?
	 * @return the directly referenced fields containing the index fields for semantic index
	 * 		constructs.
	 */
	public static Set<IEntityId> getDirektReferenzierteIFeldIds(ICondition b, ReferencedIdsMode refIdsMode, IEC ec,
			boolean addIndexFields) {
		return getDirektReferenzierteIFeldIds(Collections.singletonList(b), refIdsMode, ec, addIndexFields);
	}

	/**
	 * Returns the referenced fields with indices set as needed in the referenced fields in
	 * generated meta rules. Some indices in fields referenced by the language construct
	 * <i>RepetitionNotUnique</i> (= <i>WiederholungNichtEindeutig</i>) are treated in a special
	 * way: for levels, which are searched for duplicates, the index is set to
	 * {@link IParseTreeEntity#REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED}
	 * <br>
	 * For Details, s. EXT-20784
	 */
	public static Set<IEntityId> getReferencedIds4MetaReferences(ICondition c, IEC ec, int levelToStartDuplicateSearch,
			boolean useGroups, Set<IEntityId> allRefIndexFields) {
		return getReferencedIds4MetaReferences(c, ec, levelToStartDuplicateSearch, useGroups, useGroups, allRefIndexFields);
	}

	public static Set<IEntityId> getReferencedIds4MetaReferences(ICondition c, IEC ec, int levelToStartDuplicateSearch,
			boolean useNonRootGroups, boolean useRootGroups, Set<IEntityId> allRefIndexFields) {
		ReferencedIdsMode refIdsMode = ReferencedIdsMode.USE_SET_EXPRESSION_REPLACE_INNER_BY_WILDCARD;
		Predicate<IEntityId> filter = mkEntityFilter(useNonRootGroups, useRootGroups);
		ReferencedEntityVisitor visitor = new ReferencedEntityVisitor(true, useRootGroups, ec, refIdsMode, filter);
		visitor.setLevelToStartDuplicateSearch(levelToStartDuplicateSearch);
		c.traverse(visitor);
		if (allRefIndexFields != null) {
			allRefIndexFields.addAll(visitor.getAllRefIndexFields());
		}
		return visitor.getEntityIds();
	}

	public static Set<? extends IEntityId> getReferencedIdsInFilterCondition(
			IEntityId id, IEC ec, boolean useNonRootGroups, boolean useRootGroups) {
		if (id.getFilterCondition() == null) {
			return Collections.emptySet();
		}
		ReferencedIdsMode refIdsMode = ReferencedIdsMode.USE_SET_EXPRESSION_REPLACE_INNER_BY_WILDCARD;
		Predicate<IEntityId> filter = mkEntityFilter(useNonRootGroups, useRootGroups);
		ReferencedEntityVisitor filterVisitor = new ReferencedEntityVisitor(true, useRootGroups, ec, refIdsMode,
				// does refIdsMode make any difference here? wouldn't it only chagnge something if there were nested filter conditions?
				filter);
		id.getFilterCondition().traverse(filterVisitor);
		return transformFilterConditionIds(filterVisitor.getEntityIds(), id);
	}

	public static Predicate<IEntityId> mkEntityFilter(boolean useNonRootGroups, boolean useRootGroups) {
		return e -> {
			if (e.isField()) {
				return true;
			}
			if (EntityUtils.isRootGroup(e.getFullName())) {
				return useRootGroups;
			}
			return useNonRootGroups;
		};
	}

	/**
	 * Note: you may need to apply replaceIndices4RepNotUnqIfRequired on the result
	 */
	public static AdditionalRefsResult getEntitiyReferencesIfSemanticIndex(IEntityId entityWithSemIdx, IEC ec,
			boolean addIndexFields) {
		Optional<IEntityId> result = Optional.empty();
		Optional<IEntityId> indexField = Optional.empty();
		if (!entityWithSemIdx.getSemanticIndex().isEmpty()) {
			// at the moment it is only possible to define one index field
			if (entityWithSemIdx.getSemanticIndex().get(0) instanceof IEntityId semIdxE) {
				result = Optional.of(semIdxE);
			}
			if (addIndexFields) {
				IEntityId indexFieldReference = createUniqueIndexFieldEntityId(entityWithSemIdx, ec);
				if (indexFieldReference != null) {
					indexField = Optional.of(indexFieldReference);
				}
			}
		}
		return AdditionalRefsResult.of(result, indexField);
	}

	public record AdditionalRefsResult(Set<IEntityId> all, Optional<IEntityId> semIdxRef, Optional<IEntityId> indexFieldRef) {
		static AdditionalRefsResult of(Optional<IEntityId> semIdxRef, Optional<IEntityId> indexFieldRef) {
			Set<IEntityId> all = new HashSet<>();
			semIdxRef.ifPresent(all::add);
			indexFieldRef.ifPresent(all::add);
			return new AdditionalRefsResult(all, semIdxRef, indexFieldRef);
		}
	}

	/**
	 * Note: you may need to apply replaceIndices4RepNotUnqIfRequired on the result
	 */
	public static Optional<IEntityId> getIndexFieldIfParallelIteration(IEntityId entity, IEC ec, boolean addIndexFields) {
		if (AnalyseService.isParallelIteration(entity, false) && addIndexFields
				&& entity.getSemanticIndex().isEmpty() // semIdx overrides parallel iteration (maybe the Analyseservice should take
			// this into account, but that might have side effects
		) {
			IEntityId indexFieldReference = createUniqueIndexFieldEntityId(entity, ec);
			if (indexFieldReference != null) {
				return Optional.of(indexFieldReference);

			}
			// Note/Question: shouldn't the index field of the repeatable group that contains the error field be returned too?
		}
		return Optional.empty();
	}

	private static Set<? extends IEntityId> transformSetExpressionIds(Set<IEntityId> filterIds, long targetIdx) {
		return filterIds.stream()
				.map(id -> transformSetExpressionId(id, targetIdx))
				.collect(Collectors.toSet());
	}

	private static IEntityId transformSetExpressionId(IEntityId id, long targetIdx) {
		if (id.isOuter()) {
			return id.withIsOuter(false); // the results of this method get their indices interpreted like non-filtercondition
			// entityids, so it doesn't make sense to have isOuter set there (and the refEntity codegen gets confused if
			// isOuter==true for any of the entities.
			// Note: If the entityIds had a "isInner" flag, we'd clear that too.
		}
		long[] indices = id.getIndices().clone();
		boolean changed = false;
		for (int i = 0; i < indices.length; i++) {
			if (indices[i] == IParseTreeEntity.INTERNAL_ITERATION) {
				indices[i] = targetIdx;
				changed = true;
			}
		}
		if (!changed) {
			return id;
		}
		return id.withIndices(indices);
	}

	private static Set<? extends IEntityId> transformFilterConditionIds(Set<IEntityId> filterIds, IEntityId filteredId) {
		return filterIds.stream()
				.map(id -> transformFilterConditionId(id, filteredId))
				.collect(Collectors.toSet());
	}

	private static IEntityId transformFilterConditionId(IEntityId id, IEntityId filteredId) {
		if (id.isOuter()) {
			return id.withIsOuter(false); // the results of this method get their indices interpreted like non-filtercondition
			// entityids, so it doesn't make sense to have isOuter set there (and the refEntity codegen gets confused if
			// isOuter==true for any of the entities.
			// Note: If the entityIds had a "isInner" flag, we'd clear that too.
		}
		long[] indices = id.getIndices().clone();
		long[] filteredIndices = filteredId.getIndices();
		boolean changed = false;
		for (int i = 0; i < indices.length; i++) {
			// Set all indices that are not iterated over in the filtered id to wildcard
			if (indices[i] == -1 && filteredIndices[i] == 0) {
				indices[i] = 0;
				changed = true;
			}
		}
		if (!changed) {
			return id;
		}
		return new EntityIdImpl(id, indices);
	}

	/**
	 * @param c
	 * 		the condition, the entities are extracted from.
	 * @param ec
	 * 		the {@link IEC}, the fields are looked up in.
	 * @param allRefIndexFields
	 * 		an out-parameter map, where the index fields referenced in c are stored.
	 * @return the {@link IEntityId}s referenced in c. Here, also root groups and groups are returned if they are not expanded in c.
	 */
	public static Set<IEntityId> getReferencedEntitiesWithRootGroups(ICondition c, IEC ec, Set<IEntityId> allRefIndexFields) {
		return getReferencedIds4MetaReferences(c, ec, -1, true, allRefIndexFields);
	}

	public static IEntityId replaceIndices4RepNotUnqIfRequired(IEntityId e,
			boolean inRepetitionNotUnique, int levelToStartReplacement) {
		return (inRepetitionNotUnique && levelToStartReplacement >= 0)
				? replaceIndices4RepNotUnq(e, levelToStartReplacement)
				: e;
	}

	public static IEntityId replaceInternalIndicesByWildcard(IEntityId e) {
		long[] indices = e.getIndices().clone();
		for (int i = 0; i < indices.length; i++) {
			if (indices[i] == IParseTreeEntity.INTERNAL_ITERATION) {
				indices[i] = IParseTreeEntity.WILDCARD;
			}
		}
		return e.withIndices(indices);
	}

	/**
	 * Replaces iteration indices starting from levelToStartReplacement by the value
	 * {@link IParseTreeEntity#REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED}
	 * <br>
	 * S. also {@link #getReferencedIds4MetaReferences}
	 */
	private static IEntityId replaceIndices4RepNotUnq(IEntityId e, int levelToStartReplacement) {
		long[] indices = e.getIndices().clone();
		for (int i = levelToStartReplacement; i < indices.length; i++) {
			if (indices[i] == IParseTreeEntity.ITERATION) {
				indices[i] = IParseTreeEntity.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED;
			}
		}
		return e.withIndices(indices);
	}

	public static Set<IEntityId> getDirektReferenzierteIFeldIds(List<? extends IParseTreeEntity> parts,
			ReferencedIdsMode refIdsMode, IEC ec, boolean addIndexFields) {
		return getDirectlyReferencedIEntityIdsFilteredWithoutRootGroups(parts, refIdsMode, ec, isField, addIndexFields);
	}

	public static Set<IEntityId> getDirectlyReferencedIEntityIdsFilteredWithoutRootGroups(List<? extends IParseTreeEntity> parts,
			ReferencedIdsMode refIdsMode, IEC ec, Predicate<IEntityId> filter, boolean addIndexFields) {
		Set<IEntityId> allRefIndexFields = addIndexFields ? new HashSet<>() : null;
		return getDirectlyReferencedEntityIdsInternWithoutRootGroups(parts, refIdsMode, ec, filter, allRefIndexFields);
	}

	public static Set<IEntityId> getDirectlyReferencedIFieldIds(List<? extends IParseTreeEntity> parts,
			ReferencedIdsMode refIdsMode, IEC ec, Set<IEntityId> allRefIndexFields) {
		return getDirectlyReferencedEntityIdsInternWithoutRootGroups(parts, refIdsMode, ec, isField, allRefIndexFields);
	}

	private static Set<IEntityId> getDirectlyReferencedEntityIdsInternWithoutRootGroups(List<? extends IParseTreeEntity> parts,
			ReferencedIdsMode refIdsMode, IEC ec, Predicate<IEntityId> filter, Set<IEntityId> allRefIndexFields) {
		if (parts == null) {
			return new HashSet<>();
		}

		boolean addIndexFields = allRefIndexFields != null;

		ReferencedEntityVisitor rfv = new ReferencedEntityVisitor(addIndexFields, false, ec, refIdsMode, filter);
		parts.stream().filter(Objects::nonNull).forEach(e -> e.traverse(rfv));
		if (allRefIndexFields != null) {
			allRefIndexFields.addAll(rfv.getAllRefIndexFields());
		}
		return rfv.getEntityIds();
	}

	public static IEntityId createUniqueIndexFieldEntityId(IEntityId entityId, IEC ec) {
		IField uniqueIndexField = getUniqueIndexField(entityId.getFullName(), ec);
		return uniqueIndexField != null ? createIndexFieldEntityId(entityId, uniqueIndexField) : null;
	}

	/**
	 * Creates a set of {@link IEntityId}s containing all index fields on the path of
	 * <code>entityId</code>. The index fields have the indices and the semantic index as
	 * <code>entityId</code>
	 *
	 * @param entityId
	 * 		some entity possibly containing a semantic index.
	 * @return the entity for the index field or <code>null</code> if the index field could not be
	 * 		resolved.
	 */
	public static Set<IEntityId> createAllIndexFieldEntityIds(IEntityId entityId, IEC ec) {
		IEntity entity = ec.get(entityId.getFullName());
		Set<IField> indexFields = AnalyseService.getIndexFields(entity, ec);

		Set<IEntityId> result = new HashSet<>();
		indexFields.stream()
				.forEach(indexField -> result.add(createIndexFieldEntityId(entityId, indexField)));

		return result;
	}

	private static IEntityId createIndexFieldEntityId(IEntityId entityId, IField indexField) {
		List<Long> indices = Arrays.stream(entityId.getIndices())
				.boxed()
				.limit(EntityUtils.getLevel(indexField.getFullName()))
				.collect(Collectors.toList());
		// The row repetition is always 1
		indices.add(1l);
		return new EntityIdImpl(indexField, indices, IParseTreeEntity.UNSPECIFIED, entityId.getSemanticIndex(), false,
				entityId.isOuter(),
				entityId.getPathNotationConfig()); // Note: entityId's pathNotationConfig might be irrelevant here. Maybe we could also set PathNotationConfig.IRRELEVANT or PathNotationConfig.DEFAULT_MAYBELOSSY
	}

	/**
	 * Calculate the index field entity for the given value specifying entity name of an entity with
	 * semantic index.
	 *
	 * @param entityIdName
	 * 		the name of the entity, which is used with a semantic index.
	 * @return the corresponding index field or <code>null</code>, if there is no index field or
	 * 		there are multiple. If the entity was successfully used with a semantic index in a
	 * 		condition, <code>null</code> should never be returned.
	 */
	private static IField getUniqueIndexField(String entityIdName, IEC ec) {
		IEntity entity = ec.get(entityIdName);
		Set<IField> indexFields = AnalyseService.getIndexFields(entity, ec);
		if (indexFields.size() != 1) {
			return null;
		} else {
			return indexFields.iterator().next();
		}
	}

}
