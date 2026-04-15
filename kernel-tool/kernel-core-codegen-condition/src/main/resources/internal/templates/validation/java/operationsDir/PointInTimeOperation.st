PointInTimeOperation(t) ::= <<
<if(t.datum)>
controller.get<t.operatorNameCode>()
<else>
controller.makeDecimal(metaDataValidierung.getBaseYearAsString())
<endif>
>>