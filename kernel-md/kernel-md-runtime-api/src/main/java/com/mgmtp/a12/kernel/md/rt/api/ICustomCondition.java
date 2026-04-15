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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.internal.service.impl.EntityInstanceImpl;
import com.mgmtp.a12.kernel.md.document.internal.service.implV2.DocumentV1V2ConverterImpl;

import lombok.NonNull;

/**
 * Interface for custom conditions.<br>
 * Custom conditions can be used in rule conditions for project specific checks that can not be
 * expressed by the validation language. The implementation of this interface is provided by the
 * project and must be available at execution time of the validation.
 * <p>
 * Formerly known as <i>application condition</i>.
 * <p>
 * <b>Attention: </b>The kernel validation and thus especially custom conditions (as part of it) are designed exclusively
 * for the validation of values supplied in {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2}. Checks that are
 * independent of the {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2} should be implemented at the appropriate
 * extension point (e.g. in Data Services).
 * <p>
 * <b>Note:</b> The caller has to provide an (or several) implementation(s) of this interface.
 */
public interface ICustomCondition {

	/**
	 * The validation logic as a callback function.
	 *
	 * @param document
	 * 		<em>not null</em> -
	 * 		the document to be validated
	 * @param relevantEntityInstances
	 * 		all relevant entity instances for this check
	 * 		<ul>
	 * 		    <li>full validation: <code>null</code> (all entity instances are relevant)</li>
	 * 		    <li>partial validation: the relevant entity
	 * 		      instances that were passed to the validation call
	 * 		      (see {@link IDocumentRtService#validatePart})</li>
	 * 		</ul>
	 * @param formallyIncorrectEntityInstances
	 * 		<em>not null</em> -
	 * 		the entity instances that are formally incorrect.
	 * 		In kernel runtime formal incorrect entity instances
	 * 		are not relevant for further checks to avoid additional
	 * 		errors. Therefore formal incorrect entity instances
	 * 		should be ignored by the custom condition check.
	 * @param errorEntityInstance
	 * 		<em>not null</em> -
	 * 		the error entity instance representing the error field
	 * 		of the rule whose rule condition specifies this custom
	 * 		condition.
	 * 		<br><br>
	 * 		Remark {@link IEntityInstance#getRepetitions}:
	 * 		<ul>
	 * 		<li>used in an iteration: the repetitions specify concrete values.
	 * 		The rule and therefore the check method is evaluated for each repetition
	 * 		and contains the concrete index of the current repetition.
	 * 		</li>
	 * 		<li>used in a parallel iteration: an index value may, in special
	 * 		 cases be unknown (see IIdentifier#UNKNOWN). In these cases, a concrete index
	 * 		 for this layer of the error field cannot be assigned.</li>
	 * 		</ul>
	 * @return <code>true</code>, if the condition was met, <code>false</code>, if<br>
	 * 		a) one or more necessary fields are <em>not</em> included in relevantEntityInstances<br>
	 * 		b) one or more necessary fields are included in formallyIncorrectEntityInstances<br>
	 * 		c) condition is not met<br>
	 * 		<em><b>Note:</b> Conditions describe the error case, i.e. if <code>true</code> is returned here, the rule in
	 * 		which this custom condition was specified reports an error.</em>
	 *
	 * @deprecated implement and use {@link #check(DocumentV2, Set, Set, PartiallyKnownDocumentMultiPointer)} instead.
	 */
	@Deprecated(since = "30.0.0")
	default boolean check(IDocument document,
			Set<IEntityInstance> relevantEntityInstances,
			Set<IEntityInstance> formallyIncorrectEntityInstances,
			IEntityInstance errorEntityInstance) {
		// this method is called only if check(DocumentV2, ...) is not implemented
		throw new RuntimeException(
				"ICustomCondition#check(DocumentV2, ...) should be implemented. It _must_ be implemented if no " +
						"implementation for the old method ICustomCondition#check(IDocument, ...) is available as a fallback.");
	}

	/**
	 * The validation logic as a callback function.
	 *
	 * @param document
	 * 		the document to be validated
	 * @param relevantEntities
	 * 		all relevant entities for this check
	 * 		<ul>
	 * 		    <li>full validation: <code>null</code> (all entity instances are relevant)</li>
	 * 		    <li>partial validation: the relevant entity
	 * 		      instances that were passed to the validation call
	 * 		      (see {@link IDocumentRtService#validatePart(DocumentV2, Set, DocumentProcessingConfig)})</li>
	 * 		</ul>
	 * @param formallyIncorrectEntities
	 * 		the entity instances that are formally incorrect.
	 * 		In kernel runtime formal incorrect entity instances
	 * 		are not relevant for further checks to avoid additional
	 * 		errors. Therefore, formal incorrect entity instances
	 * 		should be ignored by the custom condition check.
	 * @param errorEntityInstance
	 * 		the error entity instance representing the error field
	 * 		of the rule whose rule condition specifies this custom
	 * 		condition.
	 * 		<br><br>
	 * 		Remark {@link PartiallyKnownDocumentMultiPointer#repetitionIndexes()}:
	 * 		<ul>
	 * 		<li>used in an iteration: the repetitions specify concrete values.
	 * 		The rule and therefore the check method is evaluated for each repetition
	 * 		and contains the concrete index of the current repetition.
	 * 		</li>
	 * 		<li>used in a parallel iteration: an index value may, in special
	 * 		 cases be unknown (see {@link PartiallyKnownDocumentMultiPointer#UNKNOWN}). In these cases, a concrete index
	 * 		 for this layer of the error field cannot be assigned and
	 *                   {@link PartiallyKnownDocumentMultiPointer#toDocumentMultiPointer()} will return empty</li>
	 * 		</ul>
	 * @return <code>true</code>, if the condition was met, <code>false</code>, if<br>
	 * 		a) one or more necessary fields are <em>not</em> included in relevantEntityInstances<br>
	 * 		b) one or more necessary fields are included in formallyIncorrectEntityInstances<br>
	 * 		c) condition is not met<br>
	 * 		<em><b>Note:</b> Conditions describe the error case, i.e. if <code>true</code> is returned here, the rule in
	 * 		which this custom condition was specified reports an error.</em>
	 *
	 * @implNote implementations of ICustomCondition should implement this method, despite the apparent presence of a default
	 * 		implementation (instead of implementing {@link #check(IDocument, Set, Set, IEntityInstance)}).
	 * 		The default implementation is only temporary and delegates to the old {@link #check(IDocument, Set, Set, IEntityInstance)}
	 * 		method which will be removed (together with the default implementation) in an upcoming mayor release.
	 */
	default boolean check(
			@NonNull DocumentV2 document,
			Set<? extends DocumentMultiPointer> relevantEntities,
			@NonNull Set<DocumentPointer> formallyIncorrectEntities,
			@NonNull PartiallyKnownDocumentMultiPointer errorEntityInstance
	) {
		Set<IEntityInstance> relevantEntityInstancesConverted = relevantEntities == null ? null : relevantEntities.stream()
				.map(p ->
						toEntityInstance(p.fullName(), p.repetitionIndexes()))
				.collect(Collectors.toSet());

		Set<IEntityInstance> formallyIncorrectEntityInstancesConverted = formallyIncorrectEntities.stream()
				.map(p -> toEntityInstance(p.fullName(), p.repetitionIndexes()))
				.collect(
						Collectors.toSet());

		return check(DocumentV1V2ConverterImpl.getInstance().toDocumentV1(document), relevantEntityInstancesConverted,
				formallyIncorrectEntityInstancesConverted,
				toEntityInstance(errorEntityInstance.fullName(), errorEntityInstance.repetitionIndexes()));
	}

	/**
	 * @deprecated remove when check(IDocument, ...) is no longer needed
	 */
	@Deprecated(since = "30.0.0")
	private static EntityInstanceImpl toEntityInstance(String p, List<Integer> p1) {
		return new EntityInstanceImpl(p,
				p1.stream().mapToInt(i -> i).toArray());
	}
}
