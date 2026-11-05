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
package com.mgmtp.a12.kernel.md.combination.internal;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mgmtp.a12.dataservices.wcf.domain.ModelTuple;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.dataservices.wcf.WorkspaceFactory;
import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationException;
import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationModelService;
import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationModelService.CombinationModelExpandParams;
import com.mgmtp.a12.kernel.md.combination.a12internal.IUnexpandedModelResolver;
import com.mgmtp.a12.kernel.md.combination.a12internal.UnexpandedModelResolverImpl;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.DocumentModelSerializerImpl;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views.MM_CombinationModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;

/**
 * Handles DM/CM expansion: document models (with includes) and combination models
 * are expanded to plain self-contained document models.
 * <p>
 * Models that expand successfully are replaced with their expanded version.
 * Models whose expansion fails are removed from the workspace.
 * Other model types are left untouched.
 */
public class ExpandWorkspaceModels {

	private ExpandWorkspaceModels() {}

	public static @NonNull Workspace expand(@NonNull Workspace workspace, @NonNull Consumer<? super RankedNotification> notificationConsumer) {
		Map<String, ModelTuple> models = workspace.getModels(); // note: ModelTuple is mutable, so we have to be careful with the
		// order in which we read vs update (even though some modifications might not actually matter, like fetching an expanded vs
		// original dm while recursively expanding includes)

		IUnexpandedModelResolver resolver = buildResolver(models);
		Map<String, Optional<IDocumentModel>> expansionResults = expandAll(models, resolver, notificationConsumer);
		// update the workspace ModelTuple in-place, after expansion has fully completed
		// (must be done in-place, since the workspace has no withModels method).
		applyResults(models, expansionResults, notificationConsumer);
		return workspace; // Workspace currently lacks a setModels method, so we must update the models map in-place
	}

	private static IUnexpandedModelResolver buildResolver(Map<String, ModelTuple> models) {
		return new UnexpandedModelResolverImpl(
				modelId -> {
					ModelTuple tuple = models.get(modelId);
					// Note: at some point in the future we might also be interested in entries from the "files" of the Workspace,
					// i.e. data that has not been parsed as an a12 model.
					if (tuple == null) {
						// CombinationException seems to be the intended way for the resolver to signal if the model does not exist.
						// The exception from here should then have the effect that expandWithErrorCheck returns empty for
						// any model that (transitively) references a missing model (and can therefore not be expanded). This seems
						// correct (modulo the still-to-be-defined error reporting, which could be handled in expandWithErrorCheck
						// for this case)
						// NOTE: the CombinationException will be caught by CombinationModelService#expand(DM/CM), which will report
						// an error and return empty.
						throw new CombinationException("Model not found in workspace: " + modelId);
					}
					assert modelId.equals(tuple.getHeader().getId());
					return new StringReader(tuple.getContent());
				}
		);
	}

	/**
	 * @param models: not mutated in-place by this method (neither the map nor the ModelTuples)
	 * @return a map with entries as follows: Optional.empty() for all ids that should be removed from the workspace; and a
	 * non-empty optional for all that should be added or updated.
	 */
	private static Map<String, Optional<IDocumentModel>> expandAll(
			Map<String, ModelTuple> models,
			IUnexpandedModelResolver resolver,
			Consumer<? super RankedNotification> notificationConsumer
	) {
		Map<String, Optional<IDocumentModel>> results = new LinkedHashMap<>();
		for (Map.Entry<String, ModelTuple> entry : models.entrySet()) {
			String modelType = entry.getValue().getHeader().getModelType();
			// currently, the only case where we want to remove (instead of replace) a model is if its expansion failed. But in the
			// future it could be the case that a model is to be removed because e.g. it was only used as a referenced model in a
			// combination model or include, and is not itself an entrypoint of the workspace (once workspaces have an explicit set
			// of entrypoints)
			// The below expansion branches should already put an Optional.empty()
			// into the map exactly iff the expansion failed.
			if (DocumentModel.MODEL_TYPE_DOCUMENT.equals(modelType)) {
				IDocumentModel dm = deserializeDocumentModel(entry.getValue());
				results.put(entry.getKey(), expandWithErrorCheck(notificationConsumer,
						params -> CombinationModelService.expand(dm, resolver, params)));

			} else if (CombinationModelService.MODEL_TYPE.equals(modelType)) {
				MM_CombinationModel_1 cm = deserializeCombinationModel(entry.getValue(), notificationConsumer);
				results.put(entry.getKey(), expandWithErrorCheck(notificationConsumer,
						params -> CombinationModelService.expand(cm, resolver, params)));
			}
			// other model types can be omitted from results, so they remain untouched (only adding Optional.empty() would remove
			// them from the final workspace)
		}
		return results;
	}

	/**
	 * Expands a model using the given expand function, tracking errors via the notification receiver.
	 * Returns {@link Optional#empty()} if an error occurred during expansion,
	 * even if the expand function itself returned a non-empty (but likely incomplete) result.
	 */
	private static Optional<IDocumentModel> expandWithErrorCheck(
			Consumer<? super RankedNotification> notificationConsumer,
			Function<CombinationModelExpandParams, Optional<IDocumentModel>> expandFn
	) {
		boolean[] errorOccurred = { false };
		CombinationModelExpandParams params = CombinationModelExpandParams.builder().notificationReceiver(
				n -> {
					if (n.getSeverity().isAtLeastAsSevereAs(Severity.ERROR)) {
						errorOccurred[0] = true;
					}
					notificationConsumer.accept(n);
				}
		).build(); // all other params, mainly locale for error messages, left at default values for now
		var result = expandFn.apply(params);
		return errorOccurred[0] ? Optional.empty() : result;
	}

	/**
	 * Mutates the models map in-place according to the entries from entryUpdates. Optional.empty() values in entryUpdates
	 * correspond to deletions from models.
	 */
	private static void applyResults(
			Map<String, ModelTuple> models,
			Map<String, Optional<IDocumentModel>> entryUpdates,
			Consumer<? super RankedNotification> notificationConsumer
	) {
		for (Map.Entry<String, Optional<IDocumentModel>> entry : entryUpdates.entrySet()) {
			String serializedDm;
			if (entry.getValue().isPresent()
					&& null != (serializedDm = serializeDocumentModel(entry.getValue().get(), notificationConsumer))  // != null -> successfully serialized, no serialization-preventing inconsistencies
			) {
				models.put(entry.getKey(), WorkspaceFactory.getInstance().createModelTuple(
						// create fresh ModelTuple because a) if one already exists for this key in the models map, the Header might
						// be / is likely stale and b) in the general case we should support cases where an entry is added for a key
						// that was not previously present at all
						entry.getValue().get().getHeader(),
						serializedDm));
			} else {
				models.remove(entry.getKey());
			}
		}
		// entries in models are left untouched if there is no corresponding entry in entryUpdates
	}

	/**
	 * @return the serialized model, or {@code null} if serialization failed.
	 */
	private static String serializeDocumentModel(IDocumentModel documentModel, Consumer<? super RankedNotification> notificationConsumer) {
		StringWriter writer = new StringWriter();
		try {
			boolean[] errorOccurred = {false};
			new DocumentModelSerializerImpl().serialize(documentModel, writer,
					notification -> {
						if (notification.getSeverity().isAtLeastAsSevereAs(Severity.ERROR)) {
							errorOccurred[0] = true;
						}
						notificationConsumer.accept(notification);
					});
			return !errorOccurred[0] ? writer.toString() : null;
		} catch (IOException e) {
			assert false : "unexpected IOException: " + e; // should not happen, but let's not crash prod over this
			return null;
		}
	}

	private static IDocumentModel deserializeDocumentModel(ModelTuple tuple) {
		try {
			return new DocumentModelSerializerImpl().deserialize(new StringReader(tuple.getContent()));
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to deserialize document model: " + tuple.getHeader().getId(), e);
		}
	}

	private static MM_CombinationModel_1 deserializeCombinationModel(ModelTuple tuple, Consumer<? super RankedNotification> notificationConsumer) {
		return CombinationModelService.deserialize(
				new StringReader(tuple.getContent()),
				notificationConsumer
		);
	}
}
