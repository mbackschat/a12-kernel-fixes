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
package com.mgmtp.a12.kernel.md.utils;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;

/**
 * <b>ATTENTION:</b><br/>
 * The project is responsible for the quality of the custom code. It is strongly
 * recommended to use this option only in exceptional situations and in close consultation with
 * A12 Kernel-Team, as in-depth knowledge of the DocumentModel is required. In case the migration is
 * customized, it is recommended to check the consistency of the DocumentModel. For this purpose,
 * the corresponding function of the kernel version that fits to the given {@code targetVersion} can
 * be used (see
 * {@link IDocumentModelService#checkConsistency(com.mgmtp.a12.kernel.md.model.api.IDocumentModel,
 * java.util.function.Consumer)}).
 */
@Documented
@Target({ TYPE, METHOD, PARAMETER, FIELD, ANNOTATION_TYPE })
@Retention(SOURCE)
public @interface UseOnlyAfterCloseConsultation {

}
