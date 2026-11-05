package com.mgmtp.a12.kernel.gradle

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * Creates the tsconfig used for the SonarQube TypeScript analysis.
 *
 * The analyzer bundles its own TypeScript compiler, which does not yet support the "ES2025" target/lib
 * configured in @com.mgmtp.a12.devtools/tsconfig. A config that simply "extends" the project's tsconfig.json and
 * overrides target/lib does not work: TypeScript validates every file of an "extends" chain on its own and
 * reports the unknown "ES2025" values of the base config no matter what a derived config overrides. So the
 * analyzer gets a config without "extends" instead: the chain as resolved by `tsc --showConfig`, with target/lib
 * pinned back to "ES2024".
 */
class SonarTsConfigUtils {

    /** the newest target/lib understood by the TypeScript compiler bundled with the analyzer */
    private static final String SUPPORTED_ES_VERSION = "ES2024"

    /**
     * Reads the tsconfig resolved by `tsc --showConfig` and writes the tsconfig for the analysis.
     *
     * @param projectPath the project directory (with '/' separators); the paths of the resolved config are
     *                    relative to it, while the written config is not
     */
    static void writeSonarTsConfig(File resolvedTsConfig, File sonarTsConfig, String projectPath) {
        def config = new JsonSlurper().parse(resolvedTsConfig)

        config.compilerOptions.target = SUPPORTED_ES_VERSION
        if (config.compilerOptions.lib != null) {
            config.compilerOptions.lib = config.compilerOptions.lib
                    .collect { it.toLowerCase().startsWith("es2025") ? SUPPORTED_ES_VERSION : it }
                    .unique()
        }

        // nothing is emitted during the analysis
        config.compilerOptions.remove("outDir")
        config.compilerOptions.remove("rootDir")

        // --showConfig also expands the "include" globs into a "files" list; keep the globs, so that sources
        // generated later in the build (e.g. the ANTLR sources of kernel-core-parser-web) are analyzed as well
        if (config.include != null) {
            config.remove("files")
        }

        ["files", "include", "exclude"].findAll { config[it] != null }.each { key ->
            config[key] = config[key].collect { String path ->
                // the paths may be globs like "./src/main/js/**/*", so no File/canonicalPath here
                path.startsWith("./") ? projectPath + "/" + path.substring(2) : path
            }
        }

        sonarTsConfig.text = JsonOutput.prettyPrint(JsonOutput.toJson(config))
    }
}
