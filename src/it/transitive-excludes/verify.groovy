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

import java.util.jar.JarEntry
import java.util.jar.JarFile

File earFile = new File( basedir, "target/mear73-1.0-SNAPSHOT.ear" );
if ( !earFile.isFile() )
{
    throw new IllegalStateException( "Missing file: " + earFile );
}

JarFile ear = new JarFile( earFile );
Enumeration entries = ear.entries();
while( entries.hasMoreElements() )
{
  JarEntry entry = (JarEntry) entries.nextElement();
  if( entry.getName().endsWith( ".jar" ) && !"org.apache.maven-maven-core-3.0.jar".equals( entry.getName() ) )
  {
    throw new IllegalStateException( "Unexpected archive entry: " + entry.getName() );
  }
}
ear.close();

return true;
