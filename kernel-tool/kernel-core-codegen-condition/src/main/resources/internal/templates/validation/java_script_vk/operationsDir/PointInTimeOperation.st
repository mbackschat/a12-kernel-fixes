PointInTimeOperation(t) ::= <<
<if(t.datum)>controller.get<t.operatorNameCode>()<else>controller.makeDecimal(this._metaDataValidierung.getBaseYearAsString())<endif>
>>