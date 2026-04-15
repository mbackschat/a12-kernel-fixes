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
package com.mgmtp.a12.kernel.md.facade.cli;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelReferenceResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.internal.service.ExternalDocumentModelService;
import com.mgmtp.a12.kernel.md.model.internal.service.utils.FilePathIncludedDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelReferenceResolverWrapper;
import com.mgmtp.a12.kernel.md.model.internal.wrapper.DocumentModelWrapper;
import com.mgmtp.a12.kernel.md.serializer.model.internal.service.DocumentModelSerializerImpl;

/**
 * Batch generator for document model expansion. The model references related to the referenced (transitive) includes and/or imports are removed from the expanded document model.
 */
public class BatchDocumentModelExpander {
	private static final List<String> PURPOSES_TO_FILTER = List.of("include", "transitiveInclude", "typeDefinitions");

	public static void main(final String[] args) {
		// For test reasons, we do only System.exit in main.
		// We test only #expandModelsWithArgs, so don't modify this method.
		System.exit(expandModelsWithArgs(args));
	}

	private static void usage(String message, Exception e) {
		String errorMsg = (StringUtils.isEmpty(message) ? "" : message + System.lineSeparator()) +
				"Usage: " + BatchDocumentModelExpander.class.getSimpleName() + " <workspacePath> {<infile> <outfile>}+" + System.lineSeparator()
				+ "<workspacePath>\t: path to the directory in wich the included/imported document models can be found." + System.lineSeparator()
				+ "<infile>\t: path to the corresponding document model which will be expanded." + System.lineSeparator()
				+ "<outfile>\t: path to the file to which the expanded document model will be written.";
		System.err.println(errorMsg);
		if (e != null) {
			e.printStackTrace();
		}
	}

	public static int expandModelsWithArgs(final String[] args) {
		if (args.length == 0 || args.length == 1) {
			usage(null, null);
			return -1;
		}
		if ((args.length - 1) % 2 != 0) {
			usage("For each infile, an outfile must be given! The first parameter is the workspace directory.", null);
			return -1;
		}
		Path workspacePath = Paths.get(args[0]);
		if (!workspacePath.toFile().exists() || !workspacePath.toFile().isDirectory()) {
			usage("The path to the workspace does not exist or does not point to a directory. Path: " + workspacePath, null);
			return -1;
		}
		final IDocumentModelReferenceResolver resolver = new DocumentModelReferenceResolverWrapper(
				new FilePathIncludedDocumentModelResolver(workspacePath));
		for (int i = 1; i < args.length; i += 2) {
			try {
				if (expandModel(resolver, Paths.get(args[i]), Paths.get(args[i + 1])) != 0) {
					return -1;
				}
			} catch (Exception e) {
				usage("The model could not be expanded: " + args[i], e);
				return -1;
			}
		}
		return 0;
	}

	private static int expandModel(
			final IDocumentModelReferenceResolver resolver,
			final Path inFile,
			final Path outFile) {
		if (!inFile.toFile().exists()) {
			usage("The path to the document model does not point to a file. Path: " + inFile.toString(), null);
			return -1;
		}
		final IDocumentModelSerializer serializer = new DocumentModelSerializerImpl();
		final Path absolutePath = inFile.toAbsolutePath();
		IDocumentModel documentModel = null;
		try (Reader reader = Files.newBufferedReader(absolutePath, StandardCharsets.UTF_8)) {
			documentModel = serializer.deserialize(reader);
		} catch (IOException e) {
			usage("The model could not be read from " + absolutePath, e);
			return -1;
		}

		new ExternalDocumentModelService().expand(documentModel, resolver);
		// remove unnecessary information
		DocumentModel intDocModel = ((DocumentModelWrapper) documentModel).getDocumentModel();
		intDocModel.getHeader().getModelReferences()
				.removeIf(modelReference -> PURPOSES_TO_FILTER.contains(modelReference.getPurpose()));
		intDocModel.getContent().getTypeDefinitions().forEach(td -> td.getAllModelReferencePaths().clear());

		final Path outAbsolutePath = outFile.toAbsolutePath();
		try {
			Files.createDirectories(outAbsolutePath.getParent());
		} catch (IOException e) {
			usage("The path " + outAbsolutePath.getParent() + " to the output file could not be created", e);
			return -1;
		}
		try (Writer writer = Files.newBufferedWriter(outAbsolutePath, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.CREATE)) {
			serializer.serialize(documentModel, writer, problem -> {
			});
		} catch (IOException e) {
			usage("The file could not be written to " + outFile.toAbsolutePath(), e);
			return -1;
		}
		System.out.printf("Successfully expanded %s to %s.%n", absolutePath, outFile.toAbsolutePath());
		return 0;
	}

}
