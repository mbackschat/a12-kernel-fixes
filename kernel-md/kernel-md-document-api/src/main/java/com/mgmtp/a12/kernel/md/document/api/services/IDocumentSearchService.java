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

import java.util.Optional;
import java.util.Set;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.api.IFieldInstance;
import com.mgmtp.a12.kernel.md.document.api.IGroupInstance;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Search functionality for {@link IDocument}s. Some methods of this service will be explained with
 * examples based on the following structure of an {@link IDocumentModel} (the number inside the
 * brackets specifies to the max amount of repetitions of the group (its repeatability), see
 * {@link IGroup#getRepeatability()}):
 *
 * <pre>
 * G[5]
 * |- F1
 * |- G1[5]
 * |- |- F2
 * |- G2[1]
 * |- |- F3
 * </pre>
 * <p>
 * and also based on the assumption that the {@link IDocument}, in which entities will be searched,
 * is formed as follows:
 *
 * <pre>
 * IDocument.addEntityInstance("/G/G2/F3", new int[] { 1, 1, 1 });
 * IDocument.addEntityInstance("/G/G2/F3", new int[] { 2, 1, 1 });
 * </pre>
 *
 * <b><i>Note:</i></b> Related to the returned values of some methods, {@link IGroupInstance}s
 * could be returned that have not been added to the {@link IDocument} explicitly (see
 * {@link IDocument#addEntityInstance(IEntityInstance)}). These {@link IGroupInstance}s
 * can help the user to find further {@link IEntityInstance}s in the {@link IDocument}.<br>
 * In the example above the {@link IGroupInstance}s <code>("/G/G2", new int[]{1, 1})</code> and
 * <code>("/G/G2", new int[]{2, 1})</code> will be found, but not
 * <code>("/G/G2", new int[]{3, 1})</code>.
 * <br>
 * The fact that these {@link IGroupInstance}s are returned does <b>not</b> mean that the original
 * {@link IDocument} was modified.
 *
 * @deprecated use {@code IDmAwareDocService} and {@code DocumentV2Utils} instead.
 */
@OnlyForUsage
@Deprecated(since = "30.0.0")
public interface IDocumentSearchService {

	/**
	 * Searches for {@link IEntityInstance}s by the given {@link IElement} id. This a special case
	 * of {@link #get(String, int[])} having the {@link IElement} id instead of the {@link IElement}
	 * path and {@code null} as the repetition array.
	 *
	 * @param id
	 * 		the id of the corresponding {@link IElement} in the {@link IDocumentModel}. May
	 * 		not be {@code null} or blank.
	 * @return all {@link IEntityInstance}s with the given id. If no element instance is
	 * 		found, the set is empty.
	 *
	 * @throws IllegalArgumentException
	 * 		if the given id is blank.
	 * @deprecated use {@code IDmAwareDocService#getGroupInstancesById} or {@code IDmAwareDocService#getFieldInstancesById} instead.
	 */
	@Deprecated(since = "30.0.0")
	Set<IEntityInstance> getById(String id);

	/**
	 * Searches for {@link IEntityInstance}s by the specified {@link IElement} path and repetitions.
	 * <p>
	 * <b>Example:</b><br>
	 * Taking the structure and assumption in {@link IDocumentSearchService}
	 * <ul>
	 * <li><code>get("/G", new int[]{1})</code> {@literal ->} set with a single
	 * {@link IGroupInstance} with
	 * path {@code /G} and repetition {@code 1}</li>
	 * <li><code>get("/G", new int[]{0})</code> {@literal ->} set with following
	 * {@link IGroupInstance}s:
	 * <ul>
	 * <li>path {@code /G} and repetition {@code 1}</li>
	 * <li>path {@code /G} and repetition {@code 2}</li>
	 * </ul>
	 * </li>
	 * <li><code>get("/G", null)</code> {@literal ->} same as last point</li>
	 * <li><code>get("/G", new int[]{3})</code> {@literal ->} empty set</li>
	 * <li><code>get("/G/G2", new int[]{1, 1})</code> {@literal ->} set with a single
	 * {@link IGroupInstance}
	 * with path {@code /G/G2} and repetitions {@code [1, 1]}</li>
	 * <li><code>get("/G/G2/F3", null)</code> {@literal ->} set with following
	 * {@link IFieldInstance}s:
	 * <ul>
	 * <li>path {@code /G/G2/F3} and repetitions {@code [1, 1, 1]}</li>
	 * <li>path {@code /G/G2/F3} and repetitions {@code [2, 1, 1]}</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @param path
	 * 		the path of the {@link IElement} the {@link IEntityInstance} is referring to. May
	 * 		not be {@code null} or blank. Further explanations can be found in
	 *        {@link IEntityInstance#getPath()}.
	 * @param repetitions
	 * 		the specific repetitions of the {@link IEntityInstance}. Further explanations
	 * 		can be found in {@link IEntityInstance#getRepetitions()}.<br>
	 * 		May be {@code null}. If {@code null} the repetition of every hierarchy level is
	 * 		interpreted as {@link IEntityInstance#ALL_SEMANTIC}.<br>
	 * 		If the array has less entries than the path elements (split by
	 *        {@link IEntityInstance#PATH_SEPARATOR}),
	 * 		for the missing path elements {@link IEntityInstance#ALL_SEMANTIC} will be used.
	 * @return all {@link IEntityInstance}s found with the given path and the given repetitions. If
	 * 		no entity instances were found, the set is empty.
	 *
	 * @throws IllegalArgumentException
	 * 		if the given array with the repetitions has more entries than the number of path
	 * 		parts or if the given path is blank.
	 * @deprecated instead of this method, use {@code DocumentV2#group}/{@code DocumentV2#field} when there is a concrete
	 * 		document pointer
	 * 		(without wildcards), or use {@code DocumentV2Utils#getGroupInstances}/{@code DocumentV2Utils#getFieldInstances} if the
	 * 		repetitions may contain wildcards.
	 */
	@Deprecated(since = "30.0.0")
	Set<IEntityInstance> get(String path, int[] repetitions);

	/**
	 * Searches for a single {@link IFieldInstance}. An example can be found in {@link #get(String, int[])}.
	 *
	 * @param path
	 * 		the path of the {@link IField} the {@link IFieldInstance} is referring to. May not be {@code null} or blank. Further
	 * 		explanations can be found in {@link IEntityInstance#getPath()}.
	 * @param repetitions
	 * 		the specific repetitions of the {@link IFieldInstance}. Further explanations can be found in
	 *        {@link IEntityInstance#getRepetitions()}.<br>May be {@code null}. If {@code null} the repetition of every hierarchy level
	 * 		is interpreted as {@link IEntityInstance#ALL_SEMANTIC}.
	 * @return the corresponding {@link IFieldInstance} or {@link Optional#empty()} if no {@link IFieldInstance} found with the
	 * 		given parameters.
	 *
	 * @throws IllegalArgumentException
	 * 		if the given array with the repetitions has more entries than the number of path parts or if the given path is blank.
	 * @throws IllegalStateException
	 * 		if more than one {@link IFieldInstance} were found or if an {@link IGroupInstance} was found with the given parameters.
	 * @deprecated either use {@code DocumentV2#field} when there is a concrete document pointer (without wildcards), or use
	 *        {@code DocumentV2Utils#getFieldInstances} instead.
	 */
	@Deprecated(since = "30.0.0")
	Optional<IFieldInstance> getFieldInstance(String path, int[] repetitions);

	/**
	 * Searches for a single {@link IGroupInstance}. An example can be found in {@link #get(String, int[])}.
	 *
	 * @param path
	 * 		the path of the {@link IGroup} the {@link IGroupInstance} is referring to. May not be {@code null} or blank. Further
	 * 		explanations can be found in {@link IEntityInstance#getPath()}.
	 * @param repetitions
	 * 		the specific repetitions of the {@link IGroupInstance}. Further explanations can be found in
	 *        {@link IEntityInstance#getRepetitions()}.<br>May be {@code null}. If {@code null} the repetition of every hierarchy level
	 * 		is interpreted as {@link IEntityInstance#ALL_SEMANTIC}.
	 * @return the corresponding {@link IGroupInstance} or {@link Optional#empty()} if no {@link IGroupInstance} found with the
	 * 		given parameters.
	 *
	 * @throws IllegalArgumentException
	 * 		if the given array with the repetitions has more entries than the number of path parts or if the given path is blank.
	 * @throws IllegalStateException
	 * 		if more than one {@link IGroupInstance} were found or if an {@link IFieldInstance} was found with the given parameters.
	 * @deprecated either use {@code DocumentV2#group} when there is a concrete document pointer (without wildcards), or use
	 *        {@code DocumentV2Utils#getGroupInstances} instead.
	 */
	@Deprecated(since = "30.0.0")
	Optional<IGroupInstance> getGroupInstance(String path, int[] repetitions);

	/**
	 * Searches for all {@link IEntityInstance}s which are descendants of the
	 * {@link IGroupInstance}s
	 * specified by the given parameters.<br>
	 * In contrast to {@link #getChildren(String, int[])}, which only returns the direct children,
	 * here all descendants are returned.
	 * <p>
	 * <b>Example:</b><br>
	 * Taking the structure and assumption in {@link IDocumentSearchService}
	 * <ul>
	 * <li><code>getDescendants("/G", new int[]{1})</code> {@literal ->} set of following
	 * {@link IEntityInstance}s:
	 * <ul>
	 * <li>{@link IGroupInstance} with path {@code /G/G2} and repetitions {@code [1, 1]}</li>
	 * <li>{@link IFieldInstance} with path {@code /G/G2/F3} and repetitions {@code [1, 1, 1]}</li>
	 * </ul>
	 * </li>
	 * <li><code>getDescendants("/G", new int[]{0})</code> {@literal ->} set of following
	 * {@link IEntityInstance}s:
	 * <ul>
	 * <li>{@link IGroupInstance} with path {@code /G/G2} and repetitions {@code [1, 1]}</li>
	 * <li>{@link IGroupInstance} with path {@code /G/G2} and repetitions {@code [2, 1]}</li>
	 * <li>{@link IFieldInstance} with path {@code /G/G2/F3} and repetitions {@code [1, 1, 1]}</li>
	 * <li>{@link IFieldInstance} with path {@code /G/G2/F3} and repetitions {@code [2, 1, 1]}</li>
	 * </ul>
	 * </li>
	 * <li><code>getDescendants("/G/G2", new int[]{1})</code> {@literal ->} set with a single
	 * {@link IFieldInstance} with path {@code /G/G2/F3} and repetitions {@code [1, 1, 1]}</li>
	 * <li><code>getDescendants("/G/G2", null)</code> {@literal ->} set with following
	 * {@link IFieldInstance}s:
	 * <ul>
	 * <li>path {@code /G/G2/F3} and repetitions {@code [1, 1, 1]}</li>
	 * <li>path {@code /G/G2/F3} and repetitions {@code [2, 1, 1]}</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @param groupPath
	 * 		path of the group whose descendants should be searched. In case the search should
	 * 		be done at the document root,
	 *        {@value IEntityInstance#PATH_SEPARATOR} can
	 * 		be used as path. May not be {@code null} or blank.
	 * @param repetitions
	 * 		the specific repetitions of the group. Further explanations
	 * 		can be found in {@link IGroupInstance#getRepetitions()}.<br>
	 * 		May be {@code null}. If {@code null} the repetition of every hierarchy level is
	 * 		interpreted as {@link IEntityInstance#ALL_SEMANTIC}.<br>
	 * 		If the array has less entries than the path elements (split by
	 *        {@link IEntityInstance#PATH_SEPARATOR}),
	 * 		for the missing path elements {@link IEntityInstance#ALL_SEMANTIC} will be used.
	 * @return a set with all {@link IEntityInstance}s which descend from the given group path for
	 * 		the given repetitions. If no {@link IEntityInstance}s were found, the set is empty.
	 *
	 * @throws IllegalArgumentException
	 * 		if the given array with the repetitions has more entries than the number of path
	 * 		parts or if the given group path is blank.
	 * @deprecated <ul>
	 * 						<li>get a single specific group instance with all descendants (<i>no wildcards in repetition</i>) => use {@code DocumentV2#group}</li>
	 * 						<li>get a list of all repetitions of a group (<i>with wildcards in repetitions</i>) => use {@code DocumentV2Utils#getGroupInstances}</li>
	 * 						</ul>
	 * 						if all group/field instances should be traversed then use {@code DocumentV2#traverse} on the sub group obtained by one of the above methods.
	 */
	@Deprecated(since = "30.0.0")
	Set<IEntityInstance> getDescendants(String groupPath, int[] repetitions);

	/**
	 * Searches for {@link IEntityInstance}s which are direct descendants (i.e. children) of the
	 * {@link IGroupInstance}s
	 * specified by the given parameters.<br>
	 * In contrast to {@link #getDescendants(String, int[])} this method only returns the direct
	 * children.
	 * <p>
	 * <b>Example:</b><br>
	 * Taking the structure and assumption in {@link IDocumentSearchService}
	 * <ul>
	 * <li><code>getChildren("/G", new int[]{1})</code> {@literal ->} set with a single
	 * {@link IGroupInstance}
	 * with path {@code /G/G2} and repetitions {@code [1, 1]}</li>
	 * <li><code>getChildren("/G", new int[]{0})</code> {@literal ->} set with following
	 * {@link IGroupInstance}s:
	 * <ul>
	 * <li>path {@code /G/G2} and repetition {@code [1, 1]}</li>
	 * <li>path {@code /G/G2} and repetition {@code [2, 1]}</li>
	 * </ul>
	 * </li>
	 * <li><code>getChildren("/G/G2", new int[]{1, 1})</code> {@literal ->} set with a single
	 * {@link IFieldInstance}
	 * with path {@code /G/G2/F3} und repetitions {@code [1, 1, 1]}</li>
	 * <li><code>getChildren("/G/G2", null)</code> {@literal ->} set with following
	 * {@link IFieldInstance}s:
	 * <ul>
	 * <li>path {@code /G/G2/F3} and repetition {@code [1, 1, 1]}
	 * <li>path {@code /G/G2/F3} and repetition {@code [2, 1, 1]}</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @param groupPath
	 * 		path of the group whose children should be searched. In case the search should
	 * 		be done at the document root,
	 *        {@value IEntityInstance#PATH_SEPARATOR} can
	 * 		be used as path. May not be {@code null} or blank.
	 * @param repetitions
	 * 		the specific repetitions of the group. Further explanations
	 * 		can be found in {@link IGroupInstance#getRepetitions()}.<br>
	 * 		May be {@code null}. If {@code null} the repetition of every hierarchy level is
	 * 		interpreted as {@link IEntityInstance#ALL_SEMANTIC}.<br>
	 * 		If the array has less entries than the path elements (split by
	 *        {@link IEntityInstance#PATH_SEPARATOR}),
	 * 		for the missing path elements {@link IEntityInstance#ALL_SEMANTIC} will be used.
	 * @return a set of all {@link IEntityInstance}s directly contained in the given group path
	 * 		with the given repetitions. If no {@link IEntityInstance}s were found, the set is
	 * 		empty.
	 *
	 * @throws IllegalArgumentException
	 * 		if the given array with the repetitions has more entries than the number of path
	 * 		parts or if the given group path is blank.
	 * @deprecated use {@code GroupInstanceV2#directSubgroups} or {@code GroupInstanceV2#directFields} instead.
	 */
	@Deprecated(since = "30.0.0")
	Set<IEntityInstance> getChildren(String groupPath, int[] repetitions);

}
