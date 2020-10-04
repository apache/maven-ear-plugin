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

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.plugins.ear.util.ResourceEntityResolver;
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
     * @param properties extra properties to be used by the embedder
     * @param expectNoError true/false
     * @param cleanBeforeExecute call clean plugin before execution
     * @return the base directory of the project
     */
    protected File executeMojo( final String projectName, final Properties properties, boolean expectNoError,
                                boolean cleanBeforeExecute ) throws VerificationException, IOException
    {
        System.out.println( "  Building: " + projectName );

        File testDir = getTestDir( projectName );
        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        verifier.setAutoclean( cleanBeforeExecute );
        // Let's add alternate settings.xml setting so that the latest dependencies are used
        String localRepo = System.getProperty( "localRepositoryPath" );
        verifier.setLocalRepo( localRepo );

        String httpsProtocols = System.getProperty( "https.protocols" );
        verifier.setSystemProperty( "https.protocols", httpsProtocols );
        verifier.getCliOptions().add( "-s \"" + settingsFile.getAbsolutePath() + "\"" );//
        verifier.getCliOptions().add( "-X" );
        verifier.setLocalRepo( localRepo );

        // On linux and MacOS X, an exception is thrown if a build failure occurs underneath
        try
        {
            verifier.executeGoal( "package" );
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
        verifier.resetStreams();
        return testDir;
    }

    /**
     * Execute the EAR plugin for the specified project.
     * 
     * @param projectName the name of the project
     * @param properties extra properties to be used by the embedder
     * @return the base directory of the project
     */
    protected File executeMojo( final String projectName, final Properties properties )
        throws VerificationException, IOException
    {
        return executeMojo( projectName, properties, true, true );
    }

    /**
     * Executes the specified projects and asserts the given artifacts. Assert the deployment descriptors are valid
     *
     * @param projectName the project to test
     * @param earModuleName the name of 1st level EAR module in multi-module project or null if project is single-module
     * @param expectedArtifacts the list of artifacts to be found in the EAR archive
     * @param artifactsDirectory whether the artifact is an exploded artifactsDirectory or not
     * @param cleanBeforeExecute call clean plugin before execution
     * @return the base directory of the project
     */
    protected File doTestProject( final String projectName, final String earModuleName, final String[] expectedArtifacts,
                                  final boolean[] artifactsDirectory, boolean cleanBeforeExecute )
        throws VerificationException, IOException
    {
        final File baseDir = executeMojo( projectName, new Properties(), true, cleanBeforeExecute );
        final File earDir = getEarModuleDirectory( baseDir, earModuleName );
        assertEarArchive( earDir, projectName );
        assertEarDirectory( earDir, projectName );

        assertArchiveContent( earDir, projectName, expectedArtifacts, artifactsDirectory );

        assertDeploymentDescriptors( earDir, projectName );

        return baseDir;
    }

    /**
     * Executes the specified projects and asserts the given artifacts. Asserts the deployment descriptors are valid.
     * Asserts Class-Path entry of manifest of EAR modules.
     *
     * @param projectName the project to test
     * @param earModuleName the name of 1st level EAR module in multi-module project or null if project is single-module
     * @param expectedArtifacts the list of artifacts to be found in the EAR archive
     * @param moduleArtifacts the list of artifacts representing EAR modules which manifest needs to be asserted
     * @param expectedClassPathElements the list of elements of Class-Path entry of manifest. Rows should match
     *                                  modules passed in {@code moduleArtifacts} parameter
     * @return the base directory of the project
     */
    protected File doTestProject( final String projectName, final String earModuleName, final String[] expectedArtifacts,
                                  final String[] moduleArtifacts, final String[][] expectedClassPathElements )
        throws VerificationException, IOException
    {
        assertEquals( "Rows of expectedClassPathElements parameter should match items of moduleArtifacts parameter",
            moduleArtifacts.length, expectedClassPathElements.length );

        final File baseDir = doTestProject( projectName, earModuleName, expectedArtifacts, new boolean[expectedArtifacts.length], true );

        final File earFile = getEarArchive( getEarModuleDirectory( baseDir, earModuleName ), projectName );
        for ( int i = 0; i != moduleArtifacts.length; ++i )
        {
            final String moduleArtifact = moduleArtifacts[i];
            Assert.assertArrayEquals( "Wrong elements of Class-Path entry of module [" + moduleArtifact + "] manifest",
                expectedClassPathElements[i], getClassPathElements( earFile, moduleArtifact ) );
        }

        return baseDir;
    }

    /**
     * Extracts elements of Class-Path entry of manifest of given EAR module.
     *
     * @param earFile the EAR file to investigate
     * @param moduleArtifact the name of artifact in EAR representing EAR module
     * @return elements of Class-Path entry of manifest of EAR module which is represented by
     * {@code moduleArtifact} artifact in {@code earFile} file
     */
    protected String[] getClassPathElements( final File earFile, final String moduleArtifact ) throws IOException
    {
        try ( JarFile earJarFile = new JarFile( earFile ) )
        {
            final ZipEntry moduleEntry = earJarFile.getEntry( moduleArtifact );
            assertNotNull( "Artifact [" + moduleArtifact + "] should exist in EAR", moduleEntry );
            try ( InputStream moduleInputStream = earJarFile.getInputStream( moduleEntry );
                  JarInputStream moduleJarInputStream = new JarInputStream( moduleInputStream ) )
            {
                final Manifest manifest = moduleJarInputStream.getManifest();
                assertNotNull( "Artifact [" + moduleArtifact + "] of EAR should have manifest", manifest );
                final String classPath = manifest.getMainAttributes().getValue( "Class-Path" );
                if ( classPath == null )
                {
                    return new String[0];
                }
                return classPath.split( " " );
            }
        }
    }

    /**
     * Executes the specified projects and asserts the given artifacts. Assert the deployment descriptors are valid
     *
     * @param projectName the project to test
     * @param expectedArtifacts the list of artifacts to be found in the EAR archive
     * @param artifactsDirectory whether the artifact is an exploded artifactsDirectory or not
     * @return the base directory of the project
     */
    protected File doTestProject( final String projectName, final String[] expectedArtifacts,
                                  final boolean[] artifactsDirectory )
        throws VerificationException, IOException
    {
        return doTestProject( projectName, null, expectedArtifacts, artifactsDirectory, true );
    }

    /**
     * Executes the specified projects and asserts the given artifacts as artifacts (non directory)
     * 
     * @param projectName the project to test
     * @param expectedArtifacts the list of artifacts to be found in the EAR archive
     * @param testDeploymentDescriptors whether we should test deployment descriptors
     * @return the base directory of the project
     */
    private File doTestProject( final String projectName, final String[] expectedArtifacts,
                                boolean testDeploymentDescriptors )
        throws VerificationException, IOException
    {
        return doTestProject( projectName, expectedArtifacts, new boolean[expectedArtifacts.length] );
    }

    /**
     * Executes the specified projects and asserts the given artifacts as artifacts (non directory). Assert the
     * deployment descriptors are valid
     * 
     * @param projectName the project to test
     * @param expectedArtifacts the list of artifacts to be found in the EAR archive
     * @return the base directory of the project
     * @throws Exception Mojo exception in case of an error.
     */
    protected File doTestProject( final String projectName, final String[] expectedArtifacts )
        throws Exception
    {
        return doTestProject( projectName, expectedArtifacts, true );
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
        final List<File> expectedDirectories = new ArrayList<File>();
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

    private static List<File> buildArchiveContentFiles( final File baseDir, final List<File> expectedDirectories )
    {
        final List<File> result = new ArrayList<File>();
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
     * This test assumes that deployment descriptors are located in the <tt>expected-META-INF</tt> directory of the
     * project. Note that the <tt>MANIFEST.mf</tt> file is ignored and is not tested.
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
}
