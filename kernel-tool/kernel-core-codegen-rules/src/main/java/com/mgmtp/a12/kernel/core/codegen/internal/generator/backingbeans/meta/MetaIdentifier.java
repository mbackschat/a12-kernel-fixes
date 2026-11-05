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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.meta;

import java.util.List;
import java.util.function.Function;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenRuleConstants;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.StringParameter;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.NullTerminal;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * Parameter class for the template of the initialization of
 * com.mgmtp.a12.kernel.core.rt._31_3.internal.formatdef.Identifier_t in the generated code.
 */
@StringTemplateGroup("meta_rules")
public class MetaIdentifier extends STParameterClassBase {

	// Enumeration wird für die Kategorisierung des referenzierten Feldes bzgl
	// der Rolle in einer Berechnung verwendet
	// Für die Codegenerierung ist dies zurzeit nur für JAVA relevant, da nur Java-Generate
	// gemeinsam mit JavaScript Generaten verwendet werden.
	// Code für Berechnungen wird  nur in JavaScript Generaten erzeugt.
	public enum ReferenzTyp {
		OPERAND,
		OPERAND_UEBERTRAG,
		BERECHNETES_FELD,
		BERECHNETES_FELD_UEBERTRAG,
		KEINE_BERECHNUNG
	}

	private ReferenzTyp refTyp = ReferenzTyp.KEINE_BERECHNUNG;
	private long regelNummer;
	private final IEntityId fid;
	private final Integer feldIndex;
	private final ProgrammingLanguage programmingLanguage;
	private final Function<IEntityId, Integer> feldIndexMapper;

	public MetaIdentifier(long regelNummer, Function<IEntityId, Integer> feldIndexMapper, IEntityId fid,
			ProgrammingLanguage programmingLanguage) {
		this.programmingLanguage = programmingLanguage;
		this.regelNummer = regelNummer;
		this.fid = fid;
		this.feldIndexMapper = feldIndexMapper;
		this.feldIndex = feldIndexMapper.apply(fid);
	}

	/**
	 * Setzt den Referenztyp - Methode wird zum Initialisieren des MetaIdentifiers aufgerufen.
	 *
	 * @param berechnetesFeldName
	 * 		- Name des berechneten Feldes
	 * @param operanden
	 * 		- Name der Operanden
	 * @param uebertrag
	 * 		- true, wenn es sich nur um einen Übertrag handelt.
	 */
	public void setReferenzTyp(String berechnetesFeldName, List<String> operanden, boolean uebertrag) {
		if (berechnetesFeldName == null) {
			refTyp = ReferenzTyp.KEINE_BERECHNUNG;
			return;
		}

		if (fid.getFullName().equals(berechnetesFeldName)) {
			refTyp = uebertrag ? ReferenzTyp.BERECHNETES_FELD_UEBERTRAG : ReferenzTyp.BERECHNETES_FELD;
			return;
		}
		for (String opName : operanden) {
			if (fid.getFullName().equals(opName)) {
				refTyp = uebertrag ? ReferenzTyp.OPERAND_UEBERTRAG : ReferenzTyp.OPERAND;
				return;
			}
		}

		// auch wenn die Regel eine Berechnungsregel ist, können auch Felder
		// referenziert sein, die nicht in der Berechnung referenziert sind.
		refTyp = ReferenzTyp.KEINE_BERECHNUNG;
	}

	public String getFeldIndex() {
		return feldIndex.toString();
	}

	public String[] getIndices() {
		long[] indices = fid.getIndices();
		String[] indicesCode = new String[indices.length];
		for (int i = 0; i < indices.length; i++) {
			if (indices[i] == IParseTreeEntity.WILDCARD) {
				indicesCode[i] = getAlleIndicesCode();
			} else {
				if (indices[i] == IParseTreeEntity.ITERATION) {
					indicesCode[i] = getIterationCode();
				} else if (indices[i] == IParseTreeEntity.PARALLEL_ITERATION) {
					indicesCode[i] = getParallelIterationCode();
				} else if (indices[i] == IParseTreeEntity.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED) {
					indicesCode[i] = getRepNotUnqLevelToBeReplacedCode();
				} else {
					indicesCode[i] = String.valueOf(indices[i]);
				}
			}

		}
		return indicesCode;
	}

	public String getUsbCode() {
		long usb = fid.getUsb();
		if (usb == IParseTreeEntity.UNSPECIFIED) {
			return getUSBNichtAngegebenCode();
		}
		if (usb == IParseTreeEntity.WILDCARD) {
			return getAlleIndicesCode();
		}
		return getIterationCode();
	}

	private String getAlleIndicesCode() {
		if (ProgrammingLanguage.CPLUS.equals(this.programmingLanguage)) {
			return "0";
		} else {
			return CodeGenRuleConstants.JAVA_JS_RT_STRING_ALL_INDICES;
		}
	}

	private String getIterationCode() {
		if (ProgrammingLanguage.CPLUS.equals(this.programmingLanguage)) {
			return "-1";
		} else {
			return CodeGenRuleConstants.JAVA_JS_RT_STRING_ITERATION;
		}
	}

	private String getParallelIterationCode() {
		if (ProgrammingLanguage.CPLUS.equals(this.programmingLanguage)) {
			return "-6";
		} else {
			return CodeGenRuleConstants.JAVA_JS_RT_STRING_PARALLEL_ITERATION;
		}
	}

	private String getRepNotUnqLevelToBeReplacedCode() {
		if (ProgrammingLanguage.CPLUS.equals(this.programmingLanguage)) {
			return "-7";
		} else {
			return CodeGenRuleConstants.JAVA_JS_RT_STRING_REP_NOT_UNQ_LEVEL_TO_REPLACE;
		}
	}

	private String getUSBNichtAngegebenCode() {
		if (ProgrammingLanguage.CPLUS.equals(this.programmingLanguage)) {
			return "-2";
		} else {
			return CodeGenRuleConstants.JAVA_JS_RT_STRING_USB_NICHT_ANGEGEBEN;
		}
	}

	public String getReferenzTyp() {
		assert (ProgrammingLanguage.JAVA.equals(this.programmingLanguage) ||
				ProgrammingLanguage.JAVA_SCRIPT_VK.equals(this.programmingLanguage) ||
				ProgrammingLanguage.GROOVY.equals(this.programmingLanguage));
		/*
		 * OPERAND, BERECHNETES_FELD, KEINE_BERECHNUNG
		 */
		return refTyp.toString();

	}

	public long getRegelNummer() {
		return regelNummer;
	}

	public String getSemanticIndexValue() {
		Object semIdx = getSemanticIndexIntern();

		if (semIdx instanceof String) {
			return new StringParameter((String) semIdx).toCode();
		} else {
			return new NullTerminal().toCode("nullString");
		}
	}

	public String getSemanticIndexCode() {
		Object semIdx = getSemanticIndexIntern();

		if (semIdx instanceof IEntityId) {
			return "\n\t" + new MetaIdentifier(regelNummer, feldIndexMapper, (IEntityId) semIdx, programmingLanguage).toCode();
		} else {
			return new NullTerminal().toCode("nullptr");
		}
	}

	private Object getSemanticIndexIntern() {
		if (fid.getSemanticIndex().isEmpty()) {
			return null;
		}
		if (fid.getSemanticIndex().size() != 1) {
			throw new IllegalStateException("Multiple semantic indexes not allowed at the moment");
		}
		return fid.getSemanticIndex().get(0);
	}

	public String getFullName() {
		return fid.getFullName();
	}
}
