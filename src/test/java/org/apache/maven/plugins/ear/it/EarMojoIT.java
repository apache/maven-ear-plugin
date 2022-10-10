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
import java.io.FileInputStream;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;

/**
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
public class EarMojoIT
    extends AbstractEarPluginIT
{

    /**
     * Builds an EAR with a single EJB and no configuration.
     */
    public void testProject001()
        throws Exception
    {
        doTestProject( "project-001", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with a customized artifact location and a customized artifact name.
     */
    public void testProject002()
        throws Exception
    {
        doTestProject( "project-002",
                       new String[] { "APP-INF/lib/eartest-ejb-sample-one-1.0.jar", "ejb-sample-two.jar" } );
    }

    /**
     * Builds an EAR with a default bundle directory for {@code java} modules.
     */
    public void testProject003()
        throws Exception
    {
        doTestProject( "project-003", new String[] { "eartest-ejb-sample-one-1.0.jar",
            "APP-INF/lib/eartest-jar-sample-one-1.0.jar", "APP-INF/lib/eartest-jar-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with a default bundle directory for _java_ modules and a custom location overriding the default.
     */
    public void testProject004()
        throws Exception
    {
        doTestProject( "project-004", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-jar-sample-one-1.0.jar",
            "APP-INF/lib/eartest-jar-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with a custom URI.
     */
    public void testProject005()
        throws Exception
    {
        doTestProject( "project-005", new String[] { "eartest-ejb-sample-one-1.0.jar", "libs/another-name.jar" } );
    }

    /**
     * Builds an EAR with an excluded module.
     */
    public void testProject006()
        throws Exception
    {
        doTestProject( "project-006",
                       new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-jar-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with a classified artifact and no extra configuration.
     */
    public void testProject007()
        throws Exception
    {
        doTestProject( "project-007", new String[] { "eartest-ejb-sample-one-1.0-classified.jar" } );
    }

    /**
     * Builds an EAR with deployment descriptor configuration for J2EE 1.3.
     */
    public void testProject008()
        throws Exception
    {
        doTestProject( "project-008", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with deployment descriptor configuration for J2EE 1.4.
     */
    public void testProject009()
        throws Exception
    {
        doTestProject( "project-009", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with deployment descriptor configuration for Java EE 5.
     */
    public void testProject010()
        throws Exception
    {
        doTestProject( "project-010", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR and make sure that deployment descriptor default settings are applied.
     */
    public void testProject011()
        throws Exception
    {
        doTestProject( "project-011", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR and make sure that EAR resources are bundled within the EAR.
     */
    public void testProject012()
        throws Exception
    {
        doTestProject( "project-012", new String[] { "README.txt", "LICENSE.txt", "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR and make sure that EAR resources in a customized resources directory are bundled within the EAR.
     */
    public void testProject013()
        throws Exception
    {
        doTestProject( "project-013", new String[] { "README.txt", "LICENSE.txt", "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR and make sure that EAR resources are bundled within the EAR using includes and excludes.
     */
    public void testProject014()
        throws Exception
    {
        doTestProject( "project-014", new String[] { "LICENSE.txt", "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR and make sure that default manifest is taken into account.
     */
    public void testProject015()
        throws Exception
    {
        final File baseDir = doTestProject( "project-015", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
        final File expectedManifest = new File( baseDir, "src/main/application/META-INF/MANIFEST.MF" );
        final File actualManifest = new File( getEarDirectory( baseDir, "project-015" ), "META-INF/MANIFEST.MF" );
        assertTrue( "Manifest was not copied", actualManifest.exists() );
        assertTrue( FileUtils.contentEquals( expectedManifest, actualManifest ) );
    }

    /**
     * Builds an EAR and make sure that custom manifest is taken into account.
     */
    public void testProject016()
        throws Exception
    {
        final File baseDir = doTestProject( "project-016", new String[] { "eartest-ejb-sample-one-1.0.jar" } );

        final File createdEarFile = getEarArchive( baseDir, "project-016" );

        final File sourceManifestFile = new File( baseDir, "src/main/ear/MANIFEST.MF" );

        try ( JarFile jarFile = new JarFile( createdEarFile );
              FileInputStream in = new FileInputStream( sourceManifestFile ) )
        {    
            Manifest manifestFromCreatedEARFile = jarFile.getManifest();
            Manifest sourceManifest = new Manifest( in );
            assertEquals( "There are differences in the manifest.", sourceManifest, manifestFromCreatedEARFile );
        }
     }

    /**
     * Builds an EAR and make sure that custom application.xml is taken into account.
     */
    public void testProject017()
        throws Exception
    {
        doTestProject( "project-017", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with a custom final name.
     */
    public void testProject018()
        throws Exception
    {
        final File baseDir = executeMojo( "project-018" );
        final File expectedFile = new File( baseDir, "target/my-custom-file.ear" );
        assertTrue( "EAR archive not found", expectedFile.exists() );
    }

    /**
     * Builds an EAR with unpacked archives using the unpackTypes.
     */
    public void testProject019()
        throws Exception
    {
        doTestProject( "project-019", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-sar-sample-one-1.0.sar",
            "eartest-jar-sample-one-1.0.jar" }, new boolean[] { false, true, true } );
    }

    /**
     * Builds an EAR with unpacked archives using the unpack module attribute.
     */
    public void testProject020()
        throws Exception
    {
        doTestProject( "project-020", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-sar-sample-one-1.0.sar",
            "eartest-jar-sample-one-1.0.jar" }, new boolean[] { true, false, false } );
    }

    /**
     * Builds an EAR with unpacked archives using both unpackTypes and the unpack module attribute.
     */
    public void testProject021()
        throws Exception
    {
        doTestProject( "project-021",
                       new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar",
                           "eartest-sar-sample-one-1.0.sar", "eartest-jar-sample-one-1.0.jar",
                           "eartest-jar-sample-two-1.0.jar" },
                       new boolean[] { false, true, false, false, true } );
    }

    /**
     * Builds an EAR with a classifier.
     */
    public void testProject022()
        throws Exception
    {
        final File baseDir = executeMojo( "project-022" );
        final File expectedFile = new File( baseDir, "target/maven-ear-plugin-test-project-022-99.0-myclassifier.ear" );
        assertTrue( "EAR archive not found", expectedFile.exists() );
    }

    /**
     * Builds an EAR and make sure that a single classified dependency is detected without specifying the classifier.
     */
    public void testProject023()
        throws Exception
    {
        doTestProject( "project-023",
                       new String[] { "eartest-ejb-sample-one-1.0-classified.jar", "eartest-ejb-sample-two-1.0.jar" },
                       new boolean[] { true, false } );
    }

    /**
     * Builds an EAR and make sure that a single classified dependency is detected when specifying the classifier.
     */
    public void testProject024()
        throws Exception
    {
        doTestProject( "project-024",
                       new String[] { "eartest-ejb-sample-one-1.0-classified.jar", "eartest-ejb-sample-two-1.0.jar" },
                       new boolean[] { true, false } );
    }

    /**
     * Builds an EAR and make sure that a classified dependency with multiple candidates is detected when specifying the
     * classifier.
     */
    public void testProject025()
        throws Exception
    {
        doTestProject( "project-025",
                       new String[] { "eartest-ejb-sample-one-1.0-classified.jar", "eartest-ejb-sample-one-1.0.jar" },
                       new boolean[] { true, false } );
    }

    /**
     * Builds an EAR and make sure that the build fails if a unclassifed module configuration with multiple candidates is
     * specified.
     */
    public void testProject026()
        throws Exception
    {
        final File baseDir = executeMojo( "project-026", false, true );
        // Stupido, checks that the ear archive is not there
        assertFalse( "Execution should have failed", getEarArchive( baseDir, "project-026" ).exists() );
    }

    /**
     * Builds an EAR and make sure that provided dependencies are not included in the EAR.
     */
    public void testProject027()
        throws Exception
    {
        doTestProject( "project-027", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR and make sure that test dependencies are not included in the EAR.
     */
    public void testProject028()
        throws Exception
    {
        doTestProject( "project-028", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR and make sure that system dependencies are not included in the EAR.
     */
    public void testProject029()
        throws Exception
    {
        doTestProject( "project-029", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR and make sure that ejb-client dependencies are detected and not added by default in the generated
     * application.xml.
     */
    public void testProject030()
        throws Exception
    {
        doTestProject( "project-030",
                       new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0-client.jar" } );
    }

    /**
     * Builds an EAR with a Jboss 4 configuration specifying the security domain and the unauthenticated-principal to
     * use.
     */
    public void testProject031()
        throws Exception
    {
        doTestProject( "project-031",
                       new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with a Jboss 3.2 configuration specifying the jmx-name to use.
     */
    public void testProject032()
        throws Exception
    {
        doTestProject( "project-032",
                       new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with a Jboss 4 configuration and Jboss specific modules.
     */
    public void testProject033()
        throws Exception
    {
        doTestProject( "project-033", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar",
            "eartest-sar-sample-one-1.0.sar", "eartest-har-sample-one-1.0.har" } );
    }

    /**
     * Builds an EAR with custom security settings.
     */
    public void testProject034()
        throws Exception
    {
        doTestProject( "project-034",
                       new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with a full filename mapping and make sure that custom locations are not overridden.
     */
    public void testProject035()
        throws Exception
    {
        doTestProject( "project-035",
                       new String[] { "foo/eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar",
                           "libs/eartest-jar-sample-one-1.0.jar", "libs/eartest-jar-sample-two-1.0.jar",
                           "sar-sample-one.sar" } );
    }

    /**
     * Builds an EAR with a full filename mapping and make sure that groupIds with dots are replaced by dashes in
     * filenames.
     */
    public void testProject036()
        throws Exception
    {
        doTestProject( "project-036",
                       new String[] { "foo/eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar",
                           "com.foo.bar-ejb-sample-one-1.0.jar", "com.foo.bar-ejb-sample-two-1.0.jar",
                           "libs/eartest-jar-sample-one-1.0.jar", "libs/eartest-jar-sample-two-1.0.jar",
                           "sar-sample-one.sar" } );
    }

    /**
     * Builds an EAR and make sure that ejb-client dependencies are detected and added in the generated application.xml
     * if includeInApplicationXml is set.
     */
    public void testProject037()
        throws Exception
    {
        doTestProject( "project-037", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0-client.jar" } );
    }

    /**
     * Builds an EAR and make sure that a non-classified dependency with multiple candidates is detected when specifying
     * the mainArtifactId as classifier.
     */
    public void testProject038()
        throws Exception
    {
        doTestProject( "project-038", new String[] { "eartest-ejb-sample-one-1.0-classified.jar", "eartest-ejb-sample-one-1.0.jar" },
                       new boolean[] { false, true } );
    }

    /**
     * Builds an EAR with a Jboss 4 configuration specifying specifying the loader repository to use.
     */
    public void testProject039()
        throws Exception
    {
        doTestProject( "project-039", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with deployment descriptor configuration for Java EE 5 and an alternative deployment descriptor.
     */
    public void testProject040()
        throws Exception
    {
        doTestProject( "project-040", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with a Jboss 4.2 configuration specifying the module order to use.
     */
    public void testProject041()
        throws Exception
    {
        doTestProject( "project-041", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with a Jboss 4.2 configuration specifying a datasource to add.
     */
    public void testProject042()
        throws Exception
    {
        doTestProject( "project-042", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with a custom descriptor location (generatedDescriptorLocation setting).
     */
    public void testProject043()
        throws Exception
    {
        final File baseDir = doTestProject( "project-043", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
        final File expectedApplicationXml = new File( baseDir, "target/custom-descriptor-dir/application.xml" );
        assertTrue( "Application.xml file not found", expectedApplicationXml.exists() );
        assertFalse( "Application.xml file should not be empty", expectedApplicationXml.length() == 0 );
    }

    /**
     * Builds an EAR with a custom library-directory.
     */
    public void testProject044()
        throws Exception
    {
        doTestProject( "project-044", new String[] { "eartest-ejb-sample-one-1.0.jar", "myLibs/eartest-jar-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR and filter the content of the sources directory.
     */
    public void testProject045()
        throws Exception
    {
        final File baseDir = doTestProject( "project-045", new String[] { "README.txt", "eartest-ejb-sample-one-1.0.jar" } );
        final File actualReadme = new File( getEarDirectory( baseDir, "project-045" ), "README.txt" );
        final String content = IOUtils.toString( ReaderFactory.newReader( actualReadme, "UTF-8" ) );
        assertTrue( "application name and version was not filtered properly", content.contains( "my-app 99.0" ) );
        assertTrue( "Escaping did not work properly", content.contains( "will not be filtered ${application.name}." ) );
    }

    /**
     * Builds an EAR and filter the content of the sources directory using a custom filter file.
     */
    public void testProject046()
        throws Exception
    {
        final File baseDir = doTestProject( "project-046", new String[] { "README.txt", "eartest-ejb-sample-one-1.0.jar" } );
        final File actualReadme = new File( getEarDirectory( baseDir, "project-046" ), "README.txt" );
        final String content = IOUtils.toString( ReaderFactory.newReader( actualReadme, "UTF-8" ) );
        assertTrue( "application name and version was not filtered properly", content.contains( "my-app 99.0" ) );
        assertTrue( "application build was not filtered properly", content.contains( "(Build 2)" ) );
        assertTrue( "Unknown property should not have been filtered",
                    content.contains( "will not be filtered ${application.unknown}." ) );
    }

    /**
     * Builds an EAR and filter the content with a list of extensions.
     */
    public void testProject047()
        throws Exception
    {
        final File baseDir = doTestProject( "project-047", new String[] { "README.txt", "eartest-ejb-sample-one-1.0.jar" } );
        final File actualReadme = new File( getEarDirectory( baseDir, "project-047" ), "README.txt" );
        final String content = IOUtils.toString( ReaderFactory.newReader( actualReadme, "UTF-8" ) );
        assertTrue( "application name and version should not have been filtered", !content.contains( "my-app 99.0" ) );
        assertTrue( "original properties not found", content.contains( "${application.name} ${project.version}" ) );
    }

    /**
     * Builds an EAR with a JBoss 5 configuration containing library directory.
     */
    public void testProject048()
        throws Exception
    {
        doTestProject( "project-048", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with a JBoss 4.2 configuration containing a library directory.
     */
    public void testProject049()
        throws Exception
    {
        doTestProject( "project-049", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with a Jboss 5 configuration containing a loader repository configuration definition.
     */
    public void testProject050()
        throws Exception
    {
        doTestProject( "project-050", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with a Jboss 5 configuration containing a loader repository class definition.
     */
    public void testProject051()
        throws Exception
    {
        doTestProject( "project-051", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with a Jboss 5 configuration containing a configuration parser class definition.
     */
    public void testProject052()
        throws Exception
    {
        doTestProject( "project-052", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with a Jboss 5 configuration containing only the loader repo configuration
     */
    public void testProject053()
        throws Exception
    {
        doTestProject( "project-053", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with deployment descriptor configuration for Java EE 5 and no application.xml
     */
    public void testProject054()
        throws Exception
    {
        doTestProject( "project-054", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with jar dependencies added in application.xml.
     */
    public void testProject055()
        throws Exception
    {
        doTestProject( "project-055", new String[] { "eartest-jar-sample-one-1.0.jar", "eartest-jar-sample-two-1.0.jar",
            "eartest-jar-sample-three-with-deps-1.0.jar" } );
    }

    /**
     * Builds an EAR with deployment descriptor configuration for J2EE 1.4 and an alternative deployment descriptor.
     */
    public void testProject056()
        throws Exception
    {
        doTestProject( "project-056", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with a complete JBoss 4.2 configuration and validate it matches the DTD (MEAR-104).
     */
    public void testProject057()
        throws Exception
    {
        doTestProject( "project-057", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with deployment descriptor configuration for Java EE 6.
     */
    public void testProject058()
        throws Exception
    {
        doTestProject( "project-058", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with no display name entry at all.
     */
    public void testProject059()
        throws Exception
    {
        doTestProject( "project-059", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with ejb-client packaged for J2EE 1.3 (MEAR-85)
     */
    public void testProject060()
        throws Exception
    {
        doTestProject( "project-060", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0-client.jar" } );
    }

    /**
     * Builds an EAR with ejb-client packaged for J2EE 1.4 (MEAR-85)
     */
    public void testProject061()
        throws Exception
    {
        doTestProject( "project-061", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0-client.jar" } );
    }

    /**
     * Builds an EAR with ejb-client packaged for JavaEE 5 (MEAR-85)
     */
    public void testProject062()
        throws Exception
    {
        doTestProject( "project-062", new String[] { "eartest-ejb-sample-one-1.0.jar", "lib/eartest-ejb-sample-two-1.0-client.jar" } );
    }

    /**
     * Builds an EAR with ejb-client packaged for JavaEE 6 (MEAR-85)
     */
    public void testProject063()
        throws Exception
    {
        doTestProject( "project-063", new String[] { "lib/eartest-ejb-sample-two-1.0-client.jar" } );
    }

    /**
     * Builds an EAR with ejb-client packaged for JavaEE 5 and still put it in the root (MEAR-85)
     */
    public void testProject064()
        throws Exception
    {
        doTestProject( "project-064", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0-client.jar" } );
    }

    /**
     * Builds an EAR with a custom moduleId.
     */
    public void testProject065()
        throws Exception
    {
        doTestProject( "project-065", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with generateModuleId enabled.
     */
    public void testProject066()
        throws Exception
    {
        doTestProject( "project-066", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with generateModuleId enabled and a custom module.
     */
    public void testProject067()
        throws Exception
    {
        doTestProject( "project-067", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with the no-version file name mapping.
     */
    public void testProject068()
        throws Exception
    {
        doTestProject( "project-068", new String[] { "eartest-ejb-sample-one.jar", "eartest-ejb-sample-two.jar" } );
    }

    /**
     * Builds an EAR with a custom library-directory and JavaEE 6.
     */
    public void testProject069()
        throws Exception
    {
        doTestProject( "project-069", new String[] { "eartest-ejb-sample-one-1.0.jar", "myLibs/eartest-jar-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with application-name and initialize-in-order tags.
     */
    public void testProject070()
        throws Exception
    {
        doTestProject( "project-070", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-jar-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with application-name and initialize-in-order tags for unsupported version.
     */
    public void testProject071()
        throws Exception
    {
        doTestProject( "project-071", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-jar-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with an application client module (app-client).
     */
    public void testProject072()
        throws Exception
    {
        doTestProject( "project-072", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-app-client-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with an application client module (app-client) and a default bundle directory for _java_ modules.
     */
    public void testProject073()
        throws Exception
    {
        doTestProject( "project-073", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-app-client-sample-one-1.0.jar",
            "APP-INF/lib/eartest-jar-sample-one-1.0.jar", "APP-INF/lib/eartest-jar-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with custom env entries settings and J2EE 1.3. Not supported by the specification so this should be
     * ignored.
     */
    public void testProject074()
        throws Exception
    {
        doTestProject( "project-074", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with custom env entries settings and J2EE 1.4. Not supported by the specification so this should be
     * ignored.
     */
    public void testProject075()
        throws Exception
    {
        doTestProject( "project-075", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with custom env entries settings and JavaEE 5. Not supported by the specification so this should be
     * ignored.
     */
    public void testProject076()
        throws Exception
    {
        doTestProject( "project-076", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with custom env entries settings and JavaEE 6.
     */
    public void testProject077()
        throws Exception
    {
        doTestProject( "project-077", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with the no version for ejb file name mapping.
     */
    public void testProject078()
        throws Exception
    {
        doTestProject( "project-078",
                       new String[] { "ejb-sample-one.jar", "war-sample-one.war", "jar-sample-two.jar" } );
    }

    /**
     * Builds an EAR with the 'default' library directory mode. Uses the value of the defaultLibBundleDir.
     */
    public void testProject079()
        throws Exception
    {
        doTestProject( "project-079", new String[] { "eartest-ejb-sample-one-1.0.jar", "myLibs/eartest-jar-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with the 'empty' library directory mode. Generate an empty library-directory element.
     */
    public void testProject080()
        throws Exception
    {
        doTestProject( "project-080", new String[] { "eartest-ejb-sample-one-1.0.jar", "myLibs/eartest-jar-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with the 'none' library directory mode. Does not generate an library-directory element.
     */
    public void testProject081()
        throws Exception
    {
        doTestProject( "project-081", new String[] { "eartest-ejb-sample-one-1.0.jar", "myLibs/eartest-jar-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with deployment descriptor configuration for JavaEE 7.
     */
    public void testProject082()
        throws Exception
    {
        doTestProject( "project-082", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with a library directory and custom env entries. The library-directory element must come first
     * (MEAR-158).
     */
    public void testProject083()
        throws Exception
    {
        doTestProject( "project-083", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Support of an application id (MEAR-174).
     */
    public void testProject084()
        throws Exception
    {
        doTestProject( "project-084", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with custom ejbRef entries settings and JavaEE 6.
     */
    public void testProject085()
        throws Exception
    {
        doTestProject( "project-085", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with custom ejbRef entries plus lookup-name entry.
     */
    public void testProject086()
        throws Exception
    {
        doTestProject( "project-086", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds an EAR with resource-ref entries.
     */
    public void testProject087()
        throws Exception
    {
        doTestProject( "project-087", new String[] { "eartest-ejb-sample-one-1.0.jar", "eartest-ejb-sample-two-1.0.jar" } );
    }

    /**
     * Builds WAR and EAR as part of multi-module project twice so that the 2nd build is guaranteed to be performed when
     * target directories and files exist.
     */
    public void testProject088()
        throws Exception
    {
        final String warModule = "eartest-war-sample-two-1.0.war";
        final String ejbModule = "eartest-ejb-sample-one-1.0.jar";
        final String jarSampleTwoLibrary = "lib/eartest-jar-sample-two-1.0.jar";
        final String[] expectedArtifacts = { warModule, ejbModule, jarSampleTwoLibrary };
        final boolean[] artifactsDirectory = { false, true, false };
        final String[] artifactsToValidateManifest = { warModule, ejbModule };
        final boolean[] artifactsToValidateManifestDirectory = { false, true };
        final String[][] expectedClassPathElements = { { jarSampleTwoLibrary }, { jarSampleTwoLibrary } };

        // "Clean" build - target directories and files do not exist
        // Pass cleanBeforeExecute parameter to ensure that target location is cleaned before Mojo execution
        doTestProject( "project-088", "ear", expectedArtifacts, artifactsDirectory,
            artifactsToValidateManifest, artifactsToValidateManifestDirectory, expectedClassPathElements, true );
        // "Dirty" build - target directories and files exist
        doTestProject( "project-088", "ear", expectedArtifacts, artifactsDirectory,
            artifactsToValidateManifest, artifactsToValidateManifestDirectory, expectedClassPathElements, false );
    }

    /**
     * Validates modification of Class-Path entry of EAR modules manifest when
     * <ul>
     * <li>skinnyWars option is turned on</li>
     * <li>skipClassPathModification option is turned off</li>
     * </ul>
     */
    public void testProject089()
        throws Exception
    {
        final String warModule = "eartest-war-sample-three-1.0.war";
        final String ejbModule = "eartest-ejb-sample-three-1.0.jar";
        final String jarSampleTwoLibrary = "lib/eartest-jar-sample-two-1.0.jar";
        final String jarSampleThreeLibrary = "lib/eartest-jar-sample-three-with-deps-1.0.jar";
        doTestProject( "project-089", "ear",
            new String[] { warModule, ejbModule, jarSampleTwoLibrary, jarSampleThreeLibrary },
            new boolean[] { false, false, false, false},
            new String[] { warModule, ejbModule },
            new boolean[] { false, false },
            new String[][] { { jarSampleTwoLibrary, jarSampleThreeLibrary }, { jarSampleThreeLibrary, jarSampleTwoLibrary } },
            true );
    }

    /**
     * Validates modification of Class-Path entry of EAR modules manifest when
     * <ul>
     * <li>skinnyWars option is turned on</li>
     * <li>skipClassPathModification option is turned on</li>
     * </ul>
     */
    public void testProject090()
        throws Exception
    {
        final String warModule = "eartest-war-sample-three-1.0.war";
        final String ejbModule = "eartest-ejb-sample-three-1.0.jar";
        final String jarSampleTwoLibrary = "lib/eartest-jar-sample-two-1.0.jar";
        final String jarSampleThreeLibrary = "lib/eartest-jar-sample-three-with-deps-1.0.jar";
        doTestProject( "project-090", "ear",
            new String[] { warModule, ejbModule, jarSampleTwoLibrary, jarSampleThreeLibrary },
            new boolean[] { false, false, false, false },
            new String[] { warModule, ejbModule },
            new boolean[] { false, false },
            new String[][] { { jarSampleTwoLibrary }, { jarSampleThreeLibrary, jarSampleTwoLibrary } },
            true );
    }

    /**
     * Validates modification of Class-Path entry of EAR modules manifest when
     * <ul>
     * <li>skinnyWars option is turned off</li>
     * <li>skipClassPathModification option is turned off</li>
     * <li>unpacking of EJB JARs is turned on</li>
     * </ul>
     */
    public void testProject091()
        throws Exception
    {
        final String warModule = "eartest-war-sample-three-1.0.war";
        final String ejbModule = "eartest-ejb-sample-three-1.0.jar";
        final String jarSampleTwoLibrary = "eartest-jar-sample-two-1.0.jar";
        final String jarSampleThreeLibrary = "eartest-jar-sample-three-with-deps-1.0.jar";
        doTestProject( "project-091", "ear",
            new String[] { warModule, ejbModule, jarSampleTwoLibrary, jarSampleThreeLibrary },
            new boolean[] { false, true, false, false },
            new String[] { warModule, ejbModule },
            new boolean[] { false, true },
            new String[][] { { "jar-sample-two-1.0.jar" }, { jarSampleThreeLibrary, jarSampleTwoLibrary } },
            true );
    }

    /**
     * Ensures that when
     * <ul>
     * <li>skinnyWars option is turned off (has default value)</li>
     * <li>skinnyModules options is turned on</li>
     * </ul>
     * then movement of JARs and modification of manifest Class-Path entry is performed for WAR, SAR, HAR and RAR
     * modules. Additionally this test ensures that
     * <ul>
     * <li>movement of JARs is not performed for modules whose libDirectory property doesn't point to the correct module
     * entry containing JAR libraries packaged into the module</li>
     * <li>JAR with provided scope is removed from modules and from Class-Path entries</li>
     * </ul>
     */
    public void testProject092()
        throws Exception
    {
        final String projectName = "project-092";
        final String earModuleName = "ear";
        final String jarSampleOneLibrary = "jar-sample-one-1.0.jar";
        final String jarSampleTwoLibrary = "jar-sample-two-1.0.jar";
        final String jarSampleThreeLibrary = "jar-sample-three-with-deps-1.0.jar";
        final String jarSampleOneEarLibrary = "libs/eartest-" + jarSampleOneLibrary;
        final String jarSampleTwoEarLibrary = "libs/eartest-" + jarSampleTwoLibrary;
        final String jarSampleThreeEarLibrary = "libs/eartest-" + jarSampleThreeLibrary;
        final String warModule = "eartest-war-sample-three-1.0.war";
        final String sarModuleTwo = "eartest-sar-sample-two-1.0.sar";
        final String sarModuleThree = "eartest-sar-sample-three-1.0.sar";
        final String sarModuleFour = "eartest-sar-sample-four-1.0.sar";
        final String harModule = "eartest-har-sample-two-1.0.har";
        final String rarModule = "eartest-rar-sample-one-1.0.rar";
        final String[] earModules = { warModule, sarModuleTwo, sarModuleThree, sarModuleFour, harModule, rarModule };
        final boolean[] earModuleDirectory = { false, false, false, false, false, false };
        final String warModuleLibDir = "WEB-INF/lib/";
        final String sarModuleTwoLibDir = "libraries/";
        final String sarModuleThreeLibDir = "";
        final String sarModuleFourLibDir = "lib/";
        final String harModuleLibDir = "lib/";
        final String rarModuleLibDir = "";

        final File baseDir = doTestProject( projectName, earModuleName,
            new String[] { warModule, sarModuleTwo, sarModuleThree, sarModuleFour, harModule, rarModule,
                jarSampleOneEarLibrary, jarSampleTwoEarLibrary, jarSampleThreeEarLibrary },
            new boolean[] { false, false, false, false, false, false, false, false, false },
            earModules, earModuleDirectory,
            new String[][] {
                { jarSampleTwoEarLibrary, jarSampleOneEarLibrary, jarSampleThreeEarLibrary },
                { jarSampleThreeEarLibrary, jarSampleTwoEarLibrary, jarSampleOneEarLibrary },
                { jarSampleThreeEarLibrary, jarSampleTwoEarLibrary, jarSampleOneEarLibrary },
                { jarSampleOneEarLibrary, jarSampleTwoEarLibrary, jarSampleThreeEarLibrary },
                { jarSampleOneEarLibrary, jarSampleThreeEarLibrary, jarSampleTwoEarLibrary },
                { jarSampleThreeEarLibrary, jarSampleTwoEarLibrary, jarSampleOneEarLibrary } },
            true );

        assertEarModulesContent( baseDir, projectName, earModuleName, earModules, earModuleDirectory,
            new String[][] {
                { warModuleLibDir },
                { sarModuleTwoLibDir },
                { sarModuleThreeLibDir },
                { sarModuleFourLibDir + jarSampleOneLibrary },
                { harModuleLibDir },
                { rarModuleLibDir } },
            new String[][] {
                { warModuleLibDir + jarSampleTwoLibrary },
                { sarModuleTwoLibDir + jarSampleTwoLibrary, sarModuleTwoLibDir + jarSampleThreeLibrary },
                { sarModuleThreeLibDir + jarSampleTwoLibrary, sarModuleThreeLibDir + jarSampleThreeLibrary },
                { },
                { harModuleLibDir + jarSampleOneLibrary, harModuleLibDir + jarSampleTwoLibrary, harModuleLibDir + jarSampleThreeLibrary },
                { rarModuleLibDir + jarSampleTwoLibrary, rarModuleLibDir + jarSampleThreeLibrary } } );
    }

    /**
     * Ensures that when
     * <ul>
     * <li>skinnyWars option is turned on</li>
     * <li>skinnyModules options is turned off (has default value)</li>
     * </ul>
     * then movement of JARs and modification of manifest Class-Path entry is performed only for WAR module and not for
     * SAR, HAR and RAR modules.
     */
    public void testProject093()
        throws Exception
    {
        final String projectName = "project-093";
        final String earModuleName = "ear";
        final String jarSampleOneLibrary = "jar-sample-one-1.0.jar";
        final String jarSampleTwoLibrary = "jar-sample-two-1.0.jar";
        final String jarSampleThreeLibrary = "jar-sample-three-with-deps-1.0.jar";
        final String jarSampleTwoEarLibrary = "lib/eartest-" + jarSampleTwoLibrary;
        final String jarSampleThreeEarLibrary = "lib/eartest-" + jarSampleThreeLibrary;
        final String warModule = "eartest-war-sample-three-1.0.war";
        final String sarModule = "eartest-sar-sample-two-1.0.sar";
        final String harModule = "eartest-har-sample-two-1.0.har";
        final String rarModule = "eartest-rar-sample-one-1.0.rar";
        final String[] earModules = { warModule, sarModule, harModule, rarModule };
        final boolean[] earModuleDirectory = { false, false, false, false };
        final String warModuleLibDir = "WEB-INF/lib/";
        final String sarModuleLibDir = "lib/";
        final String harModuleLibDir = "lib/";
        final String rarModuleLibDir = "";

        final File baseDir = doTestProject( projectName, earModuleName,
            new String[] { warModule, sarModule, harModule, rarModule, jarSampleTwoEarLibrary, jarSampleThreeEarLibrary },
            new boolean[] { false, false, false, false, false, false },
            earModules, earModuleDirectory,
            new String[][] {
                { jarSampleThreeEarLibrary, jarSampleTwoEarLibrary },
                { jarSampleThreeLibrary, jarSampleTwoLibrary, jarSampleOneLibrary },
                null,
                { jarSampleOneLibrary, jarSampleThreeLibrary, jarSampleTwoLibrary } },
            true );

        assertEarModulesContent( baseDir, projectName, earModuleName, earModules, earModuleDirectory,
            new String[][] {
                { warModuleLibDir },
                { sarModuleLibDir + jarSampleOneLibrary, sarModuleLibDir + jarSampleTwoLibrary, sarModuleLibDir + jarSampleThreeLibrary },
                { harModuleLibDir + jarSampleOneLibrary, harModuleLibDir + jarSampleTwoLibrary, harModuleLibDir + jarSampleThreeLibrary },
                { rarModuleLibDir + jarSampleOneLibrary, rarModuleLibDir + jarSampleTwoLibrary, rarModuleLibDir + jarSampleThreeLibrary } } ,
            new String[][] {
                { warModuleLibDir + jarSampleTwoLibrary, warModuleLibDir + jarSampleThreeLibrary },
                { },
                { },
                { } } );
    }

    /**
     * Ensures that when
     * <ul>
     * <li>skinnyWars option is turned off (has default value)</li>
     * <li>skinnyModules options is turned off (has default value)</li>
     * </ul>
     * then
     * <ul>
     * <li>movement of JARs and modification of the manifest Class-Path entry is not performed for WAR, SAR, HAR and
     * RAR modules</li>
     * <li>modification of the manifest Class-Path entry is performed for EJB module</li>
     * <li>provided JAR is removed from the manifest Class-Path entry of EJB module</li>
     * </ul>
     */
    public void testProject094()
        throws Exception
    {
        final String projectName = "project-094";
        final String earModuleName = "ear";
        final String jarSampleOneLibrary = "jar-sample-one-1.0.jar";
        final String jarSampleTwoLibrary = "jar-sample-two-1.0.jar";
        final String jarSampleThreeLibrary = "jar-sample-three-with-deps-1.0.jar";
        final String jarSampleTwoEarLibrary = "lib/eartest-" + jarSampleTwoLibrary;
        final String jarSampleThreeEarLibrary = "lib/eartest-" + jarSampleThreeLibrary;
        final String warModule = "eartest-war-sample-three-1.0.war";
        final String sarModule = "eartest-sar-sample-two-1.0.sar";
        final String harModule = "eartest-har-sample-two-1.0.har";
        final String rarModule = "eartest-rar-sample-one-1.0.rar";
        final String ejbModule = "eartest-ejb-sample-three-1.0.jar";
        final String[] earModules = { warModule, sarModule, harModule, rarModule, ejbModule };
        final boolean[] earModuleDirectory = { false, false, false, false, false };
        final String warModuleLibDir = "WEB-INF/lib/";
        final String sarModuleLibDir = "lib/";
        final String harModuleLibDir = "lib/";
        final String rarModuleLibDir = "";

        final File baseDir = doTestProject( projectName, earModuleName,
            new String[] { warModule, sarModule, harModule, rarModule, ejbModule, jarSampleTwoEarLibrary,
                jarSampleThreeEarLibrary },
            new boolean[] { false, false, false, false, false, false, false },
            earModules, earModuleDirectory,
            new String[][] { null, null, null, null,
                new String[] { jarSampleThreeEarLibrary, jarSampleTwoEarLibrary } },
            true );

        assertEarModulesContent( baseDir, projectName, earModuleName, earModules, earModuleDirectory,
            new String[][] {
                { warModuleLibDir + jarSampleTwoLibrary, warModuleLibDir + jarSampleThreeLibrary },
                { sarModuleLibDir + jarSampleOneLibrary, sarModuleLibDir + jarSampleTwoLibrary, sarModuleLibDir + jarSampleThreeLibrary },
                { harModuleLibDir + jarSampleOneLibrary, harModuleLibDir + jarSampleTwoLibrary, harModuleLibDir + jarSampleThreeLibrary },
                { rarModuleLibDir + jarSampleOneLibrary, rarModuleLibDir + jarSampleTwoLibrary, rarModuleLibDir + jarSampleThreeLibrary },
                null } ,
            null );
    }

    /**
     * Ensures that test JAR dependency of WAR is handled as regular JAR in terms of packaging and manifest modification
     * when skinnyWars option is turned on.
     */
    public void testProject095()
        throws Exception
    {
        final String warModule = "eartest-war-sample-two-1.0.war";
        final String jarSampleTwoLibrary = "lib/eartest-jar-sample-two-1.0.jar";
        final String jarSampleThreeLibrary = "lib/eartest-jar-sample-three-with-deps-1.0.jar";
        final String jarSampleFourTestLibrary = "lib/eartest-jar-sample-four-1.0-tests.jar";
        doTestProject( "project-095", "ear",
            new String[] { warModule, jarSampleTwoLibrary, jarSampleThreeLibrary, jarSampleFourTestLibrary },
            new boolean[] { false, false, false, false },
            new String[] { warModule },
            new boolean[] { false },
            new String[][] { { jarSampleFourTestLibrary, jarSampleThreeLibrary, jarSampleTwoLibrary } },
            true );
    }

    /**
     * Ensures that test JAR dependency representing Java module is described in deployment descriptor
     * if includeInApplicationXml property of module is {@code true}.
     */
    public void testProject096()
        throws Exception
    {
        final String warModule = "eartest-war-sample-two-1.0.war";
        final String jarSampleTwoLibrary = "eartest-jar-sample-two-1.0.jar";
        final String jarSampleThreeLibrary = "eartest-jar-sample-three-with-deps-1.0.jar";
        final String jarSampleFourTestLibrary = "eartest-jar-sample-four-1.0-tests.jar";
        final String jarSampleFiveLibrary = "eartest-jar-sample-five-1.0.jar";
        doTestProject( "project-096", "ear",
            new String[] { warModule, jarSampleTwoLibrary, jarSampleThreeLibrary, jarSampleFourTestLibrary, jarSampleFiveLibrary },
            new boolean[] { false, false, false, false, false },
            new String[] { warModule },
            new boolean[] { false },
            new String[][] { { jarSampleFourTestLibrary, jarSampleFiveLibrary, jarSampleThreeLibrary, jarSampleTwoLibrary } },
            true );
    }

    /**
     * Ensures that artifacts with jboss-sar, jboss-har and jboss-par types are packaged in EAR and
     * described in deployment descriptor when respective types are configured for EAR modules.
     */
    public void testProject097()
        throws Exception
    {
        final String warModule = "eartest-war-sample-three-1.0.war";
        final String sarSampleTwo = "eartest-sar-sample-two-1.0.sar";
        final String harSampleTwo = "eartest-har-sample-two-1.0.har";
        final String parSampleTwo = "eartest-par-sample-one-1.0.par";
        final String[] artifacts = { warModule, sarSampleTwo, harSampleTwo, parSampleTwo };
        final boolean[] artifactsDirectory = { false, false, false, false };
        doTestProject( "project-097", "ear", artifacts, artifactsDirectory, null, null, null , true );
    }

    /**
     * Ensures that when skinnyModules option is turned on then
     * <ul>
     * <li>EAR module whose classPathItem property is {@code false} is removed from the Class-Path entry of
     * MANIFEST.mf of other modules</li>
     * <li>EAR module whose classPathItem property is {@code true} is added into the Class-Path entry of MANIFEST.mf
     * or existing reference is updated to match location of the module</li>
     * <li>EAR module is removed from WARs and RARs (from modules which include their dependencies)</li>
     * </ul>
     */
    public void testProject098()
        throws Exception
    {
        final String projectName = "project-098";
        final String earModuleName = "ear";
        final String jarSampleOneLibrary = "jar-sample-one-1.0.jar";
        final String jarSampleTwoLibrary = "jar-sample-two-1.0.jar";
        final String jarSampleThreeLibrary = "jar-sample-three-with-deps-1.0.jar";
        final String ejbFourClientLibrary = "ejb-sample-four-1.0-client.jar";
        final String jarSampleOneEarLibrary = "lib/eartest-" + jarSampleOneLibrary;
        final String jarSampleTwoEarLibrary = "lib/eartest-" + jarSampleTwoLibrary;
        final String jarSampleThreeEarLibrary = "lib/eartest-" + jarSampleThreeLibrary;
        final String ejbFourClientEarLibrary = "lib/eartest-" + ejbFourClientLibrary;
        final String ejbThreeLibrary = "ejb-sample-three-1.0.jar";
        final String ejbFourLibrary = "ejb-sample-four-1.0.jar";
        final String ejbThreeModule = "eartest-" + ejbThreeLibrary;
        final String ejbFourModule = "eartest-" + ejbFourLibrary;
        final String rarLibrary = "rar-sample-one-1.0.rar";
        final String rarModule = "eartest-" + rarLibrary;
        final String warModule = "eartest-war-sample-three-1.0.war";
        final String[] earModules = { ejbThreeModule, ejbFourModule, rarModule, warModule };
        final boolean[] earModuleDirectory = { false, false, false, false };
        final String warModuleLibDir = "WEB-INF/lib/";
        final String rarModuleLibDir = "";

        final File baseDir = doTestProject( projectName, earModuleName,
            new String[] { ejbThreeModule, ejbFourModule, rarModule, warModule,
                jarSampleOneEarLibrary, jarSampleTwoEarLibrary, jarSampleThreeEarLibrary, ejbFourClientEarLibrary },
            new boolean[] { false, false, false, false, false, false, false, false },
            earModules, earModuleDirectory,
            new String[][] {
                { jarSampleThreeEarLibrary, jarSampleTwoEarLibrary, ejbFourClientEarLibrary, jarSampleOneEarLibrary },
                { jarSampleOneEarLibrary, jarSampleTwoEarLibrary, jarSampleThreeEarLibrary, ejbFourClientEarLibrary },
                { jarSampleThreeEarLibrary, jarSampleTwoEarLibrary, jarSampleOneEarLibrary, ejbFourClientEarLibrary },
                { jarSampleOneEarLibrary, jarSampleThreeEarLibrary, jarSampleTwoEarLibrary, ejbFourClientEarLibrary } },
            true );

        assertEarModulesContent( baseDir, projectName, earModuleName, earModules, earModuleDirectory,
            new String[][] { null, null, null, { warModuleLibDir } },
            new String[][] { null, null,
                {
                    rarModuleLibDir + jarSampleTwoLibrary,
                    rarModuleLibDir + jarSampleThreeLibrary,
                    rarModuleLibDir + ejbFourLibrary,
                    rarModuleLibDir + ejbFourClientLibrary,
                },
                {
                    warModuleLibDir + jarSampleOneLibrary,
                    rarModuleLibDir + jarSampleThreeLibrary,
                    rarModuleLibDir + jarSampleTwoLibrary,
                    warModuleLibDir + ejbThreeLibrary,
                    warModuleLibDir + ejbFourLibrary,
                    warModuleLibDir + ejbFourClientLibrary,
                    warModuleLibDir + rarLibrary,
                    warModuleLibDir + rarLibrary
                } } );
    }

    /**
     * Builds an EAR with deployment descriptor configuration for JakartaEE 9.
     */
    public void testProject099()
            throws Exception
    {
        doTestProject( "project-099", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Builds an EAR with deployment descriptor configuration for JakartaEE 10.
     */
    public void testProject100()
            throws Exception
    {
        doTestProject( "project-100", new String[] { "eartest-ejb-sample-one-1.0.jar" } );
    }

    /**
     * Ensure that {@code defaultLibBundleDir} with dot at begin don't remove artifacts during second execution.
     */
    public void testProject101() throws Exception
    {
        String[] expectedArtifacts = new String[] {
            "eartest-jar-sample-one-1.0.jar", "eartest-jar-sample-two-1.0.jar", "eartest-jar-sample-three-with-deps-1.0.jar" };

        boolean[] artifactsDirectory = new boolean[expectedArtifacts.length];

        doTestProject( "project-101", expectedArtifacts, true );
        doTestProject( "project-101", expectedArtifacts, false );
    }
}
