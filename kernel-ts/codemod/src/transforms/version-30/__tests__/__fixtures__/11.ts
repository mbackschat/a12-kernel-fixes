// description: Deprecation of DocumentRtCustomExtensionService.registerCustomFieldTypes
import { DocumentRtCustomExtensionService } from "@com.mgmtp.a12.kernel/kernel-md-facade/lib/main/js/api.js";


import { customFieldTypeFactory } from "./custom-field-types.js";

export function registerDevappCustomizations(): void {
  DocumentRtCustomExtensionService.registerCustomFieldTypes(customFieldTypeFactory);
}