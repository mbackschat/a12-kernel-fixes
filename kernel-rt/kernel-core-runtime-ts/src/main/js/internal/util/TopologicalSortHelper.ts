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
import { SortedSet } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

export type DirectedGraph<T> = Map<T, Set<T> | null>; // reads: key must be sorted after all in value

/**
 * Stores the result of a topological sort operation ({@link TopologicalSortHelper#topologicalSort}).
 */
export interface TopologicalSortResult<T> {
    /**
     * <code>false</code>, if the graph contained a cycle, <code>true</code> otherwise.
     */
    readonly succeeded: boolean;

    /**
     * If <code>succeeded</code> is
     *   - <code>true</code>, result contains a valid topological sorting of the graph
     *   - </code>false</code>, result contains one cyclic path in the graph (e.g. ["A", "B", "C"] if "A" depends on "B", "B" depends on "C", and "C" depends on "A")
     */
    readonly result: T[];
}

export class TopologicalSortHelper {
    private constructor() {
        // utility class should not be instantiated
    }

    private static invertedGraph<T>(graph: DirectedGraph<T>): DirectedGraph<T> {
        const invertedGraph = new Map();
        for (const [dependent, dependencies] of graph) {
            dependencies?.forEach((dependency) => {
                const dependents = invertedGraph.get(dependency);
                if (dependents == undefined) {
                    invertedGraph.set(dependency, new Set([dependent]));
                } else {
                    dependents.add(dependent);
                }
            });
        }
        return invertedGraph;
    }

    /**
     * @param cyclicGraphWithNoLeafNodes a graph that only contains nodes with at least 1 outgoing edge
     * @returns any arbitrary cycle in the graph. Each element is a node in the cycle, and has an outgoing edge to
     * the subsequent element, with the last element having an outgoing edge to the first one.
     */
    private static getCycle<T>(cyclicGraphWithNoLeafNodes: DirectedGraph<T>): T[] {
        const chain: T[] = [];
        // otherwise the graph is not actually cyclic
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        let node: T = this._min(cyclicGraphWithNoLeafNodes.keys())!;
        let indexInChain = -1;
        do {
            chain.push(node);
            // otherwise the graph is not actually cyclic
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            node = this._min(cyclicGraphWithNoLeafNodes.get(node)!.values())!;
            indexInChain = chain.indexOf(node);
        } while (indexInChain < 0);
        return chain.slice(indexInChain, chain.length);
    }

    private static _min<T>(values: IterableIterator<T>): T | undefined {
        const firstElement = values.next();
        if (firstElement.done) {
            return undefined;
        }
        let min = firstElement.value;
        for (const value of values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    /**
     * Determines a topological sort for the given directed graph. <b>This function mutates the given graph</b>.
     * The result is the same regardless of iteration order of dependents and dependencies in the graph.
     * 
     * Keys in the graph represent dependents of their respective values (i.e. key will be sorted after
     * its values). Dependencies (map values) must not contain elements that are not also a key in the graph.
     * A map value of <code>null</code> is treated the same as the respective key having no dependencies.
     * 
     * <b>Algorithm</b>
     * 
     * 1. collect all nodes in the graph that have no dependencies (and remove them from the graph)
     * 2. create inverted graph to make deleting nodes easier
     * 3. while there are nodes without dependencies in our collection
     *      1. take node out of collection
     *      2. add the node to the end of our topological sort result
     *      3. remove the node from the graph
     *      4. remove the node from all dependencies it appears in; if a dependent now has no dependencies, add it to our collection
     * 4. is the graph empty?
     *      - yes -> the result contains a valid topological sorting
     *      - no -> find a cycle and report that as the result
     */
    public static topologicalSort<T>(graph: DirectedGraph<T>): TopologicalSortResult<T> {
        for (const dependencies of graph.values()) {
            dependencies?.forEach(dependency => {
                if (!graph.has(dependency)) {
                    dependencies.delete(dependency);
                }
            })
        }

        const invertedGraph = this.invertedGraph(graph);

        // find all nodes in the initial graph that have no dependencies
        // we use SortedSet in order to make the returned result as stable as possible
        const nodesWithoutDependencies: SortedSet<T> = new SortedSet();
        for (const [dependent, dependencies] of graph) {
            if (dependencies == null || dependencies.size === 0) {
                nodesWithoutDependencies.add(dependent);
            }
        }
        for (const nodeWithoutDependencies of nodesWithoutDependencies) {
            graph.delete(nodeWithoutDependencies);
        }

        // local method to remove a node from the dependencies of other nodes
        function removeNode(node: T) {
            invertedGraph.get(node)?.forEach((dependent) => {
                const dependentDependencies = graph.get(dependent);
                if (dependentDependencies != null) {
                    dependentDependencies.delete(node);
                    if (dependentDependencies.size === 0) {
                        // all dependencies are now satisfied
                        nodesWithoutDependencies.add(dependent);
                        graph.delete(dependent);
                    }
                }
            });
        }

        // actually start the topological sort
        const sortedNodes: T[] = [];
        while (nodesWithoutDependencies.size > 0) {
            // we just checked that there is a node
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            const node = nodesWithoutDependencies.minimum()!;
            nodesWithoutDependencies.delete(node);
            sortedNodes.push(node);
            removeNode(node);
        }

        const succeeded = graph.size === 0;
        return {
            succeeded: succeeded,
            result: succeeded ? sortedNodes : this.getCycle(graph),
        }
    }
}