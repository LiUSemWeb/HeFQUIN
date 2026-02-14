# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and
this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [unreleased]

### Added
- Support for optional parameters in the template IRIs for REST APIs ([#493](https://github.com/LiUSemWeb/HeFQUIN/pull/493)).
- Adding an implementation of the batch-based bind-join algorithm for SPARQL endpoints that issues the bind-join requests without blocking, handling the processing of their responses in parallel ([#499](https://github.com/LiUSemWeb/HeFQUIN/pull/499)).
### Changed
- Change to Hydra vocabulary for the template IRIs of REST APIs in federation descriptions ([#493](https://github.com/LiUSemWeb/HeFQUIN/pull/493)).
- Updated request processors to use an HTTP client provider and shared connection pools ([#495](https://github.com/LiUSemWeb/HeFQUIN/pull/495)).
- Adding unique IDs for plans and sub-plan, and using these in the plan printers ([#500](https://github.com/LiUSemWeb/HeFQUIN/pull/500)).
- Some clean up in the classes for plans and for operators ([#500](https://github.com/LiUSemWeb/HeFQUIN/pull/500)).
- Bug fix: the base implementation of the bind-join algorithm did not correctly implement bag semantics ([#498](https://github.com/LiUSemWeb/HeFQUIN/pull/498)).


## [0.0.8] - 2026-01-14

### Added
- Initial support for RML-wrapped REST endpoints as federation members ([#486](https://github.com/LiUSemWeb/HeFQUIN/pull/486)).
- Adding a 'numberOfSolMapsRetrievedPerReqOp' entry to the stats of bind joins ([#489](https://github.com/LiUSemWeb/HeFQUIN/pull/489)).
### Changed
- Reimplementation of the printers for logical and for physical plans ([#487](https://github.com/LiUSemWeb/HeFQUIN/pull/487)).
- Bux fix: 'equals' comparison of logical and also of physical plans must take the ID of the root operator into account ([#488](https://github.com/LiUSemWeb/HeFQUIN/pull/488)).


## [0.0.7] - 2025-11-03

### Added
- Basis for proper management of query-planning-related information ([#455](https://github.com/LiUSemWeb/HeFQUIN/pull/455), [#456](https://github.com/LiUSemWeb/HeFQUIN/pull/456), [#461](https://github.com/LiUSemWeb/HeFQUIN/pull/461)).
- Extending the text-based plan printers to include information about expected variables at each level of the plan ([#457](https://github.com/LiUSemWeb/HeFQUIN/pull/457)).
- Adding the bound join algorithm from the FedX paper ([#458](https://github.com/LiUSemWeb/HeFQUIN/pull/458)).
- Adding basic printing of executable plans ([#473](https://github.com/LiUSemWeb/HeFQUIN/pull/473)).
- Adding the option to specify in the config file which logical-to-physical plan converter and which logical-to-physical operator converter should be used, including which physical operators they should consider ([#481](https://github.com/LiUSemWeb/HeFQUIN/pull/481)).
### Changed
- Merging the logical operators tpAdd, bgpAdd, and gpAdd into just one: gpAdd; likewise for tpOptAdd, bgpOptAdd, and gpOptAdd ([#454](https://github.com/LiUSemWeb/HeFQUIN/pull/454)).
- Removing the old StatsPrinter in favor of the new JSON-based one ([#460](https://github.com/LiUSemWeb/HeFQUIN/pull/460)).
- Changing the cardinality-based join ordering heuristic to avoid cardinality requests for plans without joins ([#465](https://github.com/LiUSemWeb/HeFQUIN/pull/465)).
- Removing the "GreedyBasedReordering" heuristic from the default sequence of heuristics of the logical optimizer ([#467](https://github.com/LiUSemWeb/HeFQUIN/pull/467)).
- Upgrading the code base to version 5.5.0 of Jena ([#478](https://github.com/LiUSemWeb/HeFQUIN/pull/478)).
- Reorganizes completely how the federation access manager works ([#480](https://github.com/LiUSemWeb/HeFQUIN/pull/480)).


## [0.0.6] - 2025-06-30

### Added
- Support for multiple VALUES clauses ([#417](https://github.com/LiUSemWeb/HeFQUIN/pull/417)).
- Additional endpoint of the HeFQUIN service to request query processing details ([#406](https://github.com/LiUSemWeb/HeFQUIN/pull/406)).
- Initial GUI with query editor ([#427](https://github.com/LiUSemWeb/HeFQUIN/pull/427)).
- HeFQUINEngineBuilder added as a means to consolidate and simplify the creation of a HeFQUINEngine instance ([#440](https://github.com/LiUSemWeb/HeFQUIN/pull/440), [#441](https://github.com/LiUSemWeb/HeFQUIN/pull/441), and [#448](https://github.com/LiUSemWeb/HeFQUIN/pull/448)).
- ExternalIntegrationDemo added as a means to demonstrate how the HeFQUIN engine can be used directly within the Java code of other Java projects ([#448](https://github.com/LiUSemWeb/HeFQUIN/pull/448)).
### Changed
- Improving the text-based plan printers to make the printed plans easier to look at ([d83c8c2](https://github.com/LiUSemWeb/HeFQUIN/commit/d83c8c227dae2805af8835cf10f412008604c463), [21020c5](https://github.com/LiUSemWeb/HeFQUIN/commit/21020c58f57e26f084ced610917d6ec716f33ac2), [80189b7](https://github.com/LiUSemWeb/HeFQUIN/commit/80189b757b1ee59d3e0c3d647b55ac0a126f5d52), [66d8124](https://github.com/LiUSemWeb/HeFQUIN/commit/66d81248c963d3b790aca57998218b6b77c4625a), [#442](https://github.com/LiUSemWeb/HeFQUIN/pull/442))
- The MergeRequests heuristic does not anymore push BIND operators into requests ([#433](https://github.com/LiUSemWeb/HeFQUIN/pull/433)).
- Fixing and streamlining the various bind join implementations ([#443](https://github.com/LiUSemWeb/HeFQUIN/pull/443) and [e7ae6a9](https://github.com/LiUSemWeb/HeFQUIN/commit/e7ae6a94b68f0553e89a2cedefaf28c7338619c8)).
- Removing the explicit notion of default components from the vocabulary for describing a HeFQUINEngine configuration ([#441](https://github.com/LiUSemWeb/HeFQUIN/pull/441)).
- Replacing the implicit definition of the vocabulary for describing federations by an explicit one ([#432](https://github.com/LiUSemWeb/HeFQUIN/pull/432)).
- Refactoring of DataRetrievalResponse ([#409](https://github.com/LiUSemWeb/HeFQUIN/pull/409)).
- Refactoring of QueryPatternUtils ([#428](https://github.com/LiUSemWeb/HeFQUIN/pull/428)).
- Moving the code for federation access into a separate Maven module called hefquin-access ([#450](https://github.com/LiUSemWeb/HeFQUIN/pull/450)).
- Moving the code generated from the vocabularies into hefquin-vocabs, making that an actual Maven module ([#450](https://github.com/LiUSemWeb/HeFQUIN/pull/450)).

## [0.0.5] - 2025-03-04

Starting the change log at this version.
