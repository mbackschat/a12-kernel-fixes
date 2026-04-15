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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.util;

import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICause;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;

/**
 * Einfache Implementierung von {@link IResult}.
 * <p>
 * {@link ResultNoTestId} enthält immer eine {@link IResult}-Instanz an die alle Methodenaufrufe
 * weiterdeligiert werden. Im Falle aber, dass als Info-, Hinweis- oder Fehler-Code
 * "testIDNummer" übergeben wird, werden die jeweiligen Aufrufe nicht an die Delegate-Instanz
 * weitergeleitet. Damit werden diese "Fehler" / "Hinweise" / "Infos" herausgefiltert.
 */
public class ResultNoTestId implements IResult {

	private IResult delegate;

	/**
	 * @param delegate
	 */
	public ResultNoTestId(IResult delegate) {
		this.delegate = delegate;
	}

	@Override public void addMessageV2(String ruleName, String errorCode, String messageStr, ErrorType type, SeverityType severity,
			ICause possiblyCausingEntities) {
		if (!IFormaleFehlerConstants.TEST_ID_NUMMER.equals(errorCode)) {
			delegate.addMessageV2(ruleName, errorCode, messageStr, type, severity, possiblyCausingEntities);
		}
	}

	@Override
	public void addInfo(String regelName, String infoCode, IIdentifier infoFeld, String infoText, ErrorType type,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		if (!IFormaleFehlerConstants.TEST_ID_NUMMER.equals(infoCode)) {
			delegate.addInfo(regelName, infoCode, infoFeld, infoText, type, referencedFields, refOmissionErrorResponsible,
					unknowns);
		}
	}

	@Override
	public void addHint(String regelName, String hinweisCode, IIdentifier hinweisFeld, String hinweisText, ErrorType typ,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		if (!IFormaleFehlerConstants.TEST_ID_NUMMER.equals(hinweisCode)) {
			delegate.addHint(regelName, hinweisCode, hinweisFeld, hinweisText, typ, referencedFields, refOmissionErrorResponsible,
					unknowns);
		}
	}

	@Override
	public void addError(String regelName, String fehlerCode, IIdentifier fehlerFeld, String fehlerText, ErrorType typ,
			Set<IIdentifier> referencedFields, Set<IIdentifier> refOmissionErrorResponsible, Set<IIdentifier> unknowns) {
		if (!IFormaleFehlerConstants.TEST_ID_NUMMER.equals(fehlerCode)) {
			delegate.addError(regelName, fehlerCode, fehlerFeld, fehlerText, typ, referencedFields, refOmissionErrorResponsible,
					unknowns);
		}
	}

	@Override
	public int getCountOf(ErrorType typ) {
		return delegate.getCountOf(typ);
	}

	@Override public int getCount(ErrorType type, SeverityType severityThreshold) {
		return delegate.getCount(type, severityThreshold);
	}
}
