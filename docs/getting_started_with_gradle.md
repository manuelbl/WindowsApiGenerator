# Getting Started with Gradle

_Note: These instructions are for Gradle with Groovy DSL. For Kotlin DSL, see the [enum_windows](../examples/enum_windows/README.md) example project._

## Step 1: Declare the Plugin

In your `build.gradle` file, declare the Windows API plugin in the `plugins` section:

```groovy
plugins {
    id 'net.codecrete.windows-api' version '0.8.0'
}
```


## Step 2: Configure the Plugin

In `build.gradle`, add a block to configure the plugin.

```groovy
generateWindowsApi {
    functions = [ "MessageBoxW" ]
}
```


## Step 3: Generate the Code

```bash
gradle generateWindowsApi
```

Alternatively, the task can be run from the IDE.


## Step 4: Use the Generated Code

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