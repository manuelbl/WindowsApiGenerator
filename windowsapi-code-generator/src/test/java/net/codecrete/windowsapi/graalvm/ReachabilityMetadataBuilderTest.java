//
// Windows API Generator for Java
// Copyright (c) 2026 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.graalvm;

import net.codecrete.windowsapi.graalvm.json.Downcall;
import net.codecrete.windowsapi.graalvm.json.DowncallLinkerOptions;
import net.codecrete.windowsapi.graalvm.json.Method;
import net.codecrete.windowsapi.graalvm.json.ReflectionObject;
import net.codecrete.windowsapi.graalvm.json.Upcall;
import net.codecrete.windowsapi.metadata.Metadata;
import net.codecrete.windowsapi.winmd.MetadataBuilder;
import net.codecrete.windowsapi.writer.Scope;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReachabilityMetadataBuilderTest {

    private static final Metadata metadata = MetadataBuilder.load();

    private static final DowncallLinkerOptions CAPTURE = new DowncallLinkerOptions(true);
    private static final DowncallLinkerOptions NO_CAPTURE = new DowncallLinkerOptions(false);

    private Scope scope(Set<String> functions, Set<String> comInterfaces) {
        var scope = new Scope(metadata, event -> {
            // ignore events
        });
        scope.addFunctions(functions);
        scope.addComInterfaces(comInterfaces);
        scope.buildTransitiveScope();
        return scope;
    }

    @Test
    void build_createsDowncallsForFunctionsAndComMethods() {
        var scope = scope(Set.of("RegisterClassW", "GetProcessMemoryInfo", "EnumWindows"), Set.of("IUnknown"));

        var reachabilityMetadata = new ReachabilityMetadataBuilder(scope.methods(), scope.getTransitiveTypeScope(), "").build();

        // sorted by returnType, then parameterTypes, then captureCallState
        assertThat(reachabilityMetadata.foreign().downcalls()).containsExactly(
                // IUnknown.AddRef / IUnknown.Release (leading "this" pointer)
                new Downcall("jint", List.of("void*"), NO_CAPTURE),
                // EnumWindows(WNDENUMPROC, LPARAM)
                new Downcall("jint", List.of("void*", "jlong"), CAPTURE),
                // GetProcessMemoryInfo(HANDLE, PROCESS_MEMORY_COUNTERS*, DWORD)
                new Downcall("jint", List.of("void*", "void*", "jint"), CAPTURE),
                // IUnknown.QueryInterface(this, REFIID, void**)
                new Downcall("jint", List.of("void*", "void*", "void*"), NO_CAPTURE),
                // RegisterClassW(WNDCLASSW*) returning ATOM
                new Downcall("jshort", List.of("void*"), CAPTURE)
        );
    }

    @Test
    void build_createsDowncallsForFunctions() {
        var scope = scope(
                Set.of("MessageBoxW", "EnumWindows", "GetWindowTextW", "GetWindowInfo", "FormatMessageW", "GetModuleHandleW", "LocalFree"),
                Set.of()
        );

        var reachabilityMetadata = new ReachabilityMetadataBuilder(scope.methods(), scope.getTransitiveTypeScope(), "").build();

        // sorted by returnType, then parameterTypes, then captureCallState
        assertThat(reachabilityMetadata.foreign().downcalls()).containsExactly(
                // FormatMessageW
                new Downcall("jint", List.of("jint", "void*", "jint", "jint", "void*", "jint", "void*"), CAPTURE),
                // EnumWindows
                new Downcall("jint", List.of("void*", "jlong"), CAPTURE),
                // GetWindowInfo
                new Downcall("jint", List.of("void*", "void*"), CAPTURE),
                // GetWindowTextW
                new Downcall("jint", List.of("void*", "void*", "jint"), CAPTURE),
                // MessageBoxW
                new Downcall("jint", List.of("void*", "void*", "void*", "jint"), CAPTURE),
                // GetModuleHandleW /LocalFree
                new Downcall("void*", List.of("void*"), CAPTURE)
        );

        assertThat(reachabilityMetadata.foreign().upcalls()).containsExactly(
                // WNDENUMPROC
                new Upcall("jint", List.of("void*", "jlong"))
        );

        assertThat(reachabilityMetadata.reflection()).containsExactly(
                new ReflectionObject("windows.win32.ui.windowsandmessaging.WNDENUMPROC$Function",
                        List.of(new Method("invoke", List.of("java.lang.foreign.MemorySegment", "long"))))
        );
    }

    @Test
    void build_createsUpcallsForCallbackFunctions() {
        var scope = scope(Set.of("RegisterClassW", "EnumWindows"), Set.of());

        var reachabilityMetadata = new ReachabilityMetadataBuilder(scope.methods(), scope.getTransitiveTypeScope(), "").build();

        assertThat(reachabilityMetadata.foreign().upcalls()).containsExactly(
                // WNDENUMPROC(HWND, LPARAM) returning BOOL
                new Upcall("jint", List.of("void*", "jlong")),
                // WNDPROC(HWND, UINT, WPARAM, LPARAM) returning LRESULT
                new Upcall("jlong", List.of("void*", "jint", "jlong", "jlong"))
        );
    }

    @Test
    void build_createsReflectionEntryPerDelegate() {
        var scope = scope(Set.of("RegisterClassW", "EnumWindows"), Set.of());

        var reachabilityMetadata = new ReachabilityMetadataBuilder(scope.methods(), scope.getTransitiveTypeScope(), "com.example").build();

        // one entry per delegate (not deduplicated by signature), sorted by fully-qualified type name
        assertThat(reachabilityMetadata.reflection()).containsExactly(
                // WNDENUMPROC(HWND, LPARAM)
                new ReflectionObject(
                        "com.example.windows.win32.ui.windowsandmessaging.WNDENUMPROC$Function",
                        List.of(new Method("invoke",
                                List.of("java.lang.foreign.MemorySegment", "long")))),
                // WNDPROC(HWND, UINT, WPARAM, LPARAM)
                new ReflectionObject(
                        "com.example.windows.win32.ui.windowsandmessaging.WNDPROC$Function",
                        List.of(new Method("invoke",
                                List.of("java.lang.foreign.MemorySegment", "int", "long", "long"))))
        );
    }

    @Test
    void build_deduplicatesIdenticalSignatures() {
        // both take a single HWND pointer and return BOOL, so they collapse into one downcall
        var scope = scope(Set.of("IsWindow", "IsWindowVisible"), Set.of());

        var reachabilityMetadata = new ReachabilityMetadataBuilder(scope.methods(), scope.getTransitiveTypeScope(), "").build();

        assertThat(reachabilityMetadata.foreign().downcalls())
                .containsExactly(new Downcall("jint", List.of("void*"), NO_CAPTURE));
    }

    @Test
    void build_isDeterministic() {
        var scope = scope(Set.of("RegisterClassW", "GetProcessMemoryInfo", "EnumWindows"), Set.of("IUnknown"));

        var first = new ReachabilityMetadataBuilder(scope.methods(), scope.getTransitiveTypeScope(), "").build();
        var second = new ReachabilityMetadataBuilder(scope.methods(), scope.getTransitiveTypeScope(), "").build();

        assertThat(first).isEqualTo(second);
    }

    @Test
    void build_withEmptyScope_producesEmptyConfiguration() {
        var scope = scope(Set.of(), Set.of());

        var reachabilityMetadata = new ReachabilityMetadataBuilder(scope.methods(), scope.getTransitiveTypeScope(), "").build();

        assertThat(reachabilityMetadata.foreign().downcalls()).isEmpty();
        assertThat(reachabilityMetadata.foreign().upcalls()).isEmpty();
        assertThat(reachabilityMetadata.reflection()).isEmpty();
    }
}
