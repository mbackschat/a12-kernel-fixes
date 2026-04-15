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

import com.mgmtp.a12.kernel.core.parser.internal.check.CheckImplAbstractClass;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.hdv.CheckFormularBedingungImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.operation.hdv.CheckAnzahlAngegebenOpHModelImpl;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnum;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorTypesEnumForHModel;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.AnzahlAngegebenOpHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeKonkretContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeNewSternContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FeOldSternContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FormularBedingungContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FormularEbeneLfdNrContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FormularEbenenSpecHModelContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.FormularSpecContext;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.GrammarOnly_ConditionParser.NameOhnePunktContext;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRefUtils;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;

/**
 * Utility Methoden für Formulare
 * <p>
 * Nur ExtMP H-Models.
 */
public class CheckFormularUtilitiesHModel extends CheckHModelCommon {
	private static final String LFDNR = "lfd. Nr."; // nur H-Models: daher reichen deutsche Textangaben

	public CheckFormularUtilitiesHModel(CheckImplAbstractClass obj) {
		super(obj.getParser());
	}

	/**
	 * creates the entity reference for the root group and puts it to the map.
	 * Specification of L and U is not checked, because the method is used for
	 * {@link CheckFormularBedingungImpl#check(FormularBedingungContext)} and {@link
	 * CheckAnzahlAngegebenOpHModelImpl#check(AnzahlAngegebenOpHModelContext)}
	 * and the checks are different for each caller.
	 *
	 * @param fCtx
	 * 		- context specifying the root group
	 * @return reference to the entity
	 */
	public EntityRef putEntityForRootGroupToMap(FormularSpecContext fCtx) {

		NameOhnePunktContext ctx = fCtx.nameOhnePunkt();
		String nameAsUsedInConditionText = ctx.start.getText();

		//get the entity with the path name
		IEntity e = getEntity("/" + nameAsUsedInConditionText);

		//no entity with the given name found.
		if (e == null) {
			String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_ROOT_GROUP, nameAsUsedInConditionText);
			throw createMVKParserError(ctx, errMessage);
		}

		EntityRefUtils.checkNoEmptyGroup(e, nameAsUsedInConditionText, fullParser, ctx);

		return fullParser.putEntityToMapHDVOnly(fCtx, e, nameAsUsedInConditionText);
	}

	/**
	 * checks if the l-iteration is correct.
	 * In case of an A12 model, the error field must be in the group.
	 * <p>
	 * In case of an H model, it is sufficient if the error field is in a repeatable root group.
	 *
	 * @param ref
	 * 		rootGroup as entity.
	 * @param fCtx
	 * 		for ice error messages
	 */
	private void setLIteration(EntityRef ref, FormularSpecContext fCtx) {
		NameOhnePunktContext ctx = fCtx.nameOhnePunkt();
		String rootGroup = ctx.start.getText();

		// the entity is repeatable, but nothing is specified -> ie. it is an iteration.
		if (fullParser.isA12Model()) { // A12-Model:  falls Iteration muss dies der Vordruck des Fehlerfeldes sein.
			if (!EntityUtils.isGroupAboveOrEqualErrorEntityPath(ref.getEntity(), getErrorEntity())) {
				String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_NO_WILDCARD, rootGroup,
						ref.getNameBuiltFromConditionText());
				throw createMVKParserError(ctx, errMessage);
			}
		} else { // H-Model:  es reicht aus,  dass über lfdNr. iteriert werden kann.
			if (EntityUtils.getMaxLfdNr(fullParser.getErrorEntity()) == 1) {
				String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_L_ITER, rootGroup);
				throw createMVKParserError(fCtx, errMessage);
			}
		}
		ref.setLfdNr(IParseTreeEntity.ITERATION);
	}

	/**
	 * check if the usb-specification of the FormularSpecContext is valid regarding the usb
	 * specification of the
	 * entity.
	 * <ul>
	 * <li>if the entity has no usb, no usb specification should be specified.
	 * <li>if the entity has an usb, but no usb specification, check if the usb-iteration is valid.
	 * </ul>
	 *
	 * @param ref
	 * 		reference to the root group
	 * @param fCtx
	 * 		context specifying the indices.
	 */
	public void setAndCheckUSB(EntityRef ref, FormularSpecContext fCtx) {

		String name = EntityUtils.getRootGroupName(ref.getEntity().getFullName());
		boolean hasUSB = EntityUtils.hasMaxUsb(ref.getEntity());
		if (fCtx.formularEbenenSpecHModel() != null && fCtx.formularEbenenSpecHModel()
				.usbSpezifier() != null) {//there is a specification for the usb
			if (!hasUSB) {//a usb may only be specified,  if it is allowed for the entity
				String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_USB, name);
				throw createMVKParserError(fCtx, errMessage);
			}
			ref.setUsb(IParseTreeEntity.WILDCARD);

		} else {// no spec for usb means iteration. Thus check if the error field has an usb.
			if (hasUSB && !EntityUtils.hasMaxUsb(getErrorEntity())) {
				String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_U_ITER, name);
				throw createMVKParserError(fCtx, errMessage);
			}
			if (EntityUtils.hasMaxUsb(ref.getEntity())) {
				ref.setUsb(IParseTreeEntity.ITERATION);
			}
		}
	}

	/**
	 * returns true, if a wildcard is specified
	 * either a usb wild card is specified or a wild card for L.
	 *
	 * @param fCtx
	 * 		context to be analysed
	 * @return true if a wildcard is set
	 */
	public boolean hasWildCardSpecified(FormularSpecContext fCtx) {
		FormularEbenenSpecHModelContext eCtx = fCtx.formularEbenenSpecHModel();
		return (eCtx != null && (eCtx.usbSpezifier() != null ||
				isWildCard(eCtx.formularEbeneLfdNr())));
	}

	/**
	 * Sets the concrete L specification if it is valid.
	 *
	 * @param ref
	 * 		reference to the root group
	 * @param cctx
	 * 		context to be analysed.
	 */
	public void setAndCheckL(EntityRef ref, FormularSpecContext cctx) {
		FormularEbenenSpecHModelContext ctx = cctx.formularEbenenSpecHModel();

		//check the specification.
		if (ctx != null && ctx.formularEbeneLfdNr() != null) {
			FormularEbeneLfdNrContext fCtx = ctx.formularEbeneLfdNr();
			checkForCorrectWildcard(ctx);
			setLWildcard(ref, fCtx);
			setLConcrete(ref, fCtx);

		} else {//no specification. i.e. iteration is correct.
			if (ref.getMaxLfdNr() == 1) {
				ref.setLfdNr(1);
			} else {
				setLIteration(ref, cctx);
			}
		}
	}

	private void setLConcrete(EntityRef ref, FormularEbeneLfdNrContext fCtx) {
		if (fCtx instanceof FeKonkretContext feKonkretContext) {
			//check index and set the saved index to the concrete index.
			String ls = feKonkretContext.ZiffernFolge().getText();
			String rootGroupName = EntityUtils.getRootGroupName(ref.getEntity().getFullName());
			checkIndex(ls, fCtx.getParent(), ref.getMaxLfdNr(), LFDNR, rootGroupName);
			ref.setLfdNr(Long.parseLong(ls));
		}
	}

	/**
	 * sets the wildcard, if the specification is correct.
	 *
	 * @param ref
	 * 		reference to the root group
	 * @param fCtx
	 * 		context to be analysed
	 */
	private void setLWildcard(EntityRef ref, FormularEbeneLfdNrContext fCtx) {
		if (isWildCard(fCtx)) {
			if (ref.getMaxLfdNr() == 1) {
				//wild card is not allowed,  because the root group is not repeatable
				String rootGroupName = EntityUtils.getRootGroupName(ref.getEntity().getFullName());
				String errMessage = getErrorMessage(ParserErrorTypesEnum.MVK_INVALID_WILDCARD, rootGroupName);
				throw createMVKParserError(fCtx, errMessage);
			}
			ref.setLfdNr(IParseTreeEntity.WILDCARD);
		}
	}

	private boolean isWildCard(FormularEbeneLfdNrContext fCtx) {
		return (fCtx instanceof FeOldSternContext || fCtx instanceof FeNewSternContext);
	}

	/**
	 * check if the correct form of the wildcard is used in the error condition text.
	 * This is necessary because in H models the syntax requires a slash, in A12 models
	 * the wildcard is without a slash.
	 * To get nice error messages we check this in the java code and not in the grammar.
	 * Precondition: method is only called, if a specification for LfdNr exists.
	 *
	 * @param ctx
	 * 		specification to be analysed.
	 */
	private void checkForCorrectWildcard(FormularEbenenSpecHModelContext ctx) {
		assert (ctx != null && ctx.formularEbeneLfdNr() != null);

		FormularEbeneLfdNrContext fCtx = ctx.formularEbeneLfdNr();

		if (fullParser.isHModel() && fCtx instanceof FeNewSternContext) {
			String errMessage = getErrorMessage(ParserErrorTypesEnumForHModel.MVK_INVALID_L_MULT_H_MODEL);
			throw createMVKParserError(ctx, errMessage);
		}
	}

}
