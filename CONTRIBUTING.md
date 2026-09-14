# Contributing

How the team works in this repository. CI checks the commit format; the rest of these rules rely on you.

## Workflow

1. **Issue first.** Every change starts with an issue in the template format: story, situation, acceptance criteria, out of scope. No branch without an issue number. Assign Elhan-Salaji and Snobbus, and give the issue an area label, a `priority::` label, a `difficulty::` label and a milestone.
2. **Branch off `develop`.** Name the branch `type/short-description-#issue`, for example `feature/tmdb-client-#13` or `fix/initial-search-#17`.
3. **Commit in small steps and push each commit before you make the next one.** The format is described below.
4. **Open a pull request into `develop`.** Its title follows the commit format, `<type>: <description> #<issue>`, and its body starts with `Closes #<issue>`. Assign yourself, add labels and request a review: Snobbus reviews pull requests by Elhan-Salaji, and Elhan-Salaji reviews the ones by Snobbus and by Dependabot. Work through the checklist in the template.
5. **Squash and merge** once CI is green. `develop` gets one commit per pull request: GitHub takes the pull request title as the subject, appends the pull request number and lists the branch commits in the message.

## Commit messages

```
<type>: <description> #<issue>
```

Example: `fix: retry the initial search after login #17`

Allowed types: `add`, `update`, `build`, `fix`, `feat`, `chore`, `test`, `docs`, `refactor`, `style`, `remove`, `revert`, `release`, `init`.

The Commit Check workflow runs `.github/scripts/check_commits.py` on every pull request and on every push to `develop` and `main`. It fails on a commit that breaks the format. A squash commit passes with the pull request number appended, for example `fix: retry the initial search after login #17 (#42)`. Merge commits, reverts and Dependabot commits are exempt, and so are the commits from before the check existed.

## Branching rules

- Branches start from `develop` and merge back into `develop`. Don't branch off a feature branch, and don't merge feature branches into each other.
- If your topic builds on a pull request that is still open, wait for its merge and branch fresh from `develop`.
- `main` receives release merges from `develop` only. A release pull request uses a merge commit instead of a squash, so `main` and `develop` keep a shared history. A release moves the `[Unreleased]` section of the changelog under a version heading and gets a SemVer tag.
- `main` and `develop` are protected. Changes arrive through a pull request, and the CI jobs have to pass before the merge.

## Project board

The Media Tracker project on GitHub holds every issue and pull request. Its built-in workflows add new items and move closed and merged ones to Done. Priority and Size follow the labels of an issue:

| Label | Project field |
|---|---|
| `priority::high`, `priority::mid`, `priority::low` | Priority P0, P1, P2 |
| `difficulty::low`, `difficulty::mid`, `difficulty::high` | Size S, M, L |

The Project Fields workflow (`.github/workflows/project-fields.yml`) applies that mapping when an issue is opened or its labels change, and clears a field when its label goes away. You don't set Priority or Size by hand. Start date, target date and estimate stay empty.

`GITHUB_TOKEN` cannot write to a project that belongs to a user account. The workflow reads a personal access token (classic) with the `project` and `repo` scopes from the repository secret `PROJECT_TOKEN`, created by Elhan-Salaji, who owns the project. Without the secret the job ends with a notice and changes nothing. When the token expires, replace the secret with `gh secret set PROJECT_TOKEN`.

## Before you open a pull request

Run the checks for the part you touched. CI runs all of them again.

```bash
cd backend && ./mvnw test
cd frontend && npm run lint && npm run build
```

## Architecture decisions

Record a significant decision on the [Architecture Decision Records](https://github.com/Elhan-Salaji/Media-Tracker/wiki/Architecture-Decision-Records) wiki page, with status, context, decision and consequences. When a decision changes, add a new record that supersedes the old one instead of deleting it.

## Changelog

Add every user-visible change to `CHANGELOG.md` under `[Unreleased]`, in the matching [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) category (Added, Changed, Deprecated, Removed, Fixed, Security), and reference the issue.
