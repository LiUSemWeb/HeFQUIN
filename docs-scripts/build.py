#!/usr/bin/env python3
"""
Publish HeFQUIN vocabularies.

For each vocabulary file:
- read the source Turtle file
- detect its owl:versionInfo value
- compare it with the previously published version
- publish Turtle, RDF/XML, and JSON-LD copies
- generate HTML documentation with pyLODE

The script publishes vocabularies to two publication targets:
- versioned directory: docs/vocab/<name>/<version>/
- "latest" directory: docs/vocab/<name>/latest/
"""

from pathlib import Path
import logging
import shutil
import hashlib

from rdflib import OWL, RDF, Graph
from pylode.profiles.vocpub import VocPub

# Configure logger
logging.basicConfig(
    format='%(levelname)-8s [%(filename)s:%(lineno)d] %(message)s',
    level=logging.INFO
)
logger = logging.getLogger(__name__)

SOURCE_DIR = Path("vocabs/")
DOCS_DIR = Path("docs/vocab")
DEFAULT_VERSION = "0.0.1"
VOCABS = ["engineconf", "lpg2rdfconf", "feddesc"]

def get_version_info(path: Path) -> str:
    """
    Return the ontology version from owl:versionInfo.

    The function parses the RDF file, looks for a resource of type
    owl:Ontology, and reads its owl:versionInfo value.

    Args:
        path: Path to the ontology source file.

    Returns:
        The version string from owl:versionInfo, or DEFAULT_VERSION if
        no ontology or no versionInfo triple is found.
    """
    g = Graph()
    g.parse(path)
    ontology = next(g.subjects(RDF.type, OWL.Ontology), None)
    
    version_info = g.value(ontology, OWL.versionInfo)
    if version_info is None:
        logger.warning(
            "No owl:versionInfo found in %s, using default version %s",
            path,
            DEFAULT_VERSION
        )
        return DEFAULT_VERSION

    return str(version_info)


def hash_file(path: Path) -> str:
    """
    Compute the SHA-256 hash of a file.

    Parameters:
        path (str): Path to the file.

    Returns:
        str: Hexadecimal SHA-256 hash of the file content.
    """
    sha256_hash = hashlib.sha256()

    with path.open("rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256_hash.update(byte_block)

    return sha256_hash.hexdigest()


def publish_serializations(source: Path, name: str, version: str) -> None:
    """
    Publish the source vocabulary in multiple RDF serializations.

    The original Turtle file is copied unchanged, while RDF/XML and JSON-LD
    versions are generated from the parsed graph. Files are written to both
    the versioned publication directory and the "latest" directory.

    Args:
        source: Source Turtle file.
        name: Vocabulary name without extension.
        version: Version identifier.
    """
    logger.info("Publishing vocabulary '%s' (version %s)", name, version)

    versioned_dir = DOCS_DIR / name / version
    latest_dir = DOCS_DIR / name / "latest"

    versioned_dir.mkdir(parents=True, exist_ok=True)
    latest_dir.mkdir(parents=True, exist_ok=True)

    graph = Graph()
    graph.parse(source)

    for target_dir in (versioned_dir, latest_dir):
        shutil.copy2(source, target_dir / f"{name}.ttl")
        graph.serialize(destination=target_dir / f"{name}.rdf", format="xml")
        graph.serialize(destination=target_dir / f"{name}.jsonld", format="json-ld")


def create_documentation(source: Path, name: str, version: str) -> None:
    """
    Generate HTML documentation for a vocabulary using pyLODE.

    The same generated HTML is written to both the versioned directory and
    the "latest" directory.

    Args:
        source: Source Turtle file.
        name: Vocabulary name.
        version: Version identifier.
    """
    logger.info("Generating documentation for '%s' (version %s)", name, version)

    versioned_index = DOCS_DIR / name / version / "index.html"
    latest_index = DOCS_DIR / name / "latest" / "index.html"

    doc = VocPub(ontology=str(source))
    html = doc.make_html()

    versioned_index.write_text(html, encoding="utf-8")
    latest_index.write_text(html, encoding="utf-8")


def main() -> None:
    """
    Publish all configured vocabularies.

    For each vocabulary, the script compares the current source file against
    the already published file for the detected version. If the content is
    unchanged, publication is skipped. Otherwise, RDF serializations and HTML
    documentation are regenerated.
    """
    for name in VOCABS:
        source = Path(SOURCE_DIR) / f"{name}.ttl"

        if not source.is_file():
            logger.error("Source file does not exist: %s", source)
            continue

        published_ttl = DOCS_DIR / name / f"latest/{name}.ttl"

        if published_ttl.is_file() and hash_file(source) == hash_file(published_ttl):
            logger.info("No changes detected for '%s', skipping", name)
            continue

        version = get_version_info(source)
        publish_serializations(source, name, version)
        create_documentation(source, name, version)


if __name__ == "__main__":
    main()
