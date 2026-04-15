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

import type { ErrorType, IIdentifier, IResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

export type MessageLevel = "Info" | "Hint" | "Error";

export class ResultMessage {
    constructor(
        public readonly level: MessageLevel,
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

export class CollectingResultImpl implements IResult {
    public messages: ResultMessage[] = [];

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
        this.messages.push(new ResultMessage("Info", ruleName, id, messageType, entityId, message,
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
        this.messages.push(new ResultMessage("Hint", ruleName, id, messageType, entityId, message,
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
        this.messages.push(new ResultMessage("Error", ruleName, id, messageType, entityId, message,
            referencedFields, refOmissionErrorResponsible, unknowns));
    }

    get errorMessages() {
        return this.getMessagesOfLevel("Error");
    }

    getMessagesOfLevel(level: MessageLevel) {
        return this.messages.filter(message => message.level === level);
    }

    getCountOf(type: ErrorType): number {
        return this.messages.filter(message => message.type === type).length;
    }

    get numErrors(): number {
        return this.errorMessages.length;
    }
}
