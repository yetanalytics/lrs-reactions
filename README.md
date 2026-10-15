# lrs-reactions

Shared specs and code for LRS Reactions

## Usage

Run the project's tests:

    $ clojure -T:build test

Run the project's CI pipeline and build a JAR (this will fail until you edit the tests to pass):

    $ clojure -T:build ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the JAR in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

Install it locally (requires the `ci` task be run first):

    $ clojure -T:build install

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment
variables (requires the `ci` task be run first):

    $ clojure -T:build deploy

Your library will be deployed to com.yetanalytics/lrs-reactions on clojars.org by default.

## Dependency vulnerability tracking

The `Deps` workflow regenerates the root `pom.xml` from `deps.edn` and submits
the Maven dependency tree to GitHub's dependency graph on pushes to `main`.
It can also be run manually. Dependabot uses this graph to check for known
vulnerabilities.

Run `make pom.xml` to regenerate the submission POM locally, retaining its
project metadata and excluding local user dependency configuration. The root
POM is tracked, so local generation can leave changes in the working tree.
The workflow submits this root POM; the build's POM under `target/classes` is
used separately for packaging.

The snapshot is associated with `deps.edn` and covers the base Clojure and
`xapi-schema` dependencies and their transitive Maven dependencies. The `:build`
and `:test` aliases are excluded, including deployment/build tools, `test.check`,
and the Git-based Cognitect test runner. These require separate coverage.

## License

Copyright © 2023-2025 Yet Analytics, Inc.

Distributed under the Apache License version 2.0.
