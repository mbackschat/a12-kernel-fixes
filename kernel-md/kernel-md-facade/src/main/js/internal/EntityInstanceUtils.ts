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

import type { DataFormats, SupportedType } from "@com.mgmtp.a12.utils/utils-localization";

import type { IData, IIdentifier, IMetaField, IMetaGroup, IMetaModel, IResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { IMetaKeys } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { ArrayUtils } from "@com.mgmtp.a12.kernel/kernel-core-runtime-ts/internal";

import type { ICustomFieldTypeFactory } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import type { Document, DocumentModel, EntityInstancePath, FieldInstanceValue, GroupInstance } from "../api.js";

import { EntityInstanceConverter } from "./EntityInstanceConverter.js";
import { ValidationData } from "./dm-runtime-service/ValidationData.js";


export type EntityInstance =
	| readonly GroupInstance[]
	| Readonly<GroupInstance>
	| FieldInstanceValue
	| undefined;
export type EntityInstanceRW = GroupInstance | GroupInstance[] | FieldInstanceValue | undefined;
export type EntityInstancePathPart = {
	readonly elementName: string;
	readonly index: number;
};

interface UpdateTreeNode {
	[shortName: string]: { [repetition: number]: UpdateTreeNode } | FieldInstanceValue;
}

export class EntityInstanceUtils {
	public static updateEntityInstance(
		current: EntityInstance,
		path: EntityInstancePath,
		update: EntityInstance,
		modelElement: DocumentModel.Element
	): EntityInstance {
		if (path.length === 0) {
			// end of recursion
			return update;
		}
		// recurse
		// first some asserts and hints to the type-checker
		if (
			modelElement.type !== "Group" ||
			(!EntityInstanceUtils.isNonRepeatableGroupInstance(current) && current !== undefined)
		) {
			throw Error(
				"(name, index) index can only be applied from a non-repeatable group, " +
				`but parent type is '${typeof current}'`
			);
		}
		const [first, ...rest] = path;
		const childModelElement = EntityInstanceUtils.findChildModelElement(
			modelElement,
			first.elementName
		);
		if (childModelElement === undefined) {
			throw Error(`Element '${first.elementName}' was not found in the given document model.`);
		}
		if (first.index !== 1 && !EntityInstanceUtils.isRepeatableGroup(childModelElement)) {
			throw Error(
				`Element '${first.elementName}' is not a repeatable group, ` +
				`therefore the index ${first.index} can't be applied to it.`
			);
		}
		const childEntityInstance = EntityInstanceUtils.getAssignedObject(current, [first]);
		const childUpdated = EntityInstanceUtils.updateEntityInstance(
			childEntityInstance,
			rest,
			update,
			childModelElement
		);
		const documentWithParentForChild =
			EntityInstanceUtils.createParentGroupInstanceForChildIfMissing(
				current,
				first.elementName,
				childModelElement
			);
		return childUpdated !== childEntityInstance
			? EntityInstanceUtils.setEntityInstanceValue(
				documentWithParentForChild,
				first,
				childUpdated,
				childModelElement
			)
			: current;
	}

	/**
	 * Returns a document with the given field value updates applied.
	 * This may just be the given document if all of the updates are
	 * - deletions of field instances that are already empty, or
	 * - updates of field instances whose new value is strictly equal to their previous value.
	 * 
	 * Throws an error if:
	 * - `checkDocumentCompatibility` is true, and the provided document is not compatible with the provided updates (see {@link checkUpdateTreeForComputationsIsCompatibleWithDocument})
	 * - there are multiple updates for the same field instance (i.e. path and indices are the same)
	 * - there is an update for an entity instance that is an ancestor of another update's field instance (i.e. an element is referenced as both a field and a group)
	 * 
	 * @param document the document to apply the updates to. Will not get modified.
	 * @param fieldValueUpdates a list of field value updates where the field instance in the document at given `path` should be set to the associated `value`
	 * @param checkDocumentCompatibility if `false`, the provided document is not checked for compatibility with the provided updates
	 * @returns the updated document
	 */
	public static createUpdatedDocumentForComputation(document: Document, fieldValueUpdates: { path: EntityInstancePath, value: FieldInstanceValue }[], checkDocumentCompatibility = true): Document {
		const updateTree = EntityInstanceUtils.createUpdateTree(fieldValueUpdates);
		if (checkDocumentCompatibility) {
			EntityInstanceUtils.checkUpdateTreeForComputationsIsCompatibleWithDocument(document, updateTree);
		}
		return <Document>EntityInstanceUtils.updateGroupInstance(document, updateTree);
	}

	/**
	 * Returns a document for the given {@link IData}.
	 * Empty field instances will not be included in the document.
	 * 
	 * @param data {@link IData} to be converted to a {@link Document}
	 * @param metaModel the corresponding meta model for the Document
	 * @returns the updated document
	 */
	public static createDocumentFromData(data: IData, metaModel: IMetaModel, result: IResult, df?: DataFormats, customFieldTypeFactory?: ICustomFieldTypeFactory): Document {
		const it = data.iterator();
		const fieldValueUpdates: { path: EntityInstancePath, value: FieldInstanceValue }[] = [];
		const groupInstancePaths: EntityInstancePath[] = [];
		while (it.hasNext()) {
			// we know for sure that there is a next element
			// eslint-disable-next-line @typescript-eslint/no-non-null-assertion
			const id = it.next()!;
			const ei: EntityInstancePath = EntityInstanceConverter.convertToEntityInstance(id);

			const metaField: IMetaField = metaModel.getValue(IMetaKeys.MODEL_META_FIELD, id.getName());
			if (metaField != undefined) {
				const value = EntityInstanceUtils._getValue(data, id, metaField, result, df, customFieldTypeFactory);
				if (value != undefined) {
					fieldValueUpdates.push({ path: ei, value: value });
				}
			}
			const metaGroup: IMetaGroup = metaModel.getValue(IMetaKeys.MODEL_META_GROUP, id.getName());
			if (metaGroup != undefined) {
				groupInstancePaths.push(ei);
			}
		}
		const docWithEmptyGroups = EntityInstanceUtils.createDocumentWithEmptyGroups(metaModel, groupInstancePaths);
		return EntityInstanceUtils.createUpdatedDocumentForComputation(<Document>docWithEmptyGroups, fieldValueUpdates, false);
	}

	private static _getValue(data: IData, id: IIdentifier, metaField: IMetaField, result: IResult, df?: DataFormats, customFieldTypeFactory?: ICustomFieldTypeFactory): FieldInstanceValue | undefined {
		if (data instanceof ValidationData) {
			return data.getTypedValue(id);
		}
		const strValue = data.getValue(id);
		const dataFormats = df ? df : null;
		return strValue == undefined ? undefined : metaField.convertWithOnlyBasicTypeCheck(strValue, dataFormats, result, customFieldTypeFactory);
	}

	/**
	 * @param metaModel from the corresponding document model.
	 * @param groupInstancePaths only group instance paths.
	 * @returns a document with the given group instance paths.
	 */
	public static createDocumentWithEmptyGroups(metaModel: IMetaModel, groupInstancePaths: EntityInstancePath[]): { [key: string]: object } {
		const docWithEmptyGroups: { [key: string]: object } = {};
		for (const ei of groupInstancePaths) {
			const currentGroupInstance = docWithEmptyGroups;
			for (let i = 0; i < ei.length; i++) {
				const currentEip = ei.slice(0, i + 1);
				const currentElementName = ei[i].elementName;
				const isRepeatable = EntityInstanceUtils._isRepeatable(metaModel, currentEip);
				const child = EntityInstanceUtils._getChild(currentGroupInstance, currentElementName, isRepeatable);
				if (isRepeatable) {
					for (let j = 0; j < ei[i].index; j++) {
						(child[j as keyof object] as any) ??= {};
					}
				}
			}
		}
		return docWithEmptyGroups;
	}

	private static _getChild(groupInstance: { [key: string]: object }, elementName: string, isRepeatable: boolean): any {
		let child = groupInstance[elementName];
		if (child == undefined) {
			if (isRepeatable) {
				child = groupInstance[elementName] = [];
			} else {
				child = (groupInstance[elementName] as any) = {};
			}
		}
		return child;
	}

	private static _isRepeatable(metaModel: IMetaModel, ei: EntityInstancePath): boolean {
		const path = EntityInstanceConverter.combinePath(ei);
		const metaGroup: IMetaGroup = metaModel.getValue(IMetaKeys.MODEL_META_GROUP, path);
		const repeatability: number = metaGroup.getValue(IMetaKeys.GROUP_MAX_INDEX);
		return repeatability > 1;
	}

	/**
	 * Checks that the provided document follows the expectations made by the updates produced from a computation result:
	 * - non-deletion updates for group instances beyond repetition 1 must already exist in the document
	 * - updates for group instances must not be field instances in the document
	 * - updates for field instances must not be group instances in the document
	 */
	private static checkUpdateTreeForComputationsIsCompatibleWithDocument(document: Document, updateTree: UpdateTreeNode): void {
		EntityInstanceUtils.checkUpdateTreeForComputationsIsCompatibleWithGroupInstance(document, updateTree, []);
	}

	private static checkUpdateTreeForComputationsIsCompatibleWithGroupInstance(groupInstance: GroupInstance | undefined, updateTree: UpdateTreeNode, path: EntityInstancePath): boolean {
		let allUpdatesAreClears = true;
		for (const [shortNameOfChildToUpdate, updatesForChild] of Object.entries(updateTree)) {
			const child = groupInstance === undefined ? undefined : groupInstance[shortNameOfChildToUpdate];
			if (EntityInstanceUtils.isFieldInstance(updatesForChild)) {
				const childPath = path.concat({ elementName: shortNameOfChildToUpdate, index: 1 });
				if (EntityInstanceUtils.isRepeatableGroupInstance(child) || EntityInstanceUtils.isNonRepeatableGroupInstance(child)) {
					throw new Error(`Provided document is not compatible with computation result: ${EntityInstanceConverter.combineInstancePath(childPath)} is a group but expected to be a field instance.`);
				}
				allUpdatesAreClears &&= EntityInstanceUtils.isEmpty(updatesForChild);
			} else {
				if (EntityInstanceUtils.isFieldInstance(child)) {
					const childPath = path.concat({ elementName: shortNameOfChildToUpdate, index: 1 });
					throw new Error(`Provided document is not compatible with computation result: ${EntityInstanceConverter.combineInstancePath(childPath)} is a field but expected to be a group instance.`);
				}
				for (const [repetitionAsString, updatesForRepetition] of Object.entries(updatesForChild)) {
					const repetition = parseInt(repetitionAsString);
					const childPath = path.concat({ elementName: shortNameOfChildToUpdate, index: repetition });
					const childGroupInstance = EntityInstanceUtils.isRepeatableGroupInstance(child) ? child[repetition - 1] : child;
					const allUpdatesForRepetitionAreClears = EntityInstanceUtils.checkUpdateTreeForComputationsIsCompatibleWithGroupInstance(childGroupInstance, updatesForRepetition, childPath);
					if (childGroupInstance === undefined && !allUpdatesForRepetitionAreClears && repetition > 1) {
						throw new Error(`Provided document is not compatible with computation result: The group instance ${EntityInstanceConverter.combineInstancePath(childPath)} is expected to already exist.`);
					}
					allUpdatesAreClears &&= allUpdatesForRepetitionAreClears;
				}
			}
		}
		return allUpdatesAreClears;
	}

	/**
	 * Consolidate the given pairs of `EntityInstancePath` and `FieldInstanceValue` into a tree of updates that groups updates to the same group instance into the same
	 * node of the resulting update tree.
	 */
	public static createUpdateTree(fieldValueUpdates: { path: EntityInstancePath, value: FieldInstanceValue }[]): UpdateTreeNode {
		const updates: UpdateTreeNode = {};
		for (const { path, value } of fieldValueUpdates) {
			let currentUpdateNode = updates;
			for (let level = 0; level < path.length; level++) {
				const isLastPathElement = (level === path.length - 1);
				const { elementName, index } = path[level];
				if (isLastPathElement) {
					if (currentUpdateNode[elementName] !== undefined) {
						if (!EntityInstanceUtils.isFieldInstance(currentUpdateNode[elementName])) {
							throw new Error(`Conflicting updates for entity instance ${EntityInstanceConverter.combineInstancePath(path.slice(0, level + 1))}: Referenced as both field and group.`);
						} else {
							throw new Error(`Conflicting updates for entity instance ${EntityInstanceConverter.combineInstancePath(path.slice(0, level + 1))}: Multiple values specified`);
						}
					}
					currentUpdateNode[elementName] = value;
				} else {
					const childUpdates = currentUpdateNode[elementName] ??= {};
					if (EntityInstanceUtils.isFieldInstance(childUpdates)) {
						throw new Error(`Conflicting updates for entity instance ${EntityInstanceConverter.combineInstancePath(path.slice(0, level + 1))}: Referenced as both field and group.`);
					}
					currentUpdateNode = childUpdates[index] ??= {};
				}
			}
		}
		return updates;
	}

	/**
	 * Creates an updated group instance out of a baseline group instance and a tree of updates for that group instance.
	 * @param groupInstance the group instance to use as a baseline. May be `undefined` if there is no baseline. Will not get modified.
	 * @param updates a tree of updates for the baseline group instance and its descendants. 
	 * @returns a new group instance with the given updates applied, or the baseline group instance if all of the updates are
	 * - deletions of field instances that are already empty, or
	 * - updates of field instances whose new value is strictly equal to their previous value.
	 */
	private static updateGroupInstance(groupInstance: GroupInstance | undefined, updates: UpdateTreeNode): GroupInstance | undefined {
		let groupInstanceCopy: GroupInstance | undefined;

		function setChild(shortNameOfChildToUpdate: string, entityInstance: EntityInstance): void {
			if (groupInstanceCopy === undefined) {
				groupInstanceCopy = groupInstance === undefined ? {} : { ...groupInstance };
			}
			(groupInstanceCopy[shortNameOfChildToUpdate as keyof GroupInstance] as any) = entityInstance;
		}

		for (const [shortNameOfChildToUpdate, updatesForChild] of Object.entries(updates)) {
			const child = groupInstance === undefined ? undefined : groupInstance[shortNameOfChildToUpdate as keyof GroupInstance];
			if (EntityInstanceUtils.isFieldInstance(updatesForChild)) {
				if (!(updatesForChild === null && (child == undefined || child === "")) && child !== updatesForChild) {
					setChild(shortNameOfChildToUpdate, updatesForChild);
				}
			} else {
				const updatedChild = EntityInstanceUtils.updateRepetitions(child, updatesForChild);
				if (child !== updatedChild) {
					setChild(shortNameOfChildToUpdate, updatedChild);
				}
			}
		}
		return groupInstanceCopy ?? groupInstance;
	}

	private static updateRepetitions(repetitions: EntityInstance, updatesPerRepetition: { [repetition: number]: UpdateTreeNode }): EntityInstance {
		const isNonRepeatable = EntityInstanceUtils.isNonRepeatableGroupInstance(repetitions);
		const isRepeatable = EntityInstanceUtils.isRepeatableGroupInstance(repetitions);
		let repetitionsCopy: GroupInstance[] | undefined;

		function setGroupInstanceAtRepetition(oneBasedRepetition: number, entityInstance: EntityInstance): void {
			if (repetitionsCopy === undefined) {
				if (isRepeatable) {
					repetitionsCopy = repetitions.slice();
				} else if (isNonRepeatable) {
					repetitionsCopy = [repetitions];
				} else {
					repetitionsCopy = [];
				}
			}
			(repetitionsCopy[oneBasedRepetition - 1] as any) = entityInstance;
		}

		function getGroupInstanceAtRepetition(oneBasedRepetition: number): GroupInstance | undefined {
			if (isRepeatable) {
				return repetitions[oneBasedRepetition - 1];
			} else if (oneBasedRepetition === 1 && isNonRepeatable) {
				return repetitions;
			} else {
				return undefined;
			}
		}

		for (const [repetitionAsString, updatesForRepetition] of Object.entries(updatesPerRepetition)) {
			const repetition = parseInt(repetitionAsString);
			const groupInstance = getGroupInstanceAtRepetition(repetition);
			const updatedGroupInstance = EntityInstanceUtils.updateGroupInstance(groupInstance, updatesForRepetition);
			if (groupInstance !== updatedGroupInstance) {
				setGroupInstanceAtRepetition(repetition, updatedGroupInstance);
			}
		}

		if (repetitionsCopy !== undefined) {
			return !isRepeatable && repetitionsCopy.length === 1
				? repetitionsCopy[0] // unpack again if the original was also unpacked
				: repetitionsCopy;
		} else {
			return repetitions;
		}
	}


	public static buildNewPath(
		curPath: EntityInstancePath,
		pathPart?: EntityInstancePathPart
	): EntityInstancePath {
		if (pathPart !== undefined) {
			return [...curPath, pathPart];
		} else {
			return curPath;
		}
	}

	public static getAssignedObject(
		current: EntityInstance,
		path: EntityInstancePath
	): EntityInstance {
		if (path.length === 0 || current === undefined) {
			return current;
		}
		const [first, ...rest] = path;
		if (!EntityInstanceUtils.isNonRepeatableGroupInstance(current)) {
			throw Error(
				"Cannot index into non-GroupInstance element (next remaining path element: " +
				`('${first.elementName}', '${first.index}'))`
			);
		}
		const childEntityInstance = EntityInstanceUtils.getChildEntityInstance(current, first);
		return EntityInstanceUtils.getAssignedObject(childEntityInstance, rest);
	}

	public static getChildEntityInstance(
		groupInstance: Readonly<GroupInstance>,
		{ elementName, index }: EntityInstancePathPart,
		isIndexZeroForNonRepeatableAllowed?: boolean
	): EntityInstance {
		const child = groupInstance[elementName];
		if (child === undefined) {
			// if child is undefined, we can't do a consistency check regarding the
			// type of child (repeatable or not) and the value of index, so we don't.
			// no matter what index is, the value that must be returned is undefined
			return undefined;
		} else if (EntityInstanceUtils.isRepeatableGroupInstance(child)) {
			return index === 0 ? child : child[index - 1];
		} else {
			if (index === 0 && isIndexZeroForNonRepeatableAllowed) {
				return child;
			} else if (index !== 1) {
				throw Error(
					`Child with name '${elementName}' is not a repeatable group, ` +
					`but index !== 1 ('${index}') was requested`
				);
			} else {
				return child;
			}
		}
	}

	/**
	 * if the replaced child equals (===) the new value, the original group is returned unmodified
	 */
	public static setEntityInstanceValue(
		groupInstance: Readonly<GroupInstance>,
		pathPart: EntityInstancePathPart,
		value: EntityInstance,
		modelElement: DocumentModel.Element // corresponding to the entity instance for which the value should be set
	): Readonly<GroupInstance> {
		if (EntityInstanceUtils.getChildEntityInstance(groupInstance, pathPart) === value) {
			return groupInstance;
		}
		if (modelElement.type === "Field") {
			if (!EntityInstanceUtils.isFieldInstance(value) && value !== undefined) {
				throw Error(
					`Expected field or undefined, but got '${Array.isArray(value) ? "array" : typeof value
					}'` + ` for insertion at '${pathPart.elementName}'`
				);
			}
			return EntityInstanceUtils.setOrDelete(groupInstance, pathPart.elementName, value);
		} else if (EntityInstanceUtils.isNonRepeatableGroup(modelElement)) {
			if (!EntityInstanceUtils.isNonRepeatableGroupInstance(value) && value !== undefined) {
				throw Error(
					"Expected GroupInstance or undefined, but got " +
					`'${Array.isArray(value) ? "array" : typeof value}'` +
					` for insertion at '${pathPart.elementName}'`
				);
			}
			return EntityInstanceUtils.setOrDelete(groupInstance, pathPart.elementName, value);
		} else if (EntityInstanceUtils.isRepeatableGroup(modelElement)) {
			if (pathPart.index !== 0) {
				return EntityInstanceUtils.setIndexedRepeatableGroupValue(pathPart, groupInstance, value);
			} else {
				if (!EntityInstanceUtils.isRepeatableGroupInstance(value) && value !== undefined) {
					throw Error(
						`Expected GroupInstance[] but got '${typeof value}' for ` +
						`insertion at ('${pathPart.elementName}', '${pathPart.index}')`
					);
				}
				return EntityInstanceUtils.setOrDelete(groupInstance, pathPart.elementName, value);
			}
		} else {
			throw Error("Unexpected combination of types");
		}
	}

	public static setIndexedRepeatableGroupValue(
		pathPart: EntityInstancePathPart,
		groupInstance: Readonly<GroupInstance>,
		value: EntityInstance
	) {
		if (pathPart.index < 0) {
			throw Error(
				"Illegal argument: index < 0 is invalid. " +
				`Attempted to insert at ['${pathPart.elementName}', '${pathPart.index}']`
			);
		}
		const groupShallowCopy = { ...groupInstance };
		const repsOrig = groupShallowCopy[pathPart.elementName];
		if (repsOrig === undefined || !Array.isArray(repsOrig)) {
			throw Error(
				`The entry at '${pathPart.elementName}' is expected to be ` +
				`a repeatable group or undefined, but the existing entry was of type '${typeof repsOrig}'. ` +
				`Setting a new entry	at ('${pathPart.elementName}', '${pathPart.index}') is not possible ` +
				`without first discarding the existing unexpected entry at '${pathPart.elementName}'`
			);
		}
		const repsShallowCopy = [...((repsOrig as any[]) || [])];
		if (value == undefined) {
			// repeatable groups are supposed to not contain empty slots,
			// therefore the rest of the array must be shifted
			// one position to the left, see comment at MGM Wiki Shortlink x/35tkCw
			repsShallowCopy.splice(pathPart.index - 1, 1);
		} else {
			if (!EntityInstanceUtils.isNonRepeatableGroupInstance(value)) {
				throw Error(
					`Expected GroupInstance but got '${typeof value}' for insertion at ` +
					`'${pathPart.elementName}'`
				);
			}
			if (repsShallowCopy.length < pathPart.index - 1) {
				throw Error(
					"Index out of bounds: Attempted to insert at " +
					`['${pathPart.elementName}', '${pathPart.index}'] (index '${pathPart.index - 1}')` +
					` but existing group instance has length '${repsShallowCopy.length}'. ` +
					"Repeatable groups are not allowed to be sparse."
				);
			}
			repsShallowCopy[pathPart.index - 1] = value;
		}
		groupShallowCopy[pathPart.elementName] = repsShallowCopy;
		return groupShallowCopy;
	}

	public static setOrDelete(
		groupInstance: GroupInstance,
		entityName: string,
		entity: EntityInstance
	): GroupInstance {
		const groupShallowCopy = { ...groupInstance };
		if (entity !== undefined) {
			groupShallowCopy[entityName] = entity;
		} else {
			// eslint-disable-next-line @typescript-eslint/no-dynamic-delete
			delete groupShallowCopy[entityName];
		}
		return groupShallowCopy;
	}

	public static isNonRepeatableGroup(modelElement: DocumentModel.Element): boolean {
		return modelElement.type === "Group" && modelElement.repeatability === 1;
	}

	public static isRepeatableGroup(modelElement: DocumentModel.Element): boolean {
		return modelElement.type === "Group" && modelElement.repeatability > 1;
	}

	public static isFieldInstance(
		entityInstance: EntityInstance
	): entityInstance is FieldInstanceValue {
		return (
			["string", "number", "boolean"].some(t => typeof entityInstance === t) ||
			entityInstance === null ||
			entityInstance instanceof Date ||
			EntityInstanceUtils.isArrayOfTwoDates(entityInstance)
		);
	}

	public static isRepeatableGroupInstance(
		entityInstance: EntityInstance
	): entityInstance is GroupInstance[] | readonly GroupInstance[] {
		return Array.isArray(entityInstance) && !EntityInstanceUtils.isArrayOfTwoDates(entityInstance);
	}

	public static isNonRepeatableGroupInstance(
		entityInstance: EntityInstance
	): entityInstance is GroupInstance | Readonly<GroupInstance> {
		return (
			entityInstance !== undefined &&
			!EntityInstanceUtils.isFieldInstance(entityInstance) &&
			!EntityInstanceUtils.isRepeatableGroupInstance(entityInstance) &&
			typeof entityInstance === "object"
		);
	}

	/**
	 * This method takes a group @param document that may be undefined or have
	 * missing container-arrays for repeatable-group children, and returns
	 * a non-undefined group, with an empty repeatable-group container-array
	 * added for the child element specified by @param childModel and @param index,
	 * if that container array was previously missing (undefined).
	 * Apart from these modifications, the returned document will share all entries
	 * of the input @param document.
	 *
	 * If no changes were made, the same object instances as @param document is returned
	 */
	public static createParentGroupInstanceForChildIfMissing(
		parent: Readonly<GroupInstance> | undefined,
		childEntityName: string,
		childModelElement: DocumentModel.Element
	): Readonly<GroupInstance> {
		if (parent === undefined) {
			parent = {};
		}
		if (EntityInstanceUtils.isRepeatableGroup(childModelElement)) {
			if (parent[childEntityName] === undefined) {
				parent = { ...parent, [childEntityName]: [] };
			} else if (!Array.isArray(parent[childEntityName])) {
				throw Error(
					`Model element ${childEntityName} is a repeatable group ` +
					"but the corresponding entity instance in the document is not an array."
				);
			}
		}
		return parent;
	}

	public static shallowCopy(e: EntityInstance): EntityInstanceRW {
		if (EntityInstanceUtils.isRepeatableGroupInstance(e)) {
			return [...e];
		} else if (EntityInstanceUtils.isNonRepeatableGroupInstance(e)) {
			return { ...e };
		} else {
			return e;
		}
	}

	public static isArrayOfTwoDates(value: unknown): value is Date[] {
		return (
			Array.isArray(value) &&
			value.length === 2 &&
			value.every(item => item && item instanceof Date)
		);
	}

	public static findChildModelElement(
		dm: DocumentModel.Group,
		name: string
	): DocumentModel.Element | undefined {
		return dm.elements.find(child => child.name === name);
	}

	public static equalIIdentifier(path: IIdentifier, other: IIdentifier): boolean {
		return path.getName() === other.getName() && ArrayUtils.sindGleichAbPosition(path.getIndices(), other.getIndices(), 0);
	}

	public static isEmpty(value: SupportedType): value is (null | "") {
		return value === null || value === "";
	}
}