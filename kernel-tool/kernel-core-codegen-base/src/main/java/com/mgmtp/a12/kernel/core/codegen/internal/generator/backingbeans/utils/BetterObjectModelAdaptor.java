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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

/**
 * Extends the default StringTemplate ObjectModelAdaptor to also find methods with the same name as the property to find, in order
 * to support methods in the record style by default.
 *
 * @param <T>
 */
public class BetterObjectModelAdaptor<T> extends ObjectModelAdaptor<T> {
	@Override
	public synchronized Object getProperty(Interpreter interp, ST self, T model, Object property, String propertyName)
			throws STNoSuchPropertyException {
		Objects.requireNonNull(model, "o");

		Class<?> clazz = model.getClass();

		if (property == null) {
			return throwNoSuchProperty(clazz, propertyName, null);
		}

		Member member = findMember(clazz, propertyName);
		if (member != null) {
			try {
				if (member instanceof Method method) {
					return method.invoke(model);
				} else if (member instanceof Field field) {
					return field.get(model);
				}
			} catch (Exception e) {
				throwNoSuchProperty(clazz, propertyName, e);
			}
		}

		return throwNoSuchProperty(clazz, propertyName, null);
	}

	protected static Member findMember(Class<?> clazz, String memberName) {
		Objects.requireNonNull(clazz, "clazz");
		Objects.requireNonNull(memberName, "memberName");

		synchronized (membersCache) {
			Map<String, Member> members = membersCache.computeIfAbsent(clazz, key -> new HashMap<>());
			Member member = members.computeIfAbsent(memberName, key -> lookupMember(clazz, memberName));
			return member != INVALID_MEMBER ? member : null;
		}
	}

	/**
	 * Search for a viable member by trying the following (in this order): <ol>
	 * <li>getMemberName()</li>
	 * <li>isMemberName()</li>
	 * <li>hasMemberName()</li>
	 * <li>memberName()</li>
	 * <li>memberName</li>
	 * </ol>
	 */
	protected static Member lookupMember(Class<?> clazz, String memberName) {
		String methodSuffix = Character.toUpperCase(memberName.charAt(0)) + memberName.substring(1);
		Member member = tryGetMethod(clazz, "get" + methodSuffix);
		if (member != null) {
			return member;
		}
		member = tryGetMethod(clazz, "is" + methodSuffix);
		if (member != null) {
			return member;
		}
		member = tryGetMethod(clazz, "has" + methodSuffix);
		if (member != null) {
			return member;
		}
		member = tryGetMethod(clazz, memberName);
		if (member != null) {
			return member;
		}
		// try for a visible field
		member = tryGetField(clazz, memberName);
		if (member != null) {
			return member;
		}
		return INVALID_MEMBER;
	}
}
