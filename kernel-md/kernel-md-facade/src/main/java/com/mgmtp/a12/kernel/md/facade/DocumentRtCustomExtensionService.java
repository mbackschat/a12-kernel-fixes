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

package com.mgmtp.a12.kernel.md.facade;

import java.util.Objects;

import com.mgmtp.a12.kernel.core.customfieldtype.CustomFieldTypeService;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.custom.impl.CustomConditionService;
import com.mgmtp.a12.kernel.md.rt.api.ICustomConditionFactory;
import com.mgmtp.a12.kernel.md.rt.internal.service.CustomConditionFactoryWrapper;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Service provides projects the ability to extend kernel with their own functionality.
 *
 * @deprecated Will be removed with A12K-3589. This class—and consequently the ability to statically register your
 *        {@link ICustomFieldTypeFactory}—will be removed.
 * 		Please use {@code DocumentProcessingConfig#customFieldTypes()} instead  to provide your implementations e.g. for validation,
 * 		computation or mapping.
 */
@OnlyForUsage
@SuppressWarnings("removal")
@Deprecated(since = "30.6.0", forRemoval = true)
public class DocumentRtCustomExtensionService {

	/**
	 * Universally registers custom conditions to be available to all future validation calls.
	 *
	 * @param customConditionFactory
	 * 		The custom condition factory to be registered. May not be {@code null}.
	 * @return {@code true} if any previously registered custom condition name has been overwritten
	 * 		by this call, otherwise {@code false}.
	 *
	 * @deprecated since version 29.2.0. The implementation is now obsolete. There is always {@code false} returned.
	 */
	@Deprecated(since = "29.2.0")
	public boolean registerCustomConditions(ICustomConditionFactory customConditionFactory) {
		Objects.requireNonNull(customConditionFactory);
		CustomConditionService.getInstance().registerV2(new CustomConditionFactoryWrapper(customConditionFactory));
		return false;
	}

	/**
	 * Universally registers custom conditions to be available to all future validation calls.
	 *
	 * @param customConditionFactory
	 * 		The custom condition factory to be registered. May not be {@code null}.
	 * 		<b>Note:</b> A previously registered custom condition name cannot be overwritten with this call.
	 */
	public void registerCustomConditionsV2(@NonNull ICustomConditionFactory customConditionFactory) {
		Objects.requireNonNull(customConditionFactory);
		CustomConditionService.getInstance().registerV2(new CustomConditionFactoryWrapper(customConditionFactory));
	}

	/**
	 * Universally registers custom field types to be available to all future validation/conversion
	 * calls.
	 *
	 * @param customFieldTypeFactory
	 * 		The custom field type factory to be registered. May not be {@code null}.
	 * @return {@code true} if any previously registered custom field type name has been overwritten
	 * 		by this call, otherwise {@code false}.
	 *
	 * @deprecated since version 29.2.0. The implementation is now obsolete. There is always {@code false} returned.
	 */
	@Deprecated(since = "29.2.0")
	public boolean registerCustomFieldTypes(ICustomFieldTypeFactory customFieldTypeFactory) {
		Objects.requireNonNull(customFieldTypeFactory);
		CustomFieldTypeService.getInstance().registerV2(customFieldTypeFactory);
		return false;
	}

	/**
	 * Universally registers custom field types to be available to all future validation/conversion
	 * calls.
	 *
	 * @param customFieldTypeFactory
	 * 		The custom field type factory to be registered. May not be {@code null}.
	 * 		<b>Note:</b> A previously registered custom field type name cannot be overwritten with this call.
	 */
	public void registerCustomFieldTypesV2(@NonNull ICustomFieldTypeFactory customFieldTypeFactory) {
		Objects.requireNonNull(customFieldTypeFactory);
		CustomFieldTypeService.getInstance().registerV2(customFieldTypeFactory);
	}
}
