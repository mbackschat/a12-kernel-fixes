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
package com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.formatter;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.constants.EntityIdConstants;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Base_ConditionLexer;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.languages.ConditionMultiLanguageHandler;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.bedingung.ConditionImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.impl.operation.OperationImpl;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.EntityIdFormatterUtil;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.FeldIdUtil;
import com.mgmtp.a12.kernel.core.parser.internal.parsebaum.util.formatter.FormatData.FormatModus;
import com.mgmtp.a12.kernel.core.parser.internal.util.MissingEntityEC;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityIdList;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IApplicationCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IComparisonCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICompositeCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IDateValidityCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListAndValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IEntityListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IPredefinedTypeValidityCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IRootGroupCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.IValueListCondition;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IBoolConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ICompositeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IConstantOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IEntityListOperationWithValue;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IFieldIdOperationWithParameters;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IGroupOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfRootGroupsOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.INumberOfUsbOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOpListOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOpListOperationWithParameter;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.IPointInTimeOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.ISetOpOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateAddOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateDiffOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateExtractionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateRangeConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateRangeExtractionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.IDateTimeConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.operation.date.ITimeConstructionOperation;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListAndValueListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.EntityListConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.FieldIdOperationName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.OperationType;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.types.RootGroupConditionName;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.core.utils.internal.CommonUtils;

/**
 * Provides methods to format the textual representation of a rule condition or an operation.
 * The rule condition or the operation has to be provided as a parse-tree, therefore the input
 * has to be a valid condition resp. operation.
 */
public class FormatterVisitor extends ASTVisitor {

	private static final int DEFAULT_INDENT_LEVEL = 0;
	private static final int DEFAULT_WIDTH = 80;

	/**
	 * returns a formatted output String.
	 *
	 * @param pbe
	 * 		- parse tree of an operation
	 * @param serviceConfig
	 * @param ruleFullName
	 * 		the rule group is used to determine how entities are referenced in the
	 * 		condition
	 * 		text
	 * @return formatted output string
	 */
	public static String getFormattedOutput(IParseTreeEntity pbe, IServiceConfiguration serviceConfig, String ruleFullName,
			ConditionMultiLanguageHandler mlh) {
		IEC ec = serviceConfig.getIEC();
		if (serviceConfig.strictness() == IServiceConfiguration.ParserStrictness.LENIENT) {
			ec = MissingEntityEC.wrapEC(ec);
		}
		return getFormattedOutput(pbe, ec, serviceConfig.getModelSpecificSettings(), ruleFullName, mlh);
	}

	public static String getFormattedOutput(IParseTreeEntity pbe, IEC ec, IModelSpecificSettings modelSpecificSettings,
			String ruleFullName, ConditionMultiLanguageHandler mlh) {
		return getFormattedOutput(pbe, DEFAULT_WIDTH, DEFAULT_INDENT_LEVEL, false, ec, modelSpecificSettings, ruleFullName, mlh);
	}

	public static String getFormattedOutput(IParseTreeEntity pbe, int width, int indent, boolean compact, IEC ec,
			IModelSpecificSettings modelSpecificSettings, String ruleFullName, ConditionMultiLanguageHandler mlh) {
		FormatterVisitor v = new FormatterVisitor(width, indent, compact, ec, modelSpecificSettings, ruleFullName, mlh);
		if (pbe instanceof ConditionImpl cond && cond.getSuppressedWarnings() != null && !cond.getSuppressedWarnings().isEmpty()) {
			v.addSuppressedWarnings(cond.getSuppressedWarnings());
		}
		pbe.traverse(v);
		return v.fData.getOutput();
	}

	private final FormatData fData;
	private final boolean compact;
	private final IEC ec;
	private final boolean isHmodel;
	private final ConditionMultiLanguageHandler mlh;
	private final String ruleFullName;
	private final boolean isFieldRefByShortNameAllowed;

	//wird verwendet, um die Angabe von [ bzw. ] zu unterdrücken,
	//wenn der FeldWert als Parameter verwendet wird.
	private final Deque<Boolean> feldWertNeedsBrackets = new ArrayDeque<>();

	private FormatterVisitor(int width, int indentLevel, boolean compact, IEC ec, IModelSpecificSettings modelSpecificSettings,
			String ruleFullName,
			ConditionMultiLanguageHandler mlh) {
		this.fData = new FormatData(width, indentLevel);
		feldWertNeedsBrackets.push(Boolean.TRUE);
		this.compact = compact;
		this.ec = ec;
		this.isHmodel = ExtmpUtils.isHModel(modelSpecificSettings);
		this.isFieldRefByShortNameAllowed = modelSpecificSettings.isFieldRefByShortNameAllowed();
		this.ruleFullName = ruleFullName;
		this.mlh = mlh;
	}

	private void addSuppressedWarnings(Set<String> suppressedWarnings) {
		for (String suppressedWarning : suppressedWarnings) {
			fData.append("@" + mlh.getTerminalName(Base_ConditionLexer.UNTERDRUECKE_WARNUNG) + "(" + suppressedWarning + ")");
			fData.newline();
		}
	}

	private void addCommentLines(String commentString) {
		splitCommentLinesAndTrim(commentString).forEach(fData::appendWithBreak);
	}

	private List<String> splitCommentLinesAndTrim(String comment) {
		return Arrays.stream(comment.split("\\r?\\n"))
				.map(String::trim)
				.toList();
	}

	private void addCommentBefore(ICondition cond) {
		if (!(cond instanceof ConditionImpl)) {
			return;
		}
		((ConditionImpl) cond).getCommentBefore().ifPresent(c -> {
					addCommentLines(c);
					fData.newline();
				}
		);
	}

	private void addCommentAfter(ICondition cond) {
		if (!(cond instanceof ConditionImpl)) {
			return;
		}
		((ConditionImpl) cond).getCommentAfter().ifPresent(c -> {
					addCommentLines(c);
					fData.newline();
				}
		);
	}

	private void addCommentBefore(IOperation op) {
		if (!(op instanceof OperationImpl)) {
			return;
		}
		((OperationImpl) op).getCommentBefore().ifPresent(c -> {
					addCommentLines(c);
					fData.newline();
				}
		);
	}

	private void addCommentAfter(IOperation op) {
		if (!(op instanceof OperationImpl)) {
			return;
		}
		((OperationImpl) op).getCommentAfter().ifPresent(c -> {
					addCommentLines(c);
					fData.newline();
				}
		);
	}

	@Override
	public boolean visitNext(int i, IEntityIdList feldListe) {
		if (i < feldListe.getEntityIdList().size() - 1) {
			fData.appendNoBreak(", ");
		}
		return true;
	}

	@Override
	public void endVisit(IApplicationCondition bedTeil) {
		addCommentBefore(bedTeil);
		fData.append(mlh.getTerminalName(Base_ConditionLexer.APPLIKATIONS_BEDINGUNG) + " " + FeldIdUtil.quoteNameIfNecessary(
				bedTeil.getName(), mlh));
		addCommentAfter(bedTeil);
	}

	@Override
	public boolean visit(IEntityListCondition b) {
		addCommentBefore(b);
		fData.append(getEntityListenBedingungName(b));
		fData.appendNoBreak("(");
		fData.startModus(FormatData.FormatModus.ARG_LIST);
		return true;
	}

	private String getEntityListenBedingungName(IEntityListCondition b) {
		EntityListConditionName name = b.getConditionName();
		if (FeldIdUtil.referenceOneFieldorOneGroup(b)) {
			if (name == EntityListConditionName.AtLeastOneGroupFilled
					|| name == EntityListConditionName.AllGroupsFilled // Note: not allowed with single param as of 2023-11
			) {
				return mlh.getTerminalName(Base_ConditionLexer.KONTEXT_ANGEGEBEN);
			}
			if (name == EntityListConditionName.NoGroupFilled
					|| name == EntityListConditionName.NotAllGroupsFilled // Note: not allowed with single param as of 2023-11
			) {
				return mlh.getTerminalName(Base_ConditionLexer.KONTEXT_NICHT_ANGEGEBEN);
			}
		}
		if (FeldIdUtil.referenceOneField(b)) {
			if (name == EntityListConditionName.AtLeastOneFieldFilled
					|| name == EntityListConditionName.AllFieldsFilled // Note: not allowed with single param as of 2023-11
			) {
				return mlh.getTerminalName(Base_ConditionLexer.FELD_ANGEGEBEN);
			}
			if (name == EntityListConditionName.NoFieldFilled
					|| name == EntityListConditionName.NotAllFieldsFilled // Note: not allowed with single param as of 2023-11
				// we don't normalize NichtGenauEinFeldAngegeben that replacement might be unexpected to the user
			) {
				return mlh.getTerminalName(Base_ConditionLexer.FELD_NICHT_ANGEGEBEN);
			}
		}
		return mlh.getTerminalNameByGermanTerminalName(name.getRuntimeName());
	}

	@Override
	public void endVisit(IEntityListCondition bedTeil) {
		bedTeil.getAdditionalGroupReference().ifPresent(bt -> {
			this.appendFromToken();
			this.visit(bt);
		});
		fData.endModus();
		fData.appendNoBreak(")");
		addCommentAfter(bedTeil);
	}

	@Override
	public boolean visit(IEntityListAndValueListCondition bedTeil) {
		addCommentBefore(bedTeil);
		fData.append(mlh.getTerminalNameByGermanTerminalName(bedTeil.getConditionName().getRuntimeName()));
		fData.appendNoBreak("(");
		fData.startModus(FormatModus.ARG_LIST);
		return true;
	}

	@Override
	public boolean visitNext(int i, IEntityListAndValueListCondition bedTeil) {
		int opType = bedTeil.getConditionName() == EntityListAndValueListConditionName.ValueNotConsistent ?
				Base_ConditionLexer.ZU1 :
				Base_ConditionLexer.IN1;
		fData.append(String.format(" %s ", mlh.getTerminalName(opType)));
		// Only for pure entity lists do the traversal, else endVisit will create the parameters
		return bedTeil.getValueListAsStringList().isEmpty() && bedTeil.getValueListAsMixedList().isEmpty();
	}

	@Override
	public void endVisit(IEntityListAndValueListCondition bedTeil) {
		List<String> stringList = bedTeil.getValueListAsStringList();
		for (int i = 0; i < stringList.size(); i++) {
			fData.append(CommonUtils.addAndEscapeQuotes(stringList.get(i)));
			if (i < (stringList.size() - 1)) {
				fData.appendNoBreak(", ");
			}
		}
		List<Object> mixedList = bedTeil.getValueListAsMixedList();
		for (int i = 0; i < mixedList.size(); i++) {
			formatMixedValue(mixedList.get(i));
			if (i < (mixedList.size() - 1)) {
				fData.appendNoBreak(", ");
			}
		}
		fData.endModus();
		fData.appendNoBreak(")");
		addCommentAfter(bedTeil);
	}

	private void formatMixedValue(Object value) {
		if (value instanceof String strValue) {
			fData.append(CommonUtils.addAndEscapeQuotes(strValue));
		} else if (value instanceof IEntityId entityValue) {
			entityValue.traverse(this);
		} else {
			throw new IllegalArgumentException(
					"Unknown value " + value + " with class " + (value == null ? "null" : value.getClass()));
		}
	}

	@Override
	public boolean visit(IRootGroupCondition fb) {
		addCommentBefore(fb);
		String l = "";
		if (fb.getConditionName() != RootGroupConditionName.VordruckNichtFortlaufend && fb.getConditionName() != RootGroupConditionName.UsbNichtFortlaufend) {
			l = getLString(fb.getRootGroupIndex(), getMaxLfdNr(fb), isHmodel);
		}

		if (isHmodel) {
			fData.append(
					String.format("%s(%s%s%s)",
							getVordruckBedingungNameHDV(fb),
							FeldIdUtil.quoteNameIfNecessary(fb.getRootGroupName(), mlh), l, getUString(fb.getUsb())));
		} else {
			String formattedPath = EntityIdFormatterUtil.formatEntityIdForConditionTextA12DV(
					"/" + fb.getRootGroupName(), new long[] { fb.getRootGroupIndex() },
					fb.getEntityId().isOuter(),
					fb.getEntityId().getPathNotationConfig(),
					EntityUtils.getParentPath(ruleFullName),
					shortName -> false,
					mlh,
					false,
					false);
			fData.append(String.format("%s(%s", getRootGroupConditionNameA12DV(fb), formattedPath));
			if (fb.getEntityId() != null && fb.getEntityId().getSemanticIndex() != null && !fb.getEntityId().getSemanticIndex()
					.isEmpty()) {
				appendSemanticIndexToken();
				appendSemanticIndex(fb.getEntityId().getSemanticIndex());
			}
			fData.appendNoBreak(")");
		}
		addCommentAfter(fb);
		// Do not visit the entity
		return false;
	}

	private long getMaxLfdNr(IRootGroupCondition fb) {
		String vordruckFullName = EntityUtils.createRootGroupPath(fb.getRootGroupName());
		return ec.get(vordruckFullName).getMax()[0];
	}

	private String getUString(long usb) {
		return (usb == IParseTreeEntity.WILDCARD) ? "@*" : "";
	}

	private String getLString(long l, long max, boolean isHModel) {
		if (max == 1) {
			return "";
		}

		if (l == IParseTreeEntity.WILDCARD) {
			return isHModel ? "/*" : "*";
		}
		if (l > 0) {
			return "/" + l;
		}

		return "";
	}

	private String getVordruckBedingungNameHDV(IRootGroupCondition vb) {
		RootGroupConditionName name = vb.getConditionName();
		if (vb.getRootGroupIndex() != IParseTreeEntity.WILDCARD &&
				vb.getUsb() != IParseTreeEntity.WILDCARD) {
			if (name == RootGroupConditionName.KeinVordruckAngegeben) {
				return mlh.getTerminalName(Base_ConditionLexer.VORDRUCK_NICHT_ANGEGEBEN);
			}
			if (name == RootGroupConditionName.MindestensEinVordruckAngegeben) {
				return mlh.getTerminalName(Base_ConditionLexer.VORDRUCK_ANGEGEBEN);
			}
		}
		return mlh.getTerminalNameByGermanTerminalName(name.name());
	}

	/**
	 * Note: this method does a mix of formatting normalization and the reversal of the
	 * previous pre-processing replacement from
	 * KontextListenBedingungCreator#createRootGroupConditionNameFromContext:
	 * <ul>
	 *     <li>The Vordruck-Sprachkonstrukte are mapped back to regular Kontext-Sprachkonstrukte (Vordruck-Sprachkonstrukte are
	 *     not actually allowed in A12DV Condition texts)</li>
	 *     <li>Implicit formatting normalization: KeinVordruckAngegeben and MindestensEinVordruckAngegeben are normalized
	 *     to VordruckNichtAngegeben and VordruckAngegeben in the single-repetition (i.e. no-wildcard) case</li>
	 * </ul>
	 */
	private String getRootGroupConditionNameA12DV(IRootGroupCondition rc) {
		RootGroupConditionName name = rc.getConditionName();
		assert name == RootGroupConditionName.KeinVordruckAngegeben || name == RootGroupConditionName.MindestensEinVordruckAngegeben;

		if (rc.getRootGroupIndex() == IParseTreeEntity.WILDCARD) {
			if (name == RootGroupConditionName.KeinVordruckAngegeben) {
				return mlh.getTerminalName(Base_ConditionLexer.KEIN_KONTEXT_ANGEGEBEN);
			} else { // MindestensEinVordruckAngegeben
				return mlh.getTerminalName(Base_ConditionLexer.MINDESTENS_EIN_KONTEXT_ANGEGEBEN);
			}
		} else {
			if (name == RootGroupConditionName.KeinVordruckAngegeben) { // VordruckNichtAngegeben
				return mlh.getTerminalName(Base_ConditionLexer.KONTEXT_NICHT_ANGEGEBEN);
			} else { // VordruckAngegeben
				return mlh.getTerminalName(Base_ConditionLexer.KONTEXT_ANGEGEBEN);
			}
		}
	}

	@Override public boolean visit(IComparisonCondition p) {
		addCommentBefore(p);
		return true;
	}

	@Override
	public boolean visitNext(int i, IComparisonCondition bedTeil) {
		if (i == 1) {
			if (!fData.isCurLineBlank()) {
				fData.appendNoBreak(" ");
			}
			fData.append(FormatterUtil.getComparisonOperator(mlh, bedTeil.getCmpOp()) + " ");
		}
		return true;
	}

	@Override public void endVisit(IComparisonCondition p) {
		addCommentAfter(p);
	}

	@Override
	public boolean visit(IValueListCondition bedTeil) {
		addCommentBefore(bedTeil);
		fData.append(mlh.getTerminalNameByGermanTerminalName(bedTeil.getConditionName().getRuntimeName()));
		fData.append("(");
		bedTeil.getField().traverse(this);
		fData.appendNoBreak(", ");
		fData.startModus(FormatModus.ARG_LIST);
		for (int i = 0; i < bedTeil.getValueList().size(); i++) {
			bedTeil.getValueList().get(i).traverse(this);
			if (i < bedTeil.getValueList().size() - 1) {
				fData.appendNoBreak(", ");
			}
		}
		fData.endModus();
		fData.append(")");
		addCommentAfter(bedTeil);

		return false;
	}

	@Override
	public boolean visit(IDateValidityCondition vc) {
		addCommentBefore(vc);
		fData.append(mlh.getTerminalNameByGermanTerminalName(vc.getConditionName().getRuntimeName()));
		fData.append("(");
		fData.startModus(FormatModus.ARG_LIST);
		return true;
	}

	@Override
	public void endVisit(IDateValidityCondition vc) {
		fData.endModus();
		fData.append(")");
		addCommentAfter(vc);
	}

	@Override
	public boolean visit(IPredefinedTypeValidityCondition vc) {
		addCommentBefore(vc);
		fData.append(mlh.getTerminalNameByGermanTerminalName(vc.getConditionName().getRuntimeName()));
		fData.append("(");
		fData.startModus(FormatModus.ARG_LIST);
		return true;
	}

	@Override
	public void endVisit(IPredefinedTypeValidityCondition vc) {
		fData.appendNoBreak(", ");
		fData.append(CommonUtils.addAndEscapeQuotes(vc.getPredefinedTypeConstant()));
		fData.endModus();
		fData.append(")");
		addCommentAfter(vc);
	}

	@Override
	public boolean visit(ICompositeCondition bedTeil) {
		addCommentBefore(bedTeil);
		if (bedTeil.hasBrackets()) {
			fData.appendNoBreak("(");
			fData.startModus(FormatModus.KLAMMER);
		}
		fData.startModus(FormatModus.BEDINGUNG);
		return true;
	}

	@Override
	public boolean visitNext(int i, ICompositeCondition bedTeil) {
		if (i < bedTeil.getConditionList().size() - 1) {
			if (!fData.isCurLineBlank()) {
				fData.appendNoBreak(" ");
			}
			if (compact) {
				fData.append(mlh.getTerminalNameByGermanTerminalName(bedTeil.getLogOp().getRuntimeName()));
			} else {
				fData.appendWithBreak(mlh.getTerminalNameByGermanTerminalName(bedTeil.getLogOp().getRuntimeName()));
			}
			fData.appendNoBreak(" ");
		}
		return true;
	}

	@Override
	public void endVisit(ICompositeCondition bedTeil) {
		if (bedTeil.hasBrackets()) {
			fData.appendNoBreak(")");
			fData.endModus();
		}
		fData.endModus();
		addCommentAfter(bedTeil);
	}

	@Override
	public boolean visit(IEntityId feldId) {
		String name = getFormattedName(feldId);
		fData.append(name);
		if (feldId.getCategoryName() != null) {
			fData.appendNoBreak(EntityIdConstants.CATEGORY_VALUE_ACCESS);
			fData.appendNoBreak(FeldIdUtil.quoteNameIfNecessary(feldId.getCategoryName(), mlh));
		}
		if (feldId.getSemanticIndex() != null && !feldId.getSemanticIndex().isEmpty()) {
			appendSemanticIndexToken();
			appendSemanticIndex(feldId.getSemanticIndex());
		}
		if (feldId.getFilterCondition() != null) {
			fData.appendNoBreak(" ").appendNoBreak(mlh.getTerminalName(Base_ConditionLexer.MIT)).appendNoBreak(" ");
			fData.startModus(FormatModus.KLAMMER);
			feldId.getFilterCondition().traverse(this);
			fData.endModus();
		}
		// Do not visit semantic index again
		return false;
	}

	private void appendSemanticIndexToken() {
		fData.appendNoBreak(" ");
		fData.appendNoBreak(mlh.getTerminalNameByGermanTerminalName(EntityIdConstants.SEMANTIC_INDEX_TOKEN));
		fData.appendNoBreak(" ");
	}

	private void appendFromToken() {
		fData.appendNoBreak(" ");
		fData.appendNoBreak(mlh.getTerminalName(Base_ConditionLexer.VON));
		fData.appendNoBreak(" ");
	}

	private void appendSemanticIndex(List<Object> semIdx) {
		for (Object semIdxItem : semIdx) {
			if (semIdxItem instanceof String semIdxString) {
				fData.appendNoBreak(CommonUtils.addAndEscapeQuotes(semIdxString));
			} else {
				String semIdxName = getFormattedName((IEntityId) semIdxItem);
				fData.appendNoBreak(semIdxName);
			}
		}
	}

	private String getFormattedName(IEntityId entityId) {
		return EntityIdFormatterUtil.formatEntityIdForConditionText(
				entityId,
				EntityUtils.getParentPath(ruleFullName),
				shortName -> isPathReplaceableByShortName(entityId) && ec.shortName2FullName(shortName).size() == 1,
				mlh);
	}

	private boolean isPathReplaceableByShortName(IEntityId entityId) {
		return isFieldRefByShortNameAllowed && !isHmodel && entityId.isField()
				&& !EntityUtils.isWildCardDefinedInParent(entityId)
				&& !entityId.getPathNotationConfig().isLabeledAsRelativeUpwardReference();
	}

	@Override
	public void endVisit(INumberOfUsbOperation dummy) {
		addCommentBefore(dummy);
		fData.append(mlh.getTerminalName(Base_ConditionLexer.ANZAHL_ANGEGEBENE_USB));
		addCommentAfter(dummy);
	}

	@Override
	public void endVisit(INumberOfRootGroupsOperation feldOpTeil) {
		addCommentBefore(feldOpTeil);
		if (isHmodel) {
			fData.append(String.format("%s(%s)", mlh.getTerminalName(Base_ConditionLexer.ANZAHL_ANGEGEBENER_VORDRUCKE),
					FeldIdUtil.quoteNameIfNecessary(feldOpTeil.getRootGroupName(), mlh)));
		} else {
			fData.append(String.format("%s(/%s*)", mlh.getTerminalName(Base_ConditionLexer.ANZAHL_ANGEGEBENER_KONTEXTE),
					FeldIdUtil.quoteNameIfNecessary(feldOpTeil.getRootGroupName(), mlh)));
		}
		addCommentAfter(feldOpTeil);
	}

	@Override
	public void endVisit(IBoolConstantOperation feldOpTeil) {
		addCommentBefore(feldOpTeil);
		fData.append(mlh.getTerminalNameByGermanTerminalName(feldOpTeil.getValue().getRuntimeName()));
		addCommentAfter(feldOpTeil);
	}

	@Override
	public boolean visit(IDateDiffOperation feldOpTeil) {
		addCommentBefore(feldOpTeil);
		fData.append(mlh.getTerminalNameByGermanTerminalName(feldOpTeil.getOperatorName().getRuntimeName()));
		fData.appendNoBreak("(");
		fData.startModus(FormatModus.ARG_LIST);
		feldWertNeedsBrackets.push(Boolean.FALSE);
		return true;
	}

	@Override
	public boolean visitNext(int i, IDateDiffOperation feldOpTeil) {
		fData.appendNoBreak(", ");
		return true;
	}

	@Override
	public void endVisit(IDateDiffOperation feldOpTeil) {
		fData.endModus();
		fData.appendNoBreak(")");
		feldWertNeedsBrackets.pop();
		addCommentAfter(feldOpTeil);
	}

	@Override
	public boolean visit(IDateAddOperation feldOpTeil) {
		addCommentBefore(feldOpTeil);
		fData.append(mlh.getTerminalNameByGermanTerminalName(feldOpTeil.getOperatorName().getRuntimeName()));
		fData.appendNoBreak("(");
		fData.startModus(FormatModus.ARG_LIST);
		feldWertNeedsBrackets.push(Boolean.FALSE);
		return true;
	}

	@Override
	public boolean visitNext(int i, IDateAddOperation feldOpTeil) {
		fData.appendNoBreak(", ");
		feldWertNeedsBrackets.pop();
		feldWertNeedsBrackets.push(Boolean.TRUE);
		return true;
	}

	@Override
	public void endVisit(IDateAddOperation feldOpTeil) {
		fData.endModus();
		fData.appendNoBreak(")");
		feldWertNeedsBrackets.pop();
		addCommentAfter(feldOpTeil);
	}

	@Override
	public boolean visit(IDateExtractionOperation feldOpTeil) {
		addCommentBefore(feldOpTeil);
		fData.append(mlh.getTerminalNameByGermanTerminalName(feldOpTeil.getOperatorName().getRuntimeName()));
		fData.appendNoBreak("(");
		feldWertNeedsBrackets.push(Boolean.FALSE);
		return true;
	}

	@Override
	public void endVisit(IDateExtractionOperation feldOpTeil) {
		fData.appendNoBreak(")");
		feldWertNeedsBrackets.pop();
		addCommentAfter(feldOpTeil);
	}

	@Override
	public boolean visit(IFieldIdOperation feldOpTeil) {
		addCommentBefore(feldOpTeil);
		if (feldOpTeil.getOperatorName() == FieldIdOperationName.FieldValue) {
			if (feldWertNeedsBrackets.peek()) {
				fData.append("[");
			}
			// Modus erst nach '[' setzen, denn ansonsten wird LineBreak verhindert
			fData.startModus(FormatModus.BLOCK);
		} else {
			fData.startModus(FormatModus.BLOCK);
			fData.append(mlh.getTerminalNameByGermanTerminalName(feldOpTeil.getOperatorName().getRuntimeName()));
			fData.append("(");
		}
		return true;
	}

	@Override
	public void endVisit(IFieldIdOperation feldOpTeil) {
		if (feldOpTeil.getOperatorName() == FieldIdOperationName.FieldValue) {
			if (feldWertNeedsBrackets.peek()) {
				fData.append("]");
			}
		} else {
			fData.append(")");
		}
		fData.endModus();
		addCommentAfter(feldOpTeil);
	}

	@Override
	public boolean visit(IGroupOperation groupOp) {
		addCommentBefore(groupOp);
		fData.startModus(FormatModus.BLOCK);
		fData.append(mlh.getTerminalNameByGermanTerminalName(groupOp.getOperatorName().getRuntimeName()));
		fData.append("(");
		return true;
	}

	@Override
	public void endVisit(IGroupOperation groupOp) {
		fData.append(")");
		fData.endModus();
		addCommentAfter(groupOp);
	}

	@Override
	public boolean visit(IEntityListOperation feldOpTeil) {
		addCommentBefore(feldOpTeil);
		fData.append(mlh.getTerminalNameByGermanTerminalName(feldOpTeil.getOperatorName().getRuntimeName()));
		fData.append("(");
		fData.startModus(FormatModus.ARG_LIST);
		return true;
	}

	@Override
	public void endVisit(IEntityListOperation feldOpTeil) {
		fData.append(")");
		fData.endModus();
		addCommentAfter(feldOpTeil);
	}

	@Override
	public boolean visit(ISetOpOperation setOp) {
		addCommentBefore(setOp);
		fData.append(mlh.getTerminalNameByGermanTerminalName(setOp.getOperatorName().getRuntimeName()));
		fData.append("(");
		fData.startModus(FormatModus.ARG_LIST);
		return true;
	}

	@Override public boolean visitNext(int i, ISetOpOperation p) {
		fData.appendNoBreak(", ");
		return true;
	}

	@Override public void endVisit(ISetOpOperation setOp) {
		fData.append(")");
		fData.endModus();
		addCommentAfter(setOp);
	}

	@Override
	public boolean visit(IEntityListOperationWithValue feldOpTeil) {
		addCommentBefore(feldOpTeil);
		fData.append(mlh.getTerminalNameByGermanTerminalName(feldOpTeil.getOperatorName().getRuntimeName()));
		fData.append("(");
		fData.startModus(FormatModus.ARG_LIST);
		return true;
	}

	@Override
	public boolean visitNext(int i, IEntityListOperationWithValue bedTeil) {
		fData.append(String.format(" %s ", mlh.getTerminalName(Base_ConditionLexer.IN1)));
		return true;
	}

	@Override
	public void endVisit(IEntityListOperationWithValue feldOpTeil) {
		fData.endModus();
		fData.append(")");
		addCommentAfter(feldOpTeil);
	}

	@Override
	public boolean visit(IOpListOperation feldOpTeil) {
		addCommentBefore(feldOpTeil);
		fData.startModus(FormatModus.ARG_LIST);
		fData.append(mlh.getTerminalNameByGermanTerminalName(feldOpTeil.getOperatorName().getRuntimeName()));
		fData.appendNoBreak("(");
		fData.endModus();
		fData.startModus(FormatModus.ARG_LIST);
		return true;
	}

	@Override
	public boolean visitNext(int i, IOpListOperation feldOpTeil) {
		if (i < feldOpTeil.getOperations().size() - 1) {
			fData.appendNoBreak(", ");
		}
		return true;
	}

	@Override
	public void endVisit(IOpListOperation feldOpTeil) {
		fData.endModus();
		fData.appendNoBreak(")");
		addCommentAfter(feldOpTeil);
	}

	@Override
	public boolean visit(IOpListOperationWithParameter feldOpTeil) {
		addCommentBefore(feldOpTeil);
		fData.startModus(FormatModus.ARG_LIST);
		fData.append(mlh.getTerminalNameByGermanTerminalName(feldOpTeil.getOperatorName().getRuntimeName()));
		fData.appendNoBreak("(");
		fData.endModus();
		fData.startModus(FormatModus.ARG_LIST);
		return true;
	}

	@Override
	public boolean visitNext(int i, IOpListOperationWithParameter feldOpTeil) {
		if (i < feldOpTeil.getOperations().size() - 1) {
			fData.appendNoBreak(", ");
		}
		return true;
	}

	@Override
	public void endVisit(IOpListOperationWithParameter feldOpTeil) {
		//Für Stellenanzahl beim Runden falls eingegeben
		if (feldOpTeil.getNumberOfDecimalPlaces() > 0) {
			fData.appendNoBreak(", ");
			fData.append(String.valueOf(feldOpTeil.getNumberOfDecimalPlaces()));
		}
		fData.endModus();
		fData.appendNoBreak(")");
		addCommentAfter(feldOpTeil);
	}

	@Override
	public void endVisit(IConstantOperation feldOpTeil) {
		addCommentBefore(feldOpTeil);
		if (feldOpTeil.getType() == OperationType.NUMBER) {
			fData.append(feldOpTeil.getValue());
		} else {
			fData.append(CommonUtils.addAndEscapeQuotes(feldOpTeil.getValue()));
		}
		addCommentAfter(feldOpTeil);
	}

	@Override
	public void endVisit(IPointInTimeOperation feldOpTeil) {
		addCommentBefore(feldOpTeil);
		fData.append(mlh.getTerminalNameByGermanTerminalName(feldOpTeil.getOperatorName().getRuntimeName()));
		addCommentAfter(feldOpTeil);
	}

	@Override
	public boolean visit(ICompositeOperation feldOpTeil) {
		addCommentBefore(feldOpTeil);
		if (feldOpTeil.hasBrackets()) {
			fData.appendNoBreak("{");
			fData.startModus(FormatModus.KLAMMER);
		}
		return true;
	}

	@Override
	public boolean visitNext(int i, ICompositeOperation feldOpTeil) {
		if (i < feldOpTeil.getOperations().size() - 1) {
			if (!fData.isCurLineBlank()) {
				fData.appendNoBreak(" ");
			}
			fData.appendNoBreak(FormatterUtil.getCalcOperator(feldOpTeil.getCalcOp()));
			fData.appendNoBreak(" ");
		}
		return true;
	}

	@Override
	public void endVisit(ICompositeOperation feldOpTeil) {
		if (feldOpTeil.hasBrackets()) {
			fData.appendNoBreak("}");
			fData.endModus();
		}
		addCommentAfter(feldOpTeil);
	}

	@Override
	public boolean visit(IDateConstructionOperation datumAngabeTeil) {
		addCommentBefore(datumAngabeTeil);
		feldWertNeedsBrackets.push(Boolean.FALSE);
		fData.append(String.format("%s(", mlh.getTerminalName(Base_ConditionLexer.DATUM)));
		fData.startModus(FormatModus.ARG_LIST);
		datumAngabeTeil.getDay().traverse(this);
		fData.appendNoBreak(", ");
		datumAngabeTeil.getMonth().traverse(this);
		if (datumAngabeTeil.getYear1() != null) {
			fData.appendNoBreak(", ");
			datumAngabeTeil.getYear1().traverse(this);
		}
		if (datumAngabeTeil.getYear2() != null) {
			fData.appendNoBreak(", ");
			datumAngabeTeil.getYear2().traverse(this);
		}
		fData.endModus();
		fData.appendNoBreak(")");
		feldWertNeedsBrackets.pop();
		addCommentAfter(datumAngabeTeil);
		return false;
	}

	@Override
	public boolean visit(IDateTimeConstructionOperation dateTimeAngabe) {
		addCommentBefore(dateTimeAngabe);
		feldWertNeedsBrackets.push(Boolean.FALSE);
		fData.append(String.format("%s(", mlh.getTerminalName(Base_ConditionLexer.DATETIME)));
		fData.startModus(FormatModus.ARG_LIST);
		dateTimeAngabe.getDate().traverse(this);
		fData.appendNoBreak(", ");
		dateTimeAngabe.getTime().traverse(this);
		fData.endModus();
		fData.appendNoBreak(")");
		feldWertNeedsBrackets.pop();
		addCommentAfter(dateTimeAngabe);
		return false;
	}

	@Override
	public boolean visit(ITimeConstructionOperation timeSpec) {
		addCommentBefore(timeSpec);
		feldWertNeedsBrackets.push(Boolean.FALSE);
		fData.append(String.format("%s(", mlh.getTerminalName(Base_ConditionLexer.TIME)));
		fData.startModus(FormatModus.ARG_LIST);
		if (timeSpec.getHour() != null) {
			timeSpec.getHour().traverse(this);
		}
		if (timeSpec.getMinute() != null) {
			fData.appendNoBreak(", ");
			timeSpec.getMinute().traverse(this);
		}
		if (timeSpec.getSecond() != null) {
			fData.appendNoBreak(", ");
			timeSpec.getSecond().traverse(this);
		}
		fData.endModus();
		fData.appendNoBreak(")");
		feldWertNeedsBrackets.pop();
		addCommentAfter(timeSpec);
		return false;
	}

	@Override
	public boolean visit(IDateRangeConstructionOperation dateRangeConstruct) {
		addCommentBefore(dateRangeConstruct);
		feldWertNeedsBrackets.push(Boolean.FALSE);
		fData.append(String.format("%s(", mlh.getTerminalName(Base_ConditionLexer.DATUM_BEREICH)));
		fData.startModus(FormatModus.ARG_LIST);
		dateRangeConstruct.getDate1Id().traverse(this);
		fData.appendNoBreak(", ");
		dateRangeConstruct.getDate2Id().traverse(this);
		fData.endModus();
		fData.appendNoBreak(")");
		feldWertNeedsBrackets.pop();
		addCommentAfter(dateRangeConstruct);
		return false;
	}

	@Override
	public boolean visit(IDateRangeExtractionOperation datumAngabeTeil) {
		addCommentBefore(datumAngabeTeil);
		fData.append(mlh.getTerminalNameByGermanTerminalName(datumAngabeTeil.getOperatorName().getRuntimeName()));
		fData.appendNoBreak("(");

		IOperation op = datumAngabeTeil.getDateRangeSpec();
		if (op instanceof IFieldIdOperation fOp) {
			fOp.getField().traverse(this);
		} else {
			op.traverse(this);
		}
		fData.appendNoBreak(")");
		addCommentAfter(datumAngabeTeil);
		return false;
	}

	@Override
	public boolean visit(IFieldIdOperationWithParameters feldOpTeil) {
		addCommentBefore(feldOpTeil);
		fData.append(mlh.getTerminalNameByGermanTerminalName(feldOpTeil.getOperatorName().getRuntimeName()));
		fData.append("(");
		return true;
	}

	@Override
	public void endVisit(IFieldIdOperationWithParameters feldOpTeil) {
		FieldIdOperationName name = feldOpTeil.getOperatorName();
		if (name == FieldIdOperationName.ValueAsDate) {
			fData.appendNoBreak(", ");
			fData.append(feldOpTeil.isFirstDay() ?
					mlh.getTerminalName(Base_ConditionLexer.ERSTER_TAG) :
					mlh.getTerminalName(Base_ConditionLexer.LETZTER_TAG));
		} else if (name == FieldIdOperationName.RangeAsString || name == FieldIdOperationName.RangeAsNumber) {
			// bereichStart ist um 1 verringert. Das müssen wir ausgleichen
			fData.appendNoBreak(", ");
			fData.append(Integer.toString(feldOpTeil.getRangeStart() + 1));
			fData.appendNoBreak(", ");
			fData.append(Integer.toString(feldOpTeil.getRangeEnd()));
		} else if (feldOpTeil.getNumberOfDecimalPlaces() > 0) {
			//Für Stellenanzahl beim Runden falls eingegeben
			fData.appendNoBreak(", ");
			fData.append(String.valueOf(feldOpTeil.getNumberOfDecimalPlaces()));
		}

		fData.append(")");
		addCommentAfter(feldOpTeil);
	}

}
