#!/usr/bin/env python3
"""Sets the version of the code generator, the Maven plug-in and the Gradle plug-in.

Usage: scripts/set_version.py [--check] <version>

The version must be of the form X.Y.Z or X.Y.Z-SNAPSHOT. It is applied to the three
artifacts and to the integration tests, which always build against the current
development version. The examples and the documentation are not touched; they refer
to the released version and are updated with sync_example_versions.py.

With --check, no file is modified. Instead, the script fails unless all these places
already carry the given version. The release workflow uses it to verify the tag.
"""

import re
import sys
from pathlib import Path

VERSION_PATTERN = re.compile(r"^\d+\.\d+\.\d+(-SNAPSHOT)?$")

ROOT = Path(__file__).resolve().parent.parent

# File, regular expression matching the version, and the expected number of matches.
# In each regular expression, group 1 is the version.
REPLACEMENTS = [
    ("windowsapi-code-generator/pom.xml",
     r"<artifactId>code-generator</artifactId>\s*\n\s*<version>([^<]+)</version>", 1),
    ("windowsapi-maven-plugin/pom.xml",
     r"<artifactId>windowsapi-maven-plugin</artifactId>\s*\n\s*<version>([^<]+)</version>", 1),
    ("windowsapi-maven-plugin/pom.xml",
     r"<code-generator\.version>([^<]+)</code-generator\.version>", 1),
    ("windowsapi-gradle-plugin/windowsapi-gradle-plugin/build.gradle.kts",
     r"^version = \"([^\"]+)\"", 1),
    ("windowsapi-gradle-plugin/windowsapi-gradle-plugin/build.gradle.kts",
     r"net\.codecrete\.windows-api:code-generator:([^\"]+)", 1),
    ("integration-tests/windows-api-tests/pom.xml",
     r"<artifactId>windowsapi-maven-plugin</artifactId>\s*\n\s*<version>([^<]+)</version>", 1),
]


def find_versions(path, pattern, expected_matches):
    """Returns the file's text and the matches of the pattern (group 1 is the version)."""
    text = (ROOT / path).read_text(encoding="utf-8")
    matches = list(re.finditer(pattern, text, flags=re.M))
    if len(matches) != expected_matches:
        raise SystemExit(
            f"{path}: expected {expected_matches} match(es) of /{pattern}/, found {len(matches)}")
    return text, matches


def check_version(path, pattern, expected_matches, version):
    """Returns an error message for each version matched by the pattern that differs from the given one."""
    _, matches = find_versions(path, pattern, expected_matches)
    return [f"{path}: found version {m.group(1)}, expected {version}"
            for m in matches if m.group(1) != version]


def replace_version(path, pattern, expected_matches, version):
    """Replaces the version matched by group 1 of the pattern, in the given file."""
    text, matches = find_versions(path, pattern, expected_matches)
    file = ROOT / path

    for match in reversed(matches):
        text = text[:match.start(1)] + version + text[match.end(1):]
    file.write_text(text, encoding="utf-8")


def main():
    args = sys.argv[1:]
    check = args[:1] == ["--check"]
    if check:
        args = args[1:]
    if len(args) != 1:
        raise SystemExit(f"usage: {sys.argv[0]} [--check] <version>")

    version = args[0]
    if not VERSION_PATTERN.match(version):
        raise SystemExit(f"invalid version: {version} (expected X.Y.Z or X.Y.Z-SNAPSHOT)")

    if check:
        errors = [error for path, pattern, expected_matches in REPLACEMENTS
                  for error in check_version(path, pattern, expected_matches, version)]
        if errors:
            raise SystemExit("\n".join(errors))
        print(f"version is {version}")
        return

    for path, pattern, expected_matches in REPLACEMENTS:
        replace_version(path, pattern, expected_matches, version)

    print(f"version set to {version}")


if __name__ == "__main__":
    main()
