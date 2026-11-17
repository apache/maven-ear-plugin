/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.plugins.ear.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Stephane Nicoll
 */
class JavaEEVersionTest {

    @Test
    void testGtSameVersion() {
        assertFalse(JavaEEVersion.FIVE.gt(JavaEEVersion.FIVE));
    }

    @Test
    void testGtNextVersion() {
        assertFalse(JavaEEVersion.FIVE.gt(JavaEEVersion.SIX));
    }

    @Test
    void testGtPreviousVersion() {
        assertTrue(JavaEEVersion.FIVE.gt(JavaEEVersion.ONE_DOT_FOUR));
    }

    @Test
    void testGeSameVersion() {
        assertTrue(JavaEEVersion.FIVE.ge(JavaEEVersion.FIVE));
    }

    @Test
    void testGePreviousVersion() {
        assertTrue(JavaEEVersion.FIVE.ge(JavaEEVersion.ONE_DOT_FOUR));
    }

    @Test
    void testGeNextVersion() {
        assertFalse(JavaEEVersion.FIVE.ge(JavaEEVersion.SIX));
    }

    @Test
    void testLtSameVersion() {
        assertFalse(JavaEEVersion.FIVE.lt(JavaEEVersion.FIVE));
    }

    @Test
    void testLtPreviousVersion() {
        assertFalse(JavaEEVersion.FIVE.lt(JavaEEVersion.ONE_DOT_FOUR));
    }

    @Test
    void testLtNextVersion() {
        assertTrue(JavaEEVersion.FIVE.lt(JavaEEVersion.SIX));
    }

    @Test
    void testLeSameVersion() {
        assertTrue(JavaEEVersion.FIVE.le(JavaEEVersion.FIVE));
    }

    @Test
    void testLePreviousVersion() {
        assertFalse(JavaEEVersion.FIVE.le(JavaEEVersion.ONE_DOT_FOUR));
    }

    @Test
    void testLeNextVersion() {
        assertTrue(JavaEEVersion.FIVE.le(JavaEEVersion.SIX));
    }

    @Test
    void testEqSameVersion() {
        assertTrue(JavaEEVersion.FIVE.eq(JavaEEVersion.FIVE));
    }

    @Test
    void testEqAnotherVersion() {
        assertFalse(JavaEEVersion.FIVE.eq(JavaEEVersion.ONE_DOT_THREE));
    }

    @Test
    void testGetVersion() {
        assertEquals("5", JavaEEVersion.FIVE.getVersion());
    }

    @Test
    void testGetJavaEEVersionValid() throws InvalidJavaEEVersion {
        assertEquals(JavaEEVersion.SIX, JavaEEVersion.getJavaEEVersion("6"));
    }

    @Test
    void testGetJavaEEVersionInvalid() {
        assertThrows(InvalidJavaEEVersion.class, () -> JavaEEVersion.getJavaEEVersion("2.4"));
    }

    @Test
    void testGetJavaEEVersionNull() {
        assertThrows(NullPointerException.class, () -> JavaEEVersion.getJavaEEVersion(null));
    }
}
