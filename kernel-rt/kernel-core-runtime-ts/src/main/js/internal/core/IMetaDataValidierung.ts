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
import type { IMetaField } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { HashSet } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";


import type { IFormatDefinition } from "../formatdef/IFormatDefinition.js";
import type { LocalizableDefaults } from "../util/LocalizableUtil.js";
import type { RtIdentifierTemplate } from "../util/RtIdentifierTemplate.js";
import type { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";
import type { TrieNode } from "../util/TrieNode.js";

import type { IDynEnumsIntern } from "./IDynEnumsIntern.js";

export interface IMetaDataValidierung {
	/**
	 * @return Number-Array für die gültigen Unicode-Code-Points. Es wird eine Reihenfolge vorausgesetzt, sodass
	 *         mit binärer Suche gesucht werden kann.
	 */
	getLegalCodepoints(): number[][];

	/**
	 * @return Trie, der Code-Point-Folgen enthält, die zusammengenommen ein erlaubtes Graphem bilden.
	 */
	getLegalGraphemeTrie(): TrieNode;

	/**
	 * By default, only Unicode-Plane-0 characters are accepted. By setting the annotation
	 * <code>_mgmtp.a12.kernel.deactivateLegalCharCheck</code>, all Unicode characters including Surrogates are accepted.
	 *
	 * @return <code>true</code> if the model accepts all Unicode characters, <code>false</code> otherwise.
	 */
	isLegalCharCheckDeactivated(): boolean;

	/**
	 * @return the base year as a date (January 1st). If the model does not have a base year the date is not relevant
	 */
	getBaseYearAsDate(): Date;

	/**
	 * @return the base year as a String. If the model does not have a base year, returns `null`.
	 */
	getBaseYearAsString(): string;

	/**
	 * @return the decimal separator specified in the model.
	 */
	getDezimalTrenner(): string;

	/**
	 * @return the model's base year if the model has a base year, otherwise 2000
	 */
	getBaseYearOrDefault(): number;

	/**
	 * Die Methode wird benötigt, um aus den internen Feldnamen externe Feldnamen zu machen,
	 * wenn diese an die Schnittstelle zurückgeliefert werden müssen. Intern wird immer der
	 * unique Name eines Feldes verwendet.
	 *
	 * @param uniqueName	der intern verwendete unique Name des Feldes
	 * @return Gibt den Pfadnamen des Feldes zurück.
	 */
	getPfadName(uniqueName: string): string;

	/**
	 * Wird verwendet, um aus den externen Namen (Pfadnamen) des Feldes einen internen
	 * unique Name zu machen.
	 *
	 * @param fullName	Pfadname des Feldes
	 * @return Gibt den unique Name des Feldes zurück.
	 */
	getUniqueName(fullName: string): string;

	/**
	 * @param fullName
	 * 		full name of the field
	 * @return `true` if the model contains a field with the given full name, otherwise `false`.
	 */
	isFeldDefiniert(fullName: string): boolean;

	/**
	 * Check, whether the current MetaData contains a group or root group with the given name.
	 *
	 * @param fullName 	the full name of the field.
	 * @return <code>true</code>, if the group was found, <code>false</code> otherwise.
	 */
	isGroupDefined(fullName: string): boolean;

	/**
	 * @return Gibt die jeweiligen max. Wiederholbarkeiten der verschiedenen Wiederholungsebenen
	 *         zurück. Zugriff auf das erste Element der Liste ergibt die Wiederholbarkeit der max.
	 *         laufenden Nummer. Zugriff auf das letzte Element, der Zeile, ergibt immer 1.
	 */
	getMaxFieldIndices(uniqueName: string): number[];

	/**
	 * @return the max repeatabilities on the different repetition layers. First layer is root group
	 *         repetition.
	 */
	getMaxGroupIndices(name: string): number[];

	/**
	 *
	 * @param uniqueName 	name des Feldes
	 * @return gibt die interne Feldnummer des Feldes zurück. Die Nummer wird benötigt, da aus
	 *         Generierungsgründen die interne Id zur Optimierung/Fragmentierung verwendet wird.
	 */
	getInterneFeldNr(uniqueName: string): number;

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
	getIFormatDefinition(uniqueName: string): IFormatDefinition;

	/**
	 * Gibt die Fehlertexte zu einer Regel zurück.
	 *
	 * @param fullName 	Pfadname der Regel
	 * @param sprache 	Sprache in der der Fehlertext zurückgegeben werden soll
	 * @return Fehlertext der Regel
	 */

	getErrorTexts(fullName: string): LocalizableDefaults;

	/**
	 * @param fieldFullName	field path
	 * @return the error messages
	 */
	getErrorMessage4Req(fieldFullName: string): LocalizableDefaults;

	/**
	 * @param ruleName the full name of the rule.
	 * @returns the mappings of parameters in the rule error text. Maps from the
	 * parameter names that are displayed in the rule/field editor (which may depend on the
	 * model's language) or user-supplied localized messages to the internal parameter
	 * names that are used by the runtime.
	 */
	getParameterMapping(ruleName: string): Map<string, string>;

	/**
	 * Liefert die Menge der serverseitig berechneten Felder.
	 *
	 * @return die Menge der serverseitig berechneten Felder.
	 */
	getBerechneteFelder(): HashSet<IMetaField>;

	/**
	 * @return <code>true</code> if a field can be retrieved for the given unique field name.
	 */
	isUniqueFieldName(uniqueFeldName: string): boolean;

	/**
	 * Return the index fields of the given group.
	 *
	 * @param name 	the name of the group.
	 * @return the index fields of the group.
	 */
	getIndexFields(name: string): IMetaField[];

	/**
	 * @return <code>true</code> if index fields exist in the model at all, otherwise
	 *         <code>false</code>.
	 */
	hasIndexFields(): boolean;

	/**
	 * @param uniqueFieldName 	the name of the field.
	 * @return <code>true</code>, if the given field is an index field of its container.
	 */
	isIndexField(uniqueFieldName: string): boolean;

	/**
	 * @return full names of all index field
	 */
	getIndexFieldFullNames(): string[];

	getReferencedFieldsWithoutReplacement(ruleName: string): HashSet<RtInternalIdentifier>;

	getReferencedOmissionFieldsWithoutReplacement(ruleName: string): HashSet<RtInternalIdentifier>;

	/**
	 * @return the referenced entities of the rule (may contain groups) used for the V2
	 * possibly-causing-entities API. Indices are unresolved (as in the code-generated templates).
	 */
	getReferencedEntitiesWithoutReplacement(ruleName: string): HashSet<RtIdentifierTemplate>;

	/**
	 * @return the referenced entities of the rule that could cause omission errors (may contain
	 * groups) used for the V2 possibly-causing-entities API. Indices are unresolved.
	 */
	getReferencedOmissionEntitiesWithoutReplacement(ruleName: string): HashSet<RtIdentifierTemplate>;

	/**
	 * @return <code>true</code> if the given field was explicitly set as mandatory (not through rules, but through
	 *         the mandatory check box), otherwise <code>false</code>.
	 */
	isExplicitlyMandatory(uniqueName: string): boolean;

	/**
	 * @param name 	unique name or full name of the field
	 * @return the corresponding {@link IMetaField} instance.
	 * 		If no field was found, an Error is thrown.
	 */
	getMetaField(name: string): IMetaField;

	/**
	 * @param groupFullName 	the unique name of the queried group.
	 * @return the list of unique names of child elements (fields and groups) of the group.
	 */
	getContainedElements(groupFullName: string): Set<string>;

	/**
	 * @param groupFullName 	the unique name of the queried group.
	 * @return the list of full names of child groups of the group.
	 */
	getContainedGroups(groupFullName: string): Set<string>;

	/**
	 * @param groupFullName 	the unique name of the queried group.
	 * @return the list of unique names of child fields of the group.
	 */
	getContainedFields(groupFullName: string): Set<string>;

	/**
	 * @param entityFullName the full name of the field or group
	 * @return the full name of the closest ancestor group which is repeatable, or `null` if there are none
	 */
	getInnermostRepeatableGroup(entityFullName: string): string | null;

	/**
	 * @return the internal calculator object for dynamic enumerations.
	 */
	getDynamicEnumerationCalculator(): IDynEnumsIntern | undefined;

	/**
	 * @return the names of the required custom conditions. Never returns null.
	 */
	getRequiredCustomConditions(): Set<string>;

	/**
	 * The time zone, date time and time values are interpreted in.
	 *
	 * @return the id of the time zone currently either "UTC" or "Europe/Berlin".
	 */
	getTimeZone(): string;

	/**
	 * @return the id of the document model.
	 */
	getModelId(): string;

}
