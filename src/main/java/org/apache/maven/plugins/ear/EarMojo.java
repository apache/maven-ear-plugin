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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.ear.util.EarMavenArchiver;
import org.apache.maven.plugins.ear.util.JavaEEVersion;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.apache.maven.shared.mapping.MappingUtils;
import org.apache.maven.shared.utils.io.FileUtils;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.ear.EarArchiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.Manifest.Attribute;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;

/**
 * Builds J2EE Enterprise Archive (EAR) files.
 * 
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
// CHECKSTYLE_OFF: LineLength
@Mojo( name = "ear", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST )
// CHECKSTYLE_ON: LineLength
public class EarMojo
    extends AbstractEarMojo
{
    /**
     * Default file name mapping used by artifacts located in local repository.
     */
    private static final String ARTIFACT_DEFAULT_FILE_NAME_MAPPING =
        "@{artifactId}@-@{version}@@{dashClassifier?}@.@{extension}@";

    /**
     * Single directory for extra files to include in the EAR.
     */
    @Parameter( defaultValue = "${basedir}/src/main/application", required = true )
    private File earSourceDirectory;

    /**
     * The comma separated list of tokens to include in the EAR.
     */
    @Parameter( alias = "includes", defaultValue = "**" )
    private String earSourceIncludes;

    /**
     * The comma separated list of tokens to exclude from the EAR.
     */
    @Parameter( alias = "excludes" )
    private String earSourceExcludes;

    /**
     * Specify that the EAR sources should be filtered.
     * 
     * @since 2.3.2
     */
    @Parameter( defaultValue = "false" )
    private boolean filtering;

    /**
     * Filters (property files) to include during the interpolation of the pom.xml.
     * 
     * @since 2.3.2
     */
    @Parameter
    private List<String> filters;

    /**
     * A list of file extensions that should not be filtered if filtering is enabled.
     * 
     * @since 2.3.2
     */
    @Parameter
    private List<String> nonFilteredFileExtensions;

    /**
     * To escape interpolated value with Windows path c:\foo\bar will be replaced with c:\\foo\\bar.
     * 
     * @since 2.3.2
     */
    @Parameter( defaultValue = "false" )
    private boolean escapedBackslashesInFilePath;

    /**
     * Expression preceded with this String won't be interpolated \${foo} will be replaced with ${foo}.
     * 
     * @since 2.3.2
     */
    @Parameter
    protected String escapeString;

    /**
     * In case of using the {@link #skinnyWars} and {@link #defaultLibBundleDir} usually the classpath will be modified.
     * By settings this option {@code true} you can change this and keep the classpath untouched. This option has been
     * introduced to keep the backward compatibility with earlier versions of the plugin.
     * 
     * @since 2.10
     */
    @Parameter( defaultValue = "false" )
    private boolean skipClassPathModification;

    /**
     * The location of a custom application.xml file to be used within the EAR file.
     */
    @Parameter
    private String applicationXml;

    /**
     * The directory for the generated EAR.
     */
    @Parameter( defaultValue = "${project.build.directory}", required = true )
    private String outputDirectory;

    /**
     * The name of the EAR file to generate.
     */
    @Parameter( defaultValue = "${project.build.finalName}", required = true, readonly = true )
    private String finalName;

    /**
     * The comma separated list of artifact's type(s) to unpack by default.
     */
    @Parameter
    private String unpackTypes;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
     */
    @Parameter
    private String classifier;

    /**
     * A comma separated list of tokens to exclude when packaging the EAR. By default nothing is excluded. Note that you
     * can use the Java Regular Expressions engine to include and exclude specific pattern using the expression
     * %regex[]. Hint: read the about (?!Pattern).
     * 
     * @since 2.7
     */
    @Parameter
    private String packagingExcludes;

    /**
     * A comma separated list of tokens to include when packaging the EAR. By default everything is included. Note that
     * you can use the Java Regular Expressions engine to include and exclude specific pattern using the expression
     * %regex[].
     * 
     * @since 2.7
     */
    @Parameter
    private String packagingIncludes;

    /**
     * Whether to create skinny WARs or not. A skinny WAR is a WAR that does not have all of its dependencies in
     * WEB-INF/lib. Instead those dependencies are shared between the WARs through the EAR.
     * 
     * @since 2.7
     */
    @Parameter( defaultValue = "false" )
    private boolean skinnyWars;

    /**
     * Whether to create skinny EAR modules or not. A skinny EAR module is a WAR, SAR, HAR, RAR or WSR module that
     * does not contain all of its dependencies in it. Instead those dependencies are shared between the WARs, SARs,
     * HARs, RARs and WSRs through the EAR. This option takes precedence over {@link #skinnyWars} option. That is if
     * skinnyModules is {@code true} but {@link #skinnyWars} is {@code false} (explicitly or by default) then all
     * modules including WARs are skinny.
     *
     * @since 3.2.0
     */
    @Parameter( defaultValue = "false" )
    private boolean skinnyModules;

    /**
     * The Plexus EAR archiver to create the output archive.
     */
    @Component( role = Archiver.class, hint = "ear" )
    private EarArchiver earArchiver;

    /**
     * The Plexus JAR archiver to create the output archive if not EAR application descriptor is provided (JavaEE 5+).
     */
    @Component( role = Archiver.class, hint = "jar" )
    private JarArchiver jarArchiver;

    /**
     * The Plexus Zip archiver for Skinny WAR repackaging.
     */
    @Component( role = Archiver.class, hint = "zip" )
    private ZipArchiver zipArchiver;

    /**
     * The Plexus Zip Un archiver for Skinny WAR repackaging.
     */
    @Component( role = UnArchiver.class, hint = "zip" )
    private ZipUnArchiver zipUnArchiver;

    /**
     * The archive configuration to use. See <a href="https://maven.apache.org/shared/maven-archiver/">Maven Archiver
     * Reference</a>.
     */
    @Parameter
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * Timestamp for reproducible output archive entries, either formatted as ISO 8601
     * <code>yyyy-MM-dd'T'HH:mm:ssXXX</code> or as an int representing seconds since the epoch (like
     * <a href="https://reproducible-builds.org/docs/source-date-epoch/">SOURCE_DATE_EPOCH</a>).
     *
     * @since 3.1.0
     */
    @Parameter( defaultValue = "${project.build.outputTimestamp}" )
    private String outputTimestamp;

    /**
     */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * The archive manager.
     */
    @Component
    private ArchiverManager archiverManager;

    /**
     */
    @Component( role = MavenFileFilter.class, hint = "default" )
    private MavenFileFilter mavenFileFilter;

    /**
     */
    @Component( role = MavenResourcesFiltering.class, hint = "default" )
    private MavenResourcesFiltering mavenResourcesFiltering;

    /**
     * @since 2.3.2
     */
    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;

    private List<FileUtils.FilterWrapper> filterWrappers;

    /**
     * @since 2.9
     */
    @Parameter( defaultValue = "true" )
    private boolean useJvmChmod = true;

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // Initializes ear modules
        super.execute();

        File earFile = getEarFile( outputDirectory, finalName, classifier );
        MavenArchiver archiver = new EarMavenArchiver( getModules() );
        File ddFile = new File( getWorkDirectory(), APPLICATION_XML_URI );

        JarArchiver theArchiver;
        if ( ddFile.exists() )
        {
            earArchiver.setAppxml( ddFile );
            theArchiver = earArchiver;
        }
        else
        {
            // current Plexus EarArchiver does not support application.xml-less JavaEE 5+ case
            // => fallback to Plexus Jar archiver 
            theArchiver = jarArchiver;
        }
        getLog().debug( "Ear archiver implementation [" + theArchiver.getClass().getName() + "]" );
        archiver.setArchiver( theArchiver );
        archiver.setOutputFile( earFile );
        archiver.setCreatedBy( "Maven EAR Plugin", "org.apache.maven.plugins", "maven-ear-plugin" );

        // configure for Reproducible Builds based on outputTimestamp value
        Date reproducibleLastModifiedDate = archiver.configureReproducible( outputTimestamp );

        zipArchiver.setUseJvmChmod( useJvmChmod );
        if ( reproducibleLastModifiedDate != null )
        {
            zipArchiver.configureReproducible( reproducibleLastModifiedDate );
        }
        zipUnArchiver.setUseJvmChmod( useJvmChmod );

        final JavaEEVersion javaEEVersion = JavaEEVersion.getJavaEEVersion( version );

        final Collection<String> outdatedResources = initOutdatedResources();

        // Initializes unpack types
        List<String> unpackTypesList = createUnpackList();

        // Copy modules
        copyModules( javaEEVersion, unpackTypesList, outdatedResources );

        // Copy source files
        try
        {
            File earSourceDir = earSourceDirectory;

            if ( earSourceDir.exists() )
            {
                getLog().info( "Copy ear sources to " + getWorkDirectory().getAbsolutePath() );
                String[] fileNames = getEarFiles( earSourceDir );
                for ( String fileName : fileNames )
                {
                    copyFile( new File( earSourceDir, fileName ), new File( getWorkDirectory(), fileName ) );
                    outdatedResources.remove( Paths.get( fileName ).toString() );
                }
            }

            if ( applicationXml != null && !"".equals( applicationXml ) )
            {
                // rename to application.xml
                getLog().info( "Including custom application.xml[" + applicationXml + "]" );
                File metaInfDir = new File( getWorkDirectory(), META_INF );
                copyFile( new File( applicationXml ), new File( metaInfDir, "/application.xml" ) );
                outdatedResources.remove( Paths.get( "META-INF/application.xml" ).toString() );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error copying EAR sources", e );
        }
        catch ( MavenFilteringException e )
        {
            throw new MojoExecutionException( "Error filtering EAR sources", e );
        }

        // Check if deployment descriptor is there
        if ( !ddFile.exists() && ( javaEEVersion.lt( JavaEEVersion.FIVE ) ) )
        {
            throw new MojoExecutionException( "Deployment descriptor: " + ddFile.getAbsolutePath()
                + " does not exist." );
        }
        // no need to check timestamp for descriptors: removing if outdated does not really make sense
        outdatedResources.remove( Paths.get( APPLICATION_XML_URI ).toString() );
        if ( getJbossConfiguration() != null )
        {
            outdatedResources.remove( Paths.get( "META-INF/jboss-app.xml" ).toString() );
        }

        deleteOutdatedResources( outdatedResources );

        try
        {
            getLog().debug( "Excluding " + Arrays.asList( getPackagingExcludes() ) + " from the generated EAR." );
            getLog().debug( "Including " + Arrays.asList( getPackagingIncludes() ) + " in the generated EAR." );

            archiver.getArchiver().addDirectory( getWorkDirectory(), getPackagingIncludes(), getPackagingExcludes() );

            archiver.createArchive( session, getProject(), archive );
        }
        catch ( ManifestException | IOException | DependencyResolutionRequiredException e )
        {
            throw new MojoExecutionException( "Error assembling EAR", e );
        }

        if ( classifier != null )
        {
            projectHelper.attachArtifact( getProject(), "ear", classifier, earFile );
        }
        else
        {
            getProject().getArtifact().setFile( earFile );
        }
    }

    private void copyModules( final JavaEEVersion javaEEVersion, 
                              List<String> unpackTypesList, 
                              Collection<String> outdatedResources )
        throws MojoExecutionException, MojoFailureException
    {
        final Path workingDir = getWorkDirectory().toPath();

        try
        {
            for ( EarModule module : getModules() )
            {
                final File sourceFile = module.getArtifact().getFile();
                final File destinationFile = buildDestinationFile( getWorkDirectory(), module.getUri() );
                if ( !sourceFile.isFile() )
                {
                    throw new MojoExecutionException( "Cannot copy a directory: " + sourceFile.getAbsolutePath()
                        + "; Did you package/install " + module.getArtifact() + "?" );
                }

                if ( destinationFile.getCanonicalPath().equals( sourceFile.getCanonicalPath() ) )
                {
                    getLog().info( "Skipping artifact [" + module + "], as it already exists at [" + module.getUri()
                        + "]" );
                    // FIXME: Shouldn't that result in a build failure!?
                    continue;
                }

                // If the module is within the unpack list, make sure that no unpack wasn't forced (null or true)
                // If the module is not in the unpack list, it should be true
                if ( ( unpackTypesList.contains( module.getType() )
                    && ( module.shouldUnpack() == null || module.shouldUnpack() ) )
                    || ( module.shouldUnpack() != null && module.shouldUnpack() ) )
                {
                    getLog().info( "Copying artifact [" + module + "] to [" + module.getUri() + "] (unpacked)" );
                    // Make sure that the destination is a directory to avoid plexus nasty stuff :)
                    if ( !destinationFile.isDirectory() && !destinationFile.mkdirs() )
                    {
                        throw new MojoExecutionException( "Error creating " + destinationFile );
                    }
                    unpack( sourceFile, destinationFile, outdatedResources );

                    if ( module.changeManifestClasspath() )
                    {
                        changeManifestClasspath( module, destinationFile, javaEEVersion );
                    }
                }
                else
                {
                    if ( sourceFile.lastModified() > destinationFile.lastModified() )
                    {
                        getLog().info( "Copying artifact [" + module + "] to [" + module.getUri() + "]" );
                        createParentIfNecessary( destinationFile );
                        Files.copy( sourceFile.toPath(), destinationFile.toPath(),
                            LinkOption.NOFOLLOW_LINKS, StandardCopyOption.REPLACE_EXISTING );
                        if ( module.changeManifestClasspath() )
                        {
                            changeManifestClasspath( module, destinationFile, javaEEVersion );
                        }
                    }
                    else
                    {
                        getLog().debug( "Skipping artifact [" + module + "], as it is already up to date at ["
                            + module.getUri() + "]" );
                    }
                    outdatedResources.remove( workingDir.relativize( destinationFile.toPath() ).toString() );
                }
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error copying EAR modules", e );
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "Error unpacking EAR modules", e );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "No Archiver found for EAR modules", e );
        }
    }

    private List<String> createUnpackList()
        throws MojoExecutionException
    {
        List<String> unpackTypesList = new ArrayList<String>();
        if ( unpackTypes != null )
        {
            unpackTypesList = Arrays.asList( unpackTypes.split( "," ) );
            for ( String type : unpackTypesList )
            {
                if ( !EarModuleFactory.isStandardArtifactType( type ) )
                {
                    throw new MojoExecutionException( "Invalid type [" + type + "] supported types are "
                        + EarModuleFactory.getStandardArtifactTypes() );
                }
            }
            getLog().debug( "Initialized unpack types " + unpackTypesList );
        }
        return unpackTypesList;
    }

    /**
     * @return {@link #applicationXml}
     */
    public String getApplicationXml()
    {
        return applicationXml;
    }

    /**
     * @param applicationXml {@link #applicationXml}
     */
    public void setApplicationXml( String applicationXml )
    {
        this.applicationXml = applicationXml;
    }

    /**
     * Returns a string array of the excludes to be used when assembling/copying the ear.
     * 
     * @return an array of tokens to exclude
     */
    protected String[] getExcludes()
    {
        List<String> excludeList = new ArrayList<String>( FileUtils.getDefaultExcludesAsList() );
        if ( earSourceExcludes != null && !"".equals( earSourceExcludes ) )
        {
            excludeList.addAll( Arrays.asList( StringUtils.split( earSourceExcludes, "," ) ) );
        }

        // if applicationXml is specified, omit the one in the source directory
        if ( getApplicationXml() != null && !"".equals( getApplicationXml() ) )
        {
            excludeList.add( "**/" + META_INF + "/application.xml" );
        }

        return excludeList.toArray( new String[excludeList.size()] );
    }

    /**
     * Returns a string array of the includes to be used when assembling/copying the ear.
     * 
     * @return an array of tokens to include
     */
    protected String[] getIncludes()
    {
        return StringUtils.split( Objects.toString( earSourceIncludes, "" ), "," );
    }

    /**
     * @return The array with the packaging excludes.
     */
    public String[] getPackagingExcludes()
    {
        if ( StringUtils.isEmpty( packagingExcludes ) )
        {
            return new String[0];
        }
        else
        {
            return StringUtils.split( packagingExcludes, "," );
        }
    }

    /**
     * @param packagingExcludes {@link #packagingExcludes}
     */
    public void setPackagingExcludes( String packagingExcludes )
    {
        this.packagingExcludes = packagingExcludes;
    }

    /**
     * @return the arrays with the includes
     */
    public String[] getPackagingIncludes()
    {
        if ( StringUtils.isEmpty( packagingIncludes ) )
        {
            return new String[] { "**" };
        }
        else
        {
            return StringUtils.split( packagingIncludes, "," );
        }
    }

    /**
     * @param packagingIncludes {@link #packagingIncludes}
     */
    public void setPackagingIncludes( String packagingIncludes )
    {
        this.packagingIncludes = packagingIncludes;
    }

    private static File buildDestinationFile( File buildDir, String uri )
    {
        return new File( buildDir, uri );
    }

    /**
     * Returns the EAR file to generate, based on an optional classifier.
     * 
     * @param basedir the output directory
     * @param finalName the name of the ear file
     * @param classifier an optional classifier
     * @return the EAR file to generate
     */
    private static File getEarFile( String basedir, String finalName, String classifier )
    {
        if ( classifier == null )
        {
            classifier = "";
        }
        else if ( classifier.trim().length() > 0 && !classifier.startsWith( "-" ) )
        {
            classifier = "-" + classifier;
        }

        return new File( basedir, finalName + classifier + ".ear" );
    }

    /**
     * Returns a list of filenames that should be copied over to the destination directory.
     * 
     * @param sourceDir the directory to be scanned
     * @return the array of filenames, relative to the sourceDir
     */
    private String[] getEarFiles( File sourceDir )
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( sourceDir );
        scanner.setExcludes( getExcludes() );
        scanner.addDefaultExcludes();

        scanner.setIncludes( getIncludes() );

        scanner.scan();

        return scanner.getIncludedFiles();
    }

    /**
     * Unpacks the module into the EAR structure.
     * 
     * @param source file to be unpacked
     * @param destDir where to put the unpacked files
     * @param outdatedResources currently outdated resources
     * @throws ArchiverException a corrupt archive
     * @throws NoSuchArchiverException if we don't have an appropriate archiver
     * @throws IOException in case of a general IOException
     */
    public void unpack( File source, final File destDir, final Collection<String> outdatedResources )
        throws ArchiverException, NoSuchArchiverException, IOException
    {
        UnArchiver unArchiver = archiverManager.getUnArchiver( "zip" );
        unArchiver.setSourceFile( source );
        unArchiver.setDestDirectory( destDir );
        unArchiver.setFileMappers( new FileMapper[] {
            new FileMapper()
            {
                @Override
                public String getMappedFileName( String pName )
                {
                    Path destFile = destDir.toPath().resolve( pName );
                    outdatedResources.remove( getWorkDirectory().toPath().relativize( destFile ).toString() );
                    return pName;
                }
            }
        } );

        // Extract the module
        unArchiver.extract();
    }

    private void copyFile( File source, File target )
        throws MavenFilteringException, IOException, MojoExecutionException
    {
        createParentIfNecessary( target );
        if ( filtering && !isNonFilteredExtension( source.getName() ) )
        {
            mavenFileFilter.copyFile( source, target, true, getFilterWrappers(), encoding );
        }
        else
        {
            Files.copy( source.toPath(), target.toPath(), LinkOption.NOFOLLOW_LINKS,
                       StandardCopyOption.REPLACE_EXISTING );
        }
    }

    private void createParentIfNecessary( File target )
        throws IOException
    {
        // Silly that we have to do this ourselves
        File parentDirectory = target.getParentFile();
        if ( parentDirectory != null && !parentDirectory.exists() )
        {
            Files.createDirectories( parentDirectory.toPath() );
        }
    }

    /**
     * @param fileName the name of the file which should be checked
     * @return {@code true} if the name is part of the non filtered extensions; {@code false} otherwise
     */
    public boolean isNonFilteredExtension( String fileName )
    {
        return !mavenResourcesFiltering.filteredFileExtension( fileName, nonFilteredFileExtensions );
    }

    private List<FileUtils.FilterWrapper> getFilterWrappers()
        throws MojoExecutionException
    {
        if ( filterWrappers == null )
        {
            try
            {
                MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution();
                mavenResourcesExecution.setMavenProject( getProject() );
                mavenResourcesExecution.setEscapedBackslashesInFilePath( escapedBackslashesInFilePath );
                mavenResourcesExecution.setFilters( filters );
                mavenResourcesExecution.setEscapeString( escapeString );

                filterWrappers = mavenFileFilter.getDefaultFilterWrappers( mavenResourcesExecution );
            }
            catch ( MavenFilteringException e )
            {
                getLog().error( "Fail to build filtering wrappers " + e.getMessage() );
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
        return filterWrappers;
    }

    private void changeManifestClasspath( EarModule module, File original, JavaEEVersion javaEEVersion )
        throws MojoFailureException
    {
        final String moduleLibDir = module.getLibDir();
        if ( !( ( moduleLibDir == null ) || skinnyModules || ( skinnyWars && module instanceof WebModule ) ) )
        {
            return;
        }
        try
        {
            File workDirectory;

            // Handle the case that the destination might be a directory (project-038)
            if ( original.isFile() )
            {
                // Create a temporary work directory
                // MEAR-167 use uri as directory to prevent merging of artifacts with the same artifactId
                workDirectory = new File( new File( getTempFolder(), "temp" ), module.getUri() );
                if ( !workDirectory.isDirectory() )
                {
                    if ( workDirectory.mkdirs() )
                    {
                        getLog().debug( "Created a temporary work directory: " + workDirectory.getAbsolutePath() );
                    }
                    else
                    {
                        throw new MojoFailureException( "Failed to create directory " + workDirectory );
                    }
                }
                // Unpack the archive to a temporary work directory
                zipUnArchiver.setSourceFile( original );
                zipUnArchiver.setDestDirectory( workDirectory );
                zipUnArchiver.extract();
            }
            else
            {
                workDirectory = original;
            }

            // Create a META-INF/MANIFEST.MF file if it doesn't exist (project-038)
            File metaInfDirectory = new File( workDirectory, "META-INF" );
            boolean newMetaInfCreated = metaInfDirectory.mkdirs();
            if ( newMetaInfCreated )
            {
                getLog().debug(
                    "This project did not have a META-INF directory before, so a new directory was created." );
            }
            File manifestFile = new File( metaInfDirectory, "MANIFEST.MF" );
            boolean newManifestCreated = manifestFile.createNewFile();
            if ( newManifestCreated )
            {
                getLog().debug(
                    "This project did not have a META-INF/MANIFEST.MF file before, so a new file was created." );
            }

            Manifest mf = readManifest( manifestFile );
            Attribute classPath = mf.getMainSection().getAttribute( "Class-Path" );
            List<String> classPathElements = new ArrayList<String>();

            if ( classPath != null )
            {
                classPathElements.addAll( Arrays.asList( classPath.getValue().split( " " ) ) );
            }
            else
            {
                classPath = new Attribute( "Class-Path", "" );
            }

            if ( ( moduleLibDir != null ) && ( skinnyModules || ( skinnyWars && module instanceof WebModule ) ) )
            {
                // Remove JAR modules
                for ( JarModule jm : getAllJarModules() )
                {
                    // MEAR-189:
                    // We use the original name, cause in case of outputFileNameMapping
                    // we could not not delete it and it will end up in the resulting EAR and the WAR
                    // will not be cleaned up.
                    final File workLibDir = new File( workDirectory, moduleLibDir );
                    File artifact = new File( workLibDir, module.getArtifact().getFile().getName() );

                    // MEAR-217
                    // If WAR contains files with timestamps, but EAR strips them away (useBaseVersion=true)
                    // the artifact is not found. Therefore respect the current fileNameMapping additionally.

                    if ( !artifact.exists() )
                    {
                        getLog().debug( "module does not exist with original file name." );
                        artifact = new File( workLibDir, jm.getBundleFileName() );
                        getLog().debug( "Artifact with mapping: " + artifact.getAbsolutePath() );
                    }

                    if ( !artifact.exists() )
                    {
                        getLog().debug( "Artifact with mapping does not exist." );
                        artifact = new File( workLibDir, jm.getArtifact().getFile().getName() );
                        getLog().debug( "Artifact with original file name: " + artifact.getAbsolutePath() );
                    }

                    if ( !artifact.exists() )
                    {
                        getLog().debug( "Artifact with original file name does not exist." );
                        final Artifact jmArtifact = jm.getArtifact();
                        if ( jmArtifact.isSnapshot() )
                        {
                            try
                            {
                                artifact = new File( workLibDir, MappingUtils
                                    .evaluateFileNameMapping( ARTIFACT_DEFAULT_FILE_NAME_MAPPING, jmArtifact ) );
                                getLog()
                                    .debug( "Artifact with default mapping file name: " + artifact.getAbsolutePath() );
                            }
                            catch ( InterpolationException e )
                            {
                                getLog().warn( "Failed to evaluate file name for [" + jm + "] module using mapping: "
                                    + ARTIFACT_DEFAULT_FILE_NAME_MAPPING );
                            }
                        }
                    }

                    if ( artifact.exists() )
                    {
                        getLog().debug( " -> Artifact to delete: " + artifact );
                        if ( !artifact.delete() )
                        {
                            getLog().error( "Could not delete '" + artifact + "'" );
                        }
                    }
                }
            }

            // Modify the classpath entries in the manifest
            for ( EarModule o : getModules() )
            {
                if ( o instanceof JarModule )
                {
                    JarModule jm = (JarModule) o;
                    final int moduleClassPathIndex = findModuleInClassPathElements( classPathElements, jm );
                    if ( moduleClassPathIndex != -1 )
                    {
                        classPathElements.set( moduleClassPathIndex, jm.getUri() );
                    }
                    else
                    {
                        if ( !skipClassPathModification )
                        {
                            classPathElements.add( jm.getUri() );
                        }
                        else
                        {
                            if ( javaEEVersion.lt( JavaEEVersion.FIVE ) || defaultLibBundleDir == null )
                            {
                                classPathElements.add( jm.getUri() );
                            }
                        }
                    }
                }
            }

            // Remove provided Jar modules from classpath
            for ( JarModule jm : getProvidedJarModules() )
            {
                final int moduleClassPathIndex = findModuleInClassPathElements( classPathElements, jm );
                if ( moduleClassPathIndex != -1 )
                {
                    classPathElements.remove( moduleClassPathIndex );
                }
            }

            classPath.setValue( StringUtils.join( classPathElements.iterator(), " " ) );
            mf.getMainSection().addConfiguredAttribute( classPath );

            // Write the manifest to disk
            try ( FileOutputStream out = new FileOutputStream( manifestFile );
                  OutputStreamWriter writer = new OutputStreamWriter( out, StandardCharsets.UTF_8 ) )
            {
                mf.write( writer );
            }

            if ( original.isFile() )
            {
                // Pack up the archive again from the work directory
                if ( !original.delete() )
                {
                    getLog().error( "Could not delete original artifact file " + original );
                }

                getLog().debug( "Zipping module" );
                zipArchiver.setDestFile( original );
                zipArchiver.addDirectory( workDirectory );
                zipArchiver.createArchive();
            }
        }
        catch ( ManifestException | IOException | ArchiverException e )
        {
            throw new MojoFailureException( e.getMessage(), e );
        }
    }

    private static Manifest readManifest( File manifestFile )
        throws IOException
    {
        // Read the manifest from disk
        try ( FileInputStream in = new FileInputStream( manifestFile ) )
        {
            Manifest manifest = new Manifest( in );
            return manifest;
        }
    }

    private Collection<String> initOutdatedResources()
    {
        final Collection<String> outdatedResources = new ArrayList<>();
        
        if ( getWorkDirectory().exists() )
        {
            try
            {
                Files.walkFileTree( getWorkDirectory().toPath(), new SimpleFileVisitor<Path>() 
                {
                    @Override
                    public FileVisitResult visitFile( Path file, BasicFileAttributes attrs )
                        throws IOException
                    {
                        outdatedResources.add( getWorkDirectory().toPath().relativize( file ).toString() );
                        return super.visitFile( file, attrs );
                    }
                } );
            }
            catch ( IOException e )
            {
                getLog().warn( "Can't detect outdated resources", e );
            } 
        }
        return outdatedResources;
    }

    private void deleteOutdatedResources( final Collection<String> outdatedResources )
    {
        final long startTime = session.getStartTime().getTime();
        
        for ( String outdatedResource : outdatedResources )
        {
            if ( new File( getWorkDirectory(), outdatedResource ).lastModified() < startTime )
            {
                getLog().info( "deleting outdated resource " + outdatedResource );
                new File( getWorkDirectory(), outdatedResource ).delete();
            }
        }
    }

    /**
     * Searches for the given JAR module in the list of classpath elements. If JAR module is found among specified
     * classpath elements then returns index of first matching element. Returns -1 otherwise.
     *
     * @param classPathElements classpath elements to search among
     * @param module module to find among classpath elements defined by {@code classPathElements}
     * @return -1 if {@code module} was not found in {@code classPathElements} or index of item of
     * {@code classPathElements} which matches {@code module}
     */
    private int findModuleInClassPathElements( final List<String> classPathElements, final JarModule module )
    {
        if ( classPathElements.isEmpty() )
        {
            return -1;
        }
        int moduleClassPathIndex = classPathElements.indexOf( module.getBundleFileName() );
        if ( moduleClassPathIndex != -1 )
        {
            return moduleClassPathIndex;
        }
        final Artifact artifact = module.getArtifact();
        moduleClassPathIndex = classPathElements.indexOf( artifact.getFile().getName() );
        if ( moduleClassPathIndex != -1 )
        {
            return moduleClassPathIndex;
        }
        if ( artifact.isSnapshot() )
        {
            try
            {
                moduleClassPathIndex = classPathElements
                    .indexOf( MappingUtils.evaluateFileNameMapping( ARTIFACT_DEFAULT_FILE_NAME_MAPPING, artifact ) );
                if ( moduleClassPathIndex != -1 )
                {
                    return moduleClassPathIndex;
                }
            }
            catch ( InterpolationException e )
            {
                getLog().warn( "Failed to evaluate file name for [" + module + "] module using mapping: "
                    + ARTIFACT_DEFAULT_FILE_NAME_MAPPING );
            }
        }
        return classPathElements.indexOf( module.getUri() );
    }
}
