# Update Metadata

- Go to https://www.nuget.org/packages/Microsoft.Windows.SDK.Win32Metadata
- Copy the "Download package" link on the right hand side of the page
- Execute:
    ```bash
    curl -L -o win32metadata.zip <download-link>
    unzip -p win32metadata.zip Windows.Win32.winmd > windows-api/code-generator/src/main/resources/Windows.Win32.winmd
    rm win32metadata.zip
    ```
- Run the unit tests
    ```bash
    cd windows-api
    mvn clean install
    ```
- Change the unit tests if necessary (e.g. total number of generated files)
