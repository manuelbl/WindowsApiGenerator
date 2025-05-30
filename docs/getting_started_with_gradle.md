# Getting Started with Gradle

_Note: These instructions are for Gradle with Groovy DSL. For Kotlin DSL, see the [enum_windows](../examples/enum_windows/README.md) example project._

## Step 1: Add the Maven repository

At the very top of `settings.gradle`, add the Maven repository where the Windows API plugin is hosted. The Gradle plugin portal (`gradlePluginPortal()`) is the default repository and needs to be specified as well:

```groovy
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
```


## Step 2: Declare the Plugin

In your `build.gradle` file, declare the Windows API plugin in the `plugins` section:

```groovy
plugins {
    id 'net.codecrete.windows-api' version '0.8.0'
}
```


## Step 3: Configure the Plugin

In `build.gradle`, add a block to configure the plugin.

```groovy
generateWindowsApi {
    functions = [ "MessageBoxW" ]
}
```


## Step 4: Generate the Code

```bash
gradle generateWindowsApi
```

Alternatively, the task can be run from the IDE.


## Step 5: Use the Generated Code

The generated code can now be used in Java or Kotlin code:

```java
var errorStateLayout = Linker.Option.captureStateLayout();

try (var arena = Arena.ofConfined()) {
    var errorState = arena.allocate(errorStateLayout);

    MessageBoxW(
            errorState,
            NULL,
            arena.allocateFrom("Hello, World!", UTF_16LE),
            arena.allocateFrom("Windows API", UTF_16LE),
            MESSAGEBOX_STYLE.MB_OKCANCEL
    );
}
```

Note that the code generator did not only generate the `MessageBoxW` function but also additional classes, e.g., the `MESSAGEBOX_STYLE` class, which is used as the last parameter.


## Next Steps

- [Gradle Plugin Reference](gradle_plugin.md)
- [Generated Code](generated_code.md)