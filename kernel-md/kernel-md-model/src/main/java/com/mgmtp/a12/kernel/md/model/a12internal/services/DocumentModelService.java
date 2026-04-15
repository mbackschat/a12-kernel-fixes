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

import java.io.IOException;
import java.io.Writer;
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
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IVariantFilter;
import com.mgmtp.a12.kernel.md.model.api.services.DocumentModelExpansionException;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelReferenceResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelCollapseService;
import com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandImportService;
import com.mgmtp.a12.kernel.md.model.internal.service.DocumentModelExpandService;
import com.mgmtp.a12.kernel.md.model.internal.service.ExternalDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.mapping.IRuleFromComputation;
import com.mgmtp.a12.kernel.md.model.internal.service.reduction.DocumentModelReduceService;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DMProblemUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.DocumentModelServiceUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.ElementUtils;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.WrappedProblemWithSeverityLimitAndOrigin;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.ElementWrapper;
import com.mgmtp.a12.kernel.md.serializer.model.a12internal.services.DocumentModelSerializer;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.ExternalDocumentModelSerializerService;
import com.mgmtp.a12.kernel.md.utils.OnlyForA12InternalUsage;
import com.mgmtp.a12.model.header.ModelReference;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * This service offers actions which can be run on a {@link DocumentModel}.
 */
@OnlyForA12InternalUsage
@OnlyForUsage
public class DocumentModelService {

	// ==================================
	// handling of referenced models
	// ==================================

	/**
	 * See
	 * {@link IDocumentModelService#expand(IDocumentModel,
	 * com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelReferenceResolver)}
	 *
	 * @deprecated use {@code CombinationModelService#expand} in order to get automatic recursive support for {@code CombinationModel}s too.
	 */
	@Deprecated(since = "30.2.0")
	public void expand(DocumentModel documentModel, DocumentModelReferenceResolver resolver) {
		ProblemReporter pr = new ProblemReporter();
		expand(documentModel, resolver, pr);
		if (pr.hasProblemOccurred(Severity.ERROR)) {
			throw new DocumentModelExpansionException("Following problems occurred during expansion of document model '"
					+ documentModel.getHeader().getId() + "':" + System.lineSeparator() + DMProblemUtils.join(pr.getProblems()));
		}
	}

	/**
	 * See {@link IDocumentModelService#expand(IDocumentModel, IDocumentModelReferenceResolver)}.
	 *
	 * @param pr
	 * 		Errors during expansion (due to an invalid {@link DocumentModel}) will be reported to {@link IProblemReporter} pr,
	 * 		instead of throwing an exception. If the origin of the problem could be determined, this can be accessed via
	 *        {@link IProblem#getSource()} and it will be the reference of the corresponding {@link Element}.
	 * @deprecated use {@code CombinationModelService#expand} in order to get automatic recursive support for {@code CombinationModel}s too.
	 */
	@Deprecated(since = "30.2.0")
	public void expand(DocumentModel documentModel, DocumentModelReferenceResolver resolver, IProblemReporter pr) {
		DocumentModelExpandService.expand(documentModel, resolver, pr);
	}

	/**
	 * See {@link IDocumentModelService#collapse(IDocumentModel)}
	 */
	public void collapse(DocumentModel documentModel) {
		DocumentModelCollapseService.collapse(documentModel);
	}

	/**
	 * See
	 * {@link IDocumentModelService#extractImportedTypeDefinitions(ModelReference,
	 * com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelReferenceResolver,
	 * List)}
	 */
	public List<FieldTypeDefinition> extractImportedTypeDefinitions(ModelReference modelRef,
			DocumentModelReferenceResolver resolver, List<Locale> supportedLocales) {
		ProblemReporter pr = new ProblemReporter();
		List<FieldTypeDefinition> result =
				new DocumentModelExpandImportService(resolver, supportedLocales, pr).extractImportedTypeDefinitions(modelRef);
		if (pr.hasProblemOccurred(Severity.ERROR)) {
			throw new DocumentModelExpansionException("Following problems occurred during extraction of typedefinitions of " +
					"document model '" + modelRef.getReference() + "':" + System.lineSeparator() +
					DMProblemUtils.join(pr.getProblems()));
		}
		return result;
	}
	// ==================================
	// utilities
	// ==================================

	/**
	 * For more information see {@link IMVK_Service#getApplicationConditionNames(Collection, IProblemReporter)}.
	 */
	public List<String> getApplicationConditionNames(IMVK_Service imvkService, Collection<Rule> rules, IProblemReporter pr) {
		List<IRule> irules = rules.stream().map(rule -> findIRule(imvkService, rule)).toList();
		return imvkService.getApplicationConditionNames(irules, pr);
	}

	/**
	 * Serializes the given {@link DocumentModel} into the given {@link Writer}, calling:
	 * <ol>
	 *     <li>{@link DocumentModelService#collapse(DocumentModel)}</li>
	 *     <li>{@link DocumentModelSerializer#serialize(DocumentModel, Writer, IProblemReporter)}</li>
	 * </ol>
	 */
	public void serialize(DocumentModel documentModel, Writer writer, IProblemReporter pr) throws IOException {
		collapse(documentModel);
		new DocumentModelSerializer().serialize(documentModel, writer, pr);
	}

	/**
	 * For more information see {@link IMVK_Service#getDirectlyReferencedEntities(IRule rule, IProblemReporter pr)}.
	 */
	public Set<IEntity> getDirectlyReferencedEntities(IMVK_Service imvkService, Rule rule, IProblemReporter pr) {
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
	public String getPath(Element element) {
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
	 * </ul>
	 */
	public void checkConsistency(DocumentModel documentModel, IProblemReporter pr) {
		new ExternalDocumentModelService().checkConsistency(
				convertToExternal(documentModel),
				n -> pr.reportProblem(new MVKCoreError(
						n.getMessage(),
						n.getSeverity(),
						unwrapProblemSource(n.getSource()))));
	}

	private Object unwrapProblemSource(Object source) {
		return switch (source) {
			case DocumentModelWrapper documentModelWrapper -> documentModelWrapper.getDocumentModel();
			case ElementWrapper elementWrapper -> elementWrapper.getElement();
			case null -> null;
			default -> source;
		};
	}

	/**
	 * Maps a {@link DocumentModel} to an {@link ElementContainer}. ExtMP-specific content in the {@link DocumentModel} is
	 * considered by this method.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} to map. May not be {@code null}.
	 * @param variant
	 * 		the variant to apply to the {@link DocumentModel} (see {@link IVariantFilter}. May
	 * 		be {@code null}.
	 * @param considerNotInD
	 * 		- if {@code true} ExtMP-specific information is considered otherwise it is ignored.
	 * @return an instance of {@link ElementContainer} that contains the same modeling configuration
	 * 		as the {@link DocumentModel} that was passed in.
	 *
	 * @throws IllegalArgumentException
	 * 		if the DocumentModel cannot be converted due to an illegal format
	 */
	public ElementContainer convert(DocumentModel documentModel, String variant, boolean considerNotInD) {
		IDocumentModel extDocumentModel = convertToExternal(documentModel);
		return DocumentModelServiceUtils.convert(extDocumentModel, variant, considerNotInD);
	}

	/**
	 * Maps a {@link IDocumentModel} to an {@link ElementContainer}. ExtMP-specific content in the {@link DocumentModel} is
	 * considered by this method.
	 *
	 * @param documentModel
	 * 		the {@link IDocumentModel} to map. May not be {@code null}.
	 * @param variant
	 * 		the variant to apply to the {@link IDocumentModel} (see {@link IVariantFilter}. May
	 * 		be {@code null}.
	 * @param considerNotInD
	 * 		- if {@code true} ExtMP-specific information is considered otherwise it is ignored.
	 * @return an instance of {@link ElementContainer} that contains the same modeling configuration
	 * 		as the {@link IDocumentModel} that was passed in.
	 *
	 * @throws IllegalArgumentException
	 * 		if the DocumentModel cannot be converted due to an illegal format
	 */
	public ElementContainer convert(IDocumentModel documentModel, String variant, boolean considerNotInD) {
		return DocumentModelServiceUtils.convert(documentModel, variant, considerNotInD);
	}

	/**
	 * Maps a {@link DocumentModel} to an {@link ElementContainer}.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} to map. May not be {@code null}.
	 * @param variant
	 * 		the variant to apply to the {@link DocumentModel} (see {@link IVariantFilter}. May
	 * 		be {@code null}.
	 * @return an instance of {@link ElementContainer} that contains the same modeling configuration
	 * 		as the {@link DocumentModel} that was passed in.
	 *
	 * @throws IllegalArgumentException
	 * 		if the DocumentModel cannot be converted due to an illegal format
	 */
	public ElementContainer convert(DocumentModel documentModel, String variant) {
		return convert(documentModel, variant, false);
	}

	/**
	 * Maps a {@link IDocumentModel} to an {@link ElementContainer}.
	 *
	 * @param documentModel
	 * 		the {@link IDocumentModel} to map. May not be {@code null}.
	 * @param variant
	 * 		the variant to apply to the {@link IDocumentModel} (see {@link IVariantFilter}. May
	 * 		be {@code null}.
	 * @return an instance of {@link ElementContainer} that contains the same modeling configuration
	 * 		as the {@link IDocumentModel} that was passed in.
	 *
	 * @throws IllegalArgumentException
	 * 		if the DocumentModel cannot be converted due to an illegal format
	 */
	public ElementContainer convert(IDocumentModel documentModel, String variant) {
		return convert(documentModel, variant, false);
	}

	/**
	 * Creates a {@link IMVK_Service} from the {@link DocumentModel}.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} for the service to work on. May not be {@code null}.
	 * @param variant
	 * 		the variant to apply to the {@link DocumentModel} (see {@link IVariantFilter}. May
	 * 		be {@code null}.
	 * @param pr
	 * 		a {@link IProblemReporter} to which problems will be reported if they arise during conversion of the documentModel
	 * @return the {@link IMVK_Service} instance for the given model.
	 */
	public IMVK_Service getMvkServiceForModel(DocumentModel documentModel, String variant, IProblemReporter pr) {
		return getMvkServiceForModelInternal(documentModel, variant, pr);
	}

	/**
	 * Like {@link #getMvkServiceForModel(DocumentModel, String, IProblemReporter)}, but throws a RuntimeException if problems
	 * occur during conversion.
	 */
	public IMVK_Service getMvkServiceForModel(DocumentModel documentModel, String variant) {
		return getMvkServiceForModel(
				documentModel,
				variant,
				p -> {
					throw new IllegalArgumentException("Error during creation of IMVK_Service for model "
							+ documentModel.getHeader().getId() + " and no problemReporter was given:" + p.getMessage());
				});
	}

	private MVKService getMvkServiceForModelInternal(DocumentModel documentModel, String variant, IProblemReporter pr) {
		IDocumentModel extDocModel = convertToExternal(documentModel);
		ElementContainer ec = DocumentModelServiceUtils.convert(extDocModel, variant, pr);
		IServiceConfiguration serviceConfiguration = DocumentModelServiceUtils.createServiceConfiguration(extDocModel, ec);
		return new MVKService(serviceConfiguration);
	}

	/**
	 * Creates a {@link ICalculationService} from the {@link DocumentModel}.
	 *
	 * @param documentModel
	 * 		the {@link DocumentModel} for the service to work on. May not be {@code null}.
	 * @param variant
	 * 		the variant to apply to the {@link DocumentModel} (see {@link IVariantFilter}. May
	 * 		be {@code null}.
	 * @return the {@link ICalculationService} instance for the given model.
	 */
	public ICalculationService getCalculationServiceForModel(DocumentModel documentModel, String variant) {
		ElementContainer ec = convert(documentModel, variant);
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
	 * @param variant
	 * 		variant as described in {@link #getMvkServiceForModel(DocumentModel, String)}
	 * @param pr
	 * 		an IProblemReporter. You can use {@link OriginInComputationFragment#of(IProblem)} to obtain details about the
	 * 		origin of the problem within the computation.
	 * @return {@code true} if no problems occurred during conversion to an {@link IEC} and subsequent parsing of the computation,
	 * 		otherwise {@code false}.
	 */
	public boolean isValidComputation(
			String computationFullName,
			DocumentModel documentModel,
			String variant,
			IProblemReporter pr
	) {
		return isValidComputation(computationFullName, documentModel, variant, pr, Collections.emptyList());
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
	 * @param variant
	 * 		variant as described in {@link #getMvkServiceForModel(DocumentModel, String)}
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
			String variant,
			IProblemReporter pr,
			@NonNull List<String> errorCodesToSuppress
	) {
		ComputationRuleAndMvkService mvkServiceAndRule =
				deriveCalculationRuleAndMvkServiceForDM(computationFullName, documentModel, variant, pr);
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
			String variant,
			IProblemReporter pr
	) {
		ProblemReporter prProxy = new ProblemReporterProxy(pr);
		MVKService mvkService = getMvkServiceForModelInternal(documentModel, variant, p -> prProxy.reportProblem(
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
	public boolean hasValidConditionText(IMVK_Service imvkService, Rule rule, IProblemReporter pr) {
		return imvkService.hasValidConditionText(findIRule(imvkService, rule), pr);
	}

	/**
	 * For more information see
	 * {@link IRuleConsistencyCheck#hasValidErrorText(IRule, Collection, boolean, IProblemReporter)}.
	 */
	public boolean hasValidErrorText(IMVK_Service imvkService, Rule rule, Collection<Locale> locales,
			boolean extendProblemMessage, IProblemReporter pr) {
		return imvkService.hasValidErrorText(findIRule(imvkService, rule), locales, extendProblemMessage, pr);
	}

	private ElementContainer getElementContainer(IMVK_Service imvkService) {
		if (!(imvkService instanceof MVKService mvkService) || !(mvkService.getEc() instanceof ElementContainer ec)) {
			throw new IllegalArgumentException(
					"IMVK_Service must be acquired via 'DocumentModelService#getMvkServiceForModel'. Custom implementations are not allowed.");
		}
		return ec;
	}

	private IRule findIRule(IMVK_Service imvkService, Rule rule) {
		return findIRuleByPath(getPath(rule), getElementContainer(imvkService));
	}

	private IRule findIRuleForComputation(IMVK_Service imvkService, Computation computation) {
		ICalculationRule calculationRule = findICalculationRuleByPath(getPath(computation),
				getElementContainer(imvkService));
		return calculationRule != null ? calculationRule.getRule() : null;
	}

	private static IRule findIRuleByPath(String rulePath, ElementContainer elementContainer) {
		return elementContainer.getAllRules().stream()
				.filter(rule -> rule.getFullName().equals(rulePath))
				.findFirst()
				.orElse(null);
	}

	private static ICalculationRule findICalculationRuleByPath(String calculationPath,
			ElementContainer elementContainer) {
		return elementContainer.getAllCalculations().stream()
				.filter(calculation -> calculation.getRule().getFullName().equals(calculationPath))
				.findFirst()
				.orElse(null);
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

	/**
	 * See {@link #isValidComputation(String, DocumentModel, String, IProblemReporter)}
	 */
	public boolean isValidComputation(String computationFullName,
			DocumentModel documentModel, IProblemReporter pr) {
		return isValidComputation(computationFullName, documentModel, null, pr, Collections.emptyList());
	}

	/**
	 * See {@link #isValidComputation(String, DocumentModel, String, IProblemReporter)}
	 */
	public boolean isValidComputation(String computationFullName,
			DocumentModel documentModel, IProblemReporter pr, List<String> errorCodesToSuppress) {
		return isValidComputation(computationFullName, documentModel, null, pr, errorCodesToSuppress);
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
				deriveCalculationRuleAndMvkServiceForDM(computationFullName, documentModel, null, pr);

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
				deriveCalculationRuleAndMvkServiceForDM(computationFullName, documentModel, null, pr);

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
