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

import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.ear.util.ArtifactRepository;
import org.apache.maven.shared.mapping.MappingUtils;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.util.xml.XMLWriter;

/**
 * A base implementation of an {@link EarModule}.
 * 
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
public abstract class AbstractEarModule
    implements EarModule
{

    /**
     * The module element.
     */
    protected static final String MODULE_ELEMENT = "module";

    /**
     * The java module.
     */
    protected static final String JAVA_MODULE = "java";

    /**
     * The alt-dd module.
     */
    protected static final String ALT_DD = "alt-dd";

    private Artifact artifact;

    // Those are set by the configuration

    private String groupId;

    private String artifactId;

    /**
     * The type of the artifact
     */
    protected String type;

    private String classifier;

    /**
     * The bundleDir.
     */
    protected String bundleDir;

    /**
     * The bundleFileName.
     */
    protected String bundleFileName;

    /**
     * excluded by default {@code false}.
     */
    protected Boolean excluded = Boolean.FALSE;

    private String uri;

    /**
     * unpack
     */
    protected Boolean unpack = null;

    /**
     * The alternate deployment descriptor.
     */
    protected String altDeploymentDescriptor;

    private String moduleId;

    /**
     * Directory of module which contains libraries packaged into module. {@code null} value means that module
     * doesn't contain any library. Each module type can provide default value for this directory and this option
     * can be used to override that default value. If module libraries are located at the root of module then use
     * single slash (/) to configure that in POM. That is, a single slash is treated as an empty string.
     */
    protected String libDirectory;

    /**
     * If module is considered for inclusion into the Class-Path entry of MANIFEST.mf of other modules. {@code false}
     * value leads to removal of the module from the Class-Path entry. {@code true} value leads to modification of the
     * reference to the module in the Class-Path entry if such reference exists or leads to adding of the module into
     * the Class-Path entry if such reference doesn't exist. Removal, modification or adding of the reference in the
     * Class-Path entry depends on libDirectory property of another module and on skinnyWars / skinnyModules parameters
     * of EAR Plugin.
     */
    protected boolean classPathItem;

    // This is injected once the module has been built.

    /**
     * The {@link EarExecutionContext}
     */
    protected EarExecutionContext earExecutionContext;

    /**
     * Empty constructor to be used when the module is built based on the configuration.
     */
    public AbstractEarModule()
    {
    }

    /**
     * Creates an ear module from the artifact.
     * 
     * @param a the artifact
     */
    public AbstractEarModule( Artifact a )
    {
        this.artifact = a;
        this.groupId = a.getGroupId();
        this.artifactId = a.getArtifactId();
        this.type = a.getType();
        this.classifier = a.getClassifier();
        this.bundleDir = null;
    }

    /**
     * {@inheritDoc}
     */
    public void setEarExecutionContext( EarExecutionContext earExecutionContext )
    {
        this.earExecutionContext = earExecutionContext;
    }

    /** {@inheritDoc} */
    public void resolveArtifact( Set<Artifact> artifacts )
        throws EarPluginException, MojoFailureException
    {
        // If the artifact is already set no need to resolve it
        if ( artifact == null )
        {
            // Make sure that at least the groupId and the artifactId are specified
            if ( groupId == null || artifactId == null )
            {
                throw new MojoFailureException( "Could not resolve artifact[" + groupId + ":" + artifactId + ":"
                    + getType() + "]" );
            }
            final ArtifactRepository ar = earExecutionContext.getArtifactRepository();
            artifact = ar.getUniqueArtifact( groupId, artifactId, getType(), classifier );
            // Artifact has not been found
            if ( artifact == null )
            {
                Set<Artifact> candidates = ar.getArtifacts( groupId, artifactId, getType() );
                if ( candidates.size() > 1 )
                {
                    throw new MojoFailureException( "Artifact[" + this + "] has " + candidates.size()
                        + " candidates, please provide a classifier." );
                }
                else
                {
                    throw new MojoFailureException( "Artifact[" + this + "] is not a dependency of the project." );
                }
            }
        }
    }

    /**
     * @return {@link #artifact}
     */
    public Artifact getArtifact()
    {
        return artifact;
    }

    /**
     * @return {@link #moduleId}
     */
    public String getModuleId()
    {
        return moduleId;
    }

    /**
     * @return Return the URI.
     */
    public String getUri()
    {
        if ( uri == null )
        {
            if ( getBundleDir() == null )
            {
                uri = getBundleFileName();
            }
            else
            {
                uri = getBundleDir() + getBundleFileName();
            }
        }
        return uri;
    }

    /**
     * {@inheritDoc}
     */
    public String getType()
    {
        return type;
    }

    /**
     * Returns the artifact's groupId.
     * 
     * @return {@link #groupId}
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * Returns the artifact's Id.
     * 
     * @return {@link #artifactId}
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * Returns the artifact's classifier.
     * 
     * @return the artifact classifier
     */
    public String getClassifier()
    {
        return classifier;
    }

    /**
     * Returns the bundle directory. If null, the module is bundled in the root of the EAR.
     * 
     * @return the custom bundle directory
     */
    public String getBundleDir()
    {
        bundleDir = cleanArchivePath( bundleDir );
        return bundleDir;
    }

    /**
     * {@inheritDoc}
     */
    public String getLibDir()
    {
        libDirectory = cleanArchivePath( libDirectory );
        return libDirectory;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isClassPathItem()
    {
        return classPathItem;
    }

    /**
     * {@inheritDoc}
     */
    public String getBundleFileName()
    {
        if ( bundleFileName == null )
        {
            try
            {
                String outputFileNameMapping = earExecutionContext.getOutputFileNameMapping();
                bundleFileName = MappingUtils.evaluateFileNameMapping( outputFileNameMapping, artifact );
            }
            catch ( InterpolationException e )
            {
                // We currently ignore this here, cause assumption is that
                // has already been happened before..
                // FIXME: Should be checked first.
            }

            // bundleFileName = earExecutionContext.getFileNameMapping().mapFileName( artifact );
        }
        return bundleFileName;
    }

    /**
     * The alt-dd element specifies an optional URI to the post-assembly version of the deployment descriptor file for a
     * particular Java EE module. The URI must specify the full pathname of the deployment descriptor file relative to
     * the application's root directory.
     * 
     * @return the alternative deployment descriptor for this module
     */
    public String getAltDeploymentDescriptor()
    {
        return altDeploymentDescriptor;
    }

    /**
     * Specify whether this module should be excluded or not.
     * 
     * @return true if this module should be skipped, false otherwise
     */
    public boolean isExcluded()
    {
        return excluded;
    }

    /**
     * @return {@link #unpack}
     */
    public Boolean shouldUnpack()
    {
        return unpack;
    }

    /**
     * Writes the alternative deployment descriptor if necessary.
     * 
     * @param writer the writer to use
     * @param version the java EE version in use
     */
    protected void writeAltDeploymentDescriptor( XMLWriter writer, String version )
    {
        if ( getAltDeploymentDescriptor() != null )
        {
            writer.startElement( ALT_DD );
            writer.writeText( getAltDeploymentDescriptor() );
            writer.endElement();
        }
    }

    /**
     * Starts a new {@link #MODULE_ELEMENT} on the specified writer, possibly including an id attribute.
     * 
     * @param writer the XML writer.
     * @param generateId whether an id should be generated
     */
    protected void startModuleElement( XMLWriter writer, Boolean generateId )
    {
        writer.startElement( MODULE_ELEMENT );

        // If a moduleId is specified, always include it
        if ( getModuleId() != null )
        {
            writer.addAttribute( "id", getModuleId() );
        }
        else if ( generateId )
        {
            // No module id was specified but one should be generated.
            // FIXME: Should we use the mapping using outputFileNameMapping instead
            // of doing this on our own?
            Artifact theArtifact = getArtifact();
            String generatedId = theArtifact.getType().toUpperCase() + "_" + theArtifact.getGroupId() + "."
                + theArtifact.getArtifactId();
            if ( null != theArtifact.getClassifier() && theArtifact.getClassifier().trim().length() > 0 )
            {
                generatedId += "-" + theArtifact.getClassifier().trim();
            }
            writer.addAttribute( "id", generatedId );
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( getType() ).append( ":" ).append( groupId ).append( ":" ).append( artifactId );
        if ( classifier != null )
        {
            sb.append( ":" ).append( classifier );
        }
        if ( artifact != null )
        {
            sb.append( ":" ).append( artifact.getVersion() );
        }
        return sb.toString();
    }

    /**
     * Cleans the path pointing to the resource inside the archive so that it might be used properly.
     * 
     * @param path the path to clean, can be {@code null}
     * @return the cleaned path or {@code null} if given {@code path} is {@code null}
     */
    static String cleanArchivePath( String path )
    {
        if ( path == null )
        {
            return null;
        }

        // Using slashes
        path = path.replace( '\\', '/' );

        // Remove '/' prefix if any so that path is a relative path
        if ( path.startsWith( "/" ) )
        {
            path = path.substring( 1, path.length() );
        }

        if ( path.length() > 0 && !path.endsWith( "/" ) )
        {
            // Adding '/' suffix to specify a path structure if it is not empty
            path = path + "/";
        }

        return path;
    }

    /**
     * Sets the URI of the module explicitly for testing purposes.
     * 
     * @param uri the uri
     */
    void setUri( String uri )
    {
        this.uri = uri;

    }

    /**
     * @return always {@code true}
     */
    public boolean changeManifestClasspath()
    {
        return true;
    }
}
