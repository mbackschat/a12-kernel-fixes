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

import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.md.datatransfer.a12internal.mappingmodel.SourceAndTargetDMForSMM;
import com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferCodegenErrorLookup;
import com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.DataTransferCodegenException;
import com.mgmtp.a12.kernel.md.datatransfer.internal.errormessages.LocaleIndependentNotification;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.rt.internal.service.Message2RankedNotification;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;

public class MappingModelUtils {
	private MappingModelUtils() {
		throw new UnsupportedOperationException();
	}

	public record SmmGenInfos(
			@NonNull MappingModelForCodegen mappingModelForCodegen,
			@NonNull SourceAndTargetDMForSMM sourceAndTargetDMForSMM,
			@NonNull Map<String, String> originalRootGroupNamesBySourceName
	) {
		/**
		 * @return the info necessary to generate the SMM, {@code null} if it could not be created due to errors.
		 */
		public static SmmGenInfos create(
				@NonNull MM_MappingModel_2 mappingModel,
				@NonNull IDocumentModelResolver resolver,
				@NonNull Consumer<? super RankedNotification> notificationConsumer,
				@NonNull Locale locale
		) {
			MappingModelForCodegen mappingModelForCodegen = getMappingModelForCodegen(mappingModel, notificationConsumer, locale);
			if (mappingModelForCodegen == null) {
				return null;
			}

			SourceAndTargetDMForSMM sourceAndTargetDMForSMM = reportAndReturnNullOnCodegenException(() -> {
				return mappingModelForCodegen.createSourceAndTargetDMForSMM(resolver,
						localizingWrapper(notificationConsumer, locale));
			}, notificationConsumer, locale);
			if (sourceAndTargetDMForSMM == null) {
				return null;
			}

			Map<String, String> originalRootGroupNamesBySourceName = reportAndReturnNullOnCodegenException(() -> {
				return mappingModelForCodegen.getOriginalRootGroupNamesBySourceName(resolver);
			}, notificationConsumer, locale);
			if (originalRootGroupNamesBySourceName == null) {
				return null;
			}

			return new SmmGenInfos(mappingModelForCodegen, sourceAndTargetDMForSMM, originalRootGroupNamesBySourceName);
		}
	}

	public static Consumer<LocaleIndependentNotification> localizingWrapper(
			Consumer<? super RankedNotification> wrapped,
			Locale locale
	) {
		return n -> new DataTransferCodegenErrorLookup(locale).localizeAndReport(n, wrapped);
	}

	public static MM_MappingModel_2 ensureSmmId(MM_MappingModel_2 mappingModel) {
		return ensureSmmId(mappingModel, "ensured-id");
	}

	/**
	 * If the provided mapping model does not have an SMM id set, the SMM id is set to {@code defaultId}. This is needed in order to
	 * prevent the SMM_MISSING rule in the metamodel from firing even though it is perfectly fine for the mapping model to not have
	 * it set at this point.
	 */
	public static MM_MappingModel_2 ensureSmmId(MM_MappingModel_2 mappingModel, String defaultId) {
		String idOrNull = mappingModel._at(MM_MappingModel_2._pointer().content().structuralMappingModel().id());
		if (StringUtils.isEmpty(idOrNull)) {
			return mappingModel._with(MM_MappingModel_2._pointer().content().structuralMappingModel().id(), defaultId);
		}
		return mappingModel;
	}

	/**
	 * @return the {@link MappingModelForCodegen}, or {@code null} if there were any errors during metamodel validation.
	 */
	public static MappingModelForCodegen getMappingModelForCodegen(MM_MappingModel_2 mappingModelDocument,
			Consumer<? super RankedNotification> notificationReceiver, Locale locale) {
		return FixedShapeMappingModelForCodegen.of(mappingModelDocument,
				message -> notificationReceiver.accept(Message2RankedNotification.of(message)), locale);
	}

	public static <T> T reportAndReturnNullOnCodegenException(
			Supplier<T> supplier,
			Consumer<? super RankedNotification> notificationReceiver,
			Locale locale
	) {
		try {
			return supplier.get();
		} catch (DataTransferCodegenException e) {
			new DataTransferCodegenErrorLookup(locale).reportFromException(e, notificationReceiver);
			return null;
		}
	}
}
