// description: ICustomConditionFactory check

import {
  ICustomConditionFactory,
  ICustomCondition,
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/a12internal";

function test(factory: MyCustomConditionFactory) {
  factory.createCustomConditionV2("Test");
}

class MyCustomConditionFactory implements ICustomConditionFactory {
  createCustomConditionV2(_customConditionName: string): ICustomCondition | null {
    return null;
  }
}
