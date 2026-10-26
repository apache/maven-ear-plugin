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

import java.util.Collections;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.ear.stub.ArtifactHandlerTestStub;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ear module test case.
 *
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
class EarModuleTest {

    private static class ContextDependentTypeModule extends AbstractEarModule {
        ContextDependentTypeModule(String groupId, String artifactId) {
            super(groupId, artifactId, "jar");
        }

        @Override
        public String getType() {
            if (earExecutionContext == null) {
                throw new IllegalStateException("Execution context is required to resolve the module type");
            }
            return super.getType();
        }

        @Override
        public void appendModule(org.codehaus.plexus.util.xml.XMLWriter writer, String version, Boolean generateId) {}
    }

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
    void testResolveArtifactRequiresExecutionContext() {
        DefaultArtifact artifact = new DefaultArtifact(
                "groupId", "artifactId", "1.0", "compile", "jar", null, new ArtifactHandlerTestStub("jar"));
        JarModule module = new JarModule(artifact, null, false);

        MojoFailureException exception =
                assertThrows(MojoFailureException.class, () -> module.resolveArtifact(Collections.emptySet()));

        assertTrue(exception.getMessage().contains("Ear execution context not initialized"));
        assertTrue(exception.getMessage().contains("jar:groupId:artifactId"));
    }

    @Test
    void testResolveArtifactReportsMissingCoordinatesWithoutReadingType() {
        ContextDependentTypeModule module = new ContextDependentTypeModule(null, null);

        MojoFailureException exception =
                assertThrows(MojoFailureException.class, () -> module.resolveArtifact(Collections.emptySet()));

        assertTrue(exception.getMessage().contains("Could not resolve artifact"));
        assertTrue(exception.getMessage().contains("null:null:jar"));
    }

    @Test
    void testResolveArtifactReportsIdentityWithoutCallingContextDependentType() {
        ContextDependentTypeModule module = new ContextDependentTypeModule("groupId", "artifactId");

        MojoFailureException exception =
                assertThrows(MojoFailureException.class, () -> module.resolveArtifact(Collections.emptySet()));

        assertTrue(exception.getMessage().contains("Ear execution context not initialized"));
        assertTrue(exception.getMessage().contains("jar:groupId:artifactId"));
    }
}
