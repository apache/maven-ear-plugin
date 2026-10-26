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

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EarMojoResourceCleanupTest {

    @TempDir
    Path tempDir;

    @Test
    void ignoresAResourceThatHasAlreadyDisappeared() {
        RecordingLog log = new RecordingLog();

        EarMojo.deleteOutdatedResource(tempDir.resolve("missing.txt"), Long.MAX_VALUE, log.proxy());

        assertEquals(0, log.warnings.size());
    }

    @Test
    void deletesAnOutdatedResource() throws IOException {
        Path resource = tempDir.resolve("outdated.txt");
        Files.write(resource, "stale".getBytes(StandardCharsets.UTF_8));
        Files.setLastModifiedTime(resource, FileTime.fromMillis(1));

        EarMojo.deleteOutdatedResource(resource, Long.MAX_VALUE, new RecordingLog().proxy());

        assertFalse(Files.exists(resource));
    }

    @Test
    void keepsAResourceNewerThanTheSession() throws IOException {
        Path resource = tempDir.resolve("current.txt");
        Files.write(resource, "current".getBytes(StandardCharsets.UTF_8));
        Files.setLastModifiedTime(resource, FileTime.fromMillis(System.currentTimeMillis() + 60_000));

        EarMojo.deleteOutdatedResource(resource, System.currentTimeMillis(), new RecordingLog().proxy());

        assertTrue(Files.exists(resource));
    }

    @Test
    void logsTheAbsolutePathAndReasonWhenDeletionFails() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("non-empty"));
        Files.write(directory.resolve("child.txt"), "keep directory non-empty".getBytes(StandardCharsets.UTF_8));
        RecordingLog log = new RecordingLog();

        EarMojo.deleteOutdatedResource(directory, Long.MAX_VALUE, log.proxy());

        assertEquals(1, log.warnings.size());
        Object[] warning = log.warnings.get(0);
        assertTrue(warning[0].toString().contains(directory.toAbsolutePath().toString()));
        assertTrue(warning[1] instanceof IOException);
    }

    private static final class RecordingLog {
        private final List<Object[]> warnings = new ArrayList<>();

        private Log proxy() {
            return (Log) Proxy.newProxyInstance(
                    Log.class.getClassLoader(), new Class<?>[] {Log.class}, (proxy, method, args) -> {
                        if ("warn".equals(method.getName())) {
                            warnings.add(args == null ? new Object[0] : args.clone());
                        }
                        return method.getReturnType() == boolean.class ? false : null;
                    });
        }
    }
}
