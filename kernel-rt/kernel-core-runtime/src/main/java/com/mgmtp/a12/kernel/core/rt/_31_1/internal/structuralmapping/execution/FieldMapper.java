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

package com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.execution;

import java.util.stream.Stream;

import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.Entity;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.document.Document;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.document.FieldInstance;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.document.GroupInstance;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.reference.FieldReference;
import com.mgmtp.a12.kernel.core.rt._31_1.internal.structuralmapping.resolution.GroupLevelResolver;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
public class FieldMapper {
	@NonNull @Getter private final Entity sourceField;
	@NonNull @Getter private final Entity targetField;
	@NonNull private final FieldReference sourceFieldReference; // from the lowest source group of the resolution strategies to the source field

	@NonNull private final GroupLevelResolver[] levelResolvers;
	private final boolean canAlwaysBeReified;

	private final Entity groupToClearOnFirstFill;

	public static FieldMapper create(Entity sourceField, Entity targetField, Entity lowestSourceGroup,
			GroupLevelResolver[] levelResolvers, Entity groupToClearOnFirstFill) {
		return new FieldMapper(
				sourceField, targetField,
				FieldReference.between(lowestSourceGroup, sourceField),
				levelResolvers,
				Stream.of(levelResolvers).allMatch(GroupLevelResolver::canAlwaysBeReified),
				groupToClearOnFirstFill);
	}

	public void execute(GroupInstance sourceGroupInstance, Document targetDocument, StructuralMappingState structuralMappingState) {
		execute(sourceFieldReference.resolveInstance(sourceGroupInstance), targetDocument, structuralMappingState);
	}

	public void execute(FieldInstance sourceFieldInstance, Document targetDocument, StructuralMappingState structuralMappingState) {
		if (!sourceFieldInstance.isFilled()) {
			return; // nothing to do
		}

		Resolution resolution = new Resolution(sourceFieldInstance, targetDocument, structuralMappingState);
		if (resolution.mustBeReified()) {
			if (!sourceFieldInstance.isFilled()) {
				return; // target already empty --> nothing to do
			}
			if (!resolution.canBeReified()) {
				return; // can't be reified --> do not map
			}
		} else {
			FieldInstance targetFieldInstance = resolution.reifyIfNecessary();
			if (targetFieldInstance.isFilled() && !targetFieldInstance.origin().isFromInput()) {
				// field instance was already mapped into --> remember this, but keep the first mapped value
				structuralMappingState.fieldInstanceWasNotMappedDueToCollision(sourceFieldInstance, targetFieldInstance);
				return; // nothing further to do
			}
		}
		resolution.clearIfNecessary();

		FieldInstance targetFieldInstance = resolution.reifyIfNecessary();
		targetFieldInstance.setValue(sourceFieldInstance.value(), new FieldMappingOrigin(sourceFieldInstance));
		structuralMappingState.fieldInstanceWasMapped(sourceFieldInstance, targetFieldInstance);
	}

	public class Resolution {
		private final FieldInstance sourceFieldInstance;
		private final StructuralMappingState structuralMappingState;

		private GroupInstance lowestResolvedGroupInstance;
		private FieldInstance targetFieldInstance;

		private Resolution(FieldInstance sourceFieldInstance, Document document, StructuralMappingState structuralMappingState) {
			this.sourceFieldInstance = sourceFieldInstance;
			this.structuralMappingState = structuralMappingState;

			this.lowestResolvedGroupInstance = document;
			resolveAllResolvableLevels();
		}

		private void resolveAllResolvableLevels() {
			for (GroupLevelResolver levelResolver : levelResolvers) {
				GroupInstance child = levelResolver.resolve(lowestResolvedGroupInstance, sourceFieldInstance,
						structuralMappingState);
				if (child == null) {
					return;
				}
				lowestResolvedGroupInstance = child;
			}
			this.targetFieldInstance = lowestResolvedGroupInstance.getChildFieldInstance(targetField);
		}

		public boolean canBeReified() {
			if (canAlwaysBeReified) {
				return true;
			}
			for (int levelIndex = lowestResolvedGroupInstance.levelIndex() + 1; levelIndex < levelResolvers.length; levelIndex++) {
				if (!levelResolvers[levelIndex].canReifyFor(sourceFieldInstance)) {
					return false;
				}
			}
			return true;
		}

		public boolean mustBeReified() {
			return targetFieldInstance == null;
		}

		public FieldInstance reifyIfNecessary() {
			if (targetFieldInstance != null) {
				return targetFieldInstance; // no reification necessary
			}

			for (int levelIndex = lowestResolvedGroupInstance.levelIndex() + 1; levelIndex < levelResolvers.length; levelIndex++) {
				lowestResolvedGroupInstance = levelResolvers[levelIndex].reify(lowestResolvedGroupInstance, sourceFieldInstance,
						structuralMappingState);
			}
			targetFieldInstance = lowestResolvedGroupInstance.getOrCreateChildFieldInstance(targetField,
					new FieldMappingOrigin(sourceFieldInstance));
			return targetFieldInstance;
		}

		public void clearIfNecessary() {
			if (groupToClearOnFirstFill == null) {
				return; // nothing to do
			}

			GroupInstance parentInstanceToClear = (GroupInstance) lowestResolvedGroupInstance.ancestorAtLevel(
					groupToClearOnFirstFill.parent().level());
			if (parentInstanceToClear == null) {
				return; // nothing to do
			}
			var instanceAndSiblings = parentInstanceToClear.getChildGroupInstancesInOrder(groupToClearOnFirstFill);

			assert instanceAndSiblings.stream().allMatch(groupInstance -> groupInstance.origin().isFromInput())
					|| instanceAndSiblings.stream().noneMatch(groupInstance -> groupInstance.origin().isFromInput())
					: "for a cleared group, it should not happen that some but not all instances are fromInput: either all are, i.e. the clear was not performed yet, or none are fromInput, because they have all been cleared before the first value was mapped to a fresh not-fromInput instance";

			// if instanceAndSiblings are not "from input", they have presumably already been cleared,
			// we don't want to clear freshly-mapped data again
			if (instanceAndSiblings.stream().allMatch(groupInstance -> groupInstance.origin().isFromInput())) {
				for (GroupInstance groupInstanceToClear : instanceAndSiblings) {
					groupInstanceToClear.remove();
					structuralMappingState.groupInstanceWasCleared(groupInstanceToClear);
				}
				lowestResolvedGroupInstance = parentInstanceToClear;
				targetFieldInstance = null;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(sourceField.fullName());
		sb.append(" -> ");
		for (var levelResolver : levelResolvers) {
			sb.append("/");
			sb.append(levelResolver);
		}
		sb.append("/");
		sb.append(targetField.shortName());
		return sb.toString();
	}
}
