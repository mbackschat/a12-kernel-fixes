// description: Deprecation of DocumentRtCustomExtensionService.registerCustomConditions
import { DocumentRtCustomExtensionService } from "@com.mgmtp.a12.kernel/kernel-md-facade/lib/main/js/api.js";


import { customConditionsFactory } from "./custom-conditions.js";

export function registerDevappCustomizations(): void {
  DocumentRtCustomExtensionService.registerCustomConditions(customConditionsFactory);
}