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

package com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.IExtmpModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.model.header.Annotation;

/**
 * Provides an implementation for the ExtMP-specific interface of {@link IModelSpecificSettings}.
 */
public class ExtmpModelSpecificSettingsImpl implements IExtmpModelSpecificSettings {

	private final Integer baseYear;
	private final boolean isHModel;
	private final boolean isHostBasiert;
	private final boolean isFgSoaNamingConvention;
	private final boolean isWithFkCheck;
	private final boolean withAssignedUXMLModel;
	private boolean isFirstProdVersionInExml = false;
	private boolean isFirstProdVersionA12DV = false;
	private boolean isReReDv = false;
	private List<Annotation> annotations = new ArrayList<>();
	private boolean useExtmpTemplates = true;
	private final Locale conditionLanguage;

	public ExtmpModelSpecificSettingsImpl(Builder<?> builder) {
		this.baseYear = builder.baseYear;
		this.isHModel = builder.isHModel;
		this.isHostBasiert = builder.isHostBasiert;
		this.isFgSoaNamingConvention = builder.isFgSoaNamingConvention;
		this.isWithFkCheck = builder.isWithFkCheck;
		this.withAssignedUXMLModel = builder.withAssignedUXMLModel;
		this.annotations = builder.annotations;
		this.useExtmpTemplates = builder.useExtmpTemplates;
		this.conditionLanguage = builder.conditionLanguage;
	}

	@SuppressWarnings("rawtypes")
	public static Builder<?> getBuilder() {
		return new Builder();
	}

	@Override
	public Optional<Integer> getBaseYear() {
		return Optional.ofNullable(baseYear);
	}

	@Override
	public boolean isHModel() {
		return isHModel;
	}

	@Override
	public boolean isHostbasiert() {
		return isHostBasiert;
	}

	@Override
	public boolean isFgSoaNamingConvention() {
		return isFgSoaNamingConvention;
	}

	@Override
	public boolean isWithFkCheck() {
		return isWithFkCheck;
	}

	@Override
	public void setIsFirstProdVersionInExml(boolean isFirstProdVersionInExml) {
		this.isFirstProdVersionInExml = isFirstProdVersionInExml;
	}

	@Override
	public boolean isFirstProdVersionInExml() {
		return isFirstProdVersionInExml;
	}

	@Override
	public boolean hasAssignedUXMLModel() {
		return withAssignedUXMLModel;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return annotations;
	}

	@Override public Locale getLanguageUsedInConditionText() {
		return conditionLanguage;
	}

	@Override
	public void setIsFirstProdVersionA12DV() {
		isFirstProdVersionA12DV = true;
	}

	@Override
	public boolean isFirstProdVersionA12DV() {
		return isFirstProdVersionA12DV;
	}

	@Override public boolean isReReDv() {
		return isReReDv;
	}

	@Override public void setReReDv(boolean reReDv) {
		isReReDv = reReDv;
	}

	@Override
	public boolean useExtmpTemplates() {
		return useExtmpTemplates;
	}

	public static class Builder<T extends Builder<T>> {
		private Integer baseYear;
		private boolean isHModel;
		private boolean isHostBasiert = false;
		private boolean isFgSoaNamingConvention;
		private boolean isWithFkCheck;
		private boolean withAssignedUXMLModel;
		private List<Annotation> annotations = new ArrayList<>();
		private boolean useExtmpTemplates = true;
		private Locale conditionLanguage;

		@SuppressWarnings("unchecked")
		public T self() {
			return (T) this;
		}

		public T baseYear(Integer arg) {
			this.baseYear = arg;
			return self();
		}

		public T isHModel(boolean arg) {
			this.isHModel = arg;
			return self();
		}

		public T isHostBasiert(boolean arg) {
			this.isHostBasiert = arg;
			return self();
		}

		public T isFgSoaNamingConvention(boolean arg) {
			this.isFgSoaNamingConvention = arg;
			return self();
		}

		public T isWithFkCheck(boolean arg) {
			this.isWithFkCheck = arg;
			return self();
		}

		public T withAssignedUXMLModel(boolean arg) {
			withAssignedUXMLModel = arg;
			return self();
		}

		public T withAnnotations(List<Annotation> annotations) {
			this.annotations = annotations;
			return self();
		}

		public T useExtmpTemplates(boolean arg) {
			this.useExtmpTemplates = arg;
			return self();
		}

		public T conditionLanguage(Locale conditionLanguage) {
			this.conditionLanguage = conditionLanguage;
			return self();
		}

		public ExtmpModelSpecificSettingsImpl build() {
			return new ExtmpModelSpecificSettingsImpl(this);
		}
	}

}
