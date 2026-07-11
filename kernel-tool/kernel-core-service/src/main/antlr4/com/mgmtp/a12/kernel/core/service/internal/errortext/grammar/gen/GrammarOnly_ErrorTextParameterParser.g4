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


parser grammar GrammarOnly_ErrorTextParameterParser;

options {
	superClass=AbstractErrorTextParameterParser;
	tokenVocab=BaseErrorTextParameterLexer;
}


/* Entry point for parsing a error text parameter */
parameter : (
	referenzjahr
	| lfdNr 
	| line 
	| usb 
	| vordruckzeile 
	| {isA12Model()}? groupIndex
	| field
	| indexField
) EOF;

referenzjahr: 'Referenzjahr' calc? {this.checkReferenzjahr();};

lfdNr: '#' 'LfdNr' {this.checkFehlerEnum(_localctx);};

line: '#' 'Zeile' calc? { this.checkZeile(); };

calc: operator=('+'|'-') DigitSequence;

usb: '#' 'Usb' { this.checkUSB(); };

vordruckzeile: id=entitySpec '.' 'Vordruckzeile' { this.checkVordruckzeile(_localctx); };

groupIndex: '#' id=entitySpec ('Fuer' semanticIndex)? { this.checkLevelParam(_localctx); };

field: id=entitySpec valueOrCategory? ('Fuer' semanticIndex)? { this.checkFieldRef(_localctx); };

indexField: 'index' '(' id=entitySpec ')' valueOrCategory? // valueOrCategory is a must, but in the grammar we make it optional
// for better error messages
	{ this.checkIndexFieldRef(_localctx); };

// NOTE: valueOrCategory is only used with indexField, field can have a '.Wert' suffix too, but for field, the suffix is handled in reservedKeyWordCurrentlyAllowedWithinEntityName
valueOrCategory: ('.' 'Wert') #value
| categoryRef #category
; 

categoryRef: '->' (categoryName=nameWithoutDot)? // existence of categoryName checked in parser for better error message
//TODO[EXT-19962/A12K-1304]
//	('FuerWert' fieldValue=StringLiteral)? // not allowed in indexFieldRef -> has to be checked in the parser
;



/*** similar in GrammarOnly_ConditionParser.g4, In case of changes, check if the other parser should be changed, too ****/


entitySpec:
	regelKontextEntity
	| {this.isHModel()}? entityHModel 
	| {this.isA12Model()}? entity;

regelKontextEntity:     'RegelKontext' { this.addRegelKontextEntityToReferencedEntities(_localctx); };

entityHModel:		pathHModel spezifierHModel;
pathHModel: slash='/' ? idNameRefHModel ({this.isGroupFromHModel()}? '/' idNameRefHModel)*;
idNameRefHModel: idName;

spezifierHModel:	lfdNrSpezifier? zeileSpezifier? ;
lfdNrSpezifier:		'/' lkonkret=DigitSequence;
zeileSpezifier: 	'.' zkonkret=DigitSequence;

entity:		(slash='/' ? idNameRef | relativeUp) ('/' idNameRef )*;

/**
 * relativeUp a path from the rule group directed to the root group
 *
 * -creator class:- part of entity
 * -checker class: CheckRelativeUpImpl
 */
// NOTE: if we ever consider completely removing idName, the explicit turning point (optional since A12K-1703 in 2022), we should
// also consider that the on-the-fly document-model migration service will then have to load an old parser version to reliably find
// and remove the idNames. But the parser classes and package names are not namespaced by version, therefore loading an old parser
// is not easily possible in projects that also use the current parser.
relativeUp: relUp? '..' idName?{this.checkRelativeUp(_localctx);};

/**
 * relUp a path from the rule group directed to the root group without
 * final element
 *
 */
relUp: oneUp+ {this.checkRelUp(_localctx);};
oneUp: '..' '/';

idNameRef: idName;

semanticIndex:  (id=entitySpec ('Fuer' semanticIndex)? // semantic index of semantic index not allowed, but for better error messages allow it in the grammar
	| konst=StringLiteral
) {this.checkSemanticIndex(_localctx); };

/**
 * idName the identifiers for groups, that paths are composed of,  or the final identifier
 *
 * -checker class: CheckIdNameImpl
 */
idName
	:	nameWithoutDot (('.' (NameWithoutDotSTRAtBeginning | NameWithoutDotWithQuotes))* | nameSuffixContainingReservedKeyWordCurrentlyAllowedWithinEntityName)
	{this.checkIdName(_localctx);};

nameWithoutDot
 	:	NameWithoutDotSTRAtBeginning | NameWithoutDotDigitAtBeginning | DigitSequence | NameWithoutDotWithQuotes | reservedKeyWordCurrentlyAllowedWithinEntityName;

// Some language keywords don't have to be escaped if they appear in an entity name for historic reasons. Especially for the token 'value' which is a common 
// field name it would not be easy to understand for a user why it would have to be escaped. So we keep this special treatment.
nameSuffixContainingReservedKeyWordCurrentlyAllowedWithinEntityName
	: ('.' (NameWithoutDotSTRAtBeginning | NameWithoutDotWithQuotes | reservedKeyWordCurrentlyAllowedWithinEntityName))* 
		'.' (NameWithoutDotSTRAtBeginning | NameWithoutDotWithQuotes | reservedKeyWordCurrentlyAllowedWithinAndAtTheEndOfEntityName);
// Note: 'Wert' occurs not only here, but also in valueOrCategory
// Note: if the following list is changed, ErrortextParameterMultiLanguageHandler#hasToBeQuoted has to be changed accordingly
reservedKeyWordCurrentlyAllowedWithinEntityName
	: 'Wert' | 'Vordruckzeile' | reservedKeyWordCurrentlyAllowedWithinAndAtTheEndOfEntityName;
reservedKeyWordCurrentlyAllowedWithinAndAtTheEndOfEntityName
	: 'LfdNr' | 'Zeile' | 'Usb' | 'index';
