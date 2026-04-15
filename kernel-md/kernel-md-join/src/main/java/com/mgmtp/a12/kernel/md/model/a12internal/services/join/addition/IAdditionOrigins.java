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

package com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition;

import com.mgmtp.a12.model.header.Annotation;

/**
 * In the addition context, it is good to know where an element, a type definition or the annotations in the joined document model
 * come from.
 * <p>
 * This interface offers a way to cache that information during the join process in which a single model is added.
 */
public interface IAdditionOrigins {
	/**
	 * In the addition context, it is good to know where the (model or element) annotations exactly come from.
	 */
	interface IAnnotationsOrigins {
		/**
		 * @param annotation
		 *        {@link Annotation} coming from the root model which should be added. Is never {@code null}.
		 */
		void addRootAnnotation(Annotation annotation);

		/**
		 * @param annotation
		 *        {@link Annotation} coming from the additive model which should be added. Is never {@code null}.
		 */
		void addAdditiveAnnotation(Annotation annotation);

		/**
		 * @param annotation
		 *        {@link Annotation} which is present in both (root and additive) models and which should be added. Is never
		 *        {@code null}.
		 */
		void addCommonAnnotation(Annotation annotation);

	}

	/**
	 * This method adds the given {@code id} to the cache for the root, <b>only if</b> the {@code id} was not already added to
	 * the cache for the additive model (see {@link #addAdditiveElement(String)}) or to the cache for the common elements (see
	 * {@link #addCommonElement(String)} and especially {@link #addCompletelyOverwrittenRootElement(String)}).
	 *
	 * @param id
	 * 		element id coming from the root model which should be added. Is never {@code null} or empty.
	 */
	void addRootElement(String id);

	/**
	 * This method adds the given {@code id} to the cache for the additive model, <b>only if</b> the {@code id} was not already
	 * added to the cache for the root (see {@link #addRootElement(String)}) or to the cache for the common elements (see
	 * {@link #addCommonElement(String)}).
	 *
	 * @param id
	 * 		element id coming from the additive model which should be added. Is never {@code null} or empty.
	 */
	void addAdditiveElement(String id);

	/**
	 * This method adds the given {@code id} to the cache for the common elements, <b>only if</b> the {@code id} was not
	 * already added to the cache for the root (see {@link #addRootElement(String)}) or to the cache for the additive model (see
	 * {@link #addAdditiveElement(String)}). For common rules and computations, see also
	 * {@link #addCompletelyOverwrittenRootElement(String)}.
	 *
	 * @param id
	 * 		element id which is present in both (root and additive) models and which should be added. Is never {@code null} or empty.
	 */
	void addCommonElement(String id);

	/**
	 * This method is exclusively for rules and computations which are present in both (root and additive) models. In this case,
	 * the elements in the root model are completely overwritten. The id in the joined model (the id in the additive model is
	 * modified to ensure unique ids in the joined model) is stored in
	 * {@link #addCommonElement(String)} and in this method, the corresponding id from the root model is cached.
	 *
	 * @param id
	 * 		element id which is present in the root model and which should be added. Is never {@code null} or empty.
	 */
	void addCompletelyOverwrittenRootElement(String id);

	/**
	 * This method adds the given {@code id} to the cache for the root, <b>only if</b> the {@code id} was not already added to
	 * the cache for the additive model (see {@link #addAdditiveTypeDef(String)}) or to the cache for the common type defs (see
	 * {@link #addCommonTypeDef(String)}).
	 *
	 * @param id
	 * 		type def id coming from the root model which should be added. Is never {@code null} or empty.
	 */
	void addRootTypeDef(String id);

	/**
	 * This method adds the given {@code id} to the cache for the additive model, <b>only if</b> the {@code id} was not already
	 * added to the cache for the root (see {@link #addRootTypeDef(String)}) or to the cache for the common type defs (see
	 * {@link #addCommonTypeDef(String)}).
	 *
	 * @param id
	 * 		type def id coming from the additive model which should be added. Is never {@code null} or empty.
	 */
	void addAdditiveTypeDef(String id);

	/**
	 * This method adds the given {@code id} to the cache for the common type defs, <b>only if</b> the {@code id} was not already
	 * added to the cache for the root (see {@link #addRootTypeDef(String)}) or to the cache for the additive model (see
	 * {@link #addAdditiveTypeDef(String)}).
	 *
	 * @param id
	 * 		type def id which is present in both (root and additive) models and which should be added. Is never {@code null} or empty.
	 */
	void addCommonTypeDef(String id);

	/**
	 * <b>Note regarding annotations directly in the document model:</b>  This method will always be called no matter if there are
	 * common annotations between the root and additive models (see also
	 * {@link IAnnotationsOrigins#addCommonAnnotation(Annotation)}).
	 *
	 * @param id
	 * 		element id which is present in both (root and additive) models and for which the {@link IAnnotationsOrigins} should be
	 * 		added. For the annotations directly in the document model, use the empty string. Is never {@code null}.
	 * @param origins
	 * 		information regarding where the annotations stored for {@code id} come from. Is never {@code null}.
	 */
	void addAnnotationsForCommon(String id, IAnnotationsOrigins origins);

	/**
	 * This method is exclusively for elements which are present in both (root and additive) models, for which the element in the
	 * additive model overwrites the element in the root model. With this method, a mapping is created between
	 * the id coming from the root model and the corresponding id which is created for the joined model (modified id from the
	 * additive model).
	 *
	 * @param idInJoined
	 * 		element id from the joined model. Is never {@code null}.
	 * @param idInRoot
	 * 		id of the corresponding element in the root model. Is never {@code null}.
	 */
	void addIdInRootByIdInJoined(String idInJoined, String idInRoot);

	/**
	 * This method is exclusively for elements which are present in the additive model. With this
	 * method, a mapping is created between the original id coming from the additive model and the corresponding id which
	 * is created for the joined model.
	 *
	 * @param idInJoined
	 * 		element id from the joined model. Is never {@code null}.
	 * @param idInAdditive
	 * 		id of the corresponding element in the additive model. Is never {@code null}.
	 */
	void addIdInAdditiveByIdInJoined(String idInJoined, String idInAdditive);

}
