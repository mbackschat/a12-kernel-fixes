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
/*
 * Older history for classes in this package can be found in commit
 * 06911b7f23c1c9899a2988f2aef9ac504076fca1
 * in GIT-Repository 'kernel-ext'.
 *
 */
/**
 * This package contains several implementations of {@link com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator} that can be used in projects.
 * <br>
 * If you want to use a {@link com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator} you have to implement
 * a {@link com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory} that provides a corresponding instance and
 * either provide it at the runtime APIs (e.g. {@link com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig} or
 * register it statically. The mechanisms are described in more detail in the Kernel documentation for developers.
 * <p>
 * <b>Attention:</b> Some {@link com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldValidator}s need to know the IDs
 * of supported German federal tax offices. Those IDs are provided by {@link com.mgmtp.a12.kernel.core.customfieldtype.impl.IFadat}
 * to their instances. These are the following classes:
 *     <ul>
 * 			<li>{@link com.mgmtp.a12.kernel.core.customfieldtype.impl.SteuernummerVerifier}</li>
 * 			<li>{@link com.mgmtp.a12.kernel.core.customfieldtype.impl.BundessteuernummerVerifier}</li>
 * 			<li>{@link com.mgmtp.a12.kernel.core.customfieldtype.impl.BundesfinanzamtsnummerVerifier}</li>
 * 			<li>{@link com.mgmtp.a12.kernel.core.customfieldtype.impl.EWAzVerifier}</li>
 * 			<li>{@link com.mgmtp.a12.kernel.core.customfieldtype.impl.LaenderkuerzelVerifier}</li>
 *     </ul>
 */
package com.mgmtp.a12.kernel.core.customfieldtype.impl;
