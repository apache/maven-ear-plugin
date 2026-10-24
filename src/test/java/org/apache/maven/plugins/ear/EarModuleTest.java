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
package org.apache.maven.plugins.ear;

import java.lang.reflect.Field;
import java.util.Collections;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ear module test case.
 *
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
class EarModuleTest {

    @Test
    void testCleanArchivePath() {
        assertEquals("APP-INF/lib/", AbstractEarModule.cleanArchivePath("APP-INF/lib"));
        assertEquals("APP-INF/lib/", AbstractEarModule.cleanArchivePath("APP-INF/lib/"));
        assertEquals("APP-INF/lib/", AbstractEarModule.cleanArchivePath("/APP-INF/lib"));
        assertEquals("APP-INF/lib/", AbstractEarModule.cleanArchivePath("/APP-INF/lib/"));
        assertEquals("", AbstractEarModule.cleanArchivePath("/"));
        assertEquals("", AbstractEarModule.cleanArchivePath(""));
        assertNull(AbstractEarModule.cleanArchivePath(null));
    }

    @Test
    void testResolveArtifactRequiresExecutionContext() throws Exception {
        JarModule module = new JarModule();
        setField(module, "groupId", "groupId");
        setField(module, "artifactId", "artifactId");

        MojoFailureException exception = assertThrows(
                MojoFailureException.class, () -> module.resolveArtifact(Collections.emptySet()));

        assertEquals("Ear execution context not initialized for module jar:groupId:artifactId", exception.getMessage());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = AbstractEarModule.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
