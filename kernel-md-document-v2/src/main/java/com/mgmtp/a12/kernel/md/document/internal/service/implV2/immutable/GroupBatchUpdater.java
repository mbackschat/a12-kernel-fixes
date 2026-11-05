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

package com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;

import lombok.NonNull;

// NOTE: in case of modifications to this class, consider TestDocV2Performance, it offers functionality to
// easily and locally record performance measurements for some relevant operations, before and after the
// code change, and to visually plot the results for easy comparison via Analyze_Improvised_Perftests.ipynb
class GroupBatchUpdater<S extends AbstractImmutableGroupInstance<S>> {
	private final AbstractImmutableGroupInstanceBuilder<S> baseAsBuilder;
	private ImmutableRepetitionsImpl.Builder currentRepetitions = null;

	private final List<UpdateAction> sortedUpdates;
	private final int levelOffset;

	public static <T extends AbstractImmutableGroupInstance<T>> T batchUpdate(
			@NonNull T base, @NonNull List<UpdateAction> sortedUpdates) {
		return batchUpdate(base, sortedUpdates, 0);
	}

	private static <T extends AbstractImmutableGroupInstance<T>> T batchUpdate(
			T base, List<UpdateAction> sortedUpdates, int levelOffset) {
		return new GroupBatchUpdater<>(base, sortedUpdates, levelOffset).update();
	}

	private GroupBatchUpdater(S base, List<UpdateAction> sortedUpdates, int levelOffset) {
		this.baseAsBuilder = base.toLazyCopyBuilder();
		this.sortedUpdates = sortedUpdates;
		this.levelOffset = levelOffset;
	}

	private S update() {
		if (sortedUpdates.isEmpty()) {
			return baseAsBuilder.buildIfChanged();
		}
		for (List<UpdateAction> updatesWithCurrentName : groupSortedListBy(sortedUpdates,
				up -> up.pointer().getPartAtLevel(levelOffset).name())) {
			String currentName = updatesWithCurrentName.get(0).pointer().getPartAtLevel(levelOffset).name();
			currentRepetitions = ((ImmutableRepetitionsImpl) baseAsBuilder.getSubgroupOrEmpty(currentName)).toLazyCopyBuilder();
			for (List<UpdateAction> updatesWithCurrentNameAndRep : groupSortedListBy(updatesWithCurrentName,
					up -> up.pointer().getPartAtLevel(levelOffset).repetitionIndex())) {
				updateRepetition(currentName, updatesWithCurrentNameAndRep);
			}
			if (!currentRepetitions.isUnchangedOriginal()) { // only modify the group baseAsBuilder if the repetitions for currentName
				// was modified at all
				baseAsBuilder.putSubgroup(currentName, currentRepetitions.buildIfChanged());
			}
		}

		return baseAsBuilder.buildIfChanged();
	}

	/**
	 * returns a list of zero-copy sublist views on the sorted list, with each sublist corresponding to a range of elements for
	 * which keyfn returns the same value.
	 */
	private static <T, K> List<List<T>> groupSortedListBy(List<T> list, Function<T, K> keyfn) {
		if (list.isEmpty()) {
			return List.of(list);
		}

		List<List<T>> subLists = new ArrayList<>();

		K batchKey = keyfn.apply(list.get(0));
		int batchStart = 0;
		for (int i = 0; i < list.size(); i++) {
			K currentKey = keyfn.apply(list.get(i));
			if (!Objects.equals(currentKey, batchKey)) {
				subLists.add(list.subList(batchStart, i));
				batchKey = currentKey;
				batchStart = i;
			}
		}
		subLists.add(list.subList(batchStart, list.size()));
		return subLists;
	}

	/**
	 * assumes that the given list is sorted so that there is an index at which it can be split into two sublists, so that
	 * test is false for all elements in the first sublist and true for all elements in the second
	 *
	 * @return two sublist views on the elements of the list, where test evaluates to false on all elements in the first sublist and
	 * 		to true for all elements of the second sublist.
	 * 		(The contents of the lists are not copied for performance.)
	 */
	private static <T> Pair<List<T>, List<T>> splitSortedListByBool(List<T> list, Predicate<T> test) {
		if (list.isEmpty()) {
			return Pair.of(List.of(), List.of());
		}
		int splitIndex = 0;
		while (splitIndex < list.size() && !test.test(list.get(splitIndex))) {
			splitIndex++;
		}
		return Pair.of(list.subList(0, splitIndex), list.subList(splitIndex, list.size()));
	}

	private void updateRepetition(String currentName, List<UpdateAction> updatesWithCurrentNameAndRep) {
		int currentRepetition1Based = updatesWithCurrentNameAndRep.get(0).pointer().getPartAtLevel(levelOffset).repetitionIndex();
		Pair<List<UpdateAction>, List<UpdateAction>> localAndNestedUpdates =
				splitSortedListByBool(updatesWithCurrentNameAndRep, up -> up.pointer().size() > levelOffset + 1);
		// local updates (those that don't apply to a subgroup but directly to the current group):
		for (UpdateAction localUpdate : localAndNestedUpdates.getLeft()) {
			updateSingleAction(currentName, currentRepetition1Based, localUpdate);
		}
		// nested updates:
		List<UpdateAction> nestedUpdatesWithCurrentNameAndRep = localAndNestedUpdates.getRight();
		if (!nestedUpdatesWithCurrentNameAndRep.isEmpty()) {

			GroupInstanceV2 subGroup = currentRepetitions.getOr(currentRepetition1Based, ImmutableGroupV2Impl::emptyInternal);
			currentRepetitions.replaceInPlace(
					currentRepetition1Based,
					batchUpdate(
							(ImmutableGroupV2Impl) subGroup, // we need an AbstractImmutableGroupInstance subclass for batchUpdate
							// to work, so we need to cast. As we only support our own implementation classes, an
							// AbstractImmutableGroupInstance could only be either a ImmutableGroupV2Impl or a document. but
							// repetitions won't contain documents, but subgroups -> we can cast to ImmutableGroupV2Impl
							nestedUpdatesWithCurrentNameAndRep,
							levelOffset + 1));
		}
	}

	private void updateSingleAction(String currentName, int currentRep1Based, UpdateAction localUpdate) {
		assert localUpdate.pointer().size() == levelOffset + 1;
		@SuppressWarnings("unused") // use a switch expression instead of a
		// statement, to get a compile-time completeness check
		int _dummy_force_switch_completeness_check = switch (localUpdate.type()) {

			// this should happen at most one per (currentName, currentRepetition):
			case PUT_FIELD -> applyPutField(currentName, currentRep1Based, localUpdate);
			// this should happen at most one per (currentName, currentRepetition):
			case PUT_GROUP_REPETITION -> applyPutGroupRepetition(currentRep1Based, localUpdate);
		};
	}

	private int applyPutGroupRepetition(int currentRep1Based, UpdateAction localUpdate) {
		if (localUpdate.argGroupInstance() == null) {
			throw new IllegalArgumentException(
					"group update must not be null for PUT_GROUP action, but was for pointer " + localUpdate.pointer());
		}
		currentRepetitions.replaceInPlace(currentRep1Based, localUpdate.argGroupInstance());
		return 42; // dummy value, to make the switch into an expression and get a compile-time completeness check
	}

	private int applyPutField(String currentName, int currentRep1Based, UpdateAction localUpdate) {
		assert currentRep1Based == 1 : "expected repetition 1 for field, but got " + currentRep1Based + " for field "
				+ localUpdate.pointer();
		if (localUpdate.argFieldInstance() == null) {
			throw new IllegalArgumentException(
					"field update must not be null for PUT_FIELD action, but was for pointer " + localUpdate.pointer());
		}
		baseAsBuilder.putField(currentName, localUpdate.argFieldInstance());
		return 43; // dummy value, to make the switch into an expression and get a compile-time completeness check
	}
}
