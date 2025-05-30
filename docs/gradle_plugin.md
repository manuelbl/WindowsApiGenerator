# Windows API Gradle Plugin

The Windows API Gradle plugin generates Java code for Windows API functions and data structures.
It is available through Maven Central.

## Usage

To use the Java plugin, include the following in your build script:

**Groovy**
```groovy
plugins {
    id "net.codecrete.windows-api" version "0.8.0-SNAPSHOT"
}
```

**Kotlin**
```kotlin
plugins {
    id("net.codecrete.windows-api") version "0.8.0-SNAPSHOT"
}
```

### Repositories

Since the plugin is distributed through Maven Central, it needs to be included as a repository in the top-level `settings.gradle` or `settings.gradle.kts` file:

**Groovy DSL / Kotlin DSL**
```groovy
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
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

The properties `functions`, `structs`, `enumerations`, `callbackFunctions`, `comInterfaces` and `constants` determine the scope of Windows API elements to generate code.

The scope will automatically be expanded
to include all dependencies of the specified functions, structs, etc.
The function `GetWindowInfo`, as an example, takes a pointer to a `WINDOWINFO` struct.
It is sufficient to specify the function name in the configuration, and the plugin will automatically include the `WINDOWINFO` struct as well.


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
