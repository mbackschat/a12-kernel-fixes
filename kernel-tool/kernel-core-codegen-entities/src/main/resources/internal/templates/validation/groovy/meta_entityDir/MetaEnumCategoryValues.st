MetaEnumCategoryValues(t) ::= <<
enumerationCategoryValues.put(<t.gemeinsamesFormatNummer>, new MapBuilder\<String, Map\<String, String\>\>()<if(t.notEmpty)>.put(<t.categoryValueMappingCode; separator = ").put(">)<endif>.build());

>>