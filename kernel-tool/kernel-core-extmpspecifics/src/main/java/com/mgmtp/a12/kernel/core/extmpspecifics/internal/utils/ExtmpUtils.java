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

import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.IExtmpModelSpecificSettings;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.api.IExtmpCodeGenParams.XML_RW_Type;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.CodeProfileType;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.IModelSpecificSettings;

/**
 * Contains different utility methods specific to the extended modeling platform.
 */
public class ExtmpUtils {
	/**
	 * Name of the root group Anhaenge that can be part of an exml nutzdaten element
	 */
	public static final String ROOTGROUP_ANHAENGE_NAME = "Anhaenge";

	/**
	 * @param modelSpecificSettings
	 * 		the settings to be tested.
	 * @return {@code true} if the model can handle references to source documents, otherwise {@code false}.
	 */
	public static boolean isReReDv(IModelSpecificSettings modelSpecificSettings) {
		return (modelSpecificSettings instanceof IExtmpModelSpecificSettings extmpModelSpecificSettings)
				&& extmpModelSpecificSettings.isReReDv();
	}

	/**
	 * @param modelSpecificSettings
	 * 		the settings to be tested.
	 * @return the return value of {@link IExtmpModelSpecificSettings#isHModel()} if the given settings
	 * 		are of type {@link IExtmpModelSpecificSettings}, <code>false</code> otherwise.
	 */
	public static boolean isHModel(IModelSpecificSettings modelSpecificSettings) {
		return (modelSpecificSettings instanceof IExtmpModelSpecificSettings) && ((IExtmpModelSpecificSettings) modelSpecificSettings).isHModel();
	}

	/**
	 * @param modelSpecificSettings
	 * 		the settings to be tested.
	 * @return the return value of {@link IExtmpModelSpecificSettings#isHostbasiert()} if the given settings
	 * 		are of type {@link IExtmpModelSpecificSettings}, <code>false</code> otherwise.
	 */
	public static boolean isHostbasiert(IModelSpecificSettings modelSpecificSettings) {
		if (modelSpecificSettings instanceof IExtmpModelSpecificSettings) {
			return ((IExtmpModelSpecificSettings) modelSpecificSettings).isHostbasiert();
		}
		return false;
	}

	/**
	 * @param modelSpecificSettings
	 * 		the settings to be tested.
	 * @return <code>true</code> if the given settings are of type {@link IExtmpModelSpecificSettings},
	 * 		<code>false</code> otherwise.
	 */
	public static boolean isExtmp(IModelSpecificSettings modelSpecificSettings) {
		return (modelSpecificSettings instanceof IExtmpModelSpecificSettings);
	}

	/**
	 * @param modelSpecificSettings
	 * 		the settings to be tested.
	 * @return {@code true} (the default) if the code generator should use ExtMP specific parts in template files, otherwise
	 *        {@code false}.
	 */
	public static boolean useExtmpTemplates(IModelSpecificSettings modelSpecificSettings) {
		return (modelSpecificSettings instanceof IExtmpModelSpecificSettings) && ((IExtmpModelSpecificSettings) modelSpecificSettings).useExtmpTemplates();
	}

	/**
	 * @param params
	 * 		the code generation parameters to be tested.
	 * @return the package name for the uXML model, if in ExtMP world. Else the
	 * 		name of the model {@link ICodeGeneratorParameters#getName()}.
	 */
	public static String getAssignedPackageName(ICodeGeneratorParameters params) {
		if (!(params instanceof IExtmpCodeGenParams)) {
			return params.getName();
		} else {
			return ((IExtmpCodeGenParams) params).getAssignedPackageName();
		}
	}

	/**
	 * Tests the given generation parameters, if it specifies a code generation from an eXML model
	 * mapping to an uXML model, or if it is the mapped model itself.
	 *
	 * @param params
	 * 		the code generation parameters to be tested
	 * @return <code>true</code> if the code profile is {@link CodeProfileType#CP_MAPPER_ONLY} or
	 *        {@link CodeProfileType#CP_MAPPED}, <code>false</code> otherwise.
	 */
	public static boolean isMapper(ICodeGeneratorParameters params) {
		return isMapper(params.getCodeProfile());
	}

	/**
	 * Tests the given generation parameters, if it specifies a code generation from an eXML model
	 * mapping to an uXML model, or if it is the mapped model itself.
	 *
	 * @param codeProfileType
	 * 		the code profile type to be tested
	 * @return <code>true</code> if the code profile is {@link CodeProfileType#CP_MAPPER_ONLY} or
	 *        {@link CodeProfileType#CP_MAPPED}, <code>false</code> otherwise.
	 */
	public static boolean isMapper(CodeProfileType codeProfileType) {
		return codeProfileType == CodeProfileType.CP_MAPPER_ONLY || codeProfileType == CodeProfileType.CP_MAPPED;
	}

	/**
	 * Tests the given generation parameters, if it specifies a code generation of the mapped model
	 * (uXML).
	 *
	 * @param params
	 * 		the code generation parameters to be tested
	 * @return <code>true</code> if the code profile is {@link CodeProfileType#CP_MAPPED},
	 * 		<code>false</code> otherwise.
	 */
	public static boolean isMapped(ICodeGeneratorParameters params) {
		return params.getCodeProfile() == CodeProfileType.CP_MAPPED;
	}

	/**
	 * @param params
	 * 		the code generation parameters to be tested
	 * @return <code>true</code> if an uXML model has been assigned to the model for which code
	 * 		should be generated. This does not depend on the type of code generation (with packet
	 * 		or without). Otherwise <code>false</code>.
	 */
	public static boolean hasAssignedUXMLModel(IModelSpecificSettings params) {
		if (params instanceof IExtmpModelSpecificSettings) {
			return ((IExtmpModelSpecificSettings) params).hasAssignedUXMLModel();
		} else {
			return false;
		}
	}

	/**
	 * @param params
	 * 		the code generation parameters to be tested
	 * @return <code>true</code> if it is a code generation with packet and the parameter contains
	 * 		an
	 * 		assigned uXML model. Otherwise <code>false</code>.
	 */
	public static boolean isPacketCodeGenerationWithAssignedUXMLModel(ICodeGeneratorParameters params) {
		if (params instanceof IExtmpCodeGenParams) {
			return ((IExtmpCodeGenParams) params).getMappedModelParameter() != null;
		} else {
			return false;
		}
	}

	/**
	 * @param params
	 * 		the code generation parameters to be tested.
	 * @return the package name of the mapped model
	 *        {@link IExtmpCodeGenParams#getMappedModelParameter()}, is in ExtMP world. Else the
	 * 		name of the model {@link ICodeGeneratorParameters#getName()}.
	 */
	public static String getMappedPackageName(ICodeGeneratorParameters params) {
		if (!(params instanceof IExtmpCodeGenParams extmpParams)) {
			return params.getName();
		} else {
			if (extmpParams.getMappedModelParameter() != null) {
				IExtmpCodeGenParams mappedExtmpParams = (IExtmpCodeGenParams) extmpParams.getMappedModelParameter()
						.getCodeGenParams();
				return mappedExtmpParams.getAssignedPackageName();
			} else {
				return extmpParams.getAssignedPackageName();
			}
		}
	}

	/**
	 * Mapped name is the result of {@link ICodeGeneratorParameters#getName()} for the mapped model
	 *
	 * @param params
	 * 		the code generation parameters to be tested.
	 * @return the name used for the mapped model. If it is not ExtMP or there is no mapped model,
	 *        {@link ICodeGeneratorParameters#getName()} will be returned;
	 */
	public static String getMappedName(ICodeGeneratorParameters params) {
		IExtmpCodeGenParams.IMappedData mappedData = getMappedData(params);
		if (mappedData == null) {
			return params.getName();
		} else {
			return mappedData.getCodeGenParams().getName();
		}
	}

	private static IExtmpCodeGenParams.IMappedData getMappedData(ICodeGeneratorParameters params) {
		if (!(params instanceof IExtmpCodeGenParams)) {
			return null;
		} else {
			return ((IExtmpCodeGenParams) params).getMappedModelParameter();
		}
	}

	/**
	 * @param params
	 * 		the code generation parameters to be tested.
	 * @return the version of the model {@link IExtmpCodeGenParams#getModelVersion()}, if in ExtMP
	 * 		world. Else <code>null</code>.
	 */
	public static String getModelVersion(ICodeGeneratorParameters params) {
		if (!(params instanceof IExtmpCodeGenParams)) {
			return null;
		}
		return ((IExtmpCodeGenParams) params).getModelVersion();
	}

	/**
	 * @param params
	 * 		the code generation parameters to be tested.
	 * @return the version of the model {@link IExtmpCodeGenParams#getModelName()}, if in ExtMP
	 * 		world. Else an empty string.
	 */
	public static String getModelName(ICodeGeneratorParameters params) {
		if (!(params instanceof IExtmpCodeGenParams extmpParams)) {
			return "";
		}
		return extmpParams.getModelName();
	}

	/**
	 * @param params
	 * 		the code generation parameters to be tested.
	 * @return a boolean indicating if the root group should be the root element in the eXML.
	 */
	public static boolean isRootGroupLevelAsRoot(ICodeGeneratorParameters params) {
		if (!(params instanceof IExtmpCodeGenParams)) {
			return false;
		} else {
			return ((IExtmpCodeGenParams) params).getEXMLSchemaData().isRootGroupLevelAsRoot();
		}
	}

	/**
	 * @param params
	 * 		the code generation parameters to be tested.
	 * @return the custom name used for the root element in the eXML.
	 */
	public static String getRootElementCustomName(ICodeGeneratorParameters params) {
		if (!(params instanceof IExtmpCodeGenParams)) {
			return "";
		} else {
			return ((IExtmpCodeGenParams) params).getEXMLSchemaData().getRootElementCustomName();
		}
	}

	public static XML_RW_Type getXmlType(ICodeGeneratorParameters params) {
		return params instanceof IExtmpCodeGenParams extmpParams
				? extmpParams.getXmlType()
				: XML_RW_Type.NONE;
	}

	private ExtmpUtils() {
		throw new UnsupportedOperationException("Utility class");
	}
}
