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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mgmtp.a12.kernel.core.base.utils.internal.ElementPathUtils;
import com.mgmtp.a12.kernel.core.base.utils.internal.Java8Utils;

public class NodePath {
	private final List<String> path;

	public NodePath() {
		this(Collections.emptyList());
	}

	public NodePath(List<String> path) {
		this.path = path;
	}

	public NodePath(String name) {
		this.path = List.of(name);
	}

	public NodePath addSub(String subPath) {
		List<String> newPath = new ArrayList<>(path);
		newPath.add(subPath);
		return new NodePath(newPath);
	}

	public NodePath addSub(NodePath subPath) {
		List<String> newPath = new ArrayList<>(path);
		newPath.addAll(subPath.path);
		return new NodePath(newPath);
	}

	public NodePath getParentPath(){
		return new NodePath(Java8Utils.getAllButLast(this.path));
	}

	public String toString() {
		return "/" + String.join("/", this.path);
	}

	public NodePath removeRootGroup() {
		return new NodePath(path.stream().skip(1).toList());
	}

	public String getElementName() {
		return path.get(path.size() - 1);
	}

	public String relativize(NodePath basePath) {
		return ElementPathUtils.relPath(this.path, basePath.path);
	}

	public NodePath removeElementName() {
		List<String> newPath = new ArrayList<>(path);
		if (!newPath.isEmpty()) {
			newPath.remove(newPath.size() - 1);
		}
		return new NodePath(newPath);
	}
}
