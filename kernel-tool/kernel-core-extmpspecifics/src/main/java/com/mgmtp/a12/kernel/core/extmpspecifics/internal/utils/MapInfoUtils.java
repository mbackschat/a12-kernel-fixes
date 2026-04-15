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

package com.mgmtp.a12.kernel.core.extmpspecifics.internal.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper.IFieldMapping;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper.IMapInfo;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper.IMapService;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper.IRootGroupMapping;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper.IRootgroupRepeatabilityMapping;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper.IRootgroupRepeatabilityMapping.MappingType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;

/**
 * Util methods for checking mapping types that are used in a mapping information.<br>
 * <b>Precondition:</b> all methods except {@link #isSplit(IFieldMapping)} do only work if the
 * mapping types are set in the mapping information (this is the case if
 * {@link IMapService#checkMappedEntitiesAndStoreMapTypes(List, IProblemReporter)}
 * was called for the map infos, which is done for code generation).
 */
public class MapInfoUtils {

	private MapInfoUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * <b>Precondition:</b> The hierarchy level for rootgroup repetition has to be set in the map
	 * info (this is usually only the case for code generation).
	 *
	 * @param rootGroupMapInfo
	 * 		root group map information
	 * @return <code>true</code> if the root group repetition is mapped, otherwise
	 * 		<code>false</code>.
	 */
	public static boolean isMappingToRootGroupRepetition(IRootGroupMapping rootGroupMapInfo) {
		return rootGroupMapInfo.getHierarchyLevel4RootgroupRepetition() != IMapInfo.NOT_APPLICABLE;
	}

	/**
	 * <b>Precondition:</b> The hierarchy level for USB mapping has to be set in the map info (this
	 * is usually only the case for code generation).
	 *
	 * @param rootGroupMapInfo
	 * 		root group map information
	 * @return <code>true</code> if the USB is mapped, otherwise
	 * 		<code>false</code>.
	 */
	public static boolean isMappingToUsb(IRootGroupMapping rootGroupMapInfo) {
		return rootGroupMapInfo.getHierarchyLevel4Usb() != IMapInfo.NOT_APPLICABLE;
	}

	/**
	 * @param mapInfo
	 * 		map information
	 * @return <code>true</code> if in the mapping the root group is split into two, otherwise
	 * 		<code>false</code>.
	 */
	public static boolean isSplit(IMapInfo mapInfo) {
		if (mapInfo instanceof IFieldMapping fMapInfo) {
			return isSplit(fMapInfo);
		}
		if (mapInfo instanceof IRootgroupRepeatabilityMapping rgRepMapInfo) {
			return rgRepMapInfo.getType() == MappingType.SPLIT;
		}
		if (mapInfo instanceof IRootGroupMapping rgMapInfo) {
			return isSplit(rgMapInfo);
		}
		// no map
		return false;
	}

	/**
	 * <b>Precondition:</b> The hierarchy level for splitting has to be set in the map info (this is
	 * usually only the case for code generation).
	 *
	 * @param rootGroupMapInfo
	 * 		root group map information
	 * @return <code>true</code> if in the mapping the root group is split into two, otherwise
	 * 		<code>false</code>.
	 */
	public static boolean isSplit(IRootGroupMapping rootGroupMapInfo) {
		return !StringUtils.isEmpty(rootGroupMapInfo.getRootGroupName2());
	}

	/**
	 * @param fieldMapInfo
	 * 		field map information
	 * @return <code>true</code> if in the mapping information the root group is split into two,
	 * 		otherwise <code>false</code>.
	 */
	public static boolean isSplit(IFieldMapping fieldMapInfo) {
		return !StringUtils.isEmpty(fieldMapInfo.getFieldIdentifiers().get(0).getRight());
	}

	/**
	 * <b>Precondition:</b> The hierarchy level for MZI has to be set in the map info (this is
	 * usually only the case for code generation).
	 *
	 * @param fieldMapInfo
	 * 		field map information
	 * @return <code>true</code> if the field repetition (mzi) is mapped, otherwise
	 * 		<code>false</code>.
	 */
	public static boolean isMappingToMzi(IFieldMapping fieldMapInfo) {
		return fieldMapInfo.getHierarchyLevel4Mzi() != IMapInfo.NOT_APPLICABLE;
	}

	/**
	 * <b>Precondition:</b> The hierarchy level for the field list has to be set in the map info
	 * (this is usually only the case for code generation).
	 *
	 * @param fieldMapInfo
	 * 		field map information
	 * @return <code>true</code> if a field list is mapped, otherwise <code>false</code>.
	 */
	public static boolean isMappingToFieldList(IFieldMapping fieldMapInfo) {
		return fieldMapInfo.getHierarchyLevel4FieldList() != IMapInfo.NOT_APPLICABLE;
	}

	/**
	 * @param fieldMapping
	 * 		field map information
	 * @return all different field identifiers to which the field is mapped in the map info (i.e.
	 * 		all field identifiers of a field list and/or each of the two identifiers in a
	 * 		splitting).
	 */
	public static List<String> getAllFieldIdentifiers(IFieldMapping fieldMapping) {
		return fieldMapping.getFieldIdentifiers().stream()
				.flatMap(fiPair -> Arrays.asList(fiPair.getLeft(), fiPair.getRight()).stream())
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public static boolean isRootGroupRepeatability(IMapInfo mapInfo) {
		if (mapInfo instanceof IRootgroupRepeatabilityMapping) {
			return true;
		}
		if (!(mapInfo instanceof IRootGroupMapping)) {
			return false;
		}
		IRootGroupMapping rootGroupMapping = (IRootGroupMapping) mapInfo;
		return isMappingToRootGroupRepeatability(rootGroupMapping);
	}

	/**
	 * Ideally, this method should be called after having called {@link #isRootGroupRepeatability(IMapInfo)}
	 */
	public static MappingType getRootgroupRepeatabilityMappingType(IMapInfo mapInfo) {
		if (mapInfo instanceof IRootgroupRepeatabilityMapping rgRepMapInfo) {
			return rgRepMapInfo.getType();
		}
		if (mapInfo instanceof IRootGroupMapping rgMapInfo && isMappingToRootGroupRepeatability(rgMapInfo)) {
			return rgMapInfo.getRootgroupRepeatabilityMappingType().get();
		}
		return null;
	}

	/**
	 * @return <code>true</code> if the lowest level of the mapped entity in {@code rgMapInfo} is mapped to a root group repeatability.
	 */
	public static boolean isMappingToRootGroupRepeatability(IRootGroupMapping rgMapInfo) {
		return rgMapInfo.getRootgroupRepeatabilityMappingType().isPresent();
	}

	/**
	 * @param rgMapInfo
	 * @return <code>true</code> if the lowest level of the mapped entity in {@code rgMapInfo} is
	 * 		technically mapped to the USB. In this case no index field is needed and the
	 * 		repetition number (not an index field value) is mapped to the USB.
	 */
	public static boolean isMappingToTechnicalUsb(IRootGroupMapping rgMapInfo) {
		return rgMapInfo.getRootgroupRepeatabilityMappingType().map(type -> type == MappingType.TECH_USB).orElse(false);
	}
}
