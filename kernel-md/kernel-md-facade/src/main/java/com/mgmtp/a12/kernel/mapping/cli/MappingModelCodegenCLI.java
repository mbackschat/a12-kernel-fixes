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
package com.mgmtp.a12.kernel.mapping.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.kernel.core.base.utils.internal.FileAndResourceUtil;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.serviceparams.ICodeGeneratorParameters;
import com.mgmtp.a12.kernel.md.combination.a12internal.DirectoryBasedUnexpandedModelResolver;
import com.mgmtp.a12.kernel.md.combination.a12internal.ExpandingDmResolver;
import com.mgmtp.a12.kernel.md.datatransfer.a12internal.mappingmodel.MappingModelService;
import com.mgmtp.a12.kernel.md.model.api.services.IValidationCodeGeneratorConfig;
import com.mgmtp.a12.kernel.md.model.api.services.a12internal.ICodeGeneratorConfigV1_2;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.StructuralMappingMetaModelService;
import com.mgmtp.a12.kernel.mmtypings.mm_mappingmodel_2.views.MM_MappingModel_2;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import com.mgmtp.a12.model.notification.RankedNotification;
import com.mgmtp.a12.model.notification.Severity;
import com.mgmtp.a12.model.utils.OnlyForUsage;

@OnlyForUsage
public class MappingModelCodegenCLI {
	public static void main(String[] args) {
		Options helpOnlyOptions = new Options();
		helpOnlyOptions.addOption("h", "help", false, "Display help information");

		Options options = new Options();
		options.addOption(Option.builder("d")
				.longOpt("models-dir")
				.hasArg().required()
				.desc("The directory that contains the mapping model and its referenced models. " +
						"Models will be expected in files named <model-id>.json in the models-dir or its descendant directories. " +
						"Document Models may contain unexpanded includes, if the referenced models are present too, " +
						"Combination Models are also allowed in place of Document Models. The automatic expansion of unexpanded " +
						"Document Models/Combination Models may not work, or only partially work, for the precomputation fragment."
				).get());
		options.addOption(Option.builder("m")
				.longOpt("mapping-model-id")
				.hasArg().required()
				.desc("The id/name of the mapping model (without trailing \".json\").").get());
		options.addOption(Option.builder("o")
				.longOpt("output-dir")
				.hasArg().required(false)
				.desc("The path of the output directory into which the generated sources zip will be stored. " +
						"Optional; default: present working directory").get());
		options.addOption(Option.builder("p")
				.longOpt("package-prefix")
				.hasArg().required(false)
				.desc("the package prefix in format \"com.example.my.package.prefix.\". " +
						"Defaults to \"" + ICodeGeneratorParameters.DEFAULT_PACKAGE_PATH + "\"." +
						"It is strongly recommended to specify a unique, mapping-model specific package prefix in order to avoid " +
						"name collisions of the generated code with other generated mapping code. For temporary experimentation, " +
						"it may be ok to omit this parameter.").get());

		CommandLineParser parser = new DefaultParser();
		try { // first try to check if --help was passed and not complain about mandatory options in that case
			CommandLine helpCmd = parser.parse(helpOnlyOptions, args);
			if (helpCmd.hasOption("help")) {
				printCLIHelpAnd(options);
				System.exit(0);
			}
		} catch (ParseException e) {
			// do nothing
		}

		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("unrecognized command line arguments: " + e.getMessage());
			printCLIHelpAnd(options);
			System.exit(1);
		}

		String mmId = cmd.getOptionValue("mapping-model-id");
		Path modelsDir = Path.of(cmd.getOptionValue("models-dir"));
		Path outputDir = cmd.getOptionValue("output-dir") != null ? Path.of(cmd.getOptionValue("output-dir")) : Paths.get("");
		String packagePrefixNullable = cmd.getOptionValue("package-prefix");
		if (StringUtils.isEmpty(packagePrefixNullable)) {
			System.err.println("WARNING: --package-prefix parameter not specified. The generated mapping classes are very likely " +
					"to have name collisions with other generated mapping code because they use the fallback default package " +
					"prefix. Please pass a unique, mapping-model specific package prefix to the mapping codegen CLI.");
		}

		var expandingDirectoryBasedResolver = ExpandingDmResolver.of(
				DirectoryBasedUnexpandedModelResolver.ofDir(modelsDir.toAbsolutePath().toString()),
				Locale.US, p -> {
					if (messageShouldBeIgnoredDuringExpansion(p)) {
						return;
					}
					System.err.println(p);
					if (p.getSeverity().isAtLeastAsSevereAs(Severity.ERROR)) {
						System.err.println("ERROR: Abort due to error during dm resolve or expand");
						System.exit(1);
					}
				});
		var service = MappingModelService.create(expandingDirectoryBasedResolver);
		MM_MappingModel_2 mappingModel = FileAndResourceUtil.readFileInDirTree(
				modelsDir.toFile(), mmId + ".json",
				reader -> MappingModelService.deserialize(reader, problem -> {
					throw new RuntimeException("unexpected problem while deserializing the mapping model: " + problem);
				}));
		Function<String, MM_StructuralMappingModel_1> smmResolver = smmId -> FileAndResourceUtil.readFileInDirTree(
				modelsDir.toFile(), smmId + ".json",
				reader -> StructuralMappingMetaModelService.deserialize(reader, problem -> {
					throw new RuntimeException("unexpected problem while deserializing the mapping model: " + problem);
				}));
		MappingModelService.GeneratedSources code = service.generateMappingCode(
				mappingModel,
				ICodeGeneratorConfigV1_2.of(packagePrefixNullable,
						IValidationCodeGeneratorConfig.ProgrammingLanguage.JAVA),
				smmResolver,
				notification -> {
					System.err.printf("%s: %s%n", notification.getSeverity().name(), notification.getMessage());
					if (notification.getSeverity().isAtLeastAsSevereAs(Severity.ERROR)) {
						throw new RuntimeException("unexpected problem or warning during code generation: " + notification);
					}
				});

		try {
			var _i = outputDir.toFile().mkdirs();
			Path outputFile = outputDir.resolve(mmId + ".zip");
			Files.write(outputFile, code.toZip());
			System.out.println(outputFile.toAbsolutePath()); // print the resulting path to stdout - e.g. for use in scripts
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean messageShouldBeIgnoredDuringExpansion(RankedNotification p) {
		// (for precomputation fragments), we expect that some references may not be locally resolvable, so we don't want to abort
		// during expansion. Codegen should later check that the models are valid.
		// Alternative approaches that were discarded for now:
		// - a) change expansion and dm-join in general not only report unresolvable references as _warning_. This approach was
		//      discarded because it seems not entirely clear if this is always desired, and we are already quite close to the release.
		// - b) extend notifications and iproblem to have an enum- or constant- based error code that could be used as a more robust
		//      alternative to string matching. This would be nice, but require extensive code changes that are out of scope at the
		//      moment. Also, the automatic test 'test_GeneratorCli' should be able to detect when the string-based matching breaks.
		return p.getMessage()
				.contains("Cannot successfully expand model because the computed field could not be resolved (unambiguously)")
				|| p.getMessage()
				.contains("Cannot successfully expand model because the type definition could not be resolved (unambiguously)");
	}

	private static void printCLIHelpAnd(Options options) {
		HelpFormatter formatter = HelpFormatter.builder().setShowSince(false).get();
		try {
			formatter.printHelp("java -jar kernel-md-facade-*-MappingCodeGenerator-CLI.jar OPTIONS",
					null,
					options, null, false);
		} catch (IOException e) {
			throw new RuntimeException(e); // if printing the help (after wrong cli invocation) fails, there's probably no
			// reasonable way to recover -> a checked exception seems pointless
		}
	}
}
