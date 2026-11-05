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

import type { Annotation, Model, ModelPath } from "@com.mgmtp.a12.base/base-model-api";

// used for typedoc

import type { DatePrecision, InterpretationOfYear, Localizable, LocalizedModelText, ValueConversionConfig } from "@com.mgmtp.a12.utils/utils-localization";
import { SupportedType } from "@com.mgmtp.a12.utils/utils-localization";


import type { ICustomFieldTypeFactory } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

// used for typedoc
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { DocumentRtServiceFactory } from "./facade.js";

/**
 * Serializer that can deserialize a document model represented as a string.
 * It does some transformations on the underlying JSON-string and wraps it
 * as a {@link DocumentModel}.
 *
 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
 */
export interface DocumentModelSerializer {
	/**
	 * Deserializes the document as a DocumentModel.
	 *
	 * @param documentModelAsString the document model as a JSON-string.
	 */
	deserialize(documentModelAsString: string): DocumentModel;
}

/**
 * Collection of search services which can be applied to a {@link DocumentModel}.
 *
 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
 */
export interface DocumentModelSearchService {
	/**
	 * <b>Note:</b> With the first call of this method, a cache of {@link ModelPath}s to
	 * the corresponding id will be initialised internally for all the fields and groups
	 * contained in the document model.
	 *
	 * @param id element id (field or group).
	 *
	 * @returns the corresponding path of the document model element.
	 */
	getPathById(id: string): ModelPath | undefined;
	/**
	 * @param path element path of the document model element (field or group).
	 *
	 * @returns the corresponding document model element.
	 */
	getByPath(path: ModelPath): DocumentModel.Element | undefined;
}

/**
 * Defines the path of an entity instance in a document.
 * Each path segment contains the name of an entity and its corresponding repetition index.
 * <p>
 * <b>Important notes:</b>
 * <ol>
 * <li>The index of an entity instance begins always at 1.
 * This is specially important for group instances of a repeatable group.</li>
 * <li>The index of a field is always 1.</li>
 * </ol>
 * <p>
 * <b>Example:</b>
 * Having following group tree in a document model:
 * <pre>
 * G[5]
 * |- F1
 * |- G1[5]
 * |- |- F2
 * |- G2[1]
 * |- |- F3
 * </pre>
 * Following entity instance paths for the corresponding document are possible:
 * <ul>
 * <li><code>[{"G", 1}]</code></li>
 * <li>...</li>
 * <li><code>[{"G", 5}]</code></li>
 * <li><code>[{"G", 1}, {"F1", 1}]</code></li>
 * <li>...</li>
 * <li><code>[{"G", 5}, {"F1", 1}]</code></li>
 * <li><code>[{"G", 1}, {"G1", 1}]</code></li>
 * <li>...</li>
 * <li><code>[{"G", 5}, {"G1", 1}]</code></li>
 * <li><code>[{"G", 1}, {"G1", 1}, {"F2", 1}]</code></li>
 * <li>...</li>
 * <li><code>[{"G", 5}, {"G1", 1}, {"F2", 1}]</code></li>
 * <li><code>[{"G", 1}, {"G1", 2}, {"F2", 1}]</code></li>
 * <li>...</li>
 * <li><code>[{"G", 5}, {"G1", 2}, {"F2", 1}]</code></li>
 * <li>...</li>
 * <li><code>[{"G", 5}, {"G1", 5}, {"F2", 1}]</code></li>
 * <li><code>[{"G", 1}, {"G2", 1}]</code></li>
 * <li>...</li>
 * <li><code>[{"G", 5}, {"G2", 1}]</code></li>
 * <li><code>[{"G", 1}, {"G2", 1}, {"F3", 1}]</code></li>
 * <li>...</li>
 * <li><code>[{"G", 5}, {"G2", 1}, {"F3", 1}]</code></li>
 * </ul>
 */
export type EntityInstancePath = {
	readonly elementName: string;
	readonly index: number;
}[];

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace EntityInstancePath {
	export function equals(
		instance: EntityInstancePath | PartiallyKnownEntityInstancePath,
		otherInstance: EntityInstancePath | PartiallyKnownEntityInstancePath
	): boolean {
		if (instance.length !== otherInstance.length) {
			return false;
		}
		const elements: PartiallyKnownEntityInstancePath = instance;
		const otherElements: PartiallyKnownEntityInstancePath = otherInstance;
		for (let i = 0; i < elements.length; i++) {
			const { elementName, index, semanticIndexValueForUnknownIndexGroup } = elements[i];
			const {
				elementName: otherElementName,
				index: otherIndex,
				semanticIndexValueForUnknownIndexGroup: otherSemanticIndexValueForUnknownIndexGroup
			} = otherElements[i];
			if (index !== otherIndex || elementName !== otherElementName) {
				return false;
			}
			if (semanticIndexValueForUnknownIndexGroup === undefined || otherSemanticIndexValueForUnknownIndexGroup === undefined) {
				if (semanticIndexValueForUnknownIndexGroup !== otherSemanticIndexValueForUnknownIndexGroup) {
					return false;
				}
			} else if (!SupportedType.equals(semanticIndexValueForUnknownIndexGroup, otherSemanticIndexValueForUnknownIndexGroup)) {
				return false;
			}
		}
		return true;
	}

	export function hash(instance: EntityInstancePath | PartiallyKnownEntityInstancePath): number {
		let hash = 1;
		const elements: PartiallyKnownEntityInstancePath = instance;
		for (const { elementName, index, semanticIndexValueForUnknownIndexGroup } of elements) {
			hash = mix(hash, hashString(elementName));
			hash = mix(hash, index);
			hash = mix(
				hash,
				semanticIndexValueForUnknownIndexGroup === undefined
					? 0
					: hashString(semanticIndexValueToString(semanticIndexValueForUnknownIndexGroup))
			);
		}
		return finalize(hash);
	}

	function hashString(value: string): number {
		let result = 1;
		for (let i = 0; i < value.length; i++) {
			result = ((result << 5) - result + value.charCodeAt(i)) | 0;
		}
		return result;
	}

	function mix(hash: number, value: number): number {
		return Math.imul(hash ^ value, 0x01000193) | 0;
	}

	/**
	 * Spreads the higher bits of the given hash code into the lower ones (MurmurHash3's finalizer).
	 */
	function finalize(hash: number): number {
		let result = hash;
		result ^= result >>> 16;
		result = Math.imul(result, 0x85ebca6b);
		result ^= result >>> 13;
		result = Math.imul(result, 0xc2b2ae35);
		result ^= result >>> 16;
		return result >>> 0;
	}

	function semanticIndexValueToString(value: FieldInstanceValue): string {
		if (value instanceof Date) {
			return String(value.getTime());
		}
		if (Array.isArray(value)) {
			return value.map(date => String(date.getTime())).join("-");
		}
		return String(value);
	}
}

/**
 * Defines the value which a field instance can have in a document.
 */
export type FieldInstanceValue = SupportedType;

/**
 * Defines the content of a group instance.
 * A group instance can look like as follows:
 * ```js
 * {
 *   "field" : "field instance value",
 *   "anotherGroupInstance" : {
 *     "fieldInGroupInstance" : "",
 *     ...
 *   }, // this group instance could be also empty => {}
 *   "repeatableGroup" : [
 *     {
 *       "fieldInRepeatableGroup" : "first instance",
 *       ...
 *     } // first group instance of the repeatable group
 *   ] // this array could be also empty => []
 * }
 * ```
 *
 * Note that repeatable groups cannot have empty slots - setting a slot to undefined
 * will shift the following entries to the left, fill the gap and decrement the indices
 * of those following entries.
 */
export interface GroupInstance {
	readonly [key: string]:
	| readonly GroupInstance[]
	| Readonly<GroupInstance>
	| FieldInstanceValue
	| undefined;
}

export interface Document {
	readonly [key: string]:
	| readonly GroupInstance[]
	| Readonly<GroupInstance>
	// id of the document, only 'id' is allowed as key
	| string;
}

/**
 * Document Service providing some functions to transform documents in JSON format.
 *
 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
 */
export interface DocumentService {
	/**
	 * Removes all transient fields from the document no matter if they are filled.
	 *
	 * @param document document as object.
	 * @param model respective document model.
	 *
	 * @returns a shared copy of the original document. I.e. only the modified parts are replaced.
	 */
	removeTransientFields(document: object, model: DocumentModel): object;

	/**
	 * Adds all non-existent transient fields to document, i.e. if a transient field exists already in the document, it
	 * won't be overwritten.
	 *
	 * @param document document as object.
	 * @param model respective document model.
	 *
	 * @returns a shared copy of the original document. I.e. only the modified parts are replaced.
	 */
	addTransientFields(document: object, model: DocumentModel): object;

	/**
	 * Remove all empty fields (containing null or an empty string). And remove all then empty groups recursively.
	 * @param document  document as object.
	 * @param model respective document model
	 *
	 * @returns a shared copy of the original document. I.e. only the modified parts are replaced.
	 */
	removeEmptyFieldsAndGroups(document: object, model: DocumentModel): object;

	/**
	 * Finds all filled date fields, for each field converts the given string value to a date object and replaces the
	 * original string value through the converted date object.
	 *
	 * @param document document as object.
	 * @param model respective document model.
	 *
	 * @returns a shared copy of the original document. I.e. only the modified parts are replaced.
	 */
	parseDates(document: object, model: DocumentModel): object;

	/**
	 * Finds all filled date fields, for each field converts the given date object to the string representation and
	 * replaces the original date object through the converted string representation.
	 * In case of invalid date strings, Invalid Date objects will be created
	 * (<code>(isNaN(date.getTime()) === true)</code>).
	 *
	 * @param document document as object.
	 * @param model respective document model.
	 *
	 * @returns a shared copy of the original document. I.e. only the modified parts are replaced.
	 */
	formatDates(document: object, model: DocumentModel): object;

	/**
	 * Sets the given object in the parameter <code>update</code> to the given entity instance,
	 * only if the object has changed or is new.
	 * If the entity instance, or any of its ancestor instances, does not exist in the given document,
	 * it will be added together with all of its missing ancestor instances.
	 * In order to update/set the right object to a certain entity instance, following rules must be followed:
	 * <ul>
	 * <li>use {@link FieldInstanceValue} for field instances</li>
	 * <li>use {@link GroupInstance} for group instances in general: the group instance of a non-repeatable
	 * group or a <b>specific</b> group instance of a repeatable group</li>
	 * <li>it is also possible to update/set a complete repeatable group, this means, to replace all existing
	 * group instances (if any) through the given bounch of group instances of the repeatable group <b>at once</b>.
	 * For this purpose, use an array of {@link GroupInstance}s and set the index of the last <code>path</code>
	 * segment to '0' (the one which corresponds to the repeatable group you want to update). This is needed to
	 * differentiate between this case and updating a <b>specific</b> group instance of the repeatable group.</li>
	 * <li>use <code>undefined</code> to unset</li>
	 * </ul>
	 * <b>Important note:</b> The user of this method is responsible for setting a suitable object to the given
	 * entity instance. There are only basic checks while looking for the given entity instance and updating the object.
	 *
	 * @param document document as object.
	 * @param path path of the entity instance.
	 * @param update object to update/set (for more details see method description).
	 * @param documentModelElement corresponding document model element for the given <code>document</code>.
	 *
	 * @returns a share copy of the original document. I.e. only the modified parts are replaced.
	 * @throws Error if the given entity instance does not fit to the given document model element.
	 *
	 * Note that repeatable groups cannot have empty slots - setting a slot to undefined
	 * will shift the following entries to the left, fill the gap and decrement the indices
	 * of those following entries.
	 */
	updateEntityInstance(
		document: GroupInstance,
		path: EntityInstancePath,
		update: readonly GroupInstance[] | Readonly<GroupInstance> | FieldInstanceValue | undefined,
		documentModelElement: DocumentModel
	): GroupInstance;

	/**
	 * Retrieves the assigned object of the given entity instance.
	 * If the rules in the method {@link updateEntityInstance} were followed, following objects can be expected:
	 * <ul>
	 * <li>{@link FieldInstanceValue} for field instances</li>
	 * <li>{@link GroupInstance} for the group instance of a non-repeatable group or for a <b>specific</b> group
	 * instance of a repeatabel group.</li>
	 * <li>an array of {@link GroupInstance}s of a repeatable group, if the index of the last <code>path</code>
	 * segment was set to '0'.</li>
	 * <li><code>undefined</code>, if the given entity instance itself is not present,
	 * or any of its ancestor instances</li>
	 * </ul>
	 *
	 * @param document document as object.
	 * @param path path of the entity instance.
	 *
	 * @returns the object of the given entity instance (for more details see method description).
	 */
	getAssignedObject(
		document: GroupInstance,
		path: EntityInstancePath
	): GroupInstance[] | GroupInstance | FieldInstanceValue | undefined;

	/**
	 *
	 * @param document the document in which the paths are searched
	 * @param searchPath the path which could contain 0 indices which results in search for all concrete indices for that level.
	 * @returns given a path (which might contain index 0), returns all document paths with concrete indices which exist in the document.
	 */
	getEntityPathsInDocument(
		document: GroupInstance,
		searchPath: EntityInstancePath
	): EntityInstancePath[];
}

/**
 * A model that defines a document structure.
 *
 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
 */
export interface DocumentModel extends Model {
	readonly content: DocumentModel.DocumentModelContent;
}

// TODO: Fix this the next time the file is edited.
// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace DocumentModel {
	export const fieldTypeSuffix = "Type";
	export type ReadonlyObjectMap<V> = {
		readonly [key: string]: V | undefined;
	};

	/**
	 * Provides functionality to describe model elements externally.
	 * The internal description is hidden, because it should not be used on client side.
	 * It contains information for the business analyst only.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface Described {
		readonly externalDescription?: LocalizedModelText;
	}

	/**
	 * Adds Id and Naming functionality to model elements
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface IdNamed {
		readonly id: string;
		readonly name: string;
	}

	/**
	 * Offers extensibility for annotations with key-value semantics.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface Annotated {
		readonly annotations?: Annotation[];
	}

	/**
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface DocumentModelContent {
		readonly modelInfo: DocumentModelInfo;
		readonly modelConfig: DocumentModelConfig;
		readonly documentUniquenessCriteria: DocumentUniquenessCriterion[];
		readonly modelRoot: DocumentModel.Group;
	}

	/**
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface DocumentModelInfo {
		readonly baseYear?: number;
		readonly packet?: string;
	}

	/**
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface DocumentModelConfig {
		readonly timeZone: string;
	}

	/**
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface DocumentUniquenessCriterion {
		readonly name: string;
		readonly fields: { fullName: string }[];
		readonly errorMessage: LocalizedModelText;
	}

	/**
	 * Abstract base type for {@link FieldType}s.
	 */
	export type FieldType =
		| BooleanType
		| ConfirmType
		| DateType
		| DateTimeType
		| DateRangeType
		| DateFragmentType
		| EnumerationType
		| NumberType
		| StringType
		| TimeType
		| CustomFieldType;

	/**
	 * For {@link Field}s with boolean behaviour.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface BooleanType {
		readonly type: "BooleanType";
	}

	/**
	 * For {@link Field}s containing YES value or UNDEFINED.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface ConfirmType {
		readonly type: "ConfirmType";
	}

	/**
	 * For {@link Field}s containing date values.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface DateType {
		readonly type: "DateType";
		readonly format: string;
		readonly datePrecision?: DatePrecision;
	}

	/**
	 * For {@link Field}s containing date range values.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface DateRangeType {
		readonly type: "DateRangeType";
		readonly format: string;
		readonly rangeSeparator: string;
		readonly interpretationOfYear?: InterpretationOfYear;
	}

	/**
	 * For {@link Field}s containing date time data.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface DateTimeType {
		readonly type: "DateTimeType";
		readonly format: string;
	}

	/**
	 * For {@link Field}s containing time data without a date.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface TimeType {
		readonly type: "TimeType";
		readonly format: string;
	}

	/**
	 * For {@link Field}s containing date values without a date part (day, month, year).
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface DateFragmentType {
		readonly type: "DateFragmentType";
		readonly formatOfFragment: string;
	}

	/**
	 * Models a enumeration value.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface EnumValue {
		readonly value: string;
		readonly label?: LocalizedModelText;
	}

	/**
	 * For {@link Field}s containing enumerable values.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface EnumerationType {
		readonly type: "EnumerationType";
		/**
		 * enumeration values with their corresponding label.
		 */
		readonly values: ReadonlyArray<EnumValue>;
		readonly errorMessage?: LocalizedModelText;
		readonly alphabeticalSorting?: boolean;
	}

	/**
	 * Models a hint list.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface HintList {
		readonly locale: string;
		readonly values: ReadonlyArray<string>;
	}

	/**
	 * For {@link Field}s containing string values.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface StringType {
		readonly type: "StringType";
		readonly minLength?: number;
		readonly maxLength?: number;
		readonly lineBreaksPermitted?: boolean;
		readonly noValueValidation?: boolean;
		readonly pattern?: string;
		readonly errorMessage?: LocalizedModelText;
		readonly alphabeticalSorting?: boolean;
		readonly hintList?: ReadonlyArray<HintList>;
	}

	/**
	 * For {@link Field}s containing numbers.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface NumberType {
		readonly type: "NumberType";
		readonly zeroNotAllowed?: boolean;
		readonly minValue?: number;
		readonly maxValue?: number;
		/**
		 * At the moment allowed values are: "amount" | "percent" | "permille".
		 */
		readonly trait?: string;
		readonly minFractionalDigits?: number;
		readonly maxFractionalDigits?: number;
		readonly leadingZerosAllowed?: boolean;
	}

	/**
	 * For {@link Field}s for which a custom validation logic is needed.
	 * The validation code with the custom logic must be provided by the user.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface CustomFieldType {
		readonly type: "CustomFieldType";
		readonly name: string;
	}

	/**
	 * Abstract base type for all document hierarchy elements of a {@link DocumentModel}.
	 */
	export type Element = Group | Field;

	/**
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface Labeled {
		readonly label?: LocalizedModelText;
	}

	/**
	 * Provides the configuration of group includes.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface IncludeConfig {
		readonly reference?: string;
		readonly excludeRules?: boolean;
	}

	/**
	 * A {@link Group} is a structural {@link Element} that serves as a container for other {@link Element}s.
	 * It can be repeatable which means a document instance of this {@link DocumentModel} can contain multiple instances
	 * of this {@link Group}.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface Group extends IdNamed, Described, Annotated, Labeled {
		readonly type: "Group";
		readonly repeatability: number;
		readonly indexFieldName?: string;
		readonly usageType?: string; // for example "Attachment" (formerly known as customType)
		readonly elements: ReadonlyArray<Element>;
		readonly includeConfig?: IncludeConfig;
	}

	/**
	 * Provides the configuration of required fields.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface RequirednessConfig {
		readonly mode: "absoluteOrRelativeToNextRepAncestor" | "relativeToParent";
		readonly errorMessage?: LocalizedModelText;
	}

	/**
	 * Provides access to {@link Field} specific properties.
	 * A {@link Field} is a leaf {@link Element} of a document structure that can have a value.
	 * The format of this value is defined by the  {@link Field}s {@link FieldType}.
	 *
	 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
	 */
	export interface Field extends IdNamed, Described, Annotated, Labeled {
		readonly type: "Field";
		readonly fieldType: FieldType;
		readonly global?: boolean;
		readonly transient?: boolean;
		readonly helperText?: LocalizedModelText;
		readonly requirednessConfig?: RequirednessConfig;
	}

	export function extractConversionConfig(fieldType: FieldType, timeZone?: string, baseYear?: number): ValueConversionConfig {
		switch (fieldType.type) {
			case "DateType":
			case "DateTimeType":
			case "TimeType":
			case "DateRangeType":
				return {
					...fieldType,
					timeZone: timeZone,
					baseYear: baseYear,
					singleDate: "notAllowed"
				} as ValueConversionConfig;
			case "DateFragmentType":
				return {
					...fieldType,
					format: fieldType.formatOfFragment,
					timeZone: timeZone,
					baseYear: baseYear
				} as ValueConversionConfig;
			case "CustomFieldType":
				return {
					...fieldType,
					representation: "internal"
				};
			default:
				return { ...fieldType };
		}
	}
}


/**
 * This interface defines the structure to access the generated code.
 *
 * It provides methods to acquire the validator, calculator or the access to the meta data.
 *
 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
 */
export interface IGeneratedCodeAccessor {

	/**
	 * Get the validator from the generated code.
	 */
	getValidator(): any;
	/**
	 * Get the calculator from the generated code.
	 */
	getCalculator(): any;
	/**
	 * Get the access to the meta data from the generated code.
	 */
	getMetaModel(): any;
}

// ################ MD API ################

/**
 * Entity instance to string value mapping.
 * At the moment, used in {@link DocumentRtService.validateFull} and {@link DocumentRtService.validatePart}.
 * 
 * <b>Note:</b> JavaScript's {@link Map} compares {@link EntityInstancePath} based on identity. Users are responsible for ensuring unique keys.
 * If semantic comparison and hashing of {@link EntityInstancePath} is desired, a proper HashMap using {@link EntityInstancePath.equals} and
 * {@link EntityInstancePath.hash} should be used instead. E.g. the following implementation is based on utils' HashMap:
 * ```ts
 * class EntityInstancePathMap<V> extends HashMap<EntityInstancePath, V> {
 *   constructor(entries?: readonly (readonly [EntityInstancePath, V])[] | null) {
 *     super(
 *       key => EntityInstancePath.hash(key),
 *       (a, b) => EntityInstancePath.equals(a, b)
 *     );
 *
 *     if (entries != undefined) {
 *       for (const [key, value] of entries) {
 *         this.set(key, value);
 *       }
 *     }
 *   }
 * }
 * ```
 */
export type EntityInstanceValueMapping = Map<EntityInstancePath, string>;

/**
 * Configuration to control some aspects of runtime functionalities (e.g. validation, computation).
 *
 * For example, the user can set a date used for the language construct <b>Today</b> during computation and validation, see <code>currentDateForTest</code>.
 */
export interface GeneratedCodeRtConfig {
	/**
	 * The factory for the custom conditions that should be used.
	 */
	readonly customConditionFactory?: ICustomConditionFactory;
	/**
	 * The factory for the custom field types that should be used.
	 */
	readonly customFieldTypeFactory?: ICustomFieldTypeFactory;
	/**
	 * <b>Note:</b> This config should only be used for (ad-hoc-)tests, not for production.
	 * This date has to be created in the timezone defined in the document model, e.g. <code>new Date(Date.UTC(2000, 0, 1))</code>.
	 */
	readonly currentDateForTest?: Date;
	/**
	 * The option to ignore all unknown fields in the validation and computation. Unknown fields are fields that are not defined in the document model.
	 * Unknown groups are always ignored.
	 */
	readonly ignoreUnknownFields?: boolean;
}

/**
 * Access to runtime functionalities, i.e. validation or computation on a given document.
 * An instance of this interface can be created with {@link DocumentRtServiceFactory.createDocumentRtService}.
 *
 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
 */
export interface DocumentRtService {
	/**
	 * Validates the whole given {@link Document}.
	 * 
	 * @param document The {@link Document} which should be validated.
	 * @param valuesOfFieldInstancesToConsider Values as string for field instances that should be considered for
	 * validation. These values are prioritized over the ones in the document. The configuration
	 * {@link GeneratedCodeRtConfig.ignoreUnknownFields} does not affect the field instances given in this parameter.
	 * 
	 *   Examples of use cases:
	 *   - It can be used to provide values that cannot be converted to the corresponding type and are therefore not included in the document.
	 *   - A value in the document can be overwritten by explicitly setting another value or even handled as deleted by setting <code>""</code>.
	 * @return A {@link DocumentValidationResult} containing the validation messages.
	 */
	validateFull(document: Document, valuesOfFieldInstancesToConsider?: EntityInstanceValueMapping): DocumentValidationResult;

	/**
	 * Validates a part of the given {@link Document}.
	 * 
	 * **Notes:**
	 * <ul>
	 *     <li>Rule conditions are evaluated using three value logic, which may cause validation rules to not fire if not all fields
	 *     and repetitions referenced by the rule are marked as relevant.</li>
	 *     <li>All possible field instances of global fields are internally added to the relevant entity instances and therefore,
	 *     they are considered during the partial validation.</li>
	 * </ul>
	 * 
	 * @param document The {@link Document} which should be validated.
	 * @param relevantEntityInstances The set of entity instances which are relevant for validation. 
	 * Allows the specification of repetitions as the wildcard `0`, which refers to all repetitions of the respective
	 * repeatable group.
	 * 
	 * Validation rules are only executed if their error field instance is contained in this set. Of those
	 * rules that are evaluated, validation errors will be suppressed if the rule references any repetition of a 
	 * field or group that is not marked as relevant (except for some cases where kernel is sure that no possible 
	 * value of that field/group repetition could have prevented the validation error). 
	 * In particular, this means that if the rule has a wildcard reference to all allowed repetitions of a field 
	 * (e.g. /RG`*`/f1), validation errors for that rule will usually be suppressed unless /RG[0]/f1 is marked as relevant. 
	 * Example: if only two repetitions of RG are filled, but RG's max repeatability is 10, then it is insufficient to 
	 * mark only /RG[1]/f1 and /RG[2]/f2 as revant, because /RG`*`/f1 also references to the not-filled repetitions of f1.
	 * 
	 * For groups declared as relevant, all descendants (fields and groups) are also assumed relevant (as if these
	 * descendants had also been declared, with the repetitions of path elements below the group set to `0`).
	 * 
	 * Repetitions of entity instances beyond the max repeatability will be ignored.
	 * 
	 * **Note:** 
	 * It is encouraged to set the repetitions of all path elements (including non-repeatable ones) to `0`:
	 * If only a subset of repetitions is specified as relevant, validation errors will be suppressed in cases that can
	 * be unexpected to the user (see description above).
	 * Compared to listing the all instances of a repetition layer separately, this additionally offers performance benefits and does not
	 * prevent some language constructs from being evaluated (like `NumberOfFilledFields`, `NumberOfFilledGroups`, and
	 * in some constellations semantic index and parallel iteration).
	 * 
	 * If you _do_ specify a subset of repetitions, make sure to correctly understand how partial validation will behave in 
	 * this scenario (when in doubt, ask the kernel team).
	 * 
	 * @param valuesOfFieldInstancesToConsider See parameter with the same name in {@link DocumentRtService.validateFull}.
	 * @return A {@link DocumentValidationResult} containing the validation messages.
	 */
	validatePart(document: Document, relevantEntityInstances: EntityInstancePath[], valuesOfFieldInstancesToConsider?: EntityInstanceValueMapping): DocumentValidationResult;

	/**
	 * Computes all fields which can be computed based on the given {@link Document}.
	 * 
	 * Before performing any computations, all non-computed fields in the given {@link Document} that are operands of
	 * any computation will be formally validated:
	 * 1. If a field contains an invalid value, a corresponding error will be reported.
	 * 2. If a required field is empty, no error will be reported.
	 * 3. If an index field is empty or not unique, an error will be reported.
	 * 
	 * The errors reported in this step will be returned in {@link DocumentComputationResult.formalErrorsInOperands}.
	 * 
	 * Regardless of the formal errors found, computations will be performed. Any computations relying directly or
	 * indirectly on invalid fields will be skipped.
	 * 
	 * Computations are performed in an order that ensures that all operands of a computation rule are final (i.e.
	 * computed before). If a computation results in a value that is not valid (e.g. a number value outside the valid
	 * range), the erroneous {@link ComputedFieldInstance} will be included in
	 * {@link DocumentComputationResult.computedFieldInstancesWithErrors} with a corresponding
	 * {@link ComputedFieldInstance.errorMessage}. A field instance will only be computed if its computation does not
	 * depend on such an invalid computed field instance directly or indirectly.
	 * 
	 * {@link DocumentComputationResult.computedFieldInstancesWithoutErrors} will contain all field instances that
	 * could be computed without errors.
	 * 
	 * {@link DocumentComputationResult.clearedFieldInstances} will contain field instances that were part of the
	 * {@link Document} used for computation, but should be understood as unfilled after the computation.
	 *
	 * **Note:** It is recommended to validate the document, e.g. via {@link DocumentRtService.validateFull},
	 * before executing the computation.
	 * 
	 * @param document The document to take operands from.
	 * @return A {@link DocumentComputationResult} containing all computed field instances (valid or invalid) and
	 * errors in operands.
	 */
	compute(document: Document): DocumentComputationResult;

}

/**
 * The result of a document computation (see {@link DocumentRtService.compute}).
 *
 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
 */
export interface DocumentComputationResult {
	/**
	 * Whether or not errors were encountered during computation processing.
	 */
	readonly noErrorOccurred: boolean;
	/**
	 * The {@link ComputedFieldInstance}s without formal errors (i.e. {@link ComputedFieldInstance.errorMessage} is `undefined`).
	 * Does not include cleared field instances (i.e. {@link ComputedFieldInstance.value} is `""` or `null`).
	 */
	readonly computedFieldInstancesWithoutErrors: ComputedFieldInstance[];
	/**
	 * The {@link ComputedFieldInstance}s without formal errors whose value differs from the {@link Document} used for the
	 * computation. Subset of {@link computedFieldInstancesWithoutErrors}.
	*/
	readonly computedFieldInstancesWithChanges: ComputedFieldInstance[];
	/**
	 * The {@link ComputedFieldInstance}s with formal errors (i.e. {@link ComputedFieldInstance.errorMessage} is not `undefined`).
	*/
	readonly computedFieldInstancesWithErrors: ComputedFieldInstance[];
	/**
	 * The {@link ComputedFieldInstance}s for instances of computed fields which were filled in the {@link Document} used for computation, but:
	 * - were explicitly cleared (e.g. in a computation which set the value of the field instance to `""`), or
	 * - were not computed (e.g. because the preconditions did not apply, due to a cascading error, or because there were formal errors in operands).
	 */
	readonly clearedFieldInstances: ComputedFieldInstance[];
	/**
	 * The formal errors caused by operands during formal validation.
	 */
	readonly formalErrorsInOperands: Message[];
	/**
	 * The {@link Document} based on which this {@link DocumentComputationResult} was computed.
	 */
	readonly document: Document;
	/**
	 * Creates a document based on the provided {@link Document} for which the changes in {@link computedFieldInstancesWithChanges} were applied
	 * and the field instances in {@link computedFieldInstancesWithErrors} and {@link clearedFieldInstances} were cleared.
	 * @param document The document which should be used as the basis for creating the updated result document.
	 * @returns A document with the computed values applied.
	 */
	appliedTo(document: Document): Document;
}

/**
 * A single computed value of a field instance.
 *
 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
 */
export interface ComputedFieldInstance {
	/**
	 * The {@link Document} based on which this {@link ComputedFieldInstance} was computed.
	 */
	readonly document: Document;
	/**
	 * The path of the field in the document.
	 */
	readonly path: EntityInstancePath;
	/**
	 * The typed computed value.
	 */
	readonly value: FieldInstanceValue;
	/**
	 * An optional error message if there is a formal error with the computed value.
	 */
	readonly errorMessage: Message | undefined;
	/**
	 * The string value of the computed value.
	 */
	readonly stringValue: string | undefined;

	// in contrast to Java, this interface does not offer the corresponding 'applyTo' method
	// since it is expensive to copy the document each time a computed entity instance should be applied
}

/**
 * The data class for results from a call of {@link DocumentRtService.validateFull} or {@link DocumentRtService.validatePart}.
 *
 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
 */
export interface DocumentValidationResult {
	/**
	 * List of validation messages, either coming from the formal validation or rules.
	 */
	readonly messages: Message[];
	/**
	 * Is true when there are no {@link Message}s with {@link Message.Severity} "ERROR".
	 */
	readonly noErrorOccurred: boolean;
}

/**
 * A path to an entity instance whose repetition indices are only partially known - an extension of
 * {@link EntityInstancePath} that makes the possibility of wildcard (`0`) and unknown (`-5`) repetition
 * indices more explicit.
 *
 * This is typically the path to an entity that is the cause of a validation error. When every {@link index} is
 * `> 0`, the path is fully known and equivalent to an {@link EntityInstancePath}. An {@link index} of `-5` marks an
 * index group whose semantic index could not be resolved; the value that failed to resolve is then carried in
 * {@link semanticIndexValueForUnknownIndexGroup}.
 */
export type PartiallyKnownEntityInstancePath = {
	/**
	 * The name of the model element (group or field) for this path segment, e.g. `"G"`, `"G1"` or `"F2"`.
	 */
	readonly elementName: string;
	/**
	 * The repetition index of this path element. It can be a
	 *
	 * - **number > 0**: the concrete index of the repetition,
	 * - **`0`**: a wildcard that means all repetitions,
	 * - **`-5`**: an index group for which the semantic index could not be resolved, i.e. no repetition with the
	 *   searched semantic index exists (see {@link semanticIndexValueForUnknownIndexGroup}).
	 */
	readonly index: number;
	/**
	 * The semantic index value for which this index group could not be resolved.
	 *
	 * `undefined` if {@link index} is not `-5` (i.e. there is no unresolved index group at this path element), or
	 * if the field value used for the semantic index was not filled. Otherwise, the value for which no matching
	 * repetition of the index group could be found.
	 */
	readonly semanticIndexValueForUnknownIndexGroup?: FieldInstanceValue;
}[];

/**
 * A single message used in validation and computation results.
 *
 * @OnlyForUsage - Should not be implemented. New properties will not be considered breaking.
 */
export interface Message {
	/**
	 * The path to the rule that caused the error or <code>undefined</code> in case of formal errors.
	 */
	readonly rulePath: string | undefined;
	/**
	 * The error code as defined in the validation rule.
	 */
	readonly errorCode: string;
	/**
	 * The erroneous entity instance.
	 */
	readonly entityInstance: PartiallyKnownEntityInstancePath;
	/**
	 * A detailed localizable message that should be displayed to the end user.
	 * The default values for the parameter replacements and the formatted values are returned
	 * unchanged, so in particular no substitutions of html-sensitive characters take place. Localizer can be
	 * implemented accordingly if needed.
	 */
	readonly errorText: Localizable[];
	/**
	 * Whether it is a value error or an omission error.
	 */
	readonly messageType: Message.MessageType;

	/**
	 * Contains all referenced fields that might cause the error.
	 * @Note The type of this collection is an Array due to be able to store it in a Redux Store,
	 * but behaves like a Set, as the order of the elements does not matter.
	 */
	readonly referencedFields: PartiallyKnownEntityInstancePath[];

	/**
	 * Contains all referenced fields that may be responsible for generating omission errors.
	 * @Note The type of this collection is an Array due to be able to store it in a Redux Store,
	 * but behaves like a Set, as the order of the elements does not matter.
	 */
	readonly refOmissionErrorResponsible: PartiallyKnownEntityInstancePath[];

	/**
	 * The entity instances that possibly caused the error, gathered into groups so that they can be prioritized.
	 *
	 * If a filter for possible causes was provided in the corresponding rule, the entity instances determined as a possible
	 * cause for this rule firing are filtered:
	 * - filled entity instances are removed as possible causes if their entity is not enabled as a possible cause when filled
	 * - unfilled entity instances are removed as possible causes if their entity is not enabled as a possible cause when not filled
	 *
	 * The current implementation groups the possible causes as follows:
	 * - For a {@link Message.MessageType} of `"OMISSION_ERROR"` the first group consists of entity instances that are not
	 *   filled. The second group consists of the entity instances that are filled.
	 * - For a {@link Message.MessageType} of `"VALUE_ERROR"` all possibly causing entity instances are returned as a single group.
	 *
	 * Future implementations may create other or more groups. But we always try to follow the rule: entity instances of the
	 * earlier groups should be more probable to cause the error from a technical point of view.
	 *
	 * Note that the returned paths may contain wildcard indices (repetitions with value 0) or unknown indices (repetitions with
	 * value -5).
	 * @Note The inner collections are Arrays in order to be storable in a Redux Store, but each behaves like a Set, as the order
	 * of the elements does not matter.
	 */
	readonly groupsOfPossiblyCausingEntities: PartiallyKnownEntityInstancePath[][];

	/**
	 * Whether it is an error, a hint or an info.
	 */
	readonly severity: Message.Severity;
}

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace Message {
	/**
	 * Provides differentiation of validation errors in value and omission.
	 * An omission error is present, if a rule can be satisfied by filling one or more additional fields.
	 * Otherwise, it is a value error.
	 */
	export type MessageType = "OMISSION_ERROR" | "VALUE_ERROR";
	export type Severity = "ERROR" | "WARNING" | "INFO";
}

/**
 * Interface for custom conditions.
 * 
 * Custom conditions can be used in rule conditions for project specific checks that can not be
 * expressed by the validation language. The implementation of this interface is provided by the
 * project and must be available at execution time of the validation.
 * 
 * <b>Attention:</b> The kernel validation and thus especially custom conditions (as part of it)
 * are designed exclusively for the validation of values supplied in {@link Document}. Checks
 * that are independent of the {@link Document} should be implemented at the appropriate extension
 * point (e.g. in Data Services).
 * 
 * @Note The caller has to provide an (or several) implementation(s) of this interface.
 */
export interface ICustomCondition {
	/**
	 * The validation logic as a callback function.
	 * The method must not change the data or the set of relevant entities.
	 * The method must be thread-safe.
	 *
	 * Non-present field values should be handled appropriately. In particular, when reading field values from the
	 * document via {@link DocumentService#getAssignedObject}, the returned value may be `undefined`, `null`
	 * or `""` when a field instance is not filled.
	 *
	 * @param document The document that was passed to the validation call (see {@link DocumentRtService#validateFull}
	 * and {@link DocumentRtService#validatePart}).
	 * <br><br>
	 * Please note that the {@link EntityInstanceValueMapping} passed to the validation call is not reflected
	 * in the document, and instead provided as the parameter <code>valuesOfFieldInstancesToConsider</code>.
	 * Implementations are responsible for respecting the overrides made through the {@link EntityInstanceValueMapping}.
	 *
	 * @param documentModelId The id of the {@link Document}'s model.
	 * @param relevantEntityInstances All relevant entity instances for this check.
	 *   <ul>
	 *     <li>full validation: <code>undefined</code> (all entity instances are relevant)</li>
	 *     <li>partial validation: the relevant entity instances that were passed to the validation call 
	 *         (see {@link DocumentRtService#validatePart})</li>
	 *   </ul>
	 * @param formallyIncorrectEntityInstances The entity instances that are formally incorrect.
	 *   In kernel runtime formal incorrect entity instances are not relevant for further checks
	 *   to avoid additional errors. Therefore formal incorrect entity instances should be ignored
	 *   by the custom condition check.
	 * @param errorEntityInstance The error entity instance representing the error field of the
	 *   rule whose rule condition specifies this custom condition.
	 *   <br><br>
	 *   Remark about indices of {@link EntityInstancePath}:
	 *     <ul>
	 *       <li>used in an iteration: the indices specify concrete values.
	 *         The rule and therefore the check method is evaluated for each repetition
	 *         and contains the concrete index of the current repetition.</li>
	 *       <li>used in a parallel iteration: an index value may, in special
	 *         cases be {@link IIdentifier.UNKNOWN}. In these cases, a concrete index
	 *         for this layer of the error field cannot be assigned.</li>
	 *     </ul>
	 * @param valuesOfFieldInstancesToConsider The {@link EntityInstanceValueMapping} that was passed to the
	 *    validation call, or <code>undefined</code> if none was provided (see {@link DocumentRtService#validateFull}
	 * 	  and {@link DocumentRtService#validatePart}).
	 * @return <code>true</code>, if the condition was met, <code>false</code>, if
	 *   <ul>
	 *     <li>one or more necessary fields are <em>not</em> included in relevantEntityInstances</li>
	 *     <li>one or more necessary fields are included in formallyIncorrectEntityInstances</li>
	 *     <li>the condition is not met</li>
	 *   </ul>
	 * 
	 * @Note Conditions describe the error case, i.e. if <code>true</code> is returned here, the rule in
	 *   which this custom condition was specified reports an error.
	 */
	check(
		document: Document,
		documentModelId: string,
		relevantEntityInstances: EntityInstancePath[] | undefined,
		formallyIncorrectEntityInstances: EntityInstancePath[],
		errorEntityInstance: EntityInstancePath,
		valuesOfFieldInstancesToConsider: EntityInstanceValueMapping | undefined
	): boolean;
}

/**
 * Common interface for custom condition factories.
 *
 * Note: Implementing classes must guarantee thread-safety of all methods.
 */
export interface ICustomConditionFactory {
	/**
	 * This method is used to create new instances of the custom conditions which are provided by this factory.
	 *
	 * @return A new instance of the custom condition specified by <code>customConditionName</code> or
	 *         <code>null</code> if the factory does not support this custom field type.
	 */
	createCustomCondition(customConditionName: string): ICustomCondition | null;
}

/**
 * A factory method for the simplest {@link ICustomConditionFactory} where a key statically maps to a custom condition.
 * 
 * @param obj an object whose keys are custom condition names and whose values are the custom condition to use for that respective name
 * @returns a custom condition factory based on the provided object
 */
export function createCustomConditionFactory(obj: { readonly [key: string]: ICustomCondition }): ICustomConditionFactory {
	return {
		createCustomCondition: customConditionName => {
			return obj[customConditionName];
		}
	}
}

