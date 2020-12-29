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
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.xml.XMLWriter;

import java.util.Set;

/**
 * The {@link EarModule} implementation for a Web application module.
 * 
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
public class WebModule
    extends AbstractEarModule
{
    /**
     * Default type of the artifact of a Web application module.
     */
    public static final String DEFAULT_ARTIFACT_TYPE = "war";

    private static final String WEB_MODULE = "web";

    private static final String WEB_URI_FIELD = "web-uri";

    private static final String CONTEXT_ROOT_FIELD = "context-root";

    private static final String DEFAULT_LIB_DIRECTORY = "WEB-INF/lib";

    private String contextRoot;

    /**
     * Create an instance.
     */
    public WebModule()
    {
        this.type = DEFAULT_ARTIFACT_TYPE;
        this.libDirectory = DEFAULT_LIB_DIRECTORY;
    }

    /**
     * @param a {@link Artifact}
     */
    public WebModule( Artifact a )
    {
        super( a );
        this.contextRoot = getDefaultContextRoot( a );
        this.libDirectory = DEFAULT_LIB_DIRECTORY;
    }

    /**
     * {@inheritDoc}
     */
    public void appendModule( XMLWriter writer, String version, Boolean generateId )
    {
        startModuleElement( writer, generateId );
        writer.startElement( WEB_MODULE );
        writer.startElement( WEB_URI_FIELD );
        writer.writeText( getUri() );
        writer.endElement(); // web-uri

        writer.startElement( CONTEXT_ROOT_FIELD );
        writer.writeText( getContextRoot() );
        writer.endElement(); // context-root

        writer.endElement(); // web

        writeAltDeploymentDescriptor( writer, version );

        writer.endElement(); // module
    }

    /**
     * {@inheritDoc}
     */
    public void resolveArtifact( Set<Artifact> artifacts )
        throws EarPluginException, MojoFailureException
    {
        // Let's resolve the artifact
        super.resolveArtifact( artifacts );

        // Context root has not been customized - using default
        if ( contextRoot == null )
        {
            contextRoot = getDefaultContextRoot( getArtifact() );
        }
    }

    /**
     * Returns the context root to use for the web module.
     *
     * Note that this might return {@code null} till the artifact has been resolved.
     * 
     * @return the context root
     */
    public String getContextRoot()
    {
        return contextRoot;
    }

    /**
     * Generates a default context root for the given artifact, based on the {@code artifactId}.
     * 
     * @param a the artifact
     * @return a context root for the artifact
     */
    private static String getDefaultContextRoot( Artifact a )
    {
        if ( a == null )
        {
            throw new NullPointerException( "Artifact could not be null." );
        }
        return "/" + a.getArtifactId();
    }
}
