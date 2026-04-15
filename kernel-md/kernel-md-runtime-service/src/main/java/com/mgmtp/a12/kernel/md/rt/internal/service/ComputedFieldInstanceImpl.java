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

package com.mgmtp.a12.kernel.md.rt.internal.service;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.internal.service.utils.DocumentUtils;
import com.mgmtp.a12.kernel.md.rt.a12internal.service.DocumentComputationResultImpl;
import com.mgmtp.a12.kernel.md.rt.api.IComputedFieldInstance;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1V2Conversion;

import lombok.NonNull;

public class ComputedFieldInstanceImpl implements IComputedFieldInstance {

	private final @NonNull IDocument document;
	private final @NonNull DocumentPointer pointer;
	private final Object v2Value;
	private final @NonNull String stringValue;
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType") private final Optional<IMessage> errorMessage;

	public ComputedFieldInstanceImpl(
			final @NonNull IDocument document,
			final @NonNull DocumentPointer pointer,
			final Object v2Value,
			final @NonNull String stringValue,
			@SuppressWarnings("OptionalUsedAsFieldOrParameterType") final @NonNull Optional<IMessage> errorMessage
	) {
		this.document = document;
		this.pointer = pointer;
		this.v2Value = v2Value;
		this.stringValue = stringValue;
		this.errorMessage = errorMessage;
	}

	public static ComputedFieldInstanceImpl empty(final @NonNull IDocument document,
			final @NonNull DocumentPointer pointer) {
		return new ComputedFieldInstanceImpl(document, pointer, null, "", Optional.empty());
	}

	@SuppressWarnings("removal") @Override
	public String getPath() {
		return pointer.fullName();
	}

	@SuppressWarnings("removal") @Override
	public int[] getRepetitions() {
		return pointer.repetitionIndexes().stream().mapToInt(i -> i).toArray();
	}

	@Override public DocumentPointer pointer() {
		return pointer;
	}

	@SuppressWarnings("removal") @Override
	public Optional<Object> getValue() {
		return Optional.ofNullable(BasicTypeV1V2Conversion.basicTypeV2ToV1(getValueV2()));
	}

	@Override
	public Object getValueV2() {
		return v2Value;
	}

	@Override
	public Optional<String> getStringValue() {
		return Optional.of(stringValue);
	}

	@Override
	public Optional<IMessage> getErrorMessage() {
		return errorMessage;
	}

	@SuppressWarnings("removal") @Override
	public IDocument getDocument() {
		return document;
	}

	@SuppressWarnings("deprecation") @Override
	public void apply(Consumer<IComputedFieldInstance> errorHandler) {
		apply(DocumentComputationResultImpl.DOC_FACTORY, errorHandler);
	}

	private void apply(IDocumentFactory documentFactory, Consumer<IComputedFieldInstance> errorHandler) {
		if (errorMessage.isPresent()) {
			errorHandler.accept(this);
			return;
		}
		applyTo(document, documentFactory);
	}

	@SuppressWarnings("removal") @Override
	public void applyTo(IDocument document) {
		applyTo(document, DocumentComputationResultImpl.DOC_FACTORY);
	}

	@SuppressWarnings("removal") @Override
	public void applyTo(IDocument document, IDocumentFactory documentFactory) {
		if (StringUtils.isEmpty(stringValue)) {
			DocumentUtils.clearFieldInstanceValue(document, pointer.fullName(),
					pointer.repetitionIndexes().stream().mapToInt(i -> i).toArray());
		} else {
			DocumentUtils.setFieldInstanceValue(document, pointer.fullName(),
					pointer.repetitionIndexes().stream().mapToInt(i -> i).toArray(), getValue().orElse(null), documentFactory);
		}
	}

	@Override
	public DocumentV2 applyTo(@NonNull DocumentV2 document) {
		// A12K-3432: consider use of delete-UpdateAction for field removal once implemented
		if (this.errorMessage.isPresent()) {
			assert this.errorMessage.get().getSeverity() == IMessage.Severity.ERROR;
			if (document.field(this.pointer) != null) {
				return document.withFieldValue(this.pointer, null);
			} else {
				return document;
			}
		} else {
			return document.withFieldValue(this.pointer, this.v2Value);
		}
	}

	@Override
	public String toString() {
		return pointer.toString();
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof IComputedFieldInstance that))
			return false;
		return Objects.equals(document, that.getDocument()) &&
				Objects.equals(pointer, that.pointer()) &&
				Objects.equals(v2Value, that.getValueV2()) &&
				Objects.equals(stringValue, that.getStringValue().orElse(null)) &&
				Objects.equals(errorMessage, that.getErrorMessage());
	}

	@Override public int hashCode() {
		return Objects.hash(document, pointer, v2Value, stringValue, errorMessage);
	}
}
