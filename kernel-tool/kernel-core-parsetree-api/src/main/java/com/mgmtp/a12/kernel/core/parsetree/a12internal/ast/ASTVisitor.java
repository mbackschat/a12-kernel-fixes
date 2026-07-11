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

package com.mgmtp.a12.kernel.core.parsetree.a12internal.ast;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IApplicationCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IComparisonCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IDateValidityCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListAndValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IPredefinedTypeValidityCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IBoolConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ICompositeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperationWithValue;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperationWithParameters;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IGroupOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfRootGroupsOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfUsbOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOpListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOpListOperationWithParameter;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IPointInTimeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ISetOpOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateAddOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateDiffOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateExtractionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateRangeConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateRangeExtractionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateTimeConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.ITimeConstructionOperation;

/**
 * Allgemeine Superklasse für alle Visitors auf den ParserKlassen einer geparsten Bedingung
 */
public abstract class ASTVisitor {
	public boolean visitGeneral(IParseTreeEntity e) {
		return true;
	}

	public void endVisitGeneral(IParseTreeEntity e) {
	}

	public boolean visitNextGeneral(int i, IParseTreeEntity e) {
		return true;
	}

	public boolean visitNext(int i, ICompositeCondition p) {
		return visitNextGeneral(i, p);
	}

	public boolean visitNext(int i, ICompositeOperation p) {
		return visitNextGeneral(i, p);
	}

	public boolean visitNext(int i, IComparisonCondition p) {
		return visitNextGeneral(i, p);
	}

	public boolean visit(ICompositeCondition p) {
		return visitGeneral(p);
	}

	public boolean visit(ICompositeOperation p) {
		return visitGeneral(p);
	}

	public boolean visit(IComparisonCondition p) {
		return visitGeneral(p);
	}

	public boolean visit(IValueListCondition p) {
		return visitGeneral(p);
	}

	public boolean visitNext(int i, IValueListCondition p) {
		return visitNextGeneral(i, p);
	}

	public boolean visit(IEntityListOperation p) {
		return visitGeneral(p);
	}

	public boolean visitNext(int i, IEntityListOperation p) {
		return visitNextGeneral(i, p);
	}

	public void endVisit(IEntityListOperation p) {
		endVisitGeneral(p);
	}

	public boolean visit(IEntityListOperationWithValue p) {
		return visitGeneral(p);
	}

	public boolean visitNext(int i, IEntityListOperationWithValue p) {
		return visitNextGeneral(i, p);
	}

	public void endVisit(IEntityListOperationWithValue p) {
		endVisitGeneral(p);
	}

	public boolean visit(IOpListOperation p) {
		return visitGeneral(p);
	}

	public boolean visitNext(int i, IOpListOperation p) {
		return visitNextGeneral(i, p);
	}

	public void endVisit(IOpListOperation p) {
		endVisitGeneral(p);
	}

	public boolean visit(IOpListOperationWithParameter p) {
		return visitGeneral(p);
	}

	public boolean visitNext(int i, IOpListOperationWithParameter p) {
		return visitNextGeneral(i, p);
	}

	public void endVisit(IOpListOperationWithParameter p) {
		endVisitGeneral(p);
	}

	public void endVisit(IBoolConstantOperation p) {
		endVisitGeneral(p);
	}

	public void endVisit(IPointInTimeOperation p) {
		endVisitGeneral(p);
	}

	public void endVisit(IValueListCondition p) {
		endVisitGeneral(p);
	}

	public void endVisit(IFieldIdOperation p) {
		endVisitGeneral(p);
	}

	public boolean visit(IEntityListAndValueListCondition p) {
		return visitGeneral(p);
	}

	public boolean visitNext(int i, IEntityListAndValueListCondition p) {
		return visitNextGeneral(i, p);
	}

	public void endVisit(IEntityListAndValueListCondition p) {
		endVisitGeneral(p);
	}

	public void endVisit(IComparisonCondition p) {
		endVisitGeneral(p);
	}

	public boolean visit(IEntityId p) {
		return visitGeneral(p);
	}

	public void endVisit(IEntityId p) {
		endVisitGeneral(p);
	}

	public void endVisit(IApplicationCondition p) {
		endVisitGeneral(p);
	}

	public boolean visit(IRootGroupCondition p) {
		return visitGeneral(p);
	}

	public void endVisit(IRootGroupCondition p) {
		endVisitGeneral(p);
	}

	public boolean visit(IEntityListCondition p) {
		return visitGeneral(p);
	}

	public void endVisit(IEntityListCondition p) {
		endVisitGeneral(p);
	}

	public boolean visit(IEntityIdList p) {
		return visitGeneral(p);
	}

	public void endVisit(IEntityIdList p) {
		endVisitGeneral(p);
	}

	public boolean visitNext(int i, IEntityIdList p) {
		return visitNextGeneral(i, p);
	}

	public boolean visit(IFieldIdOperationWithParameters p) {
		return visitGeneral(p);
	}

	public void endVisit(IFieldIdOperationWithParameters p) {
		endVisitGeneral(p);
	}

	public boolean visit(IFieldIdOperation p) {
		return visitGeneral(p);
	}

	public boolean visit(IDateRangeExtractionOperation p) {
		return visitGeneral(p);
	}

	public void endVisit(IDateRangeExtractionOperation p) {
		endVisitGeneral(p);
	}

	public void endVisit(ICompositeCondition p) {
		endVisitGeneral(p);
	}

	public void endVisit(ICompositeOperation p) {
		endVisitGeneral(p);
	}

	public void endVisit(INumberOfUsbOperation p) {
		endVisitGeneral(p);
	}

	public boolean visit(IDateConstructionOperation p) {
		return visitGeneral(p);
	}

	public void endVisit(IDateConstructionOperation p) {
		endVisitGeneral(p);
	}

	public boolean visitNext(int i, IDateConstructionOperation p) {
		return visitNextGeneral(i, p);
	}

	public boolean visit(IDateTimeConstructionOperation p) {
		return visitGeneral(p);
	}

	public void endVisit(IDateTimeConstructionOperation p) {
		endVisitGeneral(p);
	}

	public boolean visitNext(int i, IDateTimeConstructionOperation p) {
		return visitNextGeneral(i, p);
	}

	public boolean visit(ITimeConstructionOperation p) {
		return visitGeneral(p);
	}

	public void endVisit(ITimeConstructionOperation p) {
		endVisitGeneral(p);
	}

	public boolean visitNext(int i, ITimeConstructionOperation p) {
		return visitNextGeneral(i, p);
	}

	public boolean visit(IDateRangeConstructionOperation p) {
		return visitGeneral(p);
	}

	public void endVisit(IDateRangeConstructionOperation p) {
		endVisitGeneral(p);
	}

	public boolean visitNext(int i, IDateRangeConstructionOperation p) {
		return visitNextGeneral(i, p);
	}

	public boolean visit(IDateExtractionOperation p) {
		return visitGeneral(p);
	}

	public void endVisit(IDateExtractionOperation p) {
		endVisitGeneral(p);
	}

	public boolean visit(IDateDiffOperation p) {
		return visitGeneral(p);
	}

	public boolean visitNext(int i, IDateDiffOperation p) {
		return visitNextGeneral(i, p);
	}

	public void endVisit(IDateDiffOperation p) {
		endVisitGeneral(p);
	}

	public boolean visit(IDateAddOperation p) {
		return visitGeneral(p);
	}

	public boolean visitNext(int i, IDateAddOperation p) {
		return visitNextGeneral(i, p);
	}

	public void endVisit(IDateAddOperation p) {
		endVisitGeneral(p);
	}

	public void endVisit(INumberOfRootGroupsOperation p) {
		endVisitGeneral(p);
	}

	public void endVisit(IConstantOperation p) {
		endVisitGeneral(p);
	}

	public boolean visit(IDateValidityCondition p) {
		return visitGeneral(p);
	}

	public boolean visit(IPredefinedTypeValidityCondition p) {
		return visitGeneral(p);
	}

	public void endVisit(IDateValidityCondition p) {
		endVisitGeneral(p);
	}

	public void endVisit(IPredefinedTypeValidityCondition p) {
		endVisitGeneral(p);
	}

	public boolean visit(IGroupOperation p) {
		return visitGeneral(p);
	}

	public void endVisit(IGroupOperation p) {
		endVisitGeneral(p);
	}

	public boolean visit(ISetOpOperation p) {
		return visitGeneral(p);
	}

	public boolean visitNext(int i, ISetOpOperation p) {
		return visitNextGeneral(i, p);
	}

	public void endVisit(ISetOpOperation p) {
		endVisitGeneral(p);
	}
}
