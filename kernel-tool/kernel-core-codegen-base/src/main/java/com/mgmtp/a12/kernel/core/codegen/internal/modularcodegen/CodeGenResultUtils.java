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

package com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;

public class CodeGenResultUtils {
	public static final String GROOVY_CONTENT_KEY = "$content$";
	public static final String GROOVY_IMPORT_KEY = "import";
	public static final String GROOVY_GLOBALS_KEY = "globals";
	public static final String GROOVY_FILE_INFO_KEY = "generatedFileInfo";

	private static final Set<String> GRADLE_JAVA_LAYOUT_ROOT_FILES = Set.of(
			"build.gradle",
			"settings.gradle");

	private CodeGenResultUtils() {
		throw new RuntimeException("static utility class");
	}

	/**
	 * Utility for checking at test time (only if assertions are enabled)
	 * that an ICodeGenResult is a ISeparateFilesCodeGenResult and the contained sources
	 * are all below an expected path prefix (i.e. below a certain subdirectory in the resulting zip).
	 * It may be useful to test this kind of invariant, because it simplifies
	 * the use of the tested code generator in a modular way when it gets called
	 * by other code generators: if the sources stay below the prefix, collisions
	 * can be more easily ruled out.
	 * <p/>
	 * This method should have negligible effect when assertions are not enabled, i.e. in production.
	 *
	 * @param packagePrefix
	 * 		the path or package prefix, as a list of path parts
	 * @param exceptions
	 * 		a predicate that when it returns true for a "/"-delimited path in the result,
	 * 		excempts this path from the check.
	 */
	public static void assertIsSeparateFileCodeGenResultAndAllEntryPathsStartWithPrefix(
			ICodeGenResult<?> result,
			// actually expected to be an ISeparateFilesCodeGenResult, but we don't cast outside the assertion
			List<String> packagePrefix,
			Predicate<String> exceptions
	) {
		List<String> badPrefixPaths;
		assert result instanceof ISeparateFilesCodeGenResult<?> : "expected an ISeparateFilesCodeGenResult but got " + result;
		assert (badPrefixPaths = pathsThatDontHavePrefix((ISeparateFilesCodeGenResult<?>) result, packagePrefix,
				exceptions)).isEmpty()
				: "expected the paths of the generated sources to start with " + String.join("/", packagePrefix)
				+ " but found the following offending paths:\n" + String.join("\n", badPrefixPaths);
	}

	/**
	 * Converts an {@link ICodeGenResult} to zip bytes or plain source file bytes, in a way that maintains compatibility
	 * with the old ZipSourceCodeCollector/PlainSourceCodeCollector logic.
	 * <p>
	 * For {@link ICodeGeneratorParameters.ProgrammingLanguage#JAVA} (and non-plain mode), the resulting zip is laid out as a
	 * standalone Gradle
	 * Java project (see {@link CodeGenResultUtils#applyGradleJavaLayout}). For other languages the zip uses a flat,
	 * package-relative layout.
	 * <p>
	 * Zip entry paths are sufficiently unique so that no collisions are expected if the results of gencode from different models
	 * are extracted to the same directory.
	 */
	public static byte[] toBytes(ICodeGenResult<?> genResult, boolean isPlain,
			ICodeGeneratorParameters.ProgrammingLanguage programmingLanguage) {
		if (genResult == null) { // handle cascading nulls here, to provide better convenience for callers
			return null;
		}
		if (isPlain) {
			return genResult.toSingleFileBytesIfApplicableOrThrow();
		}
		if (genResult instanceof ISeparateFilesCodeGenResult<?> sfgr) {
			return programmingLanguage == ICodeGeneratorParameters.ProgrammingLanguage.JAVA
					? toZipWithGradleJavaLayout(sfgr.sources())
					: sourcesToZip(sfgr.sources());
		}
		if (genResult instanceof IMergedSingleScriptCodeGenResult<?> msgr) {
			return sourcesToZip(Map.of(
					String.join("/", msgr.filePathForScript()),
					msgr.toSingleFileBytesIfApplicableOrThrow()));
		}
		throw new IllegalStateException("unexpected ICodeGenResult subtype: " + genResult.getClass());
	}

	public static final Set<String> ALLOWED_FILES_IN_CODEGEN_ROOT = Set.of(
			// ModelBasedCodeGenerator
			"runtime-version.properties",
			// GradleConfigGenerator
			"build.gradle",
			"settings.gradle");

	private static List<String> pathsThatDontHavePrefix(ISeparateFilesCodeGenResult<?> codeGenResult, List<String> packagePrefix,
			Predicate<String> exceptions) {
		// NOTE: paths could also have a leading slash -> maybe we should check against a regex instead
		Pattern pattern = Pattern.compile("^/?" + String.join("/", packagePrefix) + "/.*");
		return codeGenResult.sources().keySet().stream()
				.filter(path -> !exceptions.test(path))
				.filter(path -> !pattern.matcher(path).matches())
				.sorted()
				.toList();
	}

	/**
	 * Relocates entries into the standard Gradle Java project layout:
	 * {@code *.java} &rarr; {@code src/main/java/}, other files (CSV, properties, &hellip;)
	 * &rarr; {@code src/main/resources/}, except {@code build.gradle}/{@code settings.gradle}
	 * which stay at the map root.
	 * <p>
	 * Expects input map keys to be flat, package-relative paths (e.g.
	 * {@code com/foo/Bar.java}, {@code runtime-version.properties}). Passing
	 * already-relocated keys will produce nested paths (e.g.
	 * {@code src/main/java/src/main/java/...}).
	 */
	public static Map<String, byte[]> applyGradleJavaLayout(Map<String, byte[]> sources) {
		return sources.entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(
						e -> toGradleJavaLayoutKey(e.getKey()),
						Map.Entry::getValue));
	}

	/** See {@link #applyGradleJavaLayout}. */
	static byte[] toZipWithGradleJavaLayout(Map<String, byte[]> sources) {
		return sourcesToZip(applyGradleJavaLayout(sources));
	}

	private static String toGradleJavaLayoutKey(String key) {
		if (GRADLE_JAVA_LAYOUT_ROOT_FILES.contains(key)) {
			return key;
		}
		if (key.endsWith(".java")) {
			return "src/main/java/" + key;
		}
		return "src/main/resources/" + key;
	}

	/**
	 * Creates the bytes of a zip file that contains all sources, in normalized order
	 */
	private static byte[] sourcesToZip(Map<String, byte[]> sources) {
		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(131072);
				ZipOutputStream zipStream = new ZipOutputStream(byteStream)
		) {
			for (var entry : sortedSources(sources)) {
				ZipEntry e = new ZipEntry(entry.getKey());
				zipStream.putNextEntry(e);
				zipStream.write(entry.getValue());
			}
			zipStream.closeEntry();
			zipStream.close();
			return byteStream.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Comparator<Map.Entry<String, byte[]>> keyComparator(List<String> preference) {
		return (a, b) -> {
			int idxA = preference.indexOf(a.getKey());
			int idxB = preference.indexOf(b.getKey());
			if (idxA != -1 && idxB != -1) {
				return Integer.compare(idxA, idxB); // both in preference list
			} else if (idxA != -1) {
				return -1; // a in preference, b not
			} else if (idxB != -1) {
				return 1;  // b in preference, a not
			} else {
				return a.getKey().compareTo(b.getKey()); // neither in preference, compare names
			}
		};
	}

	private static List<Map.Entry<String, byte[]>> sortedSources(Map<String, byte[]> sources) {
		return sources.entrySet().stream()
				.sorted(keyComparator(List.of(GROOVY_FILE_INFO_KEY, GROOVY_IMPORT_KEY, GROOVY_GLOBALS_KEY)))
				.toList();
	}

	public static String deduplicateImports(String imports) {
		StringBuilder importSetBuilder = new StringBuilder(System.lineSeparator());
		// every import statement only once
		imports.lines()
				.distinct()
				.forEach(line -> importSetBuilder.append(line + System.lineSeparator()));
		return importSetBuilder.toString();
	}
}
