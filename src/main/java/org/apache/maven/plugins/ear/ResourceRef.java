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

import org.codehaus.plexus.util.xml.XMLWriter;

/**
 * Representation of {@code resource-ref} element in {@code application.xml} file.
 * 
 * <pre>
 * &lt;resource-ref&gt;
 *   &lt;res-ref-name&gt;jdbc/myDs&lt;/res-ref-name&gt;
 *   &lt;res-type&gt;javax.sql.DataSource&lt;/res-type&gt;
 *   &lt;res-auth&gt;Container&lt;/res-auth&gt;
 * &lt;/resource-ref&gt;
 * </pre>
 * or
 * <pre>
 * &lt;resource-ref&gt;
 *   &lt;res-ref-name&gt;jdbc/myDs&lt;/res-ref-name&gt;
 *   &lt;res-type&gt;javax.sql.DataSource&lt;/res-type&gt;
 *   &lt;lookup-name&gt;jdbc/lookup/myDs&lt;/lookup-name&gt;
 * &lt;/resource-ref&gt;
 * </pre>
 * 
 * @author Karl Heinz Marbaise
 * @since 3.0.0
 */
public class ResourceRef
{
    static final String RESOURCE_REF = "resource-ref";

    static final String RESOURCE_REF_NAME = "res-ref-name";

    static final String RESOURCE_TYPE = "res-type";

    static final String RESOURCE_AUTH = "res-auth";

   static final String LOOKUP_NAME = "lookup-name";

    private String name;

    private String type;

    private String auth;

   private String lookupName;

    /**
     * @param name The res-ref-name.
     * @param type The res-type
     * @param auth The res-auth.
     * @param lookupName The lookup-name.
     */
   public ResourceRef( String name, String type, String auth, String lookupName )
    {
        if ( name == null || name.isEmpty() )
        {
            throw new IllegalArgumentException( RESOURCE_REF_NAME + " in " + RESOURCE_REF_NAME
                + " element cannot be null." );
        }
        else if ( ( type == null || type.isEmpty() ) && ( auth == null || auth.isEmpty() ) )
        {
            throw new IllegalArgumentException( RESOURCE_TYPE + " in " + RESOURCE_REF_NAME
                + " element cannot be null " );
        }

        this.name = name;
        this.type = type;
        this.auth = auth;
        this.lookupName = lookupName;

    }

    /**
     * Appends the {@code XML} representation of this env-entry.
     * 
     * @param writer the writer to use
     */
    public void appendResourceRefEntry( XMLWriter writer )
    {
        writer.startElement( RESOURCE_REF );

        // res-name
        doWriteElement( writer, RESOURCE_REF_NAME, getName() );

        // res_ref-type
        if ( getType() != null )
        {
            doWriteElement( writer, RESOURCE_TYPE, getType() );
        }

        // ref-auth
        if ( getAuth() != null )
        {
            doWriteElement( writer, RESOURCE_AUTH, getAuth() );
        }

        // lookup-name
        if ( getLookupName() != null ) 
        {
            doWriteElement( writer, LOOKUP_NAME, getLookupName() );
        }

        // end of ejb-ref
        writer.endElement();
    }

    private void doWriteElement( XMLWriter writer, String element, String text )
    {
        writer.startElement( element );
        writer.writeText( text );
        writer.endElement();
    }

    /**
     * @return {@link #name}
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name {@link #name}
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * @return {@link #type}
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type {@link #type}
     */
    public void setType( String type )
    {
        this.type = type;
    }

    /**
     * @return {@link #auth}
     */
    public String getAuth()
    {
        return auth;
    }

    /**
     * @param auth {@link #auth}
     */
    public void setAuth( String auth )
    {
        this.auth = auth;
    }
    
    /**
     * @return {@link #lookupName}
     */
    public String getLookupName() 
    {
        return lookupName;
    }

   /**
    * @param lookupName {@link #lookupName}
    */
    public void setLookupName( String lookupName ) 
    {
        this.lookupName = lookupName;
    }

}
