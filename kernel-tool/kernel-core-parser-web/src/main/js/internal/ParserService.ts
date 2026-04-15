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
import type { CommonTokenStream as CommonTokenStreamTypingsIncompleteAsUsedInEditorAddon } from "antlr4"; // TODO can we do this cleaner?
import type { CharStream, CommonTokenStream, Lexer } from "antlr4ForA12Kernel";
// warning: do not let intellij trick you into accepting the auto-removal of this blank line - eslint will attack if the line gets stolen!

import type { ILexerConstructor, IParserConstructor, IParserParameter, Language } from "./api.js";
import Base_ConditionLexer from "./gen/Base_ConditionLexer.js";
import GrammarOnly_ConditionParser from "./gen/GrammarOnly_ConditionParser.js";

export function createLexer(language: Language = "de"): ILexerConstructor
	& (new (...args: any[]) => Lexer) // we need to tell tsc that the ILexerConstructor creates a real lexer (the problem is that ILexerConstructor is defined in the editor-addon, which references an antlr version with reduced type declarations vs our custom antlr fork, and Lexer is from our fork here)   TODO clean the args
{
	const { mlh, translatedLexerClassLiteralNames } = Base_ConditionLexer.initMlhAndLiteralNames(language, Base_ConditionLexer.literalNames)
	const lexerClass: ILexerConstructor & (new (...args: any[]) => Lexer) = class MVKLexer extends Base_ConditionLexer {
		constructor(input: CharStream) {
			super(input);
			this.mlh = mlh
		}
	};
	lexerClass.literalNames = translatedLexerClassLiteralNames
	return lexerClass;
}

export function createParser(parserParameter: IParserParameter): IParserConstructor {
	return class MVKParser extends GrammarOnly_ConditionParser {
		constructor(input: CommonTokenStreamTypingsIncompleteAsUsedInEditorAddon) {
			super(input as CommonTokenStream);
			this.initialize(
				parserParameter.extmp,
				parserParameter.hDV
			);
		}
	};
}
