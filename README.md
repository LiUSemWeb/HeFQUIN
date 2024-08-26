# HeFQUIN
HeFQUIN is a query federation engine for heterogeneous federations of graph data sources (e.g, federated knowledge graphs) that is currently under development by [the Semantic Web research group at Linköping University](https://www.ida.liu.se/research/semanticweb/).

### Features of HeFQUIN
* Support for all features of SPARQL 1.1 (where basic graph patterns, group graph patterns (AND), union graph patterns, optional patterns, and filters are supported natively within the HeFQUIN engine, and the other features of SPARQL are supported through integration of the HeFQUIN engine into Apache Jena)
* So far, support for SPARQL endpoints, TPF, and brTPF
  * [work on openCypher Property Graphs ongoing](https://github.com/LiUSemWeb/HeFQUIN/tree/main/src/main/java/se/liu/ida/hefquin/engine/wrappers/lpgwrapper)
  * [work on GraphQL APIs ongoing](https://github.com/LiUSemWeb/HeFQUIN/tree/main/src/main/java/se/liu/ida/hefquin/engine/wrappers/graphqlwrapper)
* Initial support for vocabulary mappings
* [Heuristics-based logical query optimizer](https://github.com/LiUSemWeb/HeFQUIN/wiki/Heuristics-Based-Logical-Query-Optimizer)
* Several different [cost-based physical optimizers](https://github.com/LiUSemWeb/HeFQUIN/wiki/Cost-Based-Physical-Query-Optimizers) (greedy, dynamic programming, simulated annealing, randomized iterative improvement)
* Relevant [physical operators](https://github.com/LiUSemWeb/HeFQUIN/wiki/Physical-Operators); e.g., hash join, symmetric hash join (SHJ), request-based nested-loops join (NLJ), several variations of bind joins (brTPF-based, UNION-based, FILTER-based, VALUES-based)
* Two execution models (push-based and pull-based)
* Features for getting an understanding of the internals of the engine
  * printing of logical and physical plans
  * programmatic access to execution statistics on the level of individual operators and data structures, as well as printing of these statistics from the CLI
* 380+ unit tests

### Current Limitations
* HeFQUIN does not yet have a source selection component. All subpatterns of the queries given to HeFQUIN need to be wrapped in SERVICE clauses.

### Publications related to HeFQUIN
* Sijin Cheng and Olaf Hartig: **[FedQPL: A Language for Logical Query Plans over Heterogeneous Federations of RDF Data Sources](https://olafhartig.de/files/ChengHartig_FedQPL_iiWAS2020_Extended.pdf)**. In _Proceedings of the 22nd International Conference on Information Integration and Web-based Applications & Services (iiWAS)_, 2020.
* Sijin Cheng and Olaf Hartig: **[Source Selection for SPARQL Endpoints: Fit for Heterogeneous Federations of RDF Data Sources?](https://olafhartig.de/files/ChengHartig_QuWeDa2022.pdf)**. In _Proceedings of the 6th Workshop on Storing, Querying and Benchmarking Knowledge Graphs (QuWeDa)_, 2022.
* Sijin Cheng and Olaf Hartig: **[A Cost Model to Optimize Queries over Heterogeneous Federations of RDF Data Sources](https://olafhartig.de/files/ChengHartig_CostModel_DMKG2023.pdf)**. In _Proceedings of the 1st International Workshop on Data Management for Knowledge Graphs (DMKG)_, 2023.
  * repo related to the experiments in this paper: [LiUSemWeb/HeFQUIN-DMKG2023-Experiments](https://github.com/LiUSemWeb/HeFQUIN-DMKG2023-Experiments)
* Sijin Cheng, Sebastian Ferrada, and Olaf Hartig: **[Considering Vocabulary Mappings in Query Plans for Federations of RDF Data Sources](https://olafhartig.de/files/ChengEtAL_VocabMappings_CoopIS2023.pdf)**. In _Proceedings of the 29th International Conference on Cooperative Information Systems (CoopIS)_, 2023.
  * repo related to the experiments in this paper: [LiUSemWeb/HeFQUIN-VocabMappingsExperiments](https://github.com/LiUSemWeb/HeFQUIN-VocabMappingsExperiments)
