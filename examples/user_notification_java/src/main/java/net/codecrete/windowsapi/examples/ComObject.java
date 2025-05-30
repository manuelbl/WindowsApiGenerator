//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.examples;

import windows.win32.system.com.IUnknown;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static windows.win32.foundation.Constants.E_INVALIDARG;
import static windows.win32.foundation.Constants.E_NOINTERFACE;

/**
 * Base class for COM objects implemented in Kotlin.
 * <p>
 * Implements the `IUnknown` interface. The `thisPointer` instance variable
 * must be set before an instance is made available outside the JVM.
 * </p>
 * <p>
 * Note that reference counting is not linked to the memory management.
 * If the reference count reaches 0, the object cannot free itself.
 * It remains part of the arena it was allocated in.
 * And it must be ensured that the arena outlives any references to
 * the COM object.
 * </p>
 */
public class ComObject implements IUnknown {
    private final MemorySegment[] implementedIIDs;

    private MemorySegment thisPointer;

    private int refCount = 0;

    /**
     * Creates a new instance.
     *
     * @param implementedIIDs an array of interface identifiers (IIDs) implemented by this object
     */
    public ComObject(MemorySegment[] implementedIIDs) {
        this.implementedIIDs = implementedIIDs;
    }

    /**
     * Gets the memory segment allocated for this COM object.
     * <p>
     * It is passed to functions as the native pointer to this COM object.
     * </p>
     *
     * @return the {@code this} pointer
     */
    public MemorySegment getThisPointer() {
        return this.thisPointer;
    }

    /**
     * Sets the memory segment allocated for this COM object.
     * <p>
     * The memory segment is allocated with the {@code create()} method
     * generated for the COM interface. It is passed to functions as
     * the native pointer to this COM object.
     * </p>
     *
     * @param thisPointer the {@code this} pointer
     */
    public void setThisPointer(MemorySegment thisPointer) {
        this.thisPointer = thisPointer;
    }

    @Override
    public int QueryInterface(MemorySegment riid, MemorySegment ppvObject) {
        if (ppvObject.address() == 0L)
            return E_INVALIDARG;

        for (var iid : implementedIIDs) {
            if (equalIIDs(iid, riid)) {
                ppvObject.set(ADDRESS, 0L, thisPointer);
                AddRef();
                return 0;
            }
        }

        ppvObject.set(JAVA_LONG, 0L, 0L);
        return E_NOINTERFACE;
    }

    @Override
    public int AddRef() {
        refCount += 1;
        return refCount;
    }

    @Override
    public int Release() {
        refCount -= 1;
        return refCount;
    }

    private static boolean equalIIDs(MemorySegment iid1, MemorySegment iid2) {
        return iid1.get(JAVA_LONG, 0L) == iid2.get(JAVA_LONG, 0L)
                && iid1.get(JAVA_LONG, 8L) == iid2.get(JAVA_LONG, 8L);
    }
}
