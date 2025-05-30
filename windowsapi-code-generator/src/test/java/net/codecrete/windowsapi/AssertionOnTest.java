//
// Windows API Generator for Java
// Copyright (c) 2025 Manuel Bleichenbacher
// Licensed under MIT License
// https://opensource.org/licenses/MIT
//
package net.codecrete.windowsapi;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssertionOnTest {

    @Test
    void assertions_areEnabled() {
        // ensure that assertions are enabled for unit tests
        assertThatThrownBy(this::throwAssertion).isInstanceOf(AssertionError.class);
    }

    private void throwAssertion() {
        assert false;
    }
}
