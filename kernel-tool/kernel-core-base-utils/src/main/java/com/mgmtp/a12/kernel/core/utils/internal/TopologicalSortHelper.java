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

package com.mgmtp.a12.kernel.core.utils.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;

/**
 * Utility-Klasse zur topologischen Sortierung von Graphen.
 */
public class TopologicalSortHelper {

	private TopologicalSortHelper() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Hilfsklasse um das Ergebnis der topologischen Sortierung zu speichern. Ergebnis ist entweder
	 * die Liste der topologisch sortierten Knoten {@link #result} oder im Fehlerfall ein gefundenen
	 * Zyklus. Falls mehrere Zyklen im Graphen vorhanden sind, wird trotzdem aus Gründen der
	 * Darstellbarkeit in einer Fehlermeldung und aus Aufwandsgründen nur ein Zyklus gemeldet.
	 *
	 * @param <T>
	 */
	public static class TopologicalSortResult<T> {
		private List<T> result;
		private List<T> cycle;

		private TopologicalSortResult(List<T> result, List<T> cycle) {
			this.result = result;
			this.cycle = cycle;
		}

		public boolean isSuccessful() {
			return result != null;
		}

		public List<T> getSort() {
			return result;
		}

		public List<T> getCycle() {
			return cycle;
		}
	}

	/**
	 * Ermittelt alle Zielknoten ohne Kante.
	 *
	 * @param graph
	 * @return sortierte Menge der Zielknoten ohne Kante
	 */
	private static <T extends Comparable<T>> TreeSet<T> ermittleZielKnotenOhneKante(Map<T, Set<T>> graph) {
		TreeSet<T> result = new TreeSet<>();
		for (Entry<T, Set<T>> kanten : graph.entrySet()) {
			if (kanten.getValue().isEmpty()) {
				result.add(kanten.getKey());
			}
		}
		return result;
	}

	/**
	 * Erzeugt einen transponierten Graphen, d.h. in der Datenstruktur als Map mit key-value Paaren
	 * wird die Key-Value Beziehung einfach umgedreht.
	 *
	 * @param graph
	 * @return neuer transponierter Graph
	 */
	private static <T extends Comparable<T>> Map<T, Set<T>> erzeugeInvertiertenGraph(Map<T, Set<T>> graph) {
		Map<T, Set<T>> invertedGraph = new HashMap<>();
		for (Entry<T, Set<T>> kanten : graph.entrySet()) {
			T kKnoten = kanten.getKey();

			for (T vKnoten : kanten.getValue()) { //key wird zu value,  value wird zu key
				Java8Utils.addToSetMultiMap(invertedGraph, vKnoten, kKnoten);
			}
		}
		return invertedGraph;
	}

	/**
	 * Erzeugt eine Topologische Sortierung eines gerichteten Graphen. Zusätzlich werden die Knoten
	 * noch nach natürlicher Ordnung der Knoten sortiert, um eine deterministische Sortierung zu
	 * gewährleisten (deshalb muss T Comparable implementieren).
	 * <p>
	 * Die übergebene Map repräsentiert einen gerichteten Graphen: Jeder Eintrag in der Map
	 * beschreibt eine Menge von Kanten mit dem Key als Zielknoten und den Value-Menge als
	 * QuellKnoten.
	 * <p>
	 * In der konkreten Verwendung sind die Zielknoten jeweils das berechnete Feld und die Quellknoten die
	 * Operanden des berechneten Felds (wobei die Operanden sowohl berechnete als auch nicht-berechnete Felder
	 * sein können). Wenn die Knoten topologisch sortiert sind, wird somit Reihenfolge, in der berechnet
	 * werden muss, ermittelt.
	 * <p>
	 * <b>Algorithmus</b>
	 *
	 * <ul>
	 * <li>Ausgangsmenge für die Sortierung: alle Knoten ohne Kante</li>
	 * <li>lösche Knoten ohne Kante aus Graph</li>
	 * <li>erzeuge transponierten Graph, um spätere Such nach zu löschenden Kanten zu vereinfachen
	 * <li>solange Knoten k ohne Kanten vorhanden
	 * <ul>
	 * <li>hänge Knoten k an die Ergebnisliste</li>
	 * <li>lösche k und alle Kanten die von k ausgehen aus Graph</li>
	 * <li>falls durch das löschen neuer Knoten ohne Kante entstanden ist, füge diesen Knoten zur
	 * der Menge der Knoten ohne Kanten hinzu.</li>
	 * </ul>
	 * </ul>
	 * Ist der Graph nach Beendigung des Algorithmus leer wurden alle Knoten erfolgreich sortiert.
	 * Ist der Graph nach Beendigung des Algorihtmus nicht leer, gibt es (mindestens einen) Zyklus
	 * und die Sortierung war nicht erfolgreich.
	 *
	 * @param graph
	 * 		die Repräsentation eines gerichteten Graphen als die Menge der Kanten als
	 * 		Multimap. Alle vorhandenen Knoten müssen als Key vorkommen, evtl. mit leerem Set
	 * 		als Value. ACHTUNG: Die Repräsentation wird dabei modifiziert und zerstört.
	 * @return eine topologisch sortierte Liste der Knonten bzw. Darstellung eines Zyklus, falls
	 * 		nicht erfolgreich sortiert werden konnte.
	 */

	public static <T extends Comparable<T>> TopologicalSortResult<T> topologischeSortierung(Map<T, Set<T>> graph) {
		// Lösche Zielknoten die kein Key im Graph sind
		graph.forEach((dependent, dependencies) ->
				dependencies.removeIf(dependency -> !graph.containsKey(dependency)));

		// Gleicher Graph, Kanten in andere Richtung (zum Lookup beim Löschen)
		Map<T, Set<T>> invertedGraph = erzeugeInvertiertenGraph(graph);

		// TreeSet um die Reihenfolge der topologischen Sortierung zu fixieren.
		TreeSet<T> zielKnotenOhneKante = ermittleZielKnotenOhneKante(graph);

		//Lösche Knoten ohne Kante aus Graph
		for (T knoten : zielKnotenOhneKante) {
			graph.remove(knoten);
		}

		List<T> result = new ArrayList<>();

		while (!zielKnotenOhneKante.isEmpty()) {
			T knoten = zielKnotenOhneKante.pollFirst();
			result.add(knoten);

			// Entferne den Knoten aus allen Sets in graph (Abhängigkeit lösen)
			// Über den invertierten Graphen können alle Knoten ermittelt werden, für die
			// der aktuelle Knoten Quell-knoten ist.
			for (T quellKnoten : invertedGraph.getOrDefault(knoten, Collections.emptySet())) {
				Set<T> zielKnoten = graph.get(quellKnoten);
				if (zielKnoten == null) {//wurde bereits im ersten Iterationsschritt aus dem Graphen entfernt
					continue;
				}
				zielKnoten.remove(knoten); //Abhängigkeit im Ziel lösen

				//die Menge der Zielknoten ist leer, d.h. die letzte Abhängigkeit ist gelöst,
				//lösche den Knoten aus Graph und füge Knoten zu den ZielknotenOhneQuelle hinzu
				//--> nächster Iterationsschritt
				if (zielKnoten.isEmpty()) {
					zielKnotenOhneKante.add(quellKnoten);
					graph.remove(quellKnoten);
				}
			}
		}

		if (graph.isEmpty()) {
			// Topologische Sortierung erfolgreich
			return new TopologicalSortResult<>(result, null);
		} else {
			//Sortierung nicht erfolgreich
			List<T> cycle = getCycle(graph);
			return new TopologicalSortResult<>(null, cycle);
		}
	}

	/**
	 * @param cyclicGraphWithNoLeafNodes
	 * 		a graph that only contains nodes with at least 1 outgoing edge
	 * @return any arbitrary cycle in the graph. Each element is a node in the cycle, and has an outgoing edge to the subsequent
	 * 		element, with the last element having an outgoing edge to the first one.
	 */
	private static <T extends Comparable<T>> List<T> getCycle(Map<T, Set<T>> cyclicGraphWithNoLeafNodes) {
		List<T> result = new ArrayList<>();
		T next = cyclicGraphWithNoLeafNodes.keySet().stream()
				.min(Comparator.naturalOrder())
				.orElseThrow(); // otherwise the graph is not actually cyclic (precondition)
		do {
			result.add(next);
			next = cyclicGraphWithNoLeafNodes.get(next).stream()
					.min(Comparator.naturalOrder())
					.orElseThrow(); // otherwise the graph is not free of nodes that have no outgoing edges (precondition)
		} while (!result.contains(next));

		int startIdx = result.indexOf(next);
		return result.subList(startIdx, result.size());
	}

}
