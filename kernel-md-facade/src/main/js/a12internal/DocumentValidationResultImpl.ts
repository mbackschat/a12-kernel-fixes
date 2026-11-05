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

import {
  ErrorType,
  IResult,
  SeverityType,
  type IData,
  type IIdentifier
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { LocalizableUtil, ResultBaseImpl } from "@com.mgmtp.a12.kernel/kernel-core-runtime-ts/internal";

import type {
  DocumentValidationResult,
  Message
} from "../api.js";

import { EntityInstanceConverter } from "../internal/EntityInstanceConverter.js";

import { ValidationData } from "../internal/dm-runtime-service/ValidationData.js";

/**
 * The implementation of the results from a call of {@link DocumentRtService.validateFull} or {@link DocumentRtService.validatePart}.
 */
export class DocumentValidationResultImpl extends ResultBaseImpl implements DocumentValidationResult {
  data: IData | undefined;

  public get messages(): Message[] {
    return this.getV1AndV2Messages().map(({ v1Message, v2Message }) => ({
      rulePath: v1Message.ruleName,
      errorCode: v1Message.id,
      entityInstance: EntityInstanceConverter.convertToEntityInstance(v1Message.entityId),
      errorText: this._createNewMessageIfNeeded(v1Message.ruleName, v1Message.message, v1Message.referencedFields),
      messageType: DocumentValidationResultImpl._toMessageType(v1Message.type),
      referencedFields: EntityInstanceConverter.convertToEntityInstanceArray(v1Message.referencedFields),
      refOmissionErrorResponsible: EntityInstanceConverter.convertToEntityInstanceArray(v1Message.refOmissionErrorResponsible),
      severity: DocumentValidationResultImpl._toMessageSeverity(v1Message.level),
      groupsOfPossiblyCausingEntities: v2Message.possiblyCausingEntities.getCausingEntityGroups()
        .map(group => EntityInstanceConverter.convertToEntityInstanceArray(group)),
    }));
  }

  public get noErrorOccurred(): boolean {
    return !this.v1Messages.some(r => r.level === SeverityType.ERROR);
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

  private static _toMessageSeverity(severity: SeverityType): Message.Severity {
    switch (severity) {
      case SeverityType.ERROR:
        return "ERROR";
      case SeverityType.HINT:
        return "WARNING";
      case SeverityType.INFO:
        return "INFO";
    }
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
