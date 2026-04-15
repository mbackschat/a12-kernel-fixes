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

import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * This interface defines the internal keys of the Meta API. These are the keys that are present in generated code for A12 and ExtMP
 * and are not made public via IMetaKeys.
 * <p>
 * Keys, that are created via IMetaInfoCodeGenParams outside the VK code generator, keys that are published via
 * {@link IMetaKeys} or keys that are ExtMP-specific should <b>not</b> be added here.
 * <p>
 * The keys defined in this interface can be used freely in the runtime.
 * <p>
 * Currently, there are no keys that are generated only for A12. If such keys are added in the future, maybe a new interface
 * should be created to contain these keys.
 * <p>
 * ATTENTION: Only for internal use.
 */
public interface IMetaKeysInternal {

	/**
	 * Dieser Schlüssel dient zur internen Verwendung im Rahmen der Bestimmung von Pflichteigenschaften.
	 * Eine Verwendung des Schlüssels durch einen externen Aufrufer wird nicht empfohlen, da eine Menge
	 * von internen Strukturen für die Pflichteigenschaften zurückgegeben wird.
	 */
	String MODEL_ADDITIV_PFLICHTFELD = "Additiv_Pflichtfeld";
	String MODEL_METAFELDUNIQUE = "MetaFeldEindeutig";
	String MODEL_VALIDATOR = "validator";
	String MODEL_DYN_ENUM_CALC = "DynEnumCalc";
	/**
	 * Liefert eine Map mit Sortierinformationen, die aus den IndexFeldern erzeugt wurde falls IndexFelder vorhanden sind,
	 * andernfalls <code>null</code>.
	 */
	String MODEL_SORTIER_INFO_4_MAPPER = "SortInfo4Mapper";

	/** Liefert eine Map mit Sortierinformationen, falls welche vorhanden sind, andernfalls <code>null</code>. */
	String MODEL_SORT_INFO = "SortInfo";
	/**
	 * Liefert alle globalen Felder als Set&lt;{@link IIdentifier}&gt; zurück. Die Indizes des {@link IIdentifier}s werden als
	 * 0 gesetzt.
	 */
	String MODEL_GLOBAL_FIELDS = "GlobalFields";
	/**
	 * This key is for internal usage to determine the base year in A12.
	 * If the model has a base year, returns the base year as a String. Otherwise, returns {@code null}.
	 */
	String MODEL_BASE_YEAR = "BaseYear"; // TODO: how does this differ from the public meta key?
	/**
	 * This key is for internal usage to determine the time zone, date strings are interpreted in. The time zone is returned as
	 * string. Currently, only <code>UTC</code> and <code>Europe/Berlin</code> are supported.
	 */
	String MODEL_TIME_ZONE = "TimeZone";

	/**
	 * @return the id of the document model.
	 */
	String MODEL_ID = "ModelId";

	/**
	 * This key is for internal usage to get access to the IMetaDataValidierung interface of a model.
	 */
	String MODEL_METADATAVALIDIERUNG = "MetaDataValidierung";

	/**
	 * This key converts the provided uXML data into eXML data.
	 * <p>
	 * The following parameters are expected:<ol>
	 * <li>A parameter of type {@link IData} is required and must contain the uXML data.</li>
	 * <li>Optional: A parameter of type <code>Map&lt;{@link IIdentifier}, String&gt;</code></li>
	 * <li>Optional: Can only be provided if the other optional parameter one is also specified. Must be of type
	 * <code>Map&lt;String, String&gt;</code> and contain the header data (field name without path specification as <i>key</i>
	 * and the corresponding value as <i>value</i>).</li>
	 * </ol>
	 * <p>
	 * An {@link IData} instance with the converted eXML data is returned. If no uXML mapping information is found in the generated
	 * code for a particular field, or the indexes do not allow for a bidirectional mapping, the field will not be mapped back.
	 * In addition, this field is added to the map provided in the first optional parameter, if available, along with its associated
	 * value.
	 * <p>
	 * In addition, the header data for the eXML data is supplemented as follows:
	 * If the second optional parameter is set, the header data is taken from this map.
	 * Otherwise, the header data is extracted from the uXML data using the field name (if it includes a path, it is ignored), if it
	 * exists there.
	 * <p>
	 * If the uXML DVL contains USB index fields, the value of the USB index field is taken over for the corresponding index field
	 * of the context mapped to the USB in the eXML-DV.
	 * Only USB index fields that are hard-coded are taken into account (cf. EXT-17674).
	 * <p>
	 * This key is currently used for the data transfer from the previous year if the target DVL has already been converted to eXML
	 * and the source DVL has not yet been converted.
	 */
	String MODEL_CONVERTED_DATA_TO_EXML = "ConvertedDataToEXML";

	//Schluessel von MetaContainer
	/**
	 * Der Schlüssel liefert für eine IMetaGroup eine <code>List&lt;Long&gt;</code>. Die Liste enthält für jede Ebene des
	 * Containers den maximalen Index dieser Ebene.
	 */
	String CONTAINER_MAXINDICES = "MaxIndices";

	// Schluessel des MetaFeldes
	/** Gibt die intern verwendete Nummer des Feldes als Integer zurück. Felder werden zur internen Bearbeitung durchnummeriert. */
	String FELD_INTERNENR = "InterneNr";
	/**
	 * Falls das Feld ein Datumsfeld oder ein Datumbereich ist, wird <code>true</code> zurückgegeben, wenn für das Feld eine
	 * Zusatzprüfung durchgeführt werden soll. In der Zusatzprüfung wird geprüft, dass die Datumsangaben nicht vor 1900 liegen.
	 */
	String FELD_ZUSATZPRUEFUNG = "Zusatzpruefung";
	/**
	 * Gibt den internen Namen des Felds als String zurück. Der Name ist ein eindeutiger, kurzer Name für das Feld. Der
	 * Algorithmus zum Bestimmen des eindeutigen Namens kann variieren und wird an der Schnittstelle nicht zugesichert. Intern
	 * wird der UniqueName verwendet, da Pfadnamen zu lang sind.
	 */
	String FELD_UNIQUENAME = "UniqueName";
	/**
	 * Liefert die Liste von Nummern der Wiederholungsebenen bei denen der Sachbereich und die Kennzahl der ersten lfd. Nummer
	 * Vordruck für alle anderen lfd. Nummern Vordruck verwendet werden sollen.
	 */
	String FELD_SB_KZ_REPETITION_1 = "SbKzRepetition1";
	/**
	 * Liefert eine Map&lt;List&lt;Long&gt;, Pair&lt;String, String&gt;&gt;. Der Schlüssel besteht aus der Liste der Indizes
	 * für den {@link IIdentifier} des Feldes. Der Wert besteht aus dem entsprechenden Sachbereich und der entsprechenden
	 * Kennzahl.
	 */
	String FELD_SB_KZ = "SbKz";

	/**
	 * Der Schlüssel "MaxIndices" dient ausschließlich zur internen Verwendung.
	 * Der Schlüssel liefert für ein IMetaField List<Long>.
	 * In A12-DVs enthält die Liste für jede Wiederholungsebene des Feldes den maximalen Index dieser Ebene.
	 * Bei H-DVs enthält die Liste genau zwei Elemente:
	 * die Wiederholbarkeiten des Vordrucks und der Zeile.
	 */
	String FELD_MAXINDICES = "MaxIndices";

	/**
	 * For date type fields, specifies how many parts of the date are known. Possible values are:
	 * <ul>
	 * <li>Bekannt</li>
	 * <li>TagOptional</li>
	 * <li>MonatOptional</li>
	 * <li>JahrOptional</li>
	 * </ul>
	 * If the field is not a date, null is returned.
	 */
	String FIELD_TEILBEKANNTART = "TeilBekanntArt";

	// Schluessel der MetaRegel
	/**
	 * Es wird nicht der in ExtMP gespeicherte Fehlertext zurückgeliefert, sondern der Fehlertext, der bereits
	 * Ersetzungen enthält, die zum Zeitpunkt der Codegenerierung vorgenommen werden können. Wird als zweiter Parameter ein
	 * Locale übergeben, so wird dieses als Sprache interpretiert. Falls ein Locale für den zweiten Parameter übergeben wird, das
	 * nicht in der Liste der unterstützten Sprachen enthalten ist (von
	 * {@link com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel} für den Schlüssel Sprachen zurückgegebene Liste), wird
	 * eine IllegalArgumentException geworfen. Wird kein zweiter Parameter übergeben, so wird der deutsche Fehlertext
	 * zurückgegeben.
	 */
	String REGEL_FEHLERTEXT = "Fehlertext";

	/**
	 * Gibt die in der Regel referenzierten FeldIdentifier als
	 * Set&lt;{@link IIdentifier}&gt; zurück. Die Indices werden dabei mit Platzhaltern zurückgegeben.
	 */
	String REGEL_REFFELDIDS_INTERN = "ReferenzierteFeldIdentifierIntern";
	/**
	 * Im Gegensatz zu <code>ReferenzierteFeldIdentifierIntern</code> werden bei der Verwendung von
	 * <code>RefFeldIdentifierAuslassungsFehlerIntern</code> nur {@link IIdentifier} zurückgegeben, die Auslassungsfehler erzeugen
	 * können. D.h. die zurückgegebene Menge ist eine Teilmenge von der Ergebnismenge, die bei
	 * <code>ReferenzierteFeldIdentifierIntern</code> zurückgegeben wird. Die beiden Mengen können auch gleich sein. Falls keine
	 * in der Regel referenzierten Felder Auslassungsfehler erzeugen können, wird eine leere Menge zurückgegeben. Die Ermittlung,
	 * ob ein Feld einen Auslassungsfehler erzeugen kann, ist eine einfache Heuristik. In Sonderfällen kann daher die Menge der
	 * zurückgegebenen Felder zu groß sein, es ist jedoch sicher gestellt, dass alle zurückgegebenen Felder auch in der
	 * Regelbedingung referenziert werden.
	 */
	String REGEL_REFFELDIDS_AUSLASSUNG_INTERN = "RefFeldIdentifierAuslassungsFehlerIntern";
	/**
	 * Gibt die in der Regel referenzierten Entities als Set&lt;{@link IIdentifier}&gt; zurück. Die Indices werden dabei mit
	 * Platzhaltern zurückgegeben.
	 */
	String REGEL_REFENTITYIDS_INTERN = "ReferenzierteEntityIdentifierIntern";
	/**
	 * Im Gegensatz zu <code>ReferenzierteEntityIdentifierIntern</code> werden bei der Verwendung von
	 * <code>RefEntityIdentifierAuslassungsFehlerIntern</code> nur {@link IIdentifier} zurückgegeben, die Auslassungsfehler erzeugen
	 * können. D.h. die zurückgegebene Menge ist eine Teilmenge von der Ergebnismenge, die bei
	 * <code>ReferenzierteEntityIdentifierIntern</code> zurückgegeben wird. Die beiden Mengen können auch gleich sein. Falls
	 * keine in der Regel referenzierten Felder Auslassungsfehler erzeugen können, wird eine leere Menge zurückgegeben. Die
	 * Ermittlung, ob ein Feld einen Auslassungsfehler erzeugen kann, ist eine einfache Heuristik. In Sonderfällen kann daher die
	 * Menge der zurückgegebenen Felder zu groß sein, es ist jedoch sicher gestellt, dass alle zurückgegebenen Felder auch in der
	 * Regelbedingung referenziert werden.
	 */
	String REGEL_REFENTITYIDS_AUSLASSUNG_INTERN = "RefEntityIdentifierAuslassungsFehlerIntern";
}
