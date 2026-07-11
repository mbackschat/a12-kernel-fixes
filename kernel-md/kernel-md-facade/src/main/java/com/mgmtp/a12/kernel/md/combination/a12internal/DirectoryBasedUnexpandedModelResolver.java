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

import static com.mgmtp.a12.kernel.core.base.utils.internal.FileAndResourceUtil.readFileInDirTree;

import java.io.File;
import java.io.Reader;

import com.mgmtp.a12.kernel.core.base.utils.internal.FileAndResourceUtil;
import com.mgmtp.a12.kernel.mmtypings.mm_selectionmodel_2.views.MM_SelectionModel_2;
import lombok.NonNull;

/**
 * Default implementation for {@link IUnexpandedModelResolver}, based on a directory from the classpath and its subdirectories.
 * Note: for a file-system independent implementation, see {@link UnexpandedModelResolverImpl}.
 */
public class DirectoryBasedUnexpandedModelResolver implements IUnexpandedModelResolver {
	private final UnexpandedModelResolverImpl delegate;
	private final File dir;

	private DirectoryBasedUnexpandedModelResolver(File dir) {
		this.dir = dir;
		this.delegate = new UnexpandedModelResolverImpl(this::createReader);
	}

	public static DirectoryBasedUnexpandedModelResolver ofDir(String directoryPath) {
		return new DirectoryBasedUnexpandedModelResolver(new File(directoryPath));
	}

	/**
	 * This method is not intended for use outside of kernel (test) code.
	 *
	 * Like {@link #ofDir(String)} but tries to find the models directory via the classpath based on its resource name.
	 * <em>WARNING</em> the models dir must be uniquely identified by the given resource name and must be a file system directory.
	 * i.e. there must not be any other resource (dir or other) for resourceName on the classpath, since the choice in case of
	 * multiple matches is unspecified.
	 * Also note that the resulting resolver will only resolve files in the identified directory or its subdirectories, but not
	 * other resources (e.g. from other dirs or jars) that may exist with resource names that have {@code resourceNameOfDir} as a
	 * prefix.
	 *
	 * @param resourceNameOfDir the resource name by which the directory can be found on the classpath. Must identify a file system directory (e.g. not a subfolder of a jar)
	 * @throws FileAndResourceUtil.LookupException if the models dir cannot be found
	 * based on the given resource name.
	 */
	// named "internal_*" because the behavior of this method is tricky and because the thrown
	// LookupException is internal. We only use this method in tests and don't recommend
	// it for external users.
	public static DirectoryBasedUnexpandedModelResolver kernelInternal_ofUniqueDirFromClasspath(String resourceNameOfDir) {
		File dirOrFile = FileAndResourceUtil.findSomeFileOrDirectoryThatMatchesResourceNameOnClasspath(resourceNameOfDir);
		return new DirectoryBasedUnexpandedModelResolver(dirOrFile); // later lookup would fail if it dirOrFile is not a dir but a file
	}

	@Override public @NonNull DMLike resolve(@NonNull String dmId) throws CombinationException {
		return delegate.resolve(dmId);
	}

	@Override public MM_SelectionModel_2 resolveSelectionModel(@NonNull String selId) {
		return delegate.resolveSelectionModel(selId);
	}

	private Reader createReader(@NonNull String modelId) {
		try {
			return readFileInDirTree(dir, modelId + ".json");
		} catch (FileAndResourceUtil.LookupException e) {
			throw new CombinationException(String.format("Error resolving the model file %s: %s", modelId, e.getMessage()), e);
		}
	}
}
