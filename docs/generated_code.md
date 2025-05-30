
# Generated Code

## Namespaces / Packages

In the underlying metadata, the Windows API is organized in namespaces. The generated code uses the same organization to create Java packages.


## Functions

The code for functions is generated in Java classes called `Apis`, separately for each namespace.

For each Windows function, three items are generated:

- A Java method to call the native function.
- A method returning the function descriptor: `xxx$descriptor()`.
- A method returning the method handle: `xxx$handle()`.

```java
public static FunctionDescriptor MessageBoxW$descriptor() { ... }

public static MethodHandle MessageBoxW$handle() { ...}

public static int MessageBoxW(MemorySegment lastErrorState, MemorySegment hWnd, MemorySegment lpText, MemorySegment lpCaption, int uType) { ... }
```

### Call State / `GetLastError()`

If the function uses `SetLastError`/`GetLastError`, an additional first parameter is added to the method to capture the call state (as `GetLastError` does not work in Java).

When the function is called, the capture state segment must be passed.
It can be allocated like this (also see [Linker.Option](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/lang/foreign/Linker.Option.html) for details):

```java
var errorStateLayout = Linker.Option.captureStateLayout();
VarHandle callStateLastErrorVarHandle =
            errorStateLayout.varHandle(PathElement.groupElement("GetLastError"));

var errorState = arena.allocate(errorStateLayout);

MessageBoxW(errorState, ...);

var errorCode = (int) callStateLastErrorVarHandle.get(callState, 0);
```

The captured state is an integer error code. Most of the values are part of the `WIN32_ERROR` enumeration.
So if the function uses call state, the `WIN32_ERROR` enumeration will be generated as well.



## Enumerations

Microsoft has organized many integer constants in enumerations. For them, a class with static integer constants is generated (not a Java enumeration).

```java
public class MESSAGEBOX_STYLE {

    public static final int MB_ABORTRETRYIGNORE = 2;

    public static final int MB_CANCELTRYCONTINUE = 6;
    ...
```

It is not always predictable which constants are organized in enumerations and which are not.
In most cases, relevant enumerations are automatically generated because they are used in a
function signature or as a struct member. If not, add the desired identifier as a constant
(in the plugin configuration). If it is not a constant but part of an enumeration, the code
generator error message will tell you the name of the enumeration.



## Structs and Unions

For each struct or union, a Java class of the same name is generated in the package/namespace
defined by Microsoft.

For a C struct like this:

```c
struct SIZE {
    LONG cx;
    LONG cy;
}
```

The generated Java class will look like this (private parts, implementation and comments omitted):

```java
public class SIZE {
    public static GroupLayout layout() { ...}

    public static long sizeof() { ...}

    public static long cx$offset() { ... }

    public static int cx(MemorySegment segment) { ...}

    public static void cx(MemorySegment segment, int value) { ... }

    public static long cy$offset() { ...}

    public static int cy(MemorySegment segment) { ... }

    public static void cy(MemorySegment segment, int value) { ... }

    public static MemorySegment allocate(SegmentAllocator allocator) { ...}

    public static MemorySegment elementAsSlice(MemorySegment array, long index) { ...}

    public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) { ... }
}
```


The generated class contains:

- A Java method returning the struct/union layout: `layout()`.
- A getter and setter for each field in the struct/union. The first parameter (`MemorySegment`) is a pointer to the struct/union.
- A method for each field returning the offset of the field in the struct/union: `xxx$offset()`.
- A method to allocate a new instance of the struct/union: `allocate()`
- A method to access an element in an array of the struct/union: `elementAsSlice()`.
- A method to allocate an array of the struct/union: `allocateArray()`.


### Getters / setters

Getters and setters are generated for each field in the struct/union. If the struct/union contains unnamed inner
structs, the getters/setters are generated for their fields as well. Their field name is built by concatenating the
outer and inner field names with an underscore. If the struct/union contains named inner structs, a separate
class will be generated for the inner struct and the getter/setter pair will access the entire inner struct.

Depending on the field type, the return type of the getter and the `value` parameter of the setter vary:

- For primitive C type such as `int` or `double`, the corresponding Java type is used.
- For C pointer types, `MemorySegment` is used. Where possible, the getter will return a `MemorySegment` instance
  with the correct size.
- For embedded structs/unions and for arrays, the getter returns a `MemorySegment` pointing to the struct/union
  field. It is a reference to the struct, not a copy of the field. The setter, on the other hand, takes a
  `MemorySegment` as parameter and copies the memory segment content it into the struct/union field.


### Variable Size Structs

The Windows API makes use of variable size structs. Such a struct contains an array as the last field.
The so-called _flexible array_ is typically declared with a size of 0 or 1. By allocating additional space
for the struct, the array can be made larger. Special code will be generated to handle such structs.

[`DEV_BROADCAST_DEVICEINTERFACE_W`](https://learn.microsoft.com/en-us/windows/win32/api/dbt/ns-dbt-dev_broadcast_deviceinterface_w)
is an example of a variable size struct. `dbcc_name` is the flexible array. Its size is not given explicitly.
The caller controls it by allocating the struct with the desired size. Windows assumes that `dbcc_name` contains
a null terminated string and that the array is long enough to hold it.

```c
struct DEV_BROADCAST_DEVICEINTERFACE_W {
   DWORD dbcc_size;
   DWORD dbcc_devicetype;
   DWORD dbcc_reserved;
   GUID dbcc_classguid;
   WCHAR[1] dbcc_name;
}
```

The following code related to its variable size is generated for the struct:

```java
public class DEV_BROADCAST_DEVICEINTERFACE_W {
    ...

    public static MemorySegment dbcc_name(MemorySegment segment) { ... }

    public static void dbcc_name(MemorySegment segment, MemorySegment value) { ...}

    public static MemorySegment allocate(SegmentAllocator allocator, int elementCount) { ... }
    ...
}
```

The getter and setter have the same signature as if the field was a normal array. But they behave
slightly differently. The getter returns a `MemorySegment` that starts at the beginning of the field
and extends to the end of the struct. Thus, the size of the array is derived from the allocated size of the struct.
Due to padding, the returned slice may be slightly larger than the actually required size.

The setter copies the memory segment content into the struct. The copied sized is the smaller of the
size `value` and the available space in `segment` (from the start of the flexible array to the end of the struct).

The `allocate()` method takes an additional parameter for the size of the flexible array.

Arrays of variable size structs do not exist. Thus, the methods for allocating an array of the struct
and for accessing an element in the array are not generated.

**Note**: The code for handling variable size structs cannot be generated in all cases. It is omitted
for cases where the flexible array is part of a union, or if other data structure overlay trickery is used.


### `cbSize`

Many structs in the Windows API contain a `cbSize` field. This field must be initialized to the size of the
struct. It is mainly used by Windows to determine what version of the data structure the application is using.
An application might have been compiled for an earlier version of Windows, and the struct has since been
expanded. Even if the data structure never contains, Windows usually checks that the field is initialized
and will return an error if it is not.

If the struct contains such a field, the `allocate()` method will initialize it automatically.


### Packed Structs / Unions

Packed structs and unions are structs whose fields are not aligned to their natural size.
Instead, they are usually aligned to 1 byte, either to create a more compact data structure
without padding, or because the data structure was designed in a different context,
.e.g., as part of a network protocol.

Packed structs and unions require the use of layouts with a different alignment.
The generated code will use an alignment of 1 byte in such cases. This applies to all
fields, including embedded structs and unions.

If the struct or union contains a named embedded struct or union, a separate layout is
generated for it. It has an alignment of 1 byte instead of the default alignment.

[`NT_CONSOLE_PROPS`](https://learn.microsoft.com/en-us/windows/win32/api/shlobj_core/ns-shlobj_core-nt_console_props)
is such an example. It is a packed struct, and it contains three fields with the named struct
[`COORD`](https://learn.microsoft.com/en-us/windows/console/coord-str).
`COORD` has an alignment of 2 bytes. But within `NT_CONSOLE_PROPS`, it is aligned to 1 byte.

```c
struct NT_CONSOLE_PROPS {
    ...
    COORD dwScreenBufferSize;
    COORD dwWindowSize;
    COORD dwWindowOrigin;
    ...
}
```

The generated layout for `COORD` is accessible via `NT_CONSOLE_PROPS.COORD$unalignedLayout()`.

Note that the access to the members of `COORD` still poses a challenge if it is contained within
a packed struct or union because the `COORD` getters and setters only work on aligned memory.

The following code will not work:

```java
var props = NT_CONSOLE_PROPS.allocate(arena);
var coord = props.dwScreenBufferSize();
COORD.x(coord, 100); // fails because coord is not aligned
```

Instead, the unaligned field must be accessed using basic `MemorySegment` methods
and an unaligned layout: 

```java
var props = NT_CONSOLE_PROPS.allocate(arena);
var coord = props.dwScreenBufferSize();
coord.set(JAVA_SHORT_UNALIGNED, COORD.X$offset(), 100);
```


### GUID

Many Windows API functions, structs and unions use a data type, which is called `GUID`
in the Windows C header files and in the documentation. It is similar to Java's `UUID` class.
The underlying metadata however calls it `Guid` (note the camel case name) and does
not provide any metadata for it. Instead, it is treated as if it were a built-in type 
ike _string_. The generator makes it available as `Guid` (note the camel case name)
in the package `system` (as defined by Microsoft).



## Callback Functions

Callback functions are functions that are passed as function pointers to Windows API functions
or data structures. They are called by Windows.

For a callback function like [`WNDPROC`](https://learn.microsoft.com/en-us/windows/win32/api/winuser/nc-winuser-wndproc):

```c
LRESULT WNDPROC(
    HWND param0,
    DWORD param1,
    WPARAM param2,
    LPARAM param3
);
```

The following class is generated (simplified):

```java
public class WNDPROC {

    public interface Function {
        long invoke(MemorySegment param0, int param1, long param2, long param3);
    }

    public static FunctionDescriptor descriptor() { ... }

    public static MemorySegment allocate(Arena arena, WNDPROC.Function function) { ...}

    public static long invoke(MemorySegment callbackFunction, MemorySegment param0, int param1, long param2, long param3) { ... }
```

It contains:
- A functional interface `Function` that defines the callback function signature.
  A function (or lambda) with this signature can implement the callback function.
- A method returning the function descriptor: `descriptor()`.
- A method to allocate an upcall stub for the provided callback function: `allocate()`.
- A method to invoke a function pointer with the given function signature: `invoke()`.

**Note**: Structs and unions do not use unnamed callback function types. Instead, they only
use named ones that lead to a class like the one above.


## Constants

The Windows API uses many constants such as error codes, strings, GUIDs, property keys, etc.
These constants are not part of any Windows DLL. When programming in C/C++,
they are defined in C headers files and become part of the application code.
While this is trivial for numeric constants, it involves macro trickery for strings, GUIDs, etc.

For each namespace with constants, a class called `Constants` is generated.

Depending on the constant type, the generated code will look like this:

```java
/// integer constant
public static final int STATE_SYSTEM_INDETERMINATE = 32;

/// floating-point constant
public static final float Speech_Default_Weight = 1.000000f;

/// pointer constant
public static final MemorySegment HWND_BOTTOM = MemorySegment.ofAddress(1L);

/// String constant (UTF-16 encoded)
public static MemorySegment VSCLASS_SCROLLBAR()

/// String constant (ANSI encoded)
public static MemorySegment DATETIMEPICK_CLASSA() { ... }

/// GUID constant
public static MemorySegment GUID_IO_DEVICE_EXTERNAL_REQUEST() { ... }

/// Property key constant
public static MemorySegment DEVPKEY_Device_SupportsVideo() { ... }
```


## COM Interfaces

For COM interfaces, a Java interface and helper methods are generated
to method fo COM objects can be called from Java and COM objects can be
implemented in Java.

For a COM interface like ['IMAPIProgress`](https://learn.microsoft.com/en-gb/office/client-developer/outlook/mapi/imapiprogressiunknown), the following code is generated (many implementation details omitted):


```java
public interface IMAPIProgress extends windows.win32.system.com.IUnknown {

    int Progress(int ulValue, int ulCount, int ulTotal);
    int GetFlags(MemorySegment lpulFlags);
    int GetMax(MemorySegment lpulMax);
    int GetMin(MemorySegment lpulMin);
    int SetLimits(MemorySegment lpulMin, MemorySegment lpulMax, MemorySegment lpulFlags);

    static IMAPIProgress wrap(MemorySegment comObject) { ... }

    static AddressLayout addressLayout() { ... }

    static MemorySegment create(IMAPIProgress obj, Arena arena) { ...}   }
}
```

The interface contains:

- The method signatures for the COM interface methods.
- A method to turn a pointer to a COM object (`MemorySegment`) into an easily callable Java object: `wrap()`.
- A method returning an address layout for pointers to COM objects of this type: `addressLayout()`.
- A method to turn a Java implementation of the COM interface into a `MemorySegment` that can be passed to native code: `create()`.

For details how to work with COM objects and how to implement COM interfaces in Java, see the [Working with COM Interfaces](com_interfaces.md).



## Comments

The code generator adds extensive Javadoc comments to the public parts of the code.
In many cases, the comments also contain a link to the original documentation on the Microsoft website.

```java
/**
 * {@code MessageBoxW} function
 * <p>
 * {@snippet lang=c :
 * MESSAGEBOX_RESULT MessageBoxW(
 *     HWND hWnd,
 *     PWSTR lpText,
 *     PWSTR lpCaption,
 *     MESSAGEBOX_STYLE uType
 * );
 * }
 * </p>
 * <p>
 * The additional first parameter takes a memory segment to capture the call state (replacement for {@code GetLastError()}).
 * </p>
 *
 * @see <a href="https://learn.microsoft.com/windows/win32/api/winuser/nf-winuser-messageboxw">MessageBoxW (Microsoft)</a>
 */
```
