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

String[] includedEntries = [
    "META-INF/application.xml",
    "META-INF/appserver-application.xml",
    "META-INF/jboss-app.xml"
];
for ( String included : includedEntries )
{
    System.out.println( "Checking for existence of " + included );
    if ( jar.getEntry( included ) == null )
    {
        throw new IllegalStateException( "Missing archive entry: " + included );
    }
}

InputStream descriptor = jar.getInputStream( jar.getEntry( "META-INF/jboss-app.xml" ) );
String jbossAppXml = descriptor.getText( "UTF-8" );
descriptor.close();
jar.close();
if ( !jbossAppXml.contains( "<unauthenticated-principal>guest</unauthenticated-principal>" ) )
{
    throw new IllegalStateException( "Missing configured unauthenticated principal from jboss-app.xml" );
}
if ( !jbossAppXml.contains( "http://www.jboss.org/j2ee/dtd/jboss-app_4_0.dtd" ) )
{
    throw new IllegalStateException( "Missing default JBoss 4 descriptor from jboss-app.xml" );
}
if ( !jbossAppXml.contains( '<loader-repository loaderRepositoryClass="com.foo.LoaderRepository">com.foo:loader=foo-application-1.0.ear' ) )
{
    throw new IllegalStateException( "Missing configured loader repository and class from jboss-app.xml" );
}
if ( !jbossAppXml.contains( '<loader-repository-config configParserClass="com.foo.LoaderRepositoryConfigParser">java2ParentDelegation=true</loader-repository-config>' ) )
{
    throw new IllegalStateException( "Missing configured loader repository parser and config from jboss-app.xml" );
}

return true;
