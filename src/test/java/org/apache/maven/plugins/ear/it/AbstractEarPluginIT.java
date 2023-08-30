package org.apache.maven.plugins.ear.it;

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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.apache.maven.plugins.ear.util.ResourceEntityResolver;
import org.apache.maven.shared.verifier.VerificationException;
import org.apache.maven.shared.verifier.Verifier;
import org.apache.maven.shared.verifier.util.ResourceExtractor;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Assert;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Base class for ear test cases.
 * 
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
public abstract class AbstractEarPluginIT
    extends TestCase
{

    private final String FINAL_NAME_PREFIX = "maven-ear-plugin-test-";

    private final String FINAL_NAME_SUFFIX = "-99.0";

    /**
     * The base directory.
     */
    private File basedir;

    private File settingsFile = new File( getBasedir().getAbsolutePath(), "target/test-classes/settings.xml" );

    /**
     * Execute the EAR plugin for the specified project.
     * 
     * @param projectName the name of the project
     * @param expectNoError true/false
     * @param cleanBeforeExecute call clean plugin before execution
     * @return the base directory of the project
     */
    protected File executeMojo( final String projectName, boolean expectNoError,
                                boolean cleanBeforeExecute ) throws VerificationException, IOException
    {
        System.out.println( "  Building: " + projectName );

        File testDir = getTestDir( projectName );
        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        verifier.setAutoclean( cleanBeforeExecute );
        // Let's add alternate settings.xml setting so that the latest dependencies are used
        String localRepo = System.getProperty( "localRepositoryPath" );
        verifier.setLocalRepo( localRepo );

        verifier.addCliArguments( "-s", settingsFile.getAbsolutePath() );//
        verifier.addCliArgument( "-X" );
        verifier.addCliArgument( "package" );

        // On linux and MacOS X, an exception is thrown if a build failure occurs underneath
        try
        {
            verifier.execute();
        }
        catch ( VerificationException e )
        {
            // @TODO needs to be handled nicely in the verifier
            if ( expectNoError || !e.getMessage().contains( "Exit code was non-zero" ) )
            {
                throw e;
            }
        }

        // If no error is expected make sure that error logs are free
        if ( expectNoError )
        {
            verifier.verifyErrorFreeLog();
        }
        return testDir;
    }

    /**
     * Execute the EAR plugin for the specified project.
     * 
     * @param projectName the name of the project
     * @return the base directory of the project
     */
    protected File executeMojo( final String projectName )
        throws VerificationException, IOException
    {
        return executeMojo( projectName, true, true );
    }

    /**
     * Executes the specified projects and asserts the given artifacts. Asserts the deployment descriptors are valid.
     * Asserts Class-Path entry of manifest of EAR modules.
     *
     * @param projectName the project to test
     * @param earModuleName the name of 1st level EAR module in multi-module project or {@code null}
     *                      if project is single-module
     * @param expectedArtifacts the list of artifacts to be found in the EAR archive
     * @param artifactsDirectory whether the artifact from {@code expectedArtifacts} list is an exploded or not
     * @param artifactsToValidateManifest the list of EAR archive artifacts to validate Class-Path entry of artifact
     *                                    manifest or {@code null} if there is no need to validate Class-Path entry
     * @param artifactsToValidateManifestDirectory whether the artifact from {@code artifactsToValidateManifest} list is
     *                                             an exploded or not, can be {@code null} if
     *                                             {@code artifactsToValidateManifest} is {@code null}
     * @param expectedClassPathElements the list of elements of Class-Path entry of manifest, rows should match
     *                                  artifacts passed in {@code artifactsToValidateManifest} parameter;
     *                                  can be {@code null} if {@code artifactsToValidateManifest} is {@code null}
     * @param cleanBeforeExecute call clean plugin before execution
     * @return the base directory of the project
     */
    protected File doTestProject( final String projectName, final String earModuleName,
                                  final String[] expectedArtifacts, boolean[] artifactsDirectory,
                                  final String[] artifactsToValidateManifest,
                                  boolean[] artifactsToValidateManifestDirectory,
                                  final String[][] expectedClassPathElements,
                                  final boolean cleanBeforeExecute )
        throws VerificationException, IOException
    {
        final File baseDir = executeMojo( projectName, true, cleanBeforeExecute );

        final File earModuleDir = getEarModuleDirectory( baseDir, earModuleName );
        assertEarArchive( earModuleDir, projectName );
        assertEarDirectory( earModuleDir, projectName );
        assertArchiveContent( earModuleDir, projectName, expectedArtifacts, artifactsDirectory );
        assertDeploymentDescriptors( earModuleDir, projectName );
        assertClassPathElements( earModuleDir, projectName, artifactsToValidateManifest,
                                 artifactsToValidateManifestDirectory, expectedClassPathElements );

        return baseDir;
    }

    /**
     * Executes the specified projects and asserts the given artifacts. Assert the deployment descriptors are valid.
     *
     * @param projectName the project to test
     * @param expectedArtifacts the list of artifacts to be found in the EAR archive
     * @param artifactsDirectory whether the artifact from {@code expectedArtifacts} list is an exploded or not
     * @return the base directory of the project
     */
    protected File doTestProject( final String projectName, final String[] expectedArtifacts,
                                  final boolean[] artifactsDirectory )
        throws VerificationException, IOException
    {
        return doTestProject( projectName, null, expectedArtifacts, artifactsDirectory, null, null, null, true );
    }

    /**
     * Executes the specified projects and asserts the given artifacts as artifacts (non directory)
     * 
     * @param projectName the project to test
     * @param expectedArtifacts the list of artifacts to be found in the EAR archive
     * @return the base directory of the project
     */
    protected File doTestProject( final String projectName, final String[] expectedArtifacts )
        throws VerificationException, IOException
    {
        return doTestProject( projectName, expectedArtifacts, new boolean[expectedArtifacts.length] );
    }

    /**
     * Executes the specified projects and asserts the given artifacts as artifacts (non directory)
     *
     * @param projectName the project to test
     * @param expectedArtifacts the list of artifacts to be found in the EAR archive
     * @param cleanBeforeExecute call clean plugin before execution
     * @return the base directory of the project
     */
    protected File doTestProject( final String projectName, final String[] expectedArtifacts, boolean cleanBeforeExecute)
        throws VerificationException, IOException
    {
        return doTestProject( projectName, null, expectedArtifacts, new boolean[expectedArtifacts.length], null, null, null, cleanBeforeExecute );
    }

    protected void assertEarArchive( final File baseDir, final String projectName )
    {
        assertTrue( "EAR archive does not exist", getEarArchive( baseDir, projectName ).exists() );
    }

    protected void assertEarDirectory( final File baseDir, final String projectName )
    {
        assertTrue( "EAR archive directory does not exist", getEarDirectory( baseDir, projectName ).exists() );
    }

    protected File getEarModuleDirectory( final File baseDir, final String earModuleName)
    {
        return earModuleName == null ? baseDir : new File( baseDir, earModuleName );
    }

    protected File getTargetDirectory( final File basedir )
    {
        return new File( basedir, "target" );
    }

    protected File getEarArchive( final File baseDir, final String projectName )
    {
        return new File( getTargetDirectory( baseDir ), buildFinalName( projectName ) + ".ear" );
    }

    protected File getEarDirectory( final File baseDir, final String projectName )
    {
        return new File( getTargetDirectory( baseDir ), buildFinalName( projectName ) );
    }

    protected String buildFinalName( final String projectName )
    {
        return FINAL_NAME_PREFIX + projectName + FINAL_NAME_SUFFIX;
    }

    private void assertArchiveContent( final File baseDir, final String projectName, final String[] artifactNames,
                                         final boolean[] artifactsDirectory )
    {
        // sanity check
        assertEquals( "Wrong parameter, artifacts mismatch directory flags", artifactNames.length,
                      artifactsDirectory.length );

        File dir = getEarDirectory( baseDir, projectName );

        // Let's build the expected directories sort list
        final List<File> expectedDirectories = new ArrayList<>();
        for ( int i = 0; i < artifactsDirectory.length; i++ )
        {
            if ( artifactsDirectory[i] )
            {
                expectedDirectories.add( new File( dir, artifactNames[i] ) );
            }
        }

        final List<File> actualFiles = buildArchiveContentFiles( dir, expectedDirectories );
        assertEquals( "Artifacts mismatch " + actualFiles, artifactNames.length, actualFiles.size() );
        for ( int i = 0; i < artifactNames.length; i++ )
        {
            String artifactName = artifactNames[i];
            final boolean isDirectory = artifactsDirectory[i];
            File expectedFile = new File( dir, artifactName );

            assertEquals( "Artifact[" + artifactName + "] not in the right form (exploded/archive", isDirectory,
                          expectedFile.isDirectory() );
            assertTrue( "Artifact[" + artifactName + "] not found in ear archive", actualFiles.contains( expectedFile ) );

        }
    }

    /**
     * Asserts that given EAR archive artifacts have expected entries and don't have unexpected entries.
     *
     * @param baseDir the directory of the tested project
     * @param projectName the project to test
     * @param earModuleName the name of 1st level EAR module in multi-module project or {@code null}
     *                      if project is single-module
     * @param artifacts the list of artifacts to be found in the EAR archive
     * @param artifactsDirectory whether the artifact from {@code artifacts} list is an exploded or not
     * @param includedEntries entries which should exist in artifacts, rows should match artifacts passed in
     *                        {@code artifacts} parameter; can be {@code null} if there is no need to assert
     *                        existence of entries in artifacts
     * @param excludedEntries entries which should not exist in artifacts, rows should match artifacts passed in
     *                        {@code artifacts} parameter; can be {@code null} if there is no need to assert
     *                        absence of paths in artifacts
     * @throws IOException exception in case of failure during reading of artifact archive.
     */
    protected void assertEarModulesContent( final File baseDir, final String projectName, final String earModuleName,
                                            final String[] artifacts, final boolean[] artifactsDirectory,
                                            final String[][] includedEntries, final String[][] excludedEntries )
        throws IOException
    {
        assertTrue( "Wrong parameter, artifacts mismatch directory flags",
            artifacts.length <= artifactsDirectory.length );
        if ( includedEntries != null )
        {
            assertTrue( "Rows of includedEntries parameter should match items of artifacts parameter",
                artifacts.length <= includedEntries.length );
        }
        if ( excludedEntries != null )
        {
            assertTrue( "Rows of excludedEntries parameter should match items of artifacts parameter",
                artifacts.length <= excludedEntries.length );
        }

        final File earDirectory = getEarDirectory( getEarModuleDirectory( baseDir, earModuleName ), projectName );
        for ( int i = 0; i != artifacts.length; ++i )
        {
            final String artifactName = artifacts[i];
            final File module = new File( earDirectory, artifactName );
            assertTrue( "Artifact [" + artifactName + "] should exist in EAR", module.exists() );

            final boolean artifactDirectory = artifactsDirectory[i];
            assertEquals( "Artifact [" + artifactName + "] should be a " + ( artifactDirectory ? "directory" : "file" ),
                artifactDirectory, module.isDirectory() );

            if ( includedEntries == null && excludedEntries == null )
            {
                continue;
            }

            final boolean includedEntriesDefined =
                includedEntries != null && includedEntries[i] != null && includedEntries[i].length != 0;
            final boolean excludedEntriesDefined =
                excludedEntries != null && excludedEntries[i] != null && excludedEntries[i].length != 0;
            if ( !includedEntriesDefined && !excludedEntriesDefined )
            {
                continue;
            }

            if ( artifactDirectory )
            {
                if ( includedEntriesDefined )
                {
                    for ( String includedEntry : includedEntries[i] )
                    {
                        if ( includedEntry == null || includedEntry.length() == 0 )
                        {
                            continue;
                        }
                        final File inclusion = new File( artifactName, includedEntry );
                        assertTrue(
                            "Entry [" + includedEntry + "] should exist in artifact [" + artifactName + "] of EAR",
                            inclusion.exists() );
                    }
                }
                if ( excludedEntriesDefined )
                {
                    for ( String excludedEntry : excludedEntries[i] )
                    {
                        if ( excludedEntry == null || excludedEntry.length() == 0 )
                        {
                            continue;
                        }
                        final File exclusion = new File( artifactName, excludedEntry );
                        assertFalse(
                            "Entry [" + excludedEntry + "] should not exist in artifact [" + artifactName + "] of EAR",
                            exclusion.exists() );
                    }
                }
            }
            else
            {
                try ( JarFile moduleJar = new JarFile( module ) )
                {
                    if ( includedEntriesDefined )
                    {
                        for ( String includedEntry : includedEntries[i] )
                        {
                            if ( includedEntry == null || includedEntry.length() == 0 )
                            {
                                continue;
                            }
                            final ZipEntry inclusion = moduleJar.getEntry( includedEntry );
                            assertNotNull(
                                "Entry [" + includedEntry + "] should exist in artifact [" + artifactName + "] of EAR",
                                inclusion );
                        }
                    }
                    if ( excludedEntriesDefined )
                    {
                        for ( String excludedEntry : excludedEntries[i] )
                        {
                            if ( excludedEntry == null || excludedEntry.length() == 0 )
                            {
                                continue;
                            }
                            final ZipEntry exclusion = moduleJar.getEntry( excludedEntry );
                            assertNull( "Entry [" + excludedEntry + "] should not exist in artifact [" + artifactName
                                + "] of EAR", exclusion );
                        }
                    }
                }
            }
        }
    }

    private static List<File> buildArchiveContentFiles( final File baseDir, final List<File> expectedDirectories )
    {
        final List<File> result = new ArrayList<>();
        addFiles( baseDir, result, expectedDirectories );

        return result;
    }

    private static void addFiles( final File directory, final List<File> files, final List<File> expectedDirectories )
    {
        File[] result = directory.listFiles( new FilenameFilter()
        {
            public boolean accept( File dir, String name )
            {
                return !name.equals( "META-INF" );
            }

        } );

        /*
         * Kinda complex. If we found a file, we always add it to the list of files. If a directory is within the
         * expectedDirectories short list we add it but we don't add it's content. Otherwise, we don't add the directory
         * *BUT* we browse it's content
         */
        for ( File file : result )
        {
            if ( file.isFile() )
            {
                files.add( file );
            }
            else if ( expectedDirectories.contains( file ) )
            {
                files.add( file );
            }
            else
            {
                addFiles( file, files, expectedDirectories );
            }
        }
    }

    private File getBasedir()
    {
        if ( basedir != null )
        {
            return basedir;
        }

        final String basedirString = System.getProperty( "basedir" );
        if ( basedirString == null )
        {
            basedir = new File( "" );
        }
        else
        {
            basedir = new File( basedirString );
        }
        return basedir;
    }

    protected File getTestDir( String projectName )
        throws IOException
    {
        return ResourceExtractor.simpleExtractResources( getClass(), "/projects/" + projectName );
    }

    // Generated application.xml stuff

    /**
     * Asserts that the deployment descriptors have been generated successfully.
     * 
     * This test assumes that deployment descriptors are located in the {@code expected-META-INF} directory of the
     * project. Note that the {@code MANIFEST.mf} file is ignored and is not tested.
     * 
     * @param baseDir the directory of the tested project
     * @param projectName the name of the project
     * @throws IOException exception in case of an error.
     */
    protected void assertDeploymentDescriptors( final File baseDir, final String projectName )
        throws IOException
    {
        final File earDirectory = getEarDirectory( baseDir, projectName );
        final File[] actualDeploymentDescriptors = getDeploymentDescriptors( new File( earDirectory, "META-INF" ) );
        final File[] expectedDeploymentDescriptors =
            getDeploymentDescriptors( new File( baseDir, "expected-META-INF" ) );

        if ( expectedDeploymentDescriptors == null )
        {
            assertNull( "No deployment descriptor was expected", actualDeploymentDescriptors );
        }
        else
        {
            assertNotNull( "Missing deployment descriptor", actualDeploymentDescriptors );

            // Make sure we have the same number of files
            assertEquals( "Number of Deployment descriptor(s) mismatch", expectedDeploymentDescriptors.length,
                          actualDeploymentDescriptors.length );

            // Sort the files so that we have the same behavior here
            Arrays.sort( expectedDeploymentDescriptors );
            Arrays.sort( actualDeploymentDescriptors );

            for ( int i = 0; i < expectedDeploymentDescriptors.length; i++ )
            {
                File expectedDeploymentDescriptor = expectedDeploymentDescriptors[i];
                File actualDeploymentDescriptor = actualDeploymentDescriptors[i];

                assertEquals( "File name mismatch", expectedDeploymentDescriptor.getName(),
                              actualDeploymentDescriptor.getName() );

                try
                {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware( true );
                    dbf.setValidating( true );
                    DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                    docBuilder.setEntityResolver( new ResourceEntityResolver() );
                    docBuilder.setErrorHandler( new DefaultHandler() );

                    final Diff myDiff =
                        new Diff( docBuilder.parse( expectedDeploymentDescriptor ),
                                  docBuilder.parse( actualDeploymentDescriptor ) );
                    XMLAssert.assertXMLEqual( "Wrong deployment descriptor generated for["
                        + expectedDeploymentDescriptor.getName() + "]", myDiff, true );
                }
                catch ( SAXException | ParserConfigurationException e )
                {
                    e.printStackTrace();
                    fail( "Could not assert deployment descriptor " + e.getMessage() );
                }
            }
        }
    }

    private static File[] getDeploymentDescriptors( final File ddDirectory )
    {
        return ddDirectory.listFiles( new FilenameFilter()
        {
            public boolean accept( File dir, String name )
            {
                return !name.equalsIgnoreCase( "manifest.mf" );
            }
        } );
    }

    /**
     * Asserts that given EAR archive artifacts have expected elements in artifact manifest Class-Path entry.
     *
     * @param baseDir the directory of the tested project
     * @param projectName the name of the project
     * @param artifacts the list of EAR archive artifacts to validate Class-Path entry of artifact manifest or
     *                  {@code null} if there is no need to validate Class-Path entry
     * @param artifactsDirectory whether the artifact from {@code artifacts} list is an exploded or not,
     *                           can be {@code null} if {@code artifacts} is {@code null}
     * @param expectedClassPathElements the list of expected elements of Class-Path entry of manifest, rows should match
     *                                  artifacts passed in {@code artifacts} parameter; can be {@code null}
     *                                  if {@code artifacts} is {@code null}
     * @throws IOException exception in case of failure during reading of artifact manifest.
     */
    protected void assertClassPathElements( final File baseDir, String projectName, String[] artifacts,
                                          boolean[] artifactsDirectory, String[][] expectedClassPathElements )
        throws IOException
    {
        if ( artifacts == null )
        {
            return;
        }

        assertNotNull( "artifactsDirectory should be provided if artifacts is provided",
            artifactsDirectory );
        assertTrue( "Size of artifactsDirectory should match size of artifacts parameter",
            artifacts.length <= artifactsDirectory.length );
        assertNotNull( "expectedClassPathElements should be provided if artifacts is provided",
            expectedClassPathElements );
        assertTrue( "Rows of expectedClassPathElements parameter should match items of artifacts parameter",
            artifacts.length <= expectedClassPathElements.length );

        final File earFile = getEarArchive( baseDir, projectName );
        for ( int i = 0; i != artifacts.length; ++i )
        {
            final String moduleArtifact = artifacts[i];
            final String[] classPathElements = getClassPathElements( earFile, moduleArtifact, artifactsDirectory[i] );
            if ( expectedClassPathElements[i] == null )
            {
                assertNull( "Class-Path entry should not exist in module [" + moduleArtifact + "] manifest",
                    classPathElements );
            }
            else
            {
                Assert.assertArrayEquals( "Wrong elements of Class-Path entry of module [" + moduleArtifact + "] manifest",
                    expectedClassPathElements[i], classPathElements );
            }
        }
    }

    /**
     * Retrieves elements of Class-Path entry of manifest of given EAR module.
     *
     * @param earFile the EAR file to investigate
     * @param artifact the name of artifact in EAR archive representing EAR module
     * @return elements of Class-Path entry of manifest of EAR module which is represented by
     * {@code artifact} artifact in {@code earFile} file
     */
    protected String[] getClassPathElements( final File earFile, final String artifact, final boolean directory )
        throws IOException
    {
        final String classPath;
        try ( JarFile earJarFile = new JarFile( earFile ) )
        {
            final ZipEntry moduleEntry = earJarFile.getEntry( artifact );
            assertNotNull( "Artifact [" + artifact + "] should exist in EAR", moduleEntry );
            if (directory)
            {
                final String manifestEntryName = artifact + "/META-INF/MANIFEST.MF";
                final ZipEntry manifestEntry = earJarFile.getEntry( manifestEntryName );
                assertNotNull( manifestEntryName + " manifest file should exist in EAR", manifestEntry );
                try ( InputStream manifestInputStream = earJarFile.getInputStream( manifestEntry ) )
                {
                    final Manifest manifest = new Manifest(manifestInputStream);
                    classPath = manifest.getMainAttributes().getValue( "Class-Path" );
                }
            }
            else
            {
                try ( InputStream moduleInputStream = earJarFile.getInputStream( moduleEntry );
                      JarInputStream moduleJarInputStream = new JarInputStream( moduleInputStream ) )
                {
                    final Manifest manifest = moduleJarInputStream.getManifest();
                    assertNotNull( "Artifact [" + artifact + "] of EAR should have manifest", manifest );
                    classPath = manifest.getMainAttributes().getValue( "Class-Path" );
                }
            }
        }
        if ( classPath == null )
        {
            return null;
        }
        final String trimmedClassPath = classPath.trim();
        if ( trimmedClassPath.length() == 0 )
        {
            return new String[0];
        }
        return trimmedClassPath.split( " " );
    }
}
