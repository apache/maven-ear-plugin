package org.apache.maven.plugins.ear;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.XMLWriter;

/**
 * The {@link EarModule} implementation for an J2EE connector module.
 * 
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
public class RarModule
    extends AbstractEarModule
{
    /**
     * Default type of the artifact of an Java EE connector module.
     */
    public static final String DEFAULT_ARTIFACT_TYPE = "rar";

    private static final String RAR_MODULE = "connector";

    private static final String DEFAULT_LIB_DIRECTORY = "/";

    /**
     * Create an instance.
     */
    public RarModule()
    {
        this.type = DEFAULT_ARTIFACT_TYPE;
        this.libDirectory = DEFAULT_LIB_DIRECTORY;
    }

    /**
     * @param a {@link Artifact}
     */
    public RarModule( Artifact a )
    {
        super( a );
        this.libDirectory = DEFAULT_LIB_DIRECTORY;
    }

    /**
     * {@inheritDoc}
     */
    public void appendModule( XMLWriter writer, String version, Boolean generateId )
    {
        startModuleElement( writer, generateId );
        writer.startElement( RAR_MODULE );
        writer.writeText( getUri() );
        writer.endElement();

        writeAltDeploymentDescriptor( writer, version );

        writer.endElement();
    }
}
