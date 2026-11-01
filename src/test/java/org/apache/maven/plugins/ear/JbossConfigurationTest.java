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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JbossConfigurationTest {

    @TempDir
    File tempDir;

    @Test
    void omittedVersionDefaultsAndLoaderRepositoryOptionsAreWritten() throws Exception {
        XmlPlexusConfiguration jboss = new XmlPlexusConfiguration("jboss");
        XmlPlexusConfiguration loaderRepository = new XmlPlexusConfiguration(JbossConfiguration.LOADER_REPOSITORY);
        loaderRepository.setAttribute(JbossConfiguration.LOADER_REPOSITORY_CLASS_ATTRIBUTE, "org.example.LoaderRepo");
        loaderRepository.setValue("example:loader=app");
        jboss.addChild(loaderRepository);

        XmlPlexusConfiguration loaderRepositoryConfig =
                new XmlPlexusConfiguration(JbossConfiguration.LOADER_REPOSITORY_CONFIG);
        loaderRepositoryConfig.setAttribute(
                JbossConfiguration.CONFIG_PARSER_CLASS_ATTRIBUTE, "org.example.LoaderRepositoryConfigParser");
        loaderRepositoryConfig.setValue("java2ParentDelegation=true");
        jboss.addChild(loaderRepositoryConfig);

        JbossConfiguration configuration =
                AbstractEarMojo.createJbossConfiguration(new MissingChildReturnsNullConfiguration(jboss), null);
        File descriptor = new File(tempDir, "jboss-app.xml");
        new JbossAppXmlWriter("UTF-8").write(descriptor, configuration, Collections.emptyList());
        String jbossAppXml = new String(Files.readAllBytes(descriptor.toPath()), StandardCharsets.UTF_8);

        assertTrue(jbossAppXml.contains("http://www.jboss.org/j2ee/dtd/jboss-app_4_0.dtd"));
        assertTrue(jbossAppXml.contains(
                "<loader-repository loaderRepositoryClass=\"org.example.LoaderRepo\">example:loader=app"));
        assertTrue(
                jbossAppXml.contains(
                        "<loader-repository-config configParserClass=\"org.example.LoaderRepositoryConfigParser\">java2ParentDelegation=true</loader-repository-config>"));
    }

    private static class MissingChildReturnsNullConfiguration implements PlexusConfiguration {

        private final PlexusConfiguration delegate;

        private MissingChildReturnsNullConfiguration(PlexusConfiguration delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getValue() {
            return delegate.getValue();
        }

        @Override
        public String getValue(String defaultValue) {
            return delegate.getValue(defaultValue);
        }

        @Override
        public void setValue(String value) {
            delegate.setValue(value);
        }

        @Override
        public String[] getAttributeNames() {
            return delegate.getAttributeNames();
        }

        @Override
        public String getAttribute(String name) {
            return delegate.getAttribute(name);
        }

        @Override
        public String getAttribute(String name, String defaultValue) {
            return delegate.getAttribute(name, defaultValue);
        }

        @Override
        public void setAttribute(String name, String value) {
            delegate.setAttribute(name, value);
        }

        @Override
        public PlexusConfiguration getChild(String name) {
            return delegate.getChildren(name).length == 0 ? null : delegate.getChild(name, false);
        }

        @Override
        public PlexusConfiguration getChild(String name, boolean create) {
            return delegate.getChild(name, create);
        }

        @Override
        public PlexusConfiguration[] getChildren() {
            return delegate.getChildren();
        }

        @Override
        public PlexusConfiguration[] getChildren(String name) {
            return delegate.getChildren(name);
        }

        @Override
        public int getChildCount() {
            return delegate.getChildCount();
        }

        @Override
        public PlexusConfiguration getChild(int index) {
            return delegate.getChild(index);
        }

        @Override
        public void addChild(PlexusConfiguration child) {
            delegate.addChild(child);
        }

        @Override
        public PlexusConfiguration addChild(String name, String value) {
            return delegate.addChild(name, value);
        }
    }
}
