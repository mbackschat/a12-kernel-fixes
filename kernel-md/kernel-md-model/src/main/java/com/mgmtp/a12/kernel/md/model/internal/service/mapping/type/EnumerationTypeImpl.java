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
package com.mgmtp.a12.kernel.md.model.internal.service.mapping.type;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;

/**
 * Default {@link IEnumerationType} implementation.
 */
class EnumerationTypeImpl implements IEnumerationType {

	private final List<String> xmlValues;
	private final Map<Locale, List<IEnumerationType.EnumerationValue>> values;
	private final Map<String, List<String>> categoryValues;
	private final Map<Locale, String> errorMessages;
	private final String sValue;

	EnumerationTypeImpl(
			final Map<Locale, String> errorMessages,
			final Map<Locale, List<IEnumerationType.EnumerationValue>> values,
			final Map<String, List<String>> categoryValues,
			final List<String> xmlValues,
			final String sValue) {

		Validate.notEmpty(xmlValues, "The model is invalid. It contains Enumerations without any values.");
		values.values().stream()
				.forEach(v -> Validate.notEmpty(v,
						"The model is invalid. It contains Enumerations without required display values."));

		this.errorMessages = errorMessages;
		this.categoryValues = categoryValues;
		this.values = values;
		this.xmlValues = xmlValues;
		this.sValue = sValue;
	}

	@Override
	public List<IEnumerationType.EnumerationValue> getDisplayValues(final Locale loc) {
		return values.get(loc);
	}

	@Override
	public List<String> getXMLValues() {
		return xmlValues;
	}

	@Override
	public Map<String, List<String>> getCategoryValues() {
		return categoryValues;
	}

	@Override
	public String getErrorMessage(final Locale loc) {
		return FieldTypeMappingUtils.getErrorMessage(loc, errorMessages);
	}

	@Override
	public Set<Locale> getSupportedDisplayLocales() {
		return values.keySet();
	}

	@Override
	public String getsValue() {
		return sValue;
	}

	@Override
	public Optional<String> getTableName() {
		return Optional.empty();
	}

	@Override
	public Optional<String> getColumnName() {
		return Optional.empty();
	}

	@Override
	public List<String> getDependentColumns() {
		return Collections.emptyList();
	}

	static class EnumerationValueImpl implements IEnumerationType.EnumerationValue {

		private final String displayValue;
		private final String xmlValue;

		EnumerationValueImpl(final String displayValue, final String xmlValue) {
			this.displayValue = displayValue;
			this.xmlValue = xmlValue;
		}

		@Override
		public String getDisplayValue() {
			return displayValue;
		}

		@Override
		public String getXmlValue() {
			return xmlValue;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((displayValue == null) ? 0 : displayValue.hashCode());
			result = prime * result + ((xmlValue == null) ? 0 : xmlValue.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			EnumerationValueImpl other = (EnumerationValueImpl) obj;
			if (displayValue == null) {
				if (other.displayValue != null) {
					return false;
				}
			} else if (!displayValue.equals(other.displayValue)) {
				return false;
			}
			if (xmlValue == null) {
				if (other.xmlValue != null) {
					return false;
				}
			} else if (!xmlValue.equals(other.xmlValue)) {
				return false;
			}
			return true;
		}
	}
}
