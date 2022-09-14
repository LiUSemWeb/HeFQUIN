# HeFQUIN
HeFQUIN is a query federation engine for heterogeneous federations of graph data sources (e.g, federated knowledge graphs) that is currently under development by [the Semantic Web research group at Linköping University](https://www.ida.liu.se/research/semanticweb/).

### Features of HeFQUIN
* So far, support for SPARQL endpoints, TPF, and brTPF
  * [work on openCypher Property Graphs ongoing](https://github.com/LiUSemWeb/HeFQUIN/tree/main/src/main/java/se/liu/ida/hefquin/engine/wrappers/graphqlwrapper)
  * [work on GraphQL APIs ongoing](https://github.com/LiUSemWeb/HeFQUIN/tree/main/src/main/java/se/liu/ida/hefquin/engine/wrappers/graphqlwrapper)
* Initial support for vocabulary mappings
* [Heuristics-based logical query optimizer](https://github.com/LiUSemWeb/HeFQUIN/wiki/Heuristics-Based-Logical-Query-Optimizer)
* Several different [cost-based physical optimizers](https://github.com/LiUSemWeb/HeFQUIN/wiki/Cost-Based-Physical-Query-Optimizers) (greedy, dynamic programming, simulated annealing, randomized iterative improvement)
* Relevant physical operators; e.g., hash join, symmetric hash join (SHJ), request-based nested-loops join (NLJ), several variations of bind joins (brTPF-based, UNION-based, FILTER-based, VALUES-based)
* Two execution models (push-based and pull-based)
* Features for getting an understanding of the internals of the engine
  * printing of logical and physical plans
  * programmatic access to execution statistics on the level of individual operators and data structures, as well as printing of these statistics from the CLI
* 350+ unit tests

### Current Limitations
* HeFQUIN does not yet have a source selection component. All subpatterns of the queries given to HeFQUIN need to be wrapped in SERVICE clauses.

### Publications related to HeFQUIN
* Sijin Cheng and Olaf Hartig: [FedQPL: A Language for Logical Query Plans over Heterogeneous Federations of RDF Data Sources](http://olafhartig.de/files/ChengHartig_FedQPL_iiWAS2020_Extended.pdf). In Proceedings of the 22nd International Conference on Information Integration and Web-based Applications & Services (iiWAS), 2020.
