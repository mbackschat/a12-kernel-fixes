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

package com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.ICodeOutput;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.STParameterClassBase;
import com.mgmtp.a12.kernel.core.codegen.internal.generator.backingbeans.utils.StringTemplateGroup;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IEntityId;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.IParseTreeEntity;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.PathNotationConfig;
import com.mgmtp.a12.kernel.core.parsetree.a12internal.api.condition.ICondition;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

@StringTemplateGroup("entityid")
public final class EntityId extends STParameterClassBase implements IEntityId {

	public static EntityId build(IEntityId entityId, Function<IEntityId, String> uniqueFieldNames) {
		return new Builder().entityId(entityId).uniqueFieldNames(uniqueFieldNames).build();
	}

	private static class Builder {
		// Variables to be set from outside
		private IEntityId entityId;
		private Function<IEntityId, String> uniqueFieldNames;

		// Variables to be computed
		private String uniqueName;
		private boolean isField;

		private boolean hasSemanticIndex;
		private List<ICodeOutput> semIdx;

		private boolean hasCategory;
		private String categoryName;
		private String usbCode;
		private List<String> allLayerSpecs;

		private long[] ebenen;
		private long[] maxWerte;
		private long maxUsb;

		private boolean isHModel;

		private long zeileSternStart;
		private long zeileSternEnd;
		private long lfdNrSternStart;
		private long lfdNrSternEnd;

		private String fullName;
		private long usb;
		private ICondition filterCondition;

		private Builder entityId(IEntityId entityId) {
			this.entityId = entityId;
			return this;
		}

		private Builder uniqueFieldNames(Function<IEntityId, String> uniqueFieldNames) {
			this.uniqueFieldNames = uniqueFieldNames;
			return this;
		}

		private List<ICodeOutput> convertSemanticIndex(List<Object> semanticIndex, Function<IEntityId, String> uniqueFieldNames) {
			if (semanticIndex == null) {
				return Collections.emptyList();
			}
			return semanticIndex.stream()
					.map(si -> mapSemanticIndex(si, uniqueFieldNames))
					.collect(Collectors.toList());
		}

		private ICodeOutput mapSemanticIndex(Object si, Function<IEntityId, String> uniqueFieldNames) {
			if (si instanceof SemanticIndexConst) {
				return (SemanticIndexConst) si;
			} else if (si instanceof String) {
				return new SemanticIndexConst((String) si);
			} else if (si instanceof IEntityId) {
				return new Builder().entityId((IEntityId) si).uniqueFieldNames(uniqueFieldNames).build();
			} else {
				throw new IllegalArgumentException("The semantic index must be either a string or an IEntityId!");
			}
		}

		private List<String> computeAllLayerSpecs(long[] ebenen) {
			List<String> result = new ArrayList<>();
			for (int i = 0; i < ebenen.length; i++) {
				long spec = ebenen[i];
				if (spec == IParseTreeEntity.ITERATION || spec == IParseTreeEntity.INTERNAL_ITERATION) {
					result.add(String.valueOf(IParseTreeEntity.ITERATION));
				} else if (spec == IParseTreeEntity.SEMANTIC_INDEX) {
					result.add(String.valueOf(IParseTreeEntity.SEMANTIC_INDEX));
				} else if (spec == IParseTreeEntity.PARALLEL_ITERATION) {
					result.add(String.valueOf(IParseTreeEntity.PARALLEL_ITERATION));
				} else if (spec == IParseTreeEntity.WILDCARD) {
					addWildcardLayerSpec(result, ebenen, i);
				} else if (spec == IParseTreeEntity.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED) {
					result.add(String.valueOf(IParseTreeEntity.REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED));
				} else if (spec >= 1) {
					// Specific indices are not allowed for A12-DVs,
					// but for H-DVs, specific indexes may be used
					result.add(String.format("%d", spec));
				} else {
					assert spec != IParseTreeEntity.UNSPECIFIED :
							"IParseTreeEntity.UNSPECIFIED (-2) is only for USB, and not expected to occur here";
					result.add("1");
				}
			}
			return result;
		}

		private void addWildcardLayerSpec(List<String> layerSpecsCode, long[] ebenenSpecs, int level) {
			if (isHModel && level == 0) {// Vordruck
				layerSpecsCode.add(String.format("%d, %d", lfdNrSternStart, lfdNrSternEnd));
			} else if (isField && (level == ebenenSpecs.length - 1)) { // Zeile
				assert isHModel : "No field wildcards in A12-DVs";
				if (zeileSternEnd > zeileSternStart) {
					layerSpecsCode.add(String.format("%d, %d", zeileSternStart, zeileSternEnd));
				} else {
					layerSpecsCode.add(String.format("%d", zeileSternEnd));
				}
			} else if (!isHModel) {// for H-DVs, we ignore possibly existing wildcards for groups
				long max = maxWerte[level];
				if (max > 1) {
					layerSpecsCode.add(String.format("1, %d", max));
				} else {
					layerSpecsCode.add("1");
				}
			}
		}

		private String computeUsbCode(long usb, long maxUsb) {
			if (usb < IParseTreeEntity.ITERATION || maxUsb == IParseTreeEntity.UNSPECIFIED) {
				return String.valueOf(IIdentifier.USB_NOT_SPECIFIED);
			}
			return (usb == IParseTreeEntity.ITERATION) ? String.valueOf(IIdentifier.ITERATION) : String.format("1, %d", maxUsb);
		}

		private EntityId build() {
			this.ebenen = new long[entityId.getIndices().length];
			this.maxWerte = new long[entityId.getMaxValues().length];
			System.arraycopy(entityId.getIndices(), 0, ebenen, 0, ebenen.length);
			System.arraycopy(entityId.getMaxValues(), 0, maxWerte, 0, maxWerte.length);

			this.zeileSternStart = entityId.getRowWildcardStart();
			this.zeileSternEnd = entityId.getRowWildcardEnd();
			this.lfdNrSternStart = entityId.getRootGroupWildcardStart();
			this.lfdNrSternEnd = entityId.getRootGroupWildcardEnd();
			this.fullName = entityId.getFullName();
			this.usb = entityId.getUsb();
			this.filterCondition = entityId.getFilterCondition();

			this.isField = entityId.isField();

			String tmpUniqueName = uniqueFieldNames.apply(entityId);
			if (tmpUniqueName != null) {
				this.uniqueName = tmpUniqueName;
			} else {
				if (entityId.isField()) {
					throw new IllegalArgumentException("Unique name for field " + entityId.getFullName() + " not defined!");
				} else {
					this.uniqueName = this.fullName;
				}
			}

			this.semIdx = Collections.unmodifiableList(convertSemanticIndex(entityId.getSemanticIndex(), uniqueFieldNames));
			this.hasSemanticIndex = !semIdx.isEmpty();

			this.categoryName = entityId.getCategoryName();
			this.hasCategory = (entityId.isField() && !StringUtils.isEmpty(categoryName));

			this.maxUsb = entityId.getMaxUsb();
			this.isHModel = entityId.isHmodel();

			this.allLayerSpecs = computeAllLayerSpecs(ebenen);

			this.usbCode = computeUsbCode(usb, maxUsb);

			return new EntityId(this);
		}
	}

	private String varName = null;

	private final String uniqueName;
	private final boolean isField;

	private final boolean hasSemanticIndex;
	private final List<ICodeOutput> semIdx;

	private final boolean hasCategory;
	private final String categoryName;
	private final String usbCode;
	private final List<String> allLayerSpecs;

	private final long[] ebenen;
	private final long[] maxWerte;
	private final long maxUsb;

	private final boolean isHModel;

	private final long zeileSternStart;
	private final long zeileSternEnd;
	private final long lfdNrSternStart;
	private final long lfdNrSternEnd;

	private final String fullName;
	private final long usb;
	private ICondition filterCondition;
	private String filterConditionName;

	private final boolean isOuter;

	private final PathNotationConfig pathNotationConfig;

	private EntityId(Builder b) {
		this.ebenen = b.ebenen;
		this.maxWerte = b.maxWerte;

		this.zeileSternStart = b.zeileSternStart;
		this.zeileSternEnd = b.zeileSternEnd;
		this.lfdNrSternStart = b.lfdNrSternStart;
		this.lfdNrSternEnd = b.lfdNrSternEnd;
		this.fullName = b.fullName;
		this.usb = b.usb;

		this.isField = b.isField;

		this.uniqueName = b.uniqueName;

		this.hasSemanticIndex = b.hasSemanticIndex;
		this.semIdx = b.semIdx;

		this.categoryName = b.categoryName;
		this.hasCategory = b.hasCategory;

		this.maxUsb = b.maxUsb;
		this.isHModel = b.isHModel;

		this.allLayerSpecs = b.allLayerSpecs;

		this.usbCode = b.usbCode;
		this.filterCondition = b.filterCondition;

		this.pathNotationConfig = b.entityId != null ? b.entityId.getPathNotationConfig()
				: PathNotationConfig.DEFAULT_MAYBELOSSY;
		this.isOuter = b.entityId != null && b.entityId.isOuter();
	}

	void setVarName(String varName) {
		this.varName = varName;
	}

	@Override
	public long[] getIndices() {
		return ebenen;
	}

	@Override
	public long[] getMaxValues() {
		return maxWerte;
	}

	@Override
	public String getFullName() {
		return fullName;
	}

	@Override
	public long getUsb() {
		return usb;
	}

	@Override
	public long getRowWildcardStart() {
		return zeileSternStart;
	}

	@Override
	public long getRootGroupWildcardStart() {
		return lfdNrSternStart;
	}

	@Override
	public long getRowWildcardEnd() {
		return zeileSternEnd;
	}

	@Override
	public long getRootGroupWildcardEnd() {
		return lfdNrSternEnd;
	}

	@Override
	public String getCategoryName() {
		return categoryName;
	}

	@Override
	public List<Object> getSemanticIndex() {
		return (semIdx == null ? null : new ArrayList<>(semIdx));
	}

	@Override
	public ICondition getFilterCondition() {
		return filterCondition;
	}

	public String getSemanticIndexCodeRefCpp() {
		if (semIdx == null || semIdx.isEmpty()) {
			return "ER::SemanticIndexTemplate()";
		}
		if (semIdx.size() > 1) {
			throw new RuntimeException("Maximal one list entry expected.");
		}
		return semIdx.get(0).toCode();
	}

	public String getSemanticIndexCodeRef() {
		if (semIdx == null || semIdx.isEmpty()) {
			return "null";
		}
		if (semIdx.size() > 1) {
			throw new RuntimeException("Maximal one list entry expected.");
		}
		return semIdx.get(0).toCode();
	}

	public boolean getHasSemanticIndex() {
		return hasSemanticIndex;
	}

	/**
	 * Gibt den UniqueName zurück. Anmerkung:
	 * der Fullname ({@link #getFullName()}) ist immer unqiue.
	 *
	 * @return unique Name der Entität.
	 */
	public String getUniqueName() {
		return uniqueName;
	}

	public long getMaxUsb() {
		return maxUsb;
	}

	public boolean isField() {
		return isField;
	}

	public boolean isHmodel() {
		return isHModel;
	}

	public List<String> getAllLayerSpecs() {
		return allLayerSpecs;
	}

	public String getVariableName() {
		if (varName == null) {
			throw new RuntimeException("VariableName has not been set for this entity.");
		}
		return varName;
	}

	public boolean getHasCategory() {
		return hasCategory;
	}

	public String getUsbCode() {
		return usbCode;
	}

	public boolean essentiallyEquals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EntityId other = (EntityId) obj;
		if (!Arrays.equals(ebenen, other.ebenen)) {
			return false;
		}
		if (fullName == null) {
			if (other.fullName != null) {
				return false;
			}
		} else if (!fullName.equals(other.fullName)) {
			return false;
		}
		return (usb == other.usb);
	}

	/**
	 * <b>Attention:</b> Only fields that are referenced in the StringTemplate are used. This
	 * ensures that the variables generated from this class are unique per method.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((allLayerSpecs == null) ? 0 : allLayerSpecs.hashCode());
		result = prime * result + ((categoryName == null) ? 0 : categoryName.hashCode());
		result = prime * result + (hasCategory ? 1231 : 1237);
		result = prime * result + (hasSemanticIndex ? 1231 : 1237);
		result = prime * result + (isField ? 1231 : 1237);
		result = prime * result + ((semIdx == null) ? 0 : semIdx.hashCode());
		result = prime * result + ((uniqueName == null) ? 0 : uniqueName.hashCode());
		result = prime * result + ((usbCode == null) ? 0 : usbCode.hashCode());
		result = prime * result + ((filterCondition == null) ? 0 : filterCondition.hashCode());
		result = prime * result + (isOuter ? 1231 : 1237);
		return result;
	}

	/**
	 * <b>Attention:</b> Only fields that are referenced in the StringTemplate are used. This
	 * ensures that the variables generated from this class are unique per method.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EntityId other = (EntityId) obj;
		if (allLayerSpecs == null) {
			if (other.allLayerSpecs != null) {
				return false;
			}
		} else if (!allLayerSpecs.equals(other.allLayerSpecs)) {
			return false;
		}
		if (categoryName == null) {
			if (other.categoryName != null) {
				return false;
			}
		} else if (!categoryName.equals(other.categoryName)) {
			return false;
		}
		if (hasCategory != other.hasCategory) {
			return false;
		}
		if (hasSemanticIndex != other.hasSemanticIndex) {
			return false;
		}
		if (isField != other.isField) {
			return false;
		}
		if (semIdx == null) {
			if (other.semIdx != null) {
				return false;
			}
		} else if (!semIdx.equals(other.semIdx)) {
			return false;
		}
		if (uniqueName == null) {
			if (other.uniqueName != null) {
				return false;
			}
		} else if (!uniqueName.equals(other.uniqueName)) {
			return false;
		}
		if (usbCode == null) {
			if (other.usbCode != null) {
				return false;
			}
		} else if (!usbCode.equals(other.usbCode)) {
			return false;
		}
		if (filterCondition == null) {
			if (other.filterCondition != null) {
				return false;
			}
		} else if (!filterCondition.equals(other.filterCondition)) {
			return false;
		}
		return this.isOuter == other.isOuter;
	}

	@Override
	public String toString() {
		return "EntityId: " + this.fullName + " " + this.allLayerSpecs + " " + this.categoryName + " " + this.semIdx + " " + this.usb + " " + this.filterCondition + " isOuter:" + this.isOuter;
	}

	public void setFilterConditionName(String filterConditionName) {
		this.filterConditionName = filterConditionName;
	}

	public String getFilterConditionName() {
		if (filterConditionName == null) {
			throw new IllegalStateException(
					"The filter condition name must be set before access. Assure that all transformations to conditions are done to calculations before the filter conditions are assessed!");
		}
		return filterConditionName;
	}

	@Override
	public PathNotationConfig getPathNotationConfig() {
		return pathNotationConfig;
	}

	@Override public boolean isOuter() {
		return isOuter;
	}
}
