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
export { BedingungsOperatorHelper, VglOp } from "./core/BedingungsOperatorHelper.js";
export { CalculationCommand } from "./core/CalculationCommand.js";
export { CheckCommand } from "./core/CheckCommand.js";
export { ExternalCalculationError } from "./core/ExternalCalculationRegistry.js";
export { MainValidatorController, PruefErgebnisTyp } from "./core/MainValidatorController.js";
export { RepetitionNotUniqueErrorCache } from "./core/RepetitionNotUniqueErrorCache.js";
export { ResultBaseImpl, ResultMessageV1, ResultMessageV2 } from "./core/ResultBaseImpl.js";
export { RuntimeController } from "./core/RuntimeController.js";
export { ValidationCommand } from "./core/ValidationCommand.js";
export { Container_t } from "./formatdef/Container_t.js";
export { Feld_t } from "./formatdef/Feld_t.js";
export { FormatDefinitionDatum } from "./formatdef/FormatDefinitionDatum.js";
export { FormatDefinitionDatumBereich, Jahresinterpretation } from "./formatdef/FormatDefinitionDatumBereich.js";
export { FormatDefinitionJa } from "./formatdef/FormatDefinitionJa.js";
export { FormatDefinitionJaNein } from "./formatdef/FormatDefinitionJaNein.js";
export { FormatDefinitionString } from "./formatdef/FormatDefinitionString.js";
export { FormatDefinitionVordefiniert } from "./formatdef/FormatDefinitionVordefiniert.js";
export { FormatDefinitionZahl } from "./formatdef/FormatDefinitionZahl.js";
export { Identifier_t, ReferenzTyp } from "./formatdef/Identifier_t.js";
export { FeldFormatEnumType } from "./formatdef/IFormatDefinition.js";
export type { IFormatDefinition } from "./formatdef/IFormatDefinition.js";
export { Model_t } from "./formatdef/Model_t.js";
export { Regel_t } from "./formatdef/Regel_t.js";
export { ValidationLogger } from "./logging/ValidationLogger.js";
export { MapBuilder } from "./meta/MapBuilder.js";
export { MetaContainer } from "./meta/MetaContainer.js";
export { MetaDataHelper } from "./meta/MetaDataHelper.js";
export { MetaFeld } from "./meta/MetaFeld.js";
export { MetaIdentifier } from "./meta/MetaIdentifier.js";
export { MetaLokalePflichtInfo } from "./meta/MetaLokalePflichtInfo.js";
export { MetaModelImpl } from "./meta/MetaModelImpl.js";
export { MetaRegel } from "./meta/MetaRegel.js";
export { ArrayUtils } from "./util/ArrayUtils.js";
export { CalculationSorter } from "./util/CalculationSorter.js";
export { Constants } from "./util/Constants.js";
export { DatenImpl } from "./util/DatenImpl.js";
export { DatumTeilbekanntArt } from "./util/DatumTeilbekanntArt.js";
export { EbenenIterator } from "./util/EbenenIterator.js";
export { ErrorCause } from "./util/ErrorCause.js";
export type { FilterPredicate } from "./util/FilterPredicate.js";
export { IFormaleFehlerConstants } from "./util/IFormaleFehlerConstants.js";
export { IMetaKeysInternal } from "./util/IMetaKeysInternal.js";
export { IterationState } from "./util/IterationState.js";
export { ListBuilder } from "./util/ListBuilder.js";
export { LocalizableUtil } from "./util/LocalizableUtil.js";
export { LoggerDummy } from "./util/LoggerDummy.js";
export { MetaFeldUtil } from "./util/MetaFeldUtil.js";
export { RtExternalIdentifier } from "./util/RtExternalIdentifier.js";
export { RtIdentifierTemplate } from "./util/RtIdentifierTemplate.js";
export { RtInternalIdentifier } from "./util/RtInternalIdentifier.js";
export { RuntimeUtils } from "./util/RuntimeUtils.js";
export { TrieNode } from "./util/TrieNode.js";
export { VkString } from "./util/VkString.js";

