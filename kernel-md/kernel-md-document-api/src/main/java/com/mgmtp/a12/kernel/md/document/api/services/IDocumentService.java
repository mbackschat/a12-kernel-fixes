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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IDocumentIndexed;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstanceDerivable;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.IGroupInstance;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.model.notification.RankedNotification;

/**
 * This service offers actions which can be performed on a {@link IDocument}.
 *
 * @deprecated use the alternatives listed in each of the methods instead.
 */
@Deprecated(since = "30.0.0")
@OnlyForUsage
public interface IDocumentService {

	/**
	 * The given value will be converted to the corresponding java type based on the
	 * {@link IFieldType} of the corresponding {@link IField} determined by the given
	 * {@link IFieldInstance}. Only following java types are supported:
	 * <ul>
	 * <li>{@link String}</li>
	 * <li>{@link BigDecimal}</li>
	 * <li>{@link Boolean}</li>
	 * <li>{@link Date}</li>
	 * <li>{@link Date}[2]: the first date object is always the start of the range and the second
	 * the end</li>
	 * </ul>
	 * <b>Notes:</b>
	 * <ul>
	 * <li>For date fields which allow partially known dates, a {@link String} can be returned in
	 * case the given value contains an unknown date part, e.g. {@code 2021-01-00}.</li>
	 * <li>For number fields which allow leading zeros, a {@link String} can be returned in case the
	 * given value contains at least a leading zero, e.g. {@code 030}.</li>
	 * <li>This method is independent of the document that is wrapped by the IDocumentService instance; but it does depend on the
	 * document <em>model</em>, in order to determine field types.</li>
	 * </ul>
	 *
	 * @param fieldInstance
	 * 		the specific {@link IFieldInstance}. May not be {@code null}.
	 * @param value
	 * 		string representation of the {@link IFieldInstance}s value.
	 * @param notificationReceiver
	 * 		the {@link RankedNotification}s, found during the process, will be reported to this callback. May not be {@code null}.
	 * @return the corresponding converted value or {@code null} in following cases:
	 * 		<ul>
	 * 		<li>if the given value is {@code null} or empty</li>
	 * 		<li>if problems occurred while converting the original value</li>
	 * 		</ul>
	 *
	 * @deprecated use {@code IDmAwareDocService#convertToJavaTypeV2(String, String, Consumer)} instead.
	 */
	@Deprecated(since = "30.0.0")
	Object convertToJavaType(IFieldInstance fieldInstance, String value, Consumer<RankedNotification> notificationReceiver);

	/**
	 * similar to {@link #convertToJavaType(IFieldInstance, String, Consumer)}, but returns an object that is suitable for
	 * {@code DocumentV2} rather than {@code IDocument}; i.e. {@link Instant} instead of Date and
	 * {@code com.mgmtp.a12.utils.conversion.InstantRange} instead of {@code Date[]}.
	 * <p/>
	 * Note that this method is independent of the document that is wrapped by the IDocumentService instance; but it does depend
	 * on the document <em>model</em>, in order to determine field types.
	 *
	 * @deprecated use {@code IDmAwareDocService#convertToJavaTypeV2(String, String, Consumer)} instead.
	 */
	@Deprecated(since = "29.4.0", forRemoval = true)
	Object convertToJavaTypeV2(String fieldFullName, String value, Consumer<RankedNotification> notificationReceiver);

	/**
	 * Sets the given {@code value} in the {@link IFieldInstance} which was found with the given {@code path} and
	 * {@code repetitions} in the {@link IDocument} which was used to instantiate {@code this}. This means that if the found
	 * {@link IFieldInstance} had a value before, this old value will be replaced by the given {@code value}.
	 * If the {@link IDocument} does not
	 * contain an {@link IFieldInstance} with the given {@code path} and {@code repetitions}, a new {@link IFieldInstance} will be
	 * created with the given parameters (also {@code value}) and added to the {@link IDocument}. See also the parameter
	 * {@code documentFactory}, the method {@link #setValue(Object, String, int[])} and the notes in
	 * {@link IDocument#addEntityInstance(IEntityInstance)}.
	 * <pr>
	 * If the given {@code value} is a {@link String}, it will be converted to the corresponding Java type, see
	 * {@link #convertToJavaType(IFieldInstance, String, Consumer)}.
	 *
	 * @param value
	 * 		value which should be converted and set in the {@link IFieldInstance}.
	 * @param path
	 * 		the path of the {@link IField} the {@link IFieldInstance} is referring to. May not be {@code null} or blank. Further
	 * 		explanations can be found in {@link IEntityInstance#getPath()}.
	 * @param repetitions
	 * 		the specific repetitions of the {@link IFieldInstance}. Further explanations can be found in
	 *        {@link IEntityInstance#getRepetitions()}.<br>May be {@code null}. If {@code null} the repetition of every hierarchy level
	 * 		is interpreted as {@link IEntityInstance#ALL_SEMANTIC}.
	 * @param documentFactory
	 * 		factory with which a new {@link IFieldInstance} will be created in case none is found with the given parameters. May be
	 *        {@code null}. If {@code null}, the kernel internal implementation will be used. If no new {@link IFieldInstance} should be
	 * 		created, see {@link #setValue(Object, String, int[])}.
	 * @throws IllegalArgumentException
	 * 		if the given array with the repetitions has more entries as the number of path parts or if the given {@code value} could
	 * 		not be converted to the corresponding Java type or if the given path is blank.
	 * @throws IllegalStateException
	 * 		if the {@link IDocument} contains more than one {@link IFieldInstance}s with the given parameters or if an
	 *        {@link IGroupInstance} was found instead.
	 * @deprecated use {@code GroupInstanceV2#withField} or {@code GroupInstanceV2#withFieldValue} instead.
	 */
	@Deprecated(since = "30.0.0")
	void setValue(Object value, String path, int[] repetitions, IDocumentFactory documentFactory);

	/**
	 * The difference to {@link #setValue(Object, String, int[], IDocumentFactory)} is that no new {@link IFieldInstance} will be
	 * created if no {@link IFieldInstance} was found with the given parameters, but an {@link IllegalStateException} will be thrown
	 * in this case.
	 *
	 * @throws IllegalStateException
	 * 		if the {@link IDocument} contains more than one {@link IFieldInstance}s with the given parameters or if an
	 *        {@link IGroupInstance} was found instead or no entity instance was found at all with the given parameters.
	 * @deprecated use {@code GroupInstanceV2#withField} or {@code GroupInstanceV2#withFieldValue} instead.
	 */
	@Deprecated(since = "30.0.0")
	void setValue(Object value, String path, int[] repetitions);

	/**
	 * Sets the given {@code value} in the given {@link IFieldInstance}. If the {@link IFieldInstance} had a value before, this old
	 * value will be replaced by the given {@code value}.
	 * If the given value is a {@link String}, it will be
	 * converted to the corresponding Java type, see {@link #convertToJavaType(IFieldInstance, String, Consumer)}.
	 *
	 * @param value
	 * 		value which should be converted and set in the {@link IFieldInstance}.
	 * @param fieldInstance
	 *        {@link IFieldInstance} in which the given {@code value} should be set. May not be {@code null}.
	 * @throws IllegalArgumentException
	 * 		if the given {@code value} could not be converted to the corresponding Java type.
	 * @deprecated use {@code GroupInstanceV2#withField} or {@code GroupInstanceV2#withFieldValue} instead.
	 */
	@Deprecated(since = "30.0.0")
	void setValue(Object value, IFieldInstance fieldInstance);

	/**
	 * Validate the given {@link IDocument} against the {@link IDocumentModel} given
	 * while creating the instance of this service (see
	 * DocumentServiceFactory#createDocumentService(IDocument)). Following
	 * cases will be reported:
	 * <ul>
	 * <li>When a {@link IGroupInstance} was created for a {@link IField}.</li>
	 * <li>When a {@link IFieldInstance} was created for a {@link IGroup}.</li>
	 * <li>When the number of repetitions is not equal to the number of path parts.</li>
	 * <li>When a repetition is zero or negative.</li>
	 * <li>When the repetition for a {@link IField} itself is not 1.</li>
	 * <li>When entity instances ({@link IGroupInstance}s or {@link IFieldInstance}s) in the
	 * {@link IDocument} are unknown regarding the {@link IDocumentModel}.</li>
	 * <li>When the corresponding instance of a non-repeatable group defined in the
	 * {@link IDocumentModel} was not found in the {@link IDocument}.</li>
	 * </ul>
	 * <p>
	 * <b>Note:</b> Except for the last check, all other checks are also performed while serializing
	 * a document (see
	 * {@link IDocumentSerializer#serialize(IDocument, Writer, DocumentSerializationConfig)}).
	 *
	 * @param notificationReceiver
	 * 		the {@link RankedNotification}s, found during the process, will be reported to this callback. May not be {@code null}.
	 * @return {@code true} if no cases were found, otherwise {@code false}.
	 *
	 * @deprecated use {@code IDocumentRtService#checkStructuralConsistencyWithDm} instead.
	 */
	@Deprecated(since = "30.0.0")
	boolean checkEntities(Consumer<RankedNotification> notificationReceiver);

	/**
	 * Checks that the corresponding {@link IGroupInstance} of all non-repeatable {@link IGroup}s
	 * contains all {@link IFieldInstance} children (direct descendants).
	 * <p>
	 * <b>Note:</b> It is advisable to call {@link #checkEntities(Consumer)} before calling
	 * this method.
	 *
	 * @param notificationReceiver
	 * 		the {@link RankedNotification}s, found during the process, will be reported to this callback. May not be {@code null}.
	 * @return {@code true} if no cases were found, otherwise {@code false}.
	 *
	 * @deprecated use {@code IDmAwareDocService#checkPresenceOfFieldInstancesInNonRepeatableGroups} instead.
	 */
	@Deprecated(since = "30.0.0")
	boolean checkPresenceOfFieldInstancesInNonRepeatableGroups(Consumer<RankedNotification> notificationReceiver);

	/**
	 * Like {@link #removeEntityTree(IGroupInstance, BiFunction)}. Use this method only if the used implementation of
	 * {@link IEntityInstance} does implement {@link IEntityInstanceDerivable} or you are using the kernel internal implementation.
	 *
	 * @throws IllegalArgumentException
	 * 		If an {@link IEntityInstance} is encountered that 1) needs its repetitions updated to close a gap in the document and 2) is
	 * 		neither an {@link IGroupInstance} nor an {@link IFieldInstance} and 3) does not implement {@link IEntityInstanceDerivable}.
	 * @deprecated use {@code GroupInstanceV2#withGroupRemoved} or {@code GroupInstanceV2#withGroupEmptiedButNotRemoved} instead.
	 */
	@Deprecated(since = "30.0.0")
	boolean removeEntityTree(IGroupInstance groupInstance);

	/**
	 * Removes the given {@link IGroupInstance} together with all its descendants from the given
	 * {@link IDocument}.
	 * <p/>
	 * Any gaps in repetitions that may result from the removal will be closed, i.e. the repetition indexes of higher repetitions
	 * will be reduced. For example, having a document with following entity instances:
	 * <ul>
	 *     <li>{@code /G1[1]}</li>
	 *     <li>{@code /G1[1]/G2[1]}</li>
	 *     <li>{@code /G1[1]/G2[1]/F1[1]} with value 'test1'</li>
	 *     <li>{@code /G1[1]/G2[2]}</li>
	 *     <li>{@code /G1[1]/G2[2]/F1[1]} with value 'test2'</li>
	 *     <li>{@code /G1[1]/G2[3]}</li>
	 *     <li>{@code /G1[1]/G2[3]/F1[1]} with value 'test3'</li>
	 * </ul>
	 * removing {@code /G1[1]/G2[2]} would result in:
	 * <ul>
	 *     <li>{@code /G1[1]}</li>
	 *     <li>{@code /G1[1]/G2[1]}</li>
	 *     <li>{@code /G1[1]/G2[1]/F1[1]} with value 'test1'</li>
	 *     <li>{@code /G1[1]/G2[2]}</li>
	 *     <li>{@code /G1[1]/G2[2]/F1[1]} with value 'test3'</li>
	 * </ul>
	 * Note that {@code /G1[1]/G2[3]} changed to {@code /G1[1]/G2[2]} and {@code /G1[1]/G2[3]/F1[1]} (with value 'test3') changed
	 * to {@code /G1[1]/G2[2]/F1[1]} (still with value 'test3').
	 * <p/>
	 * We need to performe this change in order to make the resulting document valid and serializable since gaps are not allowed
	 * while serializing a document. For this change we need the parameter {@code deriveEntityInstanceWithNewRepetitions}.
	 * <p/>
	 * <b>Note:</b> Implicitly added group instances, which are ancestors of the given group
	 * instance, will be also removed, if and only if they were implicitly added through the given
	 * group instance or a descendant of it. For example: having {@code /G1[1]/G2[1]/F1[1]} and
	 * {@code /G3[1]/G4[1]/F2[1]} as the only explicitly added entity instances, removing
	 * {@code /G1[1]/G2[1]} will result in removing {@code /G1[1]/G2[1]/F1[1]}, {@code /G1[1]/G2[1]}
	 * (which was added implicitly through {@code /G1[1]/G2[1]/F1[1]}) and also {@code /G1[1]}
	 * (which was also added implicitly through {@code /G1[1]/G2[1]/F1[1]}).
	 *
	 * @param groupInstance
	 * 		the {@link IGroupInstance} which should be recursively removed from the given
	 *        {@link IDocument}. May not be {@code null}.
	 * @param deriveEntityInstanceWithNewRepetitions
	 * 		a function that defines how to create a clone of an existing {@link IEntityInstance} but with different repetitions
	 * 		(derived entity instance). This function is needed in case the used implementation of {@link IEntityInstance} does
	 * 		<b>not</b> implement {@link IEntityInstanceDerivable}. If the used implementation of {@link IEntityInstance} does
	 * 		implement {@link IEntityInstanceDerivable} or you are using the kernel internal implementation, consider using
	 *        {@link #removeEntityTree(IGroupInstance)}.
	 * @return {@code true} if the given {@link IDocument} contained the specified
	 *        {@link IGroupInstance} or at least one of its descendants, otherwise {@code false}.
	 *
	 * @deprecated use {@code GroupInstanceV2#withGroupRemoved} or {@code GroupInstanceV2#withGroupEmptiedButNotRemoved} instead.
	 */
	@Deprecated(since = "30.0.0")
	boolean removeEntityTree(
			IGroupInstance groupInstance,
			BiFunction<IEntityInstance, int[], IEntityInstance> deriveEntityInstanceWithNewRepetitions);

	/**
	 * Create a copy of the document and return a {@link IDocumentService} sharing model and search service with this one.
	 *
	 * @param documentFactory
	 * 		the {@link IDocumentFactory} used to create the new {@link IDocument} and its constituents. May be
	 * 		<code>null</code>, in which case, the default implementation will be used.
	 * @return a {@link IDocumentService} containing a copy of the current {@link IDocument}.
	 *
	 * @deprecated no longer needed as DocumentV2 modification operations automatically create and return new "copies" of the
	 * 		modified document or group. There is no need for defensive copies. (DocumentV2 and GroupInstanceV2 are immutable, with
	 * 		copy-on-write updates).
	 */
	@Deprecated(since = "30.0.0")
	IDocumentService copyDocument(IDocumentFactory documentFactory);

	/**
	 * Removes all transient fields (see {@link IField#isTransient()}) from the given {@link IDocument}, the service was created
	 * for.
	 * <br/><br/>
	 * <b>Note</b>: Depending on the implementation, a new {@link IDocument} might be created.
	 *
	 * @return <code>true</code> if the {@link IDocument} was changed.
	 *
	 * @deprecated use {@code IDmAwareDocService#removeTransientFields} instead.
	 */
	@Deprecated(since = "30.0.0")
	boolean removeTransientFields();

	/**
	 * Removes all
	 * <ul>
	 *     <li>fields that either have no value ({@code null}) or an empty value ("")</li>
	 *     <li>and groups, that after the removal have no fields</li>
	 * </ul>
	 * from the {@link IDocument}, this service was created for.
	 * <br/><br/>
	 * <b>Note</b>: Depending on the implementation, a new {@link IDocument} might be created.
	 *
	 * @return <code>true</code> if the {@link IDocument} was changed.
	 *
	 * @deprecated use {@code DocumentV2Utils#removeEmptyFieldsAndGroups} instead.
	 */
	@Deprecated(since = "30.0.0")
	boolean removeEmptyFieldsAndGroups();

	/**
	 * Removes all the entities instances from the {@link IDocument}, this service was created for, which are <b>not</b> defined in
	 * the corresponding document model.
	 * <br/><br/>
	 * <b>Note</b>: Depending on the implementation, a new {@link IDocument} might be created.
	 *
	 * @return <code>true</code> if the {@link IDocument} was changed.
	 *
	 * @deprecated use {@code IDmAwareDocService#removeUnknowns} instead.
	 */
	@Deprecated(since = "30.0.0")
	boolean removeUnknownEntityInstances();

	/**
	 * @return the {@link IDocument}, the service was created for.
	 *
	 * @deprecated no longer supported.
	 */
	@Deprecated(since = "30.0.0")
	IDocument getDocument();

	/**
	 * @return the {@link IDocumentModel} for the {@link IDocument}, the service was created for.
	 *
	 * @deprecated no longer supported.
	 */
	@Deprecated(since = "30.0.0")
	IDocumentModel getDocumentModel();

	/**
	 * @return the {@link IDocumentSearchService} for the model of the {@link IDocument}, the service was created for.
	 *
	 * @deprecated no longer supported.
	 */
	@Deprecated(since = "30.0.0")
	IDocumentModelSearchService getDocumentModelSearchService();

	/**
	 * Compares the {@link IFieldInstance}s in the {@link IDocument} contained in {@code this}
	 * {@link IDocumentService}-instance (later identified as {@code document}) to the {@link IFieldInstance}s in the given
	 * {@code reference}. Following changes can be returned:
	 * <ul>
	 * <li>{@link IEntityInstanceChangeInfo.EntityInstanceChangeType#NEW}, {@link IFieldInstance}
	 * exists in {@code document}
	 * but not in {@code reference}.</li>
	 * <li>{@link IEntityInstanceChangeInfo.EntityInstanceChangeType#DELETED},
	 * {@link IFieldInstance} does not exist in
	 * {@code document} but in {@code reference}.</li>
	 * <li>{@link IEntityInstanceChangeInfo.EntityInstanceChangeType#UPDATED},
	 * {@link IFieldInstance} exists in both
	 * {@code document} and {@code reference}, and the value is different (see Note 1 below).</li>
	 * </ul>
	 * <p>
	 * <b>Note 1:</b> A {@link IFieldInstance} exists in a {@link IDocument}-instance only if its
	 * value is not {@code null} nor empty.
	 * <b>Note 2:</b> If you are having performance problems and you are using an own implementation of {@link IDocument},
	 * you can implement {@link IDocumentIndexed} instead.
	 *
	 * @param reference
	 *        {@link IDocument}-instance which will act as the reference to determine the
	 * 		changes in {@code document}. May not be {@code null}.
	 * @return the needed information about the changed entity instances. If there were no changes,
	 * 		the collection is empty.
	 *
	 * @deprecated use {@code DocumentV2Utils#compare} instead.
	 */
	@Deprecated(since = "30.0.0")
	Collection<IEntityInstanceChangeInfo> compare(IDocument reference);
}
