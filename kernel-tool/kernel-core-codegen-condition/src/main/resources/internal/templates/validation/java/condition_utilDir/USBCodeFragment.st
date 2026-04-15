USBCodeFragment(t) ::= <<
<if(t.isOne)>1<elseif(t.isLargerThanOne)>controller.getValidationCache().getMaxUsbMitAngegebenenFeldern(usbRootGroups)<else>-2<endif>
>>