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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenEntityData;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenDateRangeTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenDateTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenEnumerationTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenFeldTypBasis;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenFeldTypBuilder;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenNumberTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenPredefinedTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.formatdefinition.CodeGenStringTypeImpl;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.codegen.internal.utils.StringUtil;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEnumerationType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IFieldType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IPredefinedType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IPredefinedType.ValidationStrategy;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IStringType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IYesNoType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IYesType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters.ProgrammingLanguage;

/**
 * Intermediate class for code generation holding information regarding a field.
 * Used to generate JavaScript code.
 */
@StringTemplateGroup("root")
public class CodeGenFieldDefinition extends CodeGenEREntities<CodeGenEntityData> {
	// This class extends CodeGenEntities exclusively for the generation of JavaScript code.
	private final int internalNumber;
	private final CodeGenFeldTypBuilder codeGenFeldTypBuilder = new CodeGenFeldTypBuilder();
	private final IField field;

	private final String uniqueName;

	private CodeGenFeldTypBasis feldtyp;

	private final boolean berechnungsUebertragServer;
	private final boolean isPflichtfeld;
	private final boolean isMandatoryForRepeatableGroup;
	private final Set<Integer> pflichtfeldLfdNr;
	private final Set<Integer> vordruckPflichtfeldLfdNr;
	private final Map<Locale, String> errorTexts4Req;

	private Locale aktuelleSprache;

	public CodeGenFieldDefinition(int internalNumber, CodeGenFieldDefinitionBuilder builder) {
		super(builder);
		this.internalNumber = internalNumber;
		this.field = builder.getField();
		this.pflichtfeldLfdNr = new HashSet<>();
		this.vordruckPflichtfeldLfdNr = new HashSet<>();
		this.uniqueName = data.getUniqueFieldNames().get(field.getFullName());

		this.berechnungsUebertragServer = builder.isCarryoverField();

		if (!(ProgrammingLanguage.JAVA_SCRIPT_REDUCED == data.getParams().getProgrammingLanguage() && noJSValidation())) {
			setzeFeldtyp(data.getParams().getLanguages());
		}

		this.isPflichtfeld = builder.isMandatoryField();

		this.isMandatoryForRepeatableGroup = builder.isMandatoryForRepeatableGroup();
		this.errorTexts4Req = builder.getErrorTexts4Req();
	}

	/**
	 * Erzeugt aus dem {@link IFieldType}-Objekten das zugehörige
	 * {@link CodeGenFeldTypBasis}-Objekt und setzt für die relevanten Feldtypen die
	 * sprachabhängigen Fehlertexte.
	 *
	 * @param unterstuetzteSprachen
	 * 		die im Modell unterstützten Sprachen.
	 */
	private void setzeFeldtyp(Collection<Locale> unterstuetzteSprachen) {
		IFieldType typ = field.getType();
		feldtyp = codeGenFeldTypBuilder.createObject(typ, data, getFullName());

		if (feldtyp instanceof CodeGenEnumerationTypeImpl enumTyp) {
			enumTyp.setOberflaechenwerte(unterstuetzteSprachen);
		}
	}

	public int getInternalNumber() {
		return internalNumber;
	}

	/**
	 * Methode wird aufgerufen um den Code für die Formatdefinitionen zu erzeugen.
	 * (für die Zielsprache JavaScript)
	 *
	 * @return erzeugter Code
	 */
	public String getFormatDefinitionCode() {
		if (data.getParams().getProgrammingLanguage() == ProgrammingLanguage.JAVA_SCRIPT_REDUCED && noJSValidation()) {
			//TODO[Template]
			return "keinePruefung(value";
		}

		// Behandele die Spezialität, dass nachKommastellenBis gesetzt sind,
		// aber die Nachkommastellen nicht. Dann wird für die Nachkommastellen
		// eine Null (0) verwendet.
		if (feldtyp instanceof CodeGenNumberTypeImpl z) {
			if (z.getNachkomma() == -1 && z.getNachkommaBis() != -1) {
				z.setNachkomma(0);
			}
		}

		if (ProgrammingLanguage.JAVA_SCRIPT_REDUCED == data.getParams().getProgrammingLanguage()) {
			return getJsReducedCode();
		}

		return feldtyp.toCode();
	}

	private String getJsReducedCode() {
		// bei Zielsprache JavaSkript wird bei Strings aus Performance Gründen
		// je nach Art des Typs
		// unterschiedliche Validator_methoden generiert.
		if (feldtyp instanceof CodeGenStringTypeImpl fs) {
			boolean ohneLaengenSpezifikation = fs.getMaxLaenge() < 1 && fs.getMinLaenge() < 1;
			if (StringUtils.isEmpty(fs.getMuster())) {
				return feldtyp.toCode(ohneLaengenSpezifikation ? "no" : "max");
			} else {
				// für Strings mit Muster müssen die Fehlertexte sprachspezifisch generiert werden
				fs.setSprache(aktuelleSprache);
				return feldtyp.toCode(ohneLaengenSpezifikation ? "pattern" : "");
			}
		}

		// Datumsformate werden in JavaScript sprachspezifisch generiert
		if (feldtyp instanceof CodeGenDateTypeImpl datumTyp) {
			datumTyp.setSprache(aktuelleSprache);
		}
		if (feldtyp instanceof CodeGenDateRangeTypeImpl datumTyp) {
			datumTyp.setSprache(aktuelleSprache);
		}
		// für Zahlen mit Nachkommastellen wird in JavaScript der sprachspezifische Dezimaltrenner benötigt
		if (feldtyp instanceof CodeGenNumberTypeImpl zahlTyp) {
			zahlTyp.setSprache(aktuelleSprache);
		}
		// Für Vordefinierte Typen müssen die Fehlertexte sprachspezifisch generiert werden
		if (feldtyp instanceof CodeGenPredefinedTypeImpl predefinedType) {
			predefinedType.setSprache(aktuelleSprache);
		}
		return feldtyp.toCode();
	}

	public boolean noJSValidation() {
		IFieldType typ = field.getType();
		return (typ instanceof IEnumerationType
				|| typ instanceof IYesType
				|| typ instanceof IYesNoType
				|| (typ instanceof IPredefinedType
				&& ((IPredefinedType) typ).getValidationConfig(ProgrammingLanguage.JAVA_SCRIPT_REDUCED)
				.orElse(ValidationStrategy.NONE) == ValidationStrategy.NONE))
				|| (typ instanceof IStringType
				&& ((IStringType) typ).isNoValueValidation());
	}

	/**
	 * Für Javascript muss pro unterstützter Sprache eine Validierungsfunktion generiert werden.
	 */
	public String getJSCode(Collection<Locale> unterstuetzteSprachen) {
		StringBuilder sb = new StringBuilder();
		for (Locale sprache : unterstuetzteSprachen) {
			aktuelleSprache = sprache;
			sb.append(this.toCode());
		}
		return sb.toString();
	}

	public String getJsSprachCode() {
		return String.format("%s_%s", aktuelleSprache.getLanguage(), aktuelleSprache.getCountry());
	}

	/**
	 * Als Abbildungsvorschrift gilt: Da Felder ab Version 10 nicht mehr eindeutig sind, muss in den
	 * JS-Namen der volle Pfadname des Feldes enthalten sein. Sonderzeichen werden entsprechend
	 * escaped.
	 *
	 * @return funktionsname
	 */
	public String getJSFunktionsName() {
		String jsName = StringUtil.transformToMethodName(getUniqueName());
		return data.getParams().getName() + jsName + "Validate";
	}

	public long getMaxZeilen() {
		return EntityUtils.getMaxZeile(field);
	}

	public boolean isPflichtfeld() {
		return isPflichtfeld;
	}

	public boolean isMandatoryForRepeatableGroup() {
		return isMandatoryForRepeatableGroup;
	}

	public IField getField() {
		return field;
	}

	public IFieldType getFormatData() {
		return field.getType();
	}

	public String getFullName() {
		return field.getFullName();
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public String getShortName() {
		return EntityUtils.getShortName(getFullName());
	}

	public CodeGenFeldTypBasis getFeldtyp() {
		return feldtyp;
	}

	public boolean isBerechnungsUebertragServer() {
		return berechnungsUebertragServer;
	}

	public Set<Integer> getPflichtFeldLfdNr() {
		return pflichtfeldLfdNr;
	}

	public void addPflichtfeldLfdNr(Integer lfdNr) {
		pflichtfeldLfdNr.add(lfdNr);
	}

	public Set<Integer> getVordruckPflichtFelder() {
		return vordruckPflichtfeldLfdNr;
	}

	public void addVordruckPflichtfeldLfdNr(Integer lfdNr) {
		vordruckPflichtfeldLfdNr.add(lfdNr);
	}

	public boolean hasErrorTexts4Req() {
		return !errorTexts4Req.isEmpty();
	}

	public Map<Locale, String> getErrorTexts4Req() {
		return errorTexts4Req;
	}

	/**
	 * Die Methode wird für die C++ und VBA Codegenerierung verwendet. Da es dort keine
	 * Mehrsprachigkeit gibt, wird hier der deutsche Fehlertext zurückgegeben.
	 */
	public String getErrorText4Req() {
		// Locale.GERMANY used for C++ and VBA only
		return Optional.ofNullable(errorTexts4Req.get(Locale.GERMANY)).orElse("");
	}

	public boolean isIndexField() {
		String groupFullName = EntityUtils.getParentPath(field.getFullName());
		String fieldShortName = EntityUtils.getShortName(field.getFullName());
		IGroup group = (IGroup) data.getEc().get(groupFullName);
		return group.getIndexFieldNames().contains(fieldShortName);
	}
}
