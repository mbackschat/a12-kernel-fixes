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

lexer grammar BaseErrorTextParameterLexer;

options {
	superClass=AbstractErrorTextParameterLexer;
}

///////////////////////////////////////////////////////////////////////////
// set type for error text parameters that are different in english
// at the moment: value (Wert), For (Fuer), RuleGroup (RegelKontext) and RootGroup (LfdNr)
ID: [a-zA-Z]+ {this.getMultiLanguageHandler().setTypeInLexer(this);};

/////////////////////////////////////LEXER //////////////////////////////////////////////////////
PLUS: '+';
MINUS: '-';
SLASH: '/';
DOT: '.';
UP: '..';
LPAREN: '(';
RPAREN: ')';
NUM: '#';
ROOT_GROUP_INDEX: 'LfdNr';
LINE: 'Zeile';
USB: 'Usb';
FOR: 'Fuer';
//TODO[EXT-19962/A12K-1304]: (additionally, 'FuerWert'/'ForValue' has to be added to the LexerTerminals, for multi language handling)
// FOR_VALUE: 'FuerWert';
INDEX: 'index';
RULE_GROUP: 'RegelKontext';
VALUE: 'Wert';
VORDRUCKZEILE: 'Vordruckzeile';
VORDRUCKNAME: 'Vordruckname';
REFERENZJAHR: 'Referenzjahr';
CAT: '->' ;


//// Lexer rules, similar in Base_ConditionLexer.g4. In case of changes, check if the other lexer should be changed, too. ////
DigitSequence 
	:	DIGIT+;
	
StringLiteral : '"' ( '""' | ~["] )* '"' ;

// escaping mechanism to allow for field or group names, which coincide with a keyword
// The lexer does not create keywords, but the quotes have to be removed externally.
NameWithoutDotWithQuotes
	:	'\'' STR (STR | DIGIT)* '\'' { this.setTextUnquotedInLexer(); };

NameWithoutDotSTRAtBeginning	
	:	STR (STR | DIGIT )*;
	
NameWithoutDotDigitAtBeginning 	
	:	DigitSequence STR (STR | DIGIT )* ;

fragment STR: 'a'..'z'|'A'..'Z'|'_'|'Ä'|'Ü'|'Ö'|'ä'|'ü'|'ö'|'ß'| ':';
fragment DIGIT:('0' .. '9');	 
WS : (' '|'\r'|'\t'|'\u000C'|'\n') -> channel(HIDDEN);
