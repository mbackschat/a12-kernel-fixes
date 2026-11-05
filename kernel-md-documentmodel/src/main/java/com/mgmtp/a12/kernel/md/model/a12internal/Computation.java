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

package com.mgmtp.a12.kernel.md.model.a12internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mgmtp.a12.kernel.md.model.api.IComputation;
import com.mgmtp.a12.kernel.md.model.api.ILocalizedTextMap;
import com.mgmtp.a12.kernel.md.utils.NoNeedToAdaptSerialVersionUID;
import com.mgmtp.a12.model.header.Annotation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a computed value for a {@link Field} of a {@link DocumentModel}. The computation
 * normally depends on values of other {@link Field}s.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Computation extends Element {

	@NoNeedToAdaptSerialVersionUID
	private static final long serialVersionUID = 1L;

	public static final String TR1 = "DiffersWithToleranceRange1";
	public static final String TR2 = "DiffersWithToleranceRange2";
	public static final String TR5 = "DiffersWithToleranceRange5";
	public static final String TR10 = "DiffersWithToleranceRange10";

	/**
	 * Matches a suppress preamble ({@code @SuppressWarning(...)} / {@code @UnterdrueckeWarnung(...)}) and
	 * captures the comma-separated argument list. Only used for the deprecated, best-effort
	 * {@link #getErrorCodesToSuppress()} derivation.
	 */
	private static final Pattern SUPPRESS_PREAMBLE_PATTERN =
			Pattern.compile("@\\s*(?:SuppressWarning|UnterdrueckeWarnung)\\s*\\(([^)]*)\\)");

	/**
	 * Definition of a way to compute a value for a {@link Field}.
	 */
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode
	@ToString
	@Builder
	public static class ComputationAlternative implements Serializable {

		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		/**
		 * @param precondition
		 * 		the new precondition for this alternative (for more information see
		 *        {@link #getPrecondition()}). May be {@code null} to unset.
		 */
		private String precondition;
		/**
		 * see {@link #getOperation()}
		 */
		private String operation; // not marked as NonNull because serialization allows invalid but null operations too

		/**
		 * @return the operation with which the value is computed if the precondition (see also
		 *        {@link #getPrecondition()}) is fulfilled.
		 * 		Normally non-empty, but may be empty (but never null) for incomplete computation alternatives for
		 * 		which modeling has not been completed yet.
		 */
		public @NonNull String getOperation() {
			return operation != null ? operation : "";
		}

		/**
		 * @param toleranceRangeOp
		 * 		the new operation with tolerance range (e.g. DiffersWithToleranceRange1)
		 * 		for this alternative. May be {@code null} to unset.
		 */
		private String toleranceRangeOp;

		/**
		 * @return the precondition for this alternative: If this precondition is fulfilled the
		 * 		value
		 * 		will be computed as described in {@link #getOperation()}.
		 */
		public Optional<String> getPrecondition() {
			return Optional.ofNullable(precondition);
		}

		/**
		 * @return the operation with tolerance range (e.g. DiffersWithToleranceRange1)
		 * 		for this alternative.
		 */
		public Optional<String> getToleranceRangeOp() {
			return Optional.ofNullable(toleranceRangeOp);
		}

	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@EqualsAndHashCode
	@ToString
	public static class ComputationPreconditionsAndOperations implements Serializable {
		@NoNeedToAdaptSerialVersionUID
		private static final long serialVersionUID = 1L;

		private String commonPrecondition;
		private List<ComputationAlternative> alternatives;

		public Optional<String> getCommonPrecondition() {
			return Optional.ofNullable(commonPrecondition);
		}

		public void setCommonPrecondition(String commonPrecondition) {
			this.commonPrecondition = commonPrecondition;
		}

		public List<ComputationAlternative> getAlternatives() {
			return alternatives;
		}

		public void setAlternatives(List<ComputationAlternative> alternatives) {
			this.alternatives = alternatives;
		}
	}

	public void setComputationPreconditionsAndOperations(ComputationPreconditionsAndOperations fragments) {
		this.computationAlternatives = fragments.alternatives;
		fragments.getCommonPrecondition().ifPresent(cp -> this.commonPrecondition = cp);
	}

	public ComputationPreconditionsAndOperations getComputationPreconditionsAndOperations() {
		return new ComputationPreconditionsAndOperations(commonPrecondition, this.computationAlternatives);
	}

	/**
	 * @return the reference to the {@link Field} to be computed. The reference
	 * 		({@link IDocumentModelObjectReference#getRelativePath()}) is relative to this computation. Never {@code null}.
	 * @param computedField
	 * 		the new reference to the {@link Field} to be computed. The reference
	 * 		({@link IDocumentModelObjectReference#getRelativePath()}) is relative to this computation. May not be {@code null}.
	 */
	private @NonNull IDocumentModelObjectReference<Field> computedField;
	/**
	 * The additional parser information: a parser preamble (e.g. {@code @SuppressWarning(...)}) for this
	 * computation. See {@link IComputation#getAdditionalParserInformation()}.
	 */
	private String additionalParserInformation;
	/**
	 * Landing pad for the removed {@code errorCodesToSuppress} JSON property. It is only ever populated while
	 * deserializing an old document model (see the {@code ComputationMixin}) and is immediately folded into
	 * {@link #additionalParserInformation} by {@link #_internal_foldLegacyErrorCodesToSuppress(Locale)}, after which it
	 * is {@code null} again. It is never serialized and never part of the computation's identity. Transitional — to be
	 * removed together with {@link #getErrorCodesToSuppress()} in the next breaking release.
	 */
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private List<String> legacyErrorCodesToSuppress;
	/**
	 * @return a list with the alternatives for this computation (see also
	 *        {@link ComputationAlternative}). Is never {@code null} or empty.
	 * @param computationAlternatives
	 * 		a list with the new alternatives for this computation (see also
	 *        {@link ComputationAlternative}). May not be {@code null} or empty.
	 */
	private @NonNull List<ComputationAlternative> computationAlternatives = new ArrayList<>();

	private String commonPrecondition;

	public Optional<String> getCommonPrecondition() {
		return Optional.ofNullable(commonPrecondition);
	}

	/**
	 * @return the additional parser information; see {@link IComputation#getAdditionalParserInformation()}.
	 */
	public Optional<String> getAdditionalParserInformation() {
		return Optional.ofNullable(additionalParserInformation);
	}

	/**
	 * @deprecated since 31.2.0. Use {@link #getAdditionalParserInformation()} instead, which holds the
	 * 		complete preamble. This read-only accessor extracts the codes (best-effort) from that preamble;
	 * 		there is no corresponding setter — write the preamble via {@link #setAdditionalParserInformation(String)}.
	 */
	@Deprecated(since = "31.2.0")
	public @NonNull List<String> getErrorCodesToSuppress() {
		return extractSuppressedCodes(additionalParserInformation);
	}

	private static List<String> extractSuppressedCodes(String additionalParserInformation) {
		if (additionalParserInformation == null) {
			return List.of();
		}
		Matcher matcher = SUPPRESS_PREAMBLE_PATTERN.matcher(additionalParserInformation);
		if (!matcher.find() || matcher.group(1).isBlank()) {
			return List.of();
		}
		return Arrays.stream(matcher.group(1).split(","))
				.map(String::trim)
				.filter(code -> !code.isEmpty())
				.toList();
	}

	/**
	 * only intended for kernel-internal use.
	 * <p/>
	 * Transitional (to be removed with the next breaking release): folds a legacy {@code errorCodesToSuppress} list —
	 * only ever populated while deserializing an old model — into {@link #additionalParserInformation}, building the
	 * suppress preamble localized to the given condition language, and then clears the landing pad. It is a no-op once
	 * the computation is already in the current single-field form (nothing to fold), and it never overwrites an existing
	 * {@link #additionalParserInformation} (the new field always wins).
	 *
	 * @param conditionLanguage
	 * 		the condition language of the enclosing document model, used to pick the localized suppress directive.
	 */
	public void _internal_foldLegacyErrorCodesToSuppress(Locale conditionLanguage) {
		if (legacyErrorCodesToSuppress == null) {
			return;
		}
		if (!legacyErrorCodesToSuppress.isEmpty()
				&& (additionalParserInformation == null || additionalParserInformation.isBlank())) {
			additionalParserInformation =
					_internal_buildSuppressPreamble(legacyErrorCodesToSuppress, isGermanConditionLanguage(conditionLanguage));
		}
		legacyErrorCodesToSuppress = null;
	}

	/**
	 * only intended for kernel-internal use.
	 * <p/>
	 * Builds a suppress-warning preamble ({@code @SuppressWarning(a,b)} in English, {@code @UnterdrueckeWarnung(a,b)} in
	 * German) from the given warning codes. Shared by deserialization (see
	 * {@link #_internal_foldLegacyErrorCodesToSuppress(Locale)}) and the document-model migration so that the choice of
	 * directive keyword lives in exactly one place.
	 *
	 * @param codes
	 * 		the warning codes to suppress. Must not be empty.
	 * @param german
	 * 		whether the enclosing model's condition language is German.
	 */
	public static @NonNull String _internal_buildSuppressPreamble(@NonNull List<String> codes, boolean german) {
		String directive = german ? "@UnterdrueckeWarnung" : "@SuppressWarning";
		return directive + "(" + String.join(",", codes) + ")";
	}

	private static boolean isGermanConditionLanguage(Locale conditionLanguage) {
		// Mirror the migration step, which decides German by comparing the raw condition-language code to "de_DE";
		// that code is the Locale's toString form, so compare it the same way to keep both paths consistent.
		return conditionLanguage != null && "de_DE".equals(conditionLanguage.toString());
	}

	/**
	 * @return the defined message which will be reported in case the corresponding rule for this
	 * 		computation fires. Is never {@code null}. If no error messages are defined, the
	 *        {@link ILocalizedTextMap} is empty.
	 * @param errorMessage
	 * 		the new message which should be reported is the rule fires. May not be
	 *        {@code null}.
	 * 		Use an empty {@link ILocalizedTextMap} to unset.
	 */
	private @NonNull ILocalizedTextMap errorMessage = new LocalizedTextMapBuilder().build();
	/**
	 * @param roundingMode
	 * 		the new rounding mode for this computation. At the moment, only "RoundUp" and "RoundDown" are supported. May be
	 *        {@code null} to unset.
	 */
	private String roundingMode;
	/**
	 * @param errorCode
	 * 		the errorCode used for the validation rule auto-generated from this computation. May be
	 *        {@code null} to unset; in that case the rule's errorCode falls back to the computation's name.
	 */
	private String errorCode;

	@Builder(builderClassName = "ComputationBuilder")
	private Computation(@NonNull String id, @NonNull String name, @NonNull ILocalizedTextMap internalDescription,
			@NonNull ILocalizedTextMap externalDescription, @NonNull List<Annotation> annotations,
			@NonNull IDocumentModelObjectReference<Field> computedField,
			String additionalParserInformation,
			@NonNull List<ComputationAlternative> computationAlternatives, String commonPrecondition,
			@NonNull ILocalizedTextMap errorMessage,
			String roundingMode, String errorCode, String changeNote) {
		super(null, id, name, internalDescription, externalDescription, annotations, changeNote);
		this.computedField = computedField;
		this.additionalParserInformation = additionalParserInformation;
		this.computationAlternatives = computationAlternatives;
		this.commonPrecondition = commonPrecondition;
		this.errorMessage = errorMessage;
		this.roundingMode = roundingMode;
		this.errorCode = errorCode;
	}

	/**
	 * @return the rounding mode for this computation.
	 */
	public Optional<String> getRoundingMode() {
		return Optional.ofNullable(roundingMode);
	}

	/**
	 * @return the errorCode used for the validation rule auto-generated from this computation.
	 * 		If not set, the rule's errorCode falls back to the computation's name.
	 */
	public Optional<String> getErrorCode() {
		return Optional.ofNullable(errorCode);
	}

	public static class ComputationBuilder {
		private ILocalizedTextMap internalDescription = new LocalizedTextMapBuilder().build(); // in order to make ComputationBuilder.internalDescription() optional
		private ILocalizedTextMap externalDescription = new LocalizedTextMapBuilder().build(); // in order to make ComputationBuilder.externalDescription() optional
		private List<Annotation> annotations = new ArrayList<>(); // in order to make ComputationBuilder.annotations() optional
		private List<ComputationAlternative> computationAlternatives = new ArrayList<>(); // in order to make ComputationBuilder.computationAlternatives() optional when calling the builder for the first time
		private ILocalizedTextMap errorMessage = new LocalizedTextMapBuilder().build(); // in order to make ComputationBuilder.errorMessage() optional when calling the builder for the first time

		public String getId() {
			return this.id;
		}
	}

}
