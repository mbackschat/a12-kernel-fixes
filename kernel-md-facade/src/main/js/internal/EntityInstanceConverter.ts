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

import type { ModelPath } from "@com.mgmtp.a12.base/base-model-api";

import type { IIdentifierWithSemanticIndex } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { HashSet, IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { RtExternalIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-ts/internal";

import type { EntityInstancePath, FieldInstanceValue, PartiallyKnownEntityInstancePath } from "../api.js";

import type { EntityInstancePathPart } from "./EntityInstanceUtils.js";


export class EntityInstanceConverter {
	static computeIndices(entityInstance: EntityInstancePath): number[] {
		return entityInstance.map(i => i.index);
	}

	static splitPath(path: string): string[] {
		if (path === IIdentifier.SEPARATOR) {
			// the only part is the root, therefore we have no path parts
			return [];
		} else {
			return path.split(IIdentifier.SEPARATOR).slice(1);
		}
	}

	static combineInstancePathPart({ elementName, index }: EntityInstancePathPart): string {
		return `${elementName}[${index}]`;
	}

	static combineInstancePath(entityInstance: EntityInstancePath): string {
		return EntityInstanceConverter.combinePathElements(entityInstance.map(i => EntityInstanceConverter.combineInstancePathPart(i)));
	}

	static combinePath(entityInstance: EntityInstancePath | ModelPath): string {
		return EntityInstanceConverter.combinePathElements(entityInstance.map(i => i.elementName));
	}

	static createChildPath(parentPath: EntityInstancePath, childName: string): string {
		if (parentPath.length === 0) {
			return IIdentifier.SEPARATOR + childName;
		} else {
			return EntityInstanceConverter.combinePath(parentPath) + IIdentifier.SEPARATOR + childName;
		}
	}

	static createEntityInstance(path: string, indices: number[]): EntityInstancePath {
		const pathElems: string[] = EntityInstanceConverter.splitPath(path);
		if (pathElems.length != indices.length) {
			throw new Error("path elements do not match the number of indices");
		}

		return pathElems.map(function (e, i) {
			return { elementName: e, index: indices[i] };
		});
	}

	static createModelPath(path: string): ModelPath {
		return EntityInstanceConverter.splitPath(path).map(elem => ({ elementName: elem }));
	}

	static combinePathElements(pathElements: string[]): string {
		return IIdentifier.SEPARATOR + EntityInstanceConverter.joinPathElements(pathElements);
	}

	static joinPathElements(pathElements: string[]): string {
		return pathElements.join(IIdentifier.SEPARATOR);
	}

	static convertToIIdentifier(entityInstance: EntityInstancePath): RtExternalIdentifier {
		return new RtExternalIdentifier(
			EntityInstanceConverter.combinePath(entityInstance),
			EntityInstanceConverter.computeIndices(entityInstance)
		);
	}

	static convertToIIdentifierArray(entityInstances: Iterable<EntityInstancePath>): IIdentifier[] {
		return Array.from(entityInstances, EntityInstanceConverter.convertToIIdentifier);
	}

	static convertToIIdentifierSet(entityInstances: Iterable<EntityInstancePath>): HashSet<IIdentifier> {
		return new HashSet(EntityInstanceConverter.convertToIIdentifierArray(entityInstances));
	}

	static convertToEntityInstance(identifier: IIdentifier): PartiallyKnownEntityInstancePath {
		const entityInstance = EntityInstanceConverter.createEntityInstance(identifier.getName(), identifier.getIndices());
		const semanticIndexValue = EntityInstanceConverter._semanticIndexValueForUnknownIndexGroup(identifier);
		if (semanticIndexValue === undefined) {
			return entityInstance;
		}
		// attach the semantic index value to the index group whose semantic index could not be resolved (index -5)
		return entityInstance.map(part =>
			part.index === IIdentifier.UNKNOWN
				? { ...part, semanticIndexValueForUnknownIndexGroup: semanticIndexValue }
				: part
		);
	}

	/**
	 * Reads the semantic index value (converted to its basic type) from an {@link IIdentifierWithSemanticIndex},
	 * or returns `undefined` if the identifier does not carry one.
	 */
	private static _semanticIndexValueForUnknownIndexGroup(identifier: IIdentifier): FieldInstanceValue | undefined {
		// duck-type IIdentifierWithSemanticIndex (avoids a hard dependency on the impl class)
		const semanticIndexIdentifier = identifier as Partial<IIdentifierWithSemanticIndex>;
		if (typeof semanticIndexIdentifier.getIndexValueAsBasicType !== "function") {
			return undefined;
		}
		return semanticIndexIdentifier.getIndexValueAsBasicType() ?? undefined;
	}

	static convertToEntityInstanceArray(identifiers: Iterable<IIdentifier>): PartiallyKnownEntityInstancePath[] {
		return Array.from(identifiers, EntityInstanceConverter.convertToEntityInstance);
	}
}
