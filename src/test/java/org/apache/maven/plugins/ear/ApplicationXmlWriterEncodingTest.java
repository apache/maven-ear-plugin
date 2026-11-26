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

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import org.apache.maven.plugins.ear.util.JavaEEVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The generated application.xml must be written in the configured encoding and declare it.
 */
class ApplicationXmlWriterEncodingTest {

    private static final String DESCRIPTION = "TEST-CHARS: ÄÖÜäöüß";

    @TempDir
    File tempDir;

    private File write(String encoding) throws Exception {
        File dest = new File(tempDir, "application.xml");
        ApplicationXmlWriter writer = new ApplicationXmlWriter(JavaEEVersion.ELEVEN, encoding, false);
        writer.write(new ApplicationXmlWriterContext(
                dest,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                DESCRIPTION,
                null,
                null,
                null));
        return dest;
    }

    private void assertEncoded(String encoding, Charset charset) throws Exception {
        File dest = write(encoding);

        String raw = new String(Files.readAllBytes(dest.toPath()), charset);
        assertTrue(raw.contains("encoding=\"" + encoding + "\""), "XML declaration should name " + encoding);
        assertTrue(raw.contains(DESCRIPTION), "description should be stored as " + encoding + " bytes");

        // a conforming parser decodes the file by its declaration
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(dest);
        assertEquals(
                DESCRIPTION, doc.getElementsByTagName("description").item(0).getTextContent());
    }

    @Test
    void testLatin1Encoding() throws Exception {
        assertEncoded("ISO-8859-1", StandardCharsets.ISO_8859_1);
    }

    @Test
    void testUtf8Encoding() throws Exception {
        assertEncoded("UTF-8", StandardCharsets.UTF_8);
    }
}
