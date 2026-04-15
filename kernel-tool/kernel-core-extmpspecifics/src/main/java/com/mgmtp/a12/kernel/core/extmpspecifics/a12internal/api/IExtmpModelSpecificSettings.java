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
package com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api;

import java.util.Locale;

import com.mgmtp.a12.kernel.core.base.utils.internal.BaseUtilConstants;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;

/**
 * Config-Class only for ExtMP in order to handle all the custom things needed by our customer
 * No other user has to provide an implementation for this class.
 */
public interface IExtmpModelSpecificSettings extends IModelSpecificSettings {

	/**
	 * ExtMP only:
	 *
	 * @return true, if H (historic) model, false otherwise
	 */
	boolean isHModel();

	/**
	 * ExtMP only:
	 *
	 * @return true, if the model is "hostbasiert"
	 */
	boolean isHostbasiert();

	/**
	 * ExtMP only:
	 *
	 * @return true, if field-, group- and rootgroup names should comply with the Fg-SOA naming
	 * 		convention
	 */
	boolean isFgSoaNamingConvention();

	/**
	 * ExtMP only:
	 *
	 * @return true, if the fk check (Feldkennungsprüfung) should be done
	 */
	boolean isWithFkCheck();

	/**
	 * ExtMP only:
	 *
	 * @param isFirstProdVersionInExml
	 * 		default is {@code false} (for more details see
	 *        {@link IExtmpModelSpecificSettings#isFirstProdVersionInExml()}).
	 */
	void setIsFirstProdVersionInExml(boolean isFirstProdVersionInExml);

	/**
	 * ExtMP only:
	 *
	 * @return {@code true} if the version was changed to eXML and it is the first version which
	 * 		goes live, otherwise {@code false}.
	 */
	boolean isFirstProdVersionInExml();

	/**
	 * @return <code>true</code> if an uXML model has been assigned to the model. Otherwise
	 * 		<code>false</code>.
	 */
	boolean hasAssignedUXMLModel();

	/**
	 * s. {@link #isFirstProdVersionA12DV()}
	 */
	void setIsFirstProdVersionA12DV();

	/**
	 * @return <code>true</code> if the previous model version was an H-DV, the current version is
	 * 		an A12-DV, without uXML mapping and without assignments for data transfer. It is
	 * 		during packet code generation. If <code>true</code>, data transfer from a previous
	 * 		version has a special logic, s. EXT-19105
	 */
	boolean isFirstProdVersionA12DV();

	@Override
	default String getTimeZone() {
		return BaseUtilConstants.TIME_ZONE_BERLIN;
	}

	/**
	 * ExtMP only:
	 * This is currently only used for testing ExtMP models in kernel.
	 *
	 * @return {@code true} (the default) if the code generator should use ExtMP specific parts in template files, otherwise
	 *        {@code false}.
	 */
	default boolean useExtmpTemplates() {
		return true;
	}

	/**
	 * @return the language used in the rule condition text (comments are not affected) and also for the reserved terms when
	 * 		defining parameters in the rule error message, e.g. ".Wert".
	 */
	default Locale getLanguageUsedInConditionText() {
		return Locale.GERMANY;
	}

	/**
	 * @return true, if referencing fields in rule conditions is also allowed via the short name. Default value is {@code true}.
	 */
	default boolean isFieldRefByShortNameAllowed() {
		return true;
	}

	/**
	 * ExtMP only:
	 *
	 * @param reReDv
	 * 		default is {@code false} (for more details see
	 *        {@link IExtmpModelSpecificSettings#isReReDv()}).
	 */
	void setReReDv(boolean reReDv);

	/**
	 * ExtMP only:
	 *
	 * @return true, if the model can handle references to source documents, i.e. e/uXML reader/writer can read or write the
	 * 		attributes for the references to source documents, respectively, and these attributes are generated in the eXML schema.
	 */
	default boolean isReReDv() {
		return false;
	}
}
