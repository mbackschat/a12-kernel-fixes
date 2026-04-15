InitEntityListAndValueListConditionParam(t) ::= <<
Set\<String\> values<t.paramNr> = new HashSet\<\>();
<if(t.atLeastOneValueConstruct)>boolean containsValueListFilter<t.paramNr> = controller.fuegePruefungsrelevanteFeldWerteZuWerteListe(iter.get(), <t.valueListVariableCode>, values<t.paramNr>);<\n><else>
boolean[] foundNonFilledFieldInValueList<t.paramNr> = [ false ];
boolean foundNonRelevantFieldInValueList<t.paramNr> = !controller.fuegeFeldWerteZuWerteListe(iter.get(), <t.valueListVariableCode>, values<t.paramNr>, foundNonFilledFieldInValueList<t.paramNr>);
<endif>
boolean isNumber<t.paramNr> = controller.isZahlFeld(<t.valueListVariableCode>.get(0).getUniqueName());
List\<Object\> valuesAsObjectList<t.paramNr> = controller.convertStringArrayToMenge(values<t.paramNr>.toArray(new String[values<t.paramNr>.size()]), isNumber<t.paramNr>);<\n><\n>
>>