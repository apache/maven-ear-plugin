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

import java.io.*;
import java.util.*;
import java.util.jar.*;

assertFileExists( File file )
{
    System.out.println( "Checking for existence of " + file );
    if ( !file.isFile() )
    {
        throw new IllegalStateException( "Missing file: " + file );
    }
}

assertIncludes( File file, String[] includedEntries )
{
    JarFile jar = new JarFile( file );
    try
    {
        for ( String included : includedEntries )
        {
            System.out.println( "Checking for included archive entry " + included );
            if ( jar.getEntry( included ) == null )
            {
                throw new IllegalStateException( "Missing archive entry: " + included );
            }
        }
    }
    finally
    {
        jar.close();
    }
}

assertExcludes( File file, String[] excludedEntries )
{
    JarFile jar = new JarFile( file );
    try
    {
        for ( String excluded : excludedEntries )
        {
            System.out.println( "Checking for excluded artifact " + excluded );
            if ( jar.getEntry( excluded ) != null )
            {
                throw new IllegalStateException( "Archive entry should be excluded: " + excluded );
            }
        }
    }
    finally
    {
        jar.close();
    }
}

assertManifestClassPath( File file, String classPath )
{
    JarFile jar = new JarFile( file );
    try
    {
        Manifest manifest = jar.getManifest();
        String manifestClassPath = manifest.getMainAttributes().getValue( "Class-Path" );
        System.out.println( "manifestClassPath: " + manifestClassPath );
        if ( !( classPath == null && manifestClassPath == null
            || manifestClassPath != null && manifestClassPath.equals( classPath ) ) )
        {
            throw new IllegalStateException( "Missing entry in war MANIFEST.MF: " + classPath );
        }
    }
    finally
    {
        jar.close();
    }
}

File warOneFile = new File( basedir, "war-module-one/target/war-module-one-1.0.war" );
assertFileExists( warOneFile );
assertIncludes( warOneFile, new String[] { "WEB-INF/web.xml",
                                           "META-INF/MANIFEST.MF",
                                           "WEB-INF/lib/commons-lang-2.6.jar",
                                           "WEB-INF/lib/jar-sample-one-1.0-20150825.210557-91.jar" } );
assertManifestClassPath( warOneFile, "commons-lang-2.6.jar jar-sample-one-1.0-20150825.210557-91.jar" );

File warTwoFile = new File( basedir, "war-module-two/target/war-module-two-1.0.war" );
assertFileExists( warTwoFile );
assertIncludes( warTwoFile, new String[] { "WEB-INF/web.xml",
                                           "META-INF/MANIFEST.MF",
                                           "WEB-INF/lib/jar-sample-one-1.0-SNAPSHOT.jar" } );
assertExcludes( warTwoFile, new String[] { "WEB-INF/lib/commons-lang-2.6.jar" } );
assertManifestClassPath( warTwoFile, "jar-sample-one-1.0-SNAPSHOT.jar" );

File warModuleOneFile = new File( basedir, "ear-module/target/ear-module-1.0/org.apache.maven.its.ear.skinnywars-war-module-one-1.0.war" );
assertFileExists( warModuleOneFile );
assertIncludes( warModuleOneFile, new String[] { "WEB-INF/web.xml",
                                                 "META-INF/MANIFEST.MF" } );
assertExcludes( warModuleOneFile, new String[] { "WEB-INF/lib/commons-lang-2.6.jar",
                                                 "WEB-INF/lib/jar-sample-one-1.0-SNAPSHOT.jar",
                                                 "WEB-INF/lib/jar-sample-one-1.0-20150825.210557-91.jar" } );
assertManifestClassPath( warModuleOneFile, "commons-lang-commons-lang-2.6.jar eartest-jar-sample-one-1.0-20150825.210557-91.jar" );

File warModuleTwoFile = new File( basedir, "ear-module/target/ear-module-1.0/org.apache.maven.its.ear.skinnywars-war-module-two-1.0.war" );
assertFileExists( warModuleTwoFile );
assertIncludes( warModuleTwoFile, new String[] { "WEB-INF/web.xml",
                                                 "META-INF/MANIFEST.MF" } );
assertExcludes( warModuleTwoFile, new String[] { "WEB-INF/lib/commons-lang-2.6.jar",
                                                 "WEB-INF/lib/jar-sample-one-1.0-SNAPSHOT.jar",
                                                 "WEB-INF/lib/jar-sample-one-1.0-20150825.210557-91.jar" } );
assertManifestClassPath( warModuleTwoFile, "eartest-jar-sample-one-1.0-20150825.210557-91.jar commons-lang-commons-lang-2.6.jar" );

return true;
