// description: ICustomFieldType -> ICustomFieldValidator (@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js//index.js)

import {
  type ICustomFieldType,
  type ICustomFieldTypeCheckError,
  type ICustomFieldTypeConversionResult,
  type ICustomFieldTypeValidationParam
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js//index.js";

export class EmailType implements ICustomFieldType {
  regex = /^\S+@\S+$/;

  convertDisplay2Internal(displayValue: string): ICustomFieldTypeConversionResult {
    return {
      getConvertedValue: () => displayValue,
      getErrorMessage: () => undefined
    };
  }

  convertInternal2Display(internalValue: string): ICustomFieldTypeConversionResult {
    return {
      getConvertedValue: () => internalValue,
      getErrorMessage: () => undefined
    };
  }

  validate(value: string, _valParam: ICustomFieldTypeValidationParam): ICustomFieldTypeCheckError | undefined {
    if (!this.regex.test(value)) {
      return {
        getErrorMessage: () => {
          return [
            {
              key: "error.email.invalid",
              defaults: {
                de: "ungültiges Email-Format",
                en: "Invalid email format"
              }
            }
          ];
        },
        getErrorKey: () => "error.email.invalid"
      };
    }
    return undefined;
  }
}