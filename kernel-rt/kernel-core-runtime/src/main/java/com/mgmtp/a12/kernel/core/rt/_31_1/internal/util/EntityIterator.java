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

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.core.ValidationCache;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * This class is used to iterate over the repetitions of an entity in an entity list.
 * The repetitions on each layer either specify a specific index or a wildcard, that will be
 * transformed to a representation &lt;from&gt;-&lt;to&gt; during code generation.
 * <p>
 * For the old world, the first layer (i.e. index 0) always specifies the USB, the second
 * the repetition of the root group, and the last one, the row number. For new world models,
 * additionally the group repetitions need to be specified.
 */
public class EntityIterator implements Iterable<RtInternalIdentifier> {

	private final String entityName;

	private final EbenenIterator ebenenIterator;

	private final boolean isField;

	private final SemanticIndexTemplate semIdx;

	private final String category;

	private final IterationState originalIterationState;

	private final boolean outer;

	public EntityIterator(RtIdentifierTemplate rtIdContainer, IterationState idx) {
		originalIterationState = idx;
		category = rtIdContainer.getCategory();
		entityName = rtIdContainer.getUniqueName();
		isField = rtIdContainer.isField();
		ebenenIterator = EbenenIterator.createEbenenIterator(rtIdContainer, idx);
		outer = rtIdContainer.isOuter();
		semIdx = rtIdContainer.getSemanticIndex();
	}

	public EntityIterator(RtInternalIdentifier id, IterationState idx) {
		originalIterationState = idx;
		semIdx = SemanticIndexTemplate.createFromSpec(id.getSemanticIndex());
		category = id.getCategory();
		entityName = id.getName();
		isField = !entityName.startsWith("/");
		ebenenIterator = EbenenIterator.createEbenenIterator(id, idx);
		outer = false;
	}

	/**
	 * Gibt den ersten Identifier zurück
	 */
	public RtInternalIdentifier get() {
		IterationState usedIterationState = outer ? originalIterationState.getOuterState() : originalIterationState;
		return RtInternalIdentifier.createFromIterationState(
				entityName,
				ebenenIterator.iterator().next(),
				category,
				semIdx.createSemanticIndex(usedIterationState));
	}

	/**
	 * Internal class is used to implement the iterator over the corresponding identifiers. In case
	 * of parallel iteration the respective index is replaced with the semantic index using the
	 * value of the current {@link IterationState}.
	 */
	private class InnerIterator implements Iterator<RtInternalIdentifier> {
		private final Iterator<IterationState> iter;

		public InnerIterator() {
			this.iter = ebenenIterator.iterator();
		}

		public boolean hasNext() {
			return iter.hasNext();
		}

		public RtInternalIdentifier next() {
			IterationState iterState = iter.next();
			return RtInternalIdentifier.createFromIterationState(
					entityName,
					iterState,
					category,
					semIdx.createSemanticIndex(originalIterationState));
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public Iterator<RtInternalIdentifier> iterator() {
		return new InnerIterator();
	}

	/**
	 * Setzt den Endindex des USB auf den maximalen USB, für den ein Feld
	 * angegeben wurde, wenn dieser Wert zwischen Start- und End-USB des
	 * {@link EntityIterator}s liegt. Ist der Wert kleiner als der Start-USB, wird End-USB
	 * gleich Start-USB gesetzt. Sonst bleibt der Iterator unverändert.
	 * <p>
	 * Wird für die Optimierung für den Sternoperator verwendet.
	 * </p>
	 *
	 * @return - true, wenn der End-USB geändert wurde, false sonst
	 */
	public boolean setEndeUsbAufMaxGesetzterUsbIfPossible(ValidationCache vc) {
		return setEndeIndexFallsKleinerAktuellesEnde(0, vc.getMaxInternerUSBMitAngegebenenFeldern());
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
	public boolean setEndeIndexFallsKleinerAktuellesEnde(int ebene, long neuerWert) {
		return ebenenIterator.setEndeIndexFallsKleinerEnde(ebene, neuerWert);
	}

	/**
	 * Setzt den Endindex der Zeile auf die maximale Zeile, für die ein Feld
	 * angegeben wurde, wenn dieser Wert zwischen der Start- und der Endzeile
	 * des {@link EntityIterator}s liegt. Ist der Wert kleiner als die Startzeile,
	 * wird Endzeile gleich Startzeile gesetzt. Sonst bleibt der Iterator unverändert.
	 * <p>
	 * Wird für die Optimierung für den Sternoperator verwendet.
	 * </p>
	 *
	 * @return true, falls der Endeindex geändert wurde, false sonst
	 */
	public boolean setEndeZeileAufMaxGesetzt(ValidationCache vc) {
		int zeilenEbene = getAnzahlWiederholungsEbenen() - 1;
		return ebenenIterator.setEndeIndexFallsKleinerEnde(zeilenEbene, vc.getMaxGesetzteZeilen());
	}

	public boolean isUStern() {
		return ebenenIterator.getEndeUsb() > ebenenIterator.getStartUsb();
	}

	void setIndexFuerEbene(int ebene, long index) {
		ebenenIterator.setStartIndexFuerEbene(ebene, index);
		ebenenIterator.setEndeIndexFuerEbene(ebene, index);
	}

	public String getEntityName() {
		return entityName;
	}

	/**
	 * Setzt den Iterator für alle Ebenen ab Startindex auf den maximal gesetzten Wert.
	 *
	 * @return true, falls mindestens ein Endeindex geändert wurde, false sonst
	 */
	public boolean setzeAlleEbenenMitZeileAufMaxGesetztIfPossible(ValidationCache vc, ValidierungArt valArt, int startIndex) {
		boolean indexGesetzt = false;
		if (this.isField && valArt.zeileGleich() && setEndeZeileAufMaxGesetzt(vc)) {
			indexGesetzt = true;
		}

		int maxLevel = isField ? getAnzahlWiederholungsEbenen() - 1 : getAnzahlWiederholungsEbenen();
		for (int i = startIndex; i < maxLevel; ++i) {
			if (valArt.istEbeneGleich(i) && ebenenIterator.setEndeIndexFallsKleinerEnde(i,
					vc.getMaxGesetzteWiederholungOhneZeile(i))) {
				indexGesetzt = true;
			}
		}
		return indexGesetzt;
	}

	public int getAnzahlWiederholungsEbenen() {
		return ebenenIterator.getAnzahlEbenen();
	}

	/**
	 * Optimiert den {@link EntityIterator} so, daß der {@link #iterator()} Identifier überspringt,
	 * für deren Wiederholung keine Werte angegeben wurden.
	 */
	public void optimiereMitGesetztenWiederholungenWennSinnvoll(ValidationCache vc) {
		ebenenIterator.optimiereMitGesetztenWiederholungenWennSinnvoll(vc.getGesetzteWiederholungen());
	}

	/**
	 * Optimierte Ermittlung, ob eine Wiederholung des {@link EntityIterator}s nicht
	 * prüfungsrelevant ist.
	 * <br>
	 * <b>Vorbedingung:</b> Es handelt sich um eine Teilvalidierung.<br>
	 * <b>Anmerkung:</b><br>
	 * Die Methode kann <code>true</code> liefern im Sonderfall, daß in den prüfungsrelevanten
	 * Feldern alle Wiederholungen eines Feldes einzeln angegeben sind statt mit
	 * {@link IIdentifier#ALL_INDICES}. <br>
	 * Von dieser Vorgehensweise wird explizit abgeraten. Sollte es doch vorkommen, wird im
	 * schlimmsten Fall der Fehler bei der Vollvalidierung gemeldet, daher ist diese
	 * Optimierung akzeptabel.<br>
	 * Außerdem falsch true liefern kann die Methode, wenn die Wiederholbarkeit mit
	 * {@link IIdentifier#ALL_INDICES} als relevant gesetzt ist, aber in einer
	 * Geschwister-Wiederholung (also mit anderem Wiederholungsindex auf darüberliegender Ebene)
	 * einzelne prüfungsrelevante indices angegeben sind, und diese Geschwister-Wiederholung
	 * von {@link EntityIterator#certainlyNotInSameSubtree(RtInternalIdentifier)} nicht als solche
	 * erkannt wird.
	 */
	public boolean istEineWiederholungNichtPruefungsRelevant(ValidationCache vc, boolean partialIsUnknown) {
		if (istEineWiederholungNichtPruefungsRelevant(vc, getEntityName(), partialIsUnknown)) {
			return true;
		}
		if (semIdx.getField() != null) {
			EntityIterator eitSemIdx = new EntityIterator(semIdx.getField(), originalIterationState);
			return eitSemIdx.istEineWiederholungNichtPruefungsRelevant(vc, eitSemIdx.getEntityName(),
					partialIsUnknown); // eitSemIdx is never a group, parameter irrelevant
		}
		return false;
	}

	private boolean istEineWiederholungNichtPruefungsRelevant(ValidationCache vc, String entityName, boolean partialIsUnknown) {
		Set<RtInternalIdentifier> pruefRelIdsMitNamenDesEntityIterators;
		if (partialIsUnknown && vc.isValidationPartWith3ValueLogic() && !this.isField) {
			pruefRelIdsMitNamenDesEntityIterators = vc.getFullyRelevantGroupsIntern().get(entityName);
		} else {
			pruefRelIdsMitNamenDesEntityIterators = vc.getPruefungsrelevanteEntitiesIntern().get(entityName);
		}

		if (pruefRelIdsMitNamenDesEntityIterators == null) {//kein Feld mit diesem Namen ist prüfungsrelevant
			return true;
		}

		pruefRelIdsMitNamenDesEntityIterators = pruefRelIdsMitNamenDesEntityIterators.stream()
				.filter(e -> !certainlyNotInSameSubtree(e)).collect(Collectors.toSet());

		if (existiertEbeneMitSternUndTeilvalidierungUngleich(pruefRelIdsMitNamenDesEntityIterators)) {
			return true;
		}
		/*
		 * nun wissen wir: für alle Wiederholungsebenen des EntityIterators mit Stern sind alle
		 * Wiederholungen prüfungsrelevant. Damit genügt es, noch die erste Entity im Iterator auf
		 * Prüfungsrelevanz zu testen
		 */
		return (!EntityUtil.isPruefungsRelevant(get(), vc, partialIsUnknown));
	}

	private boolean certainlyNotInSameSubtree(RtInternalIdentifier other) {
		long[] startIndices = ebenenIterator.getStartIndices();
		long[] endIndices = ebenenIterator.getEndeIndices();
		long[] otherIdx = other.getIndicesWithUsb();
		for (int i = 0; i < Math.min(startIndices.length, otherIdx.length); i++) {
			long iStart = startIndices[i];
			long iEnd = endIndices[i];
			long iOther = otherIdx[i];
			if (iStart > 0 && iOther > 0
					&& (iOther < iStart || iOther > iEnd)) {
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
	 * <li>eine konkrete Wiederholung (d.h. ungleich {@link IIdentifier#ALL_INDICES}) spezifiziert
	 * für eine Ebene, für die der {@link EntityIterator} einen Stern hat</li>
	 * </ul>
	 * <b>Vorbedingung:
	 * <ul>
	 * <li></b> die übergebene Menge von prüfungsrelevanten Feldern ist nicht <code>null</code>.
	 * </li>
	 * <li>wird nur im Rahmen einer Teilvalidierung aufgerufen</li>
	 * </ul>
	 */
	private boolean existiertEbeneMitSternUndTeilvalidierungUngleich(
			Set<RtInternalIdentifier> pruefRelIdsMitNamenDesEntityIterators) {
		for (int i = 0; i < getAnzahlWiederholungsEbenen(); i++) {
			if (isStern(i) && istWiederholungKonkretFuerEineId(pruefRelIdsMitNamenDesEntityIterators, i)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Liefert <code>true</code>, wenn der {@link EntityIterator} für die i-te Wiederholungsebene
	 * einen Stern spezifiziert. Der USB ist mitberücksichtigt als 0-te Ebene.
	 */
	private boolean isStern(int ebene) {
		if (ebene >= getAnzahlWiederholungsEbenen()) {
			return false;
		}
		return ebenenIterator.getEndeIndices()[ebene] > ebenenIterator.getStartIndices()[ebene];
	}

	/**
	 * Liefert <code>true</code>, wenn einer der übergebenen Identifier für die i-te
	 * Wiederholungsebene einen konkreten Wert, d.h. <b>nicht</b> {@link IIdentifier#ALL_INDICES}
	 * spezifiziert. Der USB ist mitberücksichtigt als 0-te Ebene.
	 */
	private static boolean istWiederholungKonkretFuerEineId(Set<RtInternalIdentifier> pruefRelFelder, int i) {
		for (RtInternalIdentifier id : pruefRelFelder) {
			long[] indicesMitUSB = FeldUtil.getIndicesMitUSB(id);
			if (indicesMitUSB[i] != IIdentifier.ALL_INDICES) {
				return true;
			}
		}
		return false;
	}

	public boolean isField() {
		return isField;
	}

	public int getAnzahlFelder() {
		return ebenenIterator.getAnzahlWiederholungen();
	}
}
