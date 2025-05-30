# Windows API Maven Plugin

The Windows API Maven plugin generates Java code for Windows API functions and data structures.
It is available through Maven Central.

The plugin runs in the `generate-sources` build lifecycle. So it can be executed from the command line with:

```bash
mvn generate-sources
```

Most other Maven commands will also generate the Windows API code as they include the `generate-sources` phase.

Alternatively, IDEs like IntelliJ IDEA have a separate button to generate sources.


## Basic Configuration

To use the plugin, add it to your `pom.xml` file in the `<plugins>` section of the `<build>` element
and configure an execution:

```xml
<build>
    <plugins>
        <plugin>
        <groupId>net.codecrete.windows-api</groupId>
        <artifactId>windowsapi-maven-plugin</artifactId>
        <version>0.8.0</version>
        <executions>
            <execution>
                <goals>
                    <goal>windows-api</goal>
                </goals>
                <configuration>
                    <functions>
                        <function>MessageBoxW</function>
                    </functions>
                </configuration>
            </execution>
        </executions>
        </plugin>
    </plugins>
</build>
```


## Configuration of Generation Scope

The scope of Windows API functions, structs, etc. to generate code for is defined in the `<configuration>` section with the below parameters.

The scope will automatically be expanded
to include all dependencies of the specified functions, structs, etc.
The function `GetWindowInfo`, as an example, takes a pointer to a `WINDOWINFO` struct.
It is sufficient to specify the function name in the configuration, and the plugin will automatically include the `WINDOWINFO` struct as well.


| Name                  | Type           | Description                                            |
|:----------------------|:---------------|:-------------------------------------------------------|
| `<functions>`         | `List<String>` | A list of function names.                              |
| `<structs>`           | `List<String>` | A list of struct and union names.                      |
| `<enumerations>`      | `List<String>` | A list of enumeration names.                           |
| `<callbackFunctions>` | `List<String>` | A list of callback function names (function pointers). |
| `<comInterfaces>`     | `List<String>` | A list of COM interface names.                         |
| `<constants>`         | `List<String>` | A list of constant names (function pointers).          |


## Configuration of Additional Generation Aspects

Additional configuration parameters control where the code is generated, how it is included
in the project, etc.:

| Name                     | Type      | Description                                                                                                                                                                                                                                    |
|:-------------------------|:----------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `<outputDirectory>`      | `Path`    | Path to the output directory for the generated code. The default is `generated-sources/windows-api` relative to the project's `build` directory.                                                                                               |
| `<sourceDirectory>`      | `String`  | The source directory within the output directory (relative path). Use forward slashes to separate directory names. The default is `src/main/java`.                                                                                             |
| `<basePackage>`          | `String`  | The base package for the generated Java classes. It is prepended to the package name derived from the namespace assigned by Microsoft, e.g. `windows.win32.ui.shell`. The default is an empty string, i.e., no additional names are prepended. |
| `<addAsSourceRoot>`      | `boolean` | If set to `true`, adds the source directory as a source root so the generated code will be compiled and included in the resulting artifact. The default is `true`.                                                                             |
| `<addAsTestSourceRoot>`  | `boolean` | If set to `true`, adds the source directory as a test source root so the generated code will be compiled and included for test. The default is `false`.                                                                                        |
| `<cleanOutputDirectory>` | `boolean` | If set to `true`, the output directory will be cleaned before code is generated. The default is `true`.                                                                                                                                        |


## Sample Configuration

```xml
<plugin>
    <groupId>net.codecrete.windows-api</groupId>
    <artifactId>windowsapi-maven-plugin</artifactId>
    <version>0.8.0</version>
    <executions>
        <execution>
        <goals>
            <goal>windows-api</goal>
        </goals>
        <configuration>
            <functions>
                <function>CreateFileW</function>
                <function>CreateIoCompletionPort</function>
                <function>RegQueryValueExW</function>
            </functions>
            <structs>
                <struct>DEV_BROADCAST_DEVICEINTERFACE_W</struct>
                <struct>DEV_BROADCAST_HDR</struct>
            </structs>
            <enumerations>
                <enumeration>FORMAT_MESSAGE_OPTIONS</enumeration>
                <enumeration>GENERIC_ACCESS_RIGHTS</enumeration>
            </enumerations>
            <constants>
                <constant>DEVPKEY_Device_Address</constant>
                <constant>DIREG_DEV</constant>
                <constant>HWND_MESSAGE</constant>
                <constant>INFINITE</constant>
            </constants>
            <outputDirectory>${project.build.directory}/generated-sources/win32</outputDirectory>
            <addAsSourceRoot>false</addAsSourceRoot>
            <addAsTestSourceRoot>true</addAsTestSourceRoot>
        </configuration>
        </execution>
    </executions>
</plugin>
```
