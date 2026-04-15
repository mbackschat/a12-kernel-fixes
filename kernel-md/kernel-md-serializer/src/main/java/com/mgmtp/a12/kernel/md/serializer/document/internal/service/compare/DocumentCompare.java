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
package com.mgmtp.a12.kernel.md.serializer.document.internal.service.compare;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.services.IEntityInstanceChangeInfo;
import com.mgmtp.a12.kernel.md.document.internal.service.utils.DocumentUtils;

/**
 * @deprecated use {@code DocumentCompareV2} instead.
 */
@Deprecated(since = "30.0.0")
public class DocumentCompare {

	public Collection<IEntityInstanceChangeInfo> compare(IDocument document, IDocument reference) {
		Collection<IEntityInstanceChangeInfo> result = new ArrayList<>();
		// First loop to iterate over field instances of the input document.
		for (IEntityInstance newEi : document.getEntityInstances()) {
			if (!(newEi instanceof IFieldInstance)) { // Only filed instances will be considered in the comparison.
				continue;
			}
			IFieldInstance newFi = (IFieldInstance) newEi;
			Optional<IFieldInstance> refFi = DocumentUtils.getFieldInstance(reference, newFi.getPath(), newFi.getRepetitions());
			if (refFi.isEmpty()) {
				Object newVal = getConcreteValue(newFi);
				if (newVal != null) {
					// New
					result.add(new EiChangeInfo(newFi.getPath(), newFi.getRepetitions(),
							IEntityInstanceChangeInfo.EntityInstanceChangeType.NEW, newVal));
				}
			} else {
				Object refVal = getConcreteValue(refFi.get());
				Object newVal = getConcreteValue(newFi);
				if (!Objects.deepEquals(refVal, newVal)) {
					if (refVal == null) {
						// New
						result.add(new EiChangeInfo(newFi.getPath(), newFi.getRepetitions(),
								IEntityInstanceChangeInfo.EntityInstanceChangeType.NEW, newVal));
					} else if (newVal == null) {
						// Deleted
						result.add(new EiChangeInfo(newFi.getPath(), newFi.getRepetitions(),
								IEntityInstanceChangeInfo.EntityInstanceChangeType.DELETED, null));
					} else {
						// Updated
						result.add(new EiChangeInfo(newFi.getPath(), newFi.getRepetitions(),
								IEntityInstanceChangeInfo.EntityInstanceChangeType.UPDATED, newVal));
					}
				}
			}
		}
		// Second loop to find those field instances which are present in the reference document but not in the present document.
		for (IEntityInstance refEi : reference.getEntityInstances()) {
			if (!(refEi instanceof IFieldInstance)) { // Only filed instances will be considered in the comparison.
				continue;
			}
			IFieldInstance refFi = (IFieldInstance) refEi;
			Optional<IFieldInstance> newFi = DocumentUtils.getFieldInstance(document, refFi.getPath(), refFi.getRepetitions());
			if (newFi.isEmpty()) {
				Object refVal = getConcreteValue(refFi);
				if (refVal != null) {
					// Deleted
					result.add(new EiChangeInfo(refFi.getPath(), refFi.getRepetitions(),
							IEntityInstanceChangeInfo.EntityInstanceChangeType.DELETED, null));
				}
			}
		}
		return result;
	}

	private Object getConcreteValue(IFieldInstance fi) {
		Optional<Object> val = fi.getValue();
		if (val.isEmpty()) {
			return null;
		} else if (val.get() instanceof String && ((String) val.get()).isEmpty()) {
			return null;
		} else {
			return val.get();
		}
	}

	private static class EiChangeInfo implements IEntityInstanceChangeInfo {
		private String path;
		private int[] reps;
		private EntityInstanceChangeType changeType;
		private Object changedValue;

		private EiChangeInfo(String path, int[] reps, EntityInstanceChangeType changeType, Object changedValue) {
			this.path = path;
			this.reps = reps;
			this.changeType = changeType;
			this.changedValue = changedValue;
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public int[] getRepetitions() {
			return reps;
		}

		@Override
		public EntityInstanceChangeType getChangeType() {
			return changeType;
		}

		@Override
		public Object getChangedValue() {
			return changedValue;
		}

		@Override public String toString() {
			return "EiChangeInfo{" +
					"path='" + path + '\'' +
					", reps=" + Arrays.toString(reps) +
					", changeType=" + changeType +
					", changedValue=" + changedValue +
					'}';
		}
	}

}
