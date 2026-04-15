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

package com.mgmtp.a12.kernel.md.structuralmapping.codegen.internal;

import java.util.function.BiFunction;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BetterObjectModelAdaptor;

public class PluggableModelAdaptor<T> extends BetterObjectModelAdaptor<T> {
	private final BiFunction<T, String, Object> getPropertyFunc;

	private PluggableModelAdaptor(BiFunction<T, String, Object> getPropertyFunc) {
		this.getPropertyFunc = getPropertyFunc;
	}

	public static <T> PluggableModelAdaptor<T> create(BiFunction<T, String, Object> getPropertyFunc) {
		return new PluggableModelAdaptor<>(getPropertyFunc);
	}

	@Override
	public synchronized Object getProperty(Interpreter interp, ST self, T model, Object property, String propertyName)
			throws STNoSuchPropertyException {
		Object result = getPropertyFunc.apply(model, propertyName);
		return result == null
				? super.getProperty(interp, self, model, property, propertyName)
				: result;
	}
}
