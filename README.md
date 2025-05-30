# Windows API Generator for Java (Maven / Gradle)

_Windows API Generator_ generates Java code for using the Windows API,
also called _Java bindings for Windows_.
The generated code uses Java's
[Foreign Function and Memory API](https://docs.oracle.com/en/java/javase/24/core/foreign-function-and-memory-api.html)
to make calls to Windows API functions, work with Windows data structures in native memory,
and use COM objects. The generator is available as a Maven and a Gradle plugin.


## Features

- Generates code for Windows API functions, data structures (`struct` and `union`), callback functions, enumerations, constants and COM interfaces
- COM interfaces can be called from Java or implemented in Java
- Generates code for constants of different types: integer, string, GUID, property keys, etc.
- Automatically adds the related data structures, enumerations, etc. to the initially requested scope
- Generated code includes extensive JavaDoc documentation including links to the official Microsoft documentation
- Supports capturing call state, structs of variable length, `cbSize` etc.
- Generates separate code for Windows x64 and ARM64 where necessary
- Available for both Maven and Gradle
- Plugins run on any platform (not just Windows)
- Based on Windows API metadata curated by Microsoft
- Metadata is built-in; no additional installations are required


## Getting Started

- [Getting Started with Maven](docs/getting_started_with_maven.md)
- [Getting Started with Gradle](docs/getting_started_with_gradle.md)


## Example Projects

- Show a Message Box (Java, Maven): [messagebox](examples/messagebox)
- Query the Windows Registry (Java, Gradle/Groovy): [registry](examples/registry)
- Enumerate Windows, demonstrating use of a callback function (Kotlin, Gradle/Kotlin): [enum_windows](examples/enum_windows)
- Display User Notification, showing use of COM interfaces (Java, Maven): [user_notification_java](examples/user_notification_java)
- Display User Notification, showing use of COM interfaces (Kotlin, Maven): [user_notification_kotlin](examples/user_notification_kotlin)


## Prerequisites

- Maven Plugin: Java 21 or higher, Maven 3.8.5 or higher, any platform
- Gradle Plugin: Java 21 or higher, any platform
- Generated code: Java 23 or higher, 64-bit Windows (x64 or ARM64)


## Further Documentation

- [Maven Plugin](docs/maven_plugin.md)
- [Gradle Plugin](docs/gradle_plugin.md)
- [Generated Code](docs/generated_code.md)
- [Working with COM Interfaces](docs/com_interfaces.md)


## Windows API Metadata

As part of the [win32metadata GitHub proejct](https://github.com/microsoft/win32metadata)
Microsoft maintains metadata for the Windows API (aka Win32 API).
The result is available in the [Microsoft.Windows.SDK.Win32Metadata NuGet package](https://www.nuget.org/packages/Microsoft.Windows.SDK.Win32Metadata).
The NuGet package mainly contains a `.winmd` file with the metadata.
[ECMA-335](https://www.ecma-international.org/publications-and-standards/standards/ecma-335/)
defines the format of `.winmd` files. It is the binary format used by .NET binaries.

This library includes the `Windows.Win32.winmd` file and uses it as the basis
for generating Windows API code.


## _Windows API Generator_ vs _Jextract_

[Jextract](https://github.com/openjdk/jextract/tree/master) is another tool for generating
Java bindings. This is a comparison of the two tools:

| Windows API Generator                                                                           | Jextract                                                                                     |
|:------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------|
| Generates code for using native libraries                                                       | Generates code for using native libraries                                                    |
| Code for Foreign Function and Memory API                                                        | Code for Foreign Function and Memory API                                                     |
| Restricted to Windows API                                                                       | Works with any native library                                                                |
| Processes Windows API metadata curated by Microsoft                                             | Processes C header files                                                                     |
| Metadata is built-in                                                                            | C header files and/or Windows SDK must be installed                                          |
| Code for functions, structs/unions, callback functions, enumerations, constants, COM interfaces | Code for functions, structs/unions, function pointers, constants                             |
| Code for calling COM interfaces and implementing them                                           | No support for COM interfaces                                                                |
| Generates code without `Typedef`s and names as intended by Microsoft                            | Generates unnecessary `Typedef`s and structs with internal names not documented by Microsoft |
| Determines dependencies and automatically generates all necessary code                          | Requires multiple iterations to manually specify all dependencies                            |
| Constants of different types: integer, string, GUID, property keys, etc.                        | Integer constants only                                                                       |
| Support for capturing call state (`GetLastError`)                                               | No support for capturing call state                                                          |
| Available as Maven and Gradle plugin                                                            | Available as a separate tool                                                                 |

