# Contributing

How the team works in this repository. CI checks the commit format; the rest of these rules rely on you.

## Workflow

1. **Issue first.** Every change starts with an issue in the template format: story, situation, acceptance criteria, out of scope. No branch without an issue number.
2. **Branch off `develop`.** Name the branch `type/short-description-#issue`, for example `feature/tmdb-client-#13` or `fix/initial-search-#17`.
3. **Commit in small steps and push each commit before you make the next one.** The format is described below.
4. **Open a pull request into `develop`** with `Closes #<issue>` in the body, and work through the checklist in the template.
5. **Merge with a merge commit** once CI is green. Squash and rebase merges are switched off, because the single commits belong to the history.

## Commit messages

```
<type>: <description> #<issue>
```

Example: `fix: retry the initial search after login #17`

Allowed types: `add`, `update`, `build`, `fix`, `feat`, `chore`, `test`, `docs`, `refactor`, `style`, `remove`, `revert`, `release`, `init`.

The Commit Check workflow runs `.github/scripts/check_commits.py` on every pull request and on every push to `develop` and `main`. It fails on a commit that breaks the format. Merge commits, reverts and Dependabot commits are exempt, and so are the commits from before the check existed.

## Branching rules

- Branches start from `develop` and merge back into `develop`. Don't branch off a feature branch, and don't merge feature branches into each other.
- If your topic builds on a pull request that is still open, wait for its merge and branch fresh from `develop`.
- `main` receives release merges from `develop` only. A release moves the `[Unreleased]` section of the changelog under a version heading and gets a SemVer tag.
- `main` and `develop` are protected. Changes arrive through a pull request, and the CI jobs have to pass before the merge.

## Before you open a pull request

Run the checks for the part you touched. CI runs all of them again.

```bash
cd backend && ./mvnw test
cd frontend && npm run lint && npm run build
```

## Changelog

Add every user-visible change to `CHANGELOG.md` under `[Unreleased]`, in the matching [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) category (Added, Changed, Deprecated, Removed, Fixed, Security), and reference the issue.
