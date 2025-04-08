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
* **Setup via Docker**
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
    * the `-v` argument refers to a file (e.g., `MyFedConf.ttl`) that contains the [description of your federation](https://liusemweb.github.io/HeFQUIN/doc/federation_description.html).
  * Our [documentation page about running HeFQUIN via Docker](https://liusemweb.github.io/HeFQUIN/doc/docker.html) provides more details, including a description of how to build your own Docker image of HeFQUIN
* **Setup via the Embedded Servlet Container**  TODO -- details: https://liusemweb.github.io/HeFQUIN/doc/embedded_servlet_container.html and https://liusemweb.github.io/HeFQUIN/doc/separate_servlet_container.html
* **Interacting with the HeFQUIN Service**  TODO -- details: https://liusemweb.github.io/HeFQUIN/doc/hefquin_service.html
### Using HeFQUIN via a Command-Line Program
TODO - details: https://liusemweb.github.io/HeFQUIN/doc/programs.html and https://liusemweb.github.io/HeFQUIN/doc/cli.html
### Using HeFQUIN as a Java Library
TODO - details: https://liusemweb.github.io/HeFQUIN/doc/hefquin_via_maven.html and https://liusemweb.github.io/HeFQUIN/doc/integrate_hefquin_code.html
