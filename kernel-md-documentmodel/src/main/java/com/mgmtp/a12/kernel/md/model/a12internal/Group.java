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

package com.mgmtp.a12.kernel.md.model.a12internal;

import static com.mgmtp.a12.kernel.md.model.api.IGroup.IIncludeConfig.IncludeLevel.SINGLE_RG;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.IGroup.ISortField.Order;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.model.header.Annotation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * A {@link Group} is a structural {@link Element} that serves as a container for other
 * {@link Element}s.
 * It can be repeatable which means a document instance of this {@link DocumentModel} can contain
 * multiple instances of this {@link Group}.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Group extends Element implements IEntity, ILabeled {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	/**
	 * Definition of a sorting way (at runtime) of the data.
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class SortField implements Serializable {

		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		/**
		 * @return the reference to the sort field. The reference ({@link IDocumentModelObjectReference#getRelativePath()}) is
		 * 		relative to the group holding the sort field. Never {@code null}.
		 * @param field
		 * 		the new reference to the sort field. The reference ({@link IDocumentModelObjectReference#getRelativePath()}) is
		 * 		relative to the group holding the sort field. May not be {@code null}.
		 */
		private @NonNull IDocumentModelObjectReference<Field> field;
		/**
		 * @return the {@link Order} in which the data will be sorted. Is never {@code null}.
		 * @param order
		 * 		the new {@link Order} in which the data should be sorted. May not be
		 *        {@code null}.
		 */
		private @NonNull Order order;

	}

	/**
	 * Information about the definition of an include.
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class IncludeConfig implements Serializable {

		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		/**
		 * @return the id of the referenced document model which is included. May not be {@code null}.
		 * @param modelReference
		 * 		the new model reference which should be included. May not be
		 *        {@code null}.
		 */
		private @NonNull String reference;
		/**
		 * see {@link IGroup.IIncludeConfig.IncludeLevel}
		 */
		@Builder.Default
		private @NonNull IGroup.IIncludeConfig.IncludeLevel includeLevel = SINGLE_RG;
		/**
		 * @return {@code true} if the rules in the included document model will be excluded.
		 * 		Otherwise
		 *        {@code false}.
		 * @param excludeRules
		 * 		use {@code true} if the rules in the included document model should be
		 * 		excluded.
		 * 		Otherwise {@code false}. Default value is {@code false}.
		 */
		@Builder.Default
		private boolean excludeRules = false;

		/**
		 * @return {@code true} if the computations in the included document model will be excluded.
		 * 		Otherwise
		 *        {@code false}.
		 * @param excludeComputations
		 * 		use {@code true} if the computations in the included document model should be
		 * 		excluded.
		 * 		Otherwise {@code false}. Default value is {@code false}.
		 */
		@Builder.Default
		private boolean excludeComputations = false;

	}

	/**
	 * @return the label for this group. Is never {@code null}. If no labels were defined, the
	 *        {@link ILocalizedTextMap} is empty.
	 * @param label
	 * 		the new label of this group. May not be {@code null}. Use an empty
	 *        {@link ILocalizedTextMap} to unset.
	 */
	private @NonNull ILocalizedTextMap label = new LocalizedTextMapBuilder().build();
	/**
	 * @return the repeatability of this group.
	 * @param repeatability
	 * 		the new repeatability of this group. Must be greater than {@code 0}. Default is
	 *        {@code 1}.
	 */
	private int repeatability = 1;
	/**
	 * @param indexField
	 * 		the new referenced index {@link Field} for this group. May be {@code null} to
	 * 		unset.
	 */
	private IDocumentModelObjectReference<Field> indexField;
	/**
	 * @param usageType
	 * 		the new usage type of this group (for more information see
	 *        {@link #getUsageType()}). May be {@code null} to unset.
	 */
	private String usageType;
	/**
	 * <p><strong>Note:</strong> Sort fields are used only in the mapping scenario
	 * and are only relevant for the source model(s). They define the order of the
	 * repetitions of a group. This sorting takes place <em>before</em> mapping.
	 * For example, if only the first three entries of a repeatable group are to be
	 * included in the mapping, the sort order is critical, because it defines which
	 * repetitions will be mapped.
	 *
	 * @return a list with the sort configuration. The order of the list reflects the priority in
	 * 		which the data will be sorted. Is never {@code null}. If no sort configuration was
	 * 		defined, the list is empty.
	 * @param sortFields
	 * 		a list with the new sort configuration (for more information see
	 *        {@link #getSortFields()}). May not be {@code null}. Use an empty list to
	 * 		unset.
	 */
	private @NonNull List<SortField> sortFields = new ArrayList<>();
	/**
	 * @return a list with the child {@link Element}s in this groups. Is never {@code null}. If the
	 * 		group does not contain any child {@link Element}, the list is empty.
	 * @param elements
	 * 		May not be {@code null}.
	 * 		<p>
	 * 		<b>Important notes:</b>
	 * 		<ul>
	 * 		<li>This method does not set the parent-relationship between the new {@link Element}s and {@code this} {@link Group}:
	 * 		if you want to set the new {@link Element}s through this method, also think of calling {@link Group#setParent(Group)}.
	 * 		Otherwise use {@link Group#addElement(Element)}.</li>
	 * 		<li>Other use case of this method is to set another implementation of {@link List}, internally an {@link ArrayList} is
	 * 		used.</li>
	 * 		</ul>
	 */
	private @NonNull List<Element> elements = new ArrayList<>();
	/**
	 * See also {@link IGroup#getIncludeConfig()}.
	 *
	 * @param includeConfig
	 * 		the new information about the included document model. May be
	 *        {@code null} to
	 * 		unset.
	 */
	private IncludeConfig includeConfig;

	@Builder(builderClassName = "GroupBuilder") Group(@NonNull String id, @NonNull String name,
			@NonNull ILocalizedTextMap internalDescription, @NonNull ILocalizedTextMap externalDescription,
			@NonNull List<Annotation> annotations,
			@NonNull ILocalizedTextMap label, int repeatability, IDocumentModelObjectReference<Field> indexField,
			String usageType, @NonNull List<SortField> sortFields, IncludeConfig includeConfig, String changeNote) {
		super(null, id, name, internalDescription, externalDescription, annotations, changeNote);
		this.label = label;
		this.repeatability = repeatability;
		this.indexField = indexField;
		this.usageType = usageType;
		this.sortFields = sortFields;
		this.includeConfig = includeConfig;
	}

	/**
	 * @return the reference to the index field or {@link Optional#empty()} if no index field is specified. The reference
	 * 		({@link IDocumentModelObjectReference#getRelativePath()}) is relative to this group.
	 */
	public Optional<IDocumentModelObjectReference<Field>> getIndexField() {
		return Optional.ofNullable(indexField);
	}

	/**
	 * @return the usage type of this group. Currently, the following types are known:
	 * 		<ul>
	 * 		<li>attachment</li>
	 * 		<li>multi-select</li>
	 * 		</ul>
	 */
	public Optional<String> getUsageType() {
		return Optional.ofNullable(usageType);
	}

	/**
	 * @return the information about the included document model, if this group is an include.
	 */
	public Optional<IncludeConfig> getIncludeConfig() {
		return Optional.ofNullable(includeConfig);
	}

	/**
	 * Adds a new element to this {@link Group}. This method also sets the
	 * parent-relationship between the new element and this {@link Group}.
	 *
	 * @param element
	 *        {@link Element} to add. May not be {@code null}.
	 */
	public void addElement(@NonNull Element element) {
		elements.add(element);
		element.setParent(this);
	}

	/**
	 * Adds a new element to this {@link Group} by inserting the element at the specified position
	 * in the {@link Group}'s list of elements. This method also sets the parent-relationship
	 * between the new element and this {@link Group}.
	 *
	 * @param index
	 * 		index at which the specified element is to be inserted
	 * @param element
	 *        {@link Element} to add. May not be {@code null}.
	 */
	public void addElement(int index, @NonNull Element element) {
		elements.add(index, element);
		element.setParent(this);
	}

	/**
	 * Adds a list of new elements to this {@link Group} by inserting the elements at the specified position
	 * in the {@link Group}'s list of elements. This method also sets the parent-relationship
	 * between the new element and this {@link Group}.
	 *
	 * @param index
	 * 		index at which the specified elements are to be inserted
	 * @param elementList
	 * 		List of {@link Element} to add. May not be {@code null}.
	 */
	public void addElements(int index, @NonNull Collection<Element> elementList) {
		elements.addAll(index, elementList);
		elementList.forEach(e -> e.setParent(this));
	}

	/**
	 * Replaces an element in this {@link Group} by a new one at the same position. This method also
	 * removes the parent-relationship between the replaced element and this {@link Group}, and also
	 * sets the parent-relationship between the new element and this {@link Group}.
	 *
	 * @param oldElement
	 *        {@link Element} to be replaced. May not be {@code null}.
	 * @param newElement
	 *        {@link Element} to replace the old element. May not be {@code null}.
	 */
	public void replaceElement(@NonNull Element oldElement, @NonNull Element newElement) {
		oldElement.setParent(null);
		newElement.setParent(this);
		elements.set(elements.indexOf(oldElement), newElement);
	}

	/**
	 * Removes the given element from this {@link Group}. This method also removes the
	 * parent-relationship between the element and this {@link Group}.
	 *
	 * @param element
	 *        {@link Element} to remove. May not be {@code null}.
	 */
	public void removeElement(@NonNull Element element) {
		element.setParent(null);
		elements.remove(element);
	}

	public static class GroupBuilder {
		private ILocalizedTextMap internalDescription = new LocalizedTextMapBuilder().build(); // in order to make GroupBuilder.internalDescription() optional
		private ILocalizedTextMap externalDescription = new LocalizedTextMapBuilder().build(); // in order to make GroupBuilder.externalDescription() optional
		private List<Annotation> annotations = new ArrayList<>(); // in order to make GroupBuilder.annotations() optional
		private int repeatability = 1;
		private ILocalizedTextMap label = new LocalizedTextMapBuilder().build(); // in order to make GroupBuilder.label() optional
		private List<SortField> sortFields = new ArrayList<>(); // in order to make GroupBuilder.sortFields() optional
		private boolean required = false;

		public String getId() {
			return this.id;
		}

		/**
		 * <p><strong>Note:</strong> Sort fields are used only in the mapping scenario
		 * and are only relevant for the source model(s). They define the order of the
		 * repetitions of a group. This sorting takes place <em>before</em> mapping.
		 * For example, if only the first three entries of a repeatable group are to be
		 * included in the mapping, the sort order is critical, because it defines which
		 * repetitions will be mapped.
		 *
		 * @param sortFields
		 * 		a list with the new sort configuration (for more information see
		 *        {@link Group#getSortFields()}). May not be {@code null}. Use an empty list to
		 * 		unset.
		 * @return this builder
		 */
		public GroupBuilder sortFields(@NonNull List<SortField> sortFields) {
			this.sortFields = sortFields;
			return this;
		}
	}

}
