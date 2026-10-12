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
import java.lang.reflect.Method;

import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AbstractEarMojoTest {

    @Test
    void missingJbossConfigurationChildrenAreOptional() throws Exception {
        TestMojo mojo = new TestMojo();
        Field jboss = AbstractEarMojo.class.getDeclaredField("jboss");
        jboss.setAccessible(true);
        XmlPlexusConfiguration configuration = new XmlPlexusConfiguration("jboss");
        configuration.addChild(new XmlPlexusConfiguration(JbossConfiguration.VERSION));
        jboss.set(mojo, configuration);

        Method initialize = AbstractEarMojo.class.getDeclaredMethod("initializeJbossConfiguration");
        initialize.setAccessible(true);
        initialize.invoke(mojo);

        assertEquals(JbossConfiguration.VERSION_4, mojo.getJbossConfiguration().getVersion());
        assertNull(mojo.getJbossConfiguration().getSecurityDomain());
        assertNull(mojo.getJbossConfiguration().getUnauthenticatedPrincipal());
        assertNull(mojo.getJbossConfiguration().getJmxName());
        assertNull(mojo.getJbossConfiguration().getLoaderRepository());
        assertNull(mojo.getJbossConfiguration().getModuleOrder());
        assertNull(mojo.getJbossConfiguration().getLibraryDirectory());
    }

    private static class TestMojo extends AbstractEarMojo {
        // No execution is needed; this test exercises configuration initialization directly.
    }
}
