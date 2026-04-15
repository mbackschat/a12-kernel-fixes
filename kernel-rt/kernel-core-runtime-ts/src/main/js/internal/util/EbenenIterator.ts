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
import type { IIterator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { ValidationCache } from "../core/ValidationCache.js";

import { EntityUtil } from "./EntityUtil.js";

import type { FilterPredicate } from "./FilterPredicate.js";
import { GesetzteWiederholungen } from "./GesetzteWiederholungen.js";
import { IterationState } from "./IterationState.js";
import { IteratorWrapper } from "./IteratorWrapper.js";
import { RtIdentifierTemplate } from "./RtIdentifierTemplate.js";
import { RtIdentifierTemplateLayerSpec } from "./RtIdentifierTemplateLayerSpec.js";
import type { RtInternalIdentifier } from "./RtInternalIdentifier.js";

/*
 * This class is used to realize the iterator of EbenenIterator.
 * It is initialized with all necessary informations from EbenenIterator.
 * Precondition: length of start and end indices must be the same.
 *
 * Note: this class must be declared at first
 */
export class InnerIterator implements IIterator<IterationState> {
	/**
	 *  Speichert, ob die Methode {@link hasNext} true zurückgeben darf.
	 */
	nextVorhanden = true;

	/*
	 * Current set of values for the index field used for parallel iteration.
	 * The list is initialized in #resetIndexValues and iterated over in #toNextParallelIteration.
	 */
	private _currentIndexValues: string[] | undefined;
	/**
	 * current spezifiziert das Element, das beim nächsten next-Aufruf zurückgegeben wird.
	 */
	private readonly _currentIndices: number[];
	private _currentValue: string | undefined;

	constructor(
		private _startIndices: number[],
		private _endIndices: number[],
		private _semanticIndexLevel: number,
		private _parallelIterationLevel: number,
		private _parallelIterationValueOverride: string | undefined,
		private _parallelIterationGroups: string[] | undefined,
		private _gesetzteWiederholungen: GesetzteWiederholungen | undefined,
		private _validationCache: ValidationCache | undefined,
		private _filter: FilterPredicate | undefined,
		private _outerState: IterationState | undefined
	) {
		this._currentIndices = _startIndices.slice(0);
		this._initState();
	}

	/**
	 * The initial state is the one created by resetting every layer.
	 */
	private _initState(): void {
		this.nextVorhanden = this._toNext(0, true);
	}

	/*
	 * The returned value was calculated during the previous call to {@link #toNext(int, boolean)}.
	 */
	public hasNext(): boolean {
		return this.nextVorhanden;
	}

	public next(): IterationState {
		// The next iteration state was already calculated during the previous call to toNext.
		const result: IterationState = new IterationState(
			this._currentIndices.slice(0),
			this._currentValue
		);
		// Calculate the next iteration state and set nextVorhanden if there is one.
		this.nextVorhanden = this._toNext(0, false);
		return result;
	}

	/**
	 * Increments the iterator by one element.
	 *
	 * The method is implemented recursively, because if the iteration indices above the
	 * parallel iteration level change, the currentIndexValues have to be reset, so the search
	 * has to go down again in the group hierarchy.
	 * The algorithm performs a complete search for the present index values of each iteration
	 * above the parallel iteration level.
	 *
	 * @param currentLevel the current level of iteration.
	 * @param reset true if the level has to be reset. For number iteration levels, the index of the level is reset to 1, for parallel
	 *              iteration levels the index values are reinitialized and the first one is used.
	 * @return true if the iteration on the levels below #currentLevel created a new valid iteration state. In
	 *              this case true is returned for all layers above. If there is no valid iteration state reachable iterating on the
	 *              levels below #currentLevel, <code>false</code> is returned and the recursion has to fall back to the next higher
	 *              level.
	 */
	private _toNext(currentLevel: number, reset: boolean): boolean {
		/*
		 * If we reach the end of the index array we have a complete state.
		 * We only need to check the filter condition if present.
		 * The optimization is done for each level in #toNextNumberIteration.
		 */
		if (currentLevel >= this._endIndices.length) {
			if (!this._filter) {
				return true;
			}
			/*
			 * We enhance the Iteration state for the filter condition by the outer state
			 * to be able to handle rule iteration in the filter condition.
			 */
			const currentState: IterationState = new IterationState(
				this._currentIndices,
				this._currentValue,
				this._outerState
			);
			return this._filter.isRelevant(currentState);
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
		if (!reset && currentLevel < this._endIndices.length - 1) {
			if (this._toNext(currentLevel + 1, false)) {
				return true;
			}
		}
		// we have to distinguish parallel and number iteration level.
		if (currentLevel === this._parallelIterationLevel) {
			return this._toNextParallelIteration(currentLevel, reset);
		} else {
			return this._toNextNumberIteration(currentLevel, reset);
		}
	}

	private _toNextParallelIteration(currentLevel: number, reset: boolean): boolean {
		// For a reset, we have to recalculate the #currentIndexValues and reset the #currentValue used for the index field
		if (reset) {
			this._resetIndexValues();
			this._currentValue = undefined;
		}

		// Determine the current iteration position in the #currentIndexValues list.
		let currentIndex = -1;
		if (this._currentIndexValues === undefined) {
			throw new Error("Values not initialized?!?");
		}
		if (this._currentValue) {
			currentIndex = this._currentIndexValues.indexOf(this._currentValue);
			if (currentIndex < 0) {
				throw new Error("Current value not in iteration interval?!?");
			}
		}
		// We iterate over all values in #currentIndexValues starting with the successor of #currentValue (or the first value).
		for (let i: number = currentIndex + 1; i < this._currentIndexValues.length; i++) {
			// Set the value and go to the next level
			if (this._setNewIndexAndRecurseOn(this._currentIndexValues[i], currentLevel)) {
				return true;
			}
		}
		return false;
	}

	private _resetIndexValues(): void {
		if (this._parallelIterationValueOverride) {
			this._currentIndexValues = [this._parallelIterationValueOverride];
		} else {
			this._currentIndexValues = this._createCurrentIndexValues();
		}
	}

	private _createCurrentIndexValues(): string[] {
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return this._validationCache!.getIterationValuesForGroups(
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			this._parallelIterationGroups!,
			this._currentIndices
		);
	}

	private _toNextNumberIteration(currentLevel: number, reset: boolean): boolean {
		let nextIndex: number = this._currentIndices[currentLevel] + 1;
		if (reset) {
			// To reset, use the start index for the current level.
			nextIndex = this._startIndices[currentLevel];
		}
		for (let i: number = nextIndex; i <= this._endIndices[currentLevel]; i++) {
			this._currentIndices[currentLevel] = i;
			// The first iteration (reset) should always be tested regardless of relevance.
			// This is currently necessary for RepetitionNotUnique 
			// (RepetitionNotUniqueCreator.getClearCode -> FeldUtil.sindGleichAbPosition)
			if (!reset && !this._indicesRelevantFuerEbene(this._currentIndices, currentLevel)) {
				// Optimization: If the indices are not relevant, increment again.
				continue;
			}
			// Reset everything below (after [3,2,1,5,10,1] follows [3,3,1,1,1,1] with
			// repeatabilities [5,5,1,5,10,1] when current level is 1).
			if (this._toNext(currentLevel + 1, true)) {
				return true;
			}
		}
		return false;
	}

	private _setNewIndexAndRecurseOn(value: string, currentLevel: number): boolean {
		this._currentValue = value;
		if (this._toNext(currentLevel + 1, true)) {
			return true;
		}
		return false;
	}

	/**
	 * Liefert <code>true</code>, wenn für die Wiederholungsebene, die durch das Sub-Array bis
	 * zum übergebenen Position bestimmt ist, Felder angegeben sind.<br>
	 * Beispiel:<br>
	 * [3, 4, 1, 1] ist für Position 2 relevant, wenn auf der durch [3, 4, 1] gegebenen
	 * Ebene ein Feld existiert.
	 * 3 für die LfdNr, 4 ist die Wiederholung eines Kontextes unterhalb des Vordrucks, etc.
	 *
	 */
	private _indicesRelevantFuerEbene(indices: number[], level: number): boolean {
		if (this._gesetzteWiederholungen === undefined) {
			return true;
		}
		const semIdxLevel = this.isParallelIteration()
			? this._parallelIterationLevel
			: this._semanticIndexLevel;
		return this._gesetzteWiederholungen.istWiederholungGesetzt(indices, level, semIdxLevel);
	}

	private isParallelIteration(): boolean {
		return this._parallelIterationLevel >= 0;
	}

	public remove(): void {
		throw new Error("Unsupported operation!");
	}
}

export class EbenenIterator implements Iterable<IterationState> {
	// implements Iterable<IterationState>

	private static get STERN_OPTIMIERUNGS_SCHWELLE(): number {
		return 100;
	}

	/*
	 * Laufende Nummer ist immer an Position 0.
	 * Zeile, falls vorhanden ist immer an letzter Position
	 */
	private _startIndices: number[];
	private _endIndices: number[];
	private _semanticIndexLevel: number;
	private _parallelIterationLevel: number;
	private _parallelIterationGroups: string[] | undefined;
	private _validationCache: ValidationCache | undefined;

	/*
	 * ist diese Variable auf ungleich undefined gesetzt, so wird nur über die angegebenen Wiederholungen
	 * iteriert
	 */
	private _gesetzteWiederholungen: GesetzteWiederholungen | undefined;

	/**
	 * Is set if the {@link EbenenIterator} is used for an {@link EntityIterator}. It specifies the
	 * current iteration state of the parallel iteration level.
	 */
	private _parallelIterationValueOverride: string | undefined;

	/**
	 * The filter specified in a Having construct as predicate.
	 * This is checked in the {@link InnerIterator._toNext} method.
	 */
	private readonly _filter: FilterPredicate | undefined;

	/**
	 * The {@link IterationState} of the rule iteration inside a filter operation.
	 */
	private _outerState: IterationState | undefined;

	/**
	 * If filter is set, this constructor initializes an {@link EbenenIterator} for the use in aggregations. See
	 * {@link #createEbenenIterator(RtIdentifierTemplate, IterationState)}.
	 *
	 * @param startIndices
	 *              the start indices for the iteration.
	 * @param endeIndices
	 *               the end indices for the iteration.
	 * @param filter
	 *              the filter operation for the Having language construct.
	 * @param outerState
	 *              the state of the rule iteration. Used in the {@link IterationState} transmitted to the filter.
	 */
	constructor(
		startIndices: number[],
		endIndices: number[],
		filter?: FilterPredicate,
		outerState?: IterationState
	);
	constructor(
		startIndices: number[],
		endIndices: number[],
		gesetzteWiederholungen: GesetzteWiederholungen,
		semanticIndexLevel: number
	);
	constructor(
		startIndices: number[],
		endIndices: number[],
		parallelIterationGroups: string[],
		vc: ValidationCache
	);
	constructor(
		startIndices: number[],
		endIndices: number[],
		gesetzteWiederholungen: GesetzteWiederholungen,
		parallelIterationGroups: string[],
		vc: ValidationCache
	);
	constructor(
		startIndices: number[],
		endIndices: number[],
		filterOrGesetzteWiederholungenOrParallelIterationGroups?:
			| FilterPredicate
			| GesetzteWiederholungen
			| string[],
		semanticIndexLevelOrVcOrParallelIterationGroupsOrOuterState?: number | ValidationCache | string[] | IterationState,
		vc?: ValidationCache,
	) {
		this._startIndices = startIndices;
		this._endIndices = endIndices;
		this._gesetzteWiederholungen =
			filterOrGesetzteWiederholungenOrParallelIterationGroups instanceof GesetzteWiederholungen
				? filterOrGesetzteWiederholungenOrParallelIterationGroups
				: undefined;
		this._semanticIndexLevel =
			typeof semanticIndexLevelOrVcOrParallelIterationGroupsOrOuterState === "number"
				? semanticIndexLevelOrVcOrParallelIterationGroupsOrOuterState
				: -1;
		this._parallelIterationLevel = startIndices.indexOf(IIdentifier.PARALLEL_ITERATION);
		this._parallelIterationGroups = undefined;
		this._validationCache = undefined;
		this._filter =
			!(
				filterOrGesetzteWiederholungenOrParallelIterationGroups instanceof GesetzteWiederholungen
			) && !(filterOrGesetzteWiederholungenOrParallelIterationGroups instanceof Array)
				? filterOrGesetzteWiederholungenOrParallelIterationGroups
				: undefined;
		this._outerState = semanticIndexLevelOrVcOrParallelIterationGroupsOrOuterState instanceof IterationState
			? semanticIndexLevelOrVcOrParallelIterationGroupsOrOuterState
			: undefined
		if (
			filterOrGesetzteWiederholungenOrParallelIterationGroups instanceof Array &&
			semanticIndexLevelOrVcOrParallelIterationGroupsOrOuterState instanceof ValidationCache
		) {
			this._parallelIterationGroups = filterOrGesetzteWiederholungenOrParallelIterationGroups;
			this._validationCache = semanticIndexLevelOrVcOrParallelIterationGroupsOrOuterState;
		} else if (
			semanticIndexLevelOrVcOrParallelIterationGroupsOrOuterState instanceof Array &&
			vc !== undefined
		) {
			this._parallelIterationGroups = semanticIndexLevelOrVcOrParallelIterationGroupsOrOuterState;
			this._validationCache = vc;
		}
	}

	[Symbol.iterator](): IterableIterator<IterationState> {
		return new IteratorWrapper(this.iterator());
	}

	/**
	 * Setzt den Index der Ebene idx auf den neuen Wert, wenn der
	 * neueWert ein spezifischer Wert ist.
	 * Methode gibt false zurück, wenn der neueWert außerhalb des Iterationsintervalls liegt.
	 * liegt.
	 */
	private _setzeIndices(idx: number, neuerWert: number): boolean {
		if (neuerWert === 0) {
			// kein konkreter Wert, mache nichts
			return true;
		}
		if (neuerWert < this._startIndices[idx] || neuerWert > this._endIndices[idx]) {
			return false; // neuerWert liegt außerhalb des Iterationsintervalls.
		}

		// konkreter, gültiger Wert, setze um
		this._startIndices[idx] = neuerWert;
		this._endIndices[idx] = neuerWert;
		return true;
	}

	/**
	 * @return the first iteration state from the iterator if existent or the start indices (with
	 *         parallel iteration replaced by 1) otherwise. If there is an exception thrown in the
	 *         iterator the start indices (with parallel iteration replaced by 1) are returned too.
	 */
	get(): IterationState {
		let iterationState: IterationState | undefined;
		try {
			iterationState = this.iterator().next();
		} catch (_e) {
			// if there are exceptions thrown in the iterator, we simply return the first indices
		}
		if (!iterationState) {
			const indices = this._startIndices.slice(0);
			if (this._parallelIterationLevel >= 0) {
				indices[this._parallelIterationLevel] = 1;
			}
			iterationState = new IterationState(indices, undefined);
		}
		return iterationState;
	}

	iterator(): IIterator<IterationState> {
		return new InnerIterator(
			this._startIndices,
			this._endIndices,
			this._semanticIndexLevel,
			this._parallelIterationLevel,
			this._parallelIterationValueOverride,
			this._parallelIterationGroups,
			this._gesetzteWiederholungen,
			this._validationCache,
			this._filter,
			this._outerState
		);
	}

	/**
	 * Methode wird nur bei Regeln mit einer Iteration aufgerufen und soll die Anzahl der
	 * durchgeführten
	 * Iterationen minimieren.
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
	 * @param indices 	zu prüfende Indices, bei Vollvalidierung immer nur ein-elementig
	 * @return true, wenn die zu setzenden Indices im Iterationsintervall liegen
	 */
	checkAndSetWiederholungen(indices: number[]): boolean {
		for (let i = 0; i < indices.length; i++) {
			if (!this._setzeIndices(i, indices[i])) {
				return false; // Außerhalb der Iteration,  es kann sofort aufgehört werden.
			}
		}
		return true;
	}

	private setValueForParallelIteration(value: string | undefined): void {
		this._parallelIterationValueOverride = value;
	}

	/*
	 * If rtIdContainerOrId is a RtInternalIdentifier, the method is used to dereference referenced fields.
	 */
	static createEbenenIterator(
		rtIdContainerOrId: RtIdentifierTemplate | RtInternalIdentifier,
		idxState: IterationState | undefined
	): EbenenIterator {
		if (rtIdContainerOrId instanceof RtIdentifierTemplate) {
			const usedIterationState: IterationState | undefined =
				rtIdContainerOrId.isOuter() && idxState != null ? idxState.getOuterState() : idxState;
			const startAndEndIndices: Array<RtIdentifierTemplateLayerSpec> = EntityUtil.resolveIterations(
				rtIdContainerOrId.getLayerSpec(),
				usedIterationState != undefined ? usedIterationState.getIndexes() : undefined
			);
			return this._createEbenenIterator(
				startAndEndIndices,
				rtIdContainerOrId.getFilter(),
				usedIterationState
			);
		} else {
			const indces: number[] = rtIdContainerOrId.getIndices(); // start and end are the same for a single RtInternalIdentifier
			const startAndEndIndices: Array<RtIdentifierTemplateLayerSpec> = EntityUtil.resolveIterations(
				RtIdentifierTemplateLayerSpec.startEndIndicesToLayerSpecs(indces, indces),
				idxState !== undefined ? idxState.getIndexes() : undefined
			);
			return this._createEbenenIterator(
				startAndEndIndices,
				undefined,
				idxState
			);
		}
	}

	private static _createEbenenIterator(
		startAndEndIndicesItersResolved: Array<RtIdentifierTemplateLayerSpec>,
		filter: FilterPredicate | undefined,
		iterationState: IterationState | undefined
	): EbenenIterator {
		const asArrays: [number[], number[]] =
			RtIdentifierTemplateLayerSpec.layerSpecsToStartEndIndices(startAndEndIndicesItersResolved);
		const iterator: EbenenIterator = new EbenenIterator(asArrays[0], asArrays[1], filter, iterationState);
		const valueForParallelIteration = iterationState?.getValue();
		iterator.setValueForParallelIteration(valueForParallelIteration);
		return iterator;
	}

	getAnzahlEbenen(): number {
		return this._startIndices.length;
	}

	getStartIndices(): number[] {
		return this._startIndices;
	}

	getEndIndices(): number[] {
		return this._endIndices;
	}

	/**
	 * Setzt den Endeindex für die spezifizierte Ebene auf den neuen Wert, wenn dieser kleiner ist
	 * als der aktuelle Endeindex für die Ebene. Ist der neue Wert kleiner als der Start, wird der
	 * Endeindex auf den Startindex gesetzt.
	 *
	 * @return <code>true</code>, falls der Endeindex geändert wurde, <code>false</code> sonst
	 */
	setEndeIndexFallsKleinerEnde(ebene: number, neuerWert: number): boolean {
		if (this._startIndices[ebene] === this._endIndices[ebene]) {
			return false;
		}
		if (neuerWert < this._endIndices[ebene]) {
			// das neue Ende soll nicht kleiner als der Start sein können
			neuerWert = Math.max(neuerWert, this._startIndices[ebene]);
			this.setEndeIndexFuerEbene(ebene, neuerWert);
			return true;
		}
		return false;
	}

	setEndeIndexFuerEbene(ebene: number, index: number): void {
		this._endIndices[ebene] = index;
	}

	setStartIndexFuerEbene(ebene: number, index: number): void {
		this._startIndices[ebene] = index;
	}

	optimiereMitGesetztenWiederholungenWennSinnvoll(gesetzteWhs: GesetzteWiederholungen): void {
		if (this._isSternOptimierungSinnvoll()) {
			this._gesetzteWiederholungen = gesetzteWhs;
		}
	}

	/**
	 * Liefert <code>true</code>, wenn die Anzahl der Ebenen die Schwelle
	 * {@link STERN_OPTIMIERUNGS_SCHWELLE} übersteigt.
	 */
	private _isSternOptimierungSinnvoll(): boolean {
		let anzahlFelder = 1;
		for (let i = 0; i < this._startIndices.length; i++) {
			const anzahl: number = this._endIndices[i] - this._startIndices[i] + 1;
			if (anzahl > 0) {
				anzahlFelder *= anzahl;
				if (anzahlFelder > EbenenIterator.STERN_OPTIMIERUNGS_SCHWELLE) {
					return true;
				}
			}
		}
		return false;
	}

	getAnzahlWiederholungen(): number {
		let result = 1;
		for (let i = 0; i < this._startIndices.length; i++) {
			result *= this._endIndices[i] - this._startIndices[i] + 1;
		}
		return result;
	}

	getSemanticIndexLevel(): number {
		return this._semanticIndexLevel;
	}
}
