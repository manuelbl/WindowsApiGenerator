# Releasing

The regular build does not sign the artifacts, so no GPG key is needed to build this project.
Signing is enabled by the Maven profile `release`, which is only used for publishing.

## Publishing

Publish the code generator library and the Maven plugin to Maven Central:

```shell
cd windowsapi-code-generator
mvn -Prelease deploy

cd ../windowsapi-maven-plugin
mvn -Prelease deploy
```

Publish the Gradle plugin to the Gradle Plugin Portal (credentials are read from the
environment variables `GRADLE_PUBLISH_KEY` and `GRADLE_PUBLISH_SECRET`):

```shell
cd windowsapi-gradle-plugin
./gradlew publishPlugins
```

Finally, tag the release:

```shell
git tag v0.8.6
git push origin v0.8.6
```
