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

import org.apache.maven.plugins.ear.util.ArtifactRepository;
import org.apache.maven.plugins.ear.util.ArtifactTypeMappingService;
import org.apache.maven.project.MavenProject;

/**
 * Contains various runtime parameters used to customize the generated EAR file.
 *
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
public class EarExecutionContext {
    private String defaultLibBundleDir;

    private JbossConfiguration jbossConfiguration;

    private String outputFileNameMapping;

    private ArtifactRepository artifactRepository;

    /**
     * @param project {@link MavenProject}
     * @param mainArtifactId The artifactId.
     * @param defaultLibBundleDir The defaultLibBundleDir.
     * @param jbossConfiguration {@link JbossConfiguration}
     * @param fileNameMappingName file name mapping.
     * @param typeMappingService {@link ArtifactTypeMappingService}
     */
    public EarExecutionContext(
            MavenProject project,
            String mainArtifactId,
            String defaultLibBundleDir,
            JbossConfiguration jbossConfiguration,
            String fileNameMappingName,
            ArtifactTypeMappingService typeMappingService) {
        initialize(
                project,
                mainArtifactId,
                defaultLibBundleDir,
                jbossConfiguration,
                fileNameMappingName,
                typeMappingService);
    }

    /**
     * @return {@link #defaultLibBundleDir}
     */
    public String getDefaultLibBundleDir() {
        return defaultLibBundleDir;
    }

    /**
     * @return {@link #jbossConfiguration}
     */
    public boolean isJbossConfigured() {
        return jbossConfiguration != null;
    }

    /**
     * @return {@link #outputFileNameMapping}
     */
    public String getOutputFileNameMapping() {
        return outputFileNameMapping;
    }

    /**
     * @return {@link #artifactRepository}
     */
    public ArtifactRepository getArtifactRepository() {
        return artifactRepository;
    }

    private void initialize(
            MavenProject project,
            String mainArtifactId,
            String defaultLibBundleDir,
            JbossConfiguration jbossConfiguration,
            String outputFileNameMapping,
            ArtifactTypeMappingService typeMappingService) {
        this.artifactRepository = new ArtifactRepository(project.getArtifacts(), mainArtifactId, typeMappingService);
        this.defaultLibBundleDir = defaultLibBundleDir;
        this.jbossConfiguration = jbossConfiguration;
        this.outputFileNameMapping = outputFileNameMapping;
    }
}
