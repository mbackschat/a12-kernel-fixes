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

import type { IMetaDataValidierung } from "../core/IMetaDataValidierung.js";
import type { ValidationCache } from "../core/ValidationCache.js";

import { EbenenIterator } from "./EbenenIterator.js";
import { EntityIterator } from "./EntityIterator.js";
import type { IterationState } from "./IterationState.js";
import type { RtIdentifierTemplate } from "./RtIdentifierTemplate.js";
import type { RtInternalIdentifier } from "./RtInternalIdentifier.js";
import type { ValidierungArt } from "./ValidierungArt.js";

/**
 * Implementiert einen KontextIterator für die Implementierung von
 * <code>AnzahlAngegebenerKontexte</code>
 * <p>
 * Analog zum {@link EntityIterator} kann durch eine Menge von Kontexten iteriert werden - die alle
 * die verschiedenen Wiederholungen des gleichen Kontextes beschreiben.
 * <p>
 * Die Klasse bietet eine Iteration über die verschiedenen Entities des Kontextes an
 * {@link getNextEntity} und es kann zudem zum nächsten Kontext iteriert werden
 * {@link setToNextWiederholung}.
 *
 * <p>
 * Der KontextIterator kann mittels der Methode {@link optimiereIteratorWennMoeglich} so
 * optimiert werden, dass beim Zugriff auf die Entities und die Kontextwiederholungen immer nur bis
 * zur maximal gesetzten Wiederholung iteriert wird. Der Kontext-Iterator ist so für die
 * Implementierung des Sprachkonstruktes <code>AnzahlAngegebenerKontexte</code> optimiert.
 *
 */

export class KontextIterator {
	/**
	 * Iterator, der über die Kontextwiederholungen iteriert
	 * Der EbenenIterator wird nur benötigt, um bei der Optimierung die Ende-Indices
	 * zu ändern. Die Optimierung wird unabhängig vom Erzeugen eines
	 * KontextIterators aufgerufen, damit der KontextIterator zukünftig in verschiedenen
	 * Sprachkonstrukten verwendet werden kann.
	 * Für das eigentliche Weiterschalten wird {@link _kontextEbenenAsIterationStateIterator} verwendet.
	 */
	private readonly _kontextEbenenIter: EbenenIterator;

	/**
	 * Dieser Iterator wird beim Weiterschalten des Kontext-Iterators verwendet.
	 */
	private _kontextEbenenAsIterationStateIterator: IIterator<IterationState>;

	/**
	 * Liste der {@link EntityIterator}en, zu allen Entities, die im Kontext liegen.
	 * Die EntityIteratoren werden immer resettet, wenn der KontextIterator um eine Wiederholung
	 * weitergeschaltet wird.
	 */
	private readonly _entityIters: EntityIterator[] = [];

	/**
	 * Liste der Iteratoren zu den {@link EntityIterator}en. Bei getNextEntity werden zunächst beginnend von
	 * vorne die einzelnen EntityIteratoren weitergeschaltet, bis alle EntityIteratoren der aktuellen
	 * Kontextwiederholungsebene an der Ende-Position sind. Dann kann die Wiederholungsebene des
	 * Kontextes weitergeschaltet werden, bis es keine neue Wiederholungsebene mehr gibt.
	 */
	private _entityIdentifierIteratoren: IIterator<RtInternalIdentifier>[] = [];

	/**
	 * Wird verwendet, um sich die Position in der {@link EntityIterator}-Liste zu merken, die dem aktuellen
	 * EntityIterator entspricht. Wird mit 0 vorbelegt, da mit dem ersten EntityIterator begonnen wird
	 * zu iterieren.
	 */
	private _aktPositionInEntityIteratoren = 0;

	private _hasNext = true;

	/**
	 * Erzeugt einen neuen Kontext-Iterator
	 *
	 * @param kontextUndSeineEntities	an der ersten Position ist die Stringdarstellung des Kontextes, an den
	 *            						restlichen Stellen die Entities, die im Kontext liegen, jeweils mit den
	 *            						Ebenenspezifikationen, die noch Iterationsvariablen enthalten können
	 * @param indices	die aktuellen Indices, durch die die Iterationsvariablen zu ersetzen sind
	 */
	constructor(
		groupRtIdContainer: RtIdentifierTemplate,
		groupsContainedFieldsRtIdContainer: RtIdentifierTemplate[],
		indices: IterationState,
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		metaData: IMetaDataValidierung
	) {
		this._kontextEbenenIter = EbenenIterator.createEbenenIterator(groupRtIdContainer, indices);
		this._kontextEbenenAsIterationStateIterator = this._kontextEbenenIter.iterator();
		/*
		 * next muss für eine korrekte Initialisierung hier einmal aufgerufen werden, sonst werden
		 * die Entities für die erste Kontextwiederholung über getNextEntity doppelt zurückgegeben
		 */
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const firstKontextEbenen: number[] = this._kontextEbenenAsIterationStateIterator
			.next()!
			.getIndexes();

		/**
		 * Alle EntityIteratoren für die Entities des Kontextes werden erzeugt.
		 * Der erste Position im Parameter ist der Kontext, daher beginnt die Schleife bei 1.
		 */
		for (const rtIdContainer of groupsContainedFieldsRtIdContainer) {
			const fiter: EntityIterator = new EntityIterator(rtIdContainer, indices);
			this._entityIters.push(fiter);
		}

		/**
		 * initialisiere die zugehörigen EntityIteratoren mit den aktuellen Wiederholungsebenen
		 * des ersten Kontextes.
		 */
		this._resetEntityIteratorenListe(firstKontextEbenen);
	}

	/**
	 * Ein Identifier der die konkrete nächste Wiederholung beschreibt, wird zurückgegeben.
	 * <ol>
	 * <li>die nächste Wiederholung der aktuellen Entity</li>
	 * <li>die erste Wiederholung der nächsten Entity</li>
	 * <li>die erste Entity der nächsten Wiederholung des Kontextes</li>
	 * <li>undefined, da es keine weitere Entity mehr gibt</li>
	 * </ol>
	 * Anmerkung: Die Implementierung der Methode untersucht die oben aufgelisteten Fälle in der
	 * umgekehrten Reihenfolge. Falls notwendig, wird weitergeschaltet und die Methode dann erneut
	 * aufgerufen. Durch das erneute Aufrufen der Methode ist sichergestellt, dass alle
	 * Randbedingungen immer geprüft werden.
	 *
	 * @return nächste Entity oder undefined, wenn es keine weitere Entity mehr gibt.
	 */
	public getNextEntity(): RtInternalIdentifier | undefined {
		if (!this._hasNext) {
			// wir sind beim Iterieren des Kontextes zuende.
			return undefined;
		}
		// Über alle Entity-Iteratoren wurde bereits iteriert,  setze auf die nächste
		// Kontext-Wiederholung
		if (this._aktPositionInEntityIteratoren === this._entityIdentifierIteratoren.length) {
			this.setToNextWiederholung();
			return this.getNextEntity();
		}

		const aktuellerEntityIterator: IIterator<RtInternalIdentifier> =
			this._entityIdentifierIteratoren[this._aktPositionInEntityIteratoren];
		// Über alle Wiederholungen der aktuellen Entity wurde bereits iteriert,  setze auf den nächsten
		// EntityIterator
		if (!aktuellerEntityIterator.hasNext()) {
			this._aktPositionInEntityIteratoren++;
			return this.getNextEntity();
		}

		// Gebe die nächste Wiederholung der aktuellen Entity zurück
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		return aktuellerEntityIterator.next()!;
	}

	/**
	 * Setzt den Iterator auf die nächste Wiederholung des Kontextes oder markiert das Ende, falls
	 * es keine nächste Wiederholung gibt.
	 *
	 * @return true, falls es eine nächste Wiederholung des Kontextes gibt, false sonst.
	 */
	public setToNextWiederholung(): boolean {
		if (!this._kontextEbenenAsIterationStateIterator.hasNext()) {
			this._hasNext = false;
			return false;
		}

		/**
		 * es gibt eine neue Wiederholung, schalte den Kontext-Iterator weiter und setze alle
		 * Entity-Iteratoren zurück.
		 */
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const kontextIndices: number[] = this._kontextEbenenAsIterationStateIterator
			.next()!
			.getIndexes();
		this._resetEntityIteratorenListe(kontextIndices);

		return true;
	}

	/**
	 * Bereitet die EntityIteratoren Liste für die aktuelle Wiederholung des übergebenen Kontextes vor.
	 * Dazu werden alle EntityIteratoren zurückgesetzt und die Indices der EntityIteratoren für die
	 * Wiederholungsebenen des Kontextes auf die aktuellen Indices des Kontextes gesetzt. Die
	 * Wiederholungsebenen unterhalb des Kontextes werden auf 1 zurückgesetzt.
	 *
	 * Note: In case of parallel iteration the index value is already concrete and does not change,
	 * because the {@link KontextIterator KontextIterator(
	 * RtIdentifierTemplate, RtIdentifierTemplate[], IterationState, IMetaDataValidierung)} is
	 * created for a concrete iteration state. So we only need to reset the indices and not the value.
	 *
	 * @param kontextIndices	die Wiederholungsindices des Kontextes, der die Indices der EntityIteratoren bestimmt.
	 */
	private _resetEntityIteratorenListe(kontextIndices: number[]): void {
		this._entityIdentifierIteratoren = new Array<IIterator<RtInternalIdentifier>>();
		for (const iter of this._entityIters) {
			this._uebernehmeKontextIndicesInEntityIteratoren(iter, kontextIndices);
			this._entityIdentifierIteratoren.push(iter.iterator());
		}
		this._aktPositionInEntityIteratoren = 0;
	}

	/**
	 * Die Indices des Kontextes werden in den EntityIterator übernommen. Es wird jeweils nur über die
	 * Indices unterhalb des Kontextes iteriert.
	 *
	 * @param fiter 	EntityIterator, in den die Kontextindices übernommen werden sollen
	 * @param kontextIndices 	die aktuellen Wiederholungen des Kontextes
	 */
	private _uebernehmeKontextIndicesInEntityIteratoren(
		fiter: EntityIterator,
		kontextIndices: number[]
	): void {
		for (let i = 0; i < kontextIndices.length; i++) {
			fiter.setIndexFuerEbene(i, kontextIndices[i]);
		}
	}

	/**
	 * Optimiert den Kontextiterator wie folgt:
	 * <ol>
	 * <li>Setzt alle Indices auf den für die Wiederholungsebene maximal gesetzten Wert, wenn</li>
	 * <ul>
	 * <li>dieser im Intervall zwischen Start- und Endeindex ist</li>
	 * <li>und wenn entweder eine Vollvalidierung vorliegt oder die Ebene bei der Teilvalidierung
	 * gleichbehandelt wird, d.h. wenn in den prüfungsrelevanten Feldern für die Ebene 0
	 * spezifiziert wurde</li>
	 * </ul>
	 * <li>Außerdem werden der KontextIterator und die EntityIteratoren weiter optimiert mit den
	 * gesetzten Wiederholungen, vgl. {@link ValidationCache._gesetzteWiederholungen}</li>
	 * </ol>
	 */
	public optimiereIteratorWennMoeglich(vc: ValidationCache, valArt: ValidierungArt): void {
		for (let i = 0; i < this._kontextEbenenIter.getAnzahlEbenen(); ++i) {
			if (valArt.istEbeneGleich(i)) {
				this._kontextEbenenIter.setEndeIndexFallsKleinerEnde(
					i,
					vc.getMaxGesetzteWiederholungOhneZeile(i)
				);
			}
		}
		this._kontextEbenenIter.optimiereMitGesetztenWiederholungenWennSinnvoll(
			vc.getGesetzteWiederholungen()
		);
		const startIndex: number = this._kontextEbenenIter.getAnzahlEbenen();
		for (const eIt of this._entityIters) {
			/**
			 * Für die Wiederholungsebenen der Entities, die nicht von den Kontext-Wiederholungen
			 * gesetzt sind, werden die maximalen Wiederholungsgrenzen fix auf die maximal gesetzten
			 * Wiederholungen optimiert. D.h. eine Optimierung pro Kontextwiederholung ist zurzeit
			 * nicht vorgesehen.
			 */
			eIt.setzeAlleEbenenMitZeileAufMaxGesetztIfPossible(vc, valArt, startIndex);
			eIt.optimiereMitGesetztenWiederholungenWennSinnvoll(vc);
		}
		this._kontextEbenenAsIterationStateIterator = this._kontextEbenenIter.iterator();
		// TODO: Fix this the next time the file is edited.
		// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
		const firstKontextEbene: number[] = this._kontextEbenenAsIterationStateIterator
			.next()!
			.getIndexes();
		this._resetEntityIteratorenListe(firstKontextEbene);
	}

	/**
	 * prüft, ob der KontextIterator einen Kontext beschreibt, bei dem ein Feld in mindestens einer
	 * Wiederholung nicht prüfungsrelevant ist. Falls dies der Fall ist, wird true zurückgegeben. Siehe
	 * {@link EntityIterator.istEineWiederholungNichtPruefungsRelevant EntityIterator.
	 * istEineWiederholungNichtPruefungsRelevant(ValidationCache)}
	 */
	public istEineWiederholungNichtPruefungsRelevant(
		validationCache: ValidationCache,
		partialIsUnknown: boolean
	): boolean {
		for (const eIt of this._entityIters) {
			if (eIt.istEineWiederholungNichtPruefungsRelevant(validationCache, partialIsUnknown)) {
				return true;
			}
		}
		return false;
	}

	public getAnzahlKontexte(): number {
		return this._kontextEbenenIter.getAnzahlWiederholungen();
	}
}
