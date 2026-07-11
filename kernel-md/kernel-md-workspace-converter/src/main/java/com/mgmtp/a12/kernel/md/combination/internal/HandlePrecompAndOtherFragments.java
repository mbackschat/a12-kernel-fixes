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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.mgmtp.a12.dataservices.wcf.WorkspaceFactory;
import com.mgmtp.a12.dataservices.wcf.domain.ModelTuple;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.kernel.core.base.utils.internal.Consumer2ProblemReporter;
import com.mgmtp.a12.kernel.md.datatransfer.a12internal.mappingmodel.MappingModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.internal.betterbuilders.HeaderBuilderUtil;
import com.mgmtp.a12.kernel.md.model.internal.impl.RankedNotificationImpl;
import com.mgmtp.a12.kernel.md.serializer.model.a12internal.services.DocumentModelSerializer;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;

import lombok.NonNull;

/**
 * Post-expansion step: handles document model fragments (additive-document) in the workspace.
 * <p>
 * After expansion, additive-document fragments are no longer needed — except when they are referenced as
 * precomputation fragments by mapping models. Those are retained but their modelType is changed to
 * {@value #RUNTIME_PRECOMP_FRAGMENT_MODEL_TYPE} so that e.g. Data Services does not attempt regular DM processing steps that would
 * fail for fragments.
 * All other additive-document fragments are removed from the workspace.
 */
public class HandlePrecompAndOtherFragments {

	private static final String RUNTIME_PRECOMP_FRAGMENT_MODEL_TYPE = "runtime_mapping_precompfragment_document";
	private static final String ADDITIVE_DOCUMENT_ANNOTATION = "additive-document";
	private static final String MAPPING_MODEL_TYPE = "mapping";

	private HandlePrecompAndOtherFragments() {}

	public static @NonNull Workspace handle(@NonNull Workspace workspace, Consumer<? super RankedNotification> notificationConsumer) {
		Map<String, ModelTuple> models = workspace.getModels();

		Set<String> precompFragmentIds = collectPrecompFragmentIds(models, notificationConsumer);

		// Iterate over a snapshot of the keys, since we mutate the map
		for (String modelId : new HashSet<>(models.keySet())) {
			ModelTuple tuple = models.get(modelId);
			if (!isAdditiveDocument(tuple)) {
				continue;
			}
			if (precompFragmentIds.contains(modelId)) {
				// Retain the fragment but change its modelType
				Header newHeader = HeaderBuilderUtil.toBuilder(tuple.getHeader())
						.withModelType(RUNTIME_PRECOMP_FRAGMENT_MODEL_TYPE)
						.build();
				// set the header in the model as well
				models.put(modelId, WorkspaceFactory.getInstance().createModelTuple(newHeader, swapHeaderInDM(newHeader, tuple.getContent(), notificationConsumer)));
			} else {
				models.remove(modelId);
			}
		}

		return workspace;
	}

	private static @NonNull String swapHeaderInDM(@NonNull Header newHeader, @NonNull String content, Consumer<? super RankedNotification> notificationConsumer) {
		Reader reader = new StringReader(content);
		DocumentModelSerializer serializer = new DocumentModelSerializer();
		DocumentModel dm = serializer.deserialize(reader);
		dm.setHeader(newHeader);

		Writer writer = new StringWriter();
		try {
			serializer.serialize(dm, writer, new Consumer2ProblemReporter(notificationConsumer));
		} catch (IOException e) {
			notificationConsumer.accept(RankedNotificationImpl.of(e.getMessage(), Severity.ERROR, dm));
		}
		return writer.toString();
	}

	/**
	 * Collects the IDs of all precomputation fragment DMs referenced by mapping models in the workspace.
	 * <p>
	 * [A12K-4178]: when A12K-4178 is done, the fragment reference should be obtainable simply from the modelReferences in the mapping model's
	 * header, so we'll no longer need to deserialize the mapping model to check.
	 */
	private static Set<String> collectPrecompFragmentIds(Map<String, ModelTuple> models, Consumer<? super RankedNotification> notificationConsumer) {
		Set<String> result = new HashSet<>();
		for (ModelTuple tuple : models.values()) {
			if (!MAPPING_MODEL_TYPE.equals(tuple.getHeader().getModelType())) {
				continue;
			}
			MM_MappingModel_2 mappingModel = MappingModelService.deserialize(
					new StringReader(tuple.getContent()),
					notificationConsumer
			);
			String precompDmId = mappingModel._at(MM_MappingModel_2._pointer().content().preComputationFragment().dmId());
			if (precompDmId != null && !precompDmId.isEmpty()) {
				result.add(precompDmId);
			}
		}
		return result;
	}

	private static boolean isAdditiveDocument(ModelTuple tuple) {
		var annotations = tuple.getHeader().getAnnotations();
		if (annotations == null) {
			return false;
		}
		return annotations.stream()
				.anyMatch(a -> ADDITIVE_DOCUMENT_ANNOTATION.equals(a.getName()));
	}
}
