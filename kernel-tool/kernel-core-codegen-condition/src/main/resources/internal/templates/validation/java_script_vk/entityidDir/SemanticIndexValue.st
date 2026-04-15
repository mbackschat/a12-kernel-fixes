SemanticIndexValue(t) ::= <<
<if(t.semIdxConstant)><t.semanticIndexCode><else>controller.feldWert(idx, <t.semanticIndexCode>).wert<endif>
>>