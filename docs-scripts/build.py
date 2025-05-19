#!/usr/bin/env python3
import os
from rdflib import Graph
import logging
import shutil
import hashlib
from pylode.profiles.vocpub import VocPub

# Configure root logger
logger = logging.getLogger()
logger.setLevel(logging.INFO)
logging.basicConfig(format='%(levelname)-8s [%(filename)s:%(lineno)d] %(message)s',
                    level=logging.DEBUG)


def hash_file(filename):
    """
    Compute the SHA-256 hash of a file.

    Parameters:
        filename (str): Path to the file.

    Returns:
        str: Hexadecimal SHA-256 hash of the file content.
    """
    sha256_hash = hashlib.sha256()

    with open(filename, 'rb') as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256_hash.update(byte_block)

    return sha256_hash.hexdigest()


def copy_vocabularies(name, version):
    """
    Copy the RDF vocabulary file to 'docs/vocab' and generate serialized
    versions in RDF/XML and JSON-LD formats for both versioned and latest
    targets.

    Parameters:
        name (str): Vocabulary name (without extension).
        version (str): Vocabulary version identifier.
    """
    logger.info("Copying vocabulary: '%s'", name)

    versioned_target = f"docs/vocab/{name}/{version}/"
    latest_target = f"docs/vocab/{name}/latest/"
    os.makedirs(versioned_target, exist_ok=True)
    os.makedirs(latest_target, exist_ok=True)

    g = Graph()
    g.parse(f"hefquin-vocabs/{name}.ttl")

    # Versioned
    shutil.copy2(f"hefquin-vocabs/{name}.ttl", f"{versioned_target}{name}.ttl")
    g.serialize(destination=f"{versioned_target}{name}.rdf", format="xml")
    g.serialize(destination=f"{versioned_target}{name}.jsonld", format="json-ld")

    # Latest
    shutil.copy2(f"hefquin-vocabs/{name}.ttl", f"{latest_target}{name}.ttl")
    g.serialize(destination=f"{latest_target}{name}.rdf", format="xml")
    g.serialize(destination=f"{latest_target}{name}.jsonld", format="json-ld")


def create_documentation(name, version):
    """
    Generate LODE-style HTML documentation for the vocabulary.

    Parameters:
        name (str): Vocabulary name.
        version (str): Vocabulary version identifier.
    """
    logger.info("Generating vocabulary documentation")

    logger.info("Generating documentation for '%s'", name)
    versioned_target = f"docs/vocab/{name}/{version}/index.html"
    latest_target = f"docs/vocab/{name}/latest/index.html"

    source = f"hefquin-vocabs/{name}.ttl"

    od = VocPub(ontology=source)
    html = od.make_html()

    with open(versioned_target, "w", encoding="utf8") as f:
        f.write(html)

    with open(latest_target, "w", encoding="utf8") as f:
        f.write(html)


def main():
    """
    Iterates over a set of vocabularies. If they have been updated
    ig then:
    - Copies and serializes them
    - Generates HTML documentation using pyLODE.
    """
    vocabs = { "engineconf" : "0.0.1",
               "lpg2rdfconf" : "0.0.1",
               "feddesc" : "0.0.1" }

    for name, version in vocabs.items():
        source = f"hefquin-vocabs/{name}.ttl"
        target = f"docs/vocab/{name}/{version}/{name}.ttl"
        # Compute hashes for both files
        if os.path.isfile(target) and hash_file(source) == hash_file(target):
            logger.info("No changes detected, skipping '%s'", name)
            continue

        copy_vocabularies(name, version)    
        create_documentation(name, version)


if __name__ == "__main__":
    main()
