import { strict }  from "@com.mgmtp.a12.devtools/eslint-config";
import tsEslint from "typescript-eslint";
import noNullPlugin from "eslint-plugin-no-null";
import noticePlugin from "eslint-plugin-notice";
import preferArrowPlugin from "eslint-plugin-prefer-arrow";

export default tsEslint.config(
  ...strict,
  {
    name: "kernel/general",
    plugins: {
      noNull: noNullPlugin,
      preferArrow: preferArrowPlugin,
    },
    settings: {
      "import/internal-regex": "^@com.mgmtp.a12.kernel/",
    },
    rules: {
      "@typescript-eslint/consistent-type-exports": [
        "error",
        { fixMixedExportsWithInlineTypeSpecifier: false },
      ],
      "@typescript-eslint/consistent-type-imports": "error",
      "no-control-regex": "off",
      "no-unused-vars": "off",
      "@typescript-eslint/no-extraneous-class": "off",
      "@typescript-eslint/no-unused-vars": [
        "error",
        // allow variable names starting with '_'
        {
          argsIgnorePattern: "^_",
          varsIgnorePattern: "^_",
          caughtErrorsIgnorePattern: "^_",
        },
      ],
      "react/prop-types": "off",
      "@typescript-eslint/no-explicit-any": "off",

      "@typescript-eslint/no-namespace": [
        "error",
        {
          allowDeclarations: true,
        },
      ],

      "import/no-relative-packages": "error",
      "no-multiple-empty-lines": "error",

      "import/order": [
        "error",
        {
          groups: [
            "builtin",
            "external",
            "internal",
            "parent",
            "sibling",
            "index",
          ],

          pathGroups: [
            {
              pattern: "../**",
              group: "parent",
              position: "after",
            },
          ],

          "newlines-between": "always-and-inside-groups",
          pathGroupsExcludedImportTypes: ["builtin"],
        },
      ],
    },
  },
  {
    ignores: ["**/__tests__/__fixtures__/*.ts"],
  },
  {
    files: ["**/*.ts"],
    plugins: {
      notice: noticePlugin,
      copyrightHeader: {
        rules: {
          "includes-header-once": {
            create(context) {
              const sourceCode = context.sourceCode ?? context.getSourceCode();
              return {
                Program(node) {
                  const comments = sourceCode.getAllComments();
                  const copyrightComments = comments.filter((c) =>
                    c.value.includes("Copyright (c)"),
                  );
                  if (copyrightComments.length > 1) {
                    return context.report({
                      node,
                      message: "Copyright header is included more than once!",
                    });
                  }
                },
              };
            },
          },
        },
      },
    },
    rules: {
      "notice/notice": [
        "error",
        {
          chars: 2000,
          template: [
            "/*",
            " * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial",
            " *",
            " * Copyright (c) 2012-2026 mgm technology partners GmbH",
            " *",
            " * Dual License",
            " * ------------",
            " * This source file is part of the mgm A12 Platform and available under",
            " * a choice of two different licenses:",
            " *",
            " * 1. Open-Source License – EUPL v1.2",
            " *    You may redistribute and/or modify this file under the terms of the",
            " *    European Union Public License, version 1.2 - see https://eupl.eu/.",
            " *",
            " * 2. Commercial License",
            " *    Alternatively, you may obtain a commercial license from",
            " *    mgm technology partners GmbH, that permits use of this software",
            " *    under different terms (including support and maintenance services).",
            " *",
            " *    Please contact a12-license@mgm-tp.com for more information.",
            " *",
            " * You must select and comply with exactly one of the above license options.",
            " *",
            " * Warranty Disclaimer (applies to either option)",
            " * ----------------------------------------------",
            " * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,",
            " * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES",
            " * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND",
            " * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE",
            " * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.",
            " */",
          ].join("\n"),
          messages: {
            whenFailedToMatch: "Copyright header is incorrect or missing",
          },
          onNonMatchingHeader: "replace",
        },
      ],
      "copyrightHeader/includes-header-once": "error",
    },
  },
  {
    languageOptions: {
      parserOptions: {
        projectService: true,
        tsconfigRootDir: import.meta.dirname,
      },
    },
  },
);
