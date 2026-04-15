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

import type { Localizable, Placeholder } from "@com.mgmtp.a12.utils/utils-localization";

import type {
  IData,
  IIdentifier
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import {
  ErrorType,
  IResult
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { LocalizableUtil } from "@com.mgmtp.a12.kernel/kernel-core-runtime-ts/internal";

import type {
  DocumentValidationResult,
  Message
} from "../api.js";

import { EntityInstanceConverter } from "../internal/EntityInstanceConverter.js";

import { ValidationData } from "../internal/dm-runtime-service/ValidationData.js";

/**
 * The implementation of the results from a call of {@link DocumentRtService.validateFull} or {@link DocumentRtService.validatePart}.
 */
export class DocumentValidationResultImpl implements IResult, DocumentValidationResult {
  data: IData | undefined;
  _fieldValidationResults: Message[] = [];

  public get messages(): Message[] {
    return this._fieldValidationResults;
  }

  public get noErrorOccurred(): boolean {
    return !this._fieldValidationResults.some(r => r.severity === "ERROR");
  }

  public addInfo(
    ruleName: string,
    infoCode: string,
    infoField: IIdentifier,
    infoMessage: Localizable[],
    type: ErrorType,
    referencedFields: Set<IIdentifier>,
    refOmissionErrorResponsible: Set<IIdentifier>,
    unknowns: Set<IIdentifier>
  ): void {
    this._addMessage(
      ruleName,
      infoCode,
      infoField,
      infoMessage,
      type,
      referencedFields,
      refOmissionErrorResponsible,
      unknowns,
      "INFO"
    );
  }

  public addHint(
    ruleName: string,
    infoCode: string,
    infoField: IIdentifier,
    infoMessage: Localizable[],
    type: ErrorType,
    referencedFields: Set<IIdentifier>,
    refOmissionErrorResponsible: Set<IIdentifier>,
    unknowns: Set<IIdentifier>
  ): void {
    this._addMessage(
      ruleName,
      infoCode,
      infoField,
      infoMessage,
      type,
      referencedFields,
      refOmissionErrorResponsible,
      unknowns,
      "WARNING"
    );
  }

  public addError(
    ruleName: string,
    infoCode: string,
    infoField: IIdentifier,
    infoMessage: Localizable[],
    type: ErrorType,
    referencedFields: Set<IIdentifier>,
    refOmissionErrorResponsible: Set<IIdentifier>,
    unknowns: Set<IIdentifier>
  ): void {
    this._addMessage(
      ruleName,
      infoCode,
      infoField,
      infoMessage,
      type,
      referencedFields,
      refOmissionErrorResponsible,
      unknowns,
      "ERROR"
    );
  }

  private _addMessage(
    ruleName: string,
    infoCode: string,
    field: IIdentifier,
    message: Localizable[],
    type: ErrorType,
    referencedFields: Set<IIdentifier>,
    refOmissionErrorResponsible: Set<IIdentifier>,
    _unknowns: Set<IIdentifier>,
    severity: Message.Severity
  ) {
    const _referencedFields = EntityInstanceConverter.convertToEntityInstanceArray(referencedFields);
    const _refOmissionErrorResponsible = EntityInstanceConverter.convertToEntityInstanceArray(refOmissionErrorResponsible);
    const _message = this._createNewMessageIfNeeded(ruleName, message, referencedFields);

    this._fieldValidationResults.push({
      rulePath: ruleName,
      errorCode: infoCode,
      entityInstance: EntityInstanceConverter.convertToEntityInstance(field),
      errorText: _message,
      messageType: DocumentValidationResultImpl._toMessageType(type),
      referencedFields: _referencedFields,
      refOmissionErrorResponsible: _refOmissionErrorResponsible,
      severity
    });
  }

  /**
   * Creates new localizables with the original typed values in order to be formattable.
   */
  private _createNewMessageIfNeeded(
    ruleName: string,
    message: Localizable[],
    referencedFields: Set<IIdentifier>
  ): Localizable[] {
    if (
      !this.data
      || ruleName === IResult.FORMAL_VALIDATION_RULE_NAME // for formal errors we don't need to create a new message
    ) {
      return message;
    }
    const newMessage: Localizable[] = [];
    for (const messageItem of message) {
      const args = messageItem.args;
      if (!args) {
        newMessage.push(messageItem);
        continue;
      }
      const newArgs: { [placeholder: string]: Placeholder } = {};
      for (const key in args) {
        const placeHolder = args[key];
        if (placeHolder.type !== "formattable") {
          newArgs[key] = placeHolder;
          continue;
        }
        const fieldPathParts = placeHolder.properties.formattingConfig.modelPath;
        if (!fieldPathParts) {
          newArgs[key] = placeHolder;
          continue;
        }
        const fieldId = this._getFieldId(fieldPathParts, referencedFields);
        if (!fieldId) {
          newArgs[key] = placeHolder;
          continue;
        }
        let originalValue = null;
        if (this.data instanceof ValidationData) {
          originalValue = this.data.getTypedValue(fieldId);
        } else {
          originalValue = this.data.getValue(fieldId) ?? null;
        }
        // Note: <code>!originalValue</code> does not work here, since we only want to handle null and undefined
        // if originalValue is 0 as number, !originalValue is evaluated to true which is not wanted in this case
        if (originalValue == null) {
          newArgs[key] = placeHolder;
          continue;
        }
        newArgs[key] = {
          ...placeHolder,
          value: originalValue
        };
      }
      const newMessageItem = LocalizableUtil.addLocalizableArgs(messageItem, newArgs);
      newMessage.push(newMessageItem);
    }
    return newMessage;
  }

  private _getFieldId(
    fieldPathParts: { elementName: string }[],
    referencedFields: Set<IIdentifier>
  ): IIdentifier | undefined {
    const fieldPath = EntityInstanceConverter.combinePath(fieldPathParts);
    for (const refField of referencedFields) {
      if (fieldPath === refField.getName()) {
        return refField;
      }
    }
    return undefined;
  }

  public getCountOf(type: ErrorType): number {
    return this._fieldValidationResults.filter(
      r => r.messageType === DocumentValidationResultImpl._toMessageType(type)
    ).length;
  }

  private static _toMessageType(type: ErrorType): Message.MessageType {
    switch (type) {
      case ErrorType.OMISSION_ERROR:
        return "OMISSION_ERROR";
      case ErrorType.VALUE_ERROR:
        return "VALUE_ERROR";
    }
  }
}
