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
package com.mgmtp.a12.kernel.md.combination.a12internal;

import java.util.function.Consumer;

import com.mgmtp.a12.dataservices.wcf.WorkspaceConverter;
import com.mgmtp.a12.dataservices.wcf.domain.Workspace;
import com.mgmtp.a12.kernel.md.combination.internal.ExpandWorkspaceModels;
import com.mgmtp.a12.kernel.md.combination.internal.HandlePrecompAndOtherFragments;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;

/**
 * A {@link WorkspaceConverter} that handles runtime model conversion for all model types that are maintained by kernel.
 * <p>
 * This currently performs two conversion steps:
 * <ol>
 *   <li>Expansion: document models (with includes) and combination models are expanded to plain self-contained
 *       document models. Models that expand successfully are replaced; those that fail are removed.</li>
 *   <li>Fragment handling: additive-document fragments are removed from the workspace, except those referenced as
 *       precomputation fragments by mapping models — those are retained with modelType changed to
 *       "runtime_mapping_precompfragment_document".</li>
 * </ol>
 * Other model types are left untouched.
 *
 * @implNote when model types other than DM/CM are handled in the future, the implementation should probably delegate to modularized
 * conversion code, but this converter should probably remain a single entry point to which a KernelModelsWorkspaceConverterFacade
 * in the runtime-model-conversion repo can delegate. Having a single converter for all kernel models also has the advantage that
 * kernel conversions can happen atomically, so that no potential additional externally-supplied converters can break invariants
 * between the separate kernel conversion operations.
 */
// intentionally not annotaded with @WcfConverter here, to avoid pulling spring into kernel's dependencies.
// The annotation will be put on a delegating KernelModelsWorkspaceConverterFacade class or similar in the runtime-model-conversion
// repo
public class KernelModelsWorkspaceConverter implements WorkspaceConverter {

	// error handling is not yet clearly defined, but this consumer should give us some flexibility to configure error handling
	// behavior in the delegating consumer that will be put in the runtime-model-conversion repo
	private final @NonNull Consumer<? super RankedNotification> notificationConsumer;

	/**
	 * Creates a converter that silently ignores all notifications. If expansion of some models fails, those models
	 * are removed from the workspace and conversion continues with the remaining models. No errors are reported.
	 *
	 * @see #KernelModelsWorkspaceConverter(Consumer)
	 */
	public KernelModelsWorkspaceConverter() {
		this(n -> {});
	}

	/**
	 * @param notificationConsumer receives notifications (errors, warnings, etc.) from the conversion steps.
	 *        If the consumer passively consumes or ignores the notifications, models whose expansion fails are removed from the
	 *        workspace and conversion continues. The consumer can also be used to abort conversion early by throwing
	 *        an exception (e.g. on {@code ERROR} severity); in that case the exception propagates to the caller,
	 *        but the workspace may already be partially converted.
	 *        Must be thread-safe if this converter is used concurrently.
	 */
	public KernelModelsWorkspaceConverter(@NonNull Consumer<? super RankedNotification> notificationConsumer) {
		this.notificationConsumer = notificationConsumer;
	}

	@Override
	public @NonNull Workspace convert(@NonNull Workspace workspace) {
		ExpandWorkspaceModels.expand(workspace, notificationConsumer);
		HandlePrecompAndOtherFragments.handle(workspace, notificationConsumer);
		return workspace;
	}
}
