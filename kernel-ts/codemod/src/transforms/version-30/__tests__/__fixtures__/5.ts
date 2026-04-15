// description: imports from @com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/a12internal

import {
	arraysEqual,
	de,
	defaultResourceProvider,
	DIVISION_BY_ZERO_INVALID,
	en,
	ERROR_DURING_DATA_CHECK_EVAL,
	ErrorLocalizationBundle,
	fr,
	nl,
	RuntimeFormalErrorEnum,
	StaticResourceProvider
} from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/lib/main/js/a12internal";

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