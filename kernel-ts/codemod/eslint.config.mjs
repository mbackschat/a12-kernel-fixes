import { strict as devtoolsConfigsStrict } from "@com.mgmtp.a12.devtools/eslint-config";
import tsEslint from "typescript-eslint";
import noticePlugin from "eslint-plugin-notice";
import { defineConfig } from "eslint/config";

export default defineConfig(
	...devtoolsConfigsStrict,
	tsEslint.configs.recommended,
	{
		ignores: ["**/__tests__/__fixtures__/*.ts"]
	},
	{
		name: "kernel/general",
		plugins: {
			notice: noticePlugin
		},
		languageOptions: {
			ecmaVersion: 5,
			sourceType: "script",

			parserOptions: {
				project: "./tsconfig.json"
			}
		},
		rules: {
			"@typescript-eslint/no-namespace": "off",
			"@typescript-eslint/no-empty-function": "off",
			"@typescript-eslint/no-non-null-assertion": "off",
			"no-inner-declarations": "off",
			"no-console": "error",

			"notice/notice": [
				"error",
				{
					mustMatch: "Copyright \\(c\\) 2012-2026 mgm technology partners GmbH",
					templateFile: "scripts/copyright.js"
				}
			]
		}
	}
);
