//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples

import windows.win32.foundation.Constants.E_INVALIDARG
import windows.win32.foundation.Constants.E_NOINTERFACE
import windows.win32.system.com.IUnknown
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.ADDRESS
import java.lang.foreign.ValueLayout.JAVA_LONG

/**
 * Base class for COM objects implemented in Kotlin.
 *
 * Takes care of the `IUnknown` interface. The `thisPointer` instance variable
 * must be set before an instance is made available outside the JVM.
 *
 * Note that reference counting does not fully work. If the reference count reaches 0,
 * the object cannot free itself. It remains part of the arena it was allocated in.
 * Even more important, it must be ensured that the arena outlives any references to
 * the COM object.
 *
 * @param implementedIIDs an array of interface identifiers (IIDs) implemented by this object
 */
open class ComObject(protected val implementedIIDs: Array<MemorySegment>) : IUnknown {

    /**
     * Memory segment allocated for this COM object.
     *
     * The memory segment is allocated with the `create()` method
     * generated for the COM interface. It is passed to native
     * functions as the pointer to this COM object.
     */
    lateinit var thisPointer: MemorySegment

    private var refCount = 0

    override fun QueryInterface(
        riid: MemorySegment,
        ppvObject: MemorySegment
    ): Int {
        if (ppvObject.address() == 0L)
            return E_INVALIDARG

        if (implementedIIDs.any { equalIIDs(it, riid) }) {
            ppvObject.set(ADDRESS, 0, thisPointer)
            AddRef()
            return 0
        }

        ppvObject.set(JAVA_LONG, 0, 0L)
        return E_NOINTERFACE
    }

    override fun AddRef(): Int {
        refCount += 1
        return refCount
    }

    override fun Release(): Int {
        refCount -= 1
        return refCount
    }

    private fun equalIIDs(iid1: MemorySegment, iid2: MemorySegment) =
        iid1.get(JAVA_LONG, 0) == iid2.get(JAVA_LONG, 0) &&
                iid1.get(JAVA_LONG, 8) == iid2.get(JAVA_LONG, 8)
}
