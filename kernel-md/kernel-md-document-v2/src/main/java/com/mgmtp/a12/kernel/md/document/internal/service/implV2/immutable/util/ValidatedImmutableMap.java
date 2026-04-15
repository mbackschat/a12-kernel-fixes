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

package com.mgmtp.a12.kernel.md.document.internal.service.implV2.immutable.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import lombok.NonNull;

/**
 * An immutable map similar to {@link ImmutableMap}, that additionally enforces some invariants or normalizations for its keys and
 * values (defined by a {@link MapInvariantsAndNormalizations} instance or subclass-singleton).
 * It avoids unnecessary redundant validations during construction from other maps, by checking if the other map is already
 * an {@link ValidatedImmutableMap} with equal invariants. During creation of 'updated' variants of the map via {@link LazyCopyBuilder},
 * redundant validations are avoided by only checking newly added entries.
 */
// NOTE: in case of modifications to this class, consider TestDocV2Performance, it offers functionality to
// easily and locally record performance measurements for some relevant operations, before and after the
// code change, and to visually plot the results for easy comparison via Analyze_Improvised_Perftests.ipynb
public class ValidatedImmutableMap<K, V, I extends MapInvariantsAndNormalizations<K, V, I>> implements Map<K, V> {
	private final ImmutableMap<K, V> map;
	private final I invariantsAndNormalizations;

	/**
	 * Must only be called internally, with a map that is already validated and normalized according to invariantsAndNormalizations.
	 * Does not re-check the given map, because redundant checks would hurt performance during updates.
	 */
	private ValidatedImmutableMap(@NonNull ImmutableMap<K, V> mapThatIsAlreadyNormalizedAndValidated,
			@NonNull I invariantsAndNormalizations) {
		this.map = mapThatIsAlreadyNormalizedAndValidated;
		this.invariantsAndNormalizations = invariantsAndNormalizations;
	}

	/**
	 * Returns a ValidatedImmutableMap that satisfies the given {@code invariantsAndNormalizations} and has the (normalized) contents of the
	 * {@code other} map, or throws an error if the map's contents are invalid according to {@code invariantsAndNormalizations}
	 * (the exact exception is defined in {@code invariantsAndNormalizations}'s check methods)
	 * <p/>
	 * The operation is cheap if the given map is already a ValidatedImmutableMap with equal MapInvariantsAndNormalizations (it can
	 * simply be returned as-is), otherwise a copy is created, which can be somewhat expensive, in particular for large maps.
	 */
	public static <K, V, I extends MapInvariantsAndNormalizations<K, V, I>> ValidatedImmutableMap<K, V, I> ofPotentiallyUncheckedMap(
			@NonNull Map<K, V> other,
			@NonNull I invariantsAndNormalizations
	) {
		if (other instanceof ValidatedImmutableMap<?, ?, ?> that
				// the actual comparison invariantsAndNormalizations is done dynamically; the type parameter and subclasses
				// only helps in _some_ cases to get the compiler to highlight wrong use.
				&& invariantsAndNormalizations.equals(that.invariantsAndNormalizations)
		) {
			return (ValidatedImmutableMap<K, V, I>) other;
		}
		assert !(other instanceof ImmutableMap<K, V>) : "potentially unnecessary inefficient code path, and ImmutableMap was" +
				" given, but possibly an ValidatedImmutableMap has been available to the caller";
		Map<K, V> normalizedCopy = new HashMap<>();
		for (var entry : other.entrySet()) {
			K key = entry.getKey();
			V value = entry.getValue();
			if (!invariantsAndNormalizations.treatLikeAbsent(value)) {
				// note: here we could potentially skip the creation of the new map, if the original was immutable and no values
				// are filtered out, but this code path is not expected to be on the performance-relevant path of batchUpdate
				// or withFieldValue / withGroup etc.
				invariantsAndNormalizations.checkKey(key);
				invariantsAndNormalizations.checkValue(value);
				normalizedCopy.put(key, value);
			}
		}
		return new ValidatedImmutableMap<>(
				ImmutableMap.ofPotentiallyMutableMap(normalizedCopy),
				invariantsAndNormalizations);
	}

	public static <K, V, I extends MapInvariantsAndNormalizations<K, V, I>> ValidatedImmutableMap<K, V, I> empty(
			@NonNull I invariantsAndNormalizations
	) {
		// no invariant checks need to be done, because (currently) all possible checks/normalizations can only be applied to
		// _existing_ entries (and an empty map has no entries)
		return new ValidatedImmutableMap<>(
				ImmutableMap.empty(),
				invariantsAndNormalizations);
	}

	public LazyCopyBuilder<K, V, I> toLazyCopyBuilder() {
		return new LazyCopyBuilder<>(this.map.toLazyCopyBuilder(), this.invariantsAndNormalizations);
	}

	/**
	 * individual <em>builder</em> instances are <em>not</em> thread safe
	 */
	public static class LazyCopyBuilder<K, V, I extends MapInvariantsAndNormalizations<K, V, I>> {
		private final ImmutableMap.LazyCopyBuilder<K, V> wrappedBuilder;
		private final I invariantsAndNormalizations;

		private LazyCopyBuilder(@NonNull ImmutableMap.LazyCopyBuilder<K, V> wrappedBuilder,
				@NonNull I invariantsAndNormalizations) {
			this.wrappedBuilder = wrappedBuilder;
			this.invariantsAndNormalizations = invariantsAndNormalizations;
		}

		public ValidatedImmutableMap<K, V, I> buildIfChanged() {
			// note: we could skip builds if nothing changed, but the ImmutableMap code already does this,
			// so it would only save us the wrapper object creation
			return new ValidatedImmutableMap<>(wrappedBuilder.buildIfChanged(), invariantsAndNormalizations);
		}

		public LazyCopyBuilder<K, V, I> put(K key, V value) {
			if (invariantsAndNormalizations.treatLikeAbsent(value)) {
				remove(key);
			} else {
				invariantsAndNormalizations.checkKey(key);
				invariantsAndNormalizations.checkValue(value);
				wrappedBuilder.put(key, value);
			}
			return this;
		}

		public LazyCopyBuilder<K, V, I> remove(K key) {
			// no need to check the key, because it won't be added to the map
			wrappedBuilder.remove(key);
			return this;
		}

		/**
		 * see {@link ValidatedImmutableMap#get} (but for the current state of the builder, vs the frozen contents of the map)
		 */
		public V get(K key) {
			// no need to check the key, because it won't be added to the map
			// (one could argue that the key should be checked in order to not return a substituteAbsent value for an illegal key;
			//  but this seems not very important, and undesirable performance-wise)
			return wrappedBuilder.getOrDefault(key, invariantsAndNormalizations.substituteAbsent());
		}

		public Set<K> keySet() {
			return wrappedBuilder.keySet();
		}

		public int size() {
			return wrappedBuilder.size(); // size is correct as-is, because treatLikeAbsent is applied at insert/construction time
		}

		/**
		 * @return true if the map is guaranteed to be unchanged, false if it may have been changed.
		 */
		public boolean isUnchangedOriginal() {
			return wrappedBuilder.isUnchangedOriginal();
		}
	}

	/**
	 * See the overridden return description for how this differs from standard {@link Map#get}
	 *
	 * @return the value associated with key, or the default value specified by this map's
	 *        {@link MapInvariantsAndNormalizations#substituteAbsent()} if the key is not contained in the map.
	 */
	// note: we need to provide a description part in the javadoc in addition to the @returns description, to override the
	// inherited description from java.util.Map, which does not apply here.
	@Override public V get(Object key) {
		return map.getOrDefault(key, invariantsAndNormalizations.substituteAbsent());
	}

	@Override public int size() {
		return map.size();
	}

	@Override public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override public V put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override public V remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	@Override public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override @NonNull public Set<K> keySet() {
		return map.keySet();
	}

	@Override @NonNull public Collection<V> values() {
		return map.values();
	}

	@Override @NonNull public Set<Entry<K, V>> entrySet() {
		return map.entrySet();
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass()) // this is not compliant with the Map interface, but here, for now, it seems
			// safer to be strict and reject equality with less specific map impls, particularly because we want to compare
			// invariantsAndNormalizations too.
			return false;
		ValidatedImmutableMap<?, ?, ?> that = (ValidatedImmutableMap<?, ?, ?>) o;
		return Objects.equals(map, that.map) && Objects.equals(invariantsAndNormalizations,
				that.invariantsAndNormalizations);
	}

	@Override public int hashCode() {
		return Objects.hash(map, invariantsAndNormalizations);
	}
}
