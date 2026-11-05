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

/** This class defines the internal keys of the Meta API. These are the keys that are present in generated code for ExtMP and A12
 * and are not made public via IMetaKeys.<br>
 *
 * Keys, that are created via IMetaInfoCodeGenParams outside the VK code generator, keys that are published via
 * IMetaKeys should <b>not</b> be added here.
 * <p>
 * The keys defined in this interface can be used freely in the runtime.
 * <p>
 * ATTENTION: Only for internal use.
 */
export class IMetaKeysInternal {
	/** Dieser Schlüssel dient zur internen Verwendung im Rahmen der Bestimmung von Pflichteigenschaften.
	 * Eine Verwendung des Schlüssels durch einen externen Aufrufer wird nicht empfohlen, da eine Menge
	 * von internen Strukturen für die Pflichteigenschaften zurückgegeben wird. */
	public static get MODEL_ADDITIV_PFLICHTFELD(): string {
		return "Additiv_Pflichtfeld";
	}
	public static get MODEL_METAFELDUNIQUE(): string {
		return "MetaFeldEindeutig";
	}
	/**  Liefert alle globalen Felder als Set&lt;{@link com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier}&gt;
	 * zurück. Die Indizes des IIdentifiers werden als 0 gesetzt. */
	public static get MODEL_GLOBAL_FIELDS(): string {
		return "GlobalFields";
	}
	/**
	 * This key is for internal usage to determine the base year in A12.
	 * If the model has a base year, returns the base year as a String. Otherwise, returns {@code null}.
	 */
	public static get MODEL_BASE_YEAR(): string {
		return "BaseYear";
	}
	/** This key is for internal usage to determine the time zone, date strings are interpreted in. The time zone is returned as
	 * string. Currently, only <code>UTC</code> and <code>Europe/Berlin</code> are supported. */
	public static get MODEL_TIME_ZONE(): string {
		return "TimeZone";
	}
	/** This key is used internally to create model specific localizable information. */
	public static get MODEL_ID(): string {
		return "ModelId";
	}

	// Schluessel von MetaContainer
	/** Der Schlüssel liefert für eine IMetaGroup eine <code>List&lt;Long&gt;</code>. Die Liste enthält für jede Ebene des
	 * Containers den maximalen Index dieser Ebene.*/
	public static get CONTAINER_MAXINDICES(): string {
		return "MaxIndices";
	}

	// Schluessel des MetaFeldes
	/** Gibt die intern verwendete Nummer des Feldes als Integer zurück. Felder werden zur internen Bearbeitung durchnummeriert. */
	public static get FELD_INTERNENR(): string {
		return "InterneNr";
	}

	/** Gibt den internen Namen des Felds als String zurück. Der Name ist ein eindeutiger, kurzer Name für das Feld. Der
	 * Algorithmus zum Bestimmen des eindeutigen Namens kann variieren und wird an der Schnittstelle nicht zugesichert. Intern
	 * wird der UniqueName verwendet, da Pfadnamen zu lang sind. */
	public static get FELD_UNIQUENAME(): string {
		return "UniqueName";
	}

	/**
	 * Der Schlüssel "MaxIndices" dient ausschließlich zur internen Verwendung.
	 * Der Schlüssel liefert für ein IMetaField Array<number>.
	 * Die Liste enthält für jede Wiederholungsebene des Feldes den maximalen Index dieser Ebene.
	 * Kontexte ohne Wiederholbarkeit sind in der Liste nicht enthalten.
	 */
	public static get FELD_MAXINDICES(): string {
		return "MaxIndices";
	}
	public static get FELD_UI_VALUES_FOR_VALUE(): string {
		return "EnumUIValuesForValue";
	}

	// Schluessel der MetaRegel
	/**  Es wird nicht der in ExtMP gespeicherte Fehlertext zurückgeliefert, sondern der Fehlertext, der bereits
	 * Ersetzungen enthält, die zum Zeitpunkt der Codegenerierung vorgenommen werden können. Wird als zweiter Parameter ein
	 * Locale übergeben, so wird dieses als Sprache interpretiert. Falls ein Locale für den zweiten Parameter übergeben wird, das
	 * nicht in der Liste der unterstützten Sprachen enthalten ist (von
	 * {@link com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel} für den Schlüssel Sprachen zurückgegebene Liste), wird
	 * eine IllegalArgumentException geworfen. Wird kein zweiter Parameter übergeben, so wird der deutsche Fehlertext
	 * zurückgegeben.*/
	public static get REGEL_FEHLERTEXT(): string {
		return "Fehlertext";
	}
	public static get REGEL_FEHLERTEXTE(): string {
		return "Fehlertexte";
	}
	public static get REGEL_PARAMETER_MAPPING(): string {
		return "ParameterMapping";
	}
	/**  Gibt die in der Regel referenzierten FeldIdentifier als Set&lt;IIdentifier&gt; zurück. Die Indices werden dabei mit
	 * Platzhaltern zurückgegeben. */
	public static get REGEL_REFFELDIDS_INTERN(): string {
		return "ReferenzierteFeldIdentifierIntern";
	}
	/** Im Gegensatz zu <code>ReferenzierteFeldIdentifierIntern</code> werden bei der Verwendung von
	 * <code>RefFeldIdentifierAuslassungsFehlerIntern</code> nur
	 * {@link IIdentifier} zurückgegeben, die Auslassungsfehler erzeugen
	 * können. D.h. die zurückgegebene Menge ist eine Teilmenge von der Ergebnismenge, die bei
	 * <code>ReferenzierteFeldIdentifierIntern</code> zurückgegeben wird. Die beiden Mengen können auch gleich sein. Falls keine
	 * in der Regel referenzierten Felder Auslassungsfehler erzeugen können, wird eine leere Menge zurückgegeben. Die Ermittlung,
	 * ob ein Feld einen Auslassungsfehler erzeugen kann, ist eine einfache Heuristik. In Sonderfällen kann daher die Menge der
	 * zurückgegebenen Felder zu groß sein, es ist jedoch sicher gestellt, dass alle zurückgegebenen Felder auch in der
	 * Regelbedingung referenziert werden. */
	public static get REGEL_REFFELDIDS_AUSLASSUNG_INTERN(): string {
		return "RefFeldIdentifierAuslassungsFehlerIntern";
	}
	/** Returns the referenced entities of the rule (may contain groups) as a
	 * `HashSet<RtIdentifierTemplate>`, used for the V2 possibly-causing-entities API. */
	public static get REGEL_REFENTITYIDS_INTERN(): string {
		return "ReferenzierteEntityIdentifierIntern";
	}
	/** Returns the referenced entities of the rule that could cause omission errors (may contain
	 * groups) as a `HashSet<RtIdentifierTemplate>`, used for the V2 possibly-causing-entities API. */
	public static get REGEL_REFENTITYIDS_AUSLASSUNG_INTERN(): string {
		return "RefEntityIdentifierAuslassungsFehlerIntern";
	}

}
