//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.writer;

import net.codecrete.windowsapi.events.Event;
import net.codecrete.windowsapi.events.EventListener;
import net.codecrete.windowsapi.metadata.ConstantValue;
import net.codecrete.windowsapi.metadata.Metadata;
import net.codecrete.windowsapi.metadata.Method;
import net.codecrete.windowsapi.metadata.Type;
import net.codecrete.windowsapi.winmd.MetadataBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ScopeTest {

    private static final Metadata metadata = MetadataBuilder.load();
    private final List<Event> recordedEvents = new ArrayList<>();
    private final EventListener eventListener = recordedEvents::add;

    @Test
    void buildScope_resultIsCorrect() {
        var scope = new Scope(metadata, eventListener);
        scope.addFunctions(Set.of("RegisterClassW", "GetProcessMemoryInfo"));
        scope.buildTransitiveScope();

        assertThat(scope.getTransitiveTypeScope())
                .extracting(Type::name)
                .containsExactlyInAnyOrder("WNDCLASSW", "WNDPROC", "PROCESS_MEMORY_COUNTERS", "WNDCLASS_STYLES", "WIN32_ERROR");
    }

    @Test
    void addStructs_acceptsValidName() {
        var scope = new Scope(metadata, eventListener);
        scope.addStructs(Set.of("BITMAPINFO"));

        assertThat(recordedEvents).isEmpty();
        assertThat(scope.types()).singleElement().extracting(Type::name).isEqualTo("BITMAPINFO");
        assertThat(scope.hasInvalidArguments()).isFalse();
    }

    @Test
    void addStructs_addsArchitectureVariants() {
        var scope = new Scope(metadata, eventListener);
        scope.addStructs(Set.of("WER_RUNTIME_EXCEPTION_INFORMATION"));

        assertThat(scope.types()).hasSize(2)
                .extracting(Type::name).containsExactlyInAnyOrder("WER_RUNTIME_EXCEPTION_INFORMATION_X64",
                        "WER_RUNTIME_EXCEPTION_INFORMATION_ARM64");
        assertThat(scope.hasInvalidArguments()).isFalse();
    }

    @Test
    void addStructs_rejectsInvalidName() {
        var scope = new Scope(metadata, eventListener);
        scope.addStructs(Set.of("Invalid"));

        assertThat(recordedEvents).singleElement().satisfies(event -> {
            assertThat(event).isInstanceOf(Event.InvalidArgument.class);
            assertThat(((Event.InvalidArgument) event).reason()).contains("Struct/union \"Invalid\" does not exist");
        });
        assertThat(scope.hasInvalidArguments()).isTrue();
    }

    @Test
    void addStructs_proposesAlternative() {
        var scope = new Scope(metadata, eventListener);
        scope.addStructs(Set.of("LOGFONT"));

        assertThat(recordedEvents).singleElement().satisfies(event -> {
            assertThat(event).isInstanceOf(Event.InvalidArgument.class);
            assertThat(((Event.InvalidArgument) event).reason()).contains("Did you mean \"LOGFONTW\"");
        });
        assertThat(scope.hasInvalidArguments()).isTrue();
    }

    @Test
    void addEnums_acceptsValidName() {
        var scope = new Scope(metadata, eventListener);
        scope.addEnums(Set.of("BRUSH_STYLE"));

        assertThat(recordedEvents).isEmpty();
        assertThat(scope.types()).singleElement().extracting(Type::name).isEqualTo("BRUSH_STYLE");
        assertThat(scope.hasInvalidArguments()).isFalse();
    }

    @Test
    void addEnums_rejectsInvalidName() {
        var scope = new Scope(metadata, eventListener);
        scope.addEnums(Set.of("Invalid"));

        assertThat(recordedEvents).singleElement().satisfies(event -> {
            assertThat(event).isInstanceOf(Event.InvalidArgument.class);
            assertThat(((Event.InvalidArgument) event).reason()).contains("Enumeration \"Invalid\" does not exist");
        });
        assertThat(scope.hasInvalidArguments()).isTrue();
    }

    @Test
    void addCallbackFunctions_acceptsValidName() {
        var scope = new Scope(metadata, eventListener);
        scope.addCallbackFunctions(Set.of("URegexFindProgressCallback"));

        assertThat(recordedEvents).isEmpty();
        assertThat(scope.types()).singleElement().extracting(Type::name).isEqualTo("URegexFindProgressCallback");
        assertThat(scope.hasInvalidArguments()).isFalse();
    }

    @Test
    void addCallbackFunctions_rejectsInvalidName() {
        var scope = new Scope(metadata, eventListener);
        scope.addCallbackFunctions(Set.of("Invalid"));

        assertThat(recordedEvents).singleElement().satisfies(event -> {
            assertThat(event).isInstanceOf(Event.InvalidArgument.class);
            assertThat(((Event.InvalidArgument) event).reason()).contains("Callback function \"Invalid\" " +
                    "does not exist");
        });
        assertThat(scope.hasInvalidArguments()).isTrue();
    }

    @Test
    void addCallbackFunctions_proposesAlternative() {
        var scope = new Scope(metadata, eventListener);
        scope.addCallbackFunctions(Set.of("FONTENUMPROC"));

        assertThat(recordedEvents).singleElement().satisfies(event -> {
            assertThat(event).isInstanceOf(Event.InvalidArgument.class);
            assertThat(((Event.InvalidArgument) event).reason()).contains("Did you mean \"FONTENUMPROCW\"");
        });
        assertThat(scope.hasInvalidArguments()).isTrue();
    }

    @Test
    void addComInterfaces_acceptsValidName() {
        var scope = new Scope(metadata, eventListener);
        scope.addComInterfaces(Set.of("IUnknown"));

        assertThat(recordedEvents).isEmpty();
        assertThat(scope.types()).singleElement().extracting(Type::name).isEqualTo("IUnknown");
        assertThat(scope.hasInvalidArguments()).isFalse();
    }

    @Test
    void addComInterfaces_rejectsInvalidName() {
        var scope = new Scope(metadata, eventListener);
        scope.addComInterfaces(Set.of("Invalid"));

        assertThat(recordedEvents).singleElement().satisfies(event -> {
            assertThat(event).isInstanceOf(Event.InvalidArgument.class);
            assertThat(((Event.InvalidArgument) event).reason()).contains("COM interface \"Invalid\" does not exist");
        });
        assertThat(scope.hasInvalidArguments()).isTrue();
    }

    @Test
    void addConstants_acceptsValidName() {
        var scope = new Scope(metadata, eventListener);
        scope.addConstants(Set.of("S_OK"));

        assertThat(recordedEvents).isEmpty();
        assertThat(scope.constants()).singleElement().extracting(ConstantValue::name).isEqualTo("S_OK");
        assertThat(scope.hasInvalidArguments()).isFalse();
    }

    @Test
    void addConstants_rejectsInvalidName() {
        var scope = new Scope(metadata, eventListener);
        scope.addConstants(Set.of("Invalid"));

        assertThat(recordedEvents).singleElement().satisfies(event -> {
            assertThat(event).isInstanceOf(Event.InvalidArgument.class);
            assertThat(((Event.InvalidArgument) event).reason()).contains("Constant \"Invalid\" does not exist");
        });
        assertThat(scope.hasInvalidArguments()).isTrue();
    }

    @Test
    void addConstants_proposesAlternative() {
        var scope = new Scope(metadata, eventListener);
        scope.addConstants(Set.of("ERROR_ACCESS_DENIED"));

        assertThat(recordedEvents).singleElement().satisfies(event -> {
            assertThat(event).isInstanceOf(Event.InvalidArgument.class);
            assertThat(((Event.InvalidArgument) event).reason()).contains("Enumeration \"WIN32_ERROR\" contains a " +
                    "member with that name");
        });
        assertThat(scope.hasInvalidArguments()).isTrue();
    }

    @Test
    void addFunctions_acceptsValidName() {
        var scope = new Scope(metadata, eventListener);
        scope.addFunctions(Set.of("BitBlt"));

        assertThat(recordedEvents).isEmpty();
        assertThat(scope.methods()).singleElement().extracting(Method::name).isEqualTo("BitBlt");
        assertThat(scope.hasInvalidArguments()).isFalse();
    }

    @Test
    void addFunctions_addsArchitectureVariants() {
        var scope = new Scope(metadata, eventListener);
        scope.addFunctions(Set.of("RtlLookupFunctionEntry"));

        assertThat(scope.methods()).hasSize(2)
                .extracting(Method::name).containsExactlyInAnyOrder("RtlLookupFunctionEntry_X64",
                        "RtlLookupFunctionEntry_ARM64");
        assertThat(scope.hasInvalidArguments()).isFalse();
    }

    @Test
    void addFunctions_rejectsInvalidName() {
        var scope = new Scope(metadata, eventListener);
        scope.addFunctions(Set.of("Invalid"));

        assertThat(recordedEvents).singleElement().satisfies(event -> {
            assertThat(event).isInstanceOf(Event.InvalidArgument.class);
            assertThat(((Event.InvalidArgument) event).reason()).contains("Function \"Invalid\" does not exist");
        });
        assertThat(scope.hasInvalidArguments()).isTrue();
    }

    @Test
    void addFunctions_proposesAlternative() {
        var scope = new Scope(metadata, eventListener);
        scope.addFunctions(Set.of("MessageBox"));

        assertThat(recordedEvents).singleElement().satisfies(event -> {
            assertThat(event).isInstanceOf(Event.InvalidArgument.class);
            assertThat(((Event.InvalidArgument) event).reason()).contains("Did you mean \"MessageBoxW\"");
        });
        assertThat(scope.hasInvalidArguments()).isTrue();
    }
}
