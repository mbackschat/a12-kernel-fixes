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

import type { DocumentModel, DocumentModelSerializer } from "../api.js";


export class DocumentModelSerializerImpl implements DocumentModelSerializer {
	deserialize(documentModelAsString: string): DocumentModel {
		const documentModelAsJSON = JSON.parse(documentModelAsString);
		const typeDefinitions = this._readTypeDefinitions(documentModelAsJSON.content.typeDefinitions);
		const docRoot = documentModelAsJSON.content.modelRoot;
		docRoot.id = "RootGroup";
		docRoot.name = "RootGroup";
		docRoot.type = "Group";
		docRoot.repeatability = 1;
		docRoot.elements = documentModelAsJSON.content.modelRoot.rootGroups;
		delete docRoot.rootGroups;
		this._traverseAndTransformGroup(docRoot, typeDefinitions);
		return documentModelAsJSON;
	}

	private _readTypeDefinitions(json: any[]): { [id: string]: any } {
		const typeDefinitions: { [id: string]: any } = {};
		if (!json) {
			return typeDefinitions;
		}
		for (const typeDef of json) {
			typeDefinitions[typeDef.id] = this._convertFieldType(typeDef.fieldType);
		}
		return typeDefinitions;
	}

	/**
	 * Transform a fieldtype.
	 * Move all occurrences of nested fieldtypes one level up.
	 */
	private _convertFieldType(json: any): any {
		Object.assign(json, json[json.type]);
		// eslint-disable-next-line @typescript-eslint/no-dynamic-delete
		delete json[json.type];
		if (json.type === "DateType" && json.datePrecision === undefined) {
			json["datePrecision"] = "FULL";
		}
		return json;
	}

	private _traverseAndTransformElement(
		element: any,
		typeDefinitions: { [id: string]: any }
	): boolean {
		// Replace annotations for any type
		const elementType: string = element.type;
		switch (elementType) {
			case "Field": {
				Object.assign(element, element[elementType]);
				// eslint-disable-next-line @typescript-eslint/no-dynamic-delete
				delete element[elementType];
				const fieldType = element.fieldType;
				if (fieldType.type === "TypeDefType") {
					const typeDef = typeDefinitions[fieldType.TypeDefType.typeDefinitionId];
					// If a typedef was declared, we replace the type.
					if (typeDef) {
						element.fieldType = typeDef;
					} else {
						throw Error("Unknown type definition " + fieldType.TypeDefType.typeDefinitionId);
					}
				} else {
					element.fieldType = this._convertFieldType(element.fieldType);
				}
				break;
			}
			case "Group":
				Object.assign(element, element[elementType]);
				// eslint-disable-next-line @typescript-eslint/no-dynamic-delete
				delete element[elementType];
				this._traverseAndTransformGroup(element, typeDefinitions);
				break;
			case "Computation":
			case "Rule":
				// We need to remove them from outside. Else we have a concurrent modification and
				// weird iteration behaviour.
				return false;
			default:
				throw new Error("Unknown element of type " + elementType + ": " + JSON.stringify(element));
		}
		return true;
	}

	private _traverseAndTransformGroup(element: any, typeDefinitions: { [id: string]: any }): void {
		if (element.elements === undefined) {
			element.elements = [];
		} else {
			element.elements = element.elements.filter((e: any) =>
				this._traverseAndTransformElement(e, typeDefinitions)
			);
		}
	}
}
