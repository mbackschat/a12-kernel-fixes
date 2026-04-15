MetaEnumCategoryValues(t) ::= <<
this._enumerationCategoryValues.set(<t.gemeinsamesFormatNummer>, new MapBuilder()<if(t.notEmpty)>.put(<t.categoryValueMappingCode; separator = ").put(">)<endif>.build());

>>