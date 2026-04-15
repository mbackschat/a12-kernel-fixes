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
package com.mgmtp.a12.kernel.core.extmpspecifics.internal.api;

import java.util.List;
import java.util.Map;

import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper.IFieldMapping;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper.IMapInfo;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.e2u_mapper.IRootGroupMapping;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.xml.IXMLZusatzfeld;
import com.mgmtp.a12.kernel.core.extmpspecifics.a12internal.api.xml.XMLSchemaNode;
import com.mgmtp.a12.kernel.core.extmpspecifics.internal.vba.VBAFieldGroupData;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IECWithModelProperties;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;

import lombok.NonNull;

/**
 * The code gen parameters used for ExtMP-specific extensions to the code generation process.
 */
public interface IExtmpCodeGenParams extends ICodeGeneratorParameters {

	enum XML_RW_Type {
		NONE,
		SXML,
		UXML,
		EXML,
		EXML_MAPPED
	}

	/**
	 * The data used for a mapped model. Used for data mapping from eXML in uXML.
	 */
	public interface IMappedData {
		/**
		 * The code generation parameters used for the assigned model.
		 */
		public ICodeGeneratorParameters getCodeGenParams();

		/**
		 * The {@link IECWithModelProperties} of the assigned model.
		 */
		public IECWithModelProperties getEc();

		/**
		 * The mapping information.
		 */
		public List<IMapInfo> getMappingInfos();

		/**
		 * @return the field mapping information for the given eXML field.
		 */
		public IFieldMapping getFieldMappingInfo(String eXMLFullName);

		/**
		 * @return the corresponding root group information for the given eXML field.
		 */
		public IRootGroupMapping getRootGroupMappingInfo(String eXMLFullName);

	}

	/**
	 * Interface to access the eXML schema data.
	 */
	public interface IEXMLSchemaData {

		/**
		 * @return boolean, indicating if the root group is the eXML root element.
		 */
		public boolean isRootGroupLevelAsRoot();

		/**
		 * @return the custom name used for the root element, if present; otherwise an empty string
		 */
		public String getRootElementCustomName();

	}

	/**
	 * @return the eXML schema data.
	 */
	public IEXMLSchemaData getEXMLSchemaData();

	/**
	 * Spezialparameter für VBA-Validierung. Eine Map von Modulname auf eine Liste von
	 * VBA-Feldgruppen die jeweils eine Liste von Feldern und die Wiederholbarkeit enthalten.
	 */
	public Map<String, List<VBAFieldGroupData>> getVBAModuleGroups();

	/**
	 * ExtMP only:
	 *
	 * @return true, if the model is "Feldkennungsbasiert"
	 */
	public boolean isFeldkennungsbasiert();

	/**
	 * Return the version (or base year) string of the model.
	 */
	public String getModelVersion();

	/**
	 * Return the name of the model.
	 */
	public String getModelName();

	/**
	 * Return the name of the application, e.g. "vk".
	 */
	public String getApplicationName();

	/**
	 * @return which kind of XML reader / writer to generate.
	 */
	XML_RW_Type getXmlType();

	/**
	 * For uXML mapping:
	 * <p>
	 * Usually identical to {@link #getName()}. In uXML mapping, the package name will be built a bit
	 * different for the assigned uXML model.
	 * <p>
	 * The default implementation returns {@link #getName()}.
	 *
	 * @return the package name to use.
	 */
	public default String getAssignedPackageName() {
		return getName();
	}

	/**
	 * For XML-Reader and XML-Writer.
	 *
	 * @return All XML schema nodes defined for this request.
	 */
	public List<XMLSchemaNode> getXmlSchemaNodes();

	/**
	 * Returns a list of additional XML fields.
	 */
	public List<IXMLZusatzfeld> getXmlZusatzfelder();

	/**
	 * Mapper only:
	 *
	 * @return the code gen parameters of the assigned model (for packet code generation with an
	 * 		assigned uXML model), otherwise null
	 */
	public IMappedData getMappedModelParameter();

	@NonNull ExtmpPackageInformation getExtmpPackageInformation();

	record ExtmpPackageInformation(
			@NonNull String legacyPublicPackageName,
			@NonNull String publicUXmlMapperPackageName,
			@NonNull String publicUXmlMapperClassNamePrefix,
			@NonNull String internalXmlReaderSubPackageName,
			@NonNull String internalXmlWriterSubPackageName,
			@NonNull String internalSXmlSubPackageName,
			@NonNull String internalUXmlMapperSubPackageName
	) {
	}
}
