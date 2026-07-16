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
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EarMojoTest {

    private static File invokeGetEarFile(String basedir, String finalName, String classifier) throws Exception {
        Method method = EarMojo.class.getDeclaredMethod("getEarFile", String.class, String.class, String.class);
        method.setAccessible(true);
        return (File) method.invoke(null, basedir, finalName, classifier);
    }

    @Test
    void testGetEarFileWithNullClassifier() throws Exception {
        File result = invokeGetEarFile("/tmp", "myapp", null);
        assertEquals(new File("/tmp", "myapp.ear"), result);
    }

    @Test
    void testGetEarFileWithClassifier() throws Exception {
        File result = invokeGetEarFile("/tmp", "myapp", "sources");
        assertEquals(new File("/tmp", "myapp-sources.ear"), result);
    }

    @Test
    void testGetEarFileWithDashPrefixedClassifier() throws Exception {
        File result = invokeGetEarFile("/tmp", "myapp", "-sources");
        assertEquals(new File("/tmp", "myapp-sources.ear"), result);
    }

    @Test
    void testGetEarFileWithWhitespaceOnlyClassifier() throws Exception {
        File result = invokeGetEarFile("/tmp", "myapp", "   ");
        assertEquals(new File("/tmp", "myapp.ear"), result);
        assertFalse(
                result.getAbsolutePath().contains(" "),
                "EAR filename should not contain spaces from whitespace-only classifier");
    }
}
