<!--
Copyright 2006 The Apache Software Foundation.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Apache Maven EAR Plugin

This plugin generates Java EE Enterprise Archive (EAR) file. It can also generate the deployment descriptor file (e.g. `application.xml`).

The EAR plugin supports the following artifacts:

- ejb
- war
- jar
- ejb-client
- rar
- ejb3
- par
- sar
- wsr
- har
- app-client

For available configuration options for these artifacts, please see the [modules configuration](./modules.html).

For more information please visit [Jakarta EE](https://jakarta.ee/) [Java EE at a glance](https://www.oracle.com/java/technologies/java-ee-glance.html).

## Version 3.4.0

- Require _Maven 3.6.3_ at least.
- Support _Jakarta EE 11_.
## Version 3.3.0

- Require _Java 8_ and _Maven 3.2.5_ at least.
- Support _Jakarta EE 9_ and _10_.
- Use _ZipFileSystem_ during _EAR_ packaging - whole modules re-packaging is not needed.
## Version 3.2.0

[skinnyModules](./ear-mojo.html#skinnyModules) parameter, `libDirectory` property of EAR modules, `type` property of EAR modules and `classPathItem` property of EAR modules have been implemented.

## Version 3.0.0

Starting with version 3.0.0 the usage of **fileNameMapping** has been removed. If you need to use a kind of file name mapping take a look at the new [outputFileNameMapping](./examples/customize-file-name-mapping.html) which is more flexible.

## Goals Overview

EAR Plugin has two goals:

- [ear:ear](./ear-mojo.html) generates J2EE Enterprise Archive (EAR) files.
- [ear:generate-application-xml](./generate-application-xml-mojo.html) generates the deployment descriptor file(s).
## Usage

General instructions on how to use the EAR Plugin can be found on the [usage page](./usage.html). Some more specific use cases are described in the examples given below. Further real-life examples are given in the plugin's [test suite](./tests.html).

In case you still have questions regarding the plugin's usage, please have a look at the [FAQ](./faq.html) and feel free to contact the [user mailing list](./mailing-lists.html). The posts to the mailing list are archived and could already contain the answer to your question as part of an older thread. Hence, it is also worth browsing/searching the [mail archive](./mailing-lists.html).

If you feel like the plugin is missing a feature or has a defect, you can fill a feature request or bug report in our [issue tracker](./issue-management.html). When creating a new issue, please provide a comprehensive description of your concern. Especially for fixing bugs it is crucial that the developers can reproduce your problem. For this reason, entire debug logs, POMs or most preferably little demo projects attached to the issue are very much appreciated. Of course, patches are welcome, too. Contributors can check out the project from our [source repository](./scm.html) and will find supplementary information in the [guide to helping with Maven](http://maven.apache.org/guides/development/guide-helping.html).

## Examples

To provide you with better understanding on some usages of the EAR Plugin, you can take a look into the following examples:

- [Filtering EAR Resources](./examples/filtering-sources.html)
- [Advanced Filtering Techniques](./examples/filtering-advanced.html)
- [Creating Skinny WARs](./examples/skinny-wars.html)
- [Creating Skinny Modules](./examples/skinny-modules.html)
- [Customizing A Module Filename](./examples/customizing-a-module-filename.html)
- [Customizing The Context Root](./examples/customizing-context-root.html)
- [Customizing A Module Location](./examples/customizing-module-location.html)
- [Customizing A Module URI](./examples/customizing-module-uri.html)
- [Excluding A Module](./examples/excluding-a-module.html)
- [Excluding Files From the EAR](./examples/excluding-files-from-ear.html)
- [Unpacking A Module](./examples/unpacking-a-module.html)
- [Including A Third Party Library In application.xml](./examples/including-a-third-party-library-in-application-xml.html)
- [Specifying Security Roles For The Generated application.xml](./examples/specifying-security-roles-for-the-generated-application-xml.html)
- [Specifying Environment Entries For The Generated application.xml](./examples/specifying-env-entries-for-the-generated-application-xml.html)
- [Specifying Resource Ref Entries For The Generated application.xml](./examples/specifying-resource-ref-entries-for-the-generated-application-xml.html)
- [Generating the jboss-app.xml file](./examples/generating-jboss-app.html)
- [Generating modules id](./examples/generating-modules-id.html)
- [Using JavaEE application clients](./examples/using-app-client.html)
- [Eclipse and Maven integration (without m2e)](./examples/eclipse-and-maven-integration.html)
