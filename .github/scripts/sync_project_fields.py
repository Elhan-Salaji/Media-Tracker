"""Sets Priority and Size on the Media Tracker project from an issue's labels.

The Project Fields workflow runs this script whenever an issue is opened or its
labels change. `priority::high|mid|low` becomes P0|P1|P2 and
`difficulty::low|mid|high` becomes S|M|L. A field without a matching label gets
cleared, so the project follows the labels. CONTRIBUTING.md describes the rules.
"""

import json
import os
import subprocess
import sys

# Project field -> (label prefix, label suffix -> option name of the field).
FIELD_MAPPINGS = {
    "Priority": ("priority::", {"high": "P0", "mid": "P1", "low": "P2"}),
    "Size": ("difficulty::", {"low": "S", "mid": "M", "high": "L"}),
}


def gh(*args: str) -> str:
    result = subprocess.run(["gh", *args], capture_output=True, text=True, check=True)
    return result.stdout


def desired_options(labels: list[str]) -> dict[str, str | None]:
    """Returns the option name per field, or None when no label sets that field."""
    desired = {}
    for field, (prefix, options) in FIELD_MAPPINGS.items():
        matches = [options[label[len(prefix):]] for label in labels
                   if label.startswith(prefix) and label[len(prefix):] in options]
        if len(matches) > 1:
            print(f"::warning::Several {prefix} labels, {field} uses {matches[0]}")
        desired[field] = matches[0] if matches else None
    return desired


def main() -> None:
    # GITHUB_TOKEN cannot reach a user-owned project, so the workflow passes
    # the PROJECT_TOKEN secret. A fork or a fresh clone has no such secret.
    if not os.environ.get("GH_TOKEN"):
        print("::notice::PROJECT_TOKEN is not set, the project fields stay as they are.")
        return

    repository = os.environ["REPOSITORY"]
    issue = os.environ["ISSUE_NUMBER"]
    owner = os.environ["PROJECT_OWNER"]
    project_number = os.environ["PROJECT_NUMBER"]

    labels = json.loads(gh("api", f"repos/{repository}/issues/{issue}", "--jq", "[.labels[].name]"))
    desired = desired_options(labels)

    # item-add returns the existing item when the project already holds the issue,
    # so the script does not depend on the project's own auto-add running first.
    issue_url = f"https://github.com/{repository}/issues/{issue}"
    item_id = json.loads(gh("project", "item-add", project_number, "--owner", owner,
                            "--url", issue_url, "--format", "json"))["id"]
    project_id = json.loads(gh("project", "view", project_number, "--owner", owner,
                               "--format", "json"))["id"]
    fields = {field["name"]: field for field in json.loads(
        gh("project", "field-list", project_number, "--owner", owner, "--format", "json"))["fields"]}

    for field_name, option_name in desired.items():
        field = fields[field_name]
        edit = ["project", "item-edit", "--id", item_id, "--project-id", project_id, "--field-id", field["id"]]
        if option_name is None:
            gh(*edit, "--clear")
        else:
            option_id = next(option["id"] for option in field["options"] if option["name"] == option_name)
            gh(*edit, "--single-select-option-id", option_id)

    summary = ", ".join(f"{name}={value or 'cleared'}" for name, value in desired.items())
    print(f"#{issue}: {summary}")


if __name__ == "__main__":
    try:
        main()
    except subprocess.CalledProcessError as error:
        print(f"::error::{' '.join(error.cmd[:3])} failed: {error.stderr.strip()}")
        sys.exit(1)
