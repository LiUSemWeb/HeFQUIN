## What is HeFQUIN?

HeFQUIN is a query federation engine for heterogeneous graph data sources (e.g., federated knowledge graphs).

Project home and documentation: https://liusemweb.github.io/HeFQUIN/


## Key resources

* Feature overview: https://liusemweb.github.io/HeFQUIN/doc/features.html
* User docs: https://liusemweb.github.io/HeFQUIN/doc/
* Run using Docker: https://liusemweb.github.io/HeFQUIN/doc/docker.html
* Run using CLI: https://liusemweb.github.io/HeFQUIN/doc/programs.html
* Developer docs: https://liusemweb.github.io/HeFQUIN/devdoc/
* Research publications: https://liusemweb.github.io/HeFQUIN/research/
* GitHub releases page: https://github.com/LiUSemWeb/HeFQUIN/releases/


## Quick start (Docker)

docker pull ghcr.io/LiUSemWeb/hefquin:latest
docker run \
    -p 8080:8080 \
    -v MyFedConf.ttl:/usr/local/tomcat/webapps/ROOT/DefaultFedConf.ttl \
    hefquin:latest

Visit http://localhost:8080/ and send SPARQL queries to http://localhost:8080/sparql.

Example (with curl):

curl -X POST http://localhost:8080/sparql \
    --data-binary @ExampleQuery.rq \
    -H 'Content-Type: application/sparql-query'


## Quick start (CLI):

bin/hefquin --federationDescription=MyFedConf.ttl \
    --query=ExampleQuery.rq

See https://liusemweb.github.io/HeFQUIN/doc/programs.html for full CLI options.


## Add HeFQUIN to your build:

Include using Maven:

<dependency>
    <groupId>se.liu.research.hefquin</groupId>
    <artifactId>hefquin-engine</artifactId>
    <version>x.y.z</version> <!-- replace this by the version number of the HeFQUIN release to be used -->
</dependency> 

Include using Gradle:

implementation("se.liu.research.hefquin:hefquin-engine:x.y.z")


## Need help or contribute?

Issues & discussions: https://github.com/LiUSemWeb/HeFQUIN/
Contributor guidelines: https://liusemweb.github.io/HeFQUIN/devdoc/


Licensed under the Apache 2.0 License.