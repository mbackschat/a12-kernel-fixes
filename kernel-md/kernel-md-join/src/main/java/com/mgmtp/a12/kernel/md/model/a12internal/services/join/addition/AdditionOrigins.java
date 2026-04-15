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

package com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Getter public class AdditionOrigins implements IAdditionOrigins {

	private final Set<String> elementsInRoot = new LinkedHashSet<>();
	private final Set<String> elementsInAdditive = new LinkedHashSet<>();
	private final Set<String> elementsInBoth = new LinkedHashSet<>();
	private final Set<String> elementsInRootCompletelyOverwritten = new LinkedHashSet<>();
	private final Set<String> typeDefsInRoot = new LinkedHashSet<>();
	private final Set<String> typeDefsInAdditive = new LinkedHashSet<>();
	private final Set<String> typeDefsInBoth = new LinkedHashSet<>();
	private final Map<String, IAnnotationsOrigins> commonElementsWithJoinedAnnotations = new LinkedHashMap<>();
	private final Map<String, String> idsInRootByIdInJoined = new LinkedHashMap<>();
	private final Map<String, String> idsInAdditiveByIdInJoined = new LinkedHashMap<>();

	@Override public void addRootElement(String id) {
		if (!elementsInAdditive.contains(id) && !elementsInBoth.contains(id) && !elementsInRootCompletelyOverwritten.contains(id)) {
			elementsInRoot.add(id);
		}
	}

	@Override public void addAdditiveElement(String id) {
		if (!elementsInRoot.contains(id) && !elementsInBoth.contains(id)) {
			elementsInAdditive.add(id);
		}
	}

	@Override public void addCommonElement(String id) {
		if (!elementsInRoot.contains(id) && !elementsInAdditive.contains(id)) {
			elementsInBoth.add(id);
		}
	}

	@Override public void addCompletelyOverwrittenRootElement(String id) {
		elementsInRootCompletelyOverwritten.add(id);
	}

	@Override public void addRootTypeDef(String id) {
		if (!typeDefsInAdditive.contains(id) && !typeDefsInBoth.contains(id)) {
			typeDefsInRoot.add(id);
		}
	}

	@Override public void addAdditiveTypeDef(String id) {
		if (!typeDefsInRoot.contains(id) && !typeDefsInBoth.contains(id)) {
			typeDefsInAdditive.add(id);
		}
	}

	@Override public void addCommonTypeDef(String id) {
		if (!typeDefsInRoot.contains(id) && !typeDefsInAdditive.contains(id)) {
			typeDefsInBoth.add(id);
		}
	}

	@Override public void addAnnotationsForCommon(String id, IAnnotationsOrigins origins) {
		commonElementsWithJoinedAnnotations.put(id, origins);
	}

	public void addIdInRootByIdInJoined(String idInJoined, String idInRoot) {
		idsInRootByIdInJoined.put(idInJoined, idInRoot);
	}

	@Override public void addIdInAdditiveByIdInJoined(String idInJoined, String idInAdditive) {
		idsInAdditiveByIdInJoined.put(idInJoined, idInAdditive);
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AdditionOrigins that))
			return false;
		return Objects.equals(elementsInRoot, that.elementsInRoot) &&
				Objects.equals(elementsInAdditive, that.elementsInAdditive) &&
				Objects.equals(elementsInBoth, that.elementsInBoth) &&
				Objects.equals(elementsInRootCompletelyOverwritten, that.elementsInRootCompletelyOverwritten) &&
				Objects.equals(typeDefsInRoot, that.typeDefsInRoot) &&
				Objects.equals(typeDefsInAdditive, that.typeDefsInAdditive) &&
				Objects.equals(typeDefsInBoth, that.typeDefsInBoth) &&
				Objects.equals(commonElementsWithJoinedAnnotations, that.commonElementsWithJoinedAnnotations) &&
				Objects.equals(idsInRootByIdInJoined, that.idsInRootByIdInJoined) &&
				Objects.equals(idsInAdditiveByIdInJoined, that.idsInAdditiveByIdInJoined);
	}

	@Override public int hashCode() {
		return Objects.hash(elementsInRoot, elementsInAdditive, elementsInBoth, elementsInRootCompletelyOverwritten, typeDefsInRoot,
				typeDefsInAdditive, typeDefsInBoth, commonElementsWithJoinedAnnotations, idsInRootByIdInJoined,
				idsInAdditiveByIdInJoined);
	}

	@Override public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
