<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/dark/A12-Dark.svg" />
  <img src="https://www.mgm-tp.com/global-content/cd/logos/a12/app-icons/light/A12-Light.svg" height="200" alt="A12 logo" />
</picture>

# A12 Kernel - Model-Based Plausibility Checks and Computation for Complex Forms

A12 Kernel provides a Domain-Specific Language (DSL) that enables business
domain experts to model complex interdependencies within large forms. 
A12 Kernel compiles these models into code (generally available targets are
Java and TypeScript) that can be used by other A12 components or accessed
through standalone kernel runtime APIs to perform plausibility checks and
produce helpful, user-facing error messages.

When user input fails a plausibility check, error messages are generated based
on template strings that are specified by the business domain experts, with
dynamically substituted template parameters and automatically computed links to
the form fields involved in the unsatisfied rule. A12 Kernel also supports
automatic computations for derived form fields, based on formulas that are
expressed in the DSL.

Beyond the core DSL, the kernel repository includes APIs and implementations for
A12 DocumentModels and A12 Documents. It also provides model-based mechanisms
for DocumentModel reuse and composability (e.g. CombinationModel), support for
model-driven data import and migration (MappingModel), and associated utilities
like serialization.

Refer to https://geta12.com/#/docs to get started with A12 development

## License
Parts of the A12 platform are made available under a **dual license**.  
Please check the [LICENSE](./LICENSE) file for details.

## Getting Started

### How to Use It

#### Import & Install

The A12 Kernel provides library entrypoints:
* Java
  * include `com.mgmtp.a12.kernel:kernel-md-facade` in your gradle/maven config
* TypeScript
  * include `@com.mgmtp.a12.kernel/kernel-md-facade` in your package.json (or similar)

### How to Build and Run

#### Prerequisites

The following tools are required in order to build this repository:

| Tool   | Version |
|--------|:-------:|
| [JDK]  |  `21`   |
| [Node] |  `22`   |
| [npm]  |  `10`   |


#### How to Build

This repository uses [Gradle] as the leading build tool.
Configure your build environment with the relevant [Gradle repository settings].

The following command executes the build from a terminal, for example [Git Bash]:

```sh
$ gradle build
```

This builds and tests the kernel (Java and TypeScript).

#### How to Test

The recommended task/command

```sh
# Only TypeScript components:
gradle npm_test
# Only Java modules:
gradle check -x npm_test
```

#### How to Run

The A12 Kernel does not provide a standalone application.

#### How to Access It

---

### Documentation
- Full technical documentation is available at [GetA12.com](https://GetA12.com).
- The website also provides access to the **A12 Community Discussion Forum**.
- And shows examples for provided CLIs.

---

**The mgm A12 Team**
[mgm technology partners GmbH](https://www.mgm-tp.com) • [Imprint](https://www.mgm-tp.com/imprint.html)


<!--- References--->

[Git Bash]: https://git-scm.com/

[Gradle]: https://docs.gradle.org/current/userguide/userguide.html

[Gradle repository settings]: https://docs.gradle.org/current/userguide/declaring_repositories.html#sub:declaring-custom-repository-basics

[JDK]: https://www.oracle.com/technetwork/java/javase/overview/index.html

[Node]: https://nodejs.org/en/docs/

[npm]: https://docs.npmjs.com/
