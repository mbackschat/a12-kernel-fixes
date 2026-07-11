CodeGenTypescriptDefCode(t) ::= <<
import { IMetaModel } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { ICalculator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";
import { IValidator } from "@com.mgmtp.a12.kernel/kernel-core-runtime-api-ts/a12internal";

export interface GeneratedCodeScript {
	// eslint-disable-next-line @typescript-eslint/ban-types
	(rta: object, rt: object): {
		validator: {
			getInstance(): IValidator & ICalculator;
		};
		meta: {
			getInstance(): IMetaModel;
		};
	};
}

declare const <t.generatedCodeId>: GeneratedCodeScript;
export default <t.generatedCodeId>;
>>