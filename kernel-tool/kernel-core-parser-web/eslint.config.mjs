import tsEslint from "typescript-eslint";

import kernelConfig from "@com.mgmtp.a12.kernel/kernel-eslint-config"

export default tsEslint.config(
    ...kernelConfig,
    {
        ignores: ["src/main/js/internal/gen/*.ts"]
    },
)