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

import org.codehaus.plexus.util.xml.XMLWriter;

/**
 * Represents a JBoss specific ear module.
 *
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
public interface JbossEarModule {
    /**
     * Appends the {@code XML} representation of this module for the jboss-app.xml file.
     *
     * @param writer the writer to use
     * @param version the version of the {@code jboss-app.xml} file
     */
    void appendJbossModule(XMLWriter writer, String version);
}
