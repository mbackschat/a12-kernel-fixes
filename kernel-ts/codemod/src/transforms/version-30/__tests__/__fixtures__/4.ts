// description: ICustomFieldTypeFactory check

import {
  ICustomFieldTypeFactory,
  ICustomFieldType,
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/";

function test(factory: MyCustomFieldTypeFactory) {
  factory.createCustomFieldTypeV2("Test");
}

// class comment
class MyCustomFieldTypeFactory implements ICustomFieldTypeFactory {
  createCustomFieldTypeV2(_customFieldTypeName: string): ICustomFieldType | null {
    return null;
  }
}
