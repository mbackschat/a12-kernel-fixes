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

/**
 * Representations, reader and writer for a simple subset of xml that is sufficient for our A12-document xml serialization,
 * but that does not contain anything that is specific to A12-documents, kernel or A12.
 * (Similar to how jackson's {@link com.fasterxml.jackson.databind.JsonNode} and
 * {@link com.fasterxml.jackson.databind.ObjectMapper} work for <em>json</em>, but with better support for the <em>xml-specific</em>
 * features that are relevant for our use case)
 * </p>
 * Hopefully, this helps to separate the complexities of xml reading and writing from the A12-document specific complexity.
 * In particular, this should allow to reason about the code in this package without the need for contextual knowledge about
 * A12-documents.
 */
package com.mgmtp.a12.kernel.md.serializer.document.internal.service.serialization.xml.simplexmlnode;