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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.xml.XMLWriter;

import java.util.Set;

/**
 * The ear module interface.
 * 
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
public interface EarModule
{

    /**
     * Returns the {@link Artifact} representing this module.
     * 
     * Note that this might return {@code null} till the module has been resolved.
     * 
     * @return the artifact
     * @see #resolveArtifact(java.util.Set)
     */
    Artifact getArtifact();

    /**
     * Returns the {@code URI} for this module.
     * 
     * @return the {@code URI}
     */
    String getUri();

    /**
     * Returns the type associated to the module.
     * 
     * @return the artifact's type of the module
     */
    String getType();

    /**
     * Specify whether this module should be excluded or not.
     * 
     * @return true if this module should be skipped, false otherwise
     */
    boolean isExcluded();

    /**
     * Specify whether this module should be unpacked in the EAR archive or not.
     *
     * Returns null if no configuration was specified so that defaulting may apply.
     * 
     * @return true if this module should be bundled unpacked, false otherwise
     */
    Boolean shouldUnpack();

    /**
     * The alt-dd element specifies an optional URI to the post-assembly version of the deployment descriptor file for a
     * particular Java EE module. The URI must specify the full pathname of the deployment descriptor file relative to
     * the application's root directory.
     * 
     * @return the alternative deployment descriptor for this module
     * @since JavaEE 5
     */
    String getAltDeploymentDescriptor();

    /**
     * Appends the {@code XML} representation of this module.
     * 
     * @param writer the writer to use
     * @param version the version of the {@code application.xml} file
     * @param generateId whether an id should be generated
     */
    void appendModule( XMLWriter writer, String version, Boolean generateId );

    /**
     * Resolves the {@link Artifact} represented by the module. Note that the {@link EarExecutionContext} might be used
     * to customize further the resolution.
     * 
     * @param artifacts the project's artifacts
     * @throws EarPluginException if the artifact could not be resolved
     * @throws MojoFailureException if an unexpected error occurred
     */
    void resolveArtifact( Set<Artifact> artifacts )
        throws EarPluginException, MojoFailureException;

    /**
     * @param earExecutionContext The execution context.
     */
    void setEarExecutionContext( EarExecutionContext earExecutionContext );

    /**
     * @return the state if manifest classpath will be changed or not.
     */
    boolean changeManifestClasspath();

    /**
     * @return The directory of the module which contains the JAR libraries packaged within the module.
     * Can be {@code null}, which means that module doesn't contain any packaged libraries.
     */
    String getLibDir();

    /**
     * Returns the bundle file name. If {@code null}, the artifact's file name is returned.
     *
     * @return the bundle file name
     */
    String getBundleFileName();

    /**
     * If module should be included into the Class-Path entry of MANIFEST.mf. Doesn't impact Class-Path entry of
     * MANIFEST.mf of modules which contain all of their dependencies unless skinnyWars / skinnyModules is turned on.
     *
     * @return {@code }True} if module should be included into the Class-Path entry of MANIFEST.mf
     */
    boolean isClassPathItem();

}
