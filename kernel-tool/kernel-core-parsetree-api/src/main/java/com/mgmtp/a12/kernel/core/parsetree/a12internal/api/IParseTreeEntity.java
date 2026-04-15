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

package com.mgmtp.a12.kernel.core.parsetree.a12internal.api;

import com.mgmtp.a12.kernel.core.parsetree.a12internal.ast.ASTVisitor;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Allgemeines Interface für einen Knoten im MVK parse-tree.
 * Der MVK parse-tree ist die Repräsentation einer geparsten Bedingung, die vollständig von der
 * verwendeten antlr-Parser-Technologie und den dort aus der Grammatik generierten Objekten befreit
 * ist.
 * Jeder Knoten im Parsebaum muss eine {@link IParseTreeEntity#traverse(ASTVisitor)}
 * Methode anbieten. In den jeweiligen abgeleiteten Interfaces werden default-Implementierungen
 * der traverse-Methode bereitgestellt, die nicht überschrieben werden sollen.
 */
@OnlyForUsage
public interface IParseTreeEntity {

	/**
	 * Spezifiziert die Stern-Operation, d.h. eine Mengenbildung, und wird für die
	 * Spezifikation eines Indexes einer Entity verwendet.
	 */
	public static final long WILDCARD = 0L;

	/**
	 * Spezifiziert die Iteration, und wird für die
	 * Spezifikation eines Indexes einer Entity verwendet.
	 */
	public static final long ITERATION = -1L;

	/**
	 * Spezifiziert einen nicht relevanten Index.
	 * Dies wird für den USB verwendet, falls es sich nicht um
	 * ein H-Modell (ExtMP) mit USB handelt.
	 */
	public static final long UNSPECIFIED = -2L;

	/**
	 * index is specified by the value of the index field
	 * Note: we do not use -3L because this is used in the
	 * context of the assignments.
	 */
	public static final long SEMANTIC_INDEX = -4L;

	/**
	 * specifies the parallel iteration over different repeatable groups whose repetitions are
	 * matched by the same values of index fields
	 */
	public static final long PARALLEL_ITERATION = -6L;

	/**
	 * REP_NOT_UNIQUE_REFERENCED_LEVEL_TO_BE_REPLACED is used to represent a level of the referenced
	 * fields in meta rules for the language construct RepetitionNotUniue (German name:
	 * WiederholungNichtEindeutig) that has to be replaced by all concrete erroneous repetitions.
	 * <br>
	 * <b>Note:</b> This value <b>is not set during parsing</b> but only during code generation for
	 * the referenced fields in the generated meta rules.
	 * For details s EXT-20784
	 */
	public static final long REP_NOT_UNQ_REF_LEVEL_HAS_TO_BE_REPLACED = -7L;

	/**
	 * Schnittstelle zum Traversieren des Baums, d.h. die <code>accept</code>
	 * Methode in der Namenskonvention des Visitor Patterns.
	 *
	 * @param visitor
	 * 		Visitor, der bei der Traversierung verwendet wird.
	 */
	public void traverse(ASTVisitor visitor);

}
