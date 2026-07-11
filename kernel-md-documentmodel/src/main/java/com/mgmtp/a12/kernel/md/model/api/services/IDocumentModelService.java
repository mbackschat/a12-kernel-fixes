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
package com.mgmtp.a12.kernel.md.model.api.services;

import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IFieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig.ProgrammingLanguage;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * This service offers actions which can be run on a document model.
 */
@OnlyForUsage
public interface IDocumentModelService {

	// ==================================
	// utilities
	// ==================================

	/**
	 * Creates the path for the given {@link IElement}. For example, if we have the following tree:
	 *
	 * <pre>
	 * <b>Customer</b>
	 *     |-<b>Data</b>
	 *     |   |-<b>PersonalData</b>
	 *     |   |   |-Name
	 *     |   |   |-<b>Address</b>
	 *     |   |   |   |-StreetName
	 *     |   |   |   |-Number
	 * </pre>
	 * <ul>
	 * <li>the path for the {@link IField} {@code Number} is
	 * {@code /Customer/Data/PersonalData/Address/Number}</li>
	 * <li>the path for the {@link IGroup} {@code PersonalData} is
	 * {@code /Customer/Data/PersonalData}</li>
	 * </ul>
	 *
	 * @param element
	 * 		specific element for which the path should be created. May not be {@code null}.
	 * @return the corresponding path.
	 */
	String getPath(@NonNull IElement element);

	// ==================================
	// consistency check
	// ==================================

	/**
	 * Checks the consistency of the {@link IDocumentModel}.
	 *
	 * @param documentModel
	 * 		the {@link IDocumentModel} which should be checked. May not be {@code null}. The
	 * 		given model has to be expanded. If an unexpanded {@link IDocumentModel} is
	 * 		checked, an exception may occur.
	 * @param notificationReceiver
	 * 		the problems that occurred during consistency check will be reported to this callback
	 * 		as {@link RankedNotification}s. May not be {@code null}.
	 * 		<p>
	 * 		For the reported problems, {@link RankedNotification#getSource()} always returns {@code null} or an instance of one of
	 * 		the following:
	 * 		<ul>
	 * 		<li>{@link IDocumentModel}</li>
	 * 		<li>{@link IGroup}</li>
	 * 		<li>{@link IField}</li>
	 * 		<li>{@link com.mgmtp.a12.kernel.md.model.api.IRule}</li>
	 * 		<li>{@link com.mgmtp.a12.kernel.md.model.api.IComputation}</li>
	 * 		<li>{@link IFieldTypeDefinition}</li>
	 * 		<li>{@link com.mgmtp.a12.kernel.md.model.api.IDocumentUniquenessCriterion}</li>
	 * 		</ul>
	 * @throws DocumentModelExpansionException
	 */
	void checkConsistency(@NonNull IDocumentModel documentModel, @NonNull Consumer<RankedNotification> notificationReceiver);

	// ==================================
	// code generation
	// ==================================

	/**
	 * Generates the specified validation code.
	 *
	 * @param documentModel
	 * 		the {@link IDocumentModel} from which the code should be generated.
	 * 		May not be {@code null}.
	 * @param config
	 * 		configuration of the code generation (see
	 *        {@link IValidationCodeGeneratorConfig}). May not be {@code null}.
	 * @param properties
	 * 		if not {@code null} and {@link ProgrammingLanguage#JAVA} code is to be generated,
	 * 		properties are added to this parameter ({@literal =>} <b>output parameter</b>).
	 * 		Properties that are added enable the instantiation of entry points in a later
	 * 		validation call. The validation call could even be executed in a different java
	 * 		process.<br>
	 * 		<b>Hint:</b> Those properties need to be passed to the static runtime
	 * 		service (see IDocumentStaticServiceConfig#getMappingProperties()) when
	 * 		running the validation for {@link ProgrammingLanguage#JAVA}.<br>
	 * @param notificationReceiver
	 * 		the {@link RankedNotification}s, found during the process, will be reported to this callback. May not be {@code null}.
	 * @return zipped source code as a byte array. For {@link ProgrammingLanguage#JAVA} the zip is laid out as a
	 * 		standalone Gradle Java project: sources under {@code src/main/java/}, resources (including
	 * 		{@code runtime-version.properties}) under {@code src/main/resources/}, with {@code build.gradle} and
	 * 		{@code settings.gradle} at the root. Returns {@code null} on errors (see {@code notificationReceiver}).
	 */
	byte[] generateValidationCode(@NonNull IDocumentModel documentModel, @NonNull IValidationCodeGeneratorConfig config,
			Properties properties,
			@NonNull Consumer<RankedNotification> notificationReceiver);

	/**
	 * Searches the given document model for the input annotation key. The model itself, fields, groups, rules and
	 * computations are searched for the annotation. The found elements and the corresponding annotation values are returned as a
	 * map.
	 * If no value is assigned to the annotation, an empty String is returned as the value of the annotation.
	 *
	 * @param documentModel
	 * 		the document model to be searched. May not be {@code null}.
	 * @param annotationKey
	 * 		the annotation to search for. May not be {@code null}.
	 * @return an empty map, if the given annotation key is not present in the model. Otherwise a map of element paths to the
	 * 		corresponding value of the annotation. If the model itself has the annotation, the key in the resulting map will be an
	 * 		empty string.
	 */
	Map<String, String> getAllAnnotatedElements(@NonNull IDocumentModel documentModel, @NonNull String annotationKey);
}
