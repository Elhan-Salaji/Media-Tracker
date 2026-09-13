"""Checks that commit subjects follow `<type>: <description> #<issue>`.

On a pull request the script checks every commit between base and head, on a
push every commit the push added. The rules live in CONTRIBUTING.md.
"""

import os
import re
import subprocess
import sys

VALID_TYPES = (
    "add", "update", "build", "fix", "feat", "chore", "test",
    "docs", "refactor", "style", "remove", "revert", "release", "init",
)

# A branch commit ends with the issue number. A squash commit on develop gets the
# pull request number appended by GitHub, and a Dependabot squash commit carries
# the pull request number alone.
COMMIT_PATTERN = re.compile(
    r"^(" + "|".join(VALID_TYPES) + r"): .+ (#\d+|#\d+ \(#\d+\)|\(#\d+\))$",
    re.IGNORECASE,
)
EXEMPT_PATTERN = re.compile(r'^(Merge |Revert "|init(ial)? (commit|repo))', re.IGNORECASE)

# Dependabot writes its own commit messages, without an issue number.
EXEMPT_AUTHORS = {"dependabot[bot]"}

# The last commit on develop before the convention existed. Everything reachable
# from it keeps its old message format and is left out of the check.
BASELINE = "a7ae51160e999c2a9f340bd58e39cb5b1bf328fa"

# GitHub sends this as the "before" SHA when a push creates a branch.
NULL_SHA = "0" * 40

FIELD_SEPARATOR = "\x1f"


def is_valid(subject: str) -> bool:
    return bool(EXEMPT_PATTERN.match(subject) or COMMIT_PATTERN.match(subject))


def commit_subjects() -> list[str]:
    base = os.environ.get("BASE_SHA", "")
    head = os.environ.get("HEAD_SHA", "HEAD")
    revisions = [head, "-1"] if base in ("", NULL_SHA) else [f"{base}..{head}"]
    result = subprocess.run(
        ["git", "log", f"--format=%an{FIELD_SEPARATOR}%s", *revisions, "--not", BASELINE],
        capture_output=True, text=True, check=True,
    )
    subjects = []
    for line in result.stdout.splitlines():
        author, _, subject = line.partition(FIELD_SEPARATOR)
        if subject.strip() and author not in EXEMPT_AUTHORS:
            subjects.append(subject)
    return subjects


def main() -> None:
    subjects = commit_subjects()
    invalid = [subject for subject in subjects if not is_valid(subject)]

    if invalid:
        print("These commit messages break the format from CONTRIBUTING.md:")
        for subject in invalid:
            print(f"  ✗ {subject}")
        print()
        print("Format:   <type>: <description> #<issue>, on develop with \" (#<pull request>)\" appended")
        print("Example:  fix: retry the initial search after login #17")
        sys.exit(1)

    print(f"✓ {len(subjects)} commit message(s) follow the format.")


if __name__ == "__main__":
    main()
