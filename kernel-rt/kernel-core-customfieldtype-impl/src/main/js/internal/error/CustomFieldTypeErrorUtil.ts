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
import type { Localizable, LocalizableArgs, PlainPlaceholder } from "@com.mgmtp.a12.utils/utils-localization";

import type {
	ICustomFieldTypeCheckError
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import {
	StaticResourceProvider
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";


import { customFieldTypeCheckError } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

import { de, en, fr, nl } from "./bundles/index.js";
import { CustomFieldTypeErrorEnum } from "./CustomFieldTypeErrorEnum.js";

const defaultResourceProvider = new StaticResourceProvider(de, en, fr, nl);

export class CustomFieldTypeErrorUtil {
	public static readonly ERROR_MESSAGES_RESOURCE_NAME = "custom_field_types/error_messages";

	static getResult(
		error: CustomFieldTypeErrorEnum,
		errorKey: string,
		args?: LocalizableArgs
	): ICustomFieldTypeCheckError | undefined {
		const _erroMsg = CustomFieldTypeErrorUtil.getLocalizableErrorMessage(error, args);
		if (!_erroMsg) {
			return undefined;
		}
		return customFieldTypeCheckError(errorKey, [_erroMsg]);
	}

	private static getLocalizableErrorMessage(
		error: CustomFieldTypeErrorEnum,
		args?: LocalizableArgs
	): Localizable | undefined {
		if (CustomFieldTypeErrorEnum.KEIN_FEHLER === error) {
			return undefined;
		}
		const key = CustomFieldTypeErrorUtil.createCustomTypeErrorKey(error);
		const defaults = CustomFieldTypeErrorUtil.getAllErrorTexts(error);
		return {
			key, defaults, args
		}
	}

	public static createPlainPlaceholder(value: string): PlainPlaceholder {
		return { type: "plain", value };
	}

	private static createCustomTypeErrorKey(error: CustomFieldTypeErrorEnum): string {
		return `kernel.customTypeErrors.${CustomFieldTypeErrorEnum[error]}`;
	}

	private static getAllErrorTexts(key: CustomFieldTypeErrorEnum): { readonly [locale: string]: string | undefined } {
		return defaultResourceProvider.getAllTextsForKey(CustomFieldTypeErrorEnum[key]);
	}
}
