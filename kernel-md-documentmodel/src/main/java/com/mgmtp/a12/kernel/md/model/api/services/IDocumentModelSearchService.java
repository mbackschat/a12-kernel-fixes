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
import java.util.Optional;
import java.util.OptionalInt;

import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.IRule;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * This service offers the search functionality in {@link IDocumentModel}s.
 * <p>
 * <b>Important note:</b> To create an instance see
 * DocumentModelServiceFactory#createDocumentModelSearchService(IDocumentModel).
 */
@OnlyForUsage
public interface IDocumentModelSearchService {

	/**
	 * Searches for an {@link IElement} by the given id.
	 *
	 * @param id
	 * 		element id. May not be {@code null} or empty.
	 * @return the corresponding {@link IElement} or an empty Optional instance if no
	 *        {@link IElement} was found.
	 */
	Optional<IElement> getById(@NonNull String id);

	/**
	 * Searches for an {@link IElement} by the given path.
	 *
	 * @param path
	 * 		element path. May not be {@code null} or empty.
	 * @return the corresponding {@link IElement} or an empty Optional instance if no
	 *        {@link IElement} was found.
	 */
	Optional<IElement> getByPath(@NonNull String path);

	/**
	 * Searches for a group or field path by the given id.
	 *
	 * @param id
	 * 		element id. May not be {@code null} or empty.
	 * @return the corresponding path or an empty Optional instance if no
	 *        {@link IGroup} or {@link IField} was found.
	 */
	Optional<String> getGroupOrFieldPathById(String id);

	/**
	 * Returns all {@link IElement}s of the document model, keyed by their id.
	 */
	Map<String, IElement> getAllElementsById();

	/**
	 * Returns all {@link IElement}s of the document model, keyed by their path.
	 */
	Map<String, IElement> getAllElementsByPath();

	/**
	 * @return an Optional with the {@link IGroup} at the given path, or empty if
	 * there is no group (no element or a non-group element) at that path in
	 * the DM.
	 */
	default Optional<IGroup> getGroupByPath(@NonNull String path) {
		return getByPath(path, IGroup.class);
	}

	/**
	 * @return an Optional with the {@link IField} at the given path, or empty if
	 * there is no field (no element or a non-field element) at that path in
	 * the DM.
	 */
	default Optional<IField> getFieldByPath(@NonNull String path) {
		return getByPath(path, IField.class);
	}

	/**
	 * @return an Optional with the {@link IRule} at the given path, or empty if
	 * there is no rule (no element or a non-rule element) at that path in
	 * the DM.
	 */
	default Optional<IRule> getRuleByPath(@NonNull String path) {
		return getByPath(path, IRule.class);
	}

	/**
	 * @return an Optional with the {@link IComputation} at the given path, or empty if
	 * there is no computation (no element or a non-computation element) at that path in
	 * the DM.
	 */
	default Optional<IComputation> getComputationByPath(@NonNull String path) {
		return getByPath(path, IComputation.class);
	}

	/**
	 * @return the {@link IGroup#getRepeatability()} of the group at the given path
	 * ({@code 1} for non-repeatable groups), or an empty {@link OptionalInt} if
	 * there is no group (no element or a non-group element) at that path in the DM.
	 */
	default OptionalInt getGroupRepeatability(@NonNull String path) {
		return getGroupByPath(path)
				.map(group -> OptionalInt.of(group.getRepeatability()))
				.orElseGet(OptionalInt::empty);
	}

	private <T extends IElement> Optional<T> getByPath(@NonNull String path, @NonNull Class<T> type) {
		return getByPath(path).filter(type::isInstance).map(type::cast);
	}
}
