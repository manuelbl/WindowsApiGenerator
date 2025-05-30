//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi.metadata;

import net.codecrete.windowsapi.winmd.MetadataBuilder;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FlexibleArrayTest {

    private final Metadata metadata = MetadataBuilder.load();

    private final Set<String> ignoredStructNames = Set.of(
            "IMAGEHLP_SYMBOL64_PACKAGE", // overlay of the same array at 2 levels, fixed and flexible
            "IMAGEHLP_SYMBOLW64_PACKAGE", // overlay of the same array at 2 levels, fixed and flexible
            "SYMBOL_INFO_PACKAGE", // overlay of the same array at 2 levels
            "SYMBOL_INFO_PACKAGEW", // overlay of the same array at 2 levels
            "DUMP_HEADER32",
            "DUMP_HEADER64",
            "KSSTREAMALLOCATOR_STATUS_EX"
    );

    @Test
    void flexibleArray_isLastMember() {
        metadata.types().filter(Struct.class::isInstance).map(Struct.class::cast)
                .filter(struct -> struct.namespace() != null && !struct.isUnion())
                .filter(struct -> !ignoredStructNames.contains(struct.name()))
                .forEach(this::validateFlexibleArray);
    }

    void validateFlexibleArray(Struct struct) {
        var flexibleMember = findFirstFlexibleMember(struct);
        flexibleMember.ifPresent(member -> {
                    var lastMember = struct.members().getLast();
                    assertThat(member)
                            .as("Flexible member in struct %s is %s instead of last member %s",
                                    struct.name(), member.name(), lastMember.name())
                            .isSameAs(lastMember);
                }
        );
    }

    private Optional<Member> findFirstFlexibleMember(Struct struct) {
        return struct.members().stream().filter(this::isFlexible).findFirst();
    }

    private boolean isFlexible(Member member) {
        return switch (member.type()) {
            case Array array -> array.isFlexible();
            case Struct struct -> findFirstFlexibleMember(struct).isPresent();
            default -> false;
        };
    }
}
