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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IFieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.IGroup.IIncludeDetails;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig.ProgrammingLanguage;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.model.header.ModelReference;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;

/**
 * This service offers actions which can be run on a document model.
 */
@OnlyForUsage
public interface IDocumentModelService {

	// ==================================
	// handling of referenced models
	// ==================================

	/**
	 * This method <b>recursively</b> expands the base {@link IDocumentModel} as follows
	 * <ul>
	 * <li>the {@link IGroup}s representing an include will contain the corresponding group/field
	 * subtree (the rules and computations will be added according to the corresponding include definitions - see
	 * {@link IIncludeDetails#excludeRules()} and {@link IIncludeDetails#excludeComputations()})</li>
	 * <li>all the {@link IFieldTypeDefinition}s coming from all included {@link IDocumentModel}s
	 * and recursively included {@link IDocumentModel}s will be added to the list of
	 * {@link IFieldTypeDefinition}s of the base
	 * {@link IDocumentModel}. For such includes {@link ModelReference#getPurpose()} is equal to
	 * <i>{@code typeDefinitions}</i>.</li>
	 * <li>the {@link ModelReference}s of included {@link IDocumentModel}s are added to the list of {@link ModelReference}s of the
	 * base {@link IDocumentModel} with {@link ModelReference#getPurpose()} <i>{@code transitiveInclude}</i> if their
	 * {@link ModelReference#getPurpose()} is either <i>{@code include}</i> or <i>{@code transitiveInclude}</i>.
	 * </li>
	 * <li>in conditions of rules (and preconditions and operations of computations) from <b>included</b> models, paths of groups and
	 * fields will be converted as follows: Paths that contain the name of the root group of the included model (including absolute
	 * paths) will be replaced by equivalent relative paths (with implicit turning point) that do not contain the name of the root
	 * group; this is done in order to allow includes where the name of the including group is not the same as the name of the
	 * included model's root group.</li>
	 * </ul>
	 * Note:<br/>
	 * If the document model or any of its included document models are invalid, the method will try to expand on a best-effort
	 * basis if possible. This means that if the inputs are invalid the method may still succeed, but at least some aspects of the
	 * resulting document model will then be invalid too. If users intend to ensure the validity of their models, they should
	 * use {@link #checkConsistency(IDocumentModel, Consumer)} on all included models prior to expansion, and on the expanded document model
	 * afterwards.
	 *
	 * @param documentModel
	 * 		the base {@link IDocumentModel} which should be expanded. May not be {@code null}.
	 * @param resolver
	 * 		implementation to retrieve the included {@link IDocumentModel}s. May not be
	 *        {@code null}.
	 * @throws DocumentModelExpansionException
	 * 		If errors occurred during expansion that cannot be ignored, e.g. if the model is invalid, in a way that does not allow
	 * 		the expansion to continue. (To ensure the absence of inconsistencies, use
	 *        {@link #checkConsistency(IDocumentModel, Consumer)}).
	 */
	void expand(@NonNull IDocumentModel documentModel, @NonNull IDocumentModelReferenceResolver resolver);

	/**
	 * This method reverts the expansion done in
	 * {@link #expand(IDocumentModel, IDocumentModelReferenceResolver)}.
	 *
	 * @param documentModel
	 * 		the expanded {@link IDocumentModel} which should be collapsed. May not be
	 *        {@code null}.
	 */
	void collapse(@NonNull IDocumentModel documentModel);

	/**
	 * @param modelRef
	 * 		reference to the type definition document model which is to be imported. May not
	 * 		be {@code null}.
	 * @param resolver
	 * 		implementation to retrieve the included {@link IDocumentModel}s. May not be
	 *        {@code null}.
	 * @param supportedLocales
	 * 		locales that the imported type definition document models must support. May not be
	 *        {@code null}.
	 * @return a list of the type definitions defined in the referenced document model, together
	 * 		with all transitively imported type definitions starting from the referenced document
	 * 		model.
	 */
	List<IFieldTypeDefinition> extractImportedTypeDefinitions(@NonNull ModelReference modelRef,
			@NonNull IDocumentModelReferenceResolver resolver,
			@NonNull List<Locale> supportedLocales);

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
	 * @return zipped source code as a byte array.
	 */
	byte[] generateValidationCode(@NonNull IDocumentModel documentModel, @NonNull IValidationCodeGeneratorConfig config,
			Properties properties,
			@NonNull Consumer<RankedNotification> notificationReceiver);

	// ==================================
	// utilities
	// ==================================

	/**
	 * @param documentModel
	 * 		the {@link IDocumentModel} to be analyzed. May not be {@code null}.
	 * @return a {@link Set} with the used variants in the {@link IDocumentModel}. Is never
	 *        {@code null}. If no specific variant is used, the set is empty.
	 */
	@NonNull Set<String> extractUsedVariants(@NonNull IDocumentModel documentModel);

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
