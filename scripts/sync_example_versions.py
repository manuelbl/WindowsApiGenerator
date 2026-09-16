#!/usr/bin/env python3
"""Updates the examples and the documentation to use the current version of the plug-ins.

Usage: scripts/sync_example_versions.py

The version is read from the code generator's POM. The examples and the documentation
usually refer to the released version, i.e. they are updated as part of a release, after
the version has been set to the version being released. The CI pipeline runs this script
to build the examples with the version under development.
"""

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

VERSION_SOURCE = "windowsapi-code-generator/pom.xml"
VERSION_SOURCE_PATTERN = r"<artifactId>code-generator</artifactId>\s*\n\s*<version>([^<]+)</version>"

# Maven plug-in reference, e.g. in a <plugin> element. Group 1 is the version.
MAVEN_PLUGIN = r"<artifactId>windowsapi-maven-plugin</artifactId>\s*\n\s*<version>([^<]+)</version>"
# Gradle plug-in reference, in Groovy or Kotlin syntax. Group 1 is the version.
GRADLE_PLUGIN = r"""id\s*\(?["']net\.codecrete\.windows-api["']\)?\s+version\s+["']([^"']+)["']"""

# File, regular expression matching the version, and the expected number of matches.
REPLACEMENTS = [
    ("examples/messagebox/pom.xml", MAVEN_PLUGIN, 1),
    ("examples/native/pom.xml", MAVEN_PLUGIN, 1),
    ("examples/taskbar/pom.xml", MAVEN_PLUGIN, 1),
    ("examples/user_notification_java/pom.xml", MAVEN_PLUGIN, 1),
    ("examples/user_notification_kotlin/pom.xml", MAVEN_PLUGIN, 1),
    ("examples/enum_windows/app/build.gradle.kts", GRADLE_PLUGIN, 1),
    ("examples/medium_story/build.gradle", GRADLE_PLUGIN, 1),
    ("examples/registry/app/build.gradle", GRADLE_PLUGIN, 1),
    ("docs/getting_started_with_maven.md", MAVEN_PLUGIN, 1),
    ("docs/maven_plugin.md", MAVEN_PLUGIN, 2),
    ("docs/getting_started_with_gradle.md", GRADLE_PLUGIN, 1),
    ("docs/gradle_plugin.md", GRADLE_PLUGIN, 2),
]


def replace_version(path, pattern, expected_matches, version):
    """Replaces the version matched by group 1 of the pattern, in the given file."""
    file = ROOT / path
    text = file.read_text(encoding="utf-8")
    matches = list(re.finditer(pattern, text, flags=re.M))
    if len(matches) != expected_matches:
        raise SystemExit(
            f"{path}: expected {expected_matches} match(es) of /{pattern}/, found {len(matches)}")

    for match in reversed(matches):
        text = text[:match.start(1)] + version + text[match.end(1):]
    file.write_text(text, encoding="utf-8")


def current_version():
    """Returns the version of the code generator."""
    text = (ROOT / VERSION_SOURCE).read_text(encoding="utf-8")
    match = re.search(VERSION_SOURCE_PATTERN, text)
    if match is None:
        raise SystemExit(f"{VERSION_SOURCE}: version not found")
    return match.group(1)


def main():
    version = current_version()
    for path, pattern, expected_matches in REPLACEMENTS:
        replace_version(path, pattern, expected_matches, version)

    print(f"examples and documentation set to {version}")


if __name__ == "__main__":
    main()
