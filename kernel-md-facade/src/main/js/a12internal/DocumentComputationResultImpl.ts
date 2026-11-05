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

import { SupportedType } from "@com.mgmtp.a12.utils/utils-localization";

import type {
  IData,
  IIdentifier,
  IMetaField,
  IMetaModel
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import {
  HashMap,
  HashSet,
  IMetaKeys
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { ResultBaseImpl, RtExternalIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-ts/internal";

import type {
  Document,
  DocumentComputationResult,
  DocumentValidationResult,
  FieldInstanceValue,
  Message
} from "../api.js";

import { EntityInstanceConverter } from "../internal/EntityInstanceConverter.js";

import { EntityInstanceUtils } from "../internal/EntityInstanceUtils.js";

import { ComputedFieldInstanceImpl } from "../internal/dm-runtime-service/ComputedFieldInstanceImpl.js";


import { ValidationData } from "../internal/dm-runtime-service/ValidationData.js";

/**
 * Implementation of a result of a document computation (see {@link DocumentRtService.compute}).
 */
export class DocumentComputationResultImpl implements DocumentComputationResult {
  public computedFieldInstancesWithoutErrors: ComputedFieldInstanceImpl[] = [];
  public computedFieldInstancesWithChanges: ComputedFieldInstanceImpl[] = [];
  public computedFieldInstancesWithErrors: ComputedFieldInstanceImpl[] = [];
  public clearedFieldInstances: ComputedFieldInstanceImpl[] = [];
  public formalErrorsInOperands: Message[] = [];

  /**
   * Implementation of a result of a document computation (see {@link DocumentRtService.compute}).
   * 
   * @param document  the {@link Document} based on which this {@link DocumentComputationResult} was computed
   * @param originalData  the original data
   * @param computedData  the computed data
   * @param formalErrors  the implementation of the document validation result containing the formal errors
   * @param metaModel the metamodel
   */
  public constructor(
    public document: Document,
    originalData: IData,
    computedData: IData,
    formalErrors: DocumentValidationResult,
    metaModel: IMetaModel
  ) {
    // in contrast to Java, we do not look up the original values here, so that externally computed fields don't need special treatment
    const originalInstancesOfInternallyComputedFields = DocumentComputationResultImpl._getInstancesOfInternallyComputedFields(originalData, metaModel);
    const identifierToErrorMessage = DocumentComputationResultImpl._getIdentifierToErrorMessage(formalErrors.messages);

    const it = computedData.iterator();
    while (it.hasNext()) {
      // we checked if there is a next one
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      const itId = it.next()!;
      const id = itId instanceof RtExternalIdentifier ? itId : new RtExternalIdentifier(itId);

      const computedStringValue = computedData.getValue(itId);
      const computedValue = DocumentComputationResultImpl._createValue(metaModel, id.getName(), computedStringValue);
      const errorMessage = identifierToErrorMessage.deleteAndRetrieve(id);
      const entityInstance = EntityInstanceConverter.convertToEntityInstance(id);
      const computedFieldInstance = new ComputedFieldInstanceImpl(document, entityInstance, computedValue, computedStringValue, errorMessage);

      originalInstancesOfInternallyComputedFields.delete(id);
      if (computedFieldInstance.errorMessage === undefined) {
        const originalValue = DocumentComputationResultImpl._getOriginalValue(originalData, id);
        if (!computedStringValue) { // in case of external computations, computation can return cleared fields
          if (!EntityInstanceUtils.isEmpty(originalValue)) { // do not include as cleared if original field instance is unfilled
            this.clearedFieldInstances.push(computedFieldInstance);
          }
        } else {
          this.computedFieldInstancesWithoutErrors.push(computedFieldInstance);
          if (!SupportedType.equals(originalValue, computedFieldInstance.value)) {
            this.computedFieldInstancesWithChanges.push(computedFieldInstance);
          }
        }
      } else {
        this.computedFieldInstancesWithErrors.push(computedFieldInstance)
      }
    }

    // all remaining field instances were not (internally) computed --> must be cleared
    for (const id of originalInstancesOfInternallyComputedFields) {
      const originalValue = DocumentComputationResultImpl._getOriginalValue(originalData, id);
      if (!EntityInstanceUtils.isEmpty(originalValue)) {
        const entityInstance = EntityInstanceConverter.convertToEntityInstance(id);
        const computedFieldInstance = new ComputedFieldInstanceImpl(document, entityInstance, null, "", undefined);
        this.clearedFieldInstances.push(computedFieldInstance);
      }
    }

    // all remaining errors are not from computed field instances --> must be from the operands
    this.formalErrorsInOperands = Array.from(identifierToErrorMessage.values());
  }

  public appliedTo(document: Document): Document {
    return EntityInstanceUtils.createUpdatedDocumentForComputation(document, [
      ...this.clearedFieldInstances,
      ...this.computedFieldInstancesWithChanges,
      ...this.computedFieldInstancesWithErrors.map(cfi => ({ path: cfi.path, value: null })),
    ], document !== this.document);
  }

  private static _getOriginalValue(originalData: IData, id: IIdentifier): string | number | boolean | Date | Date[] | null {
    if (originalData instanceof ValidationData) {
      return originalData.getTypedValue(id) ?? null;
    } else {
      return originalData.getValue(id) ?? null;
    }
  }

  /**
   * Convert the provided stringValue to the type of the field at the given path.
   */
  private static _createValue(metaModel: IMetaModel, path: string, stringValue: string | undefined): FieldInstanceValue {
    if (stringValue) {
      const field: IMetaField = metaModel.getValue(IMetaKeys.MODEL_META_FIELD, path); // cannot be undefined

      // stringValue comes from the computation, that means, the errors are already reported and that's why we ignore them here
      const validationResult = new ResultBaseImpl();
      // Note: Since the DataFormats parameter of convertWithOnlyBasicTypeCheck is null we do not need to provide a CustomFieldTypeFactory.
      // By default, FormatDefinitionVordefiniert does no conversion of the field values and so does not need the actual CFTs.
      return field.convertWithOnlyBasicTypeCheck(stringValue, null, validationResult);
    } else {
      return null;
    }
  }

  public get noErrorOccurred(): boolean {
    return this.computedFieldInstancesWithErrors.length === 0 && this.formalErrorsInOperands.length === 0;
  }

  private static _getIdentifierToErrorMessage(messages: Message[]): HashMap<RtExternalIdentifier, Message> {
    const identifierToErrorMessage: HashMap<RtExternalIdentifier, Message> = new HashMap();
    for (const message of messages) {
      const id = EntityInstanceConverter.convertToIIdentifier(message.entityInstance);
      identifierToErrorMessage.set(id, message);
    }
    return identifierToErrorMessage;
  }

  private static _getPathsOfComputedFields(metaModel: IMetaModel): Set<string> {
    const metaFields: Set<IMetaField> = metaModel.getValue(IMetaKeys.MODEL_CALC_FIELDS);
    const computedFieldPaths: Set<string> = new Set();
    for (const metaField of metaFields) {
      const path: string = metaField.getValue(IMetaKeys.FIELD_FULLNAME);
      computedFieldPaths.add(path);
    }
    return computedFieldPaths;
  }

  /**
   * @returns a set of instances of (internally) computed fields that were set (not necessarily filled) in the given data.
   */
  private static _getInstancesOfInternallyComputedFields(data: IData, metaModel: IMetaModel): HashSet<IIdentifier> {
    const pathsOfComputedFields = this._getPathsOfComputedFields(metaModel);
    const instancesOfComputedFields: HashSet<IIdentifier> = new HashSet();
    const it = data.iterator();
    while (it.hasNext()) {
      // we checked if there is a next one
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      const id = it.next()!;
      if (pathsOfComputedFields.has(id.getName())) {
        instancesOfComputedFields.add(id);
      }
    }
    return instancesOfComputedFields;
  }
}