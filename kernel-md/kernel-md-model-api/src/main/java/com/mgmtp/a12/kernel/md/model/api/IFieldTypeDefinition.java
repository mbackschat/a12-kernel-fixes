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
package com.mgmtp.a12.kernel.md.model.api;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.model.utils.OnlyForUsage;
import com.mgmtp.a12.model.header.ModelReference;

import lombok.NonNull;

/**
 * Data type definition for fields which can be referenced by several field definitions.
 */
@OnlyForUsage
public interface IFieldTypeDefinition extends IIdNamed, IDescribed, IAnnotated, Serializable {

	/**
	 * @return the defined field data type (see {@link IFieldType}). May not be {@code null}.
	 */
	@NonNull IFieldType getFieldType();

	/**
	 * A {@link ModelReference} path is a list of {@link ModelReference}s showing the way to the document model that actually
	 * specifies this instance. For example, lets assume that this instance is defined in DM3, DM1 imports DM2 and DM2 imports DM3.
	 * In this case, the list will contain entries for "DM2" and "DM3". There will be no entry in the list for the
	 * current document model "DM1". The entries are ordered according to the order of includes/imports, i.e. the last
	 * {@link ModelReference} entry in the list contains the reference to the document model that actually specifies this
	 * type definition.
	 * <p>
	 * A type definition can be included/imported through different paths. In the example from above, lets assume DM4 imports DM3
	 * and DM1 imports (in addition to DM2) also DM4. This means, the collection contains following {@link ModelReference} paths:
	 * <ul>
	 *     <li>DM2/DM3</li>
	 *     <li>DM4/DM3</li>
	 * </ul>
	 *
	 * @return all the {@link ModelReference} paths (see also method description) through which this type definition is imported or
	 * 		included in the corresponding document model. This means, if the type definition is defined directly in the document
	 * 		model, the collection is empty. Never {@code null}. The returned {@link Set} does not contain any empty list.
	 */
	@NonNull Set<List<ModelReference>> getAllModelReferencePaths();

}
