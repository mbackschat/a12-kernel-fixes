AddValidatorMessage_WithErrorType(t) ::= <<
<if(t.repetitionNotUniqueRule)>
LongArray repetition = new LongArray(idx.getIndexes());
Set\<LongArray\> erroneousRepetitions = valErg.getFehlerTyp() == ErrorType.VALUE_ERROR ? repNotUnqErrorCache.getErroneousRepetitionsWithValueError(repetition) : repNotUnqErrorCache.getErroneousRepetitionsWithOmissionsError(repetition);
controller.addValidatorMessageWithReplacement4RepetitionNotUnique("<t.uniqueErrorFieldName>", idx, <t.errorCode>, valErg.getFehlerTyp(), erroneousRepetitions);
<else>
controller.addValidatorMessage("<t.uniqueErrorFieldName>", idx, <t.errorCode>, valErg.getFehlerTyp());
<endif>
>>