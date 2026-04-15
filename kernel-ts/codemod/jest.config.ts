import type { Config } from "jest";

const config: Config = {
	preset: "ts-jest",
	testMatch: ["<rootDir>/src/**/*.test.ts"],
	collectCoverage: true, // Set this to false for debugging
	collectCoverageFrom: ["src/**/*.ts", "!**/__tests__/**"],
	coverageReporters: ["json", "text", "text-summary"],
	reporters: [
		"default",
		"jest-junit"
	],
    snapshotSerializers: [
        '<rootDir>/src/snapshotSerializer.ts',
    ],
};

export default config;
