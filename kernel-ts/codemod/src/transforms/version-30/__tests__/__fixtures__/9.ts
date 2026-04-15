// description: Deprecation of RtCustomExtensionService.registerCustomFieldTypes

import { RtCustomExtensionService } from "@com.mgmtp.a12.kernel/kernel-md-facade/lib/main/js/a12internal/RtCustomExtensionService.js";

import { PrintDateTimeFormatFactory } from "../../internal/validation/print-date-time-format-type/index.js";

RtCustomExtensionService.registerCustomFieldTypes(new PrintDateTimeFormatFactory());

