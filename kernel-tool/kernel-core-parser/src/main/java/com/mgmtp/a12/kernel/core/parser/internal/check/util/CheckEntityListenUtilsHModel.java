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
package com.mgmtp.a12.kernel.core.parser.internal.check.util;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.EntityListeContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRefForHModel;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;

/**
 * helper methods for ExtMP H models only
 */
public class CheckEntityListenUtilsHModel extends CheckImplAbstractClass {
	public CheckEntityListenUtilsHModel(CheckImplAbstractClass obj) {
		super(obj.getParser());
	}

	/**
	 * checks that no entity in the param list has a wildcard for the usb.
	 *
	 * @param refs
	 * 		list for entities to be checked.
	 * @param ctx
	 * 		for nice error messages only
	 * @param opName
	 * 		name of the operation, for nice error messages only
	 */
	public void checkNoUSBWildCard(List<EntityRef> refs, EntityListeContext ctx, String opName) {
		if (fullParser.isA12Model()) {
			return;
		}

		//no need to expand,  as this can be checked on the groups as well.
		refs.forEach(ref -> {
			if (ref.getUsb() == IParseTreeEntity.WILDCARD) {
				String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_USB_WILDCARD, opName);
				throw createMVKParserError(ctx.getParent(), errMessage);
			}
		});
	}

	/**
	 * checks that no entity of the list specifies a wildcard for usb or for "lfdNr"
	 *
	 * @param refs
	 * 		list for entities to be checked.
	 * @param ctx
	 * 		for nice error messages only
	 * @param opName
	 * 		name of the operation, for nice error messages only
	 */
	public void checkNoLUWildCard(List<EntityRef> refs, EntityListeContext ctx, String opName) {
		if (fullParser.isA12Model()) {
			return;
		}
		//no need to expand,  as this can be checked on the groups as well.
		refs.stream().map(EntityRefForHModel.class::cast).forEach(ref -> {
			if (ref.getUsb() == IParseTreeEntity.WILDCARD || ref.getLfdNr() == IParseTreeEntity.WILDCARD) {
				String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_LU_WILDCARD, opName,
						ref.getNameBuiltFromConditionText());
				throw createMVKParserError(ctx.getParent(), errMessage);
			}
		});
	}

	/**
	 * checks that no wildcard is specified and zeile specifies iteration.
	 *
	 * @param refs
	 * 		list for entities to be checked.
	 * @param ctx
	 * 		for nice error messages only
	 * @param opName
	 * 		name of the operation, for nice error messages only
	 */

	public void checkAlleEbeneOhneSternUndZeileNichtKonkret(List<EntityRef> refs, EntityListeContext ctx, String opName) {
		refs.stream().map(EntityRefForHModel.class::cast)
				.forEach(ref -> {
					if (ref.getZeile() != IParseTreeEntity.ITERATION) {
						String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_MISSING_Z_ITERATION, opName,
								ref.getNameBuiltFromConditionText());
						throw createMVKParserError(ctx, errMessage);
					}
					if (ref.getUsb() == IParseTreeEntity.WILDCARD ||
							ref.getLfdNr() == IParseTreeEntity.WILDCARD) {
						String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_NO_WILDCARDS_ALLOWED, opName);
						throw createMVKParserError(ctx, errMessage);
					}
				});

	}

	/**
	 * prüft dass alle Felder (direkt oder indirekt referenziert) mehrzeilig sind.
	 *
	 * @param refs
	 * 		list for entities to be checked.
	 * @param ctx
	 * 		for nice error messages only
	 * @param opName
	 * 		name of the operation, for nice error messages only
	 */

	public void checkFeldMehrzeilig(List<EntityRef> refs, EntityListeContext ctx, String opName) {
		refs.forEach(e -> checkZMult((EntityRefForHModel) e, opName, ctx.getParent()));
	}

	private void checkZMult(EntityRefForHModel ref, String opName, ParserRuleContext ctx) {
		EntityUtils.expandToFieldList(ref, getParser(), ctx).forEach(f -> {
			if (EntityUtils.getMaxZeile(f) == 1) {
				String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_NO_Z_MULT, opName,
						ref.getNameBuiltFromConditionText());
				throw createMVKParserError(ctx, errMessage);
			}
		});
	}

	/**
	 * prüft dass alle Felder Iteration spezifizieren.
	 * Precondition: in der Liste werden nur Felder angegeben.
	 *
	 * @param refs
	 * 		list for entities to be checked.
	 * @param ctx
	 * 		for nice error messages only
	 * @param opName
	 * 		name of the operation, for nice error messages only
	 */
	public void checkAllFieldsZIteration(List<EntityRef> refs, EntityListeContext ctx, String opName) {
		refs.stream().map(EntityRefForHModel.class::cast)
				.forEach(ref -> {
					if (ref.getZeile() != IParseTreeEntity.ITERATION) {
						String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_PARAMS_FOR_OP, opName,
								ref.getNameBuiltFromConditionText());
						throw createMVKParserError(ctx, errMessage);
					}
				});
	}

	/**
	 * prüft dass alle (direkt oder indirekt referenzierten) Felder den gleichen mzi haben.
	 *
	 * @param paramList
	 * 		list for entities to be checked.
	 * @param ctx
	 * 		for nice error messages only
	 * @param opName
	 * 		name of the operation, for nice error messages only
	 */
	public void checkAllFieldsSameMZI(List<EntityRef> paramList, EntityListeContext ctx, String opName) {
		Long[] mzi = new Long[1];
		//extra loop is required, as we need the ref-obj to provide a nice error message.
		for (EntityRef ref : paramList) {
			EntityUtils.expandToFieldList(ref, fullParser, ctx).forEach(f -> {
				if (mzi[0] != null && mzi[0] != EntityUtils.getMaxZeile(f)) {
					String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_DIFFERENT_Z_MULT, opName,
							ref.getNameBuiltFromConditionText());
					throw createMVKParserError(ctx, errMessage);

				}
				mzi[0] = EntityUtils.getMaxZeile(f);
			});
		}
	}

	/**
	 * Checks if all entities specify {@link IParseTreeEntity#WILDCARD} for the row.
	 * Attention: for groups a {@link IParseTreeEntity#WILDCARD} in the row index does not guarantee
	 * that the group contains fields with a repeatable row, cf.
	 * {@link EntityRefForHModel#EntityRefHModel(IEntity, String)}.
	 *
	 * @param refs
	 * 		list to be checked
	 * @param ctx
	 * 		only required for error handling
	 * @param opName
	 * 		name of the operation - only required for error handling
	 */

	public void checkAllEntitiesZWildcard(List<EntityRef> refs, EntityListeContext ctx, String opName) {
		refs.stream().map(EntityRefForHModel.class::cast)
				.forEach(ref -> {
					if (ref.getZeile() != IParseTreeEntity.WILDCARD ||
							ref.isZSternEingeschraenkt()) {
						String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_NO_Z_WILDCARD_WITHOUT_RESTRICTION,
								opName, ref.getNameBuiltFromConditionText());
						throw createMVKParserError(ctx, errMessage);
					}
				});
	}

	/**
	 * Keine Entität in der Liste spezifiziert eine Zeileniteration.
	 * Anmerkung: wird für Kontexte verwendet.
	 *
	 * @param refs
	 * 		list to be checked
	 * @param ctx
	 * 		only required for error handling
	 * @param opName
	 * 		name of the operation - only required for error handling
	 */
	public void checkNoZIteration(List<EntityRef> refs, EntityListeContext ctx, String opName) {
		if (fullParser.isA12Model()) {
			return;
		}
		refs.stream().map(EntityRefForHModel.class::cast)
				.forEach(ref -> {
					if (ref.getZeile() == IParseTreeEntity.ITERATION) {
						String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_NO_Z_ITERATION, opName);
						throw createMVKParserError(ctx.getParent(), errMessage);
					}
				});
	}

}
