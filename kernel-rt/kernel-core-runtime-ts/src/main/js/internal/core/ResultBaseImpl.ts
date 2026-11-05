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

import {
    SeverityType,
    type ErrorType,
    type ICause,
    type IIdentifier,
    type IResult
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

export class ResultMessageV1 {
    constructor(
        public readonly level: SeverityType,
        public readonly ruleName: string,
        public readonly id: string,
        public readonly type: ErrorType,
        public readonly entityId: IIdentifier,
        public readonly message: Localizable[],
        public readonly referencedFields: Set<IIdentifier>,
        public readonly refOmissionErrorResponsible: Set<IIdentifier>,
        public readonly unknowns: Set<IIdentifier>,
    ) { }
}

export class ResultMessageV2 {
    constructor(
        public readonly ruleName: string,
        public readonly errorCode: string,
        public readonly message: Localizable[],
        public readonly type: ErrorType,
        public readonly severity: SeverityType,
        public readonly possiblyCausingEntities: ICause,
    ) { }
}

export class ResultBaseImpl implements IResult {
    public v1Messages: ResultMessageV1[] = [];
    public v2Messages: ResultMessageV2[] = [];

    addMessageV2(
        ruleName: string,
        errorCode: string,
        message: Localizable[],
        type: ErrorType,
        severity: SeverityType,
        possiblyCausingEntities: ICause
    ): void {
        this.v2Messages.push(new ResultMessageV2(ruleName, errorCode, message, type, severity, possiblyCausingEntities));
    }

    addInfo(
        ruleName: string,
        id: string,
        entityId: IIdentifier,
        message: Localizable[],
        messageType: ErrorType,
        referencedFields: Set<IIdentifier>,
        refOmissionErrorResponsible: Set<IIdentifier>,
        unknowns: Set<IIdentifier>,
    ): void {
        this.v1Messages.push(new ResultMessageV1(SeverityType.INFO, ruleName, id, messageType, entityId, message,
            referencedFields, refOmissionErrorResponsible, unknowns));
    }

    addHint(
        ruleName: string,
        id: string,
        entityId: IIdentifier,
        message: Localizable[],
        messageType: ErrorType,
        referencedFields: Set<IIdentifier>,
        refOmissionErrorResponsible: Set<IIdentifier>,
        unknowns: Set<IIdentifier>,
    ): void {
        this.v1Messages.push(new ResultMessageV1(SeverityType.HINT, ruleName, id, messageType, entityId, message,
            referencedFields, refOmissionErrorResponsible, unknowns));
    }

    addError(
        ruleName: string,
        id: string,
        entityId: IIdentifier,
        message: Localizable[],
        messageType: ErrorType,
        referencedFields: Set<IIdentifier>,
        refOmissionErrorResponsible: Set<IIdentifier>,
        unknowns: Set<IIdentifier>,
    ): void {
        this.v1Messages.push(new ResultMessageV1(SeverityType.ERROR, ruleName, id, messageType, entityId, message,
            referencedFields, refOmissionErrorResponsible, unknowns));
    }

    get errorMessages() {
        return this.getMessagesOfLevel(SeverityType.ERROR);
    }

    getMessagesOfLevel(level: SeverityType) {
        return this.v1Messages.filter(message => message.level === level);
    }

    getCountOf(type: ErrorType): number {
        return this.v1Messages.filter(message => message.type === type).length;
    }

    get numErrors(): number {
        return this.errorMessages.length;
    }

    getV1AndV2Messages(): { v1Message: ResultMessageV1, v2Message: ResultMessageV2 }[] {
        return this.v1Messages.map((v1Message, i) => ({ v1Message, v2Message: this.v2Messages[i] }));
    }
}
