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
import { StringUtil } from "@com.mgmtp.a12.utils/utils-localization/a12internal";

import type { IData, IIdentifier } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { Iterator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { HashMap } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";


import { RtExternalIdentifier } from "./RtExternalIdentifier.js";

/**
 * Default implementation of {@link IData}.
 * This class saves a value through an instance of {@link RtExternalIdentifier} (extern implementation of {@link IIdentifier}).
 * The access to the value(s) works in the same way.
 */
export class DatenImpl implements IData {
	protected daten: HashMap<RtExternalIdentifier, string> = new HashMap();

	public getValue(id: IIdentifier): string | undefined {
		return this.daten.get(this._convertToRtIdentifier(id));
	}

	public iterator(): Iterator<IIdentifier> {
		return new Iterator(Array.from(this.daten.keys()));
	}

	public addValue(id: IIdentifier, value: string): void {
		this.daten.set(this._convertToRtIdentifier(id), value);
	}

	/**
	 * @return instance of {@link RtExternalIdentifier}.
	 */
	private _convertToRtIdentifier(externId: IIdentifier): RtExternalIdentifier {
		if (externId instanceof RtExternalIdentifier) {
			return externId;
		} else {
			return new RtExternalIdentifier(externId);
		}
	}

	/**
	 * Liefert die Stringrepräsentation zum übergebenen IIdentifier.
	 *
	 * @param id 	Instanz der Klasse IdentifierImpl
	 */
	static idIdToString(id: IIdentifier): string {
		const indices: number[] = id.getIndices();
		return StringUtil.stringFormat("{0}/{1}.{2}", id.getName(), indices[0], indices[1]);
	}
}
