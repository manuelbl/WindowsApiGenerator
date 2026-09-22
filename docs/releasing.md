# Development Cycle and Releasing

The version of the code generator, the Maven plug-in and the Gradle plug-in is increased at
the start of each development cycle and carries the suffix `-SNAPSHOT` until it is released:

```shell
scripts/set_version.py 0.8.7-SNAPSHOT
```

The script updates the three artifacts and the integration tests. It does not touch the
examples and the documentation: they refer to the released version so that they work for
anybody checking out the repository. The CI pipeline runs `scripts/sync_example_versions.py`
to build the examples with the version under development, ensuring that it does not break
them.

## Releasing

Set the version to release and update the examples and the documentation to use it:

```shell
scripts/set_version.py 0.8.7
scripts/sync_example_versions.py
git commit -a -m "Prepare for release 0.8.7"
git push
```

Tag the release and push the tag:

```shell
git tag v0.8.7
git push origin v0.8.7
```

The tag starts the release workflow (`.github/workflows/release.yaml`):

1. `verify` runs the continuous integration build on the tagged commit.
2. `publish` waits for approval in the `release` environment. After it has been approved, it
   checks that the tag matches the version of the three artifacts, publishes the code generator
   and the Maven plug-in to Maven Central and the Gradle plug-in to the Gradle Plugin Portal.
3. `draft-release` creates a draft GitHub release with generated release notes.

Approve the deployment in the workflow run on GitHub. Once the workflow has completed, edit
the release notes of the draft release and publish it.

Maven Central publishes the artifacts immediately (`autoPublish`), so they cannot be withdrawn.
If a later step fails, the earlier artifacts are already public. Fix the cause and publish the
missing artifacts manually.

Finally, start the next development cycle:

```shell
scripts/set_version.py 0.8.8-SNAPSHOT
git commit -a -m "Start development of 0.8.8"
git push
```

## One-time setup

The credentials are stored as secrets of the GitHub environment `release`
(*Settings > Environments*):

| Secret                  | Content                                                   |
|-------------------------|-----------------------------------------------------------|
| `CENTRAL_USERNAME`      | Username of the Maven Central user token                  |
| `CENTRAL_PASSWORD`      | Password of the Maven Central user token                  |
| `GPG_PRIVATE_KEY`       | ASCII-armored private GPG key for signing the artifacts   |
| `GPG_PASSPHRASE`        | Passphrase of the GPG key                                 |
| `GRADLE_PUBLISH_KEY`    | API key of the Gradle Plugin Portal                       |
| `GRADLE_PUBLISH_SECRET` | API secret of the Gradle Plugin Portal                    |

The private GPG key is exported with:

```shell
gpg --armor --export-secret-keys <key-id>
```

The environment is protected by:

- *Required reviewers*: the maintainer (with *Prevent self-review* disabled).
- *Deployment branches and tags*: *Selected branches and tags* with the single tag rule `v*`.

Additionally, a tag ruleset (*Settings > Rules > Rulesets*) targeting `v*` restricts the
creation of release tags to the maintainer.
