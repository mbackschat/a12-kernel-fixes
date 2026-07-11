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
package com.mgmtp.a12.kernel.core.parser.internal.grammar.gen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.IExtmpModelSpecificSettings;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.bedingung.CheckPreambleLenientImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.CheckEntitySpecLenientImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.CheckIdNameRefImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.CheckRegelKontextLenientEntityImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.CheckRelativeUpImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.entity.semanticIndex.CheckSemanticLenientIndexImpl;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.parser.internal.error.ParserErrorHandler;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRef;
import com.mgmtp.a12.kernel.core.parser.internal.util.EntityRefMap;
import com.mgmtp.a12.kernel.core.parser.internal.util.OperationResultTypeMap;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.config.IProjectSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

/**
 * Parser implementation that omits most of the checks done in {@link FullChecks_ConditionParser}, but is still able to create an
 * {@link com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition} from the parsed rule.
 * <p>
 * It was created for handling the mapping precomputation fragment and similar scenarios where additive models that are incomplete
 * have to be handled. The created {@link com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition} may contain
 * non-existent
 * {@link com.mgmtp.a12.kernel.core.parser.internal.util.IVirtual} fields or groups that might be handled as fields AND groups
 * (they
 * might return true for both {@link IEntity#isField()} and {@link IEntity#isGroup()}).
 * </p>
 * ATTENTION: If you use this parser, make sure to test the places where the resulting
 * {@link com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition} is used thoroughly. The resulting ParseTree is
 * at the creation time primarily tested by executing the formatter tests for it and check against the expectations for the normal
 * parser.
 */
public class Lenient_ConditionParser extends GrammarOnly_ConditionParser {
	private IRule rule;
	private IEC ec;
	private IProblemReporter pm;
	private IProjectSpecificSettings pss;
	private Locale languageUsedInConditionText;
	private boolean isFieldRefByShortnameAllowed;
	private boolean hasBaseYear;
	private ParserErrorHandler errorHandler;
	private final HashSet<String> suppressedWarnings = new LinkedHashSet<>();

	private final EntityRefMap entityMap = new EntityRefMap();

	private ParseTree parseTree;

	private IEntity errorEntity;

	private boolean inSetExpression;

	public EntityRefMap getEntityMap() {
		return entityMap;
	}

	private final OperationResultTypeMap operationResultTypeMap = new OperationResultTypeMap();

	public Lenient_ConditionParser(TokenStream tokens) {
		super(tokens);
	}

	/**
	 * Init the parser for parsing a rule.
	 *
	 * @param rule
	 * 		only relevant for rule conditions
	 * @param modelSpecificSettings
	 * 		only relevant for rule conditions
	 * @param ec
	 * 		only relevant for rule conditions
	 * @param pss
	 * @param pm
	 */
	public void initParser(IRule rule, IModelSpecificSettings modelSpecificSettings, IEC ec, IProjectSpecificSettings pss,
			IProblemReporter pm) {

		boolean isExtmp = ExtmpUtils.isExtmp(modelSpecificSettings);
		boolean isHModel = isExtmp && ((IExtmpModelSpecificSettings) modelSpecificSettings).isHModel();
		if (isLenient() && isHModel) {
			throw new IllegalArgumentException("Lenient parsing not possible for H-Models.");
		}

		super.initParser(isExtmp, isHModel, this::isGroupFromHModelTest);

		this.rule = rule;
		this.errorEntity = rule != null ? rule.getErrorEntity() : null;
		this.ec = ec;
		this.pm = pm;
		this.pss = pss;
		this.languageUsedInConditionText = modelSpecificSettings.getLanguageUsedInConditionText();
		this.isFieldRefByShortnameAllowed = modelSpecificSettings.isFieldRefByShortNameAllowed();
		this.hasBaseYear = modelSpecificSettings.getBaseYear().isPresent();
		this.errorHandler = new ParserErrorHandler(languageUsedInConditionText);
	}

	public ParserErrorHandler getMVKErrorHandler() {
		return errorHandler;
	}

	private boolean isGroupFromHModelTest(ParserRuleContext parserContext) {
		EntityRef ref = getEntityRef(parserContext.getParent());
		IEntity e = ref.getEntity();
		return e.isGroup();
	}

	public IRule getRule() {
		return rule;
	}

	public IEntity getErrorEntity() {
		return errorEntity;
	}

	public IEntity setErrorEntity(IEntity errorEntity) {
		IEntity result = this.errorEntity;
		this.errorEntity = errorEntity;
		return result;
	}

	public IEntity resetErrorEntity() {
		IEntity result = this.errorEntity;
		this.errorEntity = rule.getErrorEntity();
		return result;
	}

	public IEC getEc() {
		return ec;
	}

	public IProblemReporter getPm() {
		return pm;
	}

	public IProjectSpecificSettings getPss() {
		return pss;
	}

	@Override protected void checkIdNameRef(IdNameRefContext ctx) {
		CheckIdNameRefImpl checker = new CheckIdNameRefImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkRegelKontextEntity(RegelKontextEntityContext ctx) {
		CheckRegelKontextLenientEntityImpl checker = new CheckRegelKontextLenientEntityImpl(this);
		checker.check(ctx);
	}

	public EntityRef getEntityRef(ParserRuleContext ctx) {
		return entityMap.getEntityRef(ctx);
	}

	public EntityRef putEntityToMapHDVOnly(ParserRuleContext ctx, IEntity e, String nameUsedInConditionText) {
		// for h-dv only cases, we don't care about preserving the path notation type with PathNotationConfig
		return putEntityToMap(ctx, e, nameUsedInConditionText, PathNotationConfig.IRRELEVANT); // we don't care about the path
		// notation type for H-DVs (it was not preserved before this code change and there is no need to improve this for H-DVs)
	}

	public EntityRef putEntityToMap(ParserRuleContext ctx, IEntity e, String nameUsedInConditionText,
			PathNotationConfig pathNotationConfig) {
		EntityRef result = entityMap.putEntity(ctx, e, nameUsedInConditionText, isHModel());
		result.setInSetExpression(inSetExpression);
		result.setPathNotationConfig(pathNotationConfig);
		return result;
	}

	public OperationResultTypeMap getOperationResultTypeMap() {
		return operationResultTypeMap;
	}

	@Override protected void checkEntitySpec(EntitySpecContext ctx) {
		CheckEntitySpecLenientImpl checker = new CheckEntitySpecLenientImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkSemanticIndex(SemanticIndexContext ctx) {
		CheckSemanticLenientIndexImpl checker = new CheckSemanticLenientIndexImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkRelativeUp(RelativeUpContext ctx) {
		CheckRelativeUpImpl checker = new CheckRelativeUpImpl(this);
		checker.check(ctx);
	}

	@Override protected void checkPreamble(PreambleContext ctx) {
		CheckPreambleLenientImpl checker = new CheckPreambleLenientImpl(this);
		checker.check(ctx);
	}

	public EntityRef createEntryForEntity(ParserRuleContext ectx, IEntity e, String nameUsedInConditionText,
			PathNotationConfig pathNotationConfig) {
		EntityRef ref = putEntityToMap(ectx, e, nameUsedInConditionText, pathNotationConfig);

		if (isA12Model()) {
			//add indices except the index for the current level, as this index may be a wildcard.
			//thus index for the current level is added later on.
			List<Long> arr = new ArrayList<>();
			long[] max = EntityUtils.getMaxInA12Model(e);
			for (int i = 0; i < max.length - 1; i++) {
				long ind = max[i] > 1 ? IParseTreeEntity.ITERATION : 1L;
				arr.add(ind);
			}
			ref.setIndices(arr);
		}
		return ref;
	}

	public boolean hasBaseYear() {
		return hasBaseYear;
	}

	public ParseTree getParseTree() {
		return parseTree;
	}

	public void setParseTree(ParseTree parseTree) {
		this.parseTree = parseTree;
	}

	public void addSuppressedWarning(String warning) {
		this.suppressedWarnings.add(warning);
	}

	public boolean isWarningSuppressed(String warning) {
		return suppressedWarnings.contains(warning);
	}

	public Set<String> getSuppressedWarnings() {
		return Collections.unmodifiableSet(suppressedWarnings);
	}

	@Override
	protected void resetSetExpression(EntitySpecContext entitySpecCtx) {
		this.resetErrorEntity();
		// We don't allow filter conditions and set operations intermixed
		this.inSetExpression = false;
	}

	@Override
	protected void initSetExpression(EntitySpecContext ctx) {
		EntityRef entityRef = this.entityMap.getEntityRef(ctx);
		this.setErrorEntity(entityRef.getEntity());
		this.inSetExpression = true;
	}

	public boolean isLenient() {
		return true;
	}

	/**
	 * @return <code>true</code>, iff the parser is in a state where he parses a filter condition or a set operation (SumOfTerms).
	 */
	public boolean isInSetExpression() {
		return inSetExpression;
	}

	public Locale getLanguageUsedInConditionText() {
		return languageUsedInConditionText;
	}

	public boolean isFieldRefByShortNameAllowed() {
		return isFieldRefByShortnameAllowed;
	}
}