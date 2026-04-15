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

package com.mgmtp.a12.kernel.core.onTheFlyTesting.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.time.StopWatch;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.core.codegen.internal.CodeGeneratorService;
import com.mgmtp.a12.kernel.core.onTheFlyTesting.a12internal.api.IOnTheFlyTestingCodegenParameters;
import com.mgmtp.a12.kernel.core.onTheFlyTesting.a12internal.api.IOnTheFlyValidationGenerator;
import com.mgmtp.a12.kernel.core.onTheFlyTesting.a12internal.api.IValidationInvoker;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IMVK_Service;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;
import com.mgmtp.a12.model.notification.Severity;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Generator for a validator based on Groovy code for OnTheFly functionality.
 * <p>
 * Can create an {@link IValidationInvoker} for a rule set and
 * {@link IOnTheFlyTestingCodegenParameters}.
 */
public class OnTheFlyTestingValidatorGenerator implements IOnTheFlyValidationGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(OnTheFlyTestingValidatorGenerator.class);

	private final IMVK_Service mvkService;
	private final IOnTheFlyTestingCodegenParameters params;

	public OnTheFlyTestingValidatorGenerator(IMVK_Service mvkService, IOnTheFlyTestingCodegenParameters params) {
		this.mvkService = mvkService;
		this.params = params;
	}

	/**
	 * Generate the Groovy-code for the given rules.
	 *
	 * @param rules
	 * 		the rules to be generated.
	 * @return the groovy code as {@link String} or {@link Optional#empty()} if an error occurs
	 * 		during code generation.
	 */
	Optional<String> generateCode(Collection<? extends IRule> rules) {
		StopWatch swGen = new StopWatch();
		swGen.start();
		ICodeGeneratorParameters codegenParams = CodeGenParams.getBuilder()
				.setLocales(Collections.singletonList(params.getLanguage()))
				.setDecimalSeparator(params.getDecimalSeparator())
				.setRestrictedValidGraphemeSet(params.restrictedValidGraphemeSet())
				.setMetaInfoCodegenParams(params.getMetaInfoCodeGenParams())
				.build();
		// generate groovy code
		byte[] bytes = new CodeGeneratorService(mvkService).getSourceCodeAsZip(rules, codegenParams, params.getProblemReporter());
		if (bytes == null) {
			params.getProblemReporter().reportProblem(new MVKCoreError("Failed to generate code.", Severity.ERROR, null));
			return Optional.empty();
		}
		final String generatedPlainCode = unzipToString(bytes);
		swGen.stop();
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("%s ms in 'checkAll' to generate groovy code", swGen.getDuration().toMillis()));
		}
		return Optional.of(generatedPlainCode);
	}

	private String unzipToString(final byte[] zipContent) {
		final StringBuilder sb = new StringBuilder();
		try (final ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipContent))) {
			char[] buffer = new char[1024];
			int read;
			while (zis.getNextEntry() != null) {
				Reader r = new InputStreamReader(zis, StandardCharsets.UTF_8);
				while ((read = r.read(buffer, 0, 1024)) >= 0) {
					sb.append(new String(buffer, 0, read));
				}
			}
		} catch (final IOException e) {
			throw new IllegalArgumentException("Unable to unpack validation code form ZIP(ed) data.", e);
		}
		return sb.toString();
	}

	/**
	 * Convenience-method combining {@link #generateCode(Collection)} and
	 * {@link #generateValidator(String)}.
	 *
	 * @param rules
	 * 		the rule set, for which the validator should be generated.
	 * @return an {@link IValidationInvoker} for the given rule set.
	 */
	public Optional<IValidationInvoker> generateValidator(Collection<? extends IRule> rules) {
		return generateCode(rules)
				.map(this::generateValidator);
	}

	/**
	 * Compiles the given Groovy code and generates a wrapper class to invoke validation.
	 *
	 * @param generatedCode
	 * 		the code generated by {@link #generateCode(Collection)}.
	 * @return an {@link IValidationInvoker} for the script. If compilation fails, an exception is
	 * 		thrown.
	 */
	IValidationInvoker generateValidator(final String generatedCode) {
		Objects.requireNonNull(generatedCode);
		StopWatch swExecutable = new StopWatch();
		swExecutable.start();

		Binding binding = new Binding();
		CompilerConfiguration cc = new CompilerConfiguration();
		cc.setSourceEncoding(StandardCharsets.UTF_8.name());
		GroovyShell groovyShell = new GroovyShell(binding, cc);
		Script groovyScript = groovyShell.parse(generatedCode);
		swExecutable.stop();
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("%s ms in 'checkAll' to create groovy script", swExecutable.getDuration().toMillis()));
		}
		return new GeneratedCodeInvoker(groovyScript);
	}
}
