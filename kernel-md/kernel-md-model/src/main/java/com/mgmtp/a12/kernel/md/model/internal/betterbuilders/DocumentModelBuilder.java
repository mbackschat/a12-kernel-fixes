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
package com.mgmtp.a12.kernel.md.model.internal.betterbuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.BaseUtilConstants;
import com.mgmtp.a12.kernel.core.utils.internal.HashingHelper;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelConfig;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelContent;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.model.header.HeaderFactory;
import com.mgmtp.a12.model.header.ModelReference;

public class DocumentModelBuilder {

	private HeaderFactory.Builder documentModelHeaderBuilder;
	private DocumentModelInfo.DocumentModelInfoBuilder documentModelInfoBuilder;
	private DocumentModelConfig.DocumentModelConfigBuilder documentModelConfigBuilder;

	public static DocumentModelBuilder builder(String dmId, Locale... locales) {
		return builder(dmId, Arrays.asList(locales));
	}

	public static DocumentModelBuilder builder(String dmId, Collection<Locale> locales) {
		DocumentModelBuilder builder = new DocumentModelBuilder();
		return builder.initMinimal(dmId, locales);
	}

	public static void setIdAndName(DocumentModel dm, String newId) {
		dm.setHeader(HeaderBuilderUtil.toBuilder(dm.getHeader()).withId(newId).build());
		dm.getContent().setModelInfo(dm.getContent().getModelInfo().toBuilder().name(newId).build());
	}

	/**
	 * Warning: the supportedLocales must be a subset of the locales that are actually supported by all
	 * rule and field error texts, etc. in the document model; otherwise, the resulting DM can be inconsistent.
	 */
	public static void setSupportedLocales(DocumentModel dm, List<Locale> supportedLocales) {
		dm.setHeader(HeaderBuilderUtil.toBuilder(dm.getHeader()).withLocales(supportedLocales).build());
	}

	private DocumentModelBuilder initMinimal(String dmId, Collection<Locale> locales) {
		documentModelHeaderBuilder = new HeaderFactory.Builder()
				.withModelType(DocumentModel.MODEL_TYPE_DOCUMENT)
				.withModelVersion(DocumentModelContent.VERSION)
				.withLocales(new ArrayList<>(locales))
				.withAnnotations(new ArrayList<>())
				.withLabels(new ArrayList<>())
				.withModelReferences(new ArrayList<>());
		documentModelInfoBuilder = DocumentModelInfo.builder(); // should se set the "immutable" attribute?
		documentModelConfigBuilder = DocumentModelConfig.builder()
				.decimalSeparator(".")
				.timeZone(BaseUtilConstants.TIME_ZONE_UTC)
				.conditionLanguage(Locale.US)
				.fieldRefByShortNameAllowed(false);
		this.id(dmId);
		return this;
	}

	public static DocumentModelBuilder builderWithDefaultsForTests() {
		DocumentModelBuilder builder = new DocumentModelBuilder();
		return builder.initWithQuiteSpecificDefaultsForTests();
	}

	private DocumentModelBuilder initWithQuiteSpecificDefaultsForTests() {
		documentModelHeaderBuilder = new HeaderFactory.Builder()
				.withModelType(DocumentModel.MODEL_TYPE_DOCUMENT)
				.withModelVersion(DocumentModelContent.VERSION)
				.withLocales(Locale.US)
				.withAnnotations(new ArrayList<>())
				.withLabels(new ArrayList<>())
				.withModelReferences(new ArrayList<>());
		documentModelInfoBuilder = DocumentModelInfo.builder()
				.variant("cockpit")
				.revision("Arbeitsversion")
				.immutable(false);
		documentModelConfigBuilder = DocumentModelConfig.builder()
				.decimalSeparator(".")
				.timeZone("Europe/Berlin")
				.conditionLanguage(Locale.US)
				.fieldRefByShortNameAllowed(true);
		return this;
	}

	public DocumentModelBuilder id(String id) {
		documentModelHeaderBuilder.withId(id);
		documentModelInfoBuilder.name(id);
		return this;
	}

	public DocumentModelBuilder branch(String branch) {
		documentModelInfoBuilder.branch(branch);
		if (NumberUtils.isDigits(branch)) {
			documentModelInfoBuilder.baseYear(NumberUtils.createInteger(branch));
		}
		return this;
	}

	public DocumentModelBuilder modelVersion(String modelVersion) {
		documentModelHeaderBuilder.withModelVersion(modelVersion);
		return this;
	}

	public DocumentModelBuilder locales(Locale... locales) {
		documentModelHeaderBuilder.withLocales(locales);
		return this;
	}

	public DocumentModelBuilder header(UnaryOperator<HeaderFactory.Builder> modifier) {
		this.documentModelHeaderBuilder = modifier.apply(this.documentModelHeaderBuilder);
		return this;
	}

	public DocumentModelBuilder modelInfo(UnaryOperator<DocumentModelInfo.DocumentModelInfoBuilder> modifier) {
		this.documentModelInfoBuilder = modifier.apply(this.documentModelInfoBuilder);
		return this;
	}

	public DocumentModelBuilder modelConfig(UnaryOperator<DocumentModelConfig.DocumentModelConfigBuilder> modifier) {
		this.documentModelConfigBuilder = modifier.apply(this.documentModelConfigBuilder);
		return this;
	}

	public DocumentModel build() {
		return DocumentModel.builder()
				.header(documentModelHeaderBuilder.build())
				.content(DocumentModelContent.builder()
						.modelInfo(documentModelInfoBuilder.build())
						.modelConfig(documentModelConfigBuilder.build())
						.build())
				.build();
	}

	public static void addInclude(
			DocumentModel baseDm, String includedDmID, List<String> mountpointPath, int maxRep,
			IGroup.IIncludeConfig.IncludeLevel includeLevel) {
		assert baseDm != null;
		assert !mountpointPath.isEmpty();
		assert maxRep >= 1;

		Group mountPointGroup = GroupBuilder.with(baseDm)
				.name(mountpointPath.stream().map(e -> "/" + e).collect(Collectors.joining()))
				.id(generateIdForFullname("group_id_", mountpointPath)) // better than md5-based ids
				.repeat(maxRep)
				.build();

		ModelReference includeReference = createModelReferenceForInclude(includedDmID);
		mountPointGroup.setIncludeConfig(Group.IncludeConfig.builder()
				.reference(includedDmID)
				.includeLevel(includeLevel)
				.build());
		baseDm.getHeader().getModelReferences().add(includeReference);
	}

	/**
	 * Generates an id that can be used for the element with the corresponding fullName and that is very unlikely to collide
	 * with the id of another element in the same DM. The prefix string will be prepended to the hash-part of the id, for better
	 * human-interpretability.
	 * <p>
	 * Note on possible collisions: In DocumentModels, no two elements are (currently) allowed to share the same fullName,
	 * therefore
	 * the fullName uniquely identifies the element. Therefore, we can expect a collision resistance similar to the collision
	 * resistance of the underlying hash function. We currently use a truncated (128bit) sha256 hash, which should be safe against
	 * accidental collisions (and probably even pretty secure against adversarial collisions?)
	 * <p>
	 * Note: It might be somewhat problematic/breaking to change the algorithm later, because in case of DMs that are automatically
	 * (and repeatedly) generated/expanded, other models (e.g. form model?) might reference the generated elements by their
	 * generated
	 * id, which would change if the algorithm is changed in a future release. If the change is only to keep a longer prefix during
	 * truncation, the migration should be easy, though.
	 */
	// package-private instead of private, to allow unit testing
	static String generateIdForFullname(String prefix, List<String> fullName) {
		return prefix +
				HashingHelper.calculateHashSha256(String.join("/", fullName))
						.substring(0,
								32); // truncate the hash to make it not too unwieldy (not longer than the md5 we use in other places, but probably still more collision resistant)

	}

	public static ModelReference createModelReferenceForInclude(String includedDmId) {
		return HeaderFactory.createModelReference(
				"include",
				DocumentModel.MODEL_TYPE_DOCUMENT,
				includedDmId,
				includedDmId
		);
	}
}
