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

File jarFile = new File( basedir, "target/test-1.0.ear" );
System.out.println( "Checking for existence of " + jarFile );
if ( !jarFile.isFile() )
{
    throw new IllegalStateException( "Missing file: " + jarFile );
}

JarFile jar = new JarFile( jarFile );

String[] includedEntries = {
    "META-INF/application.xml",
    "META-INF/appserver-application.xml",
};

// Reproducible Builds: check that every entry has the same timestamp  
long entryTime = -1;

for ( String included : includedEntries )
{
    System.out.println( "Checking for existence of " + included );
    JarEntry jarEntry = jar.getEntry( included );
    if ( jarEntry == null )
    {
        throw new IllegalStateException( "Missing archive entry: " + included );
    }
    if ( entryTime < 0 )
    {
        entryTime = jarEntry.getTime();
    }
    if ( entryTime != jarEntry.getTime() )
    {
        throw new IllegalStateException( "Invalid jar entry time: " + jarEntry.getTime()
            + " of archive entry: " + included
            + ", expected: " + entryTime );
    }
}

jar.close();

return true;
