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

package com.mgmtp.a12.kernel.md.model.a12internal.services;

import static com.mgmtp.a12.kernel.md.model.internal.service.utils.ConsistencyCheckUtils.problemReporterToNotificationReceiver;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.facade.a12internal.ServiceFactory;
import com.mgmtp.a12.kernel.core.facade.internal.MVKService;
import com.mgmtp.a12.kernel.core.parser.internal.ParserService;
import com.mgmtp.a12.kernel.core.parser.internal.grammar.gen.Lenient_ConditionParser;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEC;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IBaseCalculationService.ICalculationRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.ICalculationService;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IConditionFormatter;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IMVK_Service;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IRuleConsistencyCheck;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IServiceConfiguration;
import com.mgmtp.a12.kernel.core.utils.internal.MVKCoreError;
import com.mgmtp.a12.kernel.core.utils.internal.PatternUtils;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporter;
import com.mgmtp.a12.kernel.core.utils.internal.ProblemReporterProxy;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentUniquenessCriterion;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.ExternalDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.consistency.rules.DocumentUniquenessCriteriaRule;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.IRuleFromComputation;
import com.mgmtp.a12.kernel.md.model.internal.service.reduction.DocumentModelReduceService;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelServiceUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.WrappedProblemWithSeverityLimitAndOrigin;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentUniquenessCriterionWrapper;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.ExternalDocumentModelSerializerService;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * This service offers actions which can be run on a {@link DocumentModel}.
 */
@OnlyForUsage
public class DocumentModelService {

	// ==================================
	// utilities
	// ==================================

	/**
	 * For more information see {@link IMVK_Service#getApplicationConditionNames(Collection, IProblemReporter)}.
	 */
	public @NonNull List<String> getApplicationConditionNames(
			@NonNull IMVK_Service imvkService, @NonNull Collection<Rule> rules, @NonNull IProblemReporter pr) {
		List<IRule> irules = rules.stream().map(rule -> findIRule(imvkService, rule)).toList();
		return imvkService.getApplicationConditionNames(irules, pr);
	}

	/**
	 * For more information see {@link IMVK_Service#getDirectlyReferencedEntities(IRule rule, IProblemReporter pr)}.
	 */
	public Set<IEntity> getDirectlyReferencedEntities(
			@NonNull IMVK_Service imvkService, @NonNull Rule rule, @NonNull IProblemReporter pr) {
		return imvkService.getDirectlyReferencedEntities(findIRule(imvkService, rule), pr);
	}

	/**
	 * For more information see {@link IMVK_Service#getDirectlyReferencedEntities(IRule rule, IProblemReporter pr)}.
	 */
	public Set<IEntity> getDirectlyReferencedEntities(IMVK_Service imvkService, Computation computation, IProblemReporter pr) {
		return imvkService.getDirectlyReferencedEntities(findIRuleForComputation(imvkService, computation), pr);
	}

	/**
	 * Converts the given {@link DocumentModel} into the external counterpart.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} to be converted. May not be {@code null}.
	 * @return the corresponding {@link IDocumentModel}.
	 */
	public IDocumentModel convertToExternal(DocumentModel documentModel) {
		return new DocumentModelWrapper(documentModel);
	}

	/**
	 * Converts the given {@link IDocumentModel} into the internal counterpart.
	 *
	 * @param documentModel
	 * 		the {@link IDocumentModel} to be converted. May not be {@code null}.
	 * @return the corresponding {@link DocumentModel}.
	 */
	public DocumentModel convertFromExternal(IDocumentModel documentModel) {
		return ExternalDocumentModelSerializerService.getInternDocumentModel(documentModel);
	}

	/**
	 * For more information see
	 * {@link com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService#getPath(com.mgmtp.a12.kernel.md.model.api.IElement)}.
	 */
	public @NonNull String getPath(@NonNull Element element) {
		return ElementUtils.getPath(element);
	}

	/**
	 * Create the shortest relative path from a given source entity to the given target entity.
	 *
	 * @param absolutePathOfSourceEntity
	 * 		the absolute path / full name of the source entity
	 * @param absolutePathOfTargetEntity
	 * 		the absolute path / full name of the target entity
	 * @return the shortest relative path from the given {@code absolutePathOfSourceEntity} to {@code absolutePathOfTargetEntity}.
	 * 		<p>
	 * 		Example: source "/A/B/Rule", target "/A/C/Field" --> "../../C/Field"
	 */
	public String createRelativePath(String absolutePathOfSourceEntity, String absolutePathOfTargetEntity) {
		return ElementPathUtils.relPath(absolutePathOfTargetEntity, absolutePathOfSourceEntity);
	}

	/**
	 * Create the absolute path / full name of the target entity by resolving its relative path against the given source entity.
	 *
	 * @param absolutePathOfSourceEntity
	 * 		the absolute path / full name of the source entity
	 * @param relativePathOfTargetEntity
	 * 		the relative path of the target entity
	 * @return the absolute path / full name of the target entity
	 * 		<p>
	 * 		Example: source "/A/B/Rule", target "../../C/Field" --> "/A/C/Field"
	 */
	public String resolveRelativePath(String absolutePathOfSourceEntity, String relativePathOfTargetEntity) {
		return ElementPathUtils.absPath(absolutePathOfSourceEntity, relativePathOfTargetEntity);
	}

	// ==================================
	// access to the validation tool api
	// ==================================

	/**
	 * For more information see
	 * {@link com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService#checkConsistency(IDocumentModel, Consumer)}.
	 * <p>
	 * For the reported problems, {@link IProblem#getSource()} always returns {@code null} or an instance of one of the following:
	 * <ul>
	 * 		<li>{@link DocumentModel}</li>
	 * 		<li>{@link Group}</li>
	 * 		<li>{@link Field}</li>
	 * 		<li>{@link Rule}</li>
	 * 		<li>{@link Computation}</li>
	 * 		<li>{@link FieldTypeDefinition}</li>
	 * 		<li>{@link DocumentUniquenessCriterion}</li>
	 * </ul>
	 */
	public void checkConsistency(DocumentModel documentModel, IProblemReporter pr) {
		new ExternalDocumentModelService().checkConsistency(
				convertToExternal(documentModel), problemReporterToNotificationReceiver(pr));
	}

	/**
	 * Check the consistency of the provided Document Uniqueness Criterion. Includes a call to
	 * {@link #checkDocumentUniquenessCriterionErrorText}.
	 *
	 * @param documentModel
	 * 		the Document Model the Document Uniqueness Criterion is / will be part of
	 * @param documentUniquenessCriterion
	 * 		the Document Uniqueness Criterion to check
	 * @param pr
	 * 		the problem reporter that will receive the problems found
	 */
	public void checkDocumentUniquenessCriterionConsistency(
			@NonNull DocumentModel documentModel,
			@NonNull DocumentUniquenessCriterion documentUniquenessCriterion,
			@NonNull IProblemReporter pr
	) {
		DocumentUniquenessCriteriaRule.checkDocumentUniquenessCriterionConsistency(
				convertToExternal(documentModel),
				new DocumentUniquenessCriterionWrapper(documentUniquenessCriterion),
				problemReporterToNotificationReceiver(pr)::accept);
	}

	/**
	 * Check the error text of the provided Document Uniqueness Criterion.
	 *
	 * @param documentModel
	 * 		the Document Model the Document Uniqueness Criterion is / will be part of
	 * @param documentUniquenessCriterion
	 * 		the Document Uniqueness Criterion to check
	 * @param pr
	 * 		the problem reporter that will receive the problems found
	 */
	public void checkDocumentUniquenessCriterionErrorText(
			@NonNull DocumentModel documentModel,
			@NonNull DocumentUniquenessCriterion documentUniquenessCriterion,
			@NonNull IProblemReporter pr
	) {
		DocumentUniquenessCriteriaRule.checkDocumentUniquenessCriterionErrorText(
				convertToExternal(documentModel),
				new DocumentUniquenessCriterionWrapper(documentUniquenessCriterion),
				problemReporterToNotificationReceiver(pr)::accept);
	}

	/**
	 * Maps a {@link DocumentModel} to an {@link ElementContainer}. ExtMP-specific content in the {@link DocumentModel} is
	 * considered by this method.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} to map. May not be {@code null}.
	 * @param considerNotInD
	 * 		- if {@code true} ExtMP-specific information is considered otherwise it is ignored.
	 * @return an instance of {@link ElementContainer} that contains the same modeling configuration
	 * 		as the {@link DocumentModel} that was passed in.
	 *
	 * @throws IllegalArgumentException
	 * 		if the DocumentModel cannot be converted due to an illegal format
	 */
	public ElementContainer convert(DocumentModel documentModel, boolean considerNotInD) {
		IDocumentModel extDocumentModel = convertToExternal(documentModel);
		return DocumentModelServiceUtils.convert(extDocumentModel, considerNotInD, p -> {
			throw new IllegalArgumentException(p.getMessage());
		});
	}

	/**
	 * Maps a {@link IDocumentModel} to an {@link ElementContainer}. ExtMP-specific content in the {@link DocumentModel} is
	 * considered by this method.
	 *
	 * @param documentModel
	 * 		the {@link IDocumentModel} to map. May not be {@code null}.
	 * @param considerNotInD
	 * 		- if {@code true} ExtMP-specific information is considered otherwise it is ignored.
	 * @return an instance of {@link ElementContainer} that contains the same modeling configuration
	 * 		as the {@link IDocumentModel} that was passed in.
	 *
	 * @throws IllegalArgumentException
	 * 		if the DocumentModel cannot be converted due to an illegal format
	 */
	public ElementContainer convert(IDocumentModel documentModel, boolean considerNotInD) {
		return DocumentModelServiceUtils.convert(documentModel, considerNotInD, p -> {
			throw new IllegalArgumentException(p.getMessage());
		});
	}

	/**
	 * Maps a {@link DocumentModel} to an {@link ElementContainer}.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} to map. May not be {@code null}.
	 * @return an instance of {@link ElementContainer} that contains the same modeling configuration
	 * 		as the {@link DocumentModel} that was passed in.
	 *
	 * @throws IllegalArgumentException
	 * 		if the DocumentModel cannot be converted due to an illegal format
	 */
	public ElementContainer convert(DocumentModel documentModel) {
		return convert(documentModel, false);
	}

	/**
	 * Maps a {@link IDocumentModel} to an {@link ElementContainer}.
	 *
	 * @param documentModel
	 * 		the {@link IDocumentModel} to map. May not be {@code null}.
	 * @return an instance of {@link ElementContainer} that contains the same modeling configuration
	 * 		as the {@link IDocumentModel} that was passed in.
	 *
	 * @throws IllegalArgumentException
	 * 		if the DocumentModel cannot be converted due to an illegal format
	 */
	public ElementContainer convert(IDocumentModel documentModel) {
		return convert(documentModel, false);
	}

	/**
	 * Creates a {@link IMVK_Service} from the {@link DocumentModel}.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} for the service to work on. May not be {@code null}.
	 * @param pr
	 * 		a {@link IProblemReporter} to which problems will be reported if they arise during conversion of the documentModel
	 * @return the {@link IMVK_Service} instance for the given model.
	 */
	public @NonNull IMVK_Service getMvkServiceForModel(@NonNull DocumentModel documentModel, @NonNull IProblemReporter pr) {
		return getMvkServiceForModelInternal(documentModel, pr);
	}

	/**
	 * Like {@link #getMvkServiceForModel(DocumentModel, IProblemReporter)}, but throws a RuntimeException if problems
	 * occur during conversion.
	 */
	public @NonNull IMVK_Service getMvkServiceForModel(@NonNull DocumentModel documentModel) {
		return getMvkServiceForModel(
				documentModel,
				p -> {
					throw new IllegalArgumentException("Error during creation of IMVK_Service for model "
							+ documentModel.getHeader().getId() + " and no problemReporter was given:" + p.getMessage());
				});
	}

	private @NonNull MVKService getMvkServiceForModelInternal(@NonNull DocumentModel documentModel, @NonNull IProblemReporter pr) {
		IDocumentModel extDocModel = convertToExternal(documentModel);
		ElementContainer ec = DocumentModelServiceUtils.convert(extDocModel, false, pr);
		IServiceConfiguration serviceConfiguration = DocumentModelServiceUtils.createServiceConfiguration(extDocModel, ec);
		return new MVKService(serviceConfiguration);
	}

	/**
	 * Creates a {@link ICalculationService} from the {@link DocumentModel}.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} for the service to work on. May not be {@code null}.
	 * @return the {@link ICalculationService} instance for the given model.
	 */
	public @NonNull ICalculationService getCalculationServiceForModel(@NonNull DocumentModel documentModel) {
		ElementContainer ec = convert(documentModel);
		IServiceConfiguration serviceConfiguration = DocumentModelServiceUtils.createServiceConfiguration(
				convertToExternal(documentModel), ec);
		return new ServiceFactory().getCalculationService(serviceConfiguration, ec.getAllCalculations());
	}

	/**
	 * Like {@link #getCalculationServiceForModel(DocumentModel)}, but reusing an already created {@link IMVK_Service}
	 * instead of deriving one from the {@link DocumentModel}.
	 * <p/>
	 * This saves time when several services are needed for the same {@link DocumentModel}: the {@link IMVK_Service}
	 * (whose creation converts the whole model) can be built once, via
	 * {@link #getMvkServiceForModel(DocumentModel, IProblemReporter)}, and reused across all calls.
	 *
	 * @param service
	 * 		the service for the document model, acquired via {@link #getMvkServiceForModel(DocumentModel, IProblemReporter)}.
	 * 		May not be {@code null}.
	 * @return the {@link ICalculationService} instance for the model of the given {@link IMVK_Service}.
	 */
	public @NonNull ICalculationService getCalculationService(@NonNull IMVK_Service service) {
		ElementContainer ec = getElementContainer(service);
		return new ServiceFactory().getCalculationService(getMvkService(service).getServiceConfiguration(),
				ec.getAllCalculations());
	}

	/**
	 * Creates a reduced {@link DocumentModel} which, compared to the passed {@link DocumentModel}, contains only elements
	 * contained
	 * in the passed set and entities referenced by them. Referenced entities are
	 * <ul>
	 *     <li>the fields and groups referenced by rules or computations (i.e. directly referenced in the condition text, in the error text,
	 *     or as an error field).</li>
	 *     <li>indirectly referenced entities: the fields and subgroups contained in directly referenced groups from last bullet point (including the entities contained therein).</li>
	 *     <li>if a root group is referenced in a language construct, it is not expanded (i.e. not all of its fields and groups end up in
	 *     the reduced DM). Because of the size of root groups and because this usage is rare, this deviation from groups is
	 *     intentional.
	 *     For such a root group, a <i>Confirm</i> field is created in the reduced DM
	 *     as the first child of the root group, which actually does not exist in the original DM.
	 *     Example: For the rule condition
	 *     "GroupFilled(/RG) and FieldNotFilled(/RG/G/F)" the existence of an instance of the root group "RG" can be controlled via the <i>Confirm</i> field.</li>
	 * </ul>
	 * Additionally, sort information ({@code sortFields}) is removed from every rootgroup and group in the reduced DM
	 * (i.e. {@code getSortFields()} returns an empty list throughout). On one hand, sort information may reference a field that is
	 * not included in the reduced Document Model, thereby making the Document Model invalid. On the other hand,
	 * sort information are not relevant for the only current use case of this method, the AdHoc test.
	 *
	 * @param completeDm
	 * 		the {@link DocumentModel} from which the reduced DM should be created. May not be {@code null}.
	 * @param selectedElementIds
	 * 		the set of IDs of elements to be included in the reduced {@link DocumentModel}. IDs not present in the original document
	 * 		model are ignored. May not be {@code null} or empty.
	 * @param partlySelectedElementIds
	 * 		the set of the ancestor IDs of all the {@code selectedElementIds}. May not be
	 *        {@code null}. This was agreed upon with SME and is just a performance improvement so that the ancestor IDs don't have to
	 * 		be
	 * 		computed.
	 * 		If this set is empty or does not contain all needed ancestor IDs, there is no error returned, but the
	 * 		reduced {@link DocumentModel} is incomplete.
	 * @param notificationReceiver
	 * 		the {@link RankedNotification}s, found during the process, will be reported to this callback. May not be {@code null}.
	 * 		Examples for reported notifications are problems when parsing the rule conditions.
	 * @return a {@link DocumentModel} which is a part of {@code completeDm} or an empty Optional instance if error
	 * 		notifications are present. This {@link DocumentModel} contains only the selected elements as well as the entities
	 * 		referenced by them (see method description). The object {@code completeDm} is not changed.
	 */
	public Optional<DocumentModel> createReducedDocumentModel(DocumentModel completeDm, Set<String> selectedElementIds,
			Set<String> partlySelectedElementIds, Consumer<RankedNotification> notificationReceiver) {
		return new DocumentModelReduceService(completeDm).reduce(selectedElementIds, partlySelectedElementIds,
				notificationReceiver);
	}

	/**
	 * Checks a computation as a whole, i.e. its fragments, the combined computation statement, its calculation rule and its
	 * error texts.
	 * <p/>
	 * Tries to parse the computation. If problems occur during parsing, {@link OriginInComputationFragment#of(IProblem)} can be used
	 * to get information about the origin of the problem within the computation (if the problem can be attributed to a
	 * computation precondition or operation).
	 * <p/>
	 * The checks build on each other and run in sequence up to the first failing parse: if a computation fragment cannot be
	 * parsed, the combined statement is not parsed again (it would only reproduce the same errors), and if the combined
	 * statement cannot be parsed, the calculation rule is not checked (it has no condition to work with). The calculation
	 * rule check and the error text check both run on a parseable condition, so a computation with a defect in each has both
	 * reported.
	 * <p/>
	 * The error texts the computation specifies are checked as well. Only those are checked: a computation <em>without</em>
	 * error texts is a valid state of the document model, because the missing texts are generated during code generation, which
	 * generates a rule for each computation. This differs on purpose from
	 * {@link #isValidRule(String, DocumentModel, IProblemReporter)}, where the error texts are mandatory and are therefore
	 * checked for all locales of the model.
	 * <p/>
	 * For "incremental" checks of a Computation (e.g. while the user edits the preconditions or operations), this method can simply
	 * be called repeatedly with the updated documentModel. Performance with large document models and high-frequency edits may
	 * be less than ideal - if this becomes relevant in practice, please inform the kernel team.
	 * <p/>
	 * If several checks are run on the same {@link DocumentModel}, prefer the overload
	 * {@link #isValidComputation(IMVK_Service, String, IProblemReporter)}: it lets you reuse a single
	 * {@link IMVK_Service} (whose creation converts the whole model) instead of deriving it anew on every call. The service
	 * can be obtained via {@link #getMvkServiceForModel(DocumentModel, IProblemReporter)}.
	 *
	 * @param computationFullName
	 * 		the fullName (path) of a computation within the documentModel
	 * @param documentModel
	 * 		the documentModel that contains the computation, the computation's referenced fields, etc.
	 * @param pr
	 * 		an IProblemReporter. You can use {@link OriginInComputationFragment#of(IProblem)} to obtain details about the
	 * 		origin of the problem within the computation.
	 * @return {@code true} if no problems occurred during conversion to an {@link IEC} and subsequent parsing of the computation,
	 * 		otherwise {@code false}.
	 *
	 * @implNote The warnings to suppress and any additional parser directives are taken from the computation's
	 *        {@link IComputation#getAdditionalParserInformation() additionalParserInformation} (which is additionally
	 * 		validated to be a valid preamble and nothing but a preamble). Use this overload instead of the deprecated
	 *        {@link #isValidComputation(String, DocumentModel, IProblemReporter, List)}.
	 */
	public boolean isValidComputation(
			@NonNull String computationFullName,
			@NonNull DocumentModel documentModel,
			@NonNull IProblemReporter pr
	) {
		return isValidComputationInternal(computationFullName, documentModel, pr, Collections.emptyList());
	}

	/**
	 * Checks a computation as a whole, i.e. its fragments, the combined computation statement, its calculation rule and its
	 * error texts.
	 * <p/>
	 * Tries to parse the computation. If problems occur during parsing, {@link OriginInComputationFragment#of(IProblem)} can be used
	 * to get information about the origin of the problem within the computation (if the problem can be attributed to a
	 * computation precondition or operation).
	 * <p/>
	 * The checks build on each other and run in sequence up to the first failing parse: if a computation fragment cannot be
	 * parsed, the combined statement is not parsed again (it would only reproduce the same errors), and if the combined
	 * statement cannot be parsed, the calculation rule is not checked (it has no condition to work with). The calculation
	 * rule check and the error text check both run on a parseable condition, so a computation with a defect in each has both
	 * reported.
	 * <p/>
	 * The error texts the computation specifies are checked as well. Only those are checked: a computation <em>without</em>
	 * error texts is a valid state of the document model, because the missing texts are generated during code generation, which
	 * generates a rule for each computation. This differs on purpose from
	 * {@link #isValidRule(String, DocumentModel, IProblemReporter)}, where the error texts are mandatory and are therefore
	 * checked for all locales of the model.
	 * <p/>
	 * For "incremental" checks of a Computation (e.g. while the user edits the preconditions or operations), this method can simply
	 * be called repeatedly with the updated documentModel. Performance with large document models and high-frequency edits may
	 * be less than ideal - if this becomes relevant in practice, please inform the kernel team.
	 * <p/>
	 * If several checks are run on the same {@link DocumentModel}, prefer the overload
	 * {@link #isValidComputation(IMVK_Service, String, IProblemReporter, List)}: it lets you reuse a single
	 * {@link IMVK_Service} (whose creation converts the whole model) instead of deriving it anew on every call. The service
	 * can be obtained via {@link #getMvkServiceForModel(DocumentModel, IProblemReporter)}.
	 *
	 * @param computationFullName
	 * 		the fullName (path) of a computation within the documentModel
	 * @param documentModel
	 * 		the documentModel that contains the computation, the computation's referenced fields, etc.
	 * @param pr
	 * 		an IProblemReporter. You can use {@link OriginInComputationFragment#of(IProblem)} to obtain details about the
	 * 		origin of the problem within the computation.
	 * @param errorCodesToSuppress
	 * 		a list of error codes that should be suppressed. These are applied in addition to the warnings derived from
	 * 		the computation's {@link IComputation#getAdditionalParserInformation() additionalParserInformation}.
	 * @return {@code true} if no problems occurred during conversion to an {@link IEC} and subsequent parsing of the computation,
	 * 		otherwise {@code false}.
	 *
	 * @deprecated the suppression info now lives on the computation itself (its
	 *        {@link IComputation#getAdditionalParserInformation() additionalParserInformation}); use
	 *        {@link #isValidComputation(String, DocumentModel, IProblemReporter)} and let the computation carry the
	 * 		preamble instead of passing an explicit list.
	 */
	@Deprecated(since = "31.2.0")
	public boolean isValidComputation(
			@NonNull String computationFullName,
			@NonNull DocumentModel documentModel,
			@NonNull IProblemReporter pr,
			@NonNull List<String> errorCodesToSuppress
	) {
		return isValidComputationInternal(computationFullName, documentModel, pr, errorCodesToSuppress);
	}

	private boolean isValidComputationInternal(
			@NonNull String computationFullName,
			@NonNull DocumentModel documentModel,
			@NonNull IProblemReporter pr,
			@NonNull List<String> errorCodesToSuppress
	) {
		ComputationRuleAndMvkService mvkServiceAndRule =
				deriveCalculationRuleAndMvkServiceForDM(computationFullName, documentModel, pr);
		MVKService mvkService = mvkServiceAndRule.mvkService;
		ICalculationRule calculationRule = mvkServiceAndRule.calculationRuleForComputation;
		if (mvkService == null || calculationRule == null) {
			return false;
		}
		return checkComputation(mvkService, calculationRule, errorCodesToSuppress, pr);
	}

	/**
	 * Like {@link #isValidComputation(String, DocumentModel, IProblemReporter)}, but reusing an already created
	 * {@link IMVK_Service} instead of deriving one from the {@link DocumentModel}.
	 * <p/>
	 * This saves time when several checks are run on the same {@link DocumentModel}: the {@link IMVK_Service} (whose
	 * creation converts the whole model) can be built once, via
	 * {@link #getMvkServiceForModel(DocumentModel, IProblemReporter)}, and reused across all calls.
	 *
	 * @param imvkService
	 * 		the service for the document model that contains the computation, acquired via
	 * 		{@link #getMvkServiceForModel(DocumentModel, IProblemReporter)}
	 * @param computationFullName
	 * 		the fullName (path) of a computation within the documentModel
	 * @param pr
	 * 		an IProblemReporter. You can use {@link OriginInComputationFragment#of(IProblem)} to obtain details about the
	 * 		origin of the problem within the computation.
	 * @return {@code true} if no problems occurred during conversion to an {@link IEC} and subsequent parsing of the computation,
	 * 		otherwise {@code false}.
	 */
	public boolean isValidComputation(
			@NonNull IMVK_Service imvkService,
			@NonNull String computationFullName,
			@NonNull IProblemReporter pr
	) {
		return isValidComputation(imvkService, computationFullName, pr, Collections.emptyList());
	}

	/**
	 * Like {@link #isValidComputation(String, DocumentModel, IProblemReporter, List)}, but reusing an already created
	 * {@link IMVK_Service} instead of deriving one from the {@link DocumentModel}.
	 * <p/>
	 * This saves time when several checks are run on the same {@link DocumentModel}: the {@link IMVK_Service} (whose
	 * creation converts the whole model) can be built once, via
	 * {@link #getMvkServiceForModel(DocumentModel, IProblemReporter)}, and reused across all calls.
	 *
	 * @param imvkService
	 * 		the service for the document model that contains the computation, acquired via
	 * 		{@link #getMvkServiceForModel(DocumentModel, IProblemReporter)}
	 * @param computationFullName
	 * 		the fullName (path) of a computation within the documentModel
	 * @param pr
	 * 		an IProblemReporter. You can use {@link OriginInComputationFragment#of(IProblem)} to obtain details about the
	 * 		origin of the problem within the computation.
	 * @param errorCodesToSuppress
	 * 		a list of error codes that should be suppressed.
	 * @return {@code true} if no problems occurred during conversion to an {@link IEC} and subsequent parsing of the computation,
	 * 		otherwise {@code false}.
	 */
	public boolean isValidComputation(
			@NonNull IMVK_Service imvkService,
			@NonNull String computationFullName,
			@NonNull IProblemReporter pr,
			@NonNull List<String> errorCodesToSuppress
	) {
		MVKService mvkService = getMvkService(imvkService);
		Optional<ICalculationRule> calculationRule = findCalculationRuleByName(computationFullName, (ElementContainer) mvkService.getEc());
		if (calculationRule.isEmpty()) {
			pr.reportProblem(new MVKCoreError(
					"No computation was found for fullName or the computation is invalid: " + computationFullName,
					Severity.ERROR,
					null));
			return false;
		}
		return checkComputation(mvkService, calculationRule.get(), errorCodesToSuppress, pr);
	}

	/**
	 * Runs the four computation check steps against an already resolved {@link MVKService} and {@link ICalculationRule}.
	 * Shared by the {@link DocumentModel}-based and the {@link IMVK_Service}-based {@code isValidComputation} overloads.
	 */
	private static boolean checkComputation(
			@NonNull MVKService mvkService,
			@NonNull ICalculationRule calculationRule,
			@NonNull List<String> errorCodesToSuppress,
			@NonNull IProblemReporter pr
	) {
		IRuleFromComputation rule = (IRuleFromComputation) calculationRule.getRule();
		ProblemReporter prInternal = new ProblemReporterProxy(pr);

		// 1. check the computation fragments (common precondition, preconditions and operations)
		boolean partsValid = hasValidComputationParts(
				rule, mvkService.createParserService(), prInternal, errorCodesToSuppress);
		if(!partsValid) {
			return false;
		}

		// 2. check the combined computation statement (the computation fragments combined)
		ICondition condition = mvkService.parseConditionNoExpand(rule, rule.getErrorConditionText(), prInternal);
		if(condition==null || prInternal.hasProblemOccurred(Severity.ERROR)) {
			return false;
		}

		// 3. the calculation rule, reusing the condition of step 2 instead of parsing the statement a second time.
		boolean calculationValid = new ServiceFactory().getCalculationService(mvkService.getServiceConfiguration())
				.checkCalculationRule(calculationRule, condition, pr);

		// 4. check the error texts that the computation specifies.
		// Only those are checked: a computation without error texts is valid - missing texts are generated later, during code
		// generation, when a rule is generated for each computation.
		Collection<Locale> specifiedLocales = rule.getErrorMessages().keySet();
		boolean errorTextValid = specifiedLocales.isEmpty()
				|| mvkService.hasValidErrorText(rule, mvkService.expandCondition(condition),
				specifiedLocales, false, pr);

		return calculationValid && errorTextValid;
	}

	private ComputationRuleAndMvkService deriveCalculationRuleAndMvkServiceForDM(
			String computationFullName,
			DocumentModel documentModel,
			IProblemReporter pr
	) {
		ProblemReporter prProxy = new ProblemReporterProxy(pr);
		MVKService mvkService = getMvkServiceForModelInternal(documentModel, p -> prProxy.reportProblem(
				new WrappedProblemWithSeverityLimitAndOrigin(
						p,
						isOriginIElementWithPath(p, computationFullName) ?
								Severity.ERROR :
								Severity.WARNING,
						p.getSource())));
		if (prProxy.hasProblemOccurred(Severity.ERROR)) {
			return new ComputationRuleAndMvkService(null, mvkService);
		}

		Optional<ICalculationRule> calculationRule = findCalculationRuleByName(
				computationFullName, (ElementContainer) mvkService.getEc());
		if (calculationRule.isEmpty()) {
			// this might be a case for an illegalargumentexception, but we cannot rule out that
			// the computation does indeed exist but has errors which were not correctly attributed to the selected computation
			// during getMvkServiceForModel
			pr.reportProblem(new MVKCoreError(
					"No computation was found for fullName or the computation is invalid: " + computationFullName,
					Severity.ERROR,
					documentModel));
		}

		return new ComputationRuleAndMvkService(calculationRule.orElse(null), mvkService);
	}

	record ComputationRuleAndMvkService(
			ICalculationRule calculationRuleForComputation,
			MVKService mvkService) {
	}

	/**
	 * For more information see
	 * {@link IRuleConsistencyCheck#hasValidConditionText(IRule, IProblemReporter)}.
	 */
	public boolean hasValidConditionText(@NonNull IMVK_Service imvkService, @NonNull Rule rule, @NonNull IProblemReporter pr) {
		return imvkService.hasValidConditionText(findIRule(imvkService, rule), pr);
	}

	/**
	 * For more information see
	 * {@link IRuleConsistencyCheck#hasValidErrorText(IRule, Collection, boolean, IProblemReporter)}.
	 */
	public boolean hasValidErrorText(@NonNull IMVK_Service imvkService, @NonNull Rule rule, @NonNull Collection<Locale> locales,
			boolean extendProblemMessage, @NonNull IProblemReporter pr) {
		return imvkService.hasValidErrorText(findIRule(imvkService, rule), locales, extendProblemMessage, pr);
	}

	/**
	 * Checks a rule as a whole, i.e. its condition text, its error texts and its custom error entities - see
	 * {@link IRuleConsistencyCheck#hasValidConditionText(IRule, IProblemReporter)},
	 * {@link IRuleConsistencyCheck#hasValidErrorText(IRule, Collection, boolean, IProblemReporter)} and
	 * {@link IRuleConsistencyCheck#hasValidCustomErrorEntities(IRule, IProblemReporter)}.
	 * <p/>
	 * The condition text is parsed first. If it cannot be parsed, the check stops there: the remaining checks all re-parse
	 * the same condition internally, so they would only reproduce the parse failure and report follow-up problems (the error
	 * text check would additionally report a redundant "the condition is invalid, so the error text could not be checked"
	 * summary). Once the condition parses, the error text and custom error entity checks both run and report, so a rule with a
	 * defect in both has both reported. The error texts are checked for the locales of the document model, i.e. for the
	 * languages the model declares that it supports, so a locale for which the rule has no error text is a problem.
	 * <p/>
	 * If several checks are run on the same {@link DocumentModel}, prefer the overload
	 * {@link #isValidRule(IMVK_Service, String, DocumentModel, IProblemReporter)}: it lets you reuse a single
	 * {@link IMVK_Service} (whose creation converts the whole model) instead of deriving it anew on every call. The service
	 * can be obtained via {@link #getMvkServiceForModel(DocumentModel, IProblemReporter)}.
	 *
	 * @param ruleFullName
	 * 		the fullName (path) of a rule within the documentModel
	 * @param documentModel
	 * 		the documentModel that contains the rule, the rule's referenced fields, etc.
	 * @param pr
	 * 		an IProblemReporter the problems of all checks are reported to
	 * @return {@code true} if all checks consider the rule valid, otherwise {@code false}
	 */
	public boolean isValidRule(@NonNull String ruleFullName, @NonNull DocumentModel documentModel, @NonNull IProblemReporter pr) {
		return isValidRule(getMvkServiceForModel(documentModel, pr), ruleFullName, documentModel, pr);
	}

	/**
	 * Like {@link #isValidRule(String, DocumentModel, IProblemReporter)}, but reusing an already created
	 * {@link IMVK_Service} instead of deriving one from the {@link DocumentModel}.
	 * <p/>
	 * This saves time when several checks are run on the same {@link DocumentModel}: the {@link IMVK_Service} (whose
	 * creation converts the whole model) can be built once, via
	 * {@link #getMvkServiceForModel(DocumentModel, IProblemReporter)}, and reused across all calls.
	 *
	 * @param imvkService
	 * 		the service for the document model that contains the rule, acquired via
	 * 		{@link #getMvkServiceForModel(DocumentModel, IProblemReporter)}
	 * @param ruleFullName
	 * 		the fullName (path) of a rule within the documentModel
	 * @param documentModel
	 * 		the documentModel that contains the rule, the rule's referenced fields, etc.
	 * @param pr
	 * 		an IProblemReporter the problems of all checks are reported to
	 * @return {@code true} if all checks consider the rule valid, otherwise {@code false}
	 */
	public boolean isValidRule(
			@NonNull IMVK_Service imvkService,
			@NonNull String ruleFullName,
			@NonNull DocumentModel documentModel,
			@NonNull IProblemReporter pr) {
		IRule iRule = findIRuleByPath(ruleFullName, getElementContainer(imvkService));
		// the locales the service was configured with are the ones of the document model, see
		// DocumentModelServiceUtils#createServiceConfiguration
		Collection<Locale> locales = imvkService.getProjectSpecificSettings().getProjectLanguageSettings().keySet();

		// Parse the condition once (NO_EXPAND, like hasValidConditionText, which routes through the same
		// parseBedingungInternal(NO_EXPAND)) so the parsed condition can be reused by the checks below. A reported error means
		// the condition text is invalid, mirroring hasValidConditionText's "valid = no error problem" semantics. If it cannot
		// be parsed we stop here: the remaining checks would only re-parse the same invalid condition internally and add
		// follow-up problems (the error text check even reports a redundant "condition invalid" summary).
		ProblemReporterProxy conditionPr = new ProblemReporterProxy(pr);
		ICondition condition = imvkService.parseBedingungNoExpand(iRule, conditionPr);
		if (condition == null || conditionPr.hasErrorOccurred()) {
			return false;
		}

		// the error text check needs the condition (to resolve referenced fields and iteration) in its expanded form, which
		// expandCondition derives from the parsed condition without parsing the text a second time. Passing it in reuses the
		// parse above instead of the internal re-parse the (rule, locales, ...) overload would do.
		boolean validErrorText = imvkService.hasValidErrorText(iRule, imvkService.expandCondition(condition), locales, false, pr);
		// note: for a rule of a document model this check cannot report anything at the moment, because the mapped rule returns
		// no custom ref error entities (see the Rule of the mapping package). It is called nevertheless, so that it takes
		// effect as soon as they are supported.
		boolean validCustomErrorEntities = imvkService.hasValidCustomErrorEntities(iRule, pr);

		return validErrorText && validCustomErrorEntities;
	}

	// IMVK_Service is @OnlyForUsage, so MVKService is its only implementation, and instances handed to this service are
	// created by getMvkServiceForModel, whose element container is always an ElementContainer. The casts below are safe.
	private static @NonNull MVKService getMvkService(@NonNull IMVK_Service imvkService) {
		return (MVKService) imvkService;
	}

	private static @NonNull ElementContainer getElementContainer(@NonNull IMVK_Service imvkService) {
		return (ElementContainer) getMvkService(imvkService).getEc();
	}

	private static @NonNull IRule findIRule(@NonNull IMVK_Service imvkService, @NonNull Rule rule) {
		return findIRuleByPath(ElementUtils.getPath(rule), getElementContainer(imvkService));
	}

	private static @NonNull IRule findIRuleForComputation(@NonNull IMVK_Service imvkService, @NonNull Computation computation) {
		return findICalculationRuleByPath(ElementUtils.getPath(computation), getElementContainer(imvkService)).getRule();
	}

	private static @NonNull IRule findIRuleByPath(@NonNull String rulePath, @NonNull ElementContainer elementContainer) {
		return elementContainer.getAllRules().stream()
				.filter(rule -> rule.getFullName().equals(rulePath))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Could not find rule at path '%s'".formatted(rulePath)));
	}

	private static @NonNull ICalculationRule findICalculationRuleByPath(
			@NonNull String calculationPath, @NonNull ElementContainer elementContainer) {
		return elementContainer.getAllCalculations().stream()
				.filter(calculation -> calculation.getRule().getFullName().equals(calculationPath))
				.findFirst()
				.orElseThrow(
						() -> new IllegalArgumentException("Could not find computation at path '%s'".formatted(calculationPath)));
	}

	/**
	 * @return the matching calculation or null if no calculation with calculationFullName exists
	 */
	private static Optional<ICalculationRule> findCalculationRuleByName(String calculationFullName, ElementContainer ec) {
		return ec.getAllCalculations().stream()
						.filter(c -> c.getRule().getFullName().equals(calculationFullName))
						.findAny();
	}

	private static boolean hasValidComputationParts(
			IRuleFromComputation ruleFromComputation,
			ParserService parserService,
			ProblemReporter pr,
			@NonNull List<String> errorCodesToSuppress
	) {
		IComputation computation = ruleFromComputation.getOriginalComputation();

		// The warnings to suppress while checking the individual fragment snippets. Any explicitly passed list is honoured
		// (deprecated overload); the new overload passes an empty list and relies solely on the field parsed below.
		Set<String> suppressedWarnings = new LinkedHashSet<>(errorCodesToSuppress);

		// Validate the additionalParserInformation field (a preamble) in isolation: the strict parser (CheckPreambleImpl)
		// enforces the supported-warning whitelist, and 'preambleSnippet' (preamble EOF) enforces that the field is a
		// preamble and nothing but a preamble. Problems are attributed to the field's own origin. Whitespace-only is
		// treated as absent. The suppressed warnings are taken from the parsed preamble itself (not re-derived via the
		// deprecated getErrorCodesToSuppress()), so the snippet checks below suppress exactly what the preamble declares.
		Optional<String> additionalParserInformation =
				computation.getAdditionalParserInformation().filter(text -> !text.isBlank());
		if (additionalParserInformation.isPresent()) {
			PreambleCheckResult preambleResult =
					checkAdditionalParserInformation(ruleFromComputation, additionalParserInformation.get(), parserService, pr);
			if (!preambleResult.parsedSuccessfully()) {
				// The preamble did not parse, so the directives it was meant to declare (e.g. which warnings to suppress)
				// are unknown. Checking the remaining fragments (common precondition, alternatives, operations) would then
				// surface follow-up errors for exactly the warnings the author wanted suppressed, masking the real problem.
				// Report only the preamble error (already reported above) and stop here.
				return false;
			}
			suppressedWarnings.addAll(preambleResult.suppressedWarnings());
		}

		computation.getCommonPrecondition().ifPresent(precondText -> parserService.checkSnippet(
				ruleFromComputation,
				precondText,
				Lenient_ConditionParser::bedingungSnippet,
				p -> pr.reportProblem(wrapWithFragmentAsOrigin(p, -1, true, precondText)),
				// the preamble is not part of a snippet (bedingungSnippet has no preamble), so the suppressed warnings
				// derived above must be registered on the parser explicitly
				parser -> suppressedWarnings.forEach(parser::addSuppressedWarning)));
		List<IComputation.IComputationAlternative> computationAlternatives =
				computation.getComputationAlternatives();
		for (int i = 0; i < computationAlternatives.size(); i++) {
			final int alternativeIndex = i;
			var computationAlternative = computationAlternatives.get(i);
			computationAlternative.getPrecondition().ifPresent(
					precondText ->
							parserService.checkSnippet(
									ruleFromComputation,
									precondText,
									Lenient_ConditionParser::bedingungSnippet,
									p -> pr.reportProblem(wrapWithFragmentAsOrigin(p, alternativeIndex, true, precondText)),
									parser -> suppressedWarnings.forEach(parser::addSuppressedWarning)));
			var operationText = computationAlternative.getOperation();
			parserService.checkSnippet(
					ruleFromComputation,
					operationText,
					Lenient_ConditionParser::operationSnippet,
					p -> pr.reportProblem(wrapWithFragmentAsOrigin(p, alternativeIndex, false, operationText)),
					parser -> suppressedWarnings.forEach(parser::addSuppressedWarning));
		}
		return !pr.hasProblemOccurred(Severity.ERROR);
	}

	/**
	 * Parses the computation's {@code additionalParserInformation} in isolation (as a {@code preambleSnippet}) and reports
	 * any problems against the field's origin ({@link OriginInComputationFragment#isAdditionalParserInformation()}).
	 *
	 * @return whether the preamble parsed without error and the warnings it suppresses (the latter only meaningful when
	 * 		the preamble parsed successfully).
	 */
	private static PreambleCheckResult checkAdditionalParserInformation(
			IRuleFromComputation ruleFromComputation,
			String additionalParserInformation,
			ParserService parserService,
			ProblemReporter pr
	) {
		// capture the parser so the warnings it registers while parsing the preamble can be read afterwards
		// (the parserInit consumer runs before parsing, so we read getSuppressedWarnings() only once checkSnippet returns)
		Lenient_ConditionParser[] parserHolder = new Lenient_ConditionParser[1];
		boolean[] parseFailed = { false };
		parserService.checkSnippet(
				ruleFromComputation,
				additionalParserInformation,
				Lenient_ConditionParser::preambleSnippet,
				p -> {
					IProblem problem = wrapWithAdditionalParserInformationOrigin(p, additionalParserInformation);
					if (problem.getSeverity() == Severity.ERROR) {
						parseFailed[0] = true;
					}
					pr.reportProblem(problem);
				},
				parser -> parserHolder[0] = parser);
		Set<String> suppressedWarnings =
				parserHolder[0] == null ? Set.of() : new LinkedHashSet<>(parserHolder[0].getSuppressedWarnings());
		return new PreambleCheckResult(!parseFailed[0], suppressedWarnings);
	}

	/**
	 * The outcome of validating a computation's {@code additionalParserInformation} preamble: whether it parsed without
	 * error, and the warnings it suppresses (only meaningful when {@link #parsedSuccessfully()} is {@code true}).
	 */
	private record PreambleCheckResult(boolean parsedSuccessfully, Set<String> suppressedWarnings) {
	}

	private static IProblem wrapWithFragmentAsOrigin(IProblem p, int alternativeIndex, boolean isPrecondition, String fragmenText) {
		// replace the origin and keep the other attributes unchanged
		return new WrappedProblemWithSeverityLimitAndOrigin(
				p,
				Severity.ERROR, // maxSeverity = ERROR has the effect of not changing the severity
				OriginInComputationFragment.of(fragmenText, alternativeIndex, isPrecondition,
						p.getLine(), p.getSourceStart(), p.getLineEnd(), p.getSourceEnd()));
	}

	private static IProblem wrapWithAdditionalParserInformationOrigin(IProblem p, String fragmentText) {
		// replace the origin and keep the other attributes unchanged
		return new WrappedProblemWithSeverityLimitAndOrigin(
				p,
				Severity.ERROR, // maxSeverity = ERROR has the effect of not changing the severity
				OriginInComputationFragment.ofAdditionalParserInformation(fragmentText,
						p.getLine(), p.getSourceStart(), p.getLineEnd(), p.getSourceEnd()));
	}

	private static boolean isOriginIElementWithPath(IProblem p, String path) {
		return (p.getSource() instanceof IElement element)
				&& path.equals(ElementUtils.getPath(element));
	}

	public String formatComputationOperation(
			String operationText,
			String computationFullName, // can this be static?
			DocumentModel documentModel,
			IProblemReporter pr
	) {
		ComputationRuleAndMvkService mvkServiceAndRule =
				deriveCalculationRuleAndMvkServiceForDM(computationFullName, documentModel, pr);

		if (mvkServiceAndRule.mvkService == null || mvkServiceAndRule.calculationRuleForComputation == null) {
			return operationText; // no formatting possible. errors have already been reported to pr
		}

		return mvkServiceAndRule.mvkService.formatOperationSnippet(
				operationText,
				mvkServiceAndRule.calculationRuleForComputation.getRule(),
				pr);
	}

	public String formatComputationCondition(
			String conditionText,
			String computationFullName, // can this be static?
			DocumentModel documentModel,
			IProblemReporter pr
	) {
		ComputationRuleAndMvkService mvkServiceAndRule =
				deriveCalculationRuleAndMvkServiceForDM(computationFullName, documentModel, pr);

		if (mvkServiceAndRule.mvkService == null || mvkServiceAndRule.calculationRuleForComputation == null) {
			return conditionText; // no formatting possible. errors have already been reported to pr
		}

		return mvkServiceAndRule.mvkService.formatConditionSnippet(
				conditionText,
				mvkServiceAndRule.calculationRuleForComputation.getRule(),
				pr);
	}

	/**
	 * For more information see
	 * {@link IConditionFormatter#formatConditionText(IRule, IProblemReporter)}.
	 */
	public String formatConditionText(IMVK_Service imvkService, String fullName, IProblemReporter pr) {
		return imvkService.formatConditionText(findIRuleByPath(fullName, getElementContainer(imvkService)), pr);
	}

	/**
	 * Checks if the given pattern in a string field is valid for java and for javascript.
	 *
	 * @param pattern
	 * 		the string pattern to be checked.
	 * @return true if the given pattern is valid for java and for javascript, otherwise false.
	 */
	public boolean checkPattern(String pattern) {
		// Check if the given pattern is a valid Java Pattern
		try {
			Pattern.compile(pattern);
		} catch (Exception e) {
			return false;
		}
		// Check if the given pattern is a valid Javascript Pattern
		return PatternUtils.isPatternValid(pattern);
	}
}
