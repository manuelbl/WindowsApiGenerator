# GraalVM Example

This example project demonstrates how to build a native application with GraalVM
calling Windows API functions.

All downcalls and upcalls must be declared in a file called `reachabilit-metadata.json`.
The Windows API generator generates the file. See
`<reachabilityMetadataFile>...</reachabilityMetadataFile>` in `pom.xml`.

## Build and Run

### Prerequisites

- [GraalVM](https://www.graalvm.org/) 25 or higher
- [Maven](https://maven.apache.org/) 3.9 or higher

Ensure that the environment variable `JAVA_HOME` points to the GraalVM directory.


### Building

```shell
mvn -Pnative clean package
```


### Running

```shell
./target/native-example
```
