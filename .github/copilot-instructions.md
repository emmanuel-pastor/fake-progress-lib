# Pull Request and Commit Guidelines

When generating pull request summaries, PR titles, or commit messages for this repository, ALWAYS adhere strictly to the
Conventional Commits 1.0.0 specification.

## Structure

The message must be structured as follows:
`<type>[optional scope][optional !]: <description>`
` `
`[optional body]`
` `
`[optional footer(s)]`

## Rules

1. **Types**:
    - MUST use `feat` for new features.
    - MUST use `fix` for bug fixes.
    - RECOMMENDED additional types: `build`, `chore`, `ci`, `docs`, `style`, `refactor`, `perf`, `test`.
2. **Scope**: MAY be provided as a noun describing a section of the codebase surrounded by parenthesis, e.g.,
   `fix(parser):`.
3. **Description**: MUST immediately follow the colon and space after the type/scope prefix.
4. **Body**: MAY be provided to give additional contextual information. It MUST begin exactly one blank line after the
   description and MAY consist of multiple paragraphs.
5. **Footers**: MAY be provided one blank line after the body.
    - MUST consist of a word token (using `-` instead of spaces, e.g., `Acked-by`), followed by `:<space>` or
      `<space>#`, followed by a string value.
6. **Breaking Changes**:
    - MUST be indicated by appending a `!` immediately before the `:` in the prefix (e.g., `feat!:`) OR as a footer
      beginning with `BREAKING CHANGE: <description>`.
    - If `!` is used, the `BREAKING CHANGE:` footer is optional.

## Examples

- `feat(api)!: drop support for Node 6`
- `fix: prevent racing of requests` (followed by a blank line and a detailed body)
- `docs: correct spelling of CHANGELOG`