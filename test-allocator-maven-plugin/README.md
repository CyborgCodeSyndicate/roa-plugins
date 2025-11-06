# test-allocator-maven-plugin

## Table of contents

- [Overview](#overview)
- [Quick start](#quick-start)
- [Goal guide](#goal-guide)
  - [Mojo goal](#mojo-goal)
  - [Execution flow](#execution-flow)
- [Configuration reference](#configuration-reference)
  - [Common parameters](#common-parameters)
  - [JUnit-specific options](#junit-specific-options)
  - [TestNG-specific options](#testng-specific-options)
- [Output format](#output-format)
- [Pipeline integration](#pipeline-integration)
- [Troubleshooting](#troubleshooting)
- [Author](#author)

## Overview

`test-allocator-maven-plugin` splits compiled test classes into balanced execution buckets before the Maven `test` phase. It supports JUnit Jupiter tags and TestNG suite selection, respects sequential-versus-parallel hints, and writes a JSON manifest that CI systems can distribute across runners.

Use it when you need predictable, repeatable load balancing of large suites without rewriting build logic or scripting class discovery by hand.

## Quick start

Invoke the goal directly (replace the version with the one published by your BOM or parent):

```bash
mvn io.cyborgcode.roa.plugins:test-allocator-maven-plugin:1.0.3:split \
  -DtestSplitter.enabled=true \
  -DtestSplitter.test.engine=junit \
  -DtestSplitter.junit.tags.include=fast,smoke \
  -DtestSplitter.junit.tags.exclude=flaky \
  -DtestSplitter.maxMethods=30 \
  -DtestSplitter.max.number.runners=8 \
  -DtestSplitter.parallel.methods=true \
  -DtestSplitter.json.output=ci/grouped-tests
```

Or wire it into your `pom.xml` so it runs every build:

```xml
<plugin>
  <groupId>io.cyborgcode.roa.plugins</groupId>
  <artifactId>test-allocator-maven-plugin</artifactId>
  <version>1.0.3</version>
  <executions>
    <execution>
      <goals>
        <goal>split</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <enabled>true</enabled>
    <testEngine>junit</testEngine>
    <maxMethods>30</maxMethods>
    <parallelMethods>true</parallelMethods>
    <maxNumberOfParallelRunners>8</maxNumberOfParallelRunners>
    <jsonOutput>ci/grouped-tests</jsonOutput>
    <junit>
      <tags>
        <include>fast,smoke</include>
        <exclude>flaky</exclude>
      </tags>
    </junit>
  </configuration>
</plugin>
```

Switch `testEngine` to `testng` and provide `<suites>` when running TestNG suites.

## Goal guide

| Component | Highlights |
| --- | --- |
| `TestAllocatorMojo` | Captures configuration parameters, builds the appropriate `TestSplitterConfiguration`, and delegates to the matching allocator service. |
| `BaseAllocatorService` | Discovers compiled test classes, calculates per-class method weights, groups them via `TestBucketAllocator`, and writes the JSON manifest. |
| `JunitAllocatorService` | Counts `@Test` methods filtered by include/exclude tags, collapsing sequential classes to a single slot when `parallel.methods` is disabled. |
| `TestNgAllocatorService` | Parses TestNG XML suites, honours `<include>` directives, and respects the parallel-by-methods policy. |
| `TestBucketAllocator` | Greedy packs classes into buckets up to `maxMethods`, isolating heavy classes automatically. |
| `TestClassLoader` | Builds a URLClassLoader from Maven’s compile and test classpaths so reflection can inspect compiled test classes safely. |

### Mojo goal

- **GOAL**: `test-splitter:split`
- **Lifecycle**: Binds to `test-compile` by default.
- **Coordinates**: `io.cyborgcode.roa.plugins:test-allocator-maven-plugin:1.0.3`

### Execution flow

1. Read plugin parameters into a `TestSplitterConfiguration`.
2. Walk `${project.build.testOutputDirectory}` for `.class` files.
3. Load each candidate via the custom test classloader.
4. Count executable methods, applying tag or suite filters.
5. Distribute classes into buckets based on `maxMethods` and `max.number.runners`.
6. Write `<json.output>.json` so downstream jobs can consume it.

## Configuration reference

### Common parameters

| Property | Default | Description |
| --- | --- | --- |
| `testSplitter.enabled` | `false` | Master switch. When `false`, the goal logs and exits. |
| `testSplitter.test.engine` | `junit` | `junit` or `testng`; selects which allocator service to use. |
| `testSplitter.maxMethods` | `20` | Greedy bucket size. Classes over the limit become single-class buckets. |
| `testSplitter.max.number.runners` | `20` | If the class count is ≤ value, each class becomes its own bucket. |
| `testSplitter.parallel.methods` | `true` | When `false`, each class contributes `1` even if it has many methods. |
| `testSplitter.json.output` | `grouped-tests` | Output path stem; `.json` is appended automatically. Relative paths are allowed. |

### JUnit-specific options

| Property | Default | Description |
| --- | --- | --- |
| `testSplitter.junit.tags.include` | — | Comma-separated JUnit 5 tags to include. Empty means “all tags”. |
| `testSplitter.junit.tags.exclude` | — | Comma-separated tags to skip before applying includes. |

### TestNG-specific options

| Property | Default | Description |
| --- | --- | --- |
| `testSplitter.testng.suites` | — | Comma-separated suite names to match across all TestNG XML files under the project root. |

## Output format

```json
[
  {
    "jobIndex": 0,
    "classes": [
      "com.example.FastTest",
      "com.example.MoreFastTests"
    ],
    "totalMethods": 34
  },
  {
    "jobIndex": 1,
    "classes": [
      "com.example.SlowSuite"
    ],
    "totalMethods": 12
  }
]
```

`jobIndex` is the sequential bucket id, `classes` lists fully qualified class names, and `totalMethods` is the cumulative weight used during balancing.

## Pipeline integration

```bash
BUCKET_INDEX="${CI_NODE_INDEX}"
CLASSES=$(jq -r ".[${BUCKET_INDEX}].classes | join(',')" ci/grouped-tests.json)

mvn \
  -Dtest="${CLASSES}" \
  -DfailIfNoTests=false \
  test
```

For TestNG, feed the class list into the TestNG runner, or generate suite XMLs dynamically per bucket.

## Troubleshooting

- **Empty JSON**: Ensure `testSplitter.enabled=true`, the chosen engine matches your framework, and tests are compiled before the goal runs.
- **Missing classes**: Confirm `${project.build.testOutputDirectory}` points to compiled tests and that dependencies are resolved (the classloader pulls from Maven classpaths).
- **TestNG suites ignored**: Double-check suite names; every `.xml` file under the project root is parsed, so misnamed suites will be skipped.
- **Unexpected single-slot classes**: Classes are collapsed when `parallel.methods=false` or the class extends a sequential base (name contains `BaseTestSequential`).

## Author
**Cyborg Code Syndicate 💍👨💻**
