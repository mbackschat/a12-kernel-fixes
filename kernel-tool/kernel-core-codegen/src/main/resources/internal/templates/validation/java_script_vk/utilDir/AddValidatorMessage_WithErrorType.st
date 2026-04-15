AddValidatorMessage_WithErrorType(t) ::= <<
<if(t.repetitionNotUniqueRule)>
var repetition = idx.getIndexes();
var erroneousRepetitions = valErg.fehlerTyp === ErrorType.VALUE_ERROR ? repNotUnqErrorCache.getErroneousRepetitionsWithValueError(repetition) : repNotUnqErrorCache.getErroneousRepetitionsWithOmissionsError(repetition);
controller.addValidatorMessageWithReplacement4RepetitionNotUnique("<t.uniqueErrorFieldName>", idx, <t.errorCode>, valErg.fehlerTyp, erroneousRepetitions);
<else>
controller.addValidatorMessage("<t.uniqueErrorFieldName>", idx, <t.errorCode>, valErg.fehlerTyp);
<endif>
>>