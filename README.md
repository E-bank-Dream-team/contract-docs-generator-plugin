# Contract documentation generator plugin

Maven plugin for generating documentation from existing contracts written in Groovy.

## How to

### Testing plugin
First step is to install plugin in our local maven repository:

```bash
mvn clean install
```

### Executing plugin

After installing plugin in local repository we can run a goal of this plugin using following command:

```bash
mvn com.example:contract-docs-generator-maven-plugin:0.0.1-SNAPSHOT:contract-docs-generator
```

Since this plugin follows maven plugin naming convension it is possible to run a goal using following, shortened command:

```bash
mvn contract-docs-generator:contract-docs-generator
```

but to make above command working, we have to add _groupId_ of this plugin to _pluginGroups_ in _settings.xml_ file:

```xml
<pluginGroups>
    <pluginGroup>com.example</pluginGroup>
</pluginGroups>
```

### Using plugin in a project

To include this plugin in a project we have to add it to the build

```xml
<build>
  <plugin>
    <groupId>com.example</groupId>
    <artifactId>contract-docs-generator-maven-plugin</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <executions>
        <execution>
            <goals>
                <goal>contract-docs-generator</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <outputDirPath>target/generated-snippets</outputDirPath>
        <outputFileName>contracts.adoc</outputFileName>
    </configuration>
  </plugin>
<build>
```

#### Plugin properties
`outputDirPath` - path to where generated documentation file will be stored. Default value: `target/generated-snippets`

`outputFileName` - name of file with documentation. Default value: `contracts.adoc`
