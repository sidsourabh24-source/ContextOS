package com.contextos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContextOSApplicationTests {

    @Test
    void mainClassLoads() {
        assertNotNull(ContextOSApplication.class);
    }
}
