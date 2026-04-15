// description: ICustomFieldType -> ICustomFieldValidator (@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/custom)

import { type ICustomFieldType } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/custom/ICustomFieldType.js";
import { type ICustomFieldTypeCheckError } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/custom/ICustomFieldTypeCheckError.js";
import { type ICustomFieldTypeConversionResult } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/custom/ICustomFieldTypeConversionResult.js";
import { type ICustomFieldTypeValidationParam } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/custom/ICustomFieldTypeValidationParam.js";

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