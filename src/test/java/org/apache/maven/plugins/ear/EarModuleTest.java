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

import java.io.StringWriter;

import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void testStartModuleElementWithoutResolvedArtifact() {
        StringWriter output = new StringWriter();
        XMLWriter writer = new PrettyPrintXMLWriter(output);
        TestableWebModule module = new TestableWebModule();

        assertDoesNotThrow(() -> module.start(writer));
        writer.endElement();

        assertFalse(output.toString().contains("id="));
    }

    private static final class TestableWebModule extends WebModule {
        void start(XMLWriter writer) {
            startModuleElement(writer, true);
        }
    }
}
