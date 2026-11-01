---
title: Using app client
author: 
  - Stephane Nicoll
  - snicoll@apache.org
date: 2011-04-03
---

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

# Using JavaEE application clients

JavaEE application clients are built by the [maven-acr-plugin](https://maven.apache.org/plugins/maven-acr-plugin/), which adds the 'app-client' packaging and the 'app-client' dependency type. Maven core defines neither, so every project that uses one has to load the acr plugin as an extension: the application client module for its packaging, and the EAR project for the dependency type. The sample below shows what the EAR project needs for an 'app-client-sample' application client. By default the ear plugin adds any application client to the generated application.xml just like it does for other JavaEE packaging types.

```xml
  <dependencies>
    <dependency>
      <groupId>com.foo</groupId>
      <artifactId>app-client-sample</artifactId>
      <version>1.0</version>
      <type>app-client</type>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-acr-plugin</artifactId>
        <version>3.2.0</version>
        <extensions>true</extensions>
      </plugin>
    </plugins>
  </build>
```
