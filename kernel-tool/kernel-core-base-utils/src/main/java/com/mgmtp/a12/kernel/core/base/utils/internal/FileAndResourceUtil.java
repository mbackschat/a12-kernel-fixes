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
package com.mgmtp.a12.kernel.core.base.utils.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.NonNull;

public class FileAndResourceUtil {
	private FileAndResourceUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static <T> T readFromFileOrResourceUtf8(@NonNull String fileOrResourcePath, @NonNull ReaderFunction<T> read) {
		if (new File(fileOrResourcePath).exists()) {
			try (Reader reader = new FileReader(fileOrResourcePath, StandardCharsets.UTF_8)) {
				return read.apply(reader);
			} catch (IOException e) {
				throw new RuntimeException("failed to read via file system from path " + fileOrResourcePath, e);
			}
		} else {
			try (Reader reader = new InputStreamReader(
					Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileOrResourcePath)),
					StandardCharsets.UTF_8)) {
				return read.apply(reader);
			} catch (IOException | NullPointerException e) {
				throw new RuntimeException("failed to read via classpath from " + fileOrResourcePath, e);
			}
		}
	}

	@FunctionalInterface
	public interface ReaderFunction<T> {
		T apply(Reader reader) throws IOException;
	}

	/**
	 * Similar to {@link #readFileInDirTree(File, String)}, but passes the reader to the given ReaderFunction and takes care of
	 * closing the reader afterward.
	 */
	public static <T> T readFileInDirTree(@NonNull File directory, @NonNull String fileName, ReaderFunction<T> read) {
		try (Reader r = readFileInDirTree(directory, fileName)) {
			return read.apply(r);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * returns a reader for the file found by {@link #findFileInDirTree}
	 * <br>
	 * @throws LookupException in the cases described for {@link #findFileInDirTree(File, String)} or if creation of a reader for
	 * the matching file fails.
	 * @apiNote  the caller is responsible for closing the reader
	 */
	public static Reader readFileInDirTree(@NonNull File directory, @NonNull String fileName) {
		File matchingFile = findFileInDirTree(directory, fileName);
		try {
			return new BufferedReader(new FileReader(matchingFile));
		} catch (FileNotFoundException e) {
			throw new LookupException("Exception while creating reader for file " + matchingFile,e);
		}
	}

	/**
	 * See {@link #findFileInDirTree(File, Predicate)}, looks for a file named fileName.
	 * @throws LookupException see {@link #findFileInDirTree(File, Predicate)} for details
	 */
	public static File findFileInDirTree(@NonNull File directory, @NonNull String fileName) {
		return findFileInDirTree(directory, file -> file.getName().equals(fileName));
	}

	/**
	 * Recursively searches {@code directory} for a file
	 * satisfying {@code filePredicate} and returns that {@link File}.
	 * If more than one file matches the predicate, the choice is unspecified.
	 *
	 * @throws LookupException if the directory does not exist, cannot be traversed, or no matching file is found
	 */
	public static File findFileInDirTree(@NonNull File directory, @NonNull Predicate<File> filePredicate) {
		if (!directory.exists() || !directory.isDirectory()) {
			throw new LookupException(directory + " does not exist or is not a directory");
		}

		Optional<File> matchingFile;
		try (Stream<Path> paths = Files.walk(directory.toPath())) {
			matchingFile = paths
					.filter(Files::isRegularFile)
					.map(java.nio.file.Path::toFile)
					.filter(filePredicate)
					.findFirst();
		} catch (IOException e) {
			throw new LookupException("Exception while walking directory " + directory, e);
		}

		return matchingFile.orElseThrow(() -> new LookupException("No file matching predicate found in " + directory));
	}

	/**
	 * Finds some resource on the classpath that has the given resource name, if such a resource exists.
	 * If multiple matching resources exist, the particular choice is unspecified.
	 * If the found and chosen resource is a file system directory or file, it will be returned.
	 * Otherwise, an exception is thrown.
	 * <p/>
	 * <em>WARNINGs</em>
	 * <ul>
	 *     <li>If the resource name identifies something that is not a file system directory, an exception may be thrown.</li>
	 *     <li>If more than one such directory exists on the classpath, the choice of the returned directory is <em>unspecified</em></li>
	 *     <li>If more than one directory with matching name exists on the classpath (intentionally or accidentally), then this
	 *     approach can <em>not</em> be used in order to subsequently enumerate all resources with resource name prefix
	 *     {@code resourceName}, since that would only yield the files below the returned matching directory, and not those below
	 *     the not-returned other matching directories, in jars, etc...</li>
	 * </ul>
	 * @throws LookupException if no resource with the given name exists or if the chosen matching resource cannot be converted
	 * to a {@link File} (e.g. because it is not a file or directory)
	 */
	public static File findSomeFileOrDirectoryThatMatchesResourceNameOnClasspath(String resourceName) {
		var url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
		if(url == null) {
			throw new LookupException("no resource found for name " + resourceName);
		}
		URI fileUri;
		try {
			fileUri = url.toURI();
		} catch (URISyntaxException e) { // not sure when this could occur in practice for the url from getResource()
			throw new LookupException("Exception while loading the models-dir from classpath", e);
		}

		File fileOrDir;
		try {
			fileOrDir = new File(fileUri);
		} catch (IllegalArgumentException e) {
			throw new LookupException("The resource found for " + resourceName + " does not appear to be a file or directory", e);
		}
		return fileOrDir;
	}

	public static class LookupException extends RuntimeException {
		public LookupException(String message) {
			super(message);
		}
		public LookupException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
