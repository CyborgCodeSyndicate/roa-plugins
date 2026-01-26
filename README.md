# roa-plugins

A collection of Maven plugins designed to enhance test automation capabilities within the Ring of Automation framework. These plugins provide intelligent test distribution, allocation, and execution orchestration for large-scale test suites.

## Table of Contents

- [Overview](#overview)
- [Structure](#structure)
- [Plugins](#plugins)
    - [Quick Usage Example](#quick-usage-example)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [CI/CD Integration](#cicd-integration)
- [Dependencies](#dependencies)
- [Author](#author)

## Overview

The ROA Plugins suite provides Maven-based tooling for intelligent test execution management. Built on Java 17 and integrated with the Cyborg Code Syndicate utilities ecosystem, these plugins enable:

- **Intelligent Test Distribution**: Automatic load balancing across multiple execution runners
- **Framework Agnostic**: Support for both JUnit Jupiter and TestNG test frameworks  
- **CI/CD Optimization**: JSON-based test manifest generation for pipeline consumption
- **Parallel Execution Control**: Configurable parallel vs sequential test execution strategies

## Structure

<details>
 <summary>Project structure</summary>

The plugin ecosystem follows a modular architecture built on Maven's plugin framework:

```
roa-plugins/
├── .github/
│   └── workflows/          # GitHub Actions CI/CD
│       ├── deploy.yml      # Automated deployment
│       ├── pr-validator.yml # PR validation pipeline
│       └── README.md # Workflows documentation
├── test-allocator-maven-plugin/
│   ├── src/main/java/      # Plugin source code
│   ├── src/test/java/      # Unit tests
│   ├── pom.xml            # Module configuration  
│   └── README.md          # Plugin documentation
├── dependency-check-suppressions.xml # Security scan config
├── pom.xml                # Parent project configuration
└── README.md             # This file
```

**Core Principles:**
- **Extensibility**: Plugin-based architecture supports future automation tools
- **Integration**: Seamless integration with Cyborg Code Syndicate utilities
- **Standardization**: Consistent configuration and deployment patterns
- **Security**: Built-in dependency vulnerability scanning and management

</details>

## Plugins

<details>
 <summary>Available plugins</summary>

| Module | Goal | Purpose | Key Features | Documentation |
|--------|------|---------|--------------|---------------|
| `test-allocator-maven-plugin` | `test-splitter:split` | Intelligent distribution of test classes across execution buckets | • Balanced test load distribution based on method count<br>• JUnit 5 tag filtering and TestNG suite selection<br>• Configurable parallel vs sequential execution hints<br>• JSON manifest output for CI system consumption<br>• Custom classloader for safe test class inspection | [README.md](test-allocator-maven-plugin/README.md) |

</details>

<details>
 <summary>Quick Usage Example</summary>

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
    <maxNumberOfParallelRunners>8</maxNumberOfParallelRunners>
  </configuration>
</plugin>
```

</details>

## Getting Started

<details>
 <summary>Prerequisites & Installation</summary>

### Prerequisites

- **Access**: GitHub Packages authentication for Cyborg Code Syndicate utilities

### Installation

**Direct Maven Coordinates**
```xml
<plugin>
  <groupId>io.cyborgcode.roa.plugins</groupId>
  <artifactId>test-allocator-maven-plugin</artifactId>
  <version>RELEASE</version>
</plugin>
```

</details>


## CI/CD Integration

<details>
 <summary>Recommended pipeline reference</summary>

For a more complete pipeline example (including tests and CI conventions), check:
https://github.com/CyborgCodeSyndicate/roa-github-workflows/blob/main/.github/workflows/roa-tests.yml

</details>

<details>
 <summary>GitHub Actions: dynamic matrix example (short snippet)</summary>

This snippet shows how to dynamically generate the matrix strategy based on the JSON output from the plugin. This approach starts runners based on the actual number of buckets created.

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

</details>

<details>
 <summary>Repository workflows</summary>

The project includes automated CI/CD workflows:

**Pull Request Validation** (`pr-validator.yml`):
- SonarCloud code quality analysis
- NVD security vulnerability scanning  
- Maven build and test execution
- Java 17 compatibility validation

**Automated Deployment** (`deploy.yml`):
- Triggered on `main` branch pushes
- Supports manual version bumping (major/minor/patch)
- Deploys to GitHub Packages
- Selective module deployment capability

**Required Secrets:**
- `GH_PACKAGES_USER` - GitHub username for package access
- `GH_PACKAGES_PAT` - Personal access token with packages scope
- `SONAR_TOKEN` - SonarCloud authentication
- `NVD_API_KEY` - National Vulnerability Database API key

</details>

## Dependencies

<details>
 <summary>Dependency overview</summary>

**Core Framework Dependencies:**
- Maven Plugin API and Core
- Maven Plugin Annotations and Tools
- Gson for JSON serialization
- ClassGraph for bytecode scanning
- Lombok for boilerplate reduction

**Test Framework Support:**
- JUnit Jupiter API (optional)
- TestNG (optional)
- Mockito for testing

**Build Dependencies:**
- SpotBugs annotations
- Plexus utilities (test scope)

***Note:** All dependency versions are managed by the parent POM to ensure consistency across the Cyborg Code Syndicate ecosystem.*

</details>

## Author
**Cyborg Code Syndicate 💍👨💻**

Licensed under Apache-2.0
