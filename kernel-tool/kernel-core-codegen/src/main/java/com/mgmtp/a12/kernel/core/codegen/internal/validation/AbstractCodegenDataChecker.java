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
package com.mgmtp.a12.kernel.core.codegen.internal.validation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mgmtp.a12.kernel.core.rt.a12internal.ValidationMode;
import com.mgmtp.a12.kernel.core.rt.a12internal.IGeneratedCodeAccessFactory;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.ICustomCondition;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IResult;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IValidator;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.SeverityType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.metaModel.IdentifierImpl;

public abstract class AbstractCodegenDataChecker implements IData {

	/**
	 * when we bootstrap the mm validators, we can't do the validator-based checks of the codegen input.
	 * This flag is set according to environment variable {@code A12_KERNEL_BOOTSTRAP_VALIDATORS_DISABLE_MM_VALIDATION} and
	 * allows to skip those checks (intended only for known-valid inputs, and only for bootstrapping the metamodel-based generated validator code)
	 */
	private static final boolean A12_KERNEL_BOOTSTRAP_VALIDATORS_DISABLE_MM_VALIDATION =
			Boolean.parseBoolean(System.getenv("A12_KERNEL_BOOTSTRAP_VALIDATORS_DISABLE_MM_VALIDATION"));
	private static final int EXPECTED_VALUE_COUNT = 20000;
	private final HashMap<IIdentifier, String> values = new HashMap<>(EXPECTED_VALUE_COUNT);
	protected CheckConfiguration checkConfiguration = new CheckConfiguration();
	protected final IModelSpecificSettings modelSpecificSettings;
	protected final IProblemReporter pr;

	protected AbstractCodegenDataChecker(IProblemReporter pr, IModelSpecificSettings modelSpecificSettings) {
		this.pr = pr;
		this.modelSpecificSettings = modelSpecificSettings;
	}

	public abstract IValidator getValidator();

	protected abstract IResult getProblemReporterValidationResult();

	protected abstract Map<String, ICustomCondition> getCustomConditions();

	@Override
	public String getValue(IIdentifier entity) {
		return values.get(new IdentifierImpl(entity));
	}

	@Override
	public String getValueForDisplay(IIdentifier entity) {
		return getValue(entity);
	}

	@Override
	public Iterator<IIdentifier> iterator() {
		return values.keySet().iterator();
	}

	@Override
	public String identifierToString(IIdentifier field) {
		/*
		 * Actually we don't want to use the error text parameters $<fieldname>$ (or $Field$
		 * respectively) in the meta model. If these parameters are used nevertheless, we simply
		 * return the field name.
		 */
		return field.getName();
	}

	@Override
	public void addValue(IIdentifier field, String value) {
		values.put(new IdentifierImpl(field), value);
	}

	/**
	 * @return true if the validation of the code generation data is successful.
	 * The deprecated {@link IValidator#validateAll(IData, Map, IResult, ValidationMode)} is used here, because the
	 * meta-model validation uses generated code of the previous Kernel version and this version does not provide the
	 * new methods, yet.
	 */
	@SuppressWarnings("removal")
	public boolean check() {
		IResult result = getProblemReporterValidationResult();
		if(!A12_KERNEL_BOOTSTRAP_VALIDATORS_DISABLE_MM_VALIDATION) { // see javadoc of A12_KERNEL_BOOTSTRAP_VALIDATORS_DISABLE_MM_VALIDATION
			IValidator validator = getValidator();
			validator.validateAll(this, getCustomConditions(), result,
					checkConfiguration.getValidationMode(modelSpecificSettings));
		}
		return result.getCount(SeverityType.ERROR) == 0;
	}


	/**
	 * Obtains an IGeneratedCodeAccessFactory instance for the given fqn, assuming that the class has a
	 * suitable getInstance() method.<br/>
	 * Uses reflection with no compile-time dependency to the requested class, in order to enable the
	 * {@link #A12_KERNEL_BOOTSTRAP_VALIDATORS_DISABLE_MM_VALIDATION} mechanism.
	 * @param fqn e.g. {@code com.mgmtp.a12.kernel.mmvalidators.extmpinternal.codegen_mm_common_v2.Factory_codegen_mm_common_v2}
	 */
	protected static IGeneratedCodeAccessFactory getGenCodeAccessFactoryReflection(String fqn) {
		try {
			Class<?> myClass = Class.forName(fqn);
			Method getInstance = myClass.getMethod("getInstance");
			return (IGeneratedCodeAccessFactory) getInstance.invoke(null);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("The requested IGeneratedCodeAccessFactory implementation " + fqn + " was not found. Is a dependency missing on the runtime classpath?");
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
