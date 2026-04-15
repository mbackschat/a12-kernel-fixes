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

import java.util.Optional;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.utils.conversion.a12internal.BasicTypeV1V2Conversion;

import lombok.NonNull;

/**
 * An implementation of this interface can be obtained via
 * {@code new com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory(null).createDocumentV1V2Converter()}
 *
 * @deprecated Use methods that expect {@link DocumentV2} instead of {@link IDocument}.
 */
@OnlyForUsage
@Deprecated(since = "30.0.0")
public interface IDocumentV1V2Converter {
	IDocument toDocumentV1(@NonNull DocumentV2 base);

	DocumentV2 ofDocumentV1(@NonNull IDocument document);

	Optional<DocumentPointer> pointerFromEntityInstance(@NonNull IEntityInstance entityInstance);

	Optional<DocumentMultiPointer> multiPointerFromEntityInstance(@NonNull IEntityInstance entityInstance);

	IEntityInstance entityInstanceFromPointer(@NonNull DocumentPointer pointer);

	IEntityInstance entityInstanceFromMultiPointer(@NonNull DocumentMultiPointer pointer);

	/**
	 * @deprecated use {@link PartiallyKnownDocumentMultiPointer#of(IEntityInstance)} instead
	 */
	@Deprecated(since = "30.0.0")
	default PartiallyKnownDocumentMultiPointer causeEntityInstanceFromEntityInstance(@NonNull IEntityInstance entityInstance) {
		return PartiallyKnownDocumentMultiPointer.of(entityInstance);
	}

	default Object fieldValueV1ToV2(Object value) {
		return BasicTypeV1V2Conversion.basicTypeV1ToV2(value);
	}

	default Object fieldValueV2ToV1(Object value) {
		return BasicTypeV1V2Conversion.basicTypeV2ToV1(value);
	}
}
