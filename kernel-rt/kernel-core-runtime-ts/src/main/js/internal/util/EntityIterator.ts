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
import { HashSet } from "@com.mgmtp.a12.utils/utils-collections";

import type { IIterator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { ValidationCache } from "../core/ValidationCache.js";

import { EbenenIterator } from "./EbenenIterator.js";
import { EntityUtil } from "./EntityUtil.js";
import type { IterationState } from "./IterationState.js";
import { IteratorWrapper } from "./IteratorWrapper.js";
import { RtIdentifierTemplate } from "./RtIdentifierTemplate.js";
import { RtInternalIdentifier } from "./RtInternalIdentifier.js";
import { SemanticIndexTemplate } from "./SemanticIndexTemplate.js";
import type { ValidierungArt } from "./ValidierungArt.js";

/**
 * Internal class is used to implement the iterator over the corresponding identifiers. In case
 * of parallel iteration the respective index is replaced with the semantic index using the
 * value of the current {@link IterationState}.
 */
class InnerIterator implements IIterator<RtInternalIdentifier> {
	private readonly _iter: IIterator<IterationState>;

	// The InnerIterator is an inner class in java. But, Typescript lacks the concept of an inner classes.
	// Therefore, here the InnerIterator gets handed the properties of the "outer" class EntityIterator, it needs to
	// access, via its constructor
	constructor(
		private _entityName: string,
		iter: EbenenIterator,
		private _category: string | undefined,
		private _semIdx: SemanticIndexTemplate,
		private _originalIterationState: IterationState
	) {
		this._iter = iter.iterator();
	}

	hasNext(): boolean {
		return this._iter.hasNext();
	}

	next(): RtInternalIdentifier {
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const iterState: IterationState = this._iter.next()!;
		const result: RtInternalIdentifier = RtInternalIdentifier.createFromIterationState(
			this._entityName,
			iterState,
			this._category,
			this._semIdx ? this._semIdx.createSemanticIndex(this._originalIterationState) : undefined
		);
		return result;
	}

	remove(): void {
		throw new Error("Operation is not supported.");
	}
}

/**
 * This class is used to iterate over the repetitions of an entity in an entity list.
 * The repetitions on each layer either specify a specific index or a wildcard, that will be
 * transformed to a representation &lt;from&gt;-&lt;to&gt; during code generation.
 *
 * The first layer (i.e. index 0) always specifies the repetition of the root group.
 * Additionally the group repetitions need to be specified.
 */
export class EntityIterator implements Iterable<RtInternalIdentifier> {
	private readonly _entityName: string;
	private readonly _ebenenIterator: EbenenIterator;
	private readonly _isField: boolean;
	private readonly _semIdx: SemanticIndexTemplate;
	private readonly _category: string | undefined;
	private readonly _originalIterationState: IterationState | undefined;
	private readonly _outer: boolean;

	constructor(
		idOrRtIdContainer: RtIdentifierTemplate | RtInternalIdentifier,
		idx: IterationState | undefined
	) {
		if (idOrRtIdContainer instanceof RtIdentifierTemplate) {
			this._originalIterationState = idx;
			this._category = idOrRtIdContainer.getCategory();
			this._entityName = idOrRtIdContainer.getUniqueName();
			this._isField = idOrRtIdContainer.isField();
			this._ebenenIterator = EbenenIterator.createEbenenIterator(idOrRtIdContainer, idx);
			this._outer = idOrRtIdContainer.isOuter();
			this._semIdx = idOrRtIdContainer.getSemanticIndex();
		} else {
			this._originalIterationState = idx;
			this._semIdx = SemanticIndexTemplate.createFromSpec(idOrRtIdContainer.getSemanticIndex());
			this._category = idOrRtIdContainer.getCategory();
			this._entityName = idOrRtIdContainer.getName();
			this._isField = !this._entityName.startsWith("/");
			this._outer = false;
			this._ebenenIterator = EbenenIterator.createEbenenIterator(idOrRtIdContainer, idx);
		}
	}

	[Symbol.iterator](): IterableIterator<RtInternalIdentifier> {
		return new IteratorWrapper(this.iterator());
	}

	/**
	 * Gibt den ersten Identifier zurück
	 */
	public get(): RtInternalIdentifier {
		const usedIterationState = this._outer && this._originalIterationState
			? this._originalIterationState.getOuterState()
			: this._originalIterationState;
		return RtInternalIdentifier.createFromIterationState(
			this._entityName,
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			this._ebenenIterator.iterator().next()!,
			this._category,
			this._semIdx.createSemanticIndex(usedIterationState)
		);
	}

	public iterator(): IIterator<RtInternalIdentifier> {
		return new InnerIterator(
			this._entityName,
			this._ebenenIterator,
			this._category,
			this._semIdx,
			// TODO: Fix this the next time the file is edited.
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			this._originalIterationState!
		);
	}

	/**
	 * Setzt den Wert des End-Indizes einer Kontextebene auf den maximalen Index-Wert
	 * verglichen mit allen Index Werten aller Kontexte die in dieser Ebene enthalten sind,
	 * sprich durch die übergebenen Felder referenziert werden.
	 * Der Wert wird nur dann gesetzt, wenn dieser zwischen Start- und End-Index der
	 * adressierten Kontextebene liegt.
	 * <p>
	 * Wird für die Optimierung für den Sternoperator verwendet.
	 * </p>
	 *
	 * @return - true, wenn die End-LfdNr geändert wurde, false sonst
	 */
	setEndeIndexFallsKleinerAktuellesEnde(ebene: number, neuerWert: number): boolean {
		return this._ebenenIterator.setEndeIndexFallsKleinerEnde(ebene, neuerWert);
	}

	/**
	 * Setzt den Endindex der Zeile auf 1.
	 * <p>
	 * Wird für die Optimierung für den Sternoperator verwendet.
	 * </p>
	 *
	 * @return true, falls der Endeindex geändert wurde, false sonst
	 */
	setEndeZeileAufEins(): boolean {
		const zeilenEbene: number = this.anzahlWiederholungsEbenen - 1;
		return this._ebenenIterator.setEndeIndexFallsKleinerEnde(
			zeilenEbene,
			1
		);
	}

	setIndexFuerEbene(ebene: number, index: number): void {
		this._ebenenIterator.setStartIndexFuerEbene(ebene, index);
		this._ebenenIterator.setEndeIndexFuerEbene(ebene, index);
	}

	getEntityName(): string {
		return this._entityName;
	}

	/**
	 * Setzt den Iterator für die Zeile auf 1 und alle anderen Ebenen ab Startindex auf den maximal gesetzten Wert.
	 *
	 * @return true, falls mindestens ein Endeindex geändert wurde, false sonst
	 */
	setzeAlleEbenenMitZeileAufMaxGesetztIfPossible(
		vc: ValidationCache,
		valArt: ValidierungArt,
		startIndex: number
	): boolean {
		let indexGesetzt = false;
		if (this._isField) {
			if (this.setEndeZeileAufEins()) {
				indexGesetzt = true;
			}
		}

		const maxLevel: number = this._isField
			? this.anzahlWiederholungsEbenen - 1
			: this.anzahlWiederholungsEbenen;

		/**
		 * Die Schleife darf nur bis -2 gehen, weil
		 * - die Zeile (d.h. die letzte Position) nicht berücksichtigt wird und
		 * - die Kontext-Ebene immer ohne die laufende Nummer gezählt wird.
		 */
		for (let i: number = startIndex; i < maxLevel; ++i) {
			if (valArt.istEbeneGleich(i)) {
				if (
					this._ebenenIterator.setEndeIndexFallsKleinerEnde(
						i,
						vc.getMaxGesetzteWiederholungOhneZeile(i)
					)
				) {
					indexGesetzt = true;
				}
			}
		}
		return indexGesetzt;
	}

	public get anzahlWiederholungsEbenen(): number {
		return this._ebenenIterator.getAnzahlEbenen();
	}

	/**
	 * Optimiert den EntityIterator so, daß der {@link iterator} Identifier überspringt, für deren
	 * Wiederholung keine Werte angegeben wurden.
	 */
	public optimiereMitGesetztenWiederholungenWennSinnvoll(vc: ValidationCache): void {
		this._ebenenIterator.optimiereMitGesetztenWiederholungenWennSinnvoll(
			vc.getGesetzteWiederholungen()
		);
	}

	/**
	 * Optimierte Ermittlung, ob eine Wiederholung des {@link EntityIterator}s nicht prüfungsrelevant ist.
	 * <br>
	 * <b>Vorbedingung:</b> Es handelt sich um eine Teilvalidierung.<br>
	 * <b>Anmerkung:</b><br>
	 * Die Methode kann <code>true</code> liefern im Sonderfall, daß in den prüfungsrelevanten
	 * Feldern alle Wiederholungen eines Feldes einzeln angegeben sind statt mit [IIdentifier.ALL_INDICES](
	 * ../../../kernel-core-runtime-api-ts/modules/_validation_iidentifier_.iidentifier.html#all_indices).<br>
	 * Von dieser Vorgehensweise wird explizit abgeraten. Sollte es doch vorkommen, wird im
	 * schlimmsten Fall der Fehler bei der Vollvalidierung gemeldet, daher ist diese
	 * Optimierung akzeptabel.<br>
	 * Außerdem falsch true liefern kann die Methode, wenn die Wiederholbarkeit mit
	 * {@link IIdentifier#ALL_INDICES} als relevant gesetzt ist, aber in einer
	 * Geschwister-Wiederholung (also mit anderem Wiederholungsindex auf darüberliegender Ebene)
	 * einzelne prüfungsrelevante Indices angegeben sind, und diese Geschwister-Wiederholung
	 * von {@link EntityIterator#certainlyNotInSameSubtree(RtInternalIdentifier)} nicht als solche
	 * erkannt wird.
	 */
	istEineWiederholungNichtPruefungsRelevant(
		vc: ValidationCache,
		partialIsUnknown: boolean
	): boolean {
		if (
			this._istEineWiederholungNichtPruefungsRelevant(vc, this.getEntityName(), partialIsUnknown)
		) {
			return true;
		}
		const field = this._semIdx.getField();
		if (field != undefined) {
			const eitSemIdx: EntityIterator = new EntityIterator(field, this._originalIterationState);
			return eitSemIdx._istEineWiederholungNichtPruefungsRelevant(
				vc,
				eitSemIdx.getEntityName(),
				partialIsUnknown
			);
		}
		return false;
	}

	private _istEineWiederholungNichtPruefungsRelevant(
		vc: ValidationCache,
		entityName: string,
		partialIsUnknown: boolean
	): boolean {
		let pruefRelIdsMitNamenDesEntityIterators: HashSet<RtInternalIdentifier> | undefined;
        if (partialIsUnknown && vc.isValidationPartWith3ValueLogic() && !this.isField()) {
            pruefRelIdsMitNamenDesEntityIterators = vc
                .getFullyRelevantGroupsIntern()
                .get(entityName) as any;
        } else {
            pruefRelIdsMitNamenDesEntityIterators = vc
                .getPruefungsrelevanteFelderIntern()
                .get(entityName) as any;
        }

		if (pruefRelIdsMitNamenDesEntityIterators == undefined) {
			// kein Feld mit diesem Namen ist prüfungsrelevant
			return true;
		}
		const s: HashSet<RtInternalIdentifier> = new HashSet();
		[...pruefRelIdsMitNamenDesEntityIterators]
			.filter(e => !this._certainlyNotInSameSubtree(e))
			.forEach(e => s.add(e));
		pruefRelIdsMitNamenDesEntityIterators = s;

		if (
			this._existiertEbeneMitSternUndTeilvalidierungUngleich(pruefRelIdsMitNamenDesEntityIterators)
		) {
			return true;
		}
		/*
		 * nun wissen wir: für alle Wiederholungsebenen des EntityIterators mit Stern sind alle
		 * Wiederholungen prüfungsrelevant. Damit genügt es, noch die erste Entity im Iterator auf
		 * Prüfungsrelevanz zu testen
		 */
		if (!EntityUtil.isPruefungsRelevant(this.get(), vc, partialIsUnknown)) {
			return true;
		}
		return false;
	}

	private _certainlyNotInSameSubtree(other: RtInternalIdentifier): boolean {
		const startIndices: number[] = this._ebenenIterator.getStartIndices();
		const endIndices: number[] = this._ebenenIterator.getEndIndices();
		const otherIdx: number[] = other.getIndices();
		for (let i = 0; i < Math.min(startIndices.length, otherIdx.length); i++) {
			const iStart: number = startIndices[i];
			const iEnd: number = endIndices[i];
			const iOther: number = otherIdx[i];
			if (iStart > 0 && iOther > 0 && (iOther < iStart || iOther > iEnd)) {
				return true;
			}
			/*
			 * else either "0" (IIdentifier.ALLE_INDICES), or some special case (semantic index,
			 * unknown, etc)
			 * -> for all other cases we don't know if/that _other_ is outside of our
			 * subtree (or would need to check more carefully)
			 */
		}
		return false;
	}

	/**
	 * Liefert <code>true</code>, wenn in den prüfungsrelevanten Feldern ein Identifier übergeben
	 * wurde, der
	 * <ul>
	 * <li>den Namen des {@link EntityIterator}s hat und</li>
	 * <li>eine konkrete Wiederholung (d.h. ungleich [IIdentifier.ALL_INDICES](
	 * ../../../kernel-core-runtime-api-ts/modules/_validation_iidentifier_.iidentifier.html#all_indices)) spezifiziert
	 * für eine Ebene, für die der {@link EntityIterator} einen Stern hat</li>
	 * </ul>
	 * <b>Vorbedingung:
	 * <ul>
	 * <li></b> die übergebene Menge von prüfungsrelevanten Feldern ist nicht <code>undefined</code>.
	 * </li>
	 * <li>wird nur im Rahmen einer Teilvalidierung aufgerufen</li>
	 * </ul>
	 */
	private _existiertEbeneMitSternUndTeilvalidierungUngleich(
		pruefRelIdsMitNamenDesEntityIterators: HashSet<RtInternalIdentifier>
	): boolean {
		for (let i = 0; i < this.anzahlWiederholungsEbenen; i++) {
			if (
				this._isStern(i) &&
				EntityIterator._istWiederholungKonkretFuerEineId(pruefRelIdsMitNamenDesEntityIterators, i)
			) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Liefert <code>true</code>, wenn der {@link EntityIterator} für die i-te Wiederholungsebene einen Stern
	 * spezifiziert.
	 */
	private _isStern(ebene: number): boolean {
		if (ebene >= this.anzahlWiederholungsEbenen) {
			return false;
		}
		return (
			this._ebenenIterator.getEndIndices()[ebene] > this._ebenenIterator.getStartIndices()[ebene]
		);
	}

	/**
	 * Liefert <code>true</code>, wenn einer der übergebenen Identifier für die i-te
	 * Wiederholungsebene einen konkreten Wert, d.h. <b>nicht</b> [IIdentifier.ALL_INDICES](
	 * ../../../kernel-core-runtime-api-ts/modules/_validation_iidentifier_.iidentifier.html#all_indices)
	 * spezifiziert.
	 */
	private static _istWiederholungKonkretFuerEineId(
		pruefRelFelder: HashSet<RtInternalIdentifier>,
		i: number
	): boolean {
		return Array.from(pruefRelFelder).some((id: RtInternalIdentifier) => {
			return id.getIndices()[i] !== IIdentifier.ALL_INDICES;
		});
	}

	isField(): boolean {
		return this._isField;
	}

	anzahlFelder(): number {
		return this._ebenenIterator.getAnzahlWiederholungen();
	}
}
