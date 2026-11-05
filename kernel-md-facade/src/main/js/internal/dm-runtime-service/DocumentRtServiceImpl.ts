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


import type { Logger } from "@com.mgmtp.a12.utils/utils-logging";

import type {
  ICalculator,
  IData,
  IIdentifier,
  IMetaModel
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import {
  ValidationMode
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { LoggerDummy } from "@com.mgmtp.a12.kernel/kernel-core-runtime-ts/internal";

import { ExternalCalculationError } from "@com.mgmtp.a12.kernel/kernel-core-runtime-ts/internal";

import type {
  Document,
  DocumentComputationResult,
  DocumentValidationResult,
  EntityInstancePath,
  EntityInstanceValueMapping,
  GeneratedCodeRtConfig,
  IGeneratedCodeAccessor
} from "../../api.js";

import type { DocumentRtServiceA12internal, ExternalComputation } from "../../a12internal/compute.js";

import { DocumentValidationResultImpl } from "../../a12internal/DocumentValidationResultImpl.js";

import { DocumentComputationResultImpl } from "../../a12internal/DocumentComputationResultImpl.js";

import { EntityInstanceConverter } from "../EntityInstanceConverter.js";

import { CustomConditionFactoryWrapper } from "./CustomConditionFactoryWrapper.js";
import { ExternalComputationError } from "./ExternalComputationError.js";
import { ExternalComputationWrapper } from "./ExternalComputationWrapper.js";
import { ValidationData } from "./ValidationData.js";

// implementation similar to DocumentAbstractRtService in Java
export class DocumentRtServiceImpl implements DocumentRtServiceA12internal {
  constructor(
    private _genCodeAccessor: IGeneratedCodeAccessor,
    private _config?: GeneratedCodeRtConfig
  ) { }

  private _validationMode(): ValidationMode {
    const validationMode = new ValidationMode();
    if (this._config?.currentDateForTest) {
      validationMode.setCurrentDateForTest(this._config?.currentDateForTest);
    }
    if (this._config?.customConditionFactory) {
      validationMode.customConditionFactory = new CustomConditionFactoryWrapper(this._config?.customConditionFactory);
    }
    validationMode.customFieldTypeFactory = this._config?.customFieldTypeFactory;
    return validationMode;
  }

  validatePart(
    document: Document,
    relevantEntityInstances: EntityInstancePath[],
    mapping?: EntityInstanceValueMapping
  ): DocumentValidationResult {
    const { data, result, validationMode, logger } = this._prepareParameter(document, mapping);

    // add ids of global fields
    const relevantIds = EntityInstanceConverter.convertToIIdentifierSet(relevantEntityInstances) as any;
    this._addGlobalFieldsToRelevantEntities(this._genCodeAccessor.getMetaModel(), relevantIds);

    // fill result
    this._genCodeAccessor
      .getValidator()
      .validatePartlyWith3ValueLogic(
        data,
        relevantIds,
        result,
        validationMode,
        logger
      );
    return result;
  }

  private _addGlobalFieldsToRelevantEntities(metaModel: IMetaModel, relevantIds: Set<IIdentifier>): void {
    const globalIds: Set<IIdentifier> = metaModel.getValue("GlobalFields");
    globalIds.forEach((globalId: IIdentifier) => relevantIds.add(globalId));
  }

  validateFull(document: Document, mapping?: EntityInstanceValueMapping): DocumentValidationResult {
    const { data, result, validationMode, logger } = this._prepareParameter(document, mapping);

    // fill result
    this._genCodeAccessor.getValidator().validateAll(data, result, validationMode, logger);
    return result;
  }

  compute(document: Document): DocumentComputationResult {
    return this.computeA12internal(document);
  }

  computeA12internal(document: Document, externalComputations?: ExternalComputation[], changedFieldInstances?: EntityInstancePath[]): DocumentComputationResult {
    const { data, result: formalErrors, validationMode, logger } = this._prepareParameter(document);

    const externalCalculations = externalComputations?.map(externalComputation => new ExternalComputationWrapper(externalComputation));
    const changedFieldIds = changedFieldInstances == undefined
      ? changedFieldInstances
      : EntityInstanceConverter.convertToIIdentifierArray(changedFieldInstances);

    const calculator: ICalculator = this._genCodeAccessor.getCalculator();

    let calculatedData: IData;
    try {
      calculatedData = calculator.calculateAll(data, validationMode, formalErrors, logger, externalCalculations, changedFieldIds);
    } catch (e) {
      if (e instanceof ExternalCalculationError && e.externalCalculation instanceof ExternalComputationWrapper) {
        const externalComputation = e.externalCalculation.externalComputation;
        throw ExternalComputationError.fromError(externalComputation, e, e.rawMessage);
      }
      throw e;
    }

    return new DocumentComputationResultImpl(
      document,
      data,
      calculatedData,
      formalErrors,
      this._genCodeAccessor.getMetaModel()
    );
  }

  private _prepareParameter(document: Document, mapping?: EntityInstanceValueMapping): { data: ValidationData, result: DocumentValidationResultImpl, validationMode: ValidationMode, logger: Logger } {
    const result = new DocumentValidationResultImpl();
    const data = new ValidationData(
      document,
      this._genCodeAccessor.getMetaModel(),
      result,
      mapping,
      this._config?.ignoreUnknownFields
    );
    result.data = data;
    const logger = new LoggerDummy();
    const validationMode = this._validationMode();
    return { data, result, validationMode, logger };
  }
}
