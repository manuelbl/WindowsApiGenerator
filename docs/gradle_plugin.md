# Windows API Gradle Plugin

The Windows API Gradle plugin generates Java code for Windows API functions and data structures.
It is available through Maven Central.

## Usage

To use the Java plugin, include the following in your build script:

**Groovy**
```groovy
plugins {
    id "net.codecrete.windows-api" version "0.8.6"
}
```

**Kotlin**
```kotlin
plugins {
    id("net.codecrete.windows-api") version "0.8.6"
}
```

## Tasks

The plugin adds a number of tasks to the project:

- **generateWindowsApi**: Generates Windows API  code and adds it to the main Java source set.
- **generateTestWindowsApi**: Generates Windows API code and adds it to the test Java source set.
- **generate*SourceSet*WindowsApi**: For any additional source set, a generation task is added with a name derived from the source set name.

The plugin adds dependencies to Java tasks:

| Task name              | Depends on                    |
|:-----------------------|:------------------------------|
| `compileJava`          | `generateWindowsApi`          |
| `compileTestJava`      | `generateTestWindowsApi`      |
| `compileSourceSetJava` | `generateSourceSetWindowsApi` |

In addition to the generator tasks bound the source sets, it is also possible to add
additional generator tasks by creating instance of the `WindowsApiTask` class (in namespace `net.codecrete.windowsapi.gradle`).



## Task Properties

| Property            | Type                   | Description                                                                                                                                                                                                                                    |
|:--------------------|:-----------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `functions`         | `ListProperty<String>` | A list of Windows API function names to generate bindings for.                                                                                                                                                                                 |
| `structs`           | `ListProperty<String>` | A list of Windows API struct and union names to generate bindings for.                                                                                                                                                                         |
| `enumerations`      | `ListProperty<String>` | A list of Windows API enumeration names to generate bindings for.                                                                                                                                                                              |
| `callbackFunctions` | `ListProperty<String>` | A list of Windows API callback function names (function pointers) to generate bindings for.                                                                                                                                                    |
| `comInterfaces`     | `ListProperty<String>` | A list of Windows API COM interface names to generate bindings for.                                                                                                                                                                            |
| `constants`         | `ListProperty<String>` | A list of Windows API constant names to generate bindings for.                                                                                                                                                                                 |
| `outputDirectory`   | `DirectoryProperty`    | The output directory for the generated Java code. Defaults to `generated/windowsapi` relative to the project's build directory.                                                                                                                |
| `basePackage`       | `Property<String>`     | The base package for the generated Java classes. It is prepended to the package name derived from the namespace assigned by Microsoft, e.g. `windows.win32.ui.shell`. The default is an empty string, i.e., no additional names are prepended. |
| `downcallTracing`   | `Property<Boolean>`    | Controls if additional code is generated to trace FFM downcalls. If set to `true` and if the system property `windowsapi.trace.downcalls` is set `true`, the generated code will output the function name and parameters before each downcall. If set to `false`, no additional code will be generated and the system property has no effect. |
| `reachabilityMetadataFile` | `RegularFileProperty` | Path to reachability metadata JSON file. If this property is set, a reachability metadata file for GraalVM is generated. |


The properties `functions`, `structs`, `enumerations`, `callbackFunctions`, `comInterfaces` and `constants` determine the scope of Windows API elements to generate code.

The scope will automatically be expanded
to include all dependencies of the specified functions, structs, etc.
The function `GetWindowInfo`, as an example, takes a pointer to a `WINDOWINFO` struct.
It is sufficient to specify the function name in the configuration, and the plugin will automatically include the `WINDOWINFO` struct as well.

After each successful generation run, Windows API Generator deletes all Java files in the output directory
that have not been generated by the current run. It thus cleans up files and empty directories
originating from previous generation runs with a different configuration.



## Example Configuration

**Groovy DSL**

```groovy
generateWindowsApi {
    functions = [
        "RegOpenKeyExW",
        "RegQueryValueExW",
        "RegCloseKey"
    ]
    constants = [
        "HKEY_CLASSES_ROOT",
        "HKEY_CURRENT_CONFIG",
        "HKEY_CURRENT_USER",
        "HKEY_LOCAL_MACHINE",
        "HKEY_USERS"
    ]
}
```

**Kotlin DSL**

```kotlin
tasks.generateWindowsApi {
    functions = listOf(
        "RegOpenKeyExW",
        "RegQueryValueExW",
        "RegCloseKey"
    )
    constants = listOf(
        "HKEY_CLASSES_ROOT",
        "HKEY_CURRENT_CONFIG",
        "HKEY_CURRENT_USER",
        "HKEY_LOCAL_MACHINE",
        "HKEY_USERS"
    )
}
```
