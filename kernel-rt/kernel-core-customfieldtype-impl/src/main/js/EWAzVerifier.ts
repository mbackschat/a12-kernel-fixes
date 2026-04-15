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

import type { ICustomFieldValidator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import { EWAzVerifierImpl } from "./internal/verifier/EWAzVerifierImpl.js";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
import type { IFadat, deserializeFadat } from "./IFadat.js";

/**
 * The {@link ICustomFieldValidator} implementation for the so called 'Einheitswert-Aktenzeichen'.
 * The following description is in German because the information is specific to German tax and translating
 * the technical terms would cause more confusion than it would help non-German speakers.
 * <br>
 * <br>
 * Für das EW-Az (kurz für 'Einheitswert-Aktenzeichen') gibt es folgende unterschiedliche Darstellungen:
 * <ul>
 * <li>die dem erklärungspflichtigen Bürger bekannte Darstellung</li>
 * <li>die Oberflächen-Darstellung</li>
 * <li>die XML-Darstellung oder auch Elster-Format bzw. internes Format genannt</li>
 * </ul>
 * Diese drei Arten der Darstellung werden im Folgenden beschrieben.
 * <ul>
 * <li>Die <b>dem erklärungspflichtigen Bürger bekannte Darstellung</b> enthält bundeslandspezifische Trennzeichen und beginnt mit der
 * FA-Nummer (Finanzamtsnummer) und nicht mit der eindeutigen 4-stelligen BUFA-Nummer (Bundesfinanzamtsnummer), denn die 4-stellige
 * BUFA-Nummer ist den erklärungspflichtigen Bürgern nicht bekannt.
 * <br>
 * Beispiel für ein EW-Az in dieser Darstellung: 208/035-3-03889.3.
 * <br>
 * <b>Hinweis – Unterschied FA-Nummer / BUFA-Nummer:</b>
 * <br>
 * Die FA-Nummer ist 3-stellig. Im Unterschied zur 4-stelligen BUFA-Nummer fehlt die erste Stelle. Die ersten beiden Stellen der
 * BUFA-Nummer sind identisch mit der Landesnummer des Bundeslandes. Den Bundesländern Bayern und Nordrhein-Westfalen sind mehrere
 * Landesnummern zugeordnet, allen anderen Bundesländern nur eine. Die Landesnummern von Bayern beginnen mit der Ziffer 9,
 * die Landesnummern von Nordrhein-Westfalen mit der Ziffer 5.
 * <br>
 * Das EW-Az kann in dieser Darstellung nicht vollständig geprüft werden, denn dazu wird die 4-stellige BUFA-Nummer benötigt.
 * Damit die BUFA-Nummer ermittelt werden kann wird die im Folgenden beschriebene Oberflächen-Darstellung eingeführt.
 * </li>
 * <li> Die <b>Oberflächen-Darstellung</b> ist eine zwischen den Projekten ElsterRules und MeinElster vereinbarte, technisch
 * notwendige Darstellung. In dieser Darstellung wird der dem erklärungspflichtigen Bürger bekannten Darstellung, getrennt durch
 * das Zeichen „@“, das Länderkürzel des Bundeslandes gemäß ElsterBasis-XML-Schema angehängt.
 * <br>
 * Beispiel für ein EW-Az in Oberflächen-Darstellung: 208/035-3-03889.3@NW
 * <br>
 * Durch diese Darstellung kann das EW-Az von ElsterRules vollständig geprüft werden ohne dazu auf den Wert eines anderen Feldes
 * (das Bundesland) zugreifen zu müssen.
 * <br>
 * <b>Hinweis – Parameter Bundesland ist ausreichend:</b>
 * <br>
 * Die Kenntnis des Bundeslandes genügt zur Bestimmung der BUFA-Nummer, obwohl in der dem erklärungspflichtigen Bürger bekannten
 * Darstellung ggf. die erste Stelle der Finanzamtsnummer unterdrückt wird, denn das EW-Az in der dem erklärungspflichtigen Bürger
 * bekannten Darstellung enthält für die Bundesländer Bayern und Nordrhein-Westfalen stets alle drei Stellen der Finanzamtsnummer.
 * Für alle anderen Bundesländer gibt es jeweils nur eine Landesnummer; somit ist für diese Bundesländer auch die Angabe der
 * letzten beiden Stellen der FA-Nummer ausreichend um zusammen mit dem Bundesland die eindeutige BUFA-Nummer zu bestimmen.
 * </li>
 * <li>Analog zur Steuernummer beginnt das EW-Az <b>im internen Format</b> anstatt mit der FA-Nummer (Finanzamtsnummer) mit der
 * 4-stelligen BUFA-Nummer. Diesen Ziffern schließt sich der sog. Formatschlüssel an. Der Formatschlüssel hat für alle Länder den
 * Wert 4. Die restlichen Ziffern des EW-Az werden ohne Trennzeichen der BUFA-Nummer angehängt. Das EW-Az “208/035-3-03889.3“ in
 * Nordrhein-Westfalen hat im internen Format somit beispielsweise den Wert “520840353038893“. Die ersten 3 Ziffern des Wertes
 * – also 208 – spezifizieren die Finanzamtsnummer, die eindeutige 4-stellige BUFA-Nummer dazu lautet 5208.
 * An die nachverarbeitenden Systeme in ELSTER muss der Wert in diesem Format übermittelt werden, deshalb wird es oft ELSTER Format
 * genannt.
 * </li>
 * </ul>
 * <b>Prüfung des EW-Az:</b>
 * <ul>
 * <li>
 * Die Prüfung des <b>EW-Az im Elster-Format</b> (dem technischen oder internen Format) besteht
 * aus den folgenden Teilen:
 * <ul>
 * <li>Ist das EW-Az formal gültig?</li>
 * <li>Existiert die in dem EW-Az enthaltene BUFA-Nummer?</li>
 * <li>Liefert das Prüfziffernberechnungsverfahren die passende Prüfziffer?</li>
 * </ul>
 *
 * <p>
 * Bei der formalen Prüfung muss das EW-Az dem folgenden regulären Ausdruck
 * entsprechen:
 * <code>[0-9]{4}[045][0-9]{8,14}</code>
 * </p>
 *
 * <p>
 * Die ersten vier Zeichen des Einheitswert-Aktenzeichens bilden die BUFA-Nummer. Es wird
 * geprüft, ob diese BUFA-Nr. laut der verbindlichen Liste im technischen Anforderungskonzept (V35,
 * Kapitel 1.2.2) zulässig ist.
 * </p>
 * <p>
 * An 5. Stelle steht der Formatschlüssel. Er hat beim Ordnungsbegriff "Einheitswertaktenzeichen"
 * der Grundsteuer in verschiedenen Ländern den Wert 0, 4, oder 5.
 *
 * <p>
 * Auch besondere Formatregeln für Schleswig-Holstein und Nordrhein-Westfalen werden geprüft.
 * </p>
 *
 * <p>
 * Abschließend wird das Einheitswert-Aktenzeichen noch anhand eines
 * Prüfziffernberechnungsverfahrens geprüft.
 * </p>
 * </li>
 * <li>
 * Zur Prüfung des <b>EW-Az im Oberflächenformat</b> wird diese ins ELSTER-Format konvertiert und erst darauf
 * erfolgt die tatsächliche Prüfung. Falls der Zusatz '@' gefolgt vom  Bundeslandkürzel fehlt, handelt es sich um einen
 * Integrationsfehler und es wird ein {@link Error} geworfen.
 * </li>
 * <li>Die <b>dem erklärungspflichtigen Bürger bekannte Darstellung</b> kann von dieser Klasse nicht geprüft werden. In diesem
 * Format fehlt das getrennt durch das Zeichen '@' angehängte Bundeslandkürzel. Falls dieses Format zur Prüfung übergeben wird,
 * handelt es sich um einen Integrationsfehler im Projekt und es wird ein {@link Error} geworfen.</li>
 * </ul>
 */

export class EWAzVerifier {

	private constructor() {
		// at the moment, we don't need to do something
	}

	/**
	 * @deprecated since version 30.6.0. Will be removed with A12K-3589. Use {@link create} instead.
	 */
	public static getInstance(): ICustomFieldValidator {
		return new EWAzVerifierImpl(undefined);
	}

	/**
	 * @param fadat
	 * 		the federal tax office data. Can be created via {@link deserializeFadat}.
	 * @return the instance of {@link ICustomFieldValidator}.
	 */
	public static create(fadat: IFadat): ICustomFieldValidator {
		return new EWAzVerifierImpl(fadat);
	}
}
