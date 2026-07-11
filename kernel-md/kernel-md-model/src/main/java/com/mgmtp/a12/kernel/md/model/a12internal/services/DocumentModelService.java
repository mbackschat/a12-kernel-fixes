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
	 * {@link IDocumentModelService#getPath(com.mgmtp.a12.kernel.md.model.api.IElement)}.
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
	 * {@link IDocumentModelService#checkConsistency(IDocumentModel, Consumer)}.
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
	 * Tries to parse a computation. If problems occur during parsing, {@link OriginInComputationFragment#of(IProblem)} can be used
	 * to get information about the origin of the problem within the computation (if the problem can be attributed to a
	 * computation precondition or operation).
	 * <p/>
	 * For "incremental" checks of a Computation (e.g. while the user edits the preconditions or operations), this method can simply
	 * be called repeatedly with the updated documentModel. Performance with large document models and high-frequency edits may
	 * be less than ideal - if this becomes relevant in practice, please inform the kernel team.
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
	 */
	public boolean isValidComputation(
			String computationFullName,
			DocumentModel documentModel,
			IProblemReporter pr
	) {
		return isValidComputation(computationFullName, documentModel, pr, Collections.emptyList());
	}

	/**
	 * Tries to parse a computation. If problems occur during parsing, {@link OriginInComputationFragment#of(IProblem)} can be used
	 * to get information about the origin of the problem within the computation (if the problem can be attributed to a
	 * computation precondition or operation).
	 * <p/>
	 * For "incremental" checks of a Computation (e.g. while the user edits the preconditions or operations), this method can simply
	 * be called repeatedly with the updated documentModel. Performance with large document models and high-frequency edits may
	 * be less than ideal - if this becomes relevant in practice, please inform the kernel team.
	 *
	 * @param computationFullName
	 * 		the fullName (path) of a computation within the documentModel
	 * @param documentModel
	 * 		the documentModel that contains the computation, the computation's referenced fields, etc.
	 * @param pr
	 * 		an IProblemReporter. You can use {@link OriginInComputationFragment#of(IProblem)} to obtain details about the
	 * 		origin of the problem within the computation.
	 * @param errorCodesToSuppress
	 * 		a list of error codes that should be suppressed.
	 * @return {@code true} if no problems occurred during conversion to an {@link IEC} and subsequent parsing of the computation,
	 * 		otherwise {@code false}.
	 */
	public boolean isValidComputation(
			String computationFullName,
			DocumentModel documentModel,
			IProblemReporter pr,
			@NonNull List<String> errorCodesToSuppress
	) {
		ComputationRuleAndMvkService mvkServiceAndRule =
				deriveCalculationRuleAndMvkServiceForDM(computationFullName, documentModel, pr);
		MVKService mvkService = mvkServiceAndRule.mvkService;
		ICalculationRule calculationRule = mvkServiceAndRule.calculationRuleForComputation;

		return mvkService != null
				&& calculationRule != null
				&& hasValidComputationParts(
				(IRuleFromComputation) calculationRule.getRule(), mvkService.createParserService(), new ProblemReporterProxy(pr),
				errorCodesToSuppress)
				&& mvkService.hasValidConditionText(calculationRule.getRule(), pr)
				&& new ServiceFactory().getCalculationService(mvkService.getServiceConfiguration())
				.checkCalculationRule(calculationRule, List.of(), pr);
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

		ICalculationRule calculationRule = findCalculationRuleByName(
				computationFullName, (ElementContainer) mvkService.getEc(),
				prProxy, documentModel);
		return new ComputationRuleAndMvkService(calculationRule, mvkService);
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

	private static @NonNull ElementContainer getElementContainer(@NonNull IMVK_Service imvkService) {
		if (!(imvkService instanceof MVKService mvkService) || !(mvkService.getEc() instanceof ElementContainer ec)) {
			throw new IllegalArgumentException(
					"IMVK_Service must be acquired via 'DocumentModelService#getMvkServiceForModel'. Custom implementations are not allowed.");
		}
		return ec;
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
	private static ICalculationRule findCalculationRuleByName(String calculationFullName,
			ElementContainer ec, IProblemReporter pr, DocumentModel originForProblems) {

		Optional<ICalculationRule> calculationRuleOpt =
				ec.getAllCalculations().stream()
						.filter(c -> c.getRule().getFullName().equals(calculationFullName))
						.findAny();

		if (calculationRuleOpt.isEmpty()) { // this might be a case for an illegalargumentexception, but we cannot rule out that
			// the computation does indeed exist but has errors which were not correctly attributed to the selected computation
			// during getMvkServiceForModel
			pr.reportProblem(new MVKCoreError(
					"No computation was found for fullName or the computation is invalid: " + calculationFullName,
					Severity.ERROR,
					originForProblems));
			return null;
		}
		return calculationRuleOpt.get();
	}

	private static boolean hasValidComputationParts(
			IRuleFromComputation ruleFromComputation,
			ParserService parserService,
			ProblemReporter pr,
			@NonNull List<String> errorCodesToSuppress
	) {
		ruleFromComputation.getOriginalComputation().getCommonPrecondition().ifPresent(precondText -> parserService.checkSnippet(
				ruleFromComputation,
				precondText,
				Lenient_ConditionParser::bedingungSnippet,
				p -> pr.reportProblem(wrapWithFragmentAsOrigin(p, -1, true, precondText)),
				// the errors to suppress are not yet set in the parser and CheckPreambleImpl is not used
				parser -> errorCodesToSuppress.forEach(parser::addSuppressedWarning)));
		List<IComputation.IComputationAlternative> computationAlternatives =
				ruleFromComputation.getOriginalComputation().getComputationAlternatives();
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
									// the errors to suppress are not yet set in the parser and CheckPreambleImpl is not used
									parser -> errorCodesToSuppress.forEach(parser::addSuppressedWarning)));
			var operationText = computationAlternative.getOperation();
			parserService.checkSnippet(
					ruleFromComputation,
					operationText,
					Lenient_ConditionParser::operationSnippet,
					p -> pr.reportProblem(wrapWithFragmentAsOrigin(p, alternativeIndex, false, operationText)),
					parser -> errorCodesToSuppress.forEach(parser::addSuppressedWarning));
		}
		return !pr.hasProblemOccurred(Severity.ERROR);
	}

	private static IProblem wrapWithFragmentAsOrigin(IProblem p, int alternativeIndex, boolean isPrecondition, String fragmenText) {
		// replace the origin and keep the other attributes unchanged
		return new WrappedProblemWithSeverityLimitAndOrigin(
				p,
				Severity.ERROR, // maxSeverity = ERROR has the effect of not changing the severity
				OriginInComputationFragment.of(fragmenText, alternativeIndex, isPrecondition,
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
