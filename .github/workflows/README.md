# GitHub Workflows

This directory contains the GitHub Actions workflow definitions. These workflows provide automated continuous integration and deployment capabilities for the Maven-based plugin ecosystem.

## Table of Contents

- [Overview](#overview)
- [Workflows](#workflows)
  - [Deploy Workflow](#deploy-workflow)
  - [PR Validator](#pr-validator)
- [Integration with ROA Framework](#integration-with-roa-framework)
- [Pipeline Utilities](#pipeline-utilities)
- [Permissions Model](#permissions-model)
  - [Deploy Permissions](#deploy-permissions)
  - [PR Validator Permissions](#pr-validator-permissions)
- [Configuration Notes](#configuration-notes)
- [Author](#author)

## Overview

The workflows in this directory leverage the centralized pipeline utilities from the Cyborg Code Syndicate organization to ensure consistent build, validation, and deployment processes across all ROA projects. Each workflow is designed to integrate seamlessly with GitHub's security model and package registry.

## Workflows

### Deploy Workflow

**Path:** `./deploy.yml`

Handles automated deployment of plugin artifacts to GitHub Packages when changes are merged to the main branch.

**Triggers:**
- Push to `main` branch (automatic deployment)
- Manual dispatch with configurable options

**Features:**
- **Version Management**: Supports semantic version bumping (major, minor, patch, or none)
- **Selective Deployment**: Optional module-specific deployment via comma-separated module list
- **Automated Publishing**: Deploys to GitHub Packages with proper authentication

**Manual Execution Parameters:**
```yaml
version_bump: [major, minor, patch, none]  # Default: none
modules: "module1,module2"                 # Optional: specific modules to deploy
```

**Required Secrets:**
- `GITHUB_TOKEN` - Provided automatically by GitHub
- `GH_PACKAGES_USER` - GitHub username for package registry access
- `GH_PACKAGES_PAT` - Personal access token with packages:write permission

### PR Validator

**Path:** `pr-validator.yml`

Provides comprehensive validation for pull requests targeting the main branch, ensuring code quality and security compliance before merge.

**Triggers:**
- Pull request opened, synchronized, or reopened against `main` branch

**Validation Pipeline:**

Pull request validation runs automatically and provides:
- **Code Quality**: SonarCloud analysis with project-specific configuration
- **Security Scanning**: NVD vulnerability database integration
- **Dependency Checks**: Maven dependency validation
- **Java Compatibility**: Ensures compatibility with project's target Java version
- **Build Status**: Ensures successful build and test execution

**Required Secrets:**
- `NVD_API_KEY` - National Vulnerability Database API access
- `SONAR_TOKEN` - SonarCloud authentication token
- `GITHUB_TOKEN` - Provided automatically by GitHub
- `GH_PACKAGES_USER` - GitHub username for dependency resolution
- `GH_PACKAGES_PAT` - Personal access token for private package access

## Integration with ROA Framework

These workflows are specifically configured for the Ring of Automation plugin ecosystem:

- **Project Identification**: Uses `CyborgCodeSyndicate_roa-plugins` as the SonarCloud project key
- **Maven Configuration**: Integrates with the `io.cyborgcode.roa.plugins` group structure
- **Centralized Pipelines**: Leverages versioned pipeline utilities
- **GitHub Packages**: Configured for the `github-roa-plugins` server deployment target

## Pipeline Utilities

Both workflows utilize centralized pipeline actions from `CyborgCodeSyndicate/utilities/pipelines/` which provide:

- Standardized build processes across all Cyborg Code Syndicate projects
- Consistent security scanning and quality gates
- Automated version management and artifact publishing
- Integration with organizational toolchain (SonarCloud, NVD, GitHub Packages)

## Permissions Model

### Deploy Permissions
- `contents: write` - Repository content modification for version bumping
- `packages: write` - GitHub Packages publishing

### PR Validator Permissions
- `contents: write` - Code analysis and potential fixes
- `pull-requests: write` - PR status updates and comments
- `checks: write` - Check run creation and updates
- `security-events: write` - Security alert management
- `packages: read` - Dependency resolution from GitHub Packages

## Configuration Notes

*Pipeline versions and SonarCloud configuration are maintained centrally by the Cyborg Code Syndicate team. Local modifications to these workflows should be coordinated to ensure compatibility with organizational standards.*

## Author
**Cyborg Code Syndicate 💍👨💻**