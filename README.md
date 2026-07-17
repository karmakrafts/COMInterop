# COMInterop

[![](https://git.karmakrafts.dev/kk/cominterop/badges/master/pipeline.svg)](https://git.karmakrafts.dev/kk/cominterop/-/pipelines)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.maven.apache.org%2Fmaven2%2Fdev%2Fkarmakrafts%2Fcominterop%2Fcominterop-core%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/cominterop/-/packages)
[![](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fdev%2Fkarmakrafts%2Fcominterop%2Fcominterop-core%2Fmaven-metadata.xml
)](https://git.karmakrafts.dev/kk/cominterop/-/packages)
[![](https://img.shields.io/badge/2.4.10-blue?logo=kotlin&label=kotlin)](https://kotlinlang.org/)
[![](https://img.shields.io/badge/documentation-black?logo=kotlin)](https://docs.karmakrafts.dev/cominterop-core)

![](https://img.shields.io/badge/-MinGW-lightgray?logo=kotlin&labelColor=black)

A basic COM runtime for Kotlin/Native based on CInterop.  
This can be used to interop with WinRT/Windows SDK functionality otherwise only available through Microsoft compiler
extensions and C++ code.

### How to use it

First, add the official Maven Central repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

Then add a dependency on the library in your buildscript:

```kotlin
kotlin {
    mingwMain {
        dependencies {
            implementation("dev.karmakrafts.cominterop:cominterop-core:<version>")
        }
    }
}
```

### Initialization

Before using any COM APIs, you need to call `ComRuntime.init()` to ensure the runtime
is properly initialized. This will invoke `CoInitializeEx` and `RoInitialize` allowing multi-threaded use.

When you are done using the COM runtime, you also should ensure proper resource cleanup
using the provided `ComRuntime.uninit()` function.

```kotlin
fun main() {
    ComRuntime.init()
    // Your code here
    ComRuntime.uninit()
}
```

### Binding a COM interface

The primary use of this library is to bind COM interface into the Kotlin world, by providing wrapper classes
which can be constructed from arbitrary CInterop pointers.  
Much like in C/C++ on Windows, interfaces require an IID which uniquely identifies them. This IID is primarily
used to query one interface from another.

> Note: Interface IIDs can be obtained from the IDL files shipped with Windows and the Windows SDK.

Just like its OOP counterpart, a COM interface also contains/uses a v-table to invoke its implementation functions.  
The v-table is built by this library at runtime and cached throughout the lifetime of the program.

Let's assume we want to bind
the [IShellLinkW](https://learn.microsoft.com/en-us/windows/win32/api/shobjidl_core/nn-shobjidl_core-ishelllinkw)
interface:

```kotlin
class IShellLinkW : ComInterface<IShellLinkW.Companion>(Companion) {
    // Define the type of the function we want to invoke
    private typealias _GetPath = (
        self: COpaquePointer, pszFile: LPWSTR, cch: Int, pfd: CPointer<WIN32_FIND_DATAW>, fFlags: DWORD
    ) -> HRESULT

    // Define the interface type through its companion object
    companion object : ComInterfaceType {
        // Construct a v-table function list based on the IDL definitions
        override val functions: List<String> = VTableFunctionList.build {
            add("GetPath")
            addStubs(17) // Stub all functions we don't need
        }

        // Populate the IID pointed to by the given pointer with the right interface IID
        override fun getIID(iid: CPointer<IID>, iface: ComInterface<*>) {
            ComRuntime.iidFromString("{000214EE-0000-0000-C000-000000000046}", iid)
        }

        // Create a default instance of the interface
        override fun create(): ComInterface<*> = IShellLinkW()
    }

    // Bind against the GetPath function defined in the v-table; calculates address of the function
    private val GetPath: CPointer<CFunction<_GetPath>> by vTable

    // Expose the functionality as a high-level Kotlin property
    val path: String
        get() = memScoped {

        }
}
```

This will let us take a given `COpaquePointer`, and turn it into an instance of the `IShellLinkW` interface:

```kotlin
fun main() = memScoped {
    ComRuntime.init()
    val myPointer = someFunction()
    val shellLink = myPointer.asCom<IShellLinkW, _>(IShellLinkW)
    val path = shellLink.path // Internally calls GetPath on the COM interface
    shellLink.release()
    ComRuntime.uninit()
}
```

### Binding a COM class

COM interfaces become especially useful, if you can also construct them by one of their  
implementations directly from within the Kotlin code:

```kotlin
class INetworkListManager : ComInterface<INetworkListManager.Companion>(Companion) {
    // ...
}

// We can define COM classes as simple singleton objects
object NetworkListManager : ComClass<INetworkListManager.Companion> {
    override fun getCLSID(clsid: CPointer<CLSID>) {
        ComRuntime.iidFromString("{DCB00C01-570F-4A9B-8D69-199FDBA5723B}", clsid)
    }

    // The first interface in the inheritance list provided by the IDL definition
    override val defaultInterface: INetworkListManager.Companion = INetworkListManager
}
```

This will allow us to obtain a new `INetworkListManager` instance as follows:

```kotlin
fun main() {
    ComRuntime.init()
    val manager = NetworkListManager.new<INetworkListManager, _, _>()
    // Do something with the network manager instance
    manager.release()
    ComRuntime.uninit()
}
```

> Note: this is purely meant as example code, in reality `NetworkListManager` needs to be
> instantiated with the `CLSCTX_ALL` context flag.