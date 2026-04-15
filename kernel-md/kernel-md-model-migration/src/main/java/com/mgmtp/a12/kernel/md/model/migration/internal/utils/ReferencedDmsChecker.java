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

package com.mgmtp.a12.kernel.md.model.migration.internal.utils;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationError;
import com.mgmtp.a12.kernel.md.model.migration.internal.MigrationErrorType;
import com.mgmtp.a12.kernel.md.model.migration.internal.exception.MigrationException;

/**
 * Implementation of checks for referenced document models (rdm). The reference is defined in another document model.
 */
public class ReferencedDmsChecker {

	public static final String PORTABLE_FILE_SEPARATOR = "/";
	public static final String WINDOWS_FILE_SEPARATOR = "\\";

	private static final Pattern DM_VERSION_PATTERN = Pattern.compile("([0-9]+)\\.([0-9]+\\.[0-9]+)");
	private static final int DM_VERSION_27 = 27;

	private boolean initialized = false;
	/**
	 * Only relevant for dms whose version is lower than 27.0.0
	 */
	Set<String> rdmsWithDifferentIdAndFileName = new LinkedHashSet<>();

	private final Map<Path, JsonNode> dmsToCheck;

	/**
	 * @param dmsToCheck
	 * 		document models whose referenced document models should be checked. Must not be {@code null}.
	 */
	public ReferencedDmsChecker(Map<Path, JsonNode> dmsToCheck) {
		this.dmsToCheck = dmsToCheck;
	}

	private void initCheckResultsCache() {
		if (initialized) {
			return;
		}
		for (Map.Entry<Path, JsonNode> entry : dmsToCheck.entrySet()) {
			checkForInconsistentRefsInPre27Dms(entry.getKey(), entry.getValue());
		}
		initialized = true;
	}

	/**
	 * Checks for dm versions lower than 27 and populates nonResolvableRdms and rdmsWithDifferentIdAndFileName
	 * Checks for inconsistencies related to model references:
	 * - nonResolvableRdms: relative-path references to DMs that don't exist in dmsToCheck
	 * - rdmsWithDifferentIdAndFileName: among _only the dms that are referenced my other models in dmsToCheck_, those DMs where the
	 * constraint filename == model_id is unsatisfied. (Inconsistencies in DMs that are not referenced by any other DM are
	 * ignored)
	 * </br>
	 * For dms of version 27 or later, no checks are done.
	 */
	private void checkForInconsistentRefsInPre27Dms(Path dmPath, JsonNode dm) {
		String dmVersion = ModelJsonUtil.extractModelVersion(dm);
		if (isDmVersionGreaterEqual(dmVersion, DM_VERSION_27)) {
			return;
		}
		Set<JsonNode> referencedDms = ModelJsonUtil.extractReferencesToDms(dm);
		if (referencedDms.isEmpty()) {
			return;
		}
		Path parentPath = dmPath.getParent();
		referencedDms.forEach(rdm -> {
			String reference = ModelJsonUtil.extractReferenceFromModelReference(rdm);
			if (reference == null  // should not happen
					|| !ModelJsonUtil.isSupportedModelReference(rdm)) { // we check only what we support
				return;
			}
			// replace even if the current system's file separator is not backslash,
			// because the document model file may have been created on another system that used backslashes
			reference = reference.replace(WINDOWS_FILE_SEPARATOR, PORTABLE_FILE_SEPARATOR);
			Path rdmPath = parentPath.resolve(reference).normalize();
			JsonNode rdmNode = dmsToCheck.get(rdmPath);
			if (rdmNode == null) {
				return;
			}
			String rdmId = ModelJsonUtil.extractModelId(rdmNode);
			String rdmFileName = rdmPath.getFileName().toString().replace(ModelJsonUtil.JSON_EXTENSION, "");
			if (!rdmFileName.equals(rdmId)) {
				rdmsWithDifferentIdAndFileName.add(rdmPath.toString());
			}
		});
	}

	/**
	 * Compares major part only.
	 */
	private boolean isDmVersionGreaterEqual(String versionToCheck, int reference) {
		Matcher matcher = DM_VERSION_PATTERN.matcher(versionToCheck);
		if (matcher.matches()) {
			return Integer.parseInt(matcher.group(1)) >= reference;
		} else {
			throw new MigrationException(new MigrationError(MigrationErrorType.MODEL_VERSION_WRONG).withMessage("The format of " +
					"the document model version is not as expected."));
		}
	}

	public boolean areRdmsWithDifferentIdAndFileNamePresent() {
		initCheckResultsCache();
		return rdmsWithDifferentIdAndFileName.size() > 0;
	}

	public String getRdmsWithDifferentIdAndFileNameAsString() {
		initCheckResultsCache();
		return String.join("\n\t", rdmsWithDifferentIdAndFileName);
	}
}
