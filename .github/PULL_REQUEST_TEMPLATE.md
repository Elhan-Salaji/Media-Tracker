Closes #

<What changes and why. Point out the decisions a reviewer should look at.>

## Checklist

- [ ] Linked to its issue with `Closes #<issue>`.
- [ ] Title follows `<type>: <description> #<issue>`, because it becomes the squash commit.
- [ ] Branch named `type/short-description-#issue` and based on `develop`.
- [ ] Every commit follows `<type>: <description> #<issue>` and was pushed on its own.
- [ ] Tests added or updated for changed behaviour.
- [ ] `CHANGELOG.md` updated under `[Unreleased]`, unless nothing user-visible changed.
- [ ] Labels and assignee set, review requested (Snobbus, or Elhan-Salaji for pull requests by Snobbus).
