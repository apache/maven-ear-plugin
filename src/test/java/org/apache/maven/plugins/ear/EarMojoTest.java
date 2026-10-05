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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EarMojoTest {

    @TempDir
    File tempDir;

    private File invokeGetEarFile(String finalName, String classifier) throws Exception {
        Method method = EarMojo.class.getDeclaredMethod("getEarFile", String.class, String.class, String.class);
        method.setAccessible(true);
        return (File) method.invoke(null, tempDir.getAbsolutePath(), finalName, classifier);
    }

    @Test
    void testGetEarFileWithNullClassifier() throws Exception {
        File result = invokeGetEarFile("myapp", null);
        assertEquals(new File(tempDir, "myapp.ear"), result);
    }

    @Test
    void testGetEarFileWithClassifier() throws Exception {
        File result = invokeGetEarFile("myapp", "sources");
        assertEquals(new File(tempDir, "myapp-sources.ear"), result);
    }

    @Test
    void testGetEarFileWithDashPrefixedClassifier() throws Exception {
        File result = invokeGetEarFile("myapp", "-sources");
        assertEquals(new File(tempDir, "myapp-sources.ear"), result);
    }

    @Test
    void testRemoveFromOutdatedResourcesWithDifferentFileSystemProvider() throws Exception {
        EarMojo mojo = new EarMojo(null, null, null, null, null, null);
        Field workDirectory = AbstractEarMojo.class.getDeclaredField("workDirectory");
        workDirectory.setAccessible(true);
        workDirectory.set(mojo, tempDir);

        Path archive = tempDir.toPath().resolve("module.war");
        try (ZipOutputStream ignored = new ZipOutputStream(Files.newOutputStream(archive))) {
            // Create an empty archive before mounting it as a file system.
        }

        try (FileSystem archiveFileSystem = FileSystems.newFileSystem(archive, null)) {
            Path destination = archiveFileSystem.getPath("/META-INF/application.xml");
            Collection<String> outdatedResources = new ArrayList<>(Arrays.asList("META-INF/application.xml"));

            Method method =
                    EarMojo.class.getDeclaredMethod("removeFromOutdatedResources", Path.class, Collection.class);
            method.setAccessible(true);
            method.invoke(mojo, destination, outdatedResources);

            assertTrue(outdatedResources.isEmpty());
        }
    }
}
