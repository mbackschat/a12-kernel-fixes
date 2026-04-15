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

 
import type {
	DocumentModel,
	DocumentModelSearchService,
	DocumentModelSerializer,
	DocumentRtService,
	DocumentService,
	GeneratedCodeRtConfig,
	IGeneratedCodeAccessor
} from "./api.js";
import { DocumentModelSearchServiceImpl } from "./internal/DocumentModelSearchServiceImpl.js";
import { DocumentModelSerializerImpl } from "./internal/DocumentModelSerializerImpl.js";
import { DocumentServiceImpl } from "./internal/DocumentServiceImpl.js";
import { GeneratedScriptCodeAccessor } from "./internal/GeneratedScriptCodeAccessor.js";
import { DocumentRtServiceImpl } from "./internal/dm-runtime-service/DocumentRtServiceImpl.js";

/**
 * The one and only entry point for all m&amp;d-services.
 *
 * Currently, a {@link DocumentModelSerializer} and a {@link DocumentService} can be retrieved.
 */
export class DocumentServiceFactory {
	getDocumentModelSerializer(): DocumentModelSerializer {
		return new DocumentModelSerializerImpl();
	}

	getDocumentModelSearchService(documentModel: DocumentModel): DocumentModelSearchService {
		return new DocumentModelSearchServiceImpl(documentModel);
	}

	getDocumentService(): DocumentService {
		return new DocumentServiceImpl();
	}
}

/**
 * Provides a method to create a {@link IGeneratedCodeAccessor} for a script, which was previously generated.
 */
export class GeneratedCodeAccessorFactory {
	createScriptAccessor(script: string): IGeneratedCodeAccessor {
		return new GeneratedScriptCodeAccessor(script);
	}
}

// ################ MD API ################

/**
 * Entry point to use the MD API.
 */
export class DocumentRtServiceFactory {
	/**
	 * {@link GeneratedCodeAccessorFactory.createScriptAccessor} provides an instance of a Kernel implementation of 
	 * {@link IGeneratedCodeAccessor}.
	 * 
	 * <b>Note:</b> Be aware that the Kernel implementation of {@link IGeneratedCodeAccessor} uses internally `new Function`
	 * which could be detected as a potential security issue.
	 * 
	 * @param genCodeAccessor The access to generated code matching the document that needs to be validated or used for computations.
	 *   See method description for more information.
	 * @param config The optional config to control some aspects of runtime functionalities.
	 * @returns An instance of {@link DocumentRtService}.
	 */
	static createDocumentRtService(genCodeAccessor: IGeneratedCodeAccessor, config?: GeneratedCodeRtConfig): DocumentRtService {
		return new DocumentRtServiceImpl(genCodeAccessor, config);
	}
}
