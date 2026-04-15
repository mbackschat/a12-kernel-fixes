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

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelConfig;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelObjectReferenceBuilder;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelRoot;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Group.IncludeDetails;
import com.mgmtp.a12.kernel.md.model.a12internal.IDocumentModelObjectReference;
import com.mgmtp.a12.kernel.md.model.a12internal.IEntity;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.VariantFilter;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.BooleanType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.ConfirmType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.CustomFieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateFragmentType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateRangeType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.DateType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.FieldType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.NumberType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.StringType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TypeDefType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.UnspecifiedType;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.model.header.JacksonConfiguration;
import com.mgmtp.a12.model.header.ModelReference;

public class JsonMapper {

	enum MapperInstance {
		BASE,
		FIELDTYPE,
		NO_NON_EMPTY
	}

	private static final EnumMap<MapperInstance, ObjectMapper> MAPPERS = new EnumMap<>(MapperInstance.class);

	static {
		MAPPERS.put(MapperInstance.BASE, createMapper(MapperInstance.BASE));
		MAPPERS.put(MapperInstance.FIELDTYPE, createMapper(MapperInstance.FIELDTYPE));
		MAPPERS.put(MapperInstance.NO_NON_EMPTY, createMapper(MapperInstance.NO_NON_EMPTY));
	}

	public static ObjectMapper getMapper() {
		return MAPPERS.get(MapperInstance.BASE);
	}

	public static ObjectMapper getWriterForFieldtypes() {
		return MAPPERS.get(MapperInstance.FIELDTYPE);
	}

	public static ObjectMapper getWriterForNoNonEmpty() {
		return MAPPERS.get(MapperInstance.NO_NON_EMPTY);
	}

	public static ObjectMapper getGeneralMapper() {
		return ObjectMapperProvider.createPreconfiguredObjectMapper();
	}

	public static ObjectMapper createMapper(Set<String> elementIdsInResult) {
		return createMapper(MapperInstance.BASE, elementIdsInResult);
	}

	private static ObjectMapper createMapper(MapperInstance instance) {
		Set<String> elementIdsInResult = new HashSet<>();
		return createMapper(instance, elementIdsInResult);
	}

	private static ObjectMapper createMapper(MapperInstance instance, Set<String> elementIdsInResult) {
		ObjectMapper result = getGeneralMapper();

		result.addMixIn(DocumentModelRoot.class, DocumentModelRootMixin.class)
				.addMixIn(Rule.class, RuleMixin.class)
				.addMixIn(Computation.class, ComputationMixin.class)
				.addMixIn(Computation.ComputationAlternative.class, ComputationAlternativesMixin.class)
				.addMixIn(Group.class, GroupMixin.class)
				.addMixIn(Group.SortField.class, GroupSortFieldMixin.class)
				.addMixIn(DocumentModelConfig.class, DocModelConfigMixin.class)
				.addMixIn(DocumentModelInfo.class, DocModelInfoMixin.class)
				.addMixIn(TypeDefType.class, TypeDefTypeMixin.class)
				// FieldTypeSerializer - ObjectMapper
				.addMixIn(FieldType.class, FieldTypeMixin.class)
				.addMixIn(DateType.class, DateTypeMixin.class)
				.addMixIn(DateFragmentType.class, DateFragmentTypeMixin.class)
				.addMixIn(DateRangeType.class, DateRangeTypeMixin.class)
				.addMixIn(NumberType.class, NumberTypeMixin.class)
				.addMixIn(StringType.class, StringTypeMixin.class)
				.addMixIn(CustomFieldType.class, CustomFieldTypeMixin.class)
				.addMixIn(ConfirmType.class, ConfirmTypeMixin.class)
				.addMixIn(BooleanType.class, BooleanTypeMixin.class)
				.addMixIn(EnumerationType.class, EnumerationTypeMixin.class)
				.addMixIn(EnumerationType.EnumCategory.class, EnumerationTypeEnumCategoryMixin.class)
				.addMixIn(UnspecifiedType.class, UnspecifiedTypeMixin.class)
				.addMixIn(FieldTypeDefinition.class, FieldTypeDefinitionMixin.class)
				// Concrete elements like Group, Field, Rule and Computation
				.addMixIn(VariantFilter.class, VariantFilterMixIn.class);
		result.setFilterProvider(new SimpleFilterProvider().addFilter("DatePrecisionFilter", new DatePrecisionFilter()));

		switch (instance) {
			case BASE, FIELDTYPE -> result.setSerializationInclusion(Include.NON_EMPTY);
			case NO_NON_EMPTY -> { /* do nothing */ }
		}

		SimpleModule module = new SimpleModule();
		module.addDeserializer(ILocalizedTextMap.class, new LocalizedTextMapDeserializer());
		module.addDeserializer(StringType.HintList.class, new LocalizedHintListDeserializer());
		module.addDeserializer(FieldType.class, new FieldtypeDeserializer());
		module.addDeserializer(Element.class, new ElementDeserializer());
		switch (instance) {
			case BASE, NO_NON_EMPTY -> module.addSerializer(FieldType.class, new FieldtypeSerializer());
			case FIELDTYPE -> { /* To avoid a Stack overflow exception when the field type serializer needs a mapper, we do not add the field type serializer here. */ }
		}
		module.addSerializer(DocumentModelRoot.class, new ModelRootSerializer());
		module.addSerializer(Element.class, new ElementSerializer(elementIdsInResult));
		module.addSerializer(ILocalizedTextMap.class, new LocalizedTextMapSerializer());
		module.addSerializer(StringType.HintList.class, new LocalizedHintListSerializer());
		module.addSerializer(Optional.class, new OptionalSerializer());

		result.registerModule(module);
		JacksonConfiguration.configureMixins(result);
		return result;
	}
}

abstract class DocumentModelRootMixin {
	@JsonProperty("rootGroups")
	List<Element> elements;
}

abstract class DocModelConfigMixin {
	@JsonInclude(Include.NON_DEFAULT)
	private String fieldRefByShortNameAllowed;
}

abstract class DocModelInfoMixin {
	@JsonProperty("variant")
	private String variant;
	@JsonProperty("branch")
	private String branch;
	@JsonProperty("baseYear")
	private String baseYear;
	@JsonProperty("revision")
	private String revision;
	@JsonProperty("immutable") // necessary for generating the infos in the json in the correct order
	private boolean immutable;
	@JsonProperty("comment")
	private String comment;
	@JsonProperty("joinedModelsInfo")
	private String joinedModelsInfo;
	@JsonInclude(Include.NON_DEFAULT)
	private boolean metaData;
}

abstract class RuleMixin {
	@JsonDeserialize(converter = EntityPathToIDocumentModelObjectReferenceConverter.class)
	@JsonProperty("errorEntityRelPath")
	IDocumentModelObjectReference<IEntity> errorEntity;
}

abstract class ComputationMixin {
	@JsonDeserialize(converter = FieldPathToIDocumentModelObjectConverter.class)
	@JsonProperty("computedFieldRelPath")
	IDocumentModelObjectReference<Field> computedField;

	@JsonProperty("commonPrecondition")
	private String commonPrecondition;
}

abstract class ComputationAlternativesMixin {
	@JsonProperty("precondition")
	private String precondition;

	@JsonProperty("toleranceRangeOp")
	private String toleranceRangeOp;
}

abstract class GroupSortFieldMixin {
	@JsonSerialize(converter = IDocumentModelObjectToFieldPathConverter.class)
	@JsonDeserialize(converter = FieldPathToIDocumentModelObjectConverter.class)
	@JsonProperty("name")
	IDocumentModelObjectReference<Field> field;
}

@JsonIgnoreProperties(ignoreUnknown = true)
abstract class GroupMixin {
	@JsonDeserialize(converter = FieldPathToIDocumentModelObjectConverter.class)
	@JsonProperty("indexFieldName")
	IDocumentModelObjectReference<Field> indexField;

	@JsonDeserialize(converter = ModelAliasConverter.class)
	@JsonProperty("modelAlias")
	IncludeDetails includeDetails;

	@JsonInclude(Include.NON_DEFAULT)
	boolean required;
}

abstract class TypeDefTypeMixin {
	@JsonProperty("typeDefinitionId")
	String id;

	@JsonIgnore
	private FieldTypeDefinition typeDefinition;
}

abstract class FieldTypeDefinitionMixin {
	Collection<List<ModelReference>> allModelReferencePaths;

	@JsonProperty("revision")
	String revision;

	@JsonIgnore
	abstract boolean isImmutable();

	@JsonIgnore
	abstract Optional<ModelReference> getModelReference();

	@JsonIgnore
	abstract List<ModelReference> getModelReferences();
}

@JsonInclude(value = Include.ALWAYS)
abstract class VariantFilterMixIn {
	@JsonInclude(value = Include.NON_EMPTY)
	List<String> variants;
}

class EntityPathToIDocumentModelObjectReferenceConverter extends StdConverter<String, IDocumentModelObjectReference<IEntity>> {

	@Override
	public IDocumentModelObjectReference<IEntity> convert(final String relativePath) {
		return new DocumentModelObjectReferenceBuilder<IEntity>().relativePath(relativePath).build();
	}

}

class ModelAliasConverter extends StdConverter<String, Group.IncludeDetails> {

	@Override
	public Group.IncludeDetails convert(final String alias) {
		ModelReference modelReference = new ModelReference() {
			private static final long serialVersionUID = 1L;

			public String getAlias() {
				return alias;
			}

			@Override
			public String getPurpose() {
				return null;
			}

			@Override
			public String getModelType() {
				return null;
			}

			@Override
			public String getReference() {
				return null;
			}
		};
		return IncludeDetails.builder().modelReference(modelReference).build();
	}

}

class FieldPathToIDocumentModelObjectConverter extends StdConverter<String, IDocumentModelObjectReference<Field>> {

	@Override
	public IDocumentModelObjectReference<Field> convert(final String relativePath) {
		return new DocumentModelObjectReferenceBuilder<Field>().relativePath(relativePath).build();
	}

}

class IDocumentModelObjectToFieldPathConverter extends StdConverter<IDocumentModelObjectReference<Field>, String> {

	@Override
	public String convert(final IDocumentModelObjectReference<Field> reference) {
		if (reference == null) {
			return null;
		}
		return reference.getRelativePath();
	}

}