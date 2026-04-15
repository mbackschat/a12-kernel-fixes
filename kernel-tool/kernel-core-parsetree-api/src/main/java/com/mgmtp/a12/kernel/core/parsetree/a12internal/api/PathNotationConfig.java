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

package com.mgmtp.a12.kernel.core.parsetree.a12internal.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Contains information about the textual representation of a relative (upward)
 * path in a rule condition that has been collected by the parser
 */
@OnlyForUsage
public class PathNotationConfig {

	public enum Type {
		/**
		 * "RegelKontext" is treated like a relative path (i.e. like a "" -> "RegelKontext" string substitution
		 */
		RELATIVE,
		ABSOLUTE,
		SHORTNAME
	}

	/**
	 * stores not only one {@link Type}, but also potential fallbacks: if the type is overridden, but the overriding type is not
	 * legal in the context for which the path gets formatted, the fallback can be used so that the original type is preserved.
	 */
	private List<Type> notationPreference;

	/**
	 * false means either unknown or not an upward references
	 */
	private final boolean isLabeledAsRelativeUpwardReference;
	private final boolean hasExplicitTurningPoint;

	public static final PathNotationConfig ABSPATH = new PathNotationConfig(List.of(Type.ABSOLUTE), false, false);
	public static final PathNotationConfig SHORTNAME = new PathNotationConfig(List.of(Type.SHORTNAME), false, false);
	public static final PathNotationConfig RELATIVE = new PathNotationConfig(List.of(Type.RELATIVE), false, false);
	/**
	 * indicates that the path notation type / preference is unknown or no longer known, we use some default in those cases.
	 */
	public static final PathNotationConfig DEFAULT_MAYBELOSSY = RELATIVE;
	/**
	 * indicates that the pathNotationConfig does not need to be preserved because it doesn't matter. We set some default as a
	 * placeholder
	 */
	public static final PathNotationConfig IRRELEVANT = RELATIVE;

	public PathNotationConfig(boolean isRelPath, boolean isShortName, boolean isLabeledAsRelativeUpwardReference,
			boolean hasExplicitTurningPoint) {
		this(derivePreference(isRelPath, isShortName), isLabeledAsRelativeUpwardReference, hasExplicitTurningPoint);
	}

	public PathNotationConfig(Type firstChoice, boolean isLabeledAsRelativeUpwardReference, boolean hasExplicitTurningPoint) {
		this(List.of(firstChoice), isLabeledAsRelativeUpwardReference, hasExplicitTurningPoint);
	}

	private static List<Type> derivePreference(boolean isRelPath, boolean isShortName) {
		assert !(isRelPath && isShortName) : "isRelPath and isShortName are mutually exclusive";
		List<Type> notationPreference = new ArrayList<>(1);
		if (isRelPath) {
			notationPreference.add(0, Type.RELATIVE);
		} else if (isShortName) {
			notationPreference.add(0, Type.SHORTNAME);
		} else {
			notationPreference.add(0, Type.ABSOLUTE);
		}
		return notationPreference;
	}

	private PathNotationConfig(List<Type> notationPreference, boolean isLabeledAsRelativeUpwardReference,
			boolean hasExplicitTurningPoint) {
		this.notationPreference = notationPreference;
		this.isLabeledAsRelativeUpwardReference = isLabeledAsRelativeUpwardReference;
		this.hasExplicitTurningPoint = hasExplicitTurningPoint;
	}

	/**
	 * @return <code>true</code>, if the path is a relative path with upward parts and the relative path uses the notation
	 * 		with named highest point on the path,
	 * 		e.g. <code>true</code> for path <code>..G1/f1</code> but <code>false</code> for the (equivalent) path <code>../f1</code>.
	 */
	public boolean hasExplicitTurningPoint() {
		return hasExplicitTurningPoint;
	}

	/**
	 * @return a new {@link PathNotationConfig} that is identical to this one except hasExplicitTurningPoint has been set
	 * 		to the given value.
	 */
	public PathNotationConfig withHasExplicitTurningPoint(boolean hasExplicitTurningPoint) {
		assert !hasExplicitTurningPoint || this.firstChoice() == Type.RELATIVE;
		return new PathNotationConfig(this.notationPreference, this.isLabeledAsRelativeUpwardReference, hasExplicitTurningPoint);
	}

	/**
	 * derives a PathNotationConfig with primary type {@link Type#RELATIVE}, but preserves the original type (and preexisting
	 * fallbacks) as a fallback, in case the primary type later gets ruled out by {@link #withFilteredPreferences(Predicate)}
	 */
	public PathNotationConfig withPreferRelPath() {
		return withFirstChoice(Type.RELATIVE);
	}

	/**
	 * derives a PathNotationConfig with primary type {@link Type#SHORTNAME}, but preserves the original type (and preexisting
	 * fallbacks) as a fallback, in case the primary type later gets ruled out by {@link #withFilteredPreferences(Predicate)}
	 */
	public PathNotationConfig withPreferShortName() {
		return withFirstChoice(Type.SHORTNAME);
	}

	/**
	 * derives a PathNotationConfig with primary type {@link Type#ABSOLUTE}, but preserves the original type (and preexisting
	 * fallbacks) as a fallback, in case the primary type later gets ruled out by {@link #withFilteredPreferences(Predicate)}
	 */
	public PathNotationConfig withPreferAbsPath() {
		return withFirstChoice(Type.ABSOLUTE);
	}

	private PathNotationConfig withFirstChoice(Type choice) {
		List<Type> notationPreference = new ArrayList<>(1 + this.notationPreference.size());
		notationPreference.add(0, choice);
		for (var e : this.notationPreference) {
			if (e != choice) {
				notationPreference.add(e);
			}
		}
		return new PathNotationConfig(notationPreference, this.isLabeledAsRelativeUpwardReference, this.hasExplicitTurningPoint);
	}

	/**
	 * depending on the context, not all path notation types are allowed for an entiyId. This method allows to filter the
	 * preferred (e.g. original pre-move, or overriden) notation types, so that the most-preferred one among the legal ones
	 * becomes the first choice, and illegal ones are discarded.
	 *
	 * @param filter
	 * 		the filter that tells if a path notation type is legal for the entityId to which this PathNotationConfig belongs.
	 */
	public PathNotationConfig withFilteredPreferences(Predicate<Type> filter) {
		List<Type> filtered = this.notationPreference.stream().filter(filter).collect(Collectors.toList());
		if (filtered.size() <= 0) {
			filtered = List.of(Type.RELATIVE, Type.SHORTNAME, Type.ABSOLUTE)
					.stream().filter(filter).collect(Collectors.toList());
			if (filtered.size() <= 0) {
				assert false : "no known path type was accepted by the filter";
				filtered = List.of(Type.RELATIVE);
			}
		}
		return new PathNotationConfig(filtered, this.isLabeledAsRelativeUpwardReference, this.hasExplicitTurningPoint);
	}

	/**
	 * @return true if the pathNotationConfig was explicitly marked as belonging to a relative upward path, false if unknown or not
	 * 		a relative upward path
	 */
	public boolean isLabeledAsRelativeUpwardReference() {
		return isLabeledAsRelativeUpwardReference;
	}

	public Type firstChoice() {
		return notationPreference.get(0);
	}

	@Override public String toString() {
		return "PathNotationConfig{" +
				"notationPreference=" + notationPreference +
				", isLabeledAsRelativeUpwardReference=" + isLabeledAsRelativeUpwardReference +
				", hasExplicitTurningPoint=" + hasExplicitTurningPoint +
				'}';
	}
}
