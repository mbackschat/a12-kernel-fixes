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
package com.mgmtp.a12.kernel.md.model.internal.services.join.selection;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.services.join.JoiningType;
import com.mgmtp.a12.kernel.mmtypings.ct_selection_v1.views._ct_selection_v1.Selection;
import com.mgmtp.a12.kernel.mmtypings.ct_selection_v1.views._ct_selection_v1._selection.Escaped_Default;
import com.mgmtp.a12.kernel.mmtypings.ct_selection_v1.views._ct_selection_v1._selection.Selected;
import com.mgmtp.a12.kernel.mmtypings.ct_selection_v1.views._ct_selection_v1._selection.Unselected;
import com.mgmtp.a12.kernel.mmtypings.mm_selectionmodel_2.views.MM_SelectionModel_2;

import lombok.Getter;
import lombok.NonNull;

/**
 * Helper class for selections during a dm join. The class contains the specific selector {@link Predicate} and offers a default
 * selector implementation.
 */
@Getter
public class SelectionModelV2Wrapper {
	public static final String ARBITRARY_PATH_IDENTIFIER = "/*/";

	public static final Integer WORST_PRIORITY = Integer.MIN_VALUE;
	public static final Integer BEST_PRIORITY = Integer.MAX_VALUE;
	public static final String DEFAULT_SELECT_ALL = "__internal_default_select_all";
	public static final String SELECTION_ID_NOT_SET = "__fallback_selection_id_not_set";
	private Predicate<Field> fieldSelector;
	private Predicate<Group> groupSelector;
	private Predicate<Rule> ruleSelector;
	private Predicate<Computation> computationSelector;
	private final @NonNull String selectionModelId;

	public SelectionModelV2Wrapper(JoiningType selectionType) {
		this(selectionType, null);
	}

	public SelectionModelV2Wrapper(JoiningType selectionType, MM_SelectionModel_2 selectionModel) {
		if (selectionModel != null) {
			init(selectionModel, selectionType);
			this.fieldSelector = fieldSelector.and(getDefaultFieldSelector(selectionType));
			this.selectionModelId = Objects.requireNonNullElse(selectionModel._at(MM_SelectionModel_2._pointer().header().id()), SELECTION_ID_NOT_SET);
		} else {
			this.fieldSelector = getDefaultFieldSelector(selectionType);
			this.groupSelector = group -> true;
			this.ruleSelector = rule -> true;
			this.computationSelector = computation -> true;
			this.selectionModelId = DEFAULT_SELECT_ALL;
		}
	}

	/**
	 * Returns the default {@link SelectionModelV2Wrapper}, which includes all application data fields and fields not contained in
	 * appendix groups (for details see MGM Wiki Shortlink x/4a_mD )
	 *
	 * @return the default {@link SelectionModelV2Wrapper}
	 */
	public static SelectionModelV2Wrapper getDefault(JoiningType selectionType) {
		return new SelectionModelV2Wrapper(selectionType);
	}

	public boolean isSelected(Element element) {
		return switch (element.getClass().getSimpleName()) {
			case "Field" -> isSelected((Field) element);
			case "Group" -> isSelected((Group) element);
			case "Rule" -> isSelected((Rule) element);
			case "Computation" -> isSelected((Computation) element);
			default -> throw new IllegalArgumentException("Invalid element type " + element.getClass().getSimpleName());
		};
	}

	public boolean isSelected(Field field) {
		return fieldSelector.test(field);
	}

	public boolean isSelected(Group group) {
		return groupSelector.test(group);
	}

	public boolean isSelected(Rule rule) {
		return ruleSelector.test(rule);
	}

	public boolean isSelected(Computation computation) {
		return computationSelector.test(computation);
	}

	private Predicate<Field> getDefaultFieldSelector(JoiningType selectionType) {
		if (selectionType == JoiningType.SELECTION_DECORATION) {
			return f -> !f.isTransient();
		} else {
			return field -> true;
		}
	}

	public void init(MM_SelectionModel_2 selectionModel, JoiningType selectionType) {
		fieldSelector = getSelector(selectionModel, MmType.DATA);
		groupSelector = getSelector(selectionModel, MmType.DATA);

		// Per definition rules are ignored by selections for decorations. Therefore, we only build the rule selection for pure
		// selections
		if (selectionType == JoiningType.SELECTION) {
			ruleSelector = getSelector(selectionModel, MmType.VALIDATION);
			computationSelector = getSelector(selectionModel, MmType.COMPUTATION);
		}
	}

	private enum MmType {
		DATA, VALIDATION, COMPUTATION
	}

	private <T extends Element> Predicate<T> getSelector(MM_SelectionModel_2 selection, MmType mmType) {
		//we know that Data is the same as Validation as that is modeled as a complex type in MM
		var sp = MM_SelectionModel_2._pointer();
		Selection selectionData = switch (mmType) {
			case MmType.DATA -> selection._at(sp.content().data());
			case MmType.VALIDATION -> selection._at(sp.content().validation());
			case MmType.COMPUTATION -> selection._at(sp.content().computation());
		};
		if (selectionData == null) {
			//value is not set - so accept all
			return entity -> true;
		}

		Escaped_Default selectionDefault = selectionData.escaped_Default();
		var pathsInGroupSelected = selectionData.selected().stream().map(Selected::path).toList();
		var pathsInGroupNotSelected = selectionData.unselected().stream().map(Unselected::path).toList();

		if (pathsInGroupSelected.isEmpty() && pathsInGroupNotSelected.isEmpty()) {
			//Nothing to evaluate, so the default decides!
			return entity -> selectionDefault == Escaped_Default.Selected;
		}

		return entity -> {
			boolean defaultIsSelected = selectionDefault == Escaped_Default.Selected;

			TreeMap<Integer, Optional<String>> selectedBestPriorityMatchPair
					= evalBestMatchFor(entity, pathsInGroupSelected);
			int bestMatchingSelectedPriority = selectedBestPriorityMatchPair.isEmpty() ? WORST_PRIORITY :
					selectedBestPriorityMatchPair.lastKey();

			TreeMap<Integer, Optional<String>> unselectedBestPriorityMatchPair
					= evalBestMatchFor(entity, pathsInGroupNotSelected);
			int bestMatchingUnselectedPriority = unselectedBestPriorityMatchPair.isEmpty() ? WORST_PRIORITY :
					unselectedBestPriorityMatchPair.lastKey();

			return (defaultIsSelected && bestMatchingSelectedPriority >= bestMatchingUnselectedPriority)
					|| (!defaultIsSelected && bestMatchingSelectedPriority > bestMatchingUnselectedPriority);
		};
	}

	/**
	 * Returns {@code true} if the path is fully qualified — i.e. it references a single, named element without any wildcards.
	 * Such a path must exist in the reference model to have any effect.
	 */
	private static boolean isFullyQualified(@NonNull String path) {
		return !path.contains("*");
	}

	/**
	 * Returns a stream of fully qualified paths declared in the selection model that are absent from {@code referencePaths}.
	 * Covers all three sections: data, validation, and computation.
	 * Only for explicit selection.
	 */
	public static Stream<String> findMissingFullyQualifiedPathsInExplicitSelections(MM_SelectionModel_2 selectionModel, Set<String> referencePaths) {
		var sp = MM_SelectionModel_2._pointer();
		return Stream.of(
				selectionModel._at(sp.content().data()),
				selectionModel._at(sp.content().validation()),
				selectionModel._at(sp.content().computation())
		)
				.filter(Objects::nonNull)
				// only relevant for explicit selection
				.flatMap(section -> section.selected().stream().map(Selected::path))
				.filter(Objects::nonNull)
				.filter(path -> !SelectionModelV2Wrapper.isGroupPath(path))
				.filter(SelectionModelV2Wrapper::isFullyQualified)
				.filter(path -> !referencePaths.contains(path));
	}

	/**
	 * Collects the full paths of all elements (fields, groups, rules, computations) in the given document model.
	 */
	public static Set<String> collectAllElementPaths(DocumentModel dm) {
		return DocumentModelUtils.getAllElements(dm).stream()
				.map(ElementUtils::getPath)
				.collect(Collectors.toSet());
	}

	private static boolean isGroupPath(@NonNull String path) {
		return path.endsWith("/");
	}

	private static boolean isArbitraryPath(String path) {
		return path.startsWith(ARBITRARY_PATH_IDENTIFIER);
	}

	private static String getBasename(String path) {
		return path.substring(path.lastIndexOf('/') + 1);
	}

	private static boolean baseNameHasWildcardSuffix(String elementPart) {
		return Optional.ofNullable(elementPart).map(p -> p.endsWith("*")).orElse(false);
	}

	private static boolean baseNameHasWildcardPrefix(String elementPart) {
		return Optional.ofNullable(elementPart).map(p -> p.startsWith("*")).orElse(false);
	}

	/**
	 * Evaluates the best matching path, with its priority ranking. Note: The last entry has the best priority and the first entry
	 * the worst.
	 */
	private TreeMap<Integer, Optional<String>> evalBestMatchFor(Element entityOfInterest, List<String> pathsInGroup) {
		return pathsInGroup.stream()
				.map(path -> evalMatchPriorityFor(entityOfInterest, path))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, TreeMap::new));
	}

	/**
	 * The core of the selection priority evaluation. Distinct matching cases are evaluated and ranked. When no suitable matching
	 * has been detected the worst priority is returned.
	 *
	 * @see MGM Wiki Shortlink x/UAyvDQ
	 */
	private Map.Entry<Integer, Optional<String>> evalMatchPriorityFor(Element entityOfInterest, String path) {
		String elementFullName = ElementUtils.getPath(entityOfInterest);
		String elementPath = ElementPathUtils.getParentPathWithSlash(elementFullName);
		String elementName = entityOfInterest.getName();

		boolean pathIsGroupPath = isGroupPath(path);
		String pathPartToCheck = pathIsGroupPath ? path : ElementPathUtils.getParentPathWithSlash(path);
		String baseNameToCheck = getBasename(path);
		String elementPartWithoutAsterisk = baseNameToCheck.replace("*", "");

		boolean pathIsArbitrary = isArbitraryPath(path);
		boolean elementEndsWithAsterisk = baseNameHasWildcardSuffix(baseNameToCheck);
		boolean elementStartWithAsterisk = baseNameHasWildcardPrefix(baseNameToCheck);

		//Best possible match. - e.G.: /A/K1/F1 matches the element full name
		if (path.equals(elementFullName)) {
			return Map.entry(BEST_PRIORITY, Optional.of(path));
		}

		//e.G.: /*/F1 => The element name is fully matched
		if (pathIsArbitrary && baseNameToCheck.equals(elementName)) {
			return Map.entry(9_000, Optional.of(path));
		}

		//e.G.: /A/K1/*1 => given path matches and the given element ends with a 1
		if (elementStartWithAsterisk && pathPartToCheck.equals(elementPath) && elementName.endsWith(elementPartWithoutAsterisk)) {
			return Map.entry(8_000, Optional.of(path));
		}
		if (elementStartWithAsterisk && pathIsArbitrary && elementName.endsWith(elementPartWithoutAsterisk)) {
			//e.G.: /*/*1 => given path is arbitrary and the given element ends with a 1
			return Map.entry(7_000, Optional.of(path));
		}
		if (elementEndsWithAsterisk && pathPartToCheck.equals(elementPath) && elementName.startsWith(elementPartWithoutAsterisk)) {
			//e.G.: /A/K1/F* => given path matches and the given element starts with an F
			return Map.entry(6_000, Optional.of(path));
		}
		if (elementEndsWithAsterisk && pathIsArbitrary && elementName.startsWith(elementPartWithoutAsterisk)) {
			//e.G.: /*/F* => given path is arbitrary and the given element starts with an F
			return Map.entry(5_000, Optional.of(path));
		}
		if (pathIsGroupPath && pathPartToCheck.equals(elementPath)) {
			//e.G.: /A/K1/ => given path matches exactly the path of the element
			return Map.entry(4_000, Optional.of(path));
		}

		//e.G.: /A/ => pathToCheck starts with the given element path
		//Note: the longer the matching pathPart the better the priority.
		if (pathIsGroupPath && elementPath.startsWith(pathPartToCheck)) {
			return Map.entry(2_000 + pathPartToCheck.length(), Optional.of(path));
		}

		//e.G.: /*/ => pathToCheck is arbitrary
		if (pathIsGroupPath && pathIsArbitrary) {
			return Map.entry(1_000, Optional.of(path));
		}

		//No supported matching case detected.
		return Map.entry(WORST_PRIORITY, Optional.empty());
	}
}
