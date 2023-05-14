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
 * The representation of a env-entry entry within an application.xml file.
 * 
 * @author Jim Brownfield based on code by <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
class EnvEntry
{

    static final String ENV_ENTRY = "env-entry";

    static final String DESCRIPTION = "description";

    static final String ENV_ENTRY_NAME = "env-entry-name";

    static final String ENV_ENTRY_TYPE = "env-entry-type";

    static final String ENV_ENTRY_VALUE = "env-entry-value";

    static final String ENV_LOOKUP_NAME = "lookup-name";

    private final String description;

    private final String name;

    private final String type;

    private final String value;

    private final String lookupName;

    EnvEntry( String description, String name, String type, String value, String lookupName )
    {
        if ( name == null || name.isEmpty() )
        {
            throw new IllegalArgumentException( ENV_ENTRY_NAME + " in " + ENV_ENTRY + " element cannot be null." );
        }
        else if ( ( type == null || type.isEmpty() ) && ( value == null || value.isEmpty() ) )

        {
            throw new IllegalArgumentException( ENV_ENTRY_TYPE + " in " + ENV_ENTRY + " element cannot be null if no "
                + ENV_ENTRY_VALUE + " was specified." );

        }

        this.description = description;
        this.name = name;
        this.type = type;
        this.value = value;
        this.lookupName = lookupName;
    }

    public String getDescription()
    {
        return description;
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public String getValue()
    {
        return value;
    }

    public String getLookupName()
    {
        return lookupName;
    }

    /**
     * Appends the {@code XML} representation of this env-entry.
     * 
     * @param writer the writer to use
     */
    public void appendEnvEntry( XMLWriter writer )
    {
        System.out.println( "appendEnvEntry()" );
        writer.startElement( ENV_ENTRY );

        // description
        if ( getDescription() != null )
        {
            doWriteElement( writer, DESCRIPTION, getDescription() );
        }

        // env entry name
        doWriteElement( writer, ENV_ENTRY_NAME, getName() );

        // env entry type
        if ( getType() != null )
        {
            doWriteElement( writer, ENV_ENTRY_TYPE, getType() );
        }

        // env entry value
        if ( getValue() != null )
        {
            doWriteElement( writer, ENV_ENTRY_VALUE, getValue() );
        }

        // lookup-name
        if ( getLookupName() != null )
        {
            doWriteElement( writer, ENV_LOOKUP_NAME, getLookupName() );
        }

        // end of env-entry
        writer.endElement();
    }

    private void doWriteElement( XMLWriter writer, String element, String text )
    {
        writer.startElement( element );
        writer.writeText( text );
        writer.endElement();
    }

    public String toString()
    {
        return "env-entry [name=" + getName() + ", type=" + getType() + ", value=" + getValue() + ", lookup-name="
            + getLookupName() + "]";
    }

}
