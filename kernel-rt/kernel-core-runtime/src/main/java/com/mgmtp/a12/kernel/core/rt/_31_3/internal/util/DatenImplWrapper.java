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
 * Die Klasse wird verwendet, wenn die von extern übergebene identifierToString Methode
 * verwendet werden muss und zusätzlich ein eigenes Objekt erzeugt werden muss.
 */
public class DatenImplWrapper implements IData {
	private final IData externalDatenInterface; // wird nur für die Implementierung von identifierToSTring benötigt.
	private final DatenImpl internalDatenImpl = new DatenImpl();

	public DatenImplWrapper(IData externalDatenInterface) {

		this.externalDatenInterface = externalDatenInterface;
	}

	/*
	 * verwende die interne DatenImplmentierung
	 */
	@Override
	public String getValue(IIdentifier entity) {
		return internalDatenImpl.getValue(entity);
	}

	@Override
	public String getValueForDisplay(IIdentifier entity) {
		return getValue(entity);
	}

	/*
	 * verwende die interne DatenImplmentierung
	 */
	@Override
	public Iterator<IIdentifier> iterator() {
		return internalDatenImpl.iterator();
	}

	/*
	 * verwende die extern übergebene IdentifierToString Methode
	 */
	@Override
	public String identifierToString(IIdentifier feld) {
		return externalDatenInterface.identifierToString(feld);
	}

	@Override
	public void addValue(IIdentifier feld, String wert) {
		internalDatenImpl.addValue(feld, wert);
	}

}
