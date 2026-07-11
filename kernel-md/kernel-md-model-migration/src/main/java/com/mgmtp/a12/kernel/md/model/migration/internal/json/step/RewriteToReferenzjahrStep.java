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

package com.mgmtp.a12.kernel.md.model.migration.internal.json.step;

import static com.mgmtp.a12.kernel.md.model.migration.internal.utils.MigrationUtils.MAPPER;
import static com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil.extractBaseYearIfPresent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.Function;

import com.mgmtp.a12.kernel.md.model.api.services.migration.config.ICustomMigrationConfig;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationCache;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationLogger;
import com.mgmtp.a12.kernel.md.model.migration.internal.utils.ModelJsonUtil;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

public final class RewriteToReferenzjahrStep {

	private RewriteToReferenzjahrStep() {
	}

	public static void execute(JsonNode model, MigrationLogger migrationLogger, MigrationCache cache,
			ICustomMigrationConfig customMigConfig) {
		var mc = ModelJsonUtil.extractDmConfig(model);
		String conditionLanguageString = mc instanceof ObjectNode && mc.get("conditionLanguage") instanceof ObjectNode clNode
				? clNode.get("code").asString()
				: null;
		if ("de_DE".equals(conditionLanguageString) && extractBaseYearIfPresent((ObjectNode) model).isPresent()) {
			ModelJsonUtil.extractAllElements(model, "Rule").forEach(rule -> {
				updateRuleDE(rule.get("Rule"));
			});
			ModelJsonUtil.extractAllElements(model, "Computation").forEach(comp -> {
				updateCompDE(comp.get("Computation"));
			});
		}
		// for US locale (default in A12), "BaseYear" is already used both in conditions and errtextparams -> nothing to do
	}

	private static void updateRuleDE(JsonNode rule) {
		updateTextField(rule, "errorCondition",
				s -> callMigratorViaReflectionDE("migrateConditionTextInA12Model", s));
		updateErrorMessagesDE(rule);
	}

	private static void updateCompDE(JsonNode computation) {
		updateTextField(computation, "commonPrecondition",
				p -> callMigratorViaReflectionDE("migrateCompCommonPrecondInA12Model", p));
		computation.get("computationAlternatives").forEach(alt -> {
			updateTextField(alt, "precondition",
					p -> callMigratorViaReflectionDE("migrateCompAltPrecondInA12Model", p));
			updateTextField(alt, "operation",
					op -> callMigratorViaReflectionDE("migrateCompAltOperationInA12Model", op));
		});
		updateErrorMessagesDE(computation);
	}

	private static void updateErrorMessagesDE(JsonNode rule) {
		rule.get("errorMessage").forEach(errTextNode -> {
			// note: we don't care about the error message's locale here: we only care about the locale of the _condition language_,
			// which was already checked in the execute() method and is independent of the user-facing error text locale
			updateTextField(errTextNode, "text", t -> callMigratorViaReflectionDE("migrateErrorTextInA12Model", t));
		});
	}

	private static void updateTextField(JsonNode parentJsonNode, String fieldName, Function<String, String> update) {
		if (parentJsonNode instanceof ObjectNode parentObjectNode && parentObjectNode.get(
				fieldName) instanceof StringNode textNode) {
			String oldText = textNode.stringValue();
			if (oldText != null) {
				String newText = update.apply(oldText);
				parentObjectNode.set(fieldName, MAPPER.valueToTree(newText));
				return;
			}
		}

		assert false : "unexpected node, got: " + parentJsonNode;
	}

	private static String callMigratorViaReflectionDE(String methodName, String stringToMigrate) {
		String migratorFqn = "com.mgmtp.a12.kernel.core.migration.parserbased_v3020.MigrateToReferenzjahr";
		Class<?> clazz = null;
		try {
			clazz = Class.forName(migratorFqn);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Could not find " + migratorFqn + " on the runtime classpath." +
					" If your model needs this special migration, " +
					"please add com.mgmtp.a12.kernel:kernel-migration-referenzjahr version 1.1.0 or later to the classpath" +
					" manually (see A12K-3884 for details)");
		}

		// 2. Get the static method: foo(String, Locale)
		Method m = null;
		try {
			m = clazz.getMethod(methodName, String.class, Locale.class);
			return (String) m.invoke(null, stringToMigrate, Locale.GERMANY);
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
