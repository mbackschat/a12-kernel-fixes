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

package com.mgmtp.a12.kernel.core.base.utils.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;

/**
 * A collection of useful java 8 Helperfunctions.
 */
public class Java8Utils {

	private Java8Utils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Applies a function to all values of a map creating a new map with the same keys.
	 *
	 * @param map
	 * 		the source map.
	 * @param mapper
	 * 		the function to be applied.
	 * @return the resulting map with the same keys an the transformed values.
	 */
	public static <T, R, S> Map<T, S> mapMapValues(Map<T, R> map, Function<R, S> mapper) {
		return map.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, mapper.compose(Entry::getValue)));
	}

	public static <T, R, S> Map<T, S> mapMapValuesPreserveOrder(Map<T, R> map, Function<R, S> mapper) {
		LinkedHashMap<T, S> newMap = new LinkedHashMap<>(map.size());
		map.forEach((k, v) -> newMap.put(k, mapper.apply(v)));
		return newMap;
	}

	/**
	 * Maps a list with the given function and returns the result as a list.
	 */
	public static <I, O> List<O> mapList(List<I> l, Function<I, O> fn) {
		return l.stream().map(fn).collect(Collectors.toList());
	}

	public static <T> List<T> filterList(List<T> l, Predicate<T> test) {
		return l.stream().filter(test).toList();
	}

	public static <T> List<T> appendList(List<T> list, T elem) {
		List<T> newList = new ArrayList<>(list);
		newList.add(elem);
		return newList;
	}

	@SafeVarargs public static <T> List<T> concatToList(Collection<? extends T>... colls) {
		return Arrays.stream(colls).flatMap(Collection::stream).collect(Collectors.toCollection(ArrayList::new));
	}

	public static <T> T getLast(List<T> list) {
		return list.get(list.size() - 1);
	}

	public static <T> List<T> subList(List<T> list, int fromIndex, int toIndex) {
		fromIndex = Math.max(fromIndex, 0);
		toIndex = Math.clamp(toIndex, fromIndex, list.size());

		return list.subList(fromIndex, toIndex);
	}

	public static <T> List<T> getAllButLast(List<T> list) {
		return list.subList(0, list.size() - 1);
	}

	public static <T> List<T> changeAt(List<T> list, int index, T value) {
		assert index < list.size() : "index out of bounds";
		ArrayList<T> newList = new ArrayList<>(list);
		newList.set(index, value);
		return newList;
	}

	public static <T> List<T> changeLast(List<T> list, T value) {
		return changeAt(list, list.size() - 1, value);
	}

	/**
	 * @return true if l1 is a prefix of, or equal to, l2
	 */
	public static boolean isPrefix(List<String> l1, List<String> l2) {
		return l1.isEmpty() || (l1.size() <= l2.size() && l1.equals(l2.subList(0, l1.size())));
	}

	/**
	 * @return all prefixes of l, <em>including the empty list and l itself</em>
	 */
	public static <E> List<List<E>> prefixes(List<E> l) {
		List<List<E>> prefixes = new ArrayList<>();
		for (int i = 0; i <= l.size(); i++) {
			prefixes.add(l.subList(0, i));
		}
		return prefixes;
	}

	@SafeVarargs public static <T> Stream<T> concatStreams(Stream<? extends T> s1, Stream<? extends T>... rest) {
		Stream<T> result = Stream.concat(Stream.of(), s1);
		for (Stream<? extends T> stream : rest) {
			result = Stream.concat(result, stream);
		}
		return result;
	}

	/**
	 * Eliminates the problem of {@link HashMap#merge(Object, Object, BiFunction)} throwing a
	 * {@link NullPointerException} if the value is <code>null</code>. This is used in the
	 * {@link Java8Utils#toNullableMap(Function, Function)} and
	 * {@link Java8Utils#toMap(Function, Function, BinaryOperator)} methods.
	 */
	private static class NullMergeableHashMap<K, V> extends HashMap<K, V> {
		private static final long serialVersionUID = -6695505014590198703L;

		@Override
		public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
			if (value != null) {
				return super.merge(key, value, remappingFunction);
			}
			// value == null
			if (super.containsKey(key)) {
				value = remappingFunction.apply(super.get(key), null);
			}
			put(key, value);
			return value;
		}
	}

	/**
	 * From {@link Collectors#throwingMerger()}.
	 * <p>
	 * Returns a merge function, suitable for use in {@link Map#merge(Object, Object, BiFunction)
	 * Map.merge()} or {@link #toMap(Function, Function, BinaryOperator) toMap()}, which always
	 * throws {@code IllegalStateException}. This can be used to enforce the assumption that the
	 * elements being collected are distinct.
	 *
	 * @param <T>
	 * 		the type of input arguments to the merge function
	 * @return a merge function which always throw {@code IllegalStateException}
	 *
	 * @see Collectors#throwingMerger()
	 */
	private static <T> BinaryOperator<T> throwingMerger() {
		return (u, v) -> {
			throw new IllegalStateException(String.format("Duplicate key %s", u));
		};
	}

	/**
	 * In comparison to {@link Collectors#toMap(Function, Function)}, also null-Values are allowed.
	 *
	 * @see Collectors#toMap(Function, Function)
	 */
	public static <T, K, U> Collector<T, ?, Map<K, U>> toNullableMap(Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper) {
		return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), NullMergeableHashMap::new);
	}

	/**
	 * creates a map from elems, according to keyExtractor. Validates that no duplicate keys occur.
	 */
	public static <E, K> Map<K, E> associateBy(Collection<E> elems, Function<E, K> keyExtractor) {
		return elems.stream().collect(toMap(keyExtractor, e -> e,
				(a, b) -> {
					throw new IllegalArgumentException("duplicate key for elems " + a + " and " + b);
				}));
	}

	/**
	 * In comparison to {@link Collectors#toMap(Function, Function, BinaryOperator)}, also
	 * null-Values are allowed.
	 *
	 * @see Collectors#toMap(Function, Function, BinaryOperator)
	 */
	public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper,
			BinaryOperator<U> mergeFunction) {
		return Collectors.toMap(keyMapper, valueMapper, mergeFunction, NullMergeableHashMap::new);
	}

	/**
	 * In comparison to {@link Collectors#toMap(Function, Function, BinaryOperator, Supplier)}, the
	 * default throwing Merger will be used as merger. Therefore no duplicate keys are allowed.
	 *
	 * @see Collectors#toMap(Function, Function, BinaryOperator, Supplier)
	 */
	public static <T, K, U> Collector<T, ?, TreeMap<K, U>> toTreeMap(Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper) {
		return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), TreeMap::new);
	}

	/**
	 * In comparison to {@link Collectors#toMap(Function, Function, BinaryOperator, Supplier)}, the
	 * default throwing Merger will be used as merger. Therefore no duplicate keys are allowed.
	 *
	 * @see Collectors#toMap(Function, Function, BinaryOperator, Supplier)
	 */
	public static <T, K, U> Collector<T, ?, LinkedHashMap<K, U>> toLinkedHashMap(Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper) {
		return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), LinkedHashMap::new);
	}

	/**
	 * In comparison to {@link Collectors#toMap(Function, Function)}, a LinkedHashMap is generated.
	 *
	 * @see Collectors#toMap(Function, Function)
	 */
	public static <T, K> Collector<T, ?, LinkedHashMap<K, List<T>>> groupingByLinkedHashMap(
			Function<? super T, ? extends K> classifier) {
		return Collectors.groupingBy(classifier, LinkedHashMap::new, Collectors.toList());
	}

	/**
	 * Parallel iteration of two streams. (See <a href=
	 * "http://stackoverflow.com/questions/17640754/zipping-streams-using-jdk8-with-lambda-java-util-
	 * stream-streams-zip">Stackoverflow: Zipping streams using JDK8 with lambda
	 * (java.util.stream.Streams.zip)</a>.
	 * <p>
	 * Adapted to fill remaining elements of the shorter stream with <code>null</code>.
	 *
	 * @param a
	 * 		the first stream to zip
	 * @param b
	 * 		the second stream to zip
	 * @param zipper
	 * 		the zipper function combining the two streams to a new Object
	 * @return a stream of the target type of the zipper.
	 */
	public static <A, B, C> Stream<C> zip(Stream<? extends A> a,
			Stream<? extends B> b,
			BiFunction<? super A, ? super B, ? extends C> zipper) {
		Objects.requireNonNull(zipper);
		var aSpliterator = Objects.requireNonNull(a).spliterator();
		var bSpliterator = Objects.requireNonNull(b).spliterator();

		// Zipping looses DISTINCT and SORTED characteristics
		int characteristics = aSpliterator.characteristics() & bSpliterator.characteristics() &
				~(Spliterator.DISTINCT | Spliterator.SORTED);

		long zipSize = ((characteristics & Spliterator.SIZED) != 0)
				? Math.max(aSpliterator.getExactSizeIfKnown(), bSpliterator.getExactSizeIfKnown())
				: -1;

		Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
		Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
		Iterator<C> cIterator = new Iterator<>() {
			@Override
			public boolean hasNext() {
				return aIterator.hasNext() || bIterator.hasNext();
			}

			@Override
			public C next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				A a = aIterator.hasNext() ? aIterator.next() : null;
				B b = bIterator.hasNext() ? bIterator.next() : null;
				return zipper.apply(a, b);
			}
		};

		Spliterator<C> split = Spliterators.spliterator(cIterator, zipSize, characteristics);
		return (a.isParallel() || b.isParallel())
				? StreamSupport.stream(split, true)
				: StreamSupport.stream(split, false);
	}

	public static <A> Stream<Pair<Integer, A>> zipWithIndex(Stream<? extends A> a) {
		var aSpliterator = Objects.requireNonNull(a).spliterator();

		// Zipping looses DISTINCT and SORTED characteristics
		int characteristics = aSpliterator.characteristics() & ~(Spliterator.DISTINCT | Spliterator.SORTED);

		long zipSize = ((characteristics & Spliterator.SIZED) != 0)
				? aSpliterator.getExactSizeIfKnown()
				: -1;

		Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
		Iterator<Pair<Integer, A>> cIterator = new Iterator<>() {
			int index = 0;

			@Override
			public boolean hasNext() {
				return aIterator.hasNext();
			}

			@Override
			public Pair<Integer, A> next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				A a = aIterator.hasNext() ? aIterator.next() : null;
				return Pair.of(index++, a);
			}
		};

		Spliterator<Pair<Integer, A>> split = Spliterators.spliterator(cIterator, zipSize, characteristics);
		return StreamSupport.stream(split, false);
	}

	public static <A, B> void forEachTogether(Collection<A> collectionA, Collection<B> collectionB,
			BiConsumer<? super A, ? super B> action) {
		Objects.requireNonNull(collectionA);
		Objects.requireNonNull(collectionB);
		if (collectionA.size() != collectionB.size()) {
			throw new IllegalArgumentException("Collections iterated together must have the same number of elements.");
		}
		forEachTogether(collectionA.iterator(), collectionB.iterator(), action);
	}

	public static <A, B> void forEachTogether(Iterator<A> iteratorA, Iterator<B> iteratorB,
			BiConsumer<? super A, ? super B> action) {
		Objects.requireNonNull(iteratorA);
		Objects.requireNonNull(iteratorB);
		while (iteratorA.hasNext() || iteratorB.hasNext()) {
			var a = iteratorA.hasNext() ? iteratorA.next() : null;
			var b = iteratorB.hasNext() ? iteratorB.next() : null;
			action.accept(a, b);
		}
	}

	public static <T> void forEachCombination(List<T> elements, BiConsumer<? super T, ? super T> action) {
		Objects.requireNonNull(elements);
		for (int i = 0; i < elements.size(); i++) {
			var elementA = elements.get(i);
			for (int j = i + 1; j < elements.size(); j++) {
				var elementB = elements.get(j);
				action.accept(elementA, elementB);
			}
		}
	}

	/**
	 * Put the given key as the first entry of the map. The resulting map will be a
	 * {@link LinkedHashMap}.
	 *
	 * @param map
	 * 		an ordered map.
	 * @param first
	 * 		the element, that should be first in the iteration.
	 * @return a {@link LinkedHashMap}, containing all elements of map in the same order, except
	 * 		that first will be the key of the first element.
	 */
	public static <T, S> LinkedHashMap<T, S> putKeyToStart(LinkedHashMap<T, S> map, T first) {
		if (!map.containsKey(first)) {
			return map;
		}
		LinkedHashMap<T, S> result = new LinkedHashMap<>();
		result.put(first, map.get(first));
		map.entrySet().stream()
				.filter(e -> !first.equals(e.getKey()))
				.forEach(e -> result.put(e.getKey(), e.getValue()));
		return result;
	}

	/**
	 * Join multiple streams to one stream.
	 * Uses {@link Stream#concat(Stream, Stream)} multiple times.
	 *
	 * @param streams
	 * 		the streams to join. Must not be empty or null.
	 * @return the joined streams.
	 *
	 * @throws IllegalArgumentException
	 * 		if the streams parameter is empty.
	 * @throws NullPointerException
	 * 		if the streams parameter is <code>null</code>.
	 */
	// The two annotations are ugly, but eclipse and javac need different things. :(
	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <T> Stream<T> joinStreams(Stream<? extends T>... streams) {
		return Arrays.stream(streams).flatMap(Function.identity());
	}

	/**
	 * Join the given maps to one bigger map. If a key is contained in multiple maps, an
	 * {@link IllegalStateException} will be thrown.
	 *
	 * @param maps
	 * 		the maps to join.
	 * @return the joined maps as a single map.
	 */
	// The two annotations are ugly, but eclipse and javac need different things. :(
	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <K, V> Map<K, V> joinMaps(Map<? extends K, ? extends V>... maps) {
		return Arrays.stream(maps)
				.flatMap(m -> m.entrySet().stream())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	public static <T> Set<T> setIntersection(Set<T> set, Set<T> allNonleafNodes) {
		return set.stream().filter(allNonleafNodes::contains).collect(Collectors.toSet());
	}

	public static <T> Set<T> setDifference(Collection<? extends T> minuend, Collection<? extends T> subtrahend) {
		Set<T> result = new HashSet<>(minuend);
		result.removeAll(subtrahend);
		return result;
	}

	@SafeVarargs
	public static <T> Set<T> setUnion(Collection<T> a, Collection<T>... rest) {
		Set<T> union = new HashSet<>(a);
		for (Collection<T> each : rest) {
			union.addAll(each);
		}
		return union;
	}

	/**
	 * Reduces a stream of optional elements to an optional list, which is {@link Optional#empty()}
	 * if one of the elements of the stream is {@link Optional#empty()}.
	 *
	 * @param stream
	 * 		the stream producing the optional elements.
	 * @return the optional list of elements.
	 */
	public static <T> Optional<List<T>> reduceToOptionalList(Stream<Optional<T>> stream) {
		List<Optional<T>> listOfOptionals = stream.collect(Collectors.toList());
		if (listOfOptionals.stream().anyMatch(o -> !o.isPresent())) {
			return Optional.empty();
		}
		return Optional.of(listOfOptionals.stream().map(Optional::get).collect(Collectors.toList()));
	}

	@SafeVarargs
	public static <T> Set<T> toSet(T... values) {
		HashSet<T> result = new HashSet<>();
		Collections.addAll(result, values);
		return result;
	}

	public static <T, U> void addToListMultiMap(Map<T, List<U>> ret, T key, U value) {
		addToMultiMap(ret, key, value, ArrayList::new);
	}

	public static <T, U> void addToSetMultiMap(Map<T, Set<U>> ret, T key, U value) {
		addToMultiMap(ret, key, value, HashSet::new);
	}

	public static <T, U, V extends Collection<U>> void addToMultiMap(Map<T, V> ret, T key, U value, Supplier<V> constructor) {
		ret.computeIfAbsent(key, k -> constructor.get()).add(value);
	}

	public static String getStackTraceAsString(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public static <T, C> Map<C, Set<T>> findDuplicates(Stream<T> values, Function<T, C> comparableMapper) {
		Map<C, Set<T>> duplicates = new HashMap<>();
		Map<C, T> present = new HashMap<>();
		values.forEach(s -> {
			C comp = comparableMapper.apply(s);
			if (present.put(comp, s) != null) {
				duplicates.computeIfAbsent(comp, k -> new HashSet<>(Collections.singleton(present.get(comp))))
						.add(s);
			}
		});
		return duplicates;
	}

	public static int compare(int[] a, int[] b) {
		for (int i = 0; i < a.length && i < b.length; i++) {
			int c = a[i] - b[i];
			if (c != 0) {
				return c;
			}
		}
		// both are equal up to the length of the shorter one
		// -> if the lengths are the same, they are fully equal -> return 0
		// -> if the lengths differ, the shorter one counts as 'smaller'
		return a.length - b.length;
	}

	public static List<Integer> toBoxedList(int[] array) {
		return Arrays.stream(array).boxed().collect(Collectors.toList());
	}

	public static long[] toLongArray(List<Long> list) {
		return list.stream().mapToLong(i -> i).toArray();
	}

	public static List<Long> copyOfRangeAsList(long[] elems, int startIdx, int stopIdx) {
		List<Long> l = new ArrayList<>();
		for (int i = startIdx; i < stopIdx; i++) {
			l.add(elems[i]);
		}
		return l;
	}

	public static boolean contains(long[] array, long value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) {
				return true;
			}
		}
		return false;
	}

	public static <T> List<Pair<T, T>> pairs(List<T> elems) {
		if (elems.size() % 2 != 0) {
			throw new IllegalArgumentException("expected even number of elems but got " + elems.size());
		}
		List<Pair<T, T>> pairs = new ArrayList<>(elems.size() / 2);
		for (int i = 0; i + 1 < elems.size(); i += 2) {
			pairs.add(Pair.of(elems.get(i), elems.get(i + 1)));
		}
		assert pairs.size() == elems.size() / 2;
		return pairs;
	}

	public static <T> List<Pair<T, T>> pairs(T[] elems) {
		return pairs(Arrays.asList(elems));
	}

	public static <T> List<T> sorted(Collection<T> c, Comparator<T> comparator) {
		return c.stream().sorted(comparator).collect(Collectors.toList());
	}

	/**
	 * A replacement for {@link Stream#allMatch(Predicate)} where all elements are evaluated. The default for
	 * {@link Stream#allMatch(Predicate)} is a shortcut solution where the first negaive evaluation stops the stream evaluation.
	 *
	 * @param stream
	 * 		the boolean stream to be checked.
	 * @return <code>true</code> if all elements of the stream are true.
	 */
	public static <T> boolean allMatchEvaluateAll(Stream<T> stream, Predicate<T> filter) {
		return stream
				.map(filter::test)
				.reduce(true, (a, b) -> a && b);
	}

	/**
	 * Note: the order (and therefore indexes) are only deterministic if the iterator of the collection supplies them in
	 * deterministic order
	 */
	public static <E, C extends Collection<E>> void forEachIndexed(C coll, BiConsumer<Integer, E> action) {
		int i = 0;
		for (E e : coll) {
			action.accept(i, e);
			i++;
		}
	}

	public static <A, B> B cascadeNulls(A a, Function<A, B> fn) {
		return a != null ? fn.apply(a) : null;
	}

	/**
	 * @param returnValForUniversalSet
	 * 		the value to return if the collector is used on an empty stream (the intersection of zero
	 * 		Sets is the universal set, which is impractical to represent as a java.util.Set for a generic element type T)
	 */
	public static <T> Collector<Collection<? extends T>, SetOrUniversalSet<T>, Set<T>> setIntersectionCollector(
			Set<T> returnValForUniversalSet) {
		return Collector.of(
				SetOrUniversalSet::universalSet,
				(acc, list) -> acc.retainAll(new HashSet<>(list)),
				(left, right) -> {
					left.retainAll(right);
					return left;
				},
				setOrUniversalSet -> setOrUniversalSet.isUniversalSet() ?
						returnValForUniversalSet :
						setOrUniversalSet.toFiniteSet(),
				Collector.Characteristics.UNORDERED
		);
	}

	/**
	 * only intended for internal use with {@link #setIntersectionCollector(Set)}
	 */
	public static class SetOrUniversalSet<T> {
		/**
		 * {code null} represents the universal set (which contains all possible values of type T).
		 * Note that setOrNull != null does not imply that the represented set if unequal to the universal set:
		 * If T allows a finite number of values, then setOrNull could also explicitly contain all of them (e.g. for enums)
		 */
		private Set<T> setOrNull;

		private SetOrUniversalSet(Set<T> setOrNull) {
			this.setOrNull = setOrNull;
		}

		// note: we don't return a boolean because we cannot always tell if the set has changed
		public void retainAll(Set<T> other) {
			if (this.setOrNull == null) {
				this.setOrNull = new HashSet<>(other);
			} else {
				this.setOrNull.retainAll(other);
			}
		}

		public void retainAll(SetOrUniversalSet<T> other) {
			if (other.setOrNull != null) {
				retainAll(other.setOrNull);
			}
			// otherwise nothing to change, because other is the universal set which contains all elements
		}

		/**
		 * Note: there is a case where this method returns false even though the set <em>is</em> equal to the universal set:
		 * If the element type of the set is finite (e.g. an enum), and if the wrapped java.util.Set contains all possible values
		 * of that type/enum
		 */
		public boolean isUniversalSet() {
			return setOrNull == null;
		}

		public Set<T> toFiniteSet() {
			if (setOrNull == null) {
				throw new IllegalStateException(
						"cannot export universal set - it was probably not intersected with any other elements");
			}
			return setOrNull;
		}

		public static <T> SetOrUniversalSet<T> of(Collection<T> elems) {
			return new SetOrUniversalSet<>(new HashSet<>(elems));
		}

		public static <T> SetOrUniversalSet<T> universalSet() {
			return new SetOrUniversalSet<>(null);
		}
	}
}
