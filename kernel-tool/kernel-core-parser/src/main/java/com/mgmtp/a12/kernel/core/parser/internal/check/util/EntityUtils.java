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
package com.mgmtp.a12.kernel.core.parser.internal.check.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.ado.IExtmpEntity;
import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.CheckRelUpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntitySpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Lenient_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.ExpandMode;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRefUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;

/**
 * Helper methods to check Entities.
 */
public class EntityUtils {

	public static final String SEPARATOR = ElementPathUtils.SEPARATOR;
	public static final char SEPARATOR_CHAR = ElementPathUtils.SEPARATOR_CHAR;
	public static final String PATH_UP = "..";

	private EntityUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * return the name of the rule group thus, we take the name of the rule and cut off the last
	 * part
	 *
	 * @param r
	 * 		the rule
	 * @return name of the rule group
	 */
	public static String getRuleGroupName(IRule r) {
		String rName = r.getFullName();
		return rName.substring(0, rName.lastIndexOf(SEPARATOR));
	}

	/**
	 * returns true, if the given name is the name of a root group.
	 *
	 * @param path
	 * 		name to be specified.
	 * @return true, if it is a root group, false otherwise.
	 */
	public static boolean isRootGroup(String path) {
		return StringUtils.countMatches(path, SEPARATOR) == 1;
	}

	public static boolean isRootGroup(IEntityId entityId) {
		return isRootGroup(entityId.getFullName());
	}

	/**
	 * returns true if the given group is on the given path. I.e. the path starts with the full name
	 * of the group. if both are equal, true is returned.
	 *
	 * @param group
	 * 		group
	 * @param path
	 * 		path
	 * @return true, if the entity starts with the given path, false otherwise.
	 */
	public static boolean isAncestorOrEqual(IEntity group, String path) {
		//add a slash to make sure we do not return if just the prefix is equal.
		String pathWithSlash = path + SEPARATOR;
		String entityPathWithSlash = group.getFullName() + SEPARATOR;
		return pathWithSlash.startsWith(entityPathWithSlash);
	}

	/**
	 * returns true if the given group is on the given path. I.e. the path starts with the full name
	 * of the group. if both are equal, false is returned.
	 *
	 * @param group
	 * 		group
	 * @param path
	 * 		path
	 * @return true, if the entity starts with the given path and is not equal to the given path, false otherwise.
	 */
	public static boolean isAncestor(IEntity group, String path) {
		//add a slash to make sure we do not return if just the prefix is equal.
		String pathWithSlash = path + SEPARATOR;
		String entityPathWithSlash = group.getFullName() + SEPARATOR;
		if (pathWithSlash.equals(entityPathWithSlash)) {
			return false;
		}
		return pathWithSlash.startsWith(entityPathWithSlash);
	}

	/**
	 * Returns true if the given "testPath" corresponds to a sub-group below or equal to the group
	 * of the given refPath. Also returns true if the given "testPath" corresponds to a field contained
	 * in the group of the given refPath.
	 *
	 * @param testPath
	 * 		the path that should be tested to be below the other one.
	 * @param refPath
	 * 		the path against which {@code testPath} should be tested.
	 * @return <code>true</code> if the given testPath is below the given refPath or the two paths are
	 * 		equal, <code>false</code> otherwise.
	 */
	public static boolean isDescendantOrEqual(String testPath, String refPath) {
		//add a slash to make sure we do not return if just the prefix is equal.
		String refPathWithSlash = refPath + SEPARATOR;
		String testPathWithSlash = testPath + SEPARATOR;
		return testPathWithSlash.startsWith(refPathWithSlash);
	}

	/**
	 * Returns true if the given "testPath" corresponds to a sub-group below but not equal to the group
	 * of the given refPath. Also returns true if the given "testPath" corresponds to a field contained
	 * in the group of the given refPath.
	 *
	 * @param testPath
	 * 		the path that should be tested to be below the other one.
	 * @param refPath
	 * 		the path against which {@code testPath} should be tested.
	 * @return <code>true</code> if the given testPath is below the given refPath and the two paths are
	 * 		not equal, <code>false</code> otherwise.
	 */
	public static boolean isDescendant(String testPath, String refPath) {
		//add a slash to make sure we do not return if just the prefix is equal.
		String refPathWithSlash = refPath + SEPARATOR;
		String testPathWithSlash = testPath + SEPARATOR;
		if (refPathWithSlash.equals(testPathWithSlash)) {
			return false;
		}
		return testPathWithSlash.startsWith(refPathWithSlash);
	}

	/**
	 * Helper method. checks if the given group lies on the path of the error field of the given
	 * rule. This is required to check if iteration is correct.
	 *
	 * @param group
	 * 		group to be checked
	 * @param errorEntity
	 * 		the error entity
	 * @return true, if the entity is on the path of the error field.
	 */
	public static boolean isGroupAboveOrEqualErrorEntityPath(IEntity group, IEntity errorEntity) {
		String errorPath = errorEntity.isGroup() ? errorEntity.getFullName() : EntityUtils.getParentPath(errorEntity.getFullName());
		return isAncestorOrEqual(group, errorPath);
	}

	/**
	 * @return true, if path starts with a slash, false otherwise
	 */
	public static boolean isAbsolutePath(String path) {
		return path.startsWith(SEPARATOR);
	}

	/**
	 * Returns true, if the given group is strictly below the path of the error field of the rule.
	 * This method is used to check if iteration resp. wildcard-specifications are valid.
	 *
	 * @param group
	 * 		group
	 * @param errorEntity
	 * 		the error entity
	 * @return true, if the entity is below the error field, false otherwise.
	 */
	public static boolean isGroupBelowErrorEntityPath(IEntity group, IEntity errorEntity) {
		String testPath = errorEntity.isGroup() ?
				errorEntity.getFullName() :
				EntityUtils.getParentPath(errorEntity.getFullName());
		String groupPath = group.getFullName();

		return isDescendant(groupPath, testPath);
	}

	/**
	 * return the parent path part of an entity
	 * <p>
	 * <i>example:</i>
	 * <p>
	 * <code>
	 * {@literal /a/b/c-->return /a/b}
	 *
	 * </code>
	 *
	 * @param path
	 * 		full name of an entity
	 * @return path part of an entity.
	 */
	public static String getParentPath(String path) {
		return ElementPathUtils.getParentPath(path);
	}

	/**
	 * return the parent path part of an entity and a slash
	 * <p>
	 * <i>example:</i>
	 * <p>
	 * <code>
	 * {@literal /a/b/c-->return /a/b/}
	 *
	 * </code>
	 *
	 * @param path
	 * 		full name of an entity
	 * @return path part of an entity and a slash
	 */
	public static String getParentPathWithSlash(String path) {
		return path.substring(0, path.lastIndexOf(SEPARATOR_CHAR)) + SEPARATOR;
	}

	/**
	 * returns the last name part of the full name
	 * <p>
	 * <i>example:</i>
	 * <p>
	 * <code>
	 * {@literal /a/b/c-->return c}
	 *
	 * </code>
	 *
	 * @param path
	 * 		full name of an entity
	 * @return last part of the full name
	 */
	public static String getShortName(String path) {
		return ElementPathUtils.getName(path);

	}

	/**
	 * returns the name of the root group
	 * <p>
	 * <i>example:</i>
	 * <p>
	 * <code>
	 * {@literal /a/b/c-->return a}
	 *
	 * </code>
	 *
	 * @param path
	 * 		full name of an entity
	 * @return root group name
	 */
	public static String getRootGroupName(String path) {
		return path.split(SEPARATOR)[1];
	}

	/**
	 * returns the path of the root group
	 * <p>
	 * <i>example:</i>
	 * <p>
	 * <code>
	 * {@literal /a/b/c-->return /a}
	 *
	 * </code>
	 *
	 * @param path
	 * 		full name of an entity
	 * @return root group path
	 */
	public static String getRootGroupPath(String path) {
		return SEPARATOR + getRootGroupName(path);
	}

	/**
	 * helper method to build a path or a relative name
	 *
	 * @return concatenates path and toAdd, or simply returns toAdd if path is empty.
	 */
	public static String createPath(String path, String toAdd) {
		return ElementPathUtils.createPath(path, toAdd);
	}

	@SuppressWarnings("serial")
	public static class RelativeBeyondRootException extends Exception {
	}

	/**
	 * Helper method to build a full name from path and a relative path that can contain also upward
	 * references '..'. <br>
	 * <b>Example:</b>
	 * <ul>
	 * <li>path="/rg/g1/g11", toAdd="../..rg/g2/g21", result="/rg/g2/g21"</li>
	 * <li>path="/rg/g1/g11", toAdd="../..", result="/rg"</li>
	 * <li>path="/rg/g1", toAdd="g2/g3", result="/rg/g1/g2/g3"</li>
	 * </ul>
	 * <p>
	 * <b>Note:</b> the method does not check the correctness of the "turning point" in toAdd, i.e.
	 * the first example with toAdd="../..wrongTurningPoint/g2/g21" leads to the same result
	 *
	 * @param path
	 * 		some path
	 * @param toAdd
	 * 		relative path possibly containing '..' and / or '..groupname'
	 * @return the result of joining path and toAdd by using the semantics of '..' as stepping up
	 * 		the path toward root group
	 *
	 * @throws RelativeBeyondRootException
	 * 		exception is thrown if too many upward steps ".." are
	 * 		contained in toAdd
	 */
	public static String relPath2AbsPath(String path, String toAdd) throws RelativeBeyondRootException {
		String[] parts = toAdd.split(SEPARATOR);
		List<String> pathParts = new ArrayList<>(Arrays.asList(StringUtils.split(path, SEPARATOR)));
		for (String part : parts) {
			if (part.startsWith(PATH_UP)) {
				if (pathParts.size() - 2 < CheckRelUpImpl.MIN_LEVEL_FOR_UPWARD_PATHS) {//we reached the root group, we can't go further up
					throw new RelativeBeyondRootException();
				}
				pathParts.remove(pathParts.size() - 1);
			} else {
				pathParts.add(part);
			}
		}
		return SEPARATOR + String.join(SEPARATOR, pathParts);
	}

	/**
	 * Note: does not warn/reject for inputs where the path ends up pointing to a different root group.
	 * <p/>
	 * Will always create relpaths with implicit turning point (e.g. "../G2/f1") and never with explicit turning point
	 * ("..G/G2/f1")
	 */
	public static String absPath2RelPathNoValidate(String abspath, String ruleGroupAbsPath, boolean replaceEmptyPathByDotSlash) {
		int lowestCommonAncestorLevel = getCommonAncestorLevel(abspath, ruleGroupAbsPath); // 0-based level
		String[] absPathParts = getPathParts(abspath);
		String[] ruleGroupAbsPathParts = getPathParts(ruleGroupAbsPath);
		int ruleGroupLevel = ruleGroupAbsPathParts.length - 1; // 0-based

		String relPath = StringUtils.repeat("../", ruleGroupLevel - lowestCommonAncestorLevel)
				+ String.join(SEPARATOR,
				Arrays.copyOfRange(absPathParts, lowestCommonAncestorLevel + 1, absPathParts.length));
		if (replaceEmptyPathByDotSlash && relPath.isEmpty()) {
			return "./";
		} else {
			return relPath;
		}
	}

	public static String entityId2RelpathNoValidate(IEntityId e, IRule rule, boolean replaceEmptyPathByDotSlash) {
		return EntityUtils.absPath2RelPathNoValidate(e.getFullName(), EntityUtils.getParentPath(rule.getFullName()),
				replaceEmptyPathByDotSlash);
	}

	/**
	 * Expand a list of parameters to a list of fields. If entity is a group, it is expanded and all
	 * contained fields are added to the result list If entity is a field, it is added to the result
	 * list.
	 */
	public static List<IField> expandToFieldList(List<EntityRef> refs, Lenient_ConditionParser parser, ParserRuleContext ctx) {
		return refs.stream()
				.flatMap(ref -> expandToFieldList(ref, parser, ctx).stream())
				.toList();
	}

	/**
	 * Expand an entity to a list of fields. If entity is a group, it is expanded and all contained
	 * fields are in the result list If entity is a field, a one-element result list is returned.
	 * checks that the group is not empty.
	 */

	public static List<IField> expandToFieldList(EntityRef ref, Lenient_ConditionParser parser, ParserRuleContext ctx) {
		IEntity e = ref.getEntity();
		if (e.isField()) { // I'm a field-> take just me
			return Collections.singletonList((IField) e);
		} else {
			return EntityRefUtils.getAllContainedFieldsAndCheckNotEmpty(ref, parser, ctx);
		}
	}

	/**
	 * Expand an entity to a list of contained entities. If the entity is a group, it is expanded
	 * according to the given expandMode (see {@link ExpandMode}). If entity is a field, a
	 * one-element result list is returned. Checks that the group is not empty.
	 */
	public static List<IEntity> expandToEntityList(EntityRef ref, Lenient_ConditionParser parser, EntitySpecContext ctx,
			ExpandMode expandMode) {
		IEntity e = ref.getEntity();
		if (e.isField()) {
			return Collections.singletonList(e);
		} else {
			return EntityRefUtils.getAllContainedEntitiesAndCheckNotEmpty(ref, parser, ctx, expandMode);
		}
	}

	/**
	 * Returns all fields, the given {@link IEC} contains.
	 * <p>
	 * ATTENTION: The value is calculated and not cached. It is potentially expensive.
	 *
	 * @param ec
	 * 		the {@link IEC} to query the fields from.
	 * @return the fields contained in the {@link IEC}.
	 */
	public static List<IField> getAllFields(IEC ec) {
		return ec.getDirectlyContainedEntities(SEPARATOR).stream()
				.flatMap(v -> ec.getAllContainedFields(v.getFullName()).stream())
				.toList();
	}

	/**
	 * Return the field for the given name. The name may be a full name or a short name, if the name
	 * is unique.
	 *
	 * @param ec
	 * 		the {@link IEC} containing the field
	 * @param name
	 * 		the name of the field (full or short).
	 * @return the field if found or {@link Optional#empty()} otherwise.
	 */
	public static Optional<IField> lookupField(IEC ec, String name) {
		return lookupEntity(ec, name).map(IField.class::cast);
	}

	/**
	 * Return the entity for the given name. The name may be a full name of an entity or a short
	 * name of a field, if the name is unique.
	 *
	 * @param ec
	 * 		the {@link IEC} containing the entity
	 * @param name
	 * 		the name of the entity (full or short). Short names are allowed for fields only.
	 * @return the entity if found or {@link Optional#empty()} otherwise.
	 */
	public static Optional<IEntity> lookupEntity(IEC ec, String name) {
		IEntity entity = ec.get(name);
		if (entity != null) {
			return Optional.of(entity);
		}
		Set<String> fullNames = ec.shortName2FullName(name);
		if (fullNames.size() == 1) {
			String fdName = ec.shortName2FullName(name).iterator().next();
			return Optional.of(ec.get(fdName));
		}
		return Optional.empty();
	}

	/**
	 * Return all groups, except root groups, contained in the given {@link IEC}.
	 *
	 * @param ec
	 * 		the {@link IEC} to query the groups from.
	 */
	public static List<IGroup> getAllInnerGroups(IEC ec) {
		List<IGroup> accumulator = new ArrayList<>();
		ec.getDirectlyContainedEntities(SEPARATOR).stream()
				.filter(IGroup.class::isInstance)
				.map(IGroup.class::cast)
				.forEach(g -> getGroupsRecursive(ec, g, accumulator));
		return accumulator;
	}

	/**
	 * Return all root groups, contained in the given {@link IEC}.
	 *
	 * @param ec
	 * 		the {@link IEC} to query the groups from.
	 */
	public static List<IGroup> getRootGroups(IEC ec) {
		return ec.getDirectlyContainedEntities(SEPARATOR).stream()
				.filter(IGroup.class::isInstance)
				.map(IGroup.class::cast)
				.toList();
	}

	/**
	 * Return all groups including root groups, contained in the given {@link IEC}.
	 *
	 * @param ec
	 * 		the {@link IEC} to query the groups from.
	 */
	public static List<IGroup> getAllGroups(IEC ec) {
		return Stream.concat(getRootGroups(ec).stream(), getAllInnerGroups(ec).stream())
				.toList();
	}

	private static void getGroupsRecursive(IEC ec, IGroup g, List<IGroup> accumulator) {
		List<IGroup> directlyContainedGroups = ec.getDirectlyContainedEntities(g.getFullName()).stream()
				.filter(IEntity::isGroup)
				.map(IGroup.class::cast)
				.toList();
		if (!directlyContainedGroups.isEmpty()) {
			accumulator.addAll(directlyContainedGroups);
			directlyContainedGroups.forEach(sg -> getGroupsRecursive(ec, sg, accumulator));
		}
	}

	/**
	 * Returns all entities (i.e. fields and groups) contained in the entity given by full name As we
	 * need also the groups the method {@link IEC#getAllContainedFields(String)} cannot be used.
	 *
	 * @param path
	 * 		full name of the entity
	 * @param ec
	 * 		Entity container
	 * @return all fields and groups contained in the entity specified by path
	 */
	public static List<IEntity> getAllContainedEntities(String path, IEC ec) {
		List<IEntity> result = new ArrayList<>();
		List<IEntity> entities = new ArrayList<>(ec.getDirectlyContainedEntities(path));
		while (!entities.isEmpty()) {
			IEntity entity = entities.remove(0);
			result.add(entity);
			entities.addAll(ec.getDirectlyContainedEntities(entity.getFullName()));
		}
		return result;
	}

	public static long getMaxUsb(IEntity e) {
		if (e instanceof IExtmpEntity extmpEntity) {
			return extmpEntity.getMaxUsb();
		}
		return IParseTreeEntity.UNSPECIFIED;
	}

	public static boolean hasMaxUsb(IEntity e) {
		return getMaxUsb(e) != IParseTreeEntity.UNSPECIFIED;
	}

	public static long getMaxLfdNr(IEntity e) {
		return e.getMax()[0];
	}

	public static long[] getMax(IEntity e, boolean isHModel) {
		if (isHModel) {
			if (e.isField()) {
				return new long[] { getMaxLfdNr(e), getMaxZeile((IField) e) };
			} else {
				return new long[] { getMaxLfdNr(e) };
			}
		}
		return e.getMax();
	}

	public static long[] getMaxInA12Model(IEntity e) {
		return e.getMax();
	}

	/**
	 * Get the max. repeatability on the lowest level.
	 */
	public static long getMax(IEntity e) {
		int level = getLevel(e.getFullName());
		long[] max = e.getMax();
		if (level != max.length - 1) { // can only occur in H models
			if (isRootGroup(e.getFullName())) {
				return e.getMax()[0];
			} else if (e.isField()) {
				return e.getMax()[1];
			} else { // groups are not repeatable in H models
				return 1;
			}
		}
		return e.getMax()[level];
	}

	/**
	 * @param entity
	 * 		entity to be analyzed.
	 * @return the maximum allowed repeatability for the lowest repeatable level, or {@code 1} if
	 * 		all the groups are not repeatable.
	 */
	public static long getMaxOfLowestRepeatableLevel(IEntity entity) {
		long[] max = entity.getMax();
		for (int i = max.length - 1; i >= 0; i--) {
			if (max[i] > 1) {
				return max[i];
			}
		}
		return 1;
	}

	/**
	 * @param entity
	 * 		entity to be analyzed.
	 * @return the maximum allowed repeatability for the lowest repeatable level except for the root
	 * 		group, or {@code 1} if all the groups are not repeatable.
	 */
	public static long getMaxOfLowestRepeatableLevelNotRG(IEntity entity) {
		long[] max = entity.getMax();
		for (int i = max.length - 1; i >= 1; i--) {
			if (max[i] > 1) {
				return max[i];
			}
		}
		return 1;
	}

	/**
	 * Please compare also
	 * {@link EntityRefUtils#getMaxZeileApplicableForGroupsAlso(ParserRuleContext, EntityRef, ParserErrorTypesEnumForHModel,
	 * CheckImplAbstractClass)}
	 *
	 * @param e
	 * 		a field
	 * @return max. row index of the field
	 */
	public static long getMaxZeile(IField e) {
		return e.getMax()[e.getMax().length - 1];
	}

	/**
	 * Returns whether the containing group is repeatable. <b>Note:</b> If only some ancestor of the
	 * parent group is repeatable, but the parent group itself is not, <code>false</code> is
	 * returned.<br>
	 * This method is only valid for A12 models!
	 */
	public static boolean isInRepeatableGroup(IEntity e) {
		long[] max = e.getMax();
		return max[max.length - 2] > 1;
	}

	public static boolean isRepeatableOnLowestLevel(IEntity e) {
		return getMax(e) > 1;
	}

	public static boolean isRepeatableOnOneLevel(IEntity e, boolean isHModel) {
		if (EntityUtils.getMaxUsb(e) > 1) {
			return true;
		}
		for (long l : EntityUtils.getMax(e, isHModel)) {
			if (l > 1) {
				return true;
			}
		}
		return false;
	}

	public static String getNameBuiltForConditionTextHModel(String rulePath, String path, boolean isField,
			boolean hasWildcardOnRootgroup) {
		if (isField) {
			return EntityUtils.getShortName(path);
		} else {
			String rulePathWithSlash = EntityUtils.getParentPathWithSlash(rulePath);
			if (path.startsWith(rulePathWithSlash) && !hasWildcardOnRootgroup) {
				return path.substring(rulePathWithSlash.length()); // entity contained in rule group -> relative path
			} else {
				return path; // entity not contained in rule group -> absolute path
			}
		}
	}

	public static String getNameBuiltForErrorTextParameterA12Model(String rulePath, String path) {
		String rulePathWithSlash = EntityUtils.getParentPathWithSlash(rulePath);
		if (path.startsWith(rulePathWithSlash)) {
			return path.substring(rulePathWithSlash.length()); // entity contained in rule group -> relative path
		} else {
			return path; // entity not contained in rule group -> absolute path
		}
	}

	private static Set<Integer> getWildcardLevelsOnRulePathA12ModelInternal(String entityPath, long[] indices,
			String rulePath) {
		int commonAncestorLevel = getCommonAncestorLevel(entityPath, getParentPath(rulePath));
		Set<Integer> result = new HashSet<>();
		for (int i = 0; i <= commonAncestorLevel; i++) {
			if (indices[i] == IParseTreeEntity.WILDCARD) {
				result.add(i);
			}
		}
		return result;
	}

	public static Set<Integer> getWildcardLevelsOnRulePathA12Model(String entityPath, List<Long> indices, String rulePath) {
		long[] indicesArray = indices.stream().mapToLong(Long::longValue).toArray();
		return getWildcardLevelsOnRulePathA12ModelInternal(entityPath, indicesArray, rulePath);
	}

	public static int getLevelBeforeFirstWildcard(long[] pathIndices) {
		int levelBeforeFirstWilcard = -1;
		for (int level = 0; level < pathIndices.length; level++) {
			if (pathIndices[level] == IParseTreeEntity.WILDCARD) {
				break;
			} else {
				levelBeforeFirstWilcard = level;
			}
		}
		return levelBeforeFirstWilcard;
	}

	/**
	 * Get the index of the last iteration. This specifies the maximum depth of iteration in the
	 * group hierarchie. Should be used for A12 models only.
	 *
	 * @param field
	 * 		the field to test.
	 * @return <code>-1</code> if the entity is not iterable, the maximum index on which the entity
	 * 		is iterable.
	 */
	public static int getMaxIterationLvl(IEntity field) {
		long[] max = field.getMax();
		for (int i = max.length - 1; i >= 0; i--) {
			if (max[i] > 1) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the ancestor path of the given path, that has level levels.
	 *
	 * @param level
	 * 		the maximum level of the returned path. 0 denotes the root group, etc.
	 * @param path
	 * 		the path of the entity.
	 * @return the ancestor path of the given path that has <code>level+1</code> slashes or the full
	 * 		path if that has less than <code>level+1</code> slashes.
	 */
	public static String getPathUpToLevel(int level, String path) {
		return splitPathBelowLevel(level, path).getLeft();
	}

	public static String getPathBelowLevel(int level, String path) {
		return splitPathBelowLevel(level, path).getRight();
	}

	public static Pair<String, String> splitPathBelowLevel(int level, String path) {
		if (!path.startsWith("/")) {
			throw new IllegalArgumentException("path must start with '/' but was " + path);
		}
		if (path.endsWith("/")) {
			throw new IllegalArgumentException("path must not end with '/' but was " + path);
		}
		if (level <= -1) {
			return Pair.of("", path.substring("/".length()));
		}
		int separatorIndex = StringUtils.ordinalIndexOf(path, SEPARATOR, level + 2);
		if (separatorIndex == -1) {
			return Pair.of(path, "");
		}
		return Pair.of(path.substring(0, separatorIndex), path.substring(separatorIndex + 1));
	}

	/**
	 * Returns the level of the given path. 0 for a root group, 1 for a group below the root group,
	 * etc.
	 */
	public static int getLevel(String path) {
		return StringUtils.countMatches(path, SEPARATOR) - 1;
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
	 * Return the path of a root group, i.e. a slash followed by the name of the root group.
	 */
	public static String createRootGroupPath(String rootGroupName) {
		return SEPARATOR + rootGroupName;
	}

	/**
	 * Returns every part of the path as separate string.
	 *
	 * @param path
	 * 		the full name of the entity
	 * @return the path parts of the entity starting with the root group.
	 */
	public static String[] getPathParts(String path) {
		while (path.startsWith(SEPARATOR)) {
			path = path.substring(1);
		}
		return StringUtils.split(path, SEPARATOR);
	}

	/**
	 * Get a field that may be referenced absolutely or relative to the group.
	 *
	 * @param ec
	 * 		the entity container to fetch the field from.
	 * @param entityName
	 * 		the entity name.
	 * @param groupName
	 * 		the group name.
	 * @return the referenced field or <code>null</code> if the field could not be resolved.
	 */
	public static IEntity getEntityInGroup(IEC ec, String entityName, String groupName) {
		String path;
		if (!entityName.startsWith(SEPARATOR)) {
			path = createPath(groupName, entityName);
		} else {
			path = entityName;
		}
		return ec.get(path);
	}

	/**
	 * Get the parent entity of the given one.
	 *
	 * @param entity
	 * 		the child entity, the parent is needed.
	 * @param ec
	 * 		the entity container containing the parent entity.
	 * @return the parent entity or <code>null</code> if the entity could not be resolved or the
	 * 		child entity is a root group.
	 */
	public static IGroup getParentGroup(IEntity entity, IEC ec) {
		return getParentGroup(entity.getFullName(), ec);
	}

	private static IGroup getParentGroup(String entityPath, IEC ec) {
		return (IGroup) ec.get(getParentPath(entityPath));
	}

	/**
	 * Tests if the given field is an index field.
	 *
	 * @param field
	 * 		the field to test.
	 * @param ec
	 * 		the entity container containing the containing group.
	 * @return <code>true</code>
	 */
	public static boolean isIndexField(IField field, IEC ec) {
		IGroup containingGroup = getParentGroup(field, ec);
		if (containingGroup.getIndexFieldNames() == null || containingGroup.getIndexFieldNames().isEmpty()) {
			return false;
		}
		for (String indexFieldName : containingGroup.getIndexFieldNames()) {
			IEntity indexField = getEntityInGroup(ec, indexFieldName, containingGroup.getFullName());
			if (indexField != null && field.getFullName().equals(indexField.getFullName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get a list of all paths from the root group path to the entity path itself.
	 * <p>
	 * E.g. for "/V/K/K1" ["/V", "/V/K", "/V/K/K1"] will be returned.
	 */
	public static List<String> getAllAncestorGroupPaths(String entityPath) {
		List<String> result = getAllRealAncestorGroupPaths(entityPath);
		result.add(entityPath);
		return result;
	}

	/**
	 * Get a list of all paths from the root group path to the entity path, without the entity path
	 * itself.
	 * <p>
	 * E.g. for "/V/K/K1" ["/V", "/V/K"] will be returned.
	 */
	public static List<String> getAllRealAncestorGroupPaths(String entityPath) {
		List<String> result = new ArrayList<>();
		int idx = entityPath.indexOf(SEPARATOR_CHAR, 1);
		while (idx >= 0) {
			result.add(entityPath.substring(0, idx));
			idx = entityPath.indexOf(SEPARATOR_CHAR, idx + 1);
		}
		return result;
	}

	/**
	 * Determine the level w.r.t. the field path that contains an index field.
	 *
	 * @param entityPath
	 * 		full name of the entity
	 * @param ec
	 * 		the entity container containing the entity.
	 * @return level of the lowest group on the path of <code>entity</code> that contains an index
	 * 		field or -1 if there is no such level
	 */
	public static int getIndexFieldLevel(String entityPath, IEC ec) {
		IEntity entity = ec.get(entityPath);
		IGroup group = entity != null && entity.isGroup() ? (IGroup) entity : getParentGroup(entityPath, ec);
		while (group != null) {
			if (!group.getIndexFieldNames().isEmpty()) {
				return group.getMax().length - 1;
			}
			group = getParentGroup(group, ec);
		}
		return -1;
	}

	/**
	 * Determine the levels w.r.t. the entity path that contain an index field.
	 *
	 * @param entityPath
	 * 		full name of the entity
	 * @param ec
	 * 		the entity container containing the entity and the ancestors.
	 * @return all levels of groups on the path of the entity that contain an index
	 * 		field or an empty list if there is no such level.
	 */
	public static Set<Integer> getIndexFieldLevels(String entityPath, IEC ec) {
		Set<Integer> result = new HashSet<>();
		IEntity entity = ec.get(entityPath);
		IGroup group = entity instanceof IGroup g ? g : getParentGroup(entity, ec);
		while (group != null) {
			if (!group.getIndexFieldNames().isEmpty()) {
				result.add(group.getMax().length - 1);
			}
			group = getParentGroup(group, ec);
		}
		return result;
	}

	/**
	 * @param path
	 * 		group full name in which the index field should be searched.
	 * @return the index field for the given group only if one index field was found, otherwise
	 * 		<code>null</code>.
	 */
	public static IField getUniqueIndexField(String path, IEC ec) {
		IEntity entity = ec.get(path);
		assert entity instanceof IGroup;
		IGroup group = (IGroup) entity;
		if (group.getIndexFieldNames().size() != 1) {
			return null;
		}
		return (IField) getEntityInGroup(ec, group.getIndexFieldNames().iterator().next(), group.getFullName());
	}

	/**
	 * Return the root group of an entity.
	 *
	 * @param path
	 * 		full name of an entity
	 * @param ec
	 * 		the entity container containing the entity
	 * @return the root group that contains the entity
	 */
	public static IGroup getRootGroup(String path, IEC ec) {
		IEntity rootGroup = ec.get(createRootGroupPath(getRootGroupName(path)));
		assert rootGroup instanceof IGroup;
		return (IGroup) rootGroup;
	}

	/**
	 * @return <code>true</code> if a wild card is defined for an ascendant (not only for the direct
	 * 		parent), otherwise <code>false</code>.
	 */
	public static boolean isWildCardDefinedInParent(IEntityId id) {
		long[] maxIndices = id.getIndices();
		for (int i = 0; i < maxIndices.length - 1; i++) {
			if (maxIndices[i] == IParseTreeEntity.WILDCARD) {
				return true;
			}
		}
		return false;
	}

	public static int getCommonAncestorLevel(String path1, String path2) {
		// Append / for both may be groups
		String prefix = StringUtils.getCommonPrefix(path1 + ElementPathUtils.SEPARATOR, path2 + ElementPathUtils.SEPARATOR);
		// If the common prefix ends with /, the prefix is the name of the common ancestor with /, e.g. /A/B, /A/C -> /A/ -> common ancestor would be /A -> lvl 0
		// If the common prefix does not end with /, there is a common prefix in differing groups, e.g. /A/B/C, /A/B1 -> /A/B -> common ancestor would be /A -> lvl 0
		return StringUtils.countMatches(prefix, ElementPathUtils.SEPARATOR) - 2;
	}

	public static long getNumberOfRepeatableLevels(IEntity entity) {
		return Arrays.stream(entity.getMax()).filter(max -> max > 1).count();
	}

	/**
	 * Returns a list containing all repeatable levels of the entity in reverse order, i.e. the
	 * deepest repeatable level is on the first position of the list.
	 */
	public static List<Integer> getRepeatableLevels(IEntity entity) {
		List<Integer> result = new ArrayList<>();
		long[] max = entity.getMax();
		for (int i = max.length - 1; i >= 0; i--) {
			if (max[i] > 1) {
				result.add(i);
			}
		}
		return result;
	}

	public static List<Integer> getRepeatableLevelsBetween(IEntity entity, String ancestorPath) {
		List<Integer> result = new ArrayList<>();
		if (!isDescendantOrEqual(entity.getFullName(), ancestorPath)) {
			return result;
		}
		int levelOfAncestor = getLevel(ancestorPath);
		long[] max = entity.getMax();
		for (int i = max.length - 1; i > levelOfAncestor; i--) {
			if (max[i] > 1) {
				result.add(i);
			}
		}
		return result;
	}

	/**
	 * Find an entity via breadth-first-search in a subtree.
	 * Therefore, one of the Entities closest to the subtreeRoot is found, which fulfills the given
	 * predicate.
	 *
	 * @param subtreeRoot
	 * 		the root of the searched tree.
	 * @param condition
	 * 		the condition specifying entities to be searched.
	 * @param ec
	 * 		the {@link IEC} to resolve parent-child relations.
	 * @return the found entity fulfilling condition or {@link Optional#empty()}.
	 */
	public static Optional<IEntity> findBFS(IEntity subtreeRoot, Predicate<IEntity> condition, IEC ec) {
		if (condition.test(subtreeRoot)) {
			return Optional.of(subtreeRoot);
		}
		if (!subtreeRoot.isGroup()) {
			return Optional.empty();
		}

		Deque<IGroup> searchQueue = new LinkedList<>();
		searchQueue.add((IGroup) subtreeRoot);

		while (!searchQueue.isEmpty()) {
			IGroup group = searchQueue.removeFirst();
			List<IEntity> entities = ec.getDirectlyContainedEntities(group.getFullName());
			for (IEntity entity : entities) {
				if (condition.test(entity)) {
					return Optional.of(entity);
				}
				if (entity.isGroup()) {
					searchQueue.add((IGroup) entity);
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * <b>Precondition</b>: A12 model. (otherwise a field path may be returned if the field is
	 * repeatable on row level)
	 *
	 * @param entity
	 * 		entity to be analyzed.
	 * @return the full name of the lowest repeatable group, or {@code null} if
	 * 		all the groups are not repeatable.
	 */
	public static String getLowestRepeatableGroupPath(IEntity entity) {
		long[] max = entity.getMax();
		for (int i = max.length - 1; i >= 0; i--) {
			if (max[i] > 1) {
				return getPathUpToLevel(i, entity.getFullName());
			}
		}
		return null;
	}

	public static boolean shouldShortNameBeUsed(String ruleGroupPath, String oldPath, String newPath,
			IServiceConfiguration serviceConfiguration) {
		IEC ec = serviceConfiguration.getIEC();
		IEntity oldEntity = ec.get(oldPath);
		if (!serviceConfiguration.getModelSpecificSettings().isFieldRefByShortNameAllowed()
				|| oldEntity.isGroup()) {
			return false;
		}
		// we have a field
		String newShortName = getShortName(newPath);
		if (existsGroupWithSameFieldShortNameAtRuleGroup(ec, ruleGroupPath, newShortName, newPath)) {
			return false;
		}
		boolean wasRenamed = determineIfWasRenamed(oldPath, newPath);
		Set<String> fullNames = ec.shortName2FullName(newShortName);
		return (wasRenamed && fullNames.isEmpty()) ||
				(!wasRenamed && fullNames.size() == 1);
	}

	private static boolean existsGroupWithSameFieldShortNameAtRuleGroup(IEC ec, String ruleGroupPath, String fieldShortName,
			String fieldPath) {
		String ruleGroupWithNewShortName = EntityUtils.createPath(ruleGroupPath, fieldShortName);
		if (fieldPath.equals(ruleGroupWithNewShortName)) {
			return false;
		}
		IEntity entity = ec.get(ruleGroupWithNewShortName);
		return entity != null && entity.isGroup();
	}

	public static boolean determineIfWasRenamed(String oldPath, String newPath) {
		return !getShortName(oldPath).equals(getShortName(newPath));
	}
}
