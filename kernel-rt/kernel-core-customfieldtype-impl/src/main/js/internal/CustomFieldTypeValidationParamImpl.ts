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

import type {
	ICustomFieldTypeValidationParam
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";

/**
 * Parameter class used for validating custom field types.
 *
 * This file has copies in kernel-core-custom-fieldtype-impl and kernel-core-runtime-impl.
 */
export class CustomFieldTypeValidationParamImpl implements ICustomFieldTypeValidationParam {
	public static readonly UNSPECIFIED_LENGTH: number = -1;

	/**
	 * @param _minLength minimal length allowed or {@link #UNSPECIFIED_LENGTH}
	 * @param _maxLength maximal length allowed or {@link #UNSPECIFIED_LENGTH}
	 */
	public constructor(
		private _minLength: number,
		private _maxLength: number
	) { }

	/**
	 * <b>Note:</b> The min. length should NOT be used by the validation in the implementation of
	 * {@link ICustomFieldValidator#validate(String, ICustomFieldTypeValidationParam, boolean)} if the
	 * value is constant for the predefined type i.e. if it can not be specified by the user of the
	 * predefined type. Currently it is the case for all predefined types except
	 * <code>EMail</code><br>
	 * <b>Reason:</b> for the operators <code>Gueltig / Ungueltig</code> for predefined types the
	 * constant min. length defined by the type is not known so {@link #UNSPECIFIED_LENGTH} is used.
	 */
	getMinLength(): number | undefined {
		return this._minLength === CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH
			? undefined
			: this._minLength;
	}

	/**
	 * <b>Note:</b> The max. length should NOT be used by the validation in the implementation of
	 * {@link ICustomFieldValidator#validate(String, ICustomFieldTypeValidationParam, boolean)} if the
	 * value is constant for the predefined type i.e. if it can not be specified by the user of the
	 * predefined type. Currently it is the case for all predefined types except
	 * <code>EMail</code><br>
	 * <b>Reason:</b> for the operators <code>Gueltig / Ungueltig</code> for predefined types the
	 * constant max. length defined by the type is not known so {@link #UNSPECIFIED_LENGTH} is used.
	 */
	getMaxLength(): number | undefined {
		return this._maxLength === CustomFieldTypeValidationParamImpl.UNSPECIFIED_LENGTH
			? undefined
			: this._maxLength;
	}
}
