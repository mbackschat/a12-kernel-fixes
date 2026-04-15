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
package com.mgmtp.a12.kernel.md.model.internal.service.utils.cli;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mgmtp.a12.kernel.core.codegen.internal.generator.CodeGenConstants;

import lombok.NonNull;

/**
 * Provides handling of generator result zips.
 * <b>Note:</b> This class is actually though to be used by a command line interface tool.
 */
public class ArchiveExtractor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveExtractor.class);
	private final boolean logToSysError;

	public ArchiveExtractor(boolean logToSysError) {
		this.logToSysError = logToSysError;
	}

	private void error(String msg, Exception e) {
		if (logToSysError) {
			System.err.println(msg);
			e.printStackTrace(System.err);
		} else {
			LOGGER.error(msg, e);
		}
	}

	private void debug(String msg) {
		if (logToSysError) {
			System.out.println(msg);
		} else {
			LOGGER.debug(msg);
		}
	}

	/**
	 * Write the artifacts ZIP to the filesystem without extracting it (as a single file).
	 */
	public void writeToDisk(byte @NonNull [] artifactsZip, @NonNull Path fileSystemPath, @NonNull String archiveFileName) {
		createNecessaryDirectories(fileSystemPath);

		File archive = fileSystemPath.resolve(archiveFileName).toFile();
		if (archive.exists()) {
			archive.delete();
		}
		debug(String.format("Writing archive of generated code '%s' to '%s' ...", archiveFileName, archive.getAbsolutePath()));
		try (FileOutputStream fos = new FileOutputStream(archive)) {
			fos.write(artifactsZip);
		} catch (IOException e) {
			error(String.format("Error while writing the archive of generated code '%s' to '%s'", archiveFileName,
					archive.getAbsolutePath()), e);
			throw new RuntimeException(e);
		}
		debug("Completed.");
	}

	/**
	 * Write the artifacts inside the passed zip (as byte[]) to the filesystem.
	 *
	 * @param artifactsZip
	 * 		the archive to extract
	 * @param fileSystemPath
	 * 		the path to extract to
	 */
	public void extractToDisk(final byte[] artifactsZip, final Path fileSystemPath, boolean isSingleFileOrJsExt) {
		if (isSingleFileOrJsExt) {
			debug("Writing generated code to '" + fileSystemPath.toAbsolutePath().toString() + "' ...");
			extractSingleFileOrJsExt(artifactsZip, fileSystemPath);
		} else {
			debug("Extracting generated code into '" + fileSystemPath.toAbsolutePath().toString() + "' ...");
			extractMultipleFiles(artifactsZip, fileSystemPath);
		}
		debug("Completed.");
	}

	private void createNecessaryDirectories(final Path fileSystemPath) {
		if (!fileSystemPath.toFile().exists()) {
			try {
				Files.createDirectories(fileSystemPath);
			} catch (final IOException e) {
				error(String.format("Failed to create directories for '%s'.", fileSystemPath.toString()), e);
			}
		}
	}

	private void extractMultipleFiles(final byte[] bytes, final Path fileSystemPath) {
		try (final ZipInputStream zipinputstream = new ZipInputStream(new ByteArrayInputStream(bytes))) {
			ZipEntry zipentry;
			while ((zipentry = zipinputstream.getNextEntry()) != null) {
				final Path filePath = fileSystemPath.resolve(zipentry.getName());
				if (zipentry.isDirectory()) {
					Files.createDirectories(filePath);
					continue;
				} else {
					Files.createDirectories(filePath.toAbsolutePath().getParent());
				}

				final byte[] buffer = new byte[2048];
				try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(filePath), buffer.length)) {
					int size;
					while ((size = zipinputstream.read(buffer)) != -1) {
						bos.write(buffer, 0, size);
					}
					bos.flush();
				}
			}
		} catch (final IOException ioe) {
			error(String.format("Error during extraction of zipped source code to '%s'.", fileSystemPath.toString()), ioe);
		}
	}

	/**
	 * This method is used for following cases:
	 * <ul>
	 * <li>javascript code is generated wrapped in a function and an additional file with the same
	 * name but different extension (.d.ts) is also generated,</li>
	 * <li>a single file is generated, e.g. javascript or groovy</li>
	 * </ul>
	 */
	private void extractSingleFileOrJsExt(final byte[] bytes, final Path fileSystemPath) {
		createNecessaryDirectories(fileSystemPath.toAbsolutePath().getParent());
		try (final ZipInputStream zipinputstream = new ZipInputStream(new ByteArrayInputStream(bytes))) {
			ZipEntry zipentry;
			while ((zipentry = zipinputstream.getNextEntry()) != null) {
				if (zipentry.getName().endsWith(CodeGenConstants.TS_DEF_CODE_SUFFIX)) {
					final Path tsDefPath = createTsDefPath(fileSystemPath);
					Files.copy(zipinputstream, tsDefPath, StandardCopyOption.REPLACE_EXISTING);
				} else {
					// case for single file
					Files.copy(zipinputstream, fileSystemPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		} catch (final IOException ioe) {
			error(String.format("Error during extraction of zipped source code to '%s'.", fileSystemPath.toString()), ioe);
		}
	}

	private Path createTsDefPath(final Path fileSystemPath) {
		final Path parentPath = fileSystemPath.getParent();
		final String fileName = fileSystemPath.getFileName().toString();
		final String fileNameWoExtension = fileName.substring(0, fileName.lastIndexOf("."));
		return parentPath.resolve(fileNameWoExtension + CodeGenConstants.TS_DEF_CODE_SUFFIX);
	}

}