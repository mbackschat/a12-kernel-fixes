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
package com.mgmtp.a12.kernel.md.model.internal.service.utils;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelRoot;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateFragmentType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateRangeType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IDateType;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.ComputationWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.ElementWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.FieldWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.GroupWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.RuleWrapper;

import lombok.NonNull;

public class ElementUtils {

	public static final String A12_ATTACHMENT = "attachment";
	private static final String EXPANSION_NEEDED = "The document model does not seem to be expanded yet.";

	private ElementUtils() {
		throw new UnsupportedOperationException("This may not be instantiated.");
	}

	public static final String PATH_SEPARATOR = "/";

	public static @NonNull String getPath(@NonNull Element element) {
		return getPath(new ElementWrapper(element));
	}

	/**
	 * <b>NOTE:</b> Must be called after all root groups of the document model have been added to the invisible root group!
	 *
	 * @return the full name of the given element
	 */
	public static @NonNull String getPath(@NonNull IElement element) {
		Deque<String> pathParts = new LinkedList<>();
		forEachAncestorAndSelf(element, selfOrAncestor -> pathParts.addFirst(selfOrAncestor.getName()));
		return createPath(pathParts);
	}

	public static String createPath(@NonNull List<? extends IElement> path) {
		return createPath(path.stream().map(IElement::getName).toList());
	}

	public static String getPathWithRepeatabilities(@NonNull IElement element) {
		return createPathWithRepeatabilities(getAncestorsAndSelf(element));
	}

	public static String createPathWithRepeatabilities(@NonNull List<? extends IElement> path) {
		return path.stream()
				.map(fieldOrGroup -> fieldOrGroup.getName() +
						(fieldOrGroup instanceof IGroup group && group.getRepeatability() > 1
								? "[" + group.getRepeatability() + "]"
								: ""))
				.collect(Collectors.joining(PATH_SEPARATOR, PATH_SEPARATOR, ""));
	}

	/**
	 * Pass all ancestors of {@code element} and {@code element} itself (but not the invisible model root), to the provided action,
	 * ordered from leaf to root.
	 */
	private static void forEachAncestorAndSelf(@NonNull IElement element, Consumer<? super IElement> action) {
		IGroup parent = element.getParent();
		// NOTE: in the DocumentModel, all modeler-visible "root groups" are children of an invisible group "RootGroup"
		// -> we must omit this last parent and stop one step before the root, in order to get the modeler-visible path
		if (parent == null) {
			assert element instanceof ElementWrapper wrapper
					&& wrapper.getElement() instanceof DocumentModelRoot;
			return;
		}
		action.accept(element);
		while (parent.getParent() != null) {
			action.accept(parent);
			parent = parent.getParent();
		}
		assert parent instanceof ElementWrapper wrapper
				&& wrapper.getElement() instanceof DocumentModelRoot;
	}

	/**
	 * Pass all ancestors of {@code element} itself (but not itself or the invisible model root), to the provided action, ordered
	 * from leaf to root.
	 */
	private static void forEachAncestor(@NonNull IElement element, Consumer<? super IGroup> action) {
		if (element.getParent() != null) {
			forEachAncestorAndSelf(element.getParent(), group -> action.accept((IGroup) group));
		}
	}

	public static String createPath(SequencedCollection<String> pathParts) {
		return pathParts.stream().collect(Collectors.joining(PATH_SEPARATOR, PATH_SEPARATOR, ""));
	}

	public static String resolveRelativePath(Element element, String referencedElementRelativePath) {
		return resolveRelativePath(new ElementWrapper(element), referencedElementRelativePath);
	}

	public static String resolveRelativePath(IElement element, String referencedElementRelativePath) {
		return ElementPathUtils.absPath(getPath(element), referencedElementRelativePath);
	}

	/**
	 * @return all ancestors of {@code element} and the element itself (but not the invisible model root), ordered from root to leaf.
	 */
	public static List<IElement> getAncestorsAndSelf(@NonNull IElement element) {
		LinkedList<IElement> withAncestors = new LinkedList<>();
		forEachAncestorAndSelf(element, withAncestors::addFirst);
		return withAncestors;
	}

	/**
	 * @return all ancestors of {@code element} (but not the element itself or the invisible model root), ordered from root to leaf.
	 */
	public static List<IGroup> getAncestors(@NonNull IElement element) {
		LinkedList<IGroup> ancestors = new LinkedList<>();
		forEachAncestor(element, ancestors::addFirst);
		return ancestors;
	}

	public static String getDateFormatOrThrow(IElement element) {
		Optional<IFieldType> fieldType = ((IField) element).getEffectiveType();
		if (fieldType.isPresent()) {
			return switch (fieldType.get()) {
				case IDateFragmentType dateFragmentType -> dateFragmentType.getFormatOfFragment();
				case IDateRangeType dateRangeType -> dateRangeType.getFormat();
				case IDateType dateType -> dateType.getFormat();
				default -> throw new IllegalArgumentException(
						String.format("Field '%s' (of type %s) does not have a date format", ElementUtils.getPath(element),
								fieldType.get()));
			};
		} else {
			throw new IllegalArgumentException(EXPANSION_NEEDED);
		}
	}

	public static String getDateRangeSeparator(IElement element) {
		Optional<IFieldType> fieldType = ((IField) element).getEffectiveType();
		if (fieldType.isPresent() && fieldType.get() instanceof IDateRangeType dateRangeType) {
			return dateRangeType.getRangeSeparator();
		}

		return null;
	}

	/**
	 * @return {@code true} if the given element is a {@link IGroup} and it is repeatable by itself
	 * 		(this means the ancestor groups are not considered). Otherwise {@code false}.
	 */
	public static boolean isRepeatableByItself(IElement element) {
		if (element instanceof IGroup group) {
			return group.getRepeatability() > 1;
		}
		return false;
	}

	/**
	 * @return {@code true} if the element itself is repeatable or any of its ancestor groups.
	 * 		Otherwise {@code false}.
	 */
	public static boolean isRepeatable(IElement element) {
		if (isRepeatableByItself(element)) {
			return true;
		}
		IGroup parent = element.getParent();
		while (parent != null) {
			if (parent.getRepeatability() > 1) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}

	public static boolean isEntity(IElement element) {
		return isGroup(element) || isField(element);
	}

	public static boolean isGroup(IElement element) {
		return element instanceof IGroup;
	}

	public static boolean isField(IElement element) {
		return element instanceof IField;
	}

	public static List<IElement> convert(List<? extends Element> elements) {
		return elements.stream()
				.map(ElementUtils::convert)
				.toList();
	}

	public static IElement convert(Element element) {
		return switch (element) {
			case Group group -> new GroupWrapper(group);
			case Field field -> new FieldWrapper(field);
			case Rule rule -> new RuleWrapper(rule);
			case Computation computation -> new ComputationWrapper(computation);
			case null, default -> throw new IllegalArgumentException(
					String.format("Type '%s' of element '%s' is not supported.", element.getClass().getName(), element.getName()));
		};
	}

	public static Group convert(IGroup group) {
		return (Group) ((GroupWrapper) group).getElement();
	}
}
