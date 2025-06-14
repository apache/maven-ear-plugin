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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.ear.util.ArtifactTypeMappingService;
import org.apache.maven.plugins.ear.util.JavaEEVersion;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;

/**
 * A base class for EAR-processing related tasks.
 *
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
public abstract class AbstractEarMojo extends AbstractMojo {
    /**
     * The application XML URI {@code META-INF/application.xml}
     */
    public static final String APPLICATION_XML_URI = "META-INF/application.xml";

    /**
     * The {@code META-INF} folder.
     */
    public static final String META_INF = "META-INF";

    /**
     * UTF-8 encoding constant.
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * The schema version of generated <code>application.xml</code> descriptor.
     * <p>
     * Valid values are 1.3, 1.4, 5, 6, 7, 8, 9, 10 and 11.
     * <p>
     * See: <a href="https://jakarta.ee/xml/ns/jakartaee/">Jakarta EE XML Schemas</a>
     */
    @Parameter(defaultValue = "7")
    protected String version;

    /**
     * Character encoding for the auto-generated deployment file(s).
     */
    @Parameter(defaultValue = "UTF-8")
    protected String encoding;

    /**
     * Directory where the deployment descriptor file(s) will be auto-generated.
     */
    @Parameter(defaultValue = "${project.build.directory}")
    protected String generatedDescriptorLocation;

    /**
     * The maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * The ear modules configuration.
     */
    @Parameter
    private EarModule[] modules;

    /**
     * The artifact type mappings.
     */
    @Parameter
    protected PlexusConfiguration artifactTypeMappings;

    /**
     * The default bundle dir for libraries.
     */
    @Parameter
    protected String defaultLibBundleDir;

    /**
     * Should libraries be added in application.xml
     */
    @Parameter(defaultValue = "false")
    private Boolean includeLibInApplicationXml = Boolean.FALSE;

    /**
     * Only here to identify migration issues. The usage of this parameter will fail the build.
     *
     * @deprecated use {@link #outputFileNameMapping}
     */
    @Deprecated
    @Parameter
    private String fileNameMapping;

    /**
     * The file name mapping to use for all dependencies included in the EAR file. The mapping between artifacts and the
     * file names which is used within the EAR file.
     * See
     * <a href="./examples/customize-file-name-mapping.html">Customizing The File Name Mapping</a>.
     *
     * @since 3.0.0
     */
    @Parameter(
            defaultValue = "@{groupId}@-@{artifactId}@-@{version}@@{dashClassifier?}@.@{extension}@",
            required = true)
    private String outputFileNameMapping;

    /**
     * Directory that resources are copied to during the build.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}", required = true)
    private File workDirectory;

    /**
     * The JBoss specific configuration.
     */
    @Parameter
    private PlexusConfiguration jboss;

    /**
     * The id to use to define the main artifact (e.g. the artifact without a classifier) when there are multiple
     * candidates.
     */
    @Parameter
    private String mainArtifactId = "none";

    private List<EarModule> earModules;

    private List<EarModule> allEarModules;

    private List<EarModule> providedEarModules;

    private JbossConfiguration jbossConfiguration;

    /** {@inheritDoc} */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (fileNameMapping != null) {
            getLog().error("fileNameMapping has been removed with version 3.0.0. You are still using it.");
            getLog().error("Use outputFileNameMapping instead.");
            throw new MojoExecutionException(
                    "fileNameMapping has been removed with version 3.0.0 " + "but you are still using it.");
        }

        final JavaEEVersion javaEEVersion = JavaEEVersion.getJavaEEVersion(version);
        getLog().debug("Resolving artifact type mappings ...");
        ArtifactTypeMappingService typeMappingService;
        try {
            typeMappingService = new ArtifactTypeMappingService();
            typeMappingService.configure(artifactTypeMappings);
        } catch (EarPluginException e) {
            throw new MojoExecutionException("Failed to initialize artifact type mappings", e);
        } catch (PlexusConfigurationException e) {
            throw new MojoExecutionException("Invalid artifact type mappings configuration", e);
        }

        getLog().debug("Initializing JBoss configuration if necessary ...");
        try {
            initializeJbossConfiguration();
        } catch (EarPluginException e) {
            throw new MojoExecutionException("Failed to initialize JBoss configuration", e);
        }

        getLog().debug("Initializing ear execution context");
        EarExecutionContext earExecutionContext = new EarExecutionContext(
                project,
                mainArtifactId,
                defaultLibBundleDir,
                jbossConfiguration,
                outputFileNameMapping,
                typeMappingService);

        getLog().debug("Resolving ear modules ...");
        List<EarModule> allModules = new ArrayList<>();
        try {
            if (modules != null) {
                // Let's validate user-defined modules
                for (EarModule module : modules) {
                    getLog().debug("Resolving ear module[" + module + "]");
                    module.setEarExecutionContext(earExecutionContext);
                    module.resolveArtifact(project.getArtifacts());
                    allModules.add(module);
                }
            }

            // Let's add other modules
            Set<Artifact> artifacts = project.getArtifacts();
            for (Artifact artifact : artifacts) {
                // If the artifact's type is POM, ignore and continue
                // since it's used for transitive deps only.
                if ("pom".equals(artifact.getType())) {
                    continue;
                }

                // Artifact is not yet registered and it has not test scope, nor is it optional
                ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_COMPILE_PLUS_RUNTIME);
                if (!isArtifactRegistered(artifact, allModules) && !artifact.isOptional() && filter.include(artifact)) {
                    EarModule module = EarModuleFactory.newEarModule(
                            artifact,
                            javaEEVersion,
                            defaultLibBundleDir,
                            includeLibInApplicationXml,
                            typeMappingService);
                    module.setEarExecutionContext(earExecutionContext);
                    allModules.add(module);
                }
            }
        } catch (EarPluginException e) {
            throw new MojoExecutionException("Failed to initialize ear modules", e);
        }

        // Now we have everything. Let's build modules which have not been excluded
        ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
        allEarModules = new ArrayList<>();
        providedEarModules = new ArrayList<>();
        earModules = new ArrayList<>();
        for (EarModule earModule : allModules) {
            if (earModule.isExcluded()) {
                getLog().debug("Skipping ear module[" + earModule + "]");
            } else {
                allEarModules.add(earModule);
                if (filter.include(earModule.getArtifact())) {
                    earModules.add(earModule);
                } else {
                    providedEarModules.add(earModule);
                }
            }
        }
    }

    /**
     * @return the list of {@link #earModules}. This corresponds to modules needed at runtime.
     */
    protected List<EarModule> getModules() {
        if (earModules == null) {
            throw new IllegalStateException("Ear modules have not been initialized");
        }
        return earModules;
    }

    /**
     * @return the list of {@link #allEarModules}. This corresponds to all modules (provided + compile + runtime).
     */
    protected List<EarModule> getAllEarModules() {
        if (allEarModules == null) {
            throw new IllegalStateException("EAR modules have not been initialized");
        }
        return allEarModules;
    }

    /**
     * @return the list of {@link #providedEarModules}. This corresponds to provided modules.
     */
    protected List<EarModule> getProvidedEarModules() {
        if (providedEarModules == null) {
            throw new IllegalStateException("Jar modules have not been initialized");
        }
        return providedEarModules;
    }

    /**
     * @return {@link MavenProject}
     */
    protected MavenProject getProject() {
        return project;
    }

    /**
     * @return {@link #workDirectory}
     */
    protected File getWorkDirectory() {
        return workDirectory;
    }

    /**
     * @return {@link #jbossConfiguration}
     */
    protected JbossConfiguration getJbossConfiguration() {
        return jbossConfiguration;
    }

    /**
     * @return {@link #outputFileNameMapping}
     */
    public String getOutputFileNameMapping() {
        return outputFileNameMapping;
    }

    private static boolean isArtifactRegistered(Artifact a, List<EarModule> currentList) {
        for (EarModule em : currentList) {
            if (em.getArtifact().equals(a)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initializes the JBoss configuration.
     *
     * @throws EarPluginException if the configuration is invalid
     */
    private void initializeJbossConfiguration() throws EarPluginException {
        if (jboss == null) {
            jbossConfiguration = null;
        } else {
            String childVersion = jboss.getChild(JbossConfiguration.VERSION).getValue();
            if (childVersion == null) {
                getLog().info("JBoss version not set, using JBoss 4 by default");
                childVersion = JbossConfiguration.VERSION_4;
            }
            final String securityDomain =
                    jboss.getChild(JbossConfiguration.SECURITY_DOMAIN).getValue();
            final String unauthenticatedPrincipal =
                    jboss.getChild(JbossConfiguration.UNAUHTHENTICTED_PRINCIPAL).getValue();

            final PlexusConfiguration loaderRepositoryEl = jboss.getChild(JbossConfiguration.LOADER_REPOSITORY);
            final String loaderRepository = loaderRepositoryEl.getValue();
            final String loaderRepositoryClass =
                    loaderRepositoryEl.getAttribute(JbossConfiguration.LOADER_REPOSITORY_CLASS_ATTRIBUTE);
            final PlexusConfiguration loaderRepositoryConfigEl =
                    jboss.getChild(JbossConfiguration.LOADER_REPOSITORY_CONFIG);
            final String loaderRepositoryConfig = loaderRepositoryConfigEl.getValue();
            final String configParserClass =
                    loaderRepositoryConfigEl.getAttribute(JbossConfiguration.CONFIG_PARSER_CLASS_ATTRIBUTE);

            final String jmxName = jboss.getChild(JbossConfiguration.JMX_NAME).getValue();
            final String moduleOrder =
                    jboss.getChild(JbossConfiguration.MODULE_ORDER).getValue();

            final List<String> dataSources = new ArrayList<>();
            final PlexusConfiguration dataSourcesEl = jboss.getChild(JbossConfiguration.DATASOURCES);
            if (dataSourcesEl != null) {

                final PlexusConfiguration[] dataSourcesConfig =
                        dataSourcesEl.getChildren(JbossConfiguration.DATASOURCE);
                for (PlexusConfiguration dataSourceConfig : dataSourcesConfig) {
                    dataSources.add(dataSourceConfig.getValue());
                }
            }
            final String libraryDirectory =
                    jboss.getChild(JbossConfiguration.LIBRARY_DIRECTORY).getValue();
            jbossConfiguration = new JbossConfiguration(
                    childVersion,
                    securityDomain,
                    unauthenticatedPrincipal,
                    jmxName,
                    loaderRepository,
                    moduleOrder,
                    dataSources,
                    libraryDirectory,
                    loaderRepositoryConfig,
                    loaderRepositoryClass,
                    configParserClass);
        }
    }
}
