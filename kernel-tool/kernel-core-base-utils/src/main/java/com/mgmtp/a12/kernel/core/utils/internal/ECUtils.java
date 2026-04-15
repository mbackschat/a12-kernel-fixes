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

package com.mgmtp.a12.kernel.core.utils.internal;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;

public class ECUtils {

	private ECUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * @param modelSpecificSettings
	 * 		the properties to be tested.
	 * @return {@code true} if the property {@link IModelSpecificSettings#getBaseYear()} is present,
	 * 		otherwise {@code false}.
	 */
	public static boolean isBaseYearDefined(IModelSpecificSettings modelSpecificSettings) {
		return modelSpecificSettings.getBaseYear().isPresent();
	}

	/**
	 * <b>Note:</b> this method works only for A12 models. It considers following cases:
	 * <ol>
	 * <li>absolute (field or group) reference</li>
	 * <li>relative (field or group) reference</li>
	 * <li>reference by the <b>field</b> short name if it is unique</li>
	 * <li>special cases for above cases 2 and 3 that arise if the addition of missing entities are possible (typically in an
	 * additive model fragment), if ec is a {@link IMissingEntityEC}</li>
	 * </ol>
	 * <p>
	 *
	 * @param ec
	 * 		entity container of the model. If this is an instance of {@link IMissingEntityEC}, special case handling is triggered.
	 * @param ruleGroup
	 * 		group full name in which the rule exists
	 * @param referencedEntity
	 * 		reference of the entity like it was defined in the rule
	 * @return the {@link IField}- or {@link IGroup}-instance of the entity referenced in a rule (error entity, entity in error message,
	 * 		entity in error condition text). If the entity was not found, {@code null} is returned.
	 */
	public static IEntity getEntityInRule(IEC ec, String ruleGroup, String referencedEntity) {
		if (referencedEntity.startsWith("/")) {
			// entity is referenced absolutely
			return ec.get(referencedEntity);
		} else if (referencedEntity.contains("/") || referencedEntity.contains("..")) {
			// _must_ be a relative path
			// Note: ".." can't really happen, can only reference a group if no "/" occurs
			String entityFullName = String.format("%s/%s", ruleGroup, referencedEntity);
			return ec.get(entityFullName);
		} else {
			// reference by shortname OR by "relative path" where the entity is directly in the rule group.
			IEC nonHallucinatingIEC = ec instanceof IMissingEntityEC missingEntityEC ? missingEntityEC.getOriginalEC() : ec;
			String entityFullNameIfRelativePathToFieldDirectlyInRuleGroup = String.format("%s/%s", ruleGroup, referencedEntity);
			IEntity fieldDirectlyInRuleGroupIfPresent = nonHallucinatingIEC.get(
					entityFullNameIfRelativePathToFieldDirectlyInRuleGroup);
			if (fieldDirectlyInRuleGroupIfPresent != null) {
				return fieldDirectlyInRuleGroupIfPresent; // this case is unambiguous even if missing fields with same shortname
				// are added, because the relative-path-to-field-directly-in-rulegroup case is defined to have precedence over
				// shortname refs
			} else {
				// must be a shortname ref to somewhere else, or a missing entity
				var shortNameCandidates = nonHallucinatingIEC.shortName2FullName(referencedEntity);
				if (shortNameCandidates.size() == 1) {
					// if there is a unique shortname, we _ASSUME_ that this is the correct entity, even if it could
					// become ambiguous once missing entities are added.
					IEntity unambiguousEntity = nonHallucinatingIEC.get(shortNameCandidates.iterator().next());
					assert unambiguousEntity != null;
					return unambiguousEntity;
				} else if (shortNameCandidates.size() > 1) {
					// ambiguous -> error case, return null as documented, if no missing entities are possible.
					// if missing entities are possible, the reference is still ambiguous, unless one of the added missing
					// entities is at entityFullNameIfRelativePathToFieldDirectlyInRuleGroup: this matches the
					// "relative path directly in rule group" case, which has precedence over shortname refs and thereby removes
					// the ambiguity. We _must_ assume this in the IMissingEntityEC case, because otherwise we would wrongly
					// always raise an error for an additive fragment model where the error field is directly in the rule group
					// but only defined in the base model and other fields with the same shortname exist.
					// TODO we could also require, for this case, that the modeler duplicates the field in the additive fragment
					// but this seems a bit unclean, because it would burden the "clean" shortname-ref-less usecase in order to
					// better support the disfavoured shortname-ref case.
					return ec instanceof IMissingEntityEC missingEntityEC // this distinction is not strictly necessary, as a
							// non-MissingEntityEC would simply return null anyway, but this seems more explicit
							? missingEntityEC.get(entityFullNameIfRelativePathToFieldDirectlyInRuleGroup)
							: null;
				} else {
					assert shortNameCandidates.isEmpty();
					// no non-missing entity matches for either
					// a) relative path to field directly in rule group
					// b) shortname reference
					// if missing entities can be ruled out (no IMissingEntityEC), we have an error, otherwise we can
					// guess where the missing field might be (we cannot know, but in this case it seems reasonable to
					// assume that the reference is a relative-path reference, because this assumption will make relative path
					// references work in this case, and is in line with our genearl preference for discouraging shortnames (due to
					// their potential ambiguity)
					return ec instanceof IMissingEntityEC missingEntityEC // this distinction is not strictly necessary, as a
							// non-MissingEntityEC would simply return null anyway, but this seems more explicit
							? missingEntityEC.get(entityFullNameIfRelativePathToFieldDirectlyInRuleGroup)
							: null;
				}
			}
		}
	}

	/**
	 * like {@link #getEntityInRule(IEC, String, String)}, but casts to {@link IField}.
	 */
	public static IField getFieldInRule(IEC ec, String ruleGroup, String referencedField) {
		return (IField) getEntityInRule(ec, ruleGroup, referencedField);
	}

}
