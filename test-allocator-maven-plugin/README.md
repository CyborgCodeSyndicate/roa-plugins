# test-allocator-maven-plugin
![JDK](https://img.shields.io/badge/JDK-17+-blue)
![Maven](https://img.shields.io/badge/Maven-3.6+-blue)

## Table of contents

- [Overview](#overview)
- [Quick start](#quick-start)
- [Package structure](#package-structure)
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
mvn io.cyborgcode.roa.plugins:test-allocator-maven-plugin:RELEASE:split \
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
  <version>RELEASE</version>
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

## Package Structure

<details>
 <summary>Package overview</summary>

| Package | Purpose                                                                                                                                                                            |
| --- |------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `io.cyborgcode.roa.maven.plugins.allocator` | Plugin entry point (`TestAllocatorMojo`)                                                                                                                                           |
| `io.cyborgcode.roa.maven.plugins.allocator.config` | Configuration classes for JUnit/TestNG (`TestSplitterConfiguration`, `TestSplitterConfigurationJunit`, `TestSplitterConfigurationTestng`)                                                                                                                                           |
| `io.cyborgcode.roa.maven.plugins.allocator.service` | Test allocation services that distribute test classes across execution groups. (`BaseAllocatorService`, `JunitAllocatorService`, `TestNgAllocatorService`, `TestAllocatorService`) |
| `io.cyborgcode.roa.maven.plugins.allocator.grouping` | Bucket allocation logic (`TestBucketAllocator`, `TestBucket`)                                                                                                                      |
| `io.cyborgcode.roa.maven.plugins.allocator.discovery` | Custom utility classes loading test classes and file discovery (`TestClassLoader`, `ClassFileDiscovery`)                                                                           |
| `io.cyborgcode.roa.maven.plugins.allocator.filtering` | JUnit tag filtering and tag extracting (`TestMethodFilter`, `TestTagExtractor`)                                                                                                    |

</details>

## Goal guide

<details>
 <summary>Component overview</summary>

| Component | Highlights |
| --- | --- |
| `TestAllocatorMojo` | Captures configuration parameters, builds the appropriate `TestSplitterConfiguration`, and delegates to the matching allocator service. |
| `BaseAllocatorService` | Discovers compiled test classes, calculates per-class method weights, groups them via `TestBucketAllocator`, and writes the JSON manifest. |
| `JunitAllocatorService` | Counts `@Test` methods filtered by include/exclude tags, collapsing sequential classes to a single slot when `parallel.methods` is disabled. |
| `TestNgAllocatorService` | Parses TestNG XML suites, honours `<include>` directives, and respects the parallel-by-methods policy. |
| `TestBucketAllocator` | Greedy packs classes into buckets up to `maxMethods`, isolating heavy classes automatically. |
| `TestClassLoader` | Builds a URLClassLoader from Maven's compile and test classpaths so reflection can inspect compiled test classes safely. |

</details>

<details>
 <summary>Mojo goal</summary>

- **GOAL**: `test-splitter:split`
- **Lifecycle**: Binds to `test-compile` by default.
- **Coordinates**: `io.cyborgcode.roa.plugins:test-allocator-maven-plugin:1.0.3`

</details>

<details>
 <summary>Execution flow</summary>

1. Read plugin parameters into a `TestSplitterConfiguration`.
2. Walk `${project.build.testOutputDirectory}` for `.class` files.
3. Load each candidate via the custom test classloader.
4. Count executable methods, applying tag or suite filters.
5. Distribute classes into buckets based on `maxMethods` and `max.number.runners`.
6. Write `<json.output>.json` so downstream jobs can consume it.

</details>

## Configuration reference

<details>
 <summary>Common parameters</summary>

| Property | Default | Description |
| --- | --- | --- |
| `testSplitter.enabled` | `false` | Master switch. When `false`, the goal logs and exits. |
| `testSplitter.test.engine` | `junit` | `junit` or `testng`; selects which allocator service to use. |
| `testSplitter.maxMethods` | `20` | Greedy bucket size. Classes over the limit become single-class buckets. |
| `testSplitter.max.number.runners` | `20` | If the class count is ≤ value, each class becomes its own bucket. |
| `testSplitter.parallel.methods` | `true` | When `false`, each class contributes `1` even if it has many methods. |
| `testSplitter.json.output` | `grouped-tests` | Output path stem; `.json` is appended automatically. Relative paths are allowed. |

</details>

<details>
 <summary>JUnit-specific options</summary>

| Property | Default | Description |
| --- | --- | --- |
| `testSplitter.junit.tags.include` | — | Comma-separated JUnit 5 tags to include. Empty means “all tags”. |
| `testSplitter.junit.tags.exclude` | — | Comma-separated tags to skip before applying includes. |

</details>

<details>
 <summary>TestNG-specific options</summary>

| Property | Default | Description |
| --- | --- | --- |
| `testSplitter.testng.suites` | — | Comma-separated suite names to match across all TestNG XML files under the project root. |

</details>

## Output format

<details>
 <summary>JSON structure</summary>

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

</details>

## Pipeline integration

<details>
 <summary>GitHub Actions: dynamic matrix example (short snippet)</summary>

For a complete end-to-end pipeline example, check the workflow here:
https://github.com/CyborgCodeSyndicate/roa-github-workflows/blob/main/.github/workflows/roa-tests.yml

Below is a minimal example showing how to dynamically generate the matrix strategy based on the JSON output from the plugin. This approach starts runners based on the actual number of buckets created.

```yaml
jobs:
  generate-matrix:
    runs-on: ubuntu-latest
    outputs:
      matrix: ${{ steps.set-matrix.outputs.matrix }}
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
          cache: maven

      - name: Split tests (creates ci/grouped-tests.json)
        run: mvn -q -DtestSplitter.enabled=true -DtestSplitter.json.output=ci/grouped-tests io.cyborgcode.roa.plugins:test-allocator-maven-plugin:RELEASE:split

      - name: Generate matrix from JSON
        id: set-matrix
        run: |
          BUCKETS=$(jq -c '[.[].jobIndex]' ci/grouped-tests.json)
          echo "matrix={\"bucket\":${BUCKETS}}" >> "$GITHUB_OUTPUT"

      - name: Upload test buckets
        uses: actions/upload-artifact@v4
        with:
          name: test-buckets
          path: ci/grouped-tests.json

  run-tests:
    needs: generate-matrix
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix: ${{ fromJson(needs.generate-matrix.outputs.matrix) }}
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
          cache: maven

      - name: Download test buckets
        uses: actions/download-artifact@v4
        with:
          name: test-buckets
          path: ci

      - name: Run tests for bucket ${{ matrix.bucket }}
        shell: bash
        run: |
          CLASSES=$(jq -r ".[${{ matrix.bucket }}].classes | join(',')" ci/grouped-tests.json)
          mvn -Dtest="${CLASSES}" -DfailIfNoTests=false test
```

For TestNG, feed the selected class list into the TestNG runner, or generate suite XMLs dynamically per bucket.

</details>

## Troubleshooting

<details>
 <summary>Common issues</summary>

- **Empty JSON**: Ensure `testSplitter.enabled=true`, the chosen engine matches your framework, and tests are compiled before the goal runs.
- **Missing classes**: Confirm `${project.build.testOutputDirectory}` points to compiled tests and that dependencies are resolved (the classloader pulls from Maven classpaths).
- **TestNG suites ignored**: Double-check suite names; every `.xml` file under the project root is parsed, so misnamed suites will be skipped.
- **Unexpected single-slot classes**: Classes are collapsed when `parallel.methods=false` or the class extends a sequential base (name contains `BaseTestSequential`).

</details>

## Author
**Cyborg Code Syndicate 💍👨💻**