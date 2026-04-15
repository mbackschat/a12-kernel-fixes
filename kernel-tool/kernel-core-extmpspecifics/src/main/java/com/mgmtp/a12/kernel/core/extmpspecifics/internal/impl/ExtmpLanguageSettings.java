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

package com.mgmtp.a12.kernel.core.extmpspecifics.internal.impl;

import java.util.Locale;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectLanguageSettings;

/**
 * Implementation for an ExtMP Language Setting
 */
public class ExtmpLanguageSettings implements IProjectLanguageSettings {
	private String name;
	private Locale locale;
	private char decimalSeparator;
	private String thousandsSeparator = "";
	private String dateFormat;
	private boolean dateNullOptional;
	private char dateRangeSeparator;
	private String dateTimeFormat;
	private String timeFormat;

	private ExtmpLanguageSettings() {
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public char getDecimalSeparator() {
		return decimalSeparator;
	}

	@Override
	public String getThousandsSeparator() {
		return thousandsSeparator;
	}

	@Override
	public String getDateFormat() {
		return dateFormat;
	}

	@Override
	public boolean isDateNullOptional() {
		return dateNullOptional;
	}

	@Override
	public char getDateRangeSeparator() {
		return dateRangeSeparator;
	}

	@Override
	public String getDateTimeFormat() {
		return dateTimeFormat;
	}

	@Override
	public String getTimeFormat() {
		return timeFormat;
	}

	public static class Builder {
		Locale locale;
		char decimalSeparator;
		String thousandsSeparator;
		String dateFormat = "dd.MM.yyyy";
		boolean dateNullOptional = false;
		char dateRangeSeparator;
		String name;
		String dateTimeFormat;
		String timeFormat;

		public Builder self() {
			return this;
		}

		public Builder locale(Locale arg) {
			this.locale = arg;
			return self();
		}

		public Builder decimalSeparator(char arg) {
			this.decimalSeparator = arg;
			return self();
		}

		public Builder thousandsSeparator(String arg) {
			this.thousandsSeparator = arg;
			return self();
		}

		public Builder dateRangeSeparator(char arg) {
			this.dateRangeSeparator = arg;
			return self();
		}

		public Builder dateFormat(String arg) {
			this.dateFormat = arg;
			return self();
		}

		public Builder dateTimeFormat(String arg) {
			this.dateTimeFormat = arg;
			return self();
		}

		public Builder timeFormat(String arg) {
			this.timeFormat = arg;
			return self();
		}

		public Builder name(String arg) {
			this.name = arg;
			return self();
		}

		public Builder dateNullOptional(boolean arg) {
			this.dateNullOptional = arg;
			return self();
		}

		public ExtmpLanguageSettings build() {
			return new ExtmpLanguageSettings(this);
		}
	}

	protected ExtmpLanguageSettings(Builder builder) {
		this.locale = builder.locale;
		this.dateFormat = builder.dateFormat;
		this.dateRangeSeparator = builder.dateRangeSeparator;
		this.dateNullOptional = builder.dateNullOptional;
		this.decimalSeparator = builder.decimalSeparator;
		this.thousandsSeparator = builder.thousandsSeparator;
		this.name = builder.name;
		this.dateTimeFormat = builder.dateTimeFormat;
		this.timeFormat = builder.timeFormat;
	}

	@Override
	public String getName() {
		return name;
	}

}
