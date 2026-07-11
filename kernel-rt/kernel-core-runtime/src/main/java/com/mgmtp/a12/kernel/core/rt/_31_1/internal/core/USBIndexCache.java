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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.core;

import java.util.HashMap;
import java.util.Map;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.EntityUtil;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * Diese Klasse wird nur für H-DVs mit USB (Untersachbereich) verwendet.
 * <p>
 * Hier werden die Maps verwaltet, die für die Konvertierung zwischen "synthetischen" (d.h. von der Anwendung übergebenen) USBs und
 * internen USBs benötigt werden. Verwendet die Anwendung keine USB-Index Felder, so gibt es eine 1-1 Korrespondenz zwischen
 * synthetischen und internen USBs. Werden dagegen USB-Index Felder verwendet, so gibt es diese 1-1 Entsprechung nur pro Vordruck.
 * <p>
 * Bezeichnungen:
 * <ul>
 * <li>Synthetischer USB: USB, der von der Anwendung übergeben wurde.</li>
 * <li>Interner USB: USB, der im Editor vergeben wurde und intern verwendet wird.</li>
 * <li>XML-USB: der USB, der dem USB im Nutzdatensatz-XML entspricht. Verwendet die Anwendung keine USB-Index-Felder, so gilt
 * XML-USB = synthetischer USB. Verwendet die Anwendung USB-Index-Felder, so entspricht der Wert eines USB-Index-Feldes dem XML-USB.</li>
 * </ul>
 */
public class USBIndexCache {

	/**
	 * Der Separator zwischen Vordruck und laufender Nummer für die Keys in
	 * {@link #internZuSynthMap} und {@link #synthZuInternMap}.
	 */
	private static final String KEY_SEPARATOR = "$";

	/**
	 * Speichert pro Vordruck die Zuordnung:
	 * Interner USB --> Synthetischer USB.
	 * Interner USB wird als Key verwendet.
	 */
	private final Map<String, Map<Long, Long>> internZuSynthMap = new HashMap<>();
	/**
	 * Speichert pro Vordruck die Zuordnung:
	 * Synthetischer USB --> Interner USB
	 * Synthetischer USB wird als Key verwendet.
	 */
	private final Map<String, Map<Long, Long>> synthZuInternMap = new HashMap<>();

	/**
	 * Speichert zum internen USB den XML-USB.
	 * Verwendet die Anwendung keine USB-Index-Felder, so gilt XML-USB = synthetischer USB.
	 * Interner USB wird als Key verwendet.
	 */

	private final Map<Long, Long> internZuXML = new HashMap<>();
	/**
	 * Speichert zu einem XML-USB den internen USB.
	 * Verwendet die Anwendung keine USB-Index-Felder, so gilt XML-USB = synthetischer USB.
	 * XML USB wird als Key verwendet.
	 */
	private final Map<Long, Long> xmlZuIntern = new HashMap<>();

	private int maxInternerUSB = 0;

	private boolean usbIndexFehler = false;
	private final IMetaDataValidierung metaData;

	public USBIndexCache(IMetaDataValidierung metaData) {
		this.metaData = metaData;
	}

	private void updateMap(Map<String, Map<Long, Long>> map, String v, Long key, Long value) {
		map.computeIfAbsent(v, k -> new HashMap<>()).put(key, value);
	}

	public long getExternUSB(RtInternalIdentifier id) {
		assert metaData.hasUniqueEntities();
		String externName = metaData.getPfadName(id.getName());
		if (externName == null) {
			externName = id.getName();
		}
		String vordruck = EntityUtil.getRootGroupName(externName);
		//Konvertiere USB
		String key = metaData.hasUSBIndexField() ? vordruck + KEY_SEPARATOR + id.getIndices()[0] : vordruck;
		return convertInternToSynthUSB(key, id.getUsb());
	}

	/**
	 * Ordnet einem XML-USB einen internen USB zu und befüllt den Cache entsprechend.
	 */
	private void addXMLUSB(String vordruck, Long synthUSB, Long xmlUSB) {
		//Falls der interne XML USB noch nicht existiert, wird er in den
		//vordruck unabhängigen Strukturen gespeichert.
		Long internerUSB = xmlZuIntern.computeIfAbsent(xmlUSB, l -> {
			Long neuerInternerUSB = Long.valueOf(++maxInternerUSB); // Erzeuge einen neuen internen USB
			internZuXML.put(neuerInternerUSB, xmlUSB);
			return neuerInternerUSB;
		});
		//Speicherung in den Vordruck spezifischen Strukturen ist auch notwendig,
		//wenn es den internen USB schon gibt. Der xml USB kann bereits bei einem
		//anderen Vordruck verwendet worden sein.
		updateMap(internZuSynthMap, vordruck, internerUSB, synthUSB);
		updateMap(synthZuInternMap, vordruck, synthUSB, internerUSB);
	}

	/**
	 * Liefert den internen USB zu vordruck und synthUSB. Ist bereits ein solcher im Cache, wird
	 * dieser zurückgegeben. Andernfalls wird ein neuer interner USB vergeben und dem
	 * Cache hinzugefügt.
	 * <p>
	 * Kann null zurückgeben, wenn doNotUpdate (sonst nicht??)
	 */
	private Long ermittleInternenUSBUndUpdateUSBCache(String vordruck, Long synthUSB) {
		Long internerUSB;
		if (!metaData.hasUSBIndexField()) {
			internerUSB = xmlZuIntern.get(synthUSB);
		} else {
			internerUSB = getInternerUSBZuVordruckUndSynthUSB(vordruck, synthUSB);
		}
		if (internerUSB == null) {
			if (metaData.hasUSBIndexField()) {//für den vordruck+synth.USB wurde kein USB Index Feld angegeben
				usbIndexFehler = true;
			}
			internerUSB = Long.valueOf(++maxInternerUSB);
			if (!metaData.hasUSBIndexField()) {
				internZuXML.put(internerUSB, synthUSB);
				xmlZuIntern.put(synthUSB, internerUSB);
			}
		}
		if (getInternerUSBZuVordruckUndSynthUSB(vordruck, synthUSB) == null) {
			updateMap(internZuSynthMap, vordruck, internerUSB, synthUSB);
			updateMap(synthZuInternMap, vordruck, synthUSB, internerUSB);
		}
		return internerUSB;
	}

	private Long getInternerUSBZuVordruckUndSynthUSB(String vordruck, Long synthUSB) {
		return synthZuInternMap.get(vordruck) == null ? null : synthZuInternMap.get(vordruck).get(synthUSB);
	}

	/**
	 * Erste Stufe der USB-Cache-Befüllung.
	 * <b>Vorbedingung</b>: id ist USB-Index-Feld.
	 * Es wird im USB-Cache zum USB von id und zum Vordruck der Wert xmlWert gespeichert und ein interner USB vergeben.
	 * <li><b>Fall 2:</b> es werden keine USB-Index-Felder verwendet, so wird dem USB von id ein
	 * interner USB zugeordnet, falls noch nicht passiert.</li>
	 * </ul>
	 */
	public void updateMitXMLUSB(RtInternalIdentifier id, String xmlWert) {
		assert metaData.hasUniqueEntities();
		Long synthUSB = id.getUsb();
		if (metaData.hasUSBIndexField()) {//Fall 1
			if (metaData.getUSBIndexFelder().contains(id.getName())) {
				String vordruck = EntityUtil.getRootGroupName(
						metaData.getPfadName(id.getName())) + KEY_SEPARATOR + id.getIndices()[0];
				Long xmlUSB = Long.valueOf(xmlWert);
				if (xmlUSBFuerVordruckBereitsVerwendet(vordruck, xmlUSB)) {
					//der XML-USB ist für den Vordruck bereits im Cache -> Fehler
					usbIndexFehler = true;
				} else {
					addXMLUSB(vordruck, synthUSB, xmlUSB);
				}
			}
		} else {//Fall2
			//Falls keine USB IndexFelder verwendet werden,  ist der synthetische USB immer gleich dem
			//XML USB. Die internen Strukturen werden analog aufgebaut,  damit keine Spezialbehandlung
			//notwendig ist.
			String vordruck = EntityUtil.getRootGroupName(metaData.getPfadName(id.getName()));
			addXMLUSB(vordruck, synthUSB, synthUSB);
		}
	}

	/**
	 * Liefert die Struktur XML-USB {@literal -->} interner USB
	 */
	public Map<Long, Long> getXMLZuIntern() {
		assert metaData.hasUniqueEntities();
		return xmlZuIntern;
	}

	/**
	 * Liefert die Struktur interner USB {@literal -->} XML-USB
	 */
	public Map<Long, Long> getInternZuXML() {
		assert metaData.hasUniqueEntities();
		return internZuXML;
	}

	/**
	 * Liefert für den übergebenen internen Usb
	 * <ul>
	 * <li>Falls es ein Usb-Index-Feld gibt: dessen Wert, oder '0' falls das Feld nicht angegeben
	 * wurde</li>
	 * <li>Falls es kein Usb-Index-Feld gibt: den synthetischen USB</li>
	 * </ul>
	 */
	public String convertInternToXmlUSB(long internerUSB) {
		assert metaData.hasUniqueEntities();
		Long result = internZuXML.get(internerUSB);
		return result != null ? result.toString() : "0";
	}

	/**
	 * Liefert zu einem Vordruck und einem internen USB den synthetischen USB bzw. 1, wenn es keinen
	 * gibt.
	 * Die Werte 0 und {@link IIdentifier#USB_NOT_SPECIFIED} werden nicht konvertiert.
	 */
	private long convertInternToSynthUSB(String vordruck, long internerUSB) {
		if (internerUSB == 0 || internerUSB == IIdentifier.USB_NOT_SPECIFIED) {
			return internerUSB;
		}
		Long result = null;
		if (metaData.hasUSBIndexField()) {
			Map<Long, Long> map = internZuSynthMap.get(vordruck);
			if (map != null) {
				result = map.get(internerUSB);
			}
		} else {
			/*
			 * wenn keine USB-Index Felder verwendet werden, ist die Zuordnung
			 * interner USB -> synth. USB unabhängig vom Vordruck
			 */
			result = internZuXML.get(internerUSB);
		}
		return (result == null) ? 1 : result;
	}

	/**
	 * Gibt true zurück, wenn 'xmlUSB' für 'vordruck' bereits im Cache ist, d.h.
	 * doppelte Angabe der gleichen Beteiligtennummer.
	 */
	private boolean xmlUSBFuerVordruckBereitsVerwendet(String vordruck, long xmlUSB) {
		Long intUSB = xmlZuIntern.get(xmlUSB);
		if (intUSB == null) {
			return false;
		}

		Map<Long, Long> intZuSynth = internZuSynthMap.get(vordruck);
		if (intZuSynth == null) {
			return false;
		}
		return intZuSynth.containsKey(intUSB);
	}

	/**
	 * Ermittelt zu einem Identifer mit dem externen USB den internen USB und fügt
	 * ihn ggf. dem Cache hinzu.
	 * Der USB 0 und {@link IIdentifier#USB_NOT_SPECIFIED} werden nicht konvertiert.
	 */
	public long getInternUSBAndUpdateUSBCache(RtInternalIdentifier id) {
		assert metaData.hasUniqueEntities();
		long usb = id.getUsb();
		if (usb == 0 || usb == IIdentifier.USB_NOT_SPECIFIED) {
			//NICHT_ANGEGEBEN und 0 müssen nicht übersetzt werden.
			return usb;
		}
		//Falls das Feld auf einem USB Vordruck liegt muss umgeschlüsselt werden.
		if (metaData.getMaxUsb(id.getName()) != IIdentifier.USB_NOT_SPECIFIED) {
			long lfdNr = id.getIndices()[0];
			// offenbar wird lfdNr = 0 wie lfdNr = 1 behandelt (wie funktioniert damit das mapping zu den internen USBs?)
			long l = (lfdNr == 0) ? 1 : lfdNr;
			String vordruck = (metaData.hasUSBIndexField()) ?
					EntityUtil.getRootGroupName(metaData.getPfadName(id.getName())) + KEY_SEPARATOR + l :
					EntityUtil.getRootGroupName(metaData.getPfadName(id.getName()));
			return ermittleInternenUSBUndUpdateUSBCache(vordruck, usb);
		}
		return usb;
	}

	/**
	 * Gibt <b><code>true</code></b> zurück, wenn
	 * <ul>
	 * <li>für zwei verschiedene Instanzen des gleichen Vordrucks der gleiche Wert für das
	 * USB-Index-Feld angegeben wurde</li>
	 * <li>für eine Vordruckinstanz, für die Werte angegeben wurden, für das USB-Index-Feld kein
	 * Wert angegeben wurde</li>
	 * </ul>
	 */
	public boolean isUSBIndexFeldFehler() {
		assert metaData.hasUniqueEntities();
		return usbIndexFehler;
	}
}
