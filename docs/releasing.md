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
```

Publish the code generator library and the Maven plug-in to Maven Central. The `release`
profile enables the signing of the artifacts with GPG:

```shell
cd windowsapi-code-generator
mvn -Prelease deploy

cd ../windowsapi-maven-plugin
mvn -Prelease deploy
```

Publish the Gradle plug-in to the Gradle Plugin Portal (credentials are read from the
environment variables `GRADLE_PUBLISH_KEY` and `GRADLE_PUBLISH_SECRET`):

```shell
cd windowsapi-gradle-plugin
./gradlew publishPlugins
```

Tag the release:

```shell
git tag v0.8.7
git push origin v0.8.7
```

Finally, start the next development cycle:

```shell
scripts/set_version.py 0.8.8-SNAPSHOT
git commit -a -m "Start development of 0.8.8"
```
