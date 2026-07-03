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
import java.util.Collections;

import org.apache.maven.plugins.ear.util.JavaEEVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationXmlWriterTest {

    @TempDir
    File tempDir;

    @Test
    void testWriteSucceedsToValidFile() throws Exception {
        ApplicationXmlWriter writer = new ApplicationXmlWriter(JavaEEVersion.ELEVEN, "UTF-8", false);
        File dest = new File(tempDir, "application.xml");
        ApplicationXmlWriterContext context = new ApplicationXmlWriterContext(
                dest,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                null,
                null,
                null);
        writer.write(context);
        assertTrue(dest.exists(), "application.xml should have been created");
        assertTrue(dest.length() > 0, "application.xml should not be empty");
    }

    @Test
    void testWriteThrowsExceptionOnIoError() {
        ApplicationXmlWriter writer = new ApplicationXmlWriter(JavaEEVersion.ELEVEN, "UTF-8", false);
        ApplicationXmlWriterContext context = new ApplicationXmlWriterContext(
                new File("/dev/full"),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null,
                null,
                null,
                null);
        assertThrows(EarPluginException.class, () -> writer.write(context));
    }
}
