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

package com.mgmtp.a12.kernel.core.calc.internal.util;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler.SpecialTokenType;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.formatter.FormatterVisitor;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICalculationInfo;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.ICalculationService;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;

/**
 * Stellt Methoden zur Verfügung, um die Berechnungen zu formatieren.
 */
public class BerechnungsFunktionFormatter {
	private final IEC ec;
	private final IModelSpecificSettings modelSpecificSettings;
	private final ConditionMultiLanguageHandler mlh;

	public BerechnungsFunktionFormatter(IServiceConfiguration config) {
		this(config.getIEC(), config.getModelSpecificSettings(), config.getProjectSpecificSettings());
	}

	public BerechnungsFunktionFormatter(IEC ec, IModelSpecificSettings modelSpecificSettings, IProjectSpecificSettings pss) {
		this.ec = ec;
		this.modelSpecificSettings = modelSpecificSettings;
		this.mlh = new ConditionMultiLanguageHandler(modelSpecificSettings.getLanguageUsedInConditionText(), pss);
	}

	/**
	 * Gibt den Text zur Ausgabe einer formatierten BerechnungsFunktion zurück
	 *
	 * @param info
	 * @param width
	 * 		Breite für den Formatter
	 * @return formatierten Text
	 */
	public String formatiereBerechnungsFunktion(ICalculationInfo info, int width) {
		if (info.getCommonPrecondition().isPresent()) {
			return formatiereVorbedingung(info.getCommonPrecondition().get(), width, info.getName()) + " {\n" +
					formatiereBerechnungsAlternativen(info, width - 5, 1) +
					"\n}";
		} else {
			return formatiereBerechnungsAlternativen(info, width, 0);
		}
	}

	private String formatiereBerechnungsAlternativen(ICalculationInfo info, int width, int indentLevel) {
		return info.getPartialCalculations().stream()
				.map(p -> formatiereTeilberechnung(p, info, width, indentLevel))
				.collect(Collectors.joining("\n"));
	}

	private String formatiereTeilberechnung(Pair<ICondition, IOperation> teilBerechnung, ICalculationInfo info, int width,
			int indentLevel) {
		if (teilBerechnung.getLeft() == null) {
			// Ohne Vorbedingung -> Nur die Berechnungsfunktion ausgeben
			return formatiereBerechnungsFunktion(info.getCalculatedField(), info.getRoundingMode(), teilBerechnung.getRight(),
					width, info.getName(), indentLevel);
		} else {
			return indent(indentLevel) + formatiereVorbedingung(teilBerechnung.getLeft(), width, info.getName()) + " {\n"
					+ formatiereBerechnungsFunktion(info.getCalculatedField(), info.getRoundingMode(), teilBerechnung.getRight(),
					width - 5, info.getName(), indentLevel + 1)
					+ "\n" + indent(indentLevel) + "}";
		}
	}

	private String indent(int indentLevel) {
		return StringUtils.repeat('\t', indentLevel);
	}

	private String formatiereVorbedingung(ICondition b, int width, String name) {
		return mlh.getTerminalName(SpecialTokenType.IF) + "(" + stripLineBreaks(
				FormatterVisitor.getFormattedOutput(b, width, 1, true, ec, modelSpecificSettings, name, mlh)) + ")";
	}

	private String formatiereBerechnungsFunktion(IEntityId berechnetesFeld, ICalculationService.RoundingMode rundungsArt,
			IOperation berFunktion, int width, String name, int indentLevel) {
		StringBuilder result = new StringBuilder();
		result.append(indent(indentLevel)).append("[")
				.append(stripLineBreaks(FormatterVisitor.getFormattedOutput(berechnetesFeld, ec, modelSpecificSettings, name, mlh)))
				.append("] := ");
		String berechnungsFkt = stripLineBreaks(
				FormatterVisitor.getFormattedOutput(berFunktion, width, 2, false, ec, modelSpecificSettings, name, mlh));
		switch (rundungsArt) {
			case EXACT:
				result.append(berechnungsFkt);
				break;
			case ROUND_DOWN:
				result.append(mlh.getTerminalName(SpecialTokenType.ROUND_DOWN) + "(" + berechnungsFkt + ")");
				break;
			case ROUND_UP:
				result.append(mlh.getTerminalName(SpecialTokenType.ROUND_UP) + "(" + berechnungsFkt + ")");
				break;
		}
		return result.toString();
	}

	private static String stripLineBreaks(String line) {
		if (line.endsWith("\n")) {
			return line.substring(0, line.length() - 1);
		} else {
			return line;
		}
	}
}
