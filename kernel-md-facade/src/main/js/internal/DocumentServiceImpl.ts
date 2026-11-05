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
import {
	DateUtil,
	TimeZonedDate,
	ValidationDateParser
} from "@com.mgmtp.a12.utils/utils-localization/a12internal";


import type {
	DocumentModel, DocumentService, EntityInstancePath,
	FieldInstanceValue,
	GroupInstance
} from "../api.js";

import { EntityInstanceUtils } from "./EntityInstanceUtils.js";


type DocGroup = Array<Record<string, unknown>> | Record<string, unknown>;

type FieldVisitor = (
	docElement: unknown,
	modelElement: DocumentModel.Field,
	docParent: Record<string, unknown>
) => boolean;

/**
 * Visit a group. Is called for nonrepeatable groups with docParent, for each group of a repeatable group array without docParent,
 * and for a repeatable group array with docParent.
 *
 * @param docGroup The group in the document
 * @param modelGroup The group in the document model
 */
type GroupVisitor = (
	docGroup: DocGroup,
	modelGroup: DocumentModel.Group
) => {
	changed: boolean;
	skip: boolean;
};

export class DocumentServiceImpl implements DocumentService {
	public static readonly DEFAULT_DATE_FORMAT: string = "yyyy-MM-dd";
	public static readonly DEFAULT_BASE_YEAR = 2000;

	removeTransientFields(document: object, model: DocumentModel): object {
		const elementsToRemove: { parent: Record<string, unknown>; elementName: string }[] = [];
		const result = this.traverseGroupInParallel(
			document as Record<string, unknown>,
			model.content.modelRoot,
			(_docElement, modelElement: DocumentModel.Field, docParent) => {
				if (modelElement.transient) {
					elementsToRemove.push({
						parent: docParent,
						elementName: modelElement.name
					});
					return true;
				}
				return false;
			}
		).result;
		// eslint-disable-next-line @typescript-eslint/no-dynamic-delete
		elementsToRemove.forEach(({ parent, elementName }) => delete parent[elementName]);
		return result;
	}

	addTransientFields(document: object, model: DocumentModel): object {
		const elementsToAdd: { parent: Record<string, unknown>; elementName: string }[] = [];
		const result = this.traverseGroupInParallel(
			document as Record<string, unknown>,
			model.content.modelRoot,
			undefined,
			(group, modelGroup) => {
				let changed = false;
				if (group == undefined || Array.isArray(group)) {
					return { changed, skip: false };
				}
				modelGroup.elements
					.filter(f => f.type === "Field" && f.transient)
					.filter(f => group[f.name] == undefined)
					.forEach(f => {
						elementsToAdd.push({ parent: group, elementName: f.name });
						changed = true;
					});
				return { changed, skip: false };
			}
		).result;
		elementsToAdd.forEach(({ parent, elementName }) => (parent[elementName] = null));
		return result;
	}

	removeEmptyFieldsAndGroups(
		document: Record<string, unknown>,
		model: DocumentModel
	): Record<string, unknown> {
		const result = this.traverseGroupInParallel(
			document,
			model.content.modelRoot,
			(value, modelField, docParent) => {
				if (value === null || value === undefined || value === "") {
					// eslint-disable-next-line @typescript-eslint/no-dynamic-delete
					delete docParent[modelField.name];
					return true;
				}
				return false;
			},
			group => {
				const skip = !group || !Object.keys(group).length;
				return { changed: false, skip };
			}
		);
		return result.result;
	}

	parseDates(document: object, model: DocumentModel): object {
		return this.traverseGroupInParallel(
			document as Record<string, unknown>,
			model.content.modelRoot,
			this.getParseDateVisitor(this._getBaseYearOrDefault(model), this._getTimeZone(model))
		).result;
	}

	private getParseDateVisitor(baseYearOrDefault: number, timeZone: string): FieldVisitor {
		return (v, f, p) => this.parseDateVisitor(v, f, p, baseYearOrDefault, timeZone);
	}

	private parseDateVisitor(
		value: unknown,
		modelField: DocumentModel.Field,
		docParent: Record<string, unknown>,
		baseYearOrDefault: number,
		timeZone: string
	): boolean {
		if (!value || typeof value !== "string") {
			// if we have no string value, we need no conversion
			return false;
		}
		if (modelField.fieldType.type === "DateTimeType") {
			docParent[modelField.name] = TimeZonedDate.createFromString(
				value,
				"yyyy-MM-dd'T'HH:mm:ss",
				timeZone
			).toDate();
		} else if (modelField.fieldType.type === "TimeType") {
			docParent[modelField.name] = TimeZonedDate.createFromString(
				"1970-01-01T" + value,
				"yyyy-MM-dd'T'HH:mm:ss",
				timeZone
			).toDate();
		} else if (modelField.fieldType.type === "DateType") {
			let dateValue;
			try {
				dateValue = DateUtil.createDateIfPossible(
					value,
					modelField.fieldType.format,
					baseYearOrDefault,
					modelField.fieldType.datePrecision !== "FULL",
					timeZone
				);
			} catch (_error) {
				// if something went wrong while parsing the date, we leave the value as it was
				return false;
			}
			if (typeof dateValue === "string") {
				// this is for sure a partially known date, we leave the value as it was
				return false;
			}
			docParent[modelField.name] = dateValue;
		} else if (modelField.fieldType.type === "DateRangeType") {
			docParent[modelField.name] = DateUtil.createDateRange(
				value,
				modelField.fieldType.rangeSeparator,
				modelField.fieldType.format,
				modelField.fieldType.interpretationOfYear,
				baseYearOrDefault,
				timeZone
			);
		} else if (modelField.fieldType.type === "DateFragmentType") {
			let dateValue;
			try {
				dateValue = DateUtil.createDateIfPossible(
					value,
					modelField.fieldType.formatOfFragment,
					baseYearOrDefault,
					false,
					timeZone
				);
			} catch (_error) {
				// if something went wrong while parsing the date, we leave the value as it was
				return false;
			}
			docParent[modelField.name] = dateValue;
		} else {
			// Only convert date/ date range/ date fragment types
			return false;
		}
		return true;
	}

	formatDates(document: object, model: DocumentModel): object {
		return this.traverseGroupInParallel(
			document as Record<string, unknown>,
			model.content.modelRoot,
			this.getFormatDateVisitor(this._getTimeZone(model))
		).result;
	}

	private _getBaseYearOrDefault(model: DocumentModel): number {
		const modelInfo = model.content.modelInfo;
		return modelInfo.baseYear ? modelInfo.baseYear : DocumentServiceImpl.DEFAULT_BASE_YEAR;
	}

	private _getTimeZone(model: DocumentModel): string {
		return model.content.modelConfig.timeZone;
	}

	private getFormatDateVisitor(timeZone: string): FieldVisitor {
		return (v, f, p) => this.formatDateVisitor(v, f, p, timeZone);
	}

	private formatDateVisitor(
		value: unknown,
		modelField: DocumentModel.Field,
		docParent: Record<string, unknown>,
		timeZone: string
	): boolean {
		if (!value) {
			// if we have no value, we need no conversion
			return false;
		}

		if (!(value instanceof Date) && !EntityInstanceUtils.isArrayOfTwoDates(value)) {
			// if we have no value, we need no conversion
			return false;
		}

		if (value instanceof Date) {

			switch (modelField.fieldType.type) {
				case "DateTimeType":
					docParent[modelField.name] = ValidationDateParser.formatDate(
						value,
						"yyyy-MM-dd'T'HH:mm:ss",
						timeZone
					);
					return true;
				case "TimeType":
					docParent[modelField.name] = ValidationDateParser.formatDate(value, "HH:mm:ss", timeZone);
					return true;
				case "DateType":
					if (
						modelField.fieldType.datePrecision !== "FULL" &&
						typeof value === "string"
					) {
						// in this case we know, it was a partially known date
						return false;
					}
					docParent[modelField.name] = ValidationDateParser.formatDate(
						value,
						modelField.fieldType.format,
						timeZone
					);
					return true;
				case "DateFragmentType":
					docParent[modelField.name] = ValidationDateParser.formatDate(
						value,
						modelField.fieldType.formatOfFragment,
						timeZone
					);
					return true;
				default:
					// If it is not a date data type, we do not convert anything
					return false;
			}
		} else if (value instanceof Array) {
			if (modelField.fieldType.type === "DateRangeType") {
				docParent[modelField.name] = DateUtil.convertDateRangeToString(
					value,
					modelField.fieldType.rangeSeparator,
					modelField.fieldType.format,
					timeZone
				);
				return true;
			} else {
				// If it is not a date range data type, we do not convert anything
				return false;
			}
		}
		return false; // This cannot happen.
	}

	private traverseGroupInParallel(
		docElement: Record<string, unknown>,
		group: DocumentModel.Group,
		fieldVisitor?: FieldVisitor,
		groupVisitor?: GroupVisitor
	): { result: Record<string, unknown>; changed: boolean; skip: boolean } {
		if (!docElement) {
			const skip = groupVisitor?.call(null, docElement, group)?.skip ?? false;
			return { result: docElement, changed: false, skip };
		}
		// Create a copy of the current group on the fly
		const myResult: Record<string, unknown> = {};
		// Have changes be done
		let myChanged = false;
		for (const elementName of Object.keys(docElement)) {
			myResult[elementName] = docElement[elementName];
			const elements = group.elements
				.filter(e => e.name === elementName)
				.filter(e => e.type === "Field" || e.type === "Group");
			if (elements.length !== 1) {
				// Element not found in DocumentModel => We do nothing
				continue;
			}
			const modelElement = elements[0];
			let elementChanged = false;
			if (modelElement.type === "Group") {
				elementChanged = this.traverseSubGroup(
					modelElement,
					docElement,
					elementName,
					myResult,
					fieldVisitor,
					groupVisitor
				);
			} else if (fieldVisitor) {
				elementChanged = fieldVisitor(docElement[elementName], modelElement, myResult);
			}
			myChanged = myChanged || elementChanged;
		}
		let mySkip = false;
		if (groupVisitor) {
			// Execute the group visitor with the fully setup replacement group
			const { changed, skip } = groupVisitor(myResult, group);
			myChanged = changed || myChanged;
			mySkip = skip;
		}
		if (!myChanged) {
			return { result: docElement, changed: false, skip: mySkip };
		} else {
			return { result: myResult, changed: true, skip: mySkip };
		}
	}

	/**
	 * Traverse a repeatable or non-repeatable subgroup.
	 * @returns whether the group was changed or not.
	 */
	private traverseSubGroup(
		modelGroup: DocumentModel.Group,
		docElement: Record<string, unknown>,
		groupName: string,
		myResult: Record<string, unknown>,
		fieldVisitor?: FieldVisitor,
		groupVisitor?: GroupVisitor
	): boolean {
		const docGroup: DocGroup = <DocGroup>docElement[groupName];
		if (docGroup instanceof Array) {
			return this.traverseRepeatableSubGroup(
				modelGroup,
				docGroup,
				groupName,
				myResult,
				fieldVisitor,
				groupVisitor
			);
		} else {
			return this.traverseNonRepeatableSubGroup(
				modelGroup,
				docGroup,
				groupName,
				myResult,
				fieldVisitor,
				groupVisitor
			);
		}
	}

	private traverseNonRepeatableSubGroup(
		modelGroup: DocumentModel.Group,
		docGroup: Record<string, unknown>,
		groupName: string,
		myResult: Record<string, unknown>,
		fieldVisitor?: FieldVisitor,
		groupVisitor?: GroupVisitor
	): boolean {
		// For a single one, we call directly
		const { result, changed, skip } = this.traverseGroupInParallel(
			docGroup,
			modelGroup,
			fieldVisitor,
			groupVisitor
		);
		if (skip) {
			// eslint-disable-next-line @typescript-eslint/no-dynamic-delete
			delete myResult[groupName];
			return true;
		} else if (changed) {
			myResult[groupName] = result;
			return true;
		}
		return false;
	}

	private traverseRepeatableSubGroup(
		modelGroup: DocumentModel.Group,
		docGroup: Record<string, unknown>[],
		groupName: string,
		myResult: Record<string, unknown>,
		fieldVisitor?: FieldVisitor,
		groupVisitor?: GroupVisitor
	): boolean {
		const repGroup: Array<Record<string, unknown>> = [];
		myResult[groupName] = repGroup;
		// For multiple repetitions, we iterate
		let myChanged = false;
		for (const docGroupRep of docGroup) {
			const { result, changed, skip } = this.traverseGroupInParallel(
				docGroupRep,
				modelGroup,
				fieldVisitor,
				groupVisitor
			);
			if (!skip) {
				repGroup.push(result);
			}
			myChanged ||= changed || skip;
		}
		if (groupVisitor) {
			const { changed, skip } = groupVisitor(repGroup, modelGroup);
			if (skip) {
				// eslint-disable-next-line @typescript-eslint/no-dynamic-delete
				delete myResult[groupName];
			}
			myChanged ||= changed || skip;
		}

		return myChanged;
	}

	updateEntityInstance(
		document: GroupInstance,
		path: EntityInstancePath,
		update: readonly GroupInstance[] | Readonly<GroupInstance> | FieldInstanceValue | undefined,
		documentModel: DocumentModel
	): GroupInstance {
		// document was a group, so the updated one is a group too and we can cast
		return EntityInstanceUtils.updateEntityInstance(
			document,
			path,
			update,
			documentModel.content.modelRoot
		) as GroupInstance;
	}

	/**
	 * This is a non-recursive depth-first-search implementation. It is non-recursive to avoid possible stack overflow for big documents.
	 * The algorithm proceeds as follows. At the current node, it finds the children of the node which lie at the current path part.
	 * <ul>
	 *  <li>Termination #1: if the current path part is empty, the current path is a valid result, add it to the output. In this case we have found a group instance.</li>
	 * 	<li>If an array of group value instances is found, push them to the stack to process later.</li>
	 * 	<li>If a single group value instance is found, push it to the stack to process later.</li>
	 *  <li>Termination #2: if a field instance value has been found, and the current path part is the last part of the search path, add it to the result.</li>
	 * </ul>
	 * Then it pops an element from the stack and repeats the above steps.
	 */
	getEntityPathsInDocument(
		document: GroupInstance,
		searchPath: EntityInstancePath
	): EntityInstancePath[] {
		const result = new Array<EntityInstancePath>();

		type PathEiPairType = {
			path: EntityInstancePath;
			gi: GroupInstance;
		};
		const pathEiStack = new Array<PathEiPairType>();
		let current: PathEiPairType | undefined = { path: [], gi: document };

		while (current !== undefined) {
			const curSrchPathPart = searchPath[current.path.length];

			if (curSrchPathPart === undefined) {
				// The search path has been fully explored.
				// The input path must have been the path of a group instance
				const newPath = EntityInstanceUtils.buildNewPath(current.path);
				result.push(newPath);
				current = pathEiStack.pop();
				continue; // Notice 'continue'
			}

			const childEi = EntityInstanceUtils.getChildEntityInstance(current.gi, curSrchPathPart, true);

			if (EntityInstanceUtils.isRepeatableGroupInstance(childEi)) {
				const curPath = current.path;
				const len = childEi.length;
				// We push the indexes from top to bottom in the stack in order to process them in desceding order by using stack pop function.
				for (let i = len - 1; i >= 0; i--) {
					const newPath = EntityInstanceUtils.buildNewPath(curPath, {
						elementName: curSrchPathPart.elementName,
						index: i + 1
					});
					pathEiStack.push({ path: newPath, gi: childEi[i] });
				}
			} else if (EntityInstanceUtils.isNonRepeatableGroupInstance(childEi)) {
				pathEiStack.push({
					path: EntityInstanceUtils.buildNewPath(current.path, {
						elementName: curSrchPathPart.elementName,
						index: curSrchPathPart.index === 0 ? 1 : curSrchPathPart.index // It is possible to search with '0' for non-repeatable groups, in this case the found group must have index '1'
					}),
					gi: childEi
				});
			} else if (
				EntityInstanceUtils.isFieldInstance(childEi) &&
				curSrchPathPart === searchPath[searchPath.length - 1] // We have already reached the end of search path.
			) {
				// A field instance value has been reached, and the path has been fully explored.
				const newPath = EntityInstanceUtils.buildNewPath(current.path, {
					elementName: curSrchPathPart.elementName,
					index: curSrchPathPart.index === 0 ? 1 : curSrchPathPart.index // It is possible to search with '0' for field instances, in this case the found field instance must have index '1'
				});
				result.push(newPath);
			}
			current = pathEiStack.pop();
		}

		return result;
	}

	getAssignedObject(
		document: GroupInstance,
		path: EntityInstancePath
	): GroupInstance[] | GroupInstance | FieldInstanceValue | undefined {
		// make fresh shallow copies of Groups and Arrays, so they can truthfully be labeled
		// rw without violating the ro promise of the input document:
		return EntityInstanceUtils.shallowCopy(EntityInstanceUtils.getAssignedObject(document, path));
	}

}
