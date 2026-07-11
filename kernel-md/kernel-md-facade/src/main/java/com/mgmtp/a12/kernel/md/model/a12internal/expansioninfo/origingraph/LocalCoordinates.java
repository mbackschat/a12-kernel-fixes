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

package com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.origingraph;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.ExpansionInfo;
import com.mgmtp.a12.kernel.md.model.api.IDocumentUniquenessCriterion;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * A sealed hierarchy of coordinates that uniquely identify a specific part of a local document
 * model. Coordinates are used as keys in {@link com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.ExpansionInfo}
 * to look up the {@link ExpansionNode}
 * of each expanded element.
 *
 * <p>The concrete coordinate types are:</p>
 * <ul>
 *   <li>{@link Element} – identifies a model element by its ID</li>
 *   <li>{@link Annotation} – identifies an annotation by name and element ID</li>
 *   <li>{@link ModelItself} – identifies the model-details section</li>
 *   <li>{@link ModelReference} – identifies a model reference</li>
 *   <li>{@link Typedef} – identifies a type definition by its ID</li>
 *   <li>{@link DocumentUniquenessCriterion} – identifies a document uniqueness criterion by its name</li>
 * </ul>
 *
 * <p>Static factory methods on this interface provide a convenient single entry-point for
 * creating any of the concrete coordinate types.</p>
 */
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type"
)
public sealed interface LocalCoordinates
		permits LocalCoordinates.Element, LocalCoordinates.Annotation, LocalCoordinates.ModelReference, LocalCoordinates.Typedef,
		LocalCoordinates.ModelItself, LocalCoordinates.DocumentUniquenessCriterion {

	// (the root type LocalCoordinates does not define any instance methods)

	/**
	 * Identifies an element (field, group, rule, etc.) by its ID.
	 */
	@Value(staticConstructor = "of") @Accessors(fluent = true)
	class Element implements LocalCoordinates {
		/**
		 * The element ID; never {@code null}.
		 */
		@NonNull String id;
	}

	/**
	 * {@link LocalCoordinates} that identify an annotation within a local model by the combination
	 * of its name and the ID of the element it is attached to.
	 *
	 * <p>Model-level (header) annotations use the sentinel string {@link #MODEL_ANNOTATION} as the
	 * annotated-element ID instead of a real element ID.</p>
	 */
	@Value(staticConstructor = "of") @Accessors(fluent = true)
	class Annotation implements LocalCoordinates {
		/**
		 * Sentinel value used as the annotated-element ID for model-level (header) annotations that
		 * are not attached to any specific element.
		 */
		public static final String MODEL_ANNOTATION = "__model_annotation";

		/**
		 * The name of the annotation; never {@code null}.
		 */
		@NonNull String annotationName;

		/**
		 * The ID of the element this annotation is attached to, or {@link #MODEL_ANNOTATION}
		 * for model-level annotations; never {@code null}.
		 */
		@NonNull String annotatedElementId;

		/**
		 * Returns coordinates for a model-level annotation identified by its name. The annotated
		 * element ID is set to {@link #MODEL_ANNOTATION}.
		 *
		 * @param annotationName
		 * 		the name of the annotation; must not be {@code null}
		 * @return a {@code Annotation} for the given model-level annotation
		 */
		public static @NonNull LocalCoordinates.Annotation model(@NonNull String annotationName) {
			return Annotation.of(annotationName, MODEL_ANNOTATION);
		}
	}

	/**
	 * {@link LocalCoordinates} that identify a model reference within a model.
	 * A model reference points to another model that the used model depends on.
	 */
	@Value(staticConstructor = "of") @Accessors(fluent = true)
	class ModelReference implements LocalCoordinates {
		/**
		 * The model reference these coordinates identify; never {@code null}.
		 */
		@NonNull com.mgmtp.a12.model.header.ModelReference modelReference;
	}

	/**
	 * {@link LocalCoordinates} that identify a type definition by its ID.
	 */
	@Value(staticConstructor = "of") @Accessors(fluent = true)
	class Typedef implements LocalCoordinates {
		/**
		 * The type definition ID; never {@code null}.
		 */
		@NonNull String typeDefId;
	}

	/**
	 * {@link LocalCoordinates} that identify the model-details section of a local document model.
	 * There is at most one such section per model, so this coordinate type carries no additional
	 * data beyond its type identity.
	 */
	@Value(staticConstructor = "of") @Accessors(fluent = true)
	class ModelItself implements LocalCoordinates {
	}

	/**
	 * {@link LocalCoordinates} that identify a {@link com.mgmtp.a12.kernel.md.model.api.IDocumentUniquenessCriterion} by its name.
	 * Uniqueness criteria are top-level dm content items, not attached to any element.
	 *
	 * <p>Note: coordinates are keyed by {@link #name()} only. If two sources contribute a
	 * criterion with the same name (a model-validity error caught by {@code consistencyCheck}), the
	 * resulting {@code DocumentModel.content.documentUniquenessCriteria} list contains both entries
	 * verbatim, but the {@link ExpansionInfo} maps both to a single {@link ExpansionNode} with one
	 * edge per contributing source. In this case the separate origins cannot be distinguished from this
	 * representation.</p>
	 */
	@Value(staticConstructor = "of") @Accessors(fluent = true)
	class DocumentUniquenessCriterion implements LocalCoordinates {
		/**
		 * the criterion's name as in {@link IDocumentUniquenessCriterion#getName()}
		 */
		@NonNull String name;
	}
}
