MetaRegelFehlertexte(t) ::= <<
// Init Daten zur Regel '<t.regelFullName>'
<t.fehlertexteCode>
this._fehlertexteParameterMapping.set(<t.regelNummer>, new MapBuilder()<if(!t.parameterMappingCode.empty)
>.put(<t.parameterMappingCode; separator = ").put(">)<endif>.build());
>>