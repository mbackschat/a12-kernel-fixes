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

package com.mgmtp.a12.kernel.md.dependency.internal;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views.MM_DependencyModel_1;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content.AlternativeErrorMessages;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content.TriggerFields;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content._alternativeerrormessages.ErrorMessages;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content._triggerfields.DependentEntities;
import com.mgmtp.a12.kernel.mmtypings.extmpinternal.mm_dependencymodel_1_codegen.views._mm_dependencymodel_1._content._triggerfields.TriggerAction;

/**
 * Factory helpers for assembling the typed {@code MM_DependencyModel_1} view from plain values, so the
 * verbose {@code _empty()._with(_pointer()…)} builder chains live in one place - used by the
 * {@code DependencyModelGenerator} and (especially) by the tests.
 */
public final class DependencyModelUtil {

	private DependencyModelUtil() {
		throw new UnsupportedOperationException("Should not be instantiated");
	}

	/** A dependency model with the given header id, trigger fields and alternative error messages. */
	public static MM_DependencyModel_1 createDependencyModel(String id, List<TriggerFields> triggerFields,
			List<AlternativeErrorMessages> alternativeMessages) {
		var contentPointer = MM_DependencyModel_1._pointer().content();
		return MM_DependencyModel_1._empty()
				._with(MM_DependencyModel_1._pointer().header().id(), id)
				._with(contentPointer.triggerFields(), triggerFields)
				._with(contentPointer.alternativeErrorMessages(), alternativeMessages);
	}

	/** A trigger field with its action and the entities it hides. */
	public static TriggerFields createTriggerField(String triggerFieldPath, TriggerAction triggerAction,
			List<DependentEntities> dependentEntities) {
		return TriggerFields._empty()
				._with(TriggerFields._pointer().triggerFieldName(), triggerFieldPath)
				._with(TriggerFields._pointer().triggerAction(), triggerAction)
				._with(TriggerFields._pointer().dependentEntities(), dependentEntities);
	}

	/** A dependent entity, optionally carrying a semantic index value ({@code null} for all repetitions). */
	public static DependentEntities createDependentEntity(String dependentEntityPath, String semanticIndexValue) {
		return DependentEntities._empty()
				._with(DependentEntities._pointer().dependentEntity(), dependentEntityPath)
				._with(DependentEntities._pointer().semanticIndexValue(), semanticIndexValue);
	}

	/** An alternative error message for a rule, one entry per language (keyed by {@link Locale#getLanguage()}). */
	public static AlternativeErrorMessages createAlternativeMessage(String rulePath, Map<Locale, String> localeToMessageText) {
		var errorMessages = localeToMessageText.entrySet().stream()
				.map(entry -> ErrorMessages._empty()
						._with(ErrorMessages._pointer().language(), entry.getKey().getLanguage())
						._with(ErrorMessages._pointer().errorMessage(), entry.getValue()))
				.toList();
		return AlternativeErrorMessages._empty()
				._with(AlternativeErrorMessages._pointer().ruleName(), rulePath)
				._with(AlternativeErrorMessages._pointer().errorMessages(), errorMessages);
	}
}
