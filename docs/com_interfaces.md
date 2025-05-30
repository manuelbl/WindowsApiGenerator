# Working with COM Interfaces

The generated code for COM interfaces supports two use cases:

- _Calling methods of COM objects from Java_: The COM object is created and implemented by Windows and returned
  by a Windows API function. The generated code allows calling methods of the COM object as if it were a regular Java object.
  This use case is similar to a function downcall.

- _Implementing COM interfaces in Java_: The COM object is implemented in Java and then passed to a Windows API
  function expecting a COM object. Windows will then call methods of the Java object.
  This use case is similar to a function upcall.



## Calling COM Methods from Java

Windows API functions such as [`CreateUri`](https://learn.microsoft.com/en-us/previous-versions/windows/internet-explorer/ie-developer/platform-apis/ms775098(v=vs.85)) return a COM object.

In Java, the function call could look like this:

```java
var uriHolder = arena.allocate(ADDRESS);
var uriString = arena.allocateFrom("https://www.google.com", UTF_16LE);
var result = CreateUri(uriString, Uri_CREATE_CANONICALIZE, 0, uriHolder);
var uriPointer = uriHolder.get(IUri.addressLayout(), 0);
```

`uriPointer` is the pointer to the [`IUri`](https://learn.microsoft.com/en-us/previous-versions/windows/internet-explorer/ie-developer/platform-apis/ms775038(v=vs.85)) COM object (`MemorySegment`). It can be turned into an easily usable Java object using `wrap()`:

```java
 var uri = IUri.wrap(uriPointer);
```

`uri` is a Java object implementing the generated `IUri` interface:

```java
public interface IUri extends IUnknown {
    int GetPropertyBSTR(int uriProp, MemorySegment pbstrProperty, int dwFlags);
    int GetPropertyLength(int uriProp, MemorySegment pcchProperty, int dwFlags);
    int GetPropertyDWORD(int uriProp, MemorySegment pdwProperty, int dwFlags);
    int HasProperty(int uriProp, MemorySegment pfHasProperty);
    int GetAbsoluteUri(MemorySegment pbstrAbsoluteUri);
    int GetPort(MemorySegment pdwPort);
    ...
}
```

So a COM method call can be made like this:

```java
var portHolder = arena.allocate(JAVA_INT);
var result = uri.GetPort(portHolder);
var port = portHolder.get(JAVA_INT, 0);
```


## Implementing COM Interfaces in Java

Some Windows API functions take a pointer to a COM object as a parameter.
The COM object is implemented by the application, and its methods will be called by Windows.

An example is [`IUserNotification2::Show`](https://learn.microsoft.com/en-us/windows/win32/api/shobjidl/nf-shobjidl-iusernotification2-show).
It's actually a COM method (and not a function), and it even takes two pointers to COM objects:
a pointer to an implementation of [`IQueryContinue`](https://learn.microsoft.com/en-us/windows/win32/api/shobjidl_core/nn-shobjidl_core-iquerycontinue)
and an implementation of [`IUserNotificationCallback`](https://learn.microsoft.com/en-us/windows/win32/api/shobjidl/nn-shobjidl-iusernotificationcallback).

To call this method from Java, the expected COM objects must be implemented in Java.

The Windows API generator generates code supporting the implementation. This includes
a Java interface for the COM interface. For `IUserNotificationCallback`, it looks like this:

```java
public interface IUserNotificationCallback extends windows.win32.system.com.IUnknown {
    int OnBalloonUserClick(MemorySegment pt);
    int OnLeftClick(MemorySegment pt);
    int OnContextMenu(MemorySegment pt);
    
    // omitted code
}
```

The extended interface `IUnknown` looks like so:

```java

public interface IUnknown {
    int QueryInterface(MemorySegment riid, MemorySegment ppvObject);
    int AddRef();
    int Release();
    
    // omitted code
}
```

`IUnknown` is the base interface all COM objects must implement.
It takes care of reference counting and of querying interfaces
implemented by the same object.

To implement the `IUserNotificationCallback` object in Java, a Java class
implementing these interfaces needs to be written:

```java
public class UserNotificationCallback /* omitted code */ implements IUserNotificationCallback {
    // omitted code
  
    @Override
    public int OnBalloonUserClick(MemorySegment pt) {
        // additional code
        return S_OK;
    }

    @Override
    public int OnLeftClick(MemorySegment pt) {
        // additional code
        return S_OK;
    }

    @Override
    public int OnContextMenu(MemorySegment pt) {
        // additional code
        return S_OK;
    }
}
```

The implementation for `IUnknown` is very similar for all COM interfaces.
Thus, the code can be factored out.
[`ComObject.java`](../examples/user_notification_java/src/main/java/net/codecrete/windowsapi/examples/ComObject.java)
is a proposed implementation. The `ComObject` constructor takes an array interface IDs (IIDs) as a parameter.

The complete `IUserNotificationCallback` looks like so:

```java
public class UserNotificationCallback extends ComObject implements IUserNotificationCallback {
    private static final MemorySegment[] implementedIIDs
            = new MemorySegment[]{IUnknown.iid(), IUserNotificationCallback.iid()};

    public UserNotificationCallback() {
        super(implementedIIDs);
    }

    @Override
    public int OnBalloonUserClick(MemorySegment pt) { /* omitted */ }

    @Override
    public int OnLeftClick(MemorySegment pt) { /* omitted */ }

    @Override
    public int OnContextMenu(MemorySegment pt) { /* omitted */ }
}
```

The IIDs for `IUnknown` and `IUserNotificationCallback` are generated by the
Windows API generator.

To create an instance of the COM object that can be passed to native code,
a memory segment needs to be created that follows the layout of COM objects
and makes upcalls to the Java object if its methods are called.
This is achieved with the following code:

```java
    var callback = new UserNotificationCallback();
    var callbackSegment = IUserNotificationCallback.create(callback, arena);
    callback.setThisPointer(callbackSegment);
```

`callback` is the Java instance. `callbackSgement` is the native memory segment.
It is created with the `create()` function that the Windows API generator
adds to the `IUserNotificationCallback` interface.
The last line sets the memory segment (_this_ pointer) in the Java object
as the `ComObject` class requires it.

The `callbackSegment` can now be passed to the native code:

```java
    notification.Show(queryContinueSegment, 5000, callbackSegment);
```


### Memory Management

COM objects use reference counting. When the reference count is decremented to 0,
there are no references to the COM object anymore, and it can be deallocated.

Since there is no operation to release a single memory allocation with the Java
Foreign Function & Memory API, the Java implementation of `IUnknown::Release()`
will not release any memory. Instead, it is up to the Java application to
close the arena containing the COM object at an appropriate time.
In particular, the application should ensure that the arena outlives any
references to the COM object.


### Full Code

The full code for the above example is available, both for Java and Kotlin:

- [`User Notification (Java)`](../examples/user_notification_java)
- [`User Notification (Kotlin)`](../examples/user_notification_kotlin)
