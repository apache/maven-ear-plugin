package org.apache.maven.plugins.ear.util;

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

import junit.framework.TestCase;

/**
 * @author Stephane Nicoll
 */
public class JavaEEVersionTest
    extends TestCase
{

    public void testGtSameVersion()
    {
        assertFalse( JavaEEVersion.FIVE.gt( JavaEEVersion.FIVE ) );
    }

    public void testGtNextVersion()
    {
        assertFalse( JavaEEVersion.FIVE.gt( JavaEEVersion.SIX ) );
    }

    public void testGtPreviousVersion()
    {
        assertTrue( JavaEEVersion.FIVE.gt( JavaEEVersion.ONE_DOT_FOUR ) );
    }

    public void testGeSameVersion()
    {
        assertTrue( JavaEEVersion.FIVE.ge( JavaEEVersion.FIVE ) );
    }

    public void testGePreviousVersion()
    {
        assertTrue( JavaEEVersion.FIVE.ge( JavaEEVersion.ONE_DOT_FOUR ) );
    }

    public void testGeNextVersion()
    {
        assertFalse( JavaEEVersion.FIVE.ge( JavaEEVersion.SIX ) );
    }

    public void testLtSameVersion()
    {
        assertFalse( JavaEEVersion.FIVE.lt( JavaEEVersion.FIVE ) );
    }

    public void testLtPreviousVersion()
    {
        assertFalse( JavaEEVersion.FIVE.lt( JavaEEVersion.ONE_DOT_FOUR ) );
    }

    public void testLtNextVersion()
    {
        assertTrue( JavaEEVersion.FIVE.lt( JavaEEVersion.SIX ) );
    }

    public void testLeSameVersion()
    {
        assertTrue( JavaEEVersion.FIVE.le( JavaEEVersion.FIVE ) );
    }

    public void testLePreviousVersion()
    {
        assertFalse( JavaEEVersion.FIVE.le( JavaEEVersion.ONE_DOT_FOUR ) );
    }

    public void testLeNextVersion()
    {
        assertTrue( JavaEEVersion.FIVE.le( JavaEEVersion.SIX ) );
    }

    public void testEqSameVersion()
    {
        assertTrue( JavaEEVersion.FIVE.eq( JavaEEVersion.FIVE ) );
    }

    public void testEqAnotherVersion()
    {
        assertFalse( JavaEEVersion.FIVE.eq( JavaEEVersion.ONE_DOT_THREE ) );
    }

    public void testGetVersion()
    {
        assertEquals( "5", JavaEEVersion.FIVE.getVersion() );
    }

    public void testGetJavaEEVersionValid() throws InvalidJavaEEVersion
    {
        assertEquals( JavaEEVersion.SIX, JavaEEVersion.getJavaEEVersion( "6" ) );
    }

    public void testGetJavaEEVersionInvalid()
    {
        try
        {
            JavaEEVersion.getJavaEEVersion( "2.4" );
            fail( "Should have failed to get an invalid version." );
        }
        catch ( InvalidJavaEEVersion expected )
        {
            // OK
        }
    }

    public void testGetJavaEEVersionNull() throws InvalidJavaEEVersion
    {
        try
        {
            JavaEEVersion.getJavaEEVersion( null );
            fail( "Should have failed to get a 'null' version." );
        }
        catch ( NullPointerException expected )
        {
            // OK
        }

    }

}
