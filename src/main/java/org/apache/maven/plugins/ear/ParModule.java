/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.plugins.ear;

import org.apache.maven.artifact.Artifact;

/**
 * The {@link EarModule} implementation for a Par module.
 *
 * @author <a href="snicoll@apache.org">Stephane Nicoll</a>
 */
public class ParModule extends EjbModule {
    /**
     * Default type of the artifact of a Par module.
     */
    public static final String DEFAULT_ARTIFACT_TYPE = "par";

    /**
     * Create an instance.
     */
    public ParModule() {
        this.type = DEFAULT_ARTIFACT_TYPE;
    }

    /**
     * @param a {@link Artifact}
     */
    public ParModule(Artifact a) {
        super(a);
    }
}
