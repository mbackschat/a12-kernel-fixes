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

package com.mgmtp.a12.kernel.core.rt._30_8.internal.datatransfer.mappingmodel;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.SourceData;
import com.mgmtp.a12.kernel.core.rt.a12internal.datatransfer.SourceIdentifier;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IData;
import com.mgmtp.a12.kernel.core.rt.a12internal.validation.IIdentifier;

/**
 * An IPrecompModelDataMapper takes care of mapping data from the individual sources (or target) into the precomputation dm and
 * back.
 * It encapsulates the handling of the shape of the precomputation model.
 * It does not deal with filtering or sorting of the SourceData, or with the overall data mapping from source to target.
 * <p>
 * Instances may internally hold state from the (initial) call to {@link #mapDataIntoPrecompDM(IData, List, Locale)}
 * for use in later calls to {@link #mapIdentifierBack(IIdentifier)}.
 */
public interface IPrecompModelDataMapper {
	record MapToPrecompResult(IData dataForPrecomp, Set<String> includedSourceIds, Set<String> excludedExcessiveSourceIds) {
	}

	/**
	 * Transforms data that is valid for transfer
	 * so that the data's repetitions & paths are suitable for the precomputation dm.<br>
	 * {@link #mapIdentifierBack(IIdentifier)} can afterward be used to invert this mapping, in order to find the origin of an
	 * identifier from the mapped IData.
	 * <p>
	 * Should only be called once on any given instance, as this will set the internal state for subsequent
	 * calls to {@link #mapIdentifierBack(IIdentifier)}.
	 */
	MapToPrecompResult mapDataIntoPrecompDM(IData targetData, List<SourceData> sourceData, Locale localeForErrors);

	/**
	 * Essentially the inverse of {@link #mapDataIntoPrecompDM(IData, List, Locale)} (but for individual identifiers rather than a
	 * complete
	 * IData.
	 * <p>
	 * Must not be called before a call to {@link #mapDataIntoPrecompDM(IData, List, Locale)} has happened, as it relies on the
	 * state
	 * (including externally-specified sourceIds) that gets set by that method.
	 */
	SourceIdentifier mapIdentifierBack(IIdentifier identifierInPrecomputationDM);

	/**
	 * serialize this instance to a String that can be deserialized again via {@link #deserialize(String)}.
	 *
	 * @implNote Implementations must add enough information to the serialized representation to allow
	 *        {@link #deserialize(String)}
	 * 		to dispatch to the suitable implementation's custom deserialization. {@link #deserialize(String)} will need to be adapted
	 * 		if alternative implementations are introduced.
	 */
	String serialize();

	/**
	 * @param classLoader
	 * 		must be passed by the caller. This is because for projAlpha, it is important that the generat-specific
	 * 		classloader is used, and via this parameter the generated code can pass its class-specific classloader
	 */
	// we put this method here, so that the generated code doesn't need to know about the specific implementation class
	static IPrecompModelDataMapper loadFromClasspath(String resourcePath, ClassLoader classLoader) {
		try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
			if (inputStream == null) {
				throw new RuntimeException("cannot find resource " + resourcePath);
			}
			return deserialize(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	// we put this method here, so that the generated code doesn't need to know about the specific implementation class
	static IPrecompModelDataMapper deserialize(String serialized) {
		// currently, FixedShapePrecompModelDataMapper is the only impl so we don't need to check
		// which impl to delegate to
		return FixedShapePrecompModelDataMapper.deserialize(serialized);
	}
}
