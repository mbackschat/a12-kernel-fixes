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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import lombok.NonNull;

/**
 * An Immutable {@link Map} with a builder that allows to easily create derived modified maps, with single-entry or batch updates.
 * <p/>
 * The main focus of this implementation (vs simple copy + {@link Collections#unmodifiableMap}) is that the Builder, as well as the
 * static factory methods of the map, are optimized to avoid unnecessary defensive copies of the data-holding underlying map, which
 * are expensive if the map has many entries.<br/>
 * <ul>
 *     <li>The builder enables that multiple updates can be batched and can share a single defensive copy operation, before
 *     	   {@link LazyCopyBuilder#buildIfChanged} is called</li>
 *     <li>The builder ensures that during changes, the <em>underlying map</em> is only copied if and when a change actually occurs.
 *         The <em>builder</em> can therefore be created eagerly (cheaply), which allows the user code to be simpler, avoid case
 *         distinctions and not handle the lazy-copy logic itself.</li>
 *     <li>The static factory methods ensure that during initial construction (from a map of statically-unknown implementation
 *         class), no redundant defensive copy is made if the given map already is an {@link ImmutableMap}</li>
 * </ul>
 * <br/>
 * These optimizations are handled internally, so that users of the map don't need to keep track of when to create defensive copies,
 * and in a way that can <em>rule out</em> accidental <em>incorrectness</em> (i.e. accidental mutability), while making it
 * <em>easy</em> (but not fool-proof) to avoid usage patterns that trigger avoidable expensive copy operations.
 * <p/>
 * Note: the Immutable map + builder pattern can not provide an efficiency gain for single-entry update operations, as these
 * always require exactly one copy of the underlying map. It aims to provide a benefit for batch updates and cases where a change
 * does not actually occur, while being not materially worse for single-entry updates.
 * <p>
 * @implNote A (better) alternative to this copy-based immutable map approach would be an implementation similar to e.g. Clojure's
 * "persistent collections" based on Hash Array Mapped Tries (HAMT), which can achieve constant or {@code log_32(N)} asymptotic cost
 * for updates, instead of {@code O(n)}.<br>
 * For now, we did not take that approach, because it would require either more complex code or an external dependency, and because
 * we currently expect that within A12 Documents, the number of fields or subgroups of an individual group instance is typically
 * low, so that the poor asymptotic complexity for large maps probably does not matter too much in practice (also there is the
 * batchUpdate operation, that can serve as a sort of escape hatch).<br>
 * If the need for good asymptotic performance for large instances should arise in the future, we <em>might</em> be able to
 * use Clojure's implementation internally, as it runs on the jvm and is even written in java. (In that case, one would probably use
 * a custom copy of the clojure code in order to avoid an external dependency?)<br>
 * In case of a HAMT-like implementations, the internal copy step becomes unnecessary, and therefore, there would no longer be a need
 * for a builder. This would likely enable various simplifications (among others, {@link ValidatedImmutableMap} would also become
 * simpler, and batchUpdate might become redundant performance-wise).
 */
// NOTE: in case of modifications to this class, consider TestDocV2Performance, it offers functionality to
// easily and locally record performance measurements for some relevant operations, before and after the
// code change, and to visually plot the results for easy comparison via Analyze_Improvised_Perftests.ipynb
// Note: this class is marked as final, because it is not designed for inheritance, and immutability guarantees might break when
// subclasses ares used
public final class ImmutableMap<K, V> implements Map<K, V> { // Don't use AbstractMap. Its default impls are inperformant, and if we
															 // override all problematic impls, our code will no longer be shorter.
	/**
	 * must be a Collections.unmodifiableMap, so that e.g. the entrySet is unmodifiable too,
	 * and to reduce the risk of accidental leaks of mutable references.
	 */
	private final Map<K, V> unmodifiableWrapped;

	/**
	 * This method should be kept private, because incorrect use could undermine the
	 * immutability of the resulting map, and of objects that build on that immutability,
	 * like derived immutable maps or GroupInstanceV2/DocumentV2
	 */
	private ImmutableMap(@NonNull Map<K, V> delegateGuaranteedToNeverBeModified) {
		assert !(delegateGuaranteedToNeverBeModified instanceof ImmutableMap)
				: "redundant wrapping should be prevented by callers";
		this.unmodifiableWrapped = Collections.unmodifiableMap(delegateGuaranteedToNeverBeModified);
	}

	/**
	 * Returns an {@link ImmutableMap} with the same contents as {@code other}.
	 * If {@code other} is not already an {@code ImmutableMap}, a copy of
	 * other will be created and wrapped in an ImmutableMap. If {@code other}
	 * is already an {@code ImmutableMap}, no copy will be created, i.e. in that
	 * case the operation will be cheap even for large maps.
	 */
	public static <K, V> ImmutableMap<K, V> ofPotentiallyMutableMap(@NonNull Map<K, V> other) {
		if (other instanceof ImmutableMap<K, V> otherImmutableMap) {
			return otherImmutableMap;
		}
		Map<K, V> delegateGuaranteedToNeverBeModified = Collections.unmodifiableMap(new HashMap<>(other));
		return new ImmutableMap<>(delegateGuaranteedToNeverBeModified);
	}

	public static <K, V> ImmutableMap<K, V> empty() {
		return new ImmutableMap<>(Collections.emptyMap());
	}

	public LazyCopyBuilder<K, V> toLazyCopyBuilder() {
		return new LazyCopyBuilder<>(this);
	}

	/**
	 * individual <em>builder</em> instances are <em>not</em> thread safe
	 */
	public static class LazyCopyBuilder<K, V> {
		private final @NonNull ImmutableMap<K, V> original;
		/**
		 * no reference to this map must be leaked, because it will be passed to the new
		 * ImmutableMap during buildIfChanged(), without further defensive copy
		 */
		private Map<K, V> modifiedInternal = null;
		/**
		 * must never be thawed once frozen, because the modified map will be used in the created new ImmutableMap, which relies
		 * on the fact that it will never change
		 */
		private boolean isFrozen = false;

		private LazyCopyBuilder(@NonNull ImmutableMap<K, V> original) {
			this.original = original;
		}

		public LazyCopyBuilder<K, V> put(K key, V value) {
			preModifyChecksAndInit();
			modifiedInternal.put(key, value);
			return this;
		}

		public LazyCopyBuilder<K, V> remove(K key) {
			preModifyChecksAndInit();
			modifiedInternal.remove(key);
			return this;
		}

		public ImmutableMap<K, V> buildIfChanged() {
			isFrozen = true;
			if (modifiedInternal != null) {
				// in this case we must guarantee that the builder will never mutate 'modified' again and never leak a
				// mutable reference to it

				// not necessary, but
				// 1. is cheap
				// 2. extra safety if `isFrozen` is accidentally ignored
				modifiedInternal = Collections.unmodifiableMap(modifiedInternal);
				return new ImmutableMap<>(modifiedInternal);
			} else {
				return original;
			}
		}

		private void preModifyChecksAndInit() {
			if (isFrozen) {
				// no modifications must be made after freeze, because the internal map has then already been transferred
				// to a new ImmutableMap instance, which relies on it never to be mutated again
				throw new IllegalStateException("this builder is already frozen and cannot be modified anymore");
			}
			if (modifiedInternal == null) {
				modifiedInternal = new HashMap<>(original);
			}
		}

		public V get(K key) {
			return getEffectiveMapInternal().get(key);
		}

		public V getOrDefault(K key, V defaultValue) {
			return getEffectiveMapInternal().getOrDefault(key, defaultValue);
		}

		public int size() {
			return getEffectiveMapInternal().size();
		}

		/**
		 * @return an unmodifiable view of the (possibly modifiable) key set of the builder.
		 * (May or may not update in-place when the builder gets updated.)
		 */
		public Set<K> keySet() {
			return Collections.unmodifiableSet(getEffectiveMapInternal().keySet());
		}

		/**
		 * the returned map must remain internal to this builder, see {@link #modifiedInternal} for details
		 */
		private Map<K, V> getEffectiveMapInternal() {
			return modifiedInternal != null ? modifiedInternal : original;
		}

		/**
		 * @return true if the map is guaranteed to be unchanged, false if it may have been changed.
		 */
		public boolean isUnchangedOriginal() {
			return modifiedInternal == null;
		}
	}

	@Override public int size() {
		return unmodifiableWrapped.size();
	}

	@Override public boolean isEmpty() {
		return unmodifiableWrapped.isEmpty();
	}

	@Override public boolean containsKey(Object key) {
		return unmodifiableWrapped.containsKey(key);
	}

	@Override public boolean containsValue(Object value) {
		return unmodifiableWrapped.containsValue(value);
	}

	@Override public V get(Object key) {
		return unmodifiableWrapped.get(key);
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
		// the additional wrapping in unmodifiableMap should be redundant, but is cheap and more robust against
		// accidental modifiability
		return Collections.unmodifiableMap(unmodifiableWrapped).keySet();
	}

	@Override @NonNull public Collection<V> values() {
		// the additional wrapping in unmodifiableMap should be redundant, but is cheap and more robust against
		// accidental modifiability
		return Collections.unmodifiableMap(unmodifiableWrapped).values();
	}

	@Override @NonNull public Set<Entry<K, V>> entrySet() {
		// the additional wrapping in unmodifiableMap should be redundant, but is cheap and more robust against
		// accidental modifiability
		return Collections.unmodifiableMap(unmodifiableWrapped).entrySet();
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass()) // this is not compliant with the Map interface, but here it seems better to be strict and reject equality with non-immutable maps
			return false;
		ImmutableMap<?, ?> that = (ImmutableMap<?, ?>) o;
		return Objects.equals(unmodifiableWrapped, that.unmodifiableWrapped);
	}

	@Override public int hashCode() {
		return Objects.hashCode(unmodifiableWrapped);
	}

	@Override public String toString() {
		return unmodifiableWrapped.toString();
	}
}
