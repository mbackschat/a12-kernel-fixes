InitEntityListAndValueListConditionParam(t) ::= <<
var values<t.paramNr> = new Set();
<if(t.atLeastOneValueConstruct)>var containsValueListFilter<t.paramNr> = controller.fuegePruefungsrelevanteFeldWerteZuWerteListe(iter.get(), <t.valueListVariableCode>, values<t.paramNr>);<\n><else>
var foundNonFilledFieldInValueList<t.paramNr> = [false];
var foundNonRelevantFieldInValueList<t.paramNr> = !controller.fuegeFeldWerteZuWerteListe(iter.get(), <t.valueListVariableCode>, values<t.paramNr>, foundNonFilledFieldInValueList<t.paramNr>);
<endif>
var isNumber<t.paramNr> = controller.isZahlFeld(<t.valueListVariableCode>[0].getUniqueName());
var valuesAsObjectList<t.paramNr> = controller.convertStringArrayToMenge(Array.from(values<t.paramNr>), isNumber<t.paramNr>);<\n><\n>
>>