# CI/CD Documentation

This document describes the Continuous Integration and Continuous Deployment processes for the `fake-progress-lib`
project.

## Continuous Integration

The project uses GitHub Actions for Continuous Integration. The following workflows are triggered on every push and pull
request to the `main` branch.

### Workflow Pipeline

```mermaid
graph TD
  Trigger[Push / PR to main] --> Lint[Linting]
  Trigger --> Test[Test Matrix]
  Trigger --> CodeQL[Security Analysis]
  Trigger --> CommitLint[Commit Lint]
  Trigger --> BinaryCompat[Binary Compatibility]
  Test --> Coverage[Coverage Report]
  CommitLint --> ReleasePlease[Release Please]
  ReleasePlease -->|release created| Tag[Release Tag]
  ReleasePlease -->|release created| Changelog[Changelog Update]
  ReleasePlease -->|release created| Version[Version Bump]

  subgraph Platforms
    JVM[JVM]
    Android[Android]
    Wasm[WasmJS]
    Linux[Linux]
    iOS[iOS]
  end

  Test -.-> JVM
  Test -.-> Android
  Test -.-> Wasm
  Test -.-> Linux
  Test -.-> iOS
```

### Workflows

- **Test (`test.yml`)**: Runs the test suite across all supported platforms (JVM, Android, WasmJS, Linux, and iOS).
  - Collects and uploads test reports as artifacts on failure.
  - Generates a consolidated Kover coverage report after all tests pass.
  - Enforces a code coverage gate (minimum 80% instruction coverage).
  - Surfaces the coverage percentage in the workflow summary.
- **Lint (`lint.yml`)**: Performs static code analysis via [detekt](https://detekt.dev/), using the
  `detekt-rules-libraries` plugin to enforce library API best practices (explicit return types, no public data classes,
  no unnecessarily public entities).
- **Binary Compatibility (`binary-compatibility.yml`)**: Validates Kotlin ABI binary compatibility against reference
  dumps using `./gradlew checkKotlinAbi` on pull requests targeting `main`. It also enforces a bridge check: if `.api`
  files are modified, the PR title must contain a conventional breaking change indicator (e.g., `feat!:`) to guarantee
  Release Please executes a major version bump.
- **Release (`release.yml`)**: Enforces conventional commits and automates versioning, changelog generation, and release
  tagging via [Release Please](https://github.com/googleapis/release-please).
  - **Commit Lint**: On every push to `main` or pull request targeting `main`, all commit messages (or the PR title for
    squash merges) are validated against the [Conventional Commits](https://www.conventionalcommits.org/) specification
    using [commitlint](https://commitlint.js.org/).
  - **Release Please**: On every push to `main`, Release Please inspects the commit history and, when releasable changes
    are present, opens or updates a release PR that bumps the version and updates `CHANGELOG.md`. Merging that PR
    creates a GitHub release and the corresponding version tag.

## Continuous Deployment

### GitHub Pages (Documentation)

A GitHub Actions workflow (`deploy-docs.yml`) is included to automatically build and deploy the documentation to GitHub
Pages on every push to the `main` branch.

### Library Publication

The library is configured to be published to Maven Central. The `.github/workflows/publish.yml` workflow handles the
publication process when a new release is created.
