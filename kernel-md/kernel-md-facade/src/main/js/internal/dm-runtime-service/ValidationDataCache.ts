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

import type { SupportedType } from "@com.mgmtp.a12.utils/utils-localization";

import type {
  IMetaField,
  IMetaGroup,
  IMetaModel,
  IResult
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import {
  HashMap,
  IData,
  IMetaKeys
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { RtExternalIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-ts/internal";

import type {
  Document,
  EntityInstancePath,
  EntityInstanceValueMapping,
  FieldInstanceValue
} from "../../api.js";

import { EntityInstanceConverter } from "../EntityInstanceConverter.js";
import type { EntityInstance } from "../EntityInstanceUtils.js";
import { EntityInstanceUtils } from "../EntityInstanceUtils.js";

import { ResultForConversion } from "./ResultForConversion.js";

/**
 * The {@link ValidationDataCache} is initialized with all field and group instance values of the provided {@link Document}.
 * Unknown groups are always ignored. Filled groups are stored with the value {@link IData.FILLEDGROUP}).
 * 
 * If <code>_ignoreUnknownFields</code> is set to true, instances of fields which are not in the model are not added to the cache.
 * Otherwise, unknown fields are added to the cache (and are contained in {@link ValidationDataCache#keys()}, but will throw an error
 * when accessed via {@link ValidationDataCache.getStringValue}.
 * 
 * The cache initially only contains {@link EntityCacheNode}s with unconverted values. Whenever a string value is accessed via
 * {@link ValidationDataCache.getStringValue} and not yet converted (i.e. {@link EntityCacheNode.wasConverted} is `false`), it is converted to the
 * internal representation. This lazy conversion avoids a lot of work particularly for partial validation.
 * 
 * Much of the above does not apply to the values provided by <code>valuesOfFieldInstancesToConsider</code>:
 * Even if not in the model, the mapping's value will be respected over the document's and no error will be thrown.
 */
export class ValidationDataCache {
  /**
   * See method {@link _traverse}.
   */
  private _cache: HashMap<RtExternalIdentifier, EntityCacheNode> = new HashMap();

  constructor(
    document: Document,
    valuesOfFieldInstancesToConsider: EntityInstanceValueMapping | undefined,
    private _metaModel: IMetaModel,
    private _result: IResult,
    private _ignoreUnknownFields?: boolean,
  ) {
    // don't handle the root group explicitly, only the included elements    
    Object.entries(document).forEach(entity => {
      // always ignore the document 'id' (don't add it to the cache), which might be included on the same level as the root groups (see also A12K-2228)
      // all other 'id' fields are normal fields
      if (entity[0] === 'id') {
        return;
      }

      this._traverse(entity, []);
    });

    // override fields or groups from the document with the mapping, this takes precedence
    if (valuesOfFieldInstancesToConsider) {
      for (const [entityInstance, value] of valuesOfFieldInstancesToConsider.entries()) {
        const identifier = EntityInstanceConverter.convertToIIdentifier(entityInstance);
        const metaField: IMetaField | undefined = this._metaModel.getValue(IMetaKeys.MODEL_META_FIELD, identifier.getName());
        this._cacheFieldValue(identifier, value, metaField, false);
      }
    }
  }

  /**
   * Traverses all the entities in a document and adds them to the cache:
   * - Field: gets always added
   * - Non-repeatable group: gets added with value "filledGroup" iff the group is mandatory.
   *   The group is mandatory, if at least a contained non-repeatable field is mandatory.
   * - Repeatable group: gets added with value "filledGroup" iff present
   * 
   * @param currentName The name of the current entity.
   * @param currentEntityInstance The current entity instance of the traversion.
   * @param parentEntityInstancePath The path of the parent entity instance in the document.
   */
  private _traverse([currentName, currentEntityInstance]: [string, EntityInstance], parentEntityInstancePath: EntityInstancePath) {
    const currentEntityPath = EntityInstanceConverter.createChildPath(parentEntityInstancePath, currentName);
    const currentMetaField: IMetaField | undefined = this._metaModel.getValue(IMetaKeys.MODEL_META_FIELD, currentEntityPath);
    const currentMetaGroup: IMetaGroup | undefined = this._metaModel.getValue(IMetaKeys.MODEL_META_GROUP, currentEntityPath);
    if (this._ignoreUnknownFields && currentMetaField == null && currentMetaGroup == null) {
      // if it is not contained in the generated code (as field or group), i.e. not defined in the document model,
      // we can ignore it
      return;
    }

    if (EntityInstanceUtils.isRepeatableGroupInstance(currentEntityInstance)) {
      const numberOfGroupInstances = currentEntityInstance.length;
      // each of the existing repetitions has to be traversed
      for (let i = 1; i <= numberOfGroupInstances; i++) {
        const groupInstancePath = [...parentEntityInstancePath, { elementName: currentName, index: i }];
        // add to the cache
        this._cacheGroup(EntityInstanceConverter.convertToIIdentifier(groupInstancePath));
        // continue traversing in the specific group instance
        Object.entries(currentEntityInstance[i - 1]).forEach(entity => {
          this._traverse(entity, groupInstancePath);
        });
      }
    } else {
      // now we know that 'index' can only be 1
      const currentEntityInstancePath = [...parentEntityInstancePath, { elementName: currentName, index: 1 }];
      if (EntityInstanceUtils.isNonRepeatableGroupInstance(currentEntityInstance)) {
        // continue traversing
        Object.entries(currentEntityInstance).forEach(entity => {
          this._traverse(entity, currentEntityInstancePath);
        });
      } else if (EntityInstanceUtils.isFieldInstance(currentEntityInstance)) {
        const identifier = EntityInstanceConverter.convertToIIdentifier(currentEntityInstancePath);
        this._cacheFieldValue(identifier, currentEntityInstance, currentMetaField, currentMetaField == undefined);
      } else {
        // undefined, which is the correct structure, but not a field or group
      }
    }
  }

  private _cacheGroup(identifier: RtExternalIdentifier) {
    this._cache.set(identifier, new EntityCacheNode(IData.FILLEDGROUP));
  }

  private _cacheFieldValue(identifier: RtExternalIdentifier, value: FieldInstanceValue, metaField?: IMetaField, isUnknownField = false) {
    this._cache.set(identifier, new EntityCacheNode(value, metaField, isUnknownField));
  }

  public keys(): RtExternalIdentifier[] {
    return [...this._cache.keys()];
  }

  /**
   * @Returns <code>undefined</code>, if <code>id</code> is not in the cache (i.e. not in document).
   * @throws if <code>id</code> describes an unknown entity (i.e. not in model).
   */
  public getStringValue(id: RtExternalIdentifier): string | undefined {
    const cacheNode = this._cache.get(id);

    if (cacheNode === undefined) {
      return undefined;
    }

    if (cacheNode.isUnknownField) {
      throw this._getUnknownEntityError(id);
    }

    if (cacheNode.wasConverted) {
      return cacheNode.stringValue;
    }

    return this._convertAndCacheStringValue(cacheNode, id);
  }

  private _convertAndCacheStringValue(cacheNode: EntityCacheNode, id: RtExternalIdentifier): string | undefined {
    if (cacheNode.metaField == undefined) {
      throw this._getUnknownEntityError(id);
    }

    const conversionResult = new ResultForConversion(this._result, id);
    // Note: Since the DataFormats parameter of convertFromBasicType is null we do not need to provide a CustomFieldTypeFactory.
    // By default, FormatDefinitionVordefiniert does no conversion of the field values and so does not need the actual CFTs
    cacheNode.stringValue = cacheNode.metaField.convertFromBasicType(cacheNode.value, null, conversionResult) ?? undefined;
    cacheNode.wasConverted = true;
    return cacheNode.stringValue;
  }

  private _getUnknownEntityError(id: RtExternalIdentifier): Error {
    // not a valid field in the meta model (only possible if _ignoreUnknownFields is false)
    return new Error(`Cannot access unknown entity '${id.getName()}'. It does not exist in the model.`)
  }

  public getValue(id: RtExternalIdentifier): SupportedType | undefined {
    const cacheNode: EntityCacheNode | undefined = this._cache.get(id);
    return cacheNode?.value;
  }
}

class EntityCacheNode {
  public stringValue: string | undefined = undefined;
  /**
   * Whether the value in {@link EntityCacheNode.stringValue} was already converted from {@link EntityCacheNode.value}.
   */
  public wasConverted = false;

  public constructor(
    public value: SupportedType,
    public metaField?: IMetaField,
    public isUnknownField = false,
  ) {
    if (typeof value === "string" || value === null) {
      this.stringValue = value ?? undefined;
      this.wasConverted = true;
    }
  }
}