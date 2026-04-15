ValueListCondition(t) ::= <<
controller.<t.bedingungNameCode>(idx, <t.feldIdCode>, [<t.OpCodeFragments; separator=", ">] <if(t.stringList)>as Object[]<else>as VkBigDecimal[]<endif>)
>>