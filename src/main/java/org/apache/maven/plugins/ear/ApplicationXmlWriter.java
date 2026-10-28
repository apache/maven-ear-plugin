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
import java.io.Writer;

import org.apache.maven.plugins.ear.util.JavaEEVersion;
import org.codehaus.plexus.util.xml.XMLWriter;

/**
 * An <code>XmlWriter</code> based implementation used to generate an {@code application.xml} file.
 *
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
final class ApplicationXmlWriter extends AbstractXmlWriter {
    // @formatter:off
    public static final String DOCTYPE_1_3 = "application PUBLIC\n"
            + "\t\"-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN\"\n"
            + "\t\"http://java.sun.com/dtd/application_1_3.dtd\"";
    // @formatter:on

    private static final String APPLICATION_ELEMENT = "application";

    private final JavaEEVersion version;

    private final Boolean generateModuleId;

    ApplicationXmlWriter(JavaEEVersion version, String encoding, Boolean generateModuleId) {
        super(encoding);
        this.version = version;
        this.generateModuleId = generateModuleId;
    }

    void write(ApplicationXmlWriterContext context) throws EarPluginException {
        try (Writer w = initializeWriter(context.getDestinationFile())) {
            XMLWriter writer = null;
            if (JavaEEVersion.ONE_DOT_THREE.eq(version)) {
                writer = initializeRootElementOneDotThree(w);
            } else {
                writer = initializeRootElement(w, version);
            }

            // writer is still on root element, so we can still add this attribute
            if (context.getApplicationId() != null) {
                writer.addAttribute("id", context.getApplicationId());
            }

            // As from JavaEE6
            if (version.ge(JavaEEVersion.SIX)) {
                writeApplicationName(context.getApplicationName(), writer);
            }

            // IMPORTANT: the order of the description and display-name elements was
            // reversed between J2EE 1.3 and J2EE 1.4.
            if (version.eq(JavaEEVersion.ONE_DOT_THREE)) {
                writeDisplayName(context.getDisplayName(), writer);
                writeDescription(context.getDescription(), writer);
            } else {
                writeDescription(context.getDescription(), writer);
                writeDisplayName(context.getDisplayName(), writer);
            }

            // As from JavaEE6
            if (version.ge(JavaEEVersion.SIX)) {
                writeInitializeInOrder(context.getInitializeInOrder(), writer);
            }

            // Do not change this unless you really know what you're doing :)
            for (EarModule module : context.getEarModules()) {
                module.appendModule(writer, version.getVersion(), generateModuleId);
            }

            for (SecurityRole securityRole : context.getSecurityRoles()) {
                securityRole.appendSecurityRole(writer);
            }

            if (version.ge(JavaEEVersion.FIVE)) {
                writeLibraryDirectory(context.getLibraryDirectory(), writer);
            }

            if (version.ge(JavaEEVersion.SIX)) {
                for (EnvEntry envEntry : context.getEnvEntries()) {
                    envEntry.appendEnvEntry(writer);
                }
                for (EjbRef ejbEntry : context.getEjbEntries()) {
                    ejbEntry.appendEjbRefEntry(writer);
                }
                for (ResourceRef resourceEntry : context.getResourceRefs()) {
                    resourceEntry.appendResourceRefEntry(writer);
                }
            }

            writer.endElement();
        } catch (IOException ex) {
            throw new EarPluginException("Failed to write application.xml", ex);
        }
    }

    private void writeApplicationName(String applicationName, XMLWriter writer) {
        if (applicationName != null) {
            writer.startElement("application-name");
            writer.writeText(applicationName);
            writer.endElement();
        }
    }

    private void writeDescription(String description, XMLWriter writer) {
        if (description != null) {
            writer.startElement("description");
            writer.writeText(description);
            writer.endElement();
        }
    }

    private void writeDisplayName(String displayName, XMLWriter writer) {
        if (displayName != null) {
            writer.startElement("display-name");
            writer.writeText(displayName);
            writer.endElement();
        }
    }

    private void writeInitializeInOrder(Boolean initializeInOrder, XMLWriter writer) {
        if (initializeInOrder != null) {
            writer.startElement("initialize-in-order");
            writer.writeText(initializeInOrder.toString());
            writer.endElement();
        }
    }

    private void writeLibraryDirectory(String libraryDirectory, XMLWriter writer) {
        if (libraryDirectory != null) {
            writer.startElement("library-directory");
            writer.writeText(libraryDirectory);
            writer.endElement();
        }
    }

    private XMLWriter initializeRootElementOneDotThree(Writer w) {
        XMLWriter writer = initializeXmlWriter(w, DOCTYPE_1_3);
        writer.startElement(APPLICATION_ELEMENT);
        return writer;
    }

    private XMLWriter initializeRootElement(Writer w, JavaEEVersion version) {
        String xmlns;
        if (version.le(JavaEEVersion.ONE_DOT_FOUR)) {
            xmlns = "http://java.sun.com/xml/ns/j2ee";
        } else if (version.le(JavaEEVersion.SIX)) {
            xmlns = "http://java.sun.com/xml/ns/javaee";
        } else if (version.le(JavaEEVersion.EIGHT)) {
            xmlns = "http://xmlns.jcp.org/xml/ns/javaee";
        } else {
            xmlns = "https://jakarta.ee/xml/ns/jakartaee";
        }
        XMLWriter writer = initializeXmlWriter(w, null);
        writer.startElement(APPLICATION_ELEMENT);
        writer.addAttribute("xmlns", xmlns);
        writer.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        String schemaVersion = version.getVersion().replace('.', '_');
        writer.addAttribute("xsi:schemaLocation", xmlns + " " + xmlns + "/application_" + schemaVersion + ".xsd");
        writer.addAttribute("version", version.getVersion());
        return writer;
    }
}
