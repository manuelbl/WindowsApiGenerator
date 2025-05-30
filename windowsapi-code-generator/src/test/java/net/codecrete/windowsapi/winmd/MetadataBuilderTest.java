//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.winmd;

import net.codecrete.windowsapi.metadata.Metadata;
import net.codecrete.windowsapi.metadata.Method;
import net.codecrete.windowsapi.metadata.Type;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataBuilderTest {
    private final Metadata metadata = MetadataBuilder.load();

    @Test
    void shouldContainSystemGuid() {
        assertThat(metadata.namespaces()).containsKey("System");
        assertThat(metadata.namespaces().get("System").types()).containsKey("Guid");
    }

    @Test
    void shouldHaveSplitArchitectureSpecificTypes() {
        assertThat(metadata.types()
                .map(Type::name)
                .filter(name -> name.endsWith("_X64")))
                .containsExactlyInAnyOrder(
                        "ARM64_NT_CONTEXT_X64",
                        "CONTEXT_X64",
                        "DISPATCHER_CONTEXT_X64",
                        "KNONVOLATILE_CONTEXT_POINTERS_X64",
                        "MINIDUMP_CALLBACK_INPUT_X64",
                        "MINIDUMP_THREAD_CALLBACK_X64",
                        "MINIDUMP_THREAD_EX_CALLBACK_X64",
                        "PGET_RUNTIME_FUNCTION_CALLBACK_X64",
                        "POUT_OF_PROCESS_FUNCTION_TABLE_CALLBACK_X64",
                        "PTERMINATION_HANDLER_X64",
                        "SLIST_HEADER_X64",
                        "UNWIND_HISTORY_TABLE_ENTRY_X64",
                        "UNWIND_HISTORY_TABLE_X64",
                        "VBS_BASIC_ENCLAVE_BASIC_CALL_RETURN_FROM_EXCEPTION_X64",
                        "VBS_BASIC_ENCLAVE_SYSCALL_PAGE_X64",
                        "WER_RUNTIME_EXCEPTION_INFORMATION_X64"
                );
        assertThat(metadata.types()
                .map(Type::name)
                .filter(name -> name.endsWith("_ARM64")))
                .containsExactlyInAnyOrder(
                        "CONTEXT_ARM64",
                        "DEBUG_PROCESSOR_IDENTIFICATION_ARM64",
                        "DISPATCHER_CONTEXT_ARM64",
                        "DISPATCHER_CONTEXT_NONVOLREG_ARM64",
                        "KNONVOLATILE_CONTEXT_POINTERS_ARM64",
                        "MINIDUMP_CALLBACK_INPUT_ARM64",
                        "MINIDUMP_THREAD_CALLBACK_ARM64",
                        "MINIDUMP_THREAD_EX_CALLBACK_ARM64",
                        "PGET_RUNTIME_FUNCTION_CALLBACK_ARM64",
                        "POUT_OF_PROCESS_FUNCTION_TABLE_CALLBACK_ARM64",
                        "PTERMINATION_HANDLER_ARM64",
                        "SCOPE_TABLE_ARM64",
                        "SLIST_HEADER_ARM64",
                        "UNWIND_HISTORY_TABLE_ARM64",
                        "UNWIND_HISTORY_TABLE_ENTRY_ARM64",
                        "VBS_BASIC_ENCLAVE_BASIC_CALL_RETURN_FROM_EXCEPTION_ARM64",
                        "VBS_BASIC_ENCLAVE_SYSCALL_PAGE_ARM64",
                        "WER_RUNTIME_EXCEPTION_INFORMATION_ARM64"
                );
    }

    @Test
    void shouldHaveSplitArchitectureSpecificMethods() {
        assertThat(metadata.methods()
                .map(Method::name)
                .filter(name -> name.endsWith("_X64")))
                .containsExactlyInAnyOrder(
                        "EnableProcessOptionalXStateFeatures_X64",
                        "GetEnabledXStateFeatures_X64",
                        "GetThreadEnabledXStateFeatures_X64",
                        "GetXStateFeaturesMask_X64",
                        "LocateXStateFeature_X64",
                        "RtlAddFunctionTable_X64",
                        "RtlAddGrowableFunctionTable_X64",
                        "RtlCaptureContext2_X64",
                        "RtlDeleteFunctionTable_X64",
                        "RtlInstallFunctionTableCallback_X64",
                        "RtlLookupFunctionEntry_X64",
                        "RtlVirtualUnwind_X64",
                        "SetXStateFeaturesMask_X64"
                );
        assertThat(metadata.methods()
                .map(Method::name)
                .filter(name -> name.endsWith("_ARM64")))
                .containsExactlyInAnyOrder(
                        "RtlAddFunctionTable_ARM64",
                        "RtlAddGrowableFunctionTable_ARM64",
                        "RtlDeleteFunctionTable_ARM64",
                        "RtlInstallFunctionTableCallback_ARM64",
                        "RtlLookupFunctionEntry_ARM64",
                        "RtlVirtualUnwind_ARM64"
                );
    }
}
