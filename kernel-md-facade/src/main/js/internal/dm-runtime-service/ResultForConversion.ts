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

import type { Localizable } from "@com.mgmtp.a12.utils/utils-localization";

import type { ErrorType, ICause, IIdentifier, IResult, SeverityType } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { ErrorCause } from "@com.mgmtp.a12.kernel/kernel-core-runtime-ts/internal";

/**
 * Wrapper class to handle the conversion where the information of the field gets lost.
 * It is initialized with the path of the field that is converted.
 */
export class ResultForConversion implements IResult {
  public constructor(private _result: IResult, private _id: IIdentifier) { }

  addInfo(ruleName: string, infoCode: string, _infoField: IIdentifier, infoMessage: Localizable[], type: ErrorType, _referencedFields: Set<IIdentifier>, refOmissionErrorResponsible: Set<IIdentifier>, unknowns: Set<IIdentifier>): void {
    this._result.addInfo(ruleName, infoCode, this._id, infoMessage, type, new Set([this._id]), refOmissionErrorResponsible, unknowns);
  }
  addHint(ruleName: string, hintCode: string, _hintField: IIdentifier, hintMessage: Localizable[], type: ErrorType, _referencedFields: Set<IIdentifier>, refOmissionErrorResponsible: Set<IIdentifier>, unknowns: Set<IIdentifier>): void {
    this._result.addHint(ruleName, hintCode, this._id, hintMessage, type, new Set([this._id]), refOmissionErrorResponsible, unknowns);
  }
  addError(ruleName: string, errorCode: string, _errorField: IIdentifier, errorMessage: Localizable[], type: ErrorType, _referencedFields: Set<IIdentifier>, refOmissionErrorResponsible: Set<IIdentifier>, unknowns: Set<IIdentifier>): void {
    this._result.addError(ruleName, errorCode, this._id, errorMessage, type, new Set([this._id]), refOmissionErrorResponsible, unknowns);
  }
  addMessageV2(ruleName: string, errorCode: string, message: Localizable[], type: ErrorType, severity: SeverityType, _possiblyCausingEntities: ICause): void {
    this._result.addMessageV2(ruleName, errorCode, message, type, severity, ErrorCause.createSingleFieldCause(this._id));
  }
  getCountOf(type: ErrorType): number {
    return this._result.getCountOf(type);
  }
}
