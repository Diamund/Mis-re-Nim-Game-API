# Working Agreements

- Read only the context relevant to the current task.
- Inspect existing code before editing.
- Add or update tests when behavior changes, and run the relevant tests before considering a task complete.
- Prefer the smallest clean change.
- Keep changes focused and preserve existing behavior unless explicitly requested.
- Keep code simple, readable, maintainable, and consistent with Spring and Kotlin conventions.
- Prefer clear names over clever abstractions.
- Modularize only when it improves clarity or testability.
- Do not add libraries or patterns without a clear need.
- Avoid unrelated refactoring and duplicated documentation.
- Propose a plan and wait for approval before architectural, cross-cutting, or otherwise risky changes.
- Explain necessary refactors with a short before-and-after comparison.

## Documentation

- Keep `README.md` and relevant Markdown documentation accurate as implementation progresses.
- Update documentation when a change affects behavior, API contracts, architecture, configuration, or build/run instructions.
- Do not document planned functionality as implemented until it has been completed and verified.
- Include necessary documentation updates in the same commit as the related implementation.
- Avoid documentation changes for internal details that do not affect the documented contract.
- Avoid duplicating the same information across files.

## Commits

- Each commit should represent one coherent change.
- Commit messages must clearly explain what changed and why it matters.
- Split unrelated changes into separate commits.
- A commit should be understandable and safely revertible on its own.

Before committing, review the staged changes:

```bash
git diff --cached --stat
git diff --cached --name-status
git diff --cached
```
