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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.core;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.IFormatDefinition;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.sorter.SortOrder;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.PresentationInformationValues;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtIdentifierTemplate;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.RtInternalIdentifier;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.util.TrieNode;
import com.mgmtp.a12.kernel.core.rt.a12internal.Pair;
import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaGroup;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;

import lombok.NonNull;

/**
 * Dieses Interface stellt Methoden zum Zugriff auf Metadaten bereit.
 */
public interface IMetaDataValidierung {

	/**
	 * @return Array containing valid unicode code point ranges. The outer array should always contain two
	 * 		elements, the list of start values for a range and the list of end values for a
	 * 		range.
	 * 		The two arrays have to have the same size.
	 */
	int[][] getLegalCodepoints();

	/**
	 * @return A prefix Trie of all legal combined code points which form a grapheme.
	 */
	TrieNode getLegalGraphemeTrie();

	/**
	 * @return Unique Namen der USB Index-Felder, falls es keine USB Indexfelder gibt, wird eine
	 * 		leere Menge zurückgegeben.
	 */
	@NonNull Set<String> getUSBIndexFelder();

	default boolean hasUSBIndexField() {
		return !getUSBIndexFelder().isEmpty();
	}

	/**
	 * @return the base year as a date (January 1st). If the model does not have a base year the date is not relevant
	 */
	Date getBaseYearAsDate();

	/**
	 * @return the base year as a String. If the model does not have a base year, returns {@code null}.
	 */
	String getBaseYearAsString();

	/**
	 * @return the decimal separator specified in the model. This value must not be mistaken with the decimal separator potentially
	 * 		specified for the display values (see {@link PresentationInformation#DECIMAL_SEPARATOR}).
	 */
	char getDezimalTrenner();

	/**
	 * @return the model's base year if the model has a base year, otherwise 2000
	 */
	int getBaseYearOrDefault();

	/**
	 * Die Methode wird benötigt, um aus den internen Feldnamen externe Feldnamen zu machen,
	 * wenn diese an die Schnittstelle zurückgeliefert werden müssen. Intern wird immer der
	 * Unique Name eines Feldes verwendet.
	 *
	 * @param uniqueName
	 * 		der intern verwendete uniqueName des Feldes
	 * @return Gibt den Pfadnamen des Feldes zurück.
	 */
	String getPfadName(String uniqueName);

	/**
	 * Wird verwendet, um aus den externen Namen (Pfadnamen) des Feldes einen internen
	 * unique Name zu machen.
	 *
	 * @param fullName
	 * 		Pfadname des Feldes
	 * @return Gibt den unique-Name des Feldes zurück.
	 */
	String getUniqueName(String fullName);

	/**
	 * @param fullName
	 * 		full name of the field
	 * @return {@code true} if the model contains a field with the given full name, otherwise {@code false}.
	 */
	boolean isFeldDefiniert(String fullName);

	/**
	 * Check, whether the current MetaData contains a group or root group with the given name.
	 *
	 * @param fullName
	 * 		the full name of the field.
	 * @return <code>true</code>, if the group was found, <code>false</code> otherwise.
	 */
	boolean isGroupDefined(String fullName);

	/**
	 * @return see {@link IMetaDataValidierung#getMaxFieldIndices(IMetaField)}
	 */
	List<Long> getMaxFieldIndices(String uniqueName);

	/**
	 * @return Gibt die jeweiligen max. Wiederholbarkeiten der verschiedenen Wiederholungsebenen
	 * 		zurück. Zugriff auf das erste Element der Liste ergibt die Wiederholbarkeit der max.
	 * 		laufenden Nummer. Zugriff auf das letzte Element ergibt die Wiederholbarkeit der
	 * 		Zeile.
	 */
	List<Long> getMaxFieldIndices(IMetaField field);

	/**
	 * @return the max repeatabilities on the different repetition layers. First layer is root group
	 * 		repetition.
	 */
	List<Long> getMaxGroupIndices(String name);

	/**
	 * @param uniqueName
	 * 		- name des Feldes
	 * @return gibt die interne Feldnummer des Feldes zurück. Die Nummer wird benötigt, da aus
	 * 		Generierungsgründen die interne Id zur Optimierung/Fragmentierung verwendet wird.
	 */
	int getInterneFeldNr(String uniqueName);

	/**
	 * @param uniqueName
	 * @return Maximaler Usb des Vordruckes, auf dem das Feld liegt. Falls
	 * 		der Vordruck keinen Usb hat oder uniqueName nicht existiert
	 * 		wird IIdentifier.USB_NICHT_ANGEGEBEN zurückgegeben.
	 */
	long getMaxUsb(String uniqueName);

	/**
	 * @param uniqueName
	 * @return Max Zeile; '0' falls uniqueName nicht existiert
	 */
	long getMaxZeile(String uniqueName);

	/**
	 * Gibt die FormatDefintion zu dem übergebenen Namen zurück.
	 * Bei der Verwendung der alten Schnittstelle ist der (externe) Name und
	 * der unique Name immer gleich. Daher ist die Verwendung des Namens für die
	 * Exception korrekt. <br>
	 * Bei Verwendung der neuen Schnittstelle wird hier der unique Name verwendet.
	 * Es wurde aber bereits bei der Transformation vom (externen) Pfadnamen
	 * in den unique Name überprüft, ob der Pfadname in der Regelmenge vorhanden
	 * ist.
	 *
	 * @return IFormatDefinition für das Feld mit Namen &lt;name&gt;
	 */
	IFormatDefinition getIFormatDefinition(String uniqueName);

	/**
	 * Gibt den Fehlertext zu einer Regel zurück.
	 *
	 * @param fullName
	 * 		Pfadname der Regel
	 * @param sprache
	 * 		Sprache in der der Fehlertext zurückgegeben werden soll
	 * @return Fehlertext der Regel
	 */
	String getFehlertext(String fullName, Locale sprache);

	/**
	 * @param fieldFullName
	 * 		field path
	 * @param locale
	 * 		locale in which the error message should be searched
	 * @return the error message for the given locale.
	 */
	public String getErrorMessage4Req(String fieldFullName, Locale locale);

	/**
	 * Liefert die Menge der serverseitig berechneten Felder.
	 *
	 * @return die Menge der serverseitig berechneten Felder.
	 */
	Set<IMetaField> getBerechneteFelder();

	/**
	 * Returns the language specific default for the presentation information values for all allowed
	 * languages.
	 */
	Map<Locale, PresentationInformationValues> getDefaultPresentationInformation();

	/**
	 * @return <code>true</code> if for the given unique field name a field can be retrieved.
	 */
	boolean isUniqueFieldName(String uniqueFeldName);

	/**
	 * @return <code>true</code> if the model requires unique field names (old world).
	 */
	boolean hasUniqueEntities();

	/**
	 * Returns the index fields of the given group.
	 *
	 * @param name
	 * 		the name of the group.
	 * @return the index fields of the group.
	 */
	List<IMetaField> getIndexFields(String name);

	/**
	 * @return <code>true</code> if index fields exist in the model at all, otherwise
	 * 		<code>false</code>.
	 */
	boolean hasIndexFields();

	/**
	 * @param uniqueFieldName
	 * 		the name of the field.
	 * @return <code>true</code>, if the given field is an index field of its container.
	 */
	boolean isIndexField(String uniqueFieldName);

	/**
	 * @param uniqueFieldName
	 * 		field unique name.
	 * @return <code>true</code>, if the given field is a transient field, otherwise
	 * 		<code>false</code>.
	 */
	boolean isTransientField(String uniqueFieldName);

	/**
	 * @return the sort information of the model.
	 */
	Map<String, List<Pair<String, SortOrder>>> getSortInfo();

	/**
	 * @return the sort info created from the index fields.
	 */
	Map<String, List<Pair<String, SortOrder>>> getSortInfo4Mapper();

	/**
	 * @return the referenced fields of the given rule as specified in the generated code. The indices of the identifiers need to
	 * 		be resolved before they can be returned in IResult.
	 */
	Set<RtInternalIdentifier> getReferencedFieldsWithoutReplacement(String ruleName);

	/**
	 * @return the referenced omission fields of the given rule as specified in the generated code. The indices of the identifiers
	 * 		need to be resolved before they can be returned in IResult.
	 */
	Set<RtInternalIdentifier> getReferencedOmissionFieldsWithoutReplacement(String ruleName);

	/**
	 * @return the referenced entities (fields and groups) of the given rule as specified in the generated code.
	 */
	Set<RtIdentifierTemplate> getReferencedEntitiesWithoutReplacement(String ruleName);

	/**
	 * @return the referenced omission entities (fields and groups) of the given rule as specified in the generated code.
	 */
	Set<RtIdentifierTemplate> getReferencedOmissionEntitiesWithoutReplacement(String ruleName);

	/**
	 * @return true, if code was generated for ExtMP. The method is necessary because in ExtMP
	 * 		there are specific error message texts for formal errors.
	 */
	boolean isExtmp();

	/**
	 * @return <code>true</code> if the given field was explicitly set as mandatory (not through
	 * 		rules,
	 * 		but through the mandatory check box), otherwise <code>false</code>.
	 */
	boolean isExplicitlyMandatory(String uniqueName);

	/**
	 * @param name
	 * 		unique name or full name of the field
	 * @return the corresponding {@link IMetaField}-instance. If no field was found, an
	 *        {@link IllegalArgumentException} is thrown.
	 */
	IMetaField getMetaField(String name);

	/**
	 * @param name
	 * 		full name of the group
	 * @return the corresponding IMetaGroup-insatnce. If no field was found, <code>null</code> is returned.
	 */
	IMetaGroup getMetaGroupOrNull(String name);

	/**
	 * @param groupFullName
	 * 		the unique name of the queried group.
	 * @return the list of unique names of child elements (fields and groups) of the group.
	 */
	Set<String> getContainedElements(String groupFullName);

	/**
	 * @param groupFullName
	 * 		the unique name of the queried group.
	 * @return the list of full names of child groups of the group.
	 */
	Set<String> getContainedGroups(String groupFullName);

	/**
	 * @param groupFullName
	 * 		the unique name of the queried group.
	 * @return the list of unique names of child fields of the group.
	 */
	Set<String> getContainedFields(String groupFullName);

	/**
	 * @param entityFullName
	 * 		the full name of the field or group
	 * @return the full name of the closest ancestor group which is repeatable, or {@code null} if there are none
	 */
	String getInnermostRepeatableGroup(String entityFullName);

	/**
	 * @return the internal calculator object for dynamic enumerations.
	 */
	IDynEnumsIntern getDynamicEnumerationCalculator();

	/**
	 * @return the names of the required custom conditions. Never returns null.
	 */
	Set<String> getRequiredCustomConditions();

	/**
	 * @return full names of all index fields. Always empty for HDVs.
	 */
	List<String> getIndexFieldFullNames();

	/**
	 * @return full names of all fields
	 */
	Set<String> getFieldFullNames();

	/**
	 * The time zone, date time and time values are interpreted in.
	 *
	 * @return the id of the time zone currently either "UTC" or "Europe/Berlin".
	 */
	TimeZone getTimeZone();

	/**
	 * @return the id of the document model.
	 */
	String getModelId();

	/**
	 * By default, only Unicode-Plane-0 characters are accepted. By setting the annotation
	 * <code>_mgmtp.a12.kernel.deactivateLegalCharCheck</code>, all Unicode characters including Surrogates are accepted.
	 *
	 * @return <code>true</code> if the model accepts all Unicode characters, <code>false</code> otherwise.
	 */
	boolean isLegalCharCheckDeactivated();

	IMetaModel getMetaModel();
}
