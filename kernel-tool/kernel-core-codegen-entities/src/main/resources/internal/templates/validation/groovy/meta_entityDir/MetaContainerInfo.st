MetaContainerInfo(t) ::= <<
	result[<t.containerIndex>] = new Container_t("<t.name>", <t.maxIndex>, [<t.indexFieldsCode:{ value | "<value>", }>] as String[], <t.additionalValuesCode>, <t.internalAdditionalKeysCode>);
>>