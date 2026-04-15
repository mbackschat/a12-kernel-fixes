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
package com.mgmtp.a12.kernel.md.rt.api;

import java.util.Optional;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentFactory;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.NonNull;

/**
 * Provides access to the details of a single computed field instance.
 */
@OnlyForUsage
public interface IComputedFieldInstance {

	/**
	 * See {@link IEntityInstance#getPath()}.
	 *
	 * @deprecated use {@link #pointer} instead.
	 */
	@Deprecated(since = "30.0.0")
	String getPath();

	/**
	 * see {@link IEntityInstance#getRepetitions()}.
	 *
	 * @deprecated use {@link #pointer} instead.
	 */
	@Deprecated(since = "30.0.0")
	int[] getRepetitions();

	DocumentPointer pointer();

	/**
	 * @return the value that was computed. Can be empty in case of errors (see {@link #getErrorMessage()}).
	 *
	 * @deprecated use {@link #getValueV2()} instead
	 */
	@Deprecated(since = "30.0.0")
	Optional<Object> getValue();

	/**
	 * similar to {@link #getValue()}, but returns an object that is suitable for
	 * {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2}:
	 * <ul>
	 *     <li>where {@link #getValue()} returns a {@link java.util.Date}, this method returns an {@link java.time.Instant}</li>
	 *     <li>where {@link #getValue()} returns an array of {@link java.util.Date}, this method returns an
	 *     {@link com.mgmtp.a12.utils.conversion.InstantRange}</li>
	 *     <li>In stead of an Optional, this method returns a nullable (similar to {@link IFieldInstance#getValue()} vs {@link FieldInstanceV2#value()}</li>
	 * </ul>
	 */
	Object getValueV2();

	/**
	 * @return the computed value in string representation. Can be empty in case of errors (see {@link #getErrorMessage()}.
	 */
	Optional<String> getStringValue();

	/**
	 * @return an error message if there is a formal error with the computed value.
	 */
	Optional<IMessage> getErrorMessage();

	/**
	 * @return the {@link IDocument} based on which this {@link IComputedFieldInstance} was computed.
	 * 		Not a copy (i.e. includes all modifications made to the document since the computation).
	 * 		Is never {@code null}.
	 *
	 * @deprecated the document is no longer linked, methods that need the {@link DocumentV2} have a respective parameter
	 */
	@Deprecated(since = "30.0.0")
	default IDocument getDocument() {
		throw new UnsupportedOperationException(
				"Calling this method having your own implementation requires the implementation of the method!");
	}

	/**
	 * Applies {@code this} to the document from which {@code this} was created (see also
	 * {@link #getDocument()}):
	 * <ul>
	 * <li>If the document already contains a field instance which corresponds to {@code this}, the
	 * corresponding field instance in the document will be updated with the corresponding computed
	 * value.</li>
	 * <li>Otherwise, an {@link IFieldInstance}, which corresponds to {@code this}, is internally
	 * created and added to the document (in no specific order).</li>
	 * </ul>
	 * <p>
	 * <b>Important note:</b> This method does change the original document mentioned above.
	 *
	 * @param errorHandler
	 * 		operation which should be performed if an error occurred while computing
	 *        {@code this}. May not be {@code null}.
	 * @deprecated since Kernel version 29.0.0. Use {@link #applyTo(IDocument)} instead.
	 */
	@Deprecated(since = "29.0.0") // can only be removed when A12-17017 is done
	default void apply(Consumer<IComputedFieldInstance> errorHandler) {
		// default implementation in order not to be breaking
	}

	/**
	 * Applies the computed field instance's value to the provided {@link IDocument}.
	 * <p>
	 * The value of the {@link IFieldInstance} in the provided {@link IDocument} which corresponds to {@link #getPath()} and
	 * {@link #getRepetitions()} is set to {@link #getValue()}.
	 * <p>
	 * If no such {@link IFieldInstance} exists yet, and {@link #getValue()} is neither {@code null} or {@code ""}, such a
	 * {@link IFieldInstance} will be created and added to the document.
	 *
	 * @param document
	 * 		the {@link IDocument} to apply the change to. May not be {@code null}.
	 * @deprecated use {@link #applyTo(DocumentV2)} instead
	 */
	@Deprecated(since = "30.0.0")
	void applyTo(IDocument document);

	/**
	 * Extension of {@link #applyTo(IDocument)} (see description there).
	 * <p>
	 * Use this method only if you used your own implementation of {@link IDocument}/{@link IFieldInstance} while performing the
	 * computation (see also {@link DocumentDeserializationConfig#getDocumentFactory()}). Otherwise, you should use
	 * {@link #applyTo(IDocument)}.
	 *
	 * @param document
	 * 		the {@link IDocument} to apply the change to. May not be {@code null}.
	 * @param documentFactory
	 * 		the {@link IDocumentFactory} from which {@link IFieldInstance}s should be created if necessary (see
	 *        {@link #applyTo(IDocument)}). May not be {@code null}.
	 * @deprecated use {@link #applyTo(DocumentV2)} instead
	 */
	@Deprecated(since = "30.0.0")
	void applyTo(IDocument document, IDocumentFactory documentFactory);

	/**
	 * Applies the computed field instance's value to the provided {@link DocumentV2}.
	 * <p>
	 * The value of the {@link FieldInstanceV2} in the provided {@link DocumentV2} which corresponds to {@link #pointer()}
	 * is set to {@link #getValueV2()}.
	 * <p>
	 * If no such {@link FieldInstanceV2} exists yet, and {@link #getValueV2()} is neither {@code null} or {@code ""}, such a
	 * {@link FieldInstanceV2} will be created and added to the document.
	 *
	 * @param document
	 * 		the {@link DocumentV2} to apply the change to. May not be {@code null}.
	 */
	DocumentV2 applyTo(@NonNull DocumentV2 document);
}
