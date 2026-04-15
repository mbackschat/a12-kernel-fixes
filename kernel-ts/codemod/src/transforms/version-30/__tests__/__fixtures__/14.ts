// description: Deprecation of CustomConditionService (a12internal)
import type {
  ICustomCondition,
  ICustomConditionFactory
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/a12internal";
import { CustomConditionService } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/a12internal";

const alwaysValidCustomCondition: ICustomCondition = {
  check: () => false
};

const customConditionFactory: ICustomConditionFactory = {
  createCustomCondition: () => alwaysValidCustomCondition
};

/**
 * Registers a custom condition that matches all possible custom conditions and is always valid.
 */
export function registerCustomConditions(): void {
  CustomConditionService.getInstance().register(customConditionFactory);
}