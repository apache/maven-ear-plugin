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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.ear.util.ArtifactTypeMappingService;
import org.apache.maven.plugins.ear.util.JavaEEVersion;

/**
 * Builds an {@link EarModule} based on an {@code Artifact}.
 * 
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
public final class EarModuleFactory
{
    private static final String TEST_JAR_ARTIFACT_TYPE = "test-jar";
    private static final String JBOSS_PAR_ARTIFACT_TYPE = "jboss-par";
    private static final String JBOSS_SAR_ARTIFACT_TYPE = "jboss-sar";
    private static final String JBOSS_HAR_ARTIFACT_TYPE = "jboss-har";

    /**
     * The list of artifact types.
     */
    private static final List<String> STANDARD_ARTIFACT_TYPES =
        Collections.unmodifiableList( Arrays.asList(
            JarModule.DEFAULT_ARTIFACT_TYPE,
            EjbModule.DEFAULT_ARTIFACT_TYPE,
            ParModule.DEFAULT_ARTIFACT_TYPE,
            EjbClientModule.DEFAULT_ARTIFACT_TYPE,
            AppClientModule.DEFAULT_ARTIFACT_TYPE,
            RarModule.DEFAULT_ARTIFACT_TYPE,
            WebModule.DEFAULT_ARTIFACT_TYPE,
            SarModule.DEFAULT_ARTIFACT_TYPE,
            WsrModule.DEFAULT_ARTIFACT_TYPE,
            HarModule.DEFAULT_ARTIFACT_TYPE,
            TEST_JAR_ARTIFACT_TYPE,
            JBOSS_PAR_ARTIFACT_TYPE,
            JBOSS_SAR_ARTIFACT_TYPE,
            JBOSS_HAR_ARTIFACT_TYPE ) );

    /**
     * Creates a new {@link EarModule} based on the specified {@link Artifact} and the specified execution
     * configuration.
     * 
     * @param artifact the artifact
     * @param javaEEVersion the javaEE version to use
     * @param defaultLibBundleDir the default bundle dir for {@link org.apache.maven.plugins.ear.JarModule}
     * @param includeInApplicationXml should {@link org.apache.maven.plugins.ear.JarModule} be included in application
     *            Xml
     * @param typeMappingService The artifact type mapping service
     * @return an ear module for this artifact
     * @throws UnknownArtifactTypeException if the artifact is not handled
     */
    public static EarModule newEarModule( Artifact artifact, JavaEEVersion javaEEVersion, String defaultLibBundleDir,
                                          Boolean includeInApplicationXml,
                                          ArtifactTypeMappingService typeMappingService )
        throws UnknownArtifactTypeException
    {
        // Get the standard artifact type based on default config and user-defined mapping(s)
        final String artifactType;
        try
        {
            artifactType = typeMappingService.getStandardType( artifact.getType() );
        }
        catch ( UnknownArtifactTypeException e )
        {
            throw new UnknownArtifactTypeException( e.getMessage() + " for " + artifact.getArtifactId() );
        }

        if ( JarModule.DEFAULT_ARTIFACT_TYPE.equals( artifactType ) || TEST_JAR_ARTIFACT_TYPE.equals( artifactType ) )
        {
            return new JarModule( artifact, defaultLibBundleDir, includeInApplicationXml );
        }
        else if ( EjbModule.DEFAULT_ARTIFACT_TYPE.equals( artifactType ) )
        {
            return new EjbModule( artifact );
        }
        else if ( ParModule.DEFAULT_ARTIFACT_TYPE.equals( artifactType )
            || JBOSS_PAR_ARTIFACT_TYPE.equals( artifactType ) )
        {
            return new ParModule( artifact );
        }
        else if ( EjbClientModule.DEFAULT_ARTIFACT_TYPE.equals( artifactType ) )
        {
            // Somewhat weird way to tackle the problem described in MEAR-85
            if ( javaEEVersion.le( JavaEEVersion.ONE_DOT_FOUR ) )
            {
                return new EjbClientModule( artifact, null );
            }
            else
            {
                return new EjbClientModule( artifact, defaultLibBundleDir );
            }
        }
        else if ( AppClientModule.DEFAULT_ARTIFACT_TYPE.equals( artifactType ) )
        {
            return new AppClientModule( artifact );
        }
        else if ( RarModule.DEFAULT_ARTIFACT_TYPE.equals( artifactType ) )
        {
            return new RarModule( artifact );
        }
        else if ( WebModule.DEFAULT_ARTIFACT_TYPE.equals( artifactType ) )
        {
            return new WebModule( artifact );
        }
        else if ( SarModule.DEFAULT_ARTIFACT_TYPE.equals( artifactType )
            || JBOSS_SAR_ARTIFACT_TYPE.equals( artifactType ) )
        {
            return new SarModule( artifact );
        }
        else if ( WsrModule.DEFAULT_ARTIFACT_TYPE.equals( artifactType ) )
        {
            return new WsrModule( artifact );
        }
        else if ( HarModule.DEFAULT_ARTIFACT_TYPE.equals( artifactType )
            || JBOSS_HAR_ARTIFACT_TYPE.equals( artifactType ) )
        {
            return new HarModule( artifact );
        }
        else
        {
            throw new IllegalStateException( "Could not handle artifact type[" + artifactType + "]" );
        }
    }

    /**
     * Returns a list of standard artifact types.
     * 
     * @return the standard artifact types
     */
    public static List<String> getStandardArtifactTypes()
    {
        return STANDARD_ARTIFACT_TYPES;
    }

    /**
     * Specify whether the specified type is standard artifact type.
     * 
     * @param type the type to check
     * @return true if the specified type is a standard artifact type
     */
    public static boolean isStandardArtifactType( final String type )
    {
        return STANDARD_ARTIFACT_TYPES.contains( type );
    }

}
