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

package com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.custom.ICustomConditionFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

@OnlyForUsage
public interface IDataTransferService {

	/**
	 * Provides the configuration for data transfer similar to {@link ValidationMode}. In the long run both implementations will be
	 * merged.
	 */
	interface DataTransferConfig {
		/**
		 * the locale for the validation error messages during source-data validation.
		 * see also {@link ValidationMode#getLanguage()}.
		 */
		Locale validationLanguage();

		/**
		 * @return the {@link ICustomFieldTypeFactory} or {@code null}.
		 */
		ICustomFieldTypeFactory customFieldTypeFactory();

		/**
		 * @return the {@link ICustomConditionFactory} or {@code null}.
		 */
		ICustomConditionFactory customConditionFactory();

		/**
		 * see {@link ValidationMode#currentDateForTest()}
		 */
		Date currentDateForTest();

		/**
		 * @param validationLanguage
		 * 		see {@link #validationLanguage()}
		 * @return The {@link DataTransferConfigBuilder}. Never {@code null}.
		 */
		static DataTransferConfigBuilder builder(@NonNull Locale validationLanguage) {
			return new DataTransferConfigBuilder(validationLanguage);
		}
	}

	/**
	 * Performs the data transfer and returns a {@link MappedData} with the update. The input IData for target and sources are not
	 * changed.
	 * <p/>
	 * Some warnings and errors are possible at runtime and are reported to the {@link IResult}s of the individual
	 * {@link SourceData}, {@code targetResult} and {@code precomputationConsistencyResult}:
	 * <ol>
	 *     <li>A {@code SourceData}'s {@code IResult} receives problem messages when
	 *     	   <ol>
	 *             <li>The SourceData is deselected from the datatransfer based on the source validation rules.<br/>
	 *             The IResult can suppress specific errors from this step by not including them in the count that is reported by
	 *             {@link IResult#getCount(SeverityType)} (and the other overloads of this method). In that case, the error will
	 *             be ignored regarding the selection or deselection of this SourceData. Note the limitations of
	 *             error-suppression that are described in the documentation of {@link IResult#getCountOf(IResult.ErrorType)}</li>
	 *             <li>The SourceData is deselected from the datatransfer because it exceeded the maximum allowed number of source
	 *                 instances for this datatransfer.</li>
	 *     	   </ol>
	 *     	   Note: in order to determine which SourceData have been included or deselected for the data transfer, use
	 *           {@link MappedData#selectedSourceIds()}. Determining this based on the error count of the corresponding IResult
	 *     	   is not guaranteed to be reliable.
	 *     </li>
	 *     <li>{@code targetResult} receives problem messages that concern target fields. Cases where this can happen:
	 *         <ol>
	 *             <li>A field instance from the target that is an input to a precomputation was formally invalid.
	 *             If this occurs, some values might be omitted from the mapping because some or all precomputations might get
	 *             skipped e.g. if they directly or indirectly depend on the invalid value. The mapping will nonetheless be
	 *             successful, but potentially incomplete.</li>
	 *         </ol>
	 *     </li>
	 *     <li>{@code precomputationConsistencyResult} receives problem messages that are likely not useful for the
	 *     end-user. They can be caused by problematic modeling, but also by intentional modeling decisions.
	 *     The field/group ids that are referenced in these messages ({@code possiblyCausingEntities}, error fields,
	 *     {@code referencedFields}, {@code refOmissionErrorResponsible} and
	 *         {@code unknowns}) might originate from any of the target model, one of the source models or the precomputation fragment.
	 *         They implement {@link SourceIdentifier}
	 *         (in addition to {@link com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier}), so that in order to link
	 *         them to their origin, {@link SourceIdentifier#sourceId()} can be used (if multiple fields or groups are referenced in
	 *         the message, they might originate from different origins).<br>
	 *         If a source result suppresses some formal errors, the precomputation result must also suppress the corresponding formal errors (see
	 *         the section for source results for details on error suppression)<br>
	 *         Examples of cases where such errors or warnings can be reported:
	 *         <ol>
	 *             <li>Structural mapping reported a warning or error e.g. if collisions occurred</li>
	 *             <li>Errors occur during the precomputation step, where the error field is from the precomputation fragment. e.g.
	 *                 if a computed field value is formally invalid and is an input to another computation. In this case,
	 *                 the mapping result may be incomplete due to values that are omitted due to skipped precomputations.</li>
	 *             <li>Formal validation for a field from a source fails <em>during the precomputation step</em> (but not during the
	 *                 initial source-selection step). This is probably only possible if the modeling was unusual, e.g. if the
	 *                 precomputation fragment does not support the full character set of the source, or if a precomputation uses a
	 *                 field from the sources as the computed field, or if a source result suppressed an error (see section about
	 *                 the source results) and the precomputation result failed to suppress the same error.</li>
	 *         </ol>
	 *     </li>
	 * </ol>
	 *
	 * @return the {@link MappedData}, never null.
	 * 		<p>
	 * 		The mapped values are not validated against the target model and may be invalid.
	 * 		<p>
	 * 		In case of an incomplete datatransfer, details about the cause of the failure may be available in
	 *        {@code targetResult} (or {@code precomputationConsistencyResult}). Note that there is currently no explicit flag that
	 * 		indicates incomplete mapping results (where some values have been skipped); if this is needed, please contact the kernel
	 * 		team.
	 */
	// NOTE: currently, we don't document details of how suppression of particular errors behaves for targetResult and
	// precomputation result: for those IResults, only formal validation errors can occur, and suppression is not properly supported
	// for formal validation errors, see the note in the javadoc of IResult#getCountOf(IResult.ErrorType)
	@NonNull MappedData getTransferredData(
			@NonNull IData targetData,
			@NonNull List<SourceData> sourceData,
			@NonNull IResult targetResult,
			@NonNull IResult precomputationConsistencyResult,
			@NonNull DataTransferConfig config
	);

	/**
	 * @return the names of all sources that are supported by this data transfer service, suitable for
	 *        {@link SourceData#sourceName()}.
	 */
	@NonNull Set<String> supportedSourceNames();

	/**
	 * @return full names of all target fields that this data transfer may map to.
	 */
	@NonNull Set<String> getTargetFields();

	/**
	 * only intended for kernel-internal use
	 */
	IMetaModel _internal_getMetaObjectForSource(String sourceName);

	/**
	 * only intended for kernel-internal use.
	 * <p/>
	 * Note: in case of noSourceValidation, the validator may not actually have the original validation rules of the source model.
	 */
	IValidator _internal_getValidatorForSource(String sourceName);
}
