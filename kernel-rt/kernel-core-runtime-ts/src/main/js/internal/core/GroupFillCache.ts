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
import { HashMap, HashSet } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { RtInternalIdentifier } from "../util/RtInternalIdentifier.js";

import { EntityUtil } from "../util/EntityUtil.js";

import type { IMetaDataValidierung } from "./IMetaDataValidierung.js";

/**
 * Stores the fill status of groups by remembering all filled child entities per group instance.
 */
export class GroupFillCache {
    private _pathToInstanceMap: Map<
        string, // full group name
        Map< // "instance map"
            RtInternalIdentifier, // group identifier
            CacheNode // cache node storing identifiers of direct child entities which are filled
        >
    > = new Map();
    private _highestRepetitions: number[] = [];

    constructor(
        private _metaData: IMetaDataValidierung,
    ) { }

    /**
     * Set the given entity and all its ancestor groups to filled.
     * @param entityId a valid entity in the document model
     */
    public setEntityFilled(entityId: RtInternalIdentifier): void {
        if (this._metaData.isGroupDefined(entityId.getName())) {
            this.setGroupFilled(entityId);
        } else {
            this.setFieldFilled(entityId);
        }
    }

    /**
     * Sets the given field and all its ancestor groups to filled.
     * @param fieldId a valid field in the document model
     */
    public setFieldFilled(fieldId: RtInternalIdentifier): void {
        this._propagateEntityFilled(fieldId, true);
    }

    /**
     * Sets the given group and all its ancestor groups to filled.
     * This is counted as an explicit set, meaning the group will not be removed through propagation
     * by {@link GroupFillCache#setFieldNotFilled}.
     * @param groupId a valid group in the document model
     */
    public setGroupFilled(groupId: RtInternalIdentifier): void {
        this._setGroupFilledExplicitly(groupId);
        this._propagateEntityFilled(groupId, false);
    }

    /**
     * Sets all ancestor groups of *entity* to filled.
     */
    private _propagateEntityFilled(entityId: RtInternalIdentifier, entityIsField: boolean): void {
        let childEntity = entityId;
        let parentGroup = RtInternalIdentifier.createParentId(entityId, this._metaData.getPfadName(entityId.getName()));
        while (parentGroup != null) {
            const instanceMap = this._assuredInstanceMap(parentGroup.getName());
            const cacheNode = instanceMap.get(parentGroup);
            if (cacheNode != undefined) {
                cacheNode.addChild(childEntity);
                childEntity = parentGroup;
                break;
            } else {
                instanceMap.set(parentGroup, new GroupNode(childEntity));
            }

            childEntity = parentGroup;
            parentGroup = RtInternalIdentifier.createParentId(childEntity, childEntity.getName());
        }

        // update highest repetitions of all newly added levels
        const furthestUpdatedAncestor = childEntity;
        const indices = entityId.getIndices();
        const start = furthestUpdatedAncestor.getIndices().length - 1;
        this._updateHighestRepetition(indices, start, entityIsField ? indices.length - 1 : indices.length);
    }

    /**
     * Compares and updates the stored max indices from *startIndex* (inclusive) to *stopIndex* (exclusive) with
     * corresponding values in *indices*.
     */
    private _updateHighestRepetition(indices: number[], startIndex: number, stopIndex: number) {
        for (let level = startIndex; level < stopIndex; level++) {
            if (indices[level] > (this._highestRepetitions[level] ?? 0)) {
                this._highestRepetitions[level] = indices[level];
            }
        }
    }

    private _setGroupFilledExplicitly(groupId: RtInternalIdentifier) {
        this._assuredInstanceMap(groupId.getName()).set(groupId, new ExplicitGroupNode());
    }

    private _assuredInstanceMap(groupFullName: string): Map<RtInternalIdentifier, CacheNode> {
        let instanceMap = this._pathToInstanceMap.get(groupFullName);
        if (instanceMap == undefined) {
            instanceMap = new HashMap();
            this._pathToInstanceMap.set(groupFullName, instanceMap);
        }
        return instanceMap;
    }

    /**
     * Sets *field* to not filled and updates all ancestor groups accordingly.
     * If one or more ancestor groups were only filled because of *field*,
     * those groups will be set to not filled (unless they are set explicitly).
     * @param fieldId a valid field in the document model
     */
    public setFieldNotFilled(fieldId: RtInternalIdentifier) {
        this._propagateEntityNotFilled(fieldId);
    }

    private _propagateEntityNotFilled(entityId: RtInternalIdentifier) {
        let childEntity = entityId;
        let parentGroup = RtInternalIdentifier.createParentId(entityId, this._metaData.getPfadName(entityId.getName()));
        while (parentGroup != null) {
            const instanceMap = this._pathToInstanceMap.get(parentGroup.getName());
            if (instanceMap == undefined) {
                return;
            }

            const cacheNode = instanceMap.get(parentGroup);
            if (cacheNode == undefined) {
                return;
            }
            cacheNode.removeChild(childEntity);

            if (cacheNode.isFilled()) {
                return;
            }
            instanceMap.delete(parentGroup);

            childEntity = parentGroup;
            parentGroup = RtInternalIdentifier.createParentId(childEntity, childEntity.getName());
        }
    }

    /**
     * @param groupFullName the full name of a valid group in the Document Model
     * @returns an {@link Iterable} containing all group instances which are currently filled.
     * Does not contain duplicates.
     */
    public getFilledGroupInstances(groupFullName: string): Iterable<RtInternalIdentifier> {
        return this._pathToInstanceMap.get(groupFullName)?.keys() ?? [];
    }

    /**
     * @param fieldFullName the full name of a valid field in the Document Model
     * @returns all field instances of the given field which exist (i.e. not repeatable or innermost repeatable group is filled).
     */
    public *getExistingFieldInstances(fieldFullName: string): Iterable<RtInternalIdentifier> {
        const fieldUniqueName = this._metaData.getUniqueName(fieldFullName);
        const fieldLevel = EntityUtil.countHierarchyLevels(fieldFullName);
        const irgFullName = this._metaData.getInnermostRepeatableGroup(fieldFullName);
        if (irgFullName == null) { // exactly one instance where all indices are 1
            yield new RtInternalIdentifier(fieldUniqueName, Array.from({ length: fieldLevel }, () => 1));
        } else { // one instance per filled IRG (indices below IRG are all 1)
            const irgLevel = EntityUtil.countHierarchyLevels(irgFullName);
            const indicesBelowIRG = Array.from({ length: fieldLevel - irgLevel }, () => 1);
            for (const irgInstance of this.getFilledGroupInstances(irgFullName)) {
                yield new RtInternalIdentifier(fieldUniqueName, irgInstance.getIndices().concat(indicesBelowIRG));
            }
        }
    }

    /**
     * @param groupFullName the full name of a valid group in the Document Model
     * @returns the exact number of group instances which are currently filled.
     */
    public numberOfFilledGroupInstances(groupFullName: string): number {
        return this._pathToInstanceMap.get(groupFullName)?.size ?? 0;
    }

    /**
     * @param entityId an entity in the Document Model
     * @returns `true`, if `entityId` is a filled group, `false` otherwise
     */
    public isGroupFilled(entityId: RtInternalIdentifier): boolean {
        return this._pathToInstanceMap.get(entityId.getName())?.has(entityId) ?? false;
    }

    /**
     * @param level zero-indexed level
     * @return the highest repetition ever recorded at the given *level*.
     * Never shrinks (e.g. calling {@link GroupFillCache#setFieldNotFilled} will not update it).
     * At least 1.
     */
    public getHighestRepetitionAtLevel(level: number): number {
        return this._highestRepetitions[level] ?? 1;
    }
}

abstract class CacheNode {
    addChild(_entityId: RtInternalIdentifier): void {
        // do nothing
    }

    removeChild(_entityId: RtInternalIdentifier): void {
        // do nothing
    }

    isFilled(): boolean {
        return true;
    }
}

class GroupNode extends CacheNode {
    private _children: HashSet<RtInternalIdentifier> = new HashSet();

    constructor(
        childToAdd: RtInternalIdentifier,
    ) {
        super();
        this.addChild(childToAdd);
    }

    override addChild(entityId: RtInternalIdentifier): void {
        this._children.add(entityId);
    }

    override removeChild(entityId: RtInternalIdentifier): void {
        this._children.delete(entityId);
    }

    override isFilled(): boolean {
        return !this._children.isEmpty;
    }
}

class ExplicitGroupNode extends CacheNode {

}