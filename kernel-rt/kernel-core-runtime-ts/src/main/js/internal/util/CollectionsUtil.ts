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
export class CollectionsUtils {

    /**
     * @returns the object assigned to the given <code>key</code>.
     *          If the map doesn't contain the given <code>key</code>, the given <code>defaultValue</code>
     *          will be returned.
     */
    public static getOrDefault<K, V>(map: Map<K, V>, key: K, defaultValue: V): V {
        const result = map.get(key);
        if (result !== undefined) {
            return result;
        }
        return defaultValue;
    }

    /**
     * @returns the object assigned to the given <code>key</code>.
     *          If the map doesn't contain the given <code>key</code>, the given <code>defaultValue</code>
     *          will be set for the given <code>key</code> and also returned.
     */
    public static getOrSetDefault<K, V>(map: Map<K, V>, key: K, defaultValue: V): V {
        let result = map.get(key);
        if (result === undefined) {
            map.set(key, defaultValue);
            // since we added the defaultValue to the map, get cannot deliver undefined
            // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
            result = map.get(key)!;
        }
        return result;
    }

}
