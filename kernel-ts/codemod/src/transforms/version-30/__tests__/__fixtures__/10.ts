// description: Deprecation of RtCustomExtensionService.registerCustomConditions
import {
  getCodeEditorErrors,
  getValidationState,
  ValidationStatus
} from "@com.mgmtp.a12.antlrcodeeditor/antlrcodeeditor-core";
import { ICustomCondition } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/a12internal/index.js";
import { RtCustomExtensionService } from "@com.mgmtp.a12.kernel/kernel-md-facade/lib/main/js/a12internal/RtCustomExtensionService.js";


export function registerCustomConditions() {
  const conditionsMap: Record<string, ICustomCondition> = {
    CodeEditorCondition: {
      check(data, relevantEntities, formallyIncorrectFields, errorField) {
        const fieldContent = data.getValue(errorField);
        if (fieldContent) {
          const errors = getCodeEditorErrors("example");
          return errors.length > 0;
        }

        return false;
      }
    },
    CodeEditorPendingValidation: {
      check(data, relevantEntities, formallyIncorrectFields, errorField) {
        const fieldContent = data.getValue(errorField);
        if (fieldContent) {
          const pendingValidations = getValidationState({
            owner: "example",
            status: ValidationStatus.CHECKING
          });
          return pendingValidations.length > 0;
        }

        return false;
      }
    },
    ProjectConditionWarning: {
      check(data, relevantEntities, formallyIncorrectFields, errorField) {
        const fieldContent = data.getValue(errorField);
        if (fieldContent) {
          const regex = ".*warning.*";
          return new RegExp(regex, "m").test(fieldContent);
        }

        return false;
      }
    },
    ProjectConditionError: {
      check(data, relevantEntities, formallyIncorrectFields, errorField) {
        const fieldContent = data.getValue(errorField);
        if (fieldContent) {
          const regex = ".*error.*";
          return new RegExp(regex, "m").test(fieldContent);
        }

        return false;
      }
    }
  };

  RtCustomExtensionService.registerCustomConditions({
    createCustomCondition(name: string) {
      return conditionsMap[name];
    }
  });
}