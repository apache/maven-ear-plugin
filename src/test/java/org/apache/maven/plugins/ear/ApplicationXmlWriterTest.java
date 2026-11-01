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
import java.util.Collections;
import java.util.Locale;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugins.ear.stub.ArtifactHandlerTestStub;
import org.apache.maven.plugins.ear.util.JavaEEVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void testUnresolvedArtifactDoesNotGenerateModuleId() throws Exception {
        File destination = writeApplicationXml(new TestableWebModule(), true);

        Element module = (Element) DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(destination)
                .getElementsByTagName("module")
                .item(0);

        assertTrue(module != null, "application.xml should contain the web module");
        assertFalse(module.hasAttribute("id"), "an unresolved artifact must not receive a generated id");
    }

    @Test
    void testGeneratedModuleIdIsLocaleIndependent() throws Exception {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            DefaultArtifact artifact = new DefaultArtifact(
                    "groupId", "artifactId", "1.0", "compile", "client", null, new ArtifactHandlerTestStub("client"));
            File destination = writeApplicationXml(new TestableWebModule(artifact), true);

            Element module = (Element) DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(destination)
                    .getElementsByTagName("module")
                    .item(0);

            assertEquals("CLIENT_groupId.artifactId", module.getAttribute("id"));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    private File writeApplicationXml(EarModule module, boolean generateModuleId) throws Exception {
        ApplicationXmlWriter writer = new ApplicationXmlWriter(JavaEEVersion.ELEVEN, "UTF-8", generateModuleId);
        File destination = new File(tempDir, "application.xml");
        ApplicationXmlWriterContext context = new ApplicationXmlWriterContext(
                destination,
                Collections.singletonList(module),
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
        return destination;
    }

    private static final class TestableWebModule extends WebModule {
        private TestableWebModule() {
            super();
        }

        private TestableWebModule(Artifact artifact) {
            super(artifact);
        }

        @Override
        public String getContextRoot() {
            return "/context";
        }

        @Override
        public String getUri() {
            return "module.war";
        }
    }
}
