## docs-scripts

This script automates the management and documentation of RDF vocabularies for the HeFQUIN project. The script is called on publish to `gh-pages` as part of the `publish_docs.yml` workflow.
The vocabulary files and their corresponding documentation are published via the GitHub Pages action. They are accessible at the following URLs:

- `https://liusemweb.github.io/HeFQUIN/vocab/<name>/<version>/` (direct access to the files)
- `https://w3id.org/hefquin/<name>/<version>/` (persistent URIs that redirect to the files)

The script performs the following tasks:

### 1. Change Detection

The script first checks the SHA-256 hashes of published vocabulary files and the source files under `hefquin-vocabs/` to detect changes. Only vocabularies with updated source files are reprocessed.

### 2. Vocabulary Copying
The vocabulary files are copied from `hefquin-vocabs/` and serialized to RDF/XML (`.rdf`) and JSON-LD (`.jsonld`) formats to two target locations:
- `docs/vocab/<name>/<version>/`
- `docs/vocab/<name>/latest/`

### 3. Documentation Generation

[PyLODE](https://github.com/RDFLib/PyLODE) is used to automatically generate HTML documentation for each vocabulary. The documentation is copied to both the versioned and latest vocabulary directories.
