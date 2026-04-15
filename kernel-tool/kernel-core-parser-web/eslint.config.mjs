import { defineConfig } from "eslint/config";

import kernelConfig from "@com.mgmtp.a12.kernel/kernel-eslint-config"

export default defineConfig(
    ...kernelConfig,
    {
        ignores: ["src/main/js/internal/gen/*.ts"]
    },
)