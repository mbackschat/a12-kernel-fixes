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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mgmtp.a12.kernel.md.model.a12internal.expansioninfo.ExpansionInfo;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Describes the type of expansion step that was used to bring an element from a source model
 * into the expanded DM one level above the source model.
 * <br>
 * e.g. if the field with id "f_abc123" came via the dm include at group "g_456def", the corresponding edge in the expansion-origin
 * graph will be labeled with {@code ExpansionEdgeLabel.Dm.Include.of("g_456def")}
 */
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type"
)
public sealed interface ExpansionEdgeLabel permits ExpansionEdgeLabel.Cm, ExpansionEdgeLabel.Dm, ExpansionEdgeLabel.JoinInternal {

	// the root type ExpansionEdgeLabel itself does not define instance methods itself

	sealed interface Cm extends ExpansionEdgeLabel permits Cm.CmItself, Cm.Base, Cm.Addition, Cm.Selection, Cm.Decoration {
		/**
		 * Points to the (unexpanded) CM. E.g. for modelRefs or annotations that originate from it.
		 */
		@Value(staticConstructor = "of") @Accessors(fluent = true)
		class CmItself implements Cm {
		}

		/**
		 * Points to the baseModel of a CM.
		 */
		@Value(staticConstructor = "of") @Accessors(fluent = true)
		class Base implements Cm {
		}

		/**
		 * Points to an additive model from a particular step of a CM.
		 */
		@Value(staticConstructor = "of") @Accessors(fluent = true)
		class Addition implements Cm {
			/**
			 * The step index, zero-based.
			 */
			int stepNumber;
		}

		/**
		 * Points to a selection model from a particular step of a CM.
		 */
		@Value(staticConstructor = "of") @Accessors(fluent = true)
		class Selection implements Cm {
			/**
			 * The step index, zero-based.
			 */
			int stepNumber;
		}

		/**
		 * Points to a decoration model from a particular step of a CM.
		 */
		@Value(staticConstructor = "of") @Accessors(fluent = true)
		class Decoration implements Cm {
			/**
			 * special value used as the decorated-element ID for the origin of model-level information (like typeDefs and
			 * modelRefs) where we don't record the details of all decorated elements that may have triggered their inclusion
			 * in the result model (because the number of _decorated_ elements can typically be very large, and all of them would
			 * otherwise redundantly be included in the origin-graph of the model-level information).
			 */
			public static final String MODEL_DECORATION_CONNECTION = "__model_decoration_connection";

			/**
			 * The step index, zero-based.
			 */
			int stepNumber;

			/**
			 * The ID of the element being decorated, or {@link #MODEL_DECORATION_CONNECTION}
			 * for model-level decorations; never {@code null}.
			 */
			@NonNull String decoratedElementId;

			/**
			 * @return an instance with {@link #MODEL_DECORATION_CONNECTION} set instead of the {@link #decoratedElementId}, for
			 * model-level information where no individual element IDs are traced.
			 */
			public static @NonNull ExpansionEdgeLabel.Cm.Decoration modelInfo(int stepNumber) {
				return Decoration.of(stepNumber, Decoration.MODEL_DECORATION_CONNECTION);
			}
		}
	}

	sealed interface Dm extends ExpansionEdgeLabel permits Dm.Base, Dm.Import, Dm.Include {

		/**
		 * Points to the (unexpanded) DM itself (i.e. not to an included or imported DM)
		 */
		@Value(staticConstructor = "of") @Accessors(fluent = true)
		class Base implements Dm {
		}

		/**
		 * An {@link ExpansionEdgeLabel} that marks a type definition as coming from an import during
		 * expansion. Import expansions bring type definitions from an external type definition model into the current model.
		 */
		@Value(staticConstructor = "of") @Accessors(fluent = true)
		class Import implements Dm {
		}

		/**
		 * An {@link ExpansionEdgeLabel} that marks an element as coming from an include during expansion,
		 * identified by its include group ID. Include expansions bring elements from an external model
		 * into the current model under a named include group.
		 */
		@Value(staticConstructor = "of") @Accessors(fluent = true)
		class Include implements Dm {
			/**
			 * The ID of the include group through which the element was brought in; never {@code null}.
			 */
			@NonNull String includeGroupId;
		}
	}

	/**
	 * An internal-only expansion connection used during join processing to temporarily link
	 * elements to their reference models. These connections are removed before the final
	 * {@link ExpansionInfo} is returned.
	 */
	@Value(staticConstructor = "of") @Accessors(fluent = true)
	class JoinInternal implements ExpansionEdgeLabel {
		/**
		 * Removes all {@link JoinInternal} edges from the given {@link ExpansionInfo}, flattening
		 * them by replacing each internal edge with the previous origins of its target node.
		 *
		 * @param expansionInfo
		 * 		the expansion info to clean up
		 * @return a new {@link ExpansionInfo} without any {@link JoinInternal} edges
		 */
		public static ExpansionInfo removeInternalConnections(@NonNull ExpansionInfo expansionInfo) {
			return ExpansionInfo.of(expansionInfo.localModelId(),
					expansionInfo.allTracedObjects().stream().collect(Collectors.toMap(
							coords -> coords,
							coords -> removeInternalConnections(expansionInfo.getOrigin(coords))
					))
			);
		}

		private static ExpansionNode removeInternalConnections(@NonNull ExpansionNode expansionNode) {
			Set<ExpansionEdge> previousOrigins = new HashSet<>(expansionNode.previousOrigins());

			Set<ExpansionEdge> flattenedOrigins = previousOrigins.stream()
					.flatMap(JoinInternal::removeInternalConnections)
					.collect(Collectors.toSet());

			return expansionNode.withPreviousOrigins(flattenedOrigins);
		}

		private static Stream<ExpansionEdge> removeInternalConnections(
				@NonNull ExpansionEdge connectionAndNestedOrigin) {
			if (connectionAndNestedOrigin.label() instanceof JoinInternal) {
				return connectionAndNestedOrigin.previousNode().previousOrigins().stream().flatMap(
						JoinInternal::removeInternalConnections);
			} else {
				// we do not need to look deeper as the 'Join'-stack is layered based on a single entry point
				return Stream.of(connectionAndNestedOrigin);
			}
		}
	}
}
