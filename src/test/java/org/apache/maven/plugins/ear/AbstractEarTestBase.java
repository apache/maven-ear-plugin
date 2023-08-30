package org.apache.maven.plugins.ear;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugins.ear.stub.ArtifactHandlerTestStub;

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

/**
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
public abstract class AbstractEarTestBase
{

    protected static final String DEFAULT_GROUPID = "eartest";

    private static final String DEFAULT_TYPE = "jar";

    protected void setUri( EarModule module, String uri )
    {
        ( (AbstractEarModule) module ).setUri( uri );
    }

    protected Set<Artifact> createArtifacts( String[] artifactIds )
    {
        return createArtifacts( artifactIds, null );
    }

    protected Set<Artifact> createArtifacts( String[] artifactIds, String[] classifiers )
    {
        Set<Artifact> result = new TreeSet<>();
        ArtifactHandlerTestStub artifactHandler = new ArtifactHandlerTestStub( "jar" );
        for ( int i = 0; i < artifactIds.length; i++ )
        {
            String artifactId = artifactIds[i];
            String classifier = classifiers == null ? null : classifiers[i];
            Artifact artifactTestStub = new DefaultArtifact(
                DEFAULT_GROUPID, artifactId, "1.0", "compile", DEFAULT_TYPE, classifier, artifactHandler );
            result.add( artifactTestStub );
        }
        return result;
    }

    protected Artifact createArtifact( String artifactId, String type )
    {
        Artifact artifactTestStub = new DefaultArtifact(
            DEFAULT_GROUPID, artifactId, "1.0", "compile", Objects.toString( type, DEFAULT_TYPE ),
            null, new ArtifactHandlerTestStub( "jar" ) );
        
        return artifactTestStub;
    }
}
