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
import type {ICustomFieldValidator} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import {BundessteuernummerVerifierImpl} from "./internal/verifier/BundessteuernummerVerifierImpl.js";
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {SteuernummerVerifier} from "./SteuernummerVerifier.js";

/**
 * The {@link ICustomFieldValidator} implementation for the German Federal tax number - the so called 'Bundessteuernummer'.
 * The following description is in German because the information is specific to German tax and translating
 * the technical terms would cause more confusion than it would help non-German speakers.
 * <br>
 * <br>
 * Die Bundessteuernummer hat folgende unterschiedliche Darstellungen:
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
 * <b>Hinweis – Unterschied FA-Nummer / BUFA-Nummer:</b>
 * <br>
 * Die FA-Nummer ist 3-stellig. Im Unterschied zur 4-stelligen BUFA-Nummer fehlt die erste Stelle. Die ersten beiden Stellen der
 * BUFA-Nummer sind identisch mit der Landesnummer des Bundeslandes. Den Bundesländern Bayern und Nordrhein-Westfalen sind mehrere
 * Landesnummern zugeordnet, allen anderen Bundesländern nur eine. Die Landesnummern von Bayern beginnen mit der Ziffer 9,
 * die Landesnummern von Nordrhein-Westfalen mit der Ziffer 5.
 * <br>
 * Die Bundessteuernummer kann in dieser Darstellung nicht vollständig geprüft werden, denn dazu wird die 4-stellige BUFA-Nummer
 * benötigt. Damit die BUFA-Nummer ermittelt werden kann wird die im Folgenden beschriebene Oberflächen-Darstellung eingeführt.
 * </li>
 * <li> Die <b>Oberflächen-Darstellung</b> ist eine zwischen den Projekten ElsterRules und MeinElster vereinbarte, technisch
 * notwendige Darstellung. In dieser Darstellung werden der dem erklärungspflichtigen Bürger bekannten Darstellung, getrennt durch
 * das Zeichen „@“ die ersten beiden Ziffern der BUFA-Nummer (die Nummer die Oberfinanzdirektion) angehängt.
 * <br>
 * Durch diese Darstellung kann die Steuernummer von ElsterRules vollständig geprüft werden ohne dazu auf den Wert eines anderen
 * Feldes zugreifen zu müssen.
 * </li>
 * <li>Die Bundessteuernummer <b>im internen Format</b> beginnt anstatt mit der FA-Nummer (Finanzamtsnummer) mit der
 * 4-stelligen BUFA-Nummer und enthält keine Trennzeichen. An die nachverarbeitenden Systeme in ELSTER muss der Wert in diesem
 * Format übermittelt werden, deshalb wird es oft ELSTER Format genannt.
 * </li>
 * </ul>
 * <b>Prüfung der Bundessteuernummer:</b>
 * <ul>
 * <li>
 * Die Prüfung der <b>Bundessteuernummer</b> im Elster-Format (auch technischen oder internen Format) ist weitgehend identisch mit
 * dem Prüfungsalgorithmus für Steuernummern in Bayern (siehe auch {@link SteuernummerVerifier}).
 * <p>
 * Die Prüfung der BZSt-Steuernummer im Elster-Format besteht aus den folgenden Teilen:<br>
 * <ul>
 * <li>Ist die BZSt-Steuernummer formal gültig?</li>
 * <li>Liefert das Prüfziffernberechnungsverfahren die passende Prüfziffer?</li>
 * </ul>
 * </p>
 * <p>
 * Bei der formalen Prüfung muss die BZSt-Steuernummer dem folgenden regulären Ausdruck entsprechen:
 * <code>91160[0-9]{8}</code>
 * </p>
 * <p>
 * Abschließend wird die BZSt-Steuernummer noch anhand eines Prüfziffernberechnungsverfahrens
 * geprüft.
 * </p>
 * </li>
 * <li>
 * Zur Prüfung der <b>Bundessteuernummer in Oberflächenformat</b> wird diese ins ELSTER-Format konvertiert und erst darauf erfolgt
 * die tatsächliche Prüfung. Falls der Zusatz '@' gefolgt von Nummer der Oberfinanzdirektion fehlt, handelt es sich um einen
 * Integrationsfehler und es wird ein {@link Error} geworfen.
 * </li>
 * <li>Die <b>dem erklärungspflichtigen Bürger bekannte Darstellung</b> kann von dieser Klasse nicht geprüft werden. In diesem
 * Format fehlt das getrennt durch das Zeichen '@' angehängte Bundeslandkürzel. Falls dieses Format zur Prüfung übergeben wird,
 * handelt es sich um einen Integrationsfehler im Projekt und es wird ein {@link Error} geworfen.</li>
 * </ul>
 */
export class BundessteuernummerVerifier {

	private constructor() {
		// at the moment, we don't need to do something
	}

    /**
     * @return the instance of {@link ICustomFieldValidator}.
     */
	public static create(): ICustomFieldValidator {
		return new BundessteuernummerVerifierImpl();
	}
}
