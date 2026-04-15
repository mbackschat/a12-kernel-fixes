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

package com.mgmtp.a12.kernel.core.codegen.internal.utils;

import java.util.List;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.RepetitionNotUniqueCreator;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;

/**
 * Schnittstelle für CodeGen und CalcGen RegelObjekte zur Verwendung im
 * {@link RepetitionNotUniqueCreator}.
 */
public interface IRepetitionNotUniqueHandler {

	String getFullRuleName();

	boolean isHModel();

	boolean isProgLangWithUsb();

	/**
	 * Return the parts of the rule necessary for code generation.
	 * For rules, this will simply be the condition itself.
	 * For calculations, the individual parts will be accumulated.
	 */
	List<IParseTreeEntity> getRulePartsForCodeGeneration();

	/**
	 * @return the layer from which on duplicates are searched for or -1 if no RepetitionNotUnique were present. Adjusted for USB if
	 *        {@link #isProgLangWithUsb()} is {@code true}.
	 */
	int getEbeneAbDerDuplikatGesucht();

	/**
	 * @param level
	 * 		the level from which on duplicates are searched for or -1 if no RepetitionNotUnique were present. Ajusted for USB if
	 *        {@link #isProgLangWithUsb()} is {@code true}.
	 */
	void setEbeneAbDerDuplikatGesucht(int level);

}