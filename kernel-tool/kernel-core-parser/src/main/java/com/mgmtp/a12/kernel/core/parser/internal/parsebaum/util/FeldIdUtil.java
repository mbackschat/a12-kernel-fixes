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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.common.grammar.languages.AbstractMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.utils.FeldIdUtilities;

/**
 * Stellt Hilfsmethoden für eine IFeldId bereit.
 */
public class FeldIdUtil {

	private FeldIdUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Aus einer übergebenen FeldId wird der im Bedingungstext benötigte String erzeugt. Hier werden
	 * folgende Spezifikationen berücksichtigt: *, .*, /*, @* und Kombination der letzten drei.
	 *
	 * @param f
	 * 		im Bedingungs-Text referenziertes Feld bzw. Kontext
	 * @param ruleFullName
	 * 		the rule group is used to determine how the entity has to be referenced in the
	 * 		condition text
	 * @param mlh
	 * 		if the rule group is referenced (without USB), it is replaced by the construct
	 * 		'RegelKontext' (depending on the project language)
	 * @return String-Darstellung wie im Bedingungstext benötigt.
	 */
	public static String asStringHDVOnly(IEntityId f, String ruleFullName, ConditionMultiLanguageHandler mlh) {
		if (!f.isHmodel()) {
			throw new IllegalArgumentException("no longer expected to be called for non-h models");
		}
		String nameUsedinConditionText = getNameBuiltForConditionTextHDVOnly(f, ruleFullName, mlh);
		return String.format("%s%s%s%s%s%s",
				nameUsedinConditionText,
				getLString(f.getIndices()[0], f.getMaxValues()[0]),
				getWildCardLRestricted(f),
				getZString(f, f.getIndices()[1], f.getMaxValues()[1]),
				getWildCardZRestricted(f),
				getUString(f.getUsb()));

	}

	/**
	 * Splits the given name by {@value ElementPathUtils#SEPARATOR} and '.' and goes through all
	 * resulting segments: If a segment is a reserved token for the given handler, it will be
	 * quoted.
	 *
	 * @param nameUsedInConditionText
	 * 		name which should be analyzed and eventually adapted. May not be {@code null} or
	 * 		empty.
	 * @param mlh
	 * 		handler from which the reserved tokens are taken. May not be {@code null}.
	 * @return if there were segments to quote, this method returns a copy of original string with
	 * 		those segments quoted, otherwise the original string.
	 */
	public static String quoteNameIfNecessary(String nameUsedInConditionText, AbstractMultiLanguageHandler mlh) {
		String[] namePartsBySlash = StringUtils.splitPreserveAllTokens(nameUsedInConditionText, EntityUtils.SEPARATOR_CHAR);
		boolean quoted = false;
		for (int i = 0; i < namePartsBySlash.length; i++) {
			String[] namePartsByDot = StringUtils.splitPreserveAllTokens(namePartsBySlash[i], '.');
			for (int j = 0; j < namePartsByDot.length; j++) {
				if (mlh.hasToBeQuoted(namePartsByDot[j])) {
					namePartsByDot[j] = "'" + namePartsByDot[j] + "'";
					quoted = true;
				}
			}
			namePartsBySlash[i] = StringUtils.join(namePartsByDot, '.');
		}
		if (!quoted) {
			return nameUsedInConditionText;
		}
		return StringUtils.join(namePartsBySlash, EntityUtils.SEPARATOR_CHAR);
	}

	private static String getNameBuiltForConditionTextHDVOnly(IEntityId f, String ruleFullName,
			ConditionMultiLanguageHandler mlh) {
		if (!f.isHmodel()) {
			throw new IllegalArgumentException();
		}
		if (f.getFullName().equals(EntityUtils.getParentPath(ruleFullName))
				// a wildcard is not allowed for the construct 'RegelKontext'
				&& !FeldIdUtilities.hasWildCard(f)) {
			return mlh.getTerminalName(GrammarOnly_ConditionParser.REGEL_KONTEXT);
		}

		boolean hasWildCardOnRootgroup = f.getIndices()[0] == IParseTreeEntity.WILDCARD;
		String name = EntityUtils.getNameBuiltForConditionTextHModel(ruleFullName, f.getFullName(), f.isField(),
				hasWildCardOnRootgroup);

		return quoteNameIfNecessary(name, mlh);
	}

	/**
	 * Hilfsmethode für die Darstellung eine Stern-Einschränkung für die lfd. Nr. Kontext
	 * Wird für das Formatieren verwendet.
	 * Nur für H-Modelle.
	 * Falls eine Einschränkung der Lfd. Nr. Stern spezifiziert wurde, gebe den
	 * entsprechenden String zurück, sonst einen Empty-String.
	 *
	 * @param f
	 * 		zu prüfende FeldId.
	 * @return String, der die Einschränkung der Laufenden Nr. Stern spezifiziert.
	 */
	private static String getWildCardLRestricted(IEntityId f) {
		if (f.getIndices()[0] == IParseTreeEntity.WILDCARD &&
				(f.getRootGroupWildcardStart() > 1 || f.getRootGroupWildcardEnd() < f.getMaxValues()[0])) {
			return String.format("(%d-%d)", f.getRootGroupWildcardStart(), f.getRootGroupWildcardEnd());
		}
		return "";
	}

	/**
	 * Hilfsmethode für die Darstellung eine Stern-Einschränkung für die Zeile eines Kontext
	 * Wird für das Formatieren verwendet.
	 * Nur für H-Modelle.
	 * Falls eine Einschränkung der Zeile- Stern spezifiziert wurde, gebe den
	 * entsprechenden String zurück, sonst einen Empty-String.
	 * Diese Einschränkung ist nur bei Feldern möglich.
	 *
	 * @param f
	 * 		zu prüfende FeldId.
	 * @return String, der die Einschränkung der Zeile Stern spezifiziert.
	 */
	private static String getWildCardZRestricted(IEntityId f) {
		if (f.isField() &&
				f.getIndices()[1] == IParseTreeEntity.WILDCARD &&
				(f.getRowWildcardStart() > 1 || f.getRowWildcardEnd() < f.getMaxValues()[1])) {
			return String.format("(%d-%d)", f.getRowWildcardStart(), f.getRowWildcardEnd());
		}
		return "";
	}

	/**
	 * Hilfsmethode für das Formatieren.
	 * Nur für H-Modelle.
	 * Gibt die Darstellung des usb-Indexes in der Stringdarstellung an.
	 *
	 * @param usb
	 * 		der USB-Index als long
	 * @return Stringdarstellung im Bedingungstext.
	 */
	private static String getUString(long usb) {
		return (usb == IParseTreeEntity.WILDCARD) ? "@*" : "";
	}

	/**
	 * Hilfsmethode für das Formatieren.
	 * Gibt die Stringdarstellung der laufenden Nummer im Bedingungstext zurück.
	 *
	 * @param l
	 * 		index
	 * @param max
	 * 		maximale Index - wird benötigt, um zu entscheiden, wie die Angabe des Index 1
	 * 		ausgegeben werden soll.
	 * @return String-Darstellung für den formatierten Text.
	 */
	private static String getLString(long l, long max) {
		if (max == 1) {
			return "";
		}
		if (l == IParseTreeEntity.WILDCARD) {
			return "/*";
		}
		if (l > 0) {
			return "/" + l;
		}

		return "";
	}

	/**
	 * Nur für H-Modelle!
	 * Gibt die Darstellung der Zeile für den Bedingungstext einer Regel zurück.
	 * In der Methode werden auch die Besonderheiten für Kontexte behandelt.
	 * Folgende Besonderheiten gelten für Kontexte
	 * <ul>
	 * <li>
	 * Für Kontexte wird die Zeilen-Iteration anders notiert, da dort explizit <code>.#Zeile</code>
	 * verwendet wird.
	 * <li>Für Kontexte wird der Stern nicht explizit notiert, da bei Kontexten für alle
	 * mehrzeiligen Felder immer der Stern spezifiziert ist.
	 * </ul>
	 *
	 * @param f
	 * 		FeldId, für die die Zeilenspezifizikation zurückgegeben werden soll
	 * @param z
	 * 		Index für die Zeile
	 * @param max
	 * 		maximaler Index für die Zeile
	 * @return die Darstellung der Zeile für den Bedingungstext einer Regel
	 */
	private static String getZString(IEntityId f, long z, long max) {

		//Zeilen-Iteration für Kontexte hat gesonderte Syntax
		if (z == IParseTreeEntity.ITERATION && !f.isField()) {
			return ".#Zeile";
		}

		//Falls keine Wiederholbarkeit,  wird auch ein .1 unterdrückt.
		if (max <= 1) {
			return "";
		}

		//konkrete Angabe der Zeile wird ausgegeben.
		if (z > 0) {
			return "." + z;
		}

		//Wildcard wird nur für das Feld ausgegeben. Für Kontexte ist dies implizit.
		if (f.isField() && z == IParseTreeEntity.WILDCARD) {
			return ".*";
		}

		return "";
	}

	/**
	 * returns true, if the condition references more than one field.
	 * This is the case if
	 * <ul>
	 * <li>more than one parameter in the list
	 * <li>a field index is a wildcard
	 * </ul>
	 * As usual, it is not checked if the group has only one element.
	 */
	public static boolean referencesMoreThanOneParam(IEntityListCondition b) {
		List<IEntityId> fs = b.getEntityList().getEntityIdList();
		return fs.size() > 1 || fs.stream().anyMatch(FeldIdUtilities::hasWildCard);
	}

	/**
	 * returns true, if only one single field is referenced, false otherwise
	 */
	public static boolean referenceOneField(IEntityListCondition b) {
		return !referencesMoreThanOneParamOrIsGroup(b);
	}

	/**
	 * returns true, if only one single field or a single group is referenced, false otherwise
	 */
	public static boolean referenceOneFieldorOneGroup(IEntityListCondition b) {
		return !referencesMoreThanOneParam(b);
	}

	/**
	 * returns true, if more than one single field is referenced. this is the case if
	 * <ul>
	 * <li>the param-list has more than one elements
	 * <li>the param in the list is a group
	 * <li>the param in the list specifies at least one wildcard
	 * </ul>
	 */
	public static boolean referencesMoreThanOneParamOrIsGroup(IEntityListCondition b) {
		List<IEntityId> fs = b.getEntityList().getEntityIdList();
		return !fs.get(0).isField() || referencesMoreThanOneParam(b);
	}

	/**
	 * Remove the quotes of a String containing tokens of the type
	 * {@link GrammarOnly_ConditionParser#NameOhnePunktMitQuotes}.
	 *
	 * @param stringWithQuotes
	 * 		the string possibly containing quotes.
	 * @return the string with the quotes removed.
	 */
	public static final String unquoteStringWithQuotes(String stringWithQuotes) {
		return stringWithQuotes.replace("'", "");
	}
}
