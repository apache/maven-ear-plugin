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

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.ear.util.ArtifactTypeMappingService;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    void testRecursiveMappingFailureDoesNotFormatModuleOrLogRepeatedly() {
        AtomicInteger artifactIdCalls = new AtomicInteger();
        Artifact artifact = (Artifact) Proxy.newProxyInstance(
                Artifact.class.getClassLoader(), new Class<?>[] {Artifact.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getGroupId":
                            return "org.example";
                        case "getArtifactId":
                            return artifactIdCalls.incrementAndGet() == 1 ? "example" : "@{artifactId}@";
                        case "getType":
                            return "ejb";
                        case "getClassifier":
                            return null;
                        case "getArtifactHandler":
                            return new DefaultArtifactHandler("ejb");
                        case "toString":
                            return "example artifact";
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == args[0];
                        default:
                            return null;
                    }
                });
        AtomicInteger warnings = new AtomicInteger();
        Log log = (Log) Proxy.newProxyInstance(
                Log.class.getClassLoader(), new Class<?>[] {Log.class}, (proxy, method, args) -> {
                    if ("warn".equals(method.getName())) {
                        warnings.incrementAndGet();
                    }
                    if (method.getReturnType() == boolean.class) {
                        return false;
                    }
                    if (method.getReturnType() == int.class) {
                        return 0;
                    }
                    if ("toString".equals(method.getName())) {
                        return "recording log";
                    }
                    return null;
                });
        EarExecutionContext context = new EarExecutionContext(
                new MavenProject(), "main", null, null, "@{artifactId}@", new ArtifactTypeMappingService());
        context.setLog(log);
        EjbModule module = new EjbModule(artifact) {
            @Override
            public String toString() {
                throw new IllegalStateException("module formatting should not be needed for a warning");
            }
        };
        module.setEarExecutionContext(context);

        assertNull(module.getBundleFileName());
        assertNull(module.getBundleFileName());
        assertEquals(1, warnings.get());
    }
}
