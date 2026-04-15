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

import static com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.CodeGenResultUtils.GROOVY_CONTENT_KEY;
import static com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.CodeGenResultUtils.GROOVY_FILE_INFO_KEY;
import static com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.CodeGenResultUtils.GROOVY_GLOBALS_KEY;
import static com.mgmtp.a12.kernel.core.codegen.internal.modularcodegen.CodeGenResultUtils.GROOVY_IMPORT_KEY;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;

public class SourceCodeCollectorV2 { // NOTE: this class is internal, so we could omit the "V2" suffix,
	// but it might help to prevent confusion with the quite different former ISourceCodeCollector.
	private final Map<String, byte[]> entries = new HashMap<>();
	private final List<IEntryPoints> entryPointsDescriptors = new ArrayList<>();

	public void createEntry(String entryName, byte[] content) {
		if (this.entries.containsKey(entryName)) {
			if (!Arrays.equals(this.entries.get(entryName), content)) {
				throw new IllegalArgumentException("entry for key " + entryName + " already exists");
			}
			return;
		}
		this.entries.put(entryName, content);
	}

	public void addEntryPoints(IEntryPoints entryPointsDescriptor) {
		this.entryPointsDescriptors.add(entryPointsDescriptor);
	}

	public ICodeGenResult<EntryPointCollection> toCodeGenResult(ICodeGeneratorParameters.ProgrammingLanguage programmingLanguage) {
		return switch (programmingLanguage) {
			case GROOVY -> {
				// for groovy (and maybe at some point similarly for js) we need do extract the special representation
				// of generatedFileInfo, imports, globals content body and script file name in the sourceCodeCollector, in order
				// to get an IMergedSingleScriptCodeGenResult (which then allows for straightforward explicit
				// .mergeWith(nestedCodeGenResults), e.g. in mappnig codegen
				String generatedFileInfo = new String(entries.get(GROOVY_FILE_INFO_KEY), StandardCharsets.UTF_8);
				Set<String> importsLines = Arrays.stream(new String(entries.get(GROOVY_IMPORT_KEY)).strip().split("\n"))
						.collect(Collectors.toSet());
				String globals = new String(entries.get(GROOVY_GLOBALS_KEY), StandardCharsets.UTF_8);
				var contentEntries = entries.entrySet().stream()
						.filter(e -> e.getKey().startsWith(GROOVY_CONTENT_KEY))
						.sorted(Comparator.comparing(Map.Entry::getKey)).toList();
				assert contentEntries.size() == 1 : "expected exactly one content entry but got " + contentEntries.stream().map(Map.Entry::getKey).toList();
				List<String> groovyFilePath = Arrays.asList(contentEntries.get(0).getKey().substring(GROOVY_CONTENT_KEY.length(), contentEntries.get(0).getKey().length()).split("/"));

				assert entryPointsDescriptors.size() == 1 : entryPointsDescriptors;
				yield IMergedSingleScriptCodeGenResult.of(
						EntryPointCollection.of(this.entryPointsDescriptors),
						generatedFileInfo,
						importsLines,
						globals,
						contentEntries.stream()
								.map(e -> new String(e.getValue(), StandardCharsets.UTF_8))
								.toList(),
						groovyFilePath
				);
			}
			case JAVA -> ISeparateFilesCodeGenResult.of(EntryPointCollection.of(this.entryPointsDescriptors), entries);
			// All other target langs (vba, cpp, js, ...)
			// NOTE for js: js code does not currently use the GROOVY_FILE_INFO_KEY/GROOVY_IMPORT_KEY/GROOVY_GLOBALS_KEY/GROOVY_CONTENT_KEY
			// structure in the SourceCodeCollector, so we don't/can't convert it to an IMergedSingleScriptCodeGenResult yet.
			// This is ok because modular nested code generations (that need a mergeWith) method do not yet occur for js.
			default -> ISeparateFilesCodeGenResult.of(EntryPointCollection.of(this.entryPointsDescriptors), entries);
		};
	}
}
