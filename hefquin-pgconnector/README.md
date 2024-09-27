# HeFQUIN PG Connector
This module of the HeFQUIN code base contains the code for HeFQUIN to connect to Property Graph data sources. In particular, this module provides the functionality to run SPARQL-star queries over user-configurable RDF-star views of Property Graphs by translating these queries into openCypher queries which, then, are sent directly to a Property Graph store (we only support Neo4j at the moment).

Based on this module, HeFQUIN also provides two extra command-line programs:
* [**hefquin-pg**](#run-the-hefquin-pg-program) provides the SPARQL-star query functionality for Property Graphs as a standalone tool, and
* [**hefquin-pgmat**](#run-the-hefquin-pgmat-program) can be used to convert the Property Graphs into an RDF-star graphs by materializing the RDF-star views that are otherwise considered only as virtual views by the query functionality.

While documenting the query-translation functionality implemented by this module is still work in progress, the (user-configurable) Property Graph to RDF-star mapping that defines the RDF-star views is described in the [next section](#user-configurable-mapping-of-property-graphs-to-rdf-star-graphs), followed by a [description of the two command-line programs](#command-line-programs).

## User-Configurable Mapping of Property Graphs to RDF-star Graphs
The SPARQL-star based query functionality provided by this module considers specific types of RDF-star views of an underlying Property Graph. These views are defined based on a user-configurable mapping from the Property Graph model to RDF-star.

The idea of this mapping is simple: Given a Property Graph, every edge of this graph (including the label of the edge) is represented as an ordinary RDF triple in the resulting RDF-star data. The same holds for each node with its label, as well as for every node property. Edge properties are represented as nested triples that contain, as their subject, the triple representing the corresponding edge (more details and examples below).

While the structure of the resulting RDF-star graphs cannot be influenced, the mapping is configurable in terms of the particular elements (blank nodes and IRIs) of the triples that it generates. Formally, this form of configuration is captured by a notion that we call *LPG-to-RDF-star configuration*. The concrete LPG-to-RDF-star configuration to be used when applying the mapping may, in principle, be specified in various forms. The form that the HeFQUIN-PGConnector expects is an RDF-based description using [a specific RDF vocabulary](https://github.com/LiUSemWeb/HeFQUIN/blob/main/hefquin-vocabs/LPGtoRDFConfiguration.ttl) that we have developed for this purpose and that provides a lot of different options for the different components of LPG-to-RDF-star configurations.

The following sections describe the mapping approach in more detail and, at the same time, introduce the components of LPG-to-RDF-star configurations, together with some of the possible options for each of these components. For a complete definition of all the options, refer to [the file that defines the vocabulary](https://github.com/LiUSemWeb/HeFQUIN/blob/main/hefquin-vocabs/LPGtoRDFConfiguration.ttl), and a formal definition of the whole mapping approach can be found in the following research paper:

* Olaf Hartig: [Foundations to Query Labeled Property Graphs using SPARQL*](http://olafhartig.de/files/Hartig_AMAR2019_Preprint.pdf). In _Proceedings of the 1st International Workshop on Approaches for Making Data Interoperable (AMAR)_, 2019.

### Node Mapping

The *first component* of every LPG-to-RDF-star configuration is a so-called *node mapping* which specifies whether the nodes of the Property Graph are mapped to blank nodes or to IRIs and, in the case of IRIs, what these IRIs look like.

If you want to use blank nodes to capture the nodes of your Property Graph in the resulting RDF-star view, then you have to use a node mapping of type `lr:BNodeBasedNodeMapping` in the RDF-based description of your LPG-to-RDF-star configuration. Hence, the relevant part of this description may look as follows (presented in RDF Turtle format, prefix declarations omitted).
```turtle
_:c1  rdf:type  lr:LPGtoRDFConfiguration ;
      lr:nodeMapping [ rdf:type lr:BNodeBasedNodeMapping ] .
```

As an alternative to blank nodes, the nodes of the Property Graph may be mapped to IRIs. The specific type of node mapping that HeFQUIN-PGConnector supports for this case is `lr:IRIPrefixBasedNodeMapping` which, for each node of the Property Graph, creates an IRI by appending the ID of the node to a common IRI prefix. The IRI prefix to be used can be specified via the `lr:prefixOfIRIs` property.

**Example:** Assume a Property Graph with two nodes which have the IDs 153 and 295, respectively. By using an LPG-to-RDF-star configuration with a node mapping as specified in the following description, these two Property Graph nodes are mapped to the IRIs `http://example.org/node/153` and `http://example.org/node/295`, respectively.
```turtle
_:c2  rdf:type  lr:LPGtoRDFConfiguration ;
      lr:nodeMapping [ rdf:type lr:IRIPrefixBasedNodeMapping ;
                       lr:prefixOfIRIs "http://example.org/node/"^^xsd:anyURI ] .
```

### Node Label Mapping and Label Predicate

The mapping approach captures the label of every Property Graph node by an RDF triple whose subject is the blank node or the IRI that the node mapping (see above) associated with that node. The object of such a triple is determined by a so-called *node label mapping*, which is the *second component* of an LPG-to-RDF-star configuration, and the predicate is given as the *third component* of LPG-to-RDF-star configurations. In the RDF-based descriptions of LPG-to-RDF-star configurations, this predicate is specified via the `lr:labelPredicate` property; whereas, for specifying the node label mapping, there are different options (see all the sub-classes of `lr:NodeLabelMapping` in [our RDF vocabulary](https://github.com/LiUSemWeb/HeFQUIN/blob/main/hefquin-vocabs/LPGtoRDFConfiguration.ttl)). One of these options, as illustrated in the following example, is to create IRIs by appending the node label to a common IRI prefix again (i.e., similar to the IRI prefix option for node mappings that we mentioned in the previous section).

**Example:** Consider again the two Property Graph nodes of the previous example and assume that the node with ID 153 has the label "Person" whereas the node with ID 295 is labeled "Book". By using an LPG-to-RDF-star configuration with a node label mapping (and label predicate) as specified in the following description, we obtain the two label-related RDF triples `(http://example.org/node/153, rdf:type, http://example.org/type/Person)` and `(http://example.org/node/295, rdf:type, http://example.org/type/Book)`.
```turtle
_:c2  rdf:type  lr:LPGtoRDFConfiguration ;
      lr:nodeMapping [
               rdf:type lr:IRIPrefixBasedNodeMapping ;
               lr:prefixOfIRIs "http://example.org/node/"^^xsd:anyURI ] ;
      lr:nodeLabelMapping [
               rdf:type lr:IRIPrefixBasedNodeLabelMapping ;
               lr:prefixOfIRIs "http://example.org/type/"^^xsd:anyURI ] ;
      lr:labelPredicate "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"^^xsd:anyURI .
```

While the IRIs for the node labels in the previous example are created in a generic manner, it is also possible to map (some of the) node labels to IRIs defined by existing RDF vocabularies.

**Example:** Consider the following LPG-to-RDF-star configuration.
```turtle
_:c3  rdf:type  lr:LPGtoRDFConfiguration ;
      lr:nodeMapping [
               rdf:type lr:IRIPrefixBasedNodeMapping ;
               lr:prefixOfIRIs "http://example.org/node/"^^xsd:anyURI ] ;
      lr:nodeLabelMapping [
               rdf:type lr:CompositeNodeLabelMapping ;
               lr:componentMappings ( _:nlm1  _:nlm2  _:nlm3 ) ] ;
      lr:labelPredicate "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"^^xsd:anyURI .

_:nlm1  rdf:type  lr:SingletonIRINodeLabelMapping ;
        lr:label  "Person" ;
        lr:iri "http://xmlns.com/foaf/0.1/Person"^^xsd:anyURI .

_:nlm2  rdf:type  lr:SingletonIRINodeLabelMapping ;
        lr:label  "Book" ;
        lr:iri "https://schema.org/Book"^^xsd:anyURI .

_:nlm3  rdf:type  lr:IRIPrefixBasedNodeLabelMapping ;
        lr:prefixOfIRIs "http://example.org/type/"^^xsd:anyURI .
```
By using this LPG-to-RDF-star configuration to map the two Property Graph nodes of the previous example, the two label-related RDF triples that we obtain are different: `(http://example.org/node/153, rdf:type, foaf:Person)` and `(http://example.org/node/295, rdf:type, schema:Book)`. Notice that the node label mapping of the LPG-to-RDF-star configuration is of type `lr:CompositeNodeLabelMapping`, which means that it is composed of multiple other node label mappings (which are listed via the `lr:componentMappings` property). In this particular case, it is composed of three other node label mappings, where the first one focuses only on the node label "Person" and maps this particular node label to the [Person class](http://xmlns.com/foaf/spec/#term_Person) of the [FOAF vocabulary](http://xmlns.com/foaf/spec/). Similarly, the second one maps (only) the node label "Book" to the [Book class](https://schema.org/Book) of the [Schema.org vocabulary](https://schema.org/) (and the third one covers all other node labels in the same generic way as in the previous example).

It is also possible to extend an `lr:IRIPrefixBasedNodeLabelMapping` with a regular expression (see `lr:RegexBasedNodeLabelMapping`) such that only the node labels that match this regular expression are mapped according to the node label mapping. This way, different groups of node labels may be mapped to IRIs with different prefixes.

As a last example, we illustrate that node labels may also be mapped to string literals (instead of IRIs), which can be achieved by using a node label mapping of type `lr:LiteralBasedNodeLabelMapping` (or `lr:SingletonLiteralNodeLabelMapping`, but that is not covered in our example).

**Example:** By using the following LPG-to-RDF-star configuration, we obtain the two label-related RDF triples `(http://example.org/node/153, rdfs:label, "Person")` and `(http://example.org/node/295, rdfs:label, "Book")` for our running example (notice that we also use a different label predicate here).
```turtle
_:c4  rdf:type  lr:LPGtoRDFConfiguration ;
      lr:nodeMapping [
               rdf:type lr:IRIPrefixBasedNodeMapping ;
               lr:prefixOfIRIs "http://example.org/node/"^^xsd:anyURI ] ;
      lr:nodeLabelMapping [
               rdf:type lr:LiteralBasedNodeLabelMapping `] ;
      lr:labelPredicate "http://www.w3.org/2000/01/rdf-schema#label"^^xsd:anyURI .
```

### Edge Label Mapping

Every edge of the given Property Graph is mapped to an RDF triple whose subject (resp. object) is the blank node or the IRI that the [node mapping](#node-mapping) assigns to the source node (resp. target node) of the edge, and the predicate is determined based on the label of the edge. As for the latter, the *fourth component* of LPG-to-RDF-star configurations is a so-called *edge label mapping* which maps any edge label to an IRI. The types of edge label mappings supported by HeFQUIN-PGConnector are similar to the types of node label mappings and are captured as sub-classes of `lr:EdgeLabelMapping` in [our RDF vocabulary](https://github.com/LiUSemWeb/HeFQUIN/blob/main/hefquin-vocabs/LPGtoRDFConfiguration.ttl).

**Example:** Assume the Property Graph considered in the previous examples contains an edge from the book node (i.e., the node with ID 295) to the person node (with ID 153) and this edge has the label "fundedBy" to indicate that the book was funded by the person. Given the following LPG-to-RDF-star configuration, this edge is mapped to the triple `(http://example.org/node/295, schema:funder, http://example.org/node/153)`.
```turtle
_:c3  rdf:type  lr:LPGtoRDFConfiguration ;
      lr:nodeMapping [
               rdf:type lr:IRIPrefixBasedNodeMapping ;
               lr:prefixOfIRIs "http://example.org/node/"^^xsd:anyURI ] ;
     #lr:nodeLabelMapping ...
     #lr:labelPredicate ...
      lr:edgeLabelMapping [
               rdf:type lr:CompositeEdgeLabelMapping ;
               lr:componentMappings ( _:elm1  _:elm2  ) ] .

_:elm1  rdf:type  lr:SingletonIRIEdgeLabelMapping ;
        lr:label  "fundedBy" ;
        lr:iri "https://schema.org/funder"^^xsd:anyURI .

_:elm2  rdf:type  lr:IRIPrefixBasedEdgeLabelMapping ;
        lr:prefixOfIRIs "http://example.org/relationship/"^^xsd:anyURI .
```

We emphasize that the mapping approach is *not* information preserving for Property Graphs that contain multiple edges between the same nodes with the same label. That is, by the mapping approach, all edges with the same source node, the same target node, and the same label, collapse into a single triple in the RDF-star view.

### Property Name Mapping

The *fifth component* of LPG-to-RDF-star configurations is a so-called *property name mapping* which is used to determine the predicate of every triple that captures a property of a node or an edge of the mapped Property Graph.

**Example:** Assume that the person node in our example Property Graph has a property named "name" with the value "Bob", and the "fundedBy" edge has a property named "amount" with the value 3000. By using the following LPG-to-RDF-star configuration, for which we now specify a property name mapping, the property of the person node is mapped to the triple `(http://example.org/node/153, foaf:name, "Bob")` and the property of the "fundedBy" edge is mapped to the (nested!) triple `( (http://example.org/node/295, schema:funder, http://example.org/node/153), http://example.org/p/amount, 3000 )`.
```turtle
_:c3  rdf:type  lr:LPGtoRDFConfiguration ;
      lr:nodeMapping [
               rdf:type lr:IRIPrefixBasedNodeMapping ;
               lr:prefixOfIRIs "http://example.org/node/"^^xsd:anyURI ] ;
     #lr:nodeLabelMapping ...
     #lr:labelPredicate ...
      lr:edgeLabelMapping [
               rdf:type lr:CompositeEdgeLabelMapping ;
               lr:componentMappings ( _:elm1  _:elm2  ) ] ;
      lr:propertyNameMapping [
               rdf:type lr:CompositePropertyNameMapping ;
               lr:componentMappings ( _:pm1  _:pm2  ) ] .

_:elm1  rdf:type  lr:SingletonIRIEdgeLabelMapping ;
        lr:label  "fundedBy" ;
        lr:iri "https://schema.org/funder"^^xsd:anyURI .

_:elm2  rdf:type  lr:IRIPrefixBasedEdgeLabelMapping ;
        lr:prefixOfIRIs "http://example.org/relationship/"^^xsd:anyURI .

_:pm1  rdf:type  lr:SingletonIRIPropertyNameMapping ;
       lr:label  "name" ;
       lr:iri "http://xmlns.com/foaf/0.1/name"^^xsd:anyURI .

_:pm2  rdf:type  lr:IRIPrefixBasedPropertyNameMapping ;
       lr:prefixOfIRIs "http://example.org/p/"^^xsd:anyURI .
```

## Command-Line Programs
### Compile the Programs
To use the command-line programs you first need to compile them using the Java source code in this repository.

To this end, first create a local clone of this repository on your computer, which you can do by opening a terminal, navigate to the directory into which you want to clone the repository, and then either [clone the repository with its SSH URL](https://docs.github.com/en/get-started/getting-started-with-git/about-remote-repositories#cloning-with-ssh-urls) by running the following command
```
git clone git@github.com:LiUSemWeb/HeFQUIN.git
```
or [clone the repository with its HTTPS URL](https://docs.github.com/en/get-started/getting-started-with-git/about-remote-repositories#cloning-with-https-urls) by running the following command
```
git clone https://github.com/LiUSemWeb/HeFQUIN.git
```
Both of these two commands should give you a sub-directory called `HeFQUIN`. Enter this directory and use Maven to compile the Java source code by running the following command
```
mvn clean package
```
Assuming the Maven process completes successfully, you can use the command-line programs provided in this repository as described in the following sections.

### Run the `hefquin-pg` Program
`hefquin-pg` provides the query functionality of the HeFQUIN PG Connector module as a standalone tool. Since it is a Unix shell script, it needs to be run in a terminal. The default way to do so is by running the following command (assuming you are in the root directory of your local clone of the HeFQUIN repository),
```
bin/hefquin-pg --query=query.rq --lpg2rdfconf=LPG2RDF.ttl --endpoint=http://localhost:7474/db/neo4j/tx/commit
```
where the `--query` argument is used to refer to a file that contains the SPARQL-star query you want execute, the `--lpg2rdfconf` argument refers to an RDF document that provides an RDF-based description of the LPG-to-RDF-star configuration that you want to use for the RDF-star view of your Property Graph (an example of an RDF Turtle file with such a description is provided as part of this repository and, thus, available in your local clone of the repository---see: `ExampleLPG2RDF.ttl`), and the `--endpoint` argument specifies the HTTP address at which your Neo4j instance responds to Cypher queries over your Property Graph.

Further arguments can be used. To see a list of all arguments supported by the program, run the program with the `--help` argument:
```
bin/hefquin-pg --help
```

### Run the `hefquin-pgmat` Program
`hefquin-pgmat` can be used to convert any Property Graph into an RDF-star graph by applying our [user-configurable Property Graph to RDF-star mapping](#user-configurable-mapping-of-property-graphs-to-rdf-star-graphs). As a Unix shell script, it needs to be run in a terminal. The default way to do so is by running the following command (assuming you are in the root directory of your local clone of the HeFQUIN repository),
```
bin/hefquin-pgmat --lpg2rdfconf=LPG2RDF.ttl --endpoint=http://localhost:7474/db/neo4j/tx/commit
```
where the `--lpg2rdfconf` argument refers to an RDF document that provides an RDF-based description of the LPG-to-RDF-star configuration that you want to use for the RDF-star view of your Property Graph (an example of an RDF Turtle file with such a description is provided as part of this repository and, thus, available in your local clone of the repository---see: `ExampleLPG2RDF.ttl`) and the `--endpoint` argument specifies the HTTP address at which your Neo4j instance responds to Cypher queries over your Property Graph.

Further arguments can be used. To see a list of all arguments supported by the program, run the program with the `--help` argument:
```
bin/hefquin-pgmat --help
```
