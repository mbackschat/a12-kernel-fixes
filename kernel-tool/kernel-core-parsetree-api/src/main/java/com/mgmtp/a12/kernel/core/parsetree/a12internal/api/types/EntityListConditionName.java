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

package com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;

/**
 * Represents possible conditions on one or more entities (fields or groups), see
 * {@link IEntityListCondition}. The names correspond to the language constructs, but not all
 * language constructs are covered in the constants, because some constructs for single fields or
 * groups are treated internally as constructs for multiple entities:
 * <ul>
 * <li>'FieldFilled' is treated as 'AllFieldsFilled'</li>
 * <li>'FieldNotFilled' is treated as 'NoFieldFilled'</li>
 * <li>'GroupFilled' is treated as 'AllGroupsFilled'</li>
 * <li>'GroupNotFilled' is treated as 'NoGroupFilled'</li>
 * </ul>
 */
public enum EntityListConditionName {
	AllFieldsFilled("AlleFelderAngegeben"),
	MoreThanOneFieldFilled("MehrAlsEinFeldAngegeben"),
	FieldsNotCollectivelyFilled("FelderNichtGemeinsamAngegeben"),
	NoFieldFilled("KeinFeldAngegeben"),
	ZeilenNichtAufsteigend("ZeilenNichtAufsteigend"),
	ZeilenLueckenlos("ZeilenLueckenlos"),
	AtLeastOneFieldFilled("MindestensEinFeldAngegeben"),
	NotAllFieldsFilled("NichtAlleFelderOderKeinFeldAngegeben"),
	NotExactlyOneFieldFilled("NichtGenauEinFeldAngegeben"),
	ZeileNichtEindeutig("ZeileNichtEindeutig"),
	RepetitionNotUnique("WiederholungNichtEindeutig"),
	FieldValuesNotUnique("FeldWerteNichtEindeutig"),
	DateRangesOverlap("DatumsBereicheUeberlappend"),
	NotAllGroupsFilled("NichtAlleOderKeinKontextAngegeben"),
	AtLeastOneGroupFilled("MindestensEinKontextAngegeben"),
	NoGroupFilled("KeinKontextAngegeben"),
	GroupsNotCollectivelyFilled("KontexteNichtGemeinsamAngegeben"),
	AllGroupsFilled("AlleKontexteAngegeben");
	private String runtimeName;

	EntityListConditionName(String runtimeName) {
		this.runtimeName = runtimeName;
	}

	public String getRuntimeName() {
		return runtimeName;
	}
}
