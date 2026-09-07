#!/usr/bin/env python3
import json
import os
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

API = "https://api.github.com/advisories"
SEVERITIES = ("critical", "high")
BATCH_SIZE = 20
RETRIES = 3


def load_coordinates(path: Path) -> list[str]:
    coordinates = []
    for raw in path.read_text(encoding="utf-8").splitlines():
        value = raw.strip()
        if not value:
            continue
        parts = value.split(":", 2)
        if len(parts) != 3 or not all(parts):
            raise SystemExit(f"Invalid Maven coordinate: {value}")
        group, artifact, version = parts
        coordinates.append(f"{group}:{artifact}@{version}")
    if not coordinates:
        raise SystemExit("No Maven coordinates to audit")
    return sorted(set(coordinates))


def request_advisories(packages: list[str], severity: str) -> list[dict]:
    query = [
        ("ecosystem", "maven"),
        ("severity", severity),
        ("is_withdrawn", "false"),
        ("per_page", "100"),
    ]
    query.extend(("affects[]", package) for package in packages)
    url = API + "?" + urllib.parse.urlencode(query)
    headers = {
        "Accept": "application/vnd.github+json",
        "User-Agent": "reruna-dependency-audit",
        "X-GitHub-Api-Version": "2022-11-28",
    }
    token = os.environ.get("GITHUB_TOKEN")
    if token:
        headers["Authorization"] = f"Bearer {token}"

    for attempt in range(1, RETRIES + 1):
        try:
            request = urllib.request.Request(url, headers=headers)
            with urllib.request.urlopen(request, timeout=30) as response:
                return json.load(response)
        except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError) as error:
            if attempt == RETRIES:
                raise SystemExit(f"GitHub Advisory Database query failed: {error}")
            time.sleep(attempt * 2)
    raise AssertionError("unreachable")


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit("Usage: audit_maven_dependencies.py <coordinates-file>")

    coordinates = load_coordinates(Path(sys.argv[1]))
    advisories = {}

    for start in range(0, len(coordinates), BATCH_SIZE):
        batch = coordinates[start : start + BATCH_SIZE]
        for severity in SEVERITIES:
            for advisory in request_advisories(batch, severity):
                advisories[advisory["ghsa_id"]] = advisory

    if advisories:
        for advisory in sorted(advisories.values(), key=lambda item: item["ghsa_id"]):
            severity = advisory.get("severity", "unknown").upper()
            summary = advisory.get("summary", "")
            url = advisory.get("html_url", "")
            print(f"{severity} {advisory['ghsa_id']}: {summary} {url}")
        raise SystemExit(
            f"Resolved dependency audit failed: {len(advisories)} high/critical advisory(s)"
        )

    print(f"Resolved dependency audit: OK ({len(coordinates)} Maven components)")


if __name__ == "__main__":
    main()
