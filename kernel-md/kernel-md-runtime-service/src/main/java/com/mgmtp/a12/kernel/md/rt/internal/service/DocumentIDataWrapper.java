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

package com.mgmtp.a12.kernel.md.rt.internal.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.core.rt.a12internal.PresentationInformation;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaField;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaGroup;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.utils.IMetaKeys;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.internal.service.impl.FieldInstanceImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.impl.GroupInstanceImpl;

import lombok.Getter;

public class DocumentIDataWrapper implements IDocument {

	private String id;
	private final String documentModelId;
	@Getter private final IData data;
	private final IMetaModel metaModel;
	private Set<IEntityInstance> entityInstances = null;
	private final PresentationInformation presentationInformation;
	private final IResult result;

	public DocumentIDataWrapper(IData d, String dmId, IMetaModel metaModel,
			IResult result, PresentationInformation presentationInformation) {
		this.data = d;
		this.documentModelId = dmId;
		this.metaModel = metaModel;
		this.result = result;
		this.presentationInformation = presentationInformation;
	}

	@Override public Optional<String> getId() {
		return Optional.ofNullable(id);
	}

	@Override public void setId(String id) {
		Validate.notBlank(id);
		this.id = id;
	}

	@Override public String getDocumentModelId() {
		return documentModelId;
	}

	@Override public Set<IEntityInstance> getEntityInstances() {
		lazyLoad();
		return entityInstances;
	}

	private void lazyLoad() {
		if (entityInstances == null) {
			entityInstances = new HashSet<>();
			for (IIdentifier identifier : data) {
				IMetaField metaField = (IMetaField) metaModel.getValue(IMetaKeys.MODEL_META_FIELD, identifier.getName());
				if (metaField != null) {
					int[] repetitions = EntityInstanceConverter.convertToRepetitions(identifier.getIndices());
					FieldInstanceImpl fieldInstance = new FieldInstanceImpl(identifier.getName(), repetitions);
					String sValue = data.getValue(identifier);
					ResultForConversion conversionResult = new ResultForConversion(result, identifier);
					Object value = metaField.convertWithOnlyBasicTypeCheck(sValue, presentationInformation, conversionResult);
					fieldInstance.setValue(value);
					entityInstances.add(fieldInstance);
				}
				IMetaGroup metaGroup = (IMetaGroup) metaModel.getValue(IMetaKeys.MODEL_META_GROUP, identifier.getName());
				if (metaGroup != null) {
					int[] repetitions = EntityInstanceConverter.convertToRepetitions(identifier.getIndices());
					entityInstances.add(new GroupInstanceImpl(identifier.getName(), repetitions));
				}
			}
		}
	}

	@Override public boolean addEntityInstance(IEntityInstance entityInstance) {
		throw new UnsupportedOperationException();
	}

	@Override public boolean removeEntityInstance(IEntityInstance entityInstance) {
		throw new UnsupportedOperationException();
	}
}
