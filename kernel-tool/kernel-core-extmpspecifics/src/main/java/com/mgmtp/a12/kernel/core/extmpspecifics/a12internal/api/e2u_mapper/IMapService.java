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

package com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper;

import java.util.List;
import java.util.Map;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Service for checking one or more mapping informations that map entities in an A12 model to
 * entities in an H model.
 */
@OnlyForUsage
public interface IMapService {

	/**
	 * Check if a single mapping info is correct.
	 *
	 * @param fullPath
	 * 		full path of the A12 entity, must not be <code>null</code>
	 * @param mapString
	 * 		mapping string that specifies to which old world entities the A12 entity is
	 * 		mapped, must not be <code>null</code>
	 * @param pr
	 * 		the {@link IProblemReporter}, which will accept the resulting errors. Must not be
	 * 		<code>null</code>.
	 * @return object representation of the mapping information, see {@link IMapInfo} or
	 * 		<code>null</code>, if problems occurred
	 */
	public IMapInfo parseMapString(String fullPath, String mapString, IProblemReporter pr);

	/**
	 * Check if several mapping infos are consistent to each other and that for every field mapping
	 * <ul>
	 * <li>there is a root group mapping for the corresponding root group</li>
	 * <li>every repeatable group on the path of the mapped root group is mapped to a repeatability: USB (if allowed), LfdNr or Split</li>
	 * <li>every repeatable group on the path of the mapped field, but below the group in which the root group mapping is defined,
	 * is mapped to a repeatability: mzi, field list or field list with mzi</li>
	 * </ul>
	 * Additionally, it is checked that the Vorsatzdaten fields specify a valid max. length (the
	 * Vorsatzdaten fields are mapped generically and must not be mapped explicitly by a mapping
	 * info). It is also checked that no mapped field (including Vorsatzdaten fields) has an
	 * application restriction that makes this field not valid for the "mapper-only" application.
	 *
	 * @param mapInfos
	 * 		mapping infos that were created with
	 *        {@link #parseMapString(String, String, IProblemReporter)}, the list and its
	 * 		entries must not be <code>null</code>
	 * @param pr
	 * 		the {@link IProblemReporter}, which will accept the resulting errors. Must not be
	 * 		<code>null</code>.
	 */
	public void checkMappedEntities(List<IMapInfo> mapInfos, IProblemReporter pr);

	/**
	 * Checks if the given mapping information is consistent (see
	 * {@link #checkMappedEntities(List, IProblemReporter)}}) and, additionally, store the
	 * information about the type of the mapping.
	 *
	 * @param mapInfos
	 * 		mapping infos that were created with
	 *        {@link #parseMapString(String, String, IProblemReporter)}, the list and its
	 * 		entries must not be <code>null</code>
	 * @param pr
	 * 		the {@link IProblemReporter}, which will accept the resulting errors. Must not be
	 * 		<code>null</code>.
	 */
	public void checkMappedEntitiesAndStoreMapTypes(List<IMapInfo> mapInfos, IProblemReporter pr);

	/**
	 * Checks if the given mapping information is consistent (see
	 * {@link #checkMappedEntities(List, IProblemReporter)}}) and, additionally, determine the
	 * completely and correctly mapped entities:
	 * <ol>
	 * <li>A field is completely mapped if it is correctly mapped (or marked with ‚keineAbbildung‘)
	 * and
	 * there is no error w.r.t. the mapping of a group on the path of the field. Especially all
	 * repetition layers of the field have to be mapped by a root group or field repetition
	 * mapping.</li>
	 * <li>A root group / group is correctly mapped if all sub-entities are completely mapped.
	 * Thereby a
	 * repeatable group / root group is also completely mapped, if all its fields are marked with
	 * ‚keineAbbildung‘.</li>
	 * <li>If a root group / group is completely mapped, all its sub-groups and fields are also
	 * returned
	 * as completely mapped. This is also the case if the root group is marked with ‚keine
	 * Abbildung‘.</li>
	 * </ol>
	 *
	 * @param mapInfos
	 * 		mapping infos that were created with
	 *        {@link #parseMapString(String, String, IProblemReporter)}, the list
	 * 		and its entries must not be <code>null</code>
	 * @param pr
	 * 		the {@link IProblemReporter}, which will accept the resulting
	 * 		errors. Must not be <code>null</code>.
	 * @param completedEntities
	 * 		the correctly and completely mapped entities are added to this list.
	 * 		The list must not be <code>null</code>.
	 */
	public void checkMappedEntitiesAndDetermineCompletedEntities(List<IMapInfo> mapInfos, IProblemReporter pr,
			List<String> completedEntities);

	/**
	 * Check if several mapping infos are consistent to each other except that the consistency of
	 * field mappings to each other is not checked. Additionally, in contrast to
	 * {@link #checkMappedEntities(List, IProblemReporter)}} it is NOT checked that for every field
	 * mapping there is a root group mapping for the corresponding root group and for every
	 * repeatable group on the path of the field there is a field repeatability mapping. Also it is
	 * not checked if all repetition levels were mapped.
	 * The method can be used for the import, where mapping infos do not have to be complete if the
	 * missing mapping info is already in the database.
	 * If there is no error reported for a set of mapping infos using
	 * {@link #checkMappedEntities(List, IProblemReporter)}}, then there is also no error reported
	 * for any subset of the mapping infos using this method.
	 *
	 * @param mapInfos
	 * 		mapping infos that were created with
	 *        {@link #parseMapString(String, String, IProblemReporter)}, the list and its
	 * 		entries must not be <code>null</code>
	 * @param pr
	 * 		the {@link IProblemReporter}, which will accept the resulting errors. Must not be
	 * 		<code>null</code>.
	 */
	public void checkMappedEntitiesForImport(List<IMapInfo> mapInfos, IProblemReporter pr);

	/**
	 * Check if the mapping information is complete. The mapping info is complete if all payload
	 * fields are mapped. It is not required that all root groups or groups are mapped.<br>
	 *
	 * <b>Precondition:</b> the mapping infos have to be checked for consistency (with one of the
	 * methods
	 * {@link #checkMappedEntities(List, IProblemReporter)},
	 * {@link #checkMappedEntitiesForImport(List, IProblemReporter)} or
	 * {@link #checkMappedEntitiesAndStoreMapTypes(List, IProblemReporter)}) before invoking this
	 * method.
	 *
	 * @param mapInfos
	 * 		mapping infos that were created with
	 *        {@link #parseMapString(String, String, IProblemReporter)}, the list and its
	 * 		entries must not be <code>null</code>
	 * @param pr
	 * 		the {@link IProblemReporter}, which will accept the resulting errors. Must not be
	 * 		<code>null</code>.
	 */
	public void checkMappingComplete(List<IMapInfo> mapInfos, IProblemReporter pr);

	/**
	 * Check if the A12 model is mappable to an H model, taking already created root group mapping
	 * infos into account. It is checked that
	 * <ol>
	 * <li>All payload fields have field types that can be mapped to an allowed field type in an H
	 * model.</li>
	 * <li>There are not too many repetition layers.</li>
	 * <li>The repeatability of the repetition layers is not too high.</li>
	 * <li>The idea for the final check is to first find all groups of the A12 model that
	 * potentially can be mapped to a root group. These groups we call <b>candidates</b> for root
	 * group mapping.<br>
	 * In a second step it is checked for every field which has to be mapped if it is contained in a
	 * candidate group.
	 * </li>
	 * </ol>
	 *
	 * @param alreadyCreatedMapInfos
	 * 		mapping infos that were created with
	 *        {@link #parseMapString(String, String, IProblemReporter)}, the list and its
	 * 		entries must not be <code>null</code>
	 * @param pr
	 * 		the {@link IProblemReporter}, which will accept the resulting errors. Must not be
	 * 		<code>null</code>.
	 */
	public void checkMappability(List<IMapInfo> alreadyCreatedMapInfos, IProblemReporter pr);

	/**
	 * For all root groups mapping infos are created as described in
	 * {@link #getAutomaticCreatedMapInfos(String, Map)}.
	 *
	 * @param mapInfos
	 * 		map containing all mapping infos for the model (with full name of the entity as
	 * 		key and map string as value)
	 * @return
	 */
	public List<IMapInfo> getAutomaticCreatedMapInfos(Map<String, String> mapInfos);

	/**
	 * Get automatically created mapping infos for 1:1 field mappings. For fields that fulfill the
	 * naming convention <code>E&lt;field identifier&gt;</code>, where <code>field identifier</code>
	 * is a digit sequence of length 7, 1:1 mapping infos (i.e. a mapping with the full name of the
	 * field as source and the field identifier as mapping string) are created, if the following
	 * conditions are fulfilled:
	 * <ol>
	 * <li>the field is contained in the group {@code groupFullName},</li>
	 * <li>there is no {@link INoMap} mapping info for the root group of the group
	 * {@code groupFullName}
	 * in {@code mapInfos},</li>
	 * <li>there is not already a mapping info info for the field in {@code mapInfos},</li>
	 * <li>the field identifier is not forbidden (some field identifiers are reserved for additional
	 * data which are generically filled in the mapper, which is why an explicit mapping info may
	 * not be defined for these identifiers)</li>
	 * <li>the field type is allowed in an uXML model or can be mapped to an allowed string type in
	 * the uXML model.</li>
	 * </ol>
	 * <p>
	 * For fields that are mapped automatically, field repetability mapping infos are added if there
	 * is a root group mapping and there is at most one repeatable group between the group mapped to
	 * a root group and the field (this repeatability is then mapped to the MZI), which has a
	 * repeatability of at most 99 and this repeatable group is not already mapped in another
	 * mapping info.
	 * <br>
	 * If there is no group {@code groupFullName}, an empty list is returned.
	 * <br>
	 * <b>Note:</b> If mapping infos are returned, it is NOT guaranteed that they are consistent.
	 *
	 * @param groupFullName
	 * 		full name of a group
	 * @param mapInfos
	 * 		map containing all mapping infos for entities contained in the group
	 *        {@code groupFullName} (with full name of the entity as key and map string as
	 * 		value)
	 * @return a list with the automatically created mapping infos for fields contained in the group
	 *        {@code groupFullName} if the above conditions are fulfilled; otherwise an empty list
	 */
	public List<IMapInfo> getAutomaticCreatedMapInfos(String groupFullName, Map<String, String> mapInfos);

	/**
	 * see the javadoc of the {@code MapService} implementation class (extmp) for details.
	 */
	public Map<String, List<IEntity>> convertMapInfosToEntities(List<IMapInfo> mapInfos, IProblemReporter pr);

}
