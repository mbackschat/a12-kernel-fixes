// description: Deprecation of CustomFieldTypeService (a12internal)
import { CustomFieldTypeService } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/custom/impl/CustomFieldTypeService.js";


import { customFieldTypeFactory } from "./custom-field-type.js";

export function registerDevappCustomizations(): void {
  CustomFieldTypeService.getInstance().register(customFieldTypeFactory);
}