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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.ValidationCache;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * Klasse implementiert die Iteration über die verschiedenen Wiederholungsebenen.
 * Die Iterationen sind jeweils einschließlich start und ende-Index.
 */
public class EbenenIterator implements Iterable<IterationState> {

	private static final int STERN_OPTIMIERUNGS_SCHWELLE = 100;

	/*
	 * USB und laufende Nummer sind immer an Position 0 und Position 1.
	 * Zeile, falls vorhanden ist immer an letzter Position
	 */
	private final long[] startIndices;
	private final long[] endIndices;
	private final int semanticIndexLevel; // without USB
	private final int parallelIterationLevelWithUsb;
	private final String[] parallelIterationGroups;
	private final ValidationCache validationCache;

	/*
	 * ist diese Variable auf ungleich null gesetzt, so wird nur über die angegebenen Wiederholungen
	 * iteriert
	 */
	private GesetzteWiederholungen gesetzteWiederholungen;

	/**
	 * Is set if the {@link EbenenIterator} is used for an {@link EntityIterator}. It specifies the
	 * current iteration state of the parallel iteration level.
	 */
	private String parallelIterationValueOverride;

	/**
	 * The filter specified in a Having construct as predicate.
	 * This is checked in the {@link InnerIterator#toNext(int, boolean)} method.
	 */
	private final FilterPredicate filter;

	/**
	 * The {@link IterationState} of the rule iteration inside a filter operation.
	 */
	private IterationState outerState = null;

	public EbenenIterator(long[] startIndices, long[] endeIndices) {
		this(startIndices, endeIndices, null, -1, null, null, null);
	}

	/**
	 * Initializes an {@link EbenenIterator} for the use in aggregations. See
	 * {@link #createEbenenIterator(RtIdentifierTemplate, IterationState)}.
	 *
	 * @param startIndices
	 * 		the start indices for the iteration.
	 * @param endeIndices
	 * 		the end indices for the iteration.
	 * @param filter
	 * 		the filter operation for the Having language construct.
	 * @param outerState
	 * 		the state of the rule iteration. Used in the {@link IterationState} transmitted to the filter.
	 */
	public EbenenIterator(long[] startIndices, long[] endeIndices, FilterPredicate filter, IterationState outerState) {
		this(startIndices, endeIndices, null, -1, null, null, filter);
		this.outerState = outerState;
	}

	public EbenenIterator(long[] startIndices, long[] endeIndices, GesetzteWiederholungen gesetzteWiederholungen,
			int semanticIndexLevel) {
		this(startIndices, endeIndices, gesetzteWiederholungen, semanticIndexLevel, null, null, null);
	}

	public EbenenIterator(long[] startIndices, long[] endeIndices, String[] parallelIterationGroups, ValidationCache vc) {
		this(startIndices, endeIndices, null, -1, parallelIterationGroups, vc, null);
	}

	public EbenenIterator(long[] startIndices, long[] endeIndices, GesetzteWiederholungen gesetzteWiederholungen,
			String[] parallelIterationGroups, ValidationCache vc) {
		this(startIndices, endeIndices, gesetzteWiederholungen, -1, parallelIterationGroups, vc, null);
	}

	private EbenenIterator(long[] startIndices, long[] endeIndices, GesetzteWiederholungen gesetzteWiederholungen,
			int semanticIndexLevel, String[] parallelIterationGroups, ValidationCache validationCache, FilterPredicate filter) {
		assert (startIndices.length == endeIndices.length);
		this.startIndices = startIndices;
		this.endIndices = endeIndices;
		this.gesetzteWiederholungen = gesetzteWiederholungen;
		this.semanticIndexLevel = semanticIndexLevel;
		this.parallelIterationLevelWithUsb = ArrayUtils.indexOf(startIndices, IIdentifier.PARALLEL_ITERATION);
		this.parallelIterationGroups = parallelIterationGroups;
		this.validationCache = validationCache;
		this.filter = filter;
	}

	/**
	 * Setzt den Index der Ebene idx auf den neuen Wert, wenn der
	 * neueWert ein spezifischer Wert ist.
	 * Methode gibt false zurück, wenn der neueWert außerhalb des Iterationsintervalls liegt.
	 * liegt.
	 */
	private boolean setzeIndices(int idx, long neuerWert) {
		if (neuerWert == 0) { //kein konkreter Wert,  mache nichts
			return true;
		}
		if (neuerWert < startIndices[idx] || neuerWert > endIndices[idx]) {
			return false; // neuerWert liegt außerhalb des Iterationsintervalls.
		}

		// konkreter, gültiger Wert, setze um
		startIndices[idx] = neuerWert;
		endIndices[idx] = neuerWert;
		return true;
	}

	/**
	 * @return the first iteration state from the iterator if existent or the start indices (with
	 * 		parallel iteration replaced by 1) otherwise. If there is an exception thrown in the
	 * 		iterator the start indices (with parallel iteration replaced by 1) are returned too.
	 */
	public IterationState get() {
		IterationState iterationState = null;
		try {
			iterationState = iterator().next();
		} catch (Exception e) {
			// if there are exceptions thrown in the iterator, we simply return the first indices
		}
		if (iterationState == null) {
			long[] indices = startIndices.clone();
			if (parallelIterationLevelWithUsb >= 0) {
				indices[parallelIterationLevelWithUsb] = 1;
			}
			iterationState = new IterationState(indices, null);
		}
		return iterationState;
	}

	/*
	 * This inner class is used to realize the Iterator.
	 */
	private class InnerIterator implements Iterator<IterationState> {
		/*
		 * speichert, ob die Methode hasNext true zurückgeben darf.
		 */
		boolean nextVorhanden = true;

		/*
		 * Current set of values for the index field used for parallel iteration.
		 * The list is initialized in #resetIndexValues and iterated over in #toNextParallelIteration.
		 */
		private List<String> currentIndexValues;
		/*
		 * current spezifiziert das Element, das beim nächsten next
		 * Aufruf zurückgegeben wird.
		 */
		private final long[] currentIndices;
		private String currentValue;

		public InnerIterator() {
			currentIndices = startIndices.clone();
			initState();
		}

		/**
		 * The initial state is the one created by resetting every layer.
		 */
		private void initState() {
			nextVorhanden = toNext(0, true);
		}

		/*
		 * The returned value was calculated during the previous call to {@link #toNext(int, boolean)}.
		 */
		public boolean hasNext() {
			return nextVorhanden;
		}

		@Override
		public IterationState next() {
			if (!nextVorhanden) {
				throw new NoSuchElementException();
			}
			// The next iteration state was already calculated during the previous call to toNext.
			IterationState result = new IterationState(currentIndices.clone(), currentValue);
			// Calculate the next iteration state and set nextVorhanden if there is one.
			nextVorhanden = toNext(0, false);
			return result;
		}

		/**
		 * Increments the iterator by one element.
		 * <p>
		 * The method is implemented recursively, because if the iteration indices above the
		 * parallel iteration level change, the currentIndexValues have to be reset, so the search
		 * has to go down again in the group hierarchy.
		 * The algorithm performs a complete search for the present index values of each iteration
		 * above the parallel iteration level.
		 *
		 * @param currentLevel
		 * 		the current level of iteration.
		 * @param reset
		 * 		true if the level has to be reset. For number iteration levels, the index of the level is reset to 1, for parallel
		 * 		iteration levels the index values are reinitialized and the first one is used.
		 * @return <code>true</code> if the iteration on the levels below #currentLevel created a new valid iteration state. In
		 * 		this case true is returned for all layers above. If there is no valid iteration state reachable iterating on the
		 * 		levels below #currentLevel, <code>false</code> is returned and the recursion has to fall back to the next higher
		 * 		level.
		 */
		private boolean toNext(int currentLevel, boolean reset) {
			/*
			 * If we reach the end of the index array we have a complete state.
			 * We only need to check the filter condition if present.
			 * The optimization is done for each level in #toNextNumberIteration.
			 */
			if (currentLevel >= endIndices.length) {
				if (filter == null) {
					return true;
				}
				/*
				 * We enhance the Iteration state for the filter condition by the outer state
				 * to be able to handle rule iteration in the filter condition.
				 */
				IterationState currentState = new IterationState(currentIndices, currentValue, outerState);
				return filter.isRelevant(currentState);
			}
			/*
			 * If not reset -> go down to the level endIndices.length - 1, where the first increment starts.
			 *
			 * For reset, we don't go deeper but reset all levels starting from the current one. This is done in the methods
			 * #toNextParallelIteration and #toNextNumberIteration.
			 *
			 * If #toNext returns true, the iteration on lower levels already created a new iteration state and nothing has to be
			 *  done  on the current level.
			 */
			if (!reset && currentLevel < endIndices.length - 1 && toNext(currentLevel + 1, false)) {
				return true;
			}
			// we have to distinguish parallel and number iteration level.
			if (currentLevel == parallelIterationLevelWithUsb) {
				return toNextParallelIteration(currentLevel, reset);
			} else {
				return toNextNumberIteration(currentLevel, reset);
			}
		}

		private boolean toNextParallelIteration(int currentLevel, boolean reset) {
			// For a reset, we have to recalculate the #currentIndexValues and reset the #currentValue used for the index field.
			if (reset) {
				resetIndexValues();
				currentValue = null;
			}

			// Determine the current iteration position in the #currentIndexValues list.
			int currentIndex = -1;
			if (currentValue != null) {
				currentIndex = currentIndexValues.indexOf(currentValue);
				if (currentIndex < 0) {
					throw new IllegalStateException("Current value not in iteration interval?!?");
				}
			}
			// We iterate over all values in #currentIndexValues starting with the successor of #currentValue (or the first value).
			for (int i = currentIndex + 1; i < currentIndexValues.size(); i++) {
				// Set the value and go to the next level
				if (setNewIndexAndRecurseOn(currentIndexValues.get(i), currentLevel)) {
					return true;
				}
			}
			return false;
		}

		private void resetIndexValues() {
			if (parallelIterationValueOverride != null) {
				currentIndexValues = List.of(parallelIterationValueOverride);
			} else {
				currentIndexValues = createCurrentIndexValues();
			}
		}

		private List<String> createCurrentIndexValues() {
			return validationCache.getIterationValuesForGroups(parallelIterationGroups, currentIndices);
		}

		private boolean toNextNumberIteration(int currentLevel, boolean reset) {
			long nextIndex = currentIndices[currentLevel] + 1;
			if (reset) {
				// To reset, use the start index for the current level.
				nextIndex = startIndices[currentLevel];
			}
			for (long i = nextIndex; i <= endIndices[currentLevel]; i++) {
				currentIndices[currentLevel] = i;
				// The first iteration (reset) should always be tested regardless of relevance.
				// This currently is necessary for RepetitionNotUnique (RepetitionNotUniqueCreator#getClearCode -> FeldUtil#sindGleichAbPosition)
				if (!reset && !indicesRelevantFuerEbene(currentIndices, currentLevel)) {
					// Optimization: If the indices are not relevant, increment again.
					continue;
				}
				// Reset everything below (after [3,2,1,5,10,1] follows [3,3,1,1,1,1] with repeatabilities [5,5,1,5,10,1] on
				// when current level is 1).
				if (toNext(currentLevel + 1, true)) {
					return true;
				}
			}
			return false;
		}

		private boolean setNewIndexAndRecurseOn(String value, int currentLevel) {
			currentValue = value;
			return toNext(currentLevel + 1, true);
		}

		/**
		 * Liefert <code>true</code>, wenn für die Wiederholungsebene, die durch das Sub-Array bis
		 * zum übergebenen Position bestimmt ist, Felder angegeben sind.<br>
		 * Beispiel:<br>
		 * [-2, 3, 4, 1, 1] ist für Position 3 relevant, wenn auf der durch [-2, 3, 4, 1] gegebenen
		 * Ebene ein Feld existiert.
		 * -2 steht dabei für den USB, 3 für die LfdNr, 4 ist die Wiederholung eines Kontextes
		 * unterhalb des Vordrucks, etc.
		 */
		private boolean indicesRelevantFuerEbene(long[] array, int position) {
			if (gesetzteWiederholungen == null) {
				return true;
			}
			long[] subArray = Arrays.copyOf(array, position + 1);
			int semIdxLevelWithoutUsb = semanticIndexLevel;
			if (isParallelIteration()) {
				semIdxLevelWithoutUsb = parallelIterationLevelWithUsb - 1;
				if (parallelIterationLevelWithUsb < subArray.length) {
					subArray[parallelIterationLevelWithUsb] = IIdentifier.SEMANTIC_INDEX;
				}
			}
			return gesetzteWiederholungen.istWiederholungGesetzt(subArray, semIdxLevelWithoutUsb);
		}

		private boolean isParallelIteration() {
			return parallelIterationLevelWithUsb >= 0;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<IterationState> iterator() {
		return new InnerIterator();
	}

	/**
	 * Methode wird nur bei Regeln mit einer Iteration aufgerufen und soll die Anzahl der
	 * durchgeführten Iterationen minimieren.
	 * Bei einer Vollvalidierung wird nur die LfdNr optimiert, d.h. indices hat immer nur ein
	 * Element.
	 * Als Index wird spezifiert
	 * <ul>
	 * <li>ein konkreter Index</li>
	 * <li>0 d.h. alle</li>
	 * </ul>
	 * Falls ein konkreter Index spezifiziert wird, wird Start und Ende umgesetzt, wenn der
	 * konkrete Index innerhalb der Iterationsgrenzen liegt.
	 * Ist ein Index außerhalb der gültigen Iterationsgrenzen wird false
	 * zurückgegeben. In solchen Fällen wird die gesamte Regel nicht ausgewertet.
	 *
	 * @param indices
	 * 		zu prüfende Indices, bei Vollvalidierung immer nur ein-elementig
	 * @return true, wenn die zu setzenden Indices im Iterationsintervall liegen
	 */
	public boolean checkAndSetWiederholungen(long[] indices) {
		for (int i = 0; i < indices.length; i++) {
			//i+1, weil das übergebene Index-Array ohne USB ist, der EbenenIterator aber mit USB an der ersten Stelle
			if (!setzeIndices(i + 1, indices[i])) {
				return false; // Außerhalb der Iteration,  es kann sofort aufgehört werden.
			}
		}
		return true;
	}

	private void setValueForParallelIteration(String value) {
		this.parallelIterationValueOverride = value;
	}

	static EbenenIterator createEbenenIterator(RtIdentifierTemplate id, IterationState idxState) {
		IterationState usedIterationState = id.isOuter() && idxState != null ? idxState.getOuterState() : idxState;
		List<RtIdentifierTemplateLayerSpec> startAndEndIndices = EntityUtil.resolveIterations(
				id.getLayerSpecWithUsb(),
				usedIterationState != null ? usedIterationState.getIndexes() : null);
		return createEbenenIterator(startAndEndIndices, id.getFilter(), usedIterationState);
	}

	/*
	 * Used to dereference referenced fields.
	 */
	static EbenenIterator createEbenenIterator(RtInternalIdentifier id, IterationState idxState) {
		long[] indices = id.getIndicesWithUsb(); // start and end are the same for a single RtInternalIdentifier
		List<RtIdentifierTemplateLayerSpec> startAndEndIndices = EntityUtil.resolveIterations(
				RtIdentifierTemplateLayerSpec.startEndIndicesToLayerSpecs(indices, indices),
				idxState.getIndexes());
		return createEbenenIterator(startAndEndIndices, null, idxState);
	}

	private static EbenenIterator createEbenenIterator(List<RtIdentifierTemplateLayerSpec> startAndEndIndicesItersResolved,
			FilterPredicate filter, IterationState iterationState) {
		Pair<long[], long[]> asArrays = RtIdentifierTemplateLayerSpec.layerSpecsToStartEndIndices(startAndEndIndicesItersResolved);
		EbenenIterator iterator = new EbenenIterator(asArrays.getEntry1(), asArrays.getEntry2(), filter, iterationState);
		String valueForParallelIteration = iterationState != null ? iterationState.getValue() : null;
		iterator.setValueForParallelIteration(valueForParallelIteration);
		return iterator;
	}

	int getAnzahlEbenen() {
		return startIndices.length;
	}

	long[] getStartIndices() {
		return startIndices;
	}

	long[] getEndeIndices() {
		return endIndices;
	}

	long getEndeUsb() {
		return endIndices[0];
	}

	long getStartUsb() {
		return startIndices[0];
	}

	/**
	 * Setzt den Endeindex für die spezifizierte Ebene auf den neuen Wert, wenn dieser kleiner ist
	 * als der aktuelle Endeindex für die Ebene. Ist der neue Wert kleiner als der Start, wird der
	 * Endeindex auf den Startindex gesetzt.
	 *
	 * @return <code>true</code>, falls der Endeindex geändert wurde, <code>false</code> sonst
	 */
	boolean setEndeIndexFallsKleinerEnde(int ebene, long neuerWert) {
		if (startIndices[ebene] == endIndices[ebene]) {
			return false;
		}
		if (neuerWert < endIndices[ebene]) {
			//das neue Ende soll nicht kleiner als der Start sein können
			neuerWert = Math.max(neuerWert, startIndices[ebene]);
			setEndeIndexFuerEbene(ebene, neuerWert);
			return true;
		}
		return false;
	}

	void setEndeIndexFuerEbene(int ebene, long index) {
		endIndices[ebene] = index;
	}

	void setStartIndexFuerEbene(int ebene, long index) {
		startIndices[ebene] = index;
	}

	void optimiereMitGesetztenWiederholungenWennSinnvoll(GesetzteWiederholungen gesetzteWhs) {
		if (isSternOptimierungSinnvoll()) {
			this.gesetzteWiederholungen = gesetzteWhs;
		}
	}

	/**
	 * Liefert <code>true</code>, wenn die Anzahl der Ebenen die Schwelle
	 * {@link #STERN_OPTIMIERUNGS_SCHWELLE} übersteigt.
	 */
	private boolean isSternOptimierungSinnvoll() {
		long anzahlFelder = 1;
		for (int i = 0; i < startIndices.length; i++) {
			long anzahl = (endIndices[i] - startIndices[i]) + 1;
			if (anzahl > 0) {
				anzahlFelder *= anzahl;
				if (anzahlFelder > STERN_OPTIMIERUNGS_SCHWELLE) {
					return true;
				}
			}
		}
		return false;
	}

	public int getAnzahlWiederholungen() {
		int result = 1;
		for (int i = 0; i < startIndices.length; i++) {
			result *= (endIndices[i] - startIndices[i]) + 1;
		}
		return result;
	}

	public int getSemanticIndexLevel() {
		return semanticIndexLevel;
	}
}
