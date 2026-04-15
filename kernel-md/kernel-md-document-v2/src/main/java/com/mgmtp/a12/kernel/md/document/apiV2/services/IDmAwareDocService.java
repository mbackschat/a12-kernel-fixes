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

package com.mgmtp.a12.kernel.md.document.apiV2.services;

import java.io.Writer;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;

/**
 * Provides document (V2) related services that require information from a document model.
 * <br/>
 * An instance of this service can be created with {@code DocumentServiceFactory#createDmAwareDocService(String)}.
 * <br/><br/>
 * Instances of this service are specific to one particular document model.
 * Instances of this service are <em>not</em> specific to a particular <em>document</em> (in contrast to {@code IDocumentService}).
 * <br/><br/>
 * Note: for additional DocumentV2-related util methods (that are <em>not</em> DM-related), see also
 * {@link com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils}
 */
@OnlyForUsage
public interface IDmAwareDocService {
	/**
	 * similar to
	 * {@link com.mgmtp.a12.kernel.md.document.api.services.IDocumentService#convertToJavaType(IFieldInstance, String, Consumer)},
	 * but returns an object that is suitable for {@code DocumentV2} rather than {@code IDocument}; i.e. {@link Instant} instead of
	 * Date and {@code com.mgmtp.a12.utils.conversion.InstantRange} instead of {@code Date[]}.
	 */
	Object convertToJavaTypeV2(@NonNull String fieldFullName, String value,
			@NonNull Consumer<RankedNotification> notificationReceiver);

	/**
	 * similar to
	 * {@link com.mgmtp.a12.kernel.md.document.api.services.IDocumentSearchService#getById(String)},
	 * but returns only <em>group</em> instances.
	 *
	 * @param groupId
	 * 		the id of the group, as specified in the DM.
	 * @return for each group instance of the group identified by groupId that is present in the document, a pair with the
	 *        {@link DocumentPointer} that points to the group instance, and the group instance itself.
	 */
	Collection<Map.Entry<DocumentPointer, GroupInstanceV2>> getGroupInstancesById(@NonNull DocumentV2 document,
			@NonNull String groupId);

	/**
	 * similar to
	 * {@link com.mgmtp.a12.kernel.md.document.api.services.IDocumentSearchService#getById(String)},
	 * but returns only <em>field</em> instances.
	 *
	 * @param fieldId
	 * 		the id of the field, as specified in the DM.
	 * @return for each field instance of the field identified by fieldId that is present in the document, a pair with the
	 *        {@link DocumentPointer} that points to the field instance, and the field instance itself.
	 * 		a pair of the {@link DocumentPointer} and the respective {@link FieldInstanceV2} that match the id
	 */
	Collection<Map.Entry<DocumentPointer, FieldInstanceV2>> getFieldInstancesById(@NonNull DocumentV2 document,
			@NonNull String fieldId);

	/**
	 * Checks the provided {@link DocumentV2} if all fields of non-repeatable groups are present.
	 * The repeatability is defined in the corresponding {@link com.mgmtp.a12.kernel.md.model.api.IDocumentModel}.
	 * <p>
	 *
	 * @param document
	 * 		the document to check
	 * @param notificationReceiver
	 * 		the {@link RankedNotification}s, found during the process, will be reported to this callback.
	 * @return {@code true} if no cases were found, otherwise {@code false}.
	 *
	 * @deprecated is not necessary for the usage of the document, the validation can handle missing fields and groups.
	 */
	@Deprecated(since = "30.0.0")
	boolean checkPresenceOfFieldInstancesInNonRepeatableGroups(@NonNull DocumentV2 document,
			@NonNull Consumer<RankedNotification> notificationReceiver);

	/**
	 * Validate the structure of a given {@link DocumentV2} against the {@link com.mgmtp.a12.kernel.md.model.api.IDocumentModel}.
	 * The document model is selected based on the {@link DocumentV2#getDocumentModelId()} and the document model resolver of this
	 * IDocumentRtService.
	 * <p>
	 * Following cases will be reported:
	 * <ul>
	 * <li>If entity instances ({@link GroupInstanceV2}s or {@link FieldInstanceV2}s) in the
	 * {@link DocumentV2} are unknown regarding the {@link com.mgmtp.a12.kernel.md.model.api.IDocumentModel}.</li>
	 * <li>If a {@link GroupInstanceV2} is present at the path of a {@link com.mgmtp.a12.kernel.md.model.api.IField}.</li>
	 * <li>If a {@link FieldInstanceV2} is present at the path of a {@link com.mgmtp.a12.kernel.md.model.api.IGroup}.</li>
	 * <li>If anything is present at the path of a {@link com.mgmtp.a12.kernel.md.model.api.IRule} or {@link com.mgmtp.a12.kernel.md.model.api.IComputation}.</li>
	 * <li>If the number of repetitions of a group is larger than the defined repeatability in the DM.</li>
	 * </ul>
	 * <p>
	 * <b>Note:</b>
	 * <ul>
	 *     <li>If multiple documents are validated against the same DM, it is recommended that the underlying
	 *     {@link com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver} implements
	 *     {@link com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver#getDocumentModelSearchService(String)} and
	 *     caches the search service, to avoid repeated re-computation of the search service's index structures. In this case, make
	 *     sure to use a suitable {@link com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver} implementation when
	 *     instantiating the {@code DocumentRtServiceFactory} that is used to obtain the {@code IDocumentRtService}</li>
	 *     <li>The checks (except the repetition check) are also performed while serializing a document (see {@link
	 *     com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer#serializeV2(DocumentV2, Writer,
	 *     DocumentSerializationConfig)}).</li>
	 *     <li>For a validation including field values use {@code IDocumentRtService}</li>
	 * </ul>
	 *
	 * @param document
	 * 		the document to check
	 * @param notificationReceiver
	 * 		the {@link RankedNotification}s, found during the process, will be reported to this callback.
	 * @return {@code true} if no problems were found, otherwise {@code false}.
	 */
	boolean checkStructuralConsistencyWithDm(@NonNull DocumentV2 document,
			@NonNull Consumer<RankedNotification> notificationReceiver);

	/**
	 * Remove all transient fields of the configured DM present in the provided {@link DocumentV2}.
	 *
	 * @param document
	 * 		the document to remove the transient fields from.
	 * @return the adapted document if there were transient fields present, otherwise the original is returned.
	 */
	DocumentV2 removeTransientFields(@NonNull DocumentV2 document);

	/**
	 * Remove all groups and fields that are not defined in the configured DM.
	 * <p>
	 * This does not throw an error (but will remove the field/group element) if a field is present at a path where a group is
	 * defined or vice versa. For this use
	 * {@code IDocumentRtService#checkStructuralConsistencyWithDm(DocumentV2, Consumer)}.
	 *
	 * @param document
	 * 		the document to remove the unknown elements from.
	 * @return the document only containing elements defined in the DM.
	 */
	DocumentV2 removeUnknowns(@NonNull DocumentV2 document);
}
