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

import Fs from "fs";
import Path from "path";

import { Transform } from "jscodeshift/src/core";
import { applyTransform } from "jscodeshift/src/testUtils";

export function runTest(module: Transform, modulePath: string) {
	const fileNames = Fs.readdirSync(Path.join(modulePath, "__tests__", "__fixtures__"), "utf-8");
	for (const fileName of fileNames) {
		const rawSource = Fs.readFileSync(
			Path.join(modulePath, "__tests__", "__fixtures__", fileName),
			"utf-8"
		);
		const { source, description, expectation } = readSource(rawSource);

		describe(description || fileName, () => {
			it(expectation || "should work", () => {
				const output = applyTransform(module, {}, { source }, { parser: "tsx" });
				expect(output).toMatchSnapshot(`\nFixture: ${fileName}\n---\n${source}\n---\n`);
			});
		});
	}
}

function isComment(line: string): boolean {
	return (
		line.startsWith("//") ||
		line.startsWith("/*") ||
		line.trimStart().startsWith("*") ||
		line.trimStart().startsWith("*/")
	);
}

function readSource(rawSource: string): {
	source: string;
	description: string;
	expectation: string;
} {
	const lines = rawSource.split("\n");

	const description =
		"com.mgmtp.a12.kernel.kernel-codemod." +
		lines
			.filter(line => line.startsWith("// description: "))
			.map(line => line.slice(16))
			.join(" ")
			.trim();
	const expectation = lines
		.filter(line => line.startsWith("// expectation: "))
		.map(line => line.slice(16))
		.join(" ")
		.trim();
	const source = lines.filter(line => !isComment(line)).join("\n");
	return { source, description, expectation };
}
