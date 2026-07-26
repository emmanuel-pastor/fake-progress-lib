# CI/CD Documentation

This document describes the Continuous Integration and Continuous Deployment processes for the `fake-progress-lib`
project.

## Documentation Generation

The project uses [Dokka](https://kotlinlang.org/docs/dokka-introduction.html) to generate browsable HTML documentation
from KDoc comments.

### Generating Locally

To generate the documentation locally, run the following command:

```bash
./gradlew :library:dokkaGeneratePublicationHtml
```

The generated HTML will be available at `kdoc/index.html`.

## Continuous Deployment

### GitHub Pages (Documentation)

A GitHub Actions workflow (`deploy-docs.yml`) is included to automatically build and deploy the documentation to GitHub
Pages on every push to the `main` branch.

### Library Publication

The library is configured to be published to Maven Central. The `.github/workflows/publish.yml` workflow handles the
publication process when a new release is created.
