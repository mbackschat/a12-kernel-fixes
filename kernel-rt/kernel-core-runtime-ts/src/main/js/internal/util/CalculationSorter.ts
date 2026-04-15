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
import type { DirectedGraph, TopologicalSortResult } from "./TopologicalSortHelper.js";
import { TopologicalSortHelper } from "./TopologicalSortHelper.js";

export type CalculationDependencyGraph = DirectedGraph<string>;

export class CalculationSorter {
    private constructor() {
        // utility class should not be instantiated
    }

    private static combineDependencyGraphs(
        calculatedFieldDependencies: CalculationDependencyGraph,
        operationDependencies: CalculationDependencyGraph,
    ): CalculationDependencyGraph {
        const combinedDependencyGraph: CalculationDependencyGraph = structuredClone(calculatedFieldDependencies);

        for (const [dependent, dependencies] of operationDependencies) {
            combinedDependencyGraph.set(dependent, new Set(dependencies));
        }

        return combinedDependencyGraph;
    }

    public static sortCalculations(
        calculatedFieldDependencies: CalculationDependencyGraph,
        operationDependencies: CalculationDependencyGraph,
    ): string[] {
        const combinedDependencyGraph = this.combineDependencyGraphs(calculatedFieldDependencies, operationDependencies);

        const result = TopologicalSortHelper.topologicalSort(combinedDependencyGraph);
        if (!result.succeeded) {
            throw new Error(`The external calculations and Document Model calculations form a cycle through the following fields: ${this._createCycleString(result)}`);
        } else {
            return result.result;
        }
    }

    private static _createCycleString(result: TopologicalSortResult<string>): string {
        const cycle = result.result;
        return [...cycle, cycle[0]].map(fieldPath => `'${fieldPath}'`).join(" - ");
    }
}