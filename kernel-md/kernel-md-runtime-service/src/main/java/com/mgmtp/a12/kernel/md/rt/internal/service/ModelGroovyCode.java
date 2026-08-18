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
package com.mgmtp.a12.kernel.md.rt.internal.service;

import java.io.IOException;

import com.mgmtp.a12.kernel.md.rt.api.IModelCode;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

/**
 * Models the cached generated groovy code.
 */
public class ModelGroovyCode implements IModelCode {
	static {
		// For groovy version 3 there is a compilation issue for each new generated code, caused by the newly introduced Parrot parser, which is disabled with setting groovy.antlr4 to false.
		// In future groovy versions we need to check, if we still need to disable this parser, so that the groovy compilation does not slow down.
		System.setProperty("groovy.antlr4", "false");
	}

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	private final String modelHash;
	private final String generatedPlainCode;
	private transient GroovyObject modelCode;

	/**
	 * The generated code is compiled in a lazy mode.
	 *
	 * @param modelHash
	 * 		hash code of the document model. This is used to determine if the generated code should be replaced, e.g.
	 * 		in case the document model was changed.
	 * @param generatedPlainCode
	 * 		generated groovy code from the model as string.
	 */
	public ModelGroovyCode(final String modelHash, final String generatedPlainCode) {
		this.modelHash = modelHash;
		this.generatedPlainCode = generatedPlainCode;
	}

	protected synchronized GroovyObject modelCode() {
		if (this.modelCode == null) {
			// we create a new ClassLoader per model to enable the deletion of corresponding class definitions form metaspace
			// after references to the model and its generated groovy scrips / classes are removed form caches
			try (GroovyClassLoader classLoader = new GroovyClassLoader(ModelGroovyCode.class.getClassLoader())) {
				Class<?> groovy = classLoader.parseClass(generatedPlainCode);
				this.modelCode = (GroovyObject) groovy.getDeclaredConstructor().newInstance();
			} catch (IOException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					 | IllegalArgumentException | java.lang.reflect.InvocationTargetException e) {
				throw new RuntimeException("Failed to instantiate class from groovy text.", e);
			}
		}
		return this.modelCode;
	}

	@Override
	public String getModelHash() {
		return modelHash;
	}
}
