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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.util;

import java.util.Iterator;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * Liefert eine Implementierung des Interfaces {@link IData}, das aus einer vorgegebenen Instanz
 * Felder filtern kann.
 */
public class DatenFilterImpl implements IData {

	private IData originalData;
	private Predicate<IIdentifier> filter;

	/**
	 * @param origin
	 * 		die zu wrappende Instanz.
	 * @param filter
	 * 		ein Filter, der angibt, ob ein konkreter IIdentifier zurückgeliefert werden soll.
	 */
	public DatenFilterImpl(IData origin, Predicate<IIdentifier> filter) {
		this.originalData = origin;
		this.filter = filter;
	}

	@Override
	public String getValue(IIdentifier entity) {
		if (!filter.test(entity)) {
			return null;
		}
		return originalData.getValue(entity);
	}

	@Override
	public String getValueForDisplay(IIdentifier entity) {
		if (!filter.test(entity)) {
			return null;
		}
		return originalData.getValueForDisplay(entity);
	}

	@Override
	public Iterator<IIdentifier> iterator() {
		return new FilteringIterator<>(originalData.iterator(), filter);
	}

	@Override
	public String identifierToString(IIdentifier feld) {
		return originalData.identifierToString(feld);
	}

	@Override
	public void addValue(IIdentifier feld, String wert) {
		throw new UnsupportedOperationException();
	}

}
