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
import {
    ArrayMap, ArraySet
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

import { CollectionsUtils } from "../util/CollectionsUtil.js";

export class RepetitionNotUniqueErrorCache {

    private _erroneousRepetitionsWithSameValueValueError = new ArrayMap<number[], Set<number[]>>();
    private _erroneousRepetitionsWithSameValueOmissionError = new ArrayMap<number[], Set<number[]>>();
    private _initialised = false;

    public clear(): void {
        this._erroneousRepetitionsWithSameValueValueError = new ArrayMap<number[], Set<number[]>>();
        this._erroneousRepetitionsWithSameValueOmissionError = new ArrayMap<number[], Set<number[]>>();
        this._initialised = false;
    }

    public getErroneousRepetitionsWithValueError(rep: number[]): Set<number[]> {
        return CollectionsUtils.getOrDefault(
            this._erroneousRepetitionsWithSameValueValueError,
            rep,
            new ArraySet<number[]>() as any
        );
    }

    public hasValueError(rep: number[]): boolean {
        return this._erroneousRepetitionsWithSameValueValueError.has(rep);
    }

    public hasOmissionError(rep: number[]): boolean {
        return this._erroneousRepetitionsWithSameValueOmissionError.has(rep);
    }

    public getErroneousRepetitionsWithOmissionsError(rep: number[]): Set<number[]> {
        return CollectionsUtils.getOrDefault(
            this._erroneousRepetitionsWithSameValueOmissionError,
            rep,
            new ArraySet<number[]>() as any
        );
    }

    public addErroneousRepetitionWithValueError(rep1: number[], rep2: number[]): void {
        let repetitions = CollectionsUtils.getOrSetDefault(
            this._erroneousRepetitionsWithSameValueValueError,
            rep1,
            new ArraySet<number[]>() as any
        );
        repetitions.add(rep1);
        repetitions.add(rep2);
        repetitions = CollectionsUtils.getOrSetDefault(
            this._erroneousRepetitionsWithSameValueValueError,
            rep2,
            new ArraySet<number[]>() as any
        );
        repetitions.add(rep1);
        repetitions.add(rep2);
    }

    public addErroneousRepetitionWithOmissionsError(rep1: number[], rep2: number[]): void {
        let repetitions = CollectionsUtils.getOrSetDefault(
            this._erroneousRepetitionsWithSameValueOmissionError,
            rep1,
            new ArraySet<number[]>() as any
        );
        repetitions.add(rep1);
        repetitions.add(rep2);
        repetitions = CollectionsUtils.getOrSetDefault(
            this._erroneousRepetitionsWithSameValueOmissionError,
            rep2,
            new ArraySet<number[]>() as any
        );
        repetitions.add(rep1);
        repetitions.add(rep2);
    }

    public isInitialised(): boolean {
        return this._initialised;
    }

    public setInitialised(): void {
        this._initialised = true;
    }
}