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

parser grammar GrammarOnly_ConditionParser;
options {
	superClass=AbstractConditionParser;
	tokenVocab=Base_ConditionLexer;
} 

bedingung_start	: preamble? bedingung EOF;

preamble:
        '@' 'UnterdrueckeWarnung' '(' n=NameOhnePunktSTRAmAnfang ')' {this.checkPreamble(_localctx);};

/* Bedingungen **********************************************************************************/

/** 
 * bedingung 
 * 
 * checker class: CheckBedingungImpl
 * creator class: ZusGesetzteBedingungCreator, if there is more than one teilBedingung
 *                if there is only one teilbedingung: see creator classes for the different subtypes of a condition
 * interface in parse tree: ICompositeCondition, if there is more than one teilBedingung
 *                          a subtype of ICondition else
 */
bedingung:
      teilBedingung (l1=logOp teilBedingung (l2=logOp teilBedingung {this.checkBedingung(_localctx);})*)? ;

/**
 * a string snippet that represents a bedingung.
 */
bedingungSnippet: bedingung EOF;

teilBedingung :	 before=Comment? (geklammerteBedingung | einfacheBedingung) after=Comment?;
geklammerteBedingung: '(' bedingung ')';
einfacheBedingung:
      feldListenBedingung | kontextListenBedingung 
     | feldUndWerteListenBedingung | feldListeUndWerteListenBedingung 
     | fieldAndValueListCondition
     | dateValidityCondition 
     | predefinedTypeValidityCondition 
     | applikationsBedingung | vergleichsBedingung   | wiederholungBedingung
     | {this.isHModel()}? zeilenBedingung
     | formularBedingung;  /* für eine schöne Fehlermeldung wird in der check-Methode auf isHModel geprüft. */


/**
 * feldListenBedingung 
 * 
 * checker class: CheckFeldListenBedingungImpl
 * creator class: FeldListenBedingungCreator
 * interface in parse tree: IEntityListCondition
 */
feldListenBedingung: feldListenBedingungName '(' entityListe ')' {this.checkFeldListenBedingung(_localctx);};

feldListenBedingungName: 
             'MindestensEinFeldAngegeben' | 'MehrAlsEinFeldAngegeben' | 'KeinFeldAngegeben' | 'FeldAngegeben' | 'FeldNichtAngegeben'
              | 'AlleFelderAngegeben' | 'FelderNichtGemeinsamAngegeben' | 'NichtAlleFelderOderKeinFeldAngegeben'
              |'DatumsBereicheUeberlappend' | 'NichtGenauEinFeldAngegeben' |'FeldWerteNichtEindeutig';

/** 
 * kontextListenBedingung 
 * 
 * checker class: CheckKontextListenBedingungImpl
 * creator class: KontextListenBedingungCreator
 * interface in parse tree: IEntityListCondition
 */
kontextListenBedingung: kontextListenBedingungName '(' entityListe ')' {this.checkKontextListenBedingung(_localctx);};

kontextListenBedingungName:
              'MindestensEinKontextAngegeben'| 'KeinKontextAngegeben' | 'KontextAngegeben' | 'KontextNichtAngegeben'
              |'AlleKontexteAngegeben' |'KontexteNichtGemeinsamAngegeben' | 'NichtAlleOderKeinKontextAngegeben';

/** 
 * feldListeUndWerteListenBedingung 
 * 
 * checker class: CheckFeldListeUndWerteListenBedingungImpl
 * creator class: FeldListeUndWerteListenBedingungCreator
 * interface in parse tree: IEntityListAndValueListCondition
 */
feldListeUndWerteListenBedingung: 
           feldListeUndWerteListenBedingungName '(' entityListe inOp wertListenSpezifier  ')'{this.checkFeldListeUndWerteListenBedingung(_localctx);};
feldListeUndWerteListenBedingungName: 'NichtAlleFeldWerteInWerteListe' | 'KeinFeldWertInWerteListe' | 'MindestensEinFeldWertInWerteListe' | 'MindestensEinDatumsBereichUeberlappend';
wertListenSpezifier: entityListe | stringListe;
 
/** 
 * fieldAndValueListCondition
 * 
 * checker class: CheckFieldAndValueListConditionImpl
 * creator class: FieldAndValueListConditionCreator
 * interface in parse tree: IEntityListAndValueListCondition
 */
fieldAndValueListCondition:
            fieldAndValueListConditionName '(' entitySpec toOp feldUndStringListenSpezifier ')'{this.checkFieldAndValueListCondition(_localctx);};
fieldAndValueListConditionName: 'WertNichtKonsistent';
feldUndStringListenSpezifier: feldOderString (',' feldOderString)*;
feldOderString: entitySpec | StringLiteral;

/** 
 * feldUndWerteListenBedingung 
 * 
 * checker class: CheckFeldUndWerteListenBedingungImpl
 * creator class: FeldUndWerteListenBedingungCreator
 * interface in parse tree: IValueListCondition
 */
feldUndWerteListenBedingung: 
          feldUndWerteListenBedingungName '(' entitySpec ',' wertListenSpezifier2 ')'{this.checkFeldUndWerteListenBedingung(_localctx);};
feldUndWerteListenBedingungName: 'FeldWertInWerteListe' | 'FeldWertNichtInWerteListe';

wertListenSpezifier2: stringListe | numberListe | feldWertOpListe;

feldWertOpListe : feldWertOp (',' feldWertOp)*; 

stringListe: s1=StringLiteral (',' s2=StringLiteral)*; 
numberListe: n1=number (',' n2=number)*; 
number: minus='-'? integerDigits=ZiffernFolge;

/**
 * dateValidityCondition
 *
 * checker class: CheckDateValidityConditionImpl
 * creator class: ValidityConditionCreator
 * interface in parse tree: IDateValidityCondition
 */
dateValidityCondition: validityConditionName '(' date=datumKonstrukt ')' {this.checkDateValidityCondition(_localctx);};

/**
 * predefinedTypeValidityCondition
 *
 * checker class: CheckPredefinedTypeValidityConditionImpl
 * creator class: ValidityConditionCreator
 * interface in parse tree: IPredefinedTypeValidityCondition
 */
predefinedTypeValidityCondition: validityConditionName '(' feld=entitySpec ',' predefinedType=StringLiteral  ')' {this.checkPredefinedTypeValidityCondition(_localctx);};

validityConditionName: 'Gueltig' | 'Ungueltig';

/** 
 * applikationsBedingung 
 * 
 * creator class: ApplikationsBedingungCreator
 * interface in parse tree: IApplicationCondition
 */
applikationsBedingung: applikationsBedingungName name = nameOhnePunkt;
applikationsBedingungName: 'ApplikationsBedingung';

/** 
 * vergleichsBedingung
 * 
 * checker class: CheckVergleichsBedingungImpl
 * creator class: VergleichsBedingungCreator
 * interface in parse tree: IComparisonCondition
 */
vergleichsBedingung:  lhs= operation vglOp rhs = operation {this.checkVergleichsBedingung(_localctx);};

/** 
 * wiederholungBedingung 
 * 
 * checker class: CheckWiederholungBedingungImpl
 * creator class: WiederholungBedingungCreator
 * interface in parse tree: IEntityListCondition
 */
wiederholungBedingung: wiederholungBedingungName  '(' entityListe (fromOp = VON from = entitySpec)? ')' {this.checkWiederholungBedingung(_localctx);};
wiederholungBedingungName: 'WiederholungNichtEindeutig' ;


/* Operationen **********************************************************************************/

/** 
 * operation 
 * 
 * checker class: CheckOperationImpl
 * creator class: ZusGesetzteFeldOperationCreator, if there is more than one operation
 *                if there is only one operation: see creator classes for the different subtypes of an operation
 * interface in parse tree: ICompositeOperation, if there is more than one operation
 *                          a subtype of IOperation else
 */
operation: before=Comment? einfacheOperation after=Comment? {this.checkOperation(_localctx);} #SINGLE_OP
          | before=Comment? '{' operation '}' after=Comment? {this.checkOperation(_localctx);} #BRACKET_OP
          | op1=operation (power='^') op2=operation {this.checkOperation(_localctx);} #POWER_OP
          | op1=operation (mult='*' | div = '/')  op2=operation  {this.checkOperation(_localctx);} #MULTDIV_OP
          | op1=operation (add='+' | sub = '-') op2=operation {this.checkOperation(_localctx);} #ADDSUB_OP;

/**
 * a string snippet that represents an operation.
 */
operationSnippet: operation EOF;

/** 
 * einfacheOperation 
 * 
 * checker class: CheckEinfacheOperationImpl
 * creator class: see creator classes for the different subtypes of an operation
 * interface in parse tree: IOperation
 */
einfacheOperation:
               (   konstante
                 | feldWertOp 
                 | feldListenOp
                 | feldListenOperOp 
                 | feldOp 
                 | feldOperOp
                 | rundenOp 
                 | rundenOperOp 
                 | feldListenAnzahlOp
                 | anzahlWertOp
                 | feldOpMitParams 
                 | datumAddOp
                 | datumDiffOp | datumExtractOp | datumBereichOp 
                 | datumKonstrukt | timeKonstrukt | dateTimeKonstrukt
                 | datumBereichKonstrukt
                 | konstanteReferenzjahr
                 | {this.isHModel()}?   extmpKonstanteUSB
                 | feldOpMitDatumSpecial
                 | {this.isHModel()}? feldListenOpHModel
                 | {this.isHModel()}? feldOpHModel
                 | anzahlAngegebenOpHModel
                 | groupOp
                 ) {this.checkEinfacheOperation(_localctx);};


/** 
 * feldWertOp 
 * 
 * checker class: CheckFeldWertOpImpl
 * creator class: FeldWertOpCreator
 * interface in parse tree: IFieldIdOperation
 */
feldWertOp: '[' entitySpec ']' {this.checkFeldWertOp(_localctx);};

/** 
 * feldListenOp 
 * 
 * checker class: CheckFeldListenOpImpl
 * creator class: FeldListenOpCreator
 * interface in parse tree: IEntityListOperation
 */
feldListenOp: feldListenOpName '(' entityListe ')' {this.checkFeldListenOp(_localctx);};    
feldListenOpName:  'MaxWert' | 'MinWert' | 'Summe' | 'SummeVonProdukten' | 'ErsterAngegebenerWert';
 
/**
 * feldListenOperOp
 * 
 * checker class: CheckFeldListenOperOpImpl
 * creator class: FeldListenOperOpCreator
 * interface in parse tree: IOpListOperation
 */
feldListenOperOp:   feldListenOperOpName '('operationListe ')' {this.checkFeldListenOperOp(_localctx);};   
operationListe: operation (',' operation)*;
feldListenOperOpName: 'Max' | 'Min';  

/** 
 * feldOp 
 * 
 * checker class: CheckFeldOpImpl
 * creator class: FeldOpCreator
 * interface in parse tree: IFieldIdOperation
 */
feldOp: feldOpName '(' entitySpec ')' {this.checkFeldOp(_localctx);}; 
feldOpName: 'AbsWert' | 'Laenge' | 'FeldWertAlsZahl' | 'FeldWertAlsString';           

/** 
 * feldOperOp
 *
 * checker class: CheckFeldOperOpImpl
 * creator class: FeldOperOpCreator
 * interface in parse tree: IOpListOperation
 */
feldOperOp: feldOperOpName '(' operation ')'{this.checkFeldOperOp(_localctx);}; 
feldOperOpName:  'Abs';          

/** 
 * rundenOp 
 * 
 * checker class: CheckRundenOpImpl
 * creator class: RundenOpCreator
 * interface in parse tree: IFieldIdOperationWithParameters
 */
rundenOp: rundenOpName '(' entitySpec (',' z=ZiffernFolge)? ')'{this.checkRundenOp(_localctx);}; 
rundenOpName: 'AbrundenWert' | 'AufrundenWert' | 'RundenWert';

/** 
 * rundenOperOp 
 * 
 * checker class: CheckRundenOperOpImpl
 * creator class: RundenOperOpCreator
 * interface in parse tree: IOpListOperationWithParameter
 */
rundenOperOp: rundenOperOpName '(' operation (',' z=ZiffernFolge)? ')'{this.checkRundenOperOp(_localctx);}; 
rundenOperOpName: 'Abrunden' | 'Aufrunden' | 'Runden';

/** 
 * feldListenAnzahlOp
 * 
 * checker class: CheckFeldListenAnzahlOpImpl
 * creator class: FeldListenAnzahlOpCreator
 * interface in parse tree: IEntityListOperation
 */
feldListenAnzahlOp: feldListenAnzahlOpName '(' entityListe ')'{this.checkFeldListenAnzahlOp(_localctx);};
feldListenAnzahlOpName: 'AnzahlAngegebenerKontexte' | 'AnzahlAngegebenerFelder' | 'AnzahlVerschiedenerWerte';
   
/** 
 * anzahlWertOp 
 * 
 * checker class: CheckAnzahlWertOpImpl
 * creator class: AnzahlWertOpCreator
 * interface in parse tree: IEntityListOperationWithValue
 */
anzahlWertOp:  anzahlWertOpName '(' konstante inOp entityListe ')' {this.checkAnzahlWertOp(_localctx);};
anzahlWertOpName: 'AnzahlWertInFeldliste';

/** 
 * feldOpMitParams 
 * 
 * checker class: CheckFeldOpMitParamsImpl
 * creator class: FeldOpMitParamsCreator
 * interface in parse tree: IFieldIdOperationWithParameters
 */
feldOpMitParams: feldOpMitParamsName '(' entitySpec ',' begin = ZiffernFolge ','ende = ZiffernFolge ')' {this.checkFeldOpMitParams(_localctx);}; 
feldOpMitParamsName: z= 'BereichAlsZahl' | s='BereichAlsString';

/** 
 * Konstante 
 * 
 * checker class: CheckKonstanteImpl
 * creator class: KonstanteCreator
 * interface in parse tree: IBoolConstantOperation for 'Wahr' and 'Falsch'
 *                          IPointInTimeOperation for 'Heute' and 'Jetzt'
 *                          IConstantOperation for string or number constants
 */
konstante: commonKonstante  {this.checkKonstante(_localctx);};           
commonKonstante:
            'Falsch' #falsch
           |'Wahr'   #wahr
           |'Heute'   #heute
           |'Jetzt'   #jetzt
           | StringLiteral   #stringLit
           | minus='-'? integerDigits=ZiffernFolge (decimalSeparator='.' fractionalDigits=ZiffernFolge)? #zahl;
        
/** 
 * konstanteReferenzjahr
 * 
 * checker class: CheckKonstanteReferenzjahrImpl
 * creator class: KonstanteCreator
 * interface in parse tree: IPointInTimeOperation
 */
konstanteReferenzjahr:   'Referenzjahr'    {this.checkKonstanteReferenzjahr(_localctx);};
/** 
 * extmpKonstanteUSB
 * 
 * checker class: CheckExtmpKonstanteUSBImpl
 * creator class: ExtmpKonstanteCreator
 * interface in parse tree: INumberOfUsbOperation
 */
extmpKonstanteUSB:  'AnzahlAngegebeneUsb' {this.checkExtmpKonstanteUSB(_localctx);};


/* Datums- und Datumsbereich-Operationen ****************************************************************************************/

/** 
 * datumDiffOp 
 * 
 * checker class: CheckDatumDiffOpImpl
 * creator class: DatumDiffOpCreator
 * interface in parse tree: IDateDiffOperation
 */
datumDiffOp: datumDiffOpName '(' da1=datumAngabe2 ',' da2=datumAngabe2 ')' {this.checkDatumDiffOp(_localctx);};
datumDiffOpName: 'DifferenzInTagen' | 'DifferenzInMonaten' | 'DifferenzInJahren' | 'DifferenzInStunden' | 'DifferenzInMinuten' | 'DifferenzInSekunden';

/** 
 * datumAddOp 
 * 
 * checker class: CheckDatumAddOpImpl
 * creator class: DatumAddOpCreator
 * interface in parse tree: IDateAddOperation
 */
datumAddOp: datumAddOpName '(' da=datumAngabe2 ',' amount=operation ')' {this.checkDatumAddOp(_localctx);};
datumAddOpName: 'AddiereJahre' | 'AddiereMonate' | 'AddiereTage' | 'AddiereStunden' | 'AddiereMinuten' | 'AddiereSekunden';

/** 
 * datumExtractOp 
 * 
 * checker class: CheckDatumExtractOpImpl
 * creator class: DatumExtractOpCreator
 * interface in parse tree: IDateExtractionOperation
 */
datumExtractOp: datumExtractOpName '(' datumAngabe ')'{this.checkDatumExtractOp(_localctx);};
datumExtractOpName: 'JahrAusDatum' | 'QuartalAusDatum' | 'MonatAusDatum' | 'TagAusDatum' | 'StundenAusZeit' | 'MinutenAusZeit' | 'SekundenAusZeit' | 'DatumAusDateTime' | 'ZeitAusDateTime';

/** 
 * datumKonstrukt 
 * 
 * checker class: CheckDatumKonstruktImpl
 * creator class: DatumKonstruktCreator
 * interface in parse tree: IDateConstructionOperation
 */
datumKonstrukt: 'Datum' '(' dp1=datumParam ',' dp2=datumParam (',' dp3=datumParam (',' dp4 = datumParam )? )?')' {this.checkDatumKonstrukt(_localctx);};

/** 
 * timeKonstrukt 
 * 
 * checker class: CheckTimeKonstruktImpl
 * creator class: TimeKonstruktCreator
 * interface in parse tree: ITimeConstructionOperation
 */
timeKonstrukt: 'Zeit' '(' (stunden=datumParam (',' minuten=datumParam (',' sekunden=datumParam ) ? ) ?) ?')' {this.checkTimeKonstrukt(_localctx);};

/** 
 * dateTimeKonstrukt 
 * 
 * checker class: CheckDateTimeKonstruktImpl
 * creator class: DateTimeKonstruktCreator
 * interface in parse tree: IDateTimeConstructionOperation
 */
dateTimeKonstrukt: 'DateTime' '(' date=datumAngabe2 ',' time=datumAngabe ')' {this.checkDateTimeKonstrukt(_localctx);};

/**
 * datumBereichKonstrukt
 *
 * checker class: CheckDatumBereichKonstruktImpl
 * creator class: DatumBereichKonstruktCreator
 * interface in parse tree: IDateRangeConstructionOperation
 **/
datumBereichKonstrukt: 'DatumBereich' '(' date1=entitySpec ',' date2=entitySpec ')' {this.checkDatumBereichKonstrukt(_localctx);};


/** 
 * datumBereichOp 
 * 
 * checker class: CheckDatumBereichOpImpl
 * creator class: DatumBereichOpCreator
 * interface in parse tree: IDateRangeExtractionOperation
 */
datumBereichOp: datumBereichOpName '(' datumBereichAngabe')'{this.checkDatumBereichOp(_localctx);};
datumBereichOpName: 'StartAusDatumBereich' | 'EndeAusDatumBereich'  ;     

/** 
 * datumAngabe 
 * 
 * checker class: CheckDatumAngabeImpl
 * creator class: DatumAngabeCreator
 * interface in parse tree: depending on the type of operation that is used to specify the date:
 *                          IFieldIdOperation, if the date is specified by a field
 *                          IPointInTimeOperation for 'Heute' and 'Jetzt'
 *                          IConstantOperation, if the date is specified by a string constant
 *                          IPointInTimeOperation for 'Referenzjahr'
 *                          IDateRangeExtractionOperation
 *                          IDateConstructionOperation
 */
datumAngabe:
            (datumFeld=entitySpec     
           | heute= 'Heute'
           | jetzt= 'Jetzt'           
           | konstanteReferenzjahr
           | datumBereichOp     
           | datumKonstrukt
           | timeKonstrukt
           | dateTimeKonstrukt
           | datumAddOp
           | datumExtractOp
  	   | datumString= StringLiteral) {this.checkDatumAngabe(_localctx);};

datumAngabe2:  datumAngabe | feldOpMitDatumSpecial;

datumBereichAngabe:  entitySpec | konstanteReferenzjahr ;
  
/** 
 * datumParam 
 * 
 * checker class: see datumKonstrukt
 * creator class: DatumParamCreator
 * interface in parse tree: IConstantOperation if the date param is specified by a string constant
 *                          IFieldIdOperation if the date param is specified by a field
 */
datumParam: entitySpec | datumExtractOp | s=StringLiteral;

/** 
 * feldOpMitDatumSpecial
 * 
 * checker class: CheckFeldOpMitDatumSpecialImpl
 * creator class: FeldOpMitDatumSpecialCreator
 * interface in parse tree: IFieldIdOperationWithParameters
 */
feldOpMitDatumSpecial: 	'WertAlsDatum' '(' entitySpec  ',' tagSpec ')' {this.checkFeldOpMitDatumSpecial(_localctx);}; 
tagSpec: 'ErsterTag' | 'LetzterTag';

/* logOp, calcOp, vglOp *******************************************************************************/
logOp: 
	('Und'  | 'und'  | 'UND')  #Und
    |	('Oder' | 'oder' | 'ODER') #Oder;

/*--------- Vergleiche ----------------------------------------------------------------------------------------*/
vglOp  : eVgl= equalityVgl | oVgl=orderingVgl | pVgl=patternVgl | tVgl=toleranceVgl;

equalityVgl: eq = '==' | ne = '!=';
orderingVgl: gt='>' | lt = '<' | ge='>=' | le ='<=';
patternVgl:  nm='NichtWieMuster' | wm='WieMuster';

toleranceVgl : 
          tol10 = 'UngleichMitToleranz10'  | tol5 = 'UngleichMitToleranz5'  
         | tol2 = 'UngleichMitToleranz2'   | tol1 = 'UngleichMitToleranz1' ;

inOp: 'In' | 'IN' | 'in';
toOp: 'Zu' | 'ZU' | 'zu';


/* Spezifikation der Entities (Felder, Kontexte) ****************************/

/** 
 * entityListe 
 * 
 * checker class: CheckEntityListenUtils
 * creator class: FeldIdListeUtil
 * interface in parse tree: IEntityIdList
 */
entityListe :	entitySpec ( ',' entitySpec )* ;


                               
/*** Specification of entitySpec is similar to GrammarOnly_ErrorTextParameterParser.g4, In case of changes, check if the other parser should be changed, too ****/
                               

/** 
 * entitySpec
 * 
 * checker class: CheckEntitySpecImpl
 * creator class: FeldIdCreator
 * interface in parse tree: IEntityId
 */
entitySpec:  (outer = '$')? regelKontextEntity ('Fuer' semanticIndex)? {this.checkEntitySpec(_localctx);}|
             {this.isHModel()}? entityHModel ('->' categoryName=nameOhnePunkt)? {this.checkEntitySpec(_localctx);}|
             {this.isA12Model()}? (outer='$')? entity ('->' categoryName=nameOhnePunkt)? ('Fuer' semIdx=semanticIndex)? ('Mit' {this.initFilterCondition(_localctx);} filterCond=bedingung {this.resetFilterCondition(_localctx);})? {this.checkEntitySpec(_localctx);};
             
             
/** 
 * regelKontextEntity 
 * 
 * checker class: CheckRegelKontextEntityImpl
 * creator class: FeldIdCreator
 * interface in parse tree: IEntityId
 */
regelKontextEntity:     regelkontext='RegelKontext' 
                        stern='*'?   /* wildcard is not allowed,  only added to get nice error-messages */
                        ({this.isHModel()}? ((hash_zeile= '.' '#Zeile')| zeile_konkret='.' ZiffernFolge))? {this.checkRegelKontextEntity(_localctx); };
             
/** 
 * entity 
 * 
 * checker class: CheckEntityImpl
 * creator class: FeldIdCreator
 * interface in parse tree: IEntityId
 */
entity:   (slash='/' ? idNameRef | relativeUp) ('/' idNameRef )*;

/**
 * relativeUp a path from the rule group directed to the root group
 * 
 * checker class: CheckRelativeUpImpl
 * -creator class:- part of entity
 * -interface in parse tree:- part of IEntityId
 */
// NOTE: if we ever consider completely removing idName, the explicit turning point (optional since A12K-1703 in 2022), we should
// also consider that the on-the-fly document-model migration service will then have to load an old parser version to reliably find
// and remove the idNames. But the parser classes and package names are not namespaced by version, therefore loading an old parser
// is not easily possible in projects that also use the current parser.
relativeUp: relUp? '..' idName? stern='*'? {this.checkRelativeUp(_localctx);};

/**
 * relUp a path from the rule group directed to the root group without 
 * final element
 * 
 * checker class: CheckRelUpImpl
 */
relUp: oneUp+ {this.checkRelUp(_localctx);};
oneUp: '..' '/';

/**
 * idNameRef
 * checker class: CheckIdNameRefImpl
 */
idNameRef: idName (pstern='.' '*')? stern='*'? {this.checkIdNameRef(_localctx);};

/**
 * semanticIndex
 * 
 * checker class: CheckSemanticIndexImpl
 * interface in parse tree: IEntityId
 */
semanticIndex:  (e=entitySpec | konst=StringLiteral) {this.checkSemanticIndex(_localctx); };
                

// semanticIndex: semanticIndexSpec (',' semanticIndexSpec)* {this.checkSemanticIndex(_localctx); };
///**
// * semanticIndexSpec
// * 
// * checker class: CheckSemanticIndexSpecImpl
// * interface in parse tree: IEntityId
// */
// semanticIndexSpec: (semanticIndexFieldName)? (e=entitySpec | konst=StringLiteral) {this.checkSemanticIndexSpec(_localctx); };
/**
 * semanticIndexFieldName
 * 
 * checker class: CheckSemanticIndexFieldNameImpl
 * interface in parse tree: IEntityId
 */
// semanticIndexFieldName: (e=entity '=') {this.checkSemanticIndexFieldName(_localctx); };

// H-Models **************************************************************
// H-Model bedingungen

// 
 // formularBedingung 
// 
// checker class: CheckFormularBedingungImpl
 // creator class: FormularBedingungCreator
 // interface in parse tree: IRootGroupCondition
 //
formularBedingung: formularBedingungName '(' formularSpec ')' {this.checkFormularBedingung(_localctx);};
/** 
 * formularSpec 
 * 
 * checker class: see formularBedingung
 * creator class: FormularUtil
 * interface in parse tree: see formularBedingung
 */
formularSpec : nameOhnePunkt formularEbenenSpecHModel;   

/** 
 * formularBedingungName 
 * 
 * checker class: CheckFormularBedingungImpl
 * creator class: see formularBedingung
 * interface in parse tree: see formularBedingung
 */
formularBedingungName:  ('MindestensEinVordruckAngegeben' |'VordruckNichtAngegeben' | 'KeinVordruckAngegeben' | 'VordruckAngegeben' | 'VordruckNichtFortlaufend' | 'UsbNichtFortlaufend') 
                {this.checkFormularBedingungName(_localctx);}; 

/** 
 * zeilenBedingung 
 * 
 * checker class: CheckZeilenBedingungImpl
 * creator class: ZeilenBedingungCreator
 * interface in parse tree: IEntityListCondition
 */
zeilenBedingung: zeilenBedingungName '(' entityListe ')' {this.checkZeilenBedingung(_localctx);};
zeilenBedingungName: 'ZeilenLueckenlos' |'ZeilenNichtAufsteigend' |'ZeileNichtEindeutig';

/** 
 * feldListenOpHModel 
 * 
 * checker class: CheckFeldListenOpHModelImpl
 * creator class: FeldListenOpHModelCreator
 * interface in parse tree: IEntityListOperation
 */
feldListenOpHModel: feldListenOpHModelName '(' entityListe ')' {this.checkFeldListenOpHModel(_localctx);};      
feldListenOpHModelName: 'MaximalAngegebenerZeilenindex'; 

/** 
 * feldOpHModel
 * 
 * checker class: CheckFeldOpHModelImpl
 * creator class: FeldOpHModelCreator
 * interface in parse tree: IFieldIdOperation
 */
feldOpHModel: feldOpHModelName '(' entitySpec ')' {this.checkFeldOpHModel(_localctx);}; 
feldOpHModelName:  'AktuelleVordruckNummer';           

/** 
 * anzahlAngegebenOpHModel
 * 
 * checker class: CheckAnzahlAngegebenOpHModelImpl
 * creator class: AnzahlAngegebenOpHModelCreator
 * interface in parse tree: INumberOfRootGroupsOperation
 */
anzahlAngegebenOpHModel: anzahlAngegebenOpHModelName '(' formularSpec ')'{this.checkAnzahlAngegebenOpHModel(_localctx);}; 
anzahlAngegebenOpHModelName: 'AnzahlAngegebenerVordrucke';

/**
 * groupOp
 * 
 * checker class: CheckGroupOpImpl
 * creator class:  GroupOpCreator
 * interface in parse tree: IGroupOperation
 */
groupOp: groupOpName '(' entitySpec ')' {this.checkGroupOp(_localctx);};
groupOpName: 'AktuelleWiederholung';


/** 
 * entityHModel
 * 
 * checker class: CheckIdNameRefHModelImpl
 * creator class: FeldIdCreator
 * interface in parse tree: IEntityId
 */
entityHModel:   slash='/' ? idNameRefHModel  ({this.isGroupFromHModel()}?'/' idNameRefHModel )* spezifierHModel;
idNameRefHModel: idName {this.checkIdNameRefHModel(_localctx);};

formularEbenenSpecHModel: formularEbeneLfdNr? usbSpezifier?;

formularEbeneLfdNr:
   '/' ZiffernFolge #feKonkret
    | '/' stern='*'   #feOldStern
| n_stern='*'   #feNewStern;


/** 
 * spezifierHModel
 * 
 * checker class: CheckSpezifierHModelImpl
 * creator class: see entityHModel
 * interface in parse tree: see entityHModel
 */
spezifierHModel:  lfdNrSpezifier? zeileSpezifier? usbSpezifier? {this.checkSpezifierHModel(_localctx);};

lfdNrSpezifier :
	 '/' ZiffernFolge #lkonkret  
        |'/' '*' einschraenkung?#lstern;

zeileSpezifier :
	 '.' ZiffernFolge #zkonkret  
        |'.' '#Zeile' #ziteration
	|'.' '*' einschraenkung? #zstern;

einschraenkung : '(' z1=ZiffernFolge '-' z2=ZiffernFolge ? ')';
usbSpezifier :	'@' '*' ;


/////////////////////////////////////LEXER //////////////////////////////////////////////////////
idName
	:	nameOhnePunkt ('.' (NameOhnePunktSTRAmAnfang | NameOhnePunktMitQuotes))*;
nameOhnePunkt
 	:	NameOhnePunktSTRAmAnfang | NameOhnePunktZifferAmAnfang | ZiffernFolge | NameOhnePunktMitQuotes;