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
import java.util.regex.*;

assertJar( String fileName, String[] includedEntries, String[] excludedEntries, boolean assertManifest,
    String[] expectedClassPathElements )
{
    File jarFile = new File( basedir, fileName );
    System.out.println( "Checking for existence of " + jarFile );
    if ( !jarFile.isFile() )
    {
        throw new IllegalStateException( "Missing file: " + jarFile );
    }

    JarFile jar = new JarFile( jarFile );

    if ( includedEntries != null )
    {
        for ( String included : includedEntries )
        {
            System.out.println( "Checking for included archive entry " + included );
            if ( jar.getEntry( included ) == null )
            {
                throw new IllegalStateException( "Missing archive entry: " + included + ". Artifact: " + fileName );
            }
        }
    }

    if ( excludedEntries != null )
    {
        for ( String excluded : excludedEntries )
        {
            System.out.println( "Checking for excluded artifact " + excluded );
            if ( jar.getEntry( excluded ) != null )
            {
                throw new IllegalStateException( "Archive entry should be excluded: " + excluded
                    + ". Artifact: " + fileName );
            }
        }
    }

    if ( assertManifest )
    {
        Manifest manifest = jar.getManifest();
        String manifestClassPath = manifest.getMainAttributes().getValue("Class-Path");
        if ( expectedClassPathElements == null)
        {
            if ( manifestClassPath != null )
            {
                throw new IllegalStateException( "Superfluous Class-Path entry in MANIFEST.MF of artifact: "
                    + fileName );
            }
        }
        else
        {
            if ( manifestClassPath == null )
            {
                throw new IllegalStateException( "Missing Class-Path entry in MANIFEST.MF of artifact: "
                    + fileName );
            }
            manifestClassPath = manifestClassPath.trim();
            String[] actualClassPathElements = manifestClassPath.length() == 0 ?
                new String[0] : manifestClassPath.split( " " );
            if ( !Arrays.equals( expectedClassPathElements, actualClassPathElements ) )
            {
                throw new IllegalStateException( "Invalid Class-Path entry in MANIFEST.MF of artifact: "
                    + fileName
                    + ". Expected: " + Arrays.toString( expectedClassPathElements )
                    + ". Actual: " + Arrays.toString( actualClassPathElements ) );
            }
        }
    }
}

String[] includedEntries = {
    "WEB-INF/web.xml",
    "META-INF/MANIFEST.MF",
    "WEB-INF/lib/commons-lang-2.6.jar"
};

assertJar( "war-module-one/target/war-module-one-1.0.war", includedEntries, null, false, null );

String[] expectedClassPathElements = {
    "commons-lang-2.6.jar"
};

assertJar( "war-module-two/target/war-module-two-1.0.war", includedEntries, null, true, expectedClassPathElements );

String[] includedEntries = {
    "WEB-INF/web.xml",
    "META-INF/MANIFEST.MF"
};

String[] excludedEntries = {
    "WEB-INF/lib/commons-lang-2.6.jar"
};

String[] expectedClassPathElements = {};

assertJar( "ear-module/target/ear-module-1.0/org.apache.maven.its.ear.skinnywars-war-module-one-1.0.war",
    includedEntries, excludedEntries, true, expectedClassPathElements );

assertJar( "ear-module/target/ear-module-1.0/org.apache.maven.its.ear.skinnywars-war-module-two-1.0.war",
    includedEntries, excludedEntries, true, expectedClassPathElements );

String[] excludedEntries = {
    "commons-lang-2.6.jar",
    "lib/commons-lang-2.6.jar",
};

assertJar( "ear-module/target/ear-module-1.0.ear", null, excludedEntries, false, null );

return true;
