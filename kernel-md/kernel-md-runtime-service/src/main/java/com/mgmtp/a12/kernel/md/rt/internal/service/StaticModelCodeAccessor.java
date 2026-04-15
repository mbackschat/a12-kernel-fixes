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

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.BaseCodeGenUtils;
import com.mgmtp.a12.kernel.core.rt.a12internal.IGeneratedCodeAccessFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.meta.IMetaModel;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICalculator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;
import com.mgmtp.a12.kernel.md.model.internal.service.codegen.CodeGeneratorParameters;
import com.mgmtp.a12.kernel.md.rt.api.DocumentValidationException;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentStaticServiceConfig;
import com.mgmtp.a12.kernel.md.rt.api.IStaticModelCode;

/**
 * Holds references to instances of the necessary access classes for using the code statically generated from Document Models.
 */
class StaticModelCodeAccessor implements IStaticModelCode {

	static class Builder {

		private Builder() {
		}

		StaticModelCodeAccessor build(String docModelId, IDocumentStaticServiceConfig config) {
			String packagePath = getPackagePath(docModelId, config);

			IGeneratedCodeAccessFactory codeAccessFactory = getInstanceFromGencode(docModelId, config, packagePath, "Factory_");
			IValidator validator = codeAccessFactory.getValidator();
			ICalculator calculator = codeAccessFactory.getCalculator();
			IMetaModel metaModel = codeAccessFactory.getMetaObject();

			return new StaticModelCodeAccessor(validator, calculator, metaModel);
		}

		/**
		 * Compare {@link CodeGeneratorParameters#getNondefaultPackagePrefix()};
		 *
		 * @param docModelId
		 * 		the DM to check
		 * @param config
		 * 		to load the package info
		 * @return the package name of the DM
		 */
		private String getPackagePath(String docModelId, IDocumentStaticServiceConfig config) {
			Optional<String> packagePath;
			if (config.getModelPackage(docModelId).isPresent()) {
				packagePath = config.getModelPackage(docModelId);
			} else {
				packagePath = Optional.empty();
			}
			return BaseCodeGenUtils.getNormalizedPackageNameOrDefault(packagePath.orElse(null));
		}

		private <T> T getInstanceFromGencode(String docModelId, IDocumentStaticServiceConfig config, String packagePath,
				String classnamePrefix) {
			final String clazzname = packagePath + classnamePrefix + docModelId;
			final Class<?> clazz;
			try {
				Optional<ClassLoader> classLoaderOpt = config.getModelCodeClassLoader(docModelId);
				if (classLoaderOpt.isPresent()) {
					// statically generated code is loaded from a specific classloader
					ClassLoader cl = classLoaderOpt.get();
					clazz = cl.loadClass(clazzname);
				} else {
					// statically generated code is available in current classloader
					clazz = Class.forName(clazzname);
				}
			} catch (ClassNotFoundException e) {
				throw new DocumentValidationException("No generated code found for document model id '" + docModelId + "'.", e);
			}
			try {
				@SuppressWarnings("unchecked") final T instance = (T) clazz.getMethod("getInstance").invoke(null);
				return instance;
			} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				throw new DocumentValidationException("Error creating model instance.", e);
			}
		}
	}

	static Builder builder() {
		return new Builder();
	}

	private final IValidator validator;
	private final ICalculator calculator;
	private final IMetaModel metaModel;

	private StaticModelCodeAccessor(IValidator validator, ICalculator calculator, IMetaModel metaModel) {
		this.validator = validator;
		this.calculator = calculator;
		this.metaModel = metaModel;
	}

	public IValidator validator() {
		return validator;
	}

	public ICalculator calculator() {
		return calculator;
	}

	public IMetaModel metaModel() {
		return metaModel;
	}
}
