package org.apache.maven.plugins.ear.util;

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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.ear.AbstractEarTestBase;
import org.junit.Test;

/**
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
public class ArtifactRepositoryTest
    extends AbstractEarTestBase
{

    private static final String MAIN_ARTIFACT_ID = "none";

    private ArtifactTypeMappingService artifactTypeMappingService = new ArtifactTypeMappingService();

    @Test
    public void testEmptyRepository()
    {
        Set<Artifact> artifacts = new HashSet<>();
        ArtifactRepository repo = new ArtifactRepository( artifacts, MAIN_ARTIFACT_ID, artifactTypeMappingService );
        assertNull( repo.getUniqueArtifact( "ear", "ar", "jar" ) );
        assertNull( repo.getUniqueArtifact( "ear", "ar", "jar", null ) );
        assertNull( repo.getUniqueArtifact( "ear", "ar", "jar", "class" ) );
    }

    @Test
    public void testRepositoryWithOneUnclassifiedArtifact()
    {
        ArtifactRepository repo =
            new ArtifactRepository( createArtifacts( new String[] { "myartifact" } ), MAIN_ARTIFACT_ID,
                                    artifactTypeMappingService );
        assertNotNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar" ) );
        assertNotNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar", null ) );
    }

    @Test
    public void testRepositoryWithOneClassifiedArtifact()
    {
        ArtifactRepository repo =
            new ArtifactRepository( createArtifacts( new String[] { "myartifact" },
                                                     new String[] { "classified" } ), MAIN_ARTIFACT_ID,
                                    artifactTypeMappingService );
        assertNotNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar" ) );
        assertNotNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar", "classified" ) );
        assertNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar", "wrong" ) );
    }

    @Test
    public void testRepositoryWithMultipleClassifiedArtifacts()
    {
        ArtifactRepository repo =
            new ArtifactRepository( createArtifacts( new String[] { "myartifact", "myartifact", "myartifact" }, 
                                                     new String[] { "class1", "class2", "class3" } ),
                                    MAIN_ARTIFACT_ID, artifactTypeMappingService );

        assertNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar" ) );
        assertNotNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar", "class1" ) );
        assertNotNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar", "class2" ) );
        assertNotNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar", "class3" ) );
        assertNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar", "wrong" ) );
    }

    @Test
    public void testRepositoryWithMultipleClassifiedArtifactsAndMainArtifact()
    {
        ArtifactRepository repo =
            new ArtifactRepository( createArtifacts( new String[] { "myartifact", "myartifact", "myartifact" },
                                                     new String[] { "class1", "class2", null } ),
                                    MAIN_ARTIFACT_ID, artifactTypeMappingService );

        assertNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar" ) );
        assertNotNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar", "class1" ) );
        assertNotNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar", "class2" ) );
        assertNotNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar", MAIN_ARTIFACT_ID ) );
        assertNull( repo.getUniqueArtifact( DEFAULT_GROUPID, "myartifact", "jar", "wrong" ) );
    }
}
