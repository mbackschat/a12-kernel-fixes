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
package com.mgmtp.a12.kernel.md.model.api;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * A {@link IGroup} is a structural {@link IElement} that serves as a container for other
 * {@link IElement}s.
 * It can be repeatable which means a document instance of this {@link IDocumentModel} can contain
 * multiple instances
 * of this {@link IGroup}.
 */
@OnlyForUsage
public interface IGroup extends IElement, IEntity, Serializable {

	/**
	 * Definition of the sort parameter.
	 */
	interface ISortField extends Serializable {

		public enum Order {
			ASC,
			DESC
		}

		/**
		 * @return the referenced {@link IField}. May not be {@code null}.
		 */
		IDocumentModelObjectReference<IField> getField();

		/**
		 * @return the order. May not be {@code null}.
		 */
		Order getOrder();

	}

	/**
	 * One can define a reference to another {@link IDocumentModel}. This interface models the
	 * information about the definition of that reference.
	 */
	interface IIncludeConfig extends Serializable {
		/**
		 * see {@link #SINGLE_RG} and {@link #MODEL_ROOT} for details.
		 *
		 * @implNote the names of the enum constants currently also serve as the string values in the serialized json
		 * 		representation. If the names are changed, care must be taken to not accidentally change the serialized representation.
		 */
		enum IncludeLevel {
			/**
			 * The subgroups of the single RG of the included DM become the children of the mountpoint group in the expanded DM.<br>
			 * The included DM must have a single non-repeatable root group.<br>
			 * This is the classical include mode and the default vs. {@link #MODEL_ROOT}, which is a new optional include mode.
			 */
			SINGLE_RG,
			/**
			 * The root groups of the included DM become the children of the mountpoint group in the expanded DM.<br>
			 * (The included dm is allowed to have multiple and/or repeatable root groups).<br>
			 * This include mode is sometimes also named a "rootPreserving" (or "preserveRootGroups") include.
			 */
			MODEL_ROOT;
		}

		/**
		 * @return the ID of referenced {@link IDocumentModel}. May not be {@code null}.
		 */
		@NonNull String reference();

		/**
		 * See {@link IncludeLevel}.
		 */
		@NonNull IncludeLevel includeLevel();

		/**
		 * @return {@code true} if the rules of the included document model will be excluded.
		 * 		Otherwise {@code false}.
		 */
		boolean excludeRules();

		/**
		 * @return {@code true} if the computations of the included document model will be excluded.
		 * 		Otherwise {@code false}.
		 */
		boolean excludeComputations();

	}

	/**
	 * @return the repeatability of this {@link IGroup}.
	 */
	int getRepeatability();

	/**
	 * @return the referenced index field or an empty Optional instance if no index field is
	 * 		specified.
	 */
	Optional<IDocumentModelObjectReference<IField>> getIndexField();

	/**
	 * @return the usage type of this group or an empty Optional instance if no usage type is
	 * 		specified.<br>
	 * 		Currently, the following usage types exist:
	 * 		<ul>
	 * 		<li>attachment</li>
	 * 		<li>multi-select</li>
	 * 		<li>complexType</li>
	 * 		</ul>
	 */
	Optional<String> getUsageType();

	/**
	 * @return a list of the specified {@link ISortField}s of the group or an empty list if no
	 *        {@link ISortField} is specified. Does not return {@code null}.
	 */
	@NonNull List<ISortField> getSortFields();

	/**
	 * @return a list of the child {@link IElement}s of this {@link IGroup}. If no children are
	 * 		present, the list is empty. Does not return {@code null}.
	 */
	@NonNull List<IElement> getElements();

	/**
	 * The following requirements must be met for an include:
	 * <ol>
	 * <li>the included model must be a document model (see {@link Header#getModelType()})</li>
	 * <li>for the included document model only a single group may be defined at the first nesting
	 * level (in other words {@link IDocumentModelContent#getDocumentModelRoot()}.getElements() ==
	 * 1)</li>
	 * <li>the single group mentioned in #2 may not be empty</li>
	 * <li>the current group references the group mentioned in #2.<br>
	 * Hint: Both name and repeatability of the group can be changed here. No further
	 * changes to the included document model are supported.</li>
	 * <li>the usage type (see also {@link #getUsageType()}) of this group is
	 * {@code complexType}</li>
	 * </ol>
	 *
	 * @return details of the configuration for an included document model or an empty Optional instance if no include is
	 * 		specified.
	 */
	Optional<IIncludeConfig> getIncludeConfig();
}
