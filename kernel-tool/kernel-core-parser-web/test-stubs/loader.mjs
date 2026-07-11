/**
 * Custom ESM loader for the Node.js test environment.
 * Stubs browser-only imports that cannot run in Node.js.
 */

const CSS_STUB = "data:text/javascript,";

export async function resolve(specifier, context, nextResolve) {
    // Stub CSS file imports with an empty module
    if (specifier.endsWith(".css")) {
        return { shortCircuit: true, url: CSS_STUB };
    }

    // Stub monaco-editor (and sub-path imports like "monaco-editor/esm/..."): browser-only.
    if (specifier === "monaco-editor" || specifier.startsWith("monaco-editor/")) {
        return {
            shortCircuit: true,
            url: new URL("./monaco-editor.mjs", import.meta.url).href,
        };
    }

    // Stub @monaco-editor/react (and any sub-packages): browser-only React wrapper.
    if (specifier === "@monaco-editor/react" || specifier.startsWith("@monaco-editor/")) {
        return {
            shortCircuit: true,
            url: new URL("./monaco-editor-react.mjs", import.meta.url).href,
        };
    }

    return nextResolve(specifier, context);
}

export async function load(url, context, nextLoad) {
    if (url === CSS_STUB) {
        return { shortCircuit: true, format: "module", source: "" };
    }
    return nextLoad(url, context);
}
