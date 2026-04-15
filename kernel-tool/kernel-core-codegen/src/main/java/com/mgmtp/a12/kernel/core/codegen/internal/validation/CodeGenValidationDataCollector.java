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

package com.mgmtp.a12.kernel.core.codegen.internal.validation;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenConstants;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils.ExtmpUtils;
import com.mgmtp.a12.kernel.core.parser.internal.check.util.EntityUtils;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IGroup;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule.RuleSeverityType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ISortField;
import com.mgmtp.a12.model.header.Annotation;

/**
 * Baseclass for filling an {@link IData}-structure with data used for code generation for
 * validation purposes. The data consists of the {@link IEC}, {@link IModelSpecificSettings}, etc.
 */
public abstract class CodeGenValidationDataCollector {
	private final IData data;
	private final IModelSpecificSettings modelSpecificSettings;
	private final IEC ec;
	private final Collection<? extends IRule> rules;

	public CodeGenValidationDataCollector(IData data, IEC ec, IModelSpecificSettings modelSpecificSettings,
			Collection<? extends IRule> rules) {
		this.data = data;
		this.modelSpecificSettings = modelSpecificSettings;
		this.ec = ec;
		this.rules = rules;
	}

	public void fillData() {
		addDataFromModelSpecificSettings();
		addDataFromEC();
		addRules();
		addAdditionalModelData();
	}

	protected void addValue(String id, String value) {
		DataCollectorUtils.addValue(data, id, value);
	}

	protected void addDataFromModelSpecificSettings() {
		boolean isHModel = ExtmpUtils.isHModel(modelSpecificSettings);
		DataCollectorUtils.addIsOldWorld(data, isHModel);

		addAnnotations(DataCollectorUtils.PATH_PREFIX_MODELPROPERTIES, modelSpecificSettings.getAnnotations());
	}

	protected void addDataFromEC() {
		List<IGroup> rootGroups = EntityUtils.getRootGroups(ec);
		addRootGroups(rootGroups);

		List<IGroup> groups = EntityUtils.getAllInnerGroups(ec);
		addGroups(groups);

		List<IField> fields = EntityUtils.getAllFields(ec);
		addFields(fields);

		List<String> langStrs = getUsedLanguages().stream()
				.map(getLocaleToLangStrFunction())
				.collect(Collectors.toList());
		DataCollectorUtils.addSupportedLanguages(data, langStrs);
	}

	private void addFields(List<IField> fields) {
		Java8Utils.forEachIndexed(fields, (i, field) -> addField(i + 1, field));
	}

	protected void addField(int index, IField field) {
		new FieldDataCollector(data, getUsedLanguages(), getLocaleToLangStrFunction()).addField(index, field);
	}

	private void addRootGroups(List<IGroup> groups) {
		AtomicInteger i = new AtomicInteger();
		groups.forEach(g -> addRootGroup(i.incrementAndGet(), g));
	}

	private void addRootGroup(int idx, IGroup rootGroup) {
		String idPrefix = String.format("/MVK[1]/Model[1]/Rootgroups[%d]/", idx);

		String name = EntityUtils.getRootGroupName(rootGroup.getFullName());
		long maxRepetitions = EntityUtils.getMaxLfdNr(rootGroup);

		addValue(idPrefix + "name[1]", name);
		addValue(idPrefix + "maxRepetitions[1]", String.valueOf(maxRepetitions));

		addIndexFields(idPrefix, rootGroup);
		addSortFields(idPrefix, rootGroup);

		addAnnotations(idPrefix, rootGroup.getAnnotations());

		addRootGroupAdditions(idPrefix, rootGroup);
	}

	private void addIndexFields(String parentPrefix, IGroup group) {
		AtomicInteger i = new AtomicInteger();
		group.getIndexFieldNames().forEach(idxField -> addIndexField(parentPrefix, i.incrementAndGet(), group, idxField));
	}

	private void addIndexField(String parentPrefix, int idx, IGroup group, String indexField) {
		String idPrefix = parentPrefix + String.format("Index[1]/IndexFields[%d]/", idx);

		String groupName = group.getFullName();
		String value;
		if (indexField.startsWith(groupName)) {
			// The field name itself should be used in the meta model
			value = indexField.substring(groupName.length());
		} else {
			value = indexField;
		}

		addValue(idPrefix + "indexField[1]", value);
	}

	private void addSortFields(String parentPrefix, IGroup group) {
		AtomicInteger i = new AtomicInteger();
		List<ISortField> sortFields = group.getSortInfo().get(CodeGenConstants.SORT_CATEGORY_DEFAULT);
		if (sortFields != null) {
			sortFields.forEach(sortField -> addSortField(parentPrefix, i.incrementAndGet(), sortField));
		}
	}

	private void addSortField(String parentPrefix, int idx, ISortField sortField) {
		String idPrefix = parentPrefix + String.format("SortInfo[1]/SortFields[%d]/", idx);

		addValue(idPrefix + "sortField[1]", sortField.getName());
		addValue(idPrefix + "sortType[1]", ValidationConverter.convertSortOrder(sortField.getSortType()));
		addValue(idPrefix + "sortPrio[1]", String.valueOf(sortField.getPosition()));
	}

	private void addGroups(List<IGroup> groups) {
		AtomicInteger i = new AtomicInteger();
		groups.forEach(g -> addGroup(i.incrementAndGet(), g));
	}

	private void addGroup(int idx, IGroup group) {
		String idPrefix = String.format("/MVK[1]/Model[1]/Groups[%d]/", idx);

		String name = group.getFullName();
		long maxRepetitions = EntityUtils.getMax(group);

		addValue(idPrefix + "name[1]", name);
		addValue(idPrefix + "maxRepetitions[1]", String.valueOf(maxRepetitions));

		addIndexFields(idPrefix, group);
		addSortFields(idPrefix, group);

		addAnnotations(idPrefix, group.getAnnotations());
	}

	private void addRules() {
		AtomicInteger i = new AtomicInteger();
		rules.forEach(r -> addRule(i.incrementAndGet(), r));
	}

	protected void addRule(int idx, IRule r) {
		String idPrefix = String.format("/MVK/Rules[%d]/", idx);

		String group = EntityUtils.getParentPath(r.getFullName());
		String name = EntityUtils.getShortName(r.getFullName());
		String errorCode = r.getErrorCode();
		String errorCondition = r.getErrorConditionText();
		Optional<String> errorEntityName = Optional.ofNullable(r.getErrorEntity()).map(IEntity::getFullName);
		if (ExtmpUtils.isHModel(modelSpecificSettings)) {
			errorEntityName = errorEntityName.map(EntityUtils::getShortName);
		} else if (errorEntityName.orElse("").startsWith(group)) {
			errorEntityName = errorEntityName.map(ef -> ef.substring(group.length() + 1));
		}
		RuleSeverityType severity = r.getSeverityType();

		addValue(idPrefix + "group", group);
		addValue(idPrefix + "name", name);
		addValue(idPrefix + "errorCode", errorCode);
		addValue(idPrefix + "errorCondition", errorCondition);
		addValue(idPrefix + "errorField", errorEntityName.orElse(null));
		addValue(idPrefix + "severity", ValidationConverter.convertSeverityType(severity));

		String errorIdPattern = idPrefix + "ErrorMessages[%d]/";
		AtomicInteger i = new AtomicInteger();
		getUsedLanguages().forEach(l -> DataCollectorUtils.addErrorMessage(data, String.format(errorIdPattern, i.incrementAndGet()),
				getLocaleToLangStrFunction().apply(l), r.getErrorMessage(l)));

		addAnnotations(idPrefix, r.getAnnotations());

		Java8Utils.forEachIndexed(
				r.getCustomRefErrorEntitiesIfFilled(),
				(l, e) -> addValue(idPrefix + "CustomRefErrorEntities/CustomRefErrorEntityIfFilled[" + (l + 1) + "]/name", e)
		);
		Java8Utils.forEachIndexed(
				r.getCustomRefErrorEntitiesIfNotFilled(),
				(l, e) -> addValue(idPrefix + "CustomRefErrorEntities/CustomRefErrorEntityIfNotFilled[" + (l + 1) + "]/name", e)
		);
	}

	private void addAnnotations(String parentPrefix, List<Annotation> annotations) {
		DataCollectorUtils.addAnnotations(data, parentPrefix, annotations);
	}

	public IData getData() {
		return data;
	}

	protected abstract Function<Locale, String> getLocaleToLangStrFunction();

	/**
	 * @return the locales that should be used for code generation.
	 */
	protected abstract Collection<Locale> getUsedLanguages();

	/**
	 * Fills the data with the additional model parameters specific to the individual collector.
	 */
	protected abstract void addAdditionalModelData();

	/**
	 * Adds additional root group parameters.
	 *
	 * @param idPrefix
	 * 		the name of the "Rootgroups" group in the meta model.
	 * @param rootGroup
	 * 		the root group data.
	 */
	protected abstract void addRootGroupAdditions(String idPrefix, IGroup rootGroup);
}
