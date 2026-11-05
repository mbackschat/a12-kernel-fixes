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

package com.mgmtp.a12.kernel.core.rt._31_3.internal.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.IMetaDataValidierung;
import com.mgmtp.a12.kernel.core.rt._31_3.internal.core.ValidationCache;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * The internal representation of an {@link IIdentifier}. It does not implement {@link IIdentifier}
 * to ensure that no internal identifier is given to users of the kernel-core-runtime-api.
 * <p>
 * It is used to handle the data access in the internal {@link ValidationCache} and contains
 * additional information for categories and semantic indexes used to extract specific values.
 */
public class RtInternalIdentifier implements Serializable {

	private static final long serialVersionUID = -5687854659946640411L;
	private final IterationState iterationState;
	private String name;
	private long[] indices;
	private long usb;

	private final String category;
	private final SemanticIndexSpec semIndex;

	public RtInternalIdentifier(final String name, final long[] indices) {
		this(name, indices, IIdentifier.USB_NOT_SPECIFIED, null, null);
	}

	public RtInternalIdentifier(final String name, final long[] indices, final long usb) {
		this(name, indices, usb, null, null);
	}

	public RtInternalIdentifier(final String name, final long[] indices, final long usb, String category,
			SemanticIndexSpec semIndex) {
		this(name, indices, usb, category, semIndex, null);
	}

	public RtInternalIdentifier(RtInternalIdentifier id) {
		this(id.getName(), id.getIndices().clone(), id.getUsb(), id.getCategory(), new SemanticIndexSpec(id.getSemanticIndex()),
				id.iterationState);
	}

	public RtInternalIdentifier cloneWithSemanticIndexReplaced(int lvl, long semIdxReplacement) {
		RtInternalIdentifier result = new RtInternalIdentifier(name, indices.clone(), usb, category, null, this.iterationState);
		result.indices[lvl] = semIdxReplacement;
		return result;
	}

	/**
	 * @param idx
	 * 		might be <code>null</code> if not known or needed.
	 */
	public RtInternalIdentifier(String name, long[] indices, long usb, String category, SemanticIndexSpec semIndex,
			IterationState idx) {
		this.name = name;
		this.indices = indices;
		this.usb = usb;
		this.semIndex = semIndex;
		this.category = category;
		this.iterationState = idx;
	}

	public SemanticIndexSpec getSemanticIndex() {
		return semIndex;
	}

	/**
	 * Erzeugt ein {@link RtInternalIdentifier} Objekt mit dem Namen &lt;name&gt;, dessen USB das
	 * erste Element aus den Indices aus &lt;idx&gt; ist, die Indices die restlichen Elemente. Die
	 * parallele Iteration wird durch den semantischen Index mit dem Wert aus dem
	 * {@link IterationState} ersetzt.
	 */
	public static RtInternalIdentifier createFromIterationState(final String name, final IterationState idx) {
		return createFromIterationState(name, idx, null, null);
	}

	/**
	 * Erzeugt ein {@link RtInternalIdentifier} Objekt mit dem Namen &lt;name&gt;, dessen USB das
	 * erste Element aus den Indices aus &lt;idx&gt; ist, die Indices die restlichen Elemente. Die
	 * parallele Iteration wird durch den semantischen Index mit dem Wert aus dem
	 * {@link IterationState} ersetzt.
	 */
	static RtInternalIdentifier createFromIterationState(final String name, final IterationState idx, String category,
			SemanticIndexSpec semIndex) {
		final long[] indices = idx.getIndexesWithoutUsb();
		int parallelIterationLevel = ArrayUtils.indexOf(indices, IIdentifier.PARALLEL_ITERATION);
		SemanticIndexSpec semIdxSpec;
		if (parallelIterationLevel >= 0) {
			indices[parallelIterationLevel] = IIdentifier.SEMANTIC_INDEX;
			semIdxSpec = new SemanticIndexSpec(idx.getValue());
		} else {
			semIdxSpec = null;
		}
		return new RtInternalIdentifier(name, indices, idx.getIndex(0), category, semIndex == null ? semIdxSpec : semIndex, idx);
	}

	/**
	 * Creates an {@link RtInternalIdentifier} instance with information given by a
	 * {@link RtIdentifierTemplate}. The iteration layers (simple iteration, semantic index,
	 * parallel iteration) are replaced by the corresponding iteration state.
	 */
	public static RtInternalIdentifier createFromTemplate(RtIdentifierTemplate template, IterationState iterState) {
		return createFromTemplate(template, iterState, false);
	}

	/**
	 * The resulting RtInternalIdentifier is not suitable for most processing in runtime, and only intended to be converted
	 * to an (external) IIdentifier that is passed as a possiblyCausingEntity in addMessageV2
	 */
	public static RtInternalIdentifier createFromTemplateReplaceAggregationsWith0DoNotProcessFurther(
			RtIdentifierTemplate template,
			IterationState iterState
	) {
		return createFromTemplate(template, iterState, true);
	}

	/**
	 * @param tolerateRepeatableAndSet0
	 * 		should only ever be set to true when called from
	 *        {@link #createFromTemplateReplaceAggregationsWith0DoNotProcessFurther}. The substitution of aggregations by "0" (without
	 * 		the
	 * 		use of an {@link EntityIterator} is not usually valid for normal use in the runtime.
	 */
	private static RtInternalIdentifier createFromTemplate(RtIdentifierTemplate template, IterationState iterState,
			boolean tolerateRepeatableAndSet0) {
		final long[] indices = new long[template.getLayerSpec().size()];
		boolean hasSemanticIndex = false;

		IterationState usedIterationState = template.isOuter() ? iterState.getOuterState() : iterState;
		for (int i = 0; i < indices.length; i++) {
			RtIdentifierTemplateLayerSpec layerSpec = template.getSingleLayerSpec(i);
			if (tolerateRepeatableAndSet0 && !startEqEnd(layerSpec)) {
				indices[i] = 0;
				continue;
			}
			if (!startEqEnd(layerSpec)) {
				throw new IllegalArgumentException("Internal error: the method must not be called for repeatable templates!");
			}
			assert layerSpec.getIdxEnd() != 0 : "the previous check suggests that 0 would be represented by the explicit range";
			// the layer specs are without usb, the iteration state with, so we have to adapt the index here

			indices[i] = RtInternalIdentifier.calculateIdxFromLayerSpec(layerSpec, usedIterationState, i);
			if (indices[i] == IIdentifier.SEMANTIC_INDEX) {
				hasSemanticIndex = true;
			}
		}
		long usb;
		if (tolerateRepeatableAndSet0 && !startEqEnd(template.getUsbSpec())) {
			usb = 0;
		} else {
			// assert instead of exception because previously there was no check at all
			assert startEqEnd(template.getUsbSpec()) : "the method must not be called for repeatable templates!";
			usb = RtInternalIdentifier.calculateIdxFromLayerSpec(template.getUsbSpec(), usedIterationState, -1);
		}

		SemanticIndexSpec semIdx = template.getSemanticIndex().createSemanticIndex(usedIterationState);
		// If we have parallel iteration, there is no spec for semantic index
		if (hasSemanticIndex && semIdx == null) {
			semIdx = new SemanticIndexSpec(usedIterationState.getValue());
		}
		return new RtInternalIdentifier(template.getUniqueName(), indices, usb, template.getCategory(), semIdx, usedIterationState);
	}

	private static boolean startEqEnd(RtIdentifierTemplateLayerSpec layerSpec) {
		return layerSpec.getIdxStart() == layerSpec.getIdxEnd();
	}

	private static long calculateIdxFromLayerSpec(RtIdentifierTemplateLayerSpec layerSpec, IterationState iterState, int i) {
		if (layerSpec.getIdxStart() == IIdentifier.PARALLEL_ITERATION) {
			return IIdentifier.SEMANTIC_INDEX;
		} else if (layerSpec.getIdxStart() == IIdentifier.ITERATION) {
			return iterState.getIndex(i + 1);
		} else {
			return layerSpec.getIdxStart();
		}
	}

	/**
	 * This implementation is important since the access to a map in typescript is done with this
	 * string representation.
	 *
	 * @return the string representation of this object.
	 */
	public String toString() {

		if (!name.contains(IIdentifier.SEPARATOR)) {
			// Intern identifier with unique name
			StringBuilder sb = new StringBuilder(name);
			sb.append(Constants.FIELD_LAYER_SEPARATOR).append(usb);
			for (int i = 0; i < indices.length; i++) {
				sb.append(Constants.FIELD_LAYER_SEPARATOR).append(indices[i]);
			}
			if (!StringUtils.isEmpty(category)) {
				sb.append(Constants.CATEGORY_VALUE_ACCESS).append(category);
			}
			if (semIndex != null) {
				sb.append(Constants.SEMANTIC_INDEX_SEPARATOR).append(semIndex.toString());
			}
			return sb.toString();
		}
		final StringBuilder sb = new StringBuilder();
		String fullName = name;

		final String[] teile = fullName.split(IIdentifier.SEPARATOR);
		for (int i = 1; i < teile.length; i++) {
			long idx = i <= indices.length ? indices[i - 1] : -2;
			sb.append(String.format("/%s[%d]", teile[i], idx));
		}
		if (semIndex != null) {
			sb.append(" für ").append(semIndex.toString());
		}
		return sb.toString();

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(indices);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (usb ^ (usb >>> 32));
		int semIdxHashCode = getSemanticIndex() == null ? 0 : getSemanticIndex().hashCode();
		result = result + 31 * semIdxHashCode;
		return result;
	}

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
		RtInternalIdentifier other = (RtInternalIdentifier) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (usb != other.usb) {
			return false;
		}
		if (!Arrays.equals(indices, other.indices)) {
			return false;
		}
		return (getSemanticIndex() != null) ?
				getSemanticIndex().equals(other.getSemanticIndex()) :
				other.getSemanticIndex() == null;
	}

	public String getName() {
		return name;
	}

	public long[] getIndices() {
		return indices;
	}

	public long getUsb() {
		return usb;
	}

	public long[] getIndicesWithUsb() {
		return ArrayUtils.prepended(getUsb(), indices);
	}

	public String getCategory() {
		return category;
	}

	public void setUsb(final long usb) {
		this.usb = usb;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setIndices(final long[] indices) {
		this.indices = indices;
	}

	/**
	 * <p>
	 * Diese Methode wird nur für die Pflichtfeld-Ermittlung aufgerufen.
	 * Die Methode muss auch berücksichtigen, dass - für nicht echt wiederholbare Kontexte -
	 * eine Spezifikation 1 eine Spezifikation mit Wildcard ({@link IIdentifier#ALL_INDICES})
	 * umfasst.
	 * </p>
	 * <p>
	 * Ist die Behandlung von diesem Spezialfall nicht notwendig, soll die sehr viel schnellere
	 * Methode {@link #umfasst(RtInternalIdentifier)} verwendet werden.
	 * </p>
	 * <p>
	 * Gibt <code>true</code> zurück, wenn dieser Identifier den anderen enthält unter
	 * Berücksichtigung der Sonderbedeutung des Index {@link IIdentifier#ALL_INDICES} <br>
	 * Z.B. umfasst der Identifier /[]/V[1]/K[0]/F[0] den Identifier /[]/V[1]/K[3]/F[2].<br>
	 * Hat der Kontext /V/Ctx max. Wiederholbarkeit 1 und das Feld /V/Ctx/F max. Zeilenindex 1, so
	 * umfasst /[]/V[3]/Ctx[1]/F[1] auch /[]/V[3]/Ctx[0]/F[0]
	 * </p>
	 */
	public boolean umfasstFuerPflichtangabenPruefung(final RtInternalIdentifier id, final IMetaDataValidierung metaData) {
		if (!name.equals(id.getName())) {
			return false;
		}
		if (indices.length != id.getIndices().length) {
			return false;
		}
		if (usb != IIdentifier.ALL_INDICES && usb != id.getUsb()) {
			return false;
		}
		final List<Long> maxIndices = metaData.getMaxFieldIndices(name);
		for (int i = 0; i < indices.length; i++) {
			if (indices[i] == IIdentifier.ALL_INDICES || indices[i] == id.getIndices()[i]) {
				continue;
			}
			if (maxIndices.get(i) == 1 && indices[i] == 1 && id.getIndices()[i] == IIdentifier.ALL_INDICES) {
				continue;
			}
			return false; // NOPMD
		}
		return true;
	}

	/**
	 * Gibt <code>true</code> zurück, wenn dieser Identifier den anderen enthält unter
	 * Berücksichtigung der Sonderbedeutung des Index {@link IIdentifier#ALL_INDICES}.<br>
	 * Z.B. umfasst der Identifier /[]/V[1]/K[0]/F[0] die Identifier
	 * <ul>
	 * <li>/[]/V[1]/K[3]/F[2]</li>
	 * <li>/[]/V[1]/K[3]/F[0]</li>
	 * <li>/[]/V[1]/K[0]/F[0]</li>
	 * <li>/[]/V[1]/K[0]/F[2]</li>
	 * </ul>
	 * Spezifiziert der übergebene Identifier für eine Ebene {@link IIdentifier#ALL_INDICES}, so
	 * kann die Methode nur dann <code>true</code> liefern, wenn der aufrufende Identifier für diese
	 * Ebene ebenfalls {@link IIdentifier#ALL_INDICES} spezifiziert.<br>
	 * Die Methode sollte also nicht mit {@link IIdentifier#ALL_INDICES} für eine nicht
	 * wiederholbare Ebene aufgerufen werden, denn in diesem Fall wird die umfasst-Relation u.U.
	 * nicht richtig erkannt - für den Fall sollte
	 * {@link #umfasstFuerPflichtangabenPruefung(RtInternalIdentifier, IMetaDataValidierung)}
	 * verwendet werden.
	 */
	public boolean umfasst(final RtInternalIdentifier id) {
		if (!name.equals(id.getName())) {
			return false;
		}
		if (indices.length != id.getIndices().length) {
			return false;
		}
		if (usb != IIdentifier.ALL_INDICES && usb != id.getUsb()) {
			return false;
		}
		for (int i = 0; i < indices.length; i++) {
			if (indices[i] != IIdentifier.ALL_INDICES && indices[i] != id.getIndices()[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Remove the index parts that are not contained in id.
	 * <p>
	 * Attention: the result is not symmetric.
	 *
	 * @param id
	 * 		the id to intersect with.
	 */
	public RtInternalIdentifier intersect(final RtInternalIdentifier id) {
		long[] indexes = id.getIndices();

		long[] resultIdx = new long[this.indices.length];
		boolean changed = false;

		if (indexes.length < this.indices.length) {
			// currently, we only intersect with sub-elements
			return null;
		}

		long resultUsb = this.usb;
		long oUsb = id.getUsb();
		if (oUsb != usb && oUsb != IIdentifier.ALL_INDICES) {
			if (usb == IIdentifier.ALL_INDICES) {
				resultUsb = oUsb;
				changed = true;
			} else {
				// Usbs do not match
				return null;
			}
		}

		for (int i = 0; i < this.indices.length; i++) {
			long idx = this.indices[i];
			long oIdx = indexes[i];

			if (idx == oIdx || oIdx == IIdentifier.ALL_INDICES) {
				resultIdx[i] = idx;
			} else if (idx == IIdentifier.ALL_INDICES) {
				resultIdx[i] = oIdx;
				changed = true;
			} else {
				// Index does not match
				return null;
			}
		}

		if (!changed) {
			return this;
		} else {
			return new RtInternalIdentifier(this.name, resultIdx, resultUsb, this.category, this.semIndex);
		}
	}

	public RtInternalIdentifier withUsb(long newUsb) {
		return new RtInternalIdentifier(name, indices, newUsb, category, semIndex, iterationState);
	}

	public static long[] createIdxWithUsb(RtInternalIdentifier id) {
		long[] result = new long[id.getIndices().length + 1];
		result[0] = id.getUsb();
		for (int j = 1; j < id.getIndices().length + 1; j++) {
			result[j] = id.getIndices()[j - 1];
		}
		return result;
	}

	/**
	 * Create a {@link RtInternalIdentifier} for the enclosing group for the given indexes.
	 *
	 * @param id
	 * 		the id to create the parent for.
	 * @param fullName
	 * 		the fullname of the id
	 */
	public static RtInternalIdentifier createParentId(RtInternalIdentifier id, String fullName) {
		String parentName = EntityUtil.getParentPath(fullName);
		if (parentName.length() == 0) {
			return null;
		}
		// for old world we need to keep the index of the root group, thus the Math.max call
		long[] parentIndexes = Arrays.copyOf(id.getIndices(), Math.max(id.getIndices().length - 1, 1));
		return new RtInternalIdentifier(parentName, parentIndexes, id.getUsb());
	}

	/**
	 * If the field was created via an iteration state or was cloned from such an identifier, this returns the used iteration state.
	 */
	public Optional<IterationState> getIterationState() {
		return Optional.of(iterationState);
	}
}
