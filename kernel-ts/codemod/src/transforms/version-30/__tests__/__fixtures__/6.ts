// description: import from sub-modules (lib)

import { arraysEqual } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/a12internal/CollectionsWrapper";
import { de, en, fr, nl } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/a12internal/formatdef/error/bundles";
import {
	DIVISION_BY_ZERO_INVALID,
	ERROR_DURING_DATA_CHECK_EVAL,
	RuntimeFormalErrorEnum
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/a12internal/formatdef/error/keys";
import {
	defaultResourceProvider,
	ErrorLocalizationBundle,
	StaticResourceProvider
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/a12internal/formatdef/error/localization";

function test() {
	de.set(RuntimeFormalErrorEnum.COMMA_PLURAL, "Kommas");
	en.set(RuntimeFormalErrorEnum.COMMA_PLURAL, "Kommas");
	fr.set(RuntimeFormalErrorEnum.COMMA_PLURAL, "Kommas");
	nl.set(RuntimeFormalErrorEnum.COMMA_PLURAL, "Kommas");
	const es = new ErrorLocalizationBundle("es");
	es.set(RuntimeFormalErrorEnum.COMMA_PLURAL, "Kommas");

	const customResourceProvider = new StaticResourceProvider(de, en);
	customResourceProvider.gettext(`kernel-formalErrors.${DIVISION_BY_ZERO_INVALID}`, "es");
	defaultResourceProvider.gettext(`kernel-formalErrors.${ERROR_DURING_DATA_CHECK_EVAL}`, "es");

	arraysEqual([1, 2], [3, 4]); // unrelated, import should not change
}