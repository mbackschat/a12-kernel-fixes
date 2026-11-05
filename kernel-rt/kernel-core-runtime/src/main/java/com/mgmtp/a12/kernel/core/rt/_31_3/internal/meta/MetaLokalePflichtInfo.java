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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.meta;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.EbenenIterator;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.FeldUtil;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.IterationState;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * Modelliert die PflichtInformationen
 * Jede PflichtInformation hat eine additivMenge und eine pMenge (jeweils Menge von MetaIdentifier)
 * mit folgender Bedeutung:
 * <br>
 * wenn ein Feld aus 'additivMenge' angegeben ist, muss jedes Feld aus 'pMenge' angegeben sein.
 */
public class MetaLokalePflichtInfo {
	private static final int NICHT_RELEVANT = 0;

	private Set<MetaIdentifier> additivMenge = new HashSet<>();
	private Set<MetaIdentifier> pMenge = new HashSet<>();

	private final IMetaDataValidierung metaDataValidierung;

	// Start/Ende der Intervall-Bereiche bzw. 0, falls keine Iteration
	// Für die Iteration über laufende Nummer bzw. Zeilen muss  eine Einschränkung
	// spezifiziert werden können, damit die "alte" Welt abgebildet werden kann.
	// Bei Iteration über wiederholbare Kontexte und USB kann die Iteration nicht eingeschränkt werden.
	// Daher muss nur Zeile und LfdNr gesondert behandelt werden.
	static class Intervall {
		int start = NICHT_RELEVANT;
		int ende = NICHT_RELEVANT;

		Intervall(String s) {
			if (s != null) {
				String[] teile = s.split("-");
				this.start = Integer.parseInt(teile[0]);
				this.ende = Integer.parseInt(teile[1]);
			}
		}

		boolean inIntervall(long i) {
			return start <= i && i <= ende;
		}

	}

	private Intervall zInterval = new Intervall(null);
	private Intervall lInterval = new Intervall(null);

	/**
	 * Alle Wiederholungsebenen, für die eine Iteration spezifiziert wurde.
	 */
	private SortedSet<Integer> ebenenMitIteration = new TreeSet<>();

	/**
	 * Erzeugt eine lokale Pflichtfeldinfo mit Additiv a, Pflichtmenge p für die durch l und z
	 * spezifizierten Muster-Bereiche. l und z sind Strings der form z1-z2 wobei z1, z2
	 * natürliche Zahlen sind und Start und Ende des Bereiches angeben.
	 * Sind sie 0, so handelt es sich nicht um eine Iteration auf dieser Ebene.
	 * Es muss nur die das Intervall für lfdNr und Zeile angegeben werden, da die
	 * Iterationsintervalle auf den anderen Ebenen nicht eingeschränkt werden können.
	 */
	public static MetaLokalePflichtInfo createLokalePflichtFeldInfo(IMetaDataValidierung metaDataValidierung,
			Set<MetaIdentifier> a, Set<MetaIdentifier> p, String l, String z) {
		return new MetaLokalePflichtInfo(metaDataValidierung, a, p, l, z);
	}

	/**
	 * Erzeugt das Objekt aus additiv, pflichtmenge sowie den Iterationsintervallen l und z
	 *
	 * @param additiv
	 * 		Additiv Menge
	 * @param pflichtmenge
	 * 		P-Menge
	 * @param l
	 * 		Iterationsintervall in Stringrepräsentation, 0-0 bedeutet keine Iteration.
	 * @param z
	 * 		Iterationsintervall in Stringrepräsentation, 0-0 bedeutet keine Iteration, darf
	 * 		null sein.
	 */
	private MetaLokalePflichtInfo(IMetaDataValidierung metaDataValidierung, Set<MetaIdentifier> additiv,
			Set<MetaIdentifier> pflichtmenge, String l, String z) {
		this.metaDataValidierung = metaDataValidierung;
		this.additivMenge = additiv;
		this.pMenge = pflichtmenge;
		this.lInterval = new Intervall(l);
		this.zInterval = new Intervall(z);

		for (MetaIdentifier feld : additiv) {
			ebenenMitIteration.addAll(feld.getEbenenMitIteration());
		}
		for (MetaIdentifier feld : pflichtmenge) {
			ebenenMitIteration.addAll(feld.getEbenenMitIteration());
		}

	}

	/**
	 * Methode zur Optimierung der Iteration bei der Entscheidung, ob diese Pflichtinformation für
	 * die übergebene Menge relevant ist.<br>
	 * Ein Index ist für eine Wiederholungsebene relevant:
	 * <ul>
	 * <li>spezifiziert die Pflichtinfo Iteration für die Ebene, so muss der Index in einem
	 * IIdentifier der übergebenen Menge vorkommen und im Iterations-Intervall liegen (bei LfdNr und
	 * Zeile kann dies in der "alten Welt" eingeschränkt sein)</li>
	 * <li>spezifiziert die Pflichtinfo keine Iteration für die Ebene, so ist nur der Index 1
	 * relevant</li>
	 * </ul>
	 */
	private EbenenIterator getRelevanteEbenen(Set<RtInternalIdentifier> ids) {
		if (ebenenMitIteration.isEmpty()) {
			return new EbenenIterator(new long[] { 1 }, new long[] { 1 });
		}

		// Der Ebeneniterator betrachtet nur relevante Ebenen,
		// Untersuche nur bis zur höchsten Ebene mit Iteration
		long[] startIndices = new long[ebenenMitIteration.last() + 1];
		long[] endeIndices = new long[ebenenMitIteration.last() + 1];

		int nochKeinWertGesetzt = -9;

		//Alle Ebenen ohne Iteration sind 1. Die anderen Ebenen werden im
		//folgenden noch gesetzt.
		for (int i = 0; i < startIndices.length; i++) {
			startIndices[i] = ebenenMitIteration.contains(i) ? nochKeinWertGesetzt : 1;
			endeIndices[i] = ebenenMitIteration.contains(i) ? nochKeinWertGesetzt : 1;
		}

		//Für jede Ebene, die eine Iteration ist, soll das maximale Iterationsintervall bestimmt werden.
		//Dazu werden alle Idententifier auf diesen Ebenen geprüft.
		for (RtInternalIdentifier id : ids) {
			for (int i : ebenenMitIteration) {
				if (isEbeneRelevant(i, id)) {
					long idx = id.getIndices()[i];
					if (startIndices[i] == nochKeinWertGesetzt) {
						startIndices[i] = idx;
						endeIndices[i] = idx;
					} else {
						startIndices[i] = Math.min(idx, startIndices[i]);
						endeIndices[i] = Math.max(idx, endeIndices[i]);
					}
				}
			}
		}

		return new EbenenIterator(startIndices, endeIndices);
	}

	/**
	 * Eine Ebene ist relevant wenn
	 * <ol>
	 * <li>Ebene ist die laufende Nummer und die laufende Nummer des Identifier liegt im
	 * Iterationsintervall</li>
	 * <li>Ebene ist die Zeile und die Zeile des Identifier liegt im Iterationsintervall</li>
	 * <li>für alle anderen Ebenen wird geprüft, ob der Identifier diese Ebene umfasst.</li>
	 * </ol>
	 *
	 * @param i
	 * 		zu prüfende Ebene
	 * @param id
	 * 		Identifier, dessen Index-Array geprüft wird
	 * @return <code>true</code>, falls Ebene relevant ist.
	 */
	private boolean isEbeneRelevant(long i, RtInternalIdentifier id) {
		// Sonderfall für alte Welt:  Zeile und laufendeNr im Iterationsintervall liegen.
		if (i == 0) {
			long lfdNr = id.getIndices()[0];
			return lInterval.inIntervall(lfdNr);
		}
		if (i == id.getIndices().length - 1) {
			long zeile = FeldUtil.getZeile(id);
			return zInterval.inIntervall(zeile);
		}

		// nicht relevant,  wenn Identifier die hier betrachtete Ebene gar nicht hat.
		return id.getIndices().length > i;
	}

	/**
	 * Konvertiert die {@link #pMenge} dieser Info in eine Menge von IIdentifiern, wenn
	 * {@link #additivMenge} die übergebene Menge von IIdentifiern enthält, wenn die
	 * Iterationsangabe in {@link #additivMenge} durch konkrete Werte ersetzt werden. <br>
	 * Für die Konvertierung werden in {@link #pMenge} die Iterationsangabe durch die gleichen
	 * konkreten Werte ersetzt.
	 */
	Set<RtInternalIdentifier> getPMengeWennMetaAdditivUmfasstMenge(Set<RtInternalIdentifier> menge, long usb) {
		Set<RtInternalIdentifier> result = new HashSet<>();
		EbenenIterator relevantIndices = getRelevanteEbenen(menge);
		for (IterationState idx : relevantIndices) {
			Set<RtInternalIdentifier> fsMenge = createIdMengeFromMetaFSMenge(additivMenge, idx.getIndexes(), usb);
			if (umfasst(fsMenge, menge)) {
				Set<RtInternalIdentifier> pflichtFeldMenge = createIdMengeFromMetaFSMenge(pMenge, idx.getIndexes(), usb);
				result.addAll(pflichtFeldMenge);
			}
		}
		return result;
	}

	/**
	 * Überprüft, ob eine Menge von {@link IIdentifier} eine andere umfasst unter
	 * Berücksichtigung
	 * der Sondersemantik von 0 als Abkürzung für "alle Ebenen".
	 *
	 * @param set1
	 * 		- Menge von Identifiern
	 * @param set2
	 * 		- Menge von Identifiern
	 * @return <code>true</code>, wenn die zweite Menge von Iidentifiern eine Teilmenge der ersten
	 * 		ist im obigen Sinne
	 */
	private boolean umfasst(Set<RtInternalIdentifier> set1, Set<RtInternalIdentifier> set2) {
		for (RtInternalIdentifier id2 : set2) {
			boolean found = false;
			for (RtInternalIdentifier id1 : set1) {
				if (id1.umfasstFuerPflichtangabenPruefung(id2, metaDataValidierung)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Liefert zur Menge metaFSMenge von MetaIdentifiern eine Menge von IIdentifiern.
	 * Zu jedem MetaIdentifier wird ein IIdentifier mit dem gleichen Namen erzeugt.<br>
	 * Wiederholungsebenen in einem {@link MetaIdentifier}, für die Iteration spezifiziert
	 * wurde, werden durch den entsprechenden Index aus den übergebenen Indices ersetzt.<br>
	 * Analog für den USB.
	 */
	private Set<RtInternalIdentifier> createIdMengeFromMetaFSMenge(
			Set<MetaIdentifier> metaFSMenge,
			long[] indices, long aktUsb) {
		Set<RtInternalIdentifier> result = new HashSet<>();
		for (MetaIdentifier mf : metaFSMenge) {
			result.add(mf.createIdentifier(indices, aktUsb));
		}
		return result;
	}

}
