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

import java.io.Writer;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.model.serialization.A12DefaultJsonPrettyPrinter;
import com.mgmtp.a12.model.utils.OnlyForUsage;

import lombok.Builder;
import lombok.Getter;
import tools.jackson.core.PrettyPrinter;

/**
 * This configuration controls
 * {@link IDocumentV2Serializer#serializeV2(DocumentV2, Writer, DocumentSerializationConfig)}.
 */
@Getter
@Builder
@OnlyForUsage
public class DocumentSerializationConfig {

	/**
	 * Format of a {@link DocumentV2}.
	 */
	public enum Format {
		JSON,
		XML
	}

	/**
	 * <b>WARNING:</b> If you use {@link Format#XML}, please note that the XML specification does not support all characters
	 * (see <a href="https://www.w3.org/TR/xml/#charsets">https://www.w3.org/TR/xml/#charsets</a>). It is the user's responsibility
	 * to ensure that only characters supported by XML are actually serialized to XML. If this is not followed, exceptions may
	 * occur during both serialization and deserialization. Since these are only edge cases, there is no check during serialization
	 * so as not to slow down performance. This check is also not part of the validation, because on the one hand it is not
	 * guaranteed that only valid {@link DocumentV2}s are serialized and if the project expects problems in this regard the modeler
	 * can and should specify the permitted characters in the document model.
	 * <br>
	 * The format to which the document should be serialized (see {@link Format}). May
	 * not be {@code null}. Default is {@link Format#JSON}.
	 */
	@Builder.Default
	private Format format = Format.JSON;

	/**
	 * Use {@code true} if transient fields should not appear in the serialized
	 * document, otherwise {@code false}. Default is {@code true}.
	 */
	@Builder.Default
	private boolean skipTransientFields = true;

	/**
	 * Use {@code true} if
	 * <ul>
	 *     <li>fields that either have no value ({@code null}) or an empty value ("")</li>
	 *     <li>and groups, that after the removal have no fields</li>
	 * </ul>
	 * should be removed.
	 * Default is {@code false}. The removal of empty fields will be done after the
	 * removal of transient fields.<br/><br/>
	 * This is suitable to remove instances of repeatable groups and
	 * will close resulting gaps automatically.
	 */
	@Builder.Default
	private boolean removeEmptyFieldsAndGroups = false;

	/**
	 * Pretty printers are used to add white space in output JSON content, to make results more human readable. Usually this
	 * means things like adding linefeeds and indentation. You can either set your own implementation of the interface to
	 * change the default behavior or set {@code null} to deactivate pretty printing.
	 */
	@Builder.Default
	private PrettyPrinter prettyJsonPrinter = new A12DefaultJsonPrettyPrinter();

	/**
	 * While serializing a {@link DocumentV2} in which entities
	 * ({@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2 fields}/{@link
	 * com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2 groups}) are present that are not defined in the
	 * corresponding {@link com.mgmtp.a12.kernel.md.model.api.IDocumentModel} (let's call them unknown entities), a
	 * {@link DocumentSerializationException} is thrown.
	 * <p>
	 * Setting this configuration to {@code true} leads to the following behavior if the {@link DocumentV2} contains unknown
	 * entities:
	 * <ul>
	 *     <li>No {@link DocumentSerializationException} is thrown.</li>
	 *     <li>The unknown {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2 fields}/{@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2 groups} will be ignored, i.e. they are not present in the serialized document.</li>
	 * </ul>
	 */
	@Builder.Default
	private boolean ignoreUnknownEntities = false;
}
