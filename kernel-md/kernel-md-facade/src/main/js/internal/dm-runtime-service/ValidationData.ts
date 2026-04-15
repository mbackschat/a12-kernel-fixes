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
  IData,
  IIdentifier,
  IIterator,
  IMetaModel,
  IResult
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import {
  Iterator
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { RtExternalIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-ts/internal";

import type {
  Document,
  EntityInstanceValueMapping,
} from "../../api.js";

import { ValidationDataCache } from "./ValidationDataCache.js";

export class ValidationData implements IData {
  private _validationDataCache: ValidationDataCache;

  constructor(
    public document: Document,
    public metaModel: IMetaModel,
    private _result: IResult,
    public valuesOfFieldInstancesToConsider?: EntityInstanceValueMapping,
    ignoreUnknownFields?: boolean,
  ) {
    this._validationDataCache = new ValidationDataCache(this.document, valuesOfFieldInstancesToConsider, this.metaModel, this._result, ignoreUnknownFields);
  }

  getValue(entity: IIdentifier): string | undefined {
    const _entity = entity instanceof RtExternalIdentifier ? entity : new RtExternalIdentifier(entity);
    return this._validationDataCache.getStringValue(_entity);
  }

  getTypedValue(entity: IIdentifier): SupportedType | undefined {
    const _entity = entity instanceof RtExternalIdentifier ? entity : new RtExternalIdentifier(entity);
    return this._validationDataCache.getValue(_entity);
  }

  /**
   * This does not filter entities that are not present in the Document Model.
   * @returns all the paths present in the Document or in the additional mapping.
   */
  iterator(): IIterator<RtExternalIdentifier> {
    // the cache contains all paths from the mapping and the document
    return new Iterator(this._validationDataCache.keys());
  }


  addValue(_field: IIdentifier, _value: string): void {
    throw new Error("document is not modifiable");
  }
}
