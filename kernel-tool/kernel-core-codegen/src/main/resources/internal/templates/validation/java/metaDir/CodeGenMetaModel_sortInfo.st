CodeGenMetaModel_sortInfo(t) ::= <<
<if(t.withSortInfo)>new MapBuilder\<String, List\<Pair\<String, SortOrder\>\>\>().put(<t.sortInfoCodeFragments; separator=").put(">).build()<else>null<endif>
>>