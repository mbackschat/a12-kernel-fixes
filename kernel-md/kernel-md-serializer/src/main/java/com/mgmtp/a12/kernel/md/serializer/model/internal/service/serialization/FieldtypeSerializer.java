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

package com.mgmtp.a12.kernel.md.serializer.model.internal.service.serialization;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType.InterpretationOfYear;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType.DatePrecision;
import com.mgmtp.a12.kernel.md.model.internal.impl.EnumValues;
import com.mgmtp.a12.kernel.md.model.internal.impl.HintLists;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.StdSerializer;

public class FieldtypeSerializer extends StdSerializer<FieldType> {

	private static final Pattern EMPTY_JSON_OBJECT = Pattern.compile("^\\s*\\{\\s*}\\s*$");

	public FieldtypeSerializer() {
		this(null);
	}

	public FieldtypeSerializer(Class<FieldType> t) {
		super(t);
	}

	@Override
	public void serialize(FieldType value, JsonGenerator gen, SerializationContext provider) {
		gen.writeStartObject();
		gen.writeStringProperty("type", idFromValue(value));
		ObjectMapper fieldTypeMapper = KernelJsonMapper.getWriterForFieldtypes();
		if (!EMPTY_JSON_OBJECT.matcher(fieldTypeMapper.writeValueAsString(value)).matches()) {
			String subTypeName = idFromValue(value);
			gen.writeName(subTypeName);
			fieldTypeMapper.writeValue(gen, value);
		}
		gen.writeEndObject();
	}

	private String idFromValue(final Object value) {
		return value.getClass().getSimpleName();
	}
}

abstract class StringTypeMixin {
	@JsonProperty("minLength")
	private Integer minLength;
	@JsonProperty("maxLength")
	private Integer maxLength;
	@JsonProperty("pattern")
	private String pattern;

	@JsonInclude(Include.NON_DEFAULT)
	private boolean lineBreaksPermitted;
	@JsonInclude(Include.NON_DEFAULT)
	private boolean noValueValidation;
	@JsonInclude(Include.NON_DEFAULT)
	private boolean alphabeticalSorting;

	@JsonSerialize(as = HintLists.class)
	@JsonDeserialize(as = HintLists.class)
	@JsonProperty("hintList")
	HintLists hintLists;

}

abstract class NumberTypeMixin {
	@JsonProperty("minValue")
	private BigDecimal minValue;
	@JsonProperty("maxValue")
	private BigDecimal maxValue;
	@JsonProperty("trait")
	private String trait;

	@JsonInclude(Include.NON_DEFAULT)
	private int minFractionalDigits;
	@JsonInclude(Include.NON_DEFAULT)
	private int maxFractionalDigits;

	@JsonInclude(Include.NON_DEFAULT)
	private boolean zeroNotAllowed;
	@JsonInclude(Include.NON_DEFAULT)
	private boolean positivesOnly;

	@JsonProperty("minLength")
	private Integer minLength;
	@JsonProperty("maxLength")
	private Integer maxLength;
	@JsonProperty("maxIntegerDigits")
	private Integer maxIntegerDigits;

	@JsonInclude(Include.NON_DEFAULT)
	private boolean leadingZerosAllowed;
}

abstract class EnumerationTypeMixin {
	@JsonInclude(Include.NON_DEFAULT)
	boolean alphabeticalSorting;

	@JsonProperty("sValue")
	String sValue;

	@JsonSerialize(as = EnumValues.class)
	@JsonDeserialize(as = EnumValues.class)
	EnumValues values;
}

abstract class DateRangeTypeMixin {
	@JsonProperty("rangeSeparator")
	String rangeSeparator;

	@JsonInclude(Include.NON_DEFAULT)
	@JsonProperty
	private boolean youngerThan1900Check;

	@JsonProperty
	private InterpretationOfYear interpretationOfYear;

	@JsonProperty
	private String notInDCustomFormat;

	@JsonProperty
	private String notInDCustomRangeSeparator;
}

abstract class EnumerationTypeEnumCategoryMixin {
	@JsonProperty("description")
	String description;
}

abstract class CustomFieldTypeMixin {
	@JsonProperty("name")
	private String name;
	@JsonProperty("displayName")
	private String displayName;
	@JsonProperty("minLength")
	private Integer minLength;
	@JsonProperty("maxLength")
	private Integer maxLength;

}

abstract class ConfirmTypeMixin {
	@JsonProperty("notInDCustomTrueValue")
	private String notInDCustomTrueValue;
}

abstract class BooleanTypeMixin {
	@JsonProperty("notInDCustomTrueValue")
	private String notInDCustomTrueValue;

	@JsonProperty("notInDCustomFalseValue")
	private String notInDCustomFalseValue;
}

@JsonFilter("DatePrecisionFilter")
abstract class DateTypeMixin {
	@JsonInclude(Include.NON_DEFAULT)
	private boolean youngerThan1900Check;
	@JsonProperty("notInDCustomFormat")
	private String notInDCustomFormat;
}

class DatePrecisionFilter extends SimpleBeanPropertyFilter {

	@Override
	public void serializeAsProperty(Object pojo, JsonGenerator jgen, SerializationContext provider, PropertyWriter writer)
			throws Exception {
		if (writer.getName()
				.equals("datePrecision") && pojo instanceof DateType dateType && dateType.getDatePrecision() == DatePrecision.FULL) {
			return;
		}
		super.serializeAsProperty(pojo, jgen, provider, writer);
	}
}

abstract class DateFragmentTypeMixin {
	@JsonInclude(Include.NON_DEFAULT)
	private boolean youngerThan1900Check;
	@JsonProperty("notInDCustomFormat")
	private String notInDCustomFormat;
}
