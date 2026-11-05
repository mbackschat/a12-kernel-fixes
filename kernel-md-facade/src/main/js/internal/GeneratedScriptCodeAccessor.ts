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
import * as ValidationRuntimeApiPublic from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts";
import * as ValidationRuntimeApi from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import * as ValidationRuntime from "@com.mgmtp.a12.kernel/kernel-core-runtime-ts/internal";

import type {
	ICalculator,
	IMetaModel,
	IValidator
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import type { IGeneratedCodeAccessor } from "../api.js";

interface GeneratedCodeScript {
	(rta: object, rt: object, rtap: object): {
		validator: {
			getInstance(): IValidator & ICalculator;
		};
		meta: {
			getInstance(): IMetaModel;
		};
	};
}

class CodeAccessorHolder {
	constructor(public validator: IValidator & ICalculator, public metaModel: IMetaModel) { }
}

export class GeneratedScriptCodeAccessor implements IGeneratedCodeAccessor {
	private holder: CodeAccessorHolder | undefined;
	constructor(private script: string) { }
	getValidator(): IValidator {
		this.holder ??= this.initialize()
		return this.holder.validator
	}
	getMetaModel(): IMetaModel {
		this.holder ??= this.initialize()
		return this.holder.metaModel
	}
	getCalculator(): ICalculator {
		this.holder ??= this.initialize()
		return this.holder.validator
	}
	private initialize(): CodeAccessorHolder {
		const Validator = <GeneratedCodeScript>(
			new Function("ValidationRuntimeApi", "ValidationRuntime", "ValidationRuntimeApiPublic", this.script)
		);
		const struct = Validator(ValidationRuntimeApi, ValidationRuntime, ValidationRuntimeApiPublic);
		return new CodeAccessorHolder(
			struct.validator.getInstance(),
			struct.meta.getInstance()
		);
	}
}