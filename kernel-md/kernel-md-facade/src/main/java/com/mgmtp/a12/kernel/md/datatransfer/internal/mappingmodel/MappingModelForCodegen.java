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

package com.mgmtp.a12.kernel.md.datatransfer.internal.mappingmodel;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.datatransfer.mappingmodel.IPrecompModelDataMapper;
import com.mgmtp.a12.kernel.md.datatransfer.a12internal.mappingmodel.SourceAndTargetDMForSMM;
import com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferCodegenException;
import com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferErrorEnum;
import com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.LocaleIndependentNotification;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;

import lombok.NonNull;

/**
 * Codegen-Time representation of a mapping model as defined by a document of MM_MappingModel/2
 * <p/>
 * Implementations are responsible for parameter validation, e.g. when creating an instance based on a
 * metamodel document, the metamodel validation checks, as well as appropriate additional checks, should
 * be performed.
 */
public interface MappingModelForCodegen {

	/**
	 * @return the names of the sources that are defined for this mapping model model.
	 */
	List<String> sourceNames();

	/**
	 * For the given sourceName, returns the id of the model (a dm or dor) that should be used to validate corresponding SourceData
	 * when the assignable SourceData are selected.
	 */
	String getSourceValidationModelId(String sourceName);

	/**
	 * For the given sourceName, returns whether the corresponding SourceData should be validated.
	 */
	boolean isNoSourceValidation(String sourceName);

	/**
	 * id of the dm or dor for the target model
	 */
	String getTargetModelId();

	/**
	 * @param resolver
	 * 		a document model resolver that can resolve the dms for all sources and the target, as well as the
	 * 		precomputation fragment. Note that, as specified in {@link IDocumentModelResolver}'s javadoc, the resolver is
	 * 		responsible for expanding includes in the resolved DMs, if any such includes exist.
	 * @param receiverForNonCriticalNotifications
	 * 		notifications about non-critical issues (i.e. issues that do not prevent successful creation of the precomputation dm)
	 * 		will be reported to this consumer.
	 * @throws DataTransferCodegenException
	 * 		if the operation fails due to inconsistencies between the mapping model and the
	 * 		referenced source/target/precomp models
	 */
	IDocumentModel createPrecomputationDm(IDocumentModelResolver resolver,
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications);

	/**
	 * Creates the joined DMs that contains the sources and target, onto which the precomputation fragment gets added.
	 * This method is meant to be used for editor support (SME).
	 * <p/>
	 * Parameters work like those of {@link #createPrecomputationDm(IDocumentModelResolver, Consumer)}
	 */
	IDocumentModel createBaseDMForPrecomputationFragment(IDocumentModelResolver resolver,
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications);

	/**
	 * Creates source and target DM as expected by the SMM _with_ rules and computations.
	 * This method is meant to be used for editor support (SME) and general tooling (e.g. SMM generation).
	 * <p/>
	 * Parameters work like those of {@link #createPrecomputationDm(IDocumentModelResolver, Consumer)}
	 *
	 * @throws DataTransferCodegenException
	 * 		may be thrown in case of problems, these exceptions should be handled and localized with
	 *        {@link MappingModelUtils#reportAndReturnNullOnCodegenException}
	 */
	SourceAndTargetDMForSMM createSourceAndTargetDMForSMM(IDocumentModelResolver resolver,
			Consumer<LocaleIndependentNotification> receiverForNonCriticalNotifications);

	/**
	 * The id of the Structural Mapping Model that is used in this Mapping Model. For ExtMP, the id may be null.
	 */
	String getStructuralMappingModelId();

	/**
	 * The type of this mapping model.
	 */
	@NonNull String getMappingType();

	/**
	 * @throws DataTransferCodegenException
	 * 		if the operation fails due to inconsistencies between the mapping model and the referenced source/target/precomp models
	 */
	IPrecompModelDataMapper rtPrecompModelDataMapper(IDocumentModelResolver resolver);

	/**
	 * @return a map from every root group name in the precomputation DM to its original name in the DM it came from. The original
	 * 		name may be {@code ""} if it was included with include level
	 *        {@link IGroup.IIncludeConfig.IncludeLevel#MODEL_ROOT}
	 *
	 * @throws DataTransferCodegenException
	 * 		may be thrown in case of problems, e.g. with
	 *        {@link DataTransferErrorEnum#SOURCE_HAS_MULTIPLE_ROOTGROUPS}, these exceptions should be handled and localized with
	 *        {@link MappingModelUtils#reportAndReturnNullOnCodegenException}
	 */
	@NonNull Map<String, String> getOriginalRootGroupNamesBySourceName(@NonNull IDocumentModelResolver resolver);
}
