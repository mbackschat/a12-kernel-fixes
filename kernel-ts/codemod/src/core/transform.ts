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

import { API, FileInfo } from "jscodeshift";

import { EntityTransformSpec } from "./move-entities/entity-transform-spec";

const EngineLibEntryPoints = {
	RT_API: "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts",
	MD_FACADE: "@com.mgmtp.a12.kernel/kernel-md-facade"
} as const;

let migrations: EntityTransformSpec[] = [];

export function setSpec(spec: EntityTransformSpec[]) {
	migrations = expandSpec(spec);
}

export default function transform(file: FileInfo, { jscodeshift: csapi }: API) {
	const root = csapi(file.source);

	for (const migration of migrations) {
		EntityTransformSpec.transform(csapi, root, migration);
	}

	return root.toSource();
}

function expandSpec(spec: EntityTransformSpec[]): EntityTransformSpec[] {
	migrations = [];
	for (const migration of spec) {
		let prop: keyof typeof migration;
		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		const migrationObject = migration as any;
		for (prop in migration) {
			if (prop.toLowerCase().endsWith("path")) {
				migrationObject[prop] = transformPath(migration[prop] as string);
			}
		}
		migrations.push(migrationObject as EntityTransformSpec);
	}

	return migrations;
}

function transformPath(shortPath: string): string {
	const repo = shortPath.split("/")[0] as keyof typeof EngineLibEntryPoints;
	if (Object.keys(EngineLibEntryPoints).includes(repo)) {
		return EngineLibEntryPoints[repo] + shortPath.slice(repo.length);
	}
	return shortPath;
}
