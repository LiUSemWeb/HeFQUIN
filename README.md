# HeFQUIN
HeFQUIN is a **query federation engine for heterogeneous federations of graph data sources (e.g, federated knowledge graphs)** that is currently under development by [the Semantic Web research group at Linköping University](https://www.ida.liu.se/research/semanticweb/).

For detailed information about HeFQUIN, refer to the **Website at [https://liusemweb.github.io/](https://liusemweb.github.io/)**, where you can find
* a list of the [features of HeFQUIN](https://liusemweb.github.io/HeFQUIN/doc/features.html),
* a detailed [user documentation](https://liusemweb.github.io/HeFQUIN/doc/index.html),
* a list of [related research publications](https://liusemweb.github.io/HeFQUIN/research),
* information [for contributors](https://liusemweb.github.io/HeFQUIN/devdoc),
and more.

## Quick Guide
### Using HeFQUIN as a Service
* **_Setup via Docker_**
  * Pull the image for the latest release by executing the following command:
    ```bash
    docker pull ghcr.io/LiUSemWeb/hefquin:latest
    ```
  * Thereafter, execute the following command to start the HeFQUIN service using Docker:
    ```bash
    docker run \
        -p 8080:8080 \
        -v MyFedConf.ttl:/usr/local/tomcat/webapps/ROOT/DefaultFedConf.ttl \
        hefquin:latest
    ```
    where
    * the `-p` argument specifies the port at which the service shall listen and
    * the `-v` argument refers to a file (e.g., `MyFedConf.ttl`) that contains an [RDF-based description of your federation](https://liusemweb.github.io/HeFQUIN/doc/federation_description.html).
  * Next, continue at the point "_Interacting with the HeFQUIN Service_" below.
  * Our [documentation page about running HeFQUIN via Docker](https://liusemweb.github.io/HeFQUIN/doc/docker.html) provides more details, including a description of how to build your own Docker image of HeFQUIN.

* **_Setup via the Embedded Servlet Container_**
  * Download the ZIP package of the latest [release of HeFQUIN](https://github.com/LiUSemWeb/HeFQUIN/releases), unpack it, enter the resulting directory in a command-line terminal and, then, execute the following command (which assumes that you have a relatively recent version of Java installed).
    ```bash
    bin/hefquin-server --federationDescription=MyFedConf.ttl
    ```
    where `MyFedConf.ttl` may be replaced by any file that contains an [RDF-based description of your federation](https://liusemweb.github.io/HeFQUIN/doc/federation_description.html).
  * Next, continue at the point "_Interacting with the HeFQUIN Service_" below.
  * Our [documentation page about running HeFQUIN via the embedded servlet container](https://liusemweb.github.io/HeFQUIN/doc/embedded_servlet_container.html) provides more details, including a description of how to use the current developer version instead of a release.
  * Moreover, you can also [set up a HeFQUIN service via a separate servlet container](https://liusemweb.github.io/HeFQUIN/doc/separate_servlet_container.html).

* **_Interacting with the HeFQUIN Service_**
  * After starting up the HeFQUIN service, you can first test it test by opening [`http://localhost:8080/`](http://localhost:8080/) in a Web browser (assuming that you have started the service at port 8080).
  * You can interact with the service like with a SPARQL endpoint (the endpoint should be exposed at `http://localhost:8080/sparql`). For instance, by using the command-line tool [`curl`](https://curl.se/), you may execute the following command to issue the query in a file called `ExampleQuery.rq`.
    ```bash
    curl -X POST http://localhost:8080/sparql --data-binary @ExampleQuery.rq -H 'Content-Type: application/sparql-query'
    ```
  * Our [documentation page about interacting with a HeFQUIN service](https://liusemweb.github.io/HeFQUIN/doc/hefquin_service.html) provides more details.
  * Moreover, you can read more about the [queries and query features that you can use](https://liusemweb.github.io/HeFQUIN/doc/queries.html).

### Using HeFQUIN via the Command-Line Program
* Download the ZIP package of the latest [release of HeFQUIN](https://github.com/LiUSemWeb/HeFQUIN/releases), unpack it, enter the resulting directory in a command-line terminal and, then, execute the following command (which assumes that you have a relatively recent version of Java installed).
  ```bash
  bin/hefquin --federationDescription=MyFedConf.ttl --query=ExampleQuery.rq
  ```
  where
  * `MyFedConf.ttl` may be replaced by any file that contains an [RDF-based description of your federation](https://liusemweb.github.io/HeFQUIN/doc/federation_description.html) and
  * `ExampleQuery.rq` is a file that contains your query (see [our documentation page about queries and query features that you can use](https://liusemweb.github.io/HeFQUIN/doc/queries.html)).
* Our [documentation page about the HeFQUIN command-line program](https://liusemweb.github.io/HeFQUIN/doc/cli.html) describes all the possible arguments that you can use for the program.
* Our [documentation page about setting up the HeFQUIN command-line programs](https://liusemweb.github.io/HeFQUIN/doc/programs.html#use-release) provides more details on the setup process, including a description of how to use the current developer version instead of a release.
* Moreover, there are [additional command-line programs that come with HeFQUIN](https://liusemweb.github.io/HeFQUIN/doc/programs.html).

### Using HeFQUIN as a Java Library
* Assuming you use Maven as the build tool for your Java project, you can specify HeFQUIN as a dependency for your project simply by adding the following snippet to the `dependencies` section of your POM file (make sure to change the given version number to the version number of the [HeFQUIN release](https://github.com/LiUSemWeb/HeFQUIN/releases) you want to use).
  ```xml
  <dependency>
    <groupId>se.liu.research.hefquin</groupId>
    <artifactId>hefquin-engine</artifactId>
    <version>0.0.1</version> <!-- replace this by the version number of the HeFQUIN release to be used -->
  </dependency>
  ```
* After changing the POM file, you can run the following command in your Maven project to compile your project with the new dependency (or you refresh/reimport your Maven project in the IDE that you are using).
  ```bash
  mvn clean compile
  ```
* Now you can use any of the Java interfaces and classes of HeFQUIN in the Java code of your project. You find information about these interfaces and classes in our [Javadoc documentation](https://liusemweb.github.io/HeFQUIN/javadoc/latest).
* Our [documentation page about integrating HeFQUIN into your Java code](https://liusemweb.github.io/HeFQUIN/doc/integrate_hefquin_code.html) provides an overview on how to run queries over a federation from within your Java code.
* Our [documentation page about adding HeFQUIN as a dependency to your Maven project](https://liusemweb.github.io/HeFQUIN/doc/hefquin_via_maven.html) provides more details on the setup process, including a description of how to use the current developer version instead of a release.
