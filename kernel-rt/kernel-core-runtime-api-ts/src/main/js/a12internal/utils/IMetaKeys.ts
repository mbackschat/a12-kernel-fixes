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

/**
 * Namespace specifying constants for the string values of publicly available keys for the Meta API.
 */
// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace IMetaKeys {
	// Keywords for IMetaModel
	export const MODEL_META_FIELD = "MetaFeld";
	export const MODEL_MANDATORY_FIELDS = "PflichtFelder";
	export const MODEL_META_RULE = "MetaRegel";
	export const MODEL_META_GROUP = "MetaContainer";
	export const MODEL_GROUP_LIST = "ContainerListe";
	export const MODEL_APPLICATION_CONDITION = "ApplikationsBedingungen";
	export const MODEL_CUSTOM_FIELD_TYPES_TO_BE_PROVIDED = "CustomFieldtypesToBeProvided";
	export const MODEL_LANGUAGES = "Sprachen";
	export const MODEL_CALC_FIELDS = "BerechneteFelder";
	export const MODEL_INDEX_FIELDS = "IndexFields";
	export const MODEL_INDEX_FIELD_FULL_NAMES = "IndexFieldFullNames";
	export const MODEL_DYNAMIC_ENUM_FIELDS = "DynamicEnumFields";
	export const MODEL_ANNOTATION = "Annotation";
	export const MODEL_ALL_ANNOTATED_ENTITIES = "ALL_AnnotatedEntities";

	// Keywords for IMetaGroup
	export const GROUP_SHORT_NAME = "Name";
	export const GROUP_FULLNAME = "PfadName";
	export const GROUP_SUBGROUP_LIST = "ContainerListe";
	export const GROUP_RULE_LIST = "RegelListe";
	export const GROUP_FIELD_LIST = "FelderListe";
	export const GROUP_MAX_INDEX = "MaxIndex";
	export const GROUP_INDEX_FIELDS = "IndexFields";
	export const GROUP_ANNOTATION = "Annotation";

	// Keywords for IMetaField
	export const FIELD_SHORT_NAME = "Name";
	export const FIELD_FULLNAME = "PfadName";
	export const FIELD_MANDATORY_FIELD = "Pflichtfeld";
	export const FIELD_MANDATORY_GLOBAL_OR_IN_SURROUNDING_REPEATABLE_GROUP =
		"MandatoryGloballyOrForRepeatableGroup";
	export const FIELD_MIN_LENGTH = "MinLaenge";
	export const FIELD_MAX_LENGTH = "MaxLaenge";
	export const FIELD_FIELD_TYPE = "Feldtyp";
	export const FIELD_PATTERN = "Muster";
	export const FIELD_ERROR_MESSAGE = "Fehlertext";
	export const FIELD_WORD_WRAP = "Zeilenumbruch";
	export const FIELD_SIGN = "Vorzeichen";
	export const FIELD_DECIMAL_PLACES = "Nachkommastellen";
	export const FIELD_MAX_DECIMAL_PLACES = "MaxNachkommastellen";
	export const FIELD_INTEGER_DIGIT = "Vorkommastellen";
	export const FIELD_ZERO_ALLOWED = "NullErlaubt";
	export const FIELD_LEADING_ZEROS = "FuehrendeNullen";
	export const FIELD_FORMAT = "Format";
	export const FIELD_VALUE_TRUE = "WertWahr";
	export const FIELD_VALUE_FALSE = "WertFalsch";
	export const FIELD_DECIMAL_SEPARATOR = "DezimalTrenner";
	export const FIELD_ENUMERATION_VALUES = "EnumerationWerte";
	export const FIELD_ROOT_GROUP_MANDATORY_FIELD = "VordruckPflichtfeld";
	export const FIELD_ENUMERATION_UI_VALUES = "EnumerationOberflaechenwerte";
	export const FIELD_OPERANDS_SET = "OperandenMenge";
	export const FIELD_TARGET_SET_FOR_CALC = "ZielMengeFuerBerechnung";
	export const FIELD_ENUMERATION_CATEGORY_VALUES = "EnumerationCategoryValues";
	export const FIELD_NO_VALUE_VALIDATION = "NoValueValidation";
	export const FIELD_GLOBAL = "GlobalesFeld";
	export const FIELD_TRANSIENT = "TransientField";
	export const FIELD_S_VALUE = "SValue";
	export const FIELD_EXPLICITLY_MANDATORY = "ExplicitlyMandatory";
	export const FIELD_SOURCE_FIELDS_FOR_DYNAMIC_ENUM = "SourceFieldsForDynamicEnum";
	export const FIELD_DYNAMIC_ENUM_VALUES = "DynamicEnumValues";
	export const FIELD_DYNAMIC_ENUMERATION = "DynamicEnumeration";
	export const FIELD_INDEX_FIELD = "IndexField";
	export const FIELD_META_FIELD_DATE_OF_DATE_RANGE = "MetaFieldDateOfDateRange";
	export const FIELD_ANNOTATION = "Annotation";
	export const FIELD_CUSTOM_ERROR_MESSAGE_FOR_REQUIRED = "CustomErrorMessageForRequired";
	export const FIELD_LABEL = "DruckText";

	// Keywords for IMetaRule
	export const RULE_NAME = "Name";
	export const RULE_FULL_NAME = "PfadName";
	export const RULE_ERROR_CODE = "Fehlercode";
	export const RULE_TYPE = "Regelart";
	export const RULE_REF_ROOTGROUP_OMISSION_ERROR = "RefVordruckeAuslassungsFehler";
	export const RULE_ERROR_FIELD = "Fehlerfeld";
	export const RULE_ANNOTATION = "Annotation";

	// Constants for the rule types
	export const RULE_TYPE_ERROR = "Fehler";
	export const RULE_TYPE_WARNING = "Hinweis";
	export const RULE_TYPE_INFO = "Info";

	// Constants for the field types
	export const FIELD_TYPE_STRING = "String";
	export const FIELD_TYPE_NUMBER = "Zahl";
	export const FIELD_TYPE_DATE = "Datum";
	export const FIELD_TYPE_DATE_RANGE = "DatumBereich";
	export const FIELD_TYPE_MONEY_2 = "GeldBetragMitCent";
	export const FIELD_TYPE_MONEY_0 = "GeldBetragOhneCent";
	export const FIELD_TYPE_YES = "Ja";
	export const FIELD_TYPE_YESNO = "JaNein";
	export const FIELD_TYPE_ENUMERATION = "Enumeration";
	export const FIELD_TYPE_EXT_ENUMERATION = "NichtAbgeschlosseneEnumeration";
}
