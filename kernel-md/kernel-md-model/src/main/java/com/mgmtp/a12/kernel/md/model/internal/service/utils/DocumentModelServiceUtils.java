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

package com.mgmtp.a12.kernel.md.model.internal.service.utils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ServiceConfiguration;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.md.model.a12internal.services.ElementContainer;
import com.mgmtp.a12.kernel.md.model.a12internal.services.codegen.A12SpecificSettings;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModelInfo;
import com.mgmtp.a12.kernel.md.model.internal.service.ValidationModelConverter;
import com.mgmtp.a12.model.header.Annotation;

import jakarta.validation.constraints.NotNull;

public class DocumentModelServiceUtils {

	public static final String DEFAULT_MODEL_VERSION_SEPARATOR = "_"; // Underscore is used per default in A12 Export as the separator.

	private DocumentModelServiceUtils() {
		throw new UnsupportedOperationException("This may not be instantiated.");
	}

	/**
	 * Standard way of determining the id of the generated code for the static server-side runtime services.
	 * <p>
	 * This method is called while generating the code and while loading the generated code once it is generated and available in
	 * the classpath.
	 */
	public static String createGeneratedCodeId(IDocumentModel docModel) {
		IDocumentModelInfo docInfo = docModel.getContent().getDocumentModelInfo();
		String version = docInfo.getBranch()
				.map(String::toLowerCase)
				.filter(v -> docInfo.getName().toLowerCase().endsWith(DEFAULT_MODEL_VERSION_SEPARATOR + v))
				.orElse(null);

		String docModelName = version != null
				// extract doc model name
				? Strings.CI.removeEnd(docInfo.getName(), DEFAULT_MODEL_VERSION_SEPARATOR + version)
				: docInfo.getName();
		return createGeneratedCodeId(docModelName, version, null);
	}

	public static String createGeneratedCodeId(String modelName, String version, String variant) {
		return Stream.of(variant, modelName, version)
				.filter(StringUtils::isNotBlank)
				.map(DocumentModelServiceUtils::encodeLow)
				.collect(Collectors.joining("_"));
	}

	private static String encodeLow(String value) {
		return VkStringEncoder.encode(value.toLowerCase());
	}

	public static ElementContainer convert(IDocumentModel documentModel, boolean considerNotInD, IProblemReporter problemReporter) {
		return new ValidationModelConverter(considerNotInD).convert(documentModel, problemReporter);
	}

	public static class A12ModelSpecificSettings implements IModelSpecificSettings {
		private final IDocumentModel documentModel;
		private final Locale conditionLanguage;

		public A12ModelSpecificSettings(IDocumentModel documentModel, @NotNull Locale conditionLanguage) {
			this.documentModel = documentModel;
			this.conditionLanguage = conditionLanguage;
		}

		@Override
		public Optional<Integer> getBaseYear() {
			return documentModel.getContent().getDocumentModelInfo().getBaseYear();
		}

		@Override
		public String getTimeZone() {
			return documentModel.getContent().getDocumentModelConfig().getTimeZone();
		}

		@Override public Locale getLanguageUsedInConditionText() {
			return conditionLanguage;
		}

		@Override
		public boolean isFieldRefByShortNameAllowed() {
			return documentModel.getContent().getDocumentModelConfig().isFieldRefByShortNameAllowed();
		}

		@Override
		public List<Annotation> getAnnotations() {
			return ListUtils.emptyIfNull(documentModel.getHeader().getAnnotations());
		}
	}

	public static IServiceConfiguration createServiceConfiguration(IDocumentModel documentModel, IEC ec) {
		List<Locale> locales = ListUtils.emptyIfNull(documentModel.getHeader().getLocales());
		Locale conditionLanguage = documentModel.getContent().getDocumentModelConfig().getConditionLanguage();

		return new ServiceConfiguration(
				ec,
				new A12ModelSpecificSettings(documentModel, conditionLanguage),
				new A12SpecificSettings(locales));
	}
}
