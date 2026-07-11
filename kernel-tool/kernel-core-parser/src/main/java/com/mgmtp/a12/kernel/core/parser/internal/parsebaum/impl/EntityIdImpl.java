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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.constants.EntityIdConstants;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRefForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.util.SemanticIndexRef;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;

public class EntityIdImpl implements IEntityId {

	private final long[] ebenen;
	private final long[] maxWerte;
	private final long maxUsb;
	private long zeileSternStart = 1;
	private long zeileSternEnd = 1;
	private long lfdNrSternStart = 1;
	private long lfdNrSternEnd = 1;
	private String fullName;
	private long usb = IParseTreeEntity.UNSPECIFIED;

	private final boolean isEchtesFeld;
	private final boolean isHmodel;
	private final String categoryName;
	private final List<Object> semanticIndex;

	private int startIdxInCondition = -1;
	private int stopIdxInCondition = -1;
	private boolean isReferencedByShortName = false;
	private PathNotationConfig pathNotationConfig = PathNotationConfig.DEFAULT_MAYBELOSSY;
	private final boolean isOuter;

	private final ICondition filterCondition;

	// make sure that this constructor sets all fields, so that it can be used to ensure completeness of the with* methods.
	private EntityIdImpl(long[] ebenen, long[] maxWerte, long maxUsb, long zeileSternStart, long zeileSternEnd,
			long lfdNrSternStart,
			long lfdNrSternEnd, String fullName, long usb, boolean isEchtesFeld, boolean isHmodel, String categoryName,
			List<Object> semanticIndex, int startIdxInCondition, int stopIdxInCondition, boolean isReferencedByShortName,
			PathNotationConfig pathNotationConfig, boolean isOuter, ICondition filterCondition) {
		this.ebenen = ebenen;
		this.maxWerte = maxWerte;
		this.maxUsb = maxUsb;
		this.zeileSternStart = zeileSternStart;
		this.zeileSternEnd = zeileSternEnd;
		this.lfdNrSternStart = lfdNrSternStart;
		this.lfdNrSternEnd = lfdNrSternEnd;
		this.fullName = fullName;
		this.usb = usb;
		this.isEchtesFeld = isEchtesFeld;
		this.isHmodel = isHmodel;
		this.categoryName = categoryName;
		this.semanticIndex = semanticIndex;
		this.startIdxInCondition = startIdxInCondition;
		this.stopIdxInCondition = stopIdxInCondition;
		this.isReferencedByShortName = isReferencedByShortName;
		this.pathNotationConfig = pathNotationConfig;
		this.isOuter = isOuter;
		this.filterCondition = filterCondition;
	}

	public EntityIdImpl(EntityRef ref, boolean isHmodel, IEC ec) {
		IEntity e = ref.getEntity();
		this.fullName = e.getFullName();
		if (e.isField()) {
			this.maxWerte = EntityUtils.getMax(e, isHmodel);
		} else {
			this.maxWerte = getMaxWerteForGroup(e, ec, isHmodel);
		}
		this.maxUsb = EntityUtils.getMaxUsb(e);
		this.isEchtesFeld = e.isField();
		this.isHmodel = isHmodel;
		this.categoryName = ref.getCategoryName();
		this.isOuter = ref.isOuter();

		if (isHmodel) {
			EntityRefForHModel ei = (EntityRefForHModel) ref;
			this.ebenen = new long[2];
			if (EntityUtils.hasMaxUsb(e)) {
				this.usb = ei.getUsb();
			}
			this.lfdNrSternEnd = ei.getLfdNrSternEnd();
			this.lfdNrSternStart = ei.getLfdNrSternStart();
			this.ebenen[0] = ei.getLfdNr();

			this.zeileSternEnd = ei.getZeileSternEnd();
			this.zeileSternStart = ei.getZeileSternStart();
			this.ebenen[1] = ei.getZeile();
		} else {
			List<Long> indices = ref.getIndices();
			this.ebenen = getEbenen(indices);
			if (indices.get(0) == IParseTreeEntity.WILDCARD) {
				lfdNrSternEnd = maxWerte[0];
			}

		}
		if (ref.getSemanticIndex() != null) {
			SemanticIndexRef semIdx = ref.getSemanticIndex();
			if (semIdx.isConstant()) {
				semanticIndex = Collections.singletonList(semIdx.getAsConstant());
			} else {
				semanticIndex = Collections.singletonList(new EntityIdImpl(semIdx.getAsEntity(), isHmodel, ec));
			}
		} else {
			this.semanticIndex = Collections.emptyList();
		}
		this.filterCondition = ref.getFilterCondition();
		this.startIdxInCondition = ref.getStartIdxInCondition();
		this.stopIdxInCondition = ref.getStopIdxInCondition();
		this.isReferencedByShortName = determineIfReferencedByShortName(ref);
		this.pathNotationConfig = ref.getPathNotationConfig();
	}

	/**
	 * @return <code>true</true> if the entity is a field in an A12 model and it is referenced with
	 * 		its short name.
	 */
	private boolean determineIfReferencedByShortName(EntityRef ref) {
		if (isEchtesFeld && !isHmodel) {
			return !ref.getNameBuiltFromConditionText().contains("/");
		} else {
			return false;
		}
	}

	public EntityIdImpl(IEntityId f, List<Object> semanticIndexes, ICondition filterCondition) {
		ebenen = f.getIndices().clone();
		maxWerte = f.getMaxValues().clone();
		zeileSternStart = f.getRowWildcardStart();
		zeileSternEnd = f.getRowWildcardEnd();
		lfdNrSternStart = f.getRootGroupWildcardStart();
		lfdNrSternEnd = f.getRootGroupWildcardEnd();
		fullName = f.getFullName();
		usb = f.getUsb();
		maxUsb = f.getMaxUsb();
		isEchtesFeld = f.isField();
		isHmodel = f.isHmodel();
		categoryName = f.getCategoryName();
		semanticIndex = Collections.unmodifiableList(semanticIndexes);
		pathNotationConfig = f.getPathNotationConfig();
		isOuter = f.isOuter();
		this.filterCondition = filterCondition;
	}

	public EntityIdImpl(IEntityId f, long[] indices) {
		this.ebenen = indices.clone();
		maxWerte = f.getMaxValues().clone();
		zeileSternStart = f.getRowWildcardStart();
		zeileSternEnd = f.getRowWildcardEnd();
		lfdNrSternStart = f.getRootGroupWildcardStart();
		lfdNrSternEnd = f.getRootGroupWildcardEnd();
		fullName = f.getFullName();
		usb = f.getUsb();
		maxUsb = f.getMaxUsb();
		isEchtesFeld = f.isField();
		isHmodel = f.isHmodel();
		categoryName = f.getCategoryName();
		semanticIndex = f.getSemanticIndex();
		filterCondition = f.getFilterCondition();
		pathNotationConfig = f.getPathNotationConfig();
		isOuter = f.isOuter();
	}

	@Override
	public EntityIdImpl clone() {
		return new EntityIdImpl(
				this, this.getIndices()
		);
	}

	private static long[] getMaxWerteForGroup(IEntity e, IEC ec, boolean isHModel) {
		if (isHModel) {
			long z = ec.getAllContainedFields(e.getFullName()).stream()
					.map(EntityUtils::getMaxZeile)
					.min(Long::compare).orElse(1L);
			long[] maxValues = new long[2];
			maxValues[0] = EntityUtils.getMaxLfdNr(e);
			maxValues[1] = z;
			return maxValues;
		} else {
			return EntityUtils.getMaxInA12Model(e);
		}
	}

	public EntityIdImpl(IField f, List<Long> indices, long usb, List<Object> semIdx, boolean isHModel, boolean isOuter,
			PathNotationConfig pathNotationConfig) {
		// For fields, we do not need the IEC
		this(f, indices, usb, semIdx, isHModel, null, isOuter, pathNotationConfig);
	}

	/**
	 * Construct an {@link IEntityId} from an {@link IEntity} and additional information.
	 *
	 * @param e
	 * 		the entity containing the base information.
	 * @param indices
	 * 		the indices for the {@link IEntityId}.
	 * @param usb
	 * 		the USB for the {@link IEntityId}.
	 * @param isHModel
	 * 		specifies, whether H model special treatments should be applied.
	 * @param ec
	 * 		the entity container. Only necessary for H model groups to extract row
	 * 		repetition information via the contained fields. May be <code>null</code>,
	 * 		otherwise.
	 */
	public EntityIdImpl(IEntity e, List<Long> indices, long usb, List<Object> semIdx, boolean isHModel, IEC ec, boolean isOuter,
			PathNotationConfig pathNotationConfig) {
		this.fullName = e.getFullName();
		if (e.isField()) {
			this.maxWerte = EntityUtils.getMax(e, isHModel);
		} else {
			this.maxWerte = getMaxWerteForGroup(e, ec, isHModel);
		}
		this.maxUsb = EntityUtils.getMaxUsb(e);
		this.isEchtesFeld = e.isField();
		this.isHmodel = isHModel;
		this.ebenen = getEbenen(indices);
		this.usb = usb;

		if (indices.get(0) == IParseTreeEntity.WILDCARD) {
			lfdNrSternEnd = maxWerte[0];
		}
		if (indices.get(indices.size() - 1) == IParseTreeEntity.WILDCARD) {
			zeileSternEnd = maxWerte[maxWerte.length - 1];
		}
		this.categoryName = null;
		this.semanticIndex = semIdx;
		this.filterCondition = null;
		this.isOuter = isOuter;
		this.pathNotationConfig = pathNotationConfig;
	}

	private long[] getEbenen(List<Long> indices) {
		long[] result = new long[indices.size()];
		for (int i = 0; i < indices.size(); i++) {
			result[i] = indices.get(i);
		}
		return result;
	}

	public String toString() {
		String u = usb == IParseTreeEntity.UNSPECIFIED ? "" : "@" + Long.toString(usb);
		String cat = StringUtils.isEmpty(categoryName) ? "" : EntityIdConstants.CATEGORY_VALUE_ACCESS + categoryName;
		String semIdx = semanticIndex.isEmpty() ?
				"" :
				" " + EntityIdConstants.SEMANTIC_INDEX_TOKEN + " " + semanticIndex.toString();
		String filterConditionText = filterCondition == null ? "" : " Mit " + filterCondition.toString();
		return String.format("%s%s%s%s%s%s", fullName, Arrays.toString(getIndices()), u, cat, semIdx, filterConditionText);
	}

	@Override
	public long[] getIndices() {
		return Arrays.copyOf(ebenen, ebenen.length);
	}

	@Override
	public String getFullName() {
		return fullName;
	}

	@Override
	public long getUsb() {
		return usb;
	}

	@Override
	public long getRowWildcardStart() {
		return zeileSternStart;
	}

	@Override
	public long getRootGroupWildcardStart() {
		return lfdNrSternStart;
	}

	@Override
	public long getRowWildcardEnd() {
		return zeileSternEnd;
	}

	@Override
	public long getRootGroupWildcardEnd() {
		return lfdNrSternEnd;
	}

	@Override
	public String getCategoryName() {
		return categoryName;
	}

	public boolean isField() {
		return isEchtesFeld;
	}

	public boolean isHmodel() {
		return isHmodel;
	}

	public long[] getMaxValues() {
		return Arrays.copyOf(maxWerte, maxWerte.length);
	}

	public long getMaxUsb() {
		return maxUsb;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Override
	public List<Object> getSemanticIndex() {
		return semanticIndex;
	}

	public int getStartIdxInCondition() {
		return startIdxInCondition;
	}

	/**
	 * @return the index at which the entity id ends up in the condition text, this includes
	 * 		following modifiers: *, .*, /*, @* and combinations of the last three.
	 */
	public int getStopIdxInCondition() {
		return stopIdxInCondition;
	}

	/**
	 * @return <code>true</true> if the entity is a field in an A12 model and it is referenced with
	 * 		its short name.
	 */
	public boolean isReferencedByShortName() {
		return isReferencedByShortName;
	}

	@Override
	public ICondition getFilterCondition() {
		return filterCondition;
	}

	@Override
	public PathNotationConfig getPathNotationConfig() {
		return pathNotationConfig;
	}

	public void setPathNotationConfig(PathNotationConfig pathNotationConfig) {
		this.pathNotationConfig = pathNotationConfig;
	}

	@Override
	public boolean isOuter() {
		return isOuter;
	}

	@Override public EntityIdImpl withIndices(long[] indices) {
		return new EntityIdImpl(
				indices,
				this.maxWerte,
				this.maxUsb,
				this.zeileSternStart,
				this.zeileSternEnd,
				this.lfdNrSternStart,
				this.lfdNrSternEnd,
				this.fullName,
				this.usb,
				this.isEchtesFeld,
				this.isHmodel,
				this.categoryName,
				this.semanticIndex,
				this.startIdxInCondition,
				this.stopIdxInCondition,
				this.isReferencedByShortName,
				this.pathNotationConfig,
				this.isOuter,
				this.filterCondition
		);
	}

	@Override public EntityIdImpl withFilterCondition(ICondition filterCondition) {
		return new EntityIdImpl(
				this.ebenen,
				this.maxWerte,
				this.maxUsb,
				this.zeileSternStart,
				this.zeileSternEnd,
				this.lfdNrSternStart,
				this.lfdNrSternEnd,
				this.fullName,
				this.usb,
				this.isEchtesFeld,
				this.isHmodel,
				this.categoryName,
				this.semanticIndex,
				this.startIdxInCondition,
				this.stopIdxInCondition,
				this.isReferencedByShortName,
				this.pathNotationConfig,
				this.isOuter,
				filterCondition
		);
	}

	@Override public EntityIdImpl withIsOuter(boolean isOuter) {
		return new EntityIdImpl(
				this.ebenen,
				this.maxWerte,
				this.maxUsb,
				this.zeileSternStart,
				this.zeileSternEnd,
				this.lfdNrSternStart,
				this.lfdNrSternEnd,
				this.fullName,
				this.usb,
				this.isEchtesFeld,
				this.isHmodel,
				this.categoryName,
				this.semanticIndex,
				this.startIdxInCondition,
				this.stopIdxInCondition,
				this.isReferencedByShortName,
				this.pathNotationConfig,
				isOuter,
				this.filterCondition
		);
	}

	@Override public EntityIdImpl withPathNotationConfig(PathNotationConfig pathNotationConfig) {
		return new EntityIdImpl(
				this.ebenen,
				this.maxWerte,
				this.maxUsb,
				this.zeileSternStart,
				this.zeileSternEnd,
				this.lfdNrSternStart,
				this.lfdNrSternEnd,
				this.fullName,
				this.usb,
				this.isEchtesFeld,
				this.isHmodel,
				this.categoryName,
				this.semanticIndex,
				this.startIdxInCondition,
				this.stopIdxInCondition,
				this.isReferencedByShortName,
				pathNotationConfig,
				this.isOuter,
				this.filterCondition
		);
	}
}
