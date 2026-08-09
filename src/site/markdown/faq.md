---
title: Frequently Asked Questions
---

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<a id="top"></a>

# Frequently Asked Questions

1. [What is an EAR archive?](#what-is-ear)
2. [The EAR Plugin throws an exception when encountering artifact types it is unfamiliar with. Is this a bug?](#har-files)
3. [How can I avoid to generate a display-name entry in the generated application.xml?](#avoid-display-name)
4. [When should one use the modules configuration?](#when-should-one-use-the-modules)

<a id="what-is-ear"></a>

### What is an EAR archive?

An EAR archive is used to deploy standalone EJBs, usually separated from the web application. Thus, there is no
need for a web application to access these EJBs. The EJBs are still accessible though using EJB clients.

<a id="har-files"></a>

### The EAR Plugin throws an exception when encountering artifact types it is unfamiliar with. Is this a bug?

The exception can be prevented by adding your custom artifact type to the artifactTypeMappings configuration.
There is a mini-guide on how to do that in the [modules configuration](modules.html#Custom_Artifact_Types)
section.

<a id="avoid-display-name"></a>

### How can I avoid to generate a display-name entry in the generated application.xml?

By default, the plugin will always generate a display-name with the id of the project if a custom one is not
provided through configuration. If for some reason you don't want any display-name at all, just use the
${null} value instead.

<a id="when-should-one-use-the-modules"></a>

### When should one use the modules configuration?

By default, the EAR plugin generates sensible defaults for all the JavaEE dependencies it finds in the current
project. The *modules* configuration is useful if you need to customize something such as the bundle location,
context root, etc.

Adding a module entry with only the groupId and artifactId of a dependency is therefore useless.
