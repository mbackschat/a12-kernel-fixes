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

package com.mgmtp.a12.kernel.md.document.api.services;

import java.io.Reader;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig.Format;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.Builder;
import lombok.Getter;

/**
 * This configuration controls
 * {@link IDocumentSerializer#deserialize(Reader, String, DocumentDeserializationConfig, Consumer)}.
 */
@Getter
@Builder
@OnlyForUsage
public class DocumentDeserializationConfig {

	/**
	 * The format in which the document was given (see {@link Format}). May not be
	 * {@code null}. Default is {@link Format#JSON}.
	 * <br/><br/>
	 * <b>WARNING:</b> If you use {@link Format#XML}, please note that the XML specification does not support all characters
	 * (see <a href="https://www.w3.org/TR/xml/#charsets">https://www.w3.org/TR/xml/#charsets</a>). It is the user's responsibility
	 * to ensure that only characters supported by XML are actually serialized to XML. If this is not followed, exceptions may
	 * occur during both serialization and deserialization. Since these are only edge cases, there is no check during serialization
	 * so as not to slow down performance. This check is also not part of the validation, because on the one hand it is not
	 * guaranteed that only valid {@link IDocument}s are serialized and if the project expects problems in this regard the modeler
	 * can and should specify the permitted characters in the document model.
	 */
	@Builder.Default
	private Format format = Format.JSON;

	/**
	 * Will be used for deserialization. May be {@code null} if the kernel internal
	 * implementation should be used.
	 *
	 * @deprecated this is no longer necessary, as new documents can be created by using one of the methods of {@code DocumentV2}.
	 */
	@Deprecated(since = "30.0.0")
	private IDocumentFactory documentFactory;

	/**
	 * Use {@code true} if transient fields should be added to the {@link IDocument} (in
	 * case they are not present), otherwise {@code false}. Default is {@code true}.
	 * The value of those added transient fields is {@code null}.
	 * <br/><br/>
	 * <b>Note</b>: Regardless of this flag transient fields will always be added if they are
	 * already present in the input.
	 *
	 * @deprecated is not necessary for the usage of the document, the validation can handle missing fields and groups.
	 */
	@Builder.Default
	@Deprecated(since = "30.0.0")
	private boolean addTransientFields = true;

	/**
	 * Use {@code true} if
	 * <ul>
	 *     <li>fields that either have no value ({@code null}) or an empty value ("")</li>
	 *     <li>and groups, that after the removal have no fields</li>
	 * </ul>
	 * should be removed.
	 * Default is {@code false}. This removal will be done before the
	 * addition of transient fields.
	 */
	@Builder.Default
	private boolean removeEmptyFieldsAndGroups = false;

	/**
	 * This configuration applies only if {@link #getFormat()} returns {@link Format#XML}.
	 * If <code>true</code>, the value of {@link IFieldInstance}s which were serialized as empty XML elements will be read as
	 * follows:
	 * <ul>
	 *     <li>&lt;fieldName&gt;&lt;/fieldName&gt; → <code>null</code></li>
	 *     <li>&lt;fieldName xsi:nil="true"&gt;&lt;/fieldName&gt; → <code>null</code></li>
	 *     <li>&lt;fieldName emptyValue="true"&gt;&lt;/fieldName&gt; → ""</li>
	 * </ul>
	 *
	 * <p>
	 * We deprecate this method since it should be used to migrate documents in XML format which are not managed in DataServices.
	 * In DataServices, the documents stored in the database are now in JSON format and DataServices will migrate them
	 * from XML to JSON automatically while upgrading to the corresponding version.
	 * This configuration can be used to read documents written with Kernel version 28.3.0 or older
	 * in order to preserve <code>null</code>-values for field instances,
	 * i.e. serialized field instances without value are deserialized with <code>null</code> as value, since
	 * with Kernel version 28.3.0 or older, there was no difference between <code>null</code>-
	 * and ""-values while (de)serializing documents.
	 * <p>
	 * It is recommended to read the documents with this configuration and write them with the current Kernel serializer.
	 * After this migration, this configuration must not be used anymore.
	 *
	 * @deprecated since Kernel version 29.1.0.
	 */
	@Deprecated(since = "Kernel version 29.1.0", forRemoval = true)
	@Builder.Default
	private boolean readEmptyAsNull = false;

	/**
	 * While deserializing a document in which entity instances are present that are not defined in the corresponding
	 * {@link IDocumentModel} (let's call them unknown entity instances), an error is reported per unknown entity instance and the
	 * resulting {@link IDocument} does not contain those {@link IEntityInstance}s.
	 * <p>
	 * Setting this configuration to {@code true} leads to ignore the unknown entity instances completely without reporting the
	 * errors mentioned above, this means that the resulting {@link IDocument} only contains {@link IEntityInstance}s which are
	 * defined in the corresponding {@link IDocumentModel}.
	 */
	@Builder.Default
	private boolean ignoreUnknownEntities = false;
}
