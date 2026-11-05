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

package com.mgmtp.a12.kernel.md.dependency.internal.check;

/**
 * Error codes reported while building and checking the dependency model from the document-model
 * annotations. Each constant has a matching message in the {@code dependency_model_check_error_messages}
 * resource bundle (see {@link DependencyModelCheckErrorLookup}).
 *
 * <p>The first group are the annotation-grammar codes (build-time transformation, WP6); the remaining
 * codes are the cross-document-model consistency checks (No-Metamodel-Checks) run once the model has been
 * built and validated against the metamodel. The concept groups the reference-not-found cases under a
 * single {@code MVK_INVALID_ENTITY}; they are split here into one code per entity kind so each carries its
 * own business-analyst-facing message (the concept explicitly permits renaming the provisional codes).
 */
public enum DependencyModelCheckErrorType {

	/** A {@code DependentEntity} annotation value does not match the trigger-spec grammar. */
	MVK_MALFORMED_DEPENDENT_ENTITY,

	/** A {@code DependentEntity} trigger spec carries an index separator but no index value. */
	MVK_MALFORMED_DEPENDENT_ENTITY_INDEX,

	/** A {@code DependentEntity} references a trigger field that is not marked with a {@code TriggerField} annotation. */
	MVK_TRIGGER_FIELD_NOT_ANNOTATED,

	/** A trigger field references a path that is not an existing field of the root document model. */
	MVK_INVALID_TRIGGER_FIELD,

	/** A dependent entity references a path that is not an existing field or group of the root document model. */
	MVK_INVALID_DEPENDENT_ENTITY,

	/** An alternative message references a rule that does not exist in the root document model. */
	MVK_INVALID_RULE,

	/**
	 * A dependent group and one of its (in)direct sub-entities are both listed as dependent entities of the
	 * same trigger field (a redundant, indirect reference).
	 */
	MVK_INVALID_INDIRECT_REFERENCE,

	/** A dependent entity carries an index value but no group on its path has an index field. */
	MVK_INVALID_INDEX_FIELD,

	/** A dependent entity's index value is not an admissible value of the resolving index field. */
	MVK_INVALID_INDEX_VALUE,

	/** A dependent entity carries an index value but more than one group on its path has an index field. */
	MVK_MULTIPLE_INDEX_FIELDS,

	/** An alternative message uses a language that is not supported by the root document model. */
	MVK_INVALID_LANGUAGE,

	/**
	 * A repeatable group lies between the common ancestor of the trigger field and the dependent entity and
	 * the group of the dependent entity's index field, so the semantic index could not resolve to a single
	 * repetition.
	 */
	MVK_INVALID_INDEX_FOR_TRIGGER,

	/** An alternative message references a parameter, which is not allowed (parameters are already substituted). */
	MVK_INVALID_ALTERNATIVE_MESSAGE_PARAMETER
}
