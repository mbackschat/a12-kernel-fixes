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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._30_8.internal.core.ValidationCache;

/**
 * Implementiert einen KontextIterator für die Implementierung von
 * <code>AnzahlAngegebenerKontexte</code>
 * <p>
 * Analog zum {@link EntityIterator} kann durch eine Menge von Kontexten iteriert werden - die alle
 * die verschiedenen Wiederholungen des gleichen Kontextes beschreiben.
 * <p>
 * Die Klasse bietet eine Iteration über die verschiedenen Entities des Kontextes an
 * {@link #getNextEntity} und es kann zudem zum nächsten Kontext iteriert werden
 * {@link #setToNextWiederholung}.
 *
 * <p>
 * Der KontextIterator kann mittels der Methode
 * {@link EntityIterator#setzeAlleEbenenMitZeileAufMaxGesetztIfPossible} so optimiert werden,
 * dass beim Zugriff auf die Entities und die Kontextwiederholungen immer nur bis
 * zur maximal gesetzten Wiederholung iteriert wird. Der Kontext-Iterator ist so für die
 * Implementierung des Sprachkonstruktes <code>AnzahlAngegebenerKontexte</code> optimiert.
 *
 * <b>Anmerkung:</b> Diese Klasse wird nur noch für H-DVs verwendet.
 */
public class KontextIterator {

	/**
	 * Iterator, der über die Kontextwiederholungen iteriert.
	 * Der EbenenIterator wird nur benötigt, um bei der Optimierung die Ende-Indices zu ändern. Die
	 * Optimierung wird unabhängig vom Erzeugen eines KontextIterators aufgerufen, damit der
	 * KontextIterator zukünftig in verschiedenen Sprachkonstrukten verwendet werden kann.
	 * Für das eigentliche Weiterschalten wird {@link #kontextEbenenAsIterationStateIterator}
	 * verwendet.
	 */
	private final EbenenIterator kontextEbenenIter;

	/**
	 * Dieser Iterator wird beim Weiterschalten des Kontext-Iterators verwendet.
	 */
	private Iterator<IterationState> kontextEbenenAsIterationStateIterator;

	/**
	 * Liste der FeldIteratoren, zu allen Feldern, die im Kontext liegen.
	 * Die FeldIteratoren werden immer resettet, wenn der KontextIterator um eine Wiederholung
	 * weitergeschaltet wird.
	 */
	private final List<EntityIterator> entityIters = new ArrayList<>();

	/**
	 * Liste der Iteratoren zu den FeldIteratoren. Bei getNextField werden zunächst beginnen von
	 * vorne die einzelnen Felditeratoren weitergeschaltet, bis alle FeldIteratoren der aktuellen
	 * Kontextwiederholungsebene an der Ende-Position sind. Dann kann die Wiederholungsebene des
	 * Kontextes weitergeschaltet werden, bis es keine neue Wiederholungsebene mehr gibt.
	 */
	private final List<Iterator<RtInternalIdentifier>> entityIdentifierIteratoren = new ArrayList<>();

	/**
	 * Wird verwendet, um sich die Position in der EntityIterator-Liste zu merken, die dem aktuellen
	 * Felditerator entspricht. Wird mit 0 vorbelegt, da mit dem ersten EntityIterator begonnen wird
	 * zu iterieren.
	 */
	private int aktPositionInEntityIteratoren = 0;

	private boolean hasNext = true;

	/**
	 * Erzeugt einen neuen Kontext-Iterator
	 *
	 * @param groupRtIdContainer
	 *        {@link RtIdentifierTemplate} des Kontexts
	 * @param groupsContainedFieldsRtIdContainer
	 *        {@link List} von {@link RtIdentifierTemplate} der
	 * 		Felder, die im Kontext liegen,
	 * 		jeweils mit den Ebenenspezifikationen, die noch
	 * 		Iterationsvariablen enthalten
	 * 		können
	 * @param indices
	 * 		- die aktuellen Indices, durch die die
	 * 		Iterationsvariablen zu ersetzen sind
	 */
	public KontextIterator(RtIdentifierTemplate groupRtIdContainer, List<RtIdentifierTemplate> groupsContainedFieldsRtIdContainer,
			IterationState indices) {
		kontextEbenenIter = EbenenIterator.createEbenenIterator(groupRtIdContainer, indices);
		kontextEbenenAsIterationStateIterator = kontextEbenenIter.iterator();
		/*
		 * next muss für eine korrekte Initialisierung hier einmal aufgerufen werden, sonst werden
		 * die Felder für die erste Kontextwiederholung über getNextFeld doppelt zurückgegeben
		 */
		long[] firstKontextEbenen = kontextEbenenAsIterationStateIterator.next().getIndexes();

		/**
		 * Alle FeldIteratoren für die Felder des Kontextes werden erzeugt.
		 */
		for (RtIdentifierTemplate rtIdContainer : groupsContainedFieldsRtIdContainer) {
			EntityIterator fiter = new EntityIterator(rtIdContainer, indices);
			entityIters.add(fiter);
		}

		/**
		 * initialisiere die zugehörigen FeldIteratoren mit den aktuellen Wiederholungsebenen
		 * des ersten Kontextes.
		 */
		resetEntityIteratorenListe(firstKontextEbenen);
	}

	/**
	 * Ein Identifier der die konkrete nächste Wiederholung beschreibt, wird zurückgegeben.
	 * <ol>
	 * <li>die nächste Wiederholung der aktuellen Entity</li>
	 * <li>die erste Wiederholung der nächsten Entity</li>
	 * <li>die erste Entity der nächsten Wiederholung des Kontextes</li>
	 * <li>null, da es kein weiteres Feld mehr gibt</li>
	 * </ol>
	 * Anmerkung: Die Implementierung der Methode untersucht die oben aufgelisteten Fälle in der
	 * umgekehrten Reihenfolge. Falls notwendig, wird weitergeschaltet und die Methode dann erneut
	 * aufgerufen. Durch das erneute Aufrufen der Methode ist sichergestellt, dass alle
	 * Randbedingungen
	 * immer geprüft werden.
	 *
	 * @return nächstes Feld oder null, wenn es kein weiteres Feld mehr gibt.
	 */
	public RtInternalIdentifier getNextEntity() {
		if (!hasNext) { // wir sind beim Iterieren des Kontextes zuende.
			return null;
		}
		//Über alle Feld-Iteratoren wurde bereits iteriert,  setze auf die nächste
		//Kontext-Wiederholung
		if (aktPositionInEntityIteratoren == entityIdentifierIteratoren.size()) {
			setToNextWiederholung();
			return getNextEntity();
		}

		Iterator<RtInternalIdentifier> aktuellerFeldIterator = entityIdentifierIteratoren.get(aktPositionInEntityIteratoren);
		//Über alle Wiederholungen des aktuellen Feldes wurde bereits iteriert,  setze auf den nächsten
		//EntityIterator
		if (!aktuellerFeldIterator.hasNext()) {
			aktPositionInEntityIteratoren++;
			return getNextEntity();
		}

		//Gebe die nächste Wiederholung des aktuellen Feldes zurück
		return aktuellerFeldIterator.next();
	}

	/**
	 * Setzt den Iterator auf die nächste Wiederholung des Kontextes oder markiert das Ende, falls
	 * es keine nächste Wiederholung gibt.
	 *
	 * @return true, falls es eine nächste Wiederholung des Kontextes gibt, false sonst.
	 */
	public boolean setToNextWiederholung() {
		if (!kontextEbenenAsIterationStateIterator.hasNext()) {
			hasNext = false;
			return false;
		}

		/**
		 * es gibt eine neue Wiederholung, schalte den Kontext-Iterator weiter und setze alle
		 * Feld-Iteratoren zurück.
		 */
		long[] kontextIndices = kontextEbenenAsIterationStateIterator.next().getIndexes();
		resetEntityIteratorenListe(kontextIndices);

		return true;
	}

	/**
	 * Bereitet die FeldIteratoren Liste für die aktuelle Wiederholung des übergebenen Kontextes
	 * vor. Dazu werden alle FeldIteratoren zurückgesetzt und die Indices der Felditeratoren für die
	 * Wiederholungsebenen des Kontextes auf die aktuellen Indices des Kontextes gesetzt. Die
	 * Wiederholungsebenen unterhalb des Kontextes werden auf 1 zurückgesetzt.
	 * <p>
	 * Note: In case of parallel iteration the index value is already concrete and does not change,
	 * because the
	 * {@link #KontextIterator(RtIdentifierTemplate, List, IterationState, IMetaDataValidierung)} is
	 * created for a concrete iteration state. So we only need to reset the indices and not the
	 * value.
	 *
	 * @param kontextIndices
	 * 		- die Wiederholungsindices des Kontextes, der die Indices der
	 * 		Felditeratoren bestimmt.
	 */
	private void resetEntityIteratorenListe(long[] kontextIndices) {
		entityIdentifierIteratoren.clear();
		for (EntityIterator iter : entityIters) {
			uebernehmeKontextIndicesInEntityIteratoren(iter, kontextIndices);
			entityIdentifierIteratoren.add(iter.iterator());
		}
		aktPositionInEntityIteratoren = 0;
	}

	/**
	 * Die Indices des Kontextes werden in den Felditerator übernommen. Es wird jeweils nur über die
	 * Indices unterhalb des Kontextes iteriert.
	 *
	 * @param fiter
	 * 		EntityIterator, in den die Kontextindices übernommen werden sollen
	 * @param kontextIndices
	 * 		die aktuellen Wiederholungen des Kontextes
	 */
	private void uebernehmeKontextIndicesInEntityIteratoren(EntityIterator fiter, long[] kontextIndices) {
		for (int i = 0; i < kontextIndices.length; i++) {
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
	 * <li>Außerdem werden der KontextIterator und die FeldIteratoren weiter optimiert mit den
	 * gesetzten Wiederholungen, vgl. {@link ValidationCache#getGesetzteWiederholungen()}</li>
	 * </ol>
	 */
	public void optimiereIteratorWennMoeglich(ValidationCache vc, ValidierungArt valArt) {
		for (int i = 0; i < kontextEbenenIter.getAnzahlEbenen(); ++i) {
			if (valArt.istEbeneGleich(i)) {
				kontextEbenenIter.setEndeIndexFallsKleinerEnde(i, vc.getMaxGesetzteWiederholungOhneZeile(i));
			}
		}
		kontextEbenenIter.optimiereMitGesetztenWiederholungenWennSinnvoll(vc.getGesetzteWiederholungen());
		int startIndex = kontextEbenenIter.getAnzahlEbenen();
		for (EntityIterator eIt : entityIters) {
			/**
			 * Für die Wiederholungsebenen der Felder, die nicht von den Kontext-Wiederholungen
			 * gesetzt sind, werden die maximalen Wiederholungsgrenzen fix auf die maximal gesetzten
			 * Wiederholungen optimiert. D.h. eine Optimierung pro Kontextwiederholung ist zurzeit
			 * nicht vorgesehen.
			 */
			eIt.setzeAlleEbenenMitZeileAufMaxGesetztIfPossible(vc, valArt, startIndex);
			eIt.optimiereMitGesetztenWiederholungenWennSinnvoll(vc);
		}
		kontextEbenenAsIterationStateIterator = kontextEbenenIter.iterator();
		long[] firstKontextEbene = kontextEbenenAsIterationStateIterator.next().getIndexes();
		resetEntityIteratorenListe(firstKontextEbene);

	}

	/**
	 * prüft, ob der KontextIterator einen Kontext beschreibt, bei dem ein Feld in mindestens einer
	 * Wiederholung nicht prüfungsrelevant ist. Falls dies der Fall ist, wird true zurückgegeben.
	 * Siehe
	 * {@link EntityIterator#istEineWiederholungNichtPruefungsRelevant}
	 */
	public boolean istEineWiederholungNichtPruefungsRelevant(ValidationCache validationCache, boolean partialIsUnknown) {
		for (EntityIterator eIt : entityIters) {
			if (eIt.istEineWiederholungNichtPruefungsRelevant(validationCache, partialIsUnknown)) {
				return true;
			}
		}
		return false;
	}

	public int getAnzahlKontexte() {
		return kontextEbenenIter.getAnzahlWiederholungen();
	}
}
