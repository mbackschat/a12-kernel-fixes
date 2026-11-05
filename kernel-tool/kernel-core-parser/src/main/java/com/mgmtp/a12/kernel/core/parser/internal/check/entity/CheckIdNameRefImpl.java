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
package com.mgmtp.a12.kernel.core.parser.internal.check.entity;

import java.util.Set;

import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils.RelativeBeyondDocumentRootException;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.IdNameRefContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Lenient_ConditionParser;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRefMap;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;

import jakarta.validation.constraints.NotNull;

/**
 * Stellt die Prüfungen für <code>checkIdNameRef</code> bereit.
 * Durch die Grammatik wird sichergestellt, dass die Methode nur für A12-Modelle aufgerufen wird.
 * <p>
 * Während des Parsen wird diese Check-Methode für jedes Pfad-Stück aufgerufen. Dadurch wird der
 * Pfad mit eventuellen Stern-Angaben Stück-für-Stück geprüft.
 * <p>
 * * <code> entity:   slash='/' ? idNameRef  ('/' idNameRef )*;
 * idNameRef: idName pstern='.*'? stern='*'? { checkIdNameRef(_localctx); };</code>
 * <p>
 * Folgendes wird überprüft:
 * <ul>
 * <li>Überprüfe, dass die Pfadangabe zu einem gültigen Vordruck/Feld/Kontext gehört.
 * {@link ParserErrorTypesEnum#MVK_INVALID_ROOT_GROUP} falls es sich um einen absoluten Pfad und um
 * die
 * erste Angabe handelt. {@link ParserErrorTypesEnum#MVK_INVALID_ENTITY} sonst.
 * <li>Überprüfe, dass eine Sternangabe nur für wiederholbare Ebenen spezifiziert wurde.
 * {@link ParserErrorTypesEnum#MVK_INVALID_WILDCARD}
 * <li>Um eine schöne Fehlermeldung zu bekommen: Überprüfe, das kein .* angegeben wurde.
 * {@link ParserErrorTypesEnum#MVK_POINT_IN_A12_MODEL}
 * </ul>
 * Die gefundene Entität wird jeweils in der {@link EntityRefMap} zu dem EntityKontext gespeichert.
 * Da die Methode Stück für Stück aufgerufen wird, kann jeweils der Pfad sukzessive geprüft werden.
 */
public class CheckIdNameRefImpl extends CheckIdNameRefCommonImpl {

	public CheckIdNameRefImpl(@NotNull Lenient_ConditionParser parser) {
		super(parser);
	}

	/*
	 * Check this part of the grammar
	 * entity: slash='/' ? idNameRef ('/' idNameRef )*
	 * idNameRef: idName stern='*'? { checkIdNameRef(_localctx); }
	 */
	public void check(@NotNull IdNameRefContext idNameCtx) {

		//initialse the contexts.
		setCtxs(idNameCtx);

		//read current part of the name
		String namePart = idNameCtx.idName() == null ? null : idNameCtx.idName().getText();

		String nameUsedInConditionText = getNameBuiltFromConditionText(namePart);

		// checks if the entity exists
		IEntity e = getEntityFromConditionText(nameUsedInConditionText);

		//this is only allowed in the grammar, to make nice error messages.
		if (idNameCtx.pstern != null) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_POINT_IN_A12_MODEL, nameUsedInConditionText);
			throw createMVKParserError(errMessage);
		}

		EntityRef eRef = parser.getEntityRef(eSpecCtx);
		PathNotationConfig pathNotation;
		if (eRef != null) {
			pathNotation = eRef.getPathNotationConfig();
		} else {
			boolean isAbsPath = idNameCtx.getParent() instanceof GrammarOnly_ConditionParser.EntityContext entityContext
					&& entityContext.slash != null;
			boolean isShortName = idNameCtx.getParent() instanceof GrammarOnly_ConditionParser.EntityContext entityContext
					&& entityContext.idNameRef().size() == 1
					&& entityContext.relativeUp() == null
					&& entityContext.slash == null
					&& parser.getEc().get(EntityUtils.getParentPath(parser.getRule().getFullName()) + "/" + namePart) == null;
			boolean isRelPath = !isAbsPath && !isShortName;
			boolean isRelativeUpwardReferences = false; // paths with relative upward steps have had their pathNotationConfig created
			// in CheckRelativeUpImpl
			boolean hasExplicitTurningPoint = false; // same for turning points (only exist on relative upward paths)
			pathNotation = new PathNotationConfig(isRelPath, isShortName, isRelativeUpwardReferences, hasExplicitTurningPoint);
		}

		//The entity is added to the map.
		//names are read in steps
		putEntityToEntityMap(e, nameUsedInConditionText, pathNotation);

		//Set the current index and check the correctness of the index.
		setIndexOfCurrentLevel(e, nameUsedInConditionText);
	}

	/**
	 * adds the entity to the entity ref map and checks the current index.
	 * checks the index for the current level. If no errors are found, the index ist set.
	 * <ul>
	 * <li>if the entity is not repeatable, no wildcard may be set</li>
	 * <li>if the entity is repeatable and wildcards are already set in parent groups, a wildcard
	 * must be set</li>
	 * <li>if the entity is repeatable and in a group below the group of the error field, a wildcard
	 * must be set</li>
	 * <li>if the entity is repeatable and not in the path of the errof field , a wildcard must be
	 * set</li>
	 * </ul>
	 */
	private void setIndexOfCurrentLevel(IEntity e, String nameUsedInConditionText) {

		//check the correctnes of the index and add the index, if it is correct.
		long index = 1; // default for non-repeatable groups
		if (isWildCardSet()) {
			if (!EntityUtils.isRepeatableOnLowestLevel(e) && !parser.isLenient()) {
				String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_WILDCARD, nameUsedInConditionText);
				throw createMVKParserError(errMessage);
			}
			index = IParseTreeEntity.WILDCARD;
		} else {
			if (EntityUtils.isRepeatableOnLowestLevel(e)) {
				// Further checks will be done in CheckEntitySpecImpl
				index = IParseTreeEntity.ITERATION;
			}
		}
		setIndex(index);
	}

	//helper method to get readable code.
	private boolean isWildCardSet() {
		assert idNameCtx instanceof IdNameRefContext;
		if (idNameCtx instanceof IdNameRefContext idNameRefCtx) {
			return idNameRefCtx.stern != null;
		} else { // should not be reached.
			return false;
		}
	}

	//helper method to get readable code.
	private String constructEntityFullName(String name) {
		try {
			return isAbsolutPath() ? name : EntityUtils.relPath2AbsPath(getRulePrefix(), name);
		} catch (RelativeBeyondDocumentRootException e) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_TOO_MANY_PARENT_GROUPS);
			throw createMVKParserError(errMessage);
		}
	}

	//helper method to get readable code.
	protected boolean isAbsolutPath() {
		assert eSpecCtx != null;
		return (eSpecCtx == null || eSpecCtx.entity().slash != null);
	}

	//helper method to get readable code.
	private void setIndex(long ind) {
		EntityRef ref = getEntityRef(eSpecCtx);
		ref.addIndex(ind);
	}

	/**
	 * return the entity. If no entity with the specified name is found, an exception is thrown.
	 *
	 * @param nameUsedInConditionText
	 * 		the name of the entity as referenced in the condition text
	 * @return the entity. is never null.
	 */
	private IEntity getEntityFromConditionText(String nameUsedInConditionText) {
		//first fullName candidate of the entity built from the rule group path and the name used in the condition text
		String entityFullNameCandidate = constructEntityFullName(nameUsedInConditionText);

		IEntity e = getEntity(entityFullNameCandidate);

		if (e == null && !isAbsolutPath() && parser.isFieldRefByShortNameAllowed()) {
			// no entity found to the fullname candidate. Check if the entity is a field referenced by a short name without a path
			Set<String> possibleFullNames = parser.getEc().shortName2FullName(nameUsedInConditionText);
			if (possibleFullNames.size() > 1) {
				ParserErrorTypesEnum err = ParserErrorTypesEnum.MVK_FIELDNAME_NOT_UNIQUE;
				String possibleFullNamesStr = "'" + String.join("', '", possibleFullNames) + "'";
				String errMessage = getErrorMessage(err, nameUsedInConditionText, possibleFullNamesStr);
				throw createMVKParserError(errMessage);
			}
			if (possibleFullNames.size() == 1) {
				e = getEntity(possibleFullNames.iterator().next());
			}
		}

		if (e == null) {//not a valid group or field
			ParserErrorTypesEnum err = EntityUtils.isRootGroup(entityFullNameCandidate) ?
					ParserErrorTypesEnum.MVK_INVALID_ROOT_GROUP :
					ParserErrorTypesEnum.MVK_INVALID_ENTITY;
			String errMessage = getErrorMessage(err, nameUsedInConditionText);
			throw createMVKParserError(errMessage);
		}

		return e;
	}
}
